package com.interfactura.firmalocal.xml.util;


import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.interfactura.firmalocal.xml.CatalogosDom;
import com.jcraft.jsch.Logger;

public class UtilValidationsXML 
{
	
	// Validando TipoDeComprobante
	public static Map<String, Object> validTipoComprobante(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
		
		Map<String, Object> responseMap = new LinkedHashMap<String, Object>();
		 System.out.println("Entrando a validTipoComprobante");
		 System.out.println("Size lista catalogos; "+ mapCatalogos.size());
		 System.out.println("Valor del tipoDeComprobante a buscar: "+ value);
		 
		 if(value != null && value.trim().length() > 0){
			 
//			 if(value.equalsIgnoreCase("I")){
//				 value = "Ingreso";
//			 }else if(value.equalsIgnoreCase("E")){
//				 value = "Egreso";
//			 }else if(value.equalsIgnoreCase("N")){
//				 value = "Nomina";
//			 }else if(value.equalsIgnoreCase("T")){
//				 value = "Traslado";
//			 }else if(value.equalsIgnoreCase("P")){
//				 value = "Pago";
//			 }
			 
			 System.out.println("Valor A Encontrar tipoDeComprobante: "+ value);
			 String tipoComp = UtilCatalogos.findTipoComprobanteByDescription(mapCatalogos, value);
			 System.out.println("Valor Encontrado tipoDeComprobante: "+ tipoComp);
			 if(!tipoComp.equalsIgnoreCase("tipoDeComprobanteIncorrecto")){
				 responseMap.put("value", tipoComp);
				 responseMap.put("message", "");
			 }else{
				 responseMap.put("value", tipoComp);
				 responseMap.put("message", " (CFDI33120) El campo TipoDeComprobante, no contiene un valor del catalogo c_TipoDeComprobante");
			 }
		 }
		 System.out.println("Respuesta De validTipoComporbante: "+ responseMap.get("value"));
		 System.out.println("Respuesta De validTipoComporbante: "+ responseMap.get("message"));
		return responseMap;
		
	}
	
	// Validando Moneda
	public static Map<String, Object> validMoneda(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
		
		Map<String, Object> responseMap = new LinkedHashMap<String, Object>();
		 System.out.println("Entrando a validMoneda");
		 System.out.println("Size lista catalogos; "+ mapCatalogos.size());
		 System.out.println("Valor del Moneda a buscar: "+ value);
		 if(value != null && value.trim().length() > 0){
			 System.out.println("Valor del Moneda antes de Normalizer: "+ value);
			 String stringa = Normalizer.normalize(value, Normalizer.Form.NFD);
				String stringU = stringa.replaceAll("[^\\p{ASCII}]", "");
				if(stringU.trim().length() < value.trim().length()){
					System.out.println("Valor del Moneda a buscar Dentro Acento: "+ stringU);
					value = UtilCatalogos.findStringAcento(value);
				}
				System.out.println("Valor del Moneda a findMonedaCatalogo: "+ value);
				String valEqMoneda = UtilCatalogos.findMonedaCatalogo(mapCatalogos, value);
//				System.out.println("Tipo Moneda AMDA FindAcento: " + tags.TIPO_MONEDA + " : " + tags.SERIE_FISCAL_CFD);
//				valEqMoneda = UtilCatalogos.findEquivalenciaMoneda(tags.mapCatalogos, tags.TIPO_MONEDA);
				System.out.println("Valor del Moneda a findMonedaCatalogo Despues: "+ valEqMoneda);
				if(!valEqMoneda.equalsIgnoreCase("vacio")){
					responseMap.put("value", valEqMoneda);
					responseMap.put("message", "");
//					System.out.println("Tipo Moneda AMDA If True: " + tags.TIPO_MONEDA + " : " + tags.SERIE_FISCAL_CFD);
//					tags.TIPO_MONEDA = UtilCatalogos.findStringAcento(tags.TIPO_MONEDA);
//					System.out.println("Tipo Moneda AMDA FindAcento: " + tags.TIPO_MONEDA + " : " + tags.SERIE_FISCAL_CFD);
				}else{
//					System.out.println("Tipo Moneda AMDA ELSE: " + tags.TIPO_MONEDA + " : " + tags.SERIE_FISCAL_CFD);
					System.out.println("Valor del Moneda a findEquivalenciaMoneda Antes: "+ value);
					valEqMoneda = UtilCatalogos.findEquivalenciaMoneda(mapCatalogos, value);
					System.out.println("Valor del Moneda a findEquivalenciaMoneda Despues: "+ value);
					if(!valEqMoneda.equalsIgnoreCase("vacio")){
						responseMap.put("value", valEqMoneda);
						responseMap.put("message", "");
					}else{
						responseMap.put("value", valEqMoneda);
						responseMap.put("message", " (CFDI33112) El campo Moneda no contiene un valor del catalogo c_Moneda.");
					}	
				}
		 }else{
			 	responseMap.put("value", "vacio");
				responseMap.put("message", " (CFDI33112) El campo Moneda no contiene un valor del catalogo c_Moneda.");
		 }
		 System.out.println("Respuesta De validMoneda: "+ responseMap.get("value"));
		 System.out.println("Respuesta De validMoneda: "+ responseMap.get("message"));
		return responseMap;
		
	}
	
