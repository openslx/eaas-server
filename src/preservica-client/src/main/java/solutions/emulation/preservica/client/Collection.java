package solutions.emulation.preservica.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.tessella.xip.v4.GenericMetadata;
import com.tessella.xip.v4.TypeCollection;
import com.tessella.xip.v4.TypeCollections;
import com.tessella.xip.v4.XIP;

import cz.jirutka.atom.jaxb.AtomLink;
import cz.jirutka.atom.jaxb.Entry;
import cz.jirutka.atom.jaxb.Feed;
import solutions.emulation.preservica.client.SDBRestSession.SDBRestSessionException;
import solutions.emulation.preservica.client.SDBRestSession.XIP_REQ_TYPE;

import javax.ws.rs.Consumes;

public class Collection {

	private final String id;
	protected final Logger log = Logger.getLogger("preservica client");
	ConcurrentHashMap<String, DeliverableUnit> units;
	
	private ExecutorService pool;
	
	public Collection(SDBRestSession session, String id) throws SDBRestSessionException
	{
		this.id = id;
		pool = Executors.newFixedThreadPool(128);
		load(session);
	}
	
	public void sync(SDBRestSession session) throws SDBRestSessionException {
		// load(session);
	}

	synchronized public void sync(SDBRestSession session, List<String> objectIDs) throws SDBRestSessionException {
		ConcurrentHashMap<String, DeliverableUnit> _units = new ConcurrentHashMap<String, DeliverableUnit>();
		for (String objectId : objectIDs) {
			pool.execute(new UnitLoader(_units, log, objectId, session));
		}
		pool.shutdown();
		try {
			pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			pool = Executors.newFixedThreadPool(128);
			units.putAll(_units);
		} catch (InterruptedException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	public DeliverableUnit getDeliverableUnit(String id)
	{
		return units.get(id);
	}
	
	public List<String> getDeliverableUnits()
	{
		return new ArrayList<String>(units.keySet());
	}
	
	private TypeCollection getCollectionById(SDBRestSession session) throws SDBRestSessionException {
		log.warning("loading collection");
		XIP xip = session.getXIP(XIP_REQ_TYPE.COLLECTIONS, id);
		if (xip == null) 
			throw new SDBRestSessionException("XIP for collection " + id + " is null.");

		TypeCollections tCollections = xip.getCollections();
		if (tCollections == null) 
			throw new SDBRestSessionException("collection not found. id: " + id);

		for (TypeCollection c : tCollections.getCollection()) {
			if (c.getCollectionRef().equals(id)) {
				return c;
			}
		}
		throw new SDBRestSessionException("collection not found. id: " + id);
	}

	private void load(SDBRestSession session) throws SDBRestSessionException
	{
		ConcurrentHashMap<String, DeliverableUnit> _units = new ConcurrentHashMap<String,DeliverableUnit>();
		TypeCollection tc = getCollectionById(session);
		
		for (GenericMetadata md : tc.getMetadata()) {
			Feed<AtomLink> feed = session.getAtomMetadata(md);
			if (feed == null) {
				continue;
			}

			for (Entry<AtomLink> fe : feed.getEntries()) {
				for (AtomLink l : fe.getLinks()) {
					if (l.getRel().equals("child-deliverable-unit")) {
						pool.execute(new UnitLoader(_units, log, fe.getId(), session));
						log.info("adding: " + fe.getId());

						break;
					} else {
						log.info("skipping : " + fe.getId());
					}
				}
			}
		}
		pool.shutdown();
		try {
			pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			pool = Executors.newFixedThreadPool(64);
			units = _units;
		} catch (InterruptedException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	private static class UnitLoader implements Runnable 
	{
		private final Logger log;
		private final ConcurrentHashMap<String, DeliverableUnit> outputs;
		private String id;
		private SDBRestSession session;
		
		UnitLoader(ConcurrentHashMap<String, DeliverableUnit> outputs, Logger log,
				String id, SDBRestSession session) {
			
			this.log = log;
			this.outputs = outputs;
			this.id = id;
			this.session = session;
		}
		
		@Override
		public void run() {
			log.warning("Worker task for unit " + id + " started.");
			try {
				outputs.put(id, new DeliverableUnit(session, id));
			} catch (SDBRestSessionException e) {
				// TODO Auto-generated catch block
				// log.info("Worker task " + id + " failed.");
				log.log(Level.WARNING, e.getMessage(), e);
			}
			String status = "Worker task " + id + " stopped. ";
			// log.info(status);
		}
	}
}
