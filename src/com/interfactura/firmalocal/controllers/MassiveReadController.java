package com.interfactura.firmalocal.controllers;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.interfactura.firmalocal.domain.entities.Massive;
import com.interfactura.firmalocal.persistence.MassiveManager;
import com.interfactura.firmalocal.xml.Properties;

@Controller
public class MassiveReadController {
	@Autowired
	Properties properties;

	@Autowired(required = true)
	private MassiveManager massiveManager;
		
	public static String PathFacturacionEntrada="/salidas/masivo/facturas/facturacion/entrada/";
	public static String PathFacturacionProceso="/salidas/masivo/facturas/facturacion/proceso/";
	public static String PathFacturacionSalida="/salidas/masivo/facturas/facturacion/salida/";
	public static String PathFacturacionOndemand="/salidas/masivo/facturas/facturacion/ondemand/";
	
	public MassiveReadController(){
		
	}
	
	public void createIdFileProcess(){			
		try{	
			List<Massive> listRequest = this.getRequests(0, 0);
			
			FileOutputStream fileStatus = new FileOutputStream(PathFacturacionProceso + "massiveRead.txt");
			fileStatus.write("Status del proceso bash massiveRead.sh\n".getBytes("UTF-8"));
			
			//int totalProcesos = Integer.parseInt(nProcesos);
			//List<FileOutputStream> listFilesIdReport = new ArrayList<FileOutputStream>();
			/*for(int index=0; index<totalProcesos; index++){
				listFilesIdReport.add(new FileOutputStream(properties.getPathFacturacionEntrada() + "IDFILEPROCESS_" + String.valueOf(index) + ".TXT"));
			}*/
						
			FileOutputStream fileProcess = new FileOutputStream(PathFacturacionEntrada + "IDFILEPROCESS.TXT");
			FileOutputStream userlog = null;
			
			if (listRequest != null && listRequest.size()>0){			
				int counterFile=0;
				for ( Massive mObj : listRequest ){
					fileProcess.write((mObj.getId() + "|" + mObj.getFilename() + "\n").getBytes("UTF-8"));
					File fileDirectory = new File(PathFacturacionSalida + mObj.getFilename() + "/");
					if(!fileDirectory.exists()){
		            	fileDirectory.mkdir();
		            }else{
		            	for(File file:fileDirectory.listFiles()){
		               	 file.delete();
		                }            	
		            }
					userlog = new FileOutputStream(PathFacturacionSalida + mObj.getFilename() + "/LOG" + mObj.getFilename() + ".TXT");
					userlog.write(("Seguimiento de la solicitud " + mObj.getFilename() + "\r\n").getBytes("UTF-8"));
					/*if(counterFile == totalProcesos-1){
						listFilesIdReport.get(counterFile).write((mObj.getId() + "|" + mObj.getFilename() + "\n").getBytes());
						counterFile=0;
						
					}else{
						listFilesIdReport.get(counterFile).write((mObj.getId() + "|" + mObj.getFilename() + "\n").getBytes());
						counterFile++;
					}*/	
					//Actualizando la solicitud de reportexxxxxx	
					updateRequest( mObj.getId() );					
									
				}												
				
				fileStatus.write("El archivo IDFILEPROCESS.TXT se ha generado exitosamente\n".getBytes("UTF-8"));				
			}else{
				fileStatus.write(("No se encontraron solicitudes a procesar\n").getBytes("UTF-8"));
			}	
			fileProcess.close();
			if(userlog!=null) userlog.close();
			/*for(int iFile=0; iFile<totalProcesos; iFile++){
				listFilesIdReport.get(iFile).close();
			}*/
			fileStatus.close();
			
		}
		catch(Exception e){
			e.printStackTrace();
			System.out.println("Exception massiveRead:" + e.getMessage());
			try {
				FileOutputStream fileError = new FileOutputStream(PathFacturacionProceso + "massiveReadError.txt");
				fileError.write((e.getMessage()).getBytes("UTF-8"));
				fileError.close();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.out.println("Exception al crear massiveReadError.txt:" + e.getMessage());
			}			
		}		
	}
	
	public List<Massive> getRequests(int status, int cfdType) throws Exception{
		
		List<Massive> listPeticiones = new ArrayList<Massive>();
		listPeticiones = massiveManager.findByStatus(status, cfdType);
		return listPeticiones;
	}

	public void updateRequest( long id ) throws Exception{
		
		Massive massive = massiveManager.getById( id );
		
		//setear las fechas
		Date date = new Date();
		
		massive.setStatus(1);
		massive.setDownloadfiledate( date );
		massive.setStartprocessdate( date );
		massive.setIssueDate( date );
		massive.setModifiedBy( "masivo" );
		
		try{
		massiveManager.update(massive);	
		}catch(Exception e){
			System.out.println("update massive: Intento 1");
		      reintentos(massive);
		}
		
	}
	
	public void reintentos(Massive massive){
		  try{
		   System.out.println("update massive: Intento 2");
		   massiveManager.update(massive);
		  }catch(Exception e2){
		   try{
		    System.out.println("update massive: Intento 3");
		    massiveManager.update(massive);
		   }catch(Exception e3){
		    try{
		     System.out.println("update massive: Intento 4");
		     massiveManager.update(massive);
		    }catch(Exception e4){
		     try{
		      System.out.println("update massive: Intento 5");
		      massiveManager.update(massive);
		     }catch(Exception e5){
		      try{
		       System.out.println("update massive: Intento 6");
		       massiveManager.update(massive);
		      }catch(Exception e6){
		       try{
		        System.out.println("update massive: Intento 7");
		        massiveManager.update(massive);
		       }catch(Exception e7){
		        try{
		         System.out.println("update massive: Intento 8");
		         massiveManager.update(massive);
		        }catch(Exception e8){
		         try{
		          System.out.println("update massive: Intento 9");
		          massiveManager.update(massive);
		         }catch(Exception e9){
		          try{
		           System.out.println("update massive: Intento 10");
		           massiveManager.update(massive);
		          }catch(Exception e10){
		        	  try{
			           System.out.println("update massive: Intento 11");
			           massiveManager.update(massive);
			          }catch(Exception e11){
			        	  try{
				           System.out.println("update massive: Intento 12");
				           massiveManager.update(massive);
				          }catch(Exception e12){
				        	  try{
					           System.out.println("update massive: Intento 13");
					           massiveManager.update(massive);
					          }catch(Exception e13){
					        	  try{
						           System.out.println("update massive: Intento 14");
						           massiveManager.update(massive);
						          }catch(Exception e14){
						        	  try{
							           System.out.println("update massive: Intento 15");
							           massiveManager.update(massive);
							          }catch(Exception e15){
							        	  try{
								           System.out.println("update massive: Intento 16");
								           massiveManager.update(massive);
								          }catch(Exception e16){
								        	  try{
									           System.out.println("update massive: Intento 17");
									           massiveManager.update(massive);
									          }catch(Exception e17){
									        	  try{
										           System.out.println("update massive: Intento 18");
										           massiveManager.update(massive);
										          }catch(Exception e18){
										        	  try{
											           System.out.println("update massive: Intento 19");
											           massiveManager.update(massive);
											          }catch(Exception e19){
											        	  try{
												           System.out.println("update massive: Intento 20");
												           massiveManager.update(massive);
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
