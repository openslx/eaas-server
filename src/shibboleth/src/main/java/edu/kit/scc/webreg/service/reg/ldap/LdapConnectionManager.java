package edu.kit.scc.webreg.service.reg.ldap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.exc.RegisterException;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.LdapConfig;

public class LdapConnectionManager {

	private static Logger logger = LoggerFactory.getLogger(LdapConnectionManager.class);
	
	private String ldapBase;
	
	private Map<String, Ldap> connectionMap;
	
	public LdapConnectionManager(PropertyReader prop) throws RegisterException {
		
		connectionMap = new HashMap<String, Ldap>();
		
		String ldapConnect = prop.readProp("ldap_connect");
		String bindDn = prop.readProp("bind_dn");
		String bindPassword = prop.readProp("bind_password");
		
		ldapBase = prop.readProp("ldap_base");

		String[] ldapConnects = ldapConnect.split(",");

		for (String connect : ldapConnects) {
			if (connect != null && (!connect.isEmpty())) {
				connect = connect.trim();
				logger.info("Creating ldap connection for {}", connect);
				Ldap ldap = getLdapConnect(connect.trim(), ldapBase, bindDn, bindPassword);
				connectionMap.put(connect, ldap);
			}
		}
	}

	public Collection<Ldap> getConnections() {
		return connectionMap.values();
	}
	
	public void closeConnections() {
		for (Ldap ldap : connectionMap.values()) {
			try {
				ldap.close();
			} 
			catch (Exception e) {
				// Cannot close, ignore
			}
		}
	}
	
	private Ldap getLdapConnect(String ldapConnect, String ldapBase, 
			String bindDn, String bindPassword) throws RegisterException {

		logger.debug("Creating ldap connection connect: {} base: {} bind-dn: {}", new Object[] {ldapConnect, ldapBase, bindDn});
		
		LdapConfig config = new LdapConfig(ldapConnect, ldapBase);
		config.setBindDn(bindDn);
		config.setBindCredential(bindPassword);
		Ldap ldap = new Ldap(config);
		
		return ldap;
	}
}
