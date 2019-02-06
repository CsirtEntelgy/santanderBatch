package com.interfactura.firmalocal.xml.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.interfactura.firmalocal.datamodel.CfdiComprobanteFiscal;
import com.interfactura.firmalocal.datamodel.CfdiConcepto;
import com.interfactura.firmalocal.datamodel.CfdiConceptoImpuestoTipo;
import com.interfactura.firmalocal.datamodel.ComplementoPago;
import com.interfactura.firmalocal.xml.Properties;
import com.interfactura.firmalocal.xml.util.ValidationConstants.TipoEmision;


@Component
public class ConvertirCfdiDivisas {

	
	@Autowired
	Properties properties;
	
	boolean fTipoAddenda;
	
	public String startXml(){
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	}
	public String closeFComprobante(){
		return "\n</cfdi:Comprobante> ";
	}
	public String fComprobante(CfdiComprobanteFiscal comp , Date date){
		StringBuilder concat =  new StringBuilder();
		
		//Certificado
		concat.append("Certificado=\"" + properties.getLblCERTIFICADO() + "\" ");
		if (comp.getDescuento() != null && comp.getDescuento().doubleValue() > 0){
			concat.append("Descuento=\"" + comp.getDescuento() + "\" ");
		}
		concat.append("Fecha=\"" + Util.convertirFecha(date) + "\" ");
		concat.append(Util.isNullEmpity(comp.getFolio(), "Folio"));
		if(comp.getFormaPago() != null && !comp.getFormaPago().isEmpty()){
			concat.append("FormaPago=\"" + comp.getFormaPago() + "\" ");
		}
		if(comp.getEmisor().getDomicilio().getCodigoPostal() != null){
			concat.append("LugarExpedicion=\"" + comp.getEmisor().getDomicilio().getCodigoPostal() + "\" ");
		}
		if(comp.getMetodoPago() != null && comp.getMetodoPago().trim() != ""){
			concat.append("MetodoPago=\"" + comp.getMetodoPago() + "\" ");
		}
		concat.append("Moneda=\"" + comp.getMoneda() + "\" ");
		
		//NoCertificado
		concat.append("NoCertificado=\"" + properties.getLblNO_CERTIFICADO() + "\" ");
		//Sello
		concat.append("Sello=\"" + properties.getLabelSELLO() + "\" ");
		if(comp.getSerie() != null && !comp.getSerie().isEmpty()){
			concat.append("Serie=\"" + comp.getSerie() + "\" ");
		}
		String subtotal = "0";
		if(!comp.getMoneda().equalsIgnoreCase("XXX")){
			subtotal = UtilCatalogos.decimales(comp.getSubTotal().toString(), comp.getDecimalesMoneda());
		}
		concat.append("SubTotal=\"" + subtotal + "\" ");
		if(comp.getTipoCambio() != null && comp.getTipoCambio().trim() != ""){
			if (comp.getMoneda().equalsIgnoreCase("MXN"))
				concat.append("TipoCambio=\"" + "1" + "\" ");
			else
				concat.append("TipoCambio=\"" + comp.getTipoCambio() + "\" ");
		}
		concat.append("TipoDeComprobante=\"" + comp.getTipoDeComprobante() + "\" ");
		String total = "0";
		if(!comp.getMoneda().equalsIgnoreCase("XXX")){
			total = UtilCatalogos.decimales(comp.getTotal().toString(), comp.getDecimalesMoneda());
		}
		concat.append("Total=\"" + total + "\" ");
		concat.append("Version=\"" + "3.3" + "\" ");
		
		StringBuilder concatSchemaLocation = new StringBuilder();
		concatSchemaLocation.append("xsi:schemaLocation=\"");
		concatSchemaLocation.append("http://www.sat.gob.mx/cfd/3 ");
		concatSchemaLocation.append("http://www.sat.gob.mx/sitio_internet/cfd/3/cfdv33.xsd ");
		
		concat.append("xmlns:cfdi=\"http://www.sat.gob.mx/cfd/3\" ");
		if(comp.getTipoEmision().equalsIgnoreCase(TipoEmision.DIVISAS)){
			concat.append("xmlns:divisas=\"http://www.sat.gob.mx/divisas\" ");
			concatSchemaLocation.append("http://www.sat.gob.mx/divisas ");
			concatSchemaLocation.append("http://www.sat.gob.mx/sitio_internet/cfd/divisas/Divisas.xsd ");
		}
		if(comp.getTipoEmision().equalsIgnoreCase(TipoEmision.DONATARIAS)){
			concat.append("xmlns:donat=\"http://www.sat.gob.mx/donat\" ");
			concatSchemaLocation.append("http://www.sat.gob.mx/donat ");
			concatSchemaLocation.append("http://www.sat.gob.mx/sitio_internet/cfd/donat/donat11.xsd ");
		}
		concat.append("xmlns:ecb=\"http://www.sat.gob.mx/ecb\" ");
		concat.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
		concatSchemaLocation = new StringBuilder(concatSchemaLocation.toString().trim()).append("\" ");
		return Util
				.conctatArguments(
						"\n<cfdi:Comprobante ",
						concat.toString(),
						concatSchemaLocation.toString(), 
						">")
				.toString();
	}
	
	public String emisor(CfdiComprobanteFiscal comp){
		StringBuilder concat =  new StringBuilder();
		
		String valNombre = comp.getEmisor().getNombre().trim().toUpperCase().replaceAll("\\.", "");
		valNombre = valNombre.replaceAll("\\(", "");
		valNombre = valNombre.replaceAll("\\)", "");
		valNombre = valNombre.replace("/", "");
		concat.append("Nombre=\""+valNombre.trim()+"\" ");
		concat.append("RegimenFiscal=\""+comp.getEmisor().getRegimenFiscal().trim()+"\" ");
		concat.append("Rfc=\""+comp.getEmisor().getRfc().trim()+"\" ");
		
		return Util
				.conctatArguments("\n<cfdi:Emisor ",
						concat.toString(),
						"/>").toString();
	}
	
