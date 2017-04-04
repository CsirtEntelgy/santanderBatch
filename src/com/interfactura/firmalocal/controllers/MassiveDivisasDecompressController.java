package com.interfactura.firmalocal.controllers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.interfactura.firmalocal.xml.Properties;

@Controller
public class MassiveDivisasDecompressController {
	@Autowired
	Properties properties;
	
	String facturacionDivisasEntrada=MassiveDivisasReadController.facturacionDivisasEntrada;
	String facturacionDivisasProceso=MassiveDivisasReadController.facturacionDivisasProceso;
	String facturacionDivisasSalida=MassiveDivisasReadController.facturacionDivisasSalida;
	String facturacionDivisasOndemand=MassiveDivisasReadController.facturacionDivisasOndemand;
	
	public void readIdFileProcess(){
		try{
			//FileInputStream fsExcelsToProcess = new FileInputStream(properties.getPathFacturacionDivisasEntrada() + "IDFILEPROCESS_" + nProceso + ".TXT");
			FileInputStream fsExcelsToProcess = new FileInputStream(facturacionDivisasEntrada + "IDFILEPROCESS.TXT");
			DataInputStream in = new DataInputStream(fsExcelsToProcess);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			int counter = 0;
			//FileOutputStream fileStatus = new FileOutputStream(properties.getPathFacturacionDivisasProceso() + "STATUS_DECOMPRESS_" + nProceso + ".TXT");
			FileOutputStream userlog = null;
			FileOutputStream fileStatus = new FileOutputStream(facturacionDivisasProceso + "massiveDivisasDecompress.txt");
			fileStatus.write(("Status del proceso bash massiveDivisasDecompress.sh" + "\n").getBytes("UTF-8"));
			while((strLine = br.readLine()) != null){
				if(!strLine.trim().equals("")){
					String [] arrayValues = strLine.trim().split("\\|");
					File fileZIP = new File(facturacionDivisasEntrada + arrayValues[1] + ".ZIP");
					if(fileZIP.exists()){
						String strDec = decompress(arrayValues[1]);
						if(strDec.equals("")){
							fileStatus.write(("Extraccion exitosa, del archivo " + arrayValues[1] + ".ZIP" + "\n").getBytes("UTF-8"));
							userlog = new FileOutputStream(facturacionDivisasSalida + arrayValues[1] + "/LOG" + arrayValues[1] + ".TXT",true);
							userlog.write(("Extraccion exitosa, del archivo " + arrayValues[1] + ".ZIP" + "\r\n").getBytes("UTF-8"));
						}else{
							fileStatus.write((strDec + "\n").getBytes("UTF-8"));
							userlog = new FileOutputStream(facturacionDivisasSalida + arrayValues[1] + "/LOG" + arrayValues[1] + ".TXT",true);
							userlog.write((strDec + "\r\n").getBytes("UTF-8"));
						}
						
					}else{
						fileStatus.write(("El archivo " + arrayValues[1] + ".ZIP no se encuentra en la ruta " + facturacionDivisasEntrada + "\n").getBytes("UTF-8"));
						userlog = new FileOutputStream(facturacionDivisasSalida + arrayValues[1] + "/LOG" + arrayValues[1] + ".TXT",true);
						userlog.write(("El archivo " + arrayValues[1] + ".ZIP no se encuentra en la ruta " + facturacionDivisasEntrada + "\r\n").getBytes("UTF-8"));
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
			System.out.println("Exception massiveDivisasDecompress:" + e.getMessage());
			try {
				//FileOutputStream fileError = new FileOutputStream(properties.getPathReportesProceso() + "ERROR_DECOMPRESS_" + nProceso + ".TXT");
				FileOutputStream fileError = new FileOutputStream(facturacionDivisasProceso + "massiveDivisasDecompressError.txt");
				fileError.write((e.getMessage()).getBytes("UTF-8"));
				fileError.close();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				//System.out.println("Exception al crear ERROR_DECOMPRESS_" + nProceso + ".TXT:" + e.getMessage());
				System.out.println("Exception al crear massiveDivisasDecompressError.txt:" + e.getMessage());
			}			
		}
	}
	
	public String decompress(String strNameFile) throws Exception{
		int tam = 1;
		final int BUFFER = tam * 1024 * 1024;
      
         BufferedOutputStream dest = null;
         FileInputStream fis = new 
	   //FileInputStream(argv[0]);
         FileInputStream(facturacionDivisasEntrada + strNameFile + ".ZIP");
         ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
         ZipEntry entry;
         //Comprobar existencia del directorio
         File fileDirectory = new File(facturacionDivisasProceso + strNameFile + "/");
         if(!fileDirectory.exists()){
         	fileDirectory.mkdir();
         }else{
         	for(File file:fileDirectory.listFiles()){
            	 file.delete();
             }            	
         }
         try{
	         while((entry = zis.getNextEntry()) != null) {
	            System.out.println("Extracting: " +entry);
	            System.out.println("isDirectory: " + entry.isDirectory());
	            if(!entry.isDirectory()){
	            	System.out.println("name file: " + entry.getName().substring(entry.getName().lastIndexOf("/") + 1));
		            String nameFile = entry.getName().substring(entry.getName().lastIndexOf("/") + 1);
		        
		            int count;
		            byte data[] = new byte[BUFFER];
		            
		            // write the files to the disk
		            //FileOutputStream fos = new FileOutputStream(facturacionDivisasProceso + strNameFile + "/" + entry.getName());
		            FileOutputStream fos = new FileOutputStream(facturacionDivisasProceso + strNameFile + "/" + nameFile);
		            dest = new BufferedOutputStream(fos, BUFFER);
		            while ((count = zis.read(data, 0, BUFFER)) 
		              != -1) {
		               dest.write(data, 0, count);
		            }
		            dest.flush();
		            dest.close();
	            }            
	         }
         }catch(IllegalArgumentException ie){
        	 System.out.println("IllegalArgumentException: " + ie.getMessage());
        	 return "Se encontraron nombres incorrectos en archivo(s), dentro de " + facturacionDivisasEntrada + strNameFile + ".ZIP - caracteres validos: letras (sin acentos, dieresis y ñ), numeros, puntos, espacios en blanco, guiones y guiones bajos; ej. nombreArchivo - 1_Y.xlsx";
         }catch(Exception e){
        	 throw e; 
         } 
         zis.close();     
         return "";
	}

}
