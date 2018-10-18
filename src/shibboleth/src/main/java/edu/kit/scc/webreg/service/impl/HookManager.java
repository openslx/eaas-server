package edu.kit.scc.webreg.service.impl;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;

import edu.kit.scc.webreg.service.GroupServiceHook;
import edu.kit.scc.webreg.service.UserServiceHook;

@Singleton
public class HookManager {

	private Set<UserServiceHook> userHooks;
	private Set<GroupServiceHook> groupHooks;

	@PostConstruct
	public void init() {
		groupHooks = new HashSet<GroupServiceHook>();
		userHooks = new HashSet<UserServiceHook>();
	}
	
	public void addGroupHook(GroupServiceHook hook) {
		groupHooks.add(hook);
	}

	public void addUserHook(UserServiceHook hook) {
		userHooks.add(hook);
	}

	public Set<GroupServiceHook> getGroupHooks() {
		return groupHooks;
	}

	public Set<UserServiceHook> getUserHooks() {
		return userHooks;
	}
	
}
