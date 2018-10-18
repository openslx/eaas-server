package edu.kit.scc.webreg.service;

import java.util.List;
import edu.kit.scc.webreg.entity.AdminUserEntity;
import edu.kit.scc.webreg.entity.RoleEntity;

public interface AdminUserService extends BaseService<AdminUserEntity> {

	AdminUserEntity findByUsernameAndPassword(String username, String password);

	List<RoleEntity> findRolesForUserById(Long id);

	AdminUserEntity findByUsername(String username);

}
