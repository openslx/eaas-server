package edu.kit.scc.webreg.dao.jpa;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import edu.kit.scc.webreg.dao.ServiceDao;
import edu.kit.scc.webreg.entity.ImageEntity;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;

@Named
@ApplicationScoped
public class JpaServiceDao implements ServiceDao {

    @PersistenceContext(unitName = "shib")
    private EntityManager em;
	
	@Override
	public ServiceEntity createNew() {
		return new ServiceEntity();
	}

	@Override
	public ServiceEntity persist(ServiceEntity entity) {
		entity = merge(entity);
		em.persist(entity);
		return entity;
	}

	@Override
	public ServiceEntity merge(ServiceEntity entity) {
		if (em.contains(entity))
			return entity;
		else
			return em.merge(entity);
	}

	@Override
    @SuppressWarnings({"unchecked"})
	public List<ServiceEntity> findAll() {
		return em.createQuery("select e from ServiceEntity e").getResultList();
	}

	@Override
	public ServiceEntity findByShortName(String shortName) {
		try {
			return (ServiceEntity) em.createQuery("select e from ServiceEntity e where e.shortName = :shortName")
				.setParameter("shortName", shortName).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public List<ServiceEntity> findAllPublishedWithServiceProps() {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<ServiceEntity> criteria = builder.createQuery(ServiceEntity.class);
		Root<ServiceEntity> root = criteria.from(ServiceEntity.class);
		criteria.where(
				builder.equal(root.get("published"), true));
		criteria.select(root);
		criteria.distinct(true);
		root.fetch("serviceProps", JoinType.LEFT);

		return em.createQuery(criteria).getResultList();
	}

	@Override
    @SuppressWarnings({"unchecked"})
	public List<ServiceEntity> findByAdminRole(RoleEntity role) {
		return em.createQuery("select e from ServiceEntity e where e.adminRole = :role")
				.setParameter("role", role).getResultList();
	}
	
	@Override
    @SuppressWarnings({"unchecked"})
	public List<ServiceEntity> findByApproverRole(RoleEntity role) {
		return em.createQuery("select e from ServiceEntity e where e.approverRole = :role")
				.setParameter("role", role).getResultList();
	}
	
	@Override
	public List<ServiceEntity> findAllWithPolicies() {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<ServiceEntity> criteria = builder.createQuery(ServiceEntity.class);
		Root<ServiceEntity> root = criteria.from(ServiceEntity.class);
		criteria.select(root);
		criteria.distinct(true);
		root.fetch("policies", JoinType.LEFT);

		return em.createQuery(criteria).getResultList();
	}


	@Override
	public ServiceEntity findWithPolicies(Long id) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<ServiceEntity> criteria = builder.createQuery(ServiceEntity.class);
		Root<ServiceEntity> root = criteria.from(ServiceEntity.class);
		criteria.where(
				builder.equal(root.get("id"), id));
		criteria.select(root);
		criteria.distinct(true);
		root.fetch("policies", JoinType.LEFT);

		return em.createQuery(criteria).getSingleResult();
	}

	@Override
	public List<ServiceEntity> findAllByImage(ImageEntity image) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<ServiceEntity> criteria = builder.createQuery(ServiceEntity.class);
		Root<ServiceEntity> root = criteria.from(ServiceEntity.class);
		criteria.select(root);
		criteria.where(builder.equal(root.get("image"), image));

		return em.createQuery(criteria).getResultList();
	}
	
	@Override
	public ServiceEntity findById(Long id) {
		return em.find(ServiceEntity.class, id);
	}

	@Override
	public ServiceEntity findByIdWithServiceProps(Long id) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<ServiceEntity> criteria = builder.createQuery(ServiceEntity.class);
		Root<ServiceEntity> root = criteria.from(ServiceEntity.class);
		criteria.where(
				builder.equal(root.get("id"), id));
		criteria.select(root);
		criteria.distinct(true);
		root.fetch("serviceProps", JoinType.LEFT);
		root.fetch("policies", JoinType.LEFT);

		try {
			return em.createQuery(criteria).getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}			
	}

	@Override
	public void delete(ServiceEntity entity) {
		entity = merge(entity);
		em.remove(entity);
	}
}
