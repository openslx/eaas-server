package edu.kit.scc.webreg.dao;

import java.util.List;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.UserEntity;

public interface RoleDao extends BaseDao<RoleEntity> {

	RoleEntity findByName(String name);

	RoleEntity findWithUsers(Long id);

	List<RoleEntity> findByUser(UserEntity user);
	
}
