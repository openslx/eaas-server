package solutions.emulation.preservica.client;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import gov.loc.mets.Mets;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.jboss.resteasy.client.jaxrs.BasicAuthentication;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.w3c.dom.Element;

import com.tessella.xip.v4.GenericMetadata;
import com.tessella.xip.v4.XIP;

import cz.jirutka.atom.jaxb.AtomLink;
import cz.jirutka.atom.jaxb.Entry;
import cz.jirutka.atom.jaxb.Feed;
import cz.jirutka.atom.jaxb.Namespaces;


public class SDBRestSession {
	
	protected Client client;
	
	private SDBConfiguration config;
	
	protected final Logger log = Logger.getLogger("preservica client");
	
	public enum XIP_REQ_TYPE {
		MANIFESTATIONS,
		COLLECTIONS,
		DELIVERABLE_UNITS,
	}
	
	private void setupInsecureClient()
	{
		ApacheHttpClient4Engine engine = new ApacheHttpClient4Engine(createAllTrustingClient());
		this.client = new ResteasyClientBuilder().httpEngine(engine).connectionPoolSize(50).build();
	}
	
	public SDBRestSession(SDBConfiguration config)
	{
		this.config = config;
		setupInsecureClient();
	}
	
	public XIP getXIP(XIP_REQ_TYPE type, String id) {
		
		String restTypeReq = null;
		switch(type)
		{
		case MANIFESTATIONS:
			restTypeReq = "manifestations";
			break;
		case COLLECTIONS:
			restTypeReq = "collections";
			break;
		case DELIVERABLE_UNITS:
			restTypeReq = "deliverableUnits";
			break;
		default:
			return null;
		}
		
		final WebTarget target = client.target(config.getUrl() + config.getRestPath() + restTypeReq + "/");
		
		if(config.isRestricted())
			target.register(new BasicAuthentication(config.getUsername(), config.getPassword()));
		
		Response response = null;
		Builder restRequest = target.path(id).request();
		response = restRequest.get();
		XIP xip = null;
		switch (Status.fromStatusCode(response.getStatus())) {
		case OK:
			xip = response.readEntity(XIP.class);
			break;

		default:
			log.warning("getXIP: got bad return status: " + response.getStatus()  + " for ID " + id);
		}
		response.close();
		return xip;

	}
	
	public void printMetadata(GenericMetadata md) {
		Feed<AtomLink> feed = getAtomMetadata(md);
		if (feed == null) {
			log.info("printMetadata: failed parsing feed");
			return;
		}

		for (Entry<AtomLink> fe : feed.getEntries()) {
			log.info("MD:title " + fe.getTitle());
			for (AtomLink l : fe.getLinks()) {
				log.info(l.getHref().toString());
				log.info(l.getRel());
			}
			log.info("---");
		}
	}

	public YaleMetsData getMETSMetadata(GenericMetadata md) throws BWFLAException {
		if (md == null)
			return null;

		if (!md.getSchemaURI().equalsIgnoreCase(Mets.NAMESPACE)) {
			return null;
		}
		Element e = md.getAny();
		return new YaleMetsData(e);

	}
	
	public Feed<AtomLink> getAtomMetadata(GenericMetadata md) {
		if (md == null)
			return null;

		if (!md.getSchemaURI().equalsIgnoreCase(Namespaces.ATOM_NS)) {

			// log.severe("unknown NAMESPACE: " + md.getSchemaURI());
			return null;
		}

		Element e = md.getAny();
		// log.info(e.getTagName());

		try {
			JAXBContext jc = JAXBContext.newInstance(Feed.class);
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			Feed<AtomLink> result = (Feed<AtomLink>) unmarshaller.unmarshal(e);
			return result;

		} catch (Throwable t) {
			throw new IllegalArgumentException(
					"passed 'data' metadata cannot be parsed by 'JAX-B', check input contents");
		}
	}
	
	
	/*
	 * Helper function to deal with self-signed / invalid SSL certs
	 */
	private DefaultHttpClient createAllTrustingClient() {
		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));

		TrustStrategy trustStrategy = new TrustStrategy() {

			@Override
			public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				// LOG.info("Is trusted? return true");
				return true;
			}
		};

		SSLSocketFactory factory = null;
		try {
			factory = new SSLSocketFactory(trustStrategy, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		} catch (KeyManagementException | UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException e) {
			// TODO Auto-generated catch block
			log.log(Level.SEVERE, e.getMessage(), e);
		}
		registry.register(new Scheme("https", 443, factory));

		ThreadSafeClientConnManager mgr = new ThreadSafeClientConnManager(registry);
		mgr.setMaxTotal(1000);
		mgr.setDefaultMaxPerRoute(1000);

		DefaultHttpClient client = new DefaultHttpClient(mgr, new DefaultHttpClient().getParams());
		return client;
	}
	
	public static class SDBRestSessionException extends Exception
	{
		public SDBRestSessionException(String message, Throwable cause)
		{
			super(message, cause);
		}
		
		public SDBRestSessionException(String message)
		{
			super(message);
		}
	}

}
