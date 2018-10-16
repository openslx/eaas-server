package solutions.emulation.preservica.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.tessella.xip.v4.GenericMetadata;
import com.tessella.xip.v4.TypeDeliverableUnit;
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

public class DeliverableUnit {

	private final String id;
	private HashMap<String, Manifestation> manifestations;
	protected final Logger log = Logger.getLogger("preservica client: deliverable unit parser");
	private String title;
	private String description;
	private String summary;
	private HashMap<String, String> marc21;
	private YaleMetsData mets = null;

	DeliverableUnit(SDBRestSession session, String id) throws SDBRestSessionException {
		this.id = id;
		load(session);
		try {
			marc21 = ObjectMetadata.getMarcMap(title);
		} catch (BWFLAException e) {
			throw new SDBRestSessionException(e.getMessage());
		}
	}

	public HashMap<String, String> getMarc21() {
		return marc21;
	}

	public Manifestation[] getManifestations()
	{
		return  manifestations.values().toArray(new Manifestation[0]);
	}
	
	public String getTitle()
	{
		return title;
	}

	private TypeDeliverableUnit loadUnit(SDBRestSession session, String unitId) throws SDBRestSessionException {
		XIP xip = session.getXIP(XIP_REQ_TYPE.DELIVERABLE_UNITS, unitId);
		if (xip == null)
			throw new SDBRestSessionException("XIP for unit " + unitId + " is null.");

		TypeDeliverableUnits units = xip.getDeliverableUnits();
		if (units == null)
			throw new SDBRestSessionException("unit not found. id: " + unitId);

		for (TypeDeliverableUnit u : units.getDeliverableUnit()) {
			if (u.getDeliverableUnitRef().equals(unitId)) {
				return u;
			}
		}
		return null;
	}

	private void loadManifestations(SDBRestSession session, TypeDeliverableUnit unit) throws SDBRestSessionException, BWFLAException {
		for (GenericMetadata md : unit.getMetadata()) {


			if(md.getSchemaURI().equalsIgnoreCase(Namespaces.ATOM_NS))
			{
				Feed<AtomLink> feed = session.getAtomMetadata(md);
				if (feed == null) {
					continue;
				}

				for (Entry<AtomLink> fe : feed.getEntries()) {
					for (AtomLink l : fe.getLinks()) {
						if (l.getRel().equals("manifestation")) {
							try {
								Manifestation m = new Manifestation(session, fe.getId());
								if(m.hasFileContent())
									manifestations.put(fe.getId(), m);
							} catch (SDBRestSessionException e) {
								log.log(Level.WARNING, e.getMessage(), e);
							}
						} else if (l.getRel().equals("child-deliverable-unit")) {
							try {
								TypeDeliverableUnit u = loadUnit(session, fe.getId());
								loadManifestations(session, u);
							} catch (SDBRestSessionException e){
								log.log(Level.WARNING, e.getMessage(), e);
							}
						} else {
							log.info("skipping : " + fe.getId());
						}
					}
				}
			}
			else if(md.getSchemaURI().equalsIgnoreCase(Mets.NAMESPACE)) {
				mets = session.getMETSMetadata(md);
			}
		}
	}

	private void load(SDBRestSession session) throws SDBRestSessionException {
		manifestations = new HashMap<String, Manifestation>();
		TypeDeliverableUnit unit = loadUnit(session, id);
		try {
			loadManifestations(session, unit);
		} catch (BWFLAException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			throw new SDBRestSessionException(e.getMessage());
		}
		title = unit.getCatalogueReference();
		description = unit.getTitle();
		summary = unit.getScopeAndContent();

		if(mets != null)
		{
			String wikiId = mets.getWikiId();
			if(wikiId != null)
				summary += "<br><p><a href=\"http://www.wikidata.org/wiki/"+ wikiId + "\" target=\"_blank\">Wikidata Page</a></p>";
		}
		else
			log.info("METS not available.");
	}

	public YaleMetsData getMets() {
		return mets;
	}

	public String getDescription() {
		return description;
	}

	public String getSummary() {
		return summary;
	}
}
