package edu.kit.scc.webreg.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "spmetadata", schema = "shib")
public class SamlSpMetadataEntity extends SamlMetadataEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@ManyToOne
	private FederationEntity federation;

	public FederationEntity getFederation() {
		return federation;
	}

	public void setFederation(FederationEntity federation) {
		this.federation = federation;
	}

}
