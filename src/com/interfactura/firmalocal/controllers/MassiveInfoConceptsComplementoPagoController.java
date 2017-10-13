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

import mx.gob.sat.cfd.x3.ComprobanteDocument;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.interfactura.firmalocal.datamodel.ElementsInvoice;
import com.interfactura.firmalocal.datamodel.Invoice_Masivo;
import com.interfactura.firmalocal.xml.Properties;
import com.interfactura.firmalocal.xml.util.ParseXML_CFDI;

@Controller
public class MassiveInfoConceptsComplementoPagoController {
	@Autowired
	Properties properties;
	
	String PathFacturacionEntrada=MassiveReadComplementoPagoController.PathFacturacionEntrada;
	String PathFacturacionProceso=MassiveReadComplementoPagoController.PathFacturacionProceso;
	String PathFacturacionSalida=MassiveReadComplementoPagoController.PathFacturacionSalida;
	String PathFacturacionOndemand=MassiveReadComplementoPagoController.PathFacturacionOndemand;

	private int maxConceptos;
	
	public void readIdFileProcess(){
		try{
			//FileInputStream fsExcelsToProcess = new FileInputStream(properties.getPathFacturacionEntrada() + "IDFILEPROCESS_" + nProceso + ".TXT");
			FileInputStream fsExcelsToProcess = new FileInputStream(PathFacturacionEntrada + "IDFILEPROCESS.TXT");
			DataInputStream in = new DataInputStream(fsExcelsToProcess);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			int counter = 0;
			//FileOutputStream fileStatus = new FileOutputStream(properties.getPathFacturacionProceso() + "STATUS_DECOMPRESS_" + nProceso + ".TXT");
			FileOutputStream fileStatus = new FileOutputStream(PathFacturacionProceso + "massiveInfoConcept.txt");
			fileStatus.write(("Status del proceso bash massiveInfoConcept.sh" + "\n").getBytes("UTF-8"));
			while((strLine = br.readLine()) != null){
				if(!strLine.trim().equals("")){
					String [] arrayValues = strLine.trim().split("\\|");
					File fileBD = new File(PathFacturacionProceso + arrayValues[1] + "/IDSDIARIO"  + arrayValues[1] + "QUERY.TXT");
					if(fileBD.exists()){
						readBD(fileBD,arrayValues[1]);
						fileStatus.write(("Extraccion de informacion exitosa, del archivo " + arrayValues[1] + "QUERY.TXT" + "\n").getBytes("UTF-8"));
					}else{
					
						//No existe el archivo BD para la solicitud de FM
						System.out.println("No existe archivo QUERY para la solicitud de FM: " + arrayValues[1]);
					
						fileStatus.write(("El archivo QUERY" + arrayValues[1] + ".TXT no ha sido leido en la ruta " + PathFacturacionProceso + arrayValues[1] + "/" + "\n").getBytes("UTF-8"));
					}					
				}			
				counter++;
			}			
			br.close();
			in.close();
			fsExcelsToProcess.close();
			if(counter == 0){
				fileStatus.write(("No se encontraron solicitudes a procesar\n").getBytes("UTF-8"));	
			}
			fileStatus.close();
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Exception massiveInfoConcept:" + e.getMessage());
			try {
				//FileOutputStream fileError = new FileOutputStream(properties.getPathReportesProceso() + "ERROR_DECOMPRESS_" + nProceso + ".TXT");
				FileOutputStream fileError = new FileOutputStream(PathFacturacionProceso + "massiveInfoConceptError.txt");
				fileError.write((e.getMessage()).getBytes("UTF-8"));
				fileError.close();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				//System.out.println("Exception al crear ERROR_DECOMPRESS_" + nProceso + ".TXT:" + e.getMessage());
				System.out.println("Exception al crear massiveInfoConceptError.txt:" + e.getMessage());
			}			
		}
	}
	
	public void readBD(File fileBD, String fileName) throws Exception{
		
		
		FileOutputStream salidaInfoConceptos = new FileOutputStream(new File(PathFacturacionProceso + fileName + "/CONCEPTOSDIARIO" + fileName + ".TXT"));
		
		FileInputStream fs = new FileInputStream(fileBD);
		DataInputStream in = new DataInputStream(fs);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine = "";
		int counter = 0;
		while((strLine = br.readLine()) != null){
			if(!strLine.trim().equals("")){
				String [] arrayValuesBd = strLine.trim().split("\\|");		
											
				salidaInfoConceptos.write(extractionInfo(arrayValuesBd, fileName).getBytes("UTF-8"));
				
				counter++;
			}			
			
		}
		br.close();
		in.close();
		fs.close();
		
		salidaInfoConceptos.close();
	}
	
	/////////////////////////////Extraction Info from XML//////////////////////////////
	
	public String extractionInfo(String [] arrayValuesBd, String fileName) throws Exception{
		StringBuilder sb = new StringBuilder();
        
		
        if(!arrayValuesBd[1].equals("")){    
            maxConceptos = 1;
            int counterCfd = 0;
            
            	File xmlFile = new File(arrayValuesBd[1]);
            	            	
                if (xmlFile.exists()) {
                    BufferedReader fr = null;
                    
                    try {                  	                    	
                        
                        	fr = new BufferedReader(new FileReader(xmlFile));
                            StringBuilder s = new StringBuilder();
                            while (fr.ready()) {
                                s.append(fr.readLine());
                            }
                            System.out.println("idFactura:" + arrayValuesBd[0] + " contenido:" + s.toString());
                            ParseXML_CFDI parser = new ParseXML_CFDI();
                            Invoice_Masivo invoice = parser.parse(xmlFile);
                            
                            ComprobanteDocument compDoc = ComprobanteDocument.Factory.parse(s.toString().replace("xmlns=\"http://www.santander.com.mx/schemas/xsd/AddendaSantanderV1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"", ""));
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

                            //String renglon = armarRenglon(nombreEmisor, rfcEmisor, invoice, valuesCfd, fileName);
                                                        
                            String renglonConceptos = armarRenglonConceptos(invoice, arrayValuesBd[0]);
                            
                            
                            sb.append(renglonConceptos);
             
                        
                    } catch (Exception e) {
                        System.out.println("Error parsing archivo reporte para la factura con folioInterno " + arrayValuesBd[5] + " -folioSAT " + arrayValuesBd[4]);
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                        System.out.println(Arrays.toString(e.getStackTrace()));
                        sb.append("Informacion no disponible para la factura con folioInterno " + arrayValuesBd[5] + " -folioSAT " + arrayValuesBd[4] + "\n");
                        
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
                	sb.append("No fue encontrado en Ondemand el XML de la factura con folioInterno " + arrayValuesBd[5] + " -folioSAT " + arrayValuesBd[4] + "\n");
                }
                counterCfd++;
                       
            
        } else {
            
        	sb.append("No fue encontrado en Ondemand el XML de la factura con folioInterno " + arrayValuesBd[5] + " -folioSAT " + arrayValuesBd[4] + "\n");
            
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
