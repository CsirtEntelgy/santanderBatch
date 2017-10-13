package com.interfactura.firmalocal.main;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.controllers.MassiveCountComplementoPagoController;
import com.interfactura.firmalocal.controllers.MassiveCountController;

public class MassiveCountComplementoPago {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try 
		{
			
			AbstractApplicationContext context = new ClassPathXmlApplicationContext("app-config.xml", MassiveCountComplementoPago.class);
			BeanFactory factory = (BeanFactory) context;
			context.registerShutdownHook();
			
			
			MassiveCountComplementoPagoController massiveCountController = (MassiveCountComplementoPagoController) factory.getBean(MassiveCountComplementoPagoController.class);
			String result = "";			
			
			massiveCountController.checkOK();
					
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
