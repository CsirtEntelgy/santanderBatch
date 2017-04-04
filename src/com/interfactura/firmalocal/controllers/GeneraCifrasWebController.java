package com.interfactura.firmalocal.controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.interfactura.firmalocal.cifras.CifrasEntidad;
import com.interfactura.firmalocal.cifras.CifrasIE;
import com.interfactura.firmalocal.cifras.WebServiceCifrasCliente;
import com.interfactura.firmalocal.persistence.FiscalEntityManager;
import com.interfactura.firmalocal.xml.Properties;

@Controller
public class GeneraCifrasWebController {

	private WebServiceCifrasCliente serviceCifrasPort = null;
	
	@Autowired
	public FiscalEntityManager fiscal;
	
	public String APLICATIVO;

	@Autowired(required = true)
    private Properties properties;
	
	public void extractionOndemand(String pathCifras, int tipoFormato, String fechaAyer){
				
		try{
									
			System.out.println("pathCifras: " + pathCifras);
			/*System.out.println("pathFoliosXml: " + pathFoliosXml);
			System.out.println("pathFoliosInc: " + pathFoliosInc);
			System.out.println("path: " + path);
			System.out.println("pathDownload: " + pathDownload);*/
														
			//Consumir web service CifrasControlSantander.asmx
			if(this.serviceCifrasPort == null){
				this.serviceCifrasPort = new WebServiceCifrasCliente();								
			}
			//String strUrlWS = "https://santandes.interfactura.com/CifrasControl/CifrasControlSantander.asmx?wsdl";
			String strUrlWS = properties.getUrlWebServiceCifrasControl();
			
			String bloqueCifras = "";
									
			if(tipoFormato == 5){
				bloqueCifras = this.serviceCifrasPort.generaCifrasControl(strUrlWS, fechaAyer, tipoFormato, "", 0);
				System.out.println("Respuesta WS CifrasContrlSantander:" + bloqueCifras);
				
				if(bloqueCifras.trim().equals(""))
					System.out.println("No se encontraron en Interfactura, facturas de tipo " + tipoFormato + ", con fecha de emision >= a " + fechaAyer + " 00:00:00 y <= al " + fechaAyer + " 23:59:59");
				else if(bloqueCifras.trim().lastIndexOf("ERRORCIFRASFACWEB:") != -1)
					System.out.println(bloqueCifras.trim());
				else
					this.generaCifrasNotasDeCredito(bloqueCifras, pathCifras, fechaAyer);					
				
			}
			else if(tipoFormato == 3){
				bloqueCifras = this.serviceCifrasPort.generaCifrasControl(strUrlWS, fechaAyer, tipoFormato, "", 0);
				System.out.println("Respuesta WS CifrasContrlSantander:" + bloqueCifras);
				
				if(bloqueCifras.trim().equals(""))
					System.out.println("No se encontraron en Interfactura, facturas de tipo " + tipoFormato + ", con fecha de emision >= a " + fechaAyer + " 00:00:00 y <= al " + fechaAyer + " 23:59:59");
				else if(bloqueCifras.trim().lastIndexOf("ERRORCIFRASFACWEB:") != -1)
					System.out.println(bloqueCifras.trim());
				else
					this.generaCifrasDonataria(bloqueCifras, pathCifras, fechaAyer);
			}
			else if(tipoFormato == 2){
				bloqueCifras = this.serviceCifrasPort.generaCifrasControl(strUrlWS, fechaAyer, tipoFormato, "FACTORAJE", 0);
				System.out.println("Respuesta WS CifrasContrlSantander:" + bloqueCifras);
				
				if(bloqueCifras.trim().equals(""))
					System.out.println("No se encontraron en Interfactura, facturas de tipo " + tipoFormato + ", con fecha de emision >= a " + fechaAyer + " 00:00:00 y <= al " + fechaAyer + " 23:59:59");
				else if(bloqueCifras.trim().lastIndexOf("ERRORCIFRASFACWEB:") != -1)
					System.out.println(bloqueCifras.trim());
				else
					this.generaCifrasFactorajeConfirming(bloqueCifras, pathCifras, fechaAyer);
			}else if(tipoFormato == 6){
				bloqueCifras = this.serviceCifrasPort.generaCifrasControl(strUrlWS, fechaAyer, tipoFormato, "CONFIRMING", 0);
				System.out.println("Respuesta WS CifrasContrlSantander:" + bloqueCifras);
				
				if(bloqueCifras.trim().equals(""))
					System.out.println("No se encontraron en Interfactura, facturas de tipo " + tipoFormato + ", con fecha de emision >= a " + fechaAyer + " 00:00:00 y <= al " + fechaAyer + " 23:59:59");
				else if(bloqueCifras.trim().lastIndexOf("ERRORCIFRASFACWEB:") != -1)
					System.out.println(bloqueCifras.trim());
				else
					this.generaCifrasFactorajeConfirming(bloqueCifras, pathCifras, fechaAyer);
			}else if(tipoFormato == 1 || tipoFormato == 4){
				bloqueCifras = this.serviceCifrasPort.generaCifrasControl(strUrlWS, fechaAyer, tipoFormato, "", 0);
				System.out.println("Respuesta WS CifrasContrlSantander:" + bloqueCifras);
				
				if(bloqueCifras.trim().equals(""))
					System.out.println("No se encontraron en Interfactura, facturas de tipo " + tipoFormato + ", con fecha de emision >= a " + fechaAyer + " 00:00:00 y <= al " + fechaAyer + " 23:59:59");
				else if(bloqueCifras.trim().lastIndexOf("ERRORCIFRASFACWEB:") != -1)
					System.out.println(bloqueCifras.trim());
				else
					this.generaCifrasFUnico_Divisas(bloqueCifras, pathCifras, fechaAyer);
			}
			else
				System.out.println("El Tipo de formato " + tipoFormato + " es incorrecto!!");
								
			
			
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Exception GeneraCifrasWeb:" + e.getMessage());
		}
	}
	
