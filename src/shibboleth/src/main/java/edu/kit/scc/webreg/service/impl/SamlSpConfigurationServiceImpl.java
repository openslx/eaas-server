package edu.kit.scc.webreg.service.impl;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.kit.scc.webreg.dao.SamlSpConfigurationDao;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.service.SamlSpConfigurationService;

@Stateless
public class SamlSpConfigurationServiceImpl implements SamlSpConfigurationService {

	private static final long serialVersionUID = 1L;

	@Inject
	private SamlSpConfigurationDao dao;

	@Override
	public SamlSpConfigurationEntity createNew() {
		return dao.createNew();
	}

	@Override
	public SamlSpConfigurationEntity save(SamlSpConfigurationEntity entity) {
		return dao.persist(entity);
	}

	@Override
	public void delete(SamlSpConfigurationEntity entity) {
		dao.delete(entity);
	}

	@Override
	public List<SamlSpConfigurationEntity> findAll() {
		return dao.findAll();
	}

	@Override
	public SamlSpConfigurationEntity findByEntityId(String entityId) {
		return dao.findByEntityId(entityId);
	}

	@Override
	public SamlSpConfigurationEntity findById(Long id) {
		return dao.findById(id);
	}

	@Override
	public SamlSpConfigurationEntity findByHostname(String hostname) {
		return dao.findByHostname(hostname);
	}

}
