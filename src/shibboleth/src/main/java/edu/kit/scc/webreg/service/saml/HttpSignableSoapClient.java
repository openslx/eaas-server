package edu.kit.scc.webreg.service.saml;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.nio.charset.Charset;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.opensaml.ws.soap.client.SOAPClientException;
import org.opensaml.ws.soap.client.http.HttpSOAPClient;
import org.opensaml.ws.soap.soap11.Envelope;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.parse.ParserPool;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureException;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.util.XMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

public class HttpSignableSoapClient extends HttpSOAPClient implements Serializable {

	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.getLogger(HttpSignableSoapClient.class);
	
	private Signature signature;
	
    public HttpSignableSoapClient(HttpClient client, ParserPool parser, Signature signature) {
		super(client, parser);
		this.signature = signature;
	}

    @Override
	protected RequestEntity createRequestEntity(Envelope message, Charset charset) throws SOAPClientException {
        try {
            Marshaller marshaller = Configuration.getMarshallerFactory().getMarshaller(message);
            ByteArrayOutputStream arrayOut = new ByteArrayOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(arrayOut, charset);

            Element element = marshaller.marshall(message);
            try {
				Signer.signObject(signature);
			} catch (SignatureException e) {
				throw new SOAPClientException(e);
			}
            
            if (logger.isDebugEnabled()) {
                logger.debug("Outbound SOAP message is:\n" + XMLHelper.prettyPrintXML(element));
            }
            XMLHelper.writeNode(element, writer);
            return new ByteArrayRequestEntity(arrayOut.toByteArray(), "text/xml");
        } catch (MarshallingException e) {
            throw new SOAPClientException("Unable to marshall SOAP envelope", e);
        }
    }	
}
