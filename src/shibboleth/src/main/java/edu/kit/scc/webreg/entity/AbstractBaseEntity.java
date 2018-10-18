package edu.kit.scc.webreg.entity;

import java.io.Serializable;

public abstract class AbstractBaseEntity implements BaseEntity, Serializable {

	private static final long serialVersionUID = 1L;

	public boolean equals(Object other) {
		if (other == null) return false;
		
	    return this.getClass().equals(other.getClass()) && 
	    		(getId() != null) 
	         ? getId().equals(((BaseEntity) other).getId()) 
	         : (other == this);
	}

	public int hashCode() {
	    return (getId() != null) 
	         ? (getClass().hashCode() + getId().hashCode())
	         : super.hashCode();
	}		
}
