package com.interfactura.firmalocal.controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;

import com.interfactura.firmalocal.domain.entities.CFDIssued;
import com.interfactura.firmalocal.domain.entities.CFDIssuedOtros;
import com.interfactura.firmalocal.persistence.CFDIssuedManager;
import com.interfactura.firmalocal.xml.Properties;
import com.interfactura.firmalocal.xml.util.FiltroParam;
import com.interfactura.firmalocal.xml.util.Util;

@Controller
public class IndicesPagosController {
	
	
	
	private Logger logger = Logger.getLogger(IndicesController.class);
	@Autowired
	private Properties properties;
	@Autowired
	private CFDIssuedManager cfdIssuedManager;
	
	
	@Value("${ondemand.file.header.factura}")
	protected String i_headerPago;
	@Value("${ondemand.file.rfc}")
	protected String i_rfc;
	@Value("${ondemand.file.date}")
	protected String i_date;
	@Value("${ondemand.file.serie}")
	protected String i_serie;
	@Value("${ondemand.file.folio}")
	protected String i_folio;
	@Value("${ondemand.file.offset}")
	protected String i_offset;
	@Value("${ondemand.file.length}")
	protected String i_length;
	@Value("${ondemand.file.gfname}")
	protected String i_gfname;
	@Value("${ondemand.file.num_clie}")
	protected String i_clie;
	@Value("${ondemand.file.cuenta}")
	protected String i_account;
	@Value("${ondemand.file.periodo}")
	protected String i_period;
	@Value("${ondemand.file.tarjeta}")
	protected String i_tarjeta;
	@Value("${ondemand.file.fecha}")
	protected String i_dateECB;
	@Value("${ondemand.file.fecha_generacion}")
	protected String i_dateG_ECB;
	@Value("${ondemand.file.contrato}")
	protected String i_contrato;
	@Value("${ondemand.file.cod_clie}")
	protected String i_cod_clien;
	@Value("${ondemand.file.end}")
	protected String i_end;
	@Value("${ondemand.file.folioSATECB}")
	protected String i_foliosatECB;
	@Value("${ondemand.file.folioSATFACT}")
	protected String i_foliosatFACT;
	
	
	@Value("${ondemand.file.size}")
	protected long maxSize;
	@Value("${ondemand.shutdown.time}")
	protected long shutdownTime;
	protected String ext = ".IND";
	
	
	public boolean processing(String fileNames, String fecha, String isFac) 
	{
		int type = 0;
		if ("fac".equals(isFac))
		{	type = -1;	}
		processing(type, fileNames, fecha);
		return true;
	}
	
	
	private boolean processing(int type, String fileNames, String fecha) 
	{
		String[] fileNamesArr = fileNames.split(",");
		List<List> nameFiles = new ArrayList<List>();
		String[] productos = fileNames.split(",");
		for (int i=0; i < fileNamesArr.length; i++)
		{	fileNamesArr[i] = fileNamesArr[i] + fecha + ".TXT"; }
		
		List<CFDIssuedOtros> listOtros = null;
		
		List<CFDIssued> lstCFD = null;
		List<String> processing=new ArrayList<String>();
		// Consultar los documentos que se van a buscar en la base de datos
		File fileLst = null; 
		FiltroParam filter = null;
		if (type == -1) 
		{
			System.out.println("***Buscando archivos para indices de facturas en: " + properties.getPathDirPro());
			fileLst = new File(properties.getPathDirProCFD());
			//filter = new FiltroParam(new String[] { "_","_processing" }, new String[] {
			//		"CFDLZELAVON", "CFDOPOPICS", "CFDCONFIRMINGFACTURAS","CFDFACTORAJEFACTURAS" });
		} 
		else 
		{
			System.out.println("***Buscando archivos para indices de facturas en: " + properties.getPathDirPro());
			fileLst = new File(properties.getPathDirProECB());
			//filter = new FiltroParam(new String[] { "_", "CFDLZELAVON",
			//		"CFDOPOPICS", "CFDCONFIRMINGFACTURAS","_processing","CFDFACTORAJEFACTURAS" });
		}

		//File[] lstFiles = fileLst.listFiles(filter);
		File[] lstFiles = fileLst.listFiles();
		String nameFileOld = "";
		String nameFileNew = null;
		String tokens[] = null;
		for (File file : lstFiles) 
		{
			// Cambio para procesar solo los que se reciben como parametro
			// Revisa que venga en los argumentos
			System.out.println(" ..archivo: " + file.getName());
			String producto = "";
			boolean isOk = false;
			for (int y = 0; y < fileNamesArr.length; y++)
			{
				System.out.println(" ....comparando con: " + fileNamesArr[y]);
				if(file.getName().equals(fileNamesArr[y]))
				{
					producto = productos[y];
					isOk = true;
					break;
				}
			}
			if (!isOk)
			{	continue;	}
			// Termina el cambio para recibir solo los que se reciben como parametro
			
			tokens = file.getName().split("\\.");
			nameFileNew = tokens[0];
			if(file.isFile())
			{
				/*if(File.separator.equals("/"))
				{	nameFileNew=tokens[0].substring(0,tokens[0].length()-9);	} 
				else 
				{	nameFileNew=tokens[0].substring(0,tokens[0].length()-7);	}*/
				try
				{
					if(new File(file.getAbsolutePath()).exists())
					{
						if (!nameFileOld.equals(nameFileNew)&&(!processing.contains(nameFileNew))) 
						{
							// Buscar por documentos los archivos xml que genero
							listOtros = cfdIssuedManager.listarOtros(nameFileNew + "." + tokens[1]);
							
							lstCFD = cfdIssuedManager.listar(nameFileNew + "." + tokens[1]);
							logger.info("Nombre del Archivo: " + nameFileNew + "." + tokens[1]);
							System.out.println("Nombre del Archivo: " + nameFileNew + "." + tokens[1]);
							logger.info("Numero de CFD: " + listOtros.size());
							System.out.println("Numero de CFD: " + listOtros.size());
							if (listOtros != null && listOtros.size() > 0) 
							{
								// Mandar a generar el archivo de indices
								this.createFilesMass(listOtros, type, producto);	
								Date d = new Date(System.currentTimeMillis());
								SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss");
								String backupFileName = properties.getPathDirBackup() + File.separator + 
									nameFileNew + "T" + f.format(d) + "." + tokens[1] + "_processing";
								FileCopyUtils.copy(file, new File(backupFileName));
								file.delete();
							}
						}
					}
				} 
				catch(IOException iex)
				{	System.out.println(iex.getLocalizedMessage());	}
				nameFileOld = nameFileNew;
				processing.add(nameFileOld);
			}
		}

		return false;
	}

