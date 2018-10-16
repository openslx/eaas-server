package edu.kit.scc.webreg.service;

import java.util.List;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlIdpScopeEntity;

public interface SamlIdpScopeService extends BaseService<SamlIdpScopeEntity> {

	List<SamlIdpScopeEntity> findByIdp(SamlIdpMetadataEntity idp);

}
