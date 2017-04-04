package com.interfactura.firmalocal.controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.interfactura.firmalocal.domain.entities.MassiveReport;
import com.interfactura.firmalocal.persistence.MassiveReportManager;
import com.interfactura.firmalocal.xml.Properties;

@Controller
public class MassiveReportReadController {

	@Autowired
	Properties properties;

	@Autowired(required = true)
	private MassiveReportManager massiveReportManager;
	
	public static String reportesEntrada="/salidas/masivo/facturas/reportes/entrada/";
	public static String reportesProceso="/salidas/masivo/facturas/reportes/proceso/";
	public static String reportesSalida="/salidas/masivo/facturas/reportes/salida/";
	
	public MassiveReportReadController(){
		
	}
	
	public void createIdReportProcess(String nProcesos){			
		try{	
			List<MassiveReport> listRequestReport = this.getRequests(0, 0);
			
			FileOutputStream fileStatus = new FileOutputStream(reportesProceso + "reportRead.txt");
			fileStatus.write("Status del proceso bash reportRead.sh\n".getBytes("UTF-8"));
			
			int totalProcesos = Integer.parseInt(nProcesos);
			List<FileOutputStream> listFilesIdReport = new ArrayList<FileOutputStream>();
			for(int index=0; index<totalProcesos; index++){
				listFilesIdReport.add(new FileOutputStream(reportesEntrada + "IDREPORTPROCESS_" + String.valueOf(index+1) + ".TXT"));
			}
			//FileOutputStream fileProcess = new FileOutputStream(properties.getPathReportesEntrada() + "IDREPORTPROCESS.TXT");
			if (listRequestReport != null && listRequestReport.size()>0){			
				int counterFile=0;
				for ( MassiveReport mObj : listRequestReport ){
					//fileProcess.write((mObj.getId() + "\n").getBytes("UTF-8"));
					if(counterFile == totalProcesos-1){
						listFilesIdReport.get(counterFile).write((mObj.getId() + "\n").getBytes());
						counterFile=0;
						
					}else{
						listFilesIdReport.get(counterFile).write((mObj.getId() + "\n").getBytes());
						counterFile++;
					}
					//Crear directorio por solicitud
					File fileDirectory = new File(reportesProceso + String.valueOf(mObj.getId()) + "/");
					if(!fileDirectory.exists()){
						fileDirectory.mkdir();
					}else{
						for(File file:fileDirectory.listFiles()){
							file.delete();
						}
					}
					
					//Creando el archivo .sql
					FileOutputStream fileQuery = new FileOutputStream(reportesProceso + mObj.getId() + "/" + mObj.getId() + "QUERY.sql");
					
					String strQuery = buildQuery(mObj.getStrQuery());
					
					fileQuery.write(strQuery.getBytes("UTF-8"));
					fileQuery.close();
				
					fileStatus.write(("El archivo " + mObj.getId() + "QUERY.sql se ha generado exitosamente\n").getBytes("UTF-8"));
					
					//Creando el archivo MAX .sql
					FileOutputStream fileMaxQuery = new FileOutputStream(reportesProceso + mObj.getId() + "/" + mObj.getId() + "MAXQUERY.sql");
					
					String strMaxQuery = buildMaxQuery(mObj.getStrQuery());
					
					fileMaxQuery.write(strMaxQuery.getBytes("UTF-8"));
					fileMaxQuery.close();
					
					//Actualizando la solicitud de reporte
					updateRequest( mObj.getId() );
					
					fileStatus.write(("El archivo " + mObj.getId() + "MAXQUERY.sql se ha generado exitosamente\n").getBytes("UTF-8"));				
				}												
				
				fileStatus.write("El archivo IDREPORTPROCESS.TXT se ha generado exitosamente\n".getBytes("UTF-8"));				
			}else{
				fileStatus.write(("No se encontraron solicitudes a procesar\n").getBytes("UTF-8"));
			}	
			//fileProcess.close();
			for(int iFile=0; iFile<totalProcesos; iFile++){
				listFilesIdReport.get(iFile).close();
			}
			fileStatus.close();
			
		}
		catch(Exception e){
			e.printStackTrace();
			System.out.println("Exception reportRead:" + e.getMessage());
			try {
				FileOutputStream fileError = new FileOutputStream(reportesProceso + "reportReadError.txt");
				fileError.write((e.getMessage()).getBytes("UTF-8"));
				fileError.close();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.out.println("Exception al crear reportReadError.txt:" + e.getMessage());
			}			
		}		
	}
	
