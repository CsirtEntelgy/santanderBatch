package com.interfactura.firmalocal.domain.entities;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
public class Series extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Size(min = 1)
	private String name;
	@Size(min = 1)
	private String documentType;
	@OneToOne(cascade=CascadeType.PERSIST)
	@JoinColumn(name = "FISCALENTITY_ID", unique = false, nullable = false)
	@NotNull
	private FiscalEntity fiscalEntity;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDocumentType() {
		return documentType;
	}

	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}

	public FiscalEntity getFiscalEntity() {
		return fiscalEntity;
	}

	public void setFiscalEntity(FiscalEntity fiscalEntity) {
		this.fiscalEntity = fiscalEntity;
	}
	
}
