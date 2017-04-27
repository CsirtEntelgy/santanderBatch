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
		
		if(mapCatalogos.size() > 0 && value.trim() != ""){
			for(int i=0; i<mapCatalogos.get("NumRegIdTrib").size(); i++){
				if(mapCatalogos.get("NumRegIdTrib").get(i).getVal2().equalsIgnoreCase(value)){
					response = mapCatalogos.get("NumRegIdTrib").get(i).getVal1();
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
	
	//Validacion Encuentra Valor Maximo de Tasa o Cuota del catalogo TasaOCuota AMDA
	public static String findValMaxTasaOCuota(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value1, String value2){
		String response = "";
		System.out.println("Validacion findValMaxTasaOCuota AMDA : " + value1 + " : " + value2);
		if(mapCatalogos.size() > 0 && value1.trim() != "" && value2.trim() != ""){
			for(int i=0; i<mapCatalogos.get("TasaOCuota").size(); i++){
				if(mapCatalogos.get("TasaOCuota").get(i).getVal4().equalsIgnoreCase(value1) && mapCatalogos.get("TasaOCuota").get(i).getVal5().equalsIgnoreCase(value2) ){
					response = mapCatalogos.get("TasaOCuota").get(i).getVal1();
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
	
}
