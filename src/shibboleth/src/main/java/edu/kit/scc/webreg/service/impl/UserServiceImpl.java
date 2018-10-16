package edu.kit.scc.webreg.service.impl;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.SerialService;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.UserServiceHook;

@Stateless
public class UserServiceImpl implements UserService, Serializable {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
	
	@Inject
	private UserDao dao;
	
	@Inject
	private SerialService serialService;
	
	@Inject
	private HookManager hookManager;

	@Override
	public UserEntity createNew() {
		return dao.createNew();
	}

	@Override
	public UserEntity save(UserEntity entity) {
		return dao.persist(entity);
	}

	@Override
	public UserEntity mergeAndSave(UserEntity entity) {
		entity = dao.merge(entity);
		dao.persist(entity);
		return entity;
	}

	@Override
	public void delete(UserEntity entity) {
		dao.delete(entity);
	}

	@Override
	public List<UserEntity> findAll() {
		return dao.findAll();
	}

	@Override
	public List<UserEntity> findAllWithRoles() {
		return dao.findAllWithRoles();
	}

	@Override
	public List<RoleEntity> findRolesForUserById(Long id) {
		return dao.findRolesForUserById(id);
	}

	@Override
	public UserEntity findByPersistentWithRoles(String spId, String idpId, String persistentId) {
		return dao.findByPersistentWithRoles(spId, idpId, persistentId);
	}

	@Override
	public UserEntity findByEppn(String eppn) {
		return dao.findByEppn(eppn);
	}

	@Override
	public UserEntity findById(Long id) {
		return dao.findById(id);
	}

	@Override
	public UserEntity findByIdWithRoles(Long id) {
		return dao.findByIdWithRoles(id);
	}

	@Override
	public UserEntity findByIdWithAll(Long id) {
		return dao.findByIdWithAll(id);
	}
	
	@Override
	public void updateUserFromAttribute(UserEntity user, Map<String, String> attributeMap) 
				throws RegisterException {
		
		UserServiceHook completeOverrideHook = null;
		Set<UserServiceHook> activeHooks = new HashSet<UserServiceHook>();
		
		for (UserServiceHook hook : hookManager.getUserHooks()) {
			if (hook.isResponsible(user, attributeMap)) {
				
				hook.preUpdateUserFromAttribute(user, attributeMap);
				activeHooks.add(hook);
				
				if (hook.isCompleteOverride()) {
					completeOverrideHook = hook;
				}
			}
		}
		
		if (completeOverrideHook == null) {
			user.setEmail(attributeMap.get("urn:oid:0.9.2342.19200300.100.1.3"));
			user.setEppn(attributeMap.get("urn:oid:1.3.6.1.4.1.5923.1.1.1.6"));
			user.setGivenName(attributeMap.get("urn:oid:2.5.4.42"));
			user.setSurName(attributeMap.get("urn:oid:2.5.4.4"));
	
			if (user.getUidNumber() == null) {
				user.setUidNumber(serialService.next("uid-number-serial").intValue());
				logger.info("Setting UID Number {} for user {}", user.getUidNumber(), user.getEppn());
			}
		}
		else {
			logger.info("Overriding standard User Update Mechanism! Activator: {}", completeOverrideHook.getClass().getName());
		}
		
		for (UserServiceHook hook : activeHooks) {
			hook.postUpdateUserFromAttribute(user, attributeMap);
		}		
	}
}
