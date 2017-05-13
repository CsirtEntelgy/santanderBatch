package com.interfactura.firmalocal.xml.factura;

import static com.interfactura.firmalocal.xml.util.Util.convierte;
import static com.interfactura.firmalocal.xml.util.Util.isNullEmpity;
import static com.interfactura.firmalocal.xml.util.Util.isNullEmpty;
import static com.interfactura.firmalocal.xml.util.Util.tags;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.interfactura.firmalocal.domain.entities.AddendumCustoms;
import com.interfactura.firmalocal.domain.entities.FiscalEntity;
import com.interfactura.firmalocal.domain.entities.FolioRange;
import com.interfactura.firmalocal.domain.entities.Iva;
import com.interfactura.firmalocal.persistence.FolioRangeManager;
import com.interfactura.firmalocal.xml.Properties;
import com.interfactura.firmalocal.xml.TagsXML;
import com.interfactura.firmalocal.xml.util.Util;
import com.interfactura.firmalocal.xml.util.UtilCatalogos;

/**
 * 
 * @author jose luis
 * 
 */
@Component
public class ConvertirImplV3_3 
{

	private TagsXML tags;
	private Stack<String> pila;
	private StringBuilder concat;
	@Autowired
	private Properties properties;
	private Logger logger = Logger.getLogger(ConvertirImplV3_3.class);
	@Autowired(required = true)
	private FolioRangeManager folioRangeManager;
	private String folio;
	private String tmp;
	private Util util = new Util();
	private List<String> descriptionFormat;

	public ConvertirImplV3_3() 
	{
		// tags = new TagsXML();
	}

	/**
	 * Limpia las Banderas
	 */
	public void clearFlag() 
	{
		tags.isEntidadFiscal = false;
		tags.isEmisor = false;
		tags.isReceptor = false;
		tags.isConceptos = false;
		tags.isConcepto = false;
		tags.isPredial = false;
		tags.isParte = false;
		tags.isComplementoConcepto = false;
		tags.isAduanera = false;
		tags.isRetenciones = false;
		tags.isTralados = false;
		tags.isImpuestos = false;
		tags.isComplemento = false;
		tags.isMovimiento = false;
		// tags.isComprobante=false;
		tags.isAdenda = false;
		tags.isDescriptionTASA = false;
		tags.isFormat = false;
		descriptionFormat = new ArrayList<String>();
	}

	/**
	 * 
	 * @param tokens
	 * @param numberLine
	 * @throws UnsupportedEncodingException 
	 */
	public void set(String tokens[], long numberLine) 
		throws UnsupportedEncodingException 
	{
		logger.debug("Inicio de Formateo de Linea");
		pila = new Stack<String>();
		clearFlag();
		tags.lstCustoms = new HashSet<AddendumCustoms>();
		// Se llena Tag con los Valores de Archivo XLS AMDA
		System.out.println("Antes de leer Archivo XLS ConvertirImplV3_3");
		tags.mapCatalogos = Util.readXLSFile(properties.getUrlArchivoCatalogs());
		System.out.println("Despues de leer Archivo XLS ConvertirImplV3_3: "+ tags.mapCatalogos.size());
		if (tokens.length >= 18) 
		{
			tags.NUM_PROVEEDOR = tokens[1];
			tags.CTA_DEPOSITO = tokens[2];
			tags.ORDEN_COMPRA = tokens[3];
			tags.INSTITUCION_RECEPTORA = tokens[4];
			tags.NOMBRE_BENIFICIARIO = tokens[5];
			tags.EMISION_CONTRATO = tokens[6];
			tags.EMISION_CODIGO_CLIENTE = tokens[7];
			tags.EMISION_CENTRO_COSTOS = tokens[8];
			tags.EMISION_PERIODO = tokens[9];
			tags.EMISION_CLAVE_SANTANDER = tokens[10];
			tags.EMISION_FOLIO_INTERNO = tokens[11];
			tags.EMAIL = tokens[12];
			tags.TOTAL_REPORTE = tokens[13].trim();
			tags.IVA_TOTAL_REPORTE = tokens[14].trim();
			tags.IVA_TOTAL_MN = "0";
			tags.LONGITUD = tokens[15].trim();
			tags.TIPO_FORMATO = tokens[16].trim();
			
			// Se llena Tag con los Valores de Archivo XLS AMDA
			System.out.println("Antes de leer Archivo XLS Convertir CFD");
			tags.mapCatalogos = Util.readXLSFile(properties.getUrlArchivoCatalogs());
			System.out.println("Despues de leer Archivo XLS Convertir CFD: "+ tags.mapCatalogos.size());

			if (Util.isNullEmpty(tags.IVA_TOTAL_REPORTE)) 
			{	tags.IVA_TOTAL_REPORTE = "0";	}

			System.out.println("tokens.length" + tokens.length);
			if (tokens.length >= 20) 
			{
				tags.TIPO_MONEDA = tokens[17];
				tags.TIPO_CAMBIO = tokens[18];
			} 
			else 
			{
				tags.TIPO_MONEDA = properties.getCurrency();
				tags.TIPO_CAMBIO = "1.0000";
			}
			System.out.println("tags.TIPO_MONEDA:" + tags.TIPO_MONEDA);
			System.out.println("tags.TIPO_CAMBIO:" + tags.TIPO_CAMBIO);
		} 
		else 
		{	formatCFD(numberLine);	}
	}

