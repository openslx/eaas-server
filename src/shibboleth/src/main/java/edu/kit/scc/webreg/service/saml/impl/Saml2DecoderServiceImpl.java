package edu.kit.scc.webreg.service.saml.impl;

import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;

import org.opensaml.common.SAMLObject;
import org.opensaml.common.binding.BasicSAMLMessageContext;
import org.opensaml.saml2.binding.decoding.HTTPPostDecoder;
import org.opensaml.saml2.core.Response;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.xml.security.SecurityException;

import edu.kit.scc.webreg.exc.SamlAuthenticationException;
import edu.kit.scc.webreg.service.saml.Saml2DecoderService;

@Stateless
public class Saml2DecoderServiceImpl implements Saml2DecoderService {

	@Override
	public Response decodePostMessage(HttpServletRequest request) throws MessageDecodingException, SecurityException,
			SamlAuthenticationException {

		HTTPPostDecoder decoder = new HTTPPostDecoder();
		BasicSAMLMessageContext<SAMLObject, SAMLObject, SAMLObject> messageContext = new BasicSAMLMessageContext<SAMLObject, SAMLObject, SAMLObject>();
		HttpServletRequestAdapter adapter = new HttpServletRequestAdapter(request);
		messageContext.setInboundMessageTransport(adapter);
		decoder.decode(messageContext);
		SAMLObject obj = messageContext.getInboundSAMLMessage();
		if (obj instanceof Response)
			return (Response) obj;
		else
			throw new SamlAuthenticationException("Not a valid SAML2 Post Response");
	}

}
