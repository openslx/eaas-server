package edu.kit.scc.webreg.service.saml.impl;


import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.binding.BasicSAMLMessageContext;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.binding.encoding.HTTPRedirectDeflateEncoder;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.SingleSignOnService;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.opensaml.ws.transport.http.HttpServletResponseAdapter;

import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.service.saml.MetadataHelper;
import edu.kit.scc.webreg.service.saml.Saml2RedirectService;
import edu.kit.scc.webreg.service.saml.SamlHelper;
import edu.kit.scc.webreg.service.saml.SsoHelper;

@Stateless
public class Saml2RedirectServiceImpl implements Saml2RedirectService {

	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	@Inject
	private SamlHelper samlHelper;

	@Inject
	private MetadataHelper metadataHelper;
	
	@Inject
	private SsoHelper ssoHelper;
	
	@Override
	public void redirectClient(SamlIdpMetadataEntity idpEntity,
			SamlSpConfigurationEntity spEntity, HttpServletResponse response) 
					throws MessageEncodingException {

		EntityDescriptor entityDesc = samlHelper.unmarshal(
				idpEntity.getEntityDescriptor(), EntityDescriptor.class);
		SingleSignOnService sso = metadataHelper.getSSO(entityDesc, SAMLConstants.SAML2_REDIRECT_BINDING_URI);

		AuthnRequest authnRequest = ssoHelper.buildAuthnRequest(
				spEntity.getEntityId(), spEntity.getAcs(), SAMLConstants.SAML2_POST_BINDING_URI);

		logger.debug("Sending client to idp {} endpoint {}" + idpEntity.getEntityId() + sso.getLocation());
		
		HTTPRedirectDeflateEncoder encoder = new HTTPRedirectDeflateEncoder();
		BasicSAMLMessageContext<SAMLObject, AuthnRequest, NameID> messageContext = 
				new BasicSAMLMessageContext<SAMLObject, AuthnRequest, NameID>();
		messageContext.setOutboundSAMLMessage(authnRequest);
		messageContext.setPeerEntityEndpoint(sso);
		messageContext.setOutboundMessageTransport(new HttpServletResponseAdapter(response, true));
		encoder.encode(messageContext);
		
	}

}
