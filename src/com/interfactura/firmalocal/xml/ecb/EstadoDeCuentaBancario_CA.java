package com.interfactura.firmalocal.xml.ecb;

public class EstadoDeCuentaBancario_CA {
	private String version;
	private String numeroCuenta;
	private String nombreCliente;
	private String periodo;
	private String sucursal;
	
	public EstadoDeCuentaBancario_CA(){
		this.version = "";
		this.numeroCuenta = "";
		this.nombreCliente = "";
		this.periodo = "";
		this.sucursal = "";
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getNumeroCuenta() {
		return numeroCuenta;
	}

	public void setNumeroCuenta(String numeroCuenta) {
		this.numeroCuenta = numeroCuenta;
	}

	public String getNombreCliente() {
		return nombreCliente;
	}

	public void setNombreCliente(String nombreCliente) {
		this.nombreCliente = nombreCliente;
	}

	public String getPeriodo() {
		return periodo;
	}

	public void setPeriodo(String periodo) {
		this.periodo = periodo;
	}

	public String getSucursal() {
		return sucursal;
	}

	public void setSucursal(String sucursal) {
		this.sucursal = sucursal;
	}

}
