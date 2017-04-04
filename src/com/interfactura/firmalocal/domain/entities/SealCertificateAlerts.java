package com.interfactura.firmalocal.domain.entities;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.format.annotation.NumberFormat;
import org.springframework.format.annotation.NumberFormat.Style;

@Entity
public class SealCertificateAlerts extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Size(min = 1)
	private String emails;
	@NotNull
	@NumberFormat(style = Style.NUMBER)
	@Min(1)
	private Integer normalAdvice;
	@NotNull
	@NumberFormat(style = Style.NUMBER)
	@Min(1)
	private Integer urgentAdvice;
	@OneToOne(cascade=CascadeType.PERSIST)
	@JoinColumn(name = "FISCALENTITY_ID", unique = false, nullable = false)
	@NotNull
	private FiscalEntity fiscalEntity;
	
	public FiscalEntity getFiscalEntity() {
		return fiscalEntity;
	}
	public void setFiscalEntity(FiscalEntity fiscalEntity) {
		this.fiscalEntity = fiscalEntity;
	}
	public String getEmails() {
		return emails;
	}
	public void setEmails(String emails) {
		this.emails = emails;
	}
	public Integer getNormalAdvice() {
		return normalAdvice;
	}
	public void setNormalAdvice(Integer normalAdvice) {
		this.normalAdvice = normalAdvice;
	}
	public Integer getUrgentAdvice() {
		return urgentAdvice;
	}
	public void setUrgentAdvice(Integer urgentAdvice) {
		this.urgentAdvice = urgentAdvice;
	}
	
}
