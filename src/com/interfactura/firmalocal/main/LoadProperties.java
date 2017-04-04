package com.interfactura.firmalocal.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.log4j.Logger;

public class LoadProperties 
{
	private String user;
	private String password;
	private String ip;
	private String instance;
	private String port;
	private String nameProperties = "configuration.properties";
	private Logger logger = Logger.getLogger(LoadProperties.class);
	private String namePersitence = "persistence.xml";

	public void set(String... args) 
	{
		this.ip = this.deleteEnter(args[0]);
		this.port = this.deleteEnter(args[1]);
		this.instance = this.deleteEnter(args[2]);
		this.user = this.deleteEnter(args[3]);
		this.password = this.deleteEnter(args[4]);
		this.load(this.deleteEnter(args[5]));
	}

	/**
	 * Cambia el archivo de propiedades y el persistence
	 * @param path
	 * @return
	 */
	private boolean load(String path) 
	{
		boolean flag=true;
		
		boolean processActive = true;
		
		// Verifica si hay otro proceso actualizando los archivos de propiedades
		File banderaUpdatingProperties = new File(path + File.separator + "updatingProperties.dat");
		FileReader updatingReader = null;
		LineNumberReader updatingLr = null;
		FileOutputStream updatingOut = null;
		Writer updatingWriter = null;
		try 
		{
			String lineUpdating = "";
			if (banderaUpdatingProperties.exists())
			{
				updatingReader = new FileReader(banderaUpdatingProperties);
				updatingLr = new LineNumberReader(updatingReader);
				while (updatingLr.ready())
				{	lineUpdating = updatingLr.readLine();	}
				// Si el archivo contiene el valor true, siginifica que lo estan actualizando
				if ("true".equals(lineUpdating))
				{	processActive = true;	}
				else //Si no lo estan actualizando, entonces se marca como true para que no lo tome otro proceso
				{
					System.out.println("***Marcando bandera updating como true...");
					updatingOut = new FileOutputStream(banderaUpdatingProperties, false);
					updatingWriter = new OutputStreamWriter(updatingOut, "UTF-8");
					updatingWriter.write("true");
					processActive = false;
				}
			} 
			else	// Si el archivo no existe, indica que nadie esta actualizando
			{	
				updatingOut = new FileOutputStream(banderaUpdatingProperties, false);
				updatingWriter = new OutputStreamWriter(updatingOut, "UTF-8");
				updatingWriter.write("true");
				processActive = false;	
			}
		}
		catch (FileNotFoundException e) 
		{	e.printStackTrace();	} 
		catch (IOException e)
		{	e.printStackTrace();	}
		finally
		{
			try
			{
				if (updatingLr != null)
				{	updatingLr.close();	}
				if (updatingReader != null)
				{	updatingReader.close();	}
				if (updatingWriter != null)
				{	updatingWriter.close();	}
				if (updatingOut != null)
				{	updatingOut.close();	}
			}
			catch(Exception eUpdating)
			{	eUpdating.printStackTrace();	}
		}
		
		// Ejecuta el replace solo si no hay proceso activo
		if (processActive == false)
		{
			try 
			{
				File file=new File(path + nameProperties);
				FileReader fr = new FileReader(file);
				LineNumberReader lr = new LineNumberReader(fr);
				String line = null;
				String tokens[] = null;
				StringBuilder properties=new StringBuilder();
				logger.info("Cambiando propiedades");
				while (lr.ready()) 
				{
					line = lr.readLine();
					if (line.startsWith("jdbc.user")) 
					{
						tokens = line.split("=");
						line=tokens[0].concat("=").concat(this.user);
					} 
					else if (line.startsWith("jdbc.password")) 
					{
						tokens = line.split("=");
						line=tokens[0].concat("=").concat(this.password);
					} 
					else if (line.startsWith("jdbc.url")) 
					{
						tokens = line.split("=");
						
						if(this.port.equals("null")&&this.instance.endsWith("null"))
						{
							line=tokens[0]+"="+"jdbc:oracle:oci:/@"+this.ip;
						} 
						else 
						{
							line=tokens[0]+"="+"jdbc:oracle:thin:@"+this.ip+":"+this.port+":"+this.instance;
						}
					}
					properties.append(line).append("\r\n");
				}
				lr.close();
				fr.close();
				logger.info("Eliminando archivo de propiedades "+path);
				file.delete();
				logger.info("Escribiendo nuevo archivo de propiedades (INICIO)");
				
				FileOutputStream confg = new FileOutputStream(path + nameProperties, false);
				Writer outConf = new OutputStreamWriter(confg, "UTF-8");
				outConf.write(properties.toString());
				outConf.close();
				logger.info("Escribiendo nuevo archivo de propiedades (FIN)");
				flag=flag&&loadPersintence(path);
			} 
			catch (FileNotFoundException e) 
			{
				flag=false;
				//logger.error("***WARNING: Concurrencia la actualizar properties");
				logger.error(e.getLocalizedMessage());			
			} 
			catch (IOException e) 
			{
				flag=false;
				//logger.error("***WARNING: Concurrencia la actualizar properties");
				logger.error(e.getLocalizedMessage());
			}
			
			// Cuando termina de ejecutar los replaces, actualiza la bandera a false
			try
			{
				System.out.println("***Termina de actualizar. Marcando bandera updating como false...");
				updatingOut = new FileOutputStream(banderaUpdatingProperties, false);
				updatingWriter = new OutputStreamWriter(updatingOut, "UTF-8");
				updatingWriter.write("false");
			}
			catch (FileNotFoundException e) 
			{	e.printStackTrace();	} 
			catch (IOException e)
			{	e.printStackTrace();	}
			finally
			{
				try
				{
					if (updatingWriter != null)
					{	updatingWriter.close();	}
					if (updatingOut != null)
					{	updatingOut.close();	}
				}
				catch(Exception eUpdating)
				{	eUpdating.printStackTrace();	}
			}
		}
		return flag;
	}
	
