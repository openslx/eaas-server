package edu.kit.scc.webreg.dao.jpa;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import edu.kit.scc.webreg.dao.AdminUserDao;
import edu.kit.scc.webreg.entity.AdminUserEntity;
import edu.kit.scc.webreg.entity.RoleEntity;

@Named
@ApplicationScoped
public class JpaAdminUserDao implements AdminUserDao {

    @PersistenceContext(unitName = "shib")
    private EntityManager em;
	
	@Override
	public AdminUserEntity createNew() {
		return new AdminUserEntity();
	}

	@Override
	public AdminUserEntity persist(AdminUserEntity entity) {
		entity = merge(entity);
		em.persist(entity);
		return entity;
	}

	@Override
	public AdminUserEntity merge(AdminUserEntity entity) {
		if (em.contains(entity))
			return entity;
		else
			return em.merge(entity);
	}

	@Override
    @SuppressWarnings({"unchecked"})
	public List<AdminUserEntity> findAll() {
		return em.createQuery("select e from AdminUserEntity e").getResultList();
	}

	@Override
	public AdminUserEntity findByUsernameAndPassword(String username, String password) {
		try {
			return (AdminUserEntity) em.createQuery("select e from AdminUserEntity e " +
					"where e.username = :username and e.password = :password")
					.setParameter("username", username).setParameter("password", password)
					.getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}	
	
	@Override
	public AdminUserEntity findByUsername(String username) {
		try {
			return (AdminUserEntity) em.createQuery("select e from AdminUserEntity e " +
					"where e.username = :username")
					.setParameter("username", username)
					.getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}	
	
	@Override
    @SuppressWarnings({"unchecked"})
	public List<RoleEntity> findRolesForUserById(Long id) {
		return em.createQuery("select e.roles from AdminUserEntity e where e.id = :id")
				.setParameter("id", id).getResultList();
	}
	
	@Override
	public AdminUserEntity findById(Long id) {
		return em.find(AdminUserEntity.class, id);
	}

	@Override
	public void delete(AdminUserEntity entity) {
		entity = merge(entity);
		em.remove(entity);
	}
}
