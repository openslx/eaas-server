package edu.kit.scc.webreg.dao.jpa;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import edu.kit.scc.webreg.dao.AuditEntryDao;
import edu.kit.scc.webreg.entity.AuditEntryEntity;

@Named
@ApplicationScoped
public class JpaAuditEntryDao implements AuditEntryDao {

    @PersistenceContext(unitName = "shib")
    private EntityManager em;
	
	@Override
	public AuditEntryEntity createNew() {
		return new AuditEntryEntity();
	}

	@Override
	public AuditEntryEntity persist(AuditEntryEntity entity) {
		entity = merge(entity);
		em.persist(entity);
		return entity;
	}

	@Override
	public AuditEntryEntity merge(AuditEntryEntity entity) {
		if (em.contains(entity))
			return entity;
		else
			return em.merge(entity);
	}

	@Override
    @SuppressWarnings({"unchecked"})
	public List<AuditEntryEntity> findAll() {
		return em.createQuery("select e from AuditEntryEntity e").getResultList();
	}

	@Override
	public AuditEntryEntity findById(Long id) {
		return em.find(AuditEntryEntity.class, id);
	}

	@Override
	public void delete(AuditEntryEntity entity) {
		entity = merge(entity);
		em.remove(entity);
	}
}
