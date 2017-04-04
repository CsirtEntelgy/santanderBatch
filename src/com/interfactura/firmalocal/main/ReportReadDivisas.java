package com.interfactura.firmalocal.main;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.controllers.MassiveReportReadDivisasController;

public class ReportReadDivisas {

public static void main(String[] args) {
		
		try 
		{
			
			AbstractApplicationContext context = new ClassPathXmlApplicationContext("app-config.xml", ReportReadDivisas.class);
			BeanFactory factory = (BeanFactory) context;
			context.registerShutdownHook();
			
			
			MassiveReportReadDivisasController massiveDivisasReport = (MassiveReportReadDivisasController) factory.getBean(MassiveReportReadDivisasController.class);
			
			massiveDivisasReport.createIdReportProcess(args[0]);
					
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
