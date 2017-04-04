package com.interfactura.firmalocal.main;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.controllers.MassiveDivisasDecompressController;

public class MassiveDivisasDecompress {


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try 
		{
			
			AbstractApplicationContext context = new ClassPathXmlApplicationContext("app-config.xml", MassiveDivisasDecompress.class);
			BeanFactory factory = (BeanFactory) context;
			context.registerShutdownHook();
			
			MassiveDivisasDecompressController massiveDivisasDecompress = (MassiveDivisasDecompressController) factory.getBean(MassiveDivisasDecompressController.class);
						
			massiveDivisasDecompress.readIdFileProcess();
					
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
