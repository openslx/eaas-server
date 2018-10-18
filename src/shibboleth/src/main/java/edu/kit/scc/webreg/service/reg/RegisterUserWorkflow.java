package edu.kit.scc.webreg.service.reg;

import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.RegisterException;

public interface RegisterUserWorkflow {

	void registerUser(UserEntity user, ServiceEntity service, RegistryEntity registry, Auditor auditor) throws RegisterException;
	void deregisterUser(UserEntity user, ServiceEntity service, RegistryEntity registry, Auditor auditor) throws RegisterException;
	void reconciliation(UserEntity user, ServiceEntity service,
			RegistryEntity registry, Auditor auditor) throws RegisterException;
	Boolean updateRegistry(UserEntity user, ServiceEntity service,
			RegistryEntity registry, Auditor auditor) throws RegisterException;
}
