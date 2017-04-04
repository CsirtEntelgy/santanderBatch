package com.interfactura.firmalocal.controllers;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

//import mx.gob.sat.cfd.x3.ComprobanteDocument;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.interfactura.firmalocal.datamodel.ElementsInvoice;
import com.interfactura.firmalocal.datamodel.Invoice_Masivo;
import com.interfactura.firmalocal.xml.Properties;
import com.interfactura.firmalocal.xml.util.ParseXML_CFDI;

@Controller
public class HistoricoInfoConceptosController {
	@Autowired
	Properties properties;

	private int maxConceptos;
		
	private String pathHistoricoProceso = "/salidas/CFDHistorico/proceso/";
	
	public void readBD(String strOdate){
		try{
			File fileBD = new File(pathHistoricoProceso + "IDSHISTORICO"   + strOdate + ".TXT");
			
			FileOutputStream salidaInfoConceptos = new FileOutputStream(new File(pathHistoricoProceso + "CONCEPTOSHISTORICO" + strOdate + ".TXT"));
			
			FileInputStream fs = new FileInputStream(fileBD);
			DataInputStream in = new DataInputStream(fs);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine = "";
			//int counter = 0;
			while((strLine = br.readLine()) != null){
				if(!strLine.trim().equals("")){
					String [] arrayValuesBd = strLine.trim().split("\\|");		
												
					salidaInfoConceptos.write(extractionInfo(arrayValuesBd, strOdate).getBytes("UTF-8"));
					
					//counter++;
				}			
				
			}
			br.close();
			in.close();
			fs.close();
			
			salidaInfoConceptos.close();
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Exception historicoInfoConceptos:" + e.getMessage());
			try {				
				FileOutputStream fileError = new FileOutputStream(pathHistoricoProceso + "historicoInfoConceptosError.txt");
				fileError.write((e.getMessage()).getBytes("UTF-8"));
				fileError.close();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();				
				System.out.println("Exception al crear historicoInfoConceptosError.txt:" + e.getMessage());
			}
		}
	}
	
	/////////////////////////////Extraction Info from XML//////////////////////////////
	
	public String extractionInfo(String [] arrayValuesBd, String strOdate) throws Exception{
		StringBuilder sb = new StringBuilder();
        
		
        if(!arrayValuesBd[2].equals("")){    
            maxConceptos = 1;
            //int counterCfd = 0;
            
            	File xmlFile = new File(arrayValuesBd[2]);
            	            	
                if (xmlFile.exists()) {
                    BufferedReader fr = null;
                    
                    try {                  	                    	
                        
                        	fr = new BufferedReader(new FileReader(xmlFile));
                            StringBuilder s = new StringBuilder();
                            while (fr.ready()) {
                                s.append(fr.readLine());
                            }
                            System.out.println("idFactura:" + arrayValuesBd[1] + " contenido:" + s.toString());
                            ParseXML_CFDI parser = new ParseXML_CFDI();
                            Invoice_Masivo invoice = parser.parse(xmlFile);
                            
                            /*ComprobanteDocument compDoc = ComprobanteDocument.Factory.parse(s.toString().replace("xmlns=\"http://www.santander.com.mx/schemas/xsd/AddendaSantanderV1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"", ""));
                            String nombreEmisor;
                            if (compDoc.getComprobante().getEmisor() != null) {
                                nombreEmisor = compDoc.getComprobante().getEmisor().getNombre();
                            } else {
                                nombreEmisor = "NO DISPONIBLE";
                            }
                            String rfcEmisor;
                            if (compDoc.getComprobante().getEmisor() != null) {
                                rfcEmisor = compDoc.getComprobante().getEmisor().getRfc();
                            } else {
                                rfcEmisor = "NO DISPONIBLE";
                            }
                             */
                            //String renglon = armarRenglon(nombreEmisor, rfcEmisor, invoice, valuesCfd, fileName);
                                                        
                            String renglonConceptos = armarRenglonConceptos(invoice, arrayValuesBd[1]);
                            
                            
                            sb.append(renglonConceptos);
             
                        
                    } catch (Exception e) {
                        System.out.println("Error parsing archivo reporte para la factura con folioInterno " + arrayValuesBd[4] + " -folioSAT " + arrayValuesBd[3]);
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                        System.out.println(Arrays.toString(e.getStackTrace()));
                        sb.append("Informacion no disponible para la factura con folioInterno " + arrayValuesBd[4] + " -folioSAT " + arrayValuesBd[3] + "\n");
                        
                    } finally {
                        if (fr != null) {
                            try {
                                fr.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }                        
                    }
                }else{
                	sb.append("No fue encontrado en Ondemand el XML de la factura con folioInterno " + arrayValuesBd[4] + " -folioSAT " + arrayValuesBd[3] + "\n");
                }
                //counterCfd++;
                       
            
        } else {
            
        	sb.append("No fue encontrado en Ondemand el XML de la factura con folioInterno " + arrayValuesBd[4] + " -folioSAT " + arrayValuesBd[3] + "\n");
            
        }
		return sb.toString();
	}
	
	
	private String armarRenglonConceptos(Invoice_Masivo invoice, String idFactura) throws Exception{
        StringBuilder sb = new StringBuilder();
                        
        if (invoice.getElements() != null && !invoice.getElements().isEmpty()) {
            if (invoice.getElements().size() > maxConceptos) {
                maxConceptos = invoice.getElements().size();
            }
            for (ElementsInvoice concepto : invoice.getElements()) {
            	sb.append("c").append(">");
            	sb.append(idFactura).append(">");											//numero de Factura
                sb.append(concepto.getQuantity()).append(">");
                sb.append(translateCodes(concepto.getUnitMeasure())).append(">");
                sb.append(translateCodes(concepto.getDescription())).append(">");
                sb.append(concepto.getUnitPrice()).append(">");
                sb.append(concepto.getAmount()).append(">");
                sb.append("\n");
            }
        }
        
        
        return sb.toString();
    }
	
	
	public String translateCodes(String cadena) throws Exception{
		org.apache.commons.lang3.text.translate.UnicodeEscaper escaper = org.apache.commons.lang3.text.translate.UnicodeEscaper.above(127);
		org.apache.commons.lang3.text.translate.UnicodeEscaper escaperMayorQue = org.apache.commons.lang3.text.translate.UnicodeEscaper.between(62, 62);
		
		String strEscaped = "";
		if(cadena != null && !cadena.equals("")){
			strEscaped = escaper.translate(cadena);
			strEscaped = escaperMayorQue.translate(strEscaped);
		}
		
		return strEscaped;
		
		
	}

}
