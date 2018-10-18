package edu.kit.scc.webreg.dao;

import java.util.List;
import edu.kit.scc.webreg.entity.FederationEntity;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;

public interface SamlIdpMetadataDao extends BaseDao<SamlIdpMetadataEntity> {

	List<SamlIdpMetadataEntity> findAllByFederation(FederationEntity federation);

	List<SamlIdpMetadataEntity> findAllByFederationOrderByOrgname(
			FederationEntity federation);

	SamlIdpMetadataEntity findByEntityId(String entityId);

	SamlIdpMetadataEntity findByScope(String scope);

	SamlIdpMetadataEntity findByFederationAndEntityId(
			FederationEntity federation, String entityId);

}
