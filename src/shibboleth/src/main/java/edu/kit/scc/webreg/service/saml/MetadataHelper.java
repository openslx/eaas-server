package edu.kit.scc.webreg.service.saml;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.namespace.QName;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.common.Extensions;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.metadata.AttributeAuthorityDescriptor;
import org.opensaml.saml2.metadata.AttributeService;
import org.opensaml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.OrganizationDisplayName;
import org.opensaml.saml2.metadata.SingleSignOnService;
import org.opensaml.samlext.saml2mdattr.EntityAttributes;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.parse.XMLParserException;
import org.opensaml.xml.schema.XSAny;
import org.w3c.dom.Document;

import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlIdpScopeEntity;


@Named("metadataHelper")
@ApplicationScoped
public class MetadataHelper implements Serializable {

	private static final long serialVersionUID = 1L;
	private Logger logger = Logger.getLogger(getClass().getName());


	@Inject
	private SamlHelper samlHelper;
	
	public InputStream fetchMetadata(String url) {
		try {
			logger.info("Fetching Metadata from {} " + url);
			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(url);
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			return entity.getContent();
		} catch (ClientProtocolException e) {
			logger.severe("No Metadata available " + e);
			return null;
		} catch (IllegalStateException e) {
			logger.severe("No Metadata available " + e);
			return null;
		} catch (IOException e) {
			logger.severe("No Metadata available" + e);
			return null;
		}
	}

	public EntitiesDescriptor parseMetadata(InputStream inputStream) {
		BasicParserPool basicParserPool = samlHelper.getBasicParserPool();
		UnmarshallerFactory unmarshallerFactory = samlHelper.getUnmarshallerFactory();
		
		try {
			Document document = basicParserPool.parse(new InputStreamReader(inputStream, "UTF-8"));
			Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(document.getDocumentElement());
			XMLObject xmlObject = unmarshaller.unmarshall(document.getDocumentElement());
			EntitiesDescriptor entities = (EntitiesDescriptor) xmlObject;
			
			return entities;
		} catch (XMLParserException e) {
			logger.severe("No Metadata available" + e);
			return null;
		} catch (UnmarshallingException e) {
			logger.severe("No Metadata available" + e);
			return null;
		} catch (UnsupportedEncodingException e) {
			logger.severe("No UTF-8 support available" + e);
			return null;
		}		
	}

	public List<EntityDescriptor> convertEntitiesDescriptor(EntitiesDescriptor entities) {
		List<EntityDescriptor> entityList = new ArrayList<EntityDescriptor>();
		convertEntities(entityList, entities);
		
		return entityList;
	}
	
	public List<EntityDescriptor> filterSP(List<EntityDescriptor> entities) {
		List<EntityDescriptor> returnList = new ArrayList<EntityDescriptor>();
		
		for (EntityDescriptor entity : entities) {
			IDPSSODescriptor idpsso = entity.getIDPSSODescriptor(SAMLConstants.SAML20P_NS);
			if (idpsso != null)
				returnList.add(entity);
		}
		
		return returnList;
	}
	
	public List<EntityDescriptor> filterEntityCategory(List<EntityDescriptor> entities, String category) {
		List<EntityDescriptor> returnList = new ArrayList<EntityDescriptor>();
		
		for (EntityDescriptor entity : entities) {
			Extensions extensions = entity.getExtensions();
			List<XMLObject> extObjs = extensions.getOrderedChildren();
			for (XMLObject xmlObject : extObjs) {
				if (xmlObject instanceof EntityAttributes) {
					EntityAttributes entityAttrs = (EntityAttributes) xmlObject;
					for (Attribute attr : entityAttrs.getAttributes()) {
						if ("http://macedir.org/entity-category".equals(attr.getName())) {
							for (XMLObject value : attr.getAttributeValues()) {
								if (value instanceof XSAny) {
									XSAny any = (XSAny) value;
									if (category.equals(any.getTextContent())) {
										returnList.add(entity);
									}
								}								
							}
						}
					}
				}
			}			
		}
		
		return returnList;
	}
	
	public String getOrganisation(EntityDescriptor entityDesc) {
		if (entityDesc.getOrganization() != null) {
			List<OrganizationDisplayName> displayList = entityDesc.getOrganization().getDisplayNames();
			
			if (displayList.size() > 0)
				return displayList.get(0).getName().getLocalString();
			else
				return entityDesc.getEntityID();
		}
		else {
			return entityDesc.getEntityID();
		}
	}

	public Set<SamlIdpScopeEntity> getScopes(EntityDescriptor entityDesc, SamlIdpMetadataEntity idp) {
		Set<SamlIdpScopeEntity> scopeList = new HashSet<SamlIdpScopeEntity>();
		
		IDPSSODescriptor idpsso = entityDesc.getIDPSSODescriptor(SAMLConstants.SAML20P_NS);
		if (idpsso != null) {
			Extensions extensions = idpsso.getExtensions();
			List<XMLObject> scopes = extensions.getUnknownXMLObjects(new QName("urn:mace:shibboleth:metadata:1.0", "Scope"));
			for (XMLObject xmlObject : scopes) {
				if (xmlObject instanceof XSAny) {
					XSAny any = (XSAny) xmlObject;
					SamlIdpScopeEntity scope = new SamlIdpScopeEntity();
					scope.setScope(any.getTextContent());
					scope.setRegex(false);
					scope.setIdp(idp);
					scopeList.add(scope);
				}
			}
		}
		
		return scopeList;
	}
	
	public SingleSignOnService getSSO(EntityDescriptor entityDesc, String binding) {
		IDPSSODescriptor idpSsoDesc = entityDesc.getIDPSSODescriptor(SAMLConstants.SAML20P_NS);
		if (idpSsoDesc != null) {
			List<SingleSignOnService> ssos = idpSsoDesc.getSingleSignOnServices();
			for (SingleSignOnService sso : ssos) {
				if (sso.getBinding().equals(binding)) {
					return sso;
				}
			}
		}
		return null;
	}
	
	public AttributeService getAttributeService(EntityDescriptor entityDesc) {
		AttributeAuthorityDescriptor idpAtrDesc = entityDesc.getAttributeAuthorityDescriptor(SAMLConstants.SAML20P_NS);
		if (idpAtrDesc != null) {
			List<AttributeService> attrs = idpAtrDesc.getAttributeServices();
			for (AttributeService attr : attrs) {
				if (attr.getBinding().equals(SAMLConstants.SAML2_SOAP11_BINDING_URI)) {
					return attr;
				}
			}
		}
		return null;
	}
	
	private void convertEntities(List<EntityDescriptor> entityList, EntitiesDescriptor entities) {
		for (EntityDescriptor entity : entities.getEntityDescriptors()) {
			entityList.add(entity);
		}

		for (EntitiesDescriptor entitiesInEntities : entities.getEntitiesDescriptors()) {
			convertEntities(entityList, entitiesInEntities);
		}		
	}
}