	/**
	 * 
	 * @param tokens
	 * @param date
	 * @param numberLine
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] fComprobante(String[] tokens, Date date, long numberLine, HashMap campos22) 
		throws UnsupportedEncodingException 
	{
		System.out.println("Entrando a Comprobante FACTURAS V3");
		if (tokens.length >= 21) 
		{
			tags.CFD_TYPE = util.getCFDType(tokens[1]);
			tags.FOLIO_REFERENCE = tokens[2];

			concat = new StringBuilder();
			
			String valEqMoneda = "";
			System.out.println("Tipo Moneda SerieFical AMDA: " + tags.TIPO_MONEDA + " : " + tags.SERIE_FISCAL_CFD);
			if(tags.TIPO_MONEDA.trim() != ""){ // Validacion Moneda Equivalencia AMDA V 3.3
				valEqMoneda = UtilCatalogos.findEquivalenciaMoneda(tags.mapCatalogos, tags.TIPO_MONEDA);
				if(!valEqMoneda.equalsIgnoreCase("vacio")){
					concat.append(" Moneda=\"" + valEqMoneda + "\"");
					tags.TIPO_MONEDA = valEqMoneda;
				}else{
					concat.append(" ElCampoMonedaNoTieneUnValorEnElCatalogo" + "=\"" + tags.SERIE_FISCAL_CFD + "\"");
				}
				
				
			}else{
				tags.TIPO_MONEDA = tags.SERIE_FISCAL_CFD;
				concat.append(" ElCampoMonedaNoTieneUnValorEnElCatalogo" + tags.TIPO_MONEDA + "=\"" + tags.SERIE_FISCAL_CFD + "\"");
			}
			
			String patternReg = "";
			double valueTipoCambioDoubl = 0.00;
			if(tags.TIPO_CAMBIO.trim().length() > 0){
				patternReg = "[0-9]{1,14}(.([0-9]{1,6}))";
				
				if(!patternReg.trim().equalsIgnoreCase("vacio") && patternReg.trim().length() > 0){
					System.out.println("Validando PATTERN REGEX Para tipo de Cambio : " + tags.TIPO_CAMBIO);
					Pattern p = Pattern.compile(patternReg);
					 Matcher m = p.matcher(tags.TIPO_CAMBIO);
				     
				     if(!m.find()){
				    	 //TipoComprobante no valido
				    	 System.out.println("PATTERN REGEX NO ES Valido el tipo de Cambio:  " + tags.TIPO_CAMBIO + " : " + patternReg);
				    	 concat.append(" ElCampoTipoCambioNoCumpleConElPatronRequerido" + tags.TIPO_CAMBIO + "=\"" + tags.TIPO_CAMBIO + "\"");
				     }else{
				    	 
				    	 try{
								valueTipoCambioDoubl = Double.parseDouble(tags.TIPO_CAMBIO);
							}catch (NumberFormatException e){
								concat.append(" TipoCambioIncorrecto" + tags.TIPO_CAMBIO + "=\"" + tags.TIPO_CAMBIO + "\"");
							}
				    	 
							if(!tags.TIPO_MONEDA.equalsIgnoreCase("MXN") && !tags.TIPO_MONEDA.equalsIgnoreCase("XXX")){ //Validacion AMDA
//								Double valMinimo = 0.000001;
//								    if(valueTipoCambioDoubl >= valMinimo){
//								       System.out.println("Tipo Cambio mayor");
//								       concat.append(" TipoCambio=\"" + tags.TIPO_CAMBIO + "\"");
//								    }else{
//								    	System.out.println("Tipo Cambio menor");
//								    	concat.append(" TipoCambioMinimoRequerido" + tags.TIPO_CAMBIO + "=\"" + tags.TIPO_CAMBIO + "\"");
//								    }
								String resultadoRipoCam = UtilCatalogos.findTipoCambioPorcentaje(tags.mapCatalogos, tags.TIPO_MONEDA, tags.TIPO_CAMBIO);
							    if(resultadoRipoCam.equalsIgnoreCase("OK")){
							       System.out.println("Tipo Cambio Bien");
							       concat.append(" TipoCambio=\"" + tags.TIPO_CAMBIO + "\"");
							    }else{
							    	System.out.println("Tipo Cambio No dentro de limites");
							    	concat.append(" ElCampoTipoCambioNoEstaDentroDeLosLimitesPara" + tags.TIPO_MONEDA + "=\"" + tags.TIPO_CAMBIO + "\"");
							    }

							}else if(tags.TIPO_MONEDA.equalsIgnoreCase("MXN")){
								 System.out.println("Cuendo es MXN La Moneda AMDA" + valueTipoCambioDoubl + " : " + tags.TIPO_CAMBIO);
								 if(valueTipoCambioDoubl > 1 || valueTipoCambioDoubl < 1){
										concat.append(" ElCampoTipoCambioNoTieneElValor1YLaMonedaIndicadaEsMXN=\"" + tags.TIPO_CAMBIO + "\"");
								 }else{
										concat.append(" TipoCambio=\"" + tags.TIPO_CAMBIO + "\""); 
								 }
							}else if(tags.TIPO_MONEDA.equalsIgnoreCase("XXX")){
								 System.out.println("Cuendo es XXX La Moneda AMDA");
//								concat.append(" TipoCambio=\"" + "1.00"+ "\"");
							}else{
//								concat.append(" TipoCambio=\"" + tags.TIPO_CAMBIO + "\"");
							} // Termina Validacion AMDA	
				    	 
				     }
					
				}
				
//				if(!tags.TIPO_MONEDA.equalsIgnoreCase("MXN") && !tags.TIPO_MONEDA.equalsIgnoreCase("XXX")){ //Validacion AMDA
//					System.out.println("Entrando a Validacion Tipo Cambio");
//					String monedaValEq= UtilCatalogos.findTipoCambioByMoneda(tags.mapCatalogos, tags.TIPO_MONEDA);
//					if(monedaValEq.trim().length() > 0 && !monedaValEq.trim().equalsIgnoreCase("vacio")){
//						concat.append(" TipoCambio=\"" + monedaValEq + "\"");
//					}else{
//						concat.append(" TipoCambio=\"" + "Tipo de Comprobante no entonctrado para el tipo de moneda" + tags.TIPO_MONEDA + "\"");
//					}
//					
//					
////					Double valMinimo = 0.000001;
////					try{
////						
////						double value = Double.parseDouble(tags.TIPO_CAMBIO);
////					    if(value >= valMinimo){
////					       System.out.println("Tipo Cambio mayor");
////					       concat.append(" TipoCambio=\"" + tags.TIPO_CAMBIO + "\"");
////					    }else{
////					    	System.out.println("Tipo Cambio menor");
////					    	concat.append(" TipoCambioMinimoRequerido" + tags.TIPO_CAMBIO + "=\"" + tags.TIPO_CAMBIO + "\"");
////					    }
////						
////					}catch (NumberFormatException e){
////						concat.append(" TipoCambioIncorrecto" + tags.TIPO_CAMBIO + "=\"" + tags.TIPO_CAMBIO + "\"");
////					}
//					
//				}else if(tags.TIPO_MONEDA.equalsIgnoreCase("MXN")){
//					 System.out.println("Cuendo es MXN La Moneda AMDA");
//					concat.append(" TipoCambio=\"" + UtilCatalogos.findTipoCambioByMoneda(tags.mapCatalogos, tags.TIPO_MONEDA) + "\"");
//				}else if(tags.TIPO_MONEDA.equalsIgnoreCase("XXX")){
//					 System.out.println("Cuendo es XXX La Moneda AMDA");
////					concat.append(" TipoCambio=\"" + "1.00"+ "\"");
//				}else{
//					concat.append(" TipoCambio=\"" + "Tipo de Comprobante no entonctrado para el tipo de moneda" + tags.TIPO_MONEDA + "\"");
////					concat.append(" TipoCambio=\"" + tags.TIPO_CAMBIO + "\"");
//				} // Termina Validacion AMDA
			}else if(tags.TIPO_MONEDA.equalsIgnoreCase("MXN")){
				 concat.append(" ElCampoTipoCambioNoTieneElValor1YLaMonedaIndicadaEsMXN" + "=\"" + tags.TIPO_CAMBIO + "\"");
			}else if(!tags.TIPO_MONEDA.equalsIgnoreCase("MXN") && !tags.TIPO_MONEDA.equalsIgnoreCase("XXX")){
				concat.append(" ElCampoTipoCambioSeDeberegistrarCuandoElCampoMonedaTieneUnValorDistintoDeMXNYXXX" + "=\"" + tags.TIPO_CAMBIO + "\"");
			}
			
			// Validando decimales soportados por el tipo de moneda AMDA V 3.3
			if(tags.TIPO_MONEDA.trim().length() > 0 ){
				tags.decimalesMoneda = UtilCatalogos.findDecimalesMoneda(tags.mapCatalogos, tags.TIPO_MONEDA);
				System.out.println("Decimales moneda: " + tags.decimalesMoneda);
//				if(tags.decimalesMoneda.contains(".")){
//					String deci[] = tags.decimalesMoneda.split("\\.");
//					tags.decimalesMoneda = deci[1];
//				}
			}
			
//			concat.append(" TipoCambio=\"" + tags.TIPO_CAMBIO + "\""); // Antes de V 3.3
//			concat.append(" Moneda=\"" + tags.TIPO_MONEDA + "\""); // Verificando si no se usa aqui AMDA
			
			if (!isNullEmpty(tokens[3])) 
			{
				tags.SERIE_FISCAL_CFD = tokens[3].trim();
				concat.append(" Serie=\"" + tags.SERIE_FISCAL_CFD + "\"");
			}
			folio = null;
			concat.append(" Folio=\"" + properties.getLblFOLIOCFD() + "\"");
			
//			tags.SUBTOTAL_MN = tokens[5];
//			concat.append(" SubTotal=\"" + tokens[5] + "\"");
			
//			if (!isNullEmpty(tokens[6])){	
//				if(UtilCatalogos.decimalesValidationMsj(tokens[6], tags.decimalesMoneda)){
//					concat.append(" Descuento=\"" + tokens[6] + "\"");
//				}else{
//					concat.append(" ElValorDelCampoDescuentoDebeTenerHastaLaCantidadDeDecimalesQueSoporteLaMoneda=\"" + tokens[6] + "\"");
//				}
//					
//			} // Descuento pendiente Validación ADMA V 3.3
//			if (!isNullEmpty(tokens[7])) 
//			{	concat.append(" motivoDescuento=\"" + tokens[7] + "\"");	} // Al Parecer nova Motivo en V 3.3 AMDA

//			tags.TOTAL_MN = tokens[8];
//			concat.append(" Total=\"" + tags.TOTAL_MN + "\"");

//			if (!isNullEmpty(tokens[9])) } // Antes V 3.3 AMDA
//			{	
//				tags.METODO_PAGO = tokens[9];
//				concat.append(" metodoDePago=\"" + tokens[9] + "\"");	
//			}

			tags.FECHA_CFD = Util.convertirFecha(date);
			Pattern p = Pattern.compile("[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])T(([01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9])");
			 Matcher m = p.matcher(tags.FECHA_CFD);
		     
		     if(!m.find()){
		    	 //RFC no valido
		    	 System.out.println("PATTERN REGEX NO ES Valido FECHA:  " + tags.FECHA_CFD);
//		    	 numRegIdTribReceptor = " ErrReceNumRegIdTrib001=\"" + valRegIdTrib + "\"";
		    	 concat.append(" ElCampoFechaNoCumpleConElPatronRequerido=\"" + tags.FECHA_CFD + "\"");
		     }else{
		    	 concat.append(" Fecha=\"" + tags.FECHA_CFD + "\"");
		     }
//			concat.append(" Fecha=\"" + tags.FECHA_CFD + "\"");
			
			String tipoComprobanteVal = "";
			if (tokens[1].equalsIgnoreCase("Nota de Credito")) {
				tipoComprobanteVal = UtilCatalogos.findTipoComprobanteByDescription(tags.mapCatalogos, "E");
				concat.append("  TipoDeComprobante=\"" + tipoComprobanteVal + "\"");	
				tags.tipoComprobante = tipoComprobanteVal;
				System.out.println("TIPO DE COMPROBANTE Factura Val AMDA: " + tipoComprobanteVal);
			}else{	
				
				// Se valida por medio de un catalogo AMDA
				if(tokens[1].trim().length() == 1){
					tipoComprobanteVal = UtilCatalogos.findTipoComprobanteByDescription(tags.mapCatalogos, "I");
					if(!tipoComprobanteVal.equalsIgnoreCase("tipoDeComprobanteIncorrecto")){
						concat.append("  TipoDeComprobante=\"" + tipoComprobanteVal + "\"");
					}else{
						concat.append(" ElCampotipoDeComprobanteNoContieneUnValorEnElCatalococ_TipoDeComprobante" + "=\"" + tokens[1].trim() + "\" ");
					}

					tags.tipoComprobante = tipoComprobanteVal;
				}else{
					if(UtilCatalogos.findTipoComprobante(tags.mapCatalogos, "I").equals("tipoDeComprobanteIncorrecto")){
						tags.tipoComprobante = "tipoDeComprobanteIncorrecto";
						concat.append(" ElCampotipoDeComprobanteNoContieneUnValorEnElCatalococ_TipoDeComprobante" + "=\"" + tokens[1].trim() + "\" ");
						tipoComprobanteVal = tags.tipoComprobante;
					}else{
						System.out.println("TIPO DE COMPROBANTE: " + tokens[1].trim());
						tags.tipoComprobante = UtilCatalogos.findTipoComprobante(tags.mapCatalogos, "I");
						concat.append(" TipoDeComprobante=\""
								+ tags.tipoComprobante + "\" ");
						tipoComprobanteVal = tags.tipoComprobante;
					}
				}
				
				System.out.println("TIPO DE COMPROBANTE Factura Val AMDA Else: " + tipoComprobanteVal);
			}
			
			// Validacion para el campo Forma de Pago y Metodo de Pago AMDA
			String formaPagoVal = "";
			String metodoPagoVal = "";
			if(!tipoComprobanteVal.trim().equalsIgnoreCase("T") && !tipoComprobanteVal.trim().equalsIgnoreCase("P")){
				formaPagoVal = UtilCatalogos.findFormaPago(tags.mapCatalogos, "Transferencia electrónica de fondos");
				System.out.println("Forma Pago consulta Catalogos: " + formaPagoVal);
				if(!formaPagoVal.equalsIgnoreCase("vacio")){
					concat.append(" FormaPago=\""
							+ formaPagoVal + "\" "); // Antes PAGO EN UNA SOLA EXHIBICION AMDA V 3.3
				}else{
					concat.append(" ElCampoFormaPagoNoContieneUnValorDelCatalogoC_FormaPago=\""
							+ formaPagoVal + "\" "); // Antes PAGO EN UNA SOLA EXHIBICION AMDA V 3.3
				}
				
				metodoPagoVal = UtilCatalogos.findMetodoPago(tags.mapCatalogos, "Pago en una sola excibicion");
				System.out.println("Metodo Pago consulta Catalogos: " + metodoPagoVal);
				if(!metodoPagoVal.equalsIgnoreCase("vacio")){
					concat.append(" MetodoPago=\""
							+  metodoPagoVal + "\" "); // Antes properties.getLabelMetodoPago() AMDA V 3.3
				}else{
					concat.append(" ElCampoMetodoPagoNoContieneUnValorDelCatalogoC_FormaPago=\""
							+  formaPagoVal + "\" "); // Antes properties.getLabelMetodoPago() AMDA V 3.3
				}

			}
//			// Validacion para el campo condicionesDePago AMDA
//			if(!tipoComprobanteVal.trim().toUpperCase().equalsIgnoreCase("T") && !tipoComprobanteVal.trim().toUpperCase().equalsIgnoreCase("P") && !tipoComprobanteVal.trim().toUpperCase().equalsIgnoreCase("N")){
//				String valorCondicionDePago = " "; // Valor fijo por el momento
//				if(valorCondicionDePago.length() <= 100){
//					concat.append(" CondicionesDePago=\""
//							+ valorCondicionDePago + "\" ");
//				}else{
//					concat.append(" CondicionesDePago=\""
//							+ "valorCondicionesDePagoIncorrecto" + valorCondicionDePago + "\" ");
//				}					
//			}
			
			tags.SUBTOTAL_MN = tokens[5];
//			if(tipoComprobanteVal.equalsIgnoreCase("T") && tipoComprobanteVal.equalsIgnoreCase("P")){
//				if(tags.decimalesMoneda == 0){
//					concat.append(" SubTotal=\"" + "0" + "\"");
//				}else if(tags.decimalesMoneda == 2){
//					concat.append(" SubTotal=\"" + "0.00" + "\"");
//				}else if(tags.decimalesMoneda == 3){
//					concat.append(" SubTotal=\"" + "0.000" + "\"");
//				}else if(tags.decimalesMoneda == 4){
//					concat.append(" SubTotal=\"" + "0.0000" + "\"");
//				}
////				concat.append(" SubTotal=\"" + "0" + "\"");
//			}else{
//				System.out.println("SubTotal agregando decimales : " + tags.decimalesMoneda + " : " + tags.SUBTOTAL_MN);
//				concat.append(" SubTotal=\"" + UtilCatalogos.decimales(tags.SUBTOTAL_MN, tags.decimalesMoneda ) + "\"");
//			}
			System.out.println("SubTotal : " + tags.SUBTOTAL_MN);
			try {
			    double valSubTotal = Double.parseDouble(tags.SUBTOTAL_MN.trim());
			    if(valSubTotal<0){
			       System.out.println("SubTotal: " + " es negativo");
			    	concat.append(" NoSePermitenValoresNegativosEnSubTotal"+ valSubTotal + "=\"" + tags.SUBTOTAL_MN.trim() + "\"");
			    }else{
			       System.out.println("SubTotal: " + " es positivo");
			       System.out.println("SubTotal agregando decimales : " + tags.decimalesMoneda + " : " + tags.SUBTOTAL_MN.trim());
			       System.out.println("SubTotal: " + " es positivo" + tags.tipoComprobante);
			       if(tipoComprobanteVal.equalsIgnoreCase("T") ||tipoComprobanteVal.equalsIgnoreCase("P")){
			    	   if(valSubTotal > 0 || valSubTotal < 0){
			    		   concat.append(" ElTipoDeComprobanteEsToPyElImporteNoEsIgualA0oCeroConDecimales=\"" + UtilCatalogos.decimales(tags.SUBTOTAL_MN.trim(), tags.decimalesMoneda) + "\"");
			    	   }else{
			    		   if(UtilCatalogos.decimalesValidationMsj(tags.SUBTOTAL_MN.trim(), tags.decimalesMoneda)){
//				    		   concat.append(" SubTotal=\"" + UtilCatalogos.decimales(tags.SUBTOTAL_MN.trim(), tags.decimalesMoneda ) + "\"");
				    		   concat.append(" SubTotal=\"" + tags.SUBTOTAL_MN.trim() + "\"");
			    		   }else{
			    			   concat.append(" ElValorDelCampoSubTotalExcedeLaCantidadDeDecimalesQueSoportaLaMoneda=\"" + tags.SUBTOTAL_MN.trim() + "\"");
			    		   }
			    	   }
			       }else{
				       if(UtilCatalogos.decimalesValidationMsj(tags.SUBTOTAL_MN.trim(), tags.decimalesMoneda)){
//			    		   concat.append(" SubTotal=\"" + UtilCatalogos.decimales(tags.SUBTOTAL_MN.trim(), tags.decimalesMoneda ) + "\"");
			    		   concat.append(" SubTotal=\"" + tags.SUBTOTAL_MN.trim() + "\"");
		    		   }else{
		    			   concat.append(" ElValorDelCampoSubTotalExcedeLaCantidadDeDecimalesQueSoportaLaMoneda=\"" + tags.SUBTOTAL_MN.trim() + "\"");
		    		   }
			       }
			       
//			       if(UtilCatalogos.decimalesValidationMsj(tags.SUBTOTAL_MN.trim(), tags.decimalesMoneda)){
////		    		   concat.append(" SubTotal=\"" + UtilCatalogos.decimales(tags.SUBTOTAL_MN.trim(), tags.decimalesMoneda ) + "\"");
//		    		   concat.append(" SubTotal=\"" + tags.SUBTOTAL_MN.trim() + "\"");
//	    		   }else{
//	    			   concat.append(" ElValorDelCampoSubTotalExcedeLaCantidadDeDecimalesQueSoportaLaMoneda=\"" + tags.SUBTOTAL_MN.trim() + "\"");
//	    		   }
			       
//			       //Validando Monto Maximo
//			       try{
//			    	   double valMaximo = Double.parseDouble(UtilCatalogos.findTipoComprobante(tags.mapCatalogos, lineas[1].trim().toUpperCase()));
//			    	   if(valSubTotal > valMaximo){
//			    		   // Se debe mostrar campo Confirmacion, no se sabe si aplica para Estados de Cuenta o Factoraje, esta por definir por parte de Santander
//			    	   }
//			       }catch (NumberFormatException e){
//			    	   System.out.println("No se encontro Valor Maximo en Catalogo TipoComprobante ");
//			       }
			    }
			} catch (NumberFormatException e) {
			    System.out.println("SubTotal: "+  "No es un numero");
			    concat.append(" SubTotallIncorrectoVieneVacioONoEsUnNumero"+  "=\"" + tags.SUBTOTAL_MN + "\"");
			}
			double valTotal = 0.00;
			tags.TOTAL_MN = tokens[8];
				if(tipoComprobanteVal.equalsIgnoreCase("T") || tipoComprobanteVal.equalsIgnoreCase("P")){
					if(tags.decimalesMoneda == 0){
						concat.append(" Total=\"" + "0" + "\"");
					}else if(tags.decimalesMoneda == 2){
						concat.append(" Total=\"" + "0.00" + "\"");
					}else if(tags.decimalesMoneda == 3){
						concat.append(" Total=\"" + "0.000" + "\"");
					}else if(tags.decimalesMoneda == 4){
						concat.append(" Total=\"" + "0.0000" + "\"");
					}
					
				}else{
					System.out.println("Total : " + tags.TOTAL_MN);
					try {
					    valTotal = Double.parseDouble(tags.TOTAL_MN);
					    if(valTotal<0){
					       System.out.println("Total: " + " es negativo");
					    	concat.append(" totalIncorrecto"+ valTotal + "=\"" + tags.TOTAL_MN + "\"");
					    }else{
					       System.out.println("Total: " + " es positivo");
					       System.out.println("Total agregando decimales : " + tags.decimalesMoneda + " : " + tags.TOTAL_MN);
					       concat.append(" Total=\"" + UtilCatalogos.decimales(tags.TOTAL_MN, tags.decimalesMoneda ) + "\"");
					       
					       //Validando Monto Maximo
					       try{
					    	   double valMaximo = Double.parseDouble(UtilCatalogos.findTipoComprobante(tags.mapCatalogos, tags.TOTAL_MN));
					    	   if(valTotal > valMaximo){
					    		   // Se debe mostrar campo Confirmacion, no se sabe si aplica para Estados de Cuenta o Factoraje, esta por definir por parte de Santander
					    	   }
					       }catch (NumberFormatException e){
					    	   System.out.println("No se encontro Valor Maximo en Catalogo TipoComprobante ");
					       }
					      
					       
					    }
					} catch (NumberFormatException e) {
					    System.out.println("Total: "+  "No es un numero");
					    concat.append(" totalIncorrecto"+  "=\"" + tags.TOTAL_MN + "\"");
					}
				}
				
//				if (!isNullEmpty(tokens[6])){	
//					if(UtilCatalogos.decimalesValidationMsj(tokens[6], tags.decimalesMoneda)){
//						try{
//							double valDesc = Double.parseDouble(tokens[6]);
//							if(valDesc > valTotal){
//								concat.append(" ElValorDelCampoDescuentoEsMayorQueElCampoImporte=\"" + tokens[6] + "\"");
//							}else{
//								concat.append(" Descuento=\"" + tokens[6] + "\"");
//							}
//						}catch(NumberFormatException e){
//							concat.append(" ElCampoImporteEsIncorrectoParaCalcularDescuento=\"" + tokens[6] + "\"");
//						}						
//					}else{
//						concat.append(" ElValorDelCampoDescuentoDebeTenerHastaLaCantidadDeDecimalesQueSoporteLaMoneda=\"" + tokens[6] + "\"");
//					}
//						
//				} // Descuento pendiente Validación ADMA V 3.3
				
				if (!isNullEmpty(tokens[6])){	
					if(UtilCatalogos.decimalesValidationMsj(tokens[6], tags.decimalesMoneda)){
						try{
							double valDesc = Double.parseDouble(tokens[6]);
							System.out.println("Valor Descuento: "+  valDesc);
							tags.descuentoFactValDou = valDesc;
							System.out.println("Valor descuentoFactValDou: "+  tags.descuentoFactValDou);
							System.out.println("Valor SubTotal Antes: "+  tags.SUBTOTAL_MN);
							double valSubTotal = Double.parseDouble(tags.SUBTOTAL_MN.trim());
							System.out.println("Valor SubTotal: "+  valSubTotal);
							if(valDesc > valSubTotal){
								concat.append(" ElValorRegistradoEnElCampoDescuentoNoEsMenorOIgualQueElCampoSubTotal=\"" + tokens[6] + "\"");
							}else{
								concat.append(" Descuento=\"" + tokens[6] + "\"");
							}
						}catch(NumberFormatException e){
							concat.append(" ElCampoSubtotalODescuentoEsIncorrectoParaCalcularDescuento=\"" + tokens[6] + "\"");
						}						
					}else{
						concat.append(" ElValorDelCampoDescuentoExcedeLaCantidadDeDecimalesQueSoporteLaMoneda=\"" + tokens[6] + "\"");
					}
						
				} // Descuento pendiente Validación ADMA V 3.3

			tags.FACTORAJE_HORA = tokens[10];
			tags.FACTORAJE_TIPO = tokens[11];
			tags.FACTORAJE_SVN = tokens[12];
			tags.FACTORAJE_SPB = tokens[13];
			tags.FACTORAJE_SPF = tokens[14];
			tags.FACTORAJE_SID = tokens[15];
			tags.FACTORAJE_LCI = tokens[16];
			tags.FACTORAJE_LIVA = tokens[17];
			tags.FACTORAJE_COMISION = tokens[18];
			tags.FACTORAJE_LETRAS = tokens[19];
			
			return Util
					.conctatArguments(
							"\n<cfdi:Comprobante Version=\"3.3\" ",
							concat.toString(),
							" xmlns:ecb=\"http://www.sat.gob.mx/ecb\" ",
							" xmlns:cfdi=\"http://www.sat.gob.mx/cfd/3\"  ",
							" xsi:schemaLocation=\"http://www.sat.gob.mx/cfd/3",
							" http://www.sat.gob.mx/sitio_internet/cfd/3/cfdv32.xsd ",
							" http://www.sat.gob.mx/ecb http://www.sat.gob.mx/sitio_internet/cfd/ecb/ecb.xsd\" ",
							" Sello=\"", properties.getLabelSELLO(), "\" ",
							" NoCertificado=\"",
							properties.getLblNO_CERTIFICADO(),
							"\" " + " Certificado=\"",
							properties.getLblCERTIFICADO(), "\" " + " ",														
							"LugarExpedicion=\"" + "01219" + "\"",//properties.getLabelLugarExpedicion()  + "\" ",
//							"NumCtaPago=\"" + properties.getlabelFormaPago()  + "\" ",
							" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" >")
					.toString().getBytes("UTF-8");
		}
		else 
		{	return formatCFD(numberLine);	}
	}

	/**
	 * 
	 * @param numberLine
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] formatCFD(long numberLine) 
		throws UnsupportedEncodingException 
	{
		tags.isFormat = true;
		descriptionFormat.add("" + numberLine);
		return "".getBytes("UTF-8");
	}

	/**
	 * 
	 * @param tokens
	 * @param lstFiscal
	 * @param numberLine
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] emisor(String tokens[], HashMap<String, FiscalEntity> lstFiscal, long numberLine
			,HashMap<String, HashMap> campos22) 
		throws UnsupportedEncodingException 
	{
		if (tokens.length >= 3) 
		{
			tags.EMISION_RFC = tokens[1].trim();
			logger.debug("RFC EMISOR: " + tags.EMISION_RFC);
			tags.fis = null;
			tags.fis = lstFiscal.get(tags.EMISION_RFC);
			if(tags.fis != null){
				tags.LUGAR_EXPEDICION = (String) campos22.get(tags.EMISION_RFC).get("LugarExpedicion");
				tags.FORMA_PAGO = (String) campos22.get(tags.EMISION_RFC).get("formaDePago");
			}else{
				tags.LUGAR_EXPEDICION = null;
				tags.FORMA_PAGO = null;
			}			
			
//			return Util
//					.conctatArguments(tags("Emisor", pila), "\n<cfdi:Emisor RFC=\"",
//							tags.EMISION_RFC, "\"", getNameEntityFiscal().toString(), "> ",
//							domicilioFiscal(campos22)).toString().getBytes("UTF-8");// Antes
			return Util
					.conctatArguments(tags("Emisor", pila), "\n<cfdi:Emisor Rfc=\"",
							tags.EMISION_RFC, "\"", getNameEntityFiscal(), domicilioFiscal(campos22),
							" />").toString().getBytes("UTF-8"); // Version 3.3 AMDA
			
		} 
		else 
		{	return formatCFD(numberLine);	}
	}

	/**
	 * 
	 * @return
	 */
	public String getNameEntityFiscal() 
	{
		if (tags.fis == null) 
		{
			tags.isEntidadFiscal = false;
			return " Nombre=\"No existe la Entidad Fiscal\"";
		} 
		else 
		{
			tags.isEntidadFiscal = true;
			if(tags.fis.getFiscalName() != null){
				String valNombre = tags.fis.getFiscalName().replaceAll("\\.", "");
				valNombre = valNombre.replaceAll("\\(", "");
				valNombre = valNombre.replaceAll("\\)", "");
				valNombre = valNombre.replace("/", "");
				System.out.println("Emisro Reg: "+valNombre );
				return " Nombre=\"" +valNombre + "\"";
			}else{
				return " Nombre=\"" +"" + "\"";
			}
//			return " Nombre=\"" + Util.isNull(tags.fis.getFiscalName()) + "\"";
		}
	}

