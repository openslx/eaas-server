package edu.kit.scc.webreg.service.impl;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.kit.scc.webreg.dao.AuditDetailDao;
import edu.kit.scc.webreg.entity.AuditDetailEntity;
import edu.kit.scc.webreg.service.AuditDetailService;

@Stateless
public class AuditDetailServiceImpl implements AuditDetailService {

	private static final long serialVersionUID = 1L;

	@Inject
	private AuditDetailDao dao;
	
	@Override
	public AuditDetailEntity createNew() {
		return dao.createNew();
	}

	@Override
	public AuditDetailEntity save(AuditDetailEntity entity) {
		return dao.persist(entity);
	}

	@Override
	public void delete(AuditDetailEntity entity) {
		dao.delete(entity);
	}

	@Override
	public List<AuditDetailEntity> findAll() {
		return dao.findAll();
	}

	@Override
	public List<AuditDetailEntity> findNewestFailed(int limit) {
		return dao.findNewestFailed(limit);
	}

	@Override
	public AuditDetailEntity findById(Long id) {
		return dao.findById(id);
	}
}
