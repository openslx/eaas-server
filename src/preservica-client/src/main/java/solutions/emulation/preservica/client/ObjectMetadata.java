package solutions.emulation.preservica.client;

import com.tessella.xip.v4.ObjectFactory;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.MARC21Xml;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ObjectMetadata {
    private static String MARC_URL = "http://deleon.library.yale.edu:9090/VoySearch/GetBibMarc";

    static private Logger log = Logger.getLogger("ObjectMetadata");

    public ObjectMetadata()
    {

    }

    private static enum MarcFields {

        _245("245"),
        _700("700"),
        _260("260");

        private final String value;
        MarcFields(String value)
        {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }

    /*
	 * Helper function to deal with self-signed / invalid SSL certs
	 */
    private static DefaultHttpClient createAllTrustingClient() {
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

    // http://deleon.library.yale.edu:9090/VoySearch/GetBibMarc?bibid=3694053

    public static HashMap<String, String> getMarcMap(String bibId) throws BWFLAException {

        ApacheHttpClient4Engine engine = new ApacheHttpClient4Engine(createAllTrustingClient());
        Client client = new ResteasyClientBuilder().httpEngine(engine).connectionPoolSize(1).build();

        HashMap<String, String> result = new HashMap<>();

        WebTarget target = client.target(MARC_URL).queryParam("bibid", bibId);
        MARC21Xml marc = new MARC21Xml(target);

        for(MarcFields mf : MarcFields.values()) {
            HashMap<String, String> tag = marc.getTag(mf.value());
            if(tag == null)
                continue;

            // System.out.println("bibid: " + bibId + " got tag: " + mf.value);
            for (String key : tag.keySet())
                result.put(mf.value() + key, tag.get(key));

        }
        return result;
    }
}
