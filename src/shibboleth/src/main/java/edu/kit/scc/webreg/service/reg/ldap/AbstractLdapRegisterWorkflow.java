package edu.kit.scc.webreg.service.reg.ldap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.entity.AuditStatus;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.reg.RegisterUserWorkflow;
import edu.kit.scc.webreg.service.reg.SetPasswordCapable;

public abstract class AbstractLdapRegisterWorkflow implements RegisterUserWorkflow, SetPasswordCapable {

	private static Logger logger = LoggerFactory.getLogger(AbstractLdapRegisterWorkflow.class);
	
	protected abstract String constructHomeDir(String homeId, String homeUid, UserEntity user, Map<String, String> reconMap);
	protected abstract String constructLocalUid(String homeId, String homeUid, UserEntity user, Map<String, String> reconMap);
	protected abstract String constructGroupName(GroupEntity group);
	
	@Override
	public void registerUser(UserEntity user, ServiceEntity service,
			RegistryEntity registry, Auditor auditor) throws RegisterException {
		reconciliation(user, service, registry, auditor);
	}

	@Override
	public void deregisterUser(UserEntity user, ServiceEntity service,
			RegistryEntity registry, Auditor auditor) throws RegisterException {
		logger.info("AbstractLdapRegister Deregister user {} for service {}", user.getEppn(), service.getName());

		PropertyReader prop = new PropertyReader(service.getServiceProps());

		Map<String, String> regMap = registry.getRegistryValues();

		String localUid = regMap.get("localUid");

		LdapWorker ldapWorker = new LdapWorker(prop, auditor);
		ldapWorker.deleteUser(localUid);
		ldapWorker.closeConnections();
	}

	@Override
	public Boolean updateRegistry(UserEntity user, ServiceEntity service,
			RegistryEntity registry, Auditor auditor) throws RegisterException {
		
		/*
		 * Compare values from user and registry store. Found differences trigger 
		 * a full reconsiliation
		 */
		Map<String, String> reconMap = new HashMap<String, String>();
		
		String homeId = user.getAttributeStore().get("http://bwidm.de/bwidmOrgId");
		String homeUid = user.getAttributeStore().get("urn:oid:0.9.2342.19200300.100.1.1");
				
		homeId = homeId.toLowerCase();
		
		reconMap.put("cn", user.getEppn());
		reconMap.put("sn", user.getSurName());
		reconMap.put("givenName", user.getGivenName());
		reconMap.put("mail", user.getEmail());
		reconMap.put("uidNumber", "" + user.getUidNumber());
		reconMap.put("gidNumber", "" + user.getPrimaryGroup().getGidNumber());
		reconMap.put("description", registry.getId().toString());
		
		reconMap.put("groupName", constructGroupName(user.getPrimaryGroup()));
		reconMap.put("localUid", constructLocalUid(homeId, homeUid, user, reconMap));
		reconMap.put("homeDir", constructHomeDir(homeId, homeUid, user, reconMap));
		
		Boolean change = false;
		
		for (Entry<String, String> entry : reconMap.entrySet()) {
			if (! registry.getRegistryValues().containsKey(entry.getKey())) {
				auditor.logAction("", "UPDATE USER REGISTRY", user.getEppn(), "ADD " + 
						entry.getKey() + ": " + registry.getRegistryValues().get(entry.getKey()) +
						" => " + entry.getValue()
						, AuditStatus.SUCCESS);
				registry.getRegistryValues().put(entry.getKey(), entry.getValue());
				change |= true;
			}
			else if (! registry.getRegistryValues().get(entry.getKey()).equals(entry.getValue())) {
				auditor.logAction("", "UPDATE USER REGISTRY", user.getEppn(), "REPLACE " + 
						entry.getKey() + ": " + registry.getRegistryValues().get(entry.getKey()) +
						" => " + entry.getValue()
						, AuditStatus.SUCCESS);
				registry.getRegistryValues().put(entry.getKey(), entry.getValue());
				change |= true;
			}
		}
		
		/*
		 * compare groups
		 */
		String groupsString = registry.getRegistryValues().get("groups");
		if (groupsString == null) {
			if (user.getGroups() == null || user.getGroups().size() == 0)
				// user and registry have no groups, no reconcile
				change |= false;
			else
				// registry has not groups, but user has. Trigger reconcile
				change |= true;
		}
		else {
			String[] groups = groupsString.split(";");
			Set<String> registrySet = new HashSet<String>(Arrays.asList(groups));
			Set<String> userGroupSet = groupsToSet(user);
			
			Set<String> addGroups = new HashSet<String>(userGroupSet);
			addGroups.removeAll(registrySet);

			Set<String> removeGroups = new HashSet<String>(registrySet);
			removeGroups.removeAll(userGroupSet);
			
			if (addGroups.size() != 0 || removeGroups.size() != 0) {
				// groups modifications detected
				change |= true;
			}
			else
				change |= false;
		}
		
		if (change)
			registry.getRegistryValues().put("groups", groupsToString(user));
		
		return change;
	}	
	
