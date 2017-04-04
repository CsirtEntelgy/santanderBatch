package com.interfactura.firmalocal.controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.interfactura.firmalocal.domain.entities.MassiveReport;
import com.interfactura.firmalocal.persistence.MassiveReportManager;
import com.interfactura.firmalocal.xml.Properties;

@Controller
public class MassiveReportDepuraDivisasController {
	@Autowired
	Properties properties;
	
	@Autowired(required=true)
	MassiveReportManager massiveReportManager;
	
	String reportesDivisasEntrada=MassiveReportReadDivisasController.reportesDivisasEntrada;
	String reportesDivisasProceso=MassiveReportReadDivisasController.reportesDivisasProceso;
	String reportesDivisasSalida=MassiveReportReadDivisasController.reportesDivisasSalida;
	
	public MassiveReportDepuraDivisasController(){
		
	}
	public void depura(){
		String [] arrayPath = {reportesDivisasEntrada,reportesDivisasProceso,reportesDivisasSalida};
		try{
			List<MassiveReport> listMassive = massiveReportManager.findByStatus(2, 1); 
			FileOutputStream fileStatus = new FileOutputStream(reportesDivisasProceso + "reportDepuraDivisas.txt");
			fileStatus.write("Status del proceso bash reportDepuraDivisas.sh\n".getBytes("UTF-8"));
			if(listMassive.size()>0){
				List<Long> listIds = new ArrayList<Long>();
				for(MassiveReport mr:listMassive){
					listIds.add(mr.getId());
				}
				for(Long id:listIds){
					//Verificar existencia de directorios creados dinamicamente en ../proceso/
					String strPathProceso = reportesDivisasProceso + String.valueOf(id) + "/";
					File fileDirProceso = new File(strPathProceso); 
					if(fileDirProceso.exists()){
						deleteFiles(String.valueOf(id), strPathProceso);
						fileDirProceso.delete();
					}
					
					//Verificar existencia de directorios creados dinamicamente en ../salida/
					String strPathSalida = reportesDivisasSalida + String.valueOf(id) + "/";
					File fileDirSalida = new File(strPathSalida); 
					if(fileDirSalida.exists()){
						deleteFiles(String.valueOf(id), strPathSalida);
						fileDirSalida.delete();
					}
					//Eliminar archivos del directorio ../salida/
					deleteFiles(String.valueOf(id), reportesDivisasSalida);
					
					try{
						massiveReportManager.delete(id);
					}
					catch(Exception e){
						System.out.println("delete massiveReport: Intento 1");
					    reintentos(id);
					}
					
					fileStatus.write(("Rutas depuradas para la solicitud " + String.valueOf(id) + "\n").getBytes("UTF-8"));
				}	
			}else{
				fileStatus.write("No se econtraron solicitudes para depurar\n".getBytes("UTF-8"));
			}		
			fileStatus.close();			
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Exception reportDepuraDivisas:" + e.getMessage());
			try {
				FileOutputStream fileError = new FileOutputStream(reportesDivisasProceso + "reportDepuraDivisasError.txt");
				fileError.write((e.getMessage()).getBytes("UTF-8"));
				fileError.close();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.out.println("Exception al crear reportDepuraDivisasError.txt:" + e.getMessage());
			}
			
		}	
	}
	
	public void deleteFiles(final String strID, String strPath) throws Exception{
		
		final File folder = new File(strPath);
		final File[] files = folder.listFiles( new FilenameFilter() {
			@Override
			public boolean accept( final File dir,
								   final String name ) {
				return name.matches( strID + ".*" );
			}
		} );
		for ( final File file : files ) {
			if ( !file.delete() ) {
				System.err.println( "Can't remove " + file.getAbsolutePath() );
			}
		}				
	}
	
	public void reintentos(long id){
		  try{
		   System.out.println("delete massiveReport: Intento 2");
		   massiveReportManager.delete(id);
		  }catch(Exception e2){
		   try{
		    System.out.println("delete massiveReport: Intento 3");
		    massiveReportManager.delete(id);
		   }catch(Exception e3){
		    try{
		     System.out.println("delete massiveReport: Intento 4");
		     massiveReportManager.delete(id);
		    }catch(Exception e4){
		     try{
		      System.out.println("delete massiveReport: Intento 5");
		      massiveReportManager.delete(id);
		     }catch(Exception e5){
		      try{
		       System.out.println("delete massiveReport: Intento 6");
		       massiveReportManager.delete(id);
		      }catch(Exception e6){
		       try{
		        System.out.println("delete massiveReport: Intento 7");
		        massiveReportManager.delete(id);
		       }catch(Exception e7){
		        try{
		         System.out.println("delete massiveReport: Intento 8");
		         massiveReportManager.delete(id);
		        }catch(Exception e8){
		         try{
		          System.out.println("delete massiveReport: Intento 9");
		          massiveReportManager.delete(id);
		         }catch(Exception e9){
		          try{
		           System.out.println("delete massiveReport: Intento 10");
		           massiveReportManager.delete(id);
		          }catch(Exception e10){
		        	  try{
				           System.out.println("delete massiveReport: Intento 11");
				           massiveReportManager.delete(id);
				          }catch(Exception e11){
				        	  try{
					           System.out.println("delete massiveReport: Intento 12");
					           massiveReportManager.delete(id);
					          }catch(Exception e12){
					        	  try{
						           System.out.println("delete massiveReport: Intento 13");
						           massiveReportManager.delete(id);
						          }catch(Exception e13){
						        	  try{
							           System.out.println("delete massiveReport: Intento 14");
							           massiveReportManager.delete(id);
							          }catch(Exception e14){
							        	  try{
								           System.out.println("delete massiveReport: Intento 15");
								           massiveReportManager.delete(id);
								          }catch(Exception e15){
								        	  try{
									           System.out.println("delete massiveReport: Intento 16");
									           massiveReportManager.delete(id);
									          }catch(Exception e16){
									        	  try{
										           System.out.println("delete massiveReport: Intento 17");
										           massiveReportManager.delete(id);
										          }catch(Exception e17){
										        	  try{
											           System.out.println("delete massiveReport: Intento 18");
											           massiveReportManager.delete(id);
											          }catch(Exception e18){
											        	  try{
												           System.out.println("delete massiveReport: Intento 19");
												           massiveReportManager.delete(id);
												          }catch(Exception e19){
												        	  try{
													           System.out.println("delete massiveReport: Intento 20");
													           massiveReportManager.delete(id);
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
