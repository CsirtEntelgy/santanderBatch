package com.interfactura.firmalocal.main;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.controllers.MassiveGenerateIdsArrendadoraController;

public class MassiveGenerateIdsArrendadora {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try{
			AbstractApplicationContext context = new ClassPathXmlApplicationContext("app-config.xml", MassiveGenerateIdsArrendadora.class);
			BeanFactory factory = (BeanFactory) context;
			context.registerShutdownHook();
			
			MassiveGenerateIdsArrendadoraController massiveGenerateIds = (MassiveGenerateIdsArrendadoraController) factory.getBean(MassiveGenerateIdsArrendadoraController.class);
						
			massiveGenerateIds.readIdFileProcess(args[0], args[1]);
					
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
