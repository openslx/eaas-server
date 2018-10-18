package edu.kit.scc.webreg.dao;

import java.util.List;
import edu.kit.scc.webreg.entity.AdminRoleEntity;
import edu.kit.scc.webreg.entity.UserEntity;

public interface AdminRoleDao extends BaseDao<AdminRoleEntity> {

	AdminRoleEntity findByName(String name);

	AdminRoleEntity findWithUsers(Long id);

	List<AdminRoleEntity> findWithServices(UserEntity user);
}
