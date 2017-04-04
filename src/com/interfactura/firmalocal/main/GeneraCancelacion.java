package com.interfactura.firmalocal.main;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.controllers.GeneraCancelacionController;

public class GeneraCancelacion {

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
			
			
			GeneraCancelacionController generaCancelacion = (GeneraCancelacionController) factory.getBean(GeneraCancelacionController.class);
			String result = "";			
			System.out.println("0:" + args[0]);
					
			generaCancelacion.leerEntradaCancelacion(args[0], args[1]);
					
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
