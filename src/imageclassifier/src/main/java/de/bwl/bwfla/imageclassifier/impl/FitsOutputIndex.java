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

package de.bwl.bwfla.imageclassifier.impl;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import de.bwl.bwfla.imageclassifier.client.ClassificationEntry;
import de.bwl.bwfla.imageclassifier.client.HistogramEntry;
import de.bwl.bwfla.imageclassifier.datatypes.IdentificationOutputIndex;
import de.bwl.bwfla.wikidata.reader.QIDsFinder;
import de.bwl.bwfla.wikidata.reader.entities.SoftwareQIDs;
import edu.harvard.hul.ois.fits.FitsMetadataElement;
import edu.harvard.hul.ois.fits.FitsOutput;
import edu.harvard.hul.ois.fits.identity.ExternalIdentifier;
import edu.harvard.hul.ois.fits.identity.FitsIdentity;


public class FitsOutputIndex extends IdentificationOutputIndex<FitsOutput>
{
	public FitsOutputIndex(int i) {
		super(i);
	}

	/** Add specified output to this index. */
	@Override
	public void add(FitsOutput output)
	{
		List<FitsIdentity> identities = output.getIdentities();
		for (FitsIdentity identity : identities) {
			this.add(mimetypes, identity.getMimetype(), output);

			// Save also the external identifiers, when available
			for (ExternalIdentifier extid : identity.getExternalIdentifiers())
				this.add(exttypes, extid.getValue(), output);
		}

		if (identities.isEmpty())
			unclassified.add(output);
	}

