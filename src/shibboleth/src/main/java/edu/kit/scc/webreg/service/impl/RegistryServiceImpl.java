package edu.kit.scc.webreg.service.impl;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.kit.scc.webreg.dao.RegistryDao;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.service.RegistryService;

@Stateless
public class RegistryServiceImpl implements RegistryService {

	private static final long serialVersionUID = 1L;

	@Inject
	private RegistryDao dao;
	
	@Override
	public RegistryEntity createNew() {
		return dao.createNew();
	}

	@Override
	public RegistryEntity save(RegistryEntity entity) {
		return dao.persist(entity);
	}

	@Override
	public void delete(RegistryEntity entity) {
		dao.delete(entity);
	}

	@Override
	public List<RegistryEntity> findAll() {
		return dao.findAll();
	}

	@Override
	public List<RegistryEntity> findAllByStatus(RegistryStatus status) {
		return dao.findAllByStatus(status);
	}

	@Override
	public List<RegistryEntity> findByServiceAndStatus(ServiceEntity service, RegistryStatus status) {
		return dao.findByServiceAndStatus(service, status);
	}

	@Override
	public List<RegistryEntity> findByServiceAndUser(ServiceEntity service, UserEntity user) {
		return dao.findByServiceAndUser(service, user);
	}
	
	@Override
	public RegistryEntity findByServiceAndUserAndStatus(ServiceEntity service, UserEntity user, RegistryStatus status) {
		return dao.findByServiceAndUserAndStatus(service, user, status);
	}
	
	@Override
	public List<RegistryEntity> findByService(ServiceEntity service) {
		return dao.findByService(service);
	}

	@Override
	public List<RegistryEntity> findByUserAndStatus(UserEntity user, RegistryStatus status) {
		return dao.findByUserAndStatus(user, status);
	}

	@Override
	public RegistryEntity findById(Long id) {
		return dao.findById(id);
	}
}
