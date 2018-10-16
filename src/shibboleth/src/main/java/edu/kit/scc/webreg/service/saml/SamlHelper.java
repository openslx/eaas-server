package edu.kit.scc.webreg.service.saml;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.Remote;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.opensaml.Configuration;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.parse.XMLParserException;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.util.XMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@Named("samlHelper")
@Remote
@ApplicationScoped
public class SamlHelper implements Serializable {

	private static final long serialVersionUID = 1L;

	Logger logger = LoggerFactory.getLogger(SamlHelper.class);

	protected SecureRandomIdentifierGenerator randomIdGen;

	protected MarshallerFactory marshallerFactory;
	protected UnmarshallerFactory unmarshallerFactory;
	protected BasicParserPool basicParserPool;

	@PostConstruct
	public void init() {
		try {
			randomIdGen = new SecureRandomIdentifierGenerator();
		} catch (NoSuchAlgorithmException e) {
			logger.error("No SecureRandomIdentifierGenerator available", e);
		}

		marshallerFactory = Configuration.getMarshallerFactory();
		unmarshallerFactory = Configuration.getUnmarshallerFactory();
		basicParserPool = new BasicParserPool();
		basicParserPool.setNamespaceAware(true);
	}

	public String getRandomId() {
		return randomIdGen.generateIdentifier();
	}

	@SuppressWarnings("unchecked")
	public <T> T create(Class<T> cls, QName qname) {
		return (T) ((XMLObjectBuilder<?>) Configuration.getBuilderFactory().getBuilder(qname)).buildObject(qname);
	}

	public <T extends XMLObject> String marshal(T t) {
		try {
			Element element = toXmlElement(t);
			return XMLHelper.nodeToString(element);
		} catch (MarshallingException e) {
			logger.error("No Marshalling possible", e);
			return null;
		}
	}

	public <T extends XMLObject> String prettyPrint(T t) {
		try {
			Element element = toXmlElement(t);
			return XMLHelper.prettyPrintXML(element);
		} catch (MarshallingException e) {
			logger.error("No Marshalling possible", e);
			return null;
		}
	}

	public <T extends XMLObject> Element toXmlElement(T t) throws MarshallingException {
		Marshaller marshaller = marshallerFactory.getMarshaller(t);
		return marshaller.marshall(t);
	}

	public String getAttribute(Attribute attribute) {
		List<XMLObject> avList = attribute.getAttributeValues();
		StringBuffer sb = new StringBuffer();
		for (XMLObject obj : avList) {
			if (obj != null && obj instanceof XSString) {
				sb.append(((XSString) obj).getValue());
				sb.append(";");
			}
		}

		if (sb.length() > 1)
			sb.setLength(sb.length() - 1);

		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	public <T extends XMLObject> T unmarshal(String s, Class<T> c) {
		try {
			Document document = basicParserPool.parse(new StringReader(s));
			Element documentElement = document.getDocumentElement();
			Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(documentElement);
			XMLObject xmlObject = unmarshaller.unmarshall(document.getDocumentElement());

			return (T) xmlObject;
		} catch (UnmarshallingException | XMLParserException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Document loadXMLFromString(String xml) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource is = new InputSource(new StringReader(xml));
		return builder.parse(is);
	}

	public Map<String, Attribute> assertionToAttributeMap(Assertion assertion) {
		Map<String, Attribute> attrMap = new HashMap<String, Attribute>();

		for (AttributeStatement attrStatement : assertion.getAttributeStatements()) {
			for (Attribute attr : attrStatement.getAttributes()) {
				String attrName = attr.getName();
				attrMap.put(attrName, attr);
			}
		}

		return attrMap;
	}

	public SecureRandomIdentifierGenerator getRandomIdGen() {
		return randomIdGen;
	}

	public MarshallerFactory getMarshallerFactory() {
		return marshallerFactory;
	}

	public UnmarshallerFactory getUnmarshallerFactory() {
		return unmarshallerFactory;
	}

	public BasicParserPool getBasicParserPool() {
		return basicParserPool;
	}
}
