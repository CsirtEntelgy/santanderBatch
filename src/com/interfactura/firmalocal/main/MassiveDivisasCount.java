package com.interfactura.firmalocal.main;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.controllers.MassiveDivisasCountController;

public class MassiveDivisasCount {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try 
		{
			
			AbstractApplicationContext context = new ClassPathXmlApplicationContext("app-config.xml", MassiveDivisasCount.class);
			BeanFactory factory = (BeanFactory) context;
			context.registerShutdownHook();
			
			
			MassiveDivisasCountController massiveDivisasCountController = (MassiveDivisasCountController) factory.getBean(MassiveDivisasCountController.class);
			String result = "";			
			
			massiveDivisasCountController.checkOK();
					
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
