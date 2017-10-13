package com.interfactura.firmalocal.xml.util;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.interfactura.firmalocal.datamodel.CfdiComprobanteFiscal;
import com.interfactura.firmalocal.datamodel.CfdiConcepto;
import com.interfactura.firmalocal.datamodel.CfdiConceptoImpuestoTipo;
import com.interfactura.firmalocal.datamodel.ComplementoPago;
import com.interfactura.firmalocal.xml.Properties;
import com.interfactura.firmalocal.xml.util.UtilCatalogos;

@Component
public class ConvertirCfdiImplV3_3 {

	@Autowired
	Properties properties;
	
	public String startXml(){
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	}
	public String closeFComprobante(){
		return "\n</cfdi:Comprobante> ";
	}
	public String fComprobante(CfdiComprobanteFiscal comp , Date date){
		StringBuilder concat =  new StringBuilder();
		
		concat.append("xmlns:cfdi=\"http://www.sat.gob.mx/cfd/3\" ");
		concat.append("xmlns:ecb=\"http://www.sat.gob.mx/ecb\" ");
		concat.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
		//Certificado
		concat.append("Certificado=\"" + properties.getLblCERTIFICADO() + "\" ");
		if (comp.getDescuento() != null && comp.getDescuento().doubleValue() > 0){
			concat.append("Descuento=\"" + comp.getDescuento() + "\" ");
		}
		concat.append("Fecha=\"" + Util.convertirFecha(date) + "\" ");
		concat.append(Util.isNullEmpity(comp.getFolio(), "Folio"));
		if(comp.getFormaPago() != null){
			concat.append("FormaPago=\"" + comp.getFormaPago() + "\" ");
		}
		concat.append("LugarExpedicion=\"" + "01219" + "\" ");
		if(comp.getMetodoPago() != null && comp.getMetodoPago().trim() != ""){
			concat.append("MetodoPago=\"" + comp.getMetodoPago() + "\" ");
		}
		concat.append("Moneda=\"" + comp.getMoneda() + "\" ");
		if(comp.getSerie() != null){
			concat.append("Serie=\"" + comp.getSerie() + "\" ");
		}
		//NoCertificado
		concat.append("NoCertificado=\"" + properties.getLblNO_CERTIFICADO() + "\" ");
		//Sello
		concat.append("Sello=\"" + properties.getLabelSELLO() + "\" ");
		
		String subtotal = "0";
		if(!comp.getMoneda().equalsIgnoreCase("XXX")){
			subtotal = UtilCatalogos.decimales(comp.getSubTotal().toString(), comp.getDecimalesMoneda());
		}
		concat.append("SubTotal=\"" + subtotal + "\" ");
		
		if(comp.getTipoCambio() != null && comp.getTipoCambio().trim() != ""){
			concat.append("TipoCambio=\"" + comp.getTipoCambio() + "\" ");
		}
		concat.append("TipoDeComprobante=\"" + comp.getTipoDeComprobante() + "\" ");
		
		String total = "0";
		if(!comp.getMoneda().equalsIgnoreCase("XXX")){
			total = UtilCatalogos.decimales(comp.getTotal().toString(), comp.getDecimalesMoneda());
		}
		concat.append("Total=\"" + total + "\" ");
		
		concat.append("Version=\"" + "3.3" + "\" ");
		
		return Util
				.conctatArguments(
						"\n<cfdi:Comprobante ",
						concat.toString(),
						"xsi:schemaLocation=\"http://www.sat.gob.mx/cfd/3 ", 
						"http://www.sat.gob.mx/sitio_internet/cfd/3/cfdv33.xsd ",
						"http://www.sat.gob.mx/ecb ",
						"http://www.sat.gob.mx/sitio_internet/cfd/ecb/ecb.xsd\">")
				.toString();
	}
	
