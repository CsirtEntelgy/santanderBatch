package com.interfactura.firmalocal.main;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.xml.masivo.GeneraXMLProcesoArrendadora_Masivo;

public class MassiveProcessArrendadora {

	/**
	 * @param args
	 */
	private static Logger logger = Logger.getLogger(MassiveProcessArrendadora.class);
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try{
			//SEt default encoding
			System.setProperty("file.encoding", "UTF-8");
			 
			System.out.println("encodig: " + System.getProperty("file.encoding"));
			
			AbstractApplicationContext context = new ClassPathXmlApplicationContext(
					"app-config.xml", MassiveProcessArrendadora.class);
			BeanFactory factory = (BeanFactory) context;
			context.registerShutdownHook();
			
			GeneraXMLProcesoArrendadora_Masivo invoice = (GeneraXMLProcesoArrendadora_Masivo) factory.getBean(GeneraXMLProcesoArrendadora_Masivo.class);
			
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date dateInicio = new Date();
			System.out.println("TIMEINICIO:" + dateFormat.format(dateInicio) + "M" + System.currentTimeMillis());
			
			invoice.processControlFile(args[0], args[1], args[2]);
			
			context.close();
			System.out.println("Fin del procesamiento Masivo");
			
			
		} 
		catch (Throwable e) 
		{
			e.printStackTrace();
		}
	}

}
