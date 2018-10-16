package edu.kit.scc.webreg.service.impl;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.kit.scc.webreg.dao.AdminRoleDao;
import edu.kit.scc.webreg.entity.AdminRoleEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.service.AdminRoleService;

@Stateless
public class AdminRoleServiceImpl implements AdminRoleService {

	private static final long serialVersionUID = 1L;

	@Inject
	private AdminRoleDao dao;
	
	@Override
	public AdminRoleEntity createNew() {
		return dao.createNew();
	}

	@Override
	public AdminRoleEntity save(AdminRoleEntity entity) {
		return dao.persist(entity);
	}

	@Override
	public void delete(AdminRoleEntity entity) {
		dao.delete(entity);
	}

	@Override
	public List<AdminRoleEntity> findAll() {
		return dao.findAll();
	}

	@Override
	public AdminRoleEntity findById(Long id) {
		return dao.findById(id);
	}
	
	@Override
	public List<AdminRoleEntity> findWithServices(UserEntity user) {
		return dao.findWithServices(user);
	}
	
	@Override
	public AdminRoleEntity findWithUsers(Long id) {
		return dao.findWithUsers(id);
	}
	
	@Override
	public AdminRoleEntity findByName(String name) {
		return dao.findByName(name);
	}
}
