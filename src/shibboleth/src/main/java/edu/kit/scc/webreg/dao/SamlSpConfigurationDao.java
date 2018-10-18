package edu.kit.scc.webreg.dao;

import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;

public interface SamlSpConfigurationDao extends BaseDao<SamlSpConfigurationEntity> {

	SamlSpConfigurationEntity findByHostname(String hostname);

	SamlSpConfigurationEntity findByEntityId(String entityId);

}
