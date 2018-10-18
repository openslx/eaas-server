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

package de.bwl.bwfla.common.services.guacplay.replay;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import de.bwl.bwfla.common.services.guacplay.GuacDefs.MetadataTag;
import de.bwl.bwfla.common.services.guacplay.io.Metadata;
import de.bwl.bwfla.common.services.guacplay.io.MetadataChunk;
import de.bwl.bwfla.common.services.guacplay.io.TraceFile;
import de.bwl.bwfla.common.services.guacplay.io.TraceFileReader;

public class IWDMetaData {
	private String uuid = null;
	private String title  = null;
	private String description = null;
	
	protected final Logger log	= Logger.getLogger(this.getClass().getName());
	
	public IWDMetaData()
	{
		// ws default constructor
	}
	
	public IWDMetaData(File trace) throws IOException
	{
		TraceFile tfile = new TraceFile(trace.toPath(), StandardCharsets.UTF_8);
		TraceFileReader treader = tfile.newBufferedReader();
		treader.prepare();
		
		Metadata metaData = tfile.getMetadata();
		MetadataChunk chunkInt = metaData.getChunk(MetadataTag.INTERNAL);
		MetadataChunk chunkPub = metaData.getChunk(MetadataTag.PUBLIC);
		uuid = chunkInt.get("id");
		title = chunkPub.get("title");
		description = chunkPub.get("description");
		
		log.info("got: " + uuid + ", " + title + ", " + description);
	}
	
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
}
