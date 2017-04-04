package com.interfactura.firmalocal.xml.ecb;

import java.util.Date;

public class MovimientoECB implements Comparable<MovimientoECB> {
	private String fecha;
	private String referencia;
	private String descripcion;
	private String importe;
	private String moneda;
	private String saldoInicial;
	private String saldoAlCorte;
	private String rfcEnajenante;
	private boolean fiscal;
	private Date fechaOrden;
	
	public MovimientoECB(){
		this.fecha = "";
		this.referencia = "";
		this.descripcion = "";
		this.importe = "";
		this.moneda = "";
		this.saldoInicial = "";
		this.saldoAlCorte = "";
		this.rfcEnajenante = "";
		this.fiscal = false;
	}

	public String getFecha() {
		return fecha;
	}

	public void setFecha(String fecha) {
		this.fecha = fecha;
	}

	public String getReferencia() {
		return referencia;
	}

	public void setReferencia(String referencia) {
		this.referencia = referencia;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getImporte() {
		return importe;
	}

	public void setImporte(String importe) {
		this.importe = importe;
	}

	public String getMoneda() {
		return moneda;
	}

	public void setMoneda(String moneda) {
		this.moneda = moneda;
	}

	public String getSaldoInicial() {
		return saldoInicial;
	}

	public void setSaldoInicial(String saldoInicial) {
		this.saldoInicial = saldoInicial;
	}

	public String getSaldoAlCorte() {
		return saldoAlCorte;
	}

	public void setSaldoAlCorte(String saldoAlCorte) {
		this.saldoAlCorte = saldoAlCorte;
	}

	public String getRfcEnajenante() {
		return rfcEnajenante;
	}

	public void setRfcEnajenante(String rfcEnajenante) {
		this.rfcEnajenante = rfcEnajenante;
	}

	public boolean getFiscal() {
		return fiscal;
	}

	public void setFiscal(boolean fiscal) {
		this.fiscal = fiscal;
	}
	
	public Date getFechaOrden() {
		return fechaOrden;
	}

	public void setFechaOrden(Date fechaOrden) {
		this.fechaOrden = fechaOrden;
	}

	@Override
	public int compareTo(MovimientoECB m){
		// TODO Auto-generated method stub
		if(this.fechaOrden.before(m.getFechaOrden())){
			return -1;
		}
		if(m.getFechaOrden().before(this.fechaOrden)){
			return 1;
		}
		return 0;
	}	
	
}
