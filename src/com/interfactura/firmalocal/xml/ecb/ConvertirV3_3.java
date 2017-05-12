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
import java.util.Map;
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
			String monedaMexicana = "MXN";
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
				if(tags.SERIE_FISCAL_CFD == null || "".equals(tags.SERIE_FISCAL_CFD.intern().trim())){
					tags.SERIE_FISCAL_CFD = monedaMexicana.intern();
				}
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
							concat.append(" Moneda=\"" + monedaMexicana.intern() + "\"");	
							tags.TIPO_MONEDA = monedaMexicana.intern();
						}else{
							//tags.SERIE_FISCAL_CFD="MONEDA INCORRECTA " + tags.SERIE_FISCAL_CFD + "";
							
							if(tags.SERIE_FISCAL_CFD.trim() != ""){ // Validacion Moneda Equivalencia AMDA V 3.3
								System.out.println("Consultando catalogo para monmeda: " + tags.SERIE_FISCAL_CFD);
								valEqMoneda = UtilCatalogos.findMonedaCatalogo(tags.mapCatalogos, tags.SERIE_FISCAL_CFD);
								if(valEqMoneda.equalsIgnoreCase("vacio")){
//									valEqMoneda = UtilCatalogos.findEquivalenciaMoneda(tags.mapCatalogos, tags.SERIE_FISCAL_CFD);
//									if(!valEqMoneda.equalsIgnoreCase("vacio")){
//										concat.append(" Moneda=\"" + valEqMoneda + "\"");
										tags.TIPO_MONEDA = valEqMoneda;
//									}else{
										concat.append(" ErrCompMoneda001=\"" + valEqMoneda + "\"");
//									}
								}else{
									concat.append(" Moneda=\"" + valEqMoneda + "\"");
									tags.TIPO_MONEDA = valEqMoneda;
								}
//								concat.append(" Moneda=\"" + valEqMoneda + "\"");
//								tags.TIPO_MONEDA = valEqMoneda;
								
							}else{
								tags.TIPO_MONEDA = tags.SERIE_FISCAL_CFD;
								concat.append(" ErrCompMoneda001" + "=\"" + tags.SERIE_FISCAL_CFD + "\"");
							}
						}
