package edu.kit.scc.webreg.entity;

import java.util.Date;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "federation", schema = "shib")
public class FederationEntity extends AbstractBaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;

	@Column(name = "federation_name", length = 128, unique = true)
	private String name;

	@Column(name = "entity_id", length = 2048, unique = true)
	private String entityId;

	@Column(name = "federation_metadata_url", length = 2048, unique = true)
	private String federationMetadataUrl;

	@Column(name = "entity_category_filter", length = 512)
	private String entityCategoryFilter;

	@Column(name = "polled_at")
	private Date polledAt;
	
	@OneToMany(mappedBy="federation")
	private Set<SamlIdpMetadataEntity> idpEntities;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public String getFederationMetadataUrl() {
		return federationMetadataUrl;
	}

	public void setFederationMetadataUrl(String federationMetadataUrl) {
		this.federationMetadataUrl = federationMetadataUrl;
	}

	public Date getPolledAt() {
		return polledAt;
	}

	public void setPolledAt(Date polledAt) {
		this.polledAt = polledAt;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setIdpEntities(Set<SamlIdpMetadataEntity> idpEntities) {
		this.idpEntities = idpEntities;
	}
	
	public Set<SamlIdpMetadataEntity> getIdpEntities() {
		return idpEntities;
	}

	public String getEntityCategoryFilter() {
		return entityCategoryFilter;
	}

	public void setEntityCategoryFilter(String entityCategoryFilter) {
		this.entityCategoryFilter = entityCategoryFilter;
	}
	
}
