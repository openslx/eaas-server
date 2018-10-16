package edu.kit.scc.webreg.service;

import java.util.List;
import java.util.Map;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.RegisterException;

public interface UserService extends BaseService<UserEntity> {

	List<UserEntity> findAllWithRoles();
	UserEntity findByPersistentWithRoles(String spId, String idpId,
			String persistentId);
	UserEntity mergeAndSave(UserEntity entity);
	UserEntity findByEppn(String eppn);
	UserEntity findByIdWithRoles(Long id);
	List<RoleEntity> findRolesForUserById(Long id);
	UserEntity findByIdWithAll(Long id);
	void updateUserFromAttribute(UserEntity user,
			Map<String, String> attributeMap) throws RegisterException;
}