	//Crea nuevo objeto CifrasEntidad
	public CifrasEntidad creaCifrasEntidad(String periodo, String nombreEmisor_Aplicativo, String tipo, boolean esVigente, CifrasIE cifrasIE, long totalComprobantesSAT, String moneda, String tasaIva){
		CifrasEntidad cifrasEntidad = new CifrasEntidad();
		
		cifrasEntidad.setNombre(nombreEmisor_Aplicativo);
		cifrasEntidad.setPeriodo(periodo);
		cifrasEntidad.setMoneda(moneda);
		cifrasEntidad.setTasaIva(tasaIva);	
		
		//SAT
		
		if(tipo.equals("ingreso")){						
			if(esVigente)
			{
				cifrasEntidad.setIngresosSAT(cifrasIE);			
				cifrasEntidad.setContadorIngresosSAT(totalComprobantesSAT);	
			}else{
				cifrasEntidad.setIngresosCanceladosSAT(cifrasIE);
				cifrasEntidad.setContadorIngresosCanceladosSAT(totalComprobantesSAT);
			}
			
			
		}else if(tipo.equals("egreso")){
			if(esVigente)
			{	
				cifrasEntidad.setEgresosSAT(cifrasIE);
				cifrasEntidad.setContadorEgresosSAT(totalComprobantesSAT);
			}else{
				cifrasEntidad.setEgresosCanceladosSAT(cifrasIE);
				cifrasEntidad.setContadorEgresosCanceladosSAT(totalComprobantesSAT);
			}
			
		}
						
		return cifrasEntidad;
	}
	
