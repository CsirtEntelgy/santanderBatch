package com.interfactura.firmalocal.main;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.controllers.FormateaECBCarterController;
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
			FormateaECBCarterController ecbCarterUtil = (FormateaECBCarterController) factory.getBean(FormateaECBCarterController.class);
			
			String[] filenames = args[0].split(",");
			String date = args[1].trim();
			
			System.out.println("names: "+args[0]);
			System.out.println("date: "+args[1]);
			
			for(int i = 0; i < filenames.length; i ++){
				
				boolean continua = true;
				
				if(filenames[i].trim().equalsIgnoreCase("CFDLMPAMPAS")
						|| filenames[i].trim().equalsIgnoreCase("CFDLMPAMPAA")){//ajuste lineas 6 para pampa
					if(!ecbPampaUtil.processECBTxtFile(filenames[i].trim() + date)){
						continua = false;
						System.out.println("Error al procesar pampa: " + filenames[i].trim());
					}
				}else if(filenames[i].trim().equalsIgnoreCase("CFDPTCARTER")
						|| filenames[i].trim().equalsIgnoreCase("CFDPTSOFOMC")) {//ajuste para carter
					continua = false;
					if(!ecbCarterUtil.processECBTxtFile(filenames[i].trim() + date)){
						System.out.println("Error al procesar carter: " + filenames[i].trim());
					}
				}
				
				if(continua){//ajuste iva para todas las interfaces
					if(!ecbIvaUtil.processECBTxtFile(filenames[i].trim().trim() + date)){
						System.out.println("Error al procesar iva: " + filenames[i].trim().trim());
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
