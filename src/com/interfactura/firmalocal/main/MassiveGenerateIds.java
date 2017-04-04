package com.interfactura.firmalocal.main;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.controllers.MassiveGenerateIdsController;

public class MassiveGenerateIds {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try{
			AbstractApplicationContext context = new ClassPathXmlApplicationContext("app-config.xml", MassiveGenerateIds.class);
			BeanFactory factory = (BeanFactory) context;
			context.registerShutdownHook();
			
			MassiveGenerateIdsController massiveGenerateIds = (MassiveGenerateIdsController) factory.getBean(MassiveGenerateIdsController.class);
						
			massiveGenerateIds.readIdFileProcess();
					
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
