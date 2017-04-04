package com.interfactura.firmalocal.xml.util;

import java.io.File;
import java.io.FilenameFilter;

public class Filtro implements FilenameFilter{
	private String nameFile;
	
	public Filtro(String nameFile){
		this.nameFile=nameFile;
	}
	
	@Override
	public boolean accept(File dir, String name) {
		return name.startsWith(this.nameFile);
	}
}
