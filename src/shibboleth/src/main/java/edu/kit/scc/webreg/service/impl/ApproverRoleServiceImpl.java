package edu.kit.scc.webreg.service.impl;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.kit.scc.webreg.dao.ApproverRoleDao;
import edu.kit.scc.webreg.entity.ApproverRoleEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.service.ApproverRoleService;

@Stateless
public class ApproverRoleServiceImpl implements ApproverRoleService {

	private static final long serialVersionUID = 1L;

	@Inject
	private ApproverRoleDao dao;
	
	@Override
	public ApproverRoleEntity createNew() {
		return dao.createNew();
	}

	@Override
	public ApproverRoleEntity save(ApproverRoleEntity entity) {
		return dao.persist(entity);
	}

	@Override
	public void delete(ApproverRoleEntity entity) {
		dao.delete(entity);
	}

	@Override
	public List<ApproverRoleEntity> findAll() {
		return dao.findAll();
	}

	@Override
	public ApproverRoleEntity findById(Long id) {
		return dao.findById(id);
	}
	
	@Override
	public List<ApproverRoleEntity> findWithServices(UserEntity user) {
		return dao.findWithServices(user);
	}
	
	@Override
	public ApproverRoleEntity findWithUsers(Long id) {
		return dao.findWithUsers(id);
	}
	
	@Override
	public ApproverRoleEntity findByName(String name) {
		return dao.findByName(name);
	}
}
