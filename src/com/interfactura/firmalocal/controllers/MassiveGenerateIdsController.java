package com.interfactura.firmalocal.controllers;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.interfactura.firmalocal.persistence.FacturasManager;
import com.interfactura.firmalocal.xml.Properties;

@Controller
public class MassiveGenerateIdsController {

	@Autowired
	Properties properties;
	
	String PathFacturacionEntrada=MassiveReadController.PathFacturacionEntrada;
	String PathFacturacionProceso=MassiveReadController.PathFacturacionProceso;
	String PathFacturacionSalida=MassiveReadController.PathFacturacionSalida;
	String PathFacturacionOndemand=MassiveReadController.PathFacturacionOndemand;
	
	public void readIdFileProcess(){
		try{
			//FileInputStream fsExcelsToProcess = new FileInputStream(properties.getPathFacturacionEntrada() + "IDFILEPROCESS_" + nProceso + ".TXT");
			FileInputStream fsExcelsToProcess = new FileInputStream(PathFacturacionEntrada + "IDFILEPROCESS.TXT");
			DataInputStream in = new DataInputStream(fsExcelsToProcess);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			int counter = 0;
			//FileOutputStream fileStatus = new FileOutputStream(properties.getPathFacturacionProceso() + "STATUS_DECOMPRESS_" + nProceso + ".TXT");
			FileOutputStream fileStatus = new FileOutputStream(PathFacturacionProceso + "massiveIdsInvoice.txt");
			fileStatus.write(("Status del proceso bash massiveIdsInvoice.sh" + "\n").getBytes("UTF-8"));
			while((strLine = br.readLine()) != null){
				if(!strLine.trim().equals("")){
					String [] arrayValues = strLine.trim().split("\\|");
					
					//Creando el archivo .sql
					FileOutputStream fileQuery = new FileOutputStream(PathFacturacionProceso + arrayValues[1] + "/IDSDIARIO" + arrayValues[1] + "QUERY.SQL");
					
					String strQuery = buildQuery(arrayValues[1]);
					
					fileQuery.write(strQuery.getBytes("UTF-8"));
					fileQuery.close();
				
					fileStatus.write(("El archivo IDS" + arrayValues[1] + "QUERY.SQL se ha generado exitosamente\n").getBytes("UTF-8"));
								
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
			System.out.println("Exception massiveIdsInvoice:" + e.getMessage());
			try {
				//FileOutputStream fileError = new FileOutputStream(properties.getPathReportesProceso() + "ERROR_DECOMPRESS_" + nProceso + ".TXT");
				FileOutputStream fileError = new FileOutputStream(PathFacturacionProceso + "massiveIdsInvoiceError.txt");
				fileError.write((e.getMessage()).getBytes("UTF-8"));
				fileError.close();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				//System.out.println("Exception al crear ERROR_DECOMPRESS_" + nProceso + ".TXT:" + e.getMessage());
				System.out.println("Exception al crear massiveIdsInvoiceError.txt:" + e.getMessage());
			}			
		}
	}
	
	public String buildQuery(String strFileName){
		return "set head off \n" +
				  "set feedback off \n" +
				  "set lin 32767 \n" +
				  "select f.ID_FACT||'|'||" +
				  "f.TXT_XML_ROUTE||'|'||" +
				  "f.TXT_FOLIO_SAT||'|'||" +
				  "f.NUM_FOLIO_INT" +
					" from facturas f where f.TXT_NOM_ARCH='FACTURASDIARIO" +
					strFileName + ".TXT';" + "\n";
	}
}
