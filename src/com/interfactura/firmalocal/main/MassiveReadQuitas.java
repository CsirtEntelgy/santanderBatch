package com.interfactura.firmalocal.main;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.controllers.MassiveReadQuitasController;

public class MassiveReadQuitas {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try 
		{
			
			AbstractApplicationContext context = new ClassPathXmlApplicationContext("app-config.xml", MassiveReadQuitas.class);
			BeanFactory factory = (BeanFactory) context;
			context.registerShutdownHook();
			
			MassiveReadQuitasController massiveRead = (MassiveReadQuitasController) factory.getBean(MassiveReadQuitasController.class);
			massiveRead.createIdFileProcess();		
			context.close();
			
			System.out.println("Fin del procesamiento Identifica Peticiones Massive Report");
			System.exit(0);
		} 
		catch (Throwable e) 
		{
			System.out.println("Errorsalida"+e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}
	}

}
