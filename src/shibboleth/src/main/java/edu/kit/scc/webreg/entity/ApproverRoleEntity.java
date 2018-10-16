package edu.kit.scc.webreg.entity;

import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

@Entity
public class ApproverRoleEntity extends RoleEntity {

	private static final long serialVersionUID = 1L;

	@OneToMany(mappedBy="approverRole")
	private Set<ServiceEntity> approverForServices;

	@NotNull
	@Column(name="approval_bean", length=255)
	private String approvalBean;
	
	public Set<ServiceEntity> getApproverForServices() {
		return approverForServices;
	}

	public void setApproverForServices(Set<ServiceEntity> approverForServices) {
		this.approverForServices = approverForServices;
	}

	public String getApprovalBean() {
		return approvalBean;
	}

	public void setApprovalBean(String approvalBean) {
		this.approvalBean = approvalBean;
	}	
}
