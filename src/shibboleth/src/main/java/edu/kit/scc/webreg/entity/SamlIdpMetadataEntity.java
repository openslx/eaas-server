package edu.kit.scc.webreg.entity;

import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "idpmetadata", schema = "shib")
public class SamlIdpMetadataEntity extends SamlMetadataEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@ManyToOne
	private FederationEntity federation;

	@Column(name = "entity_desc")
	@Lob
	@Type(type = "org.hibernate.type.TextType")	
	private String entityDescriptor;

	@Column(name = "org_name", length = 512)
	private String orgName;
	
	@OneToMany(mappedBy = "idp", cascade = CascadeType.REMOVE)
	private Set<SamlIdpScopeEntity> scopes; 
	
	public FederationEntity getFederation() {
		return federation;
	}

	public void setFederation(FederationEntity federation) {
		this.federation = federation;
	}

	public String getEntityDescriptor() {
		return entityDescriptor;
	}

	public void setEntityDescriptor(String entityDescriptor) {
		this.entityDescriptor = entityDescriptor;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public Set<SamlIdpScopeEntity> getScopes() {
		return scopes;
	}

	public void setScopes(Set<SamlIdpScopeEntity> scopes) {
		this.scopes = scopes;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other == null) return false;
		
	    return this.getClass().equals(other.getClass()) && 
	    		(getEntityId() != null) && (getFederation() != null)
	         ? getEntityId().equals(((SamlIdpMetadataEntity) other).getEntityId()) &&
	        		 getFederation().equals(((SamlIdpMetadataEntity) other).getFederation())
	         : (other == this);
	}
	
	@Override
	public int hashCode() {
	    return (getEntityId() != null && getFederation() != null) 
		         ? (getClass().hashCode() + getEntityId().hashCode() + getFederation().hashCode())
		         : super.hashCode();
	}
	
}
