/*
 * This file is part of the Emulation-as-a-Service framework.
 *
 * The Emulation-as-a-Service framework is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * The Emulation-as-a-Service framework is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Emulation-as-a-Software framework.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package de.bwl.bwfla.shibboleth.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.opensaml.DefaultBootstrap;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.entity.AdminUserEntity;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.entity.SerialEntity;
import edu.kit.scc.webreg.service.AdminUserService;
import edu.kit.scc.webreg.service.GroupService;
import edu.kit.scc.webreg.service.RoleService;
import edu.kit.scc.webreg.service.SamlSpConfigurationService;
import edu.kit.scc.webreg.service.SerialService;

@Singleton
@Startup
public class ApplicationBootstrap {

	Logger logger = LoggerFactory.getLogger(ApplicationBootstrap.class);

	@Inject
	private GroupService groupService;

	@Inject
	private RoleService roleService;

	@Inject
	private AdminUserService adminUserService;

	@Inject
	private SerialService serialService;

	@Inject
	private SamlSpConfigurationService spConfigurationService;

	@PostConstruct
	public void init() {
		
		logger.info("Removing old SP's");
		List<SamlSpConfigurationEntity> old = spConfigurationService.findAll();
		for (SamlSpConfigurationEntity sp : old) {
			spConfigurationService.delete(sp);
		}
		
		logger.info("Initializing local service provider...");
		logger.info("Loading sp configuration from XML");
		SamlSpConfigurationEntity spEntity = ShibUtil.loadSpConfigFromXML("/spConfig.xml");
		spConfigurationService.save(spEntity);
		
		logger.info("Initializing Serials");
		checkSerial("uid-number-serial", 900000L);
		checkSerial("gid-number-serial", 500000L);

		logger.info("Initializing Groups");
		checkGroup("invalid", 499999);

		logger.info("Initializing standard Roles");
		checkRole("MasterAdmin");
		checkRole("RoleAdmin");
		checkRole("UserAdmin");
		checkRole("ServiceAdmin");
		checkRole("SamlAdmin");
		checkRole("BusinessRuleAdmin");
		checkRole("BulkAdmin");
		checkRole("User");

		logger.info("Initializing admin Account");
		if (adminUserService.findByUsername("admin") == null) {
			AdminUserEntity a = adminUserService.createNew();
			a.setUsername("admin");
			a.setPassword("secret");
			Set<RoleEntity> roles = new HashSet<RoleEntity>();
			roles.add(roleService.findByName("MasterAdmin"));
			a.setRoles(roles);
			adminUserService.save(a);
		}

		try {
			logger.info("OpenSAML Bootstrap...");
			DefaultBootstrap.bootstrap();

			logger.info("Loading XMLTooling configuration liberty-paos-config.xml");
			XMLConfigurator configurator = new XMLConfigurator();
			configurator.load(ApplicationBootstrap.class.getClassLoader().getResourceAsStream("/liberty-paos-config.xml"));

		} catch (ConfigurationException e) {
			logger.error("Serious Error happened", e);
		}

	}

	private void checkGroup(String name, Integer createActual) {
		GroupEntity entity = groupService.findByName(name);
		if (entity == null) {
			entity = groupService.createNew();
			entity.setName(name);
			entity.setGidNumber(createActual);
			groupService.save(entity);
		}
	}

	private void checkRole(String roleName) {
		if (roleService.findByName(roleName) == null) {
			RoleEntity role = roleService.createNew();
			role.setName(roleName);
			roleService.save(role);
		}
	}

	private void checkSerial(String serialName, Long createActual) {
		SerialEntity serial = serialService.findByName(serialName);
		if (serial == null) {
			serial = serialService.createNew();
			serial.setName(serialName);
			serial.setActual(createActual);
			serialService.save(serial);
		}
	}

}
