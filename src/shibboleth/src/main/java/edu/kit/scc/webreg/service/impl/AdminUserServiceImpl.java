package edu.kit.scc.webreg.service.impl;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.kit.scc.webreg.dao.AdminUserDao;
import edu.kit.scc.webreg.entity.AdminUserEntity;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.service.AdminUserService;

@Stateless
public class AdminUserServiceImpl implements AdminUserService {

	private static final long serialVersionUID = 1L;

	@Inject
	private AdminUserDao dao;
	
	@Override
	public AdminUserEntity createNew() {
		return dao.createNew();
	}

	@Override
	public AdminUserEntity save(AdminUserEntity entity) {
		return dao.persist(entity);
	}

	@Override
	public void delete(AdminUserEntity entity) {
		dao.delete(entity);
	}

	@Override
	public List<AdminUserEntity> findAll() {
		return dao.findAll();
	}

	@Override
	public AdminUserEntity findByUsernameAndPassword(String username, String password) {
		return dao.findByUsernameAndPassword(username, password);
	}

	@Override
	public AdminUserEntity findByUsername(String username) {
		return dao.findByUsername(username);
	}

	@Override
	public List<RoleEntity> findRolesForUserById(Long id) {
		return dao.findRolesForUserById(id);
	}	
	@Override
	public AdminUserEntity findById(Long id) {
		return dao.findById(id);
	}
}
