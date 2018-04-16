package com.interfactura.firmalocal.controllers;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.interfactura.firmalocal.domain.entities.Massive;
import com.interfactura.firmalocal.domain.entities.MassiveReport;
import com.interfactura.firmalocal.persistence.MassiveManager;
import com.interfactura.firmalocal.xml.Properties;

@Controller
public class MassiveCountQuitasController {
	@Autowired
	Properties properties;
	
	
	String PathQuitasEntrada = MassiveReadQuitasController.PathQuitasEntrada;
	String PathQuitasProceso = MassiveReadQuitasController.PathQuitasProceso;
	String PathQuitasSalida = MassiveReadQuitasController.PathQuitasSalida;
		
	@Autowired(required = true)
	private MassiveManager massiveManager;
	
	public MassiveCountQuitasController(){
		
	}
	
	public void checkOK(){
		try{					
			
			//FileInputStream fsExcelsToProcess = new FileInputStream(properties.getPathFacturacionEntrada() + "IDFILEPROCESS_" + nProceso + ".TXT");
			FileInputStream fsExcelsToProcess = new FileInputStream(PathQuitasEntrada + "IDFILEPROCESS.TXT");
			DataInputStream in = new DataInputStream(fsExcelsToProcess);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			//FileOutputStream fileStatus = new FileOutputStream(properties.getPathFacturacionProceso() + "STATUS_COUNT_" + nProceso + ".TXT");
			FileOutputStream userlog = null;
			FileOutputStream fileStatus = new FileOutputStream(PathQuitasProceso + "massiveCountQuitas.txt");
			fileStatus.write("Status del proceso bash massiveCountQuitas.sh\n".getBytes("UTF-8"));
			String strID;
			int counter = 0;
			while((strID = br.readLine()) != null){
				
				String [] arrayRenglon = strID.split("\\|");
				if(arrayRenglon.length>1){
					Massive massive = massiveManager.getById(Long.parseLong(arrayRenglon[0].trim()));
											
						fileStatus.write(("Facturacion masiva " + arrayRenglon[1] + " procesada exitosamente\n").getBytes("UTF-8"));
						userlog = new FileOutputStream(PathQuitasSalida + arrayRenglon[1] + "/LOG" + arrayRenglon[1] + ".TXT",true);
						userlog.write(("Solicitud " + arrayRenglon[1] + " procesada exitosamente\r\n").getBytes("UTF-8"));
						
						compressZip(arrayRenglon[1].trim());
						
						massive.setStatus(2);
						massive.setEndprocessdate(new Date());
						massive.setCiffilename("CIFRAS" + massive.getFilename() + ".ZIP");
						
						try{
							massiveManager.update(massive);
						}catch(Exception e){
							System.out.println("update massive: Intento 1");
						      reintentos(massive);
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
			if(userlog!=null) userlog.close();
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Exception massiveCount:" + e.getMessage());
			try {
				//FileOutputStream fileError = new FileOutputStream(properties.getPathFacturacionProceso() + "ERROR_COUNT_" + nProceso + ".TXT");
				FileOutputStream fileError = new FileOutputStream(PathQuitasProceso + "massiveCountError.txt");
				fileError.write((e.getMessage()).getBytes("UTF-8"));
				fileError.close();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				//System.out.println("Exception al crear ERROR_COUNT_" + nProceso + ".TXT:" + e.getMessage());
				System.out.println("Exception al crear massiveCountError.txt:" + e.getMessage());
			}			
		}
	}
	
	public String existFile(String strFileName, String strPathSalida) throws Exception{
		File fileXML = new File(strPathSalida + strFileName + "/XML" + strFileName + ".TXT");
		File fileINC = new File(strPathSalida + strFileName + "/INC" + strFileName + ".TXT");
		String statusExist = "";
		
		if(!fileXML.exists()){
			statusExist = "Archivo XML" + strFileName + ".TXT no econtrado";
		}
		if(!fileINC.exists()){
			if(!fileXML.exists()){
				statusExist = statusExist + "\nArchivo INC" + strFileName + ".TXT no econtrado\n";
			}else{
				statusExist = statusExist + "Archivo INC" + strFileName + ".TXT no econtrado\n";
			}		
		}			
		return statusExist;		
	}
	
	public void compressZip(String fileName) throws Exception{
		String zipFile = PathQuitasSalida + "CIFRAS" + fileName + ".ZIP";
		
		String srcDir = PathQuitasSalida  + fileName + "/";	

		FileOutputStream fOutput = new FileOutputStream(zipFile);

		ZipOutputStream zOutput = new ZipOutputStream(fOutput);
		
		File srcFiles = new File(srcDir);
		
		buildZip(zOutput, srcFiles);

		// close the ZipOutputStream
		zOutput.close();
	}
	
	public void buildZip(ZipOutputStream zOutput, File srcFiles) throws Exception{
		File[] files = srcFiles.listFiles();

		System.out.println("Adding directory: " + srcFiles.getName());

		for (int i = 0; i < files.length; i++) {
			
			// if the file is directory, use recursion
			if (files[i].isDirectory()) {
				buildZip(zOutput, files[i]);
				continue;
			}
			
			System.out.println("tAdding file: " + files[i].getName());

			// create byte buffer
			byte[] buffer = new byte[1024];

			FileInputStream fis = new FileInputStream(files[i]);

			zOutput.putNextEntry(new ZipEntry(files[i].getName()));
			
			int length;

			while ((length = fis.read(buffer)) > 0) {
				zOutput.write(buffer, 0, length);
			}

			zOutput.closeEntry();

			// close the InputStream
			fis.close();		
			
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
