package edu.kit.scc.webreg.service.reg.ldap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.entity.AuditStatus;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.vt.middleware.ldap.AttributesFactory;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.SearchFilter;
import edu.vt.middleware.ldap.Ldap.AttributeModification;

public class LdapWorker {

	private static Logger logger = LoggerFactory.getLogger(LdapWorker.class);
	
	private String ldapUserBase;
	private String ldapGroupBase;
	
	private LdapConnectionManager connectionManager;
	
	private Auditor auditor;
	
	public LdapWorker(PropertyReader prop, Auditor auditor) throws RegisterException {
		this.auditor = auditor;
		
		connectionManager = new LdapConnectionManager(prop);
		ldapUserBase = prop.readProp("ldap_user_base");		
		ldapGroupBase = prop.readProp("ldap_group_base");		
	}

	public void deleteUser(String uid) {

		for (Ldap ldap : connectionManager.getConnections()) {
			try {
				ldap.delete("uid=" + uid + "," + ldapUserBase);
				logger.info("Deleted User {} from ldap {}", 
						new Object[] {uid, ldapUserBase});
				auditor.logAction("", "DELETE LDAP USER", uid, "User deleted in " + ldap.getLdapConfig().getLdapUrl(), AuditStatus.SUCCESS);
			} catch (NamingException e) {
				logger.warn("FAILED: Delete User {} from ldap {}: {}", 
						new Object[] {uid, ldapUserBase, e.getMessage()});
				auditor.logAction("", "DELETE LDAP USER", uid, "User deletion failed in " + ldap.getLdapConfig().getLdapUrl(), AuditStatus.FAIL);
			}

			try {
				Iterator<SearchResult> iterator = ldap.search(new SearchFilter("memberUid=" + uid), new String[] {"cn"});
				while (iterator.hasNext()) {
					SearchResult sr = iterator.next();
					Attribute cnAttr = sr.getAttributes().get("cn");
					String cn = (String) cnAttr.get();
	
					try {
						ldap.modifyAttributes("cn=" + cn + "," + ldapGroupBase, AttributeModification.REMOVE, 
							AttributesFactory.createAttributes("memberUid", uid));
					} catch (NamingException e) {
						logger.info("FAILED: Delete User {} from group {} ldap {}: {}", 
								new Object[] {uid, cn, ldapUserBase, e.getMessage()});
					}

					logger.info("Deleted User {} from group {} in ldap {}", 
							new Object[] {uid, cn, ldapUserBase});
					auditor.logAction("", "DELETE LDAP USER FROM GROUPS", uid, "User deletion from groups in " + ldap.getLdapConfig().getLdapUrl(), AuditStatus.SUCCESS);
				}
			} catch (NamingException e) {
				logger.warn("FAILED: Delete User {} from ldap {}: {}", 
						new Object[] {uid, ldapUserBase, e.getMessage()});
				auditor.logAction("", "DELETE LDAP USER FROM GROUPS", uid, "User deletion failed from groups in " + ldap.getLdapConfig().getLdapUrl(), AuditStatus.FAIL);
			}
			
		}
	}
	
	public void reconGroups(String uid, Set<String> groups) {
		for (Ldap ldap : connectionManager.getConnections()) {
			try {
				Set<String> ldapGroups = new HashSet<String>();
				Iterator<SearchResult> iterator = ldap.search(new SearchFilter("memberUid=" + uid), new String[] {"cn"});
				while (iterator.hasNext()) {
					SearchResult sr = iterator.next();
					Attribute cnAttr = sr.getAttributes().get("cn");
					String cn = (String) cnAttr.get();
					ldapGroups.add(cn);
				}

				Set<String> addGroups = new HashSet<String>(groups);
				addGroups.removeAll(ldapGroups);

				Set<String> removeGroups = new HashSet<String>(ldapGroups);
				removeGroups.removeAll(groups);
				
				for (String group : addGroups) {
					logger.debug("Adding member {} to group {}", uid, group);
					ldap.modifyAttributes("cn=" + group + "," + ldapGroupBase, AttributeModification.ADD, 
							AttributesFactory.createAttributes("memberUid", uid));
				}
				
				for (String group : removeGroups) {
					logger.debug("Removing member {} from group {}", uid, group);
					ldap.modifyAttributes("cn=" + group + "," + ldapGroupBase, AttributeModification.REMOVE, 
							AttributesFactory.createAttributes("memberUid", uid));
				}
				
			} catch (NamingException e) {
				logger.info("Group action failed", e);
			}			
		}
	}
	
