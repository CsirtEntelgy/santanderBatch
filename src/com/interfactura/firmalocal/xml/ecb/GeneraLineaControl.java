package com.interfactura.firmalocal.xml.ecb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Calendar;

public class GeneraLineaControl {

	private long contadorGlobal = 1;
	
	public void generaLinea(long bStart, long bEnd, String ruta, long cont, 
			int proceso, String producto, String fecha, String path) {
		
		System.out.println("1");
		int i = 0;
		char c = 0;
		boolean flagEnd = false;
		boolean procesando = false;
		long byteEndLine = 10;
		StringBuffer linea = new StringBuffer();
		StringBuffer llave = new StringBuffer();
		int sizeArray = 1024 * 8;
		String[] datos;
		//la ruta viene completa con todo y nombre
		String prod = producto.substring(3);
		
		File salidaFile = new File(path + "CFDCONTROL" + prod + fecha + "_" + cont + "_" + proceso + ".TXT" );
		
		RandomAccessFile interfaz=null;
		FileOutputStream salidaLlaves = null;
		String fechaECB = "";
		
		try {
			System.out.println("2");
			interfaz = new RandomAccessFile(ruta, "r");
			salidaLlaves = new FileOutputStream(salidaFile);
			byte[] array = null;
			System.out.println("3");
			do {
				
				interfaz.seek(bStart);
				array = new byte[sizeArray];
				interfaz.read(array, 0, (sizeArray - 1));
				i = 0;
				
				while (((c = (char) (array[i] & 0xFF) ) != 0)/* && (bStart != bEnd)*/){
//					System.out.println("a "+ byteS + " === " + byteE);
					i ++;
					bStart ++;
					//Pregunta si llego al fin de linea
					if ( c == byteEndLine ) {
						if (linea.toString().length() > 0) {
							//Pregunta si empieza con 01
							System.out.println("entro");
							if (linea.toString().startsWith("01")) {
								datos = linea.toString().split("\\|");
								if(datos.length >=4) {
									fechaECB = datos[3];
								}
								procesando = true;
								datos = null;
									
							}
							if(linea.toString().startsWith("02") && procesando) {
								datos = linea.toString().split("\\|");
								if(datos.length >=5) {
									llave = new StringBuffer();
									llave = armarLlave(datos[4], datos[1], producto, fecha, fechaECB);
									salidaLlaves.write(llave.toString().getBytes("UTF-8"));
									contadorGlobal ++;
									fechaECB = "";
								}
								procesando = false;
							}
						}
						
						if(bStart >= bEnd && !procesando) {
							flagEnd=true;
							break;
						}
						
						linea = new StringBuffer();
					} else if (c != 13) {	
						linea.append(c);
//						System.out.println(linea);
					} 
				}
				if (array[0] == 0){	
					flagEnd = true;
				}
				if(bStart >= bEnd) {
					flagEnd=true;
				}
				if (procesando) {
//					System.out.println("entra");
					flagEnd = false;
				}
				
				System.out.println("salio "+ flagEnd);
			} while (!flagEnd);
			
//			interfaz.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				System.out.println("4");
				if( interfaz != null) {
					interfaz.close();	
				}
				if( salidaLlaves != null) {
					salidaLlaves.close();	
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public StringBuffer armarLlave(String folio, String tipoDoc, String producto, String fecha, String fechaECB) {
		StringBuffer key = new StringBuffer();
		
		Calendar c = Calendar.getInstance();
		String d = Integer.toString(c.get(Calendar.DATE));
		int di=Integer.parseInt(d);
		if (di <10) {
			d = "0"+d;
		}
		String m = Integer.toString(c.get(Calendar.MONTH));
		int me=Integer.parseInt(m);
		if (me <10) {
			me = me+1;
			m = "0"+me;
			
		}
		String y = Integer.toString(c.get(Calendar.YEAR));
		
		key.append(producto + fecha + ",");
		key.append(folio + ",");
		key.append(tipoDoc + ",");
		key.append(y + "-" + m + "-" + d + ",");
		key.append(contadorGlobal);
//		key.append(contadorGlobal + "\n");
		key.append("|" + fechaECB + "\n");
		return key;
	}
	
	
}
