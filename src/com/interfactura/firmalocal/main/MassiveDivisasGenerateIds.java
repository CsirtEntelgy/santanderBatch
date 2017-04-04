package com.interfactura.firmalocal.main;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.controllers.MassiveDivisasGenerateIdsController;

public class MassiveDivisasGenerateIds {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	
		// TODO Auto-generated method stub
				try{
					AbstractApplicationContext context = new ClassPathXmlApplicationContext("app-config.xml", MassiveDivisasGenerateIds.class);
					BeanFactory factory = (BeanFactory) context;
					context.registerShutdownHook();
					
					MassiveDivisasGenerateIdsController massiveGenerateIds = (MassiveDivisasGenerateIdsController) factory.getBean(MassiveDivisasGenerateIdsController.class);
								
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
