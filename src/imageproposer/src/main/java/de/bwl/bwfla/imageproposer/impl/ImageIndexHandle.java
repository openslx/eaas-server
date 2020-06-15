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
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import de.bwl.bwfla.common.datatypes.identification.OperatingSystemInformation;
import de.bwl.bwfla.common.datatypes.identification.OperatingSystems;
import de.bwl.bwfla.common.taskmanager.AbstractTask;
import de.bwl.bwfla.emucomp.api.Environment;
import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.inject.api.Config;

import de.bwl.bwfla.common.datatypes.SoftwarePackage;
import de.bwl.bwfla.emucomp.api.MachineConfiguration;
import de.bwl.bwfla.imagearchive.util.EnvironmentsAdapter;
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
	@Config(value="ws.imagearchive")
	private String imageArchive;
	
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
		this.rebuildInterval = rebuildInterval;
		this.builder = new ImageIndexBuilder(imageArchive, softwareArchive);
		this.builder.loadDefaultsFromResource("ImagesMetaDataDefaults.json");
		this.index = builder.build();
		System.out.println("building from init");
	}

	public String refreshAsTask()
	{
		return taskmgr.submitTask(new ImageIndexBuilderTask());
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


	private class ImageIndexBuilderTask extends AbstractTask<Object>
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

		private final EnvironmentsAdapter envHelper;
		private final SoftwareArchiveHelper swHelper;
		private final HashMap<String, OperatingSystemInformation> operatingSystems;

		public ImageIndexBuilder(String imageArchive, String softwareArchive)
		{
			this.envHelper = new EnvironmentsAdapter(imageArchive);
			this.swHelper = new SoftwareArchiveHelper(softwareArchive);
			this.operatingSystems = new HashMap<>();
		}

		public ImageIndex build()
		{
			try {
				ImageIndex index = new ImageIndex(operatingSystems);
				List<Environment> environments = envHelper.getEnvironments(null);
				if (environments == null || environments.isEmpty()) {
					log.info("No environments found! Skip image index rebuilding.");
					return index;  // Nothing to do!
				}

				for (Environment environment : environments) {
					if (!(environment instanceof MachineConfiguration))
						continue;

					final MachineConfiguration config = (MachineConfiguration) environment;
					final String image = environment.getId();
					
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
				log.warning("Image index rebuilding for ImageProposer service failed!");
				log.log(Level.WARNING, exception.getMessage(), exception);
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
	}
}
