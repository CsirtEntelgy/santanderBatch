package com.interfactura.firmalocal.main;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.controllers.MassiveIndexQuitasController;

public class MassiveIndexQuitas {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		AbstractApplicationContext context = new ClassPathXmlApplicationContext(
				"app-config.xml", MassiveIndexQuitas.class);
		BeanFactory factory = (BeanFactory) context;
		context.registerShutdownHook();
		MassiveIndexQuitasController massiveIndex = (MassiveIndexQuitasController) factory
				.getBean(MassiveIndexQuitasController.class);
		massiveIndex.processing();
		context.close();
	}

}