	/**
	 * 
	 * @param tokens
	 * @param numberLine
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] receptor(String tokens[], long numberLine) 
		throws UnsupportedEncodingException 
	{
		if (tokens.length >= 4) 
		{
			tags.RECEPCION_RFC = tokens[1].trim();
			if(tags.RECEPCION_RFC.trim().length() == 0){ // Validacion AMDA Version 3.3
				tags.RECEPCION_RFC = "RFCNecesario";
			}
			
			String nombreReceptor = "";
			
			if(tokens.length > 2){
				if(!tokens[2].trim().equals("")){
					String valNombre = tokens[2].trim().replaceAll("\\.", "");
					valNombre = valNombre.replaceAll("\\(", "");
					valNombre = valNombre.replaceAll("\\)", "");
					valNombre = valNombre.replace("/", "");
					nombreReceptor = " Nombre=\"" + Util.convierte(valNombre) + "\"";
				}				
			}
			
			// Nuevos Atributos AMDA Version 3.3
			String valPais = "";
			String residenciaFiscalReceptor = "";
			String numRegIdTribReceptor = "";
			String usoCFDIReceptor = "" ;//" UsoCFDI=\"" + "P01" + "\"";
			
			if(!UtilCatalogos.findUsoCfdi(tags.mapCatalogos, "Por definir").equalsIgnoreCase("vacio")){ // Fijo por el momento
				usoCFDIReceptor = " UsoCFDI=\"" + UtilCatalogos.findUsoCfdi(tags.mapCatalogos, "Por definir") + "\"";
			}else{
				usoCFDIReceptor = " ElCampoUsoCFDINoContieneUnValorDelCatalogoc_UsoCFDI=\"" + UtilCatalogos.findUsoCfdi(tags.mapCatalogos, "Por definir") + "\"";
			}
			
			System.out.println("Receptor recepPais: " + tags.recepPais);
			if(tags.RECEPCION_RFC.equalsIgnoreCase("XEXX010101000") || tags.RECEPCION_RFC.equalsIgnoreCase("XAXX010101000") || tags.RECEPCION_RFC.equalsIgnoreCase("XEXE010101000") || tags.RECEPCION_RFC.equalsIgnoreCase("XEXX010101000")){
				
				if(tags.recepPais.trim().length() > 0){
					valPais = UtilCatalogos.findValPais(tags.mapCatalogos, tags.recepPais);
					System.out.println("Valor Abreviado Pais: " + valPais);
					if(valPais.equalsIgnoreCase("vacio")){
						valPais = UtilCatalogos.findEquivalenciaPais(tags.mapCatalogos, tags.recepPais);
						System.out.println("Valor Equivalencia Abreviado Pais: " + valPais);
						if(!valPais.equalsIgnoreCase("vacio")){
							residenciaFiscalReceptor = " ResidenciaFiscal=\"" + valPais + "\"";
						}else if(valPais.equalsIgnoreCase("MEX")){
							residenciaFiscalReceptor = " ElValorDelCampoResidenciaFiscalNoPuedeSerMEX=\"" + tags.recepPais + "\"";
						}else{
							residenciaFiscalReceptor = " ElCampoResidenciaFiscalNoContieneUnValorDelCatalogoc_Pais=\"" + tags.recepPais + "\"";
						}
					}else if(valPais.equalsIgnoreCase("MEX")){
						residenciaFiscalReceptor = " ElValorDelCampoResidenciaFiscalNoPuedeSerMEX=\"" + valPais + "\"";
					}else{
						residenciaFiscalReceptor = " ResidenciaFiscal=\"" + valPais + "\"";
					}

				}
				
//				if(tags.recepPais.trim().length() > 0){
//					valPais = UtilCatalogos.findValPais(tags.mapCatalogos, tags.recepPais);
//					System.out.println("Valor Abreviado Pais: " + valPais);
//					if(valPais.equalsIgnoreCase("vacio")){
//						valPais = UtilCatalogos.findEquivalenciaPais(tags.mapCatalogos, tags.recepPais);
//						System.out.println("Valor Equivalencia Abreviado Pais: " + valPais);
//					}
//					residenciaFiscalReceptor = " ResidenciaFiscal=\"" + valPais + "\"";
//				}
			}

						
			if(!tags.RECEPCION_RFC.equalsIgnoreCase("RFCNecesario")){
				
				if(tags.RECEPCION_RFC.equalsIgnoreCase("XEXX010101000") || tags.RECEPCION_RFC.equalsIgnoreCase("XAXX010101000") || tags.RECEPCION_RFC.equalsIgnoreCase("XEXE010101000") || tags.RECEPCION_RFC.equalsIgnoreCase("XEXX010101000") ){
					String valRegIdTrib = UtilCatalogos.findNumRegIdTrib(tags.mapCatalogos, tags.RECEPCION_RFC);
					if(!valRegIdTrib.equalsIgnoreCase("vacio")){
//						numRegIdTribReceptor = " NumRegIdTrib=\"" + valRegIdTrib + "\"";
						
						//Valida Num RegIdTrib
						String patternReg = "";
						if(!valPais.trim().equalsIgnoreCase("vacio") && valPais.trim().length() > 0){
							patternReg = UtilCatalogos.findPatternRFCPais(tags.mapCatalogos, valPais);
							System.out.println("PATTERN REGEX:  " + patternReg);
							if(!patternReg.trim().equalsIgnoreCase("vacio") && patternReg.trim().length() > 0){
								System.out.println("Validando PATTERN REGEX");
								Pattern p = Pattern.compile(patternReg);
								 Matcher m = p.matcher(valRegIdTrib);
							     
							     if(!m.find()){
							    	 //RFC no valido
							    	 System.out.println("PATTERN REGEX NO ES Valido el RegIdTrib:  " + valRegIdTrib + " : " + valPais + " : " + patternReg);
							    	 numRegIdTribReceptor = " ElValorRegistroIdAtributarioNoCumpleConElPatronCorrespondiente=\"" + valRegIdTrib + "\"";
							     }else{
							    	 numRegIdTribReceptor = " NumRegIdTrib=\"" + valRegIdTrib + "\"";
							     }
								
							}
						}
						
					}else{
						numRegIdTribReceptor = " NoSeHaEncontradoElReceptorRelacionadoConNumRegIdTribElRFCDelReceptorDebeDeSerUnGenericoExtranjero=\"" + tags.RECEPCION_RFC + "\"";
					}
					
				}
				
//				System.out.println("RFC PARA NUMREGIDTRIB: " + tags.RECEPCION_RFC);
//				String valRegIdTrib = UtilCatalogos.findNumRegIdTrib(tags.mapCatalogos, tags.RECEPCION_RFC);
//				if(!valRegIdTrib.equalsIgnoreCase("vacio")){
//					numRegIdTribReceptor = " NumRegIdTrib=\"" + UtilCatalogos.findNumRegIdTrib(tags.mapCatalogos, tags.RECEPCION_RFC) + "\"";
//					String patternReg = "";
//					if(!valPais.trim().equalsIgnoreCase("vacio") && valPais.trim().length() > 0){
//						patternReg = UtilCatalogos.findPatternRFCPais(tags.mapCatalogos, valPais);
//						System.out.println("PATTERN REGEX:  " + patternReg);
//						if(!patternReg.trim().equalsIgnoreCase("vacio") && patternReg.trim().length() > 0){
//							System.out.println("Validando PATTERN REGEX");
//							Pattern p = Pattern.compile(patternReg);
//							 Matcher m = p.matcher(tags.RECEPCION_RFC);
//						     
//						     if(!m.find()){
//						    	 //RFC no valido
//						    	 numRegIdTribReceptor = " ElValorRFCNoCumpleConElPatronCorrespondienteDelNumRegIdTrib=\"" + UtilCatalogos.findNumRegIdTrib(tags.mapCatalogos, tags.RECEPCION_RFC) + "\"";
//						     }
//							
//						}
//					}
//					
//				}else{
//					numRegIdTribReceptor = " NoSeHaEncontradoElRFCDelReceptorRelacionadoConNumRegIdTrib=\"" + tags.RECEPCION_RFC + "\"";
//				}
				
//				System.out.println("Valor NumRegIdTrib: " + valRegIdTrib);
			}
			
			System.out.println("Saliendo de Receptor: ");
			tags("Receptor", pila); // Validando la forma 
			return Util
					.conctatArguments(
//							tags("Receptor", pila), // Veamos si coloca bien el cierre tag de Emisor
							"\n<cfdi:Receptor Rfc=\"",
							Util.convierte(tokens[1].trim()),
							"\"",
							nombreReceptor, 
							residenciaFiscalReceptor,
							numRegIdTribReceptor,
							usoCFDIReceptor, " />").toString().getBytes("UTF-8");
			
//			return Util
//					.conctatArguments(
//							tags("Receptor", pila),
//							"\n<cfdi:Receptor Rfc=\"",
//							tokens[1].trim(),
//							"\"",
//							tokens.length > 2 ? " Nombre=\"" + convierte(tokens[2].trim())
//									+ "\"" : "", " >").toString().getBytes("UTF-8");
		} else {
			return formatCFD(numberLine);
		}
	}

	/**
	 * 
	 * @param tokens
	 * @param numberLine
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] concepto(String[] tokens, long numberLine) 
		throws UnsupportedEncodingException 
	{
		if (tokens.length >= 8) 
		{
			tags.UNIDAD_MEDIDA=tokens[2];
			System.out.println("Asignando Numero De Conceptos: " + tags.numeroConceptosFac);
			tags.numeroConceptosFac = tags.numeroConceptosFac+1;
			System.out.println("Asignando Numero De Conceptos Despues: " + tags.numeroConceptosFac);
			String valDescConcep = "";
			if(tokens[4].trim().length() > 0){
				valDescConcep = Util.convierte(tokens[4]).trim();
				valDescConcep = valDescConcep.replaceAll("\\.", "");
				valDescConcep = valDescConcep.replaceAll("\\(", "");
				valDescConcep = valDescConcep.replaceAll("\\)", "");
				valDescConcep = valDescConcep.replaceAll("/", "");
			}
			
			// Nuevo Campo AMDA Version 3.3 regimenStr = "\n<cfdi:RegimenFiscal Regimen=\"" + regVal + "\" />";
			System.out.println("Tipo Comprobante en Concepto: " + tags.tipoComprobante);
			String valorUnitarioStr = "";
			String nodoValorUnitarioStr = "";
			try{
				Double valUnit = Double.parseDouble(tokens[6].trim());
				if(tags.tipoComprobante.trim().equalsIgnoreCase("I") || tags.tipoComprobante.trim().equalsIgnoreCase("E") || tags.tipoComprobante.trim().equalsIgnoreCase("N")){
					// Valor unitario debe ser mayor a 0
					if(valUnit <= 0){
						valorUnitarioStr = "\" ElValorDelCampoValorUnitarioDebeSerMayorQueCeroCuandoElTipoDeComprobanteEsIngresoEgresoONomina=\"";
//						nodoValorUnitarioStr = "\" valorUnitarioDebeSerMayorDeCero=\"" + valorUnitarioStr ;
						nodoValorUnitarioStr = valorUnitarioStr + "El valor valor del campo ValorUnitario debe ser mayor que cero (0) cuando el tipo de comprobante es Ingreso, Egreso o Nomina" ;
					}else{
						valorUnitarioStr = tokens[6].trim(); 
						if(UtilCatalogos.decimalesValidationMsj(valorUnitarioStr, tags.decimalesMoneda)){
							nodoValorUnitarioStr = "\" ValorUnitario=\"" + valorUnitarioStr ;
		    		   }else{
		    			   	nodoValorUnitarioStr = "\" ElValorDelCampoValorUnitarioDebeTenerHastaLaCantidadDeDecimalesQueSoporteLaMoneda=\"" + valorUnitarioStr ;
		    		   }
//						nodoValorUnitarioStr = "\" ValorUnitario=\"" + valorUnitarioStr ;
					}
				}else if(tags.tipoComprobante.trim().equalsIgnoreCase("T")){
					// Valor unitario puede ser mayor o igual a 0
					if(valUnit < 0){
						valorUnitarioStr = "\" ElValorDelCampoValorUnitarioDebeSerMayorQueCeroCuandoElTipoDeComprobanteEsTraslado=\"";
//						nodoValorUnitarioStr = "\" valorUnitarioDebeSerMenorDeCero=\"" + valorUnitarioStr ;
						nodoValorUnitarioStr = valorUnitarioStr + "El valor valor del campo ValorUnitario debe ser mayor que cero (0) cuando el tipo de comprobante es Traslado" ;
					}else{
						valorUnitarioStr = tokens[6].trim();
						if(UtilCatalogos.decimalesValidationMsj(valorUnitarioStr, tags.decimalesMoneda)){
							nodoValorUnitarioStr = "\" ValorUnitario=\"" + valorUnitarioStr ;
		    		   }else{
		    			   	nodoValorUnitarioStr = "\" ElValorDelCampoValorUnitarioDebeTenerHastaLaCantidadDeDecimalesQueSoporteLaMoneda=\"" + valorUnitarioStr ;
		    		   }
//						nodoValorUnitarioStr = "\" ValorUnitario=\"" + valorUnitarioStr ;
					}
				}else if(tags.tipoComprobante.trim().equalsIgnoreCase("P")){
					// Valor unitario debe ser igual a 0
					if(valUnit != 0){
						valorUnitarioStr = "\" ElValorDelCampoValorUnitarioDebeSerMayorQueCeroCuandoElTipoDeComprobanteEsPago=\"";
//						nodoValorUnitarioStr = "\" valorUnitarioDebeSerCero=\"" + valorUnitarioStr ;
						nodoValorUnitarioStr =  valorUnitarioStr + "El valor valor del campo ValorUnitario debe ser mayor que cero (0) cuando el tipo de comprobante es Pago" ;
					}else{
						valorUnitarioStr = tokens[6].trim();
						if(UtilCatalogos.decimalesValidationMsj(valorUnitarioStr, tags.decimalesMoneda)){
							nodoValorUnitarioStr = "\" ValorUnitario=\"" + valorUnitarioStr ;
		    		   }else{
		    			   	nodoValorUnitarioStr = "\" ElValorDelCampoValorUnitarioDebeTenerHastaLaCantidadDeDecimalesQueSoporteLaMoneda=\"" + valorUnitarioStr ;
		    		   }
//						nodoValorUnitarioStr = "\" ValorUnitario=\"" + valorUnitarioStr ;
					}
				}else{
					// El tipo de comprobante no esta definido
						valorUnitarioStr = "tipoDeComprobanteNoDefinido";
						nodoValorUnitarioStr = "\" valorUnitarioNoDefinido=\"" + valorUnitarioStr ;
				}
			}catch(NumberFormatException e){
				valorUnitarioStr = "valorUnitarioIncorrecto";
				nodoValorUnitarioStr = "\" valorUnitarioIncorrecto=\"" + valorUnitarioStr ;
			}
						
			String claveUnidad = "";
			if(tags.UNIDAD_MEDIDA.trim().length() > 0){
//				claveUnidad = UtilCatalogos.findValClaveUnidad(tags.mapCatalogos, unidadVal);
				claveUnidad = "E48";
			}			
			
			// Importe V 3.3 AMDA pendiente logica de redondeo
			String valImporte = "";
			String lineImporte = "";
			if(tokens[6].trim().length() > 0){
				System.out.println("Importe en Concepto: " + tokens[6].trim());
				valImporte = tokens[6].trim();
				if(UtilCatalogos.decimalesValidationMsj(valImporte, tags.decimalesMoneda)){
					lineImporte = "\" Importe=\"" + valImporte;
				}else{
					lineImporte = "\" ElValorDelCampoImporteDebeTenerHastaLaCantidadDeDecimalesQueSoportaLaMoneda=\"" + valImporte;
				}
//				valImporte = tokens[6].trim();
			}
			
			// Elemento Impuestos V3.3 AMDA
			String elementImpuestos = "";
			
			// Elemento Traslados V3.3 AMDA
			String valorBase = "";
			String claveImp = "";
			String valTipoFactor = "Tasa"; // Por definir de donde tomar el valor AMDA
			String tasaOCuotaStr = "";
			String valImporteImpTras = "";
			if(!valorUnitarioStr.equalsIgnoreCase("valorUnitarioDebeSerMayorDeCero") && !valorUnitarioStr.equalsIgnoreCase("valorUnitarioDebeSerMenorDeCero") && !valorUnitarioStr.equalsIgnoreCase("valorUnitarioDebeSerCero") && !valorUnitarioStr.equalsIgnoreCase("tipoDeComprobanteNoDefinido") && !valorUnitarioStr.equalsIgnoreCase("valorUnitarioIncorrecto")){
				try{
					double value = Double.parseDouble(valorUnitarioStr);
					valorBase = new BigDecimal(value * 1).toString();
					System.out.println("ValorBase AMDA : " + valorBase);
				}catch(NumberFormatException e){
					System.out.println("Catch en ValorBase AMDA");
				}
			}
			
			if(tags.trasladoImpuestoVal.trim().length() > 0){ // Validando el codigo del Impuesto
				System.out.println("Valor Impuesto Traslado AMDA : " + tags.trasladoImpuestoVal);
				claveImp = UtilCatalogos.findValClaveImpuesto(tags.mapCatalogos, tags.trasladoImpuestoVal);
				System.out.println("Valor Clave Impuesto Traslado AMDA : " + claveImp);
			}
			
			if(valTipoFactor.equalsIgnoreCase("Tasa") || valTipoFactor.equalsIgnoreCase("Cuota")){
				System.out.println("Validacion TasaOCuota Traslado AMDA : " + tags.trasladoImpuestoVal + " : " + valTipoFactor);
				if(tags.retencionImpuestoVal.trim().length() > 0){
					if(!tags.trasladoImpuestoVal.trim().equalsIgnoreCase("ISR")){
						if(tags.trasladoImpuestoVal.trim().length() > 0){
//							tasaOCuotaStr = "\" TasaOCuota=\""  + UtilCatalogos.findValMaxTasaOCuota(tags.mapCatalogos, tags.trasladoImpuestoVal, valTipoFactor);
							tasaOCuotaStr = "\" TasaOCuota=\""  + Util.completeZeroDecimals(UtilCatalogos.findValMaxTasaOCuota(tags.mapCatalogos, tags.trasladoImpuestoVal, valTipoFactor), 6);
						}else{
							tasaOCuotaStr = "\" ElCampoNoContieneUnValorDelCatalogoImpuestoParaTasaOCuotaTraslado=\""  + Util.completeZeroDecimals(UtilCatalogos.findValMaxTasaOCuota(tags.mapCatalogos, tags.trasladoImpuestoVal, valTipoFactor), 6);
						}
					}
				}else{
					tasaOCuotaStr = "\" ElCampoNoContieneUnValorDelCatalogoImpuestoParaTasaOCuotaTraslado=\""  + tags.trasladoImpuestoVal;
				}
				
				System.out.println("Valor TasaOCuota Traslado AMDA : " + tasaOCuotaStr);
			}
			
			if(valTipoFactor.equalsIgnoreCase("Tasa") || valTipoFactor.equalsIgnoreCase("Cuota")){
				System.out.println("Valor Importe AMDA T : " + tags.trasladoImporteVal );
				if(tags.trasladoImporteVal.trim().length() > 0){
					valImporteImpTras = "\" Importe=\"" +tags.trasladoImporteVal.trim() + "\"";
				}else{
					valImporteImpTras = "\" Importe=\"" + "0.00" + "\"";
				}
				
			}
			// Base = ValImporte, Importe = Base por porcentajemas Base, descripcion mandar Util.convierte(lineas[1]).trim() 
			Map<String, Object> trasladoDoom = UtilCatalogos.findTraslados(tags.mapCatalogos, valImporte, valDescConcep, tags.decimalesMoneda, tags.tipoComprobante);
			System.out.println("TRASLADO NODOS AMDA : " + trasladoDoom);
			String elementTraslado = "\n<cfdi:Traslados>" + 
//									 "\n<cfdi:Traslado Base=\"" + valorBase +
//									 "\" Impuesto=\"" + claveImp +
//									 "\" TipoFactor=\"" + valTipoFactor + // Por definir de donde tomar el valor AMDA
//									 tasaOCuotaStr +
//									 valImporteImpTras +
//									 " />" +
									 trasladoDoom.get("valNodoStr") +
									 "\n</cfdi:Traslados>";
			System.out.println("Elemento Traslado AMDA : " + elementTraslado);
			System.out.println("TRASLADO NODOS AMDA SumISR : " + trasladoDoom.get("sumTotalIsr"));
			tags.sumTraTotalIsr = trasladoDoom.get("sumTotalIsr").toString();
			System.out.println("TRASLADO NODOS AMDA SumIVA : " + trasladoDoom.get("sumTotalIva"));
			tags.sumTraTotalIva = trasladoDoom.get("sumTotalIva").toString();
			System.out.println("TRASLADO NODOS AMDA SumIEPS : " + trasladoDoom.get("sumTotalIeps"));
			tags.sumTraTotalIeps = trasladoDoom.get("sumTotalIeps").toString();
			
			try{
				Double sumTotalIsrDo = Double.parseDouble(tags.sumTraTotalIsr);
				tags.sumTraTotalIsrDou = tags.sumTraTotalIsrDou + sumTotalIsrDo;
				tags.sumTraTotalIsr = tags.sumTraTotalIsrDou.toString();
//				System.out.println("TRASLADO NODOS AMDA SumISR Double : " + tags.sumTraTotalIsrDou);
				Double sumTraTotalIvaDou = Double.parseDouble(tags.sumTraTotalIva);
				tags.sumTraTotalIvaDou = tags.sumTraTotalIvaDou + sumTraTotalIvaDou;
				tags.sumTraTotalIva = tags.sumTraTotalIvaDou.toString();
//				System.out.println("TRASLADO NODOS AMDA SumIVA Double : " + tags.sumTraTotalIvaDou);
				Double sumTraTotalIepsDou = Double.parseDouble(tags.sumTraTotalIeps);
				tags.sumTraTotalIepsDou = tags.sumTraTotalIepsDou + sumTraTotalIepsDou;
				tags.sumTraTotalIeps = tags.sumTraTotalIepsDou.toString();
//				System.out.println("TRASLADO NODOS AMDA SumIEPS Double : " + tags.sumTraTotalIepsDou);
			}catch(NumberFormatException e){
				System.out.println("Calculando TRASLADO Sumas AMDA Error Numerico");
			}
			
			System.out.println("TRASLADO NODOS AMDA SumISR Double : " + tags.sumTraTotalIsrDou);
			System.out.println("TRASLADO NODOS AMDA SumIVA Double : " + tags.sumTraTotalIvaDou);
			System.out.println("TRASLADO NODOS AMDA SumIEPS Double : " + tags.sumTraTotalIepsDou);
			
			// Elemento Retenciones V3.3 AMDA
			String valorBaseRet = "";
			String claveImpRet = "";
			String valTipoFactorRet = "Tasa"; // Por definir de donde tomar el valor AMDA
			String tasaOCuotaStrRet = "";
			String valImporteImpRet = "";
			if(!valorUnitarioStr.equalsIgnoreCase("valorUnitarioDebeSerMayorDeCero") && !valorUnitarioStr.equalsIgnoreCase("valorUnitarioDebeSerMenorDeCero") && !valorUnitarioStr.equalsIgnoreCase("valorUnitarioDebeSerCero") && !valorUnitarioStr.equalsIgnoreCase("tipoDeComprobanteNoDefinido") && !valorUnitarioStr.equalsIgnoreCase("valorUnitarioIncorrecto")){
				try{
					double value = Double.parseDouble(valorUnitarioStr);
					valorBaseRet = new BigDecimal(value * 1).toString();
					System.out.println("ValorBase Ret AMDA : " + valorBaseRet);
				}catch(NumberFormatException e){
					System.out.println("Catch en ValorBase Ret AMDA");
				}
			}
			
			if(tags.retencionImpuestoVal.trim().length() > 0){ // Validando el codigo del Impuesto
				System.out.println("Valor Impuesto Ret AMDA : " + tags.retencionImpuestoVal);
				claveImpRet = UtilCatalogos.findValClaveImpuesto(tags.mapCatalogos, tags.retencionImpuestoVal);
				System.out.println("Valor Clave Impuesto Ret AMDA : " + claveImpRet);
			}
			
			if(valTipoFactorRet.equalsIgnoreCase("Tasa") || valTipoFactorRet.equalsIgnoreCase("Cuota")){
				System.out.println("Validacion TasaOCuota Ret AMDA : " + tags.retencionImpuestoVal + " : " + valTipoFactorRet);
				if(tags.retencionImpuestoVal.trim().length() > 0){
					if(!tags.retencionImpuestoVal.trim().equalsIgnoreCase("ISR")){
						if(tags.trasladoImpuestoVal.trim().length() > 0){
							tasaOCuotaStrRet = "\" TasaOCuota=\""  + Util.completeZeroDecimals(UtilCatalogos.findValMaxTasaOCuota(tags.mapCatalogos, tags.retencionImpuestoVal, valTipoFactorRet), 6);
						}else{
							tasaOCuotaStrRet = "\" ElCampoNoContieneUnValorDelCatalogoImpuestoParaTasaOCuotaRetencion=\""  + Util.completeZeroDecimals(UtilCatalogos.findValMaxTasaOCuota(tags.mapCatalogos, tags.retencionImpuestoVal, valTipoFactorRet), 6);
						}
						
					}
				}else{
					tasaOCuotaStrRet = "\" ElCampoNoContieneUnValorDelCatalogoImpuestoParaTasaOCuotaRetencion=\"" + tags.retencionImpuestoVal ;
				}
				
				System.out.println("Valor TasaOCuota Ret AMDA : " + tasaOCuotaStrRet);
			}
			
			if(valTipoFactor.equalsIgnoreCase("Tasa") || valTipoFactor.equalsIgnoreCase("Cuota")){
				System.out.println("Valor Importe Ret AMDA R : " + tags.retencionImporteVal );
				if(tags.retencionImporteVal.trim().length() > 0){
					valImporteImpRet = "\" Importe=\"" +tags.retencionImporteVal.trim() + "\"";
				}else{
					valImporteImpRet = "\" Importe=\"" + "0.00" + "\"";
				}
			}
			
			Map<String, Object> retencionDoom = UtilCatalogos.findRetencion(tags.mapCatalogos, valImporte, valDescConcep, tags.decimalesMoneda, tags.tipoComprobante);
			String elementRetencion = "\n<cfdi:Retenciones>" +
//					 				  "\n<cfdi:Retencion Base=\"" + valorBaseRet +
//					 				  "\" Impuesto=\"" + claveImpRet +
//					 				   "\" TipoFactor=\"" + valTipoFactorRet + // Por definir de donde tomar el valor AMDA
//					 				   tasaOCuotaStrRet +
//					 				   valImporteImpRet +
//					 				  "/>" +
									  retencionDoom.get("valNodoStr") +
									  "\n</cfdi:Retenciones>";
			
			System.out.println("RETENCION NODOS AMDA SumISR : " + retencionDoom.get("sumTotalIsr"));
			tags.sumRetTotalIsr = retencionDoom.get("sumTotalIsr").toString();
			System.out.println("RETENCION NODOS AMDA SumIVA : " + retencionDoom.get("sumTotalIva"));
			tags.sumRetTotalIva = retencionDoom.get("sumTotalIva").toString();
			System.out.println("RETENCION NODOS AMDA SumIEPS : " + retencionDoom.get("sumTotalIeps"));
			tags.sumRetTotalIeps = retencionDoom.get("sumTotalIeps").toString();
			
			try{
				Double sumTotalIsrDo = Double.parseDouble(tags.sumTraTotalIsr);
				tags.sumRetTotalIsrDou = tags.sumRetTotalIsrDou + sumTotalIsrDo;
				tags.sumRetTotalIsr = tags.sumRetTotalIsrDou.toString();
//				System.out.println("TRASLADO NODOS AMDA SumISR Double : " + tags.sumTraTotalIsrDou);
				Double sumRetTotalIvaDou = Double.parseDouble(tags.sumRetTotalIva);
				tags.sumRetTotalIvaDou = tags.sumRetTotalIvaDou + sumRetTotalIvaDou;
				tags.sumRetTotalIva = tags.sumRetTotalIvaDou.toString();
//				System.out.println("TRASLADO NODOS AMDA SumIVA Double : " + tags.sumTraTotalIvaDou);
				Double sumRetTotalIepsDou = Double.parseDouble(tags.sumRetTotalIeps);
				tags.sumRetTotalIepsDou = tags.sumRetTotalIepsDou + sumRetTotalIepsDou;
				tags.sumRetTotalIeps = tags.sumRetTotalIepsDou.toString();
//				System.out.println("TRASLADO NODOS AMDA SumIEPS Double : " + tags.sumTraTotalIepsDou);
			}catch(NumberFormatException e){
				System.out.println("Calculando Retencion Sumas AMDA Error Numerico");
			}
			
			System.out.println("TRASLADO NODOS AMDA SumISR Double : " + tags.sumRetTotalIsrDou);
			System.out.println("TRASLADO NODOS AMDA SumIVA Double : " + tags.sumRetTotalIvaDou);
			System.out.println("TRASLADO NODOS AMDA SumIEPS Double : " + tags.sumRetTotalIepsDou);
			
			String claveProdServVal = ""; // Fijo por el momento AMDA
			if(!UtilCatalogos.findClaveProdServbyDesc(tags.mapCatalogos, "Instituciones bancarias").equalsIgnoreCase("vacio")){
				claveProdServVal = " ClaveProdServ=\"" +UtilCatalogos.findClaveProdServbyDesc(tags.mapCatalogos, "Instituciones bancarias"); // Fijo 84121500 AMDA
			}else{
				claveProdServVal = " ElCampoClaveProdServNoContieneUnValorDelCatalogoc_ClaveProdServ=\"" + "vacio"; // Fijo 84121500 AMDA
			}
			boolean paint = false;
			if(elementTraslado.length() > 35 && elementRetencion.length() > 39){
				elementImpuestos = "\n<cfdi:Impuestos>" + 
			   			elementTraslado +
			   			elementRetencion +
			   			"\n</cfdi:Impuestos>";
				paint = true;
				System.out.println("ENTRO UNO: " + elementImpuestos);
			}else if(elementRetencion.length() > 39 && !paint){
				elementImpuestos = "\n<cfdi:Impuestos>" + 
//			   			elementTraslado +
			   			elementRetencion +
			   			"\n</cfdi:Impuestos>";
				System.out.println("ENTRO DOS: " + elementImpuestos);
			}else if(elementTraslado.length() > 35 && !paint){
				elementImpuestos = "\n<cfdi:Impuestos>" + 
			   			elementTraslado +
//			   			elementRetencion +
			   			"\n</cfdi:Impuestos>";
				System.out.println("ENTRO TRES: " + elementImpuestos);
			}
			System.out.println("Elemento Impuestos AMDA : " + elementImpuestos);
			
			String nodoConcepto = "\n<cfdi:Concepto" + claveProdServVal + 
					  "\" Cantidad=\"" + "1" +
					  "\" ClaveUnidad=\"" + claveUnidad + //Pendiente el valor de ClaveUnidad
					  "\" Unidad=\"" + tags.UNIDAD_MEDIDA.trim() + 
					  "\" Descripcion=\"" + valDescConcep +//Util.convierte(convierte(tokens[4])).trim() + 
					  nodoValorUnitarioStr + 
					  lineImporte + "\" " + " >"+
					  elementImpuestos +
//					  "\n<cfdi:Impuestos TotalImpuestosTrasladados=\"" + trasladoDoom.get("sumaTotal") +
//					  "\" TotalImpuestosRetenidos=\"" + retencionDoom.get("sumaTotal") + "\" "+ "/>" +
//					  "\n</cfdi:Impuestos>" + 
					  "\n</cfdi:Concepto>";
			System.out.println("String Nodo Concepto: " + nodoConcepto);
			
			return Util
					.conctatArguments(
//							"\n<cfdi:Concepto ",
							nodoConcepto.toString()
//							"\"/>"
							).toString().getBytes("UTF-8");
			
//		return Util
//				.conctatArguments(tags("Concepto", pila),
//						"\n<cfdi:Concepto cantidad=\"", tokens[1].trim(), "\" ",
//						isNullEmpity(tokens[2], "unidad"),
//						isNullEmpity(tokens[3], "noIdentificacion"),
//						" descripcion=\"", convierte(tokens[4]),
//						"\" valorUnitario=\"", tokens[5], "\" importe=\"",
//						tokens[6], "\">").toString().getBytes("UTF-8");
		} 
		else 
		{	return formatCFD(numberLine);	}
	}

	/**
	 * 
	 * @return
	 */
	public String domicilioFiscal(HashMap campos22) 
	{
		if (tags.isEntidadFiscal) 
		{
			tags._Calle = "calle=\"" + tags.fis.getAddress().getStreet()
					+ "\" ";
			tags._NoExterior = Util.isNullEmpity(tags.fis.getAddress()
					.getExternalNumber(), "noExterior");
			tags._NoInterior = Util.isNullEmpity(tags.fis.getAddress()
					.getInternalNumber(), "noInterior");
			tags._Colonia = Util.isNullEmpity(tags.fis.getAddress()
					.getNeighborhood(), "colonia");
			tags._Localidad = Util.isNullEmpity("", "localidad");
			tags._Referencia = Util.isNullEmpity(tags.fis.getAddress()
					.getReference(), "referencia");
			tags._Municipio = "municipio=\""
					+ tags.fis.getAddress().getRegion() + "\" ";
			if(tags.fis.getAddress().getState()!=null)
			{
				tags._Estado = "estado=\""
						+ tags.fis.getAddress().getState().getName() + "\" ";
				tags._Pais = " pais=\""
						+ tags.fis.getAddress().getState().getCountry().getName()
						+ "\" ";
			} 
			else 
			{
				tags._Estado = "estado=\"\" ";
				tags._Pais = " pais=\"\" ";
			}
			tags._CodigoPostal = "codigoPostal=\""
					+ tags.fis.getAddress().getZipCode() + "\" ";
		} else 
		{
			tags._Calle = "calle=\"\" ";
			tags._NoExterior = "";
			tags._NoInterior = "";
			tags._Colonia = "";
			tags._Localidad = "";
			tags._Referencia = "";
			tags._Municipio = "municipio=\"\" ";
			tags._Estado = "estado=\"\" ";
			tags._Pais = " pais=\"\" ";
			tags._CodigoPostal = "codigoPostal=\"\" ";
		}
		String regVal =null;
		String regimenStr="";
		try{
			if(tags.fis != null){
				System.out.println("tags.fis.getTaxID() " + tags.fis.getTaxID());
				
				regVal = (String) ((HashMap) campos22.get(tags.fis.getTaxID())).get("regimenFiscal");
				tags.REGIMEN_FISCAL=regVal;
//				regimenStr = "\n<cfdi:RegimenFiscal Regimen=\"" + regVal + "\" />";
				String regFisCon = UtilCatalogos.findRegFiscalCode(tags.mapCatalogos, regVal);
				if(!regFisCon.equalsIgnoreCase("vacio")){
					regimenStr = " RegimenFiscal=\"" + regFisCon + "\" "; // Agregue esto /> para cerrar el nodo de concepto al regresar AMDA
				}else{
					regimenStr = " ElCampoRegimenFiscalNoContieneUnValorDelCatalogoc_RegimenFiscal=\"" + regVal + "\" "; // Agregue esto /> para cerrar el nodo de concepto al regresar AMDA
				}
			}
			
		}catch(Throwable e){
			e.printStackTrace();
		}
		
//		return Util.conctatArguments("\n<cfdi:DomicilioFiscal ", tags._Calle,
//				tags._NoExterior, tags._NoInterior, tags._Colonia,
//				tags._Localidad, tags._Referencia, tags._Municipio,
//				tags._Estado, tags._Pais, tags._CodigoPostal, " />" + regimenStr.toString(),
//				tags("", pila)).toString();
		return regimenStr; // Agregue este regreso solo de prueba version 3.3 AMDA
	}

