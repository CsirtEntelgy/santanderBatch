package com.interfactura.firmalocal.datamodel;

import java.util.List;

public class Part {

	private String cantidad;
	private String unidad;
	private String noIdentificacion;
	private String descripcion;
	private String valorUnitario;
	private String importe;
	private List<CustomsInformation> aduana; 

	public String getCantidad() {
		return cantidad;
	}

	public void setCantidad(String cantidad) {
		this.cantidad = cantidad;
	}

	public String getUnidad() {
		return unidad;
	}

	public void setUnidad(String unidad) {
		this.unidad = unidad;
	}

	public String getNoIdentificacion() {
		return noIdentificacion;
	}

	public void setNoIdentificacion(String noIdentificacion) {
		this.noIdentificacion = noIdentificacion;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getValorUnitario() {
		return valorUnitario;
	}

	public void setValorUnitario(String valorUnitario) {
		this.valorUnitario = valorUnitario;
	}

	public String getImporte() {
		return importe;
	}

	public void setImporte(String importe) {
		this.importe = importe;
	}

	public List<CustomsInformation> getAduana() {
		return aduana;
	}

	public void setAduana(List<CustomsInformation> aduana) {
		this.aduana = aduana;
	}
}
