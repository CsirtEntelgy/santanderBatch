package com.interfactura.firmalocal.domain.entities;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * Clase que representa a la tabla RegimenFiscal.
 * 
 * @author hlara
 * 
 */
@Entity
public class RegimenFiscal extends BaseEntity {

	private static final long serialVersionUID = 5110331798426371471L;
	@Column
	private String code;
	@Column
	private String name;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
