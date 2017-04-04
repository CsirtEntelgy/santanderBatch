package com.interfactura.firmalocal.xml.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

public class NombreAplicativo {

	private static Logger logger = Logger.getLogger(NombreAplicativo.class);
	
	//Permite obtener el nombre del aplicativo a partir de las interfaces a procesar
	
	public static HashMap<String, String> cargaNombresApps() throws Exception{
				
		
		
		HashMap<String, String> hashApps= new HashMap<String, String>();
		
		hashApps.put("FACTORAJE", "CFDFACTORAJEFACTURAS");
		hashApps.put("CONFIRMING", "CFDCONFIRMINGFACTURAS");
		hashApps.put("CONFIRMINGNEW", "CFDCONFIRMINGNEW");
		hashApps.put("TECNOLOGICAS", "CFDOPOPICS");
		hashApps.put("FONDOS", "CFDLFFONDOS");
		hashApps.put("CARTERA Y SOFOM", "CFDPTCARTER,CFDPTSOFOMC");
		hashApps.put("ACTIVO", "CFDUGACTIVO");
		hashApps.put("CHEQUES CAPTACION", "CFDBGCAPTA6");
		hashApps.put("NOMINA CAPTACION", "CFDBGCAPTA7");
		hashApps.put("PLAZO", "CFDBGPLAZO");
		hashApps.put("UDIS", "CFDBGUDISS");
		hashApps.put("TARJETAS PAMPA", "CFDLMPAMPAS,CFDLMPAMPAA");
		hashApps.put("SOFIA SERFIN", "CFDFDSSOFIA");
		hashApps.put("SOFIA SANTANDER", "CFDFDUSOFIA");
		hashApps.put("BRANCH", "CFDOPBANCO");
		hashApps.put("MEXDER 21", "CFDOPMEXD21");
		hashApps.put("OPICS DIARIA", "CFDOPRBANCO,CFDOPRCASA,CFDOPRGEST");
		hashApps.put("MEXDER 22", "CFDOPMEXD22");
		hashApps.put("CASA DE BOLSA", "CFDOPCASA");
		hashApps.put("GESTORA", "CFDOPGEST");
		hashApps.put("PLAN DE PERSONA DE RETIRO", "CFDECPPR");
		hashApps.put("BRANCH FACTORAJE", "CFDECBRANCH7,CFDECBRANCH8,CFDECBRANCH9");
		hashApps.put("CHICAGO", "CFDCHICAGO");
		hashApps.put("ING", "CFDPTINGEDC");
		hashApps.put("COFFER", "CFDCIRCOFF");
		hashApps.put("REPROCESO OPICS", "CFDCIRBANCO,CFDCIRCASA,CFDCIRGEST");
		hashApps.put("BOSQUES", "CFDPTSCOTIA");
		hashApps.put("OPICS REPROCESO", "CFDOPRBANCORP,CFDOPRCASARP,CFDOPRGESTRP,CFDOPRCOFFRP");
			
		return hashApps;
	}
	
	public static String obtieneNombreApp(HashMap<String, String> hashApps, String interfaces, String numeroMalla){
		String nombreAplicativo = "";
		
		if(numeroMalla.trim().equals("6") || numeroMalla.trim().equals("7"))
			interfaces = interfaces + numeroMalla;
		
		
		String [] arrayInterfaces = interfaces.split(",");
		
		int i = 0;
		boolean exit = false;
		while (i < arrayInterfaces.length && !exit)
		{	Iterator it = hashApps.entrySet().iterator();
			System.out.println("arrayInterfaces " + i + ":" + arrayInterfaces[i]);
			while (it.hasNext() && !exit) {
				Map.Entry e = (Map.Entry)it.next();
				String nombresApps = (String) e.getValue();
				System.out.println("keyApp: " + e.getKey());
			
				String [] arrayValues = nombresApps.split(","); 
				System.out.println("arrayValues :" + nombresApps);
				for(int j=0; j<arrayValues.length; j++){
					System.out.println("arrayValues " + j + ":" + arrayValues[j]);
					System.out.println("nombreAplicativo:" + nombreAplicativo);
					System.out.println("---");
					if(arrayInterfaces[i].trim().equals(arrayValues[j].trim())){
						if(nombreAplicativo.equals("")){
							nombreAplicativo = (String) e.getKey();
						}else if(!nombreAplicativo.equals((String) e.getKey())){
								nombreAplicativo = "";
								exit = true;							
						}					
					}
				}			
			}
			i++;
		}
		
		
		return nombreAplicativo;
	}
	
	public static boolean validaNombreApp(HashMap<String, String> hashApps, String aplicativo){
		if(hashApps.containsKey(aplicativo.trim()))
			return true;
		else
			return false;
	}
	/*
	public static String getNombreAplicativoECB(String fileNames, String nombreInterface, HashMap<String, String> hashApps){
		if(fileNames.trim().equals("CFDREPROCESOECB"))
			return nombreInterface;
		else
			return obtieneNombreApp(hashApps, fileNames);
	}
	*/
}
