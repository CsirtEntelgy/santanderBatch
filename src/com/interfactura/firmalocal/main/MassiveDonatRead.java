package com.interfactura.firmalocal.main;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.controllers.MassiveDonatReadController;

public class MassiveDonatRead {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try 
		{
			
			AbstractApplicationContext context = new ClassPathXmlApplicationContext("app-config.xml", MassiveDonatRead.class);
			BeanFactory factory = (BeanFactory) context;
			context.registerShutdownHook();
			
			MassiveDonatReadController massiveDonatRead = (MassiveDonatReadController) factory.getBean(MassiveDonatReadController.class);
			massiveDonatRead.createIdFileProcess();		
			context.close();
			
			System.out.println("Fin del procesamiento Identifica Peticiones Massive Donat Report");
			System.exit(0);
		} 
		catch (Throwable e) 
		{
			e.printStackTrace();
			System.exit(-1);
		}
	}

}
