package edu.kit.scc.webreg.dao.jpa;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import edu.kit.scc.webreg.dao.SamlIdpScopeDao;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlIdpScopeEntity;

@Named
@ApplicationScoped
public class JpaSamlIdpScopeDao implements SamlIdpScopeDao {

    @PersistenceContext(unitName = "shib")
    private EntityManager em;
	
	@Override
	public SamlIdpScopeEntity createNew() {
		return new SamlIdpScopeEntity();
	}

	@Override
	public SamlIdpScopeEntity persist(SamlIdpScopeEntity entity) {
		entity = merge(entity);
		em.persist(entity);
		return entity;
	}

	@Override
	public SamlIdpScopeEntity merge(SamlIdpScopeEntity entity) {
		if (em.contains(entity))
			return entity;
		else
			return em.merge(entity);
	}

	@Override
    @SuppressWarnings({"unchecked"})
	public List<SamlIdpScopeEntity> findAll() {
		return em.createQuery("select e from SamlIdpScopeEntity e").getResultList();
	}

	@Override
    @SuppressWarnings({"unchecked"})
	public List<SamlIdpScopeEntity> findByIdp(SamlIdpMetadataEntity idp) {
		return em.createQuery("select e from SamlIdpScopeEntity e where e.idp = :idp")
				.setParameter("idp", idp).getResultList();
	}

	@Override
	public SamlIdpScopeEntity findById(Long id) {
		return em.find(SamlIdpScopeEntity.class, id);
	}

	@Override
	public void delete(SamlIdpScopeEntity entity) {
		entity = merge(entity);
		em.remove(entity);
	}
}