	public String receptor(CfdiComprobanteFiscal comp){
		StringBuilder concat =  new StringBuilder();
		
		String valNombre = comp.getReceptor().getNombre().trim().toUpperCase().replaceAll("\\.", "");
		valNombre = valNombre.replaceAll("\\(", "");
		valNombre = valNombre.replaceAll("\\)", "");
		valNombre = valNombre.replace("/", "");
		concat.append("Nombre=\""+ Util.convierte(valNombre).toUpperCase() +"\" ");
		if(comp.getReceptor().getNumRegIdTrib() != null && comp.getReceptor().getNumRegIdTrib() != ""){
			concat.append("NumRegIdTrib=\"" + comp.getReceptor().getNumRegIdTrib() + "\" ");
		}
		concat.append("Rfc=\""+ comp.getReceptor().getRfc() +"\" ");
		
		if(comp.getReceptor().getResidenciaFiscal() != null && comp.getReceptor().getResidenciaFiscal() != ""){
			concat.append("ResidenciaFiscal=\""+ comp.getReceptor().getResidenciaFiscal() +"\" ");
		}
		concat.append("UsoCFDI=\""+ comp.getReceptor().getUsoCFDI() +"\" ");
		
		return Util.conctatArguments(
				"\n<cfdi:Receptor ",
				concat.toString(),
				"/>").toString();
	}
	
	public String startConcepto(CfdiComprobanteFiscal comp){
		StringBuilder concatConceptos =  new StringBuilder();		
		concatConceptos.append(conceptos(comp));		
		return Util
				.conctatArguments(
						"\n<cfdi:Conceptos>",
						concatConceptos.toString(),
						"\n</cfdi:Conceptos>")
				.toString();
	}
	
	public String startConceptoFU(CfdiComprobanteFiscal comp){
		StringBuilder concatConceptos =  new StringBuilder();		
		concatConceptos.append(conceptosFU(comp));		
		return Util
				.conctatArguments(
						"\n<cfdi:Conceptos>",
						concatConceptos.toString(),
						"\n</cfdi:Conceptos>")
				.toString();
	}
	
	
	public String conceptos(CfdiComprobanteFiscal comp){
		StringBuilder sbConceptos = new StringBuilder();
		
		if(comp.getConceptos() != null && comp.getConceptos().size() > 0){
			for(CfdiConcepto concepto : comp.getConceptos()){
				
				sbConceptos.append("\n<cfdi:Concepto ");
				sbConceptos.append("Cantidad=\"" + concepto.getCantidad() + "\" ");
				sbConceptos.append("ClaveProdServ=\"" + concepto.getClaveProdServ() + "\" ");
				sbConceptos.append("ClaveUnidad=\"" + concepto.getClaveUnidad() + "\" ");
				sbConceptos.append("Descripcion=\"" + concepto.getDescripcion() + "\" ");
				if(comp.getDescuento() != null && comp.getDescuento().doubleValue() > 0){
					sbConceptos.append(" Descuento=\"" + comp.getDescuento()+ "\" ");
				}
				String importe = "0";
				if(!comp.getMoneda().equalsIgnoreCase("XXX")){
					importe = UtilCatalogos.decimales(concepto.getImporte().toString(), comp.getDecimalesMoneda());
				}
				sbConceptos.append("Importe=\"" + importe + "\" ");
				if(concepto.getUnidad() != null){
					sbConceptos.append("Unidad=\"" + concepto.getUnidad().toUpperCase() + "\" ");
				}
				
				importe = "0";
				if(!comp.getMoneda().equalsIgnoreCase("XXX")){
					DecimalFormat df = new DecimalFormat("0.00000000");
					String valUnitario = df.format(concepto.getValorUnitario());
					importe = UtilCatalogos.decimales(valUnitario, comp.getDecimalesMoneda());
				}
				sbConceptos.append("ValorUnitario=\"" + importe + "\"");
				sbConceptos.append(">");
				sbConceptos.append(conceptoImpuesto(concepto));
				sbConceptos.append("\n</cfdi:Concepto>");
			}
		}
		return sbConceptos.toString();
	}
	
	public String conceptosFU(CfdiComprobanteFiscal comp){
		StringBuilder sbConceptos = new StringBuilder();
		
		if(comp.getConceptos() != null && comp.getConceptos().size() > 0){
			for(CfdiConcepto concepto : comp.getConceptos()){
				
				sbConceptos.append("\n<cfdi:Concepto ");
				sbConceptos.append("Cantidad=\"" + concepto.getCantidad() + "\" ");
				sbConceptos.append("ClaveProdServ=\"" + concepto.getClaveProdServ() + "\" ");
				sbConceptos.append("ClaveUnidad=\"" + concepto.getClaveUnidad() + "\" ");
				sbConceptos.append("Descripcion=\"" + concepto.getDescripcion() + "\" ");
				if(comp.getDescuento() != null && comp.getDescuento().doubleValue() > 0){
					sbConceptos.append(" Descuento=\"" + comp.getDescuento()+ "\" ");
				}
				String importe = "0";
				if(!comp.getMoneda().equalsIgnoreCase("XXX")){
					importe = UtilCatalogos.decimales(concepto.getImporte().toString(), comp.getDecimalesMoneda());
				}
				sbConceptos.append("Importe=\"" + importe + "\" ");
				if(concepto.getUnidad() != null){
					sbConceptos.append("Unidad=\"" + concepto.getUnidad().toUpperCase() + "\" ");
				}
				
				importe = "0";
				if(!comp.getMoneda().equalsIgnoreCase("XXX")){
					DecimalFormat df = new DecimalFormat("0.00000000");
					String valUnitario = df.format(concepto.getValorUnitario());
					System.out.println("convierteXD: " + concepto.getValorUnitario());
					importe = concepto.getValorUnitario().toString();
				}
				sbConceptos.append("ValorUnitario=\"" + importe + "\"");
				
				if ( concepto.getAplicaIva() != null && !concepto.getAplicaIva().trim().equals("") && !concepto.getAplicaIva().trim().equals("0") ) {
					sbConceptos.append(">");
					sbConceptos.append(conceptoImpuesto(concepto));
					sbConceptos.append("\n</cfdi:Concepto>");
				} else {
					sbConceptos.append("/>");
				}
				
			}
		}
		return sbConceptos.toString();
	}
	
