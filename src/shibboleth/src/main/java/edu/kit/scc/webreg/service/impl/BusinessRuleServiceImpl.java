package edu.kit.scc.webreg.service.impl;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.kit.scc.webreg.dao.BusinessRuleDao;
import edu.kit.scc.webreg.entity.BusinessRuleEntity;
import edu.kit.scc.webreg.service.BusinessRuleService;

@Stateless
public class BusinessRuleServiceImpl implements BusinessRuleService {

	private static final long serialVersionUID = 1L;

	@Inject
	private BusinessRuleDao dao;
	
	@Override
	public BusinessRuleEntity createNew() {
		return dao.createNew();
	}

	@Override
	public BusinessRuleEntity save(BusinessRuleEntity entity) {
		return dao.persist(entity);
	}

	@Override
	public void delete(BusinessRuleEntity entity) {
		dao.delete(entity);
	}

	@Override
	public List<BusinessRuleEntity> findAll() {
		return dao.findAll();
	}

	@Override
	public BusinessRuleEntity findById(Long id) {
		return dao.findById(id);
	}
}
