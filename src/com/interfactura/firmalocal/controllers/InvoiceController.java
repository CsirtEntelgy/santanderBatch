package com.interfactura.firmalocal.controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.ValidatorHandler;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.xml.sax.SAXException;

import com.interfactura.firmalocal.domain.entities.CFDFieldsV22;
import com.interfactura.firmalocal.domain.entities.FiscalEntity;
import com.interfactura.firmalocal.domain.entities.InterfaceMetodoPago;
import com.interfactura.firmalocal.domain.entities.Iva;
import com.interfactura.firmalocal.domain.entities.RegimenFiscal;
import com.interfactura.firmalocal.domain.entities.SealCertificate;
import com.interfactura.firmalocal.persistence.CFDFieldsV22Manager;
import com.interfactura.firmalocal.persistence.FiscalEntityManager;
import com.interfactura.firmalocal.persistence.InterfaceMetodoPagoManager;
import com.interfactura.firmalocal.persistence.IvaManager;
import com.interfactura.firmalocal.persistence.RegimenFiscalManager;
import com.interfactura.firmalocal.persistence.SealCertificateManager;
import com.interfactura.firmalocal.xml.Properties;
import com.interfactura.firmalocal.xml.ecb.GeneraXML_ECBDS;
import com.interfactura.firmalocal.xml.ecb.GeneraXML_ECBDSV3_3;
import com.interfactura.firmalocal.xml.factura.GeneraXML_CFD;
import com.interfactura.firmalocal.xml.factura.GeneraXML_CFDV3_3;
import com.interfactura.firmalocal.xml.nc.GeneraXML_NC;
import com.interfactura.firmalocal.xml.util.FiltroParam;
import com.interfactura.firmalocal.xml.util.NombreAplicativo;

import altec.infra.StringEncrypter;
import altec.infra.StringEncrypter.EncryptionException;

@Controller
public class InvoiceController 
{	
	@Autowired
	private GeneraXML_ECBDS xmlECB;
	@Autowired
	private GeneraXML_ECBDSV3_3 xmlECBV3; // Clase para version 3.3 AMDA
	@Autowired
	private GeneraXML_CFDV3_3 xmlCFDV3; // Clase para version 3.3 AMDA
	@Autowired
	private GeneraXML_CFD xmlCFD;
	@Autowired
	private GeneraXML_NC xmlNC;
	@Autowired
	private Properties properties;
	@Autowired
	public FiscalEntityManager fiscal;
	@Autowired
	private SealCertificateManager sealCertificateManager;
	@Autowired
	public IvaManager ivaManager;
	@Autowired
	public CFDFieldsV22Manager fields22Manager;
	@Autowired
	public InterfaceMetodoPagoManager interfaceMetPagManager;
	
	@Autowired
	public RegimenFiscalManager regimenManager;
	private Logger logger = Logger.getLogger(InvoiceController.class);
	private int contFiles;
	private SAXTransformerFactory tr;
	private Transformer transf;
	private BufferedReader flagR;

	
	
