package edu.kit.scc.webreg.audit;

import java.util.Date;
import java.util.HashSet;
import edu.kit.scc.webreg.dao.AuditDetailDao;
import edu.kit.scc.webreg.dao.AuditEntryDao;
import edu.kit.scc.webreg.entity.AuditDetailEntity;
import edu.kit.scc.webreg.entity.AuditEntryEntity;
import edu.kit.scc.webreg.entity.AuditStatus;
import edu.kit.scc.webreg.entity.RegistryEntity;

public class Auditor {

	private AuditEntryDao auditEntryDao;
	private AuditDetailDao auditDetailDao;
	
	private AuditEntryEntity audit;
	
	public Auditor(AuditEntryDao auditEntryDao, AuditDetailDao auditDetailDao) {
		this.auditDetailDao = auditDetailDao;
		this.auditEntryDao = auditEntryDao;
	}

	public void startAuditTrail(RegistryEntity registry, String executor) {
		audit = auditEntryDao.createNew();
		audit.setStartTime(new Date());
		audit.setAuditDetails(new HashSet<AuditDetailEntity>());
		audit.setExecutor(executor);
		audit.setRegistry(registry);
	}

	public void logAction(String subject, String action, String object, String log, AuditStatus status) {
		AuditDetailEntity detail = auditDetailDao.createNew();
		detail.setSubject(subject);
		detail.setAction(action);
		detail.setObject(object);
		detail.setLog(log);
		detail.setAuditStatus(status);
		detail.setEndTime(new Date());
		detail.setAuditEntry(audit);
		audit.getAuditDetails().add(detail);
	}
	
	public void finishAuditTrail() {
		audit.setEndTime(new Date());
		auditEntryDao.persist(audit);
		audit = null;
	}
	
	public void setName(String name) {
		audit.setName(name);
	}
	
	public void setDetail(String detail) {
		audit.setDetail(detail);
	}
}
