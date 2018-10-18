package edu.kit.scc.webreg.service.reg.impl;

import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.RegistryService;
import edu.kit.scc.webreg.service.reg.ApprovalService;
import edu.kit.scc.webreg.service.reg.ApprovalWorkflow;
import edu.kit.scc.webreg.service.reg.ApprovalWorkflowStore;

@Stateless
public class ApprovalServiceImpl implements ApprovalService {

	@Inject
	private ApprovalWorkflowStore workflowStore;

	@Override
	public void registerApproval(RegistryEntity registry) throws RegisterException {
		ApprovalWorkflow workflow = workflowStore.getWorkflow(registry.getApprovalBean());
		workflow.registerApproval(registry);
	}

	@Override
	public void approve(RegistryEntity registry) {
		
	}

	@Override
	public Map<String, ApprovalWorkflow> getWorkflowMap() {
		return workflowStore.getWorkflowMap();
	}
	
	@Override
	public Boolean checkWorkflow(String name) {
		if (workflowStore.getWorkflowMap().containsKey(name))
			return true;
		else
			return false;
	}

}
