package com.interfactura.firmalocal.domain.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "CFDSEAILED")
@SequenceGenerator(sequenceName = "CFDSEAILED_SEQ", name = "CFDSEAILED_SEQ_GEN")
public class CFDSeailed implements Serializable {

	private static final long serialVersionUID = -4378459866484527923L;
	@Id
	@GeneratedValue(generator = "CFDSEAILED_SEQ_GEN", strategy = GenerationType.SEQUENCE)
	private long id;
	private String certificate;
	private String seal;
	private String version;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getCertificate() {
		return certificate;
	}

	public void setCertificate(String certificate) {
		this.certificate = certificate;
	}

	public String getSeal() {
		return seal;
	}

	public void setSeal(String seal) {
		this.seal = seal;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}
	
	public String getVersion() {
		return version;
	}

}
