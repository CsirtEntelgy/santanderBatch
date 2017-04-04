package com.interfactura.firmalocal.main;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.controllers.MassiveDivisasInfoConceptsController;

public class MassiveDivisasInfoConcepts {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try{
			AbstractApplicationContext context = new ClassPathXmlApplicationContext("app-config.xml", MassiveDivisasInfoConcepts.class);
			BeanFactory factory = (BeanFactory) context;
			context.registerShutdownHook();
			
			MassiveDivisasInfoConceptsController massiveInfoConcepts = (MassiveDivisasInfoConceptsController) factory.getBean(MassiveDivisasInfoConceptsController.class);
						
			massiveInfoConcepts.readIdFileProcess();
					
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
