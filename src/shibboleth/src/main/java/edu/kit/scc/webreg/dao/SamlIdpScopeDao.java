package edu.kit.scc.webreg.dao;

import java.util.List;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlIdpScopeEntity;

public interface SamlIdpScopeDao extends BaseDao<SamlIdpScopeEntity> {

	List<SamlIdpScopeEntity> findByIdp(SamlIdpMetadataEntity idp);

}
