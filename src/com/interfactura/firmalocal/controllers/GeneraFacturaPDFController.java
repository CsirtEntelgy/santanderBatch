package com.interfactura.firmalocal.controllers;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.interfactura.firmalocal.generapdf.impl.PDFCreation32Impl;
import com.interfactura.firmalocal.generapdf.impl.PDFFactoraje32Impl;

@Controller
public class GeneraFacturaPDFController {

	@Autowired
    private PDFFactoraje32Impl pdfFactorajeCFDI;
	@Autowired
	private PDFCreation32Impl pdfCreation;
	
	public void generaPDF(String pathInterEmision, String pathInterEmisionXml){
	
		try{
			System.out.println("Entre al metodo GeneraPDF del archivo GeneraFacturaPDFController");
			/*FileInputStream fsExcelsToProcess = new FileInputStream(pathInterEmision + "listadoFinal.txt");
			DataInputStream in = new DataInputStream(fsExcelsToProcess);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			int counter = 0;*/
			System.out.println("pathListado: " + pathInterEmision);
			
			FileReader f = new FileReader(pathInterEmision + "listadoFinal.txt");
	        BufferedReader b = new BufferedReader(f);
	        String cadena = null;
	        while((cadena = b.readLine())!=null) {
	            System.out.println(cadena);
	            File fileXML = new File(pathInterEmisionXml + cadena);
				if(fileXML.exists()){
					
					String fileNamePDF = pathInterEmisionXml + cadena + ".pdf";
					FileOutputStream fos = new FileOutputStream(fileNamePDF);
					
					ByteArrayOutputStream baos = pdfFactorajeCFDI.create(fileXML);
					
					fos.write(baos.toByteArray());
					
					fos.close();
				}else{
					System.out.println("El archivo " + fileXML.getAbsolutePath() + " no existe!");
				}				
	        }
	        b.close();
	        
	        
			/*while((strLine = br.readLine()) != null){
				if(!strLine.trim().equals("")){
					System.out.println("xml: " + strLine);
					System.out.println("pathxml: " + pathInterEmisionXml + strLine);
					File fileXML = new File(pathInterEmisionXml + strLine);
					if(fileXML.exists()){
						
						String fileNamePDF = pathInterEmisionXml + strLine + ".pdf";
						FileOutputStream fos = new FileOutputStream(fileNamePDF);
						
						ByteArrayOutputStream baos = pdfFactorajeCFDI.create(fileXML);
						
						fos.write(baos.toByteArray());
						
						fos.close();
					}else{
						System.out.println("El archivo " + fileXML.getAbsolutePath() + " no existe!");
					}					
				}			
				counter++;
			}			
			br.close();
			in.close();*/
			
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("ERROR: " + e.getMessage());			
		}		
	}
	
	public void generaPDFsMassivo() throws Exception{
		
		File folder = new File("/tmp/InputGeneraPDFs/");
		File[] listOfFiles = folder.listFiles();

		    for (int i = 0; i < listOfFiles.length; i++) {
		    	System.out.println("Generando PDF " +listOfFiles[i].getName());
		    	String fileNamePDF = "/tmp/InputGeneraPDFs/"+listOfFiles[i].getName() + ".pdf";
				FileOutputStream fos = new FileOutputStream(fileNamePDF);
				
				ByteArrayOutputStream baos =pdfCreation.createMassivePDFs(listOfFiles[i]);
				
				fos.write(baos.toByteArray());
				
				fos.close();
		    	
		      if (listOfFiles[i].isFile()) {
		        System.out.println("File " + listOfFiles[i].getName());
		      } else if (listOfFiles[i].isDirectory()) {
		        System.out.println("Directory " + listOfFiles[i].getName());
		      }
		      
		      
		      
		      
		      
		      
		      
		      
		      
		      
		      
		      
		      
		      
		      
		      
		    }
		    
		    
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
