package edu.kit.scc.webreg.service.saml;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.security.KeyPair;
import java.security.Security;
import java.security.cert.X509Certificate;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.bouncycastle.openssl.PEMReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Named("cryptoHelper")
@ApplicationScoped
public class CryptoHelper implements Serializable {

	private static final long serialVersionUID = 1L;
	private Logger logger = LoggerFactory.getLogger(CryptoHelper.class);

	@PostConstruct
	public void init() {
		logger.info("Register BounceyCastle Crypto Provider");
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	}
	
	public X509Certificate getCertificate(String certString) throws IOException {
		PEMReader pemReader = new PEMReader(new StringReader(certString));
		X509Certificate cert = (X509Certificate) pemReader.readObject();
		pemReader.close();
		return cert;
	}
	
	public KeyPair getKeyPair(String privateKey) throws IOException {
		PEMReader pemReader = new PEMReader(new StringReader(privateKey));
		KeyPair pair = (KeyPair) pemReader.readObject();
		pemReader.close();
		return pair;
	}


}
