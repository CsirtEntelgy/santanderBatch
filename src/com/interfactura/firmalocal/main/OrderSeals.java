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

public class OrderSeals {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		/*
		 * args[0] lastAccount
		 * args[1] interface ej. CFDLMPAMPAS 
		 * args[2] CuentasTotales
		 * args[3] XMLOrigen
		 * args[4] XML.. (sera la salida)
		 * args[5] validaOrdenError.txt
		 * */
		 
		try{
			//SEt default encoding
			System.setProperty("file.encoding", "UTF-8");
			 
			System.out.println("encodig: " + System.getProperty("file.encoding"));
								
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date dateInicio = new Date();
			System.out.println("TIMEINICIO:" + dateFormat.format(dateInicio) + "M" + System.currentTimeMillis());
			
			HashMap<String, StringBuilder> hashObjetosXML = new HashMap<String, StringBuilder>();
			
			hashObjetosXML = buildHashMap(args[3].trim(), args[0].trim(), args[1].trim());
			
			processXML(args[2].trim(), args[4].trim(), hashObjetosXML);
						
			Date dateFin = new Date();
			System.out.println("TIMEFIN:" + dateFormat.format(dateFin) + "M" + System.currentTimeMillis());
			
			System.out.println("Fin del procesamiento de las tareas");		
			
			
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Exception orderXmlSeals:" + e.getMessage());
			try {				
				FileOutputStream fileError = new FileOutputStream(args[5].trim());
				fileError.write((e.getMessage()).getBytes("UTF-8"));
				fileError.close();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();				
				System.out.println("Exception al crear validaOrdenError.txt:" + e.getMessage());
			}
		}
	}

	public static void processXML(String pathAccountsTotals, String pathOut, HashMap<String, StringBuilder> hashObjetosXML) throws Exception{
		//try{
			FileInputStream fStream2 = null;		
			DataInputStream dInput2 = null;
			BufferedReader bReader2 = null;
			
			fStream2 = new FileInputStream(pathAccountsTotals);			
			dInput2 = new DataInputStream(fStream2);
			bReader2 = new BufferedReader(new InputStreamReader(dInput2));
			
			FileOutputStream fileSalida = new FileOutputStream(new File(pathOut));
			
			String line2;
			int counter = 0;
			while((line2 = bReader2.readLine()) != null){
				//System.out.println("account:" + line2.trim());
				if(line2.trim().length() > 0){
					if(hashObjetosXML.containsKey(line2.trim())){						
						//System.out.println("ArchivoSalidakey:" + line2.trim() + "-contador:" + counter);
						/*System.out.println("value:" + hashObjetosXML.get(line2.trim()).toString());
						System.out.println("*****************");*/
						fileSalida.write((hashObjetosXML.get(line2.trim()).toString()).getBytes("UTF-8"));		
						counter++;
					}				
				}
				
			}
			
			fileSalida.close();
			bReader2.close();
			dInput2.close();
			fStream2.close();
			
			
	
		/*}catch(Exception e){
			System.out.println("ERROR: " + e.getMessage());
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
	}
	
	public static HashMap<String, StringBuilder> buildHashMap(String pathXML, String lastAccountCFDMerge, String type) throws Exception{
		//try{
			String [] strValuesMerge =lastAccountCFDMerge.trim().split("\\|");
			String lastAccountMerge = "";
			int index = 0;
			if(type.toUpperCase().equals("CFDLMPAMPAS") || type.toUpperCase().equals("CFDLMPAMPAA")){
				lastAccountMerge = strValuesMerge[5];
				index = 5;
			}else{
				lastAccountMerge = strValuesMerge[3];
				index = 3;
			}			
							
			FileInputStream fStream = null;		
			DataInputStream dInput = null;
			BufferedReader bReader = null;
			
			fStream = new FileInputStream(pathXML);			
			dInput = new DataInputStream(fStream);
			bReader = new BufferedReader(new InputStreamReader(dInput));
						
			String line = "";
			
			boolean firstTime = true;
			//List<String> lines = new ArrayList<String>();
			
			HashMap<String, StringBuilder> hashObjetosXML = new HashMap<String, StringBuilder>();
			
			StringBuilder sbLines = new StringBuilder();
			String strLastAccount = "";
			int counter = 0;
			boolean exit = false;
			while(((line = bReader.readLine()) != null) && !exit){
				
				
				if(line.trim().length() > 4){					
					
					if(firstTime){
						if(line.substring(0, 4).equals("CFD|")){
							firstTime = false;
										
							String [] strValues = line.split("\\|");
							strLastAccount = strValues[index];
							
							sbLines.append(line + "\r\n");
						}														
					}else{
						if(line.substring(0, 4).equals("CFD|")){
							
							hashObjetosXML.put(strLastAccount, sbLines);
							//System.out.println("HashMapkey:" + strLastAccount + "-contador:" + counter);
							counter++;
							/*System.out.println("key:" + strLastAccount);
							System.out.println("value:" + sbLines);
							System.out.println("*****************");
							*/
							
							String [] strValues = line.split("\\|");
							
							
							strLastAccount = strValues[index];
							
							
							//sbLines.delete(0, sbLines.toString().length());
							//sbLines = null;
							sbLines = new StringBuilder();
							//sbLines.setLength(0);
							
							sbLines.append(line + "\r\n");
						}else{
							//65503867106
							sbLines.append(line + "\r\n");
							
							if(strLastAccount.equals(lastAccountMerge) && line.trim().substring(0, 5).equals("COD_B")){
								hashObjetosXML.put(strLastAccount, sbLines);
								//System.out.println("HashMapkey:" + strLastAccount + "-contador:" + counter);
							}
						}
						
					}										
					
				}
				
				
				
			}
						
			bReader.close();
			dInput.close();
			fStream.close();
			
			System.out.println("hashObjetosXMLSize:" + hashObjetosXML.size());
											
			return hashObjetosXML;
		/*}catch(Exception e){
			System.out.println("ERROR: " + e.getMessage());
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;*/
	}
}
