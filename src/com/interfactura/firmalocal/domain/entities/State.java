package com.interfactura.firmalocal.domain.entities;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
/**
 * Entity implementation class for Entity: Estado
 *
 */
// @Entity
public class State extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column
	@Size(min = 1)
	private String name;
	@OneToOne(cascade = CascadeType.PERSIST)
	@JoinColumn(name = "COUNTRY_ID", unique = false, nullable = false)
	@NotNull
	private Country country;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}

	
}
