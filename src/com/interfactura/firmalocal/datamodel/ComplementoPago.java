package com.interfactura.firmalocal.datamodel;

import java.math.BigDecimal;
import java.util.Date;

public class ComplementoPago {
	/* Seccion informacion del pago */
	private Date fechaPago;
	private String formaPagoP;
	private String monedaPago;
	private BigDecimal tipoCambioPago;
	private BigDecimal monto;
	private String numeroOperacion;
	private String rfcEmisorCuentaOrden;
	private String nombreBancoOrdinarioExt;
	private String cuentaOrdenante;
	private String rfcEmisorCtaBeneficiario;
	private String cuentaBeneficiario;
	private String tipoCadenaPago;
	private String cadenaPago;
	private String certificadoPago;
	private String selloPago;
	/* Seccion doctoRelacionado */
	private String idDocumento;
	private String seriePago;
	private String folioPago;
	private String monedaDR;
	private BigDecimal tipoCambioDR;
	private String metodoPagoDR;
	private String numParcialidad;
	private BigDecimal impSaldoAnterior;
	private BigDecimal impuestoPagado;
	private BigDecimal impSaldoInsoluto;
	/* Seccion retencion */
	private String impuestoRetencion;
	private BigDecimal importeRetencion;
	/* Seccion traslados */
	private String impuestoTraslados;
	private String tipoFactor;
	private BigDecimal tasaCuota;
	private BigDecimal importeTraslado;

	public String getFormaPagoP() {
		return formaPagoP;
	}

	public void setFormaPagoP(String formaPagoP) {
		this.formaPagoP = formaPagoP;
	}

	public String getMonedaPago() {
		return monedaPago;
	}

	public void setMonedaPago(String monedaPago) {
		this.monedaPago = monedaPago;
	}

	public BigDecimal getTipoCambioPago() {
		return tipoCambioPago;
	}

	public void setTipoCambioPago(BigDecimal tipoCambioPago) {
		this.tipoCambioPago = tipoCambioPago;
	}

	public BigDecimal getMonto() {
		return monto;
	}

	public void setMonto(BigDecimal monto) {
		this.monto = monto;
	}

	public String getNumeroOperacion() {
		return numeroOperacion;
	}

	public void setNumeroOperacion(String numeroOperacion) {
		this.numeroOperacion = numeroOperacion;
	}

	public String getRfcEmisorCuentaOrden() {
		return rfcEmisorCuentaOrden;
	}

	public void setRfcEmisorCuentaOrden(String rfcEmisorCuentaOrden) {
		this.rfcEmisorCuentaOrden = rfcEmisorCuentaOrden;
	}

	public String getNombreBancoOrdinarioExt() {
		return nombreBancoOrdinarioExt;
	}

	public void setNombreBancoOrdinarioExt(String nombreBancoOrdinarioExt) {
		this.nombreBancoOrdinarioExt = nombreBancoOrdinarioExt;
	}

	public String getCuentaOrdenante() {
		return cuentaOrdenante;
	}

	public void setCuentaOrdenante(String cuentaOrdenante) {
		this.cuentaOrdenante = cuentaOrdenante;
	}

	public String getRfcEmisorCtaBeneficiario() {
		return rfcEmisorCtaBeneficiario;
	}

	public void setRfcEmisorCtaBeneficiario(String rfcEmisorCtaBeneficiario) {
		this.rfcEmisorCtaBeneficiario = rfcEmisorCtaBeneficiario;
	}

	public String getCuentaBeneficiario() {
		return cuentaBeneficiario;
	}

	public void setCuentaBeneficiario(String cuentaBeneficiario) {
		this.cuentaBeneficiario = cuentaBeneficiario;
	}

	public String getTipoCadenaPago() {
		return tipoCadenaPago;
	}

	public void setTipoCadenaPago(String tipoCadenaPago) {
		this.tipoCadenaPago = tipoCadenaPago;
	}

	public String getCadenaPago() {
		return cadenaPago;
	}

	public void setCadenaPago(String cadenaPago) {
		this.cadenaPago = cadenaPago;
	}

	public String getIdDocumento() {
		return idDocumento;
	}

	public void setIdDocumento(String idDocumento) {
		this.idDocumento = idDocumento;
	}

	public String getSeriePago() {
		return seriePago;
	}

	public void setSeriePago(String seriePago) {
		this.seriePago = seriePago;
	}

	public String getFolioPago() {
		return folioPago;
	}

	public void setFolioPago(String folioPago) {
		this.folioPago = folioPago;
	}

	public String getMonedaDR() {
		return monedaDR;
	}

	public void setMonedaDR(String monedaDR) {
		this.monedaDR = monedaDR;
	}

	public BigDecimal getTipoCambioDR() {
		return tipoCambioDR;
	}

	public void setTipoCambioDR(BigDecimal tipoCambioDR) {
		this.tipoCambioDR = tipoCambioDR;
	}

	public String getMetodoPagoDR() {
		return metodoPagoDR;
	}

	public void setMetodoPagoDR(String metodoPagoDR) {
		this.metodoPagoDR = metodoPagoDR;
	}

	public String getNumParcialidad() {
		return numParcialidad;
	}

	public void setNumParcialidad(String numParcialidad) {
		this.numParcialidad = numParcialidad;
	}

	public BigDecimal getImpSaldoAnterior() {
		return impSaldoAnterior;
	}

	public void setImpSaldoAnterior(BigDecimal impSaldoAnterior) {
		this.impSaldoAnterior = impSaldoAnterior;
	}

	public BigDecimal getImpuestoPagado() {
		return impuestoPagado;
	}

	public void setImpuestoPagado(BigDecimal impuestoPagado) {
		this.impuestoPagado = impuestoPagado;
	}

	public BigDecimal getImpSaldoInsoluto() {
		return impSaldoInsoluto;
	}

	public void setImpSaldoInsoluto(BigDecimal impSaldoInsoluto) {
		this.impSaldoInsoluto = impSaldoInsoluto;
	}

	public String getImpuestoRetencion() {
		return impuestoRetencion;
	}

	public void setImpuestoRetencion(String impuestoRetencion) {
		this.impuestoRetencion = impuestoRetencion;
	}

	public BigDecimal getImporteRetencion() {
		return importeRetencion;
	}

	public void setImporteRetencion(BigDecimal importeRetencion) {
		this.importeRetencion = importeRetencion;
	}

	public String getImpuestoTraslados() {
		return impuestoTraslados;
	}

	public void setImpuestoTraslados(String impuestoTraslados) {
		this.impuestoTraslados = impuestoTraslados;
	}

	public String getTipoFactor() {
		return tipoFactor;
	}

	public void setTipoFactor(String tipoFactor) {
		this.tipoFactor = tipoFactor;
	}

	public BigDecimal getTasaCuota() {
		return tasaCuota;
	}

	public void setTasaCuota(BigDecimal tasaCuota) {
		this.tasaCuota = tasaCuota;
	}

	public BigDecimal getImporteTraslado() {
		return importeTraslado;
	}

	public void setImporteTraslado(BigDecimal importeTraslado) {
		this.importeTraslado = importeTraslado;
	}

	public Date getFechaPago() {
		return fechaPago;
	}

	public void setFechaPago(Date fechaPago) {
		this.fechaPago = fechaPago;
	}

	public String getCertificadoPago() {
		return certificadoPago;
	}

	public void setCertificadoPago(String certificadoPago) {
		this.certificadoPago = certificadoPago;
	}

	public String getSelloPago() {
		return selloPago;
	}

	public void setSelloPago(String selloPago) {
		this.selloPago = selloPago;
	}

}
