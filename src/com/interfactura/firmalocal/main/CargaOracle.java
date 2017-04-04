package com.interfactura.firmalocal.main;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.controllers.CargaOracleController;

public class CargaOracle 
{
	private static Logger logger = Logger.getLogger(CargaOracle.class);

	public static void main(String[] args) 
	{
		try 
		{
			logger.info("Paso 1.- Propiedades para conectarse a la BD, para el proceso: " + args[6]);
			LoadProperties load = new LoadProperties();
			load.set(args[0], args[1], args[2], args[3], args[4], args[5]);

			logger.info("Paso 1.- Iniciando el Context de Spring para el proceso: " + args[6]);
			
			AbstractApplicationContext context = new ClassPathXmlApplicationContext(
					"app-config.xml", CargaOracle.class);
			BeanFactory factory = (BeanFactory) context;
			context.registerShutdownHook();
			CargaOracleController carga = (CargaOracleController) factory.getBean(CargaOracleController.class);
			carga.processOracle(args[8], args[7]);
			context.close();
			System.out.println("Fin del procesamiento de la carga a Oracle");
			System.exit(0);
		} 
		catch (Throwable e) 
		{
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
