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

import edu.kit.scc.webreg.dao.PolicyDao;
import edu.kit.scc.webreg.entity.PolicyEntity;

@Named
@ApplicationScoped
public class JpaPolicyDao implements PolicyDao {

    @PersistenceContext(unitName = "shib")
    private EntityManager em;
	
	@Override
	public PolicyEntity createNew() {
		return new PolicyEntity();
	}

	@Override
	public PolicyEntity persist(PolicyEntity entity) {
		entity = merge(entity);
		em.persist(entity);
		return entity;
	}

	@Override
	public PolicyEntity merge(PolicyEntity entity) {
		if (em.contains(entity))
			return entity;
		else
			return em.merge(entity);
	}

	@Override
    @SuppressWarnings({"unchecked"})
	public List<PolicyEntity> findAll() {
		return em.createQuery("select e from PolicyEntity e").getResultList();
	}

	@Override
	public PolicyEntity findWithAgreemets(Long id) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<PolicyEntity> criteria = builder.createQuery(PolicyEntity.class);
		Root<PolicyEntity> root = criteria.from(PolicyEntity.class);
		criteria.where(
				builder.equal(root.get("id"), id));
		criteria.select(root);
		root.fetch("agreementTexts", JoinType.LEFT);

		return em.createQuery(criteria).getSingleResult();
	}

	@Override
	public PolicyEntity findById(Long id) {
		return em.find(PolicyEntity.class, id);
	}

	@Override
	public void delete(PolicyEntity entity) {
		entity = merge(entity);
		em.remove(entity);
	}
}