	/**Procesa el CFD o ECB establecido en las 
	 * configuraciones
	 * @param idProceso
	 */
	public synchronized void processingInvoices(String idProceso,
			String fileNames, String fecha, String urlWebService, String numeroMalla) 
	{
		//Instalar certificados
		/*System.setProperty("javax.net.ssl.keyStoreType", "PKCS12");
		System.setProperty("javax.net.ssl.keyStore", properties.getCertificadoSantander());
		System.setProperty("javax.net.ssl.keyStorePassword", properties.getCertificadoPass());													
		System.setProperty("javax.net.ssl.trustStore", properties.getCertificadoInterfactura());
		*/
		
		String path=properties.getConfigurationPath();
		String zeros="";
		if(!idProceso.equals("-1"))
		{
			int max=5-idProceso.length();
			for(int c=0;c<max;c++)
			{	zeros+="0";	}
			path+=zeros+idProceso+".txt";
		}
		//logger.info("Nombre del Archivo de tareas: " + path);
		String args[]=null;
		LineNumberReader reader=null;
		File fileout=null;
		String line =null;
		File file=new File(path);
		contFiles = 0;
		long byteStart=-1;
		long byteEnd=-1;
		long cont=-1;
		
		//**** Archivo Cadena Original (Carga-Inicio)****
		try 
		{	//System.out.println("getPathFileSello:" + properties.getPathFileSello());
			tr = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
			transf = tr.newTransformer(new StreamSource(properties
					.getPathFileSello()));
		} 
		catch (TransformerConfigurationException e) 
		{	
			e.printStackTrace();
			logger.error("No se cargo el archivo de cadena original", e);	
		}
		//****  Archivo Cadena Original (Carga-End)****

		//**** Ivas (Carga-Inicio)****
		List<Iva> lstIva = ivaManager.listar();
		//**** Ivas (Carga-Fin)****
		
		//**** Certificados (Carga-Inicio)****
		List<SealCertificate> lstSeal = sealCertificateManager.listar();
		System.out.println("Inicio de decrypt private key para certificados");
		for (SealCertificate objC : lstSeal) 
		{
			if (objC.getFiscalEntity() != null) 
			{
				//logger.info(objC.getFiscalEntity().getTaxID());
				try 
				{
					StringEncrypter encrypter = new StringEncrypter(StringEncrypter.DES_ENCRYPTION_SCHEME);
					String plainPassword = encrypter.decrypt(objC.getPrivateKeyPassword());
					objC.setPrivateKeyPassword(plainPassword);
				} 
				catch (EncryptionException e) 
				{	
					e.printStackTrace();
					logger.error(e.getLocalizedMessage(), e);	
				}
			}
		}
		System.out.println("Inicio de decrypt private key para certificados");
		System.out.println("Numero de Certificados: " + lstSeal.size());
		//logger.debug("Numero de Certificados: " + lstSeal.size());
		//**** Certificados (Carga-Fin)****

		//**** Entidades Fiscales (Carga-Inicio)****
		HashMap<String, FiscalEntity> setF = new HashMap<String, FiscalEntity>();
		List<FiscalEntity> lstFiscal = fiscal.listar();
		
		//Busqueda datos v22
		HashMap<String, HashMap> lstCampos22 = new HashMap<String, HashMap>();
		HashMap<String, HashMap> lstTipoCambio = new HashMap<String, HashMap>();
		
		List<RegimenFiscal> listRegimen = new ArrayList<RegimenFiscal>();
		List<CFDFieldsV22> listFields = new ArrayList<CFDFieldsV22>();
		List<InterfaceMetodoPago> listaInterfaces = new ArrayList<InterfaceMetodoPago>();
		
		//Este HashMap Contiene como llave el FiscalEntity ID, y la interface de pago como objeto.
		HashMap<Long, InterfaceMetodoPago> listIntMetPag = new HashMap<Long, InterfaceMetodoPago>();
		
		
		listaInterfaces = interfaceMetPagManager.listByColumn(fileNames);
		listFields = fields22Manager.listAll();
		
		//System.out.println("Estas son todas las interfaces que me trajo el manager: ");
		System.out.println("Creando lista HashMap  que contiene como llave la Entidad Fiscal, y la interface de pago como objeto.");
		for(InterfaceMetodoPago iMP: listaInterfaces){
			//System.out.println("Entidad Fiscal: " + iMP.getFiscalEntity().getId());
			//System.out.println("Nombre de Interface: " + iMP.getNombreInterface());
			listIntMetPag.put(iMP.getFiscalEntity().getId(), iMP);
		}
		System.out.println("Lista creada");
		
		/*System.out.println("-----------------------------------------------------------------------------------------------------------------");
		System.out.println("A continuacion el MAP con todas las interfaces traidas: ");
		for(InterfaceMetodoPago iMP : listIntMetPag.values()){
			System.out.println("Fiscal Entity del MAP: " + iMP.getFiscalEntity().getId());
			System.out.println("Nombre de Interface: " + iMP.getNombreInterface());
		}*/
		System.out.println("Comenzar a agregar valores 22 para taxId o id");
		for (FiscalEntity obj : lstFiscal) {	
			String taxId = obj.getTaxID();
			setF.put(taxId, obj);	
			// Agregar valores 22 para taxId o id
			CFDFieldsV22 f = null;
			for (CFDFieldsV22 obj2 : listFields)
			{
				if (obj.getId() == obj2.getFiscalEntity().getId())
				{
					HashMap valores22 = new HashMap(); /// Sacar de Bd lo que corresponda la Id
					valores22.put("unidadMedida", obj2.getUnidadDeMedida());
					valores22.put("regimenFiscal", obj2.getRegimenFiscal().getName());
					valores22.put("regimenFiscalCode", obj2.getRegimenFiscal().getCode()); // Clave RegimenFiscal AMDA version 3.3
					valores22.put("formaDePago", obj2.getFormaDePago());
					if(obj2.getFiscalEntity() != null){ // Codigo Postal AMDA version 3.3
						if(obj2.getFiscalEntity().getAddress() != null){
							valores22.put("codPostal", obj2.getFiscalEntity().getAddress().getZipCode());
						}
					}
					
					//System.out.println("Fiscal Entity ID a buscar: " + obj2.getFiscalEntity().getId() );
					
					if(listIntMetPag.containsKey(obj2.getFiscalEntity().getId())){
						
						if( !(listIntMetPag.get(obj2.getFiscalEntity().getId()).getNombreInterface() == null )){
							//System.out.println("Este es el nombre de la interace de pago: " + listIntMetPag.get(obj2.getFiscalEntity().getId()).getNombreInterface());
							//System.out.println("FileNames: " + fileNames);
							
							
							String[] splitNames = fileNames.split(",");
							
							for(String fileName : splitNames){
								if(fileName.equals(listIntMetPag.get(obj2.getFiscalEntity().getId()).getNombreInterface()) && listIntMetPag.get(obj2.getFiscalEntity().getId()).getActivo().equals("1")){
									//System.out.println("El Id Entidad Fiscal y el nombre de Interface, Es igual, cambiare el metodo de pago por el de Interface Metodo de Pago");
									valores22.put("metodoDePago", listIntMetPag.get(obj2.getFiscalEntity().getId()).getClaveMetodoPago());
									valores22.put("descripcion", listIntMetPag.get(obj2.getFiscalEntity().getId()).getDescripcion());
								}
							}
							
							
							
						}else{
							System.out.println("  El nombre de la interface con entidada fiscal: "+obj2.getFiscalEntity().getId()+" era null");
						}
						
					}else{
						System.out.println("El Fiscal Entity: "+obj2.getFiscalEntity().getId()+" no estuvo en la listaMetodos de Pago");
						
						valores22.put("metodoDePago", obj2.getMetodoDePago());
						valores22.put("descripcion", "");
					}
					valores22.put("LugarExpedicion", obj2.getLugarDeExpedicion());
					valores22.put("formaDePago", obj2.getFormaDePago());
					
					
					lstCampos22.put(taxId, valores22);
					//System.out.println("***Colocando valores para: " + taxId);
					break;
				}
			}
			
			/////Buscar en el nuevo catalogo y si esta coincidiendo el fiscal entyty... sobre escribir metodo de pago y descripcion del pago...
			//Filenames y fiscalentity ID...
		}
		System.out.println("Se termino de agregar valores 22 para taxId o id");
		
		// leer archivos de tipo de cambio y cargar en lstTipoCambio
		/*
		try
		{	    	
			
	    	BufferedReader bf = new BufferedReader(new FileReader(properties.getTipoCambio()));
	    	String sCadena = null;   	
			while((sCadena  = bf.readLine()) != null)
			{			
				String[] linea = sCadena.split(",");
				String fechaStr = linea[0];
				String monedaStr = linea[1];
				String valorStr = linea[2];
				HashMap tipos = lstTipoCambio.get(fechaStr);
				if (tipos == null)
				{	tipos = new HashMap();	}
				tipos.put(monedaStr, valorStr);
				lstTipoCambio.put(fechaStr, tipos);    	}
				
			
    	} 
		catch (IOException e)
    	{	e.printStackTrace();	}*/
		
		HashMap tipos = new HashMap();
		lstTipoCambio.put("", tipos);
		
		//**** Entidades Fiscales (Carga-Inicio)****
		ValidatorHandler val=null;
		try 
		{	val=createValidatorHandler();	} 
		catch (SAXException e1) 
		{	
			e1.printStackTrace();
			logger.error(e1.getLocalizedMessage(),e1);	
		}
		
//		//Ciclo que lee una bandera para el procesamiento 
//		// de CFD o ECB 
//		while (getBandera()) {
//			contFiles = 1;
//			logger.debug("Iniciando carga masiva");
//			logger.debug("Valor: " + properties.getCurrency());
			try 
			{
				//if (properties.getPathProTipo().equals("ECB")) {
				if(!idProceso.equals("-1")) 
				{
					if(path.equals("null"))
					{
						logger.info("Paso 2.- Procesando Estados de Cuenta Completo: "+idProceso);
						FiltroParam filter=new FiltroParam(
								new String[]{"XML","INC","backUp","CFDLZELAVON","CFDOPOPICS","CFDCONFIRMINGFACTURAS","CFDFACTORAJEFACTURAS"});
						this.processing(
								new File(properties.getPathDirProECB())
									.listFiles(filter),
									true, setF, lstTipoCambio, lstCampos22, lstSeal, lstIva, transf, val,
									fileNames, fecha, idProceso, urlWebService, numeroMalla);
					}
					else 
					{
											
						System.out.println("Procesando Estados de Cuenta por Tareas: "+idProceso);
						
						/*
						Thread hilo1 = new Thread(new Thread1(path, byteStart,  byteEnd,  cont,
			        			 setF,  lstTipoCambio, lstCampos22, 
			        			 lstSeal,  lstIva,  val, idProceso, fecha, fileNames, true, transf));
			           
			            hilo1.start();
			            */
			            
			            
						if(file.exists()&&file.length()>0)
						{
							reader = new LineNumberReader(new FileReader(file));	
							//fileout=new File(properties.getPathDirProcesados()+file.getName());
													
				            line = null;
				            //int counter = 0;
				            //System.out.println("LineNumber: " + reader.getLineNumber());
				            while ((line = reader.readLine()) != null) 
							{			
				            	//System.out.println("processingLines: " + line.length());
								//System.out.println("counter: " + counter);
								//System.out.println("LINEA PROCESO: Inicia - " + line);
								//System.out.println("transf: " + transf);
								args=line.split("\\|");
								if(args!=null&&args.length>=5)
								{	
									//System.out.println("args[1]: " + args[1].toString());
									byteStart = Long.parseLong(args[2]);
									byteEnd = Long.parseLong(args[3]);
									cont = Long.parseLong(args[4]);
							    	//logger.info("Paso 2.- Procesando Estados de Cuenta (byte Inicio)"+byteStart);
							    	//logger.info("Paso 2.- Procesando Estados de Cuenta (byte Inicio)"+byteEnd);
							    	/*
							    	String pathXML = args[1].toString().substring(0, 42);
							    	String pathXML2 = args[1].toString().substring(45, args[1].toString().length()-4);
							    								    	
							    	System.out.println("pathXML: " + pathXML + "XML" + pathXML2 + "_" + cont + "_" + idProceso + ".TXT");
							    								    	
							    	File fileXML = new File(pathXML + "XML" + pathXML2 + "_" + cont + "_" + idProceso + ".TXT");
							    	if(!fileXML.exists() || fileXML.length() == 0){
							    		this.processing(true, setF, lstTipoCambio, lstCampos22, 
												lstSeal, lstIva, transf, val, byteStart, byteEnd, args[1], cont, idProceso, fecha, fileNames, urlWebService);	
							    	}*/									
							    	this.processing(true, setF, lstTipoCambio, lstCampos22, 
											lstSeal, lstIva, transf, val, byteStart, byteEnd, args[1], cont, idProceso, fecha, fileNames, urlWebService, numeroMalla);
								}
								//System.out.println("LINEA DE PROCESO Terminada:  " + line);
								//counter+=1;
							}
				            					
				            reader.close();
				            
				            //Copia el archivo en el directoria de procesados
				            //FileCopyUtils.copy(file,fileout);
				            //file.delete();
						}
						
					}
				} 
				else 
				{
					//if(path.equals("null")){
					if(idProceso.equals("-1"))
					{
						//logger.info("Paso 2.- Procesando Facturas Completo: "+idProceso);
						System.out.println("Procesando facturas");
						FiltroParam filter =new FiltroParam(new String[]{"XML","INC","backUp"}, 
								new String[]{"CFDLZELAVON","CFDOPOPICS","CFDCONFIRMINGFACTURAS","CFDFACTORAJEFACTURAS","CFDCONFIRMINGNEW"});
						this.processing(
								new File(properties.getPathDirProCFD())
									.listFiles(filter),
								false, setF, lstTipoCambio, lstCampos22, lstSeal, lstIva, transf, val,
								fileNames, fecha, idProceso, urlWebService, numeroMalla);
//					} else {
//						if(file.exists()){
//							reader=new LineNumberReader(new FileReader(file));
//							
//							//fileout=new File(properties.getPathDirProcesados()+file.getName());
//				            line = null;
//				            
//				            while ((line = reader.readLine()) != null) {
//				            	args=line.split("\\|");
//				            	byteStart=Integer.parseInt(args[2]);
//				            	byteEnd=Integer.parseInt(args[3]);
//				            	cont=Integer.parseInt(args[4]);
//								this.processing(false, setF, lstSeal, lstIva, transf, val,  
//										byteStart, byteEnd, args[1], cont);
//				            }
//				            
//				            reader.close();
//				            //Copia el archivo al directorio de procesados
//				            //FileCopyUtils.copy(file,fileout);
//				            file.delete();
//						}
					}
				}
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
				logger.error(e.getLocalizedMessage().replace("ORA-", "ORACLE-"));
				System.exit(-1);
			}
			// System.gc();
//		}
//
//		if (contFiles > 0) {
//			startFlag();
//		}

		System.exit(0);
	}

