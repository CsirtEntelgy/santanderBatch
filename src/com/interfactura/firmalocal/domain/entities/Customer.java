package com.interfactura.firmalocal.domain.entities;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.format.annotation.NumberFormat;
import org.springframework.format.annotation.NumberFormat.Style;

@Entity
public class Customer extends BaseEntity
	implements Serializable 
{
	
	private static final long serialVersionUID = 1L;

	//--- Propiedades
	@Size(min = 1)
	private String physicalName;
	@Size(min = 1)
	private String taxId;
	@OneToOne(cascade=CascadeType.ALL)
	private Address address;
	@NotNull
	@NumberFormat(style = Style.NUMBER)
	@Min(0)
	@Column(name="customerNumber")
	private Integer number;
	@OneToOne(cascade=CascadeType.PERSIST)
	@NotNull
	private FiscalEntity fiscalEntity;
	@Column(name="foreignid")
	private String idExtranjero;
	
	public String getPhysicalName() {
		return physicalName;
	}
	public void setPhysicalName(String physicalName) {
		this.physicalName = physicalName;
	}
	public String getTaxId() {
		return taxId;
	}
	public void setTaxId(String taxId) {
		this.taxId = taxId;
	}
	public Address getAddress() {
		return address;
	}
	public void setAddress(Address address) {
		this.address = address;
	}
	public Integer getNumber() {
		return number;
	}
	public void setNumber(Integer number) {
		this.number = number;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public FiscalEntity getFiscalEntity() {
		return fiscalEntity;
	}
	public void setFiscalEntity(FiscalEntity fiscalEntity) {
		this.fiscalEntity = fiscalEntity;
	}
	public String getIdExtranjero() {
		return idExtranjero;
	}
	public void setIdExtranjero(String idExtranjero) {
		this.idExtranjero = idExtranjero;
	}	
}
