package edu.kit.scc.webreg.service.reg.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.dao.AuditDetailDao;
import edu.kit.scc.webreg.dao.AuditEntryDao;
import edu.kit.scc.webreg.dao.RegistryDao;
import edu.kit.scc.webreg.dao.ServiceDao;
import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.entity.AgreementTextEntity;
import edu.kit.scc.webreg.entity.ApproverRoleEntity;
import edu.kit.scc.webreg.entity.PolicyEntity;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.reg.ApprovalService;
import edu.kit.scc.webreg.service.reg.RegisterUserService;
import edu.kit.scc.webreg.service.reg.RegisterUserWorkflow;
import edu.kit.scc.webreg.service.reg.SetPasswordCapable;

@Stateless
public class RegisterUserServiceImpl implements RegisterUserService {

	private static final Logger logger = LoggerFactory.getLogger(RegisterUserServiceImpl.class);

	@Inject
	private RegistryDao registryDao;

	@Inject
	private ServiceDao serviceDao;

	@Inject
	private UserDao userDao;

	@Inject
	private ApprovalService approvalService;

	@Inject
	private AuditEntryDao auditDao;

	@Inject
	private AuditDetailDao auditDetailDao;

	@Override
	public void registerUser(UserEntity user, ServiceEntity service)
			throws RegisterException {
		RegistryEntity registry = registryDao.createNew();
		
		try {
			ApproverRoleEntity approverRole = service.getApproverRole();
			
			Set<AgreementTextEntity> agrs = new HashSet<AgreementTextEntity>();
			for (PolicyEntity policy : service.getPolicies())
				agrs.add(policy.getActualAgreement());
			registry.setAgreedTexts(agrs);
			registry.setAgreedTime(new Date());
			registry.setService(service);
			registry.setUser(user);
			registry.setRegisterBean(service.getRegisterBean());
			
			if (approverRole != null)
				registry.setApprovalBean(approverRole.getApprovalBean());
			
			registry.setRegistryValues(new HashMap<String, String>());
			
			registry.setRegistryStatus(RegistryStatus.CREATED);
			
			registry = registryDao.persist(registry);
			
			if (registry.getApprovalBean() != null) {
				logger.debug("Registering {} for approval {}", user.getEppn(), registry.getApprovalBean());
				approvalService.registerApproval(registry);
			}
			else {
				logger.debug("No approval role for service {}. AutoApproving {}", service.getName(), user.getEppn());
				registerUserFinal(registry);
			}
			
		} catch (Throwable t) {
			throw new RegisterException(t);
		}    			
	}

	@Override
	public final void registerUserFinal(RegistryEntity registry) throws RegisterException {

		RegisterUserWorkflow workflow = getWorkflowInstance(registry.getRegisterBean());

		try {
			ServiceEntity serviceEntity = serviceDao.findByIdWithServiceProps(registry.getService().getId());
			UserEntity userEntity = userDao.findByIdWithAll(registry.getUser().getId());

			Auditor auditor = new Auditor(auditDao, auditDetailDao);
			auditor.startAuditTrail(registry, null);
			auditor.setName(workflow.getClass().getName() + "-Register-Audit");
			auditor.setDetail("Register user " + userEntity.getEppn() + " for service " + serviceEntity.getName());
			
			workflow.updateRegistry(userEntity, serviceEntity, registry, auditor);
			workflow.registerUser(userEntity, serviceEntity, registry, auditor);

			registry.setRegistryStatus(RegistryStatus.ACTIVE);
			registry.setLastReconcile(new Date());
			registryDao.persist(registry);
			
			auditor.finishAuditTrail();
			
		} catch (Throwable t) {
			throw new RegisterException(t);
		}    	
	}

