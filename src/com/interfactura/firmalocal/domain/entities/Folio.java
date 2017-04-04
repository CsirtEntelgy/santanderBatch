package com.interfactura.firmalocal.domain.entities;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
public class Folio extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@OneToOne(cascade=CascadeType.PERSIST)
	private Series series;
	private Integer currentFolioNumber;
	private Integer amountOfAvailableLeaf;

	public Series getSeries() {
		return series;
	}

	public void setSeries(Series series) {
		this.series = series;
	}

	public Integer getCurrentFolioNumber() {
		return currentFolioNumber;
	}

	public void setCurrentFolioNumber(Integer currentFolioNumber) {
		this.currentFolioNumber = currentFolioNumber;
	}

	public Integer getAmountOfAvailableLeaf() {
		return amountOfAvailableLeaf;
	}

	public void setAmountOfAvailableLeaf(Integer amountOfAvailableLeaf) {
		this.amountOfAvailableLeaf = amountOfAvailableLeaf;
	}
}