	public String conceptoImpuesto(CfdiConcepto concepto){
		StringBuilder sbConceptoImpuesto = new StringBuilder();
		if(concepto.getImpuestos() != null){
			if((concepto.getImpuestos().getTraslados() != null && concepto.getImpuestos().getTraslados().size() > 0)
					|| (concepto.getImpuestos().getRetenciones() != null && concepto.getImpuestos().getRetenciones().size() > 0)){
				
				sbConceptoImpuesto.append("\n<cfdi:Impuestos>");
				
				if(concepto.getImpuestos().getTraslados() != null && 
						concepto.getImpuestos().getTraslados().size() > 0){
					
					sbConceptoImpuesto.append("\n<cfdi:Traslados>");
					for(CfdiConceptoImpuestoTipo impuestoTipo : concepto.getImpuestos().getTraslados()){
						sbConceptoImpuesto.append("\n<cfdi:Traslado ");
						sbConceptoImpuesto.append("Base=\"" + impuestoTipo.getBase() + "\" ");
						if(!impuestoTipo.getTipoFactor().equalsIgnoreCase("exento")){
							sbConceptoImpuesto.append("Importe=\"" + impuestoTipo.getImporte() + "\" ");
						}
						sbConceptoImpuesto.append("Impuesto=\"" + impuestoTipo.getImpuesto() + "\" ");
						if(!impuestoTipo.getTipoFactor().equalsIgnoreCase("exento")){
							sbConceptoImpuesto.append("TasaOCuota=\"" + impuestoTipo.getTasaOCuota() + "\" ");
						}
						sbConceptoImpuesto.append("TipoFactor=\"" + impuestoTipo.getTipoFactor() + "\" ");
						sbConceptoImpuesto.append("/>");
					}
					sbConceptoImpuesto.append("\n</cfdi:Traslados>");
					
				}
				if(concepto.getImpuestos().getRetenciones() != null 
						&& concepto.getImpuestos().getRetenciones().size() > 0){
					
					sbConceptoImpuesto.append("\n<cfdi:Retenciones>");
					for(CfdiConceptoImpuestoTipo impuestoTipo : concepto.getImpuestos().getRetenciones()){
						sbConceptoImpuesto.append("\n<cfdi:Retencion ");
						sbConceptoImpuesto.append("Base=\"" + impuestoTipo.getBase() + "\" ");
						sbConceptoImpuesto.append("Importe=\"" + impuestoTipo.getImporte() + "\" ");
						sbConceptoImpuesto.append("Impuesto=\"" + impuestoTipo.getImpuesto() + "\" ");
						sbConceptoImpuesto.append("TasaOCuota=\"" + impuestoTipo.getTasaOCuota() + "\" ");
						sbConceptoImpuesto.append("TipoFactor=\"" + impuestoTipo.getTipoFactor() + "\" ");
						sbConceptoImpuesto.append("/>");
					}
					sbConceptoImpuesto.append("\n</cfdi:Retenciones>");
					
				}
					
				sbConceptoImpuesto.append("\n</cfdi:Impuestos>");
			}
		}
		return sbConceptoImpuesto.toString();
	}
	
	public String impuestos(CfdiComprobanteFiscal comp){
		StringBuilder concat = new StringBuilder();
		StringBuilder attributes = new StringBuilder();
		if(!comp.getTipoEmision().equalsIgnoreCase(TipoEmision.DONATARIAS)){
			if(comp.getImpuestos() != null){
				if(comp.getImpuestos().getTotalImpuestosRetenidos() != null){
					String valueRet ="0.00";
					if(comp.getImpuestos().getTotalImpuestosRetenidos().doubleValue() > 0){
						valueRet = comp.getImpuestos().getTotalImpuestosRetenidos().toString();
					}
					attributes.append( "TotalImpuestosRetenidos=\"" 
							+ UtilCatalogos.decimales(valueRet, comp.getDecimalesMoneda()) 
							+ "\" ");
				}
				if(comp.getImpuestos().getTotalImpuestosTrasladados() != null){
					String valueTra ="0.00";
					if(comp.getImpuestos().getTotalImpuestosTrasladados().doubleValue() > 0){
						valueTra = comp.getImpuestos().getTotalImpuestosTrasladados().toString();
					}
					attributes.append( "TotalImpuestosTrasladados=\"" 
							+ UtilCatalogos.decimales(valueTra, comp.getDecimalesMoneda()) 
							+ "\" ");
				}
			}
		}
		if(attributes.toString().trim().length() > 0){
			concat.append("\n<cfdi:Impuestos ");
			concat.append(attributes);
			concat.append("/>");
		}
		return concat.toString();
	}
	
	public String impuestosFU(CfdiComprobanteFiscal comp){
		StringBuilder concat = new StringBuilder();
		StringBuilder attributes = new StringBuilder();
		if(!comp.getTipoEmision().equalsIgnoreCase(TipoEmision.DONATARIAS)){
			if(comp.getImpuestos() != null){
				if(comp.getImpuestos().getTotalImpuestosRetenidos() != null){
					BigDecimal totalImpTra = comp.getImpuestos().getTotalImpuestosRetenidos();
			    	if (totalImpTra.compareTo(new BigDecimal("0")) != 0) {
			    		String valueRet ="0.00";
						if(comp.getImpuestos().getTotalImpuestosRetenidos().doubleValue() > 0){
							valueRet = comp.getImpuestos().getTotalImpuestosRetenidos().toString();
						}
						attributes.append( "TotalImpuestosRetenidos=\"" 
								+ UtilCatalogos.decimales(valueRet, comp.getDecimalesMoneda()) 
								+ "\" ");
			    	} 
					
				}
				if(comp.getImpuestos().getTotalImpuestosTrasladados() != null && !comp.isTotalExcento()){
					
					BigDecimal totalImpRet = comp.getImpuestos().getTotalImpuestosTrasladados();
					
			    	if (totalImpRet.compareTo(new BigDecimal("0")) != 0 || comp.isTasaCero() ) {
			    		String valueTra ="0.00";
						if(comp.getImpuestos().getTotalImpuestosTrasladados().doubleValue() > 0){
							valueTra = comp.getImpuestos().getTotalImpuestosTrasladados().toString();
						}
						attributes.append( "TotalImpuestosTrasladados=\"" 
								+ UtilCatalogos.decimales(valueTra, comp.getDecimalesMoneda()) 
								+ "\" ");
			    	}
					
				}
			}
		}
		if(attributes.toString().trim().length() > 0){
			concat.append("\n<cfdi:Impuestos ");
			concat.append(attributes);
			concat.append("/>");
		}
		return concat.toString();
	}
	
	
	public String complemento(CfdiComprobanteFiscal comp){
		StringBuilder concat = new StringBuilder();
		StringBuilder childs = new StringBuilder();
		
		childs.append(pagos(comp));
		if(comp.getTipoEmision().equalsIgnoreCase(TipoEmision.DIVISAS) 
				&& comp.getComplemento().getDivisaTipoOperacion() != null){
			childs.append("<divisas:Divisas ");
			childs.append("version=\"1.0\" ");
			childs.append("tipoOperacion=\""+comp.getComplemento().getDivisaTipoOperacion()+"\" ");
			childs.append("/>");
		}
		
		if(childs.toString().length() > 0){
			concat.append("<cfdi:Complemento>");
			concat.append(childs.toString());
			//concat.append(timbreFiscalDigital(comp));
			concat.append("</cfdi:Complemento>");
		}
		return concat.toString();
	}
	public String pagos(CfdiComprobanteFiscal comp){
		StringBuilder concat = new StringBuilder();
		StringBuilder pagos = new StringBuilder();
		if(comp.getComplementPagos() != null && comp.getComplementPagos().size() > 0){
			for(ComplementoPago pago : comp.getComplementPagos()){
				pagos.append(pago(pago));
			}
		}
		if(pagos.toString().trim().length() > 0){
			concat.append("\n<pago10:Pagos ");
			concat.append("xmlns:catCFDI=\""+"http://www.sat.gob.mx/sitio_internet/cfd/catalogos"+"\" ");
			concat.append("xmlns:catPagos=\""+"http://www.sat.gob.mx/sitio_internet/cfd/catalogos/Pagos"+"\" ");
			concat.append("xmlns:pago10=\""+"http://www.sat.gob.mx/Pagos"+"\" ");
			concat.append("xmlns:tdCFDI=\""+"http://www.sat.gob.mx/sitio_internet/cfd/tipoDatos/tdCFDI"+"\" ");
			concat.append("Version=\""+"1.0"+"\" ");
			concat.append(">");
			concat.append(pagos.toString());
			concat.append("</pago10:Pagos>");
		}
		return concat.toString();
	}
	
