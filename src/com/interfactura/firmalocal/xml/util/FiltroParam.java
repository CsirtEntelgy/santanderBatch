package com.interfactura.firmalocal.xml.util;

import java.io.File;
import java.io.FilenameFilter;

public class FiltroParam implements FilenameFilter{
	private String acceptedString[];
	private String deniedString[];
	
	public FiltroParam(String[]deniedString){
		this.deniedString=deniedString;
	}
	
	public FiltroParam(String[]deniedString, String[]acceptedString){
		this.deniedString=deniedString;
		this.acceptedString=acceptedString;
	}
	
	@Override
	public boolean accept(File dir, String name) {
		if(reader(name)){
			if(acceptedString!=null){
				return readerAcceptedString(name);
			} else {
				return true;
			}
		} else {
			return false;
		}
	}
	
	public boolean reader(String name){
		
		for(String objP:deniedString){
			if(name.toUpperCase().startsWith(objP.toUpperCase())){
				return false;
			} else if(name.toUpperCase().contains(objP.toUpperCase())){
				return false;
			}
		}
		
		return true;
	}
	
	public boolean readerAcceptedString(String name){
		
		for(String objP:acceptedString){
			if(name.toUpperCase().startsWith(objP.toUpperCase())){
				return true;
			}
		}
		
		return false;
	}
	
	public static void main(String ...args){
		FiltroParam p=new FiltroParam(new String[]{"XML", "INC", "backUp"});
		p.reader("test");
	}
}
