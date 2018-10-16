package edu.kit.scc.webreg.service.impl;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.kit.scc.webreg.dao.RoleDao;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.service.RoleService;

@Stateless
public class RoleServiceImpl implements RoleService {

	private static final long serialVersionUID = 1L;

	@Inject
	private RoleDao dao;
	
	@Override
	public RoleEntity createNew() {
		return dao.createNew();
	}

	@Override
	public RoleEntity save(RoleEntity entity) {
		return dao.persist(entity);
	}

	@Override
	public void delete(RoleEntity entity) {
		dao.delete(entity);
	}

	@Override
	public List<RoleEntity> findAll() {
		return dao.findAll();
	}

	@Override
	public RoleEntity findById(Long id) {
		return dao.findById(id);
	}
	
	@Override
	public List<RoleEntity> findByUser(UserEntity user) {
		return dao.findByUser(user);
	}
	
	@Override
	public RoleEntity findWithUsers(Long id) {
		return dao.findWithUsers(id);
	}
	
	@Override
	public RoleEntity findByName(String name) {
		return dao.findByName(name);
	}
}
