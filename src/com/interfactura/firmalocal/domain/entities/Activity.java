package com.interfactura.firmalocal.domain.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.validation.constraints.Size;


@Entity
public class Activity 
	extends BaseEntity implements Serializable 
{

	private static final long serialVersionUID = 1L;

	@Size(min = 1)
	private String ipAddress;
	private User aUser;
	@Size(min = 1)
	private String action;
	private Date time;

	public String getIpAddress() {
		return ipAddress;
	}

	public String getAction() {
		return action;
	}

	public Date getTime() {
		return time;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public void setTime(Date time) {
		this.time = time;
	}
	
	public User getaUser() {
		return aUser;
	}
	
	public void setaUser(User aUser) {
		this.aUser = aUser;
	}
	

}
