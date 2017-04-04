package com.interfactura.firmalocal.domain.entities;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.NumberFormat;
import org.springframework.format.annotation.NumberFormat.Style;

@Entity
public class FolioRange extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@NotNull
	@NumberFormat(style = Style.NUMBER)
	@Min(1)
	private Integer initialFolio;
	@NotNull
	@NumberFormat(style = Style.NUMBER)
	@Min(1)
	private Integer finalFolio;
	private Integer actualFolio;
	private String estatus;
	@OneToOne(cascade=CascadeType.PERSIST)
	@JoinColumn(name = "SERIES_ID", unique = false, nullable = false)
	private Series series;
	@OneToOne(cascade=CascadeType.PERSIST)
	@JoinColumn(name = "FISCALENTITY_ID", unique = false, nullable = false)
	@NotNull
	private FiscalEntity fiscalEntity;
	@NotNull
	@NumberFormat(style = Style.NUMBER)
	private Integer authorizationNumber;
	@NotNull
	@Min(2010)
	private int yearOfAuthorization;

	public Integer getInitialFolio() {
		return initialFolio;
	}

	public void setInitialFolio(Integer initialFolio) {
		this.initialFolio = initialFolio;
	}

	public Integer getFinalFolio() {
		return finalFolio;
	}

	public void setFinalFolio(Integer finalFolio) {
		this.finalFolio = finalFolio;
	}

	public Series getSeries() {
		return series;
	}

	public void setSeries(Series series) {
		this.series = series;
	}

	public Integer getAuthorizationNumber() {
		return authorizationNumber;
	}

	public void setAuthorizationNumber(Integer authorizationNumber) {
		this.authorizationNumber = authorizationNumber;
	}

	public int getYearOfAuthorization() {
		return yearOfAuthorization;
	}

	public void setYearOfAuthorization(int yearOfAuthorization) {
		this.yearOfAuthorization = yearOfAuthorization;
	}

	public FiscalEntity getFiscalEntity() {
		return fiscalEntity;
	}

	public void setFiscalEntity(FiscalEntity fiscalEntity) {
		this.fiscalEntity = fiscalEntity;
	}

	public Integer getActualFolio() {
		return actualFolio;
	}

	public void setActualFolio(Integer actualFolio) {
		this.actualFolio = actualFolio;
	}

	public String getEstatus() {
		return estatus;
	}

	public void setEstatus(String estatus) {
		this.estatus = estatus;
	}

}
