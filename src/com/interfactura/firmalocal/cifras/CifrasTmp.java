package com.interfactura.firmalocal.cifras;

public class CifrasTmp {

	private CifrasIE cifrasIE;
	
	private String rfc;
	private String nombre;
	private String moneda;	
	private String tipo;
	private String fechaEmision;
	
	private String periodo;
	private String tasaIva;
	
	public CifrasTmp(){
		this.cifrasIE = new CifrasIE();
		
		this.rfc = "";
		this.nombre = "";
		this.moneda = "";
		this.tipo = "";
		
		this.periodo = "";
		this.tasaIva = "";
	}

	public String getRfc() {
		return rfc;
	}

	public void setRfc(String rfc) {
		this.rfc = rfc;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getMoneda() {
		return moneda;
	}

	public void setMoneda(String moneda) {
		this.moneda = moneda;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public CifrasIE getCifrasIE() {
		return cifrasIE;
	}

	public void setCifrasIE(CifrasIE cifrasIE) {
		this.cifrasIE = cifrasIE;
	}

	public String getFechaEmision() {
		return fechaEmision;
	}

	public void setFechaEmision(String fechaEmision) {
		this.fechaEmision = fechaEmision;
	}

	public String getPeriodo() {
		return periodo;
	}

	public void setPeriodo(String periodo) {
		this.periodo = periodo;
	}

	public String getTasaIva() {
		return tasaIva;
	}

	public void setTasaIva(String tasaIva) {
		this.tasaIva = tasaIva;
	}
	
	
}
