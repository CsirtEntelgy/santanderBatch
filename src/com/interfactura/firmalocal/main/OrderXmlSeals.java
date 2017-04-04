package com.interfactura.firmalocal.main;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class OrderXmlSeals {

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
			
			HashMap<String, StringBuilder> hashObjetosXML = new HashMap<String, StringBuilder>();
			
			hashObjetosXML = buildHashMap(pathInc + "XMLMERGE.TXT", args[1], args[2], args[3]);
			
			processXML(pathInc + "CuentasTotales.txt", pathInc + "XMLFINAL.TXT", hashObjetosXML);
						
			Date dateFin = new Date();
			System.out.println("TIMEFIN:" + dateFormat.format(dateFin) + "M" + System.currentTimeMillis());
			
			System.out.println("Fin del procesamiento de las tareas");		
			
			
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Exception orderXmlSeals:" + e.getMessage());
			try {				
				FileOutputStream fileError = new FileOutputStream(pathInc + "orderXmlSealsError.txt");
				fileError.write((e.getMessage()).getBytes("UTF-8"));
				fileError.close();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();				
				System.out.println("Exception al crear orderXmlSealsError.txt:" + e.getMessage());
			}
		}
		
	}
	
	public static void processXML(String pathAccountsTotals, String pathOut, HashMap<String, StringBuilder> hashObjetosXML){
		try{
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
			
			
	
		}catch(Exception e){
			System.out.println("ERROR: " + e.getMessage());
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static HashMap<String, StringBuilder> buildHashMap(String pathXML, String lastAccountCFDMerge, String lastTypeCFDMerge, String type){
		try{
			String [] strValuesMerge =lastAccountCFDMerge.trim().split("\\|");
			String lastAccountMerge = "";
			int index = 0;
			if(type.trim().equals("0")){
				lastAccountMerge = strValuesMerge[3];
				index = 3;
			}else{
				lastAccountMerge = strValuesMerge[5];
				index = 5;
			}
	
			String [] strValuesTypeMerge =lastTypeCFDMerge.trim().split("\\|");
			String lastTypeMerge = strValuesTypeMerge[1];
			
			lastAccountMerge = lastAccountMerge + "#" + lastTypeMerge; 
			
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
						}else if(line.trim().length() > 14 && line.substring(0, 15).equals("TIPO_DOCUMENTO|")){
							String [] strValues = line.split("\\|");
							
							strLastAccount = strLastAccount + "#" + strValues[1];
							
							sbLines.append(line + "\r\n");
							
							if(strLastAccount.equals(lastAccountMerge)){
								hashObjetosXML.put(strLastAccount, sbLines);
								//System.out.println("HashMapkey:" + strLastAccount + "-contador:" + counter);
							}
						}else{
						
							//65503867106
							sbLines.append(line + "\r\n");
							
							/*if(strLastAccount.equals(lastAccountMerge) && line.trim().substring(0, 5).equals("COD_B")){
								hashObjetosXML.put(strLastAccount, sbLines);
								System.out.println("HashMapkey:" + strLastAccount + "-contador:" + counter);
							}*/
						}
						
					}										
					
				}
				
				
				
			}
						
			bReader.close();
			dInput.close();
			fStream.close();
			
			System.out.println("hashObjetosXMLSize:" + hashObjetosXML.size());
											
			return hashObjetosXML;
		}catch(Exception e){
			System.out.println("ERROR: " + e.getMessage());
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static HashMap<String, StringBuilder> buildHashMapNIO(String pathXML){
		try{
			RandomAccessFile aFile;
			
			aFile = new RandomAccessFile( pathXML,"r");
			FileChannel inChannel = aFile.getChannel();
		    ByteBuffer buffer = ByteBuffer.allocate(1024);
		    String strFields = "";
		    		    
			boolean firstTime = true;
			//List<String> lines = new ArrayList<String>();
			
			HashMap<String, StringBuilder> hashObjetosXML = new HashMap<String, StringBuilder>();
			
			StringBuilder sbLines = new StringBuilder();
			String strLastAccount = "";
			
			int counter =0;
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
	        				if(firstTime){
								if(strFields.substring(0, 4).equals("CFD|")){
									firstTime = false;
												
									String [] strValues = strFields.split("\\|");
									strLastAccount = strValues[3];
									
									sbLines.append(strFields + "\r\n");
								}														
							}else{
								if(strFields.substring(0, 4).equals("CFD|")){
									
									hashObjetosXML.put(strLastAccount, sbLines);
							
									System.out.println("HashMapkey:" + strLastAccount + "-contador:" + counter);
									
									counter++;
									
									
									/*System.out.println("value:" + sbLines);
									System.out.println("*****************");
									*/
									
									String [] strValues = strFields.split("\\|");
									strLastAccount = strValues[3];
																		
									//sbLines.delete(0, sbLines.toString().length());
									//sbLines = null;
									sbLines = new StringBuilder();
									//sbLines.setLength(0);
									
									sbLines.append(strFields + "\r\n");
								}else{
									sbLines.append(strFields + "\r\n");
								}
								
							}
	        			}
		        		strFields="";		
		        	}
		        }
		        buffer.clear();
		    }
			
			
			
		    inChannel.close();
		    aFile.close();
		    
		    
		    return hashObjetosXML;
		}catch(Exception e){
			System.out.println("ERROR: " + e.getMessage());
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
	
}
