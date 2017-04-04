package com.interfactura.firmalocal.domain.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
public class Report extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name="dateReport")
	private Date date;
	private String onlineReporte;
	private String cfd;
	@OneToOne(cascade=CascadeType.ALL)
	private FiscalEntity fiscalEntity;

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getOnlineReporte() {
		return onlineReporte;
	}

	public void setOnlineReporte(String onlineReporte) {
		this.onlineReporte = onlineReporte;
	}

	public String getCfd() {
		return cfd;
	}

	public void setCfd(String cfd) {
		this.cfd = cfd;
	}

	public FiscalEntity getFiscalEntity() {
		return fiscalEntity;
	}

	public void setFiscalEntity(FiscalEntity fiscalEntity) {
		this.fiscalEntity = fiscalEntity;
	}

}
