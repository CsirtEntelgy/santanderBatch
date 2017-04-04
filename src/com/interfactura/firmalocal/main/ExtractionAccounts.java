package com.interfactura.firmalocal.main;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExtractionAccounts {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String pathInc = args[0];
		
		String nameTotales02 = args[1];
		String nameSelladasCFD = args[2];
		
		String nameTotales = args[3];
		String nameSelladas = args[4];
		
		try{
			//SEt default encoding
			System.setProperty("file.encoding", "UTF-8");
			 
			System.out.println("encodig: " + System.getProperty("file.encoding"));
			
					
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date dateInicio = new Date();
			System.out.println("TIMEINICIO:" + dateFormat.format(dateInicio) + "M" + System.currentTimeMillis());
			
			process02(pathInc + nameTotales02, pathInc + nameTotales);
			
			processXML(pathInc + nameSelladasCFD, pathInc + nameSelladas);
			
			Date dateFin = new Date();
			System.out.println("TIMEFIN:" + dateFormat.format(dateFin) + "M" + System.currentTimeMillis());
			
			System.out.println("Fin del procesamiento de las tareas");
			
			
			
			
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Exception extractionAccounts:" + e.getMessage());
			try {				
				FileOutputStream fileError = new FileOutputStream(pathInc + "extractionAccountsError.txt");
				fileError.write((e.getMessage()).getBytes("UTF-8"));
				fileError.close();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();				
				System.out.println("Exception al crear extractionAccountsError.txt:" + e.getMessage());
			}
		}
	}

	
	public static void process02(String path02, String pathOut) throws Exception{
		FileInputStream fStream = null;		
		DataInputStream dInput = null;
		BufferedReader bReader = null;
		
		fStream = new FileInputStream(path02);			
		dInput = new DataInputStream(fStream);
		bReader = new BufferedReader(new InputStreamReader(dInput));
		
		FileOutputStream fileSalida = new FileOutputStream(new File(pathOut));
		
		String line;
		
		while((line = bReader.readLine()) != null){
			
			if(line.trim().length() > 4){
				System.out.println("line02:" + line);
				String [] arrayValues = line.split("\\|");
				fileSalida.write((arrayValues[4] + "\n").getBytes("UTF-8"));					
			
			}
			
		}
		
		fileSalida.close();
		bReader.close();
		dInput.close();
		fStream.close();
	}
	
	public static void processXML(String pathXML, String pathOut) throws Exception{
		FileInputStream fStream = null;		
		DataInputStream dInput = null;
		BufferedReader bReader = null;
		
		fStream = new FileInputStream(pathXML);			
		dInput = new DataInputStream(fStream);
		bReader = new BufferedReader(new InputStreamReader(dInput));
		
		FileOutputStream fileSalida = new FileOutputStream(new File(pathOut));
		
		String line;
		
		while((line = bReader.readLine()) != null){
			
			if(line.length() > 4){
				System.out.println("lineXML:" + line);
				String [] arrayValues = line.split("\\|");
				fileSalida.write((arrayValues[3] + "\n").getBytes("UTF-8"));					
				
			}
			
		}
		
		fileSalida.close();
		bReader.close();
		dInput.close();
		fStream.close();
	}
}
