package edu.kit.scc.webreg.service.reg;

import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.exc.RegisterException;

public interface ApprovalWorkflow {

	String getName();
	
	void registerApproval(RegistryEntity registry) throws RegisterException ;
	void approve(RegistryEntity registry) throws RegisterException;
	
}