	@Override
	public List<ClassificationEntry> getClassifierList(Map<String, String> policy) {
		List<ClassificationEntry> entries = new ArrayList<ClassificationEntry>();
		final String defaultValue = policy.get("default");
		final String replacement = ".";

		for (Map.Entry<String, List<FitsOutput>> entry : exttypes.entrySet()) {
			final String type = entry.getKey();
			String value = policy.get(type);
			if (value == null)
				value = defaultValue;
			SoftwareQIDs softwareQIDs = QIDsFinder.findQIDs(type);

			try {
				addEntry(entries, type, FILETYPE_NAMES.get(type), value, entry.getValue(),
						softwareQIDs.getReadQIDs(), softwareQIDs.getWriteQIDs());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (unclassified.size() > 0) {
			final String type = "unknown";
			final String value = policy.get(type);

			SoftwareQIDs softwareQIDs = QIDsFinder.findQIDs(type);
			try {
				addEntry(entries, type, value, FILETYPE_NAMES.get(type),
                        unclassified, softwareQIDs.getReadQIDs(), softwareQIDs.getWriteQIDs());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return entries;
	}

	@Override
	public List<HistogramEntry> getSummaryClassifierList(Map<String, String> policy) {
		List<HistogramEntry> entries = new ArrayList<HistogramEntry>();
		final String defaultValue = policy.get("default");

		for (Map.Entry<String, List<FitsOutput>> entry : exttypes.entrySet()) {
			final String type = entry.getKey();
			final int count = entry.getValue().size();
			String value = policy.get(type);
			if (value == null)
				value = defaultValue;

			entries.add(new HistogramEntry(type, count, value));
		}

		if (unclassified.size() > 0) {
			final String type = "unknown";
			final String value = policy.get(type);
			entries.add(new HistogramEntry(type, unclassified.size(), value));
		}
		return entries;
	}

	/** Print the index to log. */
	public void print(Logger log, boolean printExtTypes)
	{
		log.info("Mimetype-Histogram:");
		
		for (Map.Entry<String, List<FitsOutput>> entry : mimetypes.entrySet())
			log.info(entry.getKey() + " " + entry.getValue().size());
		
		if (!printExtTypes)
			return;
		
		log.info("ExtType-Histogram:");
		
		for (Map.Entry<String, List<FitsOutput>> entry : exttypes.entrySet())
			log.info(entry.getKey() + " " + entry.getValue().size());
		
		log.info("Unclassified " + unclassified.size());
		for (FitsOutput output : unclassified) {
			List<FitsMetadataElement> m = output.getFileInfoElements();
			for (FitsMetadataElement e : m) {
				log.info(e.getName() + " : " + e.getValue());
				if (e.getName().equals("fslastmodified")) {
					long uts = Long.parseLong(e.getValue());
					Date ts = new Date(uts);
					log.info("year " + (ts.getYear() + 1900));
				}
			}
			
			log.info("--");
		}
	}
	
	/** Generate and write the report to specified path. */
	public void dump(Path outpath) throws IOException
	{
		BufferedWriter writer = Files.newBufferedWriter(outpath, StandardCharsets.UTF_8);
		
		FitsOutputIndex.writeHistogram(writer, mimetypes, "Mimetype-Histogram");
		FitsOutputIndex.writeHistogram(writer, exttypes, "ExtType-Histogram");
		
		writer.write("==================== Detailed Report ====================");
		writer.newLine();
		writer.newLine();
		
		FitsOutputIndex.writeFilePaths(writer, mimetypes);
		FitsOutputIndex.writeFilePaths(writer, exttypes);
		
		writer.flush();
		writer.close();
	}
	
	
	/* ==================== Internal Helpers ==================== */
	

	private static void writeHistogram(BufferedWriter writer, Map<String, List<FitsOutput>> map, String title) throws IOException
	{
		writer.write(title + ":");
		writer.newLine();
		
		for (Map.Entry<String, List<FitsOutput>> entry : map.entrySet()) {
			writer.write("\t" + entry.getKey() + "  " + entry.getValue().size());
			writer.newLine();
		}
		
		writer.newLine();
	}
	
	private static void writeFilePaths(BufferedWriter writer, Map<String, List<FitsOutput>> map) throws IOException
	{
		for (Map.Entry<String, List<FitsOutput>> entry : map.entrySet()) {
			writer.write(entry.getKey() + ":");
			writer.newLine();
			
			for (FitsOutput output : entry.getValue()) {
				writer.write("\t" + output.getMetadataElement("filepath").getValue());
				writer.newLine();
			}
			
			writer.newLine();
		}
	}

	/* =============== Internal Helpers =============== */

	private static long getTime(FitsOutput output) {
		List<FitsMetadataElement> m = output.getFileInfoElements();
		for (FitsMetadataElement e : m) {
			// System.out.println(e.getName() + " : " + e.getValue());
			if (e.getName().equals("fslastmodified")) {
				return Long.parseLong(e.getValue());
			}
		}
		return Long.MAX_VALUE;
	}

	private String replacePathPrefix(FitsOutput output, String replacement) {
		final FitsMetadataElement element = output.getMetadataElement("filepath");
		String filename = element.getValue();
		for(String path : pathList) {
			if (!filename.startsWith(path))
				continue;
			final int offset = path.length();
			filename = replacement + filename.substring(offset);
			break;
		}
		return filename;
	}

	private static void addEntry(List<ClassificationEntry> entries, String type)
	{
		final List<String> files = new ArrayList<String>();
		entries.add(new ClassificationEntry(type, null, files));
	}

	private void addEntry(List<ClassificationEntry> entries, String type, String value, List<FitsOutput> outputs) throws IOException
	{
		final List<String> files = new ArrayList<String>(outputs.size());
		for (FitsOutput output : outputs)
			files.add(replacePathPrefix(output, "."));

		entries.add(new ClassificationEntry(type, value, files));
	}
	private void addEntry(List<ClassificationEntry> entries, String type, String nameType, String value,
						  List<FitsOutput> outputs, List<String> readQIDs, List<String> writeQID) throws IOException
	{
		long now = (new Date()).getTime();
		long fromDate = now;
		long toDate = 0;

		final List<String> files = new ArrayList<String>(outputs.size());
		for (FitsOutput output : outputs) {
			files.add(replacePathPrefix(output, "."));
			long time = getTime(output);
			if(time < fromDate)
				fromDate = time;

			if(time < now && time > toDate)
				toDate = time;
		}
		ClassificationEntry entry = new ClassificationEntry(type, value, files, readQIDs, writeQID, nameType);
		entry.setFromDate(fromDate);
		entry.setToDate(toDate);
		entries.add(entry);
	}
}