	public String pago(ComplementoPago pago){
		StringBuilder concat = new StringBuilder();
		StringBuilder attributes = new StringBuilder();
		StringBuilder doctoRelacionado = new StringBuilder();
		
		if(pago.getFechaPago() != null){
			attributes.append("FechaPago=\"" + Util.convertirFecha(pago.getFechaPago()) + "\" ");
		}
		attributes.append(Util.isNullEmpity(pago.getFormaPagoP(), "FormaDePagoP"));
		attributes.append(Util.isNullEmpity(pago.getMonedaPago(), "MonedaP"));
		if(pago.getMonto() != null){
			String monto = UtilCatalogos.decimales(pago.getMonto().doubleValue()+"", pago.getDecimalesMonedaPago());
			attributes.append(Util.isNullEmpity(monto, "Monto"));
		}
		
		attributes.append(Util.isNullEmpity(pago.getNumeroOperacion(), "NumOperacion"));
		
		if(pago.getTipoCambioPago() != null){
			String tipoCambioPago = UtilCatalogos.decimales(pago.getTipoCambioPago().toString(), pago.getDecimalesMonedaPago());
			attributes.append(Util.isNullEmpity(tipoCambioPago, "TipoCambioP"));
		}
		
		if (pago.getRfcEmisorCuentaOrden() != null && !pago.getRfcEmisorCuentaOrden().isEmpty()) {
			attributes.append(Util.isNullEmpity(pago.getRfcEmisorCuentaOrden() , "RfcEmisorCtaOrd"));
		}
		if (pago.getNombreBancoOrdinarioExt() != null && !pago.getNombreBancoOrdinarioExt().isEmpty()) {
			attributes.append(Util.isNullEmpity(pago.getNombreBancoOrdinarioExt() , "NomBancoOrdExt"));
		}
		if (pago.getCuentaOrdenante() != null && !pago.getCuentaOrdenante().isEmpty()) {
			attributes.append(Util.isNullEmpity(pago.getCuentaOrdenante(), "CtaOrdenante"));
		}
		if (pago.getRfcEmisorCtaBeneficiario() != null && !pago.getRfcEmisorCtaBeneficiario().isEmpty()) {
			attributes.append(Util.isNullEmpity(pago.getRfcEmisorCtaBeneficiario(), "RfcEmisorCtaBen"));
		}
		if (pago.getCuentaBeneficiario() != null && !pago.getCuentaBeneficiario().isEmpty()) {
			attributes.append(Util.isNullEmpity(pago.getCuentaBeneficiario(), "CtaBeneficiario"));
		}
		if (pago.getTipoCadenaPago() != null && !pago.getTipoCadenaPago().isEmpty()) {
			attributes.append(Util.isNullEmpity(pago.getTipoCadenaPago(), "TipoCadPago"));
		}
		if (pago.getCertificadoPago() != null && !pago.getCertificadoPago().isEmpty()) {
			attributes.append(Util.isNullEmpity(pago.getCertificadoPago(), "CertPago"));
		}
		if (pago.getCadenaPago() != null && !pago.getCadenaPago().isEmpty()) {
			attributes.append(Util.isNullEmpity(pago.getCadenaPago(), "CadPago"));
		}
		if (pago.getSelloPago() != null && !pago.getSelloPago().isEmpty()) {
			attributes.append(Util.isNullEmpity(pago.getSelloPago(), "SelloPago"));
		}
		
		
		doctoRelacionado.append(doctoRelacionado(pago));
		
		if(attributes.toString().trim().length() > 0){
			concat.append("\n<pago10:Pago ");
			concat.append(attributes.toString());
			concat.append(">");
			concat.append(doctoRelacionado.toString());
			concat.append("\n</pago10:Pago>");
		}
		return concat.toString();
	}
	
