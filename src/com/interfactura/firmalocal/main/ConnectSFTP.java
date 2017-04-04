package com.interfactura.firmalocal.main;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

public class ConnectSFTP {

	/**
	 * @param args
	 */
	private static Logger logger = Logger.getLogger(Start.class);
	private static final String user = "deupifct";
    private static final String host = "180.176.28.53";
    private static final Integer port = 22;
    private static final String pass = "sistemas1";
    
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try 
		{
		
			//SEt default encoding
			System.setProperty("file.encoding", "UTF-8");
			 
			System.out.println("encodig: " + System.getProperty("file.encoding"));
			
			AbstractApplicationContext context = new ClassPathXmlApplicationContext(
					"app-config.xml", ConnectSFTP.class);
			BeanFactory factory = (BeanFactory) context;
			context.registerShutdownHook();
			
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date dateInicio = new Date();
			System.out.println("TIMEINICIO:" + dateFormat.format(dateInicio) + "M" + System.currentTimeMillis());
			
			System.out.println("------------------- INICIO");
			 
	        JSch jsch = new JSch();
	        Session session = jsch.getSession(user, host, port);
	        UserInfo ui = new SUserInfo(pass, null);
	 
	        session.setUserInfo(ui);
	        session.setPassword(pass);
	        session.connect();
	 
	        ChannelSftp sftp = (ChannelSftp)session.openChannel("sftp");
	        sftp.connect();
	        
	        sftp.cd("/salidas/CFDOndemand/");
	        System.out.println("Subiendo /planCFD/procesos/Interfactura/interfaces/ejemplo.xlsx ...");
	        sftp.put("/planCFD/procesos/Interfactura/interfaces/ejemplo.xlsx", "ejemplo.xlsx");
	 
	        System.out.println("Archivos subidos.");
	 
	        sftp.exit();
	        sftp.disconnect();
	        session.disconnect();
	 
	        System.out.println("----------------- FIN");			
			
			context.close();
			System.out.println("Fin del procesamiento de las tareas");
			
			
		} 
		catch (Throwable e) 
		{
			e.printStackTrace();
		}
	}

}
