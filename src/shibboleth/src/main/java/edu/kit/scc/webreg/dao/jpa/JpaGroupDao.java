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
import javax.persistence.criteria.Root;

import edu.kit.scc.webreg.dao.GroupDao;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.UserEntity;

@Named
@ApplicationScoped
public class JpaGroupDao implements GroupDao {

    @PersistenceContext(unitName = "shib")
    private EntityManager em;
	
	@Override
	public GroupEntity createNew() {
		return new GroupEntity();
	}

	@Override
	public GroupEntity persist(GroupEntity entity) {
		entity = merge(entity);
		em.persist(entity);
		return entity;
	}

	@Override
	public GroupEntity merge(GroupEntity entity) {
		if (em.contains(entity))
			return entity;
		else
			return em.merge(entity);
	}

	@Override
    @SuppressWarnings({"unchecked"})
	public List<GroupEntity> findAll() {
		return em.createQuery("select e from GroupEntity e").getResultList();
	}

	@Override
	public List<GroupEntity> findByUser(UserEntity user) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
	    CriteriaQuery<GroupEntity> criteria = builder.createQuery(GroupEntity.class);
	    Root<UserEntity> userRoot = criteria.from(UserEntity.class);
	    criteria.where(builder.equal(userRoot.get("id"), user.getId()));
	    Join<UserEntity, GroupEntity> users = userRoot.join("groups");
	    CriteriaQuery<GroupEntity> cq = criteria.select(users);
	    TypedQuery<GroupEntity> query = em.createQuery(cq);
	    return query.getResultList();
	}

	
	@Override
	public GroupEntity findById(Long id) {
		return em.find(GroupEntity.class, id);
	}

	@Override
	public GroupEntity findByGidNumber(Integer gid) {
		try {
			return (GroupEntity) em.createQuery("select e from GroupEntity e where e.gidNumber = :gidNumber")
				.setParameter("gidNumber", gid).getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public GroupEntity findByName(String name) {
		try {
			return (GroupEntity) em.createQuery("select e from GroupEntity e where e.name = :name")
				.setParameter("name", name).getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public GroupEntity findByNameAndPrefix(String name, String prefix) {
		try {
			return (GroupEntity) em.createQuery("select e from GroupEntity e where e.name = :name and e.prefix = :prefix")
				.setParameter("name", name).setParameter("prefix", prefix).getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public void delete(GroupEntity entity) {
		entity = merge(entity);
		em.remove(entity);
	}
}
