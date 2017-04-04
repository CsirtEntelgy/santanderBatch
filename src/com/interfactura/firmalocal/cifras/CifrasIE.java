package com.interfactura.firmalocal.cifras;

public class CifrasIE {

	private double comision;
	private double retencion;
	private double iva;
	
	private double subTotal;
	private double total;
	
	
	
	public CifrasIE(){
		this.comision = 0.0d;  
		this.retencion = 0.0d;
		this.iva = 0.0d;
		
		this.subTotal = 0.0d;
		this.total = 0.0d;
		
		
	}

	public double getComision() {
		return comision;
	}

	public void setComision(double comision) {
		this.comision = comision;
	}

	public double getRetencion() {
		return retencion;
	}

	public void setRetencion(double retencion) {
		this.retencion = retencion;
	}

	public double getIva() {
		return iva;
	}

	public void setIva(double iva) {
		this.iva = iva;
	}

	public double getSubTotal() {
		return subTotal;
	}

	public void setSubTotal(double subTotal) {
		this.subTotal = subTotal;
	}

	public double getTotal() {
		return total;
	}

	public void setTotal(double total) {
		this.total = total;
	}

	
	

}
