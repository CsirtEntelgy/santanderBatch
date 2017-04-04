package com.interfactura.firmalocal.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;

import com.interfactura.firmalocal.xml.Properties;
import com.interfactura.firmalocal.xml.util.FiltroParam;
import com.interfactura.firmalocal.xml.util.Util;

@Controller
public class ConcatenateOracleControllers 
{
	private static Logger logger = Logger.getLogger(ConcatenateOracleControllers.class);
	@Autowired
	private Properties properties;
	
	/**
	 * 
	 * @param pathIncidence
	 * @param pathExit
	 * @param pathCongiguration
	 * @param pathCopy
	 * @param deleteFile
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public boolean readerFile(String pathExit, String pathCongiguration, String pathCopy, String deleteFile) 
		throws NumberFormatException, IOException
	{
		File file = new File(pathCongiguration);
		File fileBack = new File(properties.getPathDirBackup());
		FiltroParam filter = null;
		File[] filterFile = null;
		if(!file.isFile())
		{	throw new IOException("El archivo de configuraciones no se encontro");	}
		FileReader fileIn=new FileReader(file);
		LineNumberReader reader=new LineNumberReader(fileIn);
		String line[]=null;
		File fileTemp=null;
		boolean concatExit=false;
		
		FileInputStream in=null;
		FileOutputStream out=null;
		System.out.println("Leyendo archivo de configuraciones");
		logger.info("Leyendo archivo de configuraciones");
		boolean result = false;
		while (reader.ready())
		{
			try
			{
				line=reader.readLine().concat("|temp").split("\\|");
				fileTemp=new File(line[1]);
				System.out.println("Archivo Concatenando: " + fileTemp);
				logger.info("Archivo Concatenando: "+fileTemp);
				concatExit = Util.concatFile(fileTemp.getName(),Integer.parseInt(line[2]), pathExit, "BD");
				if(concatExit)
				{
					in=new FileInputStream(fileTemp);
					line=fileTemp.getName().split("\\.");	
					filter=new FiltroParam(new String[0], new String[]{line[0]+"_"});
					filterFile=fileBack.listFiles(filter);
					for(File objFile:filterFile)
					{	objFile.delete();	}
					result = true;
				}
			} 
			catch (FileNotFoundException fnfe)
			{	logger.error(fnfe);	}
			
		}
		reader.close();
		if(deleteFile.equals("true"))
		{	file.delete();	}
		return result;
	}
}
