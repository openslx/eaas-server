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

import edu.kit.scc.webreg.dao.RegistryDao;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;

@Named
@ApplicationScoped
public class JpaRegistryDao implements RegistryDao {

    @PersistenceContext(unitName = "shib")
    private EntityManager em;
	
	@Override
	public RegistryEntity createNew() {
		return new RegistryEntity();
	}

	@Override
	public RegistryEntity persist(RegistryEntity entity) {
		entity = merge(entity);
		em.persist(entity);
		return entity;
	}

	@Override
	public RegistryEntity merge(RegistryEntity entity) {
		if (em.contains(entity))
			return entity;
		else
			return em.merge(entity);
	}

	@Override
    @SuppressWarnings({"unchecked"})
	public List<RegistryEntity> findAll() {
		return em.createQuery("select e from RegistryEntity e").getResultList();
	}

	@Override
	public RegistryEntity findById(Long id) {
		return em.find(RegistryEntity.class, id);
	}

	@Override
	public List<RegistryEntity> findAllByStatus(RegistryStatus status) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<RegistryEntity> criteria = builder.createQuery(RegistryEntity.class);
		Root<RegistryEntity> root = criteria.from(RegistryEntity.class);
		criteria.where(
				builder.equal(root.get("registryStatus"), status));
		criteria.select(root);

		return em.createQuery(criteria).getResultList();
	}	
	
	@Override
	public List<RegistryEntity> findByService(ServiceEntity service) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<RegistryEntity> criteria = builder.createQuery(RegistryEntity.class);
		Root<RegistryEntity> root = criteria.from(RegistryEntity.class);
		criteria.where(builder.equal(root.get("service"), service));
		criteria.select(root);

		return em.createQuery(criteria).getResultList();
	}	
	
	@Override
	public List<RegistryEntity> findByServiceAndStatus(ServiceEntity service, RegistryStatus status) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<RegistryEntity> criteria = builder.createQuery(RegistryEntity.class);
		Root<RegistryEntity> root = criteria.from(RegistryEntity.class);
		criteria.where(builder.and(
				builder.equal(root.get("service"), service),
				builder.equal(root.get("registryStatus"), status)));
		criteria.select(root);

		return em.createQuery(criteria).getResultList();
	}	
	
	@Override
	public List<RegistryEntity> findByServiceAndUser(ServiceEntity service, UserEntity user) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<RegistryEntity> criteria = builder.createQuery(RegistryEntity.class);
		Root<RegistryEntity> root = criteria.from(RegistryEntity.class);
		criteria.where(builder.and(
				builder.equal(root.get("service"), service),
				builder.equal(root.get("user"), user)));
		criteria.select(root);

		return em.createQuery(criteria).getResultList();
	}	
	
	@Override
	public RegistryEntity findByServiceAndUserAndStatus(ServiceEntity service, UserEntity user, RegistryStatus status) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<RegistryEntity> criteria = builder.createQuery(RegistryEntity.class);
		Root<RegistryEntity> root = criteria.from(RegistryEntity.class);
		criteria.where(builder.and(
				builder.equal(root.get("service"), service),
				builder.equal(root.get("user"), user),
				builder.equal(root.get("registryStatus"), status)));
		criteria.select(root);

		try {
			return em.createQuery(criteria).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}	
	
	@Override
	public List<RegistryEntity> findByUserAndStatus(UserEntity user, RegistryStatus status) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<RegistryEntity> criteria = builder.createQuery(RegistryEntity.class);
		Root<RegistryEntity> root = criteria.from(RegistryEntity.class);
		criteria.where(builder.and(
				builder.equal(root.get("user"), user)),
				builder.equal(root.get("registryStatus"), status));
		criteria.select(root);
		criteria.distinct(true);
		criteria.orderBy(builder.asc(root.get("id")));

		return em.createQuery(criteria).getResultList();
	}	
		
	@Override
	public void delete(RegistryEntity entity) {
		entity = merge(entity);
		em.remove(entity);
	}
}
