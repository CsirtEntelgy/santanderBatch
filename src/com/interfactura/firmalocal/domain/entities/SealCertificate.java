package com.interfactura.firmalocal.domain.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

@Entity
public class SealCertificate extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Lob
	private byte[] certificate;
	@Lob
	private byte[] privateKey;
	private String privateKeyPassword;
	private String certificateName;
	private String privateKeyName;
	private Date startOfValidity;
	private Date endOfValidity;
	private String serialNumber;
	@OneToOne(cascade = CascadeType.PERSIST)
	@NotNull
	private FiscalEntity fiscalEntity;
	
	public byte[] getCertificate() {
		return certificate;
	}

	public void setCertificate(byte[] certificate) {
		this.certificate = certificate;
	}

	public byte[] getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(byte[] privateKey) {
		this.privateKey = privateKey;
	}

	public String getPrivateKeyPassword() {
		return privateKeyPassword;
	}

	public void setPrivateKeyPassword(String privateKeyPassword) {
		this.privateKeyPassword = privateKeyPassword;
	}

	public Date getStartOfValidity() {
		return startOfValidity;
	}

	public void setStartOfValidity(Date startOfValidity) {
		this.startOfValidity = startOfValidity;
	}

	public Date getEndOfValidity() {
		return endOfValidity;
	}

	public void setEndOfValidity(Date endOfValidity) {
		this.endOfValidity = endOfValidity;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public String getCertificateName() {
		return certificateName;
	}

	public void setCertificateName(String certificateName) {
		this.certificateName = certificateName;
	}

	public String getPrivateKeyName() {
		return privateKeyName;
	}

	public void setPrivateKeyName(String privateKeyName) {
		this.privateKeyName = privateKeyName;
	}

	public FiscalEntity getFiscalEntity() {
		return fiscalEntity;
	}

	public void setFiscalEntity(FiscalEntity fiscalEntity) {
		this.fiscalEntity = fiscalEntity;
	}
	
}