	// Validando TipoDeComprobante
	public static Map<String, Object> validTipoCambio(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value, String tipoMoneda){
		
		Map<String, Object> responseMap = new LinkedHashMap<String, Object>();
		 System.out.println("Entrando a validTipoCambio");
		 System.out.println("Size lista catalogos; "+ mapCatalogos.size());
		 System.out.println("Valor del validTipoCambio a buscar: "+ value);
		 String patternReg = "";
		 double valueTipoCambioDoubl = 0.00;
		 if(value.trim().length() > 0){
				patternReg = "[0-9]{1,14}(.([0-9]{1,6}))";
				
				if(!patternReg.trim().equalsIgnoreCase("vacio") && patternReg.trim().length() > 0){
//					System.out.println("Validando PATTERN REGEX Para tipo de Cambio : " + tags.TIPO_CAMBIO);
					Pattern p = Pattern.compile(patternReg);
					 Matcher m = p.matcher(value);
				     
				     if(!m.find()){
				    	 //TipoComprobante no valido
//				    	 concat.append(" ErrCompTipMon001" + "=\"" + tags.TIPO_CAMBIO + "\"");
				    	 responseMap.put("value", "vacio");
				    	 responseMap.put("message", " (CFDI33116) El Campo Tipo Cambio No Cumple Con El Patron Requerido");
				     }else{
				    	 
				    	 responseMap.put("value", value);
				    	 responseMap.put("message", "");
				    	 
//				    	 try{
//								valueTipoCambioDoubl = Double.parseDouble(value);
//							}catch (NumberFormatException e){
////								concat.append(" ErrCompTipMon004" + "=\"" + tags.TIPO_CAMBIO + "\"");
//								responseMap.put("value", "vacio");
//						    	responseMap.put("message", " Tipo Cambio Incorrecto");
//							}
//				    	 
//							if(!tipoMoneda.equalsIgnoreCase("MXN") && !tipoMoneda.equalsIgnoreCase("XXX")){ //Validacion AMDA
//								String resultadoRipoCam = UtilCatalogos.findTipoCambioPorcentaje(mapCatalogos, tipoMoneda, value);
//							    if(resultadoRipoCam.equalsIgnoreCase("OK")){
////							       System.out.println("Tipo Cambio Bien");
////							       concat.append(" TipoCambio=\"" + tags.TIPO_CAMBIO + "\"");
//							       responseMap.put("value", value);
//							       responseMap.put("message", "");
//							    }else{
////							    	System.out.println("Tipo Cambio No dentro de limites");
////							    	concat.append(" ErrCompTipMon002" + "=\"" + tags.TIPO_CAMBIO + "\"");
//							    	responseMap.put("value", "vacio");
//								    responseMap.put("message", "El Campo Tipo Cambio No Esta Dentro De Los Limites");
//							    }
//
//							}else if(tipoMoneda.equalsIgnoreCase("MXN")){
////								 System.out.println("Cuendo es MXN La Moneda AMDA" + valueTipoCambioDoubl + " : " + tags.TIPO_CAMBIO);
//								 if(valueTipoCambioDoubl > 1 || valueTipoCambioDoubl < 1){
////										concat.append(" ErrCompTipMon003" + "=\"" + tags.TIPO_CAMBIO + "\"");
//										responseMap.put("value", "vacio");
//									    responseMap.put("message", " (CFDI33113) El Campo Tipo Cambio No Tiene El Valor 1 Y La Moneda Indicada Es MXN");
//								 }else{
////										concat.append(" TipoCambio=\"" + tags.TIPO_CAMBIO + "\""); 
//										responseMap.put("value", value);
//									    responseMap.put("message", "");
//								 }
//							}else if(tipoMoneda.equalsIgnoreCase("XXX")){
////								 System.out.println("Cuendo es XXX La Moneda AMDA");
////								concat.append(" TipoCambio=\"" + "1.00"+ "\"");
//							}else{
////								concat.append(" TipoCambio=\"" + tags.TIPO_CAMBIO + "\"");
//							} // Termina Validacion AMDA	
				    	 
				     }
					
				}
			}else if(tipoMoneda.equalsIgnoreCase("MXN")){
//				 concat.append(" ErrCompTipMon003" + "=\"" + tags.TIPO_CAMBIO + "\"");
				 responseMap.put("value", "vacio");
				 responseMap.put("message", " (CFDI33113) El Campo Tipo Cambio No Tiene El Valor 1 Y La Moneda Indicada Es MXN");
			}else if(!tipoMoneda.equalsIgnoreCase("MXN") && !tipoMoneda.equalsIgnoreCase("XXX")){
//				concat.append(" ErrCompTipCam001" + "=\"" + tags.TIPO_CAMBIO + "\"");
				responseMap.put("value", "vacio");
				responseMap.put("message", " (CFDI33116) El Campo Tipo Cambio No Cumple Con El Patron Requerido");
			}
		 
		 System.out.println("Respuesta De validTipoCambio: "+ responseMap.get("value"));
		 System.out.println("Respuesta De validTipoCambio: "+ responseMap.get("message"));
		return responseMap;
		
	}
	
	
	// Validando RFC para NumRegIdTrib
	public static Map<String, Object> validRFCNumRegIdTrib(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value, String pais, String idExtranjero, String nomRecep, String numRegIdTribStr){
		
		Map<String, Object> responseMap = new LinkedHashMap<String, Object>();
		 System.out.println("Entrando a validRFCNumRegIdTrib");
		 System.out.println("Size lista catalogos; "+ mapCatalogos.size());
		 System.out.println("RFC De RECEPTOR VAL; "+ mapCatalogos.size());
		 System.out.println("Valor del Pais a buscar: "+ pais);
		 System.out.println("Valor del IdExtranjero a buscar: "+ idExtranjero);
		 if(numRegIdTribStr != null && numRegIdTribStr.trim().length() > 0){
			 if(value != null && value.trim().length() > 0 && pais != null && pais.trim().length() > 0 && nomRecep != null && nomRecep.trim().length() > 0 && numRegIdTribStr != null && numRegIdTribStr.trim().length() > 0){
				 if(!value.equalsIgnoreCase("RFCNecesario")){
						
						if(value.equalsIgnoreCase("XEXX010101000") || value.equalsIgnoreCase("XEXE010101000") ){ // AMDA se quito el RFC  XAXX010101000
							System.out.println("Recepcion RFC DO:  " + value);
//							System.out.println("Recepcion RFC DO:  " + tokens[2].trim());
//							System.out.println("Recepcion RFC valPais:  " + valPais);
							String valRegIdTrib = "";
//							if(idExtDoc != null && idExtDoc.length() > 0){
//								valRegIdTrib = idExtDoc;
//							}else{
//								valRegIdTrib = UtilCatalogos.findNumRegIdTrib(mapCatalogos, nomRecep);
								valRegIdTrib = numRegIdTribStr ;
//							}
							
//							if(pais.trim().length() > 3){
//								String stringa = Normalizer.normalize(pais, Normalizer.Form.NFD);
//								String stringU = stringa.replaceAll("[^\\p{ASCII}]", "");
//								if(stringU.trim().length() < pais.trim().length()){
//									System.out.println("Valor del Pais a buscar Dentro Acento: "+ stringU);
//									pais = UtilCatalogos.findStringAcento(pais);
//								}
//							}
							
							if(!valRegIdTrib.equalsIgnoreCase("vacio") && valRegIdTrib.equalsIgnoreCase(numRegIdTribStr) ){
//								numRegIdTribReceptor = " NumRegIdTrib=\"" + valRegIdTrib + "\"";
								
								//Valida Num RegIdTrib
//								String patternReg = "";
//								if(!pais.trim().equalsIgnoreCase("vacio") && pais.trim().length() > 0){
//									patternReg = UtilCatalogos.findPatternRFCPais(mapCatalogos, pais);
//									System.out.println("PATTERN REGEX:  " + patternReg);
//									if(!patternReg.trim().equalsIgnoreCase("vacio") && patternReg.trim().length() > 0){
//										System.out.println("Validando PATTERN REGEX");
//										Pattern p = Pattern.compile(patternReg);
//										 Matcher m = p.matcher(valRegIdTrib);
//									     
//									     if(!m.find()){
//									    	 //RFC no valido
////									    	 System.out.println("PATTERN REGEX NO ES Valido el RegIdTrib:  " + valRegIdTrib + " : " + valPais + " : " + patternReg);
////									    	 numRegIdTribReceptor = " ErrRecRegId001=\"" + valRegIdTrib + "\"";
//									    	 responseMap.put("value", "vacio");
//									    	 responseMap.put("message", " (CFDI33139) El Valor Registro NumRegIdTrib No Cumple Con El Patron Correspondiente");
//									     }else{
//									    	 // Todo PAso OK
////									    	 numRegIdTribReceptor = " NumRegIdTrib=\"" + valRegIdTrib + "\"";
//									    	 responseMap.put("value", valRegIdTrib);
//									    	 responseMap.put("message", "");
//									     }
//										
//									}else{
										// Todo Paso OK el pais no tenia validacion del Id Extranjero
										System.out.println("Else OOO:  " + valRegIdTrib);
//										numRegIdTribReceptor = " NumRegIdTrib=\"" + valRegIdTrib + "\"";
										responseMap.put("value", valRegIdTrib);
								    	responseMap.put("message", "");
//									}
//								}else{
//									System.out.println("Else ���:  " + valRegIdTrib);
////									numRegIdTribReceptor = " NumRegIdTrib=\"" + valRegIdTrib + "\"";
//									responseMap.put("value", "vacio");
//							    	responseMap.put("message", "El Pais es Requerido");
//								}
								
							}else{
//								numRegIdTribReceptor = " ErrRecRegId002=\"" + tags.RECEPCION_RFC + "\"";
								responseMap.put("value", "vacio");
						    	responseMap.put("message", "No Se Ha Encontrado El Receptor Relacionado Con NumRegIdTrib El RFC Del Receptor Debe De Ser Un Generico Extranjero");
							}
							
						}
						
					}
			 }else{
				 if(value == null || value.trim().length() < 1){
					 responseMap.put("value", "vacio");
				     responseMap.put("message", "No Se Ha Encontrado El Receptor Relacionado Con NumRegIdTrib por que ID del Extranjero viene vacio");
				 }else if(pais == null || pais.trim().length() < 1){
					 responseMap.put("value", "vacio");
				     responseMap.put("message", "No Se Ha Encontrado El Receptor Relacionado Con NumRegIdTrib por que el Pais del cliente viene vacio");
//				 }else if(idExtranjero == null || idExtranjero.trim().length() < 1){
//					 responseMap.put("value", "vacio");
//				     responseMap.put("message", "No Se Ha Encontrado El Receptor Relacionado Con NumRegIdTrib por que el Id Extranjero del cliente viene vacio");
				 }else if(nomRecep == null || nomRecep.trim().length() < 1){
					 responseMap.put("value", "vacio");
				     responseMap.put("message", "No Se Ha Encontrado El Receptor Relacionado Con NumRegIdTrib por que el Nombre del cliente viene vacio");
				 }else if(numRegIdTribStr == null || numRegIdTribStr.trim().length() < 1){
					 responseMap.put("value", "vacio");
				     responseMap.put("message", "No Se Ha Encontrado El Receptor Relacionado Con NumRegIdTrib por que el Numero de Registro Id Tributario del cliente viene vacio");
				 }else if(numRegIdTribStr == null || numRegIdTribStr.trim().length() < 1){
					 if(idExtranjero != null && idExtranjero.trim().length() > 0 && value != null && value.trim().length() > 0){
						 responseMap.put("value", "vacio");
					     responseMap.put("message", "El campo NumRegIdTrib es requerido"); 
					 }
					 
				 }else{
					 responseMap.put("value", "vacio");
				    responseMap.put("message", "No Se Ha Encontrado El Receptor Relacionado Con NumRegIdTrib");
				 }
				 
			 }
		 }else{
			 responseMap.put("value", "vacio");
			    responseMap.put("message", "El Campo NumRegIdTrib es requerido");
		 }
		 
		 System.out.println("Respuesta De validRFCNumRegIdTrib: "+ responseMap.get("value"));
		 System.out.println("Respuesta De validRFCNumRegIdTrib: "+ responseMap.get("message"));
		return responseMap;
		
	}
	
