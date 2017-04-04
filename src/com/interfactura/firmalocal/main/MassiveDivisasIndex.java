package com.interfactura.firmalocal.main;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.controllers.MassiveDivisasIndexController;

public class MassiveDivisasIndex {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		AbstractApplicationContext context = new ClassPathXmlApplicationContext(
				"app-config.xml", MassiveDivisasIndex.class);
		BeanFactory factory = (BeanFactory) context;
		context.registerShutdownHook();
		MassiveDivisasIndexController massiveDivisasIndex = (MassiveDivisasIndexController) factory
				.getBean(MassiveDivisasIndexController.class);
		massiveDivisasIndex.processing();
		context.close();
	}

}
