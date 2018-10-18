package edu.kit.scc.webreg.dao.jpa;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Root;

import edu.kit.scc.webreg.dao.SamlSpConfigurationDao;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;

@Named
@ApplicationScoped
public class JpaSamlSpConfigurationDao implements SamlSpConfigurationDao {

	@PersistenceContext(unitName = "shib")
	private EntityManager em;

	@Override
	public SamlSpConfigurationEntity createNew() {
		return new SamlSpConfigurationEntity();
	}

	@Override
	public SamlSpConfigurationEntity persist(SamlSpConfigurationEntity entity) {
		entity = merge(entity);
		em.persist(entity);
		return entity;
	}

	@Override
	public SamlSpConfigurationEntity merge(SamlSpConfigurationEntity entity) {
		if (em.contains(entity))
			return entity;
		else
			return em.merge(entity);
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public List<SamlSpConfigurationEntity> findAll() {
		return em.createQuery("select e from SamlSpConfigurationEntity e")
				.getResultList();
	}

	@Override
	public SamlSpConfigurationEntity findById(Long id) {
		return em.find(SamlSpConfigurationEntity.class, id);
	}

	@Override
	public SamlSpConfigurationEntity findByEntityId(String entityId) {
		// TODO: Fix this
		// CriteriaBuilder builder = em.getCriteriaBuilder();
		// CriteriaQuery<SamlSpConfigurationEntity> criteria =
		// builder.createQuery(SamlSpConfigurationEntity.class);
		// Root<SamlSpConfigurationEntity> root =
		// criteria.from(SamlSpConfigurationEntity.class);
		// criteria.where(
		// builder.equal(root.get("entityId"), entityId));
		// criteria.select(root);
		//
		// return em.createQuery(criteria).getSingleResult();
		return null;
	}

	@Override
	public SamlSpConfigurationEntity findByHostname(String hostname) {

		// This join will attempt to read SamlSpConfigurationEntity.hostNameList
		// without the appropriate schema declaration.
		// TODO: Fix this
		
		// CriteriaBuilder builder = em.getCriteriaBuilder();
		// CriteriaQuery<SamlSpConfigurationEntity> criteria =
		// builder.createQuery(SamlSpConfigurationEntity.class);
		// Root<SamlSpConfigurationEntity> root =
		// criteria.from(SamlSpConfigurationEntity.class);
		// ListJoin<SamlSpConfigurationEntity, String> elementJoin =
		// root.joinList("hostNameList");
		// criteria.select(root);
		// criteria.where(
		// builder.equal(elementJoin.as(String.class), hostname));
		//
		// return em.createQuery(criteria).getSingleResult();

		List<SamlSpConfigurationEntity> allSPs = findAll();
		for (SamlSpConfigurationEntity sp : allSPs) {
			if (sp.getHostNameList().contains(hostname))
				return sp;
		}
		System.err.println("Could not find hostname " + hostname);
		return null;
	}

	@Override
	public void delete(SamlSpConfigurationEntity entity) {
		entity = merge(entity);
		em.remove(entity);
	}
}
