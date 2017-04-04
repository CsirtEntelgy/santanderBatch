package com.interfactura.firmalocal.domain.entities;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
public class EmissionPermission extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@OneToOne(cascade=CascadeType.ALL)
	private Branch branch;
	@OneToOne(cascade=CascadeType.ALL)
	private Series series;

	public Branch getBranch() {
		return branch;
	}

	public void setBranch(Branch branch) {
		this.branch = branch;
	}

	public Series getSeries() {
		return series;
	}

	public void setSeries(Series series) {
		this.series = series;
	}
}
