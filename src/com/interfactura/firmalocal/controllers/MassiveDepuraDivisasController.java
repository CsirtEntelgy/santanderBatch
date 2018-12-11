package com.interfactura.firmalocal.controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.interfactura.firmalocal.domain.entities.Massive;
import com.interfactura.firmalocal.persistence.MassiveManager;
import com.interfactura.firmalocal.xml.Properties;

@Controller
public class MassiveDepuraDivisasController {
	@Autowired
	Properties properties;
	
	@Autowired(required=true)
	MassiveManager massiveManager;
	
	String PathDivisasEntrada	=	"/salidas/masivo/divisas/facturacion/entrada/";
	String PathDivisasProceso	=	"/salidas/masivo/divisas/facturacion/proceso/";
	String PathDivisasSalida	=	"/salidas/masivo/divisas/facturacion/salida/";
	String PathDivisasOndemand	=	"/salidas/masivo/divisas/facturacion/ondemand/";
	
	
	public void depura(String file, String time){
		
		try{
			String fileName = file + time;
			FileOutputStream fileStatus = new FileOutputStream(PathDivisasProceso + "massiveDepuraDivisas.txt");
			
			//Depura procesados
			
			List<Massive> listMassive = massiveManager.findByStatus(2, 4); 
			
			fileStatus.write("Status del proceso bash massiveDepuraDivisas.sh\n".getBytes("UTF-8"));
			
			File fileProcesoTxt = new File(PathDivisasProceso + fileName + "/" + fileName + ".TXT" );
			if(fileProcesoTxt.exists()){
					
					//Verificar existencia de directorios creados dinamicamente en ../proceso/
					String strPathProceso = PathDivisasProceso + fileName + "/";
					File fileDirProceso = new File(strPathProceso); 
					if(fileDirProceso.exists()){
						deleteFiles(fileName, strPathProceso, true);
						fileDirProceso.delete();
					}
					//Verificar existencia de directorios creados dinamicamente en ../salida/
					String strPathSalida = PathDivisasSalida + fileName + "/";
					File fileDirSalida = new File(strPathSalida); 
					if(fileDirSalida.exists()){
						deleteFiles(fileName, strPathSalida, true);
						fileDirSalida.delete();
					}
					//Verificar existencia de directorios creados dinamicamente en ../ondemand/
					String strPathOndemand = PathDivisasOndemand + fileName + "/";
					File fileDirOndemand = new File(strPathOndemand); 
					if(fileDirOndemand.exists()){
						deleteFiles(fileName, strPathOndemand, true);
						fileDirOndemand.delete();
					}
					//Eliminar archivos del directorio ../salida/
					deleteFiles(fileName, PathDivisasSalida, false);
					
					//Eliminar archivos del directorio ../ondemand/
					deleteFiles(fileName, PathDivisasOndemand, false);
					
					//Eliminar archivos del directorio ../entrada/
					deleteFiles(fileName, PathDivisasEntrada, false);														
					
					fileStatus.write(("Rutas depuradas para " + String.valueOf(fileName) + "\n").getBytes("UTF-8"));
			}
			fileStatus.close();			
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Exception massiveDepura:" + e.getMessage());
			try {
				FileOutputStream fileError = new FileOutputStream(PathDivisasProceso + "massiveDepuraDivisasError.txt");
				fileError.write((e.getMessage()).getBytes("UTF-8"));
				fileError.close();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.out.println("Exception al crear massiveDepuraError.txt:" + e.getMessage());
			}
			
		}	
	}
	
	public void deleteFiles(final String strFileName, String strPath, final boolean allFiles) throws Exception{
		
			final File folder = new File(strPath);
			
			final File[] files = folder.listFiles( new FilenameFilter() {
				@Override
				public boolean accept( final File dir,
									   final String name ) {
					
					if(allFiles){
						return name.matches( ".*" );
					}else{
						return name.matches( ".*" + strFileName + ".*" );
					}					
					
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
		   System.out.println("delete massive: Intento 2");
		   massiveManager.delete(id);
		  }catch(Exception e2){
		   try{
		    System.out.println("delete massive: Intento 3");
		    massiveManager.delete(id);
		   }catch(Exception e3){
		    try{
		     System.out.println("delete massive: Intento 4");
		     massiveManager.delete(id);
		    }catch(Exception e4){
		     try{
		      System.out.println("delete massive: Intento 5");
		      massiveManager.delete(id);
		     }catch(Exception e5){
		      try{
		       System.out.println("delete massive: Intento 6");
		       massiveManager.delete(id);
		      }catch(Exception e6){
		       try{
		        System.out.println("delete massive: Intento 7");
		        massiveManager.delete(id);
		       }catch(Exception e7){
		        try{
		         System.out.println("delete massive: Intento 8");
		         massiveManager.delete(id);
		        }catch(Exception e8){
		         try{
		          System.out.println("delete massive: Intento 9");
		          massiveManager.delete(id);
		         }catch(Exception e9){
		          try{
		           System.out.println("delete massive: Intento 10");
		           massiveManager.delete(id);
		          }catch(Exception e10){
		        	  try{
			           System.out.println("delete massive: Intento 11");
			           massiveManager.delete(id);
			          }catch(Exception e11){
			        	  try{
				           System.out.println("delete massive: Intento 12");
				           massiveManager.delete(id);
				          }catch(Exception e12){
				        	  try{
					           System.out.println("delete massive: Intento 13");
					           massiveManager.delete(id);
					          }catch(Exception e13){
					        	  try{
						           System.out.println("delete massive: Intento 14");
						           massiveManager.delete(id);
						          }catch(Exception e14){
						        	  try{
							           System.out.println("delete massive: Intento 15");
							           massiveManager.delete(id);
							          }catch(Exception e15){
							        	  try{
								           System.out.println("delete massive: Intento 16");
								           massiveManager.delete(id);
								          }catch(Exception e16){
								        	  try{
									           System.out.println("delete massive: Intento 17");
									           massiveManager.delete(id);
									          }catch(Exception e17){
									        	  try{
										           System.out.println("delete massive: Intento 18");
										           massiveManager.delete(id);
										          }catch(Exception e18){
										        	  try{
											           System.out.println("delete massive: Intento 19");
											           massiveManager.delete(id);
											          }catch(Exception e19){
											        	  try{
												           System.out.println("delete massive: Intento 20");
												           massiveManager.delete(id);
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
