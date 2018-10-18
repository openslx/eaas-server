package edu.kit.scc.webreg.service.impl;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;

import edu.kit.scc.webreg.dao.SerialDao;
import edu.kit.scc.webreg.entity.SerialEntity;
import edu.kit.scc.webreg.service.SerialService;

@Stateless
public class SerialServiceImpl implements SerialService {

	private static final long serialVersionUID = 1L;

	@Inject
	private SerialDao dao;
	
	@Override
	public SerialEntity createNew() {
		return dao.createNew();
	}

	@Override
	public SerialEntity save(SerialEntity entity) {
		return dao.persist(entity);
	} 

	@Override
	public void delete(SerialEntity entity) {
		dao.delete(entity);
	}

	@Override
	public List<SerialEntity> findAll() {
		return dao.findAll();
	}

	@Override
	public SerialEntity findById(Long id) {
		return dao.findById(id);
	}

	@Override
	public SerialEntity findByName(String name) {
		return dao.findByName(name);
	}
	
	@Override
	public Long next(String name) {
		SerialEntity serial = dao.findByName(name);
		Long value = serial.getActual();
		value++;
		serial.setActual(value);
		dao.persist(serial);
		return value;
	}
	
	@Override
	public void createIfNotExistant(String name, Long initalValue) {
		try {
			dao.findByName(name);
		} catch (NoResultException nre) {
			SerialEntity serial = dao.createNew();
			serial.setName(name);
			serial.setActual(initalValue);
			dao.persist(serial);
		}
		
	}
}
