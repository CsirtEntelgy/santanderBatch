package com.interfactura.firmalocal.main;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.controllers.InvoiceController_D;

public class Start_D {

	private static Logger logger = Logger.getLogger(Start_D.class);

	public static void main(String[] args) 
	{
		try 
		{			

			//SEt default encoding
			System.out.println("Inicio del procesamiento de las tareas");
			System.setProperty("file.encoding", "UTF-8");
			 
			//System.out.println("encodig: " + System.getProperty("file.encoding"));
			
			AbstractApplicationContext context = new ClassPathXmlApplicationContext(
					"app-config.xml", Start_D.class);
			
			BeanFactory factory = (BeanFactory) context;
			context.registerShutdownHook();
			
			InvoiceController_D invoice = (InvoiceController_D) factory.getBean(InvoiceController_D.class);

			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date dateInicio = new Date();
			System.out.println("TIMEINICIO:" + dateFormat.format(dateInicio) + "M" + System.currentTimeMillis());
			
			invoice.processingInvoices(args[0], args[2], args[1], args[3], args[4]);
			
			context.close();
			dateInicio = new Date();
			System.out.println("TIMEFIN:" + dateFormat.format(dateInicio) + "M" + System.currentTimeMillis());
			System.out.println("Fin del procesamiento de las tareas");
		} 
		catch (Throwable e) 
		{
			e.printStackTrace();
		}
	}
	
	
}
