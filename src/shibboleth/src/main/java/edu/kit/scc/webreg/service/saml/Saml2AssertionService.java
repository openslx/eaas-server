package edu.kit.scc.webreg.service.saml;

import java.io.IOException;
import java.util.Map;

import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.EncryptedAssertion;
import org.opensaml.xml.encryption.DecryptionException;

public interface Saml2AssertionService {

	public Assertion decryptAssertion(EncryptedAssertion encryptedAssertion, String privateKey)
			throws IOException, DecryptionException;

	Map<String, String> extractAttributes(Assertion assertion);

}
