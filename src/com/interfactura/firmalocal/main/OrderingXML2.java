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

public class OrderingXML2 {

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
		System.out.println("190000");
		//processIND(args[0], args[1]);
		//HashMap<String, StringBuilder> hashObjetosXML = new HashMap<String, StringBuilder>();
		
		//hashObjetosXML = buildHashMap(path + "XMLBGCAPTA20140731MERGE.TXT");
		
		processXML(path + "FoliosTotales.txt", path + "XMLBGCAPTA20140731PRUEBA.TXT", path + "XMLBGCAPTA20140731MERGE.TXT");
		
		Date dateFin = new Date();
		System.out.println("TIMEFIN:" + dateFormat.format(dateFin) + "M" + System.currentTimeMillis());
		
		System.out.println("Fin del procesamiento de las tareas");
		
	}
	
	public static void processXML(String pathAccountsTotals, String pathOut, String pathXML){
		try{
			FileInputStream fStream2 = null;		
			DataInputStream dInput2 = null;
			BufferedReader bReader2 = null;
			
			fStream2 = new FileInputStream(pathAccountsTotals);			
			dInput2 = new DataInputStream(fStream2);
			bReader2 = new BufferedReader(new InputStreamReader(dInput2));
			
			FileOutputStream fileSalida = new FileOutputStream(new File(pathOut));
			
			String line2;
	
			HashMap<String, StringBuilder> hashObjetosXML = new HashMap<String, StringBuilder>();
						
			boolean firstTime = true;
						
			int lastBlock = 1;
			
			boolean finded = false;
			while((line2 = bReader2.readLine()) != null){
				//System.out.println("account:" + line2.trim());
				if(line2.trim().length() > 0){
					hashObjetosXML = new HashMap<String, StringBuilder>();
					if(firstTime){
						hashObjetosXML = buildHashMap(pathXML, 1);
						firstTime = false;
					}else{
						
						
						
						
					}

					while(!finded){
						
						
						if(hashObjetosXML.containsKey(line2.trim())){
							/*System.out.println("key:" + line2.trim());
							System.out.println("value:" + hashObjetosXML.get(line2.trim()).toString());
							System.out.println("*****************");*/
							fileSalida.write((hashObjetosXML.get(line2.trim()).toString() + "\r\n").getBytes("UTF-8"));		
							
						}else{
							hashObjetosXML = new HashMap<String, StringBuilder>();
							hashObjetosXML = buildHashMap(pathXML, 2);
							if(hashObjetosXML.containsKey(line2.trim())){
								fileSalida.write((hashObjetosXML.get(line2.trim()).toString() + "\r\n").getBytes("UTF-8"));
								lastBlock = 2;
							}else{
								hashObjetosXML = new HashMap<String, StringBuilder>();
								hashObjetosXML = buildHashMap(pathXML, 3);
								if(hashObjetosXML.containsKey(line2.trim())){
									fileSalida.write((hashObjetosXML.get(line2.trim()).toString() + "\r\n").getBytes("UTF-8"));
									lastBlock = 3;
								}else{
									hashObjetosXML = new HashMap<String, StringBuilder>();
									hashObjetosXML = buildHashMap(pathXML, 4);
									if(hashObjetosXML.containsKey(line2.trim())){
										fileSalida.write((hashObjetosXML.get(line2.trim()).toString() + "\r\n").getBytes("UTF-8"));
										lastBlock = 4;
									}else{
										hashObjetosXML = new HashMap<String, StringBuilder>();
										hashObjetosXML = buildHashMap(pathXML, 5);
										if(hashObjetosXML.containsKey(line2.trim())){
											fileSalida.write((hashObjetosXML.get(line2.trim()).toString() + "\r\n").getBytes("UTF-8"));
											lastBlock = 5;
										}else{
											hashObjetosXML = new HashMap<String, StringBuilder>();
											hashObjetosXML = buildHashMap(pathXML, 6);
											if(hashObjetosXML.containsKey(line2.trim())){
												fileSalida.write((hashObjetosXML.get(line2.trim()).toString() + "\r\n").getBytes("UTF-8"));
												lastBlock = 6;
											}else{
												hashObjetosXML = new HashMap<String, StringBuilder>();
												hashObjetosXML = buildHashMap(pathXML, 7);
												if(hashObjetosXML.containsKey(line2.trim())){
													fileSalida.write((hashObjetosXML.get(line2.trim()).toString() + "\r\n").getBytes("UTF-8"));
													lastBlock = 7;
												}else{
													hashObjetosXML = new HashMap<String, StringBuilder>();
													hashObjetosXML = buildHashMap(pathXML, 8);
													if(hashObjetosXML.containsKey(line2.trim())){
														fileSalida.write((hashObjetosXML.get(line2.trim()).toString() + "\r\n").getBytes("UTF-8"));
														lastBlock = 8;
													}else{
														hashObjetosXML = new HashMap<String, StringBuilder>();
														hashObjetosXML = buildHashMap(pathXML, 9);
														if(hashObjetosXML.containsKey(line2.trim())){
															fileSalida.write((hashObjetosXML.get(line2.trim()).toString() + "\r\n").getBytes("UTF-8"));
															lastBlock = 9;
														}else{
															hashObjetosXML = new HashMap<String, StringBuilder>();
															hashObjetosXML = buildHashMap(pathXML, 10);
															if(hashObjetosXML.containsKey(line2.trim())){
																fileSalida.write((hashObjetosXML.get(line2.trim()).toString() + "\r\n").getBytes("UTF-8"));
																lastBlock = 10;
															}else{
																hashObjetosXML = new HashMap<String, StringBuilder>();
																hashObjetosXML = buildHashMap(pathXML, 11);
																if(hashObjetosXML.containsKey(line2.trim())){
																	fileSalida.write((hashObjetosXML.get(line2.trim()).toString() + "\r\n").getBytes("UTF-8"));
																	lastBlock = 11;
																}else{
																	hashObjetosXML = new HashMap<String, StringBuilder>();
																	hashObjetosXML = buildHashMap(pathXML, 12);
																	if(hashObjetosXML.containsKey(line2.trim())){
																		fileSalida.write((hashObjetosXML.get(line2.trim()).toString() + "\r\n").getBytes("UTF-8"));
																		lastBlock = 12;
																	}else{
																		hashObjetosXML = new HashMap<String, StringBuilder>();
																		hashObjetosXML = buildHashMap(pathXML, 13);
																		if(hashObjetosXML.containsKey(line2.trim())){
																			fileSalida.write((hashObjetosXML.get(line2.trim()).toString() + "\r\n").getBytes("UTF-8"));
																			lastBlock = 13;
																		}else{
																			hashObjetosXML = new HashMap<String, StringBuilder>();
																			hashObjetosXML = buildHashMap(pathXML, 14);
																			if(hashObjetosXML.containsKey(line2.trim())){
																				fileSalida.write((hashObjetosXML.get(line2.trim()).toString() + "\r\n").getBytes("UTF-8"));
																				lastBlock = 14;
																			}else{
																				hashObjetosXML = new HashMap<String, StringBuilder>();
																				hashObjetosXML = buildHashMap(pathXML, 15);
																				if(hashObjetosXML.containsKey(line2.trim())){
																					fileSalida.write((hashObjetosXML.get(line2.trim()).toString() + "\r\n").getBytes("UTF-8"));
																					lastBlock = 15;
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
									
									/*System.out.println("key:" + strLastAccount);
									System.out.println("value:" + sbLines);
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
	
	public static HashMap<String, StringBuilder> buildHashMap(String pathXML, int nParte){
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

			int start = 0;
			int end = 0;
			if(nParte == 1){
				start = 0;
				end = 190000;
			}else if(nParte == 2){
				start = 191001;  
				end = 381000;
			}else if(nParte == 3){				 
				start = 381001;  
				end = 571000;
			}else if(nParte == 4){
				start = 571001;  
				end = 761000;
			}else if(nParte == 5){
				start = 761001;  
				end = 951000;
			}else if(nParte == 6){				 
				start = 951001;  
				end = 1141000;
			}else if(nParte == 7){
				start = 1141001;    
				end = 1331000;
			}else if(nParte == 8){
				start = 1331001;    
				end = 1521000;
			}else if(nParte == 9){
				start = 1521001;    
				end = 1711000;
			}else if(nParte == 10){
				start = 1711001;    
				end = 1901000;
			}else if(nParte == 11){
				start = 1901001;    
				end = 2091000;
			}else if(nParte == 12){
				start = 2091001;    
				end = 2281000;
			}else if(nParte == 13){
				start = 2281001;    
				end = 2471000;
			}else if(nParte == 14){
				start = 2471001;    
				end = 2661000;
			}else{
				start = 2661001;    
				end = 2722043;
			}
			counter = start;
			while(((line = bReader.readLine()) != null) && !exit){
				
				if(counter>end){
					exit=true;
				}else{
					if(line.trim().length() > 4){					
						
						if(firstTime){
							if(line.substring(0, 4).equals("CFD|")){
								firstTime = false;
											
								String [] strValues = line.split("\\|");
								strLastAccount = strValues[3];
								
								sbLines.append(line + "\r\n");
							}														
						}else{
							if(line.substring(0, 4).equals("CFD|")){
								
								if(counter>=start && counter<=end){
									hashObjetosXML.put(strLastAccount, sbLines);
									counter++;
								}
									
								/*System.out.println("key:" + strLastAccount);
								System.out.println("value:" + sbLines);
								System.out.println("*****************");
								*/
								
								String [] strValues = line.split("\\|");
								strLastAccount = strValues[3];
								
								
								//sbLines.delete(0, sbLines.toString().length());
								//sbLines = null;
								sbLines = new StringBuilder();
								//sbLines.setLength(0);
								
								sbLines.append(line + "\r\n");
								
								
							}else{
								sbLines.append(line + "\r\n");
							}
							
						}										
						
					}
				}
				
				
			}
			
			
			bReader.close();
			dInput.close();
			fStream.close();
			
			System.out.println("hashObjetosXMLSize:" + hashObjetosXML.size() + "-nParte:" + nParte);
			
			
					
			return hashObjetosXML;
		}catch(Exception e){
			System.out.println("ERROR: " + e.getMessage());
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
		
}
