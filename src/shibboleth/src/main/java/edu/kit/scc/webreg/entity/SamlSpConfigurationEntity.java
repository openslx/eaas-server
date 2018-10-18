package edu.kit.scc.webreg.entity;

import java.util.Arrays;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import org.hibernate.annotations.Type;


@Entity
@Table(name = "spconfig", schema = "shib")
public class SamlSpConfigurationEntity extends SamlSpMetadataEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "private_key")
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	private String privateKey;

	@Column(name = "certificate")
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	private String certificate;

	@Column(name = "acs", length = 2048)
	private String acs;

	@Column(name = "ecp", length = 2048)
	private String ecp;
	
	@Column(name = "enabled")
	@Type(type = "org.hibernate.type.BooleanType")
	private boolean enabled;

//	@ElementCollection(fetch = FetchType.EAGER)
//	private List<String> hostNameList = new ArrayList<String>();
	
	@Column(name = "hostNameList", length = 32672)
	private String hostNameList;

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public String getCertificate() {
		return certificate;
	}

	public void setCertificate(String certificate) {
		this.certificate = certificate;
	}

	public List<String> getHostNameList() {
		//return hostNameList;
		return Arrays.asList(this.hostNameList.split(";"));
	}

	public void setHostNameList(List<String> hostNameList) {
//		this.hostNameList = hostNameList;
		StringBuilder s = new StringBuilder();
		for (String name : hostNameList) 
			s.append(name + ";");
		this.hostNameList = s.toString();
	}

	public String getAcs() {
		return acs;
	}

	public void setAcs(String acs) {
		this.acs = acs;
	}

	public String getEcp() {
		return ecp;
	}

	public void setEcp(String ecp) {
		this.ecp = ecp;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
