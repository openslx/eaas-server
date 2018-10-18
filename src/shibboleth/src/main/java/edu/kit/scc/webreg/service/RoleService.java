package edu.kit.scc.webreg.service;

import java.util.List;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.UserEntity;

public interface RoleService extends BaseService<RoleEntity> {

	RoleEntity findByName(String name);

	RoleEntity findWithUsers(Long id);

	List<RoleEntity> findByUser(UserEntity user);
	
}
