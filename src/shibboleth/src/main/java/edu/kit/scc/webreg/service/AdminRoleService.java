package edu.kit.scc.webreg.service;

import java.util.List;
import edu.kit.scc.webreg.entity.AdminRoleEntity;
import edu.kit.scc.webreg.entity.UserEntity;

public interface AdminRoleService extends BaseService<AdminRoleEntity> {

	AdminRoleEntity findByName(String name);

	AdminRoleEntity findWithUsers(Long id);

	List<AdminRoleEntity> findWithServices(UserEntity user);
	
}
