package com.interfactura.firmalocal.domain.entities;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.validation.constraints.Size;


/**
 * Entity implementation class for Entity: Role
 * 
 */
@Entity
public class Role extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Size(min = 1)
	private String name;
	@OneToMany(targetEntity = com.interfactura.firmalocal.domain.entities.Permission.class, 
			   cascade=CascadeType.PERSIST,
			   fetch = FetchType.EAGER)
	@JoinTable(name = "ROLE_PERMISSION", joinColumns = @JoinColumn(name = "ROLE_ID"), inverseJoinColumns = @JoinColumn(name = "PERMISSIONS_ID"))	   
	private Set<Permission> permissions;
	private boolean administrator;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<Permission> getPermissions() {
		return permissions;
	}

	public void setPermissions(Set<Permission> permissions) {
		this.permissions = permissions;
	}

	public boolean isAdministrator() {
		return administrator;
	}

	public void setAdministrator(boolean administrator) {
		this.administrator = administrator;
	}

}
