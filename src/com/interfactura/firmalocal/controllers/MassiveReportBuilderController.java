package com.interfactura.firmalocal.controllers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.interfactura.firmalocal.xml.Properties;

@Controller
public class MassiveReportBuilderController {
		
	@Autowired
	Properties properties;
	
	String reportesEntrada=MassiveReportReadController.reportesEntrada;
	String reportesProceso=MassiveReportReadController.reportesProceso;
	String reportesSalida=MassiveReportReadController.reportesSalida;
	
	private static final int INICIO_CONCEPTO = 58;
			
	public MassiveReportBuilderController(){
		
	}
	
	public void readIdReportProcess(String nProceso){
		try{
			
			FileInputStream fsIdReportProcess = new FileInputStream(reportesEntrada + "IDREPORTPROCESS_" + nProceso + ".TXT");
			DataInputStream in = new DataInputStream(fsIdReportProcess);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			FileOutputStream fileStatus = new FileOutputStream(reportesProceso + "reportBuilder_" + nProceso + ".txt");
			fileStatus.write("Status del proceso bash reportBuilder.sh\n".getBytes("UTF-8"));
			
			String strID;
			int counter = 0;
			while((strID = br.readLine()) != null){
				File fileDirectory = new File(reportesProceso + strID + "/");
				if(fileDirectory.exists()){
					this.doTxtReport(strID);
					fileStatus.write(("El archivo "+ strID + "REPORT.TXT se ha generado correctamente\n").getBytes("UTF-8"));
				}else{
					fileStatus.write(("No se encontro el directorio " + reportesProceso + strID + "/\n").getBytes("UTF-8"));
				}
				/*if(buildReportTxt(strID)){
					fileStatus.write(("Informacion generada para la solicitud con ID " + strID + "\n").getBytes("UTF-8"));
				}else{
					fileStatus.write(("No se encontraron registros en base de datos, para la solicitud con ID " + strID + "\n").getBytes("UTF-8"));
				}*/
				
				counter++;
			}
			br.close();
			in.close();
			fsIdReportProcess.close();
			if(counter == 0){
				fileStatus.write(("No se encontraron solicitudes a procesar\n").getBytes("UTF-8"));	
			}
			fileStatus.close();
			
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Exception reportBuilder:" + e.getMessage());
			try {
				FileOutputStream fileError = new FileOutputStream(reportesProceso + "reportBuilderError_" + nProceso + ".txt");
				/*FileWriter fwError = new FileWriter(properties.getPathReportesProceso() + "ERROR_SEARCHONDEMAND.TXT");
				BufferedWriter bwError = new BufferedWriter(fwError);
				bwError.write(e.getMessage());
				bwError.close();*/
				fileError.write((e.getMessage()).getBytes("UTF-8"));
				fileError.close();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.out.println("Exception al crear reportBuilderError_" + nProceso + ".txt:" + e.getMessage());
			}
			
		}
	}

	public String translateCodes(String cadena) throws Exception{
		org.apache.commons.lang3.text.translate.UnicodeEscaper escaper = org.apache.commons.lang3.text.translate.UnicodeEscaper.above(127);
		
		String strEscaped = "";
		if(cadena != null && !cadena.equals("")){
			strEscaped = escaper.translate(cadena);
		}
		
		return strEscaped;
		
		
	}

	public void doTxtReport( String ID ) throws Exception{
		
//		File fileDirectory = new File(properties.getPathReportesSalida() + ID);
//		if(!fileDirectory.exists()){
//			fileDirectory.mkdir();
//		}
		
		RandomAccessFile aFile;
		
		File fileOut = new File( reportesProceso + ID + "/" + ID + "REPORT.TXT" );
		//File fileOut = new File( PATH_OUT );
		//aFile = new RandomAccessFile(properties.getPathReportesProceso() + ID + "/" + ID + "FACTURAS.TXT", "r");
		aFile = new RandomAccessFile( reportesProceso + ID + "/" + ID + "CFDS.TXT","r");
		FileChannel inChannel = aFile.getChannel();
	    ByteBuffer buffer = ByteBuffer.allocate(1024);
	    String strFields = "";
	    String idFactura = "-1";
	    
	    FileWriter fw = new FileWriter(fileOut.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw); 
	    
		while(inChannel.read(buffer) > 0)
	    {
	    	
	        buffer.flip();
	        for (int i = 0; i < buffer.limit(); i++)
	        {	
	        	
	        	byte valor = buffer.get(); 
	        	//System.out.print((char) valor);
	        	if(valor != '\n'){
	        		strFields+=(char)valor;
	        	}else{        			
        			if(!strFields.trim().equals("")){        				
        				System.out.println("strFields:" + strFields);
        				
		        		//strFields = strFields.substring(0,strFields.length()-1);
	    	            String arrayValues[] = strFields.split(">");
	    	            System.out.println("ultimoID:" + idFactura + "\n");
	    	            System.out.println("ID:" + arrayValues[0] + "\n");
	    	            if ( !idFactura.equals(arrayValues[0]) ){
	    	            	if ( !idFactura.equals("-1")){
	    	            		//bw.write( System.getProperty("line.separator") );
	    	            		bw.write("\n");
	    	            	}	    	            	
	    	            	bw.write(strFields);
	    	            }
	    	            else{
	    	            	
	    	            	for( int j = INICIO_CONCEPTO; j < arrayValues.length; j++ ){
	    	            		System.out.println("campoConcepto:" + j + " valor:" + arrayValues[j] + "\n");
	    	            		bw.write(">");
	    	            		bw.write( arrayValues[j] );
	    	            	}	    	            	
	    	            	
	    	            }
	    	            idFactura = arrayValues[0];
        			}
	        		strFields="";		
	        	}
	        }
	        buffer.clear();
	    }
		
		bw.write("\n");
		
	    inChannel.close();
	    aFile.close();
	    bw.close();
		
			
	}

}
