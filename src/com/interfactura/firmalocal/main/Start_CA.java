package com.interfactura.firmalocal.main;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.controllers.InvoiceController_CA;

public class Start_CA {

	/**
	 * @param args
	 */
	private static Logger logger = Logger.getLogger(Start_CA.class);
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try 
		{
			
			//SEt default encoding
			System.setProperty("file.encoding", "UTF-8");
			 
			System.out.println("encodig: " + System.getProperty("file.encoding"));
			
			AbstractApplicationContext context = new ClassPathXmlApplicationContext(
					"app-config.xml", Start_CA.class);
			BeanFactory factory = (BeanFactory) context;
			context.registerShutdownHook();
			
			InvoiceController_CA invoice = (InvoiceController_CA) factory.getBean(InvoiceController_CA.class);
			
			
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date dateInicio = new Date();
			System.out.println("TIMEINICIO:" + dateFormat.format(dateInicio) + "M" + System.currentTimeMillis());
			
			invoice.processingInvoices(args[0], args[2], args[1], args[3], args[4]);			
			//invoice.processingInvoices(args[0], args[2], args[1], args[3]);
			
			context.close();
			System.out.println("Fin del procesamiento de las tareas");
			
			
		} 
		catch (Throwable e) 
		{
			e.printStackTrace();
		}
	}

}
