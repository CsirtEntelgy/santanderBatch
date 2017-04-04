package com.interfactura.firmalocal.domain.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

import org.springframework.format.annotation.NumberFormat;
import org.springframework.format.annotation.NumberFormat.Style;

@Entity
public class Iva implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private long id;
	@NumberFormat(style = Style.NUMBER)
	@Min(0)
	@Max(100)
	@Column(name = "PORCENTAJE")
	private Integer tasa;
	@Size(min = 1)
	private String descripcion;
	
	
	public Integer getTasa() {
		return tasa;
	}
	
	public void setTasa(Integer tasa) {
		this.tasa = tasa;
	}
	
	public String getDescripcion() {
		return descripcion;
	}
	
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
}
