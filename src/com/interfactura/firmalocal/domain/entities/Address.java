package com.interfactura.firmalocal.domain.entities;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.validation.constraints.Size;


@Entity
public class Address extends BaseEntity
	implements Serializable 
{
	
	private static final long serialVersionUID = 1L;
	
	//--- Propiedades
	private String internalNumber;
	@Size(min = 1)
	private String externalNumber;
	@Size(min = 1)
	private String street;
	@Size(min = 1)
	private String neighborhood;
	@Size(min = 1)
	private String region;
	@Size(min = 1)
	private String city;
	@OneToOne(cascade=CascadeType.PERSIST)
	private State state;
	@Size(min = 1)
	private String zipCode;
	private String reference;
	private boolean fiscal;
	
	//--- Accesos

	
	public String getStreet() {
		return street;
	}
	public String getInternalNumber() {
		return internalNumber;
	}
	public void setInternalNumber(String internalNumber) {
		this.internalNumber = internalNumber;
	}
	public String getExternalNumber() {
		return externalNumber;
	}
	public void setExternalNumber(String externalNumber) {
		this.externalNumber = externalNumber;
	}
	public void setStreet(String street) {
		this.street = street;
	}
	public String getNeighborhood() {
		return neighborhood;
	}
	public void setNeighborhood(String neighborhood) {
		this.neighborhood = neighborhood;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public State getState() {
		return state;
	}
	public void setState(State state) {
		this.state = state;
	}
	public String getZipCode() {
		return zipCode;
	}
	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}
	public String getReference() {
		return reference;
	}
	public void setReference(String reference) {
		this.reference = reference;
	}
	public boolean isFiscal() {
		return fiscal;
	}
	public void setFiscal(boolean fiscal) {
		this.fiscal = fiscal;
	}
		
}
