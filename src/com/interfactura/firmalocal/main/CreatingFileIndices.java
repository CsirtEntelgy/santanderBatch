package com.interfactura.firmalocal.main;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.controllers.IndicesController;

public class CreatingFileIndices {

	public static void main(String...args)
	{
		AbstractApplicationContext context = new ClassPathXmlApplicationContext(
				"app-config.xml", Start.class);
		BeanFactory factory = (BeanFactory) context;
		context.registerShutdownHook();
		IndicesController indice = (IndicesController) factory
				.getBean(IndicesController.class);
		indice.processing(args[1], args[0], args[2]);
		context.close();
	}
}
