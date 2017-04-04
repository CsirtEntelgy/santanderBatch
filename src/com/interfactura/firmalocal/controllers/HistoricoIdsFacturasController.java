package com.interfactura.firmalocal.controllers;

import java.io.FileOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.interfactura.firmalocal.xml.Properties;

@Controller
public class HistoricoIdsFacturasController {

	@Autowired
	Properties properties;
	
	private String pathHistoricoProceso = "/salidas/CFDHistorico/proceso/";
	public void createSqlFile(String strOdate){
		try{
			//Creando el archivo .sql
			FileOutputStream fileQuery = new FileOutputStream(pathHistoricoProceso + "IDSHISTORICO" + strOdate + ".SQL");
			
			String strQuery = buildQuery(strOdate);
			
			fileQuery.write(strQuery.getBytes("UTF-8"));
			fileQuery.close();
			
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Exception historicoIdsFacturas:" + e.getMessage());
			try {				
				FileOutputStream fileError = new FileOutputStream(pathHistoricoProceso + "historicoIdsFacturasError.txt");
				fileError.write((e.getMessage()).getBytes("UTF-8"));
				fileError.close();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();				
				System.out.println("Exception al crear historicoIdsFacturasError.txt:" + e.getMessage());
			}			
		}
	}
	
	public String buildQuery(String strOdate){
		return "set head off \n" +
				  "set feedback off \n" +
				  "set lin 32767 \n" +
				  "select 'c'||'|'||f.ID_FACT||'|'||" +
				  "f.TXT_XML_ROUTE||'|'||" +
				  "f.TXT_FOLIO_SAT||'|'||" +
				  "f.NUM_FOLIO_INT" +
					" from facturas f where f.TXT_NOM_ARCH='FACTURASHISTORICO" +
					strOdate + ".TXT';" + "\n";
	}
}
