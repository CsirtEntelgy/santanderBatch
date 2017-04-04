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

public class ExtractionInfoMissing {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String pathInc = args[0];
		String interfaceOriginal = args[1];
		try{
			//SEt default encoding
			System.setProperty("file.encoding", "UTF-8");
			 
			System.out.println("encodig: " + System.getProperty("file.encoding"));
								
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date dateInicio = new Date();
			System.out.println("TIMEINICIO:" + dateFormat.format(dateInicio) + "M" + System.currentTimeMillis());
			
			validating(pathInc + interfaceOriginal, pathInc + "CuentasFaltantes.txt", pathInc + "ComprobantesFaltantes.txt");			
			
			Date dateFin = new Date();
			System.out.println("TIMEFIN:" + dateFormat.format(dateFin) + "M" + System.currentTimeMillis());
			
			System.out.println("Fin del procesamiento de las tareas");		
			
			
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Exception extractionInfoMissing:" + e.getMessage());
			try {				
				FileOutputStream fileError = new FileOutputStream(pathInc + "extractionInfoMissingError.txt");
				fileError.write((e.getMessage()).getBytes("UTF-8"));
				fileError.close();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();				
				System.out.println("Exception al crear extractionInfoMissingError.txt:" + e.getMessage());
			}
		}
	}

	public static void validating(String interfaceOriginal, String foliosFaltantes, String salida) throws Exception{
		//String path = "E:\\folioSAT\\";
		
		FileInputStream fStream = null;		
		DataInputStream dInput = null;
		BufferedReader bReader = null;
		
		FileInputStream fStream2 = null;		
		DataInputStream dInput2 = null;
		BufferedReader bReader2 = null;
		
		File fileError = null;
		FileOutputStream salidaError = null;
		
		
		fStream = new FileInputStream(interfaceOriginal);			
		dInput = new DataInputStream(fStream);
		bReader = new BufferedReader(new InputStreamReader(dInput));
		
		fileError = new File(salida);
		salidaError = new FileOutputStream(fileError);
					
		int counterLine=0;
		String line = null;
		String line2 = null;
		String line01Tmp = null;
		int fAbierto=0;
		int fR11=0;
		int fExiste=0;
		//System.out.println("substring: " + "02|0101010|".subSequence(0, 3));
		//int nFaltantes = Integer.parseInt(nFoliosFaltantes);
		//List<String> nosellados = new ArrayList<String>();
		
		HashMap<String, String> hashNoSellados= new HashMap<String, String>();
		
		fStream2 = new FileInputStream(foliosFaltantes);			
		dInput2 = new DataInputStream(fStream2);
		bReader2 = new BufferedReader(new InputStreamReader(dInput2));
		
		while((line2 = bReader2.readLine()) != null){
			//nosellados.add(line2);				
			hashNoSellados.put(line2, line2);
		}
		///////////////////////////////////////////////////
		//FileOutputStream fileX = new FileOutputStream(new File(path + "salidaX.txt"));
		//long counter = 0;
		int counterFaltantes = 0;
		boolean exit=false;
		while(((line = bReader.readLine()) != null) && !exit){
			//fileX.write(String.valueOf(counter).getBytes());
			//counter++;
			if(line.length() >= 3){
				if(line.substring(0, 3).equals("01|")){
					line01Tmp = line;
					if(fAbierto == 1){
						fAbierto=0;
						fR11=0;
					}
					
					if(counterFaltantes == hashNoSellados.size()){							
						exit = true;						
					}
				}else{
					if(line.substring(0, 3).equals("02|")){
						fExiste=0;
						
						String [] strValues = line.split("\\|");
						
						if(hashNoSellados.containsKey(strValues[4].trim())){
							fExiste=1;
							counterFaltantes++;
						}
						
						/*for(int index=0; index<nosellados.size(); index++){
							int lenVal1=nosellados.get(index).length();
							int lenTotal=lenVal1+lenVal1+18;
							
							if(line.length() >= lenTotal){
								if(line.substring(0, lenTotal).equals("02|I|" + nosellados.get(index) + "2014-07-31||" + nosellados.get(index) + "|")){
									fExiste=1;
									counterFaltantes++;
								}
							}
						}*/
						
						if(fAbierto == 0){
							if(fExiste == 1){									
								/*sbLogError.append(line01Tmp + "\r\n");
								sbLogError.append(line + "\r\n");*/
								salidaError.write((line01Tmp + "\r\n").getBytes("UTF-8"));
								salidaError.write((line + "\r\n").getBytes("UTF-8"));
								fAbierto=1;
							}
						}
					}else{
						if(fAbierto == 1){
							if(fR11 == 0){
								if("11|".equals(line.substring(0, 3))){
									fR11=1;
									//sbLogError.append(line + "\r\n");
									salidaError.write((line + "\r\n").getBytes("UTF-8"));
								}else{
									//sbLogError.append(line + "\r\n");
									salidaError.write((line + "\r\n").getBytes("UTF-8"));
								}
							}else{
								if("11|".equals(line.substring(0, 3))){
								
									//sbLogError.append(line + "\r\n");
									salidaError.write((line + "\r\n").getBytes("UTF-8"));
								}else{
									fAbierto=0;
								}
							}
						}
					}
				}
				
			}
			counterLine+=1;
		}
		//fileX.close();
		//System.out.println(sbLogError.toString());
		//salidaError.write((sbLogError.toString() + "\r\n").getBytes("UTF-8"));
		System.out.println("numero de lineas: " + counterLine);
		
			
		if(salidaError != null)
			salidaError.close();
		
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
