package com.interfactura.firmalocal.xml.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.BufferedReader;

public class Timbrado {
	
	public static void main(String args[]){
		File fileExitODM = new File(args[0] + File.separator + args[1] + "ODM-" + args[2]);
		FileOutputStream salida = null;
		
		File fileOdmTXT = new File(args[0] + File.separator + args[1] + "ODM-" + args[2] + ".TXT");
		
		try {
			FileReader fReader = new FileReader(fileOdmTXT);		
			BufferedReader bReader = new BufferedReader(fReader);
			
			fileExitODM.createNewFile();
			salida = new FileOutputStream(fileExitODM, true);
			
			String linea = null;
			while((linea=bReader.readLine())!=null){
				salida.write(linea.getBytes());
			}
			bReader.close();
			fReader.close();
			salida.close();
			
			fileOdmTXT.delete();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
