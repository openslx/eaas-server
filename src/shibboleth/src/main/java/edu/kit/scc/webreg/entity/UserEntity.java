package edu.kit.scc.webreg.entity;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;

@Entity
@Table(name = "usertable", schema = "shib")
public class UserEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;

	@Column(name = "persistent_id", length = 1024)
	private String persistentId;
	
	@Column(name = "persistent_spid", length = 1024)
	private String persistentSpId;
	
	@Column(name = "persistent_idpid", length = 1024)
	private String persistentIdpId;
	
	@Column(name = "eppn", length = 1024, unique = true, nullable = false)
	private String eppn;
	
	@Column(name = "email", length = 1024)
	private String email;
	
	@Column(name = "given_name", length = 256)
	private String givenName;
	
	@Column(name = "sur_name", length = 256)
	private String surName;

	@Column(name = "uid_number", unique = true, nullable = false)
	private Integer uidNumber;
	
	@ManyToMany(targetEntity=RoleEntity.class, cascade = CascadeType.ALL)
	@JoinTable(name = "user_role", schema = "shib",
			joinColumns = @JoinColumn(name="user_id"),
			inverseJoinColumns = @JoinColumn(name="role_id")
	)
	private Set<RoleEntity> roles;
	
	@ElementCollection(fetch = FetchType.EAGER)
	@JoinTable(name = "user_store", schema = "shib")
    @MapKeyColumn(name = "key_data", length = 128)
    @Column(name = "value_data", length = 2048)
    private Map<String, String> genericStore; 
	
	@ElementCollection(fetch = FetchType.EAGER)
	@JoinTable(name = "user_attribute_store", schema = "shib")
    @MapKeyColumn(name = "key_data", length = 1024)
    @Column(name = "value_data", length = 2048)
    private Map<String, String> attributeStore; 

	@ManyToMany(targetEntity=GroupEntity.class, cascade = CascadeType.ALL)
	@JoinTable(name = "user_secondary_group", schema = "shib",
			joinColumns = @JoinColumn(name="user_id"),
			inverseJoinColumns = @JoinColumn(name="group_id")
	)
	private Set<GroupEntity> groups;
	
	@Enumerated(EnumType.STRING)
	private UserStatus userStatus;
	
	@Column(name = "last_update")
	private Date lastUpdate;
	
	@Column(name = "theme", length = 128)
	private String theme;
	
	@ManyToOne
	private GroupEntity primaryGroup;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Set<RoleEntity> getRoles() {
		return roles;
	}

	public void setRoles(Set<RoleEntity> roles) {
		this.roles = roles;
	}

	public String getPersistentId() {
		return persistentId;
	}

	public void setPersistentId(String persistentId) {
		this.persistentId = persistentId;
	}

	public String getEppn() {
		return eppn;
	}

	public void setEppn(String eppn) {
		this.eppn = eppn;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getGivenName() {
		return givenName;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	public String getSurName() {
		return surName;
	}

	public void setSurName(String surName) {
		this.surName = surName;
	}

	public Map<String, String> getGenericStore() {
		return genericStore;
	}

	public void setGenericStore(Map<String, String> genericStore) {
		this.genericStore = genericStore;
	}

	public Map<String, String> getAttributeStore() {
		return attributeStore;
	}

	public void setAttributeStore(Map<String, String> attributeStore) {
		this.attributeStore = attributeStore;
	}

	public UserStatus getUserStatus() {
		return userStatus;
	}

	public void setUserStatus(UserStatus userStatus) {
		this.userStatus = userStatus;
	}

	public String getPersistentSpId() {
		return persistentSpId;
	}

	public void setPersistentSpId(String persistentSpId) {
		this.persistentSpId = persistentSpId;
	}

	public String getPersistentIdpId() {
		return persistentIdpId;
	}

	public void setPersistentIdpId(String persistentIdpId) {
		this.persistentIdpId = persistentIdpId;
	}

	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public Integer getUidNumber() {
		return uidNumber;
	}

	public void setUidNumber(Integer uidNumber) {
		this.uidNumber = uidNumber;
	}

	public GroupEntity getPrimaryGroup() {
		return primaryGroup;
	}

	public void setPrimaryGroup(GroupEntity primaryGroup) {
		this.primaryGroup = primaryGroup;
	}

	public Set<GroupEntity> getGroups() {
		return groups;
	}

	public void setGroups(Set<GroupEntity> groups) {
		this.groups = groups;
	}

}
