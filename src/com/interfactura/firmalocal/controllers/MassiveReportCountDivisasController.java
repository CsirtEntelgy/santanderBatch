package com.interfactura.firmalocal.controllers;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.interfactura.firmalocal.domain.entities.MassiveReport;
import com.interfactura.firmalocal.persistence.MassiveReportManager;
import com.interfactura.firmalocal.xml.Properties;

@Controller
public class MassiveReportCountDivisasController {
	@Autowired
	Properties properties;
	
	@Autowired(required = true)
	private MassiveReportManager massiveReportManager;
	
	String reportesDivisasEntrada=MassiveReportReadDivisasController.reportesDivisasEntrada;
	String reportesDivisasProceso=MassiveReportReadDivisasController.reportesDivisasProceso;
	String reportesDivisasSalida=MassiveReportReadDivisasController.reportesDivisasSalida;
	
	public MassiveReportCountDivisasController(){
		
	}

	public void checkReportsOK(String nProceso){
		try{					
			
			FileInputStream fsExcelsToProcess = new FileInputStream(reportesDivisasEntrada + "IDREPORTPROCESS_" + nProceso + ".TXT");
			DataInputStream in = new DataInputStream(fsExcelsToProcess);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			FileOutputStream fileStatus = new FileOutputStream(reportesDivisasProceso + "reportCountDivisas_" + nProceso + ".txt");
			fileStatus.write("Status del proceso bash reportCountDivisas.sh\n".getBytes("UTF-8"));
			String strID;
			int counter=0;
			while((strID = br.readLine()) != null){
				MassiveReport massiveReport = massiveReportManager.getById(Long.parseLong(strID));
				Date date = new Date();
				//if(existFile(strID, reportesDivisasSalida)){
					
					massiveReport.setStatus(2);
					massiveReport.setEndprocessdate(date);
					
					massiveReport.setIssueDate(date);
					massiveReport.setModifiedBy( "masivo" );
					
					try{
					massiveReportManager.update(massiveReport);
					}catch(Exception e){
						System.out.println("update massiveReport: Intento 1");
					      reintentos(massiveReport);
					}
					fileStatus.write(("Reporte de la solicitud " + strID + " generado exitosamente\n").getBytes("UTF-8"));
				
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
			System.out.println("Exception reportCountDivisas:" + e.getMessage());
			try {
				FileOutputStream fileError = new FileOutputStream(reportesDivisasProceso + "reportCountDivisasError_" + nProceso + ".txt");
				fileError.write((e.getMessage()).getBytes("UTF-8"));
				fileError.close();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.out.println("Exception al crear reportCountDivisasError_" + nProceso + ".txt:" + e.getMessage());
			}			
		}
	}
	
	public boolean existFile(String strID, String strPathSalida) throws Exception{
		File fileReport = new File(strPathSalida + strID + "REPORT.ZIP");
		return fileReport.exists();		
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
