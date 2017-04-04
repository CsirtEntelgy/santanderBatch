package com.interfactura.firmalocal.main;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.controllers.IndicesController;
import com.interfactura.firmalocal.controllers.MassiveIndexController;

public class MassiveIndex {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		AbstractApplicationContext context = new ClassPathXmlApplicationContext(
				"app-config.xml", MassiveIndex.class);
		BeanFactory factory = (BeanFactory) context;
		context.registerShutdownHook();
		MassiveIndexController massiveIndex = (MassiveIndexController) factory
				.getBean(MassiveIndexController.class);
		massiveIndex.processing();
		context.close();
	}

}
