package com.interfactura.firmalocal.main;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.interfactura.firmalocal.controllers.InvoiceController;

public class GetAccountsFromIND {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try 
		{
		
			//SEt default encoding
			System.setProperty("file.encoding", "UTF-8");
			 
			System.out.println("encodig: " + System.getProperty("file.encoding"));
			
			AbstractApplicationContext context = new ClassPathXmlApplicationContext(
					"app-config.xml", GetAccountsFromIND.class);
			BeanFactory factory = (BeanFactory) context;
			context.registerShutdownHook();
			
			
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date dateInicio = new Date();
			System.out.println("TIMEINICIO:" + dateFormat.format(dateInicio) + "M" + System.currentTimeMillis());
			
			processIND(args[0], args[1]);
			
			context.close();
			System.out.println("Fin del procesamiento de las tareas");
			
			
		} 
		catch (Throwable e) 
		{
			e.printStackTrace();
		}
	}

	public static void processIND(String pathIND, String pathOut) throws Exception{
		FileInputStream fStream = null;		
		DataInputStream dInput = null;
		BufferedReader bReader = null;
		
		fStream = new FileInputStream(pathIND);			
		dInput = new DataInputStream(fStream);
		bReader = new BufferedReader(new InputStreamReader(dInput));
		
		FileOutputStream fileSalida = new FileOutputStream(new File(pathOut));
		
		String line;
		boolean flagAccount = false;
		while((line = bReader.readLine()) != null){
			if(flagAccount){
				String [] arrayValues = line.split(":");
				fileSalida.write((arrayValues[1] + "\n").getBytes("UTF-8"));
				flagAccount=false;
			}
			if(line.equals("GROUP_FIELD_NAME:CUENTA")){
				flagAccount = true;
			}
			
		}
		
		fileSalida.close();
		bReader.close();
		dInput.close();
		fStream.close();
	}
}

