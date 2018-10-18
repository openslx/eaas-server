package edu.kit.scc.webreg.dao;

import java.util.List;
import edu.kit.scc.webreg.entity.FederationEntity;

public interface FederationDao extends BaseDao<FederationEntity> {

	FederationEntity findWithIdpEntities(Long id);

	List<FederationEntity> findAllWithIdpEntities();

}
