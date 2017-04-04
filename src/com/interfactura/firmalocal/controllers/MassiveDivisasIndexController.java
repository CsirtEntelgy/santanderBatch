package com.interfactura.firmalocal.controllers;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
import com.interfactura.firmalocal.persistence.CFDIssuedManager;
import com.interfactura.firmalocal.xml.Properties;
import com.interfactura.firmalocal.xml.util.FiltroParam;
import com.interfactura.firmalocal.xml.util.Util;

@Controller
public class MassiveDivisasIndexController {
	private Logger logger = Logger.getLogger(MassiveDivisasIndexController.class);
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
	
	String facturacionDivisasEntrada=MassiveDivisasReadController.facturacionDivisasEntrada;
	String facturacionDivisasProceso=MassiveDivisasReadController.facturacionDivisasProceso;
	String facturacionDivisasSalida=MassiveDivisasReadController.facturacionDivisasSalida;
	String facturacionDivisasOndemand=MassiveDivisasReadController.facturacionDivisasOndemand;

	public boolean processing() 
	{			
		try {
			//FileInputStream fsExcelsToProcess = new FileInputStream(properties.getPathFacturacionDivisasEntrada() + "IDFILEPROCESS_" + nProceso + ".TXT");
			FileInputStream fsExcelsToProcess = new FileInputStream(facturacionDivisasEntrada + "IDFILEPROCESS.TXT");
		
			DataInputStream in = new DataInputStream(fsExcelsToProcess);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			//FileOutputStream fileStatus = new FileOutputStream(properties.getPathFacturacionDivisasProceso() + "STATUS_INDEX_" + nProceso + ".TXT");
			FileOutputStream userlog = null;
			FileOutputStream fileStatus = new FileOutputStream(facturacionDivisasProceso + "massiveDivisasIndex.txt");
			fileStatus.write(("Status del proceso bash massiveDivisasIndex.sh" + "\n").getBytes("UTF-8"));
			
			String strLine;
			int counter = 0;
			while((strLine = br.readLine()) != null){
				//if(!strLine.trim().toUpperCase().equals("NO HAY SOLICITUDES DE CARGA")){
					System.out.println("lineExcelsToProcess: " + strLine);
					String [] arrayRenglon = strLine.split("\\|");
					if(arrayRenglon.length>1){
						String idMassive = arrayRenglon[0];
						String nameFileExcel = arrayRenglon[1];
						System.out.println("idMassive: " + idMassive);
						System.out.println("nameFileExcel: " + nameFileExcel);
						
						List<CFDIssued> lstCFD = null;
						// Buscar por documentos los archivos xml que genero		
						lstCFD = cfdIssuedManager.listar(nameFileExcel + ".TXT");
						logger.info("Nombre del Archivo: " + nameFileExcel + ".TXT");
						System.out.println("Nombre del Archivo: " + nameFileExcel + ".TXT");
						logger.info("Numero de CFDs: " + lstCFD.size());
						System.out.println("Numero de CFDs: " + lstCFD.size());
						if (lstCFD != null && lstCFD.size() > 0) 
						{
							// Mandar a generar el archivo de indices
							this.createIndexFile(lstCFD);	
							//Date d = new Date(System.currentTimeMillis());
							//SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss");
							//String backupFileName = properties.getPathDirBackup() + File.separator + nameFileNew + "T" + f.format(d) + "." + tokens[1] + "_processing";
							//FileCopyUtils.copy(file, new File(backupFileName));
							//file.delete();
							fileStatus.write(("Archivo " + nameFileExcel + ".IND generado\n").getBytes("UTF-8"));
							userlog = new FileOutputStream(facturacionDivisasSalida + arrayRenglon[1] + "/LOG" + arrayRenglon[1] + ".TXT",true);
							userlog.write(("Archivo " + nameFileExcel + ".IND generado\n").getBytes("UTF-8"));
						}else{
							fileStatus.write(("No se econtraron facturas para generar el archivo " + nameFileExcel + ".IND\n").getBytes("UTF-8"));
							userlog = new FileOutputStream(facturacionDivisasSalida + arrayRenglon[1] + "/LOG" + arrayRenglon[1] + ".TXT",true);
							userlog.write(("No se econtraron facturas para generar el archivo " + nameFileExcel + ".IND\n").getBytes("UTF-8"));
						}
						
					}
				//}
					counter++;
			}
			br.close();
			in.close();
			fsExcelsToProcess.close();
			if(counter == 0){
				fileStatus.write(("No se encontraron solicitudes a procesar\n").getBytes("UTF-8"));
			}
			fileStatus.close();
			if(userlog!=null) userlog.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("Exception massiveDivisasIndex:" + ex.getMessage());
			
			try {
				//FileOutputStream fileError = new FileOutputStream(properties.getPathFacturacionDivisasProceso() + "ERROR_INDEX_" + nProceso + ".TXT");
				FileOutputStream fileError = new FileOutputStream(facturacionDivisasProceso + "massiveDivisasIndexError.txt");
				fileError.write((ex.getMessage()).getBytes("UTF-8"));
				fileError.close();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				//System.out.println("Exception al crear ERROR_INDEX_" + nProceso + ".TXT:" + e1.getMessage());
				System.out.println("Exception al crear massiveDivisasIndexError.txt:" + e1.getMessage());
			}
		}
		return true;
	}

