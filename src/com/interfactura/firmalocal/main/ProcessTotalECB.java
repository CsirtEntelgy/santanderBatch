package com.interfactura.firmalocal.main;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.controllers.MassiveReadController;
import com.interfactura.firmalocal.controllers.ProcessTotalECBController;

public class ProcessTotalECB {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try 
		{
			
			AbstractApplicationContext context = new ClassPathXmlApplicationContext("app-config.xml", ProcessTotalECB.class);
			BeanFactory factory = (BeanFactory) context;
			context.registerShutdownHook();
			
			ProcessTotalECBController processTotalECB = (ProcessTotalECBController) factory.getBean(ProcessTotalECBController.class);
			processTotalECB.processECBTxtFile(args[0]);
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
