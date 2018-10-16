package edu.kit.scc.webreg.dao.jpa;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import edu.kit.scc.webreg.dao.ImageDao;
import edu.kit.scc.webreg.entity.ImageDataEntity;
import edu.kit.scc.webreg.entity.ImageEntity;

@Named
@ApplicationScoped
public class JpaImageDao implements ImageDao {

    @PersistenceContext(unitName = "shib")
    private EntityManager em;
	
	@Override
	public ImageEntity createNew() {
		ImageEntity imageEntity = new ImageEntity();
		imageEntity.setImageData(new ImageDataEntity());
		return imageEntity;
	}

	@Override
	public ImageEntity persist(ImageEntity entity) {
		entity = merge(entity);
		em.persist(entity);
		return entity;
	}

	@Override
	public ImageEntity merge(ImageEntity entity) {
		if (em.contains(entity))
			return entity;
		else
			return em.merge(entity);
	}

	@Override
    @SuppressWarnings({"unchecked"})
	public List<ImageEntity> findAll() {
		return em.createQuery("select e from ImageEntity e").getResultList();
	}

	@Override
	public ImageEntity findById(Long id) {
		try {
			return em.find(ImageEntity.class, id);
		}
		catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public ImageEntity findByName(String name) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<ImageEntity> criteria = builder.createQuery(ImageEntity.class);
		Root<ImageEntity> root = criteria.from(ImageEntity.class);
		criteria.where(
				builder.equal(root.get("name"), name));
		criteria.select(root);
		
		try {
			return em.createQuery(criteria).getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}	

	@Override
	public ImageEntity findByIdWithData(Long id) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<ImageEntity> criteria = builder.createQuery(ImageEntity.class);
		Root<ImageEntity> root = criteria.from(ImageEntity.class);
		criteria.where(
				builder.equal(root.get("id"), id));
		criteria.select(root);
		root.fetch("imageData");

		try {
			return em.createQuery(criteria).getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}	
	
	@Override
	public void delete(ImageEntity entity) {
		entity = merge(entity);
		em.remove(entity);
	}
}