	// Validando Nombre del Receptor
	public static Map<String, Object> validNombreRecep(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
		
		Map<String, Object> responseMap = new LinkedHashMap<String, Object>();
		 System.out.println("Entrando a validNombreRecep");
		 System.out.println("Size lista catalogos; "+ mapCatalogos.size());
		 System.out.println("Valor del validNombreRecep a buscar: "+ value);
		 if(value != null && value.trim().length() > 0){
					String valNombre = value.trim().replaceAll("\\.", "");
					valNombre = valNombre.replaceAll("\\(", "");
					valNombre = valNombre.replaceAll("\\)", "");
					valNombre = valNombre.replace("/", "");
//					nombreReceptor = " Nombre=\"" + Util.convierte(valNombre) + "\"";
					responseMap.put("value", valNombre);
					responseMap.put("message", "");
		 }else{
			 responseMap.put("value", "vacio");
			 responseMap.put("message", "El Campo Nombre viene vacio");
		 }
		 System.out.println("Respuesta De validNombreRecep: "+ responseMap.get("value"));
		 System.out.println("Respuesta De validNombreRecep: "+ responseMap.get("message"));
		return responseMap;
		
	}
	
	// Validando Metodo De Pago
	public static Map<String, Object> validMetodPago(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
		
		Map<String, Object> responseMap = new LinkedHashMap<String, Object>();
		 System.out.println("Entrando a validMetodPago");
		 System.out.println("Size lista catalogos; "+ mapCatalogos.size());
		 System.out.println("Valor del validMetodPago a buscar: "+ value);
		 if(value != null && value.trim().length() > 0){
			 
			 if(value.trim().length() > 3){
					String stringa = Normalizer.normalize(value, Normalizer.Form.NFD);
					String stringU = stringa.replaceAll("[^\\p{ASCII}]", "");
					if(stringU.trim().length() < value.trim().length()){
						System.out.println("Valor MetodoPago a buscar Dentro Acento: "+ stringU);
						value = UtilCatalogos.findStringAcento(value);
					}
				}
			 
//			 if(value.equalsIgnoreCase("PUE")){
//				 responseMap.put("value", value);
//				 responseMap.put("message", "");
//			 }else{
				 String metodoPagoVal = UtilCatalogos.findMetodoPago(mapCatalogos, value);
				 if(!metodoPagoVal.equalsIgnoreCase("vacio")){
					 responseMap.put("value", metodoPagoVal);
					 responseMap.put("message", "");
				 }else{
					 responseMap.put("value", "vacio");
					 responseMap.put("message", " (CFDI33121) El Campo Metodo Pago No Contiene Un Valor Del Catalogo C_MetodoPago");
				 }
//			 }

		 }else{
			 responseMap.put("value", "vacio");
			 responseMap.put("message", "(CFDI33121) El Campo Metodo Pago No Contiene Un Valor Del Catalogo C_MetodoPago");
		 }
		 System.out.println("Respuesta De validMetodPago: "+ responseMap.get("value"));
		 System.out.println("Respuesta De validMetodPago: "+ responseMap.get("message"));
		return responseMap;
		
	}
	
