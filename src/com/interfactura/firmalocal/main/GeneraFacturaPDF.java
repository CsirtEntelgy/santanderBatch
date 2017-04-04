package com.interfactura.firmalocal.main;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.controllers.GeneraFacturaPDFController;

public class GeneraFacturaPDF {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		System.out.println("Generando PDFS Masivos");
		if(args[0].equals("Masivas")){
			try{
				System.out.println("Estoy ejecutando el Archivo GeneraFacturaPDF.java");
				AbstractApplicationContext context = new ClassPathXmlApplicationContext("app-config.xml", GeneraFacturaPDF.class);
				BeanFactory factory = (BeanFactory) context;
				context.registerShutdownHook();
				
				GeneraFacturaPDFController generaFacturaPDF = (GeneraFacturaPDFController) factory.getBean(GeneraFacturaPDFController.class);
				generaFacturaPDF.generaPDFsMassivo();
						
				context.close();
				
				System.out.println("Fin del procesamiento Identifica Peticiones Massive Report");
				System.exit(0);
			} 
			catch (Throwable e) 
			{
				e.printStackTrace();
				System.exit(-1);
			}

		}else{
		
			try{
				System.out.println("Ejecutando Normal");
				AbstractApplicationContext context = new ClassPathXmlApplicationContext("app-config.xml", GeneraFacturaPDF.class);
				BeanFactory factory = (BeanFactory) context;
				context.registerShutdownHook();
				
				GeneraFacturaPDFController generaFacturaPDF = (GeneraFacturaPDFController) factory.getBean(GeneraFacturaPDFController.class);
							
				generaFacturaPDF.generaPDF(args[0], args[1]);
						
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

}
