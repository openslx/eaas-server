package edu.kit.scc.webreg.service.impl;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.kit.scc.webreg.dao.ImageDao;
import edu.kit.scc.webreg.entity.ImageEntity;
import edu.kit.scc.webreg.service.ImageService;

@Stateless
public class ImageServiceImpl implements ImageService {

	private static final long serialVersionUID = 1L;

	@Inject
	private ImageDao dao;
	
	@Override
	public ImageEntity createNew() {
		return dao.createNew();
	}

	@Override
	public ImageEntity save(ImageEntity entity) {
		return dao.persist(entity);
	}

	@Override
	public void delete(ImageEntity entity) {
		dao.delete(entity);
	}

	@Override
	public List<ImageEntity> findAll() {
		return dao.findAll();
	}

	@Override
	public ImageEntity findByName(String name) {
		return dao.findByName(name);
	}

	@Override
	public ImageEntity findById(Long id) {
		return dao.findById(id);
	}
	
	@Override
	public ImageEntity findByIdWithData(Long id) {
		return dao.findByIdWithData(id);
	}
	
}
