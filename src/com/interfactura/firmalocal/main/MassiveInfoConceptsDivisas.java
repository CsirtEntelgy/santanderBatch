package com.interfactura.firmalocal.main;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.controllers.MassiveInfoConceptsDivisasController;

public class MassiveInfoConceptsDivisas {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
			try{
				AbstractApplicationContext context = new ClassPathXmlApplicationContext("app-config.xml", MassiveInfoConceptsDivisas.class);
				BeanFactory factory = (BeanFactory) context;
				context.registerShutdownHook();
				
				MassiveInfoConceptsDivisasController massiveInfoConcepts = (MassiveInfoConceptsDivisasController) factory.getBean(MassiveInfoConceptsDivisasController.class);
							
				massiveInfoConcepts.readIdFileProcess(args[0], args[1]);
						
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
