package com.interfactura.firmalocal.domain.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.validation.constraints.Size;

@Entity
public class AddendumCustoms 
	extends BaseEntity implements Serializable 
{

	private static final long serialVersionUID = 1L;

	@Size(min = 1)
	private String pedimento;
	private Date dateCustoms;
	@Size(min = 1)
	private String nameOfCustoms;
	private String source;
	private String aux;

	public String getPedimento() {
		return pedimento;
	}

	public void setPedimento(String pedimento) {
		this.pedimento = pedimento;
	}

	public Date getDateCustoms() {
		return dateCustoms;
	}

	public void setDateCustoms(Date dateCustoms) {
		this.dateCustoms = dateCustoms;
	}

	public String getNameOfCustoms() {
		return nameOfCustoms;
	}

	public void setNameOfCustoms(String nameOfCustoms) {
		this.nameOfCustoms = nameOfCustoms;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getAux() {
		return aux;
	}

	public void setAux(String aux) {
		this.aux = aux;
	}
}
