package com.interfactura.firmalocal.main;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.controllers.MassiveDepuraController;

public class MassiveDepura {
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try 
		{
			
			AbstractApplicationContext context = new ClassPathXmlApplicationContext("app-config.xml", MassiveDepura.class);
			BeanFactory factory = (BeanFactory) context;
			context.registerShutdownHook();
			
			
			MassiveDepuraController massiveDepura = (MassiveDepuraController) factory.getBean(MassiveDepuraController.class);
			String result = "";			
			massiveDepura.depura();
					
			context.close();
			System.out.println(result);
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
