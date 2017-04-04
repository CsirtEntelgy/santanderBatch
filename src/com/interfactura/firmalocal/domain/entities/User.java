package com.interfactura.firmalocal.domain.entities;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import org.springmodules.validation.bean.conf.loader.annotation.handler.Email;


/**
 * Entity implementation class for Entity: User
 * 
 */
@Entity
@Table(name = "AppUser", uniqueConstraints = @UniqueConstraint(columnNames = "userName"))
public class User extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Size(min = 3, max = 20)
	private String userName;
	@Size(min = 1)
	@Email
	private String email;
	private String name;
	private String apellidoPaterno;
	private String apellidoMaterno;
	private String status;
	/*@OneToMany(targetEntity = com.interfactura.firmalocal.domain.entities.Role.class, cascade=CascadeType.PERSIST, fetch = FetchType.EAGER )
	@JoinTable(name= "APPUSER_ROLE", joinColumns=@JoinColumn(name="USER_ID"), inverseJoinColumns = @JoinColumn(name="ROLES_ID") )
	private Set<Role> roles;*/
	@OneToMany(targetEntity = com.interfactura.firmalocal.domain.entities.FiscalEntity.class, cascade=CascadeType.PERSIST, fetch = FetchType.EAGER )
	@JoinTable(name= "APPUSER_FISCALENTITY", joinColumns=@JoinColumn(name="USER_ID"), inverseJoinColumns = @JoinColumn(name="FISCALENTITY_ID") )
	private Set<FiscalEntity> fiscalEntities;

	@Column(name = "ID_AREA_ID_FK")
	private long idArea;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getApellidoPaterno() {
		return apellidoPaterno;
	}

	public void setApellidoPaterno(String apellidoPaterno) {
		this.apellidoPaterno = apellidoPaterno;
	}

	public String getApellidoMaterno() {
		return apellidoMaterno;
	}

	public void setApellidoMaterno(String apellidoMaterno) {
		this.apellidoMaterno = apellidoMaterno;
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
/*
	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}
*/
	public Set<FiscalEntity> getFiscalEntities() {
		return fiscalEntities;
	}

	public void setFiscalEntities(Set<FiscalEntity> fiscalEntities) {
		this.fiscalEntities = fiscalEntities;
	}

	public long getIdArea() {
		return idArea;
	}

	public void setIdArea(long idArea) {
		this.idArea = idArea;
	}

	
}
