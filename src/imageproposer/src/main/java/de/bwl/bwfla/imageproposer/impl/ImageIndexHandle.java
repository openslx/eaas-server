/*
 * This file is part of the Emulation-as-a-Service framework.
 *
 * The Emulation-as-a-Service framework is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * The Emulation-as-a-Service framework is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Emulation-as-a-Software framework.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package de.bwl.bwfla.imageproposer.impl;

import java.io.*;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import com.openslx.eaas.common.databind.Streamable;
import com.openslx.eaas.imagearchive.ImageArchiveClient;
import de.bwl.bwfla.common.datatypes.identification.OperatingSystemInformation;
import de.bwl.bwfla.common.datatypes.identification.OperatingSystems;
import de.bwl.bwfla.common.taskmanager.BlockingTask;
import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.inject.api.Config;

import de.bwl.bwfla.common.datatypes.SoftwarePackage;
import de.bwl.bwfla.emucomp.api.MachineConfiguration;
import de.bwl.bwfla.softwarearchive.util.SoftwareArchiveHelper;


/** A handle for an image index */
@ApplicationScoped
public class ImageIndexHandle
{
	// private StopWatch stopwatch;
	@Inject
	@Config(value="imageproposer.rebuildinterval")
	private long  rebuildInterval;
	
	@Inject
	@Config(value="ws.softwarearchive")
	private String softwareArchive;

	private ImageIndexBuilder builder;
	private ImageIndex index;

	private boolean rebuildIsRunning = false;

	@Inject
	private TaskManager taskmgr;
	
	@PostConstruct
	public void init() {
		// this.stopwatch = new StopWatch();
		this.builder = new ImageIndexBuilder(softwareArchive);
		this.builder.loadDefaultsFromResource("ImagesMetaDataDefaults.json");
		this.index = builder.build();
	}

	public String refreshAsTask()
	{
		return taskmgr.submit(new ImageIndexBuilderTask());
	}

	/**
	 * Rebuilds the image index, only if it is older than rebuild interval.
	 * @return true when index was rebuilt, else false
	 */
	public boolean refresh()
	{
		return this.rebuild();
	}

	/**
	 * Rebuilds the image index, if it is older than rebuild interval.
	 * @return true when index was rebuilt, else false
	 */
	public boolean rebuild()
	{
		synchronized (this)
		{
			if(rebuildIsRunning)
				return false;

		//	if (stopwatch.timems() < rebuildInterval)
		//		return false;

			rebuildIsRunning = true;
		}

		ImageIndex newindex = builder.build();
		boolean rebuilt = (newindex != null);
		if (rebuilt)
			index = newindex;

		synchronized (this) {
		//	stopwatch.start();
			rebuildIsRunning = false;
		}

		return rebuilt;
	}
	
	/** Returns current image index instance. */
	public synchronized ImageIndex get()
	{
		return index;
	}
	
	/** Sets a new image index instance. */
	public synchronized void set(ImageIndex newindex)
	{
		index = newindex;
	}


	private class ImageIndexBuilderTask extends BlockingTask<Object>
	{
		@Override
		protected Object execute() throws Exception {
			return rebuild();
		}
	}

	/* =============== Internal Helpers =============== */
	
	private static class ImageIndexBuilder
	{
		private final Logger log = Logger.getLogger(ImageIndexBuilder.class.getName());

		private final ImageArchiveClient imagearchive;
		private final SoftwareArchiveHelper swHelper;
		private final HashMap<String, OperatingSystemInformation> operatingSystems;

		public ImageIndexBuilder(String softwareArchive)
		{
			try {
				this.imagearchive = ImageArchiveClient.create();
			}
			catch (Exception error) {
				throw new RuntimeException(error);
			}

			this.swHelper = new SoftwareArchiveHelper(softwareArchive);
			this.operatingSystems = new HashMap<>();
		}

		public ImageIndex build()
		{
			try (final var machines = this.fetch()) {
				ImageIndex index = new ImageIndex(operatingSystems);
				for (var iter = machines.iterator(); iter.hasNext();) {
					final MachineConfiguration config = iter.next();
					final String image = config.getId();
					
					// adding base fmts first
					String osId = config.getOperatingSystemId();
					if (osId != null) {
						OperatingSystemInformation operatingSystemInformation = operatingSystems.get(osId);
						if (operatingSystemInformation != null) {
							if(operatingSystemInformation.getPuids() != null) {
								for (String fmt : operatingSystemInformation.getPuids())
									index.addEnvironmentWithPUID(fmt, image);
							}
							if(operatingSystemInformation.getExtensions() != null)
							{
								for (String ext : operatingSystemInformation.getExtensions()) {
									// log.severe("adding ext " + ext + " with img " + image);
									index.addEnvironmentWithExt(ext, image);
								}
							}
						}
					}
					
					List<String> installedSoftware = config.getInstalledSoftwareIds();
					if (installedSoftware == null || installedSoftware.isEmpty())
						continue;
					
					for (String swid : installedSoftware) {
						SoftwarePackage software = swHelper.getSoftwarePackageById(swid);
						if (software == null)
							continue;
						List<String> formats = software.getSupportedFileFormats();
						if (formats == null || formats.isEmpty())
							continue;

						// Add a new index entry
						for (String format : formats)
							index.addEnvironmentWithPUID(format, image);
					}
				}

				log.info("Image index rebuilt. ");
				
				return index;
			}
			catch (Exception exception) {
				log.log(Level.WARNING, "Image index rebuilding for ImageProposer service failed!", exception);
			}
			
			return null;
		}
		
		private void loadDefaultsFromResource(String resource)
		{
			String serverDataDir = ConfigurationProvider.getConfiguration().get("commonconf.serverdatadir");
			File osInfo = new File(serverDataDir, "operating-systems.json");
			if(!osInfo.exists())
			{
				log.severe("no operating-systems.json found.");
				return;
			}

			byte[] encoded = new byte[0];
			try {
				encoded = Files.readAllBytes(osInfo.toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}

			String json = new String(encoded);

			OperatingSystems metaData = null;
			try {
				metaData = OperatingSystems.fromJsonValue("{ \"operatingSystems\" : " + json + "}", OperatingSystems.class);
			} catch (JAXBException e) {
				e.printStackTrace();
			}
			if (metaData.getOperatingSystemInformations() != null) {
				for (OperatingSystemInformation os : metaData.getOperatingSystemInformations()) {
					operatingSystems.put(os.getId(), os);
				}
			}
		}

		private Streamable<MachineConfiguration> fetch()
		{
			try {
				return imagearchive.api()
						.v2()
						.machines()
						.fetch();
			}
			catch (Exception error) {
				log.log(Level.WARNING, "Fetching machines failed!", error);
				return Streamable.of(Collections.emptyList());
			}
		}
	}
}
