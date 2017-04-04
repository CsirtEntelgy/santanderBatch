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
import java.util.HashMap;

public class ExtractionAccountsMissing {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String pathInc = args[0];
		try{
			//SEt default encoding
			System.setProperty("file.encoding", "UTF-8");
			 
			System.out.println("encodig: " + System.getProperty("file.encoding"));
			
					
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date dateInicio = new Date();
			System.out.println("TIMEINICIO:" + dateFormat.format(dateInicio) + "M" + System.currentTimeMillis());
			
			extraction(pathInc + "CuentasSelladas.txt", pathInc + "CuentasTotales.txt", pathInc + "CuentasFaltantes.txt");
			
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

	public static void extraction(String pathAccountsXML, String pathAccountsTotals, String pathOut) throws Exception{
		
		FileInputStream fStream2 = null;		
		DataInputStream dInput2 = null;
		BufferedReader bReader2 = null;
		
		FileInputStream fStream = null;		
		DataInputStream dInput = null;
		BufferedReader bReader = null;
		
		File fileOut = null;
		FileOutputStream salida = null;
								
		//List<String> sellados = new ArrayList<String>();
		HashMap<String, String> hashSellados = new HashMap<String, String>();
		
		fStream2 = new FileInputStream(pathAccountsXML);			
		dInput2 = new DataInputStream(fStream2);
		bReader2 = new BufferedReader(new InputStreamReader(dInput2));
		
		String line2 = "";
		while((line2 = bReader2.readLine()) != null){
			if(line2.trim().length()>0){
				//sellados.add(line2);
				hashSellados.put(line2, line2);
			}								
		}

		fStream = new FileInputStream(pathAccountsTotals);			
		dInput = new DataInputStream(fStream);
		bReader = new BufferedReader(new InputStreamReader(dInput));
		
		fileOut = new File(pathOut);
		salida = new FileOutputStream(fileOut);
		
		
		String line = "";
		
		while(((line = bReader.readLine()) != null)){
			if(line.trim().length()>0){
									
				if(!hashSellados.containsKey(line.trim())){
					salida.write((line + "\r\n").getBytes("UTF-8"));
				}
			}
		}
	
		if(salida != null)
			salida.close();
		
		if(bReader != null)
			bReader.close();
		
		if(dInput != null)
			dInput.close();
		
		if(fStream != null)
			fStream.close();
		
		if(bReader2 != null)
			bReader2.close();
		
		if(dInput2 != null)
			dInput2.close();
		
		if(fStream2 != null)
			fStream2.close();
	}
}
