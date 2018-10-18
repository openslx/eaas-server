package edu.kit.scc.webreg.dao.jpa;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import edu.kit.scc.webreg.dao.SamlIdpMetadataDao;
import edu.kit.scc.webreg.entity.FederationEntity;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;

@Named
@ApplicationScoped
public class JpaSamlIdpMetadataDao implements SamlIdpMetadataDao {

    @PersistenceContext(unitName = "shib")
    private EntityManager em;
	
	@Override
	public SamlIdpMetadataEntity createNew() {
		return new SamlIdpMetadataEntity();
	}

	@Override
	public SamlIdpMetadataEntity persist(SamlIdpMetadataEntity entity) {
		entity = merge(entity);
		em.persist(entity);
		return entity;
	}

	@Override
	public SamlIdpMetadataEntity merge(SamlIdpMetadataEntity entity) {
		if (em.contains(entity))
			return entity;
		else
			return em.merge(entity);
	}

	@Override
    @SuppressWarnings({"unchecked"})
	public List<SamlIdpMetadataEntity> findAll() {
		return em.createQuery("select e from SamlIdpMetadataEntity e").getResultList();
	}

	@Override
	public List<SamlIdpMetadataEntity> findAllByFederation(FederationEntity federation) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<SamlIdpMetadataEntity> criteria = builder.createQuery(SamlIdpMetadataEntity.class);
		Root<SamlIdpMetadataEntity> root = criteria.from(SamlIdpMetadataEntity.class);
		criteria.where(
				builder.equal(root.get("federation"), federation));
		criteria.select(root);
		
		return em.createQuery(criteria).getResultList();
	}	

	@Override
	public SamlIdpMetadataEntity findByFederationAndEntityId(FederationEntity federation, String entityId) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<SamlIdpMetadataEntity> criteria = builder.createQuery(SamlIdpMetadataEntity.class);
		Root<SamlIdpMetadataEntity> root = criteria.from(SamlIdpMetadataEntity.class);
		criteria.where(builder.and(
				builder.equal(root.get("federation"), federation),
				builder.equal(root.get("entityId"), entityId)));
		criteria.select(root);
		
		try {
			return em.createQuery(criteria).getSingleResult();
		} 
		catch (NoResultException e) {
			return null;
		}
	}	
	
	@Override
	public SamlIdpMetadataEntity findByEntityId(String entityId) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<SamlIdpMetadataEntity> criteria = builder.createQuery(SamlIdpMetadataEntity.class);
		Root<SamlIdpMetadataEntity> root = criteria.from(SamlIdpMetadataEntity.class);
		criteria.where(
				builder.equal(root.get("entityId"), entityId));
		criteria.select(root);
		
		List<SamlIdpMetadataEntity> idps = em.createQuery(criteria).getResultList();
		if (idps.size() < 1)
			return null;
		else
			return idps.get(0);
	}	

	@Override
	@SuppressWarnings("unchecked")
	public SamlIdpMetadataEntity findByScope(String scope) {
		List<SamlIdpMetadataEntity> idpList = em.createQuery(
				"select e from SamlIdpMetadataEntity as e join e.scopes as s where s.scope = :scope")
				.setParameter("scope", scope).getResultList();
		
		/*
		 * Always return first idp found for scope. Could be more than one.
		 */
		
		if (idpList.size() == 0)
			return null;
		else
			return idpList.get(0);
	}	
	
	@Override
	public List<SamlIdpMetadataEntity> findAllByFederationOrderByOrgname(FederationEntity federation) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<SamlIdpMetadataEntity> criteria = builder.createQuery(SamlIdpMetadataEntity.class);
		Root<SamlIdpMetadataEntity> root = criteria.from(SamlIdpMetadataEntity.class);
		criteria.where(
				builder.equal(root.get("federation"), federation));
		criteria.select(root);
		criteria.orderBy(builder.asc(root.get("orgName")));
		return em.createQuery(criteria).getResultList();
	}	
	
	@Override
	public SamlIdpMetadataEntity findById(Long id) {
		return em.find(SamlIdpMetadataEntity.class, id);
	}

	@Override
	public void delete(SamlIdpMetadataEntity entity) {
		entity = merge(entity);
		em.remove(entity);
	}
}
