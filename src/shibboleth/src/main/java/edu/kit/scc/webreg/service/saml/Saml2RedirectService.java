package edu.kit.scc.webreg.service.saml;

import javax.servlet.http.HttpServletResponse;

import org.opensaml.ws.message.encoder.MessageEncodingException;

import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;

public interface Saml2RedirectService {

	public void redirectClient(SamlIdpMetadataEntity idpEntity,
			SamlSpConfigurationEntity spEntity, HttpServletResponse response)
			throws MessageEncodingException;

}