	public List<MassiveReport> getRequests(int status, int cfdType) throws Exception{
		
		List<MassiveReport> listPeticiones = new ArrayList<MassiveReport>();
		listPeticiones = massiveReportManager.findByStatus(status, cfdType);
		return listPeticiones;
	}

	public void updateRequest( long id ) throws Exception{
		
		MassiveReport massiveReport = massiveReportManager.getById( id );
		
		//setear las fechas
		Date date = new Date();
		
		massiveReport.setStatus(1);
		massiveReport.setStartprocessdate( date );
		massiveReport.setIssueDate( date );
		massiveReport.setModifiedBy( "masivo" );
		
		try{
			massiveReportManager.update(massiveReport);
		}
		catch(Exception e){
			System.out.println("update massiveReport: Intento 1");
		      reintentos(massiveReport);
		}
				
	}
	
	public String buildQuery(String strQueryCondition){
		return "set head off \n" +
				  "set feedback off \n" +
				  "set lin 32767 \n" +
				  "select f.ID_FACT||'>'||" +
				  	"\nf.NUM_FOLIO||'>'||" +
					"\nf.NUM_FOLIO_INT||'>'||" +
					"\nf.TXT_FOLIO_SAT||'>'||" +
					"\nf.TXT_TIPO_FORM||'>'||" +
					"\nf.NUM_STAT||'>'||" +
					"\nTO_CHAR(f.FCH_EMI, 'YYYY-MM-DD HH24:MI:SS')||'>'||" +
					"\nTO_CHAR(f.FCH_CAN, 'YYYY-MM-DD HH24:MI:SS')||'>'||" +
					"\nf.TXT_RFC_EMI||'>'||" +
					"\nf.TXT_ENT||'>'||" +
					"\nf.TXT_SERIE||'>'||" +
					"\nf.TXT_TIPO_COMP||'>'||" +
					"\nf.TXT_MON||'>'||" +
					"\nf.TXT_TIPO_CAM||'>'||" +
					"\nf.TXT_RFC_CLI||'>'||" +
					"\nf.TXT_ID_EXT||'>'||" +
					"\nf.TXT_NOM_CLI||'>'||" +
					"\nf.TXT_MET_PAGO||'>'||" +
					"\nf.TXT_REG_FISC||'>'||" +
					"\nf.TXT_LUGAR_EXP||'>'||" +
					"\nf.TXT_FORM_PAGO||'>'||" +
					"\nf.TXT_NUM_CTA_PAGO||'>'||" +
					"\nf.TXT_CALLE||'>'||" +
					"\nf.TXT_NUM_INT||'>'||" +
					"\nf.TXT_NUM_EXT||'>'||" +
					"\nf.TXT_COL||'>'||" +
					"\nf.TXT_LOC||'>'||" +
					"\nf.TXT_REF||'>'||" +
					"\nf.TXT_MUNIC||'>'||" +
					"\nf.TXT_STATE||'>'||" +
					"\nf.TXT_PAIS||'>'||" +
					"\nf.TXT_CP||'>'||" +
					"\nf.TXT_COD_CLI||'>'||" +
					"\nf.TXT_NUM_CONT||'>'||" +
					"\nf.TXT_PER||'>'||" +
					"\nf.TXT_CTRO_COST||'>'||" +
					"\nf.TXT_DESC_CONCE||'>'||" +
					"\nf.TXT_TASA_IVA||'>'||" +
					"\nf.NUM_SUB_TOTAL||'>'||" +
					"\nf.POR_IVA||'>'||" +
					"\nf.NUM_TOTAL||'>'||" +
					"\nf.NUM_TIPO_ADDEN||'>'||" +
					"\nf.TXT_EMAIL||'>'||" +
					"\nf.TXT_COD_ISO||'>'||" +
					"\nf.TXT_ORDEN_COMP||'>'||" +
					"\nf.TXT_POS_COMP||'>'||" +
					"\nf.TXT_CTA_CONT||'>'||" +
					"\nf.TXT_CTRO_COST||'>'||" +
					"\nf.TXT_NUM_CONT_ARRND||'>'||" +
					"\nf.TXT_FECHA_VENC||'>'||" +
					"\nf.TXT_NOM_BENEF||'>'||" +
					"\nf.TXT_INST_RECEP||'>'||" +
					"\nf.TXT_NUM_CTA||'>'||" +
					"\nf.TXT_NUM_PROV||'>'||" +
					"\nf.TXT_MTVO_DESC||'>'||" +
					"\nf.IMP_DESC||'>'||" +
					"\nf.TXT_USR_NAME||'>'||" +
					"\nf.TXT_AREA_NAME||'>'||" +
					"\nc.NUM_CANT||'>'||" +
					"\nc.TXT_UM||'>'||" +
					"\nc.TXT_DESC||'>'||" +
					"\nc.NUM_PREC_UNIT||'>'||" +
					"\nc.NUM_IMPOR" +
					
					" from facturas f inner join conceptos c on f.ID_FACT=c.NUM_FACT_ID where " +
					strQueryCondition + "\n";
	}
	
