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

import com.interfactura.firmalocal.domain.entities.CFDIssuedOtros;
import com.interfactura.firmalocal.persistence.CFDIssuedManager;
import com.interfactura.firmalocal.xml.Properties;
import com.interfactura.firmalocal.xml.util.Util;

@Controller
public class IndicesDivisasController {
	
	private Logger logger = Logger.getLogger(MassiveIndexDivisasController.class);
	@Autowired
	private Properties properties;
	@Autowired
	private CFDIssuedManager cfdIssuedManager;
	@Value("${ondemand.file.header.factura}")
	protected String i_headerFactura;
	@Value("${ondemand.file.header.ecb}")
	protected String i_headerECB;
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

	
	
	public boolean processing(String fileNames, String fecha, String type ) {
		
		String[] fileNamesArr = fileNames.split(",");
		String[] productos = fileNames.split(",");
		for (int i=0; i < fileNamesArr.length; i++)
		{	fileNamesArr[i] = fileNamesArr[i] + fecha + ".TXT"; }
		
		List<CFDIssuedOtros> listOtros = null;
		
		List<String> processing = new ArrayList<String>();
		// Consultar los documentos que se van a buscar en la base de datos
		File fileLst = null; 
		
		System.out.println("***Buscando archivos para indices de facturas en: " + properties.getPathDirPro());
		fileLst = new File(properties.getPathDirProCFD());

		File[] lstFiles = fileLst.listFiles();
		String nameFileOld = "";
		String nameFileNew = null;
		String tokens[] = null;
		for (File file : lstFiles) {

			
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
			if(file.isFile()) {

				try	{
					
					if(new File(file.getAbsolutePath()).exists()) {
						
						if (!nameFileOld.equals(nameFileNew)&&(!processing.contains(nameFileNew))) 
						{
							// Buscar por documentos los archivos xml que genero
							listOtros = cfdIssuedManager.listarOtros(nameFileNew + "." + tokens[1]);
							
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

	public void createFilesMass(List<CFDIssuedOtros> listCFD, String type, String producto) {
		
		String idxFileName = null;
		File idxFile = null;
		FileOutputStream foStream = null;
		FileChannel foChannel = null;
		ByteBuffer buffer = null;
		boolean isNewFile = true;
		long contBytes = 0;
		byte[] bytes = null;
		int cont = 1;
		String strCont = "";
		String tokens[]=null;
		try 
		{
			logger.debug("Files " + listCFD.size());
			logger.debug("Max Size File Idx " + maxSize);
			
			String serie;
			for (CFDIssuedOtros cfdr : listCFD) 
			{
				logger.debug("File to process " + cfdr.getXmlRoute());
				if (isNewFile) 
				{
					tokens = cfdr.getSourceFileName().split("\\.");

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
					buffer = ByteBuffer.allocate(i_headerFactura.length());
					bytes = i_headerFactura.getBytes();
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
				

				str = "\n" + i_rfc + cfdr.getFiscalEntity().getTaxID()
						+ "\n" + i_contrato + Util.isNullOrEmpty(cfdr.getContractNumber(),"null") 
						+ "\n" + i_cod_clien+ Util.isNullOrEmpty(cfdr.getCustomerCode(),"null")
						+ "\n" + i_period + Util.isNullOrEmpty(cfdr.getPeriod(),"null")
						+ "\n" + i_date+ date 
						+ "\n" + i_serie + serie 
						+ "\n" + i_folio+ cfdr.getFolio()
						+ "\n" + i_foliosatFACT + Util.isNullOrEmpty(cfdr.getFolioSAT(),"null").replaceAll("\\r|\\n", "");							
						//+ "\nGROUP_FIELD_NAME:FOLIOSAT\nGROUP_FIELD_VALUE:" + "null";
				
				
				String routeStr = cfdr.getXmlRoute();
				
				
				
				if ((routeStr != null)&&(routeStr.indexOf("|") > 0))
				{
					int firstPipe = routeStr.indexOf("|");
					int lastPipe = routeStr.lastIndexOf("|");
					String startStr = routeStr.substring(firstPipe + 1, lastPipe);
					String endStr = routeStr.substring(lastPipe + 1);
					endStr = endStr.replaceAll("\\r|\\n", ""); 
					long length = Long.parseLong(endStr) - Long.parseLong(startStr);
					str += "\n" + i_offset + startStr 
					+ "\n" + i_length + length
					+ "\n" + i_gfname + routeStr.substring(0,firstPipe);
				}
				logger.debug("Massive debug: "+str);	
			
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
