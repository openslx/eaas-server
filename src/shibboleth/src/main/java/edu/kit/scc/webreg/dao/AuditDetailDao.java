package edu.kit.scc.webreg.dao;


import java.util.List;
import edu.kit.scc.webreg.entity.AuditDetailEntity;

public interface AuditDetailDao extends BaseDao<AuditDetailEntity> {

	List<AuditDetailEntity> findNewestFailed(int limit);

}