	//Genera Cifras para Notas Credito
	public void generaCifrasNotasDeCredito(String bloqueCifras, String pathCifras, String fechaAyer) throws Exception{
		String [] bloquesCifrasCompleto = bloqueCifras.split("_<CCSantan>_");
		HashMap<String, CifrasEntidad> hashSat = new HashMap<String, CifrasEntidad>();
		
		if(bloquesCifrasCompleto.length > 0 && !bloquesCifrasCompleto[0].trim().equals("")){
			//Vigentes
			String [] bloquesCifras = bloquesCifrasCompleto[0].split("<CCSantan#>");
						
			for(String bloque : bloquesCifras){
				String [] cifras = bloque.split("<CCSantan>");
									 
				long totalComprobantes = Long.parseLong(cifras[0].trim());
				String nombreEmisor = cifras[1].trim();
				String periodo = getPeriodo(cifras[2].trim());			
				String moneda = cifras[3].trim();
				String tasaIva = cifras[4].trim();								
				String tipoComprobante = cifras[5].trim();
				double subTotalAcumulado = Double.parseDouble(cifras[6].trim());
				double totalAcumulado = Double.parseDouble(cifras[7].trim());
									
				String key = nombreEmisor + periodo + moneda + tasaIva;
				System.out.println("vigente - " + key);
				//TotalComprobantes, NombreEmisor, RFCEmisor, Moneda, TipoComprobante, SubTotalAcumulado, ImpuestosAcumulados, TotalAcumulado
				if(hashSat.containsKey(key)){
					System.out.println("ya existe - ");
					hashSat.get(key).getEgresosSAT().setSubTotal(hashSat.get(key).getEgresosSAT().getSubTotal() + subTotalAcumulado);
					hashSat.get(key).getEgresosSAT().setTotal(hashSat.get(key).getEgresosSAT().getTotal() + totalAcumulado);
					
					hashSat.get(key).setContadorEgresosSAT(hashSat.get(key).getContadorEgresosSAT() + totalComprobantes);
					
				}else{
					System.out.println("nuevo - ");
					CifrasIE cifrasIE = new CifrasIE();
					cifrasIE.setSubTotal(subTotalAcumulado);
					cifrasIE.setTotal(totalAcumulado);
								
					
					hashSat.put(key, this.creaCifrasEntidad(periodo, nombreEmisor, "egreso", true, cifrasIE, totalComprobantes, moneda, tasaIva));				
					
				}						
				
			}
		}
		
		if(bloquesCifrasCompleto.length > 1 && !bloquesCifrasCompleto[1].trim().equals("")){
			//Cancelados
			String [] bloquesCifrasCancelados = bloquesCifrasCompleto[1].split("<CCSantan#>");
					
			for(String bloque : bloquesCifrasCancelados){
				String [] cifras = bloque.split("<CCSantan>");
									 
				long totalComprobantes = Long.parseLong(cifras[0].trim());
				String nombreEmisor = cifras[1].trim();
				String periodo = getPeriodo(cifras[2].trim());			
				String moneda = cifras[3].trim();
				String tasaIva = cifras[4].trim();								
				String tipoComprobante = cifras[5].trim();
				double subTotalAcumulado = Double.parseDouble(cifras[6].trim());
				double totalAcumulado = Double.parseDouble(cifras[7].trim());
									
				String key = nombreEmisor + periodo + moneda + tasaIva;
				System.out.println("cancelado - " + key);
				//TotalComprobantes, NombreEmisor, RFCEmisor, Moneda, TipoComprobante, SubTotalAcumulado, ImpuestosAcumulados, TotalAcumulado
				if(hashSat.containsKey(key)){
					System.out.println("ya existe - ");
					
					hashSat.get(key).getEgresosCanceladosSAT().setSubTotal(hashSat.get(key).getEgresosCanceladosSAT().getSubTotal() + subTotalAcumulado);
					hashSat.get(key).getEgresosCanceladosSAT().setTotal(hashSat.get(key).getEgresosCanceladosSAT().getTotal() + totalAcumulado);
					
					hashSat.get(key).setContadorEgresosCanceladosSAT(hashSat.get(key).getContadorEgresosCanceladosSAT() + totalComprobantes);
					
				}else{
					System.out.println("nuevo - ");
					
					CifrasIE cifrasIE = new CifrasIE();
					cifrasIE.setSubTotal(subTotalAcumulado);
					cifrasIE.setTotal(totalAcumulado);
					
					
					hashSat.put(key, this.creaCifrasEntidad(periodo, nombreEmisor, "egreso", false, cifrasIE, totalComprobantes, moneda, tasaIva));				
					
				}						
				
			}
		}
		escribeCifrasNotasDeCredito(hashSat, pathCifras, fechaAyer);
	}
	
