package edu.kit.scc.webreg.service.impl;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.kit.scc.webreg.dao.AgreementTextDao;
import edu.kit.scc.webreg.entity.AgreementTextEntity;
import edu.kit.scc.webreg.service.AgreementTextService;

@Stateless
public class AgreementTextServiceImpl implements AgreementTextService {

	private static final long serialVersionUID = 1L;

	@Inject
	private AgreementTextDao dao;
	
	@Override
	public AgreementTextEntity createNew() {
		return dao.createNew();
	}

	@Override
	public AgreementTextEntity save(AgreementTextEntity entity) {
		return dao.persist(entity);
	}

	@Override
	public void delete(AgreementTextEntity entity) {
		dao.delete(entity);
	}

	@Override
	public List<AgreementTextEntity> findAll() {
		return dao.findAll();
	}

	@Override
	public AgreementTextEntity findById(Long id) {
		return dao.findById(id);
	}
}