	public void createFilesMass(List<CFDIssuedOtros> listCFD, int type, String producto) 
	{
		String idxFileName = null;
		long offset = 0;
		File idxFile = null;
		FileOutputStream foStream = null;
		FileChannel foChannel = null;
		ByteBuffer buffer = null;
		boolean isNewFile = true;
		long contBytes = 0;
		byte[] bytes = null;
		// List<String> files = null;
		int cont = 1;
		String strCont = "";
		String tokens[]=null;
		String temp[]= null;
		try 
		{
			logger.debug("Files " + listCFD.size());
			logger.debug("Max Size File Idx " + maxSize);
			
			String serie;
			String serieECB;
			for (CFDIssuedOtros cfdr : listCFD) 
			{
				logger.debug("File to process " + cfdr.getXmlRoute());
				if (isNewFile) 
				{
					tokens = cfdr.getSourceFileName().split("\\.");
					// files = new LinkedList<String>();

					if (cont < 10) 
					{	strCont = "0" + cont;	} 
					else 
					{	strCont = "" + cont;	}

					idxFileName = properties.getPathDirGenr() + tokens[0]+ strCont + ext;

					idxFile = new File(idxFileName);
					idxFile.createNewFile();
					logger.debug("New file created " + idxFile.getAbsolutePath());
					foStream = new FileOutputStream(idxFile);
					foChannel = foStream.getChannel();
					buffer = ByteBuffer.allocate(i_headerPago.length());
					bytes = i_headerPago.getBytes();
					buffer.put(bytes);
					buffer.flip();
					foChannel.write(buffer);
					isNewFile = false;
					contBytes = contBytes + buffer.position();
					logger.debug("bytes header " + contBytes);
				}

				
				if (cfdr.getFolioRange() == null) 
				{	
					serie = "null";		
				} 
				else if (cfdr.getFolioRange().getSeries() == null) 
				{	
					serie = "null";		
				} 
				else 
				{	
					serie = cfdr.getFolioRange().getSeries().getName();		
				}
				
				String str = null;
				String date = Util.convertirFecha(cfdr.getCreationDate(), "MM/dd/yy H:mm:ss");
				

					//Verificar el campo serieInfo
					serieECB = "";
					if (cfdr.getSerieInfo() != null && !cfdr.getSerieInfo().isEmpty()){
						serieECB = cfdr.getSerieInfo(); 
					}
					
					temp=cfdr.getComplement().concat("|temp").split("\\|");
					logger.debug("Complemento "+cfdr.getComplement());
					logger.debug("Numero de Tokens "+temp.length);
					logger.debug("Numero de Cuenta "+temp[1]);
					logger.debug("Numero de Tarjeta "+temp[2]);
					str = "\n" + i_rfc + cfdr.getFiscalEntity().getTaxID()
							+ "\n" + i_clie + Util.isNullOrEmpty(cfdr.getCustomerCode(),"null")
							+ "\n" + i_account + serieECB + Util.isNullOrEmpty(temp[1],"null")
							+ "\n" + i_period + Util.isNullOrEmpty(cfdr.getPeriod(),"null")
							+ "\n" + i_tarjeta + Util.isNullOrEmpty(null,"null")
							+ "\n" + i_dateECB + date
							+ "\n" + i_dateG_ECB + date							
							+ "\n" + i_foliosatECB + Util.isNullOrEmpty(cfdr.getFolioSAT(),"null")							
							+ "\nGROUP_FIELD_NAME:TIPO_CFD\nGROUP_FIELD_VALUE:" + Util.isNullOrEmpty(cfdr.getCfdType(),"null")
							+ "\nGROUP_FIELD_NAME:PRODUCTO\nGROUP_FIELD_VALUE:" + Util.isNullOrEmpty(producto,"null");
				
				// Valida si el CFD corresponde a un archivo unico para calcular los offsets
				
				//17FEB2012 --- Cambio optimizacion Oracle
				//String routeStr = cfdr.getFilePath().getRoute();
				String routeStr = cfdr.getXmlRoute();
				
				int firstPipe = routeStr.indexOf("|");
				int lastPipe = routeStr.lastIndexOf("|");
				String startStr = routeStr.substring(firstPipe + 1, lastPipe);
				String endStr = routeStr.substring(lastPipe + 1);
				endStr = endStr.replaceAll("\\r|\\n", ""); 
				long length = Long.parseLong(endStr) - Long.parseLong(startStr);
				str += "\n" + i_offset + startStr 
				+ "\n" + i_length + length
				+ "\n" + i_gfname + routeStr.substring(0,firstPipe);
							
				buffer = ByteBuffer.allocate(str.length());
				bytes = str.getBytes();
				buffer.put(bytes);
				buffer.flip();
				foChannel.write(buffer);
				contBytes = contBytes + buffer.position();
				// logger.debug("bytes body " + contBytes);
				// offset = offset + cfdr.getFileLength();
				// logger.debug("offset " + offset);
				// files.add(cfdr.getFilePath().getRoute());
				if (contBytes >= (maxSize - 100)) 
				{
					isNewFile = true;
					buffer = ByteBuffer.allocate(i_end.length());
					bytes = i_end.getBytes();
					buffer.put(bytes);
					buffer.flip();
					contBytes = contBytes + buffer.position();
					logger.debug("bytes end " + contBytes);
					foChannel.write(buffer);
					foChannel.close();
					offset = 0;
					contBytes = 0;
					cont++;
				}
			}
			
			if(isNewFile==false)
			{
				buffer = ByteBuffer.allocate(i_end.length());
				bytes = i_end.getBytes();
				buffer.put(bytes);
				buffer.flip();
				contBytes = contBytes + buffer.position();
				logger.debug("bytes end " + contBytes);
				foChannel.write(buffer);
				foChannel.close();
				offset = 0;
				contBytes = 0;
				cont++;
			}

			if (foChannel != null) 
			{	foChannel.close();	}
		} 
		catch (IOException e) 
		{	logger.error(e.getClass(), e);	}
	}


}