	/**
	 * 
	 * @param tokens
	 * @param numberLine
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] impuestos(String tokens[], long numberLine) 
		throws UnsupportedEncodingException 
	{
		tags.isImpuestos = true;
		tmp = "";
		if (tokens.length >= 4) 
		{
			if (tags.isParte) 
			{	tmp += "\n</cfdi:Parte>";	}
			tmp += tags("", pila);
			if (tags.isConceptos) 
			{
				tmp += "\n</cfdi:Conceptos>";
				tags.isConceptos = false;
			}
	
			if (!Util.isNullEmpty(tokens[2])) 
			{	tags.IVA_TOTAL_MN = tokens[2];	}
	
			tags.TOTAL_IMP_RET = tokens[1].trim();
			tags.TOTAL_IMP_TRA = tokens[2].trim();
			
			String totalImpRetLine = "";
			if(!Util.isNullEmpty(tokens[1].trim())){
				if(UtilCatalogos.decimalesValidationMsj(tags.TOTAL_IMP_RET, tags.decimalesMoneda)){
					totalImpRetLine = " TotalImpuestosRetenidos=\"" + tags.TOTAL_IMP_RET + "\" ";
					tags.atributoTotalImpuestosReten = true;
				}else{
					totalImpRetLine = " ElValorDelCampoTotalImpuestosRetenidosDebeTenerHastaLaCantidadDeDecimalesQueSoporteLaMoneda=\"" + tags.TOTAL_IMP_RET + "\" ";
					tags.atributoTotalImpuestosReten = false;
				}
			}else{
				tags.atributoTotalImpuestosReten = false;
			}
			
			String totalImpTraLine = "";
			if(!Util.isNullEmpty(tokens[2].trim())){
				if(UtilCatalogos.decimalesValidationMsj(tags.TOTAL_IMP_TRA, tags.decimalesMoneda)){
					totalImpTraLine = " TotalImpuestosTrasladados=\"" + tags.TOTAL_IMP_TRA + "\" ";
					tags.atributoTotalImpuestosTras = true;
				}else{
					totalImpTraLine = " ElValorDelCampoTotalImpuestosTrasladadosDebeTenerHastaLaCantidadDeDecimalesQueSoporteLaMoneda=\"" + tags.TOTAL_IMP_TRA + "\" ";
					tags.atributoTotalImpuestosTras = false;
				}
			}else{
				tags.atributoTotalImpuestosTras = false;
			}
			
			return Util
					.conctatArguments(tmp, "\n<cfdi:Impuestos ",
							totalImpRetLine,
							totalImpTraLine,
							">").toString().getBytes("UTF-8");
			
//			return Util
//					.conctatArguments(tmp, "\n<cfdi:Impuestos ",
//							isNullEmpity(tokens[1], "TotalImpuestosRetenidos"),
//							isNullEmpity(tokens[2], "TotalImpuestosTrasladados"),
//							">").toString().getBytes("UTF-8");
		} 
		else 
		{	return formatCFD(numberLine);	}
	}

	/**
	 * 
	 * @param tokens
	 * @param lstIva
	 * @param numberLine
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] traslados(String tokens[], List<Iva> lstIva, long numberLine) 
		throws UnsupportedEncodingException 
	{
		if (tokens.length >= 5) 
		{
			if (tokens[1].trim().toUpperCase().equals("IVA")) 
			{
				tags.DESCRIPTION_TASA = "";
//				Integer tasa = new Integer(Util.getTASA(tokens[2]));
				Double tasa = Double.parseDouble(Util.getTASA(tokens[2]));
				Iva iva = null;
				for (Iva obj : lstIva) 
				{
					if (obj.getTasa().equals(tasa)) 
					{
						iva = obj;
						break;
					}
				}
	
				if (iva != null) 
				{
					tags.DESCRIPTION_TASA = iva.getDescripcion();
					if (tags.DESCRIPTION_TASA.length() > 0) 
					{	tags.isDescriptionTASA = true;	}
				}
			}
			
			System.out.println("Al parecer en facturas si viene Tasa AMDA : " + tokens[2].trim());
			// Elemento Traslados V3.3 AMDA
			String claveImp = "";
			String valTipoFactor = "Tasa"; // Por definir de donde tomar el valor AMDA
			String tasaOCuotaStr = "";
			String valImporteImpTras = "";
			String impuestoLine = "";
						
			if(tokens[1].trim().length() > 0){ // Validando el codigo del Impuesto
				System.out.println("Valor Impuesto Traslado AMDA : " + tokens[1].trim());
				claveImp = UtilCatalogos.findValClaveImpuesto(tags.mapCatalogos, tokens[1].trim());
				System.out.println("Valor Clave Impuesto Traslado AMDA : " + claveImp);
				if(tags.atributoTotalImpuestosTras){
					if(!claveImp.equalsIgnoreCase("vacio")){
						impuestoLine = " Impuesto=\"" + claveImp;
					}else{
						impuestoLine = " ElCampoImpuestoDeRetencionNoContieneUnValorDelCatalogoc_Impuesto=\"" + claveImp;
					}
				}else{
					impuestoLine = " DebeExistirElCampoTotalImpuestosTraslados=\"" + claveImp;
				}
			}
						
			String tasaOCuotaResult = "";
			if(valTipoFactor.equalsIgnoreCase("Tasa") || valTipoFactor.equalsIgnoreCase("Cuota")){
				System.out.println("Validacion TasaOCuota Traslado AMDA : " +tokens[1].trim() + " : " + valTipoFactor);
				if(!tokens[1].trim().equalsIgnoreCase("ISR")){

//					tasaOCuotaStr = "\" TasaOCuota=\""  + UtilCatalogos.findValMaxTasaOCuota(tags.mapCatalogos, tags.trasladoImpuestoVal, valTipoFactor);

//					tasaOCuotaStr = "\" TasaOCuota=\""  + Util.completeZeroDecimals(UtilCatalogos.findValMaxTasaOCuota(tags.mapCatalogos, tokens[1].trim(), valTipoFactor), 6);
					
					tasaOCuotaResult = UtilCatalogos.findValMaxTasaOCuotaTraslado(tags.mapCatalogos, tokens[1].trim(), valTipoFactor);
					if(!tasaOCuotaResult.equalsIgnoreCase("vacio")){
						tasaOCuotaStr = "\" TasaOCuota=\""  + Util.completeZeroDecimals(tasaOCuotaResult, 6);
					}else{
						tasaOCuotaStr = "\" ElValorSeleccionadoDebeCorresponderAUnValorDelCatalogoDondeLaColumnaImpuestoCorrespondaConElCampoImpuestoYLaColoumnaFactorCorrespondaAlCampoTipoFactor=\""  + tasaOCuotaResult;
					}

				}
							
				System.out.println("Valor TasaOCuota Traslado AMDA : " + tasaOCuotaStr);
			}
						
			if(valTipoFactor.equalsIgnoreCase("Tasa") || valTipoFactor.equalsIgnoreCase("Cuota")){
				System.out.println("Valor Importe AMDA T : " + tokens[3].trim() );
				if(tokens[3].trim().length() > 0){
					Double totImpTra = 0.00;
					Double importeDou = 0.00;
					Double valConepto = 0.00;
					if(claveImp.equalsIgnoreCase("001")){
						System.out.println("Valor Suma Impuesto Traslado ISR AMDA : " + tags.sumTraTotalIsr);
						if(tags.sumTraTotalIsr.trim().length() > 0){
							try{
								valConepto = valConepto + Double.parseDouble(tags.sumTraTotalIsr);
							}catch(NumberFormatException e){
								System.out.println("Valor Suma Impuesto Traslado ISR no es numerico al parecer AMDA : " + tags.sumTraTotalIsr);
							}
						}
					}else if(claveImp.equalsIgnoreCase("002")){
						System.out.println("Valor Suma Impuesto Traslado IVA AMDA : " + tags.sumTraTotalIva);
						if(tags.sumTraTotalIva.trim().length() > 0){
							try{
								valConepto = valConepto + Double.parseDouble(tags.sumTraTotalIva);
							}catch(NumberFormatException e){
								System.out.println("Valor Suma Impuesto Traslado IVA no es numerico al parecer AMDA : " + tags.sumTraTotalIva);
							}
						}
					}else if(claveImp.equalsIgnoreCase("003")){
						System.out.println("Valor Suma Impuesto Traslado IEPS AMDA : " + tags.sumTraTotalIeps);
						if(tags.sumTraTotalIeps.trim().length() > 0){
							try{
								valConepto = valConepto + Double.parseDouble(tags.sumTraTotalIeps);
							}catch(NumberFormatException e){
								System.out.println("Valor Suma Impuesto Traslado IEPS no es numerico al parecer AMDA : " + tags.sumTraTotalIeps);
							}
						}
					}
					
//					if(UtilCatalogos.decimalesValidationMsj(tokens[3].trim(), tags.decimalesMoneda)){
//						valImporteImpTras = "\" Importe=\"" +tokens[3].trim() + "\"";
//					}else{
//						valImporteImpTras = "\" ElValorDelCampoImporteCorrespondienteATrasladoDebeTenerLaCantidadDeDecimalesQueSoportaLaMoneda=\"" +tokens[3].trim() + "\"";
//					}
					boolean valid = false;
					try{
						totImpTra = Double.parseDouble(tags.TOTAL_IMP_TRA);
						importeDou = Double.parseDouble(tokens[3].trim());
						System.out.println("ValConcepto LLL: " + valConepto);
						System.out.println("importeDou LLL: " + importeDou);
						System.out.println("totImpTra LLL: " + totImpTra);
						double sumtotalTraDou = tags.sumTraTotalIepsDou + tags.sumTraTotalIvaDou + tags.sumTraTotalIsrDou;
						System.out.println("Valor SUMMMM1 Traslados : " + sumtotalTraDou);
						if(!(sumtotalTraDou > importeDou) && !(sumtotalTraDou < importeDou) ){ //!(valConepto > importeDou) && !(valConepto < importeDou)
							valid = true;
							System.out.println("Importes TRUE TASLADOS : ");
						}
						if(totImpTra > importeDou || totImpTra < importeDou){
							valImporteImpTras = "\" ElValorDelCampoTotalImpuestosTrasladoNoEsIgualALaSumaDeLosImportesRegistradosEnElElementoHijoTraslado=\"" + tokens[3].trim()+ "\" ";
						}else if(!valid){
							valImporteImpTras = "\" ErrImpTraImporte001=\"" + tokens[3].trim() + "\" ";
						}else{
							if(UtilCatalogos.decimalesValidationMsj(tokens[3].trim(), tags.decimalesMoneda)){
								valImporteImpTras = "\" Importe=\"" + tokens[3].trim()+ "\" ";
							}else{
								valImporteImpTras = "\" ElValorDelCampoImporteCorrespondienteATrasladoDebeTenerLaCantidadDeDecimalesQueSoportaLaMoneda=\"" + tokens[3].trim() + "\" ";
							}
						}
						
					}catch(NumberFormatException e){
						System.out.println("Total Importe Traslado Validando total AMDA T Error en Convertido a numerico : " + tags.TOTAL_IMP_TRA);
						valImporteImpTras = "\" ElValorDelCampoTotalImpuestosTrasladoNoEsIgualALaSumaDeLosImportesRegistradosEnElElementoHijoTraslado=\"" + tokens[3].trim() + "\" ";
					}
					
				}else{
					valImporteImpTras = "\"  ElValorDelCampoImporteCorrespondienteATrasladoDebeTenerLaCantidadDeDecimalesQueSoportaLaMoneda=\"" + tokens[3].trim() + "\"";
				}
				
			}
			
//			String elementTraslado = "\n<cfdi:Traslados>" + 
//					 "\n<cfdi:Traslado Impuesto=\"" + claveImp +
//					 "\" TipoFactor=\"" + valTipoFactor + // Por definir de donde tomar el valor AMDA
//					 tasaOCuotaStr +
//					 valImporteImpTras + // Por definir como se relaciona el importe 
//					 " />" +
//					 "\n</cfdi:Traslados>";
//			System.out.println("Elemento Traslado Impuestos AMDA : " + elementTraslado);
			
			return Util
					.conctatArguments(//"\n<cfdi:Traslados>" , 
							 "\n<cfdi:Traslado", impuestoLine ,
							 "\" TipoFactor=\"" , valTipoFactor , // Por definir de donde tomar el valor AMDA
							 tasaOCuotaStr ,
							 valImporteImpTras , // Por definir como se relaciona el importe 
							 " />" )
					.toString().getBytes("UTF-8");
			
//			return Util
//					.conctatArguments(//"\n<cfdi:Traslados>" , 
//							 "\n<cfdi:Traslado Impuesto=\"" , claveImp ,
//							 "\" TipoFactor=\"" , valTipoFactor , // Por definir de donde tomar el valor AMDA
//							 tasaOCuotaStr ,
//							 valImporteImpTras , // Por definir como se relaciona el importe 
//							 " />" )
//					.toString().getBytes("UTF-8");
			
//			return Util
//					.conctatArguments("\n<cfdi:Traslado impuesto=\"", tokens[1],
//							"\" tasa=\"", tokens[2], "\" importe=\"", tokens[3],
//							"\"/>").toString().getBytes("UTF-8");
		} 
		else 
		{	return formatCFD(numberLine);	}
	}

	/**
	 * 
	 * @param tokens
	 * @param numberLine
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] retenciones(String tokens[], long numberLine) 
		throws UnsupportedEncodingException 
	{
		if (tokens.length >= 4) 
		{
			
			// Elemento Retenciones V 3.3 AMDA
			String claveImpRet = "";
			String impuestoLine = "";
			if(tokens[1].trim().length() > 0){ // Validando el codigo del Impuesto
				System.out.println("Valor Impuesto Ret AMDA : " + tokens[1].trim());
				claveImpRet = UtilCatalogos.findValClaveImpuesto(tags.mapCatalogos, tokens[1].trim());
				System.out.println("Valor Clave Impuesto Ret AMDA : " + claveImpRet);
				if(!claveImpRet.equalsIgnoreCase("vacio")){
					impuestoLine = " Impuesto=\"" + claveImpRet;
				}else{
					impuestoLine = " ElCampoImpuestoDeRetencionNoContieneUnValorDelCatalogoc_Impuesto=\"" + claveImpRet;
				}
			}
			
			String importeLine = "";
			if(tags.atributoTotalImpuestosReten){
				if(tokens[2].trim().length() > 0){
//					if(UtilCatalogos.decimalesValidationMsj(tokens[2].trim(), tags.decimalesMoneda)){
//						importeLine = "\" Importe=\"" + tokens[2].trim();
//					}else{
//						importeLine = "\" ElValorDelCampoImporteCorrespondienteARetencionDebeTenerLaCantidadDeDecimalesQueSoportaLaMoneda=\"" + tokens[2].trim();
//					}
					
					Double totImpRet = 0.00;
					Double importeDou = 0.00;
					Double valConepto = 0.00;
					
					if(claveImpRet.equalsIgnoreCase("001")){
						System.out.println("Valor Suma Impuesto Retencion ISR AMDA : " + tags.sumRetTotalIsr);
						if(tags.sumRetTotalIsr.trim().length() > 0){
							try{
								valConepto = valConepto + Double.parseDouble(tags.sumRetTotalIsr);
							}catch(NumberFormatException e){
								System.out.println("Valor Suma Impuesto Retencion ISR no es numerico al parecer AMDA : " + tags.sumRetTotalIsr);
							}
						}
					}else if(claveImpRet.equalsIgnoreCase("002")){
						System.out.println("Valor Suma Impuesto Retencion IVA AMDA : " + tags.sumRetTotalIva);
						if(tags.sumRetTotalIva.trim().length() > 0){
							try{
								valConepto = valConepto + Double.parseDouble(tags.sumRetTotalIva);
							}catch(NumberFormatException e){
								System.out.println("Valor Suma Impuesto Retencion IVA no es numerico al parecer AMDA : " + tags.sumRetTotalIva);
							}
						}
					}else if(claveImpRet.equalsIgnoreCase("003")){
						System.out.println("Valor Suma Impuesto Retencion IEPS AMDA : " + tags.sumRetTotalIeps);
						if(tags.sumRetTotalIeps.trim().length() > 0){
							try{
								valConepto = valConepto + Double.parseDouble(tags.sumRetTotalIeps);
							}catch(NumberFormatException e){
								System.out.println("Valor Suma Impuesto Retencion IEPS no es numerico al parecer AMDA : " + tags.sumRetTotalIeps);
							}
						}
					}
					boolean valid = false;
					try{
						System.out.println("Valor Total Imp Retenidoss : " + tags.TOTAL_IMP_RET);
						totImpRet = Double.parseDouble(tags.TOTAL_IMP_RET);
						System.out.println("Valor Imp Retenidoss : " + tokens[2].trim());
						importeDou = Double.parseDouble(tokens[2].trim());
						System.out.println("Valor Imp Retenidoss : " + valConepto);
						if(valConepto > importeDou || valConepto < importeDou){ //(totImpRet > importeDou || totImpRet < importeDou)
							valid = true;
							System.out.println("Importes TRUE RETENCIONES : ");
						}
						if(totImpRet > importeDou || totImpRet < importeDou){
							importeLine = " ElValorDelCampoTotalImpuestosRetenidosDebeSerIgualALaSumaDeLosImportesRegistradosEnElElementoHijoRetencion=\"" + tokens[2].trim();
						}else if(!valid){
							importeLine = " ErrImpRetImporte001=\"" + tokens[2].trim();
						}else{
							if(UtilCatalogos.decimalesValidationMsj(tokens[2].trim(), tags.decimalesMoneda)){
								importeLine = " Importe=\"" + tokens[2].trim() ;
							}else{
								importeLine = " ElValorDelCampoImporteCorrespondienteARetencionDebeTenerLaCantidadDeDecimalesQueSoportaLaMoneda=\"" + tokens[2].trim();
							}
						}
						
					}catch(NumberFormatException e){
						System.out.println("Total Importe Traslado Validando total AMDA T Error en Convertido a numerico : " + tags.TOTAL_IMP_RET);
						importeLine = " ElValorDelCampoTotalImpuestosRetenidosDebeSerIgualALaSumaDeLosImportesRegistradosEnElElementoHijoRetencion=\"" + tokens[2].trim();
					}
					
				}else{
					importeLine = " ElValorDelCampoImporteCorrespondienteARetencionDebeTenerLaCantidadDeDecimalesQueSoportaLaMoneda=\"" + tokens[2].trim();
				}

			}else{
				importeLine = " DebeExistirElAtributoTotalImpuestosRetenidos=\"" + tokens[2].trim();
			}
			
			return Util
					.conctatArguments("\n<cfdi:Retencion", impuestoLine, 
							importeLine, "\"/>").toString().getBytes("UTF-8");
			
//			return Util
//					.conctatArguments("\n<cfdi:Retencion Impuesto=\"",
//							claveImpRet, "\" Importe=\"",
//							tokens[2].trim(), "\"/>").toString().getBytes("UTF-8");
			
//			return Util
//					.conctatArguments("\n<cfdi:Retencion impuesto=\"", tokens[1],
//							"\" importe=\"", tokens[2], "\"/>").toString()
//					.getBytes("UTF-8");
		} 
		else 
		{	return formatCFD(numberLine);	}
	}

	/**
	 * 
	 * @param tokens
	 * @param numberLine
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] domicilio(String tokens[], long numberLine) 
		throws UnsupportedEncodingException 
	{
		if (tokens.length >= 12) 
		{
			tags._Calle = " calle=\"" + convierte(tokens[1]) + "\" ";
			tags._NoExterior = isNullEmpity(tokens[2], " noExterior");
			tags._NoInterior = isNullEmpity(tokens[3], " noInterior");
			tags._Colonia = isNullEmpity(tokens[4], " colonia");
			tags._Localidad = isNullEmpity(tokens[5], " localidad");
			tags._Referencia = isNullEmpity(tokens[6], " referencia");
			tags._Municipio = isNullEmpity(tokens[7], " municipio");
			tags._Estado = isNullEmpity(tokens[8], " estado");
			tags._Pais = " pais=\"" + convierte(tokens[9]) + "\"";
			tags._CodigoPostal = tokens.length >= 11 ? isNullEmpity(tokens[10],
					" codigoPostal") : "";
			tags("", pila).toString();
//			return Util
//					.conctatArguments("\n<cfdi:Domicilio ", tags._Calle,
//							tags._NoExterior, tags._NoInterior, tags._Colonia,
//							tags._Localidad, tags._Referencia, tags._Municipio,
//							tags._Estado, tags._Pais, tags._CodigoPostal, " />",
//							tags("", pila)).toString().getBytes("UTF-8");
			return "".getBytes("UTF-8");
		} 
		else 
		{	return formatCFD(numberLine);	}
	}

	/**
	 * 
	 * @param tokens
	 * @param date
	 * @param numberLine
	 * @return
	 * @throws ParseException
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] infoAduanera(String[] tokens, Date date, long numberLine)
			throws ParseException, UnsupportedEncodingException 
	{
		if (tokens.length >= 5) 
		{
			AddendumCustoms customs = new AddendumCustoms();
			customs.setAuthor("masivo");
			customs.setCreationDate(date);
			customs.setDateCustoms(Util.convertirString(tokens[2]));
			customs.setNameOfCustoms(tokens[3]);
			customs.setPedimento(tokens[1]);
			if (this.tags.isParte) 
			{	customs.setSource("IP");	}
			else 
			{	customs.setSource("I");	}
			tags.lstCustoms.add(customs);
			return Util
					.conctatArguments("\n<cfdi:InformacionAduanera numero=\"",
							tokens[1], "\"", " fecha=\"", tokens[2], "\" ",
							" aduana=\"", tokens[3], "\"/>").toString().getBytes("UTF-8");
		} else {
			return formatCFD(numberLine);
		}
	}

	/**
	 * 
	 * @param tokens
	 * @param numberLine
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] parte(String[] tokens, long numberLine) 
		throws UnsupportedEncodingException 
	{
		if (tokens.length >= 8) 
		{
			tmp = "";
			if (tags.isParte) 
			{	tmp = "\n</cfdi:Parte>";	}
			if (!tags.isParte) 
			{	tags.isParte = true;	}
	
			return Util
					.conctatArguments(tmp, "\n<cfdi:Parte cantidad=\"", tokens[1],
							"\" unidad=\"", tokens[2], "\" ",
							" noIdentificacion=\"", tokens[3],
							"\" " + "descripcion=\"", convierte(tokens[4]), "\" ",
							" valorUnitario=\"", tokens[5], "\" ", " importe=\"",
							tokens[6], "\">").toString().getBytes("UTF-8");
		} 
		else 
		{	return formatCFD(numberLine);	}
	}

	/**
	 * 
	 * @param tokens
	 * @param numberLine
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] predial(String[] tokens, long numberLine) 
		throws UnsupportedEncodingException 
	{
		if (tokens.length >= 3) 
		{
			return Util
					.conctatArguments("\n<cfdi:CuentaPredial numero=\"", tokens[1],
							"\" />").toString().getBytes("UTF-8");
		} 
		else 
		{	return formatCFD(numberLine);	}
	}

	/**
	 * 
	 * @param tokens
	 * @param numberLine
	 * @return
	 */
	public byte[] complementoConcepto(String[] tokens, long numberLine) 
	{	return null;	}

