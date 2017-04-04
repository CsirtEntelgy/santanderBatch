package com.interfactura.firmalocal.main;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.controllers.MassiveReportReadController;

public class ReportRead {

public static void main(String[] args) {
		
		try 
		{
			
			AbstractApplicationContext context = new ClassPathXmlApplicationContext("app-config.xml", ReportRead.class);
			BeanFactory factory = (BeanFactory) context;
			context.registerShutdownHook();
			
			
			MassiveReportReadController massiveReport = (MassiveReportReadController) factory.getBean(MassiveReportReadController.class);
			
			massiveReport.createIdReportProcess(args[0]);
					
			context.close();
			System.out.println("Fin del procesamiento Identifica Peticiones Massive Report");
			System.exit(0);
		} 
		catch (Throwable e) 
		{
			e.printStackTrace();
			System.exit(-1);
		}

	}
	
}
