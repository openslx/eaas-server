package edu.kit.scc.webreg.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "agreement_text", schema = "shib")
public class AgreementTextEntity extends AbstractBaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;

	@Column(name = "agreement")
	@Lob
	@Type(type = "org.hibernate.type.TextType")	
	private String agreement;
	
	@Column(name = "name", length = 128)
	private String name;
	
	@ManyToOne
	private PolicyEntity policy;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAgreement() {
		return agreement;
	}

	public void setAgreement(String agreement) {
		this.agreement = agreement;
	}

	public PolicyEntity getPolicy() {
		return policy;
	}

	public void setPolicy(PolicyEntity policy) {
		this.policy = policy;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
