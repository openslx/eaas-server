package edu.kit.scc.webreg.entity;

import java.util.Date;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "audit_entry", schema = "shib")
public class AuditEntryEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;

	@Column(name = "start_time")
	private Date startTime;
	
	@Column(name = "end_time")
	private Date endTime;
	
	@Column(name = "audit_name", length=256)
	private String name;
	
	@Column(name = "audit_detail", length=1024)
	private String detail;
	
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, 
			mappedBy = "auditEntry")
	private Set<AuditDetailEntity> auditDetails;

	@ManyToOne
	private RegistryEntity registry;
	
	@Column(name = "audit_executor", length=64)
	private String executor;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Set<AuditDetailEntity> getAuditDetails() {
		return auditDetails;
	}

	public void setAuditDetails(Set<AuditDetailEntity> auditDetails) {
		this.auditDetails = auditDetails;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public RegistryEntity getRegistry() {
		return registry;
	}

	public void setRegistry(RegistryEntity registry) {
		this.registry = registry;
	}

	public String getExecutor() {
		return executor;
	}

	public void setExecutor(String executor) {
		this.executor = executor;
	}


}