	// Validando Metodo De Pago
		public static Map<String, Object> validRegFiscal(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			
			Map<String, Object> responseMap = new LinkedHashMap<String, Object>();
			 System.out.println("Entrando a validRegFiscal");
			 System.out.println("Size lista catalogos; "+ mapCatalogos.size());
			 System.out.println("Valor del validRegFiscal a buscar: "+ value);
			 if(value != null && value.trim().length() > 0){
//				 if(value.equalsIgnoreCase("609")){
//					 responseMap.put("value", value);
//					 responseMap.put("message", "");
//				 }else{
					 String regFisCon = UtilCatalogos.findRegFiscalCode(mapCatalogos, value);
					 if(!regFisCon.equalsIgnoreCase("vacio")){
						 responseMap.put("value", regFisCon);
						 responseMap.put("message", "");
					 }else{
						 responseMap.put("value", "vacio");
						 responseMap.put("message", " (CFDI33130) El campo RegimenFiscal, no contiene un valor del cat�logo c_RegimenFiscal");
					 }
//				 }
				 
			 }else{
				 responseMap.put("value", "vacio");
				 responseMap.put("message", " (CFDI33130) El campo RegimenFiscal, no contiene un valor del cat�logo c_RegimenFiscal");
			 }
			 System.out.println("Respuesta De validRegFiscal: "+ responseMap.get("value"));
			 System.out.println("Respuesta De validRegFiscal: "+ responseMap.get("message"));
			return responseMap;
			
		}
		
