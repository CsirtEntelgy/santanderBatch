package com.interfactura.firmalocal.domain.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "CFD")
@SequenceGenerator(sequenceName = "CFD_SEQ", name = "CFD_SEQ_GEN")
public class CFD implements Serializable {

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getRFC() {
		return RFC;
	}

	public void setRFC(String rFC) {
		RFC = rFC;
	}

	public long getApprobationNumber() {
		return approbationNumber;
	}

	public void setApprobationNumber(long approbationNumber) {
		this.approbationNumber = approbationNumber;
	}

	public int getApprobationYear() {
		return approbationYear;
	}

	public void setApprobationYear(int approbationYear) {
		this.approbationYear = approbationYear;
	}

	public String getSerie() {
		return serie;
	}

	public void setSerie(String serie) {
		this.serie = serie;
	}

	public long getInitialFolio() {
		return initialFolio;
	}

	public void setInitialFolio(long initialFolio) {
		this.initialFolio = initialFolio;
	}

	public long getEndingFolio() {
		return endingFolio;
	}

	public void setEndingFolio(long endingFolio) {
		this.endingFolio = endingFolio;
	}

	@Id
	@GeneratedValue(generator = "CFD_SEQ_GEN", strategy = GenerationType.SEQUENCE)
	private long id;
	private String RFC;
	private long approbationNumber;
	private int approbationYear;
	private String serie;
	private long initialFolio;
	private long endingFolio;
	private static final long serialVersionUID = 954413464772587730L;

}
