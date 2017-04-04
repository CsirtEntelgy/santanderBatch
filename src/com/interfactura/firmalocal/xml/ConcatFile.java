package com.interfactura.firmalocal.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Vector;

public class ConcatFile {

	/**
	 * 
	 * @param nameFile
	 * @throws IOException
	 */
	public void concat(String nameFile, String path) throws IOException {
		String nameSplit[] = nameFile.split("\\.");
		File listFile[] = new File(path).listFiles();
		Vector<InputStream> lstF = new Vector<InputStream>();

		for (File obj : listFile) {
			if (obj.getName().startsWith(nameSplit[0])) {
				lstF.add(new FileInputStream(obj));
			}
		}
		int caracter = -1;
		int fin_archivo = -1;
		InputStream input = new SequenceInputStream(lstF.elements());
		FileOutputStream output = new FileOutputStream(path.concat(
				nameSplit[0].concat(".".concat(nameSplit[1]))));
		caracter = input.read();

		while (caracter != fin_archivo) {
			output.write(caracter);
			caracter = input.read();
		}
		output.close();
		input.close();
	}
	
	private void unirArchivos(){
		
	}
}
