package com.interfactura.firmalocal.datamodel;

import java.util.ArrayList;
import java.util.List;

import org.springmodules.validation.bean.conf.loader.annotation.handler.Length;
import org.springmodules.validation.bean.conf.loader.annotation.handler.NotBlank;

import com.interfactura.firmalocal.xml.util.Util;

public class Invoice {
	private String reference;
	@NotBlank
	@Length(min = 1)
	private String date;
	@NotBlank
	@Length(min = 1)
	private String codigoPostal;
	private String lugarExpedicion;
	@NotBlank
	@Length(min = 1)
	private String name;
	private String address;
	@NotBlank
	@Length(min = 1)
	private String rfc;
	private String rfcL;
	private String shipped;
	private List<ElementsInvoice> elements = new ArrayList<ElementsInvoice>();
	private String numberMotion;
	private String dateMotion;
	private String customs;
	@NotBlank
	@Length(min = 1)
	private String calle;
	@NotBlank
	@Length(min = 1)
	private String exterior;
	private String interior;
	private String municipio;
	@NotBlank
	@Length(min = 1)
	private String pais;
	@NotBlank
	@Length(min = 1)
	private String estado;
	private String referencia;
	private String localidad;
	@NotBlank
	@Length(min = 1)
	private String colonia;
	private String cadena;
	private String sello;
	private String noCertificado;
	private String noAprobacion;
	private String yearAprobacion;
	private String fechaHora;
	private int idFiscal;
	private double iva;
	private double porcentaje;
	private double subTotal;
	private double total;
	private double vat;
	private String quantityWriting;
	private String[] quantity;
	private String[] unitMeasure;
	private String[] tokens;
	private String[] description;
	private String[] unitPrice;
	private String[] amount;
	private double exchange;
	private String folio;
	private String serie;
	private String customerCode;
	private String period;
	private String contractNumber;
	private String costCenter;
	private String tipoMoneda;
	private String tipoFormato;
	private String descriptionConcept;
	private String descriptionIVA;
	private String providerNumber;
	private String purchaseOrder;
	private String beneficiaryName;
	private String email;
	private String accountNumber;
	private String receivingInstitution;

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		if(!Util.isNullEmpty(interior)){
			interior=" No. Interior: "+interior;
		} else {
			interior="";
		}
		return calle + " #" + exterior + interior + " Col." + colonia + " C.P."
				+ codigoPostal + " " + municipio + ", " + estado;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getRfc() {
		return rfc;
	}

	public void setRfc(String rfc) {
		this.rfc = rfc;
	}

	public String getRfcL() {
		return rfcL;
	}

	public void setRfcL(String rfcL) {
		this.rfcL = rfcL;
	}

	public String getShipped() {
		return shipped;
	}

	public void setShipped(String shipped) {
		this.shipped = shipped;
	}

	public List<ElementsInvoice> getElements() {
		return elements;
	}

	public void setElements(List<ElementsInvoice> elements) {
		this.elements = elements;
	}

	public String getNumberMotion() {
		return numberMotion;
	}

	public void setNumberMotion(String numberMotion) {
		this.numberMotion = numberMotion;
	}

	public String getDateMotion() {
		return dateMotion;
	}

	public void setDateMotion(String dateMotion) {
		this.dateMotion = dateMotion;
	}

	public String getCustoms() {
		return customs;
	}