	public void escribeCifrasNotasDeCredito(HashMap<String, CifrasEntidad> hashSat, String pathCifras, String fechaAyer) throws Exception{
		
		File fileCifras = null;
		FileOutputStream salidaCifras = null;
		
		DecimalFormat df = new DecimalFormat("0.00");
		
		fileCifras = new File(pathCifras);
		salidaCifras = new FileOutputStream(fileCifras);
					
		//Cifras SAT vigentes y canceladas
		
		Iterator itSat = hashSat.entrySet().iterator();
		while (itSat.hasNext()) {
			Map.Entry e = (Map.Entry)itSat.next();
			CifrasEntidad cifras = new CifrasEntidad();
			cifras = (CifrasEntidad) e.getValue();
			System.out.println("key: " + e.getKey()); 
				
			String tasaIva = getTasaIva(cifras.getTasaIva());
			
			salidaCifras.write((cifras.getNombre() + "|" + cifras.getPeriodo() + "|" + cifras.getMoneda() + "|" +  
					cifras.getContadorEgresosSAT() + "|" + 
					df.format(cifras.getEgresosSAT().getSubTotal()) + "|" + 
					tasaIva + "|" + 
					df.format(cifras.getEgresosSAT().getTotal()) + "|" + 
					cifras.getContadorEgresosCanceladosSAT() + "|" +
					df.format(cifras.getEgresosCanceladosSAT().getSubTotal()) + "|" + 
					tasaIva + "|" + 
					df.format(cifras.getEgresosCanceladosSAT().getTotal()) +  
					"|" + "\r\n").getBytes("UTF-8"));
						
			System.out.println(cifras.getNombre() + "|" + cifras.getPeriodo() + "|" + cifras.getMoneda() + "|" +  
					cifras.getContadorEgresosSAT() + "|" + 
					df.format(cifras.getEgresosSAT().getSubTotal()) + "|" + 
					tasaIva + "|" + 
					df.format(cifras.getEgresosSAT().getTotal()) + "|" + 
					cifras.getContadorEgresosCanceladosSAT() + "|" +
					df.format(cifras.getEgresosCanceladosSAT().getSubTotal()) + "|" + 
					tasaIva + "|" + 
					df.format(cifras.getEgresosCanceladosSAT().getTotal()) +  
					"|");
						
		}
				
		if(salidaCifras != null)
			salidaCifras.close();
	}
	
	//Genera Cifras para Donataria
	public void generaCifrasDonataria(String bloqueCifras, String pathCifras, String fechaAyer) throws Exception{
		String [] bloquesCifrasCompleto = bloqueCifras.split("_<CCSantan>_");
		HashMap<String, CifrasEntidad> hashSat = new HashMap<String, CifrasEntidad>();
		
		if(bloquesCifrasCompleto.length > 0 && !bloquesCifrasCompleto[0].trim().equals("")){
			//Vigentes
			String [] bloquesCifras = bloquesCifrasCompleto[0].split("<CCSantan#>");
						
			for(String bloque : bloquesCifras){
				String [] cifras = bloque.split("<CCSantan>");
									 
				long totalComprobantes = Long.parseLong(cifras[0].trim());
				String nombreEmisor = cifras[1].trim();
				String periodo = getPeriodo(cifras[2].trim());
												
				String tipoComprobante = cifras[3].trim();
				
				double totalAcumulado = Double.parseDouble(cifras[4].trim());
									
				String key = nombreEmisor + periodo;
				//TotalComprobantes, NombreEmisor, RFCEmisor, Moneda, TipoComprobante, SubTotalAcumulado, ImpuestosAcumulados, TotalAcumulado
				if(hashSat.containsKey(key)){
									
					hashSat.get(key).getIngresosSAT().setTotal(hashSat.get(key).getIngresosSAT().getTotal() + totalAcumulado);
					
					hashSat.get(key).setContadorIngresosSAT(hashSat.get(key).getContadorIngresosSAT() + totalComprobantes);
					
				}else{
					CifrasIE cifrasIE = new CifrasIE();
					cifrasIE.setTotal(totalAcumulado);
											
					hashSat.put(key, this.creaCifrasEntidad(periodo, nombreEmisor, "ingreso", true, cifrasIE, totalComprobantes, "", ""));
					
					
				}						
				
			}
		}
		
		if(bloquesCifrasCompleto.length > 1 && !bloquesCifrasCompleto[1].trim().equals("")){
			//Cancelados
			String [] bloquesCifrasCancelados = bloquesCifrasCompleto[1].split("<CCSantan#>");
				
			for(String bloque : bloquesCifrasCancelados){
				String [] cifras = bloque.split("<CCSantan>");
									 
				long totalComprobantes = Long.parseLong(cifras[0].trim());
				String nombreEmisor = cifras[1].trim();
				String periodo = getPeriodo(cifras[2].trim());
												
				String tipoComprobante = cifras[3].trim();
				
				double totalAcumulado = Double.parseDouble(cifras[4].trim());
									
				String key = nombreEmisor + periodo;
				//TotalComprobantes, NombreEmisor, RFCEmisor, Moneda, TipoComprobante, SubTotalAcumulado, ImpuestosAcumulados, TotalAcumulado
				if(hashSat.containsKey(key)){
									
					hashSat.get(key).getIngresosCanceladosSAT().setTotal(hashSat.get(key).getIngresosCanceladosSAT().getTotal() + totalAcumulado);
					
					hashSat.get(key).setContadorIngresosCanceladosSAT(hashSat.get(key).getContadorIngresosCanceladosSAT() + totalComprobantes);
					
				}else{
					CifrasIE cifrasIE = new CifrasIE();
					cifrasIE.setTotal(totalAcumulado);
											
					hashSat.put(key, this.creaCifrasEntidad(periodo, nombreEmisor, "ingreso", false, cifrasIE, totalComprobantes, "", ""));
					
					
				}						
				
			}
		}
		escribeCifraDonataria(hashSat, pathCifras, fechaAyer);
	}
	