//					}else if(tags.SERIE_FISCAL_CFD.trim() == ""){
//						concat.append(" Serie=\"" + tags.SERIE_FISCAL_CFD + "\"");	
//						concat.append(" Moneda=\"" + tags.SERIE_FISCAL_CFD + "\"");
//						tags.TIPO_MONEDA = tags.SERIE_FISCAL_CFD;
					}else{
						//tags.SERIE_FISCAL_CFD="MONEDA INCORRECTA " + tags.SERIE_FISCAL_CFD + "";
						if(tags.SERIE_FISCAL_CFD.trim() != ""){ // Validacion Moneda Equivalencia AMDA V 3.3
							System.out.println("Consultando catalogo para monmeda 2: " + tags.SERIE_FISCAL_CFD);
							valEqMoneda = UtilCatalogos.findMonedaCatalogo(tags.mapCatalogos, tags.SERIE_FISCAL_CFD);
							if(valEqMoneda.equalsIgnoreCase("vacio")){
								valEqMoneda = UtilCatalogos.findEquivalenciaMoneda(tags.mapCatalogos, tags.SERIE_FISCAL_CFD);
								if(!valEqMoneda.equalsIgnoreCase("vacio")){
									concat.append(" Moneda=\"" + valEqMoneda + "\"");
									tags.TIPO_MONEDA = valEqMoneda;
								}else{
									concat.append(" ErrCompMoneda002=\"" + valEqMoneda + "\"");
								}
							}else{
								concat.append(" Moneda=\"" + valEqMoneda + "\"");
								tags.TIPO_MONEDA = valEqMoneda;
							}
//							concat.append(" Moneda=\"" + valEqMoneda + "\"");
//							tags.TIPO_MONEDA = valEqMoneda;
							
						}else{
							tags.TIPO_MONEDA = tags.SERIE_FISCAL_CFD;
							concat.append(" ErrCompMoneda002" + "=\"" + tags.SERIE_FISCAL_CFD + "\"");
						}
					}
					System.out.println("Tipo Moneda AMDA: " + tags.TIPO_MONEDA);
					// Validando decimales soportados por el tipo de moneda AMDA V 3.3
					if(tags.TIPO_MONEDA.trim().length() > 0 ){
						tags.decimalesMoneda = UtilCatalogos.findDecimalesMoneda(tags.mapCatalogos, tags.TIPO_MONEDA);
						System.out.println("Decimales moneda: " + tags.decimalesMoneda);
//						if(tags.decimalesMoneda.contains(".")){
//							String deci[] = tags.decimalesMoneda.split("\\.");
//							tags.decimalesMoneda = deci[1];
//						}
						
						if(!tags.TIPO_MONEDA.trim().equalsIgnoreCase("MXN")){ // Si la moneda es MXN automaticamente se debe de colocar el tipo cambio cambio es 1
							
						}else{
							tags.TIPO_CAMBIO = "1.00";
						}
						
					}
					
						
					//Validando tipo de Cambio AMDA 
					System.out.println("Tags tipo CAM : "+ tags.TIPO_CAMBIO);
					if(fileNames.equals("CFDLFFONDOS")){
						
						String patternReg = "";
						double valueTipoCambioDoubl = 0.00;
						if(tags.TIPO_CAMBIO.trim().length() > 0){
							patternReg = "[0-9]{1,14}(.([0-9]{1,6}))";
//							System.out.println("PATTERN REGEX:  " + patternReg);
							if(!patternReg.trim().equalsIgnoreCase("vacio") && patternReg.trim().length() > 0){
								System.out.println("Validando PATTERN REGEX Para tipo de Cambio : " + tags.TIPO_CAMBIO);
								Pattern p = Pattern.compile(patternReg);
								 Matcher m = p.matcher(tags.TIPO_CAMBIO);
							     
							     if(!m.find()){
							    	 //TipoComprobante no valido
							    	 System.out.println("PATTERN REGEX NO ES Valido el tipo de Cambio:  " + tags.TIPO_CAMBIO + " : " + patternReg);
							    	 concat.append(" ErrCompTipoCambio001" + tags.TIPO_CAMBIO + "=\"" + tags.TIPO_CAMBIO + "\"");
							     }else{
							    	 
							    	 if(!tags.TIPO_CAMBIO.isEmpty()){//////condición de prueba"""""""""""""""
											//HashMap<String, String> monedas = (HashMap<String, String>) tipoCambio.get(tags.EMISION_PERIODO);
											System.out.println("tags.TIPO_CAMBIO Uno: " + tags.TIPO_CAMBIO);
//											if (!tags.TIPO_CAMBIO.trim().equals("TIPODECAMBIO_INCORRECTO")) // Diferente Incorrecto AMDA
//											{
											if(!tags.TIPO_MONEDA.equalsIgnoreCase("MXN") && !tags.TIPO_MONEDA.equalsIgnoreCase("XXX")){ //Validacion AMDA
												
//												String monedaValEq= UtilCatalogos.findTipoCambioByMoneda(tags.mapCatalogos, tags.TIPO_MONEDA);
//												if(monedaValEq.trim().length() > 0 && !monedaValEq.trim().equalsIgnoreCase("vacio")){
//													concat.append(" TipoCambio=\"" + monedaValEq + "\"");
//												}else{
//													concat.append(" TipoCambioNoEntonctradoParaElTipoDeMoneda=\"" + tags.TIPO_MONEDA + "\"");
//												}
												
												String resultadoRipoCam = UtilCatalogos.findTipoCambioPorcentaje(tags.mapCatalogos, tags.TIPO_MONEDA, tags.TIPO_CAMBIO);
											    if(resultadoRipoCam.equalsIgnoreCase("OK")){
											       System.out.println("Tipo Cambio Bien");
											       concat.append(" TipoCambio=\"" + tags.TIPO_CAMBIO + "\"");
											    }else{
											    	System.out.println("Tipo Cambio No dentro de limites");
											    	concat.append(" ErrCompTipoCambio002" + tags.TIPO_MONEDA + "=\"" + tags.TIPO_CAMBIO + "\"");
											    }
												
												
//												Double valMinimo = 0.000001;
//												try{
//													
//													double value = Double.parseDouble(tags.TIPO_CAMBIO);
//												    if(value >= valMinimo){
//												       System.out.println("Tipo Cambio mayor");
//												       concat.append(" TipoCambio=\"" + tags.TIPO_CAMBIO + "\"");
//												    }else{
//												    	System.out.println("Tipo Cambio menor");
//												    	concat.append(" TipoCambioMinimoRequerido" + tags.TIPO_CAMBIO + "=\"" + tags.TIPO_CAMBIO + "\"");
//												    }
//													
//												}catch (NumberFormatException e){
//													concat.append(" TipoCambioIncorrecto" + tags.TIPO_CAMBIO + "=\"" + tags.TIPO_CAMBIO + "\"");
//												}
												
											}else if(tags.TIPO_MONEDA.equalsIgnoreCase("MXN")){
												 System.out.println("Cuendo es MXN La Moneda AMDA");
												 String tipocambioVal = UtilCatalogos.findTipoCambioByMoneda(tags.mapCatalogos, tags.TIPO_MONEDA);
												 if(!tipocambioVal.equalsIgnoreCase("vacio")){
														concat.append(" TipoCambio=\"" + tipocambioVal + "\"");
												 }else{
														concat.append(" ErrCompTipoCambio003=\"" + UtilCatalogos.findTipoCambioByMoneda(tags.mapCatalogos, tags.TIPO_MONEDA) + "\"");
												 }

											}else if(tags.TIPO_MONEDA.equalsIgnoreCase("XXX")){
												 System.out.println("Cuendo es XXX La Moneda AMDA");
//												concat.append(" TipoCambio=\"" + "1.00"+ "\"");
											}else{
//												concat.append(" TipoCambio=\"" + "Tipo de Comprobante no entonctrado para el tipo de moneda" + tags.TIPO_MONEDA + "\"");
//												concat.append(" TipoCambio=\"" + tags.TIPO_CAMBIO + "\"");
											} // Termina Validacion AMDA
																	
											}
							     }
							}
						}else if(tags.TIPO_MONEDA.equalsIgnoreCase("MXN")){
							 concat.append(" ErrCompTipoCambio004" + "=\"" + tags.TIPO_CAMBIO + "\"");
						}else if(!tags.TIPO_MONEDA.equalsIgnoreCase("MXN") && !tags.TIPO_MONEDA.equalsIgnoreCase("XXX")){
							concat.append(" ErrCompTipoCambio005" + "=\"" + tags.TIPO_CAMBIO + "\"");
						}
						
						
//							else{ // Comentado reciente AMDA
//								//tags.TIPO_CAMBIO="TIPO_CAMBIO INCORRECTO " + tags.TIPO_CAMBIO + "";
//								concat.append(" TipoCambioIncorrecto" + tags.TIPO_CAMBIO + "=\"" + tags.TIPO_CAMBIO + "\"");
//							}							
//						}else{
//							tags.TIPO_CAMBIO = "1.0000";
//							concat.append(" TipoCambio=\"" + tags.TIPO_CAMBIO + "\"");
//						}	// Diferente Incorercto AMDA						
					}else{
						//HashMap<String, String> monedas = (HashMap<String, String>) tipoCambio.get(tags.EMISION_PERIODO);
						System.out.println("tags.TIPO_CAMBIO Dos: " + tags.TIPO_CAMBIO);
//						if (tags.TIPO_CAMBIO.length() > 0 && !tags.TIPO_CAMBIO.trim().equals("TIPODECAMBIO_INCORRECTO"))
//						{
						String patternReg = "";
						double valueTipoCambioDoubl = 0.00;
						if(tags.TIPO_CAMBIO.trim().length() > 0){
							patternReg = "[0-9]{1,14}(.([0-9]{1,6}))";
//							System.out.println("PATTERN REGEX:  " + patternReg);
							if(!patternReg.trim().equalsIgnoreCase("vacio") && patternReg.trim().length() > 0){
								System.out.println("Validando PATTERN REGEX Para tipo de Cambio : " + tags.TIPO_CAMBIO);
								Pattern p = Pattern.compile(patternReg);
								 Matcher m = p.matcher(tags.TIPO_CAMBIO);
							     
							     if(!m.find()){
							    	 //TipoComprobante no valido
							    	 System.out.println("PATTERN REGEX NO ES Valido el tipo de Cambio:  " + tags.TIPO_CAMBIO + " : " + patternReg);
							    	 concat.append(" ErrCompTipoCambio001" + tags.TIPO_CAMBIO + "=\"" + tags.TIPO_CAMBIO + "\"");
							     }else{
							    	 
							    	 if(!tags.TIPO_MONEDA.equalsIgnoreCase("MXN") && !tags.TIPO_MONEDA.equalsIgnoreCase("XXX")){ //Validacion AMDA
											if(tags.TIPO_CAMBIO.trim().length() > 0){
												
//												String monedaValEq= UtilCatalogos.findTipoCambioByMoneda(tags.mapCatalogos, tags.TIPO_MONEDA);
//												UtilCatalogos.findTipoCambioPorcentaje(tags.mapCatalogos, tags.TIPO_MONEDA, tags.TIPO_CAMBIO.trim());
//												if(monedaValEq.trim().length() > 0 && !monedaValEq.trim().equalsIgnoreCase("vacio")){
//													concat.append(" TipoCambio=\"" + monedaValEq + "\"");
//												}else{
//													concat.append(" TipoCambio=\"" + "Tipo de Comprobante no entonctrado para el tipo de moneda" + tags.TIPO_MONEDA + "\"");
//												}
												
												String resultadoRipoCam = UtilCatalogos.findTipoCambioPorcentaje(tags.mapCatalogos, tags.TIPO_MONEDA, tags.TIPO_CAMBIO);
											    if(resultadoRipoCam.equalsIgnoreCase("OK")){
											       System.out.println("Tipo Cambio Bien");
											       concat.append(" TipoCambio=\"" + tags.TIPO_CAMBIO + "\"");
											    }else{
											    	System.out.println("Tipo Cambio No dentro de limites");
											    	concat.append(" ErrCompTipoCambio002" + tags.TIPO_MONEDA + "=\"" + tags.TIPO_CAMBIO + "\"");
											    }
												
											}else{
												concat.append(" ErrCompTipoCambio005=\"" + tags.TIPO_CAMBIO + tags.TIPO_MONEDA + "\"");
											}

											
											
//											Double valMinimo = 0.000001;
//											try{
//												
//												double value = Double.parseDouble(tags.TIPO_CAMBIO);
//											    if(value >= valMinimo){
//											       System.out.println("Tipo Cambio mayor");
//											       concat.append(" TipoCambio=\"" + tags.TIPO_CAMBIO + "\"");
//											    }else{
//											    	System.out.println("Tipo Cambio menor");
//											    	concat.append(" TipoCambioMinimoRequerido" + tags.TIPO_CAMBIO + "=\"" + tags.TIPO_CAMBIO + "\"");
//											    }
//												
//											}catch (NumberFormatException e){
//												concat.append(" TipoCambioIncorrecto" + tags.TIPO_CAMBIO + "=\"" + tags.TIPO_CAMBIO + "\"");
//											}
											
										}else if(tags.TIPO_MONEDA.equalsIgnoreCase("MXN")){
											System.out.println("Cuendo es MXN La Moneda AMDA 2 " + tags.TIPO_CAMBIO);
											if(tags.TIPO_CAMBIO.trim().length() > 0){
												System.out.println("Cuendo es MXN La Moneda AMDA 2 no viene vacio " + tags.TIPO_CAMBIO);
												try{
													double tipoCamDou = Double.parseDouble(tags.TIPO_CAMBIO);
													if(tipoCamDou > 1 || tipoCamDou < 1){
														concat.append(" ErrCompTipoCambio004=\"" + tags.TIPO_CAMBIO + "\"");
													}else{
														concat.append(" TipoCambio=\"" + tags.TIPO_CAMBIO + "\"");
													}
												}catch(NumberFormatException e){
													System.out.println("Cuendo es MXN La Moneda AMDA 2 no viene vacio No numerico" + tags.TIPO_CAMBIO);
													concat.append(" ErrCompTipoCambio006=\"" + tags.TIPO_CAMBIO + "\"");
												}
											}else{
//												System.out.println("Cuendo es MXN La Moneda AMDA 2 no viene vacio " + tags.TIPO_CAMBIO);
//												String tipocambioVal = UtilCatalogos.findTipoCambioByMoneda(tags.mapCatalogos, tags.TIPO_MONEDA);
//												 if(!tipocambioVal.equalsIgnoreCase("vacio")){
//														concat.append(" TipoCambio=\"" + tipocambioVal + "\"");
//												 }else{
//														concat.append(" NoSeEncontroTipoCambioRelacionadoALaMoneda=\"" + UtilCatalogos.findTipoCambioByMoneda(tags.mapCatalogos, tags.TIPO_MONEDA) + "\"");
//												 }
											}
											 
										}else if(tags.TIPO_MONEDA.equalsIgnoreCase("XXX")){
											 System.out.println("Cuendo es XXX La Moneda AMDA");
//											concat.append(" TipoCambio=\"" + "1.00"+ "\"");
										}else{
											concat.append(" TipoCambio=\"" + "Tipo de Comprobante no entonctrado para el tipo de moneda" + tags.TIPO_MONEDA + "\"");
//											concat.append(" TipoCambio=\"" + tags.TIPO_CAMBIO + "\"");
										} // Termina Validacion AMDA
							    	 
							     }
							}
						}else if(tags.TIPO_MONEDA.equalsIgnoreCase("MXN")){
							 concat.append(" ErrCompTipoCambio004" + "=\"" + tags.TIPO_CAMBIO + "\"");
						}else if(!tags.TIPO_MONEDA.equalsIgnoreCase("MXN") && !tags.TIPO_MONEDA.equalsIgnoreCase("XXX")){
							concat.append(" ErrCompTipoCambio005" + "=\"" + tags.TIPO_CAMBIO + "\"");
						}
						
											
//						}
//						else if(tags.TIPO_CAMBIO.trim().equals("TIPODECAMBIO_INCORRECTO")){
//							//tags.TIPO_CAMBIO="TIPO_CAMBIO INCORRECTO " + tags.TIPO_CAMBIO + "";
//							concat.append(" TipoCambioIncorrecto" + tags.TIPO_CAMBIO + "=\"" + tags.TIPO_CAMBIO + "\"");
//						}
//						else
//						{
//							//tags.TIPO_CAMBIO="NO EXISTEN TIPOS DE CAMBIO PARA EL DIA " + tags.EMISION_PERIODO + "";
//							concat.append(" TipoCambioNoDefinido" + tags.TIPO_CAMBIO + "=\"" + tags.TIPO_CAMBIO + "\"");
//						}
					}				
				}
				else
				{
					System.out.println("Tipo Cambio 3 : " + tags.TIPO_CAMBIO);
					String patternReg = "";
					double valueTipoCambioDoubl = 0.00;
					if(tags.TIPO_CAMBIO.trim().length() > 0){
						patternReg = "[0-9]{1,14}(.([0-9]{1,6}))";
//						System.out.println("PATTERN REGEX:  " + patternReg);
						if(!patternReg.trim().equalsIgnoreCase("vacio") && patternReg.trim().length() > 0){
							System.out.println("Validando PATTERN REGEX Para tipo de Cambio : " + tags.TIPO_CAMBIO);
							Pattern p = Pattern.compile(patternReg);
							 Matcher m = p.matcher(tags.TIPO_CAMBIO);
						     
						     if(!m.find()){
						    	 //TipoComprobante no valido
						    	 System.out.println("PATTERN REGEX NO ES Valido el tipo de Cambio:  " + tags.TIPO_CAMBIO + " : " + patternReg);
						    	 concat.append(" ErrCompTipoCambio001" + tags.TIPO_CAMBIO + "=\"" + tags.TIPO_CAMBIO + "\"");
						     }else{
						    	 
						    	 try{
										valueTipoCambioDoubl = Double.parseDouble(tags.TIPO_CAMBIO);
									}catch (NumberFormatException e){
										concat.append(" ErrCompTipoCambio006" + tags.TIPO_CAMBIO + "=\"" + tags.TIPO_CAMBIO + "\"");
									}
						    	 
									if(!tags.TIPO_MONEDA.equalsIgnoreCase("MXN") && !tags.TIPO_MONEDA.equalsIgnoreCase("XXX")){ //Validacion AMDA
//										Double valMinimo = 0.000001;
//										    if(valueTipoCambioDoubl >= valMinimo){
//										       System.out.println("Tipo Cambio mayor");
//										       concat.append(" TipoCambio=\"" + tags.TIPO_CAMBIO + "\"");
//										    }else{
//										    	System.out.println("Tipo Cambio menor");
//										    	concat.append(" TipoCambioMinimoRequerido" + tags.TIPO_CAMBIO + "=\"" + tags.TIPO_CAMBIO + "\"");
//										    }
										
										String resultadoRipoCam = UtilCatalogos.findTipoCambioPorcentaje(tags.mapCatalogos, tags.TIPO_MONEDA, tags.TIPO_CAMBIO);
									    if(resultadoRipoCam.equalsIgnoreCase("OK")){
									       System.out.println("Tipo Cambio Bien");
									       concat.append(" TipoCambio=\"" + tags.TIPO_CAMBIO + "\"");
									    }else{
									    	System.out.println("Tipo Cambio No dentro de limites");
									    	concat.append(" ErrCompTipoCambio002=\"" + tags.TIPO_CAMBIO + "\"");
									    }

									}else if(tags.TIPO_MONEDA.equalsIgnoreCase("MXN")){
										 System.out.println("Cuendo es MXN La Moneda AMDA" + valueTipoCambioDoubl + " : " + tags.TIPO_CAMBIO);
										 if(valueTipoCambioDoubl > 1 || valueTipoCambioDoubl < 1){
												concat.append(" ErrCompTipoCambio004=\"" + tags.TIPO_CAMBIO + "\"");
										 }else{
												concat.append(" TipoCambio=\"" + tags.TIPO_CAMBIO + "\""); 
										 }
									}else if(tags.TIPO_MONEDA.equalsIgnoreCase("XXX")){
										 System.out.println("Cuendo es XXX La Moneda AMDA");
//										concat.append(" TipoCambio=\"" + "1.00"+ "\"");
									}else{
//										concat.append(" TipoCambio=\"" + tags.TIPO_CAMBIO + "\"");
									} // Termina Validacion AMDA	
						    	 
						     }
							
						}
					}else if(tags.TIPO_MONEDA.equalsIgnoreCase("MXN")){
						 concat.append(" ErrCompTipoCambio004" + "=\"" + tags.TIPO_CAMBIO + "\"");
					}else if(!tags.TIPO_MONEDA.equalsIgnoreCase("MXN") && !tags.TIPO_MONEDA.equalsIgnoreCase("XXX")){
						concat.append(" ErrCompTipoCambio005" + "=\"" + tags.TIPO_CAMBIO + "\"");
					}
					
//					if(!tags.TIPO_MONEDA.equalsIgnoreCase("MXN") && !tags.TIPO_MONEDA.equalsIgnoreCase("XXX")){ //Validacion AMDA
//						Double valMinimo = 0.000001;
//						try{
//							
//							double value = Double.parseDouble(tags.TIPO_CAMBIO);
//						    if(value >= valMinimo){
//						       System.out.println("Tipo Cambio mayor");
//						       concat.append(" TipoCambio=\"" + tags.TIPO_CAMBIO + "\"");
//						    }else{
//						    	System.out.println("Tipo Cambio menor");
//						    	concat.append(" TipoCambioMinimoRequerido" + tags.TIPO_CAMBIO + "=\"" + tags.TIPO_CAMBIO + "\"");
//						    }
//							
//						}catch (NumberFormatException e){
//							concat.append(" TipoCambioIncorrecto" + tags.TIPO_CAMBIO + "=\"" + tags.TIPO_CAMBIO + "\"");
//						}
//						
//					}else if(tags.TIPO_MONEDA.equalsIgnoreCase("MXN")){
//						 System.out.println("Cuendo es MXN La Moneda AMDA");
//						concat.append(" TipoCambio=\"" + "1.00"+ "\"");
//					}else if(tags.TIPO_MONEDA.equalsIgnoreCase("XXX")){
//						 System.out.println("Cuendo es XXX La Moneda AMDA");
////						concat.append(" TipoCambio=\"" + "1.00"+ "\"");
//					}else{
//						concat.append(" TipoCambio=\"" + tags.TIPO_CAMBIO + "\"");
//					} // Termina Validacion AMDA					
					
//					tags.TIPO_CAMBIO="";
//					tags.TIPO_MONEDA="";
				}
					
