package com.interfactura.firmalocal.controllers;

import java.io.FileOutputStream;

import org.springframework.stereotype.Controller;

@Controller
public class HistoricoInfoCfdsController {

	private String pathHistoricoProceso = "/salidas/CFDHistorico/proceso/";
	public void createSqlFile(String strOdateStart, String strOdateEnd){
		try{
			System.out.println("strOdateStart: " + strOdateStart);
			System.out.println("strOdateEnd: " + strOdateEnd);
			
			//Creando el archivo .sql
			FileOutputStream fileQuery = new FileOutputStream(pathHistoricoProceso + "CFDSHISTORICO.SQL");
					
			String strQuery = buildQuery(strOdateStart, strOdateEnd);
			
			fileQuery.write(strQuery.getBytes("UTF-8"));
			fileQuery.close();
					
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Exception historicoInfoCfds:" + e.getMessage());
			try {				
				FileOutputStream fileError = new FileOutputStream(pathHistoricoProceso + "historicoInfoCfdsError.txt");
				fileError.write((e.getMessage()).getBytes("UTF-8"));
				fileError.close();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();				
				System.out.println("Exception al crear historicoInfoCfdsError.txt:" + e.getMessage());
			}
		}		
	}
	
	public String buildQuery(String strOdateStart, String strOdateEnd) throws Exception{
					
		String strQueryCondition = " (cfdissuedotros.COD_FRMAT_TYPE = 1 OR cfdissuedotros.COD_FRMAT_TYPE = 2 OR cfdissuedotros.COD_FRMAT_TYPE = 3 OR cfdissuedotros.COD_FRMAT_TYPE = 4)" +
		" AND cfdissuedotros.FCH_DATE_ISSNC >= to_date('" + strOdateStart + "', 'YYYYMMDD') AND cfdissuedotros.FCH_DATE_ISSNC <= to_date('" + strOdateEnd + "', 'YYYYMMDD')";
		
		return "set head off \n" +
		  "set feedback off \n" +
		  "set lin 32767 \n" +				  
			"Select cfdissuedotros.id_cfd||'>'||cfdissuedotros.FLG_IS_CFDI||'>'||" +
			"cfdissuedotros.TXT_FOLIO_SAT||'>'||fiscalentity.taxid||'>'||" + 
			"cfdissuedotros.NUM_FOLIO||'>'||cfdissuedotros.NUM_FOLIO_INT||'>'||" +
			"cfdissuedotros.COD_FRMAT_TYPE||'>'||cfdissuedotros.NUM_STATS||'>'||" +
			"to_char(cfdissuedotros.FCH_CNCEL_DATE, 'yyyy-mm-dd hh24:mi:ss')||'>'||to_char(cfdissuedotros.FCH_DATE_ISSNC, 'yyyy-mm-dd hh24:mi:ss')||'>'||" +
			"fiscalentity.id||'>'||NVL(cfdissuedotros.TXT_SRC_FILE_NAME, ' ')||'>'||" +
			"NVL(cfdissuedotros.TXT_XML_ROUTE, ' ') FROM CFDIssuedOtros inner join fiscalentity on cfdissuedOtros.ID_FSCAL_ENTTY=fiscalentity.id where " +
			
			strQueryCondition + ";\n";	
		
	}

}
