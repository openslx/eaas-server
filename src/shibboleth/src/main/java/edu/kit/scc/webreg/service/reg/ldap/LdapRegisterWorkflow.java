package edu.kit.scc.webreg.service.reg.ldap;

import java.util.Map;

import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.UserEntity;

public class LdapRegisterWorkflow extends AbstractLdapRegisterWorkflow {

	@Override
	protected String constructHomeDir(String homeId, String homeUid, UserEntity user, Map<String, String> reconMap) {
		return "/home/" + homeId + "/" + reconMap.get("groupName") + "/" + reconMap.get("localUid");
	}

	@Override
	protected String constructLocalUid(String homeId, String homeUid, UserEntity user,
			Map<String, String> reconMap) {
		return homeId + "_" + homeUid;
	}

	@Override
	protected String constructGroupName(GroupEntity group) {
		if (group.getPrefix() == null)
			return "_" + group.getName();
		else
			return group.getPrefix() + "_" +
			group.getName();
	}
}