//				UtilCatalogos.findTipoCambioPorcentaje(tags.mapCatalogos, tags.TIPO_MONEDA, tags.TIPO_CAMBIO.trim());
				
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
				System.out.println("Validando PATTERN REGEX");
				Pattern p = Pattern.compile("[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])T(([01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9])");
				 Matcher m = p.matcher(tags.FECHA_CFD);
			     
			     if(!m.find()){
			    	 //RFC no valido
			    	 System.out.println("PATTERN REGEX NO ES Valido FECHA:  " + tags.FECHA_CFD);
//			    	 numRegIdTribReceptor = " ErrReceNumRegIdTrib001=\"" + valRegIdTrib + "\"";
			    	 concat.append(" ErrCompFecha001=\"" + tags.FECHA_CFD + "\"");
			     }else{
			    	 concat.append(" Fecha=\"" + tags.FECHA_CFD + "\"");
			     }
//				concat.append(" Fecha=\"" + tags.FECHA_CFD + "\"");
				
				//Doble Sellado
				System.out.println("TIPO DE COMPROBANTE PRUEBA: " + lineas[1].trim().toUpperCase());
				System.out.println("TAG CATALOGOS PRUEBA: " + tags.mapCatalogos.size());
				System.out.println("Respuesta a funcion de busqueda de TIPOCOMPROBANTE: "+UtilCatalogos.findTipoComprobante(tags.mapCatalogos, "I"));
				// Se valida por medio de un catalogo AMDA
				if(UtilCatalogos.findTipoComprobante(tags.mapCatalogos, lineas[1].trim().toUpperCase()).equals("tipoDeComprobanteIncorrecto")){
					tags.tipoComprobante = "tipoDeComprobanteIncorrecto";
					concat.append(" ErrCompTipoComprobante001" + "=\"" + lineas[1].trim().toUpperCase() + "\" ");
				}else{
					System.out.println("TIPO DE COMPROBANTE: " + lineas[1].trim().toUpperCase());
					tags.tipoComprobante = UtilCatalogos.findTipoComprobante(tags.mapCatalogos, lineas[1].trim().toUpperCase());
					concat.append(" TipoDeComprobante=\""
							+ UtilCatalogos.findTipoComprobante(tags.mapCatalogos, lineas[1].trim().toUpperCase()) + "\" ");					
				}
				
				System.out.println("SubTotal : " + lineas[6].trim());
				tags.SUBTOTAL_MN = lineas[6].trim();
