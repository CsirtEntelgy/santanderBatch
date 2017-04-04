package com.interfactura.firmalocal.main;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.controllers.HistoricoInfoFacturasController;

public class HistoricoInfoFacturas {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try 
		{
			
			AbstractApplicationContext context = new ClassPathXmlApplicationContext("app-config.xml", HistoricoInfoFacturas.class);
			BeanFactory factory = (BeanFactory) context;
			context.registerShutdownHook();
			
			
			HistoricoInfoFacturasController historicoInfoInvoiceController = (HistoricoInfoFacturasController) factory.getBean(HistoricoInfoFacturasController.class);
						
			historicoInfoInvoiceController.readBD(args[0]);
					
			context.close();
			
			System.exit(0);
		} 
		catch (Throwable e) 
		{
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
