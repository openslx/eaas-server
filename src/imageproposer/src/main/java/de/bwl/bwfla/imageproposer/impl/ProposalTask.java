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

import java.util.*;

import de.bwl.bwfla.common.datatypes.identification.DiskType;
import de.bwl.bwfla.common.datatypes.identification.OperatingSystemInformation;
import de.bwl.bwfla.common.taskmanager.AbstractTask;
import de.bwl.bwfla.imageproposer.client.Proposal;
import de.bwl.bwfla.imageproposer.client.ProposalRequest;

import javax.xml.bind.JAXBException;


public class ProposalTask extends AbstractTask<Object>
{
	private final ProposalRequest request;
	private final ImageIndexHandle indexHandle;
	private final ImageSorter sorter;
	
	public ProposalTask(ProposalRequest request, ImageIndexHandle index, ImageSorter sorter)
	{
		this.request = request;
		this.indexHandle = index;
		this.sorter = sorter;
	}

	private Set<String> getExtensions() throws JAXBException {
		Set<String> extensions = new HashSet<>();
		for(String key : request.getMediaFormats().keySet()) {
			if (request.getMediaFormats().get(key) != null) {
				DiskType diskType = request.getMediaFormats().get(key);
				log.info(diskType.JSONvalue(true));

				String fileName = diskType.getLocalAlias();
				if(fileName == null)
					continue;

				int index = fileName.lastIndexOf('.');
				if(index < 0)
					continue;
				String ext = fileName.substring(index + 1);
				extensions.add(ext.trim().toLowerCase());
				log.info("found extension: " + ext);
			}
		}

		return extensions;
	}

	private void proposeByExtension(ImageIndex index,
									Collection<String> images,
									Map<String, String> missing) throws JAXBException {
		int maxCount = 0;
		HashMap<String, Integer> resultMap = new HashMap<>(); // count hits per environment

		log.info("using file extensions...");

		Set<String> extensions = getExtensions();
		if(getExtensions().isEmpty())
			return;

		for (String ext : extensions) {
			Set<String> envIds = index.getEnvironmentsByExt(ext);
			if (envIds != null) {
				for (String envId : envIds) {
					Integer count = resultMap.get(envId);
					if (count == null)
						count = 0;
					count += 1;
					if (count > maxCount)
						maxCount = count;
					resultMap.put(envId, count);
				}
			}

			Set<String> os = index.getOsRequirementByExt(ext);
			if (os != null) {
				for (String osId : os) {
					OperatingSystemInformation operatingSystemInformation = index.getOperatingSystemByPUID(osId);
					if(operatingSystemInformation != null)
						missing.put(operatingSystemInformation.getId(), operatingSystemInformation.getLabel());
				}
			}
		}

		log.info("propose: maxCount " + maxCount);
		for(String proposedEnv : resultMap.keySet())
		{
			Integer count = resultMap.get(proposedEnv);
			if(count != maxCount)
				continue;

			images.add(proposedEnv);
		}
	}

	private void proposeByPUID(ImageIndex index,
								  Collection<String> images,
								  Map<String, String> missing)
	{
		int maxCount = 0;
		HashMap<String, Integer> resultMap = new HashMap<>(); // count hits per environment
		log.info("Running propose algorithm...");

		for(String key : request.getFileFormats().keySet()) {
			for (ProposalRequest.Entry entry : request.getFileFormats().get(key)) {
				Set<String> envIds = index.getEnvironmentsByPUID(entry.getType());
				if (envIds != null) {
					for (String envId : envIds) {
						Integer count = resultMap.get(envId);
						if (count == null)
							count = 0;
						count += 1;
						if (count > maxCount)
							maxCount = count;
						resultMap.put(envId, count);
					}
				}

				Set<String> os = index.getOsRequirementByPUID(entry.getType());
				if (os != null) {
					for (String osId : os) {
						OperatingSystemInformation operatingSystemInformation = index.getOperatingSystemByPUID(osId);
						if(operatingSystemInformation != null)
							missing.put(operatingSystemInformation.getId(), operatingSystemInformation.getLabel());
					}
				}
			}
		}

		log.info("propose: maxCount " + maxCount);
		for(String proposedEnv : resultMap.keySet())
		{
			Integer count = resultMap.get(proposedEnv);
			if(count != maxCount)
				continue;

			images.add(proposedEnv);
		}
	}

	@Override
	public Proposal execute() throws Exception
	{
		// Update the index, if needed!
		// indexHandle.refresh();

		Collection<String> images = new HashSet<String>();
		final Map<String, String> missing = new HashMap<>();
		final ImageIndex index = indexHandle.get();
//		int numMissingFormats = 0;
//		int numFoundFormats = 0;

		proposeByPUID(index, images, missing);
		if(images.isEmpty() && missing.isEmpty())
		{
			proposeByExtension(index, images, missing);
		}

		images = sorter.sort(images);
		
//		log.info("Propose algorithm finished! " + images.size() + " suitable image(s) found.");
//		if (numMissingFormats > 0) {
//			final int numFormats = numFoundFormats + numMissingFormats;
//			log.info("No suitable images found for " + numMissingFormats + " of " + numFormats + " format(s).");
//		}
		
		return new Proposal(images, missing);
	}

	//	private  Drive.DriveType getMediaType(DiskType type)
//	{
//		Set<String> wikidataSet = new HashSet<>();
//
//		if(type.getContent() == null)
//			return null;
//
//		for (Content c : type.getContent())
//		{
//			if(c.getWikidata() != null)
//				wikidataSet.addEnvironmentWithPUID(c.getWikidata());
//			log.info("found " + c.getType() + "(" + c.getWikidata()+ ")");
//		}
//
//		if(wikidataSet.contains("Q3063042")) {
//			log.info("found floppy");
//			return Drive.DriveType.FLOPPY;
//		}
//
//		if(wikidataSet.contains("Q55336682")) {
//			log.info("found iso");
//			return Drive.DriveType.CDROM;
//		}
//		return null;
//	}
}
