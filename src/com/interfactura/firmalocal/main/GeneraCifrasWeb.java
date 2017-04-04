package com.interfactura.firmalocal.main;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.controllers.GeneraCifrasWebController;

public class GeneraCifrasWeb {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try 
		{
			
			AbstractApplicationContext context = new ClassPathXmlApplicationContext("app-config.xml", GeneraCifrasWeb.class);
			BeanFactory factory = (BeanFactory) context;
			context.registerShutdownHook();
			
			
			GeneraCifrasWebController generaCifrasWebController = (GeneraCifrasWebController) factory.getBean(GeneraCifrasWebController.class);
			String result = "";			
			System.out.println("0:" + args[0]);
			System.out.println("1:" + args[1]);
			System.out.println("2:" + args[2]);
			
			
			generaCifrasWebController.extractionOndemand(args[0], Integer.parseInt(args[1].trim()), args[2]);
					
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
