package edu.kit.scc.webreg.dao.jpa;

import java.io.Serializable;
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

import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.UserEntity;

@Named
@ApplicationScoped
public class JpaUserDao implements UserDao, Serializable {

	private static final long serialVersionUID = 1L;

    @PersistenceContext(unitName = "shib")
    private EntityManager em;
	
	@Override
	public UserEntity createNew() {
		return new UserEntity();
	}

	@Override
	public UserEntity persist(UserEntity entity) {
		entity = merge(entity);
		em.persist(entity);
		return entity;
	}

	@Override
	public UserEntity merge(UserEntity entity) {
		if (em.contains(entity))
			return entity;
		else
			return em.merge(entity);
	}

	@Override
    @SuppressWarnings({"unchecked"})
	public List<UserEntity> findAll() {
		return em.createQuery("select e from UserEntity e").getResultList();
	}

	@Override
    @SuppressWarnings({"unchecked"})
	public List<UserEntity> findAllWithRoles() {
		return em.createQuery("select distinct e from UserEntity e left join fetch e.roles").getResultList();
	}

	@Override
    @SuppressWarnings({"unchecked"})
	public List<RoleEntity> findRolesForUserById(Long id) {
		return em.createQuery("select e.roles from UserEntity e where e.id = :id")
				.setParameter("id", id).getResultList();
	}
	
	@Override
	public UserEntity findByPersistentWithRoles(String spId, String idpId, String persistentId) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<UserEntity> criteria = builder.createQuery(UserEntity.class);
		Root<UserEntity> user = criteria.from(UserEntity.class);
		criteria.where(builder.and(
				builder.equal(user.get("persistentSpId"), spId),
				builder.equal(user.get("persistentIdpId"), idpId),
				builder.equal(user.get("persistentId"), persistentId)
				));
		criteria.select(user);
		criteria.distinct(true);
		user.fetch("roles", JoinType.LEFT);
		user.fetch("groups", JoinType.LEFT);
		user.fetch("genericStore", JoinType.LEFT);
		user.fetch("attributeStore", JoinType.LEFT);
		
		try {
			return em.createQuery(criteria).getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}			
	}	

	@Override
	public UserEntity findByEppn(String eppn) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<UserEntity> criteria = builder.createQuery(UserEntity.class);
		Root<UserEntity> user = criteria.from(UserEntity.class);
		criteria.where(builder.equal(user.get("eppn"), eppn));
		criteria.select(user);
		
		try {
			return em.createQuery(criteria).getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}	

	@Override
	public UserEntity findById(Long id) {
		try {
			return em.find(UserEntity.class, id);
		}
		catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public UserEntity findByIdWithRoles(Long id) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<UserEntity> criteria = builder.createQuery(UserEntity.class);
		Root<UserEntity> user = criteria.from(UserEntity.class);
		criteria.where(builder.and(
				builder.equal(user.get("id"), id)
				));
		criteria.select(user);
		criteria.distinct(true);
		user.fetch("roles", JoinType.LEFT);

		try {
			return em.createQuery(criteria).getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}	

	@Override
	public UserEntity findByIdWithAll(Long id) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<UserEntity> criteria = builder.createQuery(UserEntity.class);
		Root<UserEntity> user = criteria.from(UserEntity.class);
		criteria.where(builder.and(
				builder.equal(user.get("id"), id)
				));
		criteria.select(user);
		criteria.distinct(true);
		user.fetch("roles", JoinType.LEFT);
		user.fetch("groups", JoinType.LEFT);
		user.fetch("genericStore", JoinType.LEFT);
		user.fetch("attributeStore", JoinType.LEFT);

		try {
			return em.createQuery(criteria).getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}	

	@Override
	public void delete(UserEntity entity) {
		entity = merge(entity);
		em.remove(entity);
	}
}
