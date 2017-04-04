package com.interfactura.firmalocal.main;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.controllers.MassiveReportBuilderDivisasController;

public class ReportBuilderDivisas {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
			try{
				//SEt default encoding
				System.setProperty("file.encoding", "UTF-8");
				 
				System.out.println("encodig: " + System.getProperty("file.encoding"));
				
				AbstractApplicationContext context = new ClassPathXmlApplicationContext(
						"app-config.xml", ReportBuilderDivisas.class);
				BeanFactory factory = (BeanFactory) context;
				context.registerShutdownHook();
				//logger.info("Paso 1.- Invocando a Procesar facturas para el proceso: " + args[6]);
				MassiveReportBuilderDivisasController builder = (MassiveReportBuilderDivisasController) factory.getBean(MassiveReportBuilderDivisasController.class);
				//if (args.length <= 7)
				//{	invoice.processingInvoices(args[6], "", "");	}
				//else
				//{	invoice.processingInvoices(args[6], args[8], args[7]);	}
				
				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				Date dateInicio = new Date();
				System.out.println("TIMEINICIO:" + dateFormat.format(dateInicio) + "M" + System.currentTimeMillis());
				
				builder.readIdReportProcess(args[0]);
				
				context.close();
				System.out.println("Fin del procesamiento Masivo");
				
				
			} 
			catch (Throwable e) 
			{
				e.printStackTrace();
			}

	}

}
