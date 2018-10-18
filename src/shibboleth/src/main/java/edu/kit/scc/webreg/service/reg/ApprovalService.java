package edu.kit.scc.webreg.service.reg;

import java.util.Map;

import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.exc.RegisterException;

public interface ApprovalService {

	Map<String, ApprovalWorkflow> getWorkflowMap();

	Boolean checkWorkflow(String name);

	void registerApproval(RegistryEntity registry) throws RegisterException;

	void approve(RegistryEntity registry);
	
}
