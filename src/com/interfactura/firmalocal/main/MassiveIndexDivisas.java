package com.interfactura.firmalocal.main;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.controllers.MassiveIndexDivisasController;

public class MassiveIndexDivisas {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		AbstractApplicationContext context = new ClassPathXmlApplicationContext(
				"app-config.xml", MassiveIndexDivisas.class);
		BeanFactory factory = (BeanFactory) context;
		context.registerShutdownHook();
		MassiveIndexDivisasController massiveIndex = (MassiveIndexDivisasController) factory
				.getBean(MassiveIndexDivisasController.class);
		massiveIndex.processing(args[0], args[1]);
		context.close();
	}

}
