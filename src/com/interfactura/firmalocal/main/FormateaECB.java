package com.interfactura.firmalocal.main;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.controllers.FormateaECBAjusteIvaController;
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
			FormateaECBAjusteIvaController ecbAjusteIvaUtil = (FormateaECBAjusteIvaController) factory.getBean(FormateaECBAjusteIvaController.class);
			
			String[] filenames = args[0].split(",");
			String date = args[1].trim();
			String timeStamp = args[2].trim();
			
			System.out.println("names: "+args[0]);
			System.out.println("date: "+args[1]);
			System.out.println("timestamp: "+args[2]);
			
			for(int i = 0; i < filenames.length; i ++){
				
				boolean continua = true;
				boolean carter = false;
				boolean pampa = false;
				
				if(filenames[i].trim().equalsIgnoreCase("CFDLMPAMPAS")
						|| filenames[i].trim().equalsIgnoreCase("CFDLMPAMPAA")){//ajuste lineas 6 para pampa
					pampa = true;
					if(!ecbPampaUtil.processECBTxtFile(filenames[i].trim() + date, timeStamp)){
						continua = false;
						System.out.println("Error al procesar pampa: " + filenames[i].trim());
					}
				}else if(filenames[i].trim().equalsIgnoreCase("CFDPTCARTER")
						|| filenames[i].trim().equalsIgnoreCase("CFDPTSOFOMC")) {//ajuste para carter
					carter = true;
					if(!ecbCarterUtil.processECBTxtFile(filenames[i].trim() + date, timeStamp)){
						continua = false;
						System.out.println("Error al procesar carter: " + filenames[i].trim());
					}
				}
				
				if(continua && !carter && !pampa){//ajuste iva para todas las interfaces - no aplica carter ni pampa
					if(!ecbIvaUtil.processECBTxtFile(filenames[i].trim() + date, timeStamp)){
						System.out.println("Error al procesar iva: " + filenames[i].trim().trim());
						continua = false;
					}
				}
				
				if (continua && !carter){ //nuevo ajuste iva para todas las interfaces, ya no aplica para carter y sofom
					if(!ecbAjusteIvaUtil.processECBTxtFile(filenames[i].trim() + date, timeStamp)){
						System.out.println("Error al ajustar iva: " + filenames[i].trim().trim());
					}
				}
			}
			
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
