package edu.kit.scc.webreg.service.reg.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.reg.RegisterUserWorkflow;
import edu.kit.scc.webreg.service.reg.SetPasswordCapable;

public class NullRegisterWorkflowImpl implements RegisterUserWorkflow, SetPasswordCapable {

	private static final Logger logger = LoggerFactory.getLogger(NullRegisterWorkflowImpl.class);

	@Override
	public void registerUser(UserEntity user, ServiceEntity service, RegistryEntity registry, Auditor auditor) throws RegisterException {
		logger.info("Null-Registering user {} for service {}", user.getEppn(), service.getName());
	}

	@Override
	public void deregisterUser(UserEntity user, ServiceEntity service,
			RegistryEntity registry, Auditor auditor) throws RegisterException {
		logger.info("Null-Deregistering user {} for service {}", user.getEppn(), service.getName());
	}

	@Override
	public void setPassword(UserEntity user, ServiceEntity service,
			RegistryEntity registry, Auditor auditor, String password) throws RegisterException {
		logger.info("Setting password for user {} for service {}", user.getEppn(), service.getName());
	}

	@Override
	public void reconciliation(UserEntity user, ServiceEntity service,
			RegistryEntity registry, Auditor auditor) {
		logger.info("Reconciliation for user {} for service {}", user.getEppn(), service.getName());
	}

	@Override
	public Boolean updateRegistry(UserEntity user, ServiceEntity service,
			RegistryEntity registry, Auditor auditor) throws RegisterException {
		logger.info("Update registry for user {} for service {}", user.getEppn(), service.getName());
		return false;
	}
}