	@Override
	public final void reconsiliation(RegistryEntity registry, Boolean fullRecon) throws RegisterException {

		RegisterUserWorkflow workflow = getWorkflowInstance(registry.getRegisterBean());

		try {
			ServiceEntity serviceEntity = serviceDao.findByIdWithServiceProps(registry.getService().getId());
			UserEntity userEntity = userDao.findByIdWithAll(registry.getUser().getId());
			
			Auditor auditor = new Auditor(auditDao, auditDetailDao);
			auditor.startAuditTrail(registry, null);
			auditor.setName(workflow.getClass().getName() + "-Reconsiliation-Audit");
			auditor.setDetail("Recon user " + userEntity.getEppn() + " for service " + serviceEntity.getName());

			Boolean updated = workflow.updateRegistry(userEntity, serviceEntity, registry, auditor);

			if (fullRecon) {
				logger.debug("Doing full reconsiliation");
				workflow.reconciliation(userEntity, serviceEntity, registry, auditor);
			}
			else if (updated) {
				logger.debug("Changes detected, starting reconcile");
				workflow.reconciliation(userEntity, serviceEntity, registry, auditor);
			} 
			else
				logger.debug("No Changes detected");

			registry.setLastReconcile(new Date());
			registry = registryDao.persist(registry);

			auditor.finishAuditTrail();
		} catch (Throwable t) {
			throw new RegisterException(t);
		}    	
	}

	@Override
	public final void deregisterUser(RegistryEntity registry) throws RegisterException {
		
		RegisterUserWorkflow workflow = getWorkflowInstance(registry.getRegisterBean());

		try {
			ServiceEntity serviceEntity = serviceDao.findByIdWithServiceProps(registry.getService().getId());
			UserEntity userEntity = userDao.findByIdWithAll(registry.getUser().getId());

			Auditor auditor = new Auditor(auditDao, auditDetailDao);
			auditor.startAuditTrail(registry, null);
			auditor.setName(workflow.getClass().getName() + "-Deregister-Audit");
			auditor.setDetail("Deregister user " + registry.getUser().getEppn() + " for service " + serviceEntity.getName());
			
			workflow.deregisterUser(userEntity, serviceEntity, registry, auditor);

			registry.setRegistryStatus(RegistryStatus.DELETED);
			registryDao.persist(registry);

			auditor.finishAuditTrail();
		} catch (RegisterException e) {
			throw e;
		} catch (Throwable t) {
			throw new RegisterException(t);
		}    	
	}
	
	@Override
	public void setPassword(UserEntity user, ServiceEntity service,
			RegistryEntity registry, String password) throws RegisterException {

		RegisterUserWorkflow workflow = getWorkflowInstance(registry.getRegisterBean());

		try {
			ServiceEntity serviceEntity = serviceDao.findByIdWithServiceProps(registry.getService().getId());
			UserEntity userEntity = userDao.findByIdWithAll(registry.getUser().getId());

			Auditor auditor = new Auditor(auditDao, auditDetailDao);
			auditor.startAuditTrail(registry, null);
			auditor.setName(workflow.getClass().getName() + "-SetPassword-Audit");
			auditor.setDetail("Setting service password for user " + registry.getUser().getEppn() + " for service " + serviceEntity.getName());
			
			((SetPasswordCapable) workflow).setPassword(userEntity, serviceEntity, registry, auditor, password);

			auditor.finishAuditTrail();
		} catch (RegisterException e) {
			throw e;
		} catch (Throwable t) {
			throw new RegisterException(t);
		}    			
	}
	
	@Override
	public Boolean checkWorkflow(String name) {
		if (getWorkflowInstance(name) != null) {
			return true;
		}
		else {
			return false;
		}
	}
	
	@Override
	public RegisterUserWorkflow getWorkflowInstance(String className) {
		try {
			Object o = Class.forName(className).newInstance();
			if (o instanceof RegisterUserWorkflow)
				return (RegisterUserWorkflow) o;
			else {
				logger.warn("Service Register bean misconfigured, Object not Type RegisterUserWorkflow but: {}", o.getClass());
				return null;
			}
		} catch (InstantiationException e) {
			logger.warn("Service Register bean misconfigured: {}", e.getMessage());
			return null;
		} catch (IllegalAccessException e) {
			logger.warn("Service Register bean misconfigured: {}", e.getMessage());
			return null;
		} catch (ClassNotFoundException e) {
			logger.warn("Service Register bean misconfigured: {}", e.getMessage());
			return null;
		}
	}
}
