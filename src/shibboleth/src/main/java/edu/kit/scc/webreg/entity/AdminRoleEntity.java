package edu.kit.scc.webreg.entity;

import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

@Entity
public class AdminRoleEntity extends RoleEntity {

	private static final long serialVersionUID = 1L;

	@OneToMany(mappedBy="adminRole")
	private Set<ServiceEntity> adminForServices;

	public Set<ServiceEntity> getAdminForServices() {
		return adminForServices;
	}

	public void setAdminForServices(Set<ServiceEntity> adminForServices) {
		this.adminForServices = adminForServices;
	}
	
}
