package edu.kit.scc.webreg.service.saml;

import org.opensaml.common.SignableSAMLObject;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.metadata.EntityDescriptor;

import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.exc.SamlAuthenticationException;

public interface Saml2ResponseValidationService {

	public void verifyIssuer(SamlIdpMetadataEntity idpEntity, Response samlResponse)
			throws SamlAuthenticationException;

	void verifyExpiration(Response samlResponse, Long expiryMillis)
			throws SamlAuthenticationException;

	void verifyStatus(Response samlResponse) throws SamlAuthenticationException;

	void validateSignature(SignableSAMLObject assertion,
			Issuer issuer, EntityDescriptor idpEntityDescriptor);
	
}
