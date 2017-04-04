package com.interfactura.firmalocal.main;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.controllers.HistoricoInfoCfdsController;

public class HistoricoInfoCfds {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try 
		{
			
			AbstractApplicationContext context = new ClassPathXmlApplicationContext("app-config.xml", HistoricoInfoCfds.class);
			BeanFactory factory = (BeanFactory) context;
			context.registerShutdownHook();
			
			
			HistoricoInfoCfdsController historicoInfoCfdsController = (HistoricoInfoCfdsController) factory.getBean(HistoricoInfoCfdsController.class);
						
			historicoInfoCfdsController.createSqlFile(args[0], args[1]);
					
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
