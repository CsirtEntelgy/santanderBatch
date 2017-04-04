package com.interfactura.firmalocal.domain.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.Size;

import org.springmodules.validation.bean.conf.loader.annotation.handler.Length;
import org.springmodules.validation.bean.conf.loader.annotation.handler.NotBlank;

@Entity
public class Country extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column
	@NotBlank
	@Length(min=1,max=20)
	private String name;
	@Column
	@Size(min = 1,max=5)
	private String countryCode;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}
}
