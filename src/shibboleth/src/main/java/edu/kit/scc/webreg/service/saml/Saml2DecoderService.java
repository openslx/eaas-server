package edu.kit.scc.webreg.service.saml;

import javax.servlet.http.HttpServletRequest;

import org.opensaml.saml2.core.Response;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.xml.security.SecurityException;

import edu.kit.scc.webreg.exc.SamlAuthenticationException;

public interface Saml2DecoderService {

	public Response decodePostMessage(HttpServletRequest request)
			throws MessageDecodingException, SecurityException, SamlAuthenticationException;

}