		// Validando Forma De Pago
		public static Map<String, Object> validFormaPago(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			
			Map<String, Object> responseMap = new LinkedHashMap<String, Object>();
			 System.out.println("Entrando a validFormaPago");
			 System.out.println("Size lista catalogos; "+ mapCatalogos.size());
			 System.out.println("Valor del validFormaPago a buscar: "+ value);
			 if(value != null && value.trim().length() > 0){
				 
				 if(value.trim().length() > 2){
						String stringa = Normalizer.normalize(value, Normalizer.Form.NFD);
						String stringU = stringa.replaceAll("[^\\p{ASCII}]", "");
						if(stringU.trim().length() < value.trim().length()){
							System.out.println("Valor FormaPago a buscar Dentro Acento: "+ stringU);
							value = UtilCatalogos.findStringAcento(value);
						}
					}
				 
//				 if(value.length() == 2){
//					 responseMap.put("value", value);
//					 responseMap.put("message", "");
//				 }else{
					 String formaPagoVal = UtilCatalogos.findFormaPago(mapCatalogos, value);
					 if(!formaPagoVal.equalsIgnoreCase("vacio")){
						 responseMap.put("value", formaPagoVal);
						 responseMap.put("message", "");
					 }else{
						 responseMap.put("value", "vacio");
						 responseMap.put("message", " (CFDI33103) El Campo Forma Pago No Contiene Un Valor Del Catalogo C_FormaPago");
					 }
//				 }
				 
			 }else{
				 responseMap.put("value", "vacio");
				 responseMap.put("message", "(CFDI33103) El Campo Metodo Pago No Contiene Un Valor Del Catalogo C_FormaPago");
			 }
			 System.out.println("Respuesta De validFormaPago: "+ responseMap.get("value"));
			 System.out.println("Respuesta De validFormaPago: "+ responseMap.get("message"));
			return responseMap;
			
		}
		
		// Validando UsoCFDI
		public static Map<String, Object> validUsoCFDI(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			
			Map<String, Object> responseMap = new LinkedHashMap<String, Object>();
			 System.out.println("Entrando a validUsoCFDI");
			 System.out.println("Size lista catalogos; "+ mapCatalogos.size());
			 System.out.println("Valor del validUsoCFDI a buscar: "+ value); 
			 if(value != null && value.trim().length() > 0){
//				 if(value.length() == 3){
//					 responseMap.put("value", value);
//					 responseMap.put("message", "");
//				 }else{
					 String usoCFDI = UtilCatalogos.findUsoCfdi(mapCatalogos, value);
					 if(!usoCFDI.equalsIgnoreCase("vacio")){
						 responseMap.put("value", usoCFDI);
						 responseMap.put("message", "");
					 }else{
						 responseMap.put("value", "vacio");
						 responseMap.put("message", " CFDI33140 El Campo UsoCFDI No Contiene Un Valor Del Catalogo c_UsoCFDI");
					 }
//				 }
				 
			 }else{
				 responseMap.put("value", "vacio");
				 responseMap.put("message", "CFDI33140 El Campo UsoCFDI No Contiene Un Valor Del Catalogo c_UsoCFDI");
			 }
			 System.out.println("Respuesta De validUsoCFDI: "+ responseMap.get("value"));
			 System.out.println("Respuesta De validUsoCFDI: "+ responseMap.get("message"));
			return responseMap;
			
		}
		
		// Validando Pais
		public static Map<String, Object> validPais(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			
		Map<String, Object> responseMap = new LinkedHashMap<String, Object>();
		System.out.println("Entrando a validPais");
		System.out.println("Size lista catalogos; " + mapCatalogos.size());
		System.out.println("Valor del validPais a buscar: " + value);
		if (value != null && value.trim().length() > 0) {

			if (value.trim().length() > 3) {
				String stringa = Normalizer.normalize(value,Normalizer.Form.NFD);
				String stringU = stringa.replaceAll("[^\\p{ASCII}]", "");
				if (stringU.trim().length() < value.trim().length()) {
					System.out.println("Valor del Pais a buscar Dentro Acento: "+ stringU);
					value = UtilCatalogos.findStringAcento(value);
				}
			}
//			if (value.trim().length() == 3) {
//				responseMap.put("value", value);
//				responseMap.put("message", "");
//			} else {
				System.out.println("Valor del Pais Validationssss: " + value);
				String valPais = UtilCatalogos.findValPais(mapCatalogos, value);
				System.out.println("Valor Abreviado Pais: " + valPais);
				if (valPais.equalsIgnoreCase("vacio")) {
					valPais = UtilCatalogos.findEquivalenciaPais(mapCatalogos, value);
					System.out.println("Valor Equivalencia Abreviado Pais: " + valPais);
					if (!valPais.equalsIgnoreCase("vacio")) {
						responseMap.put("value", valPais);
						responseMap.put("message", "");
					}else {
						responseMap.put("value", "vacio");
						responseMap
								.put("message",
										" CFDI33133 El Campo Pais No Contiene Un Valor Del Catalogo c_Pais");
					}
				} else {
					responseMap.put("value", valPais);
					responseMap.put("message", "");
				}
//			}

		} else {
			responseMap.put("value", "vacio");
			responseMap.put("message", " CFDI33133 El Campo Pais No Contiene Un Valor Del Catalogo c_Pais");
		}
		System.out.println("Respuesta De validPais: " + responseMap.get("value"));
		System.out.println("Respuesta De validPais: " + responseMap.get("message"));
		return responseMap;
			
		}
		
