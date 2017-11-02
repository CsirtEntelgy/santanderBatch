package com.interfactura.firmalocal.main;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import com.interfactura.firmalocal.controllers.FormateaECBPampaController;

public class FormateaECB {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try 
		{
			
			AbstractApplicationContext context = new ClassPathXmlApplicationContext("app-config.xml", FormateaECB.class);
			BeanFactory factory = (BeanFactory) context;
			context.registerShutdownHook();
			
			FormateaECBPampaController ecbPampaUtil = (FormateaECBPampaController) factory.getBean(FormateaECBPampaController.class);
			
			String[] filenames = args[0].split(",");
			String date = args[1].trim();
			
			System.out.println("names: "+args[0]);
			System.out.println("date: "+args[1]);
			
			for(int i = 0; i < filenames.length; i ++){
				if(filenames[i].equalsIgnoreCase("CFDLMPAMPAS")
						|| filenames[i].equalsIgnoreCase("CFDLMPAMPAA")){
					if(!ecbPampaUtil.processECBTxtFile(filenames[i].trim()+date)){
						System.out.println("Error al procesar: " + filenames[i].trim());
					}
				}
			}
			
			context.close();
			System.out.println("Fin del procesamiento Process total ECB");
			System.exit(0);
		} 
		catch (Throwable e) 
		{
			e.printStackTrace();
			System.exit(-1);
		}
	}

}
