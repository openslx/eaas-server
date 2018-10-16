package edu.kit.scc.webreg.service;

import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;

public interface SamlSpConfigurationService extends BaseService<SamlSpConfigurationEntity> {

	SamlSpConfigurationEntity findByHostname(String hostname);

	SamlSpConfigurationEntity findByEntityId(String entityId);

}
