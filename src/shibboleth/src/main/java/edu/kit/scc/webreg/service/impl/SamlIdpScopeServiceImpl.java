package edu.kit.scc.webreg.service.impl;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.kit.scc.webreg.dao.SamlIdpScopeDao;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlIdpScopeEntity;
import edu.kit.scc.webreg.service.SamlIdpScopeService;

@Stateless
public class SamlIdpScopeServiceImpl implements SamlIdpScopeService {

	private static final long serialVersionUID = 1L;

	@Inject
	private SamlIdpScopeDao dao;
	
	@Override
	public SamlIdpScopeEntity createNew() {
		return dao.createNew();
	}

	@Override
	public SamlIdpScopeEntity save(SamlIdpScopeEntity entity) {
		return dao.persist(entity);
	}

	@Override
	public void delete(SamlIdpScopeEntity entity) {
		dao.delete(entity);
	}

	@Override
	public List<SamlIdpScopeEntity> findAll() {
		return dao.findAll();
	}

	@Override
	public List<SamlIdpScopeEntity> findByIdp(SamlIdpMetadataEntity idp) {
		return dao.findByIdp(idp);
	}

	@Override
	public SamlIdpScopeEntity findById(Long id) {
		return dao.findById(id);
	}
}