	public void escribeCifraDonataria(HashMap<String, CifrasEntidad> hashSat, String pathCifras, String fechaAyer) throws Exception{
		
		File fileCifras = null;
		FileOutputStream salidaCifras = null;
		
		DecimalFormat df = new DecimalFormat("0.00");
		
		fileCifras = new File(pathCifras);
		salidaCifras = new FileOutputStream(fileCifras);
					
		//Cifras SAT
		
		Iterator itSat = hashSat.entrySet().iterator();
		while (itSat.hasNext()) {
			Map.Entry e = (Map.Entry)itSat.next();
			CifrasEntidad cifras = new CifrasEntidad();
			cifras = (CifrasEntidad) e.getValue();
			System.out.println("key: " + e.getKey()); 
									
			salidaCifras.write((cifras.getNombre() + "|" + cifras.getPeriodo() + "|" +  
					cifras.getContadorIngresosSAT() + "|" +										
					df.format(cifras.getIngresosSAT().getTotal()) + "|" + 
					cifras.getContadorIngresosCanceladosSAT() + "|" +										
					df.format(cifras.getIngresosCanceladosSAT().getTotal()) +
					"|" + "\r\n").getBytes("UTF-8"));
			
			System.out.print((cifras.getNombre() + "|" + cifras.getPeriodo() + "|" +   
					cifras.getContadorIngresosSAT() + "|" +									
					df.format(cifras.getIngresosSAT().getTotal()) + "|" + 
					cifras.getContadorIngresosCanceladosSAT() + "|" +										
					df.format(cifras.getIngresosCanceladosSAT().getTotal()) +
					"|" + "\r\n").getBytes("UTF-8"));
			
					
		}
	   		
		if(salidaCifras != null)
			salidaCifras.close();
	}
	
