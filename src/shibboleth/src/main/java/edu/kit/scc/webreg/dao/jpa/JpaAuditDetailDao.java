package edu.kit.scc.webreg.dao.jpa;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import edu.kit.scc.webreg.dao.AuditDetailDao;
import edu.kit.scc.webreg.entity.AuditDetailEntity;
import edu.kit.scc.webreg.entity.AuditStatus;

@Named
@ApplicationScoped
public class JpaAuditDetailDao implements AuditDetailDao {

    @PersistenceContext(unitName = "shib")
    private EntityManager em;
	
	@Override
	public AuditDetailEntity createNew() {
		return new AuditDetailEntity();
	}

	@Override
	public AuditDetailEntity persist(AuditDetailEntity entity) {
		entity = merge(entity);
		em.persist(entity);
		return entity;
	}

	@Override
	public AuditDetailEntity merge(AuditDetailEntity entity) {
		if (em.contains(entity))
			return entity;
		else
			return em.merge(entity);
	}

	@Override
    @SuppressWarnings({"unchecked"})
	public List<AuditDetailEntity> findAll() {
		return em.createQuery("select e from AuditDetailEntity e").getResultList();
	}

	@Override
    @SuppressWarnings({"unchecked"})
	public List<AuditDetailEntity> findNewestFailed(int limit) {
		return em.createQuery("select e from AuditDetailEntity e where e.auditStatus = :status" +
				" order by e.endTime desc")
				.setParameter("stats", AuditStatus.FAIL)
				.setMaxResults(limit).getResultList();
	}

	@Override
	public AuditDetailEntity findById(Long id) {
		return em.find(AuditDetailEntity.class, id);
	}

	@Override
	public void delete(AuditDetailEntity entity) {
		entity = merge(entity);
		em.remove(entity);
	}
}