	/**
	 * 
	 * @param tokens
	 * @param numberLine
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] factoraje(String[] tokens, long numberLine) 
		throws UnsupportedEncodingException 
	{
		if (tokens.length >= 13) 
		{
			return Util
				.conctatArguments("\n<as:InformacionFactoraje ",
						Util.isNullEmpity(tokens[1], "deudorProveedor"),
						Util.isNullEmpity(tokens[2], "tipoDocumento"),
						Util.isNullEmpity(tokens[3], "numeroDocumento"),
						Util.isNullEmpity(tokens[4], "fechaVencimiento"),
						Util.isNullEmpity(tokens[5], "plazo"),
						Util.isNullEmpity(tokens[6], "valorNominal"),
						Util.isNullEmpity(tokens[7], "aforo"),
						Util.isNullEmpity(tokens[8], "precioBase"),
						Util.isNullEmpity(tokens[9], "tasaDescuento"),
						Util.isNullEmpity(tokens[10], "precioFactoraje"),
						Util.isNullEmpity(tokens[11], "importeDescuento"),
						">\n</as:InformacionFactoraje>").toString().getBytes("UTF-8");
		} 
		else 
		{	return formatCFD(numberLine);	}
	}

	/**
	 * Consume el folio
	 * 
	 * @param serie
	 * @param idEntityFiscal
	 * @return
	 */
	public FolioRange folioActivo(String serie, long idEntityFiscal) 
	{
		boolean flagOcupados = false;
		FolioRange p_folioRange = null;
		
		logger.info("Serie de la entidad Fiscal: "+serie);
		logger.info("Entidad Fiscal ID: "+idEntityFiscal);
		for (FolioRange objF : folioRangeManager.listarActivos(serie, idEntityFiscal)) 
		{
			Integer p_folio = objF.getActualFolio();
			tags.NUM_APROBACION = objF.getAuthorizationNumber().toString();
			tags.YEAR_APROBACION = String.valueOf(objF.getYearOfAuthorization());
			p_folioRange = objF;
			p_folioRange.setActualFolio(p_folio + 1);
			if (p_folioRange.getActualFolio() == p_folioRange.getFinalFolio()) 
			{
				p_folioRange.setEstatus("OCUPADOS");
				flagOcupados = true;
			}
		}

		if (flagOcupados) 
		{
			int folioI = Integer.parseInt(folio);
			for (FolioRange objF : folioRangeManager.listar(serie,
					idEntityFiscal)) {
				if (objF.getActualFolio() - folioI == 1) 
				{
					objF.setEstatus("ACTIVO");
					folioRangeManager.update(objF);
				}
			}
		}

		return p_folioRange;
	}
	