	public void setCustoms(String customs) {
		this.customs = customs;
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

	public double getVat() {
		return vat;
	}

	public void setVat(double vat) {
		this.vat = vat;
	}

	public String getQuantityWriting() {
		return quantityWriting;
	}

	public void setQuantityWriting(String quantityWriting) {
		this.quantityWriting = quantityWriting;
	}

	public double getExchange() {
		return exchange;
	}

	public void setExchange(double exchange) {
		this.exchange = exchange;
	}

	public String getCalle() {
		return calle;
	}

	public void setCalle(String calle) {
		this.calle = calle;
	}

	public String getExterior() {
		return exterior;
	}

	public void setExterior(String exterior) {
		this.exterior = exterior;
	}

	public String getInterior() {
		return interior;
	}

	public void setInterior(String interior) {
		this.interior = interior;
	}

	public String getMunicipio() {
		return municipio;
	}

	public void setMunicipio(String municipio) {
		this.municipio = municipio;
	}

	public String getPais() {
		return pais;
	}

	public void setPais(String pais) {
		this.pais = pais;
	}

	public String getCodigoPostal() {
		return codigoPostal;
	}

	public void setCodigoPostal(String codigoPostal) {
		this.codigoPostal = codigoPostal;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public String getReferencia() {
		return referencia;
	}

	public void setReferencia(String referencia) {
		this.referencia = referencia;
	}

	public String getLocalidad() {
		return localidad;
	}

	public void setLocalidad(String localidad) {
		this.localidad = localidad;
	}

	public String getColonia() {
		return colonia;
	}

	public void setColonia(String colonia) {
		this.colonia = colonia;
	}

	public int getIdFiscal() {
		return idFiscal;
	}

	public void setIdFiscal(int idFiscal) {
		this.idFiscal = idFiscal;
	}

	public double getIva() {
		return iva;
	}

	public void setIva(double iva) {
		this.iva = iva;
	}

	public double getPorcentaje() {
		return porcentaje;
	}

	public void setPorcentaje(double porcentaje) {
		this.porcentaje = porcentaje;
	}

	public String getLugarExpedicion() {
		return lugarExpedicion;
	}

	public void setLugarExpedicion(String lugarExpedicion) {
		this.lugarExpedicion = lugarExpedicion;
	}

	public String getCadena() {
		return cadena;
	}

	public void setCadena(String cadena) {
		this.cadena = cadena;
	}

	public String getSello() {
		return sello;
	}

	public void setSello(String sello) {
		this.sello = sello;
	}

	public String getNoCertificado() {
		return noCertificado;
	}

	public void setNoCertificado(String noCertificado) {
		this.noCertificado = noCertificado;
	}

	public String getNoAprobacion() {
		return noAprobacion;
	}

	public void setNoAprobacion(String noAprobacion) {
		this.noAprobacion = noAprobacion;
	}

	public String getFechaHora() {
		return fechaHora;
	}

	public void setFechaHora(String fechaHora) {
		this.fechaHora = fechaHora;
	}

	public String[] getQuantity() {
		return quantity;
	}

	public void setQuantity(String[] quantity) {
		this.quantity = quantity;
	}

	public String[] getUnitMeasure() {
		return unitMeasure;
	}

	public void setUnitMeasure(String[] unitMeasure) {
		this.unitMeasure = unitMeasure;
	}

	public String[] getDescription() {
		return description;
	}

	public void setDescription(String[] description) {
		this.description = description;
	}

	public String[] getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(String[] unitPrice) {
		this.unitPrice = unitPrice;
	}

	public String[] getAmount() {
		return amount;
	}

	public void setAmount(String[] amount) {
		this.amount = amount;
	}

	public String getFolio() {
		return folio;
	}

	public void setFolio(String folio) {
		this.folio = folio;
	}

	public String getSerie() {
		return serie;
	}

	public void setSerie(String serie) {
		this.serie = serie;
	}

	public String getCustomerCode() {
		return customerCode;
	}

	public void setCustomerCode(String customerCode) {
		this.customerCode = customerCode;
	}

	public String getPeriod() {
		return period;
	}

	public void setPeriod(String period) {
		this.period = period;
	}

	public String getContractNumber() {
		return contractNumber;
	}

	public void setContractNumber(String contractNumber) {
		this.contractNumber = contractNumber;
	}

	public String getCostCenter() {
		return costCenter;
	}

	public void setCostCenter(String costCenter) {
		this.costCenter = costCenter;
	}

	public String[] getTokens() {
		return tokens;
	}

	public void setTokens(String[] tokens) {
		this.tokens = tokens;
	}

	public String getTipoMoneda() {
		return tipoMoneda;
	}

	public void setTipoMoneda(String tipoMoneda) {
		this.tipoMoneda = tipoMoneda;
	}

	public String getYearAprobacion() {
		return yearAprobacion;
	}

	public void setYearAprobacion(String yearAprobacion) {
		this.yearAprobacion = yearAprobacion;
	}

	public String getTipoFormato() {
		return tipoFormato;
	}

	public void setTipoFormato(String tipoFormato) {
		this.tipoFormato = tipoFormato;
	}

	public String getDescriptionConcept() {
		return descriptionConcept;
	}

	public void setDescriptionConcept(String descriptionConcept) {
		this.descriptionConcept = descriptionConcept;
	}

	public String getProviderNumber() {
		return providerNumber;
	}

	public void setProviderNumber(String providerNumber) {
		this.providerNumber = providerNumber;
	}

	public String getPurchaseOrder() {
		return purchaseOrder;
	}

	public void setPurchaseOrder(String purchaseOrder) {
		this.purchaseOrder = purchaseOrder;
	}

	public String getBeneficiaryName() {
		return beneficiaryName;
	}

	public void setBeneficiaryName(String beneficiaryName) {
		this.beneficiaryName = beneficiaryName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getReceivingInstitution() {
		return receivingInstitution;
	}

	public void setReceivingInstitution(String receivingInstitution) {
		this.receivingInstitution = receivingInstitution;
	}

	public String getDescriptionIVA() {
		return descriptionIVA;
	}

	public void setDescriptionIVA(String descriptionIVA) {
		this.descriptionIVA = descriptionIVA;
	}
}
