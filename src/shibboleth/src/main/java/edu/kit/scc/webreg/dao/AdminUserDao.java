package edu.kit.scc.webreg.dao;

import java.util.List;
import edu.kit.scc.webreg.entity.AdminUserEntity;
import edu.kit.scc.webreg.entity.RoleEntity;

public interface AdminUserDao extends BaseDao<AdminUserEntity> {

	AdminUserEntity findByUsernameAndPassword(String username, String password);

	List<RoleEntity> findRolesForUserById(Long id);

	AdminUserEntity findByUsername(String username);

}