	public void loadInfoV33(String linea) 
	{
		String[] tokens = linea.concat("|temp").split("\\|");
		System.out.println("entra LoadInfoV33: "+linea);
//		System.out.println("entra LoadInfoV33 Element: "+tokens[0]);
		String[] lin = linea.split("\\|");
		if(lin.length > 1){
			
			if (tokens[0].equals(tags._CONTROLCFD)){
				// Set

			} else if (tokens[0].equals(tags._CFD)) {
				// Comprobante

			} else if (tokens[0].equals(tags._EMISOR)) {
				// Emisor

			} else if (tokens[0].equals(tags._RECEPTOR)) {
				// Receptor
				System.out.println("entra LoadInfoV33 Receptor:" + lin[1].trim() );
			} else if (tokens[0].equals(tags._DOMICILIO)) {
				// Domicilio
				System.out.println("entra LoadInfoV33 Domicilio:" + lin[9].trim() );
				tags.recepPais = lin[9].trim();

			} else if (tokens[0].equals(tags._CONCEPTO)) {
				// Concepto

			} else if (tokens[0].equals(tags._IMPUESTOS)) {
				// Impuestos

			} else if (tokens[0].equals(tags._RETENCION)) {
				// Retenciones
				System.out.println("entra LoadInfoV33 Retenciones Back: "+ lin[1].trim() + " : " + lin[2].trim());
//				tags.retencionImpuestoVal = lineas[1].trim();
//				tags.retencionImporteVal = lineas[2].trim(); // Se comenta por que al parecer se recorro uno despues AMDA
				System.out.println("entra LoadInfoV33 Retenciones: "+ lin[1].trim() + " : " + lin[2].trim());
				tags.retencionImpuestoVal = lin[1].trim();
				
				if(lin[2].trim().equalsIgnoreCase("0.00")){
					tags.retencionImporteVal = "0.00";
				}else{
					tags.retencionImporteVal = lin[2].trim();
				}

			} else if (tokens[0].equals(tags._TRASLADO)) {
				// Traslados
//				System.out.println("entra LoadInfoV33 Traslados: "+ lineas[1].trim() + " : " + lineas[2].trim() + " : " + lineas[3].trim());
//				tags.trasladoImpuestoVal = lineas[1].trim();
//				tags.trasladoTasaVal = lineas[2].trim();
//				tags.trasladoImporteVal = lineas[3].trim(); // Se comenta por que al parecer se recorro uno despues AMDA
				System.out.println("entra LoadInfoV33 Traslados: "+ lin[1].trim() + " : " + lin[2].trim() + " : " + lin[3].trim());
				tags.trasladoImpuestoVal = lin[1].trim();
				tags.trasladoTasaVal = lin[2].trim();
				if(lin[3].trim().equalsIgnoreCase("0.00")){
					tags.trasladoImporteVal = "0.00";
				}else{
					tags.trasladoImporteVal = lin[3].trim();
				}
//				valImporteTraslado = lin[3].trim();
				System.out.println("Sale LoadInfoV33 Traslados:" + tags.trasladoImporteVal);			
				
//				valImporteRetencion = lin[2].trim();
				System.out.println("Sale LoadInfoV33 Retencion:" + tags.retencionImporteVal);

			} else if (tokens[0].equals(tags._FACTORAJE)) {
				// -
//				System.out.println("entra LoadInfoV33 Traslados: "+ lin[1].trim() + " : " + lin[2].trim() + " : " + lin[3].trim());
//				tags.trasladoImpuestoVal = lin[1].trim();
//				tags.trasladoTasaVal = lin[2].trim();
//				if(lin[3].trim().equalsIgnoreCase("0.00")){
//					tags.trasladoImporteVal = "0.00";
//				}else{
//					tags.trasladoImporteVal = lin[3].trim();
//				}
////				valImporteTraslado = lin[3].trim();
//				System.out.println("Sale LoadInfoV33 Traslados:" + tags.trasladoImporteVal);
			}
			
		}// Validando split Termina
		
	}

	public TagsXML getTags() 
	{	return tags;	}

	public void setTags(TagsXML tags) 
	{	this.tags = tags;	}

	public Stack<String> getPila() 
	{	return pila;	}

	public void setPila(Stack<String> pila) 
	{	this.pila = pila;	}

	public String getFolio() 
	{	return folio;	}

	public void setFolio(String folio) 
	{	this.folio = folio;	}

	public FolioRangeManager getFolioRangeManager() 
	{	return folioRangeManager;	}

	public void setFolioRangeManager(FolioRangeManager folioRangeManager) 
	{	this.folioRangeManager = folioRangeManager;		}

	public List<String> getDescriptionFormat() 
	{	return descriptionFormat;	}

	public void setDescriptionFormat(List<String> descriptionFormat) 
	{	this.descriptionFormat = descriptionFormat;		}
	
}
