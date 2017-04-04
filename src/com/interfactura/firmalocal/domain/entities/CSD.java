package com.interfactura.firmalocal.domain.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "CSD")
@SequenceGenerator(sequenceName = "CSD_SEQ", name = "CSD_SEQ_GEN")
public class CSD implements Serializable {

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Date getDateBeginCer() {
		return dateBeginCer;
	}

	public void setDateBeginCer(Date dateBeginCer) {
		this.dateBeginCer = dateBeginCer;
	}

	public Date getDateEndCer() {
		return dateEndCer;
	}

	public void setDateEndCer(Date dateEndCer) {
		this.dateEndCer = dateEndCer;
	}

	public String getRFC() {
		return RFC;
	}

	public void setRFC(String rFC) {
		RFC = rFC;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public void setCertificateNumber(String certificateNumber) {
		this.certificateNumber = certificateNumber;
	}
	
	public String getCertificateNumber() {
		return certificateNumber;
	}

	@Id
	@GeneratedValue(generator = "CSD_SEQ_GEN", strategy = GenerationType.SEQUENCE)
	private long id;
	private String certificateNumber;
	private Date dateBeginCer;
	private Date dateEndCer;
	private String RFC;
	private String status;
	private static final long serialVersionUID = -4399891202007465382L;

}