	public String doctoRelacionado(ComplementoPago pago){
		StringBuilder concat = new StringBuilder();
		StringBuilder attributes = new StringBuilder();
		
		attributes.append(Util.isNullEmpity(pago.getIdDocumento(), "IdDocumento"));
		
		if (pago.getSeriePago() != null && !pago.getSeriePago().isEmpty()) {
			attributes.append(Util.isNullEmpity(pago.getSeriePago(), "Serie"));
		}
		if (pago.getFolioPago() != null &&  !pago.getFolioPago().isEmpty()) {
			attributes.append(Util.isNullEmpity(pago.getFolioPago(), "Folio"));
		}
		
		if(pago.getImpuestoPagado() != null){
			String impPagado = UtilCatalogos.decimales(pago.getImpuestoPagado().toString(), pago.getDecimalesMonedaDr());
			attributes.append(Util.isNullEmpity(impPagado, "ImpPagado"));
		}
		if(pago.getImpSaldoAnterior() != null){
			String impSaldoAnt = UtilCatalogos.decimales(pago.getImpSaldoAnterior().toString(), pago.getDecimalesMonedaDr());
			attributes.append(Util.isNullEmpity(impSaldoAnt, "ImpSaldoAnt"));
		}
		if(pago.getImpSaldoInsoluto() != null){
			String impSaldoInsoluto = UtilCatalogos.decimales(pago.getImpSaldoInsoluto().toString(), pago.getDecimalesMonedaDr());
			attributes.append(Util.isNullEmpity(impSaldoInsoluto, "ImpSaldoInsoluto"));
		}
		
		attributes.append(Util.isNullEmpity(pago.getMetodoPagoDR(), "MetodoDePagoDR"));
		attributes.append(Util.isNullEmpity(pago.getMonedaDR(), "MonedaDR"));
		attributes.append(Util.isNullEmpity(pago.getNumParcialidad(), "NumParcialidad"));
		
		if(pago.getTipoCambioDR() != null){
			String tipoCambioDr = UtilCatalogos.decimales(pago.getTipoCambioDR().toString(), pago.getDecimalesMonedaDr());
			attributes.append(Util.isNullEmpity(tipoCambioDr, "TipoCambioDR"));
		}
		
		if(attributes.toString().trim().length() > 0){
			concat.append("\n<pago10:DoctoRelacionado ");
			concat.append(attributes.toString());
			concat.append("/>");
		}
		return concat.toString();
	}
	
	public String timbreFiscalDigital(CfdiComprobanteFiscal comp){
		StringBuilder concat = new StringBuilder();
		
		concat.append("\n<tfd:TimbreFiscalDigital ");
		concat.append("xmlns:tfd=\"http://www.sat.gob.mx/TimbreFiscalDigital\" ");
		if (comp.getComplemento() != null && comp.getComplemento().getTimbreFiscalDigital() != null){
			comp.getComplemento().getTimbreFiscalDigital().setVersion("1.1");
			
			concat.append(Util.isNullEmpity(comp.getComplemento().getTimbreFiscalDigital().getNoCertificadoSAT(),
					"NoCertificadoSAT"));
			
			concat.append(Util.isNullEmpity(comp.getComplemento().getTimbreFiscalDigital().getSelloCFD(),
					"SelloCFD"));
			
			concat.append(Util.isNullEmpity(comp.getComplemento().getTimbreFiscalDigital().getUuid(),
					"UUID"));
			
			concat.append(Util.isNullEmpity(comp.getComplemento().getTimbreFiscalDigital().getVersion(),
					"Version"));
		}
		concat.append("xsi:schemaLocation=\""+"http://www.sat.gob.mx/TimbreFiscalDigital http://www.sat.gob.mx/sitio_internet/cfd/TimbreFiscalDigital/TimbreFiscalDigitalv11.xsd"+"\" ");
		concat.append("/>");
		
		return concat.toString();
	}
	
	public String addendaNewDivisas(CfdiComprobanteFiscal comp){
		
		fTipoAddenda = false;
		if (!Util.isNullEmpty(comp.getTipoAddendaCellValue())) {
			if (!comp.getTipoAddendaCellValue().equals("0")) {
				fTipoAddenda = true;
			}
		}
		
		StringBuilder concat = new StringBuilder();
		if(comp.getAddenda() != null){
			concat.append("\n<cfdi:Addenda ");
			concat.append("xmlns:as=\"http://www.santander.com.mx/schemas/xsd/AddendaSantanderV1\" ");
			if(fTipoAddenda){
				concat.append("xmlns:asant=\"http://www.santander.com.mx/schemas/xsd/AddendaSantanderV1\" ");
			}
			
			concat.append(">");
			if(fTipoAddenda){
				concat.append(addendaAsant(comp));
			}
			concat.append("\n<as:AddendaSantanderV1>");
			//concat.append("xmlns:as=\"http://www.santander.com.mx/schemas/xsd/AddendaSantanderV1\">");
			concat.append(informacionPagoDivisas(comp));
			concat.append(informacionEmision(comp));
			concat.append(camposAdicionalesDivisas(comp));
			concat.append(domicilioReceptorDivisas(comp));
			concat.append(domicilioEmisor(comp));
			concat.append("\n</as:AddendaSantanderV1>");
			concat.append("\n</cfdi:Addenda>");
		}
		
		return concat.toString();
	}
	