		// Validando ClaveProdServ
		public static Map<String, Object> validClaveProdServ(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			
			Map<String, Object> responseMap = new LinkedHashMap<String, Object>();
			 System.out.println("Entrando a validClaveProdServ");
			 System.out.println("Size lista catalogos; "+ mapCatalogos.size());
			 System.out.println("Valor del validClaveProdServ a buscar: "+ value); 
			 if(value != null && value.trim().length() > 0){
//				 if(value.length() == 3){
//					 responseMap.put("value", value);
//					 responseMap.put("message", "");
//				 }else{
					 String usoCFDI = UtilCatalogos.findClaveProdServbyDesc(mapCatalogos, value);
					 if(!usoCFDI.equalsIgnoreCase("vacio")){
						 responseMap.put("value", usoCFDI);
						 responseMap.put("message", "");
					 }else{
						 responseMap.put("value", "vacio");
						 responseMap.put("message", " El Campo ClaveProdServ No Contiene Un Valor Del Catalogo c_ClaveProdServ");
					 }
//				 }
				 
			 }else{
				 responseMap.put("value", "vacio");
				 responseMap.put("message", " El Campo ClaveProdServ No Contiene Un Valor Del Catalogo c_ClaveProdServ");
			 }
			 System.out.println("Respuesta De validClaveProdServ: "+ responseMap.get("value"));
			 System.out.println("Respuesta De validClaveProdServ: "+ responseMap.get("message"));
			return responseMap;
			
		}
		
		// Validando ClaveUnidad
		public static Map<String, Object> validClaveUnidad(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			
			Map<String, Object> responseMap = new LinkedHashMap<String, Object>();
			 System.out.println("Entrando a validClaveUnidad");
			 System.out.println("Size lista catalogos; "+ mapCatalogos.size());
			 System.out.println("Valor del validClaveUnidad a buscar: "+ value); 
			 if(value != null && value.trim().length() > 0){
//				 if(value.length() == 3){
//					 responseMap.put("value", value);
//					 responseMap.put("message", "");
//				 }else{
					 String usoCFDI = UtilCatalogos.findValClaveUnidad(mapCatalogos, value);
					 if(!usoCFDI.equalsIgnoreCase("vacio")){
						 responseMap.put("value", usoCFDI);
						 responseMap.put("message", "");
					 }else{
						 responseMap.put("value", "vacio");
						 responseMap.put("message", " El Campo ClaveUnidad No Contiene Un Valor Del Catalogo c_CalveUnidad");
					 }
//				 }
				 
			 }else{
				 responseMap.put("value", "vacio");
				 responseMap.put("message", "El Campo ClaveUnidad No Contiene Un Valor Del Catalogo c_CalveUnidad");
			 }
			 System.out.println("Respuesta De validClaveUnidad: "+ responseMap.get("value"));
			 System.out.println("Respuesta De validClaveUnidad: "+ responseMap.get("message"));
			return responseMap;
			
		}
	 
		// Validando TasaOCuota Traslado
		public static Map<String, Object> validTasaOCuotaTra(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String descImp, String desc, String value, String descTraORet){
			
			Map<String, Object> responseMap = new LinkedHashMap<String, Object>();
			 System.out.println("Entrando a validTasaOCuotaTra");
			 System.out.println("Size lista catalogos; "+ mapCatalogos.size());
			 System.out.println("Valor del validTasaOCuotaTra a buscar: "+ value);
			 System.out.println("Valor del Impuesto validTasaOCuotaTra a buscar: "+ descImp);
			 System.out.println("Valor de la Descripcion validTasaOCuotaTra a buscar: "+ desc);
			 System.out.println("Valor de la Descripcion validTasaOCuotaTra a buscar: "+ descTraORet); 
			 if(value != null && value.trim().length() > 0 && descImp != null && descImp.trim().length() > 0 && desc != null && desc.trim().length() > 0 && descTraORet != null && descTraORet.trim().length() > 0){
//				 if(value.length() == 3){
//					 responseMap.put("value", value);
//					 responseMap.put("message", "");
//				 }else{
					 String tasaocuo = UtilCatalogos.findTasaOCuotaExist(mapCatalogos, descImp, desc, value, descTraORet);
					 if(!tasaocuo.equalsIgnoreCase("vacio")){
						 responseMap.put("value", tasaocuo);
						 responseMap.put("message", "");
					 }else{
						 responseMap.put("value", "vacio");
						 responseMap.put("message", " El valor del campo Tasa o Cuota que corresponde a Traslado no contiene un valor del catalogo c_Tasa o Cuota");
					 }
//				 }
				 
			 }else{
				 responseMap.put("value", "vacio");
				 responseMap.put("message", "El valor del campo Tasa o Cuota que corresponde a Traslado no contiene un valor del catalogo c_Tasa o Cuota");
			 }
			 System.out.println("Respuesta De validTasaOCuotaTra: "+ responseMap.get("value"));
			 System.out.println("Respuesta De validTasaOCuotaTra: "+ responseMap.get("message"));
			return responseMap;
			
		}
		
