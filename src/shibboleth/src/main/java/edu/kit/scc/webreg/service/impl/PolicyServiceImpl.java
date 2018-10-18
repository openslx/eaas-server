package edu.kit.scc.webreg.service.impl;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.kit.scc.webreg.dao.PolicyDao;
import edu.kit.scc.webreg.entity.PolicyEntity;
import edu.kit.scc.webreg.service.PolicyService;

@Stateless
public class PolicyServiceImpl implements PolicyService {

	private static final long serialVersionUID = 8748738084507514779L;

	@Inject
	private PolicyDao dao;
	
	@Override
	public PolicyEntity createNew() {
		return dao.createNew();
	}

	@Override
	public PolicyEntity save(PolicyEntity entity) {
		return dao.persist(entity);
	}

	@Override
	public void delete(PolicyEntity entity) {
		dao.delete(entity);
	}

	@Override
	public List<PolicyEntity> findAll() {
		return dao.findAll();
	}

	@Override
	public PolicyEntity findById(Long id) {
		return dao.findById(id);
	}

	@Override
	public PolicyEntity findWithAgreemets(Long id) {
		return dao.findWithAgreemets(id);
	}
}
