package com.interfactura.firmalocal.main;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.controllers.MassiveDivisasReadController;

public class MassiveDivisasRead {


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try 
		{
			
			AbstractApplicationContext context = new ClassPathXmlApplicationContext("app-config.xml", MassiveDivisasRead.class);
			BeanFactory factory = (BeanFactory) context;
			context.registerShutdownHook();
			
			MassiveDivisasReadController massiveDivisasRead = (MassiveDivisasReadController) factory.getBean(MassiveDivisasReadController.class);
			massiveDivisasRead.createIdFileProcess();		
			context.close();
			
			System.out.println("Fin del procesamiento Identifica Peticiones Massive Divisas Report");
			System.exit(0);
		} 
		catch (Throwable e) 
		{
			e.printStackTrace();
			System.exit(-1);
		}
	}

}
