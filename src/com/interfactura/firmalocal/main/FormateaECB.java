package com.interfactura.firmalocal.main;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.controllers.FormateaECBIvaController;
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
			FormateaECBIvaController ecbIvaUtil = (FormateaECBIvaController) factory.getBean(FormateaECBIvaController.class);
			
			String[] filenames = args[0].split(",");
			String date = args[1].trim();
			
			System.out.println("names: "+args[0]);
			System.out.println("date: "+args[1]);
			
			for(int i = 0; i < filenames.length; i ++){
				
				boolean continua = true;
				
				if(filenames[i].equalsIgnoreCase("CFDLMPAMPAS")
						|| filenames[i].equalsIgnoreCase("CFDLMPAMPAA")){//ajuste lineas 6 para pampa
					if(!ecbPampaUtil.processECBTxtFile(filenames[i].trim()+date)){
						continua = false;
						System.out.println("Error al procesar pampa: " + filenames[i].trim());
					}
				}else{
					//reglas faltantes carter...
				}
				
				if(continua){//ajuste iva para todas las interfaces
					if(!ecbIvaUtil.processECBTxtFile(filenames[i].trim()+date)){
						System.out.println("Error al procesar iva: " + filenames[i].trim());
					}
				}
				
			}
			context.close();
			System.out.println("Fin del procesamiento Formatea ECB");
			System.exit(0);
		} 
		catch (Throwable e) 
		{
			e.printStackTrace();
			System.exit(-1);
		}
	}

}
