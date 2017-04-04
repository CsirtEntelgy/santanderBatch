package com.interfactura.firmalocal.main;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.controllers.MassiveReportExcelController;
import com.interfactura.firmalocal.controllers.InvoiceController;

public class ReportExcel {

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
					"app-config.xml", ReportExcel.class);
			BeanFactory factory = (BeanFactory) context;
			context.registerShutdownHook();
			//logger.info("Paso 1.- Invocando a Procesar facturas para el proceso: " + args[6]);
			MassiveReportExcelController excel = (MassiveReportExcelController) factory.getBean(MassiveReportExcelController.class);
			//if (args.length <= 7)
			//{	invoice.processingInvoices(args[6], "", "");	}
			//else
			//{	invoice.processingInvoices(args[6], args[8], args[7]);	}
			
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date dateInicio = new Date();
			System.out.println("TIMEINICIO:" + dateFormat.format(dateInicio) + "M" + System.currentTimeMillis());
			excel.readIdReportProcess(args[0]);
			//invoice.processingInvoices(args[0], args[2], args[1], args[3]);
			
			context.close();
			System.out.println("Fin del procesamiento de las tareas");
		}catch(Throwable e){
			e.printStackTrace();	
		}
	}

}
