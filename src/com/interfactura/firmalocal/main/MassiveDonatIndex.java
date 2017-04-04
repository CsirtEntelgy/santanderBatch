package com.interfactura.firmalocal.main;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.controllers.MassiveDonatIndexController;

public class MassiveDonatIndex {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		AbstractApplicationContext context = new ClassPathXmlApplicationContext(
				"app-config.xml", MassiveDonatIndex.class);
		BeanFactory factory = (BeanFactory) context;
		context.registerShutdownHook();
		MassiveDonatIndexController massiveDonatIndex = (MassiveDonatIndexController) factory
				.getBean(MassiveDonatIndexController.class);
		massiveDonatIndex.processing();
		context.close();
	}

}
