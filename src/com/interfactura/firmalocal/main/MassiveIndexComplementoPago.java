package com.interfactura.firmalocal.main;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.controllers.IndicesController;
import com.interfactura.firmalocal.controllers.MassiveIndexComplementoPagoController;
import com.interfactura.firmalocal.controllers.MassiveIndexController;

public class MassiveIndexComplementoPago {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		AbstractApplicationContext context = new ClassPathXmlApplicationContext(
				"app-config.xml", MassiveIndexComplementoPago.class);
		BeanFactory factory = (BeanFactory) context;
		context.registerShutdownHook();
		MassiveIndexComplementoPagoController massiveIndex = (MassiveIndexComplementoPagoController) factory
				.getBean(MassiveIndexComplementoPagoController.class);
		massiveIndex.processing();
		context.close();
	}

}
