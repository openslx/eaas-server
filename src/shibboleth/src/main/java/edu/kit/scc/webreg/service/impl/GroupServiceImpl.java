package edu.kit.scc.webreg.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.kit.scc.webreg.dao.GroupDao;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.GroupService;
import edu.kit.scc.webreg.service.GroupServiceHook;
import edu.kit.scc.webreg.service.SerialService;

@Stateless
public class GroupServiceImpl implements GroupService {

	private static final long serialVersionUID = 1L;


//	private Logger logger = Logger;

	@Inject
	private GroupDao dao;
	
	@Inject 
	private SerialService serialService;
	
	@Inject
	private HookManager hookManager;
	
	@Override
	public GroupEntity createNew() {
		return dao.createNew();
	}

	@Override
	public GroupEntity save(GroupEntity entity) {
		return dao.persist(entity);
	}

	@Override
	public void delete(GroupEntity entity) {
		dao.delete(entity);
	}

	@Override
	public List<GroupEntity> findAll() {
		return dao.findAll();
	}

	@Override
	public GroupEntity findById(Long id) {
		return dao.findById(id);
	}

	@Override
	public GroupEntity findByName(String name) {
		return dao.findByName(name);
	}	
	
	@Override
	public List<GroupEntity> findByUser(UserEntity user) {
		return dao.findByUser(user);
	}	
	
	@Override
	public void updateUserPrimaryGroupFromAttribute(UserEntity user, Map<String, String> attributeMap)
			throws RegisterException {

		GroupServiceHook completeOverrideHook = null;
		Set<GroupServiceHook> activeHooks = new HashSet<GroupServiceHook>();

		GroupEntity group = null;

		for (GroupServiceHook hook : hookManager.getGroupHooks()) {
			if (hook.isPrimaryResponsible(user, attributeMap)) {
				group = hook.preUpdateUserPrimaryGroupFromAttribute(dao, group, user, attributeMap);
				activeHooks.add(hook);
				
				if (hook.isPrimaryCompleteOverride()) {
					completeOverrideHook = hook;
				}
			}
		}
		
		if (completeOverrideHook == null) {
			
			String homeId = attributeMap.get("http://bwidm.de/bwidmOrgId");
	
			if (homeId == null) {
//				logger.warn("No Home ID is set for User {}", user.getEppn());
				throw new RegisterException("Es fehlt das Präfix der Heimatorganisation");
			}
			else {
				// TODO: Take SAML Criteria for Primary Group, not only standard group
				
//				logger.info("Setting standard HomeID group for user {}", user.getEppn());
				group = dao.findByNameAndPrefix("default", homeId);
				
				if (group == null) {
					group = dao.createNew();
					group.setName("default");
					group.setPrefix(homeId);
					group.setGidNumber(serialService.next("gid-number-serial").intValue());
					group = dao.persist(group);
				}
			}
		}
		else {
//			logger.info("Overriding standard Primary Group Update Mechanism! Activator: {}", completeOverrideHook.getClass().getName());
		}
		
		if (group == null) {
//			logger.warn("No Primary Group for user {}", user.getEppn());
			throw new RegisterException("Es konnte keine Primäre Gruppe angelegt/gesetzt werden");
		}

		for (GroupServiceHook hook : activeHooks) {
			group = hook.postUpdateUserPrimaryGroupFromAttribute(dao, group, user, attributeMap);
		}
				
		user.setPrimaryGroup(group);
	}
	
	@Override
	public void updateUserSecondaryGroupsFromAttribute(UserEntity user, Map<String, String> attributeMap)
			throws RegisterException {

		GroupServiceHook completeOverrideHook = null;
		Set<GroupServiceHook> activeHooks = new HashSet<GroupServiceHook>();

		for (GroupServiceHook hook : hookManager.getGroupHooks()) {
			if (hook.isSecondaryResponsible(user, attributeMap)) {
				hook.preUpdateUserSecondaryGroupFromAttribute(dao, user, attributeMap);
				activeHooks.add(hook);
				
				if (hook.isSecondaryCompleteOverride()) {
					completeOverrideHook = hook;
				}
			}
		}
				

		if (completeOverrideHook == null) {

			String homeId = attributeMap.get("http://bwidm.de/bwidmOrgId");

			if (homeId == null) {
//				logger.warn("No Home ID is set for User {}", user.getEppn());
				throw new RegisterException("Es fehlt das Präfix der Heimatorganisation");
			}

			// TODO: Take SAML Criteria for Secondary Groups
			
			//groupEntity.setGidNumber(serialService.next("gid-number-serial").intValue());
		}
		else {
//			logger.info("Overriding standard Secondary Group Update Mechanism! Activator: {}", completeOverrideHook.getClass().getName());
		}
			
		for (GroupServiceHook hook : hookManager.getGroupHooks()) {
			hook.postUpdateUserSecondaryGroupFromAttribute(dao, user, attributeMap);
		}
	}	
}