	public String addenda(CfdiComprobanteFiscal comp){
		
		fTipoAddenda = false;
		if (!Util.isNullEmpty(comp.getTipoAddendaCellValue())) {
			if (!comp.getTipoAddendaCellValue().equals("0")) {
				fTipoAddenda = true;
			}
		}
		
		StringBuilder concat = new StringBuilder();
		if(comp.getAddenda() != null){
			concat.append("\n<cfdi:Addenda ");
			concat.append("xmlns:as=\"http://www.santander.com.mx/schemas/xsd/AddendaSantanderV1\" ");
			if(fTipoAddenda){
				concat.append("xmlns:asant=\"http://www.santander.com.mx/schemas/xsd/AddendaSantanderV1\" ");
			}
			
			concat.append(">");
			if(fTipoAddenda){
				concat.append(addendaAsant(comp));
			}
			concat.append("\n<as:AddendaSantanderV1>");
			//concat.append("xmlns:as=\"http://www.santander.com.mx/schemas/xsd/AddendaSantanderV1\">");
			concat.append(informacionPago(comp));
			concat.append(informacionEmision(comp));
			concat.append(camposAdicionales(comp));
			concat.append(domicilioReceptor(comp));
			concat.append(domicilioEmisor(comp));
			concat.append("\n</as:AddendaSantanderV1>");
			concat.append("\n</cfdi:Addenda>");
		}
		
		return concat.toString();
	}
	
	
	public String addendaAsant(CfdiComprobanteFiscal comp){
		StringBuilder concat = new StringBuilder();
		
		if(comp.getAddenda().getInformacionPago() != null){			
			concat.append("\n<asant:AddendaSantanderV1 ");
			concat.append("xsi:schemaLocation=\"http://www.santander.com.mx/schemas/xsd/AddendaSantanderV1 AddendaSantanderV1.xsd\" >");
			concat.append(asantInformacionPago(comp));
			concat.append(asantInformacionEmision(comp));
			concat.append(asantInmuebles(comp));
			concat.append("</asant:AddendaSantanderV1>");
		}
		return concat.toString();
	}
	public String asantInformacionPago(CfdiComprobanteFiscal comp){
		StringBuilder concat = new StringBuilder();
		StringBuilder attributes = new StringBuilder();
		
		if(comp.getAddenda().getInformacionPago().getEmail() != null){
			attributes.append(Util.isNullEmpity(comp.getAddenda().getInformacionPago().getEmail().toUpperCase()
					, "email"));
		}
		
		if(comp.getAddenda().getInformacionPago().getCodigoISOMoneda() != null){
			attributes.append(Util.isNullEmpity(comp.getAddenda().getInformacionPago().getCodigoISOMoneda().toUpperCase()
					, "codigoISOMoneda"));
		}
				
		if(comp.getTipoAddendaCellValue().equals("1")){
			if(comp.getAddenda().getInformacionPago().getOrdenCompra() != null){
				attributes.append(Util.isNullEmpity(comp.getAddenda().getInformacionPago().getOrdenCompra().toUpperCase()
						, "ordenCompra"));
			}
			
			if(comp.getAddenda().getInformacionPago().getPosCompra() != null){
				attributes.append(Util.isNullEmpity(comp.getAddenda().getInformacionPago().getPosCompra().toUpperCase()
						, "posCompra"));
			}
		}else if (comp.getTipoAddendaCellValue().equals("2")){
			attributes.append(Util.isNullEmpity(comp.getAddenda().getInformacionPago().getCuentaContable().toUpperCase()
					, "cuentaContable"));
			
		}
		if(attributes.toString().trim().length() > 0){
			concat.append("\n<asant:InformacionPago ");
			concat.append(attributes.toString());
			concat.append("/>");
		}
		return concat.toString();
	}
	
	public String asantInformacionEmision(CfdiComprobanteFiscal comp){
		StringBuilder concat = new StringBuilder();
		StringBuilder attributes = new StringBuilder();
		
		if(comp.getTipoAddendaCellValue().equals("2")){
			if(comp.getAddenda().getInformacionEmision() !=  null){
				attributes.append(Util.isNullEmpity(comp.getAddenda().getInformacionEmision().getCentroCostos()
						, "centroCostos"));
			}
		}
		
		if(attributes.toString().trim().length() > 0){
			concat.append("\n<asant:InformacionEmision ");
			concat.append(attributes.toString());
			concat.append("/>");
		}
		return concat.toString();
	}
	
	public String asantInmuebles(CfdiComprobanteFiscal comp){
		StringBuilder concat = new StringBuilder();
		StringBuilder attributes = new StringBuilder();
		
		if(comp.getTipoAddendaCellValue().equals("3")){
			if(comp.getAddenda().getInmuebles() != null){
				attributes.append(Util.isNullEmpity(comp.getAddenda().getInmuebles().getNumContrato()
						, "numContrato"));
				attributes.append(Util.isNullEmpity(comp.getAddenda().getInmuebles().getFechaVencimiento()
						, "fechaVencimiento"));
			}
		}
		
		if(attributes.toString().trim().length() > 0){
			concat.append("\n<asant:Inmuebles ");
			concat.append(attributes.toString());
			concat.append("/>");
		}
		return concat.toString();
	}
	
	public String informacionPago(CfdiComprobanteFiscal comp){
		StringBuilder concat = new StringBuilder();
		StringBuilder attributes = new StringBuilder();
		if(comp.getAddenda().getInformacionPago() != null){

			if(comp.getAddenda().getInformacionPago().getEmail() != null){
				attributes.append(Util.isNullEmpity(comp.getAddenda().getInformacionPago().getEmail().toUpperCase()
						, "Email"));
			}
			if(comp.getAddenda().getInformacionPago().getInstitucionReceptora() != null){
				attributes.append("InstitucionReceptora=\"" 
						+ comp.getAddenda().getInformacionPago().getInstitucionReceptora().toUpperCase() + "\" ");
			}
			
			if(comp.getAddenda().getInformacionPago().getNombreBeneficiario() != null){
				attributes.append("NombreBeneficiario=\"" 
						+ comp.getAddenda().getInformacionPago().getNombreBeneficiario().toUpperCase() + "\" ");
			}

			if(comp.getAddenda().getInformacionPago().getNumeroCuenta() != null){
				attributes.append("NumeroCuenta=\"" 
						+ comp.getAddenda().getInformacionPago().getNumeroCuenta().toUpperCase() + "\" ");
			}
			if(comp.getAddenda().getInformacionPago().getNumProveedor() != null){
				attributes.append("NumProveedor=\"" 
						+ comp.getAddenda().getInformacionPago().getNumProveedor().toUpperCase() + "\" ");
			}
			
			if(comp.getAddenda().getInformacionPago().getOrdenCompra() != null){
				attributes.append(Util.isNullEmpity(comp.getAddenda().getInformacionPago().getOrdenCompra().toUpperCase()
						, "OrdenCompra"));
			}
//			if(comp.getAddenda().getInformacionPago().getPosCompra() != null){
//				attributes.append(Util.isNullEmpity(comp.getAddenda().getInformacionPago().getPosCompra().toUpperCase(), "posCompra"));
//			}
		}
		
		if(attributes.toString().trim().length() > 0){
			concat.append("\n<as:InformacionPago ");
			concat.append(attributes.toString());
			concat.append("/>");
		}
		
		return concat.toString();
	}
	
