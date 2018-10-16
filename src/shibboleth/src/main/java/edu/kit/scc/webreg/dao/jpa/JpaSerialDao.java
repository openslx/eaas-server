package edu.kit.scc.webreg.dao.jpa;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import edu.kit.scc.webreg.dao.SerialDao;
import edu.kit.scc.webreg.entity.SerialEntity;

@Named
@ApplicationScoped
public class JpaSerialDao implements SerialDao {

    @PersistenceContext(unitName = "shib")
    private EntityManager em;
    
	
	@Override
	public SerialEntity createNew() {
		return new SerialEntity();
	}

	@Override
	public SerialEntity persist(SerialEntity entity) {
		entity = merge(entity);
		em.persist(entity);
		return entity;
	}

	@Override
	public SerialEntity merge(SerialEntity entity) {
		if (em.contains(entity))
			return entity;
		else
			return em.merge(entity);
	}

	@Override
    @SuppressWarnings({"unchecked"})
	public List<SerialEntity> findAll() {
		return em.createQuery("select e from UserEntity e").getResultList();
	}

	@Override
	public SerialEntity findByName(String name) {
		try {
			return (SerialEntity) em.createQuery("select e from SerialEntity e where e.name = :name")
					.setParameter("name", name)
					.getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public SerialEntity findById(Long id) {
		return em.find(SerialEntity.class, id);
	}

	@Override
	public void delete(SerialEntity entity) {
		entity = merge(entity);
		em.remove(entity);
	}
}
