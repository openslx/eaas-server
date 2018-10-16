package edu.kit.scc.webreg.service;

import java.util.List;
import edu.kit.scc.webreg.entity.ApproverRoleEntity;
import edu.kit.scc.webreg.entity.UserEntity;

public interface ApproverRoleService extends BaseService<ApproverRoleEntity> {

	ApproverRoleEntity findByName(String name);

	ApproverRoleEntity findWithUsers(Long id);

	List<ApproverRoleEntity> findWithServices(UserEntity user);
	
}
