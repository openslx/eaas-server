package edu.kit.scc.webreg.entity;

import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name = "group_store", schema = "shib")
public class GroupEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;

	@Column(name = "gid_number", unique = true, nullable = false)
	private Integer gidNumber;
	
	@Column(name = "group_name", nullable = false)
	private String name;
	
	@Column(name = "group_prefix")
	private String prefix;
	
    @ManyToMany(mappedBy="groups")
    private Set<UserEntity> users;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getGidNumber() {
		return gidNumber;
	}

	public void setGidNumber(Integer gidNumber) {
		this.gidNumber = gidNumber;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<UserEntity> getUsers() {
		return users;
	}

	public void setUsers(Set<UserEntity> users) {
		this.users = users;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

}
