package com.interfactura.firmalocal.main;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.controllers.ConcatenateFileControllers;

public class ConcatenateFile 
{
	private static Logger logger = Logger.getLogger(ConcatenateFile.class);
	
	public static void main(String...args)
	{	
		AbstractApplicationContext context = new ClassPathXmlApplicationContext(
				"app-config.xml", Start.class);
		BeanFactory factory = (BeanFactory) context;
		context.registerShutdownHook();
		String origin = args[5];
		boolean ok = false;
		if (!("fac".equals(origin)))
		{
			ConcatenateFileControllers concat = (ConcatenateFileControllers) factory
					.getBean(ConcatenateFileControllers.class);
			try 
			{	ok = concat.readerFile(args[0], args[1], args[2], args[3], args[4]);	} 
			catch (FileNotFoundException e) 
			{	logger.error(e);	} 
			catch (NumberFormatException e) 
			{	logger.error(e);	} 
			catch (IOException e) 
			{	logger.error(e);	}
		}
		else
		{	ok = true;	}
		context.close();
		if (!ok)
		{	throw new RuntimeException("---Concatenate file. Error al abrir archivo.");	}
	}
}
