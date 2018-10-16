package edu.kit.scc.webreg.entity;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name = "registry", schema = "shib")
public class RegistryEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;

	@Enumerated(EnumType.STRING)
	private RegistryStatus registryStatus;
	
	@ManyToOne
	private UserEntity user;
	
	@ManyToOne
	private ServiceEntity service;

	@NotNull
	@Column(name="register_bean", length=256, nullable=false)
	private String registerBean;
	
	@Column(name="approval_bean", length=256)
	private String approvalBean;
	
	@ManyToMany(targetEntity=AgreementTextEntity.class)
	@JoinTable(name = "registry_agreementtext", schema = "shib",
			joinColumns = @JoinColumn(name="registry_id"),
			inverseJoinColumns = @JoinColumn(name="agreementtext_id")
	)
	private Set<AgreementTextEntity> agreedTexts;
	
	@Column(name="agreed_time")
	private Date agreedTime;
	
	@ElementCollection(fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SELECT)
	@JoinTable(name = "registry_value", schema = "shib")
    @MapKeyColumn(name = "key_data", length = 128)
    @Column(name = "value_data", length = 2048)
    private Map<String, String> registryValues; 
	
	@Column(name = "last_recon")
	private Date lastReconcile;
	
	@Column(name = "last_full_recon")
	private Date lastFullReconcile;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public UserEntity getUser() {
		return user;
	}

	public void setUser(UserEntity user) {
		this.user = user;
	}

	public ServiceEntity getService() {
		return service;
	}

	public void setService(ServiceEntity service) {
		this.service = service;
	}

	public Date getAgreedTime() {
		return agreedTime;
	}

	public void setAgreedTime(Date agreedTime) {
		this.agreedTime = agreedTime;
	}

	public Map<String, String> getRegistryValues() {
		return registryValues;
	}

	public void setRegistryValues(Map<String, String> registryValues) {
		this.registryValues = registryValues;
	}

	public Set<AgreementTextEntity> getAgreedTexts() {
		return agreedTexts;
	}

	public void setAgreedTexts(Set<AgreementTextEntity> agreedTexts) {
		this.agreedTexts = agreedTexts;
	}

	public RegistryStatus getRegistryStatus() {
		return registryStatus;
	}

	public void setRegistryStatus(RegistryStatus registryStatus) {
		this.registryStatus = registryStatus;
	}

	public String getRegisterBean() {
		return registerBean;
	}

	public void setRegisterBean(String registerBean) {
		this.registerBean = registerBean;
	}

	public String getApprovalBean() {
		return approvalBean;
	}

	public void setApprovalBean(String approvalBean) {
		this.approvalBean = approvalBean;
	}

	public Date getLastReconcile() {
		return lastReconcile;
	}

	public void setLastReconcile(Date lastReconcile) {
		this.lastReconcile = lastReconcile;
	}

	public Date getLastFullReconcile() {
		return lastFullReconcile;
	}

	public void setLastFullReconcile(Date lastFullReconcile) {
		this.lastFullReconcile = lastFullReconcile;
	}
}
