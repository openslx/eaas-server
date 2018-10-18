package edu.kit.scc.webreg.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "idpmetadata_scope", schema = "shib")
public class SamlIdpScopeEntity extends AbstractBaseEntity {
 
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;

	@Column(name = "scope", nullable = false, length = 512)
	private String scope;
	
	@Column(name = "regex")
	private Boolean regex;

	@ManyToOne
	private SamlIdpMetadataEntity idp;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public Boolean getRegex() {
		return regex;
	}

	public void setRegex(Boolean regex) {
		this.regex = regex;
	}

	public SamlIdpMetadataEntity getIdp() {
		return idp;
	}

	public void setIdp(SamlIdpMetadataEntity idp) {
		this.idp = idp;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) return false;
		
	    return this.getClass().equals(other.getClass()) && 
	    		(getScope() != null) && (getIdp() != null)
	         ? getScope().equals(((SamlIdpScopeEntity) other).getScope()) &&
	        		 getIdp().equals(((SamlIdpScopeEntity) other).getIdp())
	         : (other == this);
	}
	
	@Override
	public int hashCode() {
	    return (getScope() != null && getIdp() != null) 
		         ? (getClass().hashCode() + getScope().hashCode() + getIdp().hashCode())
		         : super.hashCode();
	}
	
}
