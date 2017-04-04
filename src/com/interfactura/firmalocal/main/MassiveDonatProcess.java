package com.interfactura.firmalocal.main;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.xml.masivo.GeneraDonatXML_Masivo;

public class MassiveDonatProcess {

	/**
	 * @param args
	 */
	private static Logger logger = Logger.getLogger(MassiveDonatProcess.class);
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try{
			//SEt default encoding
			System.setProperty("file.encoding", "UTF-8");
			 
			System.out.println("encodig: " + System.getProperty("file.encoding"));
			
			AbstractApplicationContext context = new ClassPathXmlApplicationContext(
					"app-config.xml", MassiveDonatProcess.class);
			BeanFactory factory = (BeanFactory) context;
			context.registerShutdownHook();
			//logger.info("Paso 1.- Invocando a Procesar facturas para el proceso: " + args[6]);
			GeneraDonatXML_Masivo invoice = (GeneraDonatXML_Masivo) factory.getBean(GeneraDonatXML_Masivo.class);
			//if (args.length <= 7)
			//{	invoice.processingInvoices(args[6], "", "");	}
			//else
			//{	invoice.processingInvoices(args[6], args[8], args[7]);	}
			
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date dateInicio = new Date();
			System.out.println("TIMEINICIO:" + dateFormat.format(dateInicio) + "M" + System.currentTimeMillis());
			
			invoice.processControlFile(args[0]);
			
			context.close();
			System.out.println("Fin del procesamiento Masivo");
			
			
		} 
		catch (Throwable e) 
		{
			e.printStackTrace();
		}
	}



}
