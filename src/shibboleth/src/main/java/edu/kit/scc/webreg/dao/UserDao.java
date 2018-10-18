package edu.kit.scc.webreg.dao;

import java.util.List;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.UserEntity;

public interface UserDao extends BaseDao<UserEntity> {

	List<UserEntity> findAllWithRoles();
	UserEntity findByPersistentWithRoles(String spId, String idpId,
			String persistentId);
	UserEntity findByEppn(String eppn);
	UserEntity findByIdWithRoles(Long id);
	List<RoleEntity> findRolesForUserById(Long id);
	UserEntity findByIdWithAll(Long id);
}
