package edu.kit.scc.webreg.service.reg;

import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.RegisterException;

public interface RegisterUserService {

	Boolean checkWorkflow(String name);

	void registerUser(UserEntity user, ServiceEntity service)
			throws RegisterException;

	void registerUserFinal(RegistryEntity registry) throws RegisterException;

	RegisterUserWorkflow getWorkflowInstance(String className);

	void deregisterUser(RegistryEntity registry) throws RegisterException;

	void reconsiliation(RegistryEntity registry, Boolean fullRecon)
			throws RegisterException;

	void setPassword(UserEntity user, ServiceEntity service,
			RegistryEntity registry, String password) throws RegisterException;
	
}
