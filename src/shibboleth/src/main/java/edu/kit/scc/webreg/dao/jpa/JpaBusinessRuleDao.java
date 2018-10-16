package edu.kit.scc.webreg.dao.jpa;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import edu.kit.scc.webreg.dao.BusinessRuleDao;
import edu.kit.scc.webreg.entity.BusinessRuleEntity;

import javax.inject.Named;

//@Named
//@New
//@ApplicationScoped
@Named
@ApplicationScoped
public class JpaBusinessRuleDao implements BusinessRuleDao {

    @PersistenceContext(unitName = "shib")
    private EntityManager em;
	
	@Override
	public BusinessRuleEntity createNew() {
		return new BusinessRuleEntity();
	}

	@Override
	public BusinessRuleEntity persist(BusinessRuleEntity entity) {
		entity = merge(entity);
		em.persist(entity);
		return entity;
	}

	@Override
	public BusinessRuleEntity merge(BusinessRuleEntity entity) {
		if (em.contains(entity))
			return entity;
		else
			return em.merge(entity);
	}

	@Override
    @SuppressWarnings({"unchecked"})
	public List<BusinessRuleEntity> findAll() {
		return em.createQuery("select e from BusinessRuleEntity e").getResultList();
	}

	@Override
	public BusinessRuleEntity findById(Long id) {
		return em.find(BusinessRuleEntity.class, id);
	}

	@Override
	public void delete(BusinessRuleEntity entity) {
		entity = merge(entity);
		em.remove(entity);
	}
}