	public String informacionPagoDivisas(CfdiComprobanteFiscal comp){
		StringBuilder concat = new StringBuilder();
		StringBuilder attributes = new StringBuilder();
		if(comp.getAddenda().getInformacionPago() != null){

			if(comp.getAddenda().getInformacionPago().getEmail() != null && !comp.getAddenda().getInformacionPago().getEmail().trim().equalsIgnoreCase("") ){
				attributes.append(Util.isNullEmpity(comp.getAddenda().getInformacionPago().getEmail().toUpperCase()
						, "Email"));
			}
			if(comp.getAddenda().getInformacionPago().getInstitucionReceptora() != null 
					&& !comp.getAddenda().getInformacionPago().getInstitucionReceptora().trim().equalsIgnoreCase("")){
				attributes.append("InstitucionReceptora=\"" 
						+ comp.getAddenda().getInformacionPago().getInstitucionReceptora().toUpperCase() + "\" ");
			}
			
			if(comp.getAddenda().getInformacionPago().getNombreBeneficiario() != null 
					&& !comp.getAddenda().getInformacionPago().getNombreBeneficiario().trim().equalsIgnoreCase("") ){
				attributes.append("NombreBeneficiario=\"" 
						+ comp.getAddenda().getInformacionPago().getNombreBeneficiario().toUpperCase() + "\" ");
			}

			if(comp.getAddenda().getInformacionPago().getNumeroCuenta() != null
					&& !comp.getAddenda().getInformacionPago().getNumeroCuenta().trim().equalsIgnoreCase("") ){
				attributes.append("NumeroCuenta=\"" 
						+ comp.getAddenda().getInformacionPago().getNumeroCuenta().toUpperCase() + "\" ");
			}
			if(comp.getAddenda().getInformacionPago().getNumProveedor() != null 
					&& !comp.getAddenda().getInformacionPago().getNumProveedor().trim().equalsIgnoreCase("") ){
				attributes.append("NumProveedor=\"" 
						+ comp.getAddenda().getInformacionPago().getNumProveedor().toUpperCase() + "\" ");
			}
			
			if(comp.getAddenda().getInformacionPago().getOrdenCompra() != null 
					&& !comp.getAddenda().getInformacionPago().getOrdenCompra().trim().equalsIgnoreCase("") ){
				attributes.append(Util.isNullEmpity(comp.getAddenda().getInformacionPago().getOrdenCompra().toUpperCase()
						, "OrdenCompra"));
			}
//			if(comp.getAddenda().getInformacionPago().getPosCompra() != null){
//				attributes.append(Util.isNullEmpity(comp.getAddenda().getInformacionPago().getPosCompra().toUpperCase(), "posCompra"));
//			}
		}
		
		if(attributes.toString().trim().length() > 0){
			concat.append("\n<as:InformacionPago ");
			concat.append(attributes.toString());
			concat.append("/>");
		}
		
		return concat.toString();
	}
	
	
	public String informacionEmision(CfdiComprobanteFiscal comp){
		StringBuilder concat = new StringBuilder();
		StringBuilder attributes = new StringBuilder();
		
		if(comp.getAddenda().getInformacionEmision() != null){
			attributes.append(Util.isNullEmpity(comp.getAddenda().getInformacionEmision().getCodigoCliente(), "CodigoCliente"));
			attributes.append(Util.isNullEmpity(comp.getAddenda().getInformacionEmision().getContrato(), "Contrato"));
			attributes.append(Util.isNullEmpity(comp.getAddenda().getInformacionEmision().getPeriodo(), "Periodo"));
			attributes.append(Util.isNullEmpity(comp.getAddenda().getInformacionEmision().getCentroCostos(), "CentroCostos"));
			attributes.append(Util.isNullEmpity(comp.getAddenda().getInformacionEmision().getFolioInterno(), "FolioInterno"));
			attributes.append(Util.isNullEmpity(comp.getAddenda().getInformacionEmision().getClaveSantander(), "ClaveSantander"));
		}
		//if(attributes.toString().trim().length() > 0){
			concat.append("\n<as:InformacionEmision ");
			concat.append(attributes.toString());
			concat.append(" />");
			//concat.append("\n</as:InformacionEmision>");
		//}
		
		return concat.toString();
	}
	
	public String camposAdicionales(CfdiComprobanteFiscal comp){
		StringBuilder concat = new StringBuilder();
		if(comp.getAddenda().getCampoAdicional() != null 
				&& comp.getAddenda().getCampoAdicional().size() > 0){
			for (Entry<String, String> entry : comp.getAddenda().getCampoAdicional().entrySet()) {
				String campo = "\n<as:CampoAdicional Campo=\"" + entry.getKey()
						+ "\" Valor=\"" + entry.getValue() + "\" />";
			    concat.append(campo);
			}
		}
		return concat.toString();
	}
	
	public String camposAdicionalesDivisas(CfdiComprobanteFiscal comp){
		StringBuilder concat = new StringBuilder();
		if(comp.getAddenda().getCampoAdicional() != null 
				&& comp.getAddenda().getCampoAdicional().size() > 0){
			for (Entry<String, String> entry : comp.getAddenda().getCampoAdicional().entrySet()) {
				
				if ( !entry.getValue().trim().equalsIgnoreCase("") ) {
					String campo = "\n<as:CampoAdicional Campo=\"" + entry.getKey()
						+ "\" Valor=\"" + entry.getValue() + "\" />";
					concat.append(campo);
				}
				
			}
		}
		return concat.toString();
	}
	
	
	public String domicilioEmisor(CfdiComprobanteFiscal comp){
		StringBuilder concat = new StringBuilder();
		//Domicilio Emisor
		StringBuilder attributes = new StringBuilder();
		if(comp.getEmisor().getDomicilio().getCalle() != null){
			attributes.append("Calle=\"" + comp.getEmisor().getDomicilio().getCalle().toUpperCase()  + "\" ");
		}
		if(comp.getEmisor().getDomicilio().getLocalidad() != null){
			attributes.append("Ciudad=\"" + comp.getEmisor().getDomicilio().getLocalidad() + "\" ");
		}
		if(comp.getEmisor().getDomicilio().getCodigoPostal() != null){
			attributes.append("CodigoPostal=\"" + comp.getEmisor().getDomicilio().getCodigoPostal() + "\" ");
		}
		if(comp.getEmisor().getDomicilio().getColonia() != null){
			attributes.append("Colonia=\"" + comp.getEmisor().getDomicilio().getColonia().toUpperCase()  + "\" ");
		}
		if(comp.getEmisor().getDomicilio().getEstado() != null){
			attributes.append("Estado=\"" + comp.getEmisor().getDomicilio().getEstado().toUpperCase()  + "\" ");
		}
		//if(comp.getEmisor().getDomicilio().getLocalidad() != null){
			attributes.append("Localidad=\"\" ");
		//}
		if(comp.getEmisor().getDomicilio().getMunicipio() != null){
			attributes.append("Municipio=\"" + comp.getEmisor().getDomicilio().getMunicipio().toUpperCase()  + "\" ");
		}
		if(comp.getEmisor().getDomicilio().getNoExterior() != null){
			attributes.append("NoExterior=\"" + comp.getEmisor().getDomicilio().getNoExterior() + "\" ");
		}
		if(comp.getEmisor().getDomicilio().getNoInterior() != null){
			attributes.append("NoInterior=\"" + comp.getEmisor().getDomicilio().getNoInterior() + "\" ");
		}
		if(comp.getNumeroCuentaPago() != null && comp.getTipoEmision().equalsIgnoreCase(TipoEmision.DONATARIAS)){
			attributes.append("NumCtaPagoEntidad=\"" + comp.getNumeroCuentaPago() + "\" ");
		}
		if(comp.getEmisor().getDomicilio().getReferencia() != null){
			attributes.append("Referencia=\"" + comp.getEmisor().getDomicilio().getReferencia() + "\" ");
		}else{
			attributes.append("Referencia=\"\" ");
		}
		if(comp.getEmisor().getDomicilio().getPais() != null){
			attributes.append("pais=\"" + comp.getEmisor().getDomicilio().getPais().toUpperCase()  + "\" ");
		}
		
		if(attributes.toString().trim().length() > 0){
			concat.append("\n<as:DomicilioEmisor ");
			concat.append(attributes.toString());
			concat.append("/>");
		}
		return concat.toString();
	}
	