		// Validando TasaOCuota Retencion
		public static Map<String, Object> validTasaOCuotaRet(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String descImp, String desc, String value, String descTraORet){
			
			Map<String, Object> responseMap = new LinkedHashMap<String, Object>();
			 System.out.println("Entrando a validTasaOCuotaRet");
			 System.out.println("Size lista catalogos; "+ mapCatalogos.size());
			 System.out.println("Valor del validTasaOCuotaRet a buscar: "+ value);
			 System.out.println("Valor del Impuesto validTasaOCuotaRet a buscar: "+ descImp);
			 System.out.println("Valor de la Descripcion validTasaOCuotaRet a buscar: "+ desc);
			 System.out.println("Valor de la Descripcion validTasaOCuotaRet a buscar: "+ descTraORet); 
			 if(value != null && value.trim().length() > 0 && descImp != null && descImp.trim().length() > 0 && desc != null && desc.trim().length() > 0 && descTraORet != null && descTraORet.trim().length() > 0){
//				 if(value.length() == 3){
//					 responseMap.put("value", value);
//					 responseMap.put("message", "");
//				 }else{
					 String tasaocuo = UtilCatalogos.findTasaOCuotaExist(mapCatalogos, descImp, desc, value, descTraORet);
					 if(!tasaocuo.equalsIgnoreCase("vacio")){
						 responseMap.put("value", tasaocuo);
						 responseMap.put("message", "");
					 }else{
						 responseMap.put("value", "vacio");
						 responseMap.put("message", " El valor del campo Tasa o Cuota que corresponde a Retencion no contiene un valor del catalogo c_Tasa o Cuota");
					 }
//				 }
				 
			 }else{
				 responseMap.put("value", "vacio");
				 responseMap.put("message", "El valor del campo Tasa o Cuota que corresponde a Retencion no contiene un valor del catalogo c_Tasa o Cuota");
			 }
			 System.out.println("Respuesta De validTasaOCuotaRet: "+ responseMap.get("value"));
			 System.out.println("Respuesta De validTasaOCuotaRet: "+ responseMap.get("message"));
			return responseMap;
			
		}
		
		// Validando TipoFactor Traslado
				public static Map<String, Object> validTipoFactorTra(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
					
					Map<String, Object> responseMap = new LinkedHashMap<String, Object>();
					 System.out.println("Entrando a validTipoFactorTra");
					 System.out.println("Size lista catalogos; "+ mapCatalogos.size());
					 System.out.println("Valor del validTipoFactorTra a buscar: "+ value); 
					 if(value != null && value.trim().length() > 0){
//						 if(value.length() == 3){
//							 responseMap.put("value", value);
//							 responseMap.put("message", "");
//						 }else{
							 String tipoFacto = UtilCatalogos.findValTipoFactorByDesc(mapCatalogos, value);
							 if(!tipoFacto.equalsIgnoreCase("vacio")){
								 responseMap.put("value", tipoFacto);
								 responseMap.put("message", "");
							 }else{
								 responseMap.put("value", "vacio");
								 responseMap.put("message", " El valor del campo TipoFactor que corresponde a Traslado no contiene un valor del cat�logo c_TipoFactor");
							 }
//						 }
						 
					 }else{
						 responseMap.put("value", "vacio");
						 responseMap.put("message", "El valor del campo TipoFactor que corresponde a Traslado no contiene un valor del cat�logo c_TipoFactor");
					 }
					 System.out.println("Respuesta De validTipoFactorTra: "+ responseMap.get("value"));
					 System.out.println("Respuesta De validTipoFactorTra: "+ responseMap.get("message"));
					return responseMap;
					
				}
				
				// Validando TipoFactor Retencion
				public static Map<String, Object> validTipoFactorRet(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
					
					Map<String, Object> responseMap = new LinkedHashMap<String, Object>();
					 System.out.println("Entrando a validTipoFactorRet");
					 System.out.println("Size lista catalogos; "+ mapCatalogos.size());
					 System.out.println("Valor del validTipoFactorRet a buscar: "+ value); 
					 if(value != null && value.trim().length() > 0){
//						 if(value.length() == 3){
//							 responseMap.put("value", value);
//							 responseMap.put("message", "");
//						 }else{
							 String tipoFacto = UtilCatalogos.findValTipoFactorByDesc(mapCatalogos, value);
							 if(!tipoFacto.equalsIgnoreCase("vacio")){
								 if(!tipoFacto.equalsIgnoreCase("Exento") && !tipoFacto.equalsIgnoreCase("Excento")){
									 responseMap.put("value", tipoFacto);
									 responseMap.put("message", ""); 
								 }else{
									 responseMap.put("value", "vacio");
									 responseMap.put("message", " El valor registrado en el campo TipoFactor que corresponde a Retencion debe ser distinto de Excento ");
								 }
							 }else{
								 responseMap.put("value", "vacio");
								 responseMap.put("message", " El valor del campo TipoFactor que corresponde a Retencion no contiene un valor del catalogo c_TipoFactor");
							 }
//						 }
						 
					 }else{
						 responseMap.put("value", "vacio");
						 responseMap.put("message", "El valor del campo TipoFactor que corresponde a Retencion no contiene un valor del catalogo c_TipoFactor");
					 }
					 System.out.println("Respuesta De validTipoFactorRet: "+ responseMap.get("value"));
					 System.out.println("Respuesta De validTipoFactorRet: "+ responseMap.get("message"));
					return responseMap;
					
				}
				
				// Validando ClaveProdServ
				public static Map<String, Object> validValorUnitario(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value, Integer decimalesMoneda, String tipoDeComprobante){
					
					Map<String, Object> responseMap = new LinkedHashMap<String, Object>();
					 System.out.println("Entrando a validValorUnitario");
					 System.out.println("Size lista catalogos; "+ mapCatalogos.size());
					 System.out.println("Valor del validValorUnitario a buscar: "+ value);
					 System.out.println("Valor del validValorUnitario Decimales: "+ decimalesMoneda);
					 System.out.println("Valor del validValorUnitario TipoDeComprobante: "+ tipoDeComprobante); 
					 if(value != null && value.trim().length() > 0){
//						 if(value.length() == 3){
//							 responseMap.put("value", value);
//							 responseMap.put("message", "");
//						 }else{
//							 String usoCFDI = UtilCatalogos.decimalesValidationMsj(mapCatalogos, value);
//							 if(UtilCatalogos.decimalesValidationMsj(value, decimalesMoneda)){
						 if(tipoDeComprobante != null){
							 System.out.println("Valor del validValorUnitario TipoDeComprobante Entrando Diff: "+ tipoDeComprobante);
							 if(tipoDeComprobante.equalsIgnoreCase("I") || tipoDeComprobante.equalsIgnoreCase("E") || tipoDeComprobante.equalsIgnoreCase("N")){
								 System.out.println("Valor del validValorUnitario Dentro Valida:");
								 try{
									 System.out.println("Valor del validValorUnitario Dentro del Try:");
									 Double valDou = Double.parseDouble(value);
									 if(valDou > 0){
										 System.out.println("Valor del validValorUnitario Dentro Mayor Cero:");
										 responseMap.put("value", value);
										 responseMap.put("message", ""); 
									 }else{
										 System.out.println("Valor del validValorUnitario Dentro No mayor cero:");
										 responseMap.put("value", "vacio");
										 responseMap.put("message", " El valor del campo Valor Unitario debe ser mayor que cero (0) cuando el tipo de comprobante es Ingreso, Egreso o Nomina");
									 }
								 }catch(NumberFormatException e){
									 System.out.println("Valor del validValorUnitario Dentro Catch No numero:");
									 responseMap.put("value", "vacio");
									 responseMap.put("message", " El valor del campo Valor Unitario debe ser Numerico");
								 }
//							 }else{
//								 responseMap.put("value", value);
//								 responseMap.put("message", ""); 
//							 }
							 
						 }else{
//							 responseMap.put("value", "vacio");
//							 responseMap.put("message", " El valor del campo Valor Unitario debe tener hasta la cantidad de decimales que soporte la moneda");
							 System.out.println("Valor del validValorUnitario Dentro Valida Else tipos:");
							 responseMap.put("value", value);
							 responseMap.put("message", "");
						 }
							 
						 }else{
							 System.out.println("Valor del validValorUnitario Dentro Null tipoComprobante:");
							 responseMap.put("value", "vacio");
							 responseMap.put("message", " El valor del campo Valor Unitario No se puede registrar por que no se encuentra un Tipo De Comprobante");
						 }
								 
//						 }
						 
					 }else{
						 responseMap.put("value", "vacio");
						 responseMap.put("message", " El valor del campo Valor Unitario debe contener un registro");
					 }
					 System.out.println("Respuesta De validValorUnitario: "+ responseMap.get("value"));
					 System.out.println("Respuesta De validValorUnitario: "+ responseMap.get("message"));
					return responseMap;
					
				}
				
				// Validando ClaveProdServ
				public static Map<String, Object> validBaseTra(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
					
					Map<String, Object> responseMap = new LinkedHashMap<String, Object>();
					 System.out.println("Entrando a validBaseTra");
					 System.out.println("Size lista catalogos; "+ mapCatalogos.size());
					 System.out.println("Valor del validBaseTra a buscar: "+ value);
					 if(value != null && value.trim().length() > 0){
//						 if(value.length() == 3){
//							 responseMap.put("value", value);
//							 responseMap.put("message", "");
//						 }else{
//							 String usoCFDI = UtilCatalogos.decimalesValidationMsj(mapCatalogos, value);
									 try{
										 Double valDou = Double.parseDouble(value);
										 if(valDou > 0){
											 responseMap.put("value", value);
											 responseMap.put("message", ""); 
										 }else{
											 responseMap.put("value", "vacio");
											 responseMap.put("message", " El valor del campo Base que corresponde a Traslado debe ser mayor que cero");
										 }
									 }catch(NumberFormatException e){
										 responseMap.put("value", "vacio");
										 responseMap.put("message", " El valor del campo Base que corresponde a Traslado debe ser Numerico");
									 }
								 
//						 }
						 
					 }else{
						 responseMap.put("value", "vacio");
						 responseMap.put("message", " El valor del campo Base que corresponde a Traslado debe tener un registro");
					 }
					 System.out.println("Respuesta De validBaseTra: "+ responseMap.get("value"));
					 System.out.println("Respuesta De validBaseTra: "+ responseMap.get("message"));
					return responseMap;
					
				}
				
				// Validando ClaveProdServ
				public static Map<String, Object> validBaseRet(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
					
					Map<String, Object> responseMap = new LinkedHashMap<String, Object>();
					 System.out.println("Entrando a validBaseRet");
					 System.out.println("Size lista catalogos; "+ mapCatalogos.size());
					 System.out.println("Valor del validBaseRet a buscar: "+ value);
					 if(value != null && value.trim().length() > 0){
//						 if(value.length() == 3){
//							 responseMap.put("value", value);
//							 responseMap.put("message", "");
//						 }else{
//							 String usoCFDI = UtilCatalogos.decimalesValidationMsj(mapCatalogos, value);
									 try{
										 Double valDou = Double.parseDouble(value);
										 if(valDou > 0){
											 responseMap.put("value", value);
											 responseMap.put("message", ""); 
										 }else{
											 responseMap.put("value", "vacio");
											 responseMap.put("message", " El valor del campo Base que corresponde a Retencion debe ser mayor que cero");
										 }
									 }catch(NumberFormatException e){
										 responseMap.put("value", "vacio");
										 responseMap.put("message", " El valor del campo Base que corresponde a Retencion debe ser Numerico");
									 }
								 
//						 }
						 
					 }else{
						 responseMap.put("value", "vacio");
						 responseMap.put("message", " El valor del campo Base que corresponde a Retencion debe tener un registro");
					 }
					 System.out.println("Respuesta De validBaseRet: "+ responseMap.get("value"));
					 System.out.println("Respuesta De validBaseRet: "+ responseMap.get("message"));
					return responseMap;
					
				}
}
