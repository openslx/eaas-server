package edu.kit.scc.webreg.service;

import java.util.List;
import org.opensaml.saml2.metadata.EntityDescriptor;
import edu.kit.scc.webreg.entity.FederationEntity;
import edu.kit.scc.webreg.service.impl.FederationServiceImpl;

/**
 * Known implementations: {@link FederationServiceImpl}
 */
public interface FederationService extends BaseService<FederationEntity> {

	void updateEntities(FederationEntity entity,
			List<EntityDescriptor> entityList);

	FederationEntity findWithIdpEntities(Long id);

	List<FederationEntity> findAllWithIdpEntities();

}
