package com.interfactura.firmalocal.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

import org.springframework.beans.factory.annotation.Autowired;

import com.interfactura.firmalocal.xml.Properties;
//import com.interfactura.firmalocal.xml.ecb.GeneraLineaControl;
//import com.interfactura.firmalocal.xml.ecb.GeneraLineaControl;
import com.interfactura.firmalocal.xml.ecb.GeneraLineaControl;

public class StartGeneraLinea {

	@Autowired
	private static Properties properties;
	
	private static GeneraLineaControl generaLineaControl;
	
	public static void main(String[] args) {
		
		generaLineaControl = new GeneraLineaControl();
		
		String path = args[0];
		String idProceso = "";
		String dirInterfaces = args[5];
//		String malla = args[2];
//		String producto = args[3];
//		String fecha = args[4];
		
//		String path = pa;
		String zeros="";
		FileReader fr = null;
        BufferedReader br = null;
		
		
		
//		LineNumberReader reader=null;
		String cad[]=null;
//		File fileout=null;
		
		File file;
		long byteStart;
		long byteEnd;
		long cont;
		String ruta;
		
		String nomArchArr[] = null;
		String prodFechaArr[] = null;
		String nomArch = null;
        String prodFecha = null;
		
		int procesos = Integer.parseInt(args[1]);
		System.out.println("procesos " +procesos);
		int malla = Integer.parseInt(args[2]);
		System.out.println("malla " +malla);
		String producto = args[3];
		System.out.println("producto " +producto);
		String fecha = args[4];
		System.out.println("fecha " +fecha);
		
		
		
		int totalProcesos = procesos * malla;
		
		try {
			System.out.println("entro al try");
			
			for (int i = totalProcesos - procesos; i < totalProcesos; i++) {
				idProceso ="" + i;
				path = args[0];
				if(!idProceso.equals("-1"))
				{
					int max=5-idProceso.length();
					zeros="";
					for(int c=0;c<max;c++)
					{	zeros+="0";	}
					path+= "proceso"+zeros+idProceso+".txt";
					System.out.println("path "+path);
				}
				
				file =new File(path);
				
				
				
				System.out.println("dentro del for");
				System.out.println(file.getPath());
				if(file.exists()&&file.length()>0){
					
					fr = new FileReader (file);
			        br = new BufferedReader(fr);
					
//					reader = new LineNumberReader(new FileReader(file));	
											
					String line;
					prodFechaArr = producto.split(",");
					while ((line = br.readLine()) != null) {
						cad = line.split("\\|");
						System.out.println("argss    "+cad);
						if(cad != null &&  cad.length >= 5){	
							byteStart = Long.parseLong(cad[2]);
							byteEnd = Long.parseLong(cad[3]);
							cont = Long.parseLong(cad[4]);
							ruta = cad[1];
							
							nomArchArr = ruta.split("/");
							nomArch = nomArchArr[5].substring(0, nomArchArr[5].length()-4);
							System.out.println("Nombre archivo recortado  ------ " + nomArch);
							
							for (int j = 0; j < prodFechaArr.length; j++) {
								
								prodFecha = prodFechaArr[j] +fecha;
								if (nomArch.equals(prodFecha)) {
									generaLineaControl.generaLinea(byteStart, byteEnd, ruta, cont, i, prodFechaArr[j], fecha, dirInterfaces);
									break;
								}else {
									System.out.println("El archivo no coincide");
								}
							
							}
							
//						    	generaLineaControl.generaLinea(byteStart, byteEnd, ruta, cont, idProceso, producto, fecha);
						}
					}
					
		            					
//		            br.close();
				}
				
				if (br != null) {
					br.close();
				}
				if (fr != null) {
					fr.close();
				}
			}
			
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (br != null) {
					br.close();
				}
				if (fr != null) {
					fr.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
