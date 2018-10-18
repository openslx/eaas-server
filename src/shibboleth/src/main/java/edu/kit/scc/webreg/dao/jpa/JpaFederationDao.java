package edu.kit.scc.webreg.dao.jpa;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import edu.kit.scc.webreg.dao.FederationDao;
import edu.kit.scc.webreg.entity.FederationEntity;

@Named
@ApplicationScoped
public class JpaFederationDao implements FederationDao {

    @PersistenceContext(unitName = "shib")
    private EntityManager em;
	
	@Override
	public FederationEntity createNew() {
		return new FederationEntity();
	}

	@Override
	public FederationEntity persist(FederationEntity entity) {
		entity = merge(entity);
		em.persist(entity);
		return entity;
	}

	@Override
	public FederationEntity merge(FederationEntity entity) {
		if (em.contains(entity))
			return entity;
		else
			return em.merge(entity);
	}

	@Override
	public List<FederationEntity> findAll() {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<FederationEntity> criteria = builder.createQuery(FederationEntity.class);
		Root<FederationEntity> root = criteria.from(FederationEntity.class);
		criteria.select(root);
		criteria.orderBy(builder.asc(root.get("id")));
		
		return em.createQuery(criteria).getResultList();
	}

	@Override
	public List<FederationEntity> findAllWithIdpEntities() {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<FederationEntity> criteria = builder.createQuery(FederationEntity.class);
		Root<FederationEntity> root = criteria.from(FederationEntity.class);
		criteria.select(root).distinct(true);
		root.fetch("idpEntities", JoinType.LEFT);

		return em.createQuery(criteria).getResultList();
	}

	@Override
	public FederationEntity findById(Long id) {
		return em.find(FederationEntity.class, id);
	}

	@Override
	public FederationEntity findWithIdpEntities(Long id) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<FederationEntity> criteria = builder.createQuery(FederationEntity.class);
		Root<FederationEntity> root = criteria.from(FederationEntity.class);
		criteria.where(
				builder.equal(root.get("id"), id));
		criteria.select(root);
		root.fetch("idpEntities", JoinType.LEFT);

		return em.createQuery(criteria).getSingleResult();
	}

	@Override
	public void delete(FederationEntity entity) {
		entity = merge(entity);
		em.remove(entity);
	}
}
