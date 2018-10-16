package edu.kit.scc.webreg.entity;

import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "policy", schema = "shib")
public class PolicyEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;

	@ManyToOne
	private ServiceEntity service;
	
	@OneToMany(mappedBy="policy")
	private Set<AgreementTextEntity> agreementTexts;

	@OneToOne
	private AgreementTextEntity actualAgreement;
	
	@Column(name = "mandatory")
	private Boolean mandatory;
	
	@Column(name = "name", length = 128)
	private String name;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Boolean getMandatory() {
		return mandatory;
	}

	public void setMandatory(Boolean mandatory) {
		this.mandatory = mandatory;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ServiceEntity getService() {
		return service;
	}

	public void setSevice(ServiceEntity service) {
		this.service = service;
	}

	public Set<AgreementTextEntity> getAgreementTexts() {
		return agreementTexts;
	}

	public void setAgreementTexts(Set<AgreementTextEntity> agreementTexts) {
		this.agreementTexts = agreementTexts;
	}

	public AgreementTextEntity getActualAgreement() {
		return actualAgreement;
	}

	public void setActualAgreement(AgreementTextEntity actualAgreement) {
		this.actualAgreement = actualAgreement;
	}

}
