package com.interfactura.firmalocal.xml.ecb;

import static com.interfactura.firmalocal.xml.util.Util.isNullEmpity;
import static com.interfactura.firmalocal.xml.util.Util.tags;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.interfactura.firmalocal.domain.entities.FiscalEntity;
import com.interfactura.firmalocal.xml.Properties;
import com.interfactura.firmalocal.xml.TagsXML;
import com.interfactura.firmalocal.xml.util.Util;
import com.interfactura.firmalocal.xml.util.NombreAplicativo;
import com.interfactura.firmalocal.xml.util.UtilCatalogos;

/**
 * Clase que se encargara de convertir las lineas a XML
 * 
 * @author jose luis
 * 
 */
@Component
public class ConvertirV3_3 
{

	private Logger logger = Logger.getLogger(ConvertirV3_3.class);

	@Autowired
	private TagsXML tags;
	private Stack<String> pila;
	//private StringBuilder concat;
	private StringBuffer  concat;
	private List<String> descriptionFormat;
	private String[] lineas;
	@Autowired
	private Properties properties;
	
	private List<StringBuffer> lstMovimientosECB = new ArrayList<StringBuffer>();
	
	//AMDA Pruebas V3.3
	private String valImporteRetencion;
	private String valImporteTraslado;
	
	public void set(String linea, long numberLine, String fileNames, HashMap<String, String> hashApps, String numeroMalla) 
	{
		lineas = linea.split("\\|");
		logger.debug("Iniciando parseo de lineas");
		clearFlag();
		pila = new Stack<String>();
		
		/*if(fileNames.trim().equals("CFDREPROCESOECB")){
			//Solo la malla de Reproceso ECB
			if (lineas.length >= 10) 
			{	System.out.println("entra If");
				tags.NUM_CTE = lineas[1];
				tags.NUM_CTA = lineas[2];
				tags.EMISION_PERIODO = lineas[3];
				tags.NUM_TARJETA = lineas[4];
				tags.TOTAL_MN = lineas[5].trim();
				tags.IVA_TOTAL_MN = lineas[6].trim();
				tags.LONGITUD = lineas[7].trim();
				tags.TIPO_FORMATO = lineas[8].trim();
				
				boolean fDigitOK = true;
				String tipoCambio = lineas[9].trim();
				if(!tipoCambio.equals("")){
					String[] fileNamesArr = tipoCambio.split("\\.");
					System.out.println("lineas[9].trim():" + lineas[9].trim());
					System.out.println("fileNamesArr:" + fileNamesArr);
					for (int i=0; i < fileNamesArr.length; i++)
					{							
						if(i==0){
							//parte entera
							if(fileNamesArr[i].length()==0 || fileNamesArr[i].length()>2){
								fDigitOK = false;
								break;
							}
						}else if(i==1){
							//parte decimal
							if(fileNamesArr[i].length()>4){
								fDigitOK = false;
								break;
							}
						}else if(i>1){
							fDigitOK = false;
							break;
						}
					}
					if(fDigitOK){
						tags.TIPO_CAMBIO = lineas[9].trim();	
					}else{
						tags.TIPO_CAMBIO="TIPODECAMBIO_INCORRECTO";
					}
				}else{
					tags.TIPO_CAMBIO = "";
				}
												
				if ((lineas.length >= 11) && (lineas[10] != null) && (lineas[10].length() > 0) && (!lineas[10].trim().equals("temp"))){
											
					tags.NOMBRE_APP_REPECB = lineas[10].trim();
					
				}else{
					//No se informa el nombre de aplicativo al que pertenece el comprobante
					tags.NOMBRE_APP_REPECB = "";
				}
				System.out.println("tags.TIPO_CAMBIO:" + tags.TIPO_CAMBIO);
				System.out.println("tags.NOMBRE_APP_REPECB:" + tags.NOMBRE_APP_REPECB);
			} 
			else 
			{	System.out.println("entra else");formatECB(numberLine);	}
		}else{*/
			//Cualquier malla de Estados de Cuenta (excepto Reproceso ECB)
			if (lineas.length >= 9) 
			{	System.out.println("entra If");
				tags.NUM_CTE = lineas[1];
				tags.NUM_CTA = lineas[2];
				tags.EMISION_PERIODO = lineas[3];
				tags.NUM_TARJETA = lineas[4];
				tags.TOTAL_MN = lineas[5].trim();
				tags.IVA_TOTAL_MN = lineas[6].trim();
				tags.LONGITUD = lineas[7].trim();
				tags.TIPO_FORMATO = lineas[8].trim();
				// Se llena Tag con los Valores de Archivo XLS AMDA
				System.out.println("Antes de leer Archivo XLS Convertir");
				tags.mapCatalogos = Util.readXLSFile(properties.getUrlArchivoCatalogs());
				System.out.println("Despues de leer Archivo XLS Convertir: "+ tags.mapCatalogos.size());
				tags.mapCatalogos = UtilCatalogos.arregloConceptos(tags.mapCatalogos);
				System.out.println("Despues de Arrego Conceptos: "+ tags.mapCatalogos.size());
				if ((lineas.length >= 10) && (lineas[9] != null) && (lineas[9].length() > 0) && (!lineas[9].trim().equals("temp"))){				
					boolean fDigitOK = true;
					String tipoCambio = lineas[9].trim();
					String[] fileNamesArr = tipoCambio.split("\\.");
					System.out.println("lineas[9].trim():" + lineas[9].trim());
					System.out.println("fileNamesArr:" + fileNamesArr);
					for (int i=0; i < fileNamesArr.length; i++)
					{		
						
						if(i==0){
							//parte entera
							if(fileNamesArr[i].length()==0 || fileNamesArr[i].length()>2){
								fDigitOK = false;
								break;
							}
						}else if(i==1){
							//parte decimal
							if(fileNamesArr[i].length()>4){
								fDigitOK = false;
								break;
							}
						}else if(i>1){
							fDigitOK = false;
							break;
						}
					}
					if(fDigitOK){
						tags.TIPO_CAMBIO = lineas[9].trim();	
					}else{
						tags.TIPO_CAMBIO="TIPODECAMBIO_INCORRECTO";
					}			
									
				}else{
					tags.TIPO_CAMBIO = "";
				}
												
				tags.NOMBRE_APP_REPECB = NombreAplicativo.obtieneNombreApp(hashApps, fileNames, numeroMalla); 
						
				System.out.println("tags.TIPO_CAMBIO:" + tags.TIPO_CAMBIO);
				System.out.println("tags.NOMBRE_APP_REPECB:" + tags.NOMBRE_APP_REPECB);
			} 
			else 
			{	System.out.println("entra else");formatECB(numberLine);	}
		//}
		
		
	}