//				if(lineas[1].trim().toUpperCase().equalsIgnoreCase("T") && lineas[1].trim().toUpperCase().equalsIgnoreCase("P")){
//					if(tags.decimalesMoneda == 0){
//						concat.append(" SubTotal=\"" + "0" + "\"");
//					}else if(tags.decimalesMoneda == 2){
//						concat.append(" SubTotal=\"" + "0.00" + "\"");
//					}else if(tags.decimalesMoneda == 3){
//						concat.append(" SubTotal=\"" + "0.000" + "\"");
//					}else if(tags.decimalesMoneda == 4){
//						concat.append(" SubTotal=\"" + "0.0000" + "\"");
//					}
////					concat.append(" SubTotal=\"" + "0" + "\"");
//				}else{
//					System.out.println("SubTotal agregando decimales : " + tags.decimalesMoneda + " : " + tags.SUBTOTAL_MN);
//					concat.append(" SubTotal=\"" + UtilCatalogos.decimales(tags.SUBTOTAL_MN, tags.decimalesMoneda ) + "\"");
//				}
				System.out.println("SubTotal : " + tags.SUBTOTAL_MN);
				try {
				    double valSubTotal = Double.parseDouble(tags.SUBTOTAL_MN.trim());
				    if(valSubTotal<0){
				       System.out.println("SubTotal: " + " es negativo");
				    	concat.append(" ErrCompSubTotal001" + "=\"" + tags.SUBTOTAL_MN.trim() + "\"");
				    }else{
				       System.out.println("SubTotal: " + " es positivo");
				       System.out.println("SubTotal agregando decimales : " + tags.decimalesMoneda + " : " + tags.SUBTOTAL_MN.trim());
				       System.out.println("SubTotal: " + " es positivo" + tags.tipoComprobante);
				       if(tags.tipoComprobante.equalsIgnoreCase("T") ||tags.tipoComprobante.equalsIgnoreCase("P")){
				    	   if(valSubTotal > 0 || valSubTotal < 0){
				    		   concat.append(" ErrCompSubTotal002=\"" + UtilCatalogos.decimales(tags.SUBTOTAL_MN.trim(), tags.decimalesMoneda) + "\"");
				    	   }else{
				    		   if(UtilCatalogos.decimalesValidationMsj(tags.SUBTOTAL_MN.trim(), tags.decimalesMoneda)){
//					    		   concat.append(" SubTotal=\"" + UtilCatalogos.decimales(tags.SUBTOTAL_MN.trim(), tags.decimalesMoneda ) + "\"");
					    		   concat.append(" SubTotal=\"" + tags.SUBTOTAL_MN.trim() + "\"");
				    		   }else{
				    			   concat.append(" ErrCompSubTotal003=\"" + tags.SUBTOTAL_MN.trim() + "\"");
				    		   }

				    	   }
				       }else{
					       if(UtilCatalogos.decimalesValidationMsj(tags.SUBTOTAL_MN.trim(), tags.decimalesMoneda)){
//				    		   concat.append(" SubTotal=\"" + UtilCatalogos.decimales(tags.SUBTOTAL_MN.trim(), tags.decimalesMoneda ) + "\"");
				    		   concat.append(" SubTotal=\"" + tags.SUBTOTAL_MN.trim() + "\"");
			    		   }else{
			    			   concat.append(" ErrCompSubTotal003=\"" + tags.SUBTOTAL_MN.trim() + "\"");
			    		   }
				       }
				       
//				       concat.append(" SubTotal=\"" + UtilCatalogos.decimales(tags.SUBTOTAL_MN.trim(), tags.decimalesMoneda ) + "\"");
//				       if(UtilCatalogos.decimalesValidationMsj(tags.SUBTOTAL_MN.trim(), tags.decimalesMoneda)){
////			    		   concat.append(" SubTotal=\"" + UtilCatalogos.decimales(tags.SUBTOTAL_MN.trim(), tags.decimalesMoneda ) + "\"");
//			    		   concat.append(" SubTotal=\"" + tags.SUBTOTAL_MN.trim() + "\"");
//		    		   }else{
//		    			   concat.append(" ElValorDelCampoSubTotalExcedeLaCantidadDeDecimalesQueSoportaLaMoneda=\"" + tags.SUBTOTAL_MN.trim() + "\"");
//		    		   }
				       
//				       //Validando Monto Maximo
//				       try{
//				    	   double valMaximo = Double.parseDouble(UtilCatalogos.findTipoComprobante(tags.mapCatalogos, lineas[1].trim().toUpperCase()));
//				    	   if(valSubTotal > valMaximo){
//				    		   // Se debe mostrar campo Confirmacion, no se sabe si aplica para Estados de Cuenta o Factoraje, esta por definir por parte de Santander
//				    	   }
//				       }catch (NumberFormatException e){
//				    	   System.out.println("No se encontro Valor Maximo en Catalogo TipoComprobante ");
//				       }
				      
				       
				    }
				} catch (NumberFormatException e) {
				    System.out.println("SubTotal: "+  "No es un numero");
				    concat.append(" ErrCompSubTotal004"+  "=\"" + lineas[7].trim() + "\"");
				}
				
				
				// Validacion de no ser negativo el total AMDA
				if(lineas[7] != null){
//					if(lineas[1].trim().toUpperCase().equalsIgnoreCase("T") || lineas[1].trim().toUpperCase().equalsIgnoreCase("P")){
//						if(tags.decimalesMoneda == 0){
//							concat.append(" Total=\"" + "0" + "\"");
//						}else if(tags.decimalesMoneda == 2){
//							concat.append(" Total=\"" + "0.00" + "\"");
//						}else if(tags.decimalesMoneda == 3){
//							concat.append(" Total=\"" + "0.000" + "\"");
//						}else if(tags.decimalesMoneda == 4){
//							concat.append(" Total=\"" + "0.0000" + "\"");
//						}
//						
//					}else{
						System.out.println("Total : " + lineas[7].trim());
						try {
						    double valTotal = Double.parseDouble(lineas[7].trim());
						    if(valTotal<0){
						       System.out.println("Total: " + " es negativo");
						    	concat.append(" ErrCompTotal001"+ valTotal + "=\"" + lineas[7].trim() + "\" ");
						    }else{
						       System.out.println("Total: " + " es positivo");
						       System.out.println("Total agregando decimales : " + tags.decimalesMoneda + " : " + lineas[7].trim());
						       concat.append(" Total=\"" + UtilCatalogos.decimales(lineas[7].trim(), tags.decimalesMoneda ) + "\" ");
						       
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
						    concat.append(" ErrCompTotal002"+  "=\"" + lineas[7].trim() + "\" ");
						}
//					}
					
//					concat.append(" total=\"" + lineas[7].trim() + "\"");
				}else{
					concat.append(" ErrCompTotal002"+  "=\"" + lineas[7].trim() + "\" ");
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
				String formaPagoVal = "";
				String metodoPagoVal = "";
				if(!tags.tipoComprobante.trim().equalsIgnoreCase("T") && !tags.tipoComprobante.trim().equalsIgnoreCase("P")){
					formaPagoVal = UtilCatalogos.findFormaPago(tags.mapCatalogos, "Transferencia electrónica de fondos");
					System.out.println("Forma Pago consulta Catalogos: " + formaPagoVal);
					if(!formaPagoVal.equalsIgnoreCase("vacio")){
						concat.append(" FormaPago=\""
								+ formaPagoVal + "\" "); // Antes PAGO EN UNA SOLA EXHIBICION AMDA V 3.3
					}else{
						concat.append(" ErrCompFormaPago001=\""
								+ formaPagoVal + "\" "); // Antes PAGO EN UNA SOLA EXHIBICION AMDA V 3.3
					}
					
					metodoPagoVal = UtilCatalogos.findMetodoPago(tags.mapCatalogos, "Pago en una sola excibicion");
					System.out.println("Metodo Pago consulta Catalogos: " + metodoPagoVal);
					if(!metodoPagoVal.equalsIgnoreCase("vacio")){
						concat.append(" MetodoPago=\""
								+  metodoPagoVal + "\" "); // Antes properties.getLabelMetodoPago() AMDA V 3.3
					}else{
						concat.append(" ErrCompMetodoPago001=\""
								+  formaPagoVal + "\" "); // Antes properties.getLabelMetodoPago() AMDA V 3.3
					}

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
				valNombre = valNombre.replace("/", "");
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
					String valNombre = lineas[2].trim().replaceAll("\\.", "");
					valNombre = valNombre.replaceAll("\\(", "");
					valNombre = valNombre.replaceAll("\\)", "");
					valNombre = valNombre.replace("/", "");
					System.out.println("Receptor Reg: "+valNombre );
					nombreReceptor = " Nombre=\"" + Util.convierte(valNombre) + "\"";
				}				
			}
			
			// Nuevos Atributos AMDA Version 3.3
			String valPais = "";
			String residenciaFiscalReceptor = "";
			String numRegIdTribReceptor = "";
			String usoCFDIReceptor = "";//" UsoCFDI=\"" + "P01" + "\""; // Fijo por el momento
			
			if(!UtilCatalogos.findUsoCfdi(tags.mapCatalogos, "Por definir").equalsIgnoreCase("vacio")){ // Fijo por el momento
				usoCFDIReceptor = " UsoCFDI=\"" + UtilCatalogos.findUsoCfdi(tags.mapCatalogos, "Por definir") + "\"";
			}else{
				usoCFDIReceptor = " ErrCompUsoCFDI001=\"" + UtilCatalogos.findUsoCfdi(tags.mapCatalogos, "Por definir") + "\"";
			}
			
			System.out.println("Receptor recepPais: " + tags.recepPais);
			System.out.println("Receptor RFC Receptor Valida ResidenciaFiscal: "+ tags.RECEPCION_RFC );
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
							residenciaFiscalReceptor = " ErrCompResidenciaFiscal001=\"" + tags.recepPais + "\"";
						}else{
							residenciaFiscalReceptor = " ErrCompResidenciaFiscal002=\"" + tags.recepPais + "\"";
						}
					}else if(valPais.equalsIgnoreCase("MEX")){
						residenciaFiscalReceptor = " ErrCompResidenciaFiscal001=\"" + valPais + "\"";
					}else{
						residenciaFiscalReceptor = " ResidenciaFiscal=\"" + valPais + "\"";
					}

				}
			}
			
			// Validando RFC si es RFC Generico
			if(!tags.RECEPCION_RFC.equalsIgnoreCase("RFCNecesario")){
				
				if(tags.RECEPCION_RFC.equalsIgnoreCase("XEXX010101000") || tags.RECEPCION_RFC.equalsIgnoreCase("XAXX010101000") || tags.RECEPCION_RFC.equalsIgnoreCase("XEXE010101000") || tags.RECEPCION_RFC.equalsIgnoreCase("XEXX010101000") ){
					String valRegIdTrib = UtilCatalogos.findNumRegIdTrib(tags.mapCatalogos, lineas[2].trim()); //
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
//							    	 numRegIdTribReceptor = " ErrReceNumRegIdTrib001=\"" + valRegIdTrib + "\"";
							    	 numRegIdTribReceptor = " ErrReceNumRegIdTrib001=\"" + " " + "\"";
							     }else{
							    	 numRegIdTribReceptor = " NumRegIdTrib=\"" + valRegIdTrib + "\"";
							     }
								
							}
						}
						
					}else{
//						numRegIdTribReceptor = " ErrReceNumRegIdTrib002=\"" + tags.RECEPCION_RFC + "\"";
						numRegIdTribReceptor = " ErrReceNumRegIdTrib002=\"" + " " + "\"";
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
			
			String valDescConcep = "";
			if(lineas[1].trim().length() > 0){
				valDescConcep = Util.convierte(lineas[1]).trim();
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
				Double valUnit = Double.parseDouble(lineas[2].trim());
				if(tags.tipoComprobante.trim().equalsIgnoreCase("I") || tags.tipoComprobante.trim().equalsIgnoreCase("E") || tags.tipoComprobante.trim().equalsIgnoreCase("N")){
					// Valor unitario debe ser mayor a 0
					if(valUnit <= 0){
						valorUnitarioStr = "\" ErrCompValUni001=\"";
//						nodoValorUnitarioStr = "\" valorUnitarioDebeSerMayorDeCero=\"" + valorUnitarioStr ;
						nodoValorUnitarioStr = valorUnitarioStr + "El valor valor del campo ValorUnitario debe ser mayor que cero (0) cuando el tipo de comprobante es Ingreso, Egreso o Nomina" ;
					}else{
						valorUnitarioStr = lineas[2].trim();
						if(UtilCatalogos.decimalesValidationMsj(valorUnitarioStr, tags.decimalesMoneda)){
								nodoValorUnitarioStr = "\" ValorUnitario=\"" + valorUnitarioStr ;
			    		   }else{
			    			   	nodoValorUnitarioStr = "\" ErrCompValUni002=\"" + valorUnitarioStr ;
			    		   }
//						nodoValorUnitarioStr = "\" ValorUnitario=\"" + valorUnitarioStr ;
					}
				}else if(tags.tipoComprobante.trim().equalsIgnoreCase("T")){
					// Valor unitario puede ser mayor o igual a 0
					if(valUnit < 0){
						valorUnitarioStr = "\" ErrCompValUni003=\"";
//						nodoValorUnitarioStr = "\" valorUnitarioDebeSerMenorDeCero=\"" + valorUnitarioStr ;
						nodoValorUnitarioStr = valorUnitarioStr + "El valor valor del campo ValorUnitario debe ser mayor que cero (0) cuando el tipo de comprobante es Traslado" ;
					}else{
						valorUnitarioStr = lineas[2].trim();
						if(UtilCatalogos.decimalesValidationMsj(valorUnitarioStr, tags.decimalesMoneda)){
							nodoValorUnitarioStr = "\" ValorUnitario=\"" + valorUnitarioStr ;
		    		   }else{
		    			   	nodoValorUnitarioStr = "\" ErrCompValUni002=\"" + valorUnitarioStr ;
		    		   }
//						nodoValorUnitarioStr = "\" ValorUnitario=\"" + valorUnitarioStr ;
					}
				}else if(tags.tipoComprobante.trim().equalsIgnoreCase("P")){
					// Valor unitario debe ser igual a 0
					if(valUnit != 0){
						valorUnitarioStr = "\" ErrCompValUni004=\"";
//						nodoValorUnitarioStr = "\" valorUnitarioDebeSerCero=\"" + valorUnitarioStr ;
						nodoValorUnitarioStr =  valorUnitarioStr + "El valor valor del campo ValorUnitario debe ser mayor que cero (0) cuando el tipo de comprobante es Pago" ;
					}else{
						valorUnitarioStr = lineas[2].trim();
						if(UtilCatalogos.decimalesValidationMsj(valorUnitarioStr, tags.decimalesMoneda)){
							nodoValorUnitarioStr = "\" ValorUnitario=\"" + valorUnitarioStr ;
		    		   }else{
		    			   	nodoValorUnitarioStr = "\" ErrCompValUni002=\"" + valorUnitarioStr ;
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
			if(unidadVal.length() > 0){
//				claveUnidad = UtilCatalogos.findValClaveUnidad(tags.mapCatalogos, unidadVal);
				claveUnidad = "E48";
			}
			
			// Importe V 3.3 AMDA pendiente logica de redondeo
			String valImporte = "";
			String lineImporte = "";
			if(lineas[2].trim().length() > 0){
				System.out.println("Importe en Concepto: " + lineas[2].trim());
				valImporte = lineas[2].trim();
				if(UtilCatalogos.decimalesValidationMsj(valImporte, tags.decimalesMoneda)){
					lineImporte = "\" Importe=\"" + valImporte;
				}else{
					lineImporte = "\" ErrConcImport001=\"" + valImporte;
				}
//				valImporte = "\" Importe=\"" + lineas[2].trim();
				
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
				if(tags.retencionImpuestoVal.trim().length() > 0){
					if(!tags.trasladoImpuestoVal.trim().equalsIgnoreCase("ISR")){
						if(tags.trasladoImpuestoVal.trim().length() > 0){
//							tasaOCuotaStr = "\" TasaOCuota=\""  + UtilCatalogos.findValMaxTasaOCuota(tags.mapCatalogos, tags.trasladoImpuestoVal, valTipoFactor);
							tasaOCuotaStr = "\" TasaOCuota=\""  + Util.completeZeroDecimals(UtilCatalogos.findValMaxTasaOCuota(tags.mapCatalogos, tags.trasladoImpuestoVal, valTipoFactor), 6);
						}else{
							tasaOCuotaStr = "\" ErrConcImpuet001=\""  + Util.completeZeroDecimals(UtilCatalogos.findValMaxTasaOCuota(tags.mapCatalogos, tags.trasladoImpuestoVal, valTipoFactor), 6);
						}
					}
				}else{
					tasaOCuotaStr = "\" ElCampoNoContieneUnValorDelCatalogoImpuestoParaTasaOCuotaTraslado=\""  + tags.trasladoImpuestoVal;
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
//			Map<String, Object> trasladoDoom ;
			String elementTraslado = "";
			if(lineas[1].trim().length() > 1){
				Map<String, Object> trasladoDoom = UtilCatalogos.findTraslados(tags.mapCatalogos, valImporte, valDescConcep, tags.decimalesMoneda, tags.tipoComprobante);
				System.out.println("TRASLADO NODOS AMDA : " + trasladoDoom);
				elementTraslado = "\n<cfdi:Traslados>" + 
//										 "\n<cfdi:Traslado Base=\"" + valorBase +
//										 "\" Impuesto=\"" + claveImp +
//										 "\" TipoFactor=\"" + valTipoFactor + // Por definir de donde tomar el valor AMDA
//										 tasaOCuotaStr +
//										 valImporteImpTras +
//										 " />" +
										 trasladoDoom.get("valNodoStr") +
										 "\n</cfdi:Traslados>";		
				tags.sumTotalImpuestosTras = trasladoDoom.get("sumaTotal").toString();
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
//					System.out.println("TRASLADO NODOS AMDA SumISR Double : " + tags.sumTraTotalIsrDou);
					Double sumTraTotalIvaDou = Double.parseDouble(tags.sumTraTotalIva);
					tags.sumTraTotalIvaDou = tags.sumTraTotalIvaDou + sumTraTotalIvaDou;
					tags.sumTraTotalIva = tags.sumTraTotalIvaDou.toString();
//					System.out.println("TRASLADO NODOS AMDA SumIVA Double : " + tags.sumTraTotalIvaDou);
					Double sumTraTotalIepsDou = Double.parseDouble(tags.sumTraTotalIeps);
					tags.sumTraTotalIepsDou = tags.sumTraTotalIepsDou + sumTraTotalIepsDou;
					tags.sumTraTotalIeps = tags.sumTraTotalIepsDou.toString();
//					System.out.println("TRASLADO NODOS AMDA SumIEPS Double : " + tags.sumTraTotalIepsDou);
				}catch(NumberFormatException e){
					System.out.println("Calculando TRASLADO Sumas AMDA Error Numerico");
				}
				
			}else{
				elementTraslado = "\n<cfdi:Traslados>" + 
						 "\n<cfdi:Traslado NoSeEncontroUnConceptoTrasladosParaBuscar=\"" + lineas[1].trim() + "\"" +
						 " />" +
						 "\n</cfdi:Traslados>";	
			}

			System.out.println("Elemento Traslado AMDA : " + elementTraslado);
			System.out.println("Elemento Traslado Length AMDA : " + elementTraslado.length()); // 35 Default
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
				System.out.println("Valor Importe Ret AMDA R : " + tags.retencionImporteVal + " : " + valImporteRetencion);
				if(tags.retencionImporteVal.trim().length() > 0){
					valImporteImpRet = "\" Importe=\"" +tags.retencionImporteVal.trim() + "\"";
				}else{
					valImporteImpRet = "\" Importe=\"" + "0.00" + "\"";
				}
			}
			
			String elementRetencion = "";
			if(lineas[1].trim().length() > 1){
				Map<String, Object> retencionDoom = UtilCatalogos.findRetencion(tags.mapCatalogos, valImporte, valDescConcep, tags.decimalesMoneda, tags.tipoComprobante);
				elementRetencion = "\n<cfdi:Retenciones>" +
//						 				  "\n<cfdi:Retencion Base=\"" + valorBaseRet +
//						 				  "\" Impuesto=\"" + claveImpRet +
//						 				   "\" TipoFactor=\"" + valTipoFactorRet + // Por definir de donde tomar el valor AMDA
//						 				   tasaOCuotaStrRet +
//						 				   valImporteImpRet +
//						 				  "/>" +
										  retencionDoom.get("valNodoStr") +
										  "\n</cfdi:Retenciones>";
				tags.sumTotalImpuestosReten = retencionDoom.get("sumaTotal").toString();
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
//					System.out.println("TRASLADO NODOS AMDA SumISR Double : " + tags.sumTraTotalIsrDou);
					Double sumRetTotalIvaDou = Double.parseDouble(tags.sumRetTotalIva);
					tags.sumRetTotalIvaDou = tags.sumRetTotalIvaDou + sumRetTotalIvaDou;
					tags.sumRetTotalIva = tags.sumRetTotalIvaDou.toString();
//					System.out.println("TRASLADO NODOS AMDA SumIVA Double : " + tags.sumTraTotalIvaDou);
					Double sumRetTotalIepsDou = Double.parseDouble(tags.sumRetTotalIeps);
					tags.sumRetTotalIepsDou = tags.sumRetTotalIepsDou + sumRetTotalIepsDou;
					tags.sumRetTotalIeps = tags.sumRetTotalIepsDou.toString();
//					System.out.println("TRASLADO NODOS AMDA SumIEPS Double : " + tags.sumTraTotalIepsDou);
				}catch(NumberFormatException e){
					System.out.println("Calculando Retencion Sumas AMDA Error Numerico");
				}
				
			}else{
				elementRetencion = "\n<cfdi:Retenciones>" +
		 				  "\n<cfdi:Retencion NoSeEncontroUnConceptoRetencionesParaBuscar=\"" + valorBaseRet + "\"" +
						  "\n</cfdi:Retenciones>";
			}
			System.out.println("Elemento Retencion AMDA : " + elementRetencion);
			System.out.println("Elemento Retencion Length AMDA : " + elementRetencion.length()); // 39 Default
			System.out.println("TRASLADO NODOS AMDA SumISR Double : " + tags.sumRetTotalIsrDou);
			System.out.println("TRASLADO NODOS AMDA SumIVA Double : " + tags.sumRetTotalIvaDou);
			System.out.println("TRASLADO NODOS AMDA SumIEPS Double : " + tags.sumRetTotalIepsDou);
			
			String claveProdServVal = ""; // Fijo por el momento AMDA
			boolean claveProdServTraslado = false;
			boolean claveProdServRetencion = false;
			if(!UtilCatalogos.findClaveProdServbyDesc(tags.mapCatalogos, "Instituciones bancarias").equalsIgnoreCase("vacio")){
				claveProdServVal = "ClaveProdServ=\"" +UtilCatalogos.findClaveProdServbyDesc(tags.mapCatalogos, "Instituciones bancarias"); // Fijo 84121500 AMDA
				
			}else{
				claveProdServVal = "ElCampoClaveProdServNoContieneUnValorDelCatalogoc_ClaveProdServ=\"" + "vacio"; // Fijo 84121500 AMDA
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
			System.out.println("Elemento Impuestos Total Traslados AMDA : " + tags.sumTotalImpuestosTras);
			System.out.println("Elemento Impuestos Total Retenciones AMDA : " + tags.sumTotalImpuestosReten);
			
			String nodoConcepto = "\n<cfdi:Concepto " + claveProdServVal + 
								  "\" Cantidad=\"" + "1" +
								  "\" ClaveUnidad=\"" + claveUnidad + //Pendiente el valor de ClaveUnidad
								  "\" Unidad=\"" + unidadVal + 
								  "\" Descripcion=\"" + valDescConcep + 
								  nodoValorUnitarioStr + 
								  lineImporte + "\" " + " >"+
								  elementImpuestos +
//								  "\n<cfdi:Impuestos TotalImpuestosTrasladados=\"" + trasladoDoom.get("sumaTotal") +
//								  "\" TotalImpuestosRetenidos=\"" + retencionDoom.get("sumaTotal") + "\" "+ "/>" +
//							   	  "\n</cfdi:Impuestos>" + 
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
			String regFisCon = UtilCatalogos.findRegFiscalCode(tags.mapCatalogos, regVal);
			if(!regFisCon.equalsIgnoreCase("vacio")){
				regimenStr = " RegimenFiscal=\"" + regFisCon + "\" "; // Agregue esto /> para cerrar el nodo de concepto al regresar AMDA
			}else{
				regimenStr = " ElCampoRegimenFiscalNoContieneUnValorDelCatalogoc_RegimenFiscal=\"" + regVal + "\" "; // Agregue esto /> para cerrar el nodo de concepto al regresar AMDA
			}
			
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
		
//		// Elemento Retenciones V 3.3 AMDA
//		String claveImpRet = "";
//		if(tags.retencionImpuestoVal.trim().length() > 0){ // Validando el codigo del Impuesto
//			System.out.println("Valor Impuesto Ret AMDA : " + tags.retencionImpuestoVal);
//			claveImpRet = UtilCatalogos.findValClaveImpuesto(tags.mapCatalogos, tags.retencionImpuestoVal);
//			System.out.println("Valor Clave Impuesto Ret AMDA : " + claveImpRet);
//		}
//		
//		String elementRetencion = "\n<cfdi:Retenciones>" +
//				  				  "\n<cfdi:Retencion Impuesto=\"" + claveImpRet +
//				  				  "\" Importe=\"" + tags.retencionImporteVal + "\"" +
//				  "/>" +
//				  "\n</cfdi:Retenciones>";
//		System.out.println("Elemento Retenciones Impuestos AMDA : " + elementRetencion);
		
//		// Elemento Traslados V3.3 AMDA
//		String claveImp = "";
//		String valTipoFactor = "Tasa"; // Por definir de donde tomar el valor AMDA
//		String tasaOCuotaStr = "";
//		String valImporteImpTras = "";
//					
//		if(tags.trasladoImpuestoVal.trim().length() > 0){ // Validando el codigo del Impuesto
//			System.out.println("Valor Impuesto Traslado AMDA : " + tags.trasladoImpuestoVal);
//			claveImp = UtilCatalogos.findValClaveImpuesto(tags.mapCatalogos, tags.trasladoImpuestoVal);
//			System.out.println("Valor Clave Impuesto Traslado AMDA : " + claveImp);
//		}
//					
//		if(valTipoFactor.equalsIgnoreCase("Tasa") || valTipoFactor.equalsIgnoreCase("Cuota")){
//			System.out.println("Validacion TasaOCuota Traslado AMDA : " + tags.trasladoImpuestoVal + " : " + valTipoFactor);
//			if(!tags.trasladoImpuestoVal.trim().equalsIgnoreCase("ISR")){
//
////				tasaOCuotaStr = "\" TasaOCuota=\""  + UtilCatalogos.findValMaxTasaOCuota(tags.mapCatalogos, tags.trasladoImpuestoVal, valTipoFactor);
//
//				tasaOCuotaStr = "\" TasaOCuota=\""  + Util.completeZeroDecimals(UtilCatalogos.findValMaxTasaOCuota(tags.mapCatalogos, tags.trasladoImpuestoVal, valTipoFactor), 6);
//
//			}
//						
//			System.out.println("Valor TasaOCuota Traslado AMDA : " + tasaOCuotaStr);
//		}
//					
//		if(valTipoFactor.equalsIgnoreCase("Tasa") || valTipoFactor.equalsIgnoreCase("Cuota")){
//			System.out.println("Valor Importe AMDA T : " + tags.trasladoImporteVal + " : " + valImporteTraslado);
//			if(tags.trasladoImporteVal.trim().length() > 0){
//				valImporteImpTras = "\" Importe=\"" +tags.trasladoImporteVal.trim() + "\"";
//			}else{
//				valImporteImpTras = "\" Importe=\"" + "0.00" + "\"";
//			}
//			
//		}
//					
//		String elementTraslado = "\n<cfdi:Traslados>" + 
//								 "\n<cfdi:Traslado Impuesto=\"" + claveImp +
//								 "\" TipoFactor=\"" + valTipoFactor + // Por definir de donde tomar el valor AMDA
//								 tasaOCuotaStr +
//								 valImporteImpTras + // Por definir como se relaciona el importe 
//								 " />" +
//								 "\n</cfdi:Traslados>";
//		System.out.println("Elemento Traslado Impuestos AMDA : " + elementTraslado);
		
		if (lineas.length >= 3) 
		{
			tags.TOTAL_IMP_RET = lineas[1].trim();
			tags.TOTAL_IMP_TRA = lineas[2].trim();
			String totalImpRetLine = "";
//			Double totalImpRet = 0.00;
//			Double tagSumTotalImpuestosRetenDoub = 0.00;
			if(!Util.isNullEmpty(lineas[1].trim())){
//				try{
//					totalImpRet = Double.parseDouble(tags.TOTAL_IMP_RET);
//					if(tags.sumTotalImpuestosReten.length() > 0){
//						tagSumTotalImpuestosRetenDoub = Double.parseDouble(tags.sumTotalImpuestosReten);
//						if(totalImpRet > tagSumTotalImpuestosRetenDoub || totalImpRet < tagSumTotalImpuestosRetenDoub){
//							totalImpRetLine = "\" ElValorDelCampoTotalImpuestosRetenidosDebeSerIgualALaSumaDeLosImportesRegistradosEnElElementoHijoRetencion=\"" + tags.TOTAL_IMP_RET + "\" ";
//						}else{
//							
//						}
//					}
//					
//				}catch(NumberFormatException e){
//					
//				}
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
			Double totalImpTra = 0.00;
			if(!Util.isNullEmpty(lineas[2].trim())){
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

//			if(tags.tipoComprobante.equalsIgnoreCase("T") || tags.tipoComprobante.equalsIgnoreCase("P")){
//				
//			}else{
//				
//			}
			
			return Util
					.conctatArguments(
							tags("", pila),
							"\n<cfdi:Impuestos ",
							totalImpRetLine,
							totalImpTraLine,
//							">",
//							elementRetencion,
//							elementTraslado,
							">").toString().getBytes("UTF-8");
			
//			return Util
//					.conctatArguments(
//							tags("", pila),
//							"\n<cfdi:Impuestos  ",
//							isNullEmpity(lineas[1], "TotalImpuestosRetenidos"),
//							isNullEmpity(lineas[2], "TotalImpuestosTrasladados"),
////							">",
////							elementRetencion,
////							elementTraslado,
//							">").toString().getBytes("UTF-8");
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
			
			// Elemento Traslados V3.3 AMDA
			String claveImp = "";
			String valTipoFactor = "Tasa"; // Por definir de donde tomar el valor AMDA
			String tasaOCuotaStr = "";
			String valImporteImpTras = "";
			String impuestoLine = "";
						
			if(lineas[1].trim().length() > 0){ // Validando el codigo del Impuesto
				System.out.println("Valor Impuesto Traslado AMDA : " + lineas[1].trim());
				claveImp = UtilCatalogos.findValClaveImpuesto(tags.mapCatalogos, lineas[1].trim());
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
				System.out.println("Validacion TasaOCuota Traslado AMDA : " +lineas[1].trim() + " : " + valTipoFactor);
				if(!lineas[1].trim().equalsIgnoreCase("ISR")){

//					tasaOCuotaStr = "\" TasaOCuota=\""  + UtilCatalogos.findValMaxTasaOCuota(tags.mapCatalogos, tags.trasladoImpuestoVal, valTipoFactor);

//					tasaOCuotaStr = "\" TasaOCuota=\""  + Util.completeZeroDecimals(UtilCatalogos.findValMaxTasaOCuotaTraslado(tags.mapCatalogos, lineas[1].trim(), valTipoFactor), 6);
					
					tasaOCuotaResult = UtilCatalogos.findValMaxTasaOCuotaTraslado(tags.mapCatalogos, lineas[1].trim(), valTipoFactor);
					if(!tasaOCuotaResult.equalsIgnoreCase("vacio")){
						tasaOCuotaStr = "\" TasaOCuota=\""  + Util.completeZeroDecimals(tasaOCuotaResult, 6);
					}else{
						tasaOCuotaStr = "\" ElValorSeleccionadoDebeCorresponderAUnValorDelCatalogoDondeLaColumnaImpuestoCorrespondaConElCampoImpuestoYLaColoumnaFactorCorrespondaAlCampoTipoFactor=\""  + tasaOCuotaResult;
					}

				}
							
				System.out.println("Valor TasaOCuota Traslado AMDA : " + tasaOCuotaStr);
			}
						
			if(valTipoFactor.equalsIgnoreCase("Tasa") || valTipoFactor.equalsIgnoreCase("Cuota")){
				System.out.println("Valor Importe AMDA T : " + lineas[3].trim() + " : " + valImporteTraslado);
				System.out.println("Total Importe Traslado Validando total AMDA T : " + tags.TOTAL_IMP_TRA);
				if(lineas[3].trim().length() > 0){
//					valImporteImpTras = "\" Importe=\"" +lineas[3].trim() + "\"";
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
					boolean valid = false;
					try{
						totImpTra = Double.parseDouble(tags.TOTAL_IMP_TRA);
						importeDou = Double.parseDouble(lineas[3].trim());
						double sumtotalTraDou = tags.sumTraTotalIepsDou + tags.sumTraTotalIvaDou + tags.sumTraTotalIsrDou;
						System.out.println("Valor SUMMMM1 Traslados : " + sumtotalTraDou);
						System.out.println("Valor SUMMMM Traslados : " + valConepto);
						System.out.println("Valor SUMMMM2 Traslados : " + importeDou);
						if(!(sumtotalTraDou > importeDou) && !(sumtotalTraDou < importeDou) ){ // ImporteDou:9 Linea 3, Importe del Traslado
							valid = true;
							System.out.println("Importes TRUE TASLADOS : ");
						}
						if(totImpTra > importeDou || totImpTra < importeDou){
							valImporteImpTras = "\" ElValorDelCampoTotalImpuestosTrasladoNoEsIgualALaSumaDeLosImportesRegistradosEnElElementoHijoTraslado=\"" + lineas[3].trim()+ "\" ";
						}else if(!valid){
							valImporteImpTras = "\" ErrImpTraImporte001=\"" + lineas[3].trim() + "\" ";
						}else{
							if(UtilCatalogos.decimalesValidationMsj(lineas[3].trim(), tags.decimalesMoneda)){
								valImporteImpTras = "\" Importe=\"" + lineas[3].trim() + "\" ";
							}else{
								valImporteImpTras = "\" ElValorDelCampoImporteCorrespondienteATrasladoDebeTenerLaCantidadDeDecimalesQueSoportaLaMoneda=\"" + lineas[3].trim() + "\" ";
							}
						}
						
					}catch(NumberFormatException e){
						System.out.println("Total Importe Traslado Validando total AMDA T Error en Convertido a numerico : " + tags.TOTAL_IMP_TRA);
						valImporteImpTras = "\" ElValorDelCampoTotalImpuestosTrasladoNoEsIgualALaSumaDeLosImportesRegistradosEnElElementoHijoTraslado=\"" + lineas[3].trim() + "\" ";
					}
					
				}else{
					valImporteImpTras = "\" ElValorDelCampoImporteCorrespondienteATrasladoDebeTenerLaCantidadDeDecimalesQueSoportaLaMoneda=\"" + lineas[3].trim() + "\" ";
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
							 "\n<cfdi:Traslado" , impuestoLine ,
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
//					.conctatArguments("\n<cfdi:Traslado Impuesto=\"",
//							lineas[1].trim(), "\" Tasa=\"", lineas[2].trim(),
//							"\" Importe=\"", lineas[3].trim(), 
//							"\"/>")
//					.toString().getBytes("UTF-8");
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
			
			// Elemento Retenciones V 3.3 AMDA
			String claveImpRet = "";
			String impuestoLine = "";
			if(lineas[1].trim().length() > 0){ // Validando el codigo del Impuesto
				System.out.println("Valor Impuesto Ret AMDA : " + lineas[1].trim());
				claveImpRet = UtilCatalogos.findValClaveImpuesto(tags.mapCatalogos, lineas[1].trim());
				System.out.println("Valor Clave Impuesto Ret AMDA : " + claveImpRet);
				if(!claveImpRet.equalsIgnoreCase("vacio")){
					impuestoLine = " Impuesto=\"" + claveImpRet;
				}else{
					impuestoLine = " ElCampoImpuestoDeRetencionNoContieneUnValorDelCatalogoc_Impuesto=\"" + claveImpRet;
				}
			}else{
				impuestoLine = " ElCampoImpuestoDeRetencionNoContieneUnValorDelCatalogoc_Impuesto=\"" + "";
			}
			
//			String elementRetencion = "\n<cfdi:Retenciones>" +
//					  				  "\n<cfdi:Retencion Impuesto=\"" + claveImpRet +
//					  				  "\" Importe=\"" + lineas[2].trim() + "\"" +
//					  "/>" +
//					  "\n</cfdi:Retenciones>";
//			System.out.println("Elemento Retenciones Impuestos AMDA : " + elementRetencion);
			String importeLine = "";
			if(tags.atributoTotalImpuestosReten){
				if(lineas[2].trim().length() > 0){
					
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
						System.out.println("Valor Imp Retenidoss : " + lineas[2].trim());
						importeDou = Double.parseDouble(lineas[2].trim());
						System.out.println("Valor SUMMMM Retenidoss : " + valConepto);
						if(!(valConepto > importeDou) && !(valConepto < importeDou) ){
							valid = true;
							System.out.println("Importes TRUE RETENCIONES : ");
						}
						if(totImpRet > importeDou || totImpRet < importeDou){
							importeLine = "\" ElValorDelCampoTotalImpuestosRetenidosDebeSerIgualALaSumaDeLosImportesRegistradosEnElElementoHijoRetencion=\"" + lineas[2].trim();
						}else if(!valid){
							importeLine = "\" ErrImpRetImporte001=\"" + lineas[2].trim();
						}else{
							if(UtilCatalogos.decimalesValidationMsj(lineas[2].trim(), tags.decimalesMoneda)){
								importeLine = "\" Importe=\"" + lineas[2].trim() ;
							}else{
								importeLine = "\" ElValorDelCampoImporteCorrespondienteARetencionDebeTenerLaCantidadDeDecimalesQueSoportaLaMoneda=\"" + lineas[2].trim();
							}
						}
						
					}catch(NumberFormatException e){
						System.out.println("Total Importe Traslado Validando total AMDA T Error en Convertido a numerico : " + tags.TOTAL_IMP_RET);
						importeLine = "\" ElValorDelCampoTotalImpuestosRetenidosDebeSerIgualALaSumaDeLosImportesRegistradosEnElElementoHijoRetencion=\"" + lineas[2].trim();
					}
					
				}else{
					importeLine = "\" ElValorDelCampoImporteCorrespondienteARetencionDebeTenerLaCantidadDeDecimalesQueSoportaLaMoneda=\"" + lineas[2].trim();
				}

			}else{
				importeLine = "\" DebeExistirElAtributoTotalImpuestosRetenidos=\"" + lineas[2].trim();
			}
			
			return Util
					.conctatArguments("\n<cfdi:Retencion", impuestoLine, 
							importeLine, "\"/>").toString().getBytes("UTF-8");
			
//			return Util
//					.conctatArguments("\n<cfdi:Retencion Impuesto=\"", claveImpRet, 
//							importeLine, "\"/>").toString().getBytes("UTF-8");
//			return Util
//					.conctatArguments("\n<cfdi:Retencion Impuesto=\"", claveImpRet, 
//							"\" Importe=\"", lineas[2].trim(), "\"/>").toString().getBytes("UTF-8");
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
			System.out.println("entra LoadInfoV33 Receptor:" + lin[1].trim() );
			break;
		case 5:
			// Domicilio
			System.out.println("entra LoadInfoV33 Domicilio:" + lin[9].trim() );
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
			System.out.println("entra LoadInfoV33 RetencionesBack: "+ lin[1].trim() + " : " + lin[2].trim());
//			tags.retencionImpuestoVal = lineas[1].trim();
//			tags.retencionImporteVal = lineas[2].trim(); // Se comenta por que al parecer se recorro uno despues AMDA
			break;
		case 9:
			// Traslados
			System.out.println("entra LoadInfoV33 TrasladosBack: "+ lin[1].trim() + " : " + lin[2].trim() + " : " + lin[3].trim());
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
