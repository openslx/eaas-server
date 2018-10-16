package edu.kit.scc.webreg.service.impl;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.kit.scc.webreg.dao.SamlIdpMetadataDao;
import edu.kit.scc.webreg.entity.FederationEntity;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.service.SamlIdpMetadataService;

@Stateless
public class SamlIdpMetadataServiceImpl implements SamlIdpMetadataService {

	private static final long serialVersionUID = 2142732348120150826L;

	@Inject
	private SamlIdpMetadataDao dao;
	
	@Override
	public SamlIdpMetadataEntity createNew() {
		return dao.createNew();
	}

	@Override
	public SamlIdpMetadataEntity save(SamlIdpMetadataEntity entity) {
		return dao.persist(entity);
	}

	@Override
	public void delete(SamlIdpMetadataEntity entity) {
		dao.delete(entity);
	}

	@Override
	public List<SamlIdpMetadataEntity> findAll() {
		return dao.findAll();
	}

	@Override
	public List<SamlIdpMetadataEntity> findAllByFederation(FederationEntity federation) {
		return dao.findAllByFederation(federation);
	}
	
	@Override
	public SamlIdpMetadataEntity findByEntityId(String entityId) {
		return dao.findByEntityId(entityId);
	}
	
	@Override
	public List<SamlIdpMetadataEntity> findAllByFederationOrderByOrgname(FederationEntity federation) {
		return dao.findAllByFederationOrderByOrgname(federation);
	}
		
	@Override
	public SamlIdpMetadataEntity findByFederationAndEntityId(FederationEntity federation, String entityId) {
		return dao.findByFederationAndEntityId(federation, entityId);
	}

	@Override
	public SamlIdpMetadataEntity findById(Long id) {
		return dao.findById(id);
	}

	@Override
	public SamlIdpMetadataEntity findByScope(String scope) {
		return dao.findByScope(scope);
	}

}