	public String emisor(CfdiComprobanteFiscal comp){
		StringBuilder concat =  new StringBuilder();
		
		String valNombre = comp.getEmisor().getNombre().trim().toUpperCase().replaceAll("\\.", "");
		valNombre = valNombre.replaceAll("\\(", "");
		valNombre = valNombre.replaceAll("\\)", "");
		valNombre = valNombre.replace("/", "");
		concat.append("Nombre=\" "+Util.convierte(valNombre).toUpperCase()+"\" ");
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
					sbConceptos.append("Unidad=\"" + concepto.getUnidad() + "\" ");
				}
				sbConceptos.append("ValorUnitario=\"" + concepto.getValorUnitario() + "\"");
				sbConceptos.append(">");
				sbConceptos.append(conceptoImpuesto(concepto));
				sbConceptos.append("\n</cfdi:Concepto>");
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
						sbConceptoImpuesto.append("Importe=\"" + impuestoTipo.getImporte() + "\" ");
						sbConceptoImpuesto.append("Impuesto=\"" + impuestoTipo.getImpuesto() + "\" ");
						sbConceptoImpuesto.append("TasaOCuota=\"" + impuestoTipo.getTasaOCuota() + "\" ");
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
	
	public String addenda(CfdiComprobanteFiscal comp){
		StringBuilder concat = new StringBuilder();
		if(comp.getAddenda() != null){
			concat.append("\n<cfdi:Addenda>");
			concat.append("\n<as:AddendaSantanderV1 ");
			concat.append("xmlns:as=\"http://www.santander.com.mx/schemas/xsd/AddendaSantanderV1\">");
			concat.append(informacionPago(comp));
			concat.append(informacionEmision(comp));
			concat.append(camposAdicionales(comp));
			concat.append(domicilioEmisor(comp));
			concat.append(domicilioReceptor(comp));
			concat.append("\n</as:AddendaSantanderV1>");
			concat.append("\n</cfdi:Addenda>");
		}
		
		return concat.toString();
	}
	
	public String informacionPago(CfdiComprobanteFiscal comp){
		StringBuilder concat = new StringBuilder();
		StringBuilder attributes = new StringBuilder();
		if(comp.getAddenda().getInformacionPago() != null){
			if(!Util.isNullEmpty(comp.getAddenda().getInformacionPago().getNombreBeneficiario())
					|| !Util.isNullEmpty(comp.getAddenda().getInformacionPago().getNumeroCuenta())
					|| !Util.isNullEmpty(comp.getAddenda().getInformacionPago().getNumProveedor())
					|| !Util.isNullEmpty(comp.getAddenda().getInformacionPago().getInstitucionReceptora())){
				
				if(comp.getAddenda().getInformacionPago().getEmail() != null){
					attributes.append(Util.isNullEmpity(comp.getAddenda().getInformacionPago().getEmail().toUpperCase()
							, "email"));
				}
				
				attributes.append(Util.isNullEmpity(comp.getAddenda().getInformacionPago().getInstitucionReceptora().toUpperCase()
						, "institucionReceptora"));
				attributes.append(Util.isNullEmpity(comp.getAddenda().getInformacionPago().getNombreBeneficiario().toUpperCase()
						, "nombreBeneficiario"));
				attributes.append(Util.isNullEmpity(comp.getAddenda().getInformacionPago().getNumeroCuenta().toUpperCase()
						, "numeroCuenta"));
				attributes.append(Util.isNullEmpity(comp.getAddenda().getInformacionPago().getNumProveedor().toUpperCase()
						, "numProveedor"));
				if(comp.getAddenda().getInformacionPago().getOrdenCompra() != null){
					attributes.append(Util.isNullEmpity(comp.getAddenda().getInformacionPago().getOrdenCompra().toUpperCase()
							, "ordenCompra"));
				}
				if(comp.getAddenda().getInformacionPago().getPosCompra() != null){
					attributes.append(Util.isNullEmpity(comp.getAddenda().getInformacionPago().getPosCompra().toUpperCase(), "posCompra"));
				}
			}
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
			attributes.append(Util.isNullEmpity(comp.getAddenda().getInformacionEmision().getCodigoCliente(), "codigoCliente"));
			attributes.append(Util.isNullEmpity(comp.getAddenda().getInformacionEmision().getContrato(), "contrato"));
			attributes.append(Util.isNullEmpity(comp.getAddenda().getInformacionEmision().getPeriodo(), "periodo"));
			attributes.append(Util.isNullEmpity(comp.getAddenda().getInformacionEmision().getCentroCostos(), "centroCostos"));
			attributes.append(Util.isNullEmpity(comp.getAddenda().getInformacionEmision().getFolioInterno(), "folioInterno"));
			attributes.append(Util.isNullEmpity(comp.getAddenda().getInformacionEmision().getClaveSantander(), "claveSantander"));
		}
		if(attributes.toString().trim().length() > 0){
			concat.append("\n<as:InformacionEmision ");
			concat.append(attributes.toString());
			concat.append(">");
			//Informacion factoraje??
			concat.append("\n</as:InformacionEmision>");
		}
		
		return concat.toString();
	}
	
