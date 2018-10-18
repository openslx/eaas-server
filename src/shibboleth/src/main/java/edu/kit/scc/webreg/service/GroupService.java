package edu.kit.scc.webreg.service;

import java.util.List;
import java.util.Map;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.RegisterException;

public interface GroupService extends BaseService<GroupEntity> {

	void updateUserPrimaryGroupFromAttribute(UserEntity user,
			Map<String, String> attributeMap) throws RegisterException;

	GroupEntity findByName(String name);

	void updateUserSecondaryGroupsFromAttribute(UserEntity user,
			Map<String, String> attributeMap) throws RegisterException;

	List<GroupEntity> findByUser(UserEntity user);

}
