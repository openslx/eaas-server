package solutions.emulation.preservica.client;

import java.net.URI;
import java.util.HashMap;

import com.tessella.xip.v4.GenericMetadata;
import com.tessella.xip.v4.TypeDeliverableUnits;
import com.tessella.xip.v4.TypeManifestation;
import com.tessella.xip.v4.XIP;

import cz.jirutka.atom.jaxb.AtomLink;
import cz.jirutka.atom.jaxb.Entry;
import cz.jirutka.atom.jaxb.Feed;
import cz.jirutka.atom.jaxb.Namespaces;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import gov.loc.mets.Mets;
import solutions.emulation.preservica.client.SDBRestSession.SDBRestSessionException;
import solutions.emulation.preservica.client.SDBRestSession.XIP_REQ_TYPE;

public class Manifestation {
	
	private final String id;
	private HashMap<String, DigitalFileContent> content;
	private YaleMetsData mets = null;

	public Manifestation(SDBRestSession session, String id) throws SDBRestSessionException
	{
		this.id = id;
		TypeManifestation m = loadManifestation(session);
		try {
			loadFileContent(session, m);
		} catch (BWFLAException e) {
			throw new SDBRestSessionException(e.getMessage());
		}
	}
	
	public DigitalFileContent[] getDigitalFileContents()
	{
		return content.values().toArray(new DigitalFileContent[0]);
	}
	
	public boolean hasFileContent()
	{
		return (content.size() > 0);
	}
	
	private void loadFileContent(SDBRestSession session, TypeManifestation m) throws BWFLAException {
		content = new HashMap<String,DigitalFileContent>();
		for (GenericMetadata md : m.getMetadata()) {

			if(md.getSchemaURI().equalsIgnoreCase(Namespaces.ATOM_NS))
			{
				Feed<AtomLink> feed = session.getAtomMetadata(md);
				if (feed == null) {
					continue;
				}

				for (Entry<AtomLink> fe : feed.getEntries()) {
					for (AtomLink l : fe.getLinks()) {
						if (l.getRel().equals("digital-file-content")) {
							content.put(fe.getId(), new DigitalFileContent(fe));
							break;
						}
					}
				}
			}
			else if(md.getSchemaURI().equalsIgnoreCase(Mets.NAMESPACE)) {
				mets = session.getMETSMetadata(md);
			}
		}
	}
	
	private TypeManifestation loadManifestation(SDBRestSession session) throws SDBRestSessionException 
	{
		XIP xip = session.getXIP(XIP_REQ_TYPE.MANIFESTATIONS, id);
		if (xip == null) 
			throw new SDBRestSessionException("XIP for manifestation " + id + " is null.");

		TypeDeliverableUnits units = xip.getDeliverableUnits();
		if (units == null) 
			throw new SDBRestSessionException("manifestation not found. id: " + id);
		

		for (TypeManifestation m : units.getManifestation()) {
			if (m.getManifestationRef().equals(id))
				return m;
		}
		return null;
	}

	public YaleMetsData getMets() {
		return mets;
	}
	
	public static class DigitalFileContent
	{
		private String summary;
		private URI fileContentUrl;
		private String id;
		private String title;
			
		public DigitalFileContent(Entry<AtomLink> fe)
		{
			summary = fe.getSummary();
			id = fe.getId();
			
			for (AtomLink l : fe.getLinks()) {
				if (l.getRel().equals("digital-file-content")) 
					fileContentUrl = l.getHref();
			}
			title = fe.getTitle();

			// System.out.println("Digital file content :" + toString());
		}

		public String getSummary() {
			return summary;
		}

		public URI getFileContentUrl() {
			return fileContentUrl;
		}
		
		public String getId() {
			return id;
		}
		
		public String getTitle() {
			return title;
		}

		public String toString()
		{
			return "DigitalFileContent:: id:" + id + "::title:" +title+ "::summary:" +summary + "::url:" + fileContentUrl + "\n";
		}
	}
}
