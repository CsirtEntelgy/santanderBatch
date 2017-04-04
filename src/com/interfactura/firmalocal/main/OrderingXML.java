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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class OrderingXML {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//SEt default encoding
		System.setProperty("file.encoding", "UTF-8");
		 
		System.out.println("encodig: " + System.getProperty("file.encoding"));
		
		//String path = "C:\\Users\\galeman\\Documents\\01Ago2014\\";
		String path = "/planCFD/";
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date dateInicio = new Date();
		System.out.println("TIMEINICIO:" + dateFormat.format(dateInicio) + "M" + System.currentTimeMillis());
		
		//processIND(args[0], args[1]);
		HashMap<String, StringBuilder> hashObjetosXML = new HashMap<String, StringBuilder>();
		
		hashObjetosXML = buildHashMap(path + "SellosOrigen.txt");
		
		processXML(path + "FoliosTotales.txt", path + "SellosOrdenados.txt", hashObjetosXML);
		
		Date dateFin = new Date();
		System.out.println("TIMEFIN:" + dateFormat.format(dateFin) + "M" + System.currentTimeMillis());
		
		System.out.println("Fin del procesamiento de las tareas");
		
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
						System.out.println("ArchivoSalidakey:" + line2.trim() + "-contador:" + counter);
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
	
	public static HashMap<String, StringBuilder> buildHashMap(String pathXML){
		try{
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
				
				/*if(counter>=1000000){
					exit=true;
				}else{*/
					if(line.trim().length() > 4){					
						
						if(firstTime){
							if(line.substring(0, 4).equals("CFD|")){
								firstTime = false;
											
								String [] strValues = line.split("\\|");
								
								//Para todas las mallas excepto pampa se usa el numero de cuenta
								//strLastAccount = strValues[3];
								
								//Para pampa se usa el numero de tarjeta
								strLastAccount = strValues[5];
								
								sbLines.append(line + "\r\n");
							}														
						}else{
							if(line.substring(0, 4).equals("CFD|")){
								
								hashObjetosXML.put(strLastAccount, sbLines);
								System.out.println("HashMapkey:" + strLastAccount + "-contador:" + counter);
								counter++;
								/*System.out.println("key:" + strLastAccount);
								System.out.println("value:" + sbLines);
								System.out.println("*****************");
								*/
								
								String [] strValues = line.split("\\|");
								
								//Para todas las mallas excepto pampa se usa el numero de cuenta
								//strLastAccount = strValues[3];
								
								//Para pampa se usa el numero de tarjeta
								strLastAccount = strValues[5];								
								
								//sbLines.delete(0, sbLines.toString().length());
								//sbLines = null;
								sbLines = new StringBuilder();
								//sbLines.setLength(0);
								
								sbLines.append(line + "\r\n");
							}else{
								//65503867106
								sbLines.append(line + "\r\n");
								 
								if(strLastAccount.equals("5471460089811546") && line.trim().substring(0, 5).equals("COD_B")){
									hashObjetosXML.put(strLastAccount, sbLines);
									System.out.println("HashMapkey:" + strLastAccount + "-contador:" + counter);
								}
							}
							
						}										
						
					}
				//}
				
				
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