	public void reconUser(String cn, String sn, String givenName, String mail, String uid, String uidNumber, String gidNumber,
			String homeDir, String description) {
		for (Ldap ldap : connectionManager.getConnections()) {
			try {
				Attributes attrs = ldap.getAttributes("uid=" + uid + "," + ldapUserBase);

				List<ModificationItem> modList = new ArrayList<ModificationItem>();
				StringBuilder log = new StringBuilder();
				
				compareAttr(attrs, "cn", cn, modList, log);
				compareAttr(attrs, "sn", sn, modList, log);
				compareAttr(attrs, "givenName", givenName, modList, log);
				compareAttr(attrs, "mail", mail, modList, log);
				compareAttr(attrs, "uid", uid, modList, log);
				compareAttr(attrs, "uidNumber", uidNumber, modList, log);
				compareAttr(attrs, "gidNumber", gidNumber, modList, log);
				compareAttr(attrs, "homeDirectory", homeDir, modList, log);
				compareAttr(attrs, "description", description, modList, log);
				
				if (modList.size() == 0) {
					logger.debug("No modification detected");
				}
				else {
					logger.debug("Replacing {} attribute", modList.size());
					ldap.modifyAttributes("uid=" + uid + "," + ldapUserBase, modList.toArray(new ModificationItem[modList.size()]));
					if (log.length() > 512) log.setLength(512);
					auditor.logAction("", "RECON LDAP USER", uid, log.toString(), AuditStatus.SUCCESS);
				}
					
			} catch (NamingException e) {
				logger.debug("Account does not exist. Creating...");
				try {
					createUserIntern(ldap, cn, givenName, sn, mail, uid, uidNumber, gidNumber, homeDir, description);
					logger.info("User {},{} with ldap {} successfully created", 
							new Object[] {uid, gidNumber, ldapUserBase});
					auditor.logAction("", "RECON CREATE LDAP USER", uid, "User created in " + ldap.getLdapConfig().getLdapUrl(), AuditStatus.SUCCESS);
				} catch (NamingException ne) {
					logger.warn("FAILED: User {}, uid:{}, gid:{} with ldap {}: {}", 
							new Object[] {uid, uidNumber, gidNumber, 
							ldapUserBase, e.getMessage()});
					auditor.logAction("", "RECON CREATE LDAP USER", uid, "User creation failed in " + ldap.getLdapConfig().getLdapUrl(), AuditStatus.FAIL);
				}				
			}
		}		
	}

	public void createGroup(String cn, String gidNumber) {
		
		for (Ldap ldap : connectionManager.getConnections()) {
			if (existsGroupEntry(ldap, cn)) {
				logger.debug("Group already registered whit cn {} in LDAP {}", new Object[] { 
						cn, ldap.getLdapConfig().getLdapUrl()});
				continue;
			}
			
			try {
				createGroupIntern(ldap, cn, gidNumber);
				logger.info("Group {},{} with ldap {} successfully created", 
						new Object[] {cn, gidNumber, ldapUserBase});
				auditor.logAction("", "CREATE LDAP GROUP", cn, "Group created in " + ldap.getLdapConfig().getLdapUrl(), AuditStatus.SUCCESS);
			} catch (NamingException e) {
				logger.warn("FAILED: Group cn:{}, gid:{} with ldap {}: {}", 
						new Object[] {cn, gidNumber, 
						ldapUserBase, e.getMessage()});
				auditor.logAction("", "CREATE LDAP GROUP", cn, "Group creation failed in " + ldap.getLdapConfig().getLdapUrl(), AuditStatus.FAIL);
			}

		}
	}
	
