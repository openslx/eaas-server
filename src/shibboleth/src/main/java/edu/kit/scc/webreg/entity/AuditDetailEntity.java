package edu.kit.scc.webreg.entity;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "audit_detail", schema = "shib")
public class AuditDetailEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;

	@ManyToOne
	private AuditEntryEntity auditEntry;
	
	@Column(name = "end_time")
	private Date endTime;
	
	@Enumerated(EnumType.STRING)
	private AuditStatus auditStatus;

	@Column(name = "audit_subject", length = 256)
	private String subject;
	
	@Column(name = "audit_action", length = 128)
	private String action;
	
	@Column(name = "audit_object", length = 256)
	private String object;
	
	@Column(name = "audit_log", length = 1024)
	private String log;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public AuditEntryEntity getAuditEntry() {
		return auditEntry;
	}

	public void setAuditEntry(AuditEntryEntity auditEntry) {
		this.auditEntry = auditEntry;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public AuditStatus getAuditStatus() {
		return auditStatus;
	}

	public void setAuditStatus(AuditStatus auditStatus) {
		this.auditStatus = auditStatus;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getObject() {
		return object;
	}

	public void setObject(String object) {
		this.object = object;
	}

	public String getLog() {
		return log;
	}

	public void setLog(String log) {
		this.log = log;
	}

	
}
