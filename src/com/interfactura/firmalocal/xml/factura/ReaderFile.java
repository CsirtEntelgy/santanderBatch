package com.interfactura.firmalocal.xml.factura;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class ReaderFile {

	public static void main(String... args) {
		StringBuilder linea = new StringBuilder();
		boolean flagEnd = false;
		boolean procesa = false;
		boolean activo=false;
		boolean activo2=false;
		int i=0;
		char c;
		byte[] array=null;
		
		try {
			RandomAccessFile file = new RandomAccessFile(
					"D:\\xmlInterfactura\\procesar\\ecb\\Layout_081091S10I_2DA.dat",
					"r");
			
			long byteStart = 0;
			long byteEnd = 4145;
			int sizeArray = 1024*8;
			long byteEndLine = 10;
			
			do {
				file.seek(byteStart);
				array = new byte[sizeArray*8];
				file.read(array, 0, (sizeArray - 1));
				i = 0;
	
				while ((c = (char) array[i]) != 0) {
					i++;
					byteStart++;
					linea.append(c);
					if (c == byteEndLine) {
						if (!linea.toString().startsWith(";")
								&& linea.toString().length() > 0) {
								if (linea.toString().startsWith("01")) {
									procesa = true;

									if(activo){
										flagEnd=true;
										break;
									}
									
									if(activo2){
										activo=true;
										activo2=false;
									}
								}

							if (procesa) {
								System.out.print(linea.toString());
							}
						}
						linea = new StringBuilder();
					}
					if (byteStart == byteEnd) {
						flagEnd = true;
						break;
					}
				}

				if(flagEnd&&activo==false){
					if(linea.toString().startsWith("01")){
						activo2=true;
					}else {
						activo=true;
					}
					flagEnd=false;
				} else if(activo){
					if(linea.toString().startsWith("01")){
						flagEnd=true;
					}
				}
				
				if(array[0]==0){
					flagEnd=true;
				}
			} while (!flagEnd);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			linea=null;
		}
	}
}
