package edu.kit.scc.webreg.dao.jpa;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import edu.kit.scc.webreg.dao.RoleDao;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.UserEntity;

@Named
@ApplicationScoped
public class JpaRoleDao implements RoleDao {

    @PersistenceContext(unitName = "shib")
    private EntityManager em;

	@Override
	public RoleEntity createNew() {
		return new RoleEntity();
	}

	@Override
	public RoleEntity persist(RoleEntity entity) {
		entity = merge(entity);
		em.persist(entity);
		return entity;
	}

	@Override
	public RoleEntity merge(RoleEntity entity) {
		if (em.contains(entity))
			return entity;
		else
			return em.merge(entity);
	}

	@Override
    @SuppressWarnings({"unchecked"})
	public List<RoleEntity> findAll() {
		return em.createQuery("select e from RoleEntity e").getResultList();
	}

	@Override
	public RoleEntity findById(Long id) {
		return em.find(RoleEntity.class, id);
	}

	@Override
	public List<RoleEntity> findByUser(UserEntity user) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
	    CriteriaQuery<RoleEntity> criteria = builder.createQuery(RoleEntity.class);
	    Root<UserEntity> userRoot = criteria.from(UserEntity.class);
	    criteria.where(builder.equal(userRoot.get("id"), user.getId()));
	    Join<UserEntity, RoleEntity> users = userRoot.join("roles");
	    CriteriaQuery<RoleEntity> cq = criteria.select(users);
	    TypedQuery<RoleEntity> query = em.createQuery(cq);
	    return query.getResultList();
	}

	@Override
	public RoleEntity findWithUsers(Long id) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<RoleEntity> criteria = builder.createQuery(RoleEntity.class);
		Root<RoleEntity> root = criteria.from(RoleEntity.class);
		criteria.where(
				builder.equal(root.get("id"), id));
		criteria.select(root);
		root.fetch("users", JoinType.LEFT);
		
		return em.createQuery(criteria).getSingleResult();
	}

	@Override
	public RoleEntity findByName(String name) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<RoleEntity> criteria = builder.createQuery(RoleEntity.class);
		Root<RoleEntity> role = criteria.from(RoleEntity.class);
		criteria.where(
				builder.equal(role.get("name"), name));
		criteria.select(role);
		
		try {		
			return em.createQuery(criteria).getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}	

	@Override
	public void delete(RoleEntity entity) {
		entity = merge(entity);
		em.remove(entity);
	}
}