	/**
	 * Procesa varios archivos CFD o ECB
	 * @param file
	 * @param isECB
	 * @param lstFiscal
	 * @param lstSeal
	 * @param lstIva
	 * @param transf
	 * @param val
	 */
	/*
	public void processingLines(String path, long byteStart, long byteEnd, long cont, 
			HashMap<String, FiscalEntity> setF, HashMap<String, HashMap> lstTipoCambio, 
			HashMap<String, HashMap> lstCampos22, List<SealCertificate> lstSeal, List<Iva> lstIva, 
			ValidatorHandler val, String idProceso, String fecha, String fileNames){
		
		try {
			LineNumberReader readerLines = null;
			File file=new File(path);
			if(file.exists()&&file.length()>0)
			{
				readerLines = new LineNumberReader(new FileReader(file));
				int counter=0;
				
				String lineLines = null;
				String args[] = null;
				
				while ((lineLines = readerLines.readLine()) != null) 
				{
					System.out.println("processingLines: " + lineLines.length());
					if (counter % 2 == 0){
						System.out.println("counter: " + counter);
						System.out.println("LINEA PROCESO: Inicia - " + lineLines);
						System.out.println("transf: " + transf);
						args=lineLines.split("\\|");
						if(args!=null&&args.length>=5)
						{	System.out.println("args[1]: " + args[1].toString());
					    	byteStart = Long.parseLong(args[2]);
					    	byteEnd = Long.parseLong(args[3]);
					    	cont= Long.parseLong(args[4]);
					    	logger.info("Paso 2.- Procesando Estados de Cuenta (byte Inicio)"+byteStart);
					    	logger.info("Paso 2.- Procesando Estados de Cuenta (byte Inicio)"+byteEnd);
							this.processing(true, setF, lstTipoCambio, lstCampos22, 
									lstSeal, lstIva, transf, val, byteStart, byteEnd, args[1], cont, idProceso, fecha, fileNames);
						}
						System.out.println("LINEA PROCESO: Termina - " + lineLines);
					}				
					counter+=1;
				}	
				readerLines.close();
				file.delete();
			}
								
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			logger.error(e.getLocalizedMessage().replace("ORA-", "ORACLE-"));
			System.exit(-1);
		}
		
	}
	
	public void processingLines2(String path, long byteStart, long byteEnd, long cont, 
			HashMap<String, FiscalEntity> setF, HashMap<String, HashMap> lstTipoCambio, 
			HashMap<String, HashMap> lstCampos22, List<SealCertificate> lstSeal, List<Iva> lstIva, 
			ValidatorHandler val, String idProceso, String fecha, String fileNames){
		
		try {
			LineNumberReader readerLines2 = null;
			File file=new File(path);
			if(file.exists()&&file.length()>0)
			{
				readerLines2 = new LineNumberReader(new FileReader(file));
				int counter=0;
				
				String lineLines2 =null;
				String args[] = null;
				
				while ((lineLines2 = readerLines2.readLine()) != null) 
				{
					System.out.println("processingLines2: " + lineLines2.length());
					if (counter % 2 > 0){
						System.out.println("counter2: " + counter);
						System.out.println("LINEA PROCESO: Inicia - " + lineLines2);
						System.out.println("transf: " + transf);
						args=lineLines2.split("\\|");
						if(args!=null&&args.length>=5)
						{	System.out.println("args[1]2: " + args[1].toString());
					    	byteStart = Long.parseLong(args[2]);
					    	byteEnd = Long.parseLong(args[3]);
					    	cont= Long.parseLong(args[4]);
					    	logger.info("Paso 2.- Procesando Estados de Cuenta (byte Inicio)"+byteStart);
					    	logger.info("Paso 2.- Procesando Estados de Cuenta (byte Inicio)"+byteEnd);
							this.processing(true, setF, lstTipoCambio, lstCampos22, 
									lstSeal, lstIva, transf, val, byteStart, byteEnd, args[1], cont, idProceso, fecha, fileNames);
						}
						System.out.println("LINEA PROCESO: Termina - " + lineLines2);
					}				
					counter+=1;
				}	
				readerLines2.close();
				file.delete();
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			logger.error(e.getLocalizedMessage().replace("ORA-", "ORACLE-"));
			System.exit(-1);
		}
		
	}
	
class Thread1 implements Runnable{
		
		String pathT;
		long byteStartT; 
		long byteEndT; 
		long contT; 
		HashMap<String, FiscalEntity> setFT;
		HashMap<String, HashMap> lstTipoCambioT; 
		HashMap<String, HashMap> lstCampos22T; 
		List<SealCertificate> lstSealT; 
		List<Iva> lstIvaT; 
		ValidatorHandler valT; 
		String idProcesoT; 
		String fechaT; 
		String fileNamesT;
		
		boolean flag;	
		
		Transformer transfT;
		
		private Logger logger = Logger.getLogger(InvoiceController.class);
		
		public Thread1(String path, long byteStart, long byteEnd, long cont,
				HashMap<String, FiscalEntity> setF, HashMap<String, HashMap> lstTipoCambio, HashMap<String, HashMap> lstCampos22, 
				List<SealCertificate> lstSeal, List<Iva> lstIva, ValidatorHandler val, String idProceso, String fecha, 
				String fileNames, boolean flag, Transformer transf) {
			// TODO Auto-generated constructor stub
			
			try{
				System.out.println("THREAD");			
											
				this.pathT = path;
				this.byteStartT = byteStart; 
				this.byteEndT = byteEnd; 
				this.contT = cont;				
				this.setFT = new HashMap<String, FiscalEntity>();
				this.setFT = setF;
				this.lstTipoCambioT = new HashMap<String, HashMap>();
				this.lstTipoCambioT = lstTipoCambio;
				this.lstCampos22T = new HashMap<String, HashMap>();
				this.lstCampos22T = lstCampos22;
				this.lstSealT = lstSeal; 
				this.lstIvaT = lstIva; 
				this.valT=val;			 
				this.idProcesoT = idProceso; 
				this.fechaT = fecha; 
				this.fileNamesT = fileNames;
				
				this.flag = flag;				
				
				this.transfT = transf;
				
			}catch(Exception ex){
				ex.printStackTrace();
				System.out.println(ex.getMessage());
			}			
		}
		@Override
		public synchronized  void run() {
			System.out.println("RUN");		
			
			// TODO Auto-generated method stub
			if (this.flag){				
				InvoiceController.this.processingLines(this.pathT, this.byteStartT,  this.byteEndT,  this.contT,
						this.setFT,  this.lstTipoCambioT, this.lstCampos22T, 
						this.lstSealT,  this.lstIvaT,  this.valT, this.idProcesoT, this.fechaT, this.fileNamesT);					
			}else{			
				InvoiceController.this.processingLines2(this.pathT, this.byteStartT,  this.byteEndT,  this.contT,
						this.setFT,  this.lstTipoCambioT, this.lstCampos22T, 
						this.lstSealT,  this.lstIvaT,  this.valT, this.idProcesoT, this.fechaT, this.fileNamesT);				
			}
		}
		
	}
	*/
	public void processing(File[] file, boolean isECB,
			HashMap<String, FiscalEntity> lstFiscal,
			HashMap<String, HashMap> lstTipoCambio,
			HashMap<String, HashMap> lstCampos22,
			List<SealCertificate> lstSeal, List<Iva> lstIva, 
			Transformer transf, ValidatorHandler val, String fileNames, String fecha, String idProceso, String urlWebService, String numeroMalla)
	throws Exception
	{
		boolean versionTypo = true; // Tipo version AMDA
		String[] fileNamesArr = fileNames.split(",");
		for (int i=0; i < fileNamesArr.length; i++)
		{	fileNamesArr[i] = fileNamesArr[i] + fecha + ".TXT"; }
		for (File objF : file) 
		{	
			// Revisa que venga en los argumentos
			//System.out.println(" ..archivo: " + objF.getName());
			boolean isOk = false;
			for (int y = 0; y < fileNamesArr.length; y++)
			{
				//System.out.println(" ....comparando con: " + fileNamesArr[y]);
				if(objF.getName().equals(fileNamesArr[y]))
				{
					isOk = true;
					break;
				}
			}
			if (!isOk)
			{	continue;	}
			
			if(objF.length()>0)
			{
				//logger.debug("Paso2.- El Archivo a procesar es: " + objF.getName());
				System.out.println("El Archivo a procesar es: " + objF.getName());
				if (isECB) 
				{
					if(!versionTypo){
						logger.debug("Path de ECB: " + properties.getPathDirProECB());
						xmlECB.setNombresApps(NombreAplicativo.cargaNombresApps());
						xmlECB.setNameFile(objF.getName());
						xmlECB.setLstFiscal(lstFiscal);
						xmlECB.setLstSeal(lstSeal);
						xmlECB.setTipoCambio(lstTipoCambio);
						xmlECB.setCampos22(lstCampos22);
						xmlECB.setTransf(transf);
						xmlECB.setValidator(val);
						xmlECB.convierte(idProceso, fecha, fileNames, numeroMalla);
					}else{
						System.out.println("Path de ECB V 3.3: " + properties.getPathDirProECB());
						xmlECBV3.setNombresApps(NombreAplicativo.cargaNombresApps());
						xmlECBV3.setNameFile(objF.getName());
						xmlECBV3.setLstFiscal(lstFiscal);
						xmlECBV3.setLstSeal(lstSeal);
						xmlECBV3.setTipoCambio(lstTipoCambio);
						xmlECBV3.setCampos22(lstCampos22);
						xmlECBV3.setTransf(transf);
						xmlECBV3.setValidator(val);
						xmlECBV3.convierte(idProceso, fecha, fileNames, numeroMalla);
					}
					
					
				} 
				else 
				{
					if(!versionTypo){
						logger.debug("Paso 2.- Path de CFD: " + properties.getPathDirProCFD());
						xmlCFD.setNombresApps(NombreAplicativo.cargaNombresApps());
						xmlCFD.setLstFiscal(lstFiscal);
						xmlCFD.setLstSeal(lstSeal);
						xmlCFD.setLstIva(lstIva);
						xmlCFD.setTipoCambio(lstTipoCambio);
						xmlCFD.setCampos22(lstCampos22);
						xmlCFD.setTransf(transf);
						xmlCFD.setValidator(val);
						xmlCFD.setUrlWebService(urlWebService);
						xmlCFD.convierte(objF.getName());
					}else{
						//logger.debug("Paso 2.- Path de CFD V 3.3: " + properties.getPathDirProCFD());
						System.out.println("Path de CFD V 3.3: \" + properties.getPathDirProCFD()");
						xmlCFDV3.setNombresApps(NombreAplicativo.cargaNombresApps());
						xmlCFDV3.setLstFiscal(lstFiscal);
						xmlCFDV3.setLstSeal(lstSeal);
						xmlCFDV3.setLstIva(lstIva);
						xmlCFDV3.setTipoCambio(lstTipoCambio);
						xmlCFDV3.setCampos22(lstCampos22);
						xmlCFDV3.setTransf(transf);
						xmlCFDV3.setValidator(val);
						xmlCFDV3.setUrlWebService(urlWebService);
						xmlCFDV3.convierte(objF.getName());
					}
						
					
				}
			}
		}
	}

	

	
	/**
	 * Procesa un CFD o ECB
	 * 
	 * @param isECB
	 * @param lstFiscal
	 * @param lstSeal
	 * @param lstIva
	 * @param transf
	 * @param val
	 * @param byteStart
	 * @param byteEnd
	 * @param path
	 * @param cont
	 */
	public void processing(boolean isECB,
			HashMap<String, FiscalEntity> lstFiscal,
			HashMap<String, HashMap> lstTipoCambio,
			HashMap<String, HashMap> lstCampos22,
			List<SealCertificate> lstSeal, List<Iva> lstIva, 
			Transformer transf, ValidatorHandler val, 
			long byteStart, long byteEnd, String path,long cont, String idProceso, String fecha, String fileNames, String urlWebService, String numeroMalla) throws Exception
	{
		File file=new File(path);
		boolean versionTypo = true; // Tipo version AMDA
		if(file.length()>0)
		{
			logger.debug("El archivo a procesar es: " + path);
			if (isECB) 
				
			{
				if(!versionTypo){
					logger.debug("Procesando Estados de Cuenta");
					xmlECB.setNombresApps(NombreAplicativo.cargaNombresApps());
					xmlECB.setNameFile(file.getName());
					xmlECB.setLstFiscal(lstFiscal);
					xmlECB.setLstSeal(lstSeal);
					xmlECB.setCampos22(lstCampos22);
					xmlECB.setTipoCambio(lstTipoCambio);
					xmlECB.setTransf(transf);
					xmlECB.setValidator(val);
					xmlECB.setUrlWebService(urlWebService);
					xmlECB.convierte(byteStart, byteEnd, path, cont, idProceso, fecha, fileNames, numeroMalla);
				}else{
					System.out.println("Procesando Estados de Cuenta V3.3");
					xmlECBV3.setNombresApps(NombreAplicativo.cargaNombresApps());
					xmlECBV3.setNameFile(file.getName());
					xmlECBV3.setLstFiscal(lstFiscal);
					xmlECBV3.setLstSeal(lstSeal);
					xmlECBV3.setCampos22(lstCampos22);
					xmlECBV3.setTipoCambio(lstTipoCambio);
					xmlECBV3.setTransf(transf);
					xmlECBV3.setValidator(val);
					xmlECBV3.setUrlWebService(urlWebService);
					xmlECBV3.convierte(byteStart, byteEnd, path, cont, idProceso, fecha, fileNames, numeroMalla);
				}
				
							
			} 
			else 
			{
				if(!versionTypo){
					logger.debug("Paso 2.- Procesando Facturas");
					xmlCFD.setNombresApps(NombreAplicativo.cargaNombresApps());
					//xmlCFD.setNameFile(file.getName());
					xmlCFD.setLstFiscal(lstFiscal);
					xmlCFD.setLstSeal(lstSeal);
					xmlCFD.setLstIva(lstIva);
					xmlCFD.setTipoCambio(lstTipoCambio);
					xmlCFD.setCampos22(lstCampos22);
					xmlCFD.setTransf(transf);
					xmlCFD.setValidator(val);
				}else{
					logger.debug("Paso 2.- Procesando Facturas V3.3 AMDA");
					xmlCFDV3.setNombresApps(NombreAplicativo.cargaNombresApps());
					//xmlCFD.setNameFile(file.getName());
					xmlCFDV3.setLstFiscal(lstFiscal);
					xmlCFDV3.setLstSeal(lstSeal);
					xmlCFDV3.setLstIva(lstIva);
					xmlCFDV3.setTipoCambio(lstTipoCambio);
					xmlCFDV3.setCampos22(lstCampos22);
					xmlCFDV3.setTransf(transf);
					xmlCFDV3.setValidator(val);
				}
				
				
			}
		}
	}
	
