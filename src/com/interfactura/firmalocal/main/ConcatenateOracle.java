package com.interfactura.firmalocal.main;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.controllers.ConcatenateOracleControllers;

public class ConcatenateOracle 
{
	private static Logger logger = Logger.getLogger(ConcatenateOracle.class);
	
	public static void main(String...args)
	{	
		AbstractApplicationContext context = new ClassPathXmlApplicationContext("app-config.xml", Start.class);
		BeanFactory factory = (BeanFactory) context;
		context.registerShutdownHook();
		boolean ok = false;

		ConcatenateOracleControllers concat = (ConcatenateOracleControllers) factory
				.getBean(ConcatenateOracleControllers.class);
		try 
		{	ok = concat.readerFile(args[0], args[1], args[2], args[3]);	} 
		catch (FileNotFoundException e) 
		{	logger.error(e);	} 
		catch (NumberFormatException e) 
		{	logger.error(e);	} 
		catch (IOException e) 
		{	logger.error(e);	}

		context.close();
		if (!ok)
		{	throw new RuntimeException("---Concatenate file. Partes Faltantes.");	}
	}
}
