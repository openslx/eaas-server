package edu.kit.scc.webreg.service.reg;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Startup
public class ApprovalWorkflowStore {

	private static final Logger logger = LoggerFactory.getLogger(ApprovalWorkflowStore.class);
	
	private Map<String, ApprovalWorkflow> workflowMap;
	
	@PostConstruct
	public void init() {
		workflowMap = new HashMap<String, ApprovalWorkflow>();
	}

	public void registerWorkflow(ApprovalWorkflow workflow) {
		logger.info("Registering approval workflow {}", workflow.getName());
		workflowMap.put(workflow.getName(), workflow);
	}
	
	public Map<String, ApprovalWorkflow> getWorkflowMap() {
		return workflowMap;
	}
	
	public ApprovalWorkflow getWorkflow(String name) {
		return workflowMap.get(name);
	}
}