	public String domicilioReceptor(CfdiComprobanteFiscal comp){
		StringBuilder concat = new StringBuilder();
		StringBuilder attributes = new StringBuilder();
		//DomicilioReceptor
		if(comp.getReceptor().getDomicilio().getCalle() != null){
			attributes.append("Calle=\"" + comp.getReceptor().getDomicilio().getCalle().toUpperCase() + "\" ");
		}
		if(comp.getReceptor().getDomicilio().getCodigoPostal() != null){
			attributes.append("CodigoPostal=\"" + comp.getReceptor().getDomicilio().getCodigoPostal()  + "\" ");
		}
		if(comp.getReceptor().getDomicilio().getColonia() != null){
			attributes.append("Colonia=\"" + comp.getReceptor().getDomicilio().getColonia().toUpperCase()  + "\" ");
		}		
		if(comp.getReceptor().getDomicilio().getEstado() != null){
			attributes.append("Estado=\"" + comp.getReceptor().getDomicilio().getEstado().toUpperCase()  + "\" ");
		}
		if(comp.getReceptor().getDomicilio().getLocalidad() != null){
			attributes.append("Localidad=\"" + comp.getReceptor().getDomicilio().getLocalidad().toUpperCase()  + "\" ");
		}
		if(comp.getReceptor().getDomicilio().getMunicipio() != null){
			attributes.append("Municipio=\"" + comp.getReceptor().getDomicilio().getMunicipio().toUpperCase()  + "\" ");
		}
		if(comp.getReceptor().getDomicilio().getNoExterior() != null){
			attributes.append("NoExterior=\"" + comp.getReceptor().getDomicilio().getNoExterior().toUpperCase()  + "\" ");
		}
		if(comp.getReceptor().getDomicilio().getNoInterior() != null){
			attributes.append("NoInterior=\"" + comp.getReceptor().getDomicilio().getNoInterior().toUpperCase()  + "\" ");
		}
		if(comp.getReceptor().getDomicilio().getPais() != null){
			attributes.append("Pais=\"" + comp.getReceptor().getDomicilio().getPais().toUpperCase()  + "\" ");
		}
		if(comp.getReceptor().getDomicilio().getReferencia() != null){
			attributes.append("Referencia=\"" + comp.getReceptor().getDomicilio().getReferencia().toUpperCase()  + "\" ");
		}else{
			attributes.append("Referencia=\"\" ");
		}
		
		if(attributes.toString().trim().length() > 0){
			concat.append("\n<as:DomicilioReceptor ");
			concat.append(attributes.toString());
			concat.append("/>");
		}
		
		return concat.toString();
	}
	
	public String domicilioReceptorDivisas(CfdiComprobanteFiscal comp){
		StringBuilder concat = new StringBuilder();
		StringBuilder attributes = new StringBuilder();
		//DomicilioReceptor
		if(comp.getReceptor().getDomicilio().getCalle() != null){
			attributes.append("Calle=\"" + comp.getReceptor().getDomicilio().getCalle().toUpperCase() + "\" ");
		}
		if(comp.getReceptor().getDomicilio().getCodigoPostal() != null){
			attributes.append("CodigoPostal=\"" + comp.getReceptor().getDomicilio().getCodigoPostal()  + "\" ");
		}
		if(comp.getReceptor().getDomicilio().getColonia() != null){
			attributes.append("Colonia=\"" + comp.getReceptor().getDomicilio().getColonia().toUpperCase()  + "\" ");
		}		
		if(comp.getReceptor().getDomicilio().getEstado() != null){
			attributes.append("Estado=\"" + comp.getReceptor().getDomicilio().getEstado().toUpperCase()  + "\" ");
		}
		if(comp.getReceptor().getDomicilio().getLocalidad() != null){
			attributes.append("Localidad=\"" + comp.getReceptor().getDomicilio().getLocalidad().toUpperCase()  + "\" ");
		}
		if(comp.getReceptor().getDomicilio().getMunicipio() != null){
			attributes.append("Municipio=\"" + comp.getReceptor().getDomicilio().getMunicipio().toUpperCase()  + "\" ");
		}
		if(comp.getReceptor().getDomicilio().getNoExterior() != null){
			attributes.append("NoExterior=\"" + comp.getReceptor().getDomicilio().getNoExterior().toUpperCase()  + "\" ");
		}
		if(comp.getReceptor().getDomicilio().getNoInterior() != null){
			attributes.append("NoInterior=\"" + comp.getReceptor().getDomicilio().getNoInterior().toUpperCase()  + "\" ");
		}
		if(comp.getReceptor().getDomicilio().getPais() != null){
			attributes.append("Pais=\"" + comp.getReceptor().getDomicilio().getPais().toUpperCase()  + "\" ");
		}
		if(comp.getReceptor().getDomicilio().getReferencia() != null){
			attributes.append("Referencia=\"" + comp.getReceptor().getDomicilio().getReferencia().toUpperCase()  + "\" ");
		}
		
		if(attributes.toString().trim().length() > 0){
			concat.append("\n<as:DomicilioReceptor ");
			concat.append(attributes.toString());
			concat.append("/>");
		}
		
		return concat.toString();
	}
	
	
}