	//Genera Cifras para Formato Unico y Divisas
	public void generaCifrasFUnico_Divisas(String bloqueCifras, String pathCifras, String fechaAyer) throws Exception{
		String [] bloquesCifrasCompleto = bloqueCifras.split("_<CCSantan>_");
		HashMap<String, CifrasEntidad> hashSat = new HashMap<String, CifrasEntidad>();
		
		if(bloquesCifrasCompleto.length > 0 && !bloquesCifrasCompleto[0].trim().equals("")){
			//Vigentes
			String [] bloquesCifras = bloquesCifrasCompleto[0].split("<CCSantan#>");
						
			for(String bloque : bloquesCifras){
				String [] cifras = bloque.split("<CCSantan>");
									 
				long totalComprobantes = Long.parseLong(cifras[0].trim());
				String nombreEmisor = cifras[1].trim();
				String periodo = getPeriodo(cifras[2].trim());
				String moneda = cifras[3].trim();
				String tasaIva = cifras[4].trim();
									
				String tipoComprobante = cifras[5].trim();
				double subTotalAcumulado = Double.parseDouble(cifras[6].trim());
				double totalAcumulado = Double.parseDouble(cifras[7].trim());
									
				String key = nombreEmisor + periodo + moneda + tasaIva;
				
				if(hashSat.containsKey(key)){
					
					hashSat.get(key).getIngresosSAT().setSubTotal(hashSat.get(key).getIngresosSAT().getSubTotal() + subTotalAcumulado);
					hashSat.get(key).getIngresosSAT().setTotal(hashSat.get(key).getIngresosSAT().getTotal() + totalAcumulado);
					
					hashSat.get(key).setContadorIngresosSAT(hashSat.get(key).getContadorIngresosSAT() + totalComprobantes);
					
				}else{
					CifrasIE cifrasIE = new CifrasIE();
					cifrasIE.setSubTotal(subTotalAcumulado);
					cifrasIE.setTotal(totalAcumulado);
										
					
					hashSat.put(key, this.creaCifrasEntidad(periodo, nombreEmisor, "ingreso", true, cifrasIE, totalComprobantes, moneda, tasaIva));
					
					
				}						
				
			}
		}
		if(bloquesCifrasCompleto.length > 1 && !bloquesCifrasCompleto[1].trim().equals("")){
			//Cancelados
			String [] bloquesCifrasCancelados = bloquesCifrasCompleto[1].split("<CCSantan#>");
					
			for(String bloque : bloquesCifrasCancelados){
				String [] cifras = bloque.split("<CCSantan>");
									 
				long totalComprobantes = Long.parseLong(cifras[0].trim());
				String nombreEmisor = cifras[1].trim();
				String periodo = getPeriodo(cifras[2].trim());
				String moneda = cifras[3].trim();
				String tasaIva = cifras[4].trim();
									
				String tipoComprobante = cifras[5].trim();
				double subTotalAcumulado = Double.parseDouble(cifras[6].trim());
				double totalAcumulado = Double.parseDouble(cifras[7].trim());
									
				String key = nombreEmisor + periodo + moneda + tasaIva;
				
				if(hashSat.containsKey(key)){
					
					hashSat.get(key).getIngresosCanceladosSAT().setSubTotal(hashSat.get(key).getIngresosCanceladosSAT().getSubTotal() + subTotalAcumulado);
					hashSat.get(key).getIngresosCanceladosSAT().setTotal(hashSat.get(key).getIngresosCanceladosSAT().getTotal() + totalAcumulado);
					
					hashSat.get(key).setContadorIngresosCanceladosSAT(hashSat.get(key).getContadorIngresosCanceladosSAT() + totalComprobantes);
					
				}else{
					CifrasIE cifrasIE = new CifrasIE();
					cifrasIE.setSubTotal(subTotalAcumulado);
					cifrasIE.setTotal(totalAcumulado);
						
					
					hashSat.put(key, this.creaCifrasEntidad(periodo, nombreEmisor, "ingreso", false, cifrasIE, totalComprobantes, moneda, tasaIva));
					
					
				}						
				
			}
		}
		
		escribeCifrasFUnico_Divisas(hashSat, pathCifras, fechaAyer);
	}
	
