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

import edu.kit.scc.webreg.dao.ApproverRoleDao;
import edu.kit.scc.webreg.entity.ApproverRoleEntity;
import edu.kit.scc.webreg.entity.UserEntity;

@Named
@ApplicationScoped
public class JpaApproverRoleDao implements ApproverRoleDao {

    @PersistenceContext(unitName = "shib")
    private EntityManager em;
	
	@Override
	public ApproverRoleEntity createNew() {
		return new ApproverRoleEntity();
	}

	@Override
	public ApproverRoleEntity persist(ApproverRoleEntity entity) {
		entity = merge(entity);
		em.persist(entity);
		return entity;
	}

	@Override
	public ApproverRoleEntity merge(ApproverRoleEntity entity) {
		if (em.contains(entity))
			return entity;
		else
			return em.merge(entity);
	}

	@Override
    @SuppressWarnings({"unchecked"})
	public List<ApproverRoleEntity> findAll() {
		return em.createQuery("select e from ApproverRoleEntity e").getResultList();
	}

	@Override
	public ApproverRoleEntity findById(Long id) {
		return em.find(ApproverRoleEntity.class, id);
	}

	@Override
	public List<ApproverRoleEntity> findWithServices(UserEntity user) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<ApproverRoleEntity> criteria = builder.createQuery(ApproverRoleEntity.class);
		Root<ApproverRoleEntity> root = criteria.from(ApproverRoleEntity.class);
		Root<UserEntity> userRoot = criteria.from(UserEntity.class);
		
		CriteriaQuery<ApproverRoleEntity> select = criteria.select(root);
		select.where(builder.equal(userRoot.get("id"), user.getId())).distinct(true);
		root.fetch("approverForServices", JoinType.LEFT);
		return em.createQuery(select).getResultList();
	}

	@Override
	public ApproverRoleEntity findWithUsers(Long id) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<ApproverRoleEntity> criteria = builder.createQuery(ApproverRoleEntity.class);
		Root<ApproverRoleEntity> root = criteria.from(ApproverRoleEntity.class);
		criteria.where(builder.equal(root.get("id"), id));
		criteria.select(root);
		root.fetch("users", JoinType.LEFT);
		
		return em.createQuery(criteria).getSingleResult();
	}

	@Override
	public ApproverRoleEntity findByName(String name) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<ApproverRoleEntity> criteria = builder.createQuery(ApproverRoleEntity.class);
		Root<ApproverRoleEntity> role = criteria.from(ApproverRoleEntity.class);
		criteria.where(
				builder.equal(role.get("name"), name));
		criteria.select(role);
		
		return em.createQuery(criteria).getSingleResult();
	}	
	
	@Override
	public void delete(ApproverRoleEntity entity) {
		entity = merge(entity);
		em.remove(entity);
	}
}
