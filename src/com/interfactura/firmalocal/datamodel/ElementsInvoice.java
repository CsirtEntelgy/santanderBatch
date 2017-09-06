package com.interfactura.firmalocal.datamodel;

import java.util.List;

public class ElementsInvoice {

	private double quantity;
	private String unitMeasure;
	private String description;
	private double unitPrice;
	private double amount;
	private List<CustomsInformation> informacionAduanera;
	private List<FarmAccount> cuentaPredial;
	private List<Part> partes;
	private String concept;
	private String claveProdServ;
	private String claveUnidad;

	public double getQuantity() {
		return quantity;
	}

	public void setQuantity(double quantity) {
		this.quantity = quantity;
	}

	public String getUnitMeasure() {
		return unitMeasure;
	}

	public void setUnitMeasure(String unitMeasure) {
		this.unitMeasure = unitMeasure;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public double getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(double unitPrice) {
		this.unitPrice = unitPrice;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public List<CustomsInformation> getInformacionAduanera() {
		return informacionAduanera;
	}

	public void setInformacionAduanera(List<CustomsInformation> informacionAduanera) {
		this.informacionAduanera = informacionAduanera;
	}

	public List<FarmAccount> getCuentaPredial() {
		return cuentaPredial;
	}

	public void setCuentaPredial(List<FarmAccount> cuentaPredial) {
		this.cuentaPredial = cuentaPredial;
	}

	public List<Part> getPartes() {
		return partes;
	}

	public void setPartes(List<Part> partes) {
		this.partes = partes;
	}

	public String getConcept() {
		return concept;
	}

	public void setConcept(String concept) {
		this.concept = concept;
	}

	public String getClaveProdServ() {
		return claveProdServ;
	}

	public void setClaveProdServ(String claveProdServ) {
		this.claveProdServ = claveProdServ;
	}

	public String getClaveUnidad() {
		return claveUnidad;
	}

	public void setClaveUnidad(String claveUnidad) {
		this.claveUnidad = claveUnidad;
	}
	
}
