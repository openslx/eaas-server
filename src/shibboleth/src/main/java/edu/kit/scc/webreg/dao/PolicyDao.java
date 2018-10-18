package edu.kit.scc.webreg.dao;

import edu.kit.scc.webreg.entity.PolicyEntity;

public interface PolicyDao extends BaseDao<PolicyEntity> {

	PolicyEntity findWithAgreemets(Long id);
}