	public void setPassword(String uid, String password) {
		for (Ldap ldap : connectionManager.getConnections()) {
			try {
				String ldapDn = "uid=" + uid + "," + ldapUserBase;
				Attributes attrs = ldap.getAttributes(ldapDn);
				Attribute attr = attrs.get("userPassword");
				if (attr == null) {
					ldap.modifyAttributes(ldapDn, AttributeModification.ADD, 
							AttributesFactory.createAttributes("userPassword", password));
				}
				else {
					ldap.modifyAttributes(ldapDn, AttributeModification.REPLACE, 
							AttributesFactory.createAttributes("userPassword", password));
				}
				logger.info("Setting password for User {} in ldap {}", 
						new Object[] {uid, ldapUserBase});
				auditor.logAction("", "SET PASSWORD LDAP USER", uid, "Set User password in " + ldap.getLdapConfig().getLdapUrl(), AuditStatus.SUCCESS);
			} catch (NamingException e) {
				logger.warn("FAILED: Setting password for User {} in ldap {}: {}", 
						new Object[] {uid, ldapUserBase, e.getMessage()});
				auditor.logAction("", "SET PASSWORD LDAP USER", uid, "Set User password failed in " + ldap.getLdapConfig().getLdapUrl(), AuditStatus.FAIL);
			}
		}		
	}
	
	public void closeConnections() {
		connectionManager.closeConnections();
	}
	
	private boolean existsGroupEntry(Ldap ldap, String cn) {

		try {
			ldap.getAttributes("cn=" + cn + "," + ldapGroupBase);
			return true;
		} catch (NamingException e) {
			return false;
		}

	}
	
	private void compareAttr(Attributes attrs, String key, String value, List<ModificationItem> modList,
				StringBuilder log)
			throws NamingException {
		Attribute attr = attrs.get(key);
		
		if (attr == null) {
			modList.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, 
					AttributesFactory.createAttribute(key, value)));
			log.append("ADD: ");
			log.append(key);
			log.append("=");
			log.append(value);
			log.append(" ");
		}
		else if (! attr.get().equals(value)) {
			modList.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, 
					AttributesFactory.createAttribute(key, value)));
			log.append("REPLACE: ");
			log.append(key);
			log.append("=");
			log.append(attr.get());
			log.append("->");
			log.append(value);
			log.append(" ");
		}
	}
	
	private void createUserIntern(Ldap ldap, String cn, String givenName, String sn, String mail, String uid, String uidNumber, String gidNumber,
			String homeDir, String description) throws NamingException {
		Attributes attrs = AttributesFactory.createAttributes("objectClass", new String[] {
				"top", "person", "organizationalPerson", 
        		"inetOrgPerson", "posixAccount"});
		attrs.put(AttributesFactory.createAttribute("cn", cn));
		attrs.put(AttributesFactory.createAttribute("sn", sn));
		attrs.put(AttributesFactory.createAttribute("givenName", givenName));
		attrs.put(AttributesFactory.createAttribute("mail", mail));
		attrs.put(AttributesFactory.createAttribute("uid", uid));
		attrs.put(AttributesFactory.createAttribute("uidNumber", uidNumber));
		attrs.put(AttributesFactory.createAttribute("gidNumber", gidNumber));
		attrs.put(AttributesFactory.createAttribute("homeDirectory", homeDir));
		attrs.put(AttributesFactory.createAttribute("description", description));

		ldap.create("uid=" + uid + "," + ldapUserBase, attrs);
	}
	
	private void createGroupIntern(Ldap ldap, String cn, String gidNumber) 
			throws NamingException {
		Attributes attrs = AttributesFactory.createAttributes("objectClass", new String[] {
				"top", "posixGroup"});
		attrs.put(AttributesFactory.createAttribute("cn", cn));
		attrs.put(AttributesFactory.createAttribute("gidNumber", gidNumber));

		ldap.create("cn=" + cn + "," + ldapGroupBase, attrs);
	}
}
