package com.interfactura.firmalocal.main;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.controllers.IndicesDivisasController;


public class CreatingFileIndicesDivisas {
	
	
	public static void main(String...args)
	{
		AbstractApplicationContext context = new ClassPathXmlApplicationContext(
				"app-config.xml", Start.class);
		BeanFactory factory = (BeanFactory) context;
		context.registerShutdownHook();
		IndicesDivisasController indice = (IndicesDivisasController) factory
				.getBean(IndicesDivisasController.class);
		indice.processing(args[1], args[0], args[2]);
		context.close();
	}

}