	@Override
	public void reconciliation(UserEntity user, ServiceEntity service,
			RegistryEntity registry, Auditor auditor) throws RegisterException {
		logger.info("UniCluster Reconsiliation user {} for service {}", user.getEppn(), service.getName());

		PropertyReader prop = new PropertyReader(service.getServiceProps());
		Map<String, String> regMap = registry.getRegistryValues();
		
		String cn = regMap.get("cn");
		String sn = regMap.get("sn");
		String givenName = regMap.get("givenName");
		String mail = regMap.get("mail");
		String localUid = regMap.get("localUid");
		String uidNumber = regMap.get("uidNumber");
		String gidNumber = regMap.get("gidNumber");
		String groupName = regMap.get("groupName");
		String homeDir = regMap.get("homeDir");
		String description = registry.getId().toString();
		
		LdapWorker ldapWorker = new LdapWorker(prop, auditor);
		ldapWorker.createGroup(groupName, gidNumber);
		Set<String> groups = new HashSet<String>();
		for (GroupEntity group : user.getGroups()) {
			String secondaryGroupName = constructGroupName(group);
			ldapWorker.createGroup(secondaryGroupName, "" + group.getGidNumber());
			groups.add(secondaryGroupName);
		}
		ldapWorker.reconUser(cn, sn, givenName, mail, localUid, uidNumber, gidNumber, homeDir, description);
		ldapWorker.reconGroups(localUid, groups);
		ldapWorker.closeConnections();
	}

	@Override
	public void setPassword(UserEntity user, ServiceEntity service,
			RegistryEntity registry, Auditor auditor, String password) throws RegisterException {
		logger.debug("Setting service password for user {}", user.getEppn());

		PropertyReader prop = new PropertyReader(service.getServiceProps());
		Map<String, String> regMap = registry.getRegistryValues();

		String passwordRegex;
		if (prop.hasProp("password_regex")) 
			passwordRegex = prop.readProp("password_regex");
		else
			passwordRegex = ".{6,}";

		String passwordRegexMessage;
		if (prop.hasProp("password_regex_message")) 
			passwordRegexMessage = prop.readProp("password_regex_message");
		else
			passwordRegexMessage = "Das Passwort ist nicht komplex genug";
		
		if (! password.matches(passwordRegex))
			throw new RegisterException(passwordRegexMessage);

		String localUid = regMap.get("localUid");

		LdapWorker ldapWorker = new LdapWorker(prop, auditor);
		ldapWorker.setPassword(localUid, password);
		ldapWorker.closeConnections();		
	}	
	
	protected Set<String> groupsToSet(UserEntity user) {
		Set<String> returnSet = new HashSet<String>();
		for (GroupEntity group : user.getGroups()) {
			if (group.getPrefix() != null)
				returnSet.add(group.getPrefix() + "_" + group.getName());
			else
				returnSet.add("_" + group.getName());
		}
		return returnSet;
	}
	
	protected String groupsToString(UserEntity user) {
		StringBuilder sb = new StringBuilder();
		for (GroupEntity group : user.getGroups()) {
			if (group.getPrefix() != null) {
				sb.append(group.getPrefix());
			}
			sb.append("_");
			sb.append(group.getName());
			sb.append(";");
		}
		if (sb.length() > 0)
			sb.setLength(sb.length() - 1);
		
		return sb.toString();
	}
}
