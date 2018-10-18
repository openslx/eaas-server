package edu.kit.scc.webreg.service.saml.impl;

import java.io.IOException;
import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.EncryptedAssertion;
import org.opensaml.saml2.encryption.Decrypter;
import org.opensaml.xml.encryption.DecryptionException;
import org.opensaml.xml.encryption.InlineEncryptedKeyResolver;
import org.opensaml.xml.security.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xml.security.keyinfo.StaticKeyInfoCredentialResolver;
import org.opensaml.xml.security.x509.BasicX509Credential;

import edu.kit.scc.webreg.service.saml.CryptoHelper;
import edu.kit.scc.webreg.service.saml.Saml2AssertionService;
import edu.kit.scc.webreg.service.saml.SamlHelper;

@Stateless
public class Saml2AssertionServiceImpl implements Saml2AssertionService {

	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	@Inject
	private CryptoHelper cryptoHelper;
	
	@Inject
	private SamlHelper samlHelper;
	
	@Override
	public Assertion decryptAssertion(EncryptedAssertion encryptedAssertion,
			String privateKey) throws IOException, DecryptionException {
		logger.debug("Decrypting assertion...");
		
		KeyPair keyPair = cryptoHelper.getKeyPair(privateKey);
		BasicX509Credential decryptCredential = new BasicX509Credential();
		decryptCredential.setPrivateKey(keyPair.getPrivate());
		KeyInfoCredentialResolver keyResolver = new StaticKeyInfoCredentialResolver(decryptCredential);
		InlineEncryptedKeyResolver encryptionKeyResolver = new InlineEncryptedKeyResolver();
		Decrypter decrypter = new Decrypter(null, keyResolver, encryptionKeyResolver);
		decrypter.setRootInNewDocument(true);
		Assertion assertion = decrypter.decrypt(encryptedAssertion);
		return assertion;
	}
	
	@Override
	public Map<String, String> extractAttributes(Assertion assertion) {
		Map<String, Attribute> attributes = samlHelper.assertionToAttributeMap(assertion);
		Map<String, String> attributeMap = new HashMap<String, String>();
		
		for (Entry<String, Attribute> entry : attributes.entrySet()) {
			attributeMap.put(entry.getKey(), samlHelper.getAttribute(entry.getValue()));
		}
		
		return attributeMap;
	}	
}