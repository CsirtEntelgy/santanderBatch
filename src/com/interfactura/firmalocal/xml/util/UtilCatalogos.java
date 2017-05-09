package com.interfactura.firmalocal.xml.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.SequenceInputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.interfactura.firmalocal.xml.CatalogosDom;

public class UtilCatalogos 
{
	public static final Map<String, String> errorMessage = new HashMap<String, String>();
	public static StringBuffer lstErrors = new StringBuffer();
	static {
        errorMessage.put("ErrCompMoneda", "El valor de este campo SubTotal excede la cantidad de decimales que soporta la moneda.");
        errorMessage.put("ErrCompSubTotal001", "Clave=\"ErrCompSubTotal001\" Nodo=\"Comprobante\" Mensaje=\"No se permiten campos negativos en el campo Subtotal\"");
        errorMessage.put("ErrCompSubTotal002", "Clave=\"ErrCompSubTotal002\" Nodo=\"Comprobante\"  Mensaje=\"El TipoDeComprobante es T o P y el importe no es igual a 0, o cero con decimales.\"");
        errorMessage.put("ErrCompSubTotal003", "Clave=\"ErrCompSubTotal003\" Nodo=\"Comprobante\"  Mensaje=\"El valor de este campo SubTotal excede la cantidad de decimales que soporta la moneda.\"");
        errorMessage.put("ErrCompSubTotal004", "Clave=\"ErrCompSubTotal004\" Nodo=\"Comprobante\"  Mensaje=\"El valor del campo SubTotal viene vacio o no es numerico.\"");
        errorMessage.put("ErrReceNumRegIdTrib001", "Clave=\"ErrReceNumRegIdTrib001\" Nodo=\"Receptor\"  Mensaje=\"El Valor RegistroIdAtributario No Cumple Con El Patron Correspondiente.\"");
        errorMessage.put("ErrReceNumRegIdTrib002", "Clave=\"ErrReceNumRegIdTrib002\" Nodo=\"Receptor\"  Mensaje=\"No Se Ha Encontrado El Receptor Relacionado Con NumRegIdTrib El RFC Del Receptor Debe De Ser Un Generico Extranjero.\"");
    }
	// Validacion Tipo de comprobante AMDA
		public static String findTipoComprobante(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			
			String response = "";
			 System.out.println("Entrando a findTipoComprobante");
			 System.out.println("Tamaño lista catalogos; "+ mapCatalogos.size());
			 System.out.println("Valor del tipoDeComprobante a buscar: "+ value);
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				for(int i=0; i<mapCatalogos.get("TipoDeComprobante").size(); i++){
					if(mapCatalogos.get("TipoDeComprobante").get(i).getVal1().equalsIgnoreCase(value)){
						response = mapCatalogos.get("TipoDeComprobante").get(i).getVal1();
						break;
					}else{
						response = "tipoDeComprobanteIncorrecto";
					}
				}
			}else{
				response = "tipoDeComprobanteIncorrecto";
			}
			