	public String buildMaxQuery(String strQueryCondition){
		return "set head off \n" +
				  "set feedback off \n" +
				  "set lin 32767 \n" +
					"select max(count_max_concepts) from ( " +
						"SELECT   COUNT (ff.ID_FACT) OVER (PARTITION BY ff.ID_FACT) count_max_concepts, " + 
					    "ff.* from " +
						"( select f.ID_FACT ,f.TXT_SRC_FILE_NAME,f.NUM_FOLIO,f.NUM_FOLIO_INT,f.TXT_FOLIO_SAT, " +
					          "c.ID_CONC id_concepto,c.NUM_FACT_ID,c.NUM_CANT,c.TXT_UM, " +
					          "c.TXT_DESC,c.NUM_PREC_UNIT,c.NUM_IMPOR " +
					    "from facturas f " +
					    "inner join conceptos c " +
					    "on f.ID_FACT=c.NUM_FACT_ID where " +
					    strQueryCondition.replace(";", "") +
					    " ) ff " +
						" order by 1 desc); \n";
		
	}
	
	public void reintentos(MassiveReport massiveReport){
		  try{
		   System.out.println("update massiveReport: Intento 2");
		   massiveReportManager.update(massiveReport);
		  }catch(Exception e2){
		   try{
		    System.out.println("update massiveReport: Intento 3");
		    massiveReportManager.update(massiveReport);
		   }catch(Exception e3){
		    try{
		     System.out.println("update massiveReport: Intento 4");
		     massiveReportManager.update(massiveReport);
		    }catch(Exception e4){
		     try{
		      System.out.println("update massiveReport: Intento 5");
		      massiveReportManager.update(massiveReport);
		     }catch(Exception e5){
		      try{
		       System.out.println("update massiveReport: Intento 6");
		       massiveReportManager.update(massiveReport);
		      }catch(Exception e6){
		       try{
		        System.out.println("update massiveReport: Intento 7");
		        massiveReportManager.update(massiveReport);
		       }catch(Exception e7){
		        try{
		         System.out.println("update massiveReport: Intento 8");
		         massiveReportManager.update(massiveReport);
		        }catch(Exception e8){
		         try{
		          System.out.println("update massiveReport: Intento 9");
		          massiveReportManager.update(massiveReport);
		         }catch(Exception e9){
		          try{
		           System.out.println("update massiveReport: Intento 10");
		           massiveReportManager.update(massiveReport);
		          }catch(Exception e10){
		        	  try{
				           System.out.println("update massiveReport: Intento 11");
				           massiveReportManager.update(massiveReport);
				          }catch(Exception e11){
				        	  try{
					           System.out.println("update massiveReport: Intento 12");
					           massiveReportManager.update(massiveReport);
					          }catch(Exception e12){
					        	  try{
						           System.out.println("update massiveReport: Intento 13");
						           massiveReportManager.update(massiveReport);
						          }catch(Exception e13){
						        	  try{
							           System.out.println("update massiveReport: Intento 14");
							           massiveReportManager.update(massiveReport);
							          }catch(Exception e14){
							        	  try{
								           System.out.println("update massiveReport: Intento 15");
								           massiveReportManager.update(massiveReport);
								          }catch(Exception e15){
								        	  try{
									           System.out.println("update massiveReport: Intento 16");
									           massiveReportManager.update(massiveReport);
									          }catch(Exception e16){
									        	  try{
										           System.out.println("update massiveReport: Intento 17");
										           massiveReportManager.update(massiveReport);
										          }catch(Exception e17){
										        	  try{
											           System.out.println("update massiveReport: Intento 18");
											           massiveReportManager.update(massiveReport);
											          }catch(Exception e18){
											        	  try{
												           System.out.println("update massiveReport: Intento 19");
												           massiveReportManager.update(massiveReport);
												          }catch(Exception e19){
												        	  try{
													           System.out.println("update massiveReport: Intento 20");
													           massiveReportManager.update(massiveReport);
													          }catch(Exception e20){
													           e20.printStackTrace();
													           System.out.println("ERROR BD- " + e20.getMessage());
													          }
												          }
											          }
										          }
									          }
								          }
							          }
						          }
					          }
				          }
		          }
		         }
		        }
		       }
		      }
		     }
		    }
		   }
		  }
		 }

}
