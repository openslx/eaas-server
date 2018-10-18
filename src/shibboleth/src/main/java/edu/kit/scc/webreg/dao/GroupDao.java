package edu.kit.scc.webreg.dao;

import java.util.List;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.UserEntity;

public interface GroupDao extends BaseDao<GroupEntity> {

	GroupEntity findByGidNumber(Integer gid);

	GroupEntity findByName(String name);

	GroupEntity findByNameAndPrefix(String name, String prefix);

	List<GroupEntity> findByUser(UserEntity user);

}
