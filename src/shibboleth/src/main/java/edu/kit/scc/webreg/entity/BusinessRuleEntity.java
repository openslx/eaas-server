package edu.kit.scc.webreg.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "business_rule", schema = "shib")
public class BusinessRuleEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;

	@Column(name = "rule_text")
	@Lob
	@Type(type = "org.hibernate.type.TextType")	
	private String rule;
	
	@Column(name = "rule_type", length = 32)
	private String ruleType;
	
	@Column(name = "name", length = 128)
	private String name;
	
	@Column(name = "base_name", length = 128)
	private String knowledgeBaseName;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRule() {
		return rule;
	}

	public void setRule(String rule) {
		this.rule = rule;
	}

	public boolean equals(Object other) {
	    return (this.getClass().equals(other.getClass())) && (getId() != null) 
	         ? getId().equals(((BaseEntity) other).getId()) 
	         : (other == this);
	}

	public int hashCode() {
	    return (getId() != null) 
	         ? (getClass().hashCode() + getId().hashCode())
	         : super.hashCode();
	}

	public String getRuleType() {
		return ruleType;
	}

	public void setRuleType(String ruleType) {
		this.ruleType = ruleType;
	}

	public String getKnowledgeBaseName() {
		return knowledgeBaseName;
	}

	public void setKnowledgeBaseName(String knowledgeBaseName) {
		this.knowledgeBaseName = knowledgeBaseName;
	}	

}
