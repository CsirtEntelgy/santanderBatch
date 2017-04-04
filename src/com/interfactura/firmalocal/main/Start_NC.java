package com.interfactura.firmalocal.main;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.controllers.InvoiceController_NC;

public class Start_NC
{
	private static Logger logger = Logger.getLogger(Start_NC.class);

	public static void main(String[] args) 
	{
		try 
		{
			//logger.info("Paso 1.- Propiedades para conectarse a la BD, para el proceso: " + args[6]);
			//LoadProperties load = new LoadProperties();
			//load.set(args[0], args[1], args[2], args[3], args[4], args[5]);
 
			//logger.info("Paso 1.- Iniciando el Context de Spring para el proceso: " + args[6]);

			//SEt default encoding
			System.setProperty("file.encoding", "UTF-8");
			 
			System.out.println("encodig: " + System.getProperty("file.encoding"));
			
			AbstractApplicationContext context = new ClassPathXmlApplicationContext(
					"app-config.xml", Start_NC.class);
			BeanFactory factory = (BeanFactory) context;
			context.registerShutdownHook();
			//logger.info("Paso 1.- Invocando a Procesar facturas para el proceso: " + args[6]);
			InvoiceController_NC invoice = (InvoiceController_NC) factory.getBean(InvoiceController_NC.class);
			//if (args.length <= 7)
			//{	invoice.processingInvoices(args[6], "", "");	}
			//else
			//{	invoice.processingInvoices(args[6], args[8], args[7]);	}
			
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date dateInicio = new Date();
			System.out.println("TIMEINICIO:" + dateFormat.format(dateInicio) + "M" + System.currentTimeMillis());
			
			invoice.processingInvoices(args[0], args[2], args[1], args[3], args[4]);
		
			context.close();
			System.out.println("Fin del procesamiento de las tareas");
			
			
		} 
		catch (Throwable e) 
		{
			e.printStackTrace();
		}
	}
}








