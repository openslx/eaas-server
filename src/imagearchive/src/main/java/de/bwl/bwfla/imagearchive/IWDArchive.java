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

package de.bwl.bwfla.imagearchive;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import de.bwl.bwfla.common.services.guacplay.replay.IWDMetaData;
import de.bwl.bwfla.imagearchive.conf.ImageArchiveConfig;

public class IWDArchive {

	protected final Logger	log	= Logger.getLogger(this.getClass().getName());
	private ImageArchiveConfig iaConfig;
	
	public IWDArchive(ImageArchiveConfig conf)
	{
		iaConfig = conf;
	}
	
	protected List<IWDMetaData> getRecordings(String envId)
	{
		List<IWDMetaData> recs = new ArrayList<IWDMetaData>();
		File recordingsDir = iaConfig.recordingsPath;
		if(!recordingsDir.exists() || !recordingsDir.isDirectory())
		{
			log.severe("make sure that the repository directory exists and user has sufficient permissions: " 
					+ recordingsDir.getAbsolutePath());
			return recs;
		}

		File dir = new File(recordingsDir, envId);
		if(!dir.exists())
			return recs;

		for (final File fileEntry : dir.listFiles()) {
			if (!fileEntry.isDirectory()) {
				try {
					IWDMetaData d = new IWDMetaData(fileEntry);
					recs.add(d);
				} catch (Exception e) {
					log.severe("failed loading metadata for trace: " + fileEntry.getName() + " reason " + e.getMessage());
				}
			}
		}
		return recs;	
	}
	
	protected boolean addRecordingFile(String envId, String traceId, String data)
	{
		File recordingsDir = iaConfig.recordingsPath;
		if(!recordingsDir.exists() || !recordingsDir.isDirectory())
		{
			log.severe("make sure that the repository directory exists and user has sufficient permissions: " 
					+ recordingsDir.getAbsolutePath());
			return false;
		}

		File outPath = new File(recordingsDir, envId);
		if(!outPath.exists())
		{
			try {
				Files.createDirectory(outPath.toPath());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				log.severe("failed creating: " + outPath);
				return false;
			}
		}

		File outFile = new File(outPath, traceId);
		if(outFile.exists())
			outFile.delete();

		return DataUtil.writeString(data, outFile);

	}
	
	protected String getRecording(String envId, String traceId)
	{	
		File recordingsDir = iaConfig.recordingsPath;
		if(!recordingsDir.exists() || !recordingsDir.isDirectory())
		{
			log.severe("make sure that the repository directory exists and user has sufficient permissions: " 
					+ recordingsDir.getAbsolutePath());
			return null;
		}

		File dir = new File(recordingsDir, envId);
		if(!dir.exists())
			return null;

		File rec = new File(dir, traceId);
		if(!rec.exists())
			return null;

		try {
			return FileUtils.readFileToString(rec);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
}