	private boolean processing(int type, String fileNames, String fecha) 
	{
		String[] fileNamesArr = fileNames.split(",");
		for (int i=0; i < fileNamesArr.length; i++)
		{	fileNamesArr[i] = fileNamesArr[i] + fecha + ".TXT"; }
		
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
			boolean isOk = false;
			for (int y = 0; y < fileNamesArr.length; y++)
			{
				System.out.println(" ....comparando con: " + fileNamesArr[y]);
				if(file.getName().equals(fileNamesArr[y]))
				{
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
							lstCFD = cfdIssuedManager.listar(nameFileNew + "." + tokens[1]);
							logger.info("Nombre del Archivo: " + nameFileNew + "." + tokens[1]);
							System.out.println("Nombre del Archivo: " + nameFileNew + "." + tokens[1]);
							logger.info("Numero de CFD: " + lstCFD.size());
							System.out.println("Numero de CFD: " + lstCFD.size());
							if (lstCFD != null && lstCFD.size() > 0) 
							{
								// Mandar a generar el archivo de indices
								this.createFilesMass(lstCFD, type);	
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

	/**
	 * Proceso que genera el archivo de indices para un listado de CFD del
	 * proceso masivo
	 * 
	 * @param listCFD
	 */
	public void createFilesMass(List<CFDIssued> listCFD, int type) 
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
			for (CFDIssued cfdr : listCFD) 
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
					if (type == -1) 
					{
						buffer = ByteBuffer.allocate(i_headerFactura.length());
						bytes = i_headerFactura.getBytes();
					} 
					else 
					{
						buffer = ByteBuffer.allocate(i_headerECB.length());
						bytes = i_headerECB.getBytes();
					}
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
				
				if (type == -1) 
				{
					str = "\n" + i_rfc + cfdr.getFiscalEntity().getTaxID()
							+ "\n" + i_contrato + Util.isNullOrEmpty(cfdr.getContractNumber(),"null") 
							+ "\n" + i_cod_clien+ Util.isNullOrEmpty(cfdr.getCustomerCode(),"null")
							+ "\n" + i_period + Util.isNullOrEmpty(cfdr.getPeriod(),"null")
							+ "\n" + i_date+ date 
							+ "\n" + i_serie + Util.isNullOrEmpty(serie, "null") 
							+ "\n" + i_folio+ Util.isNullOrEmpty(cfdr.getFolio(), "null")
							+ "\n" + i_foliosatFACT + Util.isNullOrEmpty(cfdr.getFolioSAT(),"null");							
							//+ "\nGROUP_FIELD_NAME:FOLIOSAT\nGROUP_FIELD_VALUE:" + "null";
				} 
				else 
				{
					//Verificar el campo serieInfo
					serieECB = "";
					if (cfdr.getserieInfo() != null && !cfdr.getserieInfo().isEmpty()){
						serieECB = cfdr.getserieInfo(); 
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
							+ "\n" + i_tarjeta + Util.isNullOrEmpty(temp[2],"null")
							+ "\n" + i_dateECB + date
							+ "\n" + i_dateG_ECB + date							
							+ "\n" + i_foliosatECB + Util.isNullOrEmpty(cfdr.getFolioSAT(),"null");							
							//+ "\nGROUP_FIELD_NAME:FOLIOSAT\nGROUP_FIELD_VALUE:" + "null";
				}
				
				// Valida si el CFD corresponde a un archivo unico para calcular los offsets
				
				//17FEB2012 --- Cambio optimizacion Oracle
				//String routeStr = cfdr.getFilePath().getRoute();
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
				else
				{
					str += "\n" + i_offset + 0 
					+ "\n" + i_length + 0
					+ "\n" + i_gfname + routeStr;
				}				
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
	
	public void createIndexFile(List<CFDIssued> listCFD) throws Exception 
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
		/*try 
		{*/
			logger.debug("Files " + listCFD.size());
			logger.debug("Max Size File Idx " + maxSize);
			
			String serie;
			String serieECB;
			for (CFDIssued cfdr : listCFD) 
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

					idxFileName = facturacionDivisasOndemand + tokens[0]+ strCont + ext;

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
				
				
				// Valida si el CFD corresponde a un archivo unico para calcular los offsets
				
				//17FEB2012 --- Cambio optimizacion Oracle
				//String routeStr = cfdr.getFilePath().getRoute();
				String routeStr = cfdr.getXmlRoute();
				logger.debug("Massive debug: "+routeStr);
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
				logger.debug("Massive debug: "+routeStr);				
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
		/*} 
		catch (IOException e) 
		{	logger.error(e.getClass(), e);	}*/
	}
}