	/**
	 * Reinicia las Banderas
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

	//24 de Abril 2013 Verificar si una cadena es numérica
	public boolean isNotNumeric(String strNumber){
		boolean fNotNumber = true; // Cambio de validacion antes False AMDA version 3.3
		int i=0;
//		while(!fNotNumber && i < strNumber.length()){
//			try{
//				Integer.parseInt(Character.toString(strNumber.charAt(i)));				
//			}catch(NumberFormatException ex){
//				fNotNumber = true;
//				break;
//			}
//			i++;
//		};
		if (strNumber.matches("([A-Z]|[a-z]|[0-9]| |Ñ|ñ|!|&quot;|%|&amp;|&apos;|´|-|:|;|&gt;|=|&lt;|@|_|,|\\{|\\}|`|~|á|é|í|ó|ú|Á|É|Í|Ó|Ú|ü |Ü){1,40}")) {
			fNotNumber = false;			
		}
		return fNotNumber;		
	}
	
	/**
	 * Genera el TAG 'Comprobante' que tiene la siguiente estructura:
	 * 
	 * @param linea
	 * @param numberLine
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] fComprobante(String linea, long numberLine, HashMap<String, HashMap> tipoCambio, HashMap fiscalEntities, HashMap campos22, String fileNames) 
		throws UnsupportedEncodingException 
	{
		
			
		
			lineas = linea.split("\\|");
			System.out.println("linea " + linea);
			//System.out.println("lineas[9]" + lineas[9]);
			HashMap map = (HashMap) campos22.get(tags.EMISION_RFC);
			if(map != null){
				if(map.get("codPostal") != null){
					tags._CodigoPostal = map.get("codPostal").toString();
				}else{
					tags._CodigoPostal = "01219";
				}
				
			}
//			System.out.println("ZP " + map.get("codPostal").toString());
			
			if (lineas.length >= 8) 
			{
				
				concat = new StringBuffer();
				
				tags.CFD_TYPE = lineas[1].trim().toUpperCase();
				
				tags.SERIE_FISCAL_CFD = lineas[3].trim();
				String valEqMoneda = ""; // AMDA Version 3.3
				if (!Util.isNullEmpty(tags.SERIE_FISCAL_CFD)) 
				{
					
					if(tags.SERIE_FISCAL_CFD.length() >= 3){
						if(tags.SERIE_FISCAL_CFD.substring(0, 3).trim().equals("MXN") || 
							tags.SERIE_FISCAL_CFD.substring(0, 3).trim().equals("USD") || 
							tags.SERIE_FISCAL_CFD.substring(0, 3).trim().equals("EUR") || 
							tags.SERIE_FISCAL_CFD.substring(0, 3).trim().equals("UDI")){
							concat.append(" Serie=\"" + tags.SERIE_FISCAL_CFD + "\"");	
							concat.append(" Moneda=\"" + tags.SERIE_FISCAL_CFD.substring(0, 3).trim() + "\"");	
							tags.TIPO_MONEDA = tags.SERIE_FISCAL_CFD.substring(0, 3).trim();
						}else if(tags.SERIE_FISCAL_CFD.substring(0, 3).trim().equals("BME") && fileNames.equals("CFDLFFONDOS")){
							concat.append(" Serie=\"" + tags.SERIE_FISCAL_CFD + "\"");	
							concat.append(" Moneda=\"" + "MXN" + "\"");	
							tags.TIPO_MONEDA = "MXN";
						}else{
							//tags.SERIE_FISCAL_CFD="MONEDA INCORRECTA " + tags.SERIE_FISCAL_CFD + "";
							
							if(tags.SERIE_FISCAL_CFD.trim() != ""){ // Validacion Moneda Equivalencia AMDA V 3.3
								valEqMoneda = UtilCatalogos.findEquivalenciaMoneda(tags.mapCatalogos, tags.SERIE_FISCAL_CFD);
								concat.append(" Moneda=\"" + valEqMoneda + "\"");
								tags.TIPO_MONEDA = valEqMoneda;
								
							}else{
								tags.TIPO_MONEDA = tags.SERIE_FISCAL_CFD;
								concat.append(" MonedaIncorrecta" + tags.TIPO_MONEDA + "=\"" + tags.SERIE_FISCAL_CFD + "\"");
							}
						}
					}else if(tags.SERIE_FISCAL_CFD.trim() == ""){
						concat.append(" Serie=\"" + tags.SERIE_FISCAL_CFD + "\"");	
						concat.append(" Moneda=\"" + tags.SERIE_FISCAL_CFD + "\"");
						tags.TIPO_MONEDA = tags.SERIE_FISCAL_CFD;
					}else{
						//tags.SERIE_FISCAL_CFD="MONEDA INCORRECTA " + tags.SERIE_FISCAL_CFD + "";
						if(tags.SERIE_FISCAL_CFD.trim() != ""){ // Validacion Moneda Equivalencia AMDA V 3.3
							valEqMoneda = UtilCatalogos.findEquivalenciaMoneda(tags.mapCatalogos, tags.SERIE_FISCAL_CFD);
							concat.append(" Moneda=\"" + valEqMoneda + "\"");
							tags.TIPO_MONEDA = valEqMoneda;
						}else{
							tags.TIPO_MONEDA = tags.SERIE_FISCAL_CFD;
							concat.append(" MonedaIncorrecta" + tags.TIPO_MONEDA + "=\"" + tags.SERIE_FISCAL_CFD + "\"");
						}
					}
					
					// Validando decimales soportados por el tipo de moneda AMDA V 3.3
					if(tags.TIPO_MONEDA.trim().length() > 0 ){
						tags.decimalesMoneda = UtilCatalogos.findDecimalesMoneda(tags.mapCatalogos, tags.TIPO_MONEDA);
						System.out.println("Decimales moneda: " + tags.decimalesMoneda);
//						if(tags.decimalesMoneda.contains(".")){
//							String deci[] = tags.decimalesMoneda.split("\\.");
//							tags.decimalesMoneda = deci[1];
//						}
					}
						
					//Validando tipo de Cambio AMDA 
								
					if(fileNames.equals("CFDLFFONDOS")){
						if(!tags.TIPO_CAMBIO.isEmpty()){//////condición de prueba"""""""""""""""
							//HashMap<String, String> monedas = (HashMap<String, String>) tipoCambio.get(tags.EMISION_PERIODO);
							System.out.println("tags.TIPO_CAMBIO: " + tags.TIPO_CAMBIO);
							if (!tags.TIPO_CAMBIO.trim().equals("TIPODECAMBIO_INCORRECTO"))
							{
								if(!tags.TIPO_MONEDA.equalsIgnoreCase("MXN") && !tags.TIPO_MONEDA.equalsIgnoreCase("XXX")){ //Validacion AMDA
									Double valMinimo = 0.000001;
									try{
										
										double value = Double.parseDouble(tags.TIPO_CAMBIO);
									    if(value >= valMinimo){
									       System.out.println("Tipo Cambio mayor");
									       concat.append(" TipoCambio=\"" + tags.TIPO_CAMBIO + "\"");
									    }else{
									    	System.out.println("Tipo Cambio menor");
									    	concat.append(" TipoCambioMinimoRequerido" + tags.TIPO_CAMBIO + "=\"" + tags.TIPO_CAMBIO + "\"");
									    }
										
									}catch (NumberFormatException e){
										concat.append(" TipoCambioIncorrecto" + tags.TIPO_CAMBIO + "=\"" + tags.TIPO_CAMBIO + "\"");
									}
									
								}else{
									concat.append(" TipoCambio=\"" + tags.TIPO_CAMBIO + "\"");
								} // Termina Validacion AMDA
													
							}
							else{
								//tags.TIPO_CAMBIO="TIPO_CAMBIO INCORRECTO " + tags.TIPO_CAMBIO + "";
								concat.append(" TipoCambioIncorrecto" + tags.TIPO_CAMBIO + "=\"" + tags.TIPO_CAMBIO + "\"");
							}							
						}else{
							tags.TIPO_CAMBIO = "1.0000";
							concat.append(" TipoCambio=\"" + tags.TIPO_CAMBIO + "\"");
						}							
					}else{
						//HashMap<String, String> monedas = (HashMap<String, String>) tipoCambio.get(tags.EMISION_PERIODO);
						System.out.println("tags.TIPO_CAMBIO: " + tags.TIPO_CAMBIO);
						if (tags.TIPO_CAMBIO.length() > 0 && !tags.TIPO_CAMBIO.trim().equals("TIPODECAMBIO_INCORRECTO"))
						{
							if(!tags.TIPO_MONEDA.equalsIgnoreCase("MXN") && !tags.TIPO_MONEDA.equalsIgnoreCase("XXX")){ //Validacion AMDA
								Double valMinimo = 0.000001;
								try{
									
									double value = Double.parseDouble(tags.TIPO_CAMBIO);
								    if(value >= valMinimo){
								       System.out.println("Tipo Cambio mayor");
								       concat.append(" TipoCambio=\"" + tags.TIPO_CAMBIO + "\"");
								    }else{
								    	System.out.println("Tipo Cambio menor");
								    	concat.append(" TipoCambioMinimoRequerido" + tags.TIPO_CAMBIO + "=\"" + tags.TIPO_CAMBIO + "\"");
								    }
									
								}catch (NumberFormatException e){
									concat.append(" TipoCambioIncorrecto" + tags.TIPO_CAMBIO + "=\"" + tags.TIPO_CAMBIO + "\"");
								}
								
							}else{
								concat.append(" TipoCambio=\"" + tags.TIPO_CAMBIO + "\"");
							} // Termina Validacion AMDA					
						}
						else if(tags.TIPO_CAMBIO.trim().equals("TIPODECAMBIO_INCORRECTO")){
							//tags.TIPO_CAMBIO="TIPO_CAMBIO INCORRECTO " + tags.TIPO_CAMBIO + "";
							concat.append(" TipoCambioIncorrecto" + tags.TIPO_CAMBIO + "=\"" + tags.TIPO_CAMBIO + "\"");
						}
						else
						{
							//tags.TIPO_CAMBIO="NO EXISTEN TIPOS DE CAMBIO PARA EL DIA " + tags.EMISION_PERIODO + "";
							concat.append(" TipoCambioNoDefinido" + tags.TIPO_CAMBIO + "=\"" + tags.TIPO_CAMBIO + "\"");
						}
					}				
				}
				else
				{
					tags.TIPO_CAMBIO="";
					tags.TIPO_MONEDA="";
				}
							
				if(!isNotNumeric(lineas[4].trim()) && lineas[4].trim().length() <= 40){	
					tags.FOLIO_FISCAL_CFD=lineas[4].trim();
					concat.append(" Folio=\"" + tags.FOLIO_FISCAL_CFD + "\"");
				}else{
					tags.FOLIO_FISCAL_CFD=lineas[4].trim();
					concat.append(" CuentaIncorrecta" + tags.FOLIO_FISCAL_CFD + "=\"" + tags.FOLIO_FISCAL_CFD + "\"");
				}
				
				
				//System.out.println("antesCalendar ");
				
				tags.FECHA_CFD = Util.convertirFecha(Calendar.getInstance().getTime());
						
				//System.out.println("despuesCalendar " + tags.FECHA_CFD );
				
				System.out.println("tags.EMISION_PERIODO " + tags.EMISION_PERIODO);
				System.out.println("FECHA_CFD " + tags.FECHA_CFD);
				//concat.append(" fecha=\"" + Util.convertirFecha(tags.EMISION_PERIODO, tags.FECHA_CFD) + "\"");
				concat.append(" Fecha=\"" + tags.FECHA_CFD + "\"");
				
				System.out.println("SubTotal : " + lineas[6].trim());
				tags.SUBTOTAL_MN = lineas[6].trim();
				if(lineas[1].trim().toUpperCase().equalsIgnoreCase("T") && lineas[1].trim().toUpperCase().equalsIgnoreCase("P")){
					concat.append(" SubTotal=\"" + "0" + "\"");
				}else{
					concat.append(" SubTotal=\"" + tags.SUBTOTAL_MN + "\"");
				}
				
				// Validacion de no ser negativo el total AMDA
				if(lineas[7] != null){
					if(lineas[1].trim().toUpperCase().equalsIgnoreCase("T") || lineas[1].trim().toUpperCase().equalsIgnoreCase("P")){
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
						System.out.println("Total : " + lineas[7].trim());
						try {
						    double valTotal = Double.parseDouble(lineas[7].trim());
						    if(valTotal<0){
						       System.out.println("Total: " + " es negativo");
						    	concat.append(" totalIncorrecto"+ valTotal + "=\"" + lineas[7].trim() + "\"");
						    }else{
						       System.out.println("Total: " + " es positivo");
						       concat.append(" Total=\"" + lineas[7].trim() + "\"");
						       
						       //Validando Monto Maximo
						       try{
						    	   double valMaximo = Double.parseDouble(UtilCatalogos.findTipoComprobante(tags.mapCatalogos, lineas[1].trim().toUpperCase()));
						    	   if(valTotal > valMaximo){
						    		   // Se debe mostrar campo Confirmacion, no se sabe si aplica para Estados de Cuenta o Factoraje, esta por definir por parte de Santander
						    	   }
						       }catch (NumberFormatException e){
						    	   System.out.println("No se encontro Valor Maximo en Catalogo TipoComprobante ");
						       }
						      
						       
						    }
						} catch (NumberFormatException e) {
						    System.out.println("Total: "+  "No es un numero");
						    concat.append(" totalIncorrecto"+  "=\"" + lineas[7].trim() + "\"");
						}
					}
					
//					concat.append(" total=\"" + lineas[7].trim() + "\"");
				}else{
					concat.append(" totalIncorrecto"+  "=\"" + lineas[7].trim() + "\"");
				}
				
				
				//Doble Sellado
				System.out.println("TIPO DE COMPROBANTE PRUEBA: " + lineas[1].trim().toUpperCase());
				System.out.println("TAG CATALOGOS PRUEBA: " + tags.mapCatalogos.size());
				System.out.println("Respuesta a funcion de busqueda de TIPOCOMPROBANTE: "+UtilCatalogos.findTipoComprobante(tags.mapCatalogos, "I"));
				// Se valida por medio de un catalogo AMDA
				if(UtilCatalogos.findTipoComprobante(tags.mapCatalogos, lineas[1].trim().toUpperCase()).equals("tipoDeComprobanteIncorrecto")){
					tags.tipoComprobante = "tipoDeComprobanteIncorrecto";
					concat.append(" tipoDeComprobanteIncorrecto" + UtilCatalogos.findTipoComprobante(tags.mapCatalogos, lineas[1].trim().toUpperCase()) + "=\"" + lineas[1].trim().toUpperCase() + "\" ");
				}else{
					System.out.println("TIPO DE COMPROBANTE: " + lineas[1].trim().toUpperCase());
					tags.tipoComprobante = UtilCatalogos.findTipoComprobante(tags.mapCatalogos, lineas[1].trim().toUpperCase());
					concat.append(" TipoDeComprobante=\""
							+ UtilCatalogos.findTipoComprobante(tags.mapCatalogos, lineas[1].trim().toUpperCase()) + "\" ");					
				}
				
				// Validacion para el campo condicionesDePago AMDA
//				if(!lineas[1].trim().toUpperCase().equalsIgnoreCase("T") && !lineas[1].trim().toUpperCase().equalsIgnoreCase("P") && !lineas[1].trim().toUpperCase().equalsIgnoreCase("N")){
//					String valorCondicionDePago = " "; // Valor fijo por el momento
//					if(valorCondicionDePago.length() <= 100){
//						concat.append(" CondicionesDePago=\""
//								+ valorCondicionDePago + "\" ");
//					}else{
//						concat.append(" CondicionesDePago=\""
//								+ "valorCondicionesDePagoIncorrecto" + valorCondicionDePago + "\" ");
//					}					
//				}
				// Validacion para el campo descuento AMDA
//				if(!lineas[1].trim().toUpperCase().equalsIgnoreCase("T") && !lineas[1].trim().toUpperCase().equalsIgnoreCase("P")){
//					concat.append(" descuento=\""
//							+ "0.00" + "\" ");
//				}
				// Validacion para el campo Forma de Pago y Metodo de Pago AMDA
				if(!lineas[1].trim().toUpperCase().equalsIgnoreCase("T") && !lineas[1].trim().toUpperCase().equalsIgnoreCase("P")){
					concat.append(" FormaPago=\""
							+ "03" + "\" "); // Antes PAGO EN UNA SOLA EXHIBICION AMDA V 3.3
					concat.append(" MetodoPago=\""
							+  "PUE" + "\" "); // Antes properties.getLabelMetodoPago() AMDA V 3.3
				}
				
				tags.NUM_APROBACION = tags.EMISION_PERIODO.split("-")[0];
				tags.YEAR_APROBACION = tags.NUM_APROBACION;
				
				
				/*
				return Util
						.conctatArguments(
								"\n<cfdi:Comprobante version=\"3.2\" ",
								concat.toString(),
								"xmlns:ecb=\"http://www.sat.gob.mx/ecb\" ",
								"xmlns:cfdi=\"http://www.sat.gob.mx/cfd/3\"  ",								
								"xsi:schemaLocation=\"http://www.sat.gob.mx/cfd/3 ",
								"http://www.sat.gob.mx/sitio_internet/cfd/3/cfdv32.xsd ",
								"http://www.sat.gob.mx/ecb http://www.sat.gob.mx/sitio_internet/cfd/ecb/ecb.xsd\" ",
								"sello=\"", properties.getLabelSELLO(), "\" ",
								"noCertificado=\"",
								properties.getLblNO_CERTIFICADO(), "\" ",
								"certificado=\"", properties.getLblCERTIFICADO(),
								"\" ","formaDePago=\"" + "PAGO EN UNA SOLA EXHIBICION" + "\" ",
								"metodoDePago=\"" + properties.getLabelMetodoPago() + "\" ",
								"LugarExpedicion=\"" + properties.getLabelLugarExpedicion() + "\" ",
								"NumCtaPago=\"" + properties.getlabelFormaPago() + "\" ",
								"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">")
						.toString().getBytes("UTF-8");
						*/
				return Util
						.conctatArguments(
								"\n<cfdi:Comprobante Version=\"3.3\" ",
								concat.toString(),
								"xmlns:Santander=\"http://www.santander.com.mx/addendaECB\" ",		
								"xmlns:cfdi=\"http://www.sat.gob.mx/cfd/3\"  ",		
								"xsi:schemaLocation=\"http://www.sat.gob.mx/cfd/3 ",
								"http://www.sat.gob.mx/sitio_internet/cfd/3/cfdv32.xsd ",
								"http://www.santander.com.mx/addendaECB http://www.santander.com.mx/cfdi/addendaECB.xsd\" ",
								"Sello=\"", properties.getLabelSELLO(), "\" ",
								"NoCertificado=\"",
								properties.getLblNO_CERTIFICADO(), "\" ",
								"Certificado=\"", properties.getLblCERTIFICADO(), "\" ",
//								"formaDePago=\"" + "PAGO EN UNA SOLA EXHIBICION" + "\" ",
//								"metodoDePago=\"" + properties.getLabelMetodoPago() + "\" ",
								"LugarExpedicion=\"" + "01219" + "\" ", // antes properties.getLabelLugarExpedicion() AMDA
//								"NumCtaPago=\"" + properties.getlabelFormaPago() + "\" ",
								"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">")
						.toString().getBytes("UTF-8");
			} 
			else 
			{	return formatECB(numberLine);	}
		
	}

	/**
	 * 
	 * @param linea
	 * @param lstFiscal
	 * @param numberLine
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] emisor(String linea, HashMap<String, FiscalEntity> lstFiscal, 
			long numberLine, HashMap campos22) 
		throws UnsupportedEncodingException 
	{
		lineas = linea.split("\\|");
		System.out.println("Entrando a Emisor: ");
		if (lineas.length >= 2) 
		{
			tags.EMISION_RFC = lineas[1].trim();
			if(tags.EMISION_RFC.trim().length() == 0){ // Validacion AMDA Version 3.3
				tags.EMISION_RFC = "RFCNecesario";
			}
			tags.fis = null;
			tags.fis = lstFiscal.get(tags.EMISION_RFC);
			
			//String expedidoStr = "\n<ExpedidoEn calle=\"AVE. VASCONCELOS\" noExterior=\"142 OTE.\" colonia=\"COL DEL VALLE\" localidad=\"S.PEDRO GARZA G.\" municipio=\"S.PEDRO GARZA G.\" estado=\"N.L.\" pais=\"Mexico\" codigoPostal=\"66220\" xmlns=\"http://www.sat.gob.mx/cfd/2\" />";
			
//			StringBuffer emisorStr = Util
//			.conctatArguments(tags("Emisor", pila), "\n<cfdi:Emisor rfc=\"",
//					tags.EMISION_RFC, "\"", getNameEntityFiscal(),
//					" >", domicilioFiscal(campos22)); // ANTES AMDA Version 3.2
//			System.out.println("Antes de Concat Emisor: "+tags("Emisor", pila));
			StringBuffer emisorStr = Util
					.conctatArguments(tags("Emisor", pila), "\n<cfdi:Emisor Rfc=\"",
							tags.EMISION_RFC, "\"", getNameEntityFiscal(), domicilioFiscal(campos22),
							" />");
			try
			{
				HashMap map1 = (HashMap) campos22.get(tags.EMISION_RFC);
				if (map1 != null)
				{
					String LugarExpedicion = (String) map1.get("LugarExpedicion");
					tags.LUGAR_EXPEDICION = LugarExpedicion;
				}
				
				//El metodo de Pago para la fase 1 será 99 para todas las interfaces de Estados de Cuenta
				
				HashMap map2 =  (HashMap) campos22.get(tags.EMISION_RFC);
				
				if (map2 != null)
				{
					String MetodoDePago = (String) map2.get("metodoDePago");
					tags.METODO_PAGO = MetodoDePago;
				}
				else
				{
					tags.METODO_PAGO = "99";
				}
				
				
				HashMap map3 =  (HashMap) campos22.get(tags.EMISION_RFC);
				if (map3 != null)
				{
					String formaDePago = (String) map3.get("formaDePago");
					tags.FORMA_PAGO = formaDePago;
				}	
				
			}
			catch (Throwable e)
			{	e.printStackTrace();	}
			System.out.println("Saliendo de Emisor: "+emisorStr.toString() );			
			return emisorStr.toString().getBytes("UTF-8");
		} 
		else 
		{	return formatECB(numberLine);	}
	}

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
				System.out.println("Emisro Reg: "+valNombre );
				return " Nombre=\"" +valNombre + "\"";
			}else{
				return " Nombre=\"" +"" + "\"";
			}
			
		}
	}

	public byte[] receptor(String linea, long numberLine) 
		throws UnsupportedEncodingException 
	{
		lineas = linea.split("\\|");
		System.out.println("Entrando a Receptor: ");
		if (lineas.length >= 3) 
		{
			tags.RECEPCION_RFC = lineas[1].trim();
			if(tags.RECEPCION_RFC.trim().length() == 0){ // Validacion AMDA Version 3.3
				tags.RECEPCION_RFC = "RFCNecesario";
			}
			//Doble Sellado
			String nombreReceptor = "";
			
			if(lineas.length > 2){
				if(!lineas[2].trim().equals("")){
					nombreReceptor = " Nombre=\"" + Util.convierte(lineas[2].trim()) + "\"";
				}				
			}
			
			// Nuevos Atributos AMDA Version 3.3
			String residenciaFiscalReceptor = "";
			String numRegIdTribReceptor = "";
			String usoCFDIReceptor = " UsoCFDI=\"" + "P01" + "\"";
			System.out.println("Receptor recepPais: " + tags.recepPais);
			if(tags.recepPais.trim().length() > 0){
				String valPais = UtilCatalogos.findValPais(tags.mapCatalogos, tags.recepPais);
				System.out.println("Valor Abreviado Pais: " + valPais);
				if(valPais.equalsIgnoreCase("vacio")){
					valPais = UtilCatalogos.findEquivalenciaPais(tags.mapCatalogos, tags.recepPais);
					System.out.println("Valor Equivalencia Abreviado Pais: " + valPais);
				}
				residenciaFiscalReceptor = " ResidenciaFiscal=\"" + valPais + "\"";
			}
			
			if(!tags.RECEPCION_RFC.equalsIgnoreCase("RFCNecesario")){
				numRegIdTribReceptor = " NumRegIdTrib=\"" + UtilCatalogos.findNumRegIdTrib(tags.mapCatalogos, tags.RECEPCION_RFC) + "\"";
				System.out.println("Valor NumRegIdTrib: " + UtilCatalogos.findNumRegIdTrib(tags.mapCatalogos, tags.RECEPCION_RFC));
			}
			
			System.out.println("Saliendo de Receptor: ");
			tags("Receptor", pila); // Validando la forma 
			return Util
					.conctatArguments(
//							tags("Receptor", pila), // Veamos si coloca bien el cierre tag de Emisor
							"\n<cfdi:Receptor Rfc=\"",
							Util.convierte(lineas[1].trim()),
							"\"",
							nombreReceptor, 
							residenciaFiscalReceptor,
							numRegIdTribReceptor,
							usoCFDIReceptor, " />").toString().getBytes("UTF-8");
			/*return Util
					.conctatArguments(
							tags("Receptor", pila),
							"\n<cfdi:Receptor rfc=\"",
							Util.convierte(lineas[1].trim()),
							"\"",
							lineas.length > 2 ? " nombre=\"" + Util.convierte(lineas[2].trim())
									+ "\"" : "", " >").toString().getBytes("UTF-8");*/
		} 
		else 
		{	return formatECB(numberLine);	}
	}

	/**
	 * 
	 * @param linea
	 * @param numberLine
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] concepto(String linea, long numberLine, HashMap fiscalEntities, HashMap campos22) 
		throws UnsupportedEncodingException 
	{
		lineas = linea.split("\\|");
		if (lineas.length >= 3) 
		{			
			//HashMap campos = (HashMap) campos22.get(tags.fis.getTaxID());
			//System.out.println("concepto RFC:" + tags.EMISION_RFC);
			HashMap campos = (HashMap) campos22.get(tags.EMISION_RFC);
			System.out.println("tags.EMISION_RFC:" + tags.EMISION_RFC);
			String unidadVal; 	
			if(campos !=null)
			{				
				System.out.println("campos(campos !=null):" + campos);
				unidadVal= (String) campos.get("unidadMedida");
				tags.UNIDAD_MEDIDA=unidadVal;
			}
			else
			{
				System.out.println("campos(campos==null):" + campos);
				tags.UNIDAD_MEDIDA="***NO EXISTE UNIDAD DE MEDIDA DEFINIDA***";
				unidadVal=tags.UNIDAD_MEDIDA;
			}
			
			// Nuevo Campo AMDA Version 3.3 regimenStr = "\n<cfdi:RegimenFiscal Regimen=\"" + regVal + "\" />";
			System.out.println("Tipo Comprobante en Concepto: " + tags.tipoComprobante);
			String valorUnitarioStr = "";
			String nodoValorUnitarioStr = "";
			try{
				Double valUnit = Double.parseDouble(lineas[2].trim());
				if(tags.tipoComprobante.trim().equalsIgnoreCase("I") || tags.tipoComprobante.trim().equalsIgnoreCase("E") || tags.tipoComprobante.trim().equalsIgnoreCase("N")){
					// Valor unitario debe ser mayor a 0
					if(valUnit <= 0){
						valorUnitarioStr = "valorUnitarioDebeSerMayorDeCero";
						nodoValorUnitarioStr = "\" valorUnitarioDebeSerMayorDeCero=\"" + valorUnitarioStr ;
					}else{
						valorUnitarioStr = lineas[2].trim();
						nodoValorUnitarioStr = "\" ValorUnitario=\"" + valorUnitarioStr ;
					}
				}else if(tags.tipoComprobante.trim().equalsIgnoreCase("T")){
					// Valor unitario puede ser mayor o igual a 0
					if(valUnit < 0){
						valorUnitarioStr = "valorUnitarioDebeSerMenorDeCero";
						nodoValorUnitarioStr = "\" valorUnitarioDebeSerMenorDeCero=\"" + valorUnitarioStr ;
					}else{
						valorUnitarioStr = lineas[2].trim();
						nodoValorUnitarioStr = "\" ValorUnitario=\"" + valorUnitarioStr ;
					}
				}else if(tags.tipoComprobante.trim().equalsIgnoreCase("P")){
					// Valor unitario debe ser igual a 0
					if(valUnit != 0){
						valorUnitarioStr = "valorUnitarioDebeSerCero";
						nodoValorUnitarioStr = "\" valorUnitarioDebeSerCero=\"" + valorUnitarioStr ;
					}else{
						valorUnitarioStr = lineas[2].trim();
						nodoValorUnitarioStr = "\" ValorUnitario=\"" + valorUnitarioStr ;
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
			if(unidadVal.length() > 0){
//				claveUnidad = UtilCatalogos.findValClaveUnidad(tags.mapCatalogos, unidadVal);
				claveUnidad = "E48";
			}
			
			// Importe V 3.3 AMDA pendiente logica de redondeo
			String valImporte = "";
			if(lineas[2].trim().length() > 0){
				System.out.println("Importe en Concepto: " + lineas[2].trim());
				valImporte = lineas[2].trim();
			}
			
			// Descuento V 3.3 AMDA este campo es opcional, esta por definir tengo entendido 
			
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
				if(!tags.trasladoImpuestoVal.trim().equalsIgnoreCase("ISR")){
					tasaOCuotaStr = "\" TasaOcuota=\""  + UtilCatalogos.findValMaxTasaOCuota(tags.mapCatalogos, tags.trasladoImpuestoVal, valTipoFactor);
					tasaOCuotaStr = "\" TasaOCuota=\""  + UtilCatalogos.findValMaxTasaOCuota(tags.mapCatalogos, tags.trasladoImpuestoVal, valTipoFactor);
				}
				
				System.out.println("Valor TasaOCuota Traslado AMDA : " + tasaOCuotaStr);
			}
			
			if(valTipoFactor.equalsIgnoreCase("Tasa") || valTipoFactor.equalsIgnoreCase("Cuota")){
				System.out.println("Valor Importe AMDA T : " + tags.trasladoImporteVal + " : " + valImporteTraslado);
				if(tags.trasladoImporteVal.trim().length() > 0){
					valImporteImpTras = "\" Importe=\"" +tags.trasladoImporteVal.trim() + "\"";
				}else{
					valImporteImpTras = "\" Importe=\"" + "0.00" + "\"";
				}
				
			}
			// Base = ValImporte, Importe = Base por porcentajemas Base, descripcion mandar Util.convierte(lineas[1]).trim() 
			String trasladoDoom = UtilCatalogos.findTraslados(tags.mapCatalogos, valImporte, Util.convierte(lineas[1]).trim());
			System.out.println("TRASLADO NODOS AMDA : " + trasladoDoom);
			String elementTraslado = "\n<cfdi:Traslados>" + 
//									 "\n<cfdi:Traslado Base=\"" + valorBase +
//									 "\" Impuesto=\"" + claveImp +
//									 "\" TipoFactor=\"" + valTipoFactor + // Por definir de donde tomar el valor AMDA
//									 tasaOCuotaStr +
//									 valImporteImpTras +
//									 " />" +
									 trasladoDoom +
									 "\n</cfdi:Traslados>";
			System.out.println("Elemento Traslado AMDA : " + elementTraslado);
			
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
				if(!tags.retencionImpuestoVal.trim().equalsIgnoreCase("ISR")){
					tasaOCuotaStrRet = "\" TasaOCuota=\""  + UtilCatalogos.findValMaxTasaOCuota(tags.mapCatalogos, tags.retencionImpuestoVal, valTipoFactorRet);
				}
				
				System.out.println("Valor TasaOCuota Ret AMDA : " + tasaOCuotaStrRet);
			}
			
			if(valTipoFactor.equalsIgnoreCase("Tasa") || valTipoFactor.equalsIgnoreCase("Cuota")){
				System.out.println("Valor Importe Ret AMDA R : " + tags.retencionImporteVal + " : " + valImporteRetencion);
				if(tags.retencionImporteVal.trim().length() > 0){
					valImporteImpRet = "\" Importe=\"" +tags.retencionImporteVal.trim() + "\"";
				}else{
					valImporteImpRet = "\" Importe=\"" + "0.00" + "\"";
				}
			}
			
			String retencionDoom = UtilCatalogos.findRetencion(tags.mapCatalogos, valImporte, Util.convierte(lineas[1]).trim());
			String elementRetencion = "\n<cfdi:Retenciones>" +
//					 				  "\n<cfdi:Retencion Base=\"" + valorBaseRet +
//					 				  "\" Impuesto=\"" + claveImpRet +
//					 				   "\" TipoFactor=\"" + valTipoFactorRet + // Por definir de donde tomar el valor AMDA
//					 				   tasaOCuotaStrRet +
//					 				   valImporteImpRet +
//					 				  "/>" +
									  retencionDoom +
									  "\n</cfdi:Retenciones>";
			
			elementImpuestos = "\n<cfdi:Impuestos>" + 
							   elementTraslado +
							   elementRetencion +
					 		   "\n</cfdi:Impuestos>";
			System.out.println("Elemento Impuestos AMDA : " + elementImpuestos);
			
			String nodoConcepto = "\n<cfdi:Concepto ClaveProdServ=\"" + "84121500" + 
								  "\" Cantidad=\"" + "1" +
								  "\" ClaveUnidad=\"" + claveUnidad + //Pendiente el valor de ClaveUnidad
								  "\" Unidad=\"" + unidadVal + 
								  "\" Descripcion=\"" + Util.convierte(lineas[1]).trim() + 
								  nodoValorUnitarioStr + 
								  "\" Importe=\"" + valImporte + "\"" + " >"+
								  elementImpuestos +
								  "\n</cfdi:Concepto>";
			System.out.println("String Nodo Concepto: " + nodoConcepto);
				
//			return Util
//					.conctatArguments(
//							"\n<cfdi:Concepto cantidad=\"1\" descripcion=\"",
//							Util.convierte(lineas[1]).trim(), "\" valorUnitario=\"",
//							lineas[2].trim(), 
//							"\" importe=\"", lineas[2].trim(), 
//							"\" unidad=\"", unidadVal,
//							"\"/>").toString().getBytes("UTF-8");
			// Cambio de estructura AMDA Version 3.3
			return Util
					.conctatArguments(
//							"\n<cfdi:Concepto ",
							nodoConcepto.toString()
//							"\"/>"
							).toString().getBytes("UTF-8");
		} 
		else 
		{	return formatECB(numberLine);	}
	}

	public String domicilioFiscal(HashMap campos22) 
	{
		if (tags.isEntidadFiscal) 
		{
			tags._Calle = "calle=\""
					+ Util.isNull(tags.fis.getAddress().getStreet()) + "\" ";
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
					+ Util.convierte(tags.fis.getAddress().getRegion()) + "\" ";
			if(tags.fis.getAddress().getState()!=null)
			{
				tags._Estado = "estado=\""
					+ Util.convierte(tags.fis.getAddress().getState().getName()) + "\" ";
				tags._Pais = " pais=\""
					+ Util.convierte(tags.fis.getAddress().getState().getCountry().getName())
					+ "\" ";
			} 
			else 
			{
				tags._Estado = "estado=\"\" ";
				tags._Pais = " pais=\"\" ";
			}

			tags._CodigoPostal = "codigoPostal=\""
					+ tags.fis.getAddress().getZipCode() + "\" ";
		} 
		else 
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

		String regimenStr = "";
		HashMap map = (HashMap) campos22.get(tags.EMISION_RFC);
		System.out.println("***Buscando campos cfd22 para: " + tags.EMISION_RFC);
		if (map != null)
		{	
			tags.regimenFiscalCode = (String) map.get("regimenFiscalCode");
			System.out.println("***Buscando campos cfd22 para Regimen Fiscal Code: " + map.get("regimenFiscalCode"));
			System.out.println("***Buscando campos cfd22 para Regimen Fiscal Codigo: " + tags.fis.getAddress().getZipCode());
			System.out.println("***Buscando campos cfd22 para Regimen Fiscal Codigo Postal: " + map.get("codPostal"));
			tags._CodigoPostal = map.get("codPostal").toString();
			String regVal = (String) map.get("regimenFiscal");
//			regimenStr = "\n<cfdi:RegimenFiscal Regimen=\"" + regVal + "\" />";
			regimenStr = " RegimenFiscal=\"" + UtilCatalogos.findRegFiscalCode(tags.mapCatalogos, regVal) + "\" "; // Agregue esto /> para cerrar el nodo de concepto al regresar AMDA
			tags.REGIMEN_FISCAL = regVal;	
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
	 * @param linea
	 * @param numberLine
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] impuestos(String linea, long numberLine) 
		throws UnsupportedEncodingException 
	{
		lineas = linea.split("\\|");
		tags.isImpuestos = true;
		tags.isConceptos = false;
		
		// Elemento Retenciones V 3.3 AMDA
		String claveImpRet = "";
		if(tags.retencionImpuestoVal.trim().length() > 0){ // Validando el codigo del Impuesto
			System.out.println("Valor Impuesto Ret AMDA : " + tags.retencionImpuestoVal);
			claveImpRet = UtilCatalogos.findValClaveImpuesto(tags.mapCatalogos, tags.retencionImpuestoVal);
			System.out.println("Valor Clave Impuesto Ret AMDA : " + claveImpRet);
		}
		
		String elementRetencion = "\n<cfdi:Retenciones>" +
				  				  "\n<cfdi:Retencion Impuesto=\"" + claveImpRet +
				  				  "\" Importe=\"" + tags.retencionImporteVal + "\"" +
				  "/>" +
				  "\n</cfdi:Retenciones>";
		System.out.println("Elemento Retenciones Impuestos AMDA : " + elementRetencion);
		
		// Elemento Traslados V3.3 AMDA
		String claveImp = "";
		String valTipoFactor = "Tasa"; // Por definir de donde tomar el valor AMDA
		String tasaOCuotaStr = "";
		String valImporteImpTras = "";
					
		if(tags.trasladoImpuestoVal.trim().length() > 0){ // Validando el codigo del Impuesto
			System.out.println("Valor Impuesto Traslado AMDA : " + tags.trasladoImpuestoVal);
			claveImp = UtilCatalogos.findValClaveImpuesto(tags.mapCatalogos, tags.trasladoImpuestoVal);
			System.out.println("Valor Clave Impuesto Traslado AMDA : " + claveImp);
		}
					
		if(valTipoFactor.equalsIgnoreCase("Tasa") || valTipoFactor.equalsIgnoreCase("Cuota")){
			System.out.println("Validacion TasaOCuota Traslado AMDA : " + tags.trasladoImpuestoVal + " : " + valTipoFactor);
			if(!tags.trasladoImpuestoVal.trim().equalsIgnoreCase("ISR")){
				tasaOCuotaStr = "\" TasaOCuota=\""  + UtilCatalogos.findValMaxTasaOCuota(tags.mapCatalogos, tags.trasladoImpuestoVal, valTipoFactor);
			}
						
			System.out.println("Valor TasaOCuota Traslado AMDA : " + tasaOCuotaStr);
		}
					
		if(valTipoFactor.equalsIgnoreCase("Tasa") || valTipoFactor.equalsIgnoreCase("Cuota")){
			System.out.println("Valor Importe AMDA T : " + tags.trasladoImporteVal + " : " + valImporteTraslado);
			if(tags.trasladoImporteVal.trim().length() > 0){
				valImporteImpTras = "\" Importe=\"" +tags.trasladoImporteVal.trim() + "\"";
			}else{
				valImporteImpTras = "\" Importe=\"" + "0.00" + "\"";
			}
			
		}
					
		String elementTraslado = "\n<cfdi:Traslados>" + 
								 "\n<cfdi:Traslado Impuesto=\"" + claveImp +
								 "\" TipoFactor=\"" + valTipoFactor + // Por definir de donde tomar el valor AMDA
								 tasaOCuotaStr +
								 valImporteImpTras + // Por definir como se relaciona el importe 
								 " />" +
								 "\n</cfdi:Traslados>";
		System.out.println("Elemento Traslado Impuestos AMDA : " + elementTraslado);
		
		if (lineas.length >= 3) 
		{
			tags.TOTAL_IMP_RET = lineas[1].trim();
			tags.TOTAL_IMP_TRA = lineas[2].trim();
			return Util
					.conctatArguments(
							tags("", pila),
							"\n<cfdi:Impuestos  ",
							isNullEmpity(lineas[1], "TotalImpuestosRetenidos"),
							isNullEmpity(lineas[2], "TotalImpuestosTrasladados"),
							">",
							elementRetencion,
							elementTraslado,
							">").toString().getBytes("UTF-8");
		} 
		else 
		{	return formatECB(numberLine);	}
	}

	public byte[] traslados(String linea, long numberLine) 
		throws UnsupportedEncodingException 
	{
		lineas = linea.split("\\|");
		if (lineas.length >= 4) 
		{
			return Util
					.conctatArguments("\n<cfdi:Traslado Impuesto=\"",
							lineas[1].trim(), "\" Tasa=\"", lineas[2].trim(),
							"\" Importe=\"", lineas[3].trim(), "\"/>")
					.toString().getBytes("UTF-8");
		} 
		else 
		{	return formatECB(numberLine);	}
	}

	/**
	 * 
	 * @param linea
	 * @param numberLine
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] retenciones(String linea, long numberLine) 
		throws UnsupportedEncodingException 
	{
		lineas = linea.split("\\|");
		if (lineas.length >= 3) 
		{
			return Util
					.conctatArguments("\n<cfdi:Retencion impuesto=\"",
							lineas[1].trim(), "\" importe=\"",
							lineas[2].trim(), "\"/>").toString().getBytes("UTF-8");
		} 
		else 
		{	return formatECB(numberLine);	}
	}

	/**
	 * 
	 * @param linea
	 * @param numberLine
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] complemento(String linea, long numberLine) 
		throws UnsupportedEncodingException 
	{
		lineas = linea.split("\\|");
		if (lineas.length >= 5) {
			String nombreCliente = "";
			
			if(!lineas[2].trim().equals("")){
				nombreCliente = "nombreCliente=\"" + Util.convierte(lineas[2].trim()) + "\" ";
			}
			return Util
					.conctatArguments(
							"\n<Santander:EstadoDeCuentaBancario version=\"1.0\" ",
							"numeroCuenta=\"", lineas[1].trim(), "\" ",
							nombreCliente,
							"periodo=\"", lineas[3].trim(), "\">").toString()
					.getBytes("UTF-8");
		} 
		else 
		{	return formatECB(numberLine);	}
	}

	/**
	 * 
	 * @param linea
	 * @return flg
	 * @throws PatternSyntaxException
	 */
	public boolean validarRFC(String rfc)
		throws PatternSyntaxException
	{
		// TODO Auto-generated method stub
		//Patron del RFC--->>>[A-Z,Ñ,&]{3,4}[0-9]{2}[0-1][0-9][0-3][0-9][A-Z,0-9]{2}[0-9,A]
		
		 Pattern p = Pattern.compile("[A-Z,Ñ,&]{3,4}[0-9]{2}[0-1][0-9][0-3][0-9][A-Z,0-9]{2}[0-9,A]");
		 Matcher m = p.matcher(rfc);
	     
	     if(!m.find()){
	    	 //RFC no valido
	    	 return false;
	     }
	     else{
	    	 //RFC valido
	    	 return true;
	     }
	}
	/**
	 * 
	 * @param linea
	 * @param numberLine
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] movimeinto(String linea, long numberLine) 
		throws UnsupportedEncodingException 
	{
		
			
			System.out.println("length: " + lineas.length);
			lineas = linea.split("\\|");
			if (lineas.length >= 7) 
			{
				Calendar c = Calendar.getInstance();
				
				String[] date = lineas[1].trim().split("-");
				String fechaCal = "";
				try
				{
					c.set(Integer.parseInt(date[0]), Integer.parseInt(date[1]) - 1,
						Integer.parseInt(date[2]), 0, 0, 0);
					fechaCal = "fecha=\"" + Util.convertirFecha(c.getTime()) + "\"";
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				
				
				String rfcEnajenante = lineas[4];
				
				//Validar el RFC
				boolean flgRfcOk;
				flgRfcOk = validarRFC(rfcEnajenante);
				
							
				if ((rfcEnajenante != null)&&(rfcEnajenante.trim().length() > 0) && flgRfcOk)
				{
					
					return Util
					.conctatArguments("\n<Santander:MovimientoECBFiscal ", fechaCal,
							" descripcion=\"", Util.convierte(lineas[3].trim()), "\"",
							" RFCenajenante=\"", Util.convierte(lineas[4].trim()), "\"",
							" Importe=\"", lineas[5].trim(), "\"/>").toString()
					.getBytes("UTF-8");
				}
				else
				{			
					/*
					StringBuffer sb = new StringBuffer();
					sb = Util.conctatArguments("\n<ecb:MovimientoECB ", fechaCal,
									" descripcion=\"", Util.convierte(lineas[3].trim()), "\"",
									" importe=\"", lineas[5].trim(), "\"/>");
					this.lstMovimientosECB.add(sb);
									
					return "".getBytes("UTF-8");
					*/
					
					return Util
						.conctatArguments("\n<Santander:MovimientoECB ", fechaCal,
								" descripcion=\"", Util.convierte(lineas[3].trim()), "\"",
								" importe=\"", lineas[5].trim(), "\"/>").toString()
						.getBytes("UTF-8");
						
				}
			} 
			else 
			{	return formatECB(numberLine);	}
		
	}

	/**
	 * 
	 * @param linea
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] domicilio(String linea, long numberLine) 
		throws UnsupportedEncodingException 
	{
		lineas = linea.split("\\|");
		if (lineas.length >= 11) {
			tags._Calle = Util.isNullEmpity(lineas[1].trim(), "calle");
			tags._NoExterior = Util
					.isNullEmpity(lineas[2].trim(), "noExterior");
			tags._NoInterior = Util
					.isNullEmpity(lineas[3].trim(), "noInterior");
			tags._Colonia = Util.isNullEmpity(lineas[4].trim(), "colonia");
			tags._Localidad = Util.isNullEmpity(lineas[5].trim(), "localidad");
			tags._Referencia = Util
					.isNullEmpity(lineas[6].trim(), "referencia");
			tags._Municipio = Util.isNullEmpity(lineas[7].trim(), "municipio");
			tags._Estado = Util.isNullEmpity(lineas[8].trim(), "estado");
			tags._Pais = " pais=\"" + lineas[9].trim() + "\" ";
			tags._CodigoPostal = lineas.length >= 11 ? Util.isNullEmpity(
					lineas[10].trim(), "codigoPostal") : "";
					tags("", pila).toString();
//			return Util
//					.conctatArguments("\n<cfdi:Domicilio ", tags._Calle,
//							tags._NoExterior, tags._NoInterior, tags._Colonia,
//							tags._Localidad, tags._Referencia, tags._Municipio,
//							tags._Estado, tags._Pais, tags._CodigoPostal,
//							" />", tags("", pila)).toString().getBytes("UTF-8");
			return "".getBytes("UTF-8");
		} 
		else 
		{	return formatECB(numberLine);	}
	}
	
	public void loadInfoV33(int numElement, String linea, HashMap campos22, HashMap<String, FiscalEntity> lstFiscal) 
	{
		System.out.println("entra LoadInfoV33: "+linea);
		System.out.println("entra LoadInfoV33 numElement: "+numElement);
		String[] lin = linea.split("\\|");
		switch (numElement) 
		{
		case 1:
			// Set
			break;
		case 2:
			// Comprobante
			break;
		case 3:
			// Emisor
			System.out.println("Emisor ? LoadInfoV33: "+lin[1].trim() + " : " + lin[2].trim());
			if (lin.length >= 2) 
			{
				tags.EMISION_RFC = lin[1].trim();
				if(tags.EMISION_RFC.trim().length() == 0){ // Validacion AMDA Version 3.3
					tags.EMISION_RFC = "RFCNecesario";
				}
				tags.fis = null;
				tags.fis = lstFiscal.get(tags.EMISION_RFC);
				domicilioFiscal(campos22);
			}
			
			break;
		case 4:
			// Receptor
			break;
		case 5:
			// Domicilio
			tags.recepPais = lin[9].trim();
			break;
		case 6:
			// Concepto
			break;
		case 7:
			// Impuestos
			break;
		case 8:
			// Retenciones
			System.out.println("entra LoadInfoV33 Retenciones: "+ lin[1].trim() + " : " + lin[2].trim());
//			tags.retencionImpuestoVal = lineas[1].trim();
//			tags.retencionImporteVal = lineas[2].trim(); // Se comenta por que al parecer se recorro uno despues AMDA
			break;
		case 9:
			// Traslados
//			System.out.println("entra LoadInfoV33 Traslados: "+ lineas[1].trim() + " : " + lineas[2].trim() + " : " + lineas[3].trim());
//			tags.trasladoImpuestoVal = lineas[1].trim();
//			tags.trasladoTasaVal = lineas[2].trim();
//			tags.trasladoImporteVal = lineas[3].trim(); // Se comenta por que al parecer se recorro uno despues AMDA
			System.out.println("entra LoadInfoV33 Retenciones: "+ lineas[1].trim() + " : " + lineas[2].trim());
			tags.retencionImpuestoVal = lineas[1].trim();
			
			if(lineas[3].trim().equalsIgnoreCase("0.00")){
				tags.retencionImporteVal = "0.00";
			}else{
				tags.retencionImporteVal = lineas[2].trim();
			}
			
			valImporteRetencion = lineas[2].trim();
			System.out.println("Sale LoadInfoV33 Retencion:" + tags.retencionImporteVal);
			break;
		case 10:
			// -
			System.out.println("entra LoadInfoV33 Traslados: "+ lineas[1].trim() + " : " + lineas[2].trim() + " : " + lineas[3].trim());
			tags.trasladoImpuestoVal = lineas[1].trim();
			tags.trasladoTasaVal = lineas[2].trim();
			if(lineas[3].trim().equalsIgnoreCase("0.00")){
				tags.trasladoImporteVal = "0.00";
			}else{
				tags.trasladoImporteVal = lineas[3].trim();
			}
			valImporteTraslado = lineas[3].trim();
			System.out.println("Sale LoadInfoV33 Traslados:" + tags.trasladoImporteVal);
			break;
		case 11:
			// Movimiento
			break;
		}
	}

	public TagsXML getTags() 
	{	return tags;	}

	public void setTags(TagsXML tags) 
	{	this.tags = tags;	}

	public Stack<String> getPila() 
	{	return pila;	}

	public void setPila(Stack<String> pila) 
	{	this.pila = pila;	}

	public List<String> getDescriptionFormat() 
	{	return descriptionFormat;	}

	public void setDescriptionFormat(List<String> descriptionFormat) 
	{	this.descriptionFormat = descriptionFormat;		}
	
	public byte[] formatECB(long numberLine)
	{
		tags.isFormat = true;
		descriptionFormat.add("" + numberLine);
		return "".getBytes();
	}

	public List<StringBuffer> getLstMovimientosECB() {
		return lstMovimientosECB;
	}

	public void setLstMovimientosECB(List<StringBuffer> lstMovimientosECB) {
		this.lstMovimientosECB = lstMovimientosECB;
	}

	public String getValImporteRetencion() {
		return valImporteRetencion;
	}

	public void setValImporteRetencion(String valImporteRetencion) {
		this.valImporteRetencion = valImporteRetencion;
	}

	public String getValImporteTraslado() {
		return valImporteTraslado;
	}

	public void setValImporteTraslado(String valImporteTraslado) {
		this.valImporteTraslado = valImporteTraslado;
	}
	
		
}
