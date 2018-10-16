package edu.kit.scc.webreg.dao;

import java.util.List;
import edu.kit.scc.webreg.entity.ApproverRoleEntity;
import edu.kit.scc.webreg.entity.UserEntity;

public interface ApproverRoleDao extends BaseDao<ApproverRoleEntity> {

	ApproverRoleEntity findByName(String name);

	ApproverRoleEntity findWithUsers(Long id);

	List<ApproverRoleEntity> findWithServices(UserEntity user);
}
