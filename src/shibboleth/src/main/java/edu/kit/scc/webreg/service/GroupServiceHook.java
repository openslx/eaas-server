package edu.kit.scc.webreg.service;

import java.util.Map;
import edu.kit.scc.webreg.dao.GroupDao;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.RegisterException;

public interface GroupServiceHook {

	public GroupEntity preUpdateUserPrimaryGroupFromAttribute(GroupDao dao, GroupEntity group, UserEntity user, Map<String, String> attributeMap) 
			throws RegisterException;

	public GroupEntity postUpdateUserPrimaryGroupFromAttribute(GroupDao dao, GroupEntity group, UserEntity user, Map<String, String> attributeMap) 
			throws RegisterException;

	public void preUpdateUserSecondaryGroupFromAttribute(GroupDao dao, UserEntity user, Map<String, String> attributeMap) 
			throws RegisterException;

	public void postUpdateUserSecondaryGroupFromAttribute(GroupDao dao, UserEntity user, Map<String, String> attributeMap) 
			throws RegisterException;

	boolean isPrimaryResponsible(UserEntity user, Map<String, String> attributeMap);

	boolean isPrimaryCompleteOverride();

	boolean isSecondaryResponsible(UserEntity user, Map<String, String> attributeMap);

	boolean isSecondaryCompleteOverride();
	
}