	/**
	 * 
	 * @param path
	 * @return
	 */
	private boolean loadPersintence(String path)
	{
		boolean flag=true;
		try 
		{
			if(File.separator.equals("/"))
			{	namePersitence="META-INF/"+namePersitence;	}
			else 
			{	namePersitence="META-INF\\"+namePersitence;		}
			File file=new File(path + namePersitence);
			if (!file.exists())
			{
				try
				{
					Thread.currentThread().sleep(10000);
					file=new File(path + namePersitence);
				}
				catch (Exception e1)
				{	e1.printStackTrace();					}
			}
			FileReader fr = null;
			try
			{
				fr = new FileReader(file);
			}
			catch (Exception e2)
			{
				try 
				{	
					Thread.currentThread().sleep(10000);
					file=new File(path + namePersitence);
				} 
				catch (InterruptedException e) 
				{	e.printStackTrace();	}
			}
			LineNumberReader lr = new LineNumberReader(fr);
			String line = null;
			String tokens[] = null;
			StringBuilder properties=new StringBuilder();
			logger.info("Cambiando persistence");
			while (lr.ready()) 
			{
				line = lr.readLine();
				if (line.contains("openjpa.ConnectionUserName")) 
				{
					tokens = line.split("value");
					line=tokens[0].concat("value=\"").concat(this.user.concat("\"/>"));
					logger.info(line);
				} 
				else if (line.contains("openjpa.ConnectionPassword")) 
				{
					tokens = line.split("value");
					line=tokens[0].concat("value=\"").concat(this.password.concat("\"/>"));
					logger.info(line);
				}
				properties.append(line).append("\r\n");
			}
			lr.close();
			fr.close();
			logger.info("Eliminando archivo de persistence "+path);
			file.delete();
			logger.info("Escribiendo nuevo archivo de persistence");
			FileWriter newProperties=new FileWriter(path + namePersitence);
			newProperties.write(properties.toString());
			newProperties.close();
		} 
		catch (FileNotFoundException e) 
		{
			flag=false;
			e.printStackTrace();
			logger.error(e.getLocalizedMessage());
		} 
		catch (IOException e) 
		{
			flag=false;
			logger.error(e.getLocalizedMessage());
		} 
		return flag;
	}
	
	public String deleteEnter(String cadena)
	{
		String newString="";		
		for(int c=0; c<cadena.length();c++)
		{
			if((int)cadena.charAt(c)!=13)
			{	newString+=cadena.charAt(c);	}
		}
		return newString;
	}
	
	public static void main(String ...args)
	{	LoadProperties l = new LoadProperties();	}
}