			return response;
			
		}
		
		// Validacion Tipo de comprobante por descripcion AMDA
			public static String findTipoComprobanteByDescription(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
				
				String response = "";
				 System.out.println("Entrando a findTipoComprobante");
				 System.out.println("Tamaño lista catalogos; "+ mapCatalogos.size());
				 System.out.println("Valor del tipoDeComprobante a buscar: "+ value);
				if(mapCatalogos.size() > 0 && value.trim() != ""){
					for(int i=0; i<mapCatalogos.get("TipoDeComprobante").size(); i++){
						if(mapCatalogos.get("TipoDeComprobante").get(i).getVal2().equalsIgnoreCase(value)){
							response = mapCatalogos.get("TipoDeComprobante").get(i).getVal1();
							break;
						}else{
							response = "tipoDeComprobanteIncorrecto";
						}
					}
				}else{
					response = "tipoDeComprobanteIncorrecto";
				}
				
				return response;
				
			}
		
		// Validacion Encuentra Valor Maximo en Tipo de Comprobante AMDA
		public static String findValMaxTipoComprobanteByTotal(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			String response = "";
			
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				for(int i=0; i<mapCatalogos.get("TipoDeComprobante").size(); i++){
					if(mapCatalogos.get("TipoDeComprobante").get(i).getVal1().equalsIgnoreCase(value)){
						response = mapCatalogos.get("TipoDeComprobante").get(i).getVal3();
						break;
					}else{
						response = "vacio";
					}
				}
			}else{
				response = "vacio";
			}
			
			return response;
		}
		
		// Validacion Encuentra Valor Abreviado de el Pais AMDA
		public static String findValPais(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			String response = "";
				
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				for(int i=0; i<mapCatalogos.get("Pais").size(); i++){
					if(mapCatalogos.get("Pais").get(i).getVal2().equalsIgnoreCase(value)){
						response = mapCatalogos.get("Pais").get(i).getVal1();
						break;
					}else{
						response = "vacio";
					}
				}
			}else{
				response = "vacio";
			}
				
			return response;
		}
		
		// Validacion Encuentra Valor Equivalencia de el Pais AMDA
		public static String findEquivalenciaPais(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			String response = "";
				
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				for(int i=0; i<mapCatalogos.get("EquivalenciaPais").size(); i++){
					if(mapCatalogos.get("EquivalenciaPais").get(i).getVal2().equalsIgnoreCase(value)){
						response = mapCatalogos.get("EquivalenciaPais").get(i).getVal1();
						break;
					}else{
						response = "vacio";
					}
				}
			}else{
				response = "vacio";
			}
				
			return response;
		}
		
		// Validacion Encuentra Valor del codigo de Regimen Fiscal AMDA
		public static String findRegFiscalCode(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			String response = "";
			
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				for(int i=0; i<mapCatalogos.get("RegimenFiscal").size(); i++){
					if(mapCatalogos.get("RegimenFiscal").get(i).getVal2().equalsIgnoreCase(value)){
						response = mapCatalogos.get("RegimenFiscal").get(i).getVal1();
						System.out.println("*** response AMDA: " + response);
						if(response.contains(".")){
							System.out.println("*** response Dentro IF AMDA: " + response);
							String words[] = response.split("\\.");
							response = words[0];
							System.out.println("*** response Dentro IF despues AMDA: " + response);
						}
						
						break;
					}else{
						response = "vacio";
					}
				}
			}else{
				response = "vacio";
			}
			
			return response;
		}	
		
		//Validacion Encuentra Valor del Numero de Registro Tributario AMDA
		public static String findNumRegIdTrib(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			String response = "";
			
			if(mapCatalogos.size() > 0 && value.trim().length() > 0){
				for(int i=0; i<mapCatalogos.get("NumRegIdTrib").size(); i++){
					if(mapCatalogos.get("NumRegIdTrib").get(i).getVal3() != null){
						if(mapCatalogos.get("NumRegIdTrib").get(i).getVal3().equalsIgnoreCase(value)){
							response = mapCatalogos.get("NumRegIdTrib").get(i).getVal1();
							break;
						}else{
							response = "vacio";
						}
					}else{
						response = "vacio";
					}
				}
			}else{
				response = "vacio";
			}
			
			return response;
		}
		
		//Validacion Encuentra Valor del Patron del RFC expresado en el catalogo de Pais AMDA
		public static String findPatternRFCPais(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			String response = "";
			
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				for(int i=0; i<mapCatalogos.get("Pais").size(); i++){
					if(mapCatalogos.get("Pais").get(i).getVal1().equalsIgnoreCase(value)){
						response = mapCatalogos.get("Pais").get(i).getVal4();
						break;
					}else{
						response = "vacio";
					}
				}
			}else{
				response = "vacio";
			}
			
			return response;
		}
		
		//Validacion Encuentra Valor Clave de la Unidad AMDA
		public static String findValClaveUnidad(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			String response = "";
			
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				for(int i=0; i<mapCatalogos.get("ClaveUnidad").size(); i++){
					if(mapCatalogos.get("ClaveUnidad").get(i).getVal2().equalsIgnoreCase(value)){
						response = mapCatalogos.get("ClaveUnidad").get(i).getVal1();
						break;
					}else{
						response = "vacio";
					}
				}
			}else{
				response = "vacio";
			}
			
			return response;
		}
		
		//Validacion Encuentra Valor Clave del Impuesto AMDA
		public static String findValClaveImpuesto(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			String response = "";
			
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				for(int i=0; i<mapCatalogos.get("Impuesto").size(); i++){
					if(mapCatalogos.get("Impuesto").get(i).getVal2().equalsIgnoreCase(value)){
						response = mapCatalogos.get("Impuesto").get(i).getVal1();
						break;
					}else{
						response = "vacio";
					}
				}
			}else{
				response = "vacio";
			}
			
			return response;
		}
		
		//Validacion Encuentra Valor del Impuesto por Clave AMDA
		public static String findValImpuestoByClave(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			String response = "";
			
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				for(int i=0; i<mapCatalogos.get("Impuesto").size(); i++){
					if(mapCatalogos.get("Impuesto").get(i).getVal1().equalsIgnoreCase(value)){
						response = mapCatalogos.get("Impuesto").get(i).getVal1();
						break;
					}else{
						response = "vacio";
					}
				}
			}else{
				response = "vacio";
			}
			
			return response;
		}
		
		//Validacion Encuentra Valor del TipoFactor por descripcion AMDA
		public static String findValTipoFactorByDesc(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			String response = "";
			
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				for(int i=0; i<mapCatalogos.get("TipoFactor").size(); i++){
					if(mapCatalogos.get("TipoFactor").get(i).getVal1().equalsIgnoreCase(value)){
						response = mapCatalogos.get("TipoFactor").get(i).getVal1();
						break;
					}else{
						response = "vacio";
					}
				}
			}else{
				response = "vacio";
			}
			
			return response;
		}
		
		//Validacion Encuentra Valor del TipoFactor por descripcion AMDA
		public static String findValTipoFactorByDescRet(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			String response = "";
			
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				for(int i=0; i<mapCatalogos.get("TipoFactor").size(); i++){
					if(mapCatalogos.get("TipoFactor").get(i).getVal1().equalsIgnoreCase(value) && !mapCatalogos.get("TipoFactor").get(i).getVal1().equalsIgnoreCase("Exento")){
						response = mapCatalogos.get("TipoFactor").get(i).getVal1();
						break;
					}else{
						response = "vacio";
					}
				}
			}else{
				response = "vacio";
			}
			
			return response;
		}
		
		//Validacion Encuentra Valor Maximo de Tasa o Cuota del catalogo TasaOCuota AMDA
		public static String findValMaxTasaOCuota(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value1, String value2){
			String response = "";
			System.out.println("Validacion findValMaxTasaOCuota AMDA : " + value1 + " : " + value2);
			if(mapCatalogos.size() > 0 && value1.trim() != "" && value2.trim() != ""){
				for(int i=0; i<mapCatalogos.get("TasaOCuota").size(); i++){
					if(mapCatalogos.get("TasaOCuota").get(i).getVal4().equalsIgnoreCase(value1) && mapCatalogos.get("TasaOCuota").get(i).getVal5().equalsIgnoreCase(value2)){
						response = mapCatalogos.get("TasaOCuota").get(i).getVal3();
						break;
					}else{
						response = "vacio";
					}
				}
			}else{
				response = "vacio";
			}
			
			return response;
		}
		
		//Validacion Encuentra Valor Maximo de Tasa o Cuota del catalogo TasaOCuota para Traslado AMDA
		public static String findValMaxTasaOCuotaTraslado(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value1, String value2){
			String response = "";
			System.out.println("Validacion findValMaxTasaOCuotaTraslado AMDA : " + value1 + " : " + value2);
			if(mapCatalogos.size() > 0 && value1.trim() != "" && value2.trim() != ""){
				for(int i=0; i<mapCatalogos.get("TasaOCuota").size(); i++){
					if(mapCatalogos.get("TasaOCuota").get(i).getVal4().equalsIgnoreCase(value1) && mapCatalogos.get("TasaOCuota").get(i).getVal5().equalsIgnoreCase(value2) && mapCatalogos.get("TasaOCuota").get(i).getVal3().equalsIgnoreCase("0.16")){
						response = mapCatalogos.get("TasaOCuota").get(i).getVal3();
						break;
					}else{
						response = "vacio";
					}
				}
			}else{
				response = "vacio";
			}
			System.out.println("Validacion findValMaxTasaOCuotaTraslado AMDA Saliendo : " + response);
			return response;
		}
		
		//Validacion Encuentra Valor de la Moneda AMDA
		public static String findEquivalenciaMoneda(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			String response = "";
			System.out.println("Equivalencia Moneda A Buscar : " + value);
			System.out.println("Mapa Contenido EquivalenciaMoneda : " + mapCatalogos.get("EquivalenciaMoneda").size());
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				System.out.println("Dentro If Equivalencia Moneda A Buscar : " + value);
				for(int i=0; i<mapCatalogos.get("EquivalenciaMoneda").size(); i++){
					System.out.println("Dentro For Equivalencia Moneda A Buscar : " + value);
					if(mapCatalogos.get("EquivalenciaMoneda").get(i).getVal2() != null){
						System.out.println("Dentro no null Equivalencia Moneda A Buscar : " + value);
						if(mapCatalogos.get("EquivalenciaMoneda").get(i).getVal2().equalsIgnoreCase(value)){
							response = mapCatalogos.get("EquivalenciaMoneda").get(i).getVal1();	
							break;
						}else{
							response = "vacio";
						}
					}else{
						response = "vacio";
					}

				}
			}else{
				response = "vacio";
			}
			
			return response;
		}
		
		//Validacion Encuentra Valor de la Moneda en catalogo Moneda AMDA
		public static String findMonedaCatalogo(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			String response = "";
			
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				for(int i=0; i<mapCatalogos.get("Moneda").size(); i++){
					if(mapCatalogos.get("Moneda").get(i).getVal2().equalsIgnoreCase(value)){
						response = mapCatalogos.get("Moneda").get(i).getVal1();	
						break;
					}else{
						response = "vacio";
					}
				}
			}else{
				response = "vacio";
			}
			
			return response;
		}
		
		//Validacion Encuentra Valor Tipo cambio a travez de moneda AMDA
		public static String findTipoCambioByMoneda(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			String response = "";
			
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				for(int i=0; i<mapCatalogos.get("EquivalenciaTipoCambio").size(); i++){
					if(mapCatalogos.get("EquivalenciaTipoCambio").get(i).getVal1().equalsIgnoreCase(value)){
						response = mapCatalogos.get("EquivalenciaTipoCambio").get(i).getVal3();
						break;
					}else{
						response = "vacio";
					}
				}
			}else{
				response = "vacio";
			}
			
			return response;
		}
		
		//Validacion Encuentra Valor Porcentaje de catalogo moneda AMDA
		public static String findTipoCambioPorcentaje(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value, String tipoCam){
			String response = "";
			String porcentaje = "";
			String tipoCamCatalog = "";
			double porcentajeDoub = 0.00;
			double tipoCamDoub = 0.00;
			double tipoCamCatalogDoub = 0.00;
			double total = 0.00;
			double totalArriba = 0.00;
			double totalAbajo = 0.00;
			
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				for(int i=0; i<mapCatalogos.get("Moneda").size(); i++){
					if(mapCatalogos.get("Moneda").get(i).getVal1().equalsIgnoreCase(value)){
						response = mapCatalogos.get("Moneda").get(i).getVal3();
						porcentaje = mapCatalogos.get("Moneda").get(i).getVal4();
						tipoCamCatalog = mapCatalogos.get("Moneda").get(i).getVal5();
						System.out.println("Porcentaje AMDA: " + porcentaje);
						System.out.println("Tipo Cambio AMDA: " + tipoCam);
						System.out.println("Tipo Cambio Catalogo AMDA: " + tipoCamCatalog);
//						total = 120*(50.0f/100.0f); // 50% de 120
						
						
						try{
							porcentajeDoub = Double.parseDouble(porcentaje);
							System.out.println("Porcentaje Double AMDA: " + porcentajeDoub);
							tipoCamDoub = Double.parseDouble(tipoCam);
							System.out.println("Tipo Cambio Double AMDA: " + tipoCamDoub);
							tipoCamCatalogDoub = Double.parseDouble(tipoCamCatalog);
							System.out.println("Tipo Cambio Catalogo Double AMDA: " + tipoCamCatalogDoub);
							
							total = tipoCamCatalogDoub*(porcentajeDoub/100.0);
							System.out.println("Total AMDA PORCENTAJE: " + total);
							
							totalArriba = tipoCamCatalogDoub+total;
							System.out.println("TotalArriba AMDA PORCENTAJE: " + totalArriba);
							totalAbajo = tipoCamCatalogDoub-total;
							System.out.println("TotalAbajo AMDA PORCENTAJE: " + totalAbajo);
							
							if(tipoCamDoub >= totalAbajo && tipoCamDoub <= totalArriba ){
								response = "OK";
							}else{
								response = "vacio";
							}

							System.out.println("Decimales moneda Conversion numerico: " + response);
						}catch(NumberFormatException e){
							System.out.println("Problema al convertir datos de tipoCambio: ");
							response = "vacio";
						}
						
						break;
					}else{
						response = "vacio";
					}
				}
			}else{
				response = "vacio";
			}
			
			return response;
		}
		
		//Validacion Encuentra Valor de Decimales soportados por la Moneda AMDA
		public static Integer findDecimalesMoneda(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			Integer response = 0;
			String responseFile = "";
			
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				for(int i=0; i<mapCatalogos.get("Moneda").size(); i++){
					if(mapCatalogos.get("Moneda").get(i).getVal1().equalsIgnoreCase(value)){
						responseFile = mapCatalogos.get("Moneda").get(i).getVal3();
						try{
							Integer intVal = Integer.parseInt(responseFile);
							response = intVal;
							System.out.println("Decimales moneda Conversion numerico: " + response);
						}catch(NumberFormatException e){
							System.out.println("Decimales moneda No era numero la respuesta: " + responseFile);
						}
						break;
					}else{
						response = 0;
					}
				}
			}else{
				response = 0;
			}
			
			return response;
		}
		
		//Agrega equivalencia de conceptos con traslados en conceptos AMDA
		public static Map<String, ArrayList<CatalogosDom>> arregloConceptos(Map<String, ArrayList<CatalogosDom>> mapCatalogos){
			Map<String, ArrayList<CatalogosDom>> response = mapCatalogos;
			System.out.println("Equvalencia arreglo con: " + mapCatalogos.size());
			String idCatalog = "";
			ArrayList<CatalogosDom> catalogDetail = new ArrayList<CatalogosDom>();//
			if(mapCatalogos.size() > 0 ){
				
				for(int i=0; i<mapCatalogos.get("EquivalenciaConceptoImpuesto").size(); i++){
					
					idCatalog = mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal5();
//					ArrayList<CatalogosDom> catalogDetail = new ArrayList<CatalogosDom>();//
					catalogDetail.add(mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i));
					
				}
				response.put(idCatalog, catalogDetail);
				
//				HashMap<String, List<CatalogosDom>> lstCatalogs = new HashMap();
//	            while (mapCatalogos) {
//	                String idCatalog = mapCatalogos.get("EquivalenciaImpuestosTraslados");
//	                List<CatalogosDom> catalogDetail = lstCatalogs.get(idCatalog);
//	                if (catalogDetail == null) {
//	                    catalogDetail = new ArrayList<>();
//	                }
//	                catalogDetail.add(catalogoDetalle);
//	                lstCatalogs.put(idCatalog, catalogDetail);
//	            }

			}
			System.out.println("Salida Equvalencia arreglo con: " + response.size());
			return response;
		}
		
		//Encuentra los traslados en los catalogos de equivalencia AMDA
		public static Map<String, Object> findTraslados(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String importeCon, String descCon, Integer decimalesMoneda){
			Map<String, Object> responseMap = new LinkedHashMap<String, Object>();
			String response = "";
			String valTasa = "";
			Double valTasaNum = 0.00 ;
			Double importeConNum = 0.00 ;
			Double importeTrasladoMul = 0.00;
			String nodocon = "";
			Double sumTotal = 0.00 ;
			Double sumTotalIva = 0.00;
			Double sumTotalIsr= 0.00;
			Double sumTotalIeps = 0.00;
			System.out.println("findTraslados Inicio " + descCon);
			System.out.println("findTraslados Inicio " + mapCatalogos.get("EquivalenciaConceptoImpuesto").size());
			if(mapCatalogos.size() > 0 && descCon.trim().length() > 0){
				
				for(int i=0; i<mapCatalogos.get("EquivalenciaConceptoImpuesto").size(); i++){
					System.out.println("findTraslados Dentro For " + mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal5());
					if(mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal5().equalsIgnoreCase(descCon) && mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal4().equalsIgnoreCase("Traslado")){
						
						if(mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal3() != null){
							System.out.println("Val Tasa No nulo");
							if(mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal3().length() > 0){
								valTasa = mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal3();
							}else{
								System.out.println("Val Tasa vacio length");
								valTasa = "vacio";
							}
						}else{
							System.out.println("Val Tasa nulo");
							valTasa = "vacio";
						}

						try{
							if(!valTasa.equalsIgnoreCase("vacio")){
								valTasaNum = Double.parseDouble(valTasa);
								System.out.println("Val Tasa " + valTasaNum);
							}else{
//								valTasaNum = Double.parseDouble(valTasa);
								System.out.println("Val Tasa Vacio " + valTasa);
							}
							valTasaNum = Double.parseDouble(valTasa);
							System.out.println("Val Tasa " + valTasaNum);
							System.out.println("Val Impor Conce Antes " + importeCon);
							importeConNum = Double.parseDouble(importeCon);
							System.out.println("Val Impor Conce " + valTasaNum);
//							importeTrasladoMul = (valTasaNum*importeConNum) + importeConNum;
							importeTrasladoMul = (valTasaNum*importeConNum);
							System.out.println("Val Impor Traslado " + importeTrasladoMul);
							if(mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal1().equalsIgnoreCase("001")){
								responseMap.put("ISR", true);
								sumTotalIsr = sumTotalIsr + importeTrasladoMul;
								System.out.println("Val sumTotalIsr Traslado " + sumTotalIsr);
							}else if(mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal1().equalsIgnoreCase("002")){
								responseMap.put("IVA", true);
								sumTotalIva = sumTotalIva + importeTrasladoMul;
								System.out.println("Val sumTotalIva Traslado " + sumTotalIva);
							}else if(mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal1().equalsIgnoreCase("003")){
								responseMap.put("IEPS", true);
								sumTotalIeps = sumTotalIeps + importeTrasladoMul;
								System.out.println("Val sumTotalIeps Traslado " + sumTotalIeps);
							}
							
							String baseCamp = "";
							if(UtilCatalogos.decimalesValidationMsj(importeCon, decimalesMoneda)){
								if(importeConNum > 0){
									baseCamp = "Base=\"" + importeCon;
								}else{
									baseCamp = "ElValorDelCampoBaseQueCorrespondeATrasladoDebeSerMayorQueCero=\"" + importeCon;
								}
								
							}else{
								baseCamp = "ElValorDelCampoBaseQueCorrespondeATrasladoDebetenerHastaLaCantidadDeDecimalesQueSporteLaMoneda=\"" + importeCon;
							}
							
							String impuestoLine = "";
							if(mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal1() != null){
								if(!findValImpuestoByClave(mapCatalogos, mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal1()).equalsIgnoreCase("vacio")){
									impuestoLine = "\" Impuesto=\"" + mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal1() ;
								}else{
									impuestoLine = "\" ElValorDelCampoImpuestoQueCorrespondeATrasladoNoContieneUnValorDelCatalogoc_Impuesto=\"" + mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal1() ;
								}
							}else{
								impuestoLine = "\" ElValorDelCampoImpuestoQueCorrespondeATrasladoNoContieneUnValorDelCatalogoc_Impuesto=\"" + mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal1() ;
							}
							
							String tipoFactorLine = "";
							String tipoFactorValue = "";
							String tasaOCutoaLine = "";
							String importeLine = "";
							if(mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal2() != null){
								tipoFactorValue= findValTipoFactorByDesc(mapCatalogos, mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal2());
								if(!tipoFactorValue.equalsIgnoreCase("vacio")){
									tipoFactorLine = "\" TipoFactor=\"" + tipoFactorValue;
									
									if(!tipoFactorValue.equalsIgnoreCase("Exento")){
										if(!valTasa.equalsIgnoreCase("vacio")){
											tasaOCutoaLine = "\" TasaOCuota=\"" + Util.completeZeroDecimals(valTasa, 6);
										}else{
											tasaOCutoaLine = "\" ElValorDelCampoTasaOCuotaQueCorrespondeATrasladoNoContieneUnValorDelCatalogoc_TasaOCuota=\"" + Util.completeZeroDecimals(valTasa, 6);
										}
										
										importeLine = "\" Importe=\"" + decimales(importeTrasladoMul.toString(), decimalesMoneda);
									}
									
								}else{
									tipoFactorLine = "\" ElValorDelCampoTipoFactorQueCorrespondeATrasladoNoContieneUnValorDelCatalogoc_TipoFactor=\"" + tipoFactorValue;
								}
							}else{
								tipoFactorLine = "\" ElValorDelCampoTipoFactorQueCorrespondeATrasladoNoContieneUnValorDelCatalogoc_TipoFactor=\"" + mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal2();
							}
							
							nodocon +=  "\n<cfdi:Traslado " + baseCamp +
									   impuestoLine +
									   tipoFactorLine +
									   tasaOCutoaLine +
									   importeLine + "\" " +
									   " />" ;
//							nodocon +=  "\n<cfdi:Traslado Base=\"" + importeCon +
//									   "\" Impuesto=\"" + mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal1() +
//									   "\" TipoFactor=\"" + mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal2() +
//									   "\" TasaOCuota=\"" + Util.completeZeroDecimals(valTasa, 6) +
//									   "\" Importe=\"" + decimales(importeTrasladoMul.toString(), decimalesMoneda) + "\" " +
//									   " />" ;
							sumTotal += importeTrasladoMul;
							System.out.println("Val Suma Total Traslado " + sumTotal);
							System.out.println("Val NodoCon Traslado " + nodocon);
						}catch(NumberFormatException e){
							System.out.println("No es numero findTraslados: " + valTasa);
						}
//						response = response + nodocon;
//						break;
					}else{
						// La tasaOCuota no se encontro en el catalogo
//						response = "\n<cfdi:Traslado Base=\"" + "0.00" +
//								   "\" Impuesto=\"" + "001" +
//								   "\" TipoFactor=\"" + "Tasa" +
//								   "\" ElValorDelCampoTasaOCuotaQueCorrespondeATrasladoNoContieneUnValorDelCatalogoc_TasaOCuota=\"" + "0.000" +
//								   "\" Importe=\"" + "0.00" + "\" " +
//								   " />" ;
						response = "";
					}
					response = nodocon;
				}
				
//				for(int i=0; i<mapCatalogos.get(descCon).size(); i++){
//					System.out.println("findTraslados Dentro For " + mapCatalogos.get(descCon).get(i).getVal5());
//					if(mapCatalogos.get(descCon).get(i).getVal5().equalsIgnoreCase(descCon) && mapCatalogos.get(descCon).get(i).getVal4().equalsIgnoreCase("Traslado")){
//						valTasa = mapCatalogos.get(descCon).get(i).getVal3();
//						try{
//							valTasaNum = Double.parseDouble(valTasa);
//							System.out.println("Val Tasa " + valTasaNum);
//							importeConNum = Double.parseDouble(importeCon);
//							System.out.println("Val Impor Conce " + valTasaNum);
//							importeTrasladoMul = (valTasaNum*importeConNum) + importeConNum;
//							System.out.println("Val Impor Traslado " + importeTrasladoMul);
//							
//							nodocon = "\n<cfdi:Traslado Base=\"" + importeCon +
//									   "\" Impuesto=\"" + mapCatalogos.get(descCon).get(i).getVal1() +
//									   "\" TipoFactor=\"" + mapCatalogos.get(descCon).get(i).getVal2() +
//									   "\" TasaOcuota=\"" + valTasa +
//									   "\" Importe=\"" + importeTrasladoMul.toString() + "\" " +
//									   " />" ;
//							System.out.println("Val NodoCon Traslado " + nodocon);
//						}catch(NumberFormatException e){
//							System.out.println("No es numero findTraslados: " + valTasa);
//						}
//						response = response + nodocon;
//						break;
//					}else{
//						response = "";
//					}
//				}
			}else{
				response = "";
			}

			responseMap.put("valNodoStr", response);
			responseMap.put("sumaTotal", decimales(sumTotal.toString(), decimalesMoneda));
			responseMap.put("sumTotalIsr", decimales(sumTotalIsr.toString(), decimalesMoneda));
			responseMap.put("sumTotalIva", decimales(sumTotalIva.toString(), decimalesMoneda));
			responseMap.put("sumTotalIeps", decimales(sumTotalIeps.toString(), decimalesMoneda));
			System.out.println("Response Get Traslado: " + responseMap.get("sumaTotal"));
			System.out.println("Response Get Traslado sumTotalIsr: " + responseMap.get("sumTotalIsr"));
			System.out.println("Response Get Traslado sumTotalIva: " + responseMap.get("sumTotalIva"));
			System.out.println("Response Get Traslado sumTotalIeps: " + responseMap.get("sumTotalIeps"));
			return responseMap;
		}
		
		//Encuentra las retenciones en los catalogos de equivalencia AMDA
		public static Map<String, Object> findRetencion(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String importeCon, String descCon, Integer decimalesMoneda){
			Map<String, Object> responseMap = new LinkedHashMap<String, Object>();
			String response = "";
			String valTasa = "";
			Double valTasaNum ;
			Double importeConNum ;
			Double importeTrasladoMul;
			String nodocon = "";
			Double sumTotal = 0.00 ;
			Double sumTotalIva = 0.00;
			Double sumTotalIsr= 0.00;
			Double sumTotalIeps = 0.00;
			System.out.println("findRetencion Inicio " + descCon);
			System.out.println("findRetencion Inicio 2 " + mapCatalogos.get("EquivalenciaConceptoImpuesto").size());
			if(mapCatalogos.size() > 0 && descCon.trim().length() > 0){
				
				for(int i=0; i<mapCatalogos.get("EquivalenciaConceptoImpuesto").size(); i++){
					System.out.println("findTraslados Dentro For " + mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal5());
					if(mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal5().equalsIgnoreCase(descCon) && mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal4().equalsIgnoreCase("Retencion")){
						valTasa = mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal3();
						
						if(mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal3() != null){
							System.out.println("Val Tasa No nulo Reten");
							if(mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal3().length() > 0){
								valTasa = mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal3();
							}else{
								System.out.println("Val Tasa vacio length Reten");
								valTasa = "vacio";
							}
							
						}else{
							System.out.println("Val Tasa nulo Reten");
							valTasa = "vacio";
						}
						
						try{
							if(!valTasa.equalsIgnoreCase("vacio")){
								valTasaNum = Double.parseDouble(valTasa);
								System.out.println("Val Tasa Reten " + valTasaNum);
							}else{
//								valTasaNum = Double.parseDouble(valTasa);
								System.out.println("Val Tasa Vacio Reten " + valTasa);
							}
							valTasaNum = Double.parseDouble(valTasa);
							System.out.println("Val Tasa Reten " + valTasaNum);
							System.out.println("Val Impor Conce Antes Reten " + importeCon);
							importeConNum = Double.parseDouble(importeCon);
							System.out.println("Val Impor Conce Reten " + valTasaNum);
//							importeTrasladoMul = (valTasaNum*importeConNum) + importeConNum;
							importeTrasladoMul = (valTasaNum*importeConNum);
							System.out.println("Val Impor Retencion " + importeTrasladoMul);
							if(mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal1().equalsIgnoreCase("001")){
								responseMap.put("ISR", true);
								sumTotalIsr = sumTotalIsr + importeTrasladoMul;
								System.out.println("Val sumTotalIsr Retencion " + sumTotalIsr);
							}else if(mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal1().equalsIgnoreCase("002")){
								responseMap.put("IVA", true);
								sumTotalIva = sumTotalIva + importeTrasladoMul;
								System.out.println("Val sumTotalIva Retencion " + sumTotalIva);
							}else if(mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal1().equalsIgnoreCase("003")){
								responseMap.put("IEPS", true);
								sumTotalIeps = sumTotalIeps + importeTrasladoMul;
								System.out.println("Val sumTotalIeps Retencion " + sumTotalIeps);
							}
							
							String baseCamp = "";
							if(UtilCatalogos.decimalesValidationMsj(importeCon, decimalesMoneda)){
								if(importeConNum > 0){
									baseCamp = "Base=\"" + importeCon;
								}else{
									baseCamp = "ElValorDelCampoBaseQueCorrespondeARetencionDebeSerMayorQueCero=\"" + importeCon;
								}
								
							}else{
								baseCamp = "ElValorDelCampoBaseQueCorrespondeARetencionDebetenerHastaLaCantidadDeDecimalesQueSporteLaMoneda=\"" + importeCon;
							}
							
							String impuestoLine = "";
							if(mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal1() != null){
								if(!findValImpuestoByClave(mapCatalogos, mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal1()).equalsIgnoreCase("vacio")){
									impuestoLine = "\" Impuesto=\"" + mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal1() ;
								}else{
									impuestoLine = "\" ElValorDelCampoImpuestoQueCorrespondeARetencionNoContieneUnValorDelCatalogoc_Impuesto=\"" + mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal1() ;
								}
							}else{
								impuestoLine = "\" ElValorDelCampoImpuestoQueCorrespondeARetencionNoContieneUnValorDelCatalogoc_Impuesto=\"" + mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal1() ;
							}
							
							String tipoFactorLine = "";
							String tipoFactorValue = "";
							String tasaOCutoaLine = "";
							String importeLine = "";
							if(mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal2() != null){
								tipoFactorValue= findValTipoFactorByDescRet(mapCatalogos, mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal2());
								if(!tipoFactorValue.equalsIgnoreCase("vacio")){
									tipoFactorLine = "\" TipoFactor=\"" + tipoFactorValue;
									
									if(!tipoFactorValue.equalsIgnoreCase("Exento")){
										if(!valTasa.equalsIgnoreCase("vacio")){
											tasaOCutoaLine = "\" TasaOCuota=\"" + Util.completeZeroDecimals(valTasa, 6);
										}else{
											tasaOCutoaLine = "\" TasaOCuota=\"" + "ElValorDelCampoTasaOCuotaQueCorrespondeARetencionNoContieneUnValorDelCatalogoc_TasaOCuota";
										}
										
										importeLine = "\" Importe=\"" + decimales(importeTrasladoMul.toString(), decimalesMoneda);
									}else{
										if(!valTasa.equalsIgnoreCase("vacio")){
											tasaOCutoaLine = "\" TasaOCuota=\"" + Util.completeZeroDecimals(valTasa, 6);
										}else{
											tasaOCutoaLine = "\" TasaOCuota=\"" + "ElValorDelCampoTasaOCuotaQueCorrespondeARetencionNoContieneUnValorDelCatalogoc_TasaOCuota";
										}
										importeLine = "\" Importe=\"" + decimales(importeTrasladoMul.toString(), decimalesMoneda);
										
										tipoFactorLine = "\" ElValorDelCampoTipoFactorQueCorrespondeARetencionDebeSerDistintoDeExento=\"" + tipoFactorValue;
									}
									
								}else{
									tipoFactorLine = "\" ElValorDelCampoTipoFactorQueCorrespondeARetencionNoContieneUnValorDelCatalogoc_TipoFactor=\"" + tipoFactorValue;
								}
							}else{
								tipoFactorLine = "\" ElValorDelCampoTipoFactorQueCorrespondeARetencionNoContieneUnValorDelCatalogoc_TipoFactor=\"" + mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal2();
							}
							
							nodocon +=  "\n<cfdi:Retencion " + baseCamp +
									   impuestoLine +
									   tipoFactorLine +
									   tasaOCutoaLine +
									   importeLine + "\" " +
									   " />" ;
							
//							nodocon +=  "\n<cfdi:Retencion Base=\"" + importeCon +
//									   "\" Impuesto=\"" + mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal1() +
//									   "\" TipoFactor=\"" + mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal2() +
//									   "\" TasaOCuota=\"" +  Util.completeZeroDecimals(valTasa, 6) +
//									   "\" Importe=\"" + decimales(importeTrasladoMul.toString(), decimalesMoneda) + "\" " +
//									   " />" ;
							sumTotal += importeTrasladoMul;
							System.out.println("Val Suma Total Retencion " + sumTotal);
							System.out.println("Val NodoCon findRetencion " + nodocon);
						}catch(NumberFormatException e){
							System.out.println("No es numero findRetencion: " + valTasa);
						}
//						response = response + nodocon;
//						break;
					}else{
//						response = "\n<cfdi:Retencion Base=\"" + "0.00" +
//								   "\" Impuesto=\"" + "001" +
//								   "\" TipoFactor=\"" + "Tasa" +
//								   "\" ElValorDelCampoTasaOCuotaQueCorrespondeARetencionNoContieneUnValorDelCatalogoc_TasaOCuota=\"" + "0.000" +
//								   "\" Importe=\"" + "0.00" + "\" " +
//								   " />" ;
						response = "";
					}
					response = nodocon;
				}
				
//				for(int i=0; i<mapCatalogos.get(descCon).size(); i++){
//					System.out.println("findTraslados Dentro For " + mapCatalogos.get(descCon).get(i).getVal5());
//					if(mapCatalogos.get(descCon).get(i).getVal5().equalsIgnoreCase(descCon) && mapCatalogos.get(descCon).get(i).getVal4().equalsIgnoreCase("Traslado")){
//						valTasa = mapCatalogos.get(descCon).get(i).getVal3();
//						try{
//							valTasaNum = Double.parseDouble(valTasa);
//							System.out.println("Val Tasa " + valTasaNum);
//							importeConNum = Double.parseDouble(importeCon);
//							System.out.println("Val Impor Conce " + valTasaNum);
//							importeTrasladoMul = (valTasaNum*importeConNum) + importeConNum;
//							System.out.println("Val Impor Traslado " + importeTrasladoMul);
//							
//							nodocon = "\n<cfdi:Traslado Base=\"" + importeCon +
//									   "\" Impuesto=\"" + mapCatalogos.get(descCon).get(i).getVal1() +
//									   "\" TipoFactor=\"" + mapCatalogos.get(descCon).get(i).getVal2() +
//									   "\" TasaOcuota=\"" + valTasa +
//									   "\" Importe=\"" + importeTrasladoMul.toString() + "\" " +
//									   " />" ;
//							System.out.println("Val NodoCon Traslado " + nodocon);
//						}catch(NumberFormatException e){
//							System.out.println("No es numero findTraslados: " + valTasa);
//						}
//						response = response + nodocon;
//						break;
//					}else{
//						response = "";
//					}
//				}
			}else{
				response = "";
			}
			responseMap.put("valNodoStr", response);
			responseMap.put("sumaTotal", decimales(sumTotal.toString(), decimalesMoneda));
			responseMap.put("sumTotalIsr", decimales(sumTotalIsr.toString(), decimalesMoneda));
			responseMap.put("sumTotalIva", decimales(sumTotalIva.toString(), decimalesMoneda));
			responseMap.put("sumTotalIeps", decimales(sumTotalIeps.toString(), decimalesMoneda));
			System.out.println("Response Get Retencion sumaTotal: " + responseMap.get("sumaTotal"));
			System.out.println("Response Get Retencion sumTotalIsr: " + responseMap.get("sumTotalIsr"));
			System.out.println("Response Get Retencion sumTotalIva: " + responseMap.get("sumTotalIva"));
			System.out.println("Response Get Retencion sumTotalIeps: " + responseMap.get("sumTotalIeps"));
			return responseMap;
		}
		
		
		public static String decimales(String importeval, Integer decimalesMoneda){
			String response = "";
			String importeValDer = "";
			String importeValIzq = "";
			System.out.println("Entrando funcion Decimales: " + importeval + " : " + decimalesMoneda);
			if(importeval.contains(".")){
				String deci[] = importeval.split("\\.");
				importeValIzq = deci[0];
				importeValDer = deci[1];
				if(decimalesMoneda > 0){
					if(importeValDer.length() > decimalesMoneda){
						response = importeValIzq+"."+importeValDer.substring(0,decimalesMoneda);
					}else{
						response = importeval;
					}
				}else{
//					response = importeval.substring(0,2);
					response = importeval;
				}
			}
			System.out.println("Regreso Importe Substring: " + response);
			return response;
		}
		
		public static boolean decimalesValidationMsj(String importeval, Integer decimalesMoneda){
			boolean response = false;
			String importeValDer = "";
			String importeValIzq = "";
			System.out.println("Entrando funcion Decimales: " + importeval + " : " + decimalesMoneda);
			if(importeval.contains(".")){
				String deci[] = importeval.split("\\.");
				importeValIzq = deci[0];
				importeValDer = deci[1];
				if(decimalesMoneda > 0){
					if(importeValDer.length() > decimalesMoneda){
//						response = importeValIzq+"."+importeValDer.substring(0,decimalesMoneda);
						response = false;
					}else{
//						response = importeval;
						response = true;
					}
				}else{
//					response = importeval.substring(0,2);
//					response = importeval;
					response = true;
				}
			}else{
				response = true;
			}
				
			System.out.println("Regreso Importe Substring: " + response);
			return response;
		}
		
		//Validacion Encuentra Valor de la Forma De Pago a travez de la descripcion AMDA
		public static String findFormaPago(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			String response = "";
			
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				for(int i=0; i<mapCatalogos.get("FormaPago").size(); i++){
					if(mapCatalogos.get("FormaPago").get(i).getVal2().equalsIgnoreCase(value)){
						response = mapCatalogos.get("FormaPago").get(i).getVal1();	
						break;
					}else{
						response = "vacio";
					}
				}
			}else{
				response = "vacio";
			}
			
			return response;
		}
		
		//Validacion Encuentra Valor de la Forma De Pago a travez de la descripcion AMDA
		public static String findMetodoPago(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			String response = "";
			
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				for(int i=0; i<mapCatalogos.get("MetodoPago").size(); i++){
					if(mapCatalogos.get("MetodoPago").get(i).getVal2().equalsIgnoreCase(value)){
						response = mapCatalogos.get("MetodoPago").get(i).getVal1();	
						break;
					}else{
						response = "vacio";
					}
				}
			}else{
				response = "vacio";
			}
			
			return response;
		}
		
		//Validacion Encuentra Valor del UsoCFDI a travez de la descripcion AMDA
		public static String findUsoCfdi(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			String response = "";
			
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				for(int i=0; i<mapCatalogos.get("UsoCFDI").size(); i++){
					if(mapCatalogos.get("UsoCFDI").get(i).getVal2().equalsIgnoreCase(value)){
						response = mapCatalogos.get("UsoCFDI").get(i).getVal1();	
						break;
					}else{
						response = "vacio";
					}
				}
			}else{
				response = "vacio";
			}
			
			return response;
		}
		
		//Validacion Encuentra Valor de la clave del catalogo claveProdServ a travez de la descripcion AMDA
		public static String findClaveProdServbyDesc(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			String response = "";
			
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				for(int i=0; i<mapCatalogos.get("ClaveProdServ").size(); i++){
					if(mapCatalogos.get("ClaveProdServ").get(i).getVal2().equalsIgnoreCase(value)){
						response = mapCatalogos.get("ClaveProdServ").get(i).getVal1();	
						break;
					}else{
						response = "vacio";
					}
				}
			}else{
				response = "vacio";
			}
			
			return response;
		}
		
		//Validacion Encuentra Valor de Traslado del catalogo claveProdServ a travez de la descripcion AMDA
		public static boolean findClaveProdServTrasladoIVAbyDesc(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			boolean response = false;
			String valres = "";
			
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				for(int i=0; i<mapCatalogos.get("ClaveProdServ").size(); i++){
					if(mapCatalogos.get("ClaveProdServ").get(i).getVal2().equalsIgnoreCase(value)){
						valres = mapCatalogos.get("ClaveProdServ").get(i).getVal5();	
						if(valres.equalsIgnoreCase("No")){
							response = false;
						}else{
							response = true;
						}
						break;
					}else{
						response = true;
					}
				}
			}else{
				response = true;
			}
			
			return response;
		}
		
		//Validacion Encuentra Valor de Retencion del catalogo claveProdServ a travez de la descripcion AMDA
		public static boolean findClaveProdServTrasladoIEPSbyDesc(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			boolean response = false;
			String valres = "";
			
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				for(int i=0; i<mapCatalogos.get("ClaveProdServ").size(); i++){
					if(mapCatalogos.get("ClaveProdServ").get(i).getVal2().equalsIgnoreCase(value)){
						valres = mapCatalogos.get("ClaveProdServ").get(i).getVal6();	
						if(valres.equalsIgnoreCase("No")){
							response = false;
						}else{
							response = true;
						}
						break;
					}else{
						response = true;
					}
				}
			}else{
				response = true;
			}
			
			return response;
		}
	
}
