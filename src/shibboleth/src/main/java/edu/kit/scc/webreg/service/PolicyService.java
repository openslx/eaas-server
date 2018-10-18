package edu.kit.scc.webreg.service;

import edu.kit.scc.webreg.entity.PolicyEntity;

public interface PolicyService extends BaseService<PolicyEntity> {

	PolicyEntity findWithAgreemets(Long id);
	
}
