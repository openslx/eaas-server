package edu.kit.scc.webreg.service.impl;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.kit.scc.webreg.dao.AuditEntryDao;
import edu.kit.scc.webreg.entity.AuditEntryEntity;
import edu.kit.scc.webreg.service.AuditEntryService;

@Stateless
public class AuditEntryServiceImpl implements AuditEntryService {

	private static final long serialVersionUID = 1L;

	@Inject
	private AuditEntryDao dao;
	
	@Override
	public AuditEntryEntity createNew() {
		return dao.createNew();
	}

	@Override
	public AuditEntryEntity save(AuditEntryEntity entity) {
		return dao.persist(entity);
	}

	@Override
	public void delete(AuditEntryEntity entity) {
		dao.delete(entity);
	}

	@Override
	public List<AuditEntryEntity> findAll() {
		return dao.findAll();
	}

	@Override
	public AuditEntryEntity findById(Long id) {
		return dao.findById(id);
	}
}