	public void escribeCifrasFUnico_Divisas(HashMap<String, CifrasEntidad> hashSat, String pathCifras, String fechaAyer) throws Exception{
		File fileCifras = null;
		FileOutputStream salidaCifras = null;
		
		DecimalFormat df = new DecimalFormat("0.00");
		
		fileCifras = new File(pathCifras);
		salidaCifras = new FileOutputStream(fileCifras);
					
		//Cifras SAT
		
		Iterator itSat = hashSat.entrySet().iterator();
		while (itSat.hasNext()) {
			Map.Entry e = (Map.Entry)itSat.next();
			CifrasEntidad cifras = new CifrasEntidad();
			cifras = (CifrasEntidad) e.getValue();
			System.out.println("key: " + e.getKey()); 
			
			String tasaIva = getTasaIva(cifras.getTasaIva());
			
			salidaCifras.write((cifras.getNombre() + "|" + cifras.getPeriodo() + "|" + cifras.getMoneda() + "|" +  
					cifras.getContadorIngresosSAT() + "|" + 
					df.format(cifras.getIngresosSAT().getSubTotal()) + "|" + 
					tasaIva + "|" + 
					df.format(cifras.getIngresosSAT().getTotal()) + "|" + 
					cifras.getContadorIngresosCanceladosSAT() + "|" + 
					df.format(cifras.getIngresosCanceladosSAT().getSubTotal()) + "|" + 
					tasaIva + "|" + 
					df.format(cifras.getIngresosCanceladosSAT().getTotal()) + 
					"|" + "\r\n").getBytes("UTF-8"));
			
						
			System.out.println(cifras.getNombre() + "|" + cifras.getPeriodo() + "|" + cifras.getMoneda() + "|" +  
					cifras.getContadorIngresosSAT() + "|" + 
					df.format(cifras.getIngresosSAT().getSubTotal()) + "|" + 
					tasaIva + "|" + 
					df.format(cifras.getIngresosSAT().getTotal()) + "|" + 
					cifras.getContadorIngresosCanceladosSAT() + "|" + 
					df.format(cifras.getIngresosCanceladosSAT().getSubTotal()) + "|" + 
					tasaIva + "|" + 
					df.format(cifras.getIngresosCanceladosSAT().getTotal()) + 
					"|");
			
						
			
		}
	   		
		if(salidaCifras != null)
			salidaCifras.close();
	}
	
	//Genera Cifras para Factoraje y Confirming
	public void generaCifrasFactorajeConfirming(String bloqueCifras, String pathCifras, String fechaAyer) throws Exception{
		String [] bloquesCifrasCompleto = bloqueCifras.split("_<CCSantan>_");
		HashMap<String, CifrasEntidad> hashSat = new HashMap<String, CifrasEntidad>();
		
		if(bloquesCifrasCompleto.length > 0 && !bloquesCifrasCompleto[0].trim().equals("")){
			//Vigentes
			String [] bloquesCifras = bloquesCifrasCompleto[0].split("<CCSantan#>");
					
			for(String bloque : bloquesCifras){
				String [] cifras = bloque.split("<CCSantan>");
									 
				long totalComprobantes = Long.parseLong(cifras[0].trim());
				String nombreAplicativo = cifras[1].trim();
				String periodo = getPeriodo(cifras[2].trim());
				String moneda = cifras[3].trim();
				String tasaIva = cifras[4].trim();
									
				String tipoComprobante = cifras[5].trim();
				double subTotalAcumulado = Double.parseDouble(cifras[6].trim());
				double totalAcumulado = Double.parseDouble(cifras[7].trim());
									
				String key = nombreAplicativo + periodo + moneda + tasaIva;
				
				if(hashSat.containsKey(key)){
					
					hashSat.get(key).getIngresosSAT().setSubTotal(hashSat.get(key).getIngresosSAT().getSubTotal() + subTotalAcumulado);
					hashSat.get(key).getIngresosSAT().setTotal(hashSat.get(key).getIngresosSAT().getTotal() + totalAcumulado);
					
					hashSat.get(key).setContadorIngresosSAT(hashSat.get(key).getContadorIngresosSAT() + totalComprobantes);
					
				}else{
					CifrasIE cifrasIE = new CifrasIE();
					cifrasIE.setSubTotal(subTotalAcumulado);
					cifrasIE.setTotal(totalAcumulado);
										
					
					hashSat.put(key, this.creaCifrasEntidad(periodo, nombreAplicativo, "ingreso", true, cifrasIE, totalComprobantes, moneda, tasaIva));
					
					
				}						
				
			}
		}
		
		if(bloquesCifrasCompleto.length > 1 && !bloquesCifrasCompleto[1].trim().equals("")){
			//Cancelados
			String [] bloquesCifrasCancelados = bloquesCifrasCompleto[1].split("<CCSantan#>");
				
			for(String bloque : bloquesCifrasCancelados){
				String [] cifras = bloque.split("<CCSantan>");
									 
				long totalComprobantes = Long.parseLong(cifras[0].trim());
				String nombreAplicativo = cifras[1].trim();
				String periodo = getPeriodo(cifras[2].trim());
				String moneda = cifras[3].trim();
				String tasaIva = cifras[4].trim();
									
				String tipoComprobante = cifras[5].trim();
				double subTotalAcumulado = Double.parseDouble(cifras[6].trim());
				double totalAcumulado = Double.parseDouble(cifras[7].trim());
									
				String key = nombreAplicativo + periodo + moneda + tasaIva;
				
				if(hashSat.containsKey(key)){
					
					hashSat.get(key).getIngresosCanceladosSAT().setSubTotal(hashSat.get(key).getIngresosCanceladosSAT().getSubTotal() + subTotalAcumulado);
					hashSat.get(key).getIngresosCanceladosSAT().setTotal(hashSat.get(key).getIngresosCanceladosSAT().getTotal() + totalAcumulado);
					
					hashSat.get(key).setContadorIngresosCanceladosSAT(hashSat.get(key).getContadorIngresosCanceladosSAT() + totalComprobantes);
					
				}else{
					CifrasIE cifrasIE = new CifrasIE();
					cifrasIE.setSubTotal(subTotalAcumulado);
					cifrasIE.setTotal(totalAcumulado);
									
					
					hashSat.put(key, this.creaCifrasEntidad(periodo, nombreAplicativo, "ingreso", false, cifrasIE, totalComprobantes, moneda, tasaIva));
					
					
				}						
				
			}
		}
		escribeCifrasFactorajeConfirming(hashSat, pathCifras, fechaAyer);
	}
	
