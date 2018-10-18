package edu.kit.scc.webreg.service.impl;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.kit.scc.webreg.dao.ServiceDao;
import edu.kit.scc.webreg.entity.ImageEntity;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.service.ServiceService;

@Stateless
public class ServiceServiceImpl implements ServiceService {

	private static final long serialVersionUID = 1L;

	@Inject
	private ServiceDao dao;
	
	@Override
	public ServiceEntity createNew() {
		return dao.createNew();
	}

	@Override
	public ServiceEntity save(ServiceEntity entity) {
		return dao.persist(entity);
	}

	@Override
	public void delete(ServiceEntity entity) {
		dao.delete(entity);
	}

	@Override
	public List<ServiceEntity> findAll() {
		return dao.findAll();
	}

	@Override
	public List<ServiceEntity> findAllPublishedWithServiceProps() {
		return dao.findAllPublishedWithServiceProps();
	}	

	@Override
	public ServiceEntity findByShortName(String shortName) {
		return dao.findByShortName(shortName);
	}
	
	@Override
	public List<ServiceEntity> findByAdminRole(RoleEntity role) {
		return dao.findByAdminRole(role);
	}

	@Override
	public List<ServiceEntity> findByApproverRole(RoleEntity role) {
		return dao.findByApproverRole(role);
	}

	@Override
	public List<ServiceEntity> findAllWithPolicies() {
		return dao.findAllWithPolicies();
	}

	@Override
	public List<ServiceEntity> findAllByImage(ImageEntity image) {
		return dao.findAllByImage(image);
	}

	@Override
	public ServiceEntity findWithPolicies(Long id) {
		return dao.findWithPolicies(id);
	}
	
	@Override
	public ServiceEntity findById(Long id) {
		return dao.findById(id);
	}
	
	@Override
	public ServiceEntity findByIdWithServiceProps(Long id) {
		return dao.findByIdWithServiceProps(id);
	}
}
