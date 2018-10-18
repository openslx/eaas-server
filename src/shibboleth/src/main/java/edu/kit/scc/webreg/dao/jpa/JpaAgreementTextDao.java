package edu.kit.scc.webreg.dao.jpa;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import edu.kit.scc.webreg.dao.AgreementTextDao;
import edu.kit.scc.webreg.entity.AgreementTextEntity;

@Named
@ApplicationScoped
public class JpaAgreementTextDao implements AgreementTextDao {

    @PersistenceContext(unitName = "shib")
    private EntityManager em;
	
	@Override
	public AgreementTextEntity createNew() {
		return new AgreementTextEntity();
	}

	@Override
	public AgreementTextEntity persist(AgreementTextEntity entity) {
		entity = merge(entity);
		em.persist(entity);
		return entity;
	}

	@Override
	public AgreementTextEntity merge(AgreementTextEntity entity) {
		if (em.contains(entity))
			return entity;
		else
			return em.merge(entity);
	}

	@Override
    @SuppressWarnings({"unchecked"})
	public List<AgreementTextEntity> findAll() {
		return em.createQuery("select e from AgreementTextEntity e").getResultList();
	}

	@Override
	public AgreementTextEntity findById(Long id) {
		return em.find(AgreementTextEntity.class, id);
	}

	@Override
	public void delete(AgreementTextEntity entity) {
		entity = merge(entity);
		em.remove(entity);
	}
}