	public void escribeCifrasFactorajeConfirming(HashMap<String, CifrasEntidad> hashSat, String pathCifras, String fechaAyer) throws Exception{
		File fileCifras = null;
		FileOutputStream salidaCifras = null;
		
		DecimalFormat df = new DecimalFormat("0.00");
		
		fileCifras = new File(pathCifras);
		salidaCifras = new FileOutputStream(fileCifras);
					
		//Cifras SAT
		
		Iterator itSat = hashSat.entrySet().iterator();
		while (itSat.hasNext()) {
			Map.Entry e = (Map.Entry)itSat.next();
			CifrasEntidad cifras = new CifrasEntidad();
			cifras = (CifrasEntidad) e.getValue();
			System.out.println("key: " + e.getKey()); 
			
			String tasaIva = getTasaIva(cifras.getTasaIva());
					
			salidaCifras.write((cifras.getNombre() + "|" + cifras.getPeriodo() + "|" + cifras.getMoneda() + "|" +  
					cifras.getContadorIngresosSAT() + "|" + 
					df.format(cifras.getIngresosSAT().getSubTotal()) + "|" + 
					tasaIva + "|" + 
					df.format(cifras.getIngresosSAT().getTotal()) + "|" + 
					cifras.getContadorIngresosCanceladosSAT() + "|" + 
					df.format(cifras.getIngresosCanceladosSAT().getSubTotal()) + "|" + 
					tasaIva + "|" + 
					df.format(cifras.getIngresosCanceladosSAT().getTotal()) +
					"|" + "\r\n").getBytes("UTF-8"));
			
						
			System.out.println(cifras.getNombre() + "|" + cifras.getPeriodo() + "|" + cifras.getMoneda() + "|" +  
					cifras.getContadorIngresosSAT() + "|" + 
					df.format(cifras.getIngresosSAT().getSubTotal()) + "|" + 
					tasaIva + "|" + 
					df.format(cifras.getIngresosSAT().getTotal()) + "|" + 
					cifras.getContadorIngresosCanceladosSAT() + "|" + 
					df.format(cifras.getIngresosCanceladosSAT().getSubTotal()) + "|" + 
					tasaIva + "|" + 
					df.format(cifras.getIngresosCanceladosSAT().getTotal()) + 
					"|");
			
						
			
		}
	   		
		if(salidaCifras != null)
			salidaCifras.close();
	}
	
	public String getTasaIva(String tasaIva) throws Exception{
		
		if(!tasaIva.trim().equals("")){
			DecimalFormat df = new DecimalFormat("0.00");
			return df.format(Double.parseDouble(tasaIva));
		}
		return tasaIva;	
	}
	
	public String getPeriodo(String periodo) throws Exception{
		if(!periodo.equals("") && periodo.length() > 5)
			return periodo.substring(0, 4) + "-" + periodo.substring(4, 6);
		else
			return periodo;
	}
}
