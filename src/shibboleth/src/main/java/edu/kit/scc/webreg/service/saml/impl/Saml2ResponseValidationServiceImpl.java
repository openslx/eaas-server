package edu.kit.scc.webreg.service.saml.impl;

import org.joda.time.Duration;
import org.joda.time.Instant;
import org.opensaml.Configuration;
import org.opensaml.common.SignableSAMLObject;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.provider.DOMMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.security.MetadataCredentialResolver;
import org.opensaml.security.MetadataCriteria;
import org.opensaml.security.SAMLSignatureProfileValidator;
import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.criteria.EntityIDCriteria;
import org.opensaml.xml.security.criteria.UsageCriteria;
import org.opensaml.xml.security.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xml.signature.impl.ExplicitKeySignatureTrustEngine;
import org.opensaml.xml.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.exc.SamlAuthenticationException;
import edu.kit.scc.webreg.service.saml.Saml2ResponseValidationService;

public class Saml2ResponseValidationServiceImpl implements
		Saml2ResponseValidationService {

	private Logger logger = LoggerFactory.getLogger(Saml2ResponseValidationServiceImpl.class);
	
	@Override
	public void verifyIssuer(SamlIdpMetadataEntity idpEntity,
			Response samlResponse) throws SamlAuthenticationException {

		Issuer issuer = samlResponse.getIssuer();
		
		if (issuer == null)
			throw new SamlAuthenticationException("Response issuer is not set");

		String issuerString = issuer.getValue();
		if (! issuerString.equals(idpEntity.getEntityId())) 
			throw new SamlAuthenticationException("Response issuer " + issuerString + 
					" differs from excpected " + idpEntity.getEntityId());

	}

	@Override
	public void verifyExpiration(Response samlResponse, Long expiryMillis) 
			throws SamlAuthenticationException {

		Duration duration = new Duration(samlResponse.getIssueInstant(), new Instant());
		if (duration.isLongerThan(new Duration(expiryMillis))) 
			throw new SamlAuthenticationException("Response is already expired after " + duration.getStandardSeconds() + " seconds");
	}	

	@Override
	public void verifyStatus(Response samlResponse) 
			throws SamlAuthenticationException {

		Status status = samlResponse.getStatus();
		if (! status.getStatusCode().getValue().equals(StatusCode.SUCCESS_URI)) 
			throw new SamlAuthenticationException("SAML Response: Login was not successful " + status.getStatusCode().getValue());
	}

	@Override
	public void validateSignature(SignableSAMLObject sigObj, Issuer issuer, EntityDescriptor idpEntityDescriptor) {
	
		if (sigObj.getSignature() == null)
			throw new SamlAuthenticationException("No Signature on SignableSamlObject");
		
		DOMMetadataProvider mp = new DOMMetadataProvider(idpEntityDescriptor.getDOM());
		try {
			mp.initialize();
		} catch (MetadataProviderException e) {
			throw new SamlAuthenticationException("Metadata for IDP " + idpEntityDescriptor.getEntityID() + " could not be established");			
		}
		
		MetadataCredentialResolver mdCredResolver = new MetadataCredentialResolver(mp);
		KeyInfoCredentialResolver keyInfoCredResolver =
			    Configuration.getGlobalSecurityConfiguration().getDefaultKeyInfoCredentialResolver();
		ExplicitKeySignatureTrustEngine trustEngine = new ExplicitKeySignatureTrustEngine(mdCredResolver, keyInfoCredResolver);
		
		SAMLSignatureProfileValidator sigValidator = new SAMLSignatureProfileValidator();
		try {
			sigValidator.validate(sigObj.getSignature());
		} catch (ValidationException e) {
			throw new SamlAuthenticationException("SAMLSignableObject signature is not valid");
		}
		
		CriteriaSet criteriaSet = new CriteriaSet();
		criteriaSet.add(new EntityIDCriteria(issuer.getValue()));
		criteriaSet.add(new MetadataCriteria(IDPSSODescriptor.DEFAULT_ELEMENT_NAME, SAMLConstants.SAML20P_NS));
		criteriaSet.add(new UsageCriteria(UsageType.SIGNING));
			
		try {
			trustEngine.validate(sigObj.getSignature(), criteriaSet);
			logger.info("Signutare validation success for " + idpEntityDescriptor.getEntityID());
		} catch (SecurityException e) {
			throw new SamlAuthenticationException("SAMLSignableObject could not be validated.");
		}
	}	
}