	/**
	 * Obtiene si el proceso esta en 
	 * ACTIVO(1) o INACTIVO(0)
	 * @return
	 */
	public boolean getBandera() {
		boolean flag = false;
		logger.debug("Leyendo bandera");
		String linea = "";

		try {
			flagR = new BufferedReader(new FileReader(
					properties.getPathFlag()));
			while ((linea = flagR.readLine()) != null) {
				if (linea.equals("1")) {
					flag = true;
				}
			}
			
		} catch (FileNotFoundException e) {
			startFlag();
			flag = true;
			logger.error(e.getLocalizedMessage(), e);
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage(), e);
		} finally {
			try {
				flagR.close();
			} catch (IOException e) {
				logger.error(e.getLocalizedMessage(),e);
			}
		}

		return flag;
	}

	/**
	 * Inicializa la bandera en ACTIVO(1)
	 */
	public void startFlag() {
		PrintWriter fileOut =null;
		try {
			File f = new File(properties.getPathFlag());
			fileOut = new PrintWriter(new FileWriter(f));
			fileOut.println("1");
			fileOut.close();
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage(), e);
		} finally{
			if(fileOut!=null){
				fileOut.close();
			}
		}
	}

	/**
	 * Crea un validador con los esquemas sigutentes:<br>
	 * <li>General</li>
	 * <li>Addenda</li> 
	 * <li>Estado de Cuenta</li><br>
	 * @return
	 * @throws SAXException
	 */
	private ValidatorHandler createValidatorHandler() throws SAXException {
		String path[] = { properties.getPathFileValidation(),
				properties.getPathFileValidationECB(),
				properties.getPathFileValidaationADD(),
				properties.getPathComplementoTerceros33()};
		SchemaFactory schemaFactory = SchemaFactory
				.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);// (XMLConstants.XML_NS_URI)
		StreamSource[] schemas = new StreamSource[path.length];

		for (int i = 0; i < path.length; i++) {
			schemas[i] = new StreamSource(new File(path[i]));
			logger.info("Esquemas: " + path[i]);
		}
		Schema schema = schemaFactory.newSchema(schemas);
		return schema.newValidatorHandler();
	}

		
}