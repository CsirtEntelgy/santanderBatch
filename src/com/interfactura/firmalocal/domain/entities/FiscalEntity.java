package com.interfactura.firmalocal.domain.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.validation.constraints.Size;

import org.springmodules.validation.bean.conf.loader.annotation.handler.NotNull;

@Entity
public class FiscalEntity extends BaseEntity
	implements Serializable 
{
	
	private static final long serialVersionUID = 1L;

	//--- Propiedades
	@Size(min = 1)
	private String fiscalName;
	@Column(unique=true)
	@Size(min = 1)
	private String taxID;
	@NotNull
	@OneToOne(cascade=CascadeType.ALL)
	private Address address;
	private String noAutorizacionDonataria;
	private Date fechaAutorizacionDonataria;
	private String leyendaDonataria;
	private String versionDonataria;
	private int isDonataria;
	
	public String getFiscalName() {
		return fiscalName;
	}
	public void setFiscalName(String fiscalName) {
		this.fiscalName = fiscalName;
	}
	public String getTaxID() {
		return taxID;
	}
	public void setTaxID(String taxID) {
		this.taxID = taxID;
	}
	public Address getAddress() {
		return address;
	}
	public void setAddress(Address address) {
		this.address = address;
	}
	public int getIsDonataria() {
		return isDonataria;
	}
	public void setIsDonataria(int isDonataria) {
		this.isDonataria = isDonataria;
	}
	public String getNoAutorizacionDonataria() {
		return noAutorizacionDonataria;
	}
	public void setNoAutorizacionDonataria(String noAutorizacionDonataria) {
		this.noAutorizacionDonataria = noAutorizacionDonataria;
	}
	public Date getFechaAutorizacionDonataria() {
		return fechaAutorizacionDonataria;
	}
	public void setFechaAutorizacionDonataria(Date fechaAutorizacionDonataria) {
		this.fechaAutorizacionDonataria = fechaAutorizacionDonataria;
	}
	public String getLeyendaDonataria() {
		return leyendaDonataria;
	}
	public void setLeyendaDonataria(String leyendaDonataria) {
		this.leyendaDonataria = leyendaDonataria;
	}
	public String getVersionDonataria() {
		return versionDonataria;
	}
	public void setVersionDonataria(String versionDonataria) {
		this.versionDonataria = versionDonataria;
	}

	
}
