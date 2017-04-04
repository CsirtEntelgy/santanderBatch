package com.interfactura.firmalocal.main;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.controllers.CFDIssuedController;

public class GeneraReporteMensual {
	
	public static void main(String...args) {
		
		try 
		{
			
			AbstractApplicationContext context = new ClassPathXmlApplicationContext(
					"app-config.xml", GeneraReporteMensual.class);
			BeanFactory factory = (BeanFactory) context;
			context.registerShutdownHook();
			CFDIssuedController cfdissued = (CFDIssuedController) factory.getBean(CFDIssuedController.class);			
			cfdissued.reporte(Integer.parseInt(args[1]),Integer.parseInt(args[2]));			
			context.close();
			System.out.println("Fin del procesamiento de Genera Reporte Mensual");
			System.exit(0);
		} 
		catch (Throwable e) 
		{
			e.printStackTrace();
			System.exit(-1);
		}
	}
}