package edu.kit.scc.webreg.service;

import java.util.Map;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.RegisterException;

public interface UserServiceHook {

	public void preUpdateUserFromAttribute(UserEntity user, Map<String, String> attributeMap) 
			throws RegisterException;

	public void postUpdateUserFromAttribute(UserEntity user, Map<String, String> attributeMap) 
			throws RegisterException;

	boolean isResponsible(UserEntity user, Map<String, String> attributeMap);

	boolean isCompleteOverride();

}
