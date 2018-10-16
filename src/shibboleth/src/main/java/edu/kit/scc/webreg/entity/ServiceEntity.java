package edu.kit.scc.webreg.entity;

import java.util.Map;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "service", schema = "shib")
public class ServiceEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;

	@NotNull
	@Column(name="name", length=128, nullable=false)
	private String name;
	
	@NotNull
	@Column(name="short_name", length=32, nullable=false, unique=true)
	private String shortName;
	
	@ManyToOne
	private AdminRoleEntity adminRole;
	
	@ManyToOne
	private ApproverRoleEntity approverRole;
	
	@ManyToOne
	private ImageEntity image;
	
	@OneToMany(mappedBy="service")
	private Set<PolicyEntity> policies;

	@NotNull
	@Column(name="register_bean", length=256, nullable=false)
	private String registerBean;
	
	@ManyToOne
	private BusinessRuleEntity accessRule;
	
	@ElementCollection(fetch = FetchType.LAZY)
    @Fetch(value = FetchMode.SELECT)
	@JoinTable(name = "service_properties", schema = "shib")
    @MapKeyColumn(name = "key_data", length = 128)
    @Column(name = "value_data", length = 2048)
    private Map<String, String> serviceProps; 

	@Column(name = "description")
	@Lob 
	@Type(type = "org.hibernate.type.TextType")	
	private String description;
	
	@Column(name = "short_description", length = 2048)
	private String shortDescription;

	@Column(name = "published")
	private Boolean published;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Set<PolicyEntity> getPolicies() {
		return policies;
	}

	public void setPolicies(Set<PolicyEntity> policies) {
		this.policies = policies;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public AdminRoleEntity getAdminRole() {
		return adminRole;
	}

	public void setAdminRole(AdminRoleEntity adminRole) {
		this.adminRole = adminRole;
	}

	public ApproverRoleEntity getApproverRole() {
		return approverRole;
	}

	public void setApproverRole(ApproverRoleEntity approverRole) {
		this.approverRole = approverRole;
	}

	public Map<String, String> getServiceProps() {
		return serviceProps;
	}

	public void setServiceProps(Map<String, String> serviceProps) {
		this.serviceProps = serviceProps;
	}

	public String getRegisterBean() {
		return registerBean;
	}

	public void setRegisterBean(String registerBean) {
		this.registerBean = registerBean;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getShortDescription() {
		return shortDescription;
	}

	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}

	public ImageEntity getImage() {
		return image;
	}

	public void setImage(ImageEntity image) {
		this.image = image;
	}

	public BusinessRuleEntity getAccessRule() {
		return accessRule;
	}

	public void setAccessRule(BusinessRuleEntity accessRule) {
		this.accessRule = accessRule;
	}

	public Boolean getPublished() {
		return published;
	}

	public void setPublished(Boolean published) {
		this.published = published;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

}
