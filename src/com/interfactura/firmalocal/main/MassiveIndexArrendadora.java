package com.interfactura.firmalocal.main;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.controllers.MassiveIndexArrendadoraController;

public class MassiveIndexArrendadora {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		AbstractApplicationContext context = new ClassPathXmlApplicationContext(
				"app-config.xml", MassiveIndexArrendadora.class);
		BeanFactory factory = (BeanFactory) context;
		context.registerShutdownHook();
		MassiveIndexArrendadoraController massiveIndex = (MassiveIndexArrendadoraController) factory
				.getBean(MassiveIndexArrendadoraController.class);
		massiveIndex.processing(args[0], args[1]);
		context.close();
	}

}
