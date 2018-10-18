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

import edu.kit.scc.webreg.dao.AdminRoleDao;
import edu.kit.scc.webreg.entity.AdminRoleEntity;
import edu.kit.scc.webreg.entity.UserEntity;

@Named
@ApplicationScoped
public class JpaAdminRoleDao implements AdminRoleDao {

    @PersistenceContext(unitName = "shib")
    private EntityManager em;

	@Override
	public AdminRoleEntity createNew() {
		return new AdminRoleEntity();
	}

	@Override
	public AdminRoleEntity persist(AdminRoleEntity entity) {
		entity = merge(entity);
		em.persist(entity);
		return entity;
	}

	@Override
	public AdminRoleEntity merge(AdminRoleEntity entity) {
		if (em.contains(entity))
			return entity;
		else
			return em.merge(entity);
	}

	@Override
    @SuppressWarnings({"unchecked"})
	public List<AdminRoleEntity> findAll() {
		return em.createQuery("select e from AdminRoleEntity e").getResultList();
	}

	@Override
	public AdminRoleEntity findById(Long id) {
		return em.find(AdminRoleEntity.class, id);
	}

	@Override
	public List<AdminRoleEntity> findWithServices(UserEntity user) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<AdminRoleEntity> criteria = builder.createQuery(AdminRoleEntity.class);
		Root<AdminRoleEntity> root = criteria.from(AdminRoleEntity.class);
		Root<UserEntity> userRoot = criteria.from(UserEntity.class);
		
		CriteriaQuery<AdminRoleEntity> select = criteria.select(root);
		select.where(builder.equal(userRoot.get("id"), user.getId())).distinct(true);
		root.fetch("adminForServices", JoinType.LEFT);
		return em.createQuery(select).getResultList();
	}

	@Override
	public AdminRoleEntity findWithUsers(Long id) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<AdminRoleEntity> criteria = builder.createQuery(AdminRoleEntity.class);
		Root<AdminRoleEntity> root = criteria.from(AdminRoleEntity.class);
		criteria.where(
				builder.equal(root.get("id"), id));
		criteria.select(root);
		root.fetch("users", JoinType.LEFT);
		
		return em.createQuery(criteria).getSingleResult();
	}

	@Override
	public AdminRoleEntity findByName(String name) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<AdminRoleEntity> criteria = builder.createQuery(AdminRoleEntity.class);
		Root<AdminRoleEntity> role = criteria.from(AdminRoleEntity.class);
		criteria.where(
				builder.equal(role.get("name"), name));
		criteria.select(role);
		
		return em.createQuery(criteria).getSingleResult();
	}	
	
	@Override
	public void delete(AdminRoleEntity entity) {
		entity = merge(entity);
		em.remove(entity);
	}
}
