package com.interfactura.firmalocal.datamodel;

public class Traslados {
	
	private String tipoImpuestos;
	private String impuesto;
	private String tipoFactor;
	private String tasaOCuota;
	private String importe;
	private String id;

	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTipoImpuestos() {
		return tipoImpuestos;
	}
	public void setTipoImpuestos(String tipoImpuestos) {
		this.tipoImpuestos = tipoImpuestos;
	}
	public String getImpuesto() {
		return impuesto;
	}
	public void setImpuesto(String impuesto) {
		this.impuesto = impuesto;
	}
	public String getTipoFactor() { 
		return tipoFactor;
	}
	public void setTipoFactor(String tipoFactor) {
		this.tipoFactor = tipoFactor;
	}
	public String getTasaOCuota() {
		return tasaOCuota;
	}
	public void setTasaOCuota(String tasaOCuota) {
		this.tasaOCuota = tasaOCuota;
	}
	public String getImporte() {
		return importe;
	}
	public void setImporte(String importe) {
		this.importe = importe;
	}
	@Override
	public String toString() {
		return "Traslados [tipoImpuestos=" + tipoImpuestos + ", impuesto=" + impuesto + ", tipoFactor=" + tipoFactor
				+ ", tasaOCuota=" + tasaOCuota + ", importe=" + importe + ", id=" + id + "]"; 
	}


	
	

}
