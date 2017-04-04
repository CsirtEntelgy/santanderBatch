package com.interfactura.firmalocal.main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import org.apache.log4j.Logger;

public class Stop {
	private static Logger logger = Logger.getLogger(Stop.class);
	
	public static void main(String[] args) {
		Properties info  = new Properties(); 
		String path="";
		try { 
			info.load(Stop.class.getResourceAsStream("../../../../configuration.properties")); 
			path=info.getProperty("xml.masivo"); 
		}catch(Exception ex){
			ex.printStackTrace();
		}
	    stopFlag(path);
	}
	
	public static void stopFlag(String path){
        try {
        	File f=new File(path);
        	logger.debug("Flag name file");
        	System.out.println(path);
            PrintWriter fileOut = new PrintWriter(new FileWriter(f));
            fileOut.println("0");
            fileOut.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
	}
}
