package edu.kit.scc.webreg.service.saml;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.DateTime;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameIDPolicy;

@Named("ssoHelper")
@ApplicationScoped
public class SsoHelper implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private SamlHelper samlHelper;
	
	public AuthnRequest buildAuthnRequest(String spEntityId, String acs, String binding) {
		
		AuthnRequest authnRequest = samlHelper.create(AuthnRequest.class, AuthnRequest.DEFAULT_ELEMENT_NAME);
		authnRequest.setID(samlHelper.getRandomId());
		authnRequest.setVersion(SAMLVersion.VERSION_20);
		authnRequest.setIssueInstant(new DateTime());
		authnRequest.setForceAuthn(false);
		authnRequest.setIsPassive(false);
		authnRequest.setProtocolBinding(binding);
		authnRequest.setAssertionConsumerServiceURL(acs);
		
		Issuer issuer = samlHelper.create(Issuer.class, Issuer.DEFAULT_ELEMENT_NAME);
		issuer.setValue(spEntityId);
		authnRequest.setIssuer(issuer);
		
		NameIDPolicy nameIdPolicy = samlHelper.create(NameIDPolicy.class, NameIDPolicy.DEFAULT_ELEMENT_NAME);
		nameIdPolicy.setAllowCreate(true);
		authnRequest.setNameIDPolicy(nameIdPolicy);

		return authnRequest;
	}
	
}
