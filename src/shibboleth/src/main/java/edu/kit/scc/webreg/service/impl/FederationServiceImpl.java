package edu.kit.scc.webreg.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.opensaml.saml2.metadata.EntityDescriptor;

import edu.kit.scc.webreg.dao.FederationDao;
import edu.kit.scc.webreg.entity.FederationEntity;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlIdpScopeEntity;
import edu.kit.scc.webreg.service.FederationService;
import edu.kit.scc.webreg.service.SamlIdpMetadataService;
import edu.kit.scc.webreg.service.SamlIdpScopeService;
import edu.kit.scc.webreg.service.saml.MetadataHelper;
import edu.kit.scc.webreg.service.saml.SamlHelper;

@Stateless
public class FederationServiceImpl implements FederationService {

	private static final long serialVersionUID = 1L;

	private Logger logger = Logger.getLogger(getClass());
	
	@Inject
	private FederationDao dao;
	
	@Inject
	private SamlIdpMetadataService idpService;
	
	@Inject
	private SamlIdpScopeService idpScopeService;
	
	@Inject 
	private SamlHelper samlHelper;

	@Inject 
	private MetadataHelper metadataHelper;
	
	@Override
	public FederationEntity createNew() {
		return dao.createNew();
	}

	@Override
	public FederationEntity save(FederationEntity entity) {
		return dao.persist(entity);
	}

	@Override
	public void updateEntities(FederationEntity entity, List<EntityDescriptor> entityList) {
		
		List<SamlIdpMetadataEntity> oldList = idpService.findAllByFederation(entity);
		List<SamlIdpMetadataEntity> updatedList = new ArrayList<SamlIdpMetadataEntity>();
		
		for (EntityDescriptor ed : entityList) {
			SamlIdpMetadataEntity idp = idpService.findByFederationAndEntityId(entity, ed.getEntityID());

			Boolean newIdp = (idp == null ? true : false);
			if (newIdp) {
				idp = idpService.createNew();
				logger.debug("Creating new idp {} " + ed.getEntityID());
			}

			idp.setEntityId(ed.getEntityID());
			idp.setEntityDescriptor(samlHelper.marshal(ed));
			idp.setOrgName(metadataHelper.getOrganisation(ed));
			idp.setFederation(entity);
			
			idp = idpService.save(idp);

			Set<SamlIdpScopeEntity> scopes = metadataHelper.getScopes(ed, idp);

			List<SamlIdpScopeEntity> oldScopes;
			if (newIdp) 
				oldScopes = new ArrayList<SamlIdpScopeEntity>();
			else
				oldScopes = idpScopeService.findByIdp(idp);
			
			Set<SamlIdpScopeEntity> deleteScopes = new HashSet<SamlIdpScopeEntity>(oldScopes);
			deleteScopes.removeAll(scopes);
			for (SamlIdpScopeEntity scope : deleteScopes) {
				logger.debug("Deleting idp scope {} " + scope.getScope());
				idpScopeService.delete(scope);
			}
			
			scopes.removeAll(oldScopes);
			for (SamlIdpScopeEntity scope : scopes) {
				logger.debug("Creating new idp scope {} " + scope.getScope());
				idpScopeService.save(scope);
			}
			
			updatedList.add(idp);
		}
		
		oldList.removeAll(updatedList);

		for (SamlIdpMetadataEntity idp : oldList) {
			idpService.delete(idp);
			logger.debug("Delete idp {} " + idp.getEntityId());
		}		

		entity.setPolledAt(new Date());
		
		dao.persist(entity);
	}

	@Override
	public void delete(FederationEntity entity) {
		dao.delete(entity);
	}

	@Override
	public List<FederationEntity> findAll() {
		return dao.findAll();
	}

	@Override
	public List<FederationEntity> findAllWithIdpEntities() {
		return dao.findAllWithIdpEntities();
	}

	@Override
	public FederationEntity findById(Long id) {
		return dao.findById(id);
	}

	@Override
	public FederationEntity findWithIdpEntities(Long id) {
		return dao.findWithIdpEntities(id);
	}

}
