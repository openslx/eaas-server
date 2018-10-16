package edu.kit.scc.webreg.service;

import java.util.List;
import edu.kit.scc.webreg.entity.AuditDetailEntity;

public interface AuditDetailService extends BaseService<AuditDetailEntity> {

	List<AuditDetailEntity> findNewestFailed(int limit);

}
