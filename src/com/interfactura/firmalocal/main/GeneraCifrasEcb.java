package com.interfactura.firmalocal.main;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.controllers.GeneraCifrasEcbController;

public class GeneraCifrasEcb {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try 
		{
			
			AbstractApplicationContext context = new ClassPathXmlApplicationContext("app-config.xml", GeneraCifrasEcb.class);
			BeanFactory factory = (BeanFactory) context;
			context.registerShutdownHook();
			
			
			GeneraCifrasEcbController generaCifrasEcbController = (GeneraCifrasEcbController) factory.getBean(GeneraCifrasEcbController.class);
			String result = "";			
			
			generaCifrasEcbController.extractionInfo(args[0], args[1], args[2], args[3], args[4], args[5]);
					
			context.close();
			System.out.println(result);
			System.out.println("Fin del procesamiento Identifica Peticiones Massive");
			System.exit(0);
		} 
		catch (Throwable e) 
		{
			e.printStackTrace();
			System.exit(-1);
		}

	}

}