	public String camposAdicionales(CfdiComprobanteFiscal comp){
		StringBuilder concat = new StringBuilder();
		if(comp.getAddenda().getCampoAdicional() != null 
				&& comp.getAddenda().getCampoAdicional().size() > 0){
			for (Entry<String, String> entry : comp.getAddenda().getCampoAdicional().entrySet()) {
				String campo = "\n<as:CampoAdicional campo=\"" + entry.getKey()
						+ "\" valor=\"" + entry.getValue() + "\" />";
			    concat.append(campo);
			}
		}
		return concat.toString();
	}
	
	public String domicilioEmisor(CfdiComprobanteFiscal comp){
		StringBuilder concat = new StringBuilder();
		//Domicilio Emisor
		StringBuilder attributes = new StringBuilder();
		attributes.append(Util.isNullEmpity(comp.getEmisor().getDomicilio().getCalle(), "Calle"));
		attributes.append(Util.isNullEmpity(comp.getEmisor().getDomicilio().getLocalidad(), "Ciudad"));
		attributes.append(Util.isNullEmpity(comp.getEmisor().getDomicilio().getCodigoPostal(), "CodigoPostal"));
		attributes.append(Util.isNullEmpity(comp.getEmisor().getDomicilio().getColonia(), "Colonia"));
		attributes.append(Util.isNullEmpity(comp.getEmisor().getDomicilio().getEstado(), "Estado"));
		attributes.append(Util.isNullEmpity(comp.getEmisor().getDomicilio().getMunicipio(), "Municipio"));
		attributes.append(Util.isNullEmpity(comp.getEmisor().getDomicilio().getNoExterior(), "NoExterior"));
		attributes.append(Util.isNullEmpity(comp.getEmisor().getDomicilio().getNoInterior(), "NoInterior"));
		attributes.append(Util.isNullEmpity(comp.getEmisor().getDomicilio().getPais(), "Pais"));
		attributes.append(Util.isNullEmpity(comp.getEmisor().getDomicilio().getReferencia(), "Referecncia"));
		
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
		attributes.append(Util.isNullEmpity(comp.getReceptor().getDomicilio().getCalle(), "Calle"));
		attributes.append(Util.isNullEmpity(comp.getReceptor().getDomicilio().getLocalidad(), "Ciudad"));
		attributes.append(Util.isNullEmpity(comp.getReceptor().getDomicilio().getCodigoPostal(), "CodigoPostal"));
		attributes.append(Util.isNullEmpity(comp.getReceptor().getDomicilio().getColonia(), "Colonia"));
		attributes.append(Util.isNullEmpity(comp.getReceptor().getDomicilio().getEstado(), "Estado"));
		attributes.append(Util.isNullEmpity(comp.getReceptor().getDomicilio().getMunicipio(), "Municipio"));
		attributes.append(Util.isNullEmpity(comp.getReceptor().getDomicilio().getNoExterior(), "NoExterior"));
		attributes.append(Util.isNullEmpity(comp.getNumeroCuentaPago(), "NumCtaPagoEntidad"));
		attributes.append(Util.isNullEmpity(comp.getNumeroCuenta(), "NumCtaPagoEntidad"));
		attributes.append(Util.isNullEmpity(comp.getReceptor().getDomicilio().getNoInterior(), "NoInterior"));
		attributes.append(Util.isNullEmpity(comp.getReceptor().getDomicilio().getPais(), "Pais"));
		attributes.append(Util.isNullEmpity(comp.getReceptor().getDomicilio().getReferencia(), "Referencia"));
		
		if(attributes.toString().trim().length() > 0){
			concat.append("\n<as:DomicilioReceptor ");
			concat.append(attributes.toString());
			concat.append("/>");
		}
		
		return concat.toString();
	}
}
