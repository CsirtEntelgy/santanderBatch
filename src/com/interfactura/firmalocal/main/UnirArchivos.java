package com.interfactura.firmalocal.main;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.interfactura.firmalocal.xml.ConcatFile;

public class UnirArchivos {
	private List<String> files;
	
	public static void main(String...args) throws IOException {
		String path="D:\\xmlInterfactura\\procesados\\";
		UnirArchivos u=new UnirArchivos();
		
		ConcatFile concat=new ConcatFile();
		u.directorio(path);
	}
	
	public void directorio(String path){
		File file[]=new File(path).listFiles();
		for(File obj:file){
			if(obj.isDirectory()){
				directorio(obj.getAbsolutePath());
			} else {
				System.out.println(obj.getName());
			}
		}
	}
}
