package com.interfactura.firmalocal.xml.ecb;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.ValidatorHandler;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.interfactura.firmalocal.domain.entities.CFDIssued;
import com.interfactura.firmalocal.domain.entities.CFDIssuedIn;
import com.interfactura.firmalocal.domain.entities.FiscalEntity;
import com.interfactura.firmalocal.domain.entities.SealCertificate;
import com.interfactura.firmalocal.xml.Properties;
import com.interfactura.firmalocal.xml.WebServiceCliente;
import com.interfactura.firmalocal.xml.file.XMLProcess;
import com.interfactura.firmalocal.xml.util.NombreAplicativo;
import com.interfactura.firmalocal.xml.util.Util;
import com.interfactura.firmalocal.xml.util.UtilCatalogos;

@Component
public class GeneraXML_ECBDSV3_3 {
	private Logger logger = Logger.getLogger(GeneraXML_ECBDSV3_3.class);
	private BufferedReader br;
	private String linea;
	private String token;
	@Autowired
	private ConvertirV3_3 conver; 
	private int cont;
	@Autowired
	private Properties properties;
	private ByteArrayOutputStream out;
	private FileOutputStream salida;
	private FileOutputStream salidaBD;
	private FileOutputStream salidaODM;
	private FileOutputStream incidencia;
	private FileOutputStream incidenciaCifras;
	private long offSetComprobante = 0;
	@Autowired
	private XMLProcess xmlProcess;
	private Transformer transf;
	private String temp;
	private boolean flagProcesado;
	private File file;
	private String nameFile;
	private FiscalEntity fiscal;
	private SealCertificate certificate;
	private long contCFD;
	private long contCFD2;
	private String startLine;
	private String endLine;
	private HashMap<String, FiscalEntity> lstFiscal;
	private HashMap<String, HashMap> campos22;
	private HashMap<String, HashMap> tipoCambio;
	private List<SealCertificate> lstSeal;
	private ValidatorHandler validator;
	private int sizeT = 255;
	private List<CFDIssuedIn> lstECBIncidence;
	private List<CFDIssued> lstECB;
	private String msgError;
	private boolean processStarted = false;

	private WebServiceCliente servicePort = null;
	private DocumentBuilderFactory dbf = null;
	private DocumentBuilder db = null;
	private Transformer tx = null;
	
	private String urlWebService = null;
	
    private List<MovimientoECB> lstMovimientosECB = new ArrayList<MovimientoECB>();						
	private EstadoDeCuentaBancario estadoDeCuentaBancario = new EstadoDeCuentaBancario();
	private Queue<String> addendaDomiciliosNodeStr = new LinkedList<String>();
		
	//Atributos TimbreFiscalDigital
    private String strFechaTimbrado = "";
    private String strUUID = "";
    private String strNoCertificadoSAT = "";
    private String strSelloCFD = "";
    private String strSelloSAT = "";
    private String strVersion = "";				            
    
    //RFC del Emisor y Receptor
    private String strEmisorRFC = "";
    private String strReceptorRFC = "";
    
    //Total del xml timbrado
    private String strTotal = "";
	/**
	 * 
	 */
    //Lista de objetos ECB
    private List<ECB> lstObjECBs = new ArrayList<ECB>();
    private HashMap<String, String> catalogoCincoCampos33 = new HashMap<String, String>();
    
    private boolean fAttMovIncorrect = false;
    
    //Banderas para validar Santander:EstadoDeCuentaBancario
    private boolean fnumeroCuenta = false;
    private boolean fnombreCliente = false;
    private boolean fperiodo = false;
    private boolean fsucursal = false;
    
    //Banderas para comprobar que existan algunos atributos de Santander:EstadoDeCuentaBancario
    private boolean existNumeroCuenta = false;
    private boolean existNombreCliente = false;
    private boolean existPeriodo = false;
       
  //Banderas para comprobar que existan algunos atributos de Santander:Movimientos
    private boolean existFecha = false;
    private boolean existDescripcion = false;
    private boolean existImporte = false;

    //Nombres de los Aplicativos ECB
    private HashMap<String, String> nombresApps = new HashMap<String, String>();
    
    //Addenda  recompensas 
    private FileInputStream fileRecompensa;
    
//    NewAddenda bet
    private boolean dobleAddenda = false;
	private boolean starnewAddenda;
	private List<Operacion> lstOperaciones = new ArrayList<Operacion>();
	private Operacion complementarios;
	private List<Cobranza> lstCobranzas = new ArrayList<Cobranza>();
	private boolean attDobleAddIncorrect = false;
	private boolean endOper = false;
	private boolean contOper = false;
	private boolean contCobr = false;
	
    
   	public GeneraXML_ECBDSV3_3() {

	}

	/**
	 * 
	 * @param nameFile
	 */
	public GeneraXML_ECBDSV3_3(String nameFile) {
		this.nameFile = nameFile;
	}

	/**
	 * Proceso de generacion de ECB para un solo archivo de entrada
	 * 
	 * @return
	 */
	public boolean convierte(String idProceso, String fecha, String fileNames, String numeroMalla)
	{
		try 
		{
			flagProcesado = true;
			// Se crea el archivo de salida
			File fileExit = new File(this.getNameFile(properties.getPathSalida(), -1, "XML", idProceso));
			File fileExitBD = new File(this.getNameFile(properties.getPathSalida(), -1, "BD", idProceso));
			
			this.salida = new FileOutputStream(fileExit);
			this.salidaBD = new FileOutputStream(fileExitBD);
			File fileExitODM = new File(properties.getPathDirGenr() + File.separator + fecha + "ODM-" + idProceso);
			if (!this.processStarted)
			{	
				this.salidaODM = new FileOutputStream(fileExitODM);
				this.processStarted = true;
			}
			else
			{	
				this.salidaODM = new FileOutputStream(fileExitODM, true);
			}
			// Se crea el archivo de incidencias
			File fileIncidence=new File(this.getNameFile(properties.getPathIncidencia(), -1, "INC", idProceso));
			
			//Se crea el archivo de incindencias para cifras control
			File fileIncidenceCifras=new File(this.getNameFile(properties.getPathIncidencia(), -1, "ERR", idProceso));
			
			//fileIncidence.setReadable(true,true);
			this.incidencia = new FileOutputStream(fileIncidence);

			this.incidenciaCifras = new FileOutputStream(fileIncidenceCifras);
			
			file = new File(properties.getPathDirProECB() + this.nameFile);
			br = new BufferedReader(new FileReader(file));
			contCFD = 0;
			this.begin();
			lstECB = new ArrayList<CFDIssued>();
			lstECBIncidence = new ArrayList<CFDIssuedIn>();
			//Copia el archivo antes de procesarlo
			copy(this.getNameFile(properties.getPathDirBackup(), -1, null, idProceso));
			logger.info("Inicio de lectura del archivo");
			//Empieza a procesar el archivo
			while ((linea = br.readLine()) != null) 
			{
				contCFD += 1;
				if (!linea.startsWith(";")&&linea.length()>0) 
				{	this.formatLinea(idProceso, fecha, fileNames, numeroMalla);	}
				//logger.debug("Numero de linea: "+contCFD+" "+this.nameFile + " . . . " + linea);
			}
			logger.info("Fin de lectura del archivo");
			this.endMOVIMIENTOS();
			this.endCOMPLEMENTO();			
			System.out.println("filenamesContabilizar:" + fileNames);
			
			this.end(0, idProceso, fecha, fileNames, numeroMalla);
			//Manda a guardar los ultimos ECB
			if (lstECB != null && lstECB.size() > 0) {
				conver.getTags().cFDIssuedManager.update(lstECB);
				lstECB = null;
			}
			
			//Manda a guardar las incidencias
			if (lstECBIncidence != null && lstECBIncidence.size() > 0) 
			{	conver.getTags().cFDIssuedIncidenceManager.update(lstECBIncidence);		}
		} 
		catch (IOException ioe) 
		{
			logger.error("Ocurrio un error inesperado", ioe);
			flagProcesado = false;
		} 
		finally 
		{
			lstECBIncidence = null;
			this.close();
		}

		return flagProcesado;
	}

	
	
	
	/*Utilizado
	 * 
	 * 
	 * 
	 */
	/**
	 * Procesa una parte de un archivo en especifico
	 * 
	 * @param byteStart
	 * @param byteEnd
	 * @param path
	 * @param cont
	 * @return
	 */
	public boolean convierte(long byteStart, long byteEnd, String path, long cont, String idProceso, String fecha, String fileNames, String numeroMalla) 
	{
		//System.out.println("convierte");
		//System.out.println("LINEA PROCESO: Inicia bloque - " + cont + "," + byteStart + "," + byteEnd + "," + idProceso);
		System.out.println("Inicia proceso de Estados de cuenta");
		flagProcesado = true;	
		//Cadena que contiene la linea leida del archivo
		//StringBuilder linea = new StringBuilder();
		StringBuffer linea = new StringBuffer();
		//Bandera que indica si se termino de leer los ECB completos
		boolean flagEnd = false;
		//Bandera que indica si se procesa la linea actual
		boolean procesa = false;
		//Bandera que indica si se termina el ciclo de lectura
		boolean activo = false;
		//Bandera que indica si sigue leyendo
		boolean activo2 = false;
		int i = 0;
		char c=0;
		byte[] array = null;
		RandomAccessFile file=null;

		//System.out.println("anteslstECB y lstECBIncidence");
		lstECB = new ArrayList<CFDIssued>();
		lstECBIncidence = new ArrayList<CFDIssuedIn>();
		//System.out.println("despueslstECB y lstECBIncidence");
		try 
		{
			this.file=new File(path);
			this.nameFile = this.file.getName();
			 
			// Se crea el archivo de salida
			System.out.println("Creando archivos de salida " );
			//logger.debug("Paso 3.- Creando archivo de salida byte de final: " + byteEnd);
			File fileInputREC = new File(this.getName(properties.getPathSalida(), "REC"));
			if (fileInputREC.exists())
				this.fileRecompensa = new FileInputStream(fileInputREC);
			File fileExit=new File(this.getNameFile(properties.getPathSalida(), cont,"XML", idProceso));
			this.salida = new FileOutputStream(fileExit);
			File fileExitBD=new File(this.getNameFile(properties.getPathSalida(), cont,"BD", idProceso));
			this.salidaBD = new FileOutputStream(fileExitBD);
			File fileExitODM = new File(properties.getPathDirGenr() + File.separator + fecha + "ODM-" + idProceso);
			if (!this.processStarted)
			{	
				this.salidaODM = new FileOutputStream(fileExitODM);
				this.processStarted = true;
			}
			else
			{	
				this.salidaODM = new FileOutputStream(fileExitODM, true);	
			}
			// Se crea el archivo de incidencias
			System.out.println("Creando archivo de incidencia");
			//logger.debug("Paso 3.- Creando archivo de incidencia byte de final: "+byteEnd);
			File fileIncidence=new File(this.getNameFile(properties.getPathIncidencia(), cont,"INC", idProceso));
			this.incidencia = new FileOutputStream(fileIncidence);
			
			//Se crea el archivo de incidencias para cifras control
			System.out.println("Creando archivo de incidencia cifras control");
			//logger.debug("Paso 3.- Creando archivo de incidencia cifras control byte de final: "+byteEnd);
			File fileIncidenceCifras=new File(this.getNameFile(properties.getPathIncidencia(), cont,"ERR", idProceso));
			this.incidenciaCifras = new FileOutputStream(fileIncidenceCifras);
			
			System.out.println("BackUp de la parte del archivo");
			copy(byteStart, byteEnd, path, cont, idProceso);
			file = new RandomAccessFile(path, "r");
			int sizeArray = 1024 * 8;
			long byteEndLine = 10;
					
			contCFD = byteStart;
			System.out.println("Comienza el formateo de las lineas");
			this.begin();
			do 
			{
				file.seek(byteStart);
				array = new byte[sizeArray];
				file.read(array, 0, (sizeArray - 1));
				i = 0;
				while (((c = (char) (array[i] & 0xFF) ) != 0)) 
				{
					i++;
					byteStart++;
					//Pregunta si llego al fin de linea
					if (c == byteEndLine) 
					{
						// Si no empieza con ';' se procesa
						if (!linea.toString().startsWith(";")
								&& linea.toString().length() > 0) 
						{
							//Pregunta si empieza con 01
							if (linea.toString().startsWith("01")) 
							{	
								procesa = true;
								if (activo) 
								{
									flagEnd = true;
									break;
								}
								if (activo2) 
								{
									activo = true;
									activo2 = false;
								}
							}
							//Procesa la linea
							if (procesa) 
							{								
								this.linea = new String(linea.toString().getBytes("UTF-8"), "UTF-8");
								this.formatLinea(idProceso, fecha, fileNames, numeroMalla);							
							}
						}
						//linea = new StringBuilder();
						linea = new StringBuffer();
						contCFD = byteStart;
					} 
					else 
					{
						if (c != 13) 
						{	linea.append(c);	}
					}

					//Pregunta si ya se terminaron de leer los bytes establecidos
					if (byteStart == byteEnd) 
					{
						flagEnd = true;
						break;
					}
				}

				if (flagEnd && (!activo)) 
				{
					if (linea.toString().startsWith("01")) 
					{	 activo2 = true;		} 
					else 
					{	activo = true;		}
					flagEnd = false;
				} 
				else if (activo) 
				{
					if (linea.toString().startsWith("01")) 
					{	flagEnd = true;		}
				}
				if (array[0] == 0) 
				{	flagEnd = true;		}
			} while (!flagEnd);
			System.out.println("Finaliza el formateo de las lineas");
			
			// Problema de incidencia duplicada de entidad fiscal no existente
			if((linea.toString().length()>0)&&(!activo))
			{
				this.linea = linea.toString();
				this.formatLinea(idProceso, fecha, fileNames, numeroMalla);	
			}
			if ( !this.dobleAddenda ) {
				this.endMOVIMIENTOS();
				this.endADDENDA();
			}
			else
				this.endAddendaECB();
			//System.out.println("filenamesContabilizar:" + fileNames);
			System.out.println("Inicia cerrado y verificacion de XML");
			this.end(0, idProceso, fecha, fileNames, numeroMalla);
			System.out.println("Termina cerrado y verificacion de XML");
			
			System.out.println("Inicia Actualizacion de DB");
			if (lstECB.size() > 0) 
			{
				try
				{	conver.getTags().cFDIssuedManager.update(lstECB);	}
				catch (Exception e1)
				{
					String ids = "";
					for (int j = 0; j < lstECB.size(); j++)
					{
						CFDIssued cf = lstECB.get(j);
						ids += cf.getId() + "-" + cf.getFolio() + "|";
					}
					System.out.println("BD: Reintentando ids " + ids);
					try
					{	
						System.out.println("BD: Intento 2");
						conver.getTags().cFDIssuedManager.update(lstECB);	
					}
					catch (Exception e2)
					{
						try
						{	
							System.out.println("BD: Intento 3");
							conver.getTags().cFDIssuedManager.update(lstECB);	
						}
						catch (Exception e3)
						{	
							try
							{	
								System.out.println("BD: Intento 4");
								conver.getTags().cFDIssuedManager.update(lstECB);	
							}
							catch (Exception e4)
							{	
								try
								{	
									System.out.println("BD: Intento 5");
									conver.getTags().cFDIssuedManager.update(lstECB);	
								}
								catch (Exception e5)
								{	
									try
									{	
										System.out.println("BD: Intento 6");
										conver.getTags().cFDIssuedManager.update(lstECB);	
									}
									catch (Exception e6)
									{	
										try
										{	
											System.out.println("BD: Intento 7");
											conver.getTags().cFDIssuedManager.update(lstECB);	
										}
										catch (Exception e7)
										{	
											try
											{
												System.out.println("BD: Intento 8");
												conver.getTags().cFDIssuedManager.update(lstECB);	
											}
											catch (Exception e8)
											{	
												try
												{	
													System.out.println("BD: Intento 9");
													conver.getTags().cFDIssuedManager.update(lstECB);	
												}
												catch (Exception e9)
												{	
													try
													{	
														System.out.println("BD: Intento 10");
														conver.getTags().cFDIssuedManager.update(lstECB);	
													}
													catch (Exception e10)
													{	
														try
														{	
															System.out.println("BD: Intento 11");
															conver.getTags().cFDIssuedManager.update(lstECB);	
														}
														catch (Exception e11)
														{																
															try
															{	
																System.out.println("BD: Intento 12");
																conver.getTags().cFDIssuedManager.update(lstECB);	
															}
															catch (Exception e12)
															{										
																try
																{	
																	System.out.println("BD: Intento 13");
																	conver.getTags().cFDIssuedManager.update(lstECB);	
																}	
																catch (Exception e13)
																{	
																	try
																	{	
																		System.out.println("BD: Intento 14");
																		conver.getTags().cFDIssuedManager.update(lstECB);	
																	}
																	catch (Exception e14)
																	{	
																		System.out.println("BD: Intento 15");
																		conver.getTags().cFDIssuedManager.update(lstECB);
																	}
																}
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
				lstECB = new ArrayList<CFDIssued>();
			}
			System.out.println("Termina Actualizacion de DB");
		} 
		catch (FileNotFoundException e) 
		{
			flagProcesado = false;
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			flagProcesado = false;
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			flagProcesado = false;
			e.printStackTrace();
		} 
		finally 
		{
			if(file!=null)
			{
				try 
				{	file.close();		} 
				catch (IOException e) 
				{	logger.error("No se pudo cerrar el archivo ",e);	}
			}
			this.linea = null;
			//this.closeByte();
		}
		/*DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date dateFin = new Date();
		System.out.println("TIMEFIN:" + dateFormat.format(dateFin) + "M" + System.currentTimeMillis());
		*/
		
		/*
		 *Enviar bloque a timbrar 
		 */
		StringBuffer sbXmlATimbrar = new StringBuffer();
		StringBuffer sbPeriodos = new StringBuffer();
		StringBuffer sbNombresAplicativo = new StringBuffer();
		System.out.println("Enviando bloque a timbrar");
		long t1;
		long t2;
		String timbrados = null;
		try{
			
			//Instalar certificados
			/*System.setProperty("javax.net.ssl.keyStoreType", "PKCS12");
			System.setProperty("javax.net.ssl.keyStore", properties.getCertificadoSantander());
			System.setProperty("javax.net.ssl.keyStorePassword", properties.getCertificadoPass());													
			System.setProperty("javax.net.ssl.trustStore", properties.getCertificadoInterfactura());
			*/
			//Iniciar conexion con WebService
			
			if(this.servicePort == null){
				this.servicePort = new WebServiceCliente();								
			}			
			
			t1 = System.currentTimeMillis();
			System.out.println("ANTES DE ENTRAR TIMBRAR SACANDO VALORES: "+lstObjECBs.size());						
			for(int index=0; index<lstObjECBs.size(); index++){
				//System.out.println("xmlATimbrar " + index + " :" + lstObjECBs.get(index).getXmlSinECB().toString("UTF-8"));
				//System.out.println("periodos " + index + " :" + lstObjECBs.get(index).getTagEMISION_PERIODO());
				//System.out.println("nombreAplicativo" + index + " :" + lstObjECBs.get(index).getTagNOMBRE_APP_REPECB());
				if(index < lstObjECBs.size()-1){
					sbXmlATimbrar.append(lstObjECBs.get(index).getXmlSinECB().toString("UTF-8") + "|");		
					sbPeriodos.append(lstObjECBs.get(index).getTagEMISION_PERIODO() + "|");
					sbNombresAplicativo.append(lstObjECBs.get(index).getTagNOMBRE_APP_REPECB() + "|");
				}else{
					sbXmlATimbrar.append(lstObjECBs.get(index).getXmlSinECB().toString("UTF-8"));
					sbPeriodos.append(lstObjECBs.get(index).getTagEMISION_PERIODO());
					sbNombresAplicativo.append(lstObjECBs.get(index).getTagNOMBRE_APP_REPECB());
				}
			}								
			
			t2 = t1- System.currentTimeMillis();
			System.out.println("TIME: Unir XMLs a Timbrar:" + t2 + " ms");
			
			//System.out.println("Bloque de XMLs a timbrar: " + sbXmlATimbrar.toString());
			//System.out.println("NombreInterface:" +  this.nameFile);
			//System.out.println("NumeroProceso:" + idProceso);
			
			if(!sbXmlATimbrar.toString().trim().equals("")){ // Aqui se timbra al parecer AMDA
				//System.out.println("Num xmls A Timbrar:" + lstObjECBs.size());
				
				t1 = System.currentTimeMillis();
				timbrados = "";
				System.out.println("EnvioWebService" + sbXmlATimbrar.toString());
				timbrados = this.servicePort.generaTimbre(sbXmlATimbrar.toString(), false, this.urlWebService, properties, this.nameFile, Integer.parseInt(idProceso), 0, sbPeriodos.toString(), sbNombresAplicativo.toString());
//				System.out.println("Respuesta Timbrado:" + timbrados);
				t2 =  System.currentTimeMillis() - t1;
				System.out.println("TIME: Timbrado:" + t2 + " ms - contador: " + lstObjECBs.size());
				System.out.println("Timbrado del bloque " + cont + " terminado.");
				//System.out.println("Bloque de XMLs timbrados por Interfactura: " + timbrados);

				String [] xmlsTimbrados = timbrados.split("\\|");
				//System.out.println("Num xmls Timbrados:" + xmlsTimbrados.length);
				
				t1 = System.currentTimeMillis();
				StringBuffer sbFoliosSAT = new StringBuffer();
				for(int index=0; index<xmlsTimbrados.length; index++){
					lstObjECBs.get(index).setDomResultado(stringToDocument(xmlsTimbrados[index]));
					//Obtenemos la etiqueta raiz (Resultado)
					Element docEleResultado = lstObjECBs.get(index).getDomResultado().getDocumentElement();
					
					String descripcion = docEleResultado.getAttribute("Descripcion");
					String idRespuesta = docEleResultado.getAttribute("IdRespuesta");
					
					if(descripcion.toLowerCase().trim().equals("ok") && idRespuesta.trim().equals("1")){
						//Obtenemos la etiqueta raiz (Comprobante)								
						//Element docEleComprobante = domComprobante.getDocumentElement();
						Element docEleComprobante = (Element) docEleResultado.getFirstChild();
						
						if(index < xmlsTimbrados.length-1){
							sbFoliosSAT.append(this.getFolioSAT(docEleComprobante, lstObjECBs.get(index).getDomResultado()) + ",");
						}else{
							sbFoliosSAT.append(this.getFolioSAT(docEleComprobante, lstObjECBs.get(index).getDomResultado()));
						}					
					}
				}
				
				//System.out.println("Bloque: " + cont);
				//System.out.println("Folios-SAT timbrados: " + sbFoliosSAT.toString());
				if(catalogoCincoCampos33.isEmpty()) {
					fillCatalogoCincoCampos33();
				}
				
				for(int index=0; index<xmlsTimbrados.length; index++){
					//System.out.println("xmlTimbrado " + index + " :" + xmlsTimbrados[index]);
					//Convertir xmlTimbrado a objeto Document						
					//Document domResultado = stringToDocument(xmlsTimbrados[index]);
				
					//Obtenemos la etiqueta raiz (Resultado)
					//Element docEleResultado = domResultado.getDocumentElement();
					Element docEleResultado = lstObjECBs.get(index).getDomResultado().getDocumentElement();
					
					String descripcion = docEleResultado.getAttribute("Descripcion");
					String idRespuesta = docEleResultado.getAttribute("IdRespuesta");
					
					if(descripcion.toLowerCase().trim().equals("ok") && idRespuesta.trim().equals("1")){
						//Transformar el hijo del nodo Resultado (Comprobante) a StreamResult								
						//StreamResult resultComprobante = nodeToStreamResult(docEleResultado.getFirstChild());
						
						//Obtenemos la etiqueta raiz (Comprobante)								
						//Element docEleComprobante = domComprobante.getDocumentElement();
						Element docEleComprobante = (Element) docEleResultado.getFirstChild();
						//Atributos TimbreFiscalDigital
			            this.strFechaTimbrado = "";this.strUUID = "";this.strNoCertificadoSAT = "";this.strSelloCFD = "";this.strSelloSAT = "";this.strVersion = "";  
			            
			            //RFC del Emisor y Receptor
			            this.strEmisorRFC = "";this.strReceptorRFC = "";
			            
			            //Total del xml timbrado
			            this.strTotal = "";
			            
			            //System.out.println("docEleComprobante: " + docEleComprobante.toString());
			            //System.out.println("AntesdeAgregarMovimientosECB: " + lstObjECBs.get(index).getDomResultado().toString());
			            
						//domResultado = this.putMovimientoECB(docEleComprobante, domResultado);
			            lstObjECBs.get(index).setDomResultado(this.putMovimientoECB(docEleComprobante, lstObjECBs.get(index).getDomResultado(), lstObjECBs.get(index).getEstadoDeCuentaBancario(), lstObjECBs.get(index).getLstMovimientosECB(), lstObjECBs.get(index).getLstOperacionesECB(), lstObjECBs.get(index).getLstCobranzaECB(), lstObjECBs.get(index).isAddendaNew(), lstObjECBs.get(index).getOperacion()));
						//Concatenar foliosSAT
						sbFoliosSAT.append(this.strUUID + "||");
						
						//Transformar el hijo del nodo Resultado (document Comprobante) a StringResult								
						//StreamResult resultComprobanteTimbrado = this.documentToStreamResult(domComprobante);
						
						//Obtenemos la etiqueta raiz (Resultado con MovimientosECB) 								
						//Element docEleResultadoConMovimientosECB = domResultado.getDocumentElement();
						Element docEleResultadoConMovimientosECB = lstObjECBs.get(index).getDomResultado().getDocumentElement();
						
						StreamResult resultComprobanteTimbrado = this.nodeToStreamResult(docEleResultadoConMovimientosECB.getFirstChild());
										
						StringBuffer sbTimbradoFinal = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + resultComprobanteTimbrado.getWriter().toString().replaceAll("[\n\r]", ""));
						
						//System.out.println("XML Timbrado con addenda incluida: " + sbTimbradoFinal.toString());
						long length = sbTimbradoFinal.toString().getBytes("UTF-8").length;
						this.salidaODM.write(sbTimbradoFinal.toString().getBytes("UTF-8"));
						//System.out.println("Bloque AMDA 3 : " + sbTimbradoFinal.toString());
						//Route route = new Route();
						String routeName = properties.getPathDirGenr() + File.separator + fecha + "ODM-" + idProceso;
						
						//17FEB2012 --- Cambio optimizacion Oracle
						//route.setRoute(routeName + "|" + this.offSetComprobante + "|" + (this.offSetComprobante + length));
						//conver.getTags().cfd.setFilePath(route);
						conver.getTags().cfd.setXmlRoute(routeName + "|" + this.offSetComprobante + "|" + (this.offSetComprobante + length));
						conver.getTags().cfd.setProcessID(idProceso);
						
						String strTotalZeros = putZeros(this.strTotal);
										
						this.fileSALIDA(lstObjECBs.get(index), fileNames, 
								this.strUUID, this.strFechaTimbrado, this.strNoCertificadoSAT, this.strSelloCFD, this.strSelloSAT,
								this.strVersion, this.strEmisorRFC, this.strReceptorRFC, strTotalZeros);
						this.offSetComprobante += length;
					}else{
						System.out.println("ERROR: " + descripcion + " " + idRespuesta);
									
						fileINCIDENCIA(idRespuesta + "-" + descripcion + " ", "ERROR", 
								lstObjECBs.get(index).getTagEMISON_RFC(), lstObjECBs.get(index).getTagNUM_CTE(), lstObjECBs.get(index).getTagNUM_CTA(), lstObjECBs.get(index).getTagEMISION_PERIODO(), lstObjECBs.get(index).getTagNUM_TARJETA(), lstObjECBs.get(index).getTagCFD_TYPE());				
						
						//Generar Archivo de incidentes para Cifras (ERR...TXT)
						fileINCIDENCIACIFRAS(idRespuesta + "-" + descripcion + " ", "ERROR", 
								lstObjECBs.get(index).getTagEMISON_RFC(), lstObjECBs.get(index).getTagNUM_CTE(), lstObjECBs.get(index).getTagNUM_CTA(), lstObjECBs.get(index).getTagEMISION_PERIODO(), lstObjECBs.get(index).getTagNUM_TARJETA(), lstObjECBs.get(index).getTagCFD_TYPE(), 
								lstObjECBs.get(index).getTagSUBTOTAL_MN(), lstObjECBs.get(index).getTagTOTAL_IMP_TRA(), lstObjECBs.get(index).getTagTOTAL_IMP_RET(), fileNames, lstObjECBs.get(index).getTagSERIE_FISCAL_CFD(), numeroMalla);
					}
				}
				this.lstObjECBs.clear();
				this.catalogoCincoCampos33.clear();
				t2 = t1- System.currentTimeMillis();
				System.out.println("TIME: Procesar timbrados: " + t2 + " - Bloque: " + cont);
				System.out.println("FoliosSAT del Bloque " + cont + " " + sbFoliosSAT.toString());
				
			}
			
		}catch(Exception e){
			e.printStackTrace();
			System.out.println(e.getMessage());
		}finally{
			this.closeByte();
		}
			
		System.out.println("LINEA PROCESO: Fin bloque - " + cont + "," + byteStart + "," + byteEnd + "," + idProceso);
		return flagProcesado;
	}

	/**
	 * Copia el archivo en el path indicado
	 * @param path
	 * @throws IOException
	 */
	private void copy(String path) throws IOException 
	{
		/*DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date dateInicio = new Date();
		System.out.println("TIMEINICIOCOPY:" + dateFormat.format(dateInicio) + " M" + System.currentTimeMillis());
		*/
		long t1 = System.currentTimeMillis();
		FileCopyUtils.copy(file, new File(path));
		long t2 = t1- System.currentTimeMillis();
		System.out.println("TIME: Para copiar archivo a " + path + t2 + " ms");
		/*		
		Date dateInicio2 = new Date();
		System.out.println("TIMEFINALCOPY:" + dateFormat.format(dateInicio2) + " M" + System.currentTimeMillis());
		*/
	}
	
	/**
	 * 
	 * @param path
	 * @param cont
	 * @param prefix
	 * @return
	 */
	private String getNameFile(String path,long cont, String prefix, String idProceso)
	{
		String nameProcess[] = this.nameFile.split("\\.");
		String time=null;	
		if(File.separatorChar=='/')
		{	time=Util.convertirFecha(Calendar.getInstance().getTime(), "HH:mm:ss");		} 
		else 
		{	time=Util.convertirFecha(Calendar.getInstance().getTime(), "HHmmss");	}
		
		if(prefix==null)
		{
			if(cont==-1)
			{	return path+nameProcess[0]+"T"+time+ "."+ nameProcess[1];	} 
			else 
			{	return path+nameProcess[0]+ "_" + cont + "_" + idProceso + "."+ nameProcess[1];	}
		} 
		else 
		{
			if(cont==-1)
			{	return path+prefix+nameProcess[0].substring(3,nameProcess[0].length())+"." + nameProcess[1];	} 
			else 
			{	return path+prefix+nameProcess[0].substring(3,nameProcess[0].length())+ "_" + cont + "_" + idProceso +  "." + nameProcess[1];	}
		}
	}
	
	private String getName(String path, String prefix) {
		String nameProcess[] = this.nameFile.split("\\.");
		return path+prefix+nameProcess[0].substring(3,nameProcess[0].length())+ "." + nameProcess[1];
	}

	/**
	 * Copia desde un byte de inicio hasta uno final y el nombre del archivo
	 * queda de la siguiente manera 
	 * <Nombre del Archivo>_<consecutivo>.<extension>
	 * @param byteStart
	 * @param byteEnd
	 * @param path
	 * @param cont
	 * @throws IOException
	 */
	private void copy(long byteStart, long byteEnd, String path, long cont, String idProceso)
			throws IOException 
	{/*
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date dateInicio = new Date();
		System.out.println("TIMEINICIOCopiando archivo:" + dateFormat.format(dateInicio) + " M" + System.currentTimeMillis());
		*/
		if(byteStart == byteEnd){
			byteStart = byteStart - 1 ;
		}
		long t1 = System.currentTimeMillis();
		RandomAccessFile file = new RandomAccessFile(path, "r");
		FileWriter fileW = new FileWriter(this.getNameFile(properties.getPathDirBackup(), cont,null, idProceso));
		int sizeArray = 1024 * 8;
		int i = 0;
		char c=0;
		byte array[]=null;
		boolean flagReader = true;
		file.seek(byteStart);
		//StringBuilder line = null;
		StringBuffer line = null;
		System.out.println("Inicia copiado");
		do 
		{
			//line = new StringBuilder();
			line = new StringBuffer();
			array = new byte[sizeArray];
			file.read(array, 0, (sizeArray - 1));
			i = 0;
			while ((c = (char) array[i]) != 0) {
				if (byteStart < byteEnd) 
				{
					flagReader = false;
					line.append(c);
				}
				i++;
				byteStart++;
			}
			fileW.write(line.toString());
		} while (flagReader);
		System.out.println("Finaliza copiado");
		file.close();
		fileW.close();
		long t2 =  System.currentTimeMillis() - t1;
		System.out.println("TIME: Tiempo de  Copiando transcurrido:  " + t2 + " MS");
		
		/*
		Date dateInicio2 = new Date();
		System.out.println("TIMEFINALCopiando archivos:" + dateFormat.format(dateInicio2) + " M" + System.currentTimeMillis());
		*/
	}

	/**
	 * 
	 */
	private void close() 
	{
		try 
		{
			if (br != null) 
			{	br.close();	}
			
			if(salida!=null)
			{	salida.close();		}
			if(incidencia!=null)
			{	incidencia.close();	}
			if(incidenciaCifras!=null)
			{	incidenciaCifras.close();	}
			if (salidaBD != null)
			{	salidaBD.close();	}
			if (salidaODM != null)
			{	salidaODM.close();	}
			if (fileRecompensa != null)
			{	fileRecompensa.close();	}
			
						
			if (flagProcesado) 
			{
				//Una vez terminado el proeso copia el archivo
				copy(properties.getPathDirProcesados()+this.nameFile);
				//Elimina el archivo del directorio procesar ECB
				/*DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				Date dateInicio = new Date();
				System.out.println("TIMEINICIOBorrando archivo:" + dateFormat.format(dateInicio) + " M" + System.currentTimeMillis());
				*/
				long t1 = System.currentTimeMillis();
				file.delete();
				long t2 = t1- System.currentTimeMillis();
				System.out.println("TIME: Borrando archivo, " + this.nameFile + t2 + " ms");
					/*			
				Date dateInicio2 = new Date();
				System.out.println("TIMEFINALBorrando archivo:" + dateFormat.format(dateInicio2) + " M" + System.currentTimeMillis());*/
			}
		} 
		catch (IOException ioe) 
		{
			logger.error(ioe.getLocalizedMessage(), ioe);
		} 
		finally 
		{
			br = null;
			salida = null;
			salidaBD = null;
			salidaODM = null;
			incidencia = null;
			incidenciaCifras = null;
		}
	}

	/**
	 * 
	 */
	private void closeByte() 
	{
		try 
		{
			if (salida != null) 
			{	salida.close();		}
			
			if (salidaBD != null)
			{	salidaBD.close();	}
			
			if (salidaODM != null)
			{	salidaODM.close();	}
			
			if (incidencia != null) 
			{	incidencia.close();	}
			
			if (incidenciaCifras != null) 
			{	incidenciaCifras.close();	}
			
			if (fileRecompensa != null) 
			{	fileRecompensa.close();	}
			
			//this.file=new File(this.nameFile);
//			boolean flag1=Util.concatFile(file.getName(), properties.getPathIncidencia(), properties.getConfiguration(), "INC");
//			boolean flag2=Util.concatFile(file.getName(), properties.getPathSalida(), properties.getConfiguration(), "XML");
//			
//			if(flag1&&flag2){
//				FileCopyUtils.copy(file, new File(properties.getPathDirProcesados()+file.getName()));
//				file.delete();
//			}
		} 
		catch (Throwable ioe) 
		{
			ioe.printStackTrace();
		} 
		finally 
		{
			salida = null;
			salidaBD = null;
			salidaODM = null;
			incidencia = null;
			incidenciaCifras = null;
		}
	}

	/**
	 * 
	 * @throws IOException
	 */
	
	private static long generaXmlTime = 0;
	
	private void formatLinea(String idProceso, String fecha, String fileNames, String numeroMalla) 
		throws IOException 
	{	//System.out.println("formatLinea");
		linea = Util.convierte(linea).concat("|temp");
//		boolean versionTypo = true; // Tipo version AMDA
		token = linea.substring(0, 2);
		int numElement = 0;
		try 
		{	numElement = Integer.parseInt(token);	} 
		catch (NumberFormatException numberEx) 
		{	logger.error("No empieza con un numero " + linea);	} 
		// Metodo Prueba AMDA Version 3.3
		conver.loadInfoV33(numElement, linea, campos22, lstFiscal);
		switch (numElement) 
		{
		case 1:
			if (conver.getTags().isComprobante) 
			{	
				if ( !this.dobleAddenda ) {
					this.endIMPUESTOS();
					this.endMOVIMIENTOS();
					this.addenda();
				}
				else
					this.endAddendaECB();
				System.out.println("filenamesContabilizar:" + fileNames);
				this.end(1, idProceso, fecha, fileNames, numeroMalla);
				long t2 = generaXmlTime - System.currentTimeMillis();
				System.out.println("TIME: Genera XMLTime " + t2 + " ms");
				generaXmlTime = System.currentTimeMillis();
				this.begin();
			}
			startLine = "" + contCFD;
			endLine = null;
			conver.getTags().isComprobante = true;
			conver.set(linea, contCFD, fileNames, getNombresApps(), numeroMalla);
			this.dobleAddenda = false;
			this.starnewAddenda = false;
			break;
		case 2:
				out.write(conver.fComprobante(linea, contCFD, tipoCambio, lstFiscal, campos22, fileNames));
			break;
		case 3:
			out.write(conver.emisor(linea, lstFiscal, contCFD, campos22));
			break;
		case 4:
			break;
		case 5:
			out.write(conver.receptor(conver.getTags().lineaAnterior, conver.getTags().contCFDAnterior));
			out.write(conver.domicilio(linea, contCFD)); 
			this.beginCONCEPTOS();
			break;
		case 6: 
			if (this.nameFile.contains("PTCARTERR") || this.nameFile.contains("PTSOFOMR") || this.nameFile.contains("INGEDCR")) 
				out.write(conver.conceptoCarter(linea, contCFD, lstFiscal, campos22));// , fileNames)); Correccion para funcion concepto que recibe 4 parametro
			else
				out.write(conver.concepto(linea, contCFD, lstFiscal, campos22));// , fileNames)); Correccion para funcion concepto que recibe 4 parametro
			break;
		case 7:
			if(!conver.getTags().tipoComprobante.equalsIgnoreCase("T") && !conver.getTags().tipoComprobante.equalsIgnoreCase("P")){
				out.write(conver.impuestos(linea, contCFD));
			}
			break;
		case 8:
			if(!conver.getTags().tipoComprobante.equalsIgnoreCase("T") && !conver.getTags().tipoComprobante.equalsIgnoreCase("P")){
			}
			break;
		case 9:
			if(!conver.getTags().tipoComprobante.equalsIgnoreCase("T") && !conver.getTags().tipoComprobante.equalsIgnoreCase("P")){
				//System.out.println("Conver Tags Tipo Comprobante DEntro De Ret y Tra: " + conver.getTags().tipoComprobante);
			}
			break;
		case 10:
			this.endCONCEPTOS();
			if (conver.getTags().isImpuestos) 
			{	this.endIMPUESTOS();	} 
			else if (!conver.getTags().isImpuestos) 
			{	out.write("\n<cfdi:Impuestos/>".getBytes());		}
			this.endMOVIMIENTOS();
			this.addenda();
			break;
		case 11:
			this.beginMOVIMIENTOS(); 
			out.write(conver.movimeinto(linea, contCFD));
			break;
		case 13:
			System.out.println("Case 13:Entra cfdi ");
			conver.getInfoCfdiRelacionado(linea);
			break;
		case 14:
			this.endIMPUESTOS();
			this.endMOVIMIENTOS();
			this.dobleAddenda = true;
			this.startAddendaECB(); 
			out.write( conver.barCode(linea, contCFD));
			this.startOperaciones();
			break;
		case 15:
			
			out.write( conver.operaciones(linea, contCFD) );
			this.contOper = true;
			break;
		case 16:
			this.contCobr = true;
			this.endOperaciones();
			out.write( conver.cobranza(linea, contCFD) );
			
			break;
		default:
			System.out.println("caseBreak");
			break;
		}

		conver.getTags().lineaAnterior = linea;
		conver.getTags().contCFDAnterior = contCFD;
	}

	/**
	 * 
	 */
	public void beginIMPUESTOS() 
	{

	}

	/**
	 * 
	 * @throws IOException
	 */
	public void endIMPUESTOS() 
		throws IOException 
	{
		if (conver.getTags().isImpuestos) 
		{
			try 
			{	temp = "\n</cfdi:" + conver.getPila().pop() + ">";		} 
			catch (Exception e) 
			{
				// logger.error(e.getMessage(),e);
			}
			out.write("\n</cfdi:Impuestos>".getBytes());
			conver.getTags().isImpuestos = false;
		}
	}

	/**
	 * 
	 * @throws IOException
	 */
	public void beginCONCEPTOS() 
		throws IOException 
	{
		out.write("\n<cfdi:Conceptos>".getBytes());
		conver.getTags().isConceptos = true;
		conver.getPila().push("Conceptos");
	}

	/**
	 * 
	 * @throws IOException
	 */
	public void endCONCEPTOS() 
		throws IOException 
	{
		if (conver.getTags().isConceptos) 
		{
			temp = "\n</cfdi:" + conver.getPila().pop() + ">";
			out.write(temp.getBytes());
			conver.getTags().isConceptos = false;
		}
	}

	/**
	 * 
	 * @throws IOException
	 */
	public void beginRETENCIONES() 
		throws IOException 
	{
		if (!conver.getTags().isRetenciones) 
		{
			out.write("\n<cfdi:Retenciones>".getBytes());
			conver.getTags().isRetenciones = true;
		}
	}

	/**
	 * 
	 * @throws IOException
	 */
	public void endRETENCIONES() 
		throws IOException 
	{
		if (conver.getTags().isRetenciones) 
		{
			out.write("\n</cfdi:Retenciones>".getBytes());
			conver.getTags().isRetenciones = false;
		}
	}

	/**
	 * 
	 * @throws IOException
	 */
	public void beginTRASLADOS() 
		throws IOException 
	{
		if (!conver.getTags().isTralados) 
		{
			out.write("\n<cfdi:Traslados>".getBytes());
			conver.getTags().isTralados = true;
		}
	}

	/**
	 * 
	 * @throws IOException
	 */
	public void endTRALADOS() 
		throws IOException 
	{
		if (conver.getTags().isTralados) 
		{
			out.write("\n</cfdi:Traslados>".getBytes());
			conver.getTags().isTralados = false;
		}
	}

	/**
	 * 
	 * @throws IOException
	 */
	public void beginMOVIMIENTOS() 
		throws IOException 
	{
		if (!conver.getTags().isMovimiento) 
		{
			out.write("\n<Santander:Movimientos>".getBytes());
			conver.getTags().isMovimiento = true;
		}
	}

	/**
	 * 
	 * @throws IOException
	 */
	public void endMOVIMIENTOS() 
		throws IOException 
	{
		if (conver.getTags().isMovimiento) 
		{
			out.write("\n</Santander:Movimientos>".getBytes());
			conver.getTags().isMovimiento = false;
		}
	}

	/**
	 * 
	 * @throws IOException
	 */
	public void complemento() 
		throws IOException 
	{
		if (conver.getTags().isComplemento) 
		{	this.endCOMPLEMENTO();	} 
		else if (!conver.getTags().isComplemento) 
		{	this.beginCOMPLEMENTO();	}
	}

	/**
	 * 
	 * @throws IOException
	 */
	public void beginCOMPLEMENTO() 
		throws IOException 
	{
		if (!conver.getTags().isComplemento) 
		{
			out.write("\n<cfdi:Complemento>".getBytes());
			out.write(conver.complemento(linea, contCFD));
			conver.getTags().isComplemento = true;
		}
	}

	/**
	 * ADDENDA SANTANDER
	 * @throws IOException
	 */
	public void addenda() throws IOException {
		if (conver.getTags().isAddenda) {
			this.endADDENDA();
		} else if (!conver.getTags().isAddenda) {
			this.beginADDENDA();
		}
	}

	/**
	 * ADDENDA SANTANDER
	 * @throws IOException
	 */
	public void beginADDENDA() 
		throws IOException 
	{
		if (!conver.getTags().isAddenda) 
		{
			out.write("\n<cfdi:Addenda>".getBytes());
			out.write("\n<Santander:addendaECB xmlns:Santander=\"http://www.santander.com.mx/schemas/xsd/addendaECB\">".getBytes());
			out.write(conver.complemento(linea, contCFD));		
			conver.getTags().isAddenda = true;
		}
	}
	/**
	 * 
	 * @throws IOException
	 */
	public void endCOMPLEMENTO() 
		throws IOException 
	{
		if (conver.getTags().isComplemento) 
		{
			out.write("\n</ecb:EstadoDeCuentaBancario>".getBytes());
			out.write("\n</cfdi:Complemento>".getBytes());
			conver.getTags().isComplemento = false;
		}
	}
	/**
	 * ADDENDA SANTANDER
	 * @throws IOException
	 */
	public void endADDENDA() 
		throws IOException 
	{
		if (conver.getTags().isAddenda) 
		{
			
			out.write("\n</Santander:EstadoDeCuentaBancario>".getBytes());
			out.write("\n</Santander:addendaECB>".getBytes());
			out.write("\n</cfdi:Addenda>".getBytes());
			conver.getTags().isAddenda = false;
//			this.addendaDomicilios();
		}
	}
	
	public void endAddendaECB() throws IOException {
		if (conver.getTags().isAddenda && this.contCobr ) 
		{
			out.write("\n</Santander:Cobranzas>".getBytes());
			out.write("\n</Santander:addendaConfirming>".getBytes());
			out.write("\n</cfdi:Addenda>".getBytes());
			this.addendaDomicilios();
			conver.getTags().isAddenda = false;
			this.endOper = false;
			this.contOper = false;
			this.contCobr = false;
		} else {
			out.write("\n</Santander:Operaciones>".getBytes());
			out.write("\n</Santander:addendaConfirming>".getBytes());
			out.write("\n</cfdi:Addenda>".getBytes());
			this.addendaDomicilios();
			conver.getTags().isAddenda = false;
			this.endOper = false;
			this.contOper = false;
			this.contCobr = false;
		}
		
	}
	
	public void endOperaciones() throws IOException {
		if ( !this.endOper ) {
			out.write("\n</Santander:Operaciones>".getBytes());
			out.write("\n<Santander:Cobranzas>".getBytes());
			this.endOper = true;
		} 
	}
	
	/**
	 * @throws IOException
	 */
	public void startAddendaECB() throws IOException {
		out.write("\n</Santander:EstadoDeCuentaBancario>".getBytes());
		out.write("\n</Santander:addendaECB>".getBytes());
		out.write("\n<Santander:addendaConfirming>".getBytes());
	}
	
	public void startOperaciones() throws IOException {
		out.write("\n<Santander:Operaciones>".getBytes());
	}
	
	
	/**
	 * Metodo para generar addenda de domicilios
	 */
	public void addendaDomicilios() throws IOException {
		this.beginAddendaDomicilios();
//		out.write(conver.domicilioEmisor());		
		out.write(conver.domicilioReceptor());		
		this.endAddendaDomicilios();
	}
	private void beginAddendaDomicilios() throws IOException {
		out.write("\n<cfdi:Addenda>".getBytes("UTF-8"));
		out.write(conver.getTags().addenda.getBytes("UTF-8"));
	}
	
	private void endAddendaDomicilios() throws IOException {
		out.write("\n</as:AddendaSantanderV1>".getBytes("UTF-8"));
		out.write("\n</cfdi:Addenda>".getBytes("UTF-8"));
	}
	/**
	 * 
	 * @throws IOException
	 */
	private void begin() 
		throws IOException 
	{
		
		out = new ByteArrayOutputStream();
		out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>".getBytes());
		conver.getTags().cfd = new CFDIssued();
	}

	//Convierte un ByteArrayOutputStream a Document
	public Document byteArrayOutputStreamToDocument(ByteArrayOutputStream byteArray) throws Exception{
		Document dom = null;
		
		if (this.db == null){
			this.dbf = DocumentBuilderFactory.newInstance();
			this.db = this.dbf.newDocumentBuilder();
		}
		dom = this.db.parse(new InputSource(new StringReader(byteArray.toString("UTF-8"))));
		
		return dom;
	}
	
	//Covierte de Document a StringWriter
	public StringWriter documentToStringWriter(Document dom) throws Exception{
		StringWriter sw2 = new StringWriter();		
		
		if(this.tx == null){
			this.tx = TransformerFactory.newInstance().newTransformer();
			this.tx.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		}					
		this.tx.transform(new DOMSource(dom), new StreamResult(sw2));
		
		return sw2;
	}
	
	//Convierte de String a Document
	public Document stringToDocument(String strXML) throws Exception{
		Document domResultado = null;
	
		if (this.db == null){
			this.dbf = DocumentBuilderFactory.newInstance();
			this.db = this.dbf.newDocumentBuilder();
		}
		
		domResultado = this.db.parse(new InputSource(new StringReader(strXML)));	
		
		return domResultado;
	}
	
	public StreamResult nodeToStreamResult(Node nodo) throws Exception{
		StreamResult sr = null;
		
		if (this.tx == null){
			this.tx = TransformerFactory.newInstance().newTransformer();
			this.tx.setOutputProperty(OutputKeys.INDENT, "yes");
		}
		sr = new StreamResult(new StringWriter());
		DOMSource sourceComprobante = new DOMSource(nodo);
		this.tx.transform(sourceComprobante, sr);	
			
		return sr;
	}
	
	public StreamResult documentToStreamResult(Document dom) throws Exception{
		StreamResult sr = null;
	
		if (this.tx == null){
			this.tx = TransformerFactory.newInstance().newTransformer();
			this.tx.setOutputProperty(OutputKeys.INDENT, "yes");
		}
		
		sr = new StreamResult(new StringWriter());
		DOMSource sourceComprobante = new DOMSource(dom);
		this.tx.transform(sourceComprobante, sr);	
		
		return sr;
	}
	
	public String putZeros(String str) throws Exception{
		String [] total = str.split("\\.");
		System.out.println("size: " + total.length);
		
		int nEnteros;
		int nDecimales;
		
		String strEnteros = "";
		String strDecimales = "";
				
		if(total.length > 1){
			nEnteros = total[0].length();
			nDecimales = total[1].length();
			
			int nZerosEnteros = 10 - nEnteros;
			int nZerosDecimales = 6 - nDecimales;
			
			if(nZerosEnteros > 0){
				for(int nZero=0; nZero<nZerosEnteros; nZero++){
					strEnteros = strEnteros + "0";
				}
				strEnteros = strEnteros + total[0];
			}else{
				strEnteros =  total[0];
			}
			if(nZerosDecimales > 0){
				strDecimales = total[1];
				for(int nZero=0; nZero<nZerosDecimales; nZero++){
					strDecimales = strDecimales + "0";
				}
			}else{
				strDecimales = total[1];
			}
		}else{
			nEnteros = str.length();
			
			int nZerosEnteros = 10 - nEnteros;
						
			if(nZerosEnteros > 0){
				for(int nZero=0; nZero<nZerosEnteros; nZero++){
					strEnteros = strEnteros + "0";
				}
				strEnteros = strEnteros + str;
			}else{
				strEnteros =  str;
			}
			strDecimales = "000000";
		}
			 
		return strEnteros + "." + strDecimales;
	}
	
	private boolean valorVacio(String strValor) throws Exception{
		if (strValor.trim().equals("")){
			return true;
		}else{
			return false;
		}
		
	}
	
	//24 de Abril 2013 Verificar si una cadena es numérica
	public boolean isNotDecimal2Pos(String strNumber) throws Exception{
		boolean fNotDecimal2Pos = false;
		if(strNumber.indexOf(".") > 0){
			String [] partesEnteras = strNumber.split("\\."); 
			if(partesEnteras.length == 2){
				if(partesEnteras[0].length() == 0 || partesEnteras[1].length() != 2 ){
					fNotDecimal2Pos = true;
				}
			}else{
				fNotDecimal2Pos = true;
			}
		}else{
			fNotDecimal2Pos = true;
		}
		
		return fNotDecimal2Pos;		
	}
	
	public boolean isNotNumeric(String strNumber) throws Exception{
		boolean fNotNumber = false;
		int i=0;
		while(!fNotNumber && i < strNumber.length()){
			try{
				Integer.parseInt(Character.toString(strNumber.charAt(i)));				
			}catch(NumberFormatException ex){
				fNotNumber = true;
				break;
			}
			i++;
		};
		return fNotNumber;		
	}
	
	private void validateNodes() throws Exception {

	}
	
	private Document removeMovimientoECB(Document dom) throws Exception{		
		this.fAttMovIncorrect = false;
		this.fnombreCliente = false;
		this.fnumeroCuenta = false;
		this.fperiodo = false;
		this.fsucursal = false;
		    		
		Node nodeAddenda = null;
		
		Element root = dom.getDocumentElement();
		
		//Recorrer los nodos hijos de cfdi:Addenda														
		for(int i=0; i<root.getChildNodes().getLength(); i++){			
			//Verificar si el hijo actual corresponde a una instancia de Element y se llama cfdi:Addenda
			if(root.getChildNodes().item(i) instanceof Element && 
					root.getChildNodes().item(i).getNodeName().equals("cfdi:Addenda")){
					nodeAddenda = root.getChildNodes().item(i).cloneNode(true);										
					/*StreamResult srXXX = new StreamResult();
					srXXX = nodeToStreamResult(nodeAddenda);					
					System.out.println("nodeAddenda: " + srXXX.getWriter().toString());*/
					//Recorrer hijos de cfdi:Complemento		
					for(int x=0; x<root.getChildNodes().item(i).getChildNodes().getLength(); x++){
						if(root.getChildNodes().item(i).getChildNodes().item(x) instanceof Element && 
								root.getChildNodes().item(i).getChildNodes().item(x).getNodeName().equals("Santander:addendaECB")){
							
							//Banderas para comprobar que existan algunos atributos de Santander:EstadoDeCuentaBancario
						    this.existNumeroCuenta = false;
						    this.existNombreCliente = false;
						    this.existPeriodo = false;
						    
							for(int j=0; j<root.getChildNodes().item(i).getChildNodes().item(x).getChildNodes().getLength(); j++){
								
								//Verificar si el hijo actual corresponde a una instancia de Element y se llama ecb:EstadoDeCuentaBancario
								if(root.getChildNodes().item(i).getChildNodes().item(x).getChildNodes().item(j) instanceof Element && 
										root.getChildNodes().item(i).getChildNodes().item(x).getChildNodes().item(j).getNodeName().equals("Santander:EstadoDeCuentaBancario")){
									
									//Respaldar el nodo ecb:EstadoDeCuentaBancario existente
									NamedNodeMap atributosECB = root.getChildNodes().item(i).getChildNodes().item(x).getChildNodes().item(j).getAttributes();
									EstadoDeCuentaBancario ecb = new EstadoDeCuentaBancario();
									
									for(int iAtt=0; iAtt<atributosECB.getLength();  iAtt++){
										Attr atributo = (Attr) atributosECB.item(iAtt);
										if(atributo.getName().equals("version")){											
											ecb.setVersion(atributo.getValue());											
										}else if(atributo.getName().equals("numeroCuenta")){
											this.existNumeroCuenta = true;
											if(atributo.getValue().length() > 0 &&  atributo.getValue().length() <= 40){
												if (this.valorVacio(atributo.getValue())){
													this.fnumeroCuenta = true;
													
												}else{
													ecb.setNumeroCuenta(atributo.getValue());
												}												
											}else{
												this.fnumeroCuenta = true;
												
											}											
										}else if(atributo.getName().equals("nombreCliente")){
											this.existNombreCliente = true;
											if (this.valorVacio(atributo.getValue())){
												this.fnombreCliente = true;	
												
											}else{
												ecb.setNombreCliente(atributo.getValue());
											}
										}else if(atributo.getName().equals("periodo")){
											this.existPeriodo = true;
											if (this.valorVacio(atributo.getValue())){
												this.fperiodo = true;
												
											}else{
												ecb.setPeriodo(atributo.getValue());
											}											
										}else if(atributo.getName().equals("sucursal")){											
											ecb.setSucursal(atributo.getValue());
																						
										}
									}
									
									if(this.existNumeroCuenta && this.existPeriodo){
										
									
									
										this.estadoDeCuentaBancario = ecb;
																					
										//Recorrer los hijos de ecb:EstadoDeCuentaBancario
										for(int k=0; k<root.getChildNodes().item(i).getChildNodes().item(x).getChildNodes().item(j).getChildNodes().getLength(); k++){
											
											//Verificar si el hijo actual corresponde a una instancia de Element y se llama ecb:Movimientos
											if(root.getChildNodes().item(i).getChildNodes().item(x).getChildNodes().item(j).getChildNodes().item(k) instanceof Element && 
													root.getChildNodes().item(i).getChildNodes().item(x).getChildNodes().item(j).getChildNodes().item(k).getNodeName().equals("Santander:Movimientos")){
																						    
												//Recorrer los hijos de ecb:Movimientos 
												for(int l=0; l<root.getChildNodes().item(i).getChildNodes().item(x).getChildNodes().item(j).getChildNodes().item(k).getChildNodes().getLength(); l++){
													
													//Banderas para comprobar que existan algunos atributos de Santander:Movimientos
												    this.existFecha = false;
												    this.existDescripcion = false;
												    this.existImporte = false;
												    
													//Verificar si el hijo actual corresponde a una instancia de Element y se llama ecb:MovimientoECB
													if(root.getChildNodes().item(i).getChildNodes().item(x).getChildNodes().item(j).getChildNodes().item(k).getChildNodes().item(l) instanceof Element){
														MovimientoECB movEcb = new MovimientoECB();	
														
														if(root.getChildNodes().item(i).getChildNodes().item(x).getChildNodes().item(j).getChildNodes().item(k).getChildNodes().item(l).getNodeName().equals("Santander:MovimientoECB")){
															NamedNodeMap atributos = root.getChildNodes().item(i).getChildNodes().item(x).getChildNodes().item(j).getChildNodes().item(k).getChildNodes().item(l).getAttributes();
																											
															for(int iAtt=0; iAtt< atributos.getLength(); iAtt++){
																Attr atributo = (Attr) atributos.item(iAtt);
																if(atributo.getName().equals("fecha")){	
																	this.existFecha = true;
																	if (this.valorVacio(atributo.getValue())){
																		this.fAttMovIncorrect = true;	
																		break;
																	}else{																		
																    	 //Fecha valida
																    	 movEcb.setFecha(atributo.getValue());		
																    	 movEcb.setFechaOrden(convertDateMov(atributo.getValue()));
																	}																
																}else if(atributo.getName().equals("referencia")){																
																	movEcb.setReferencia(atributo.getValue());																	
																}else if(atributo.getName().equals("descripcion")){
																	this.existDescripcion = true;
																	if (this.valorVacio(atributo.getValue())){
																		this.fAttMovIncorrect = true;	
																		break;
																	}else{
																		//System.out.println("DESCRIPCION: " +atributo.getValue().getBytes("UTF-8").toString());
																		movEcb.setDescripcion(atributo.getValue());
																	}																
																}else if(atributo.getName().equals("importe")){
																	this.existImporte = true;
																	if (this.isNotDecimal2Pos(atributo.getValue())){
																		this.fAttMovIncorrect = true;	
																		break;
																	}else{
																		movEcb.setImporte(atributo.getValue());
																	}																	
																}else if(atributo.getName().equals("moneda")){
																	movEcb.setMoneda(atributo.getValue());																
																}else if(atributo.getName().equals("saldoInicial")){
																	movEcb.setSaldoInicial(atributo.getValue());																
																}else if(atributo.getName().equals("saldoAlCorte")){
																	movEcb.setSaldoAlCorte(atributo.getValue());																
																}
															}
															if(!this.existFecha || !this.existDescripcion || !this.existImporte){
																this.fAttMovIncorrect = true;
																break;
															}
														}else if(root.getChildNodes().item(i).getChildNodes().item(x).getChildNodes().item(j).getChildNodes().item(k).getChildNodes().item(l).getNodeName().equals("Santander:MovimientoECBFiscal")){
															NamedNodeMap atributos = root.getChildNodes().item(i).getChildNodes().item(x).getChildNodes().item(j).getChildNodes().item(k).getChildNodes().item(l).getAttributes();
															movEcb.setFiscal(true);												
															for(int iAtt=0; iAtt< atributos.getLength(); iAtt++){
																Attr atributo = (Attr) atributos.item(iAtt);
																if(atributo.getName().equals("fecha")){
																	this.existFecha = true;
																	if (this.valorVacio(atributo.getValue())){
																		this.fAttMovIncorrect = true;	
																		break;
																	}else{
																		movEcb.setFecha(atributo.getValue());
																		movEcb.setFechaOrden(convertDateMov(atributo.getValue()));
																	}																
																}else if(atributo.getName().equals("referencia")){
																	movEcb.setReferencia(atributo.getValue());																
																}else if(atributo.getName().equals("descripcion")){
																	this.existDescripcion = true;
																	if (this.valorVacio(atributo.getValue())){
																		this.fAttMovIncorrect = true;	
																		break;
																	}else{
																		movEcb.setDescripcion(atributo.getValue());
																	}																															
																}else if(atributo.getName().equals("Importe")){
																	this.existImporte = true;
																	if (this.isNotDecimal2Pos(atributo.getValue())){
																		this.fAttMovIncorrect = true;	
																		break;
																	}else{
																		movEcb.setImporte(atributo.getValue());
																	}																	
																}else if(atributo.getName().equals("moneda")){
																	movEcb.setMoneda(atributo.getValue());															
																}else if(atributo.getName().equals("saldoInicial")){
																	movEcb.setSaldoInicial(atributo.getValue());																
																}else if(atributo.getName().equals("saldoAlCorte")){																
																	movEcb.setSaldoAlCorte(atributo.getValue());	
																}else if(atributo.getName().equals("RFCenajenante")){																
																	if (this.valorVacio(atributo.getValue())){
																		this.fAttMovIncorrect = true;	
																		break;
																	}else{
																		movEcb.setRfcEnajenante(atributo.getValue());																
																	}																
																}
															}	
															if(!this.existFecha || !this.existDescripcion || !this.existImporte){
																this.fAttMovIncorrect = true;
																break;
															}
														}													
														lstMovimientosECB.add(movEcb);																												
													}
												}																																
											}
										}		
									}else{
										if(!this.existNumeroCuenta){
											this.fnumeroCuenta = true;
											break;
										}else if(!this.existPeriodo){
											this.fperiodo = true;
											break;
										}
									}
								}
							}
						}else if (root.getChildNodes().item(i).getChildNodes().item(x) instanceof Element && root.getChildNodes()
								.item(i).getChildNodes().item(x).getNodeName().equals("as:AddendaSantanderV1")) {
							//System.out.println("Elimina addenda domicilios");
							String domStr="";
							domStr=UtilCatalogos.convertDocumentXmlToString(dom);
							if (domStr.indexOf("<as:AddendaSantanderV1") != -1){
								int iStart = domStr.indexOf("<as:AddendaSantanderV1");
								int iEnd = domStr.indexOf("</as:AddendaSantanderV1>");
								iEnd = iEnd + 24;
								addendaDomiciliosNodeStr.add(domStr.substring(iStart, iEnd));
							}
							root.removeChild(root.getChildNodes().item(i));
						}
					}
				}
			}
		
		System.out.println("nodeADDXD: " + this.dobleAddenda);
		String domStr="";
		domStr=UtilCatalogos.convertDocumentXmlToString(dom);
		System.out.println("xml1ADDXD: " + domStr); 
		if ( this.dobleAddenda )
			nodeAddenda = null;
		
		if(nodeAddenda != null){
			//Recorrer los nodos hijos de cfdi:Comprobante
			int i=0;
			boolean fDelAddenda=false;
			do{			
				//Verificar si el hijo actual corresponde a una instancia de Element y se llama cfdi:Complemento
				if(root.getChildNodes().item(i) instanceof Element && 
						root.getChildNodes().item(i).getNodeName().equals("cfdi:Addenda")){						
						root.removeChild(dom.getDocumentElement().getChildNodes().item(i));											
						fDelAddenda = true;
				}
				i++;
			}while(i<root.getChildNodes().getLength() && !fDelAddenda);
			
		}	
		return dom;
	}
	
	public Date convertDateMov(String fechaMov) throws Exception{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		int indexT = fechaMov.indexOf("T");
		return sdf.parse(fechaMov.substring(0, indexT) + " " + 
				fechaMov.substring(indexT + 1));
	}
	
	
	
	private String getFolioSAT(Element docEleComprobante, Document domResultado){
		String strFolioSAT = "";
		NodeList hijosComprobante = docEleComprobante.getChildNodes();
		for(int i=0; i<hijosComprobante.getLength(); i++){
			Node nodo = hijosComprobante.item(i);								
			//System.out.println("NAME: " + nodo.getNodeName());
			if(nodo instanceof Element && nodo.getNodeName().equals("cfdi:Complemento")){
				NodeList hijosComplemento = nodo.getChildNodes();
				for(int j=0; j<hijosComplemento.getLength(); j++){
					Node nodo2 = hijosComplemento.item(j);
					if(nodo2 instanceof Element && nodo2.getNodeName().equals("tfd:TimbreFiscalDigital")){	
						strFolioSAT = nodo2.getAttributes().getNamedItem("UUID").getTextContent().trim();						
					}
				}
			}
		}
		return strFolioSAT;
	}

	private Document putNewAddendaECB(Element docEleComprobante, Document domResultado, List<Operacion> lstopEcb, List<Cobranza> lstcobEcb) {

		Element doc = domResultado.getDocumentElement();
		NodeList hijosComprobante = doc.getChildNodes();
		for(int i=0; i<hijosComprobante.getLength(); i++){
			Node nodo = hijosComprobante.item(i);								
			if(nodo instanceof Element && nodo.getNodeName().equals("cfdi:Addenda")){
				Element addenda = domResultado.createElement("Santander:addendaConfirming"); 
				domResultado.getDocumentElement().getChildNodes().item(0).getChildNodes().item(i).appendChild(addenda).appendChild(domResultado.createElement("Santander:Operaciones"));
				Element addenda2 = domResultado.createElement("Santander:Cobranzas"); 
				domResultado.getDocumentElement().getChildNodes().item(0).getChildNodes().item(i).appendChild(addenda2).appendChild(domResultado.createElement("Santander:Cobranzas"));
			}
		}
		
		doc = domResultado.getDocumentElement();
		hijosComprobante = doc.getChildNodes();
		for(int i=0; i<hijosComprobante.getLength(); i++){
			Node nodo = hijosComprobante.item(i);								
			if(nodo instanceof Element && nodo.getNodeName().equals("cfdi:Addenda")){
				NodeList hijosAddendaRoot = nodo.getChildNodes();
				
				for(int j=0; j<hijosAddendaRoot.getLength(); j++){
					
					Node nodoj = hijosAddendaRoot.item(j);
					
					if(nodoj instanceof Element && nodoj.getNodeName().equals("Santander:addendaConfirming")){
						
						NodeList hijosAddendaConfirming = nodoj.getChildNodes();
						
						for ( int k = 0; k < hijosAddendaConfirming.getLength(); k++ ) {
							
							Node nodok = hijosAddendaConfirming.item(k);
							
							if ( nodok instanceof Element && nodok.getNodeName().equals("Santander:Operaciones") ) {
								
								for( Operacion operacion : this.lstOperaciones ) {
									Element operacioneECB = domResultado.createElement("Santander:Operacion");
									if ( operacion.getCodBar() != null && !operacion.getCodBar().equals("") )
										operacioneECB.setAttribute("CodBar", operacion.getCodBar());
									
									if ( operacion.getFechaPagoRecibo() != null && !operacion.getFechaPagoRecibo().equals("") )
										operacioneECB.setAttribute("FechaPagoRecibo", operacion.getFechaPagoRecibo());
									
									if ( operacion.getNumeroContrato() != null && !operacion.getNumeroContrato().equals("") )
										operacioneECB.setAttribute("NumeroContrato", operacion.getNumeroContrato());
									
									if ( operacion.getNombreDeudorOpe() != null && !operacion.getNombreDeudorOpe().equals("") )
										operacioneECB.setAttribute("NombreDeudor", operacion.getNombreDeudorOpe());
									
									if ( operacion.getRfcDeudorOpe() != null && !operacion.getRfcDeudorOpe().equals("") )
										operacioneECB.setAttribute("RFCDeudor", operacion.getRfcDeudorOpe());
									
									if ( operacion.getNoDeudor() != null && !operacion.getNoDeudor().equals("") )
										operacioneECB.setAttribute("NoDocumento", operacion.getNoDeudor());
									
									if ( operacion.getFechaVencimiento() != null && !operacion.getFechaVencimiento().equals("") )
										operacioneECB.setAttribute("FechaVencimiento", operacion.getFechaVencimiento());
									
									if ( operacion.getTasaDescInt() != null && !operacion.getTasaDescInt().equals("") )
										operacioneECB.setAttribute("TasaDescInt", operacion.getTasaDescInt());
									
									if ( operacion.getPlazo() != null && !operacion.getPlazo().equals("") )
										operacioneECB.setAttribute("Plazo", operacion.getPlazo());
									
									if ( operacion.getValorNominalOpe() != null && !operacion.getValorNominalOpe().equals("") )
										operacioneECB.setAttribute("ValorNominal", operacion.getValorNominalOpe());
									
									if ( operacion.getDescRend() != null && !operacion.getDescRend().equals("") )
										operacioneECB.setAttribute("DescRend", operacion.getDescRend());
									
									if ( operacion.getPrecioFactoraje() != null && !operacion.getPrecioFactoraje().equals("") )
										operacioneECB.setAttribute("PrecioFactoraje", operacion.getPrecioFactoraje());
									
									domResultado.getDocumentElement().getChildNodes().item(0).getChildNodes().item(i).getChildNodes().item(j).getChildNodes().item(k).appendChild(operacioneECB);
								}
								
							} else if ( nodok instanceof Element && nodok.getNodeName().equals("Santander:Cobranzas") ) {
								
								for ( Cobranza cobranza: lstCobranzas ) {
									Element cobECB = domResultado.createElement("Santander:Cobranza");
									
									if ( cobranza.getFechaCobro() != null && !cobranza.getFechaCobro().equals("") )
										cobECB.setAttribute("", cobranza.getFechaCobro());
									
									if ( cobranza.getNombreDeudorCob() != null && !cobranza.getNombreDeudorCob().equals("") )
										cobECB.setAttribute("", cobranza.getNombreDeudorCob());
									
									if ( cobranza.getRfcDeudorCob() != null && !cobranza.getRfcDeudorCob().equals("") )
										cobECB.setAttribute("", cobranza.getRfcDeudorCob());
									
									if ( cobranza.getFechaCesion() != null && !cobranza.getFechaCesion().equals("") )
										cobECB.setAttribute("", cobranza.getFechaCesion());
									
									if ( cobranza.getNoDocumento() != null && !cobranza.getNoDocumento().equals("") )
										cobECB.setAttribute("", cobranza.getNoDocumento());
									
									if ( cobranza.getValorNominalCon() != null && !cobranza.getValorNominalCon().equals("") )
										cobECB.setAttribute("", cobranza.getValorNominalCon());
									
									if ( cobranza.getTotalCobrado() != null && !cobranza.getTotalCobrado().equals("") )
										cobECB.setAttribute("", cobranza.getTotalCobrado());
									
									domResultado.getDocumentElement().getChildNodes().item(0).getChildNodes().item(i).getChildNodes().item(j).getChildNodes().item(k).appendChild(cobECB);
									
								}
								
							}
							
						}
						
					}
				}
				
			}
		}
		
		String xmlString2 ="";
		try {
			xmlString2 = UtilCatalogos.convertDocumentXmlToString(domResultado);
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("xmlADDXD: " + xmlString2 );
		
		return domResultado;
	}
	
	private Document putMovimientoECB( Element docEleComprobante, Document domResultado, 
			EstadoDeCuentaBancario estadoDeCuentaBancariox, List<MovimientoECB> lstMovimientosECBx , 
			List<Operacion> lstopEcb, List<Cobranza> lstcobEcb, boolean addendaNew , Operacion oper) {
		
		
		Element rootAddenda = domResultado.createElement("cfdi:Addenda");
		//rootAddenda.setAttribute("xmlns:Santander", "http://www.santander.com.mx/schemas/xsd/addendaECB");
		
		Element nodeAddendaECB = domResultado.createElement("Santander:addendaECB"); 
		nodeAddendaECB.setAttribute("xmlns:Santander", "http://www.santander.com.mx/schemas/xsd/addendaECB");
				
		docEleComprobante.appendChild(rootAddenda).appendChild(nodeAddendaECB).appendChild(domResultado.createElement("Santander:EstadoDeCuentaBancario")).appendChild(domResultado.createElement("Santander:Movimientos"));
		
		strTotal = docEleComprobante.getAttributes().getNamedItem("Total").getTextContent(); // Antes total, ahora en version 3.3 Total AMDA
		
		NodeList hijosComprobante = docEleComprobante.getChildNodes();
		for(int i=0; i<hijosComprobante.getLength(); i++){
			Node nodo = hijosComprobante.item(i);								
			if(nodo instanceof Element && nodo.getNodeName().equals("cfdi:Emisor")){
				strEmisorRFC = nodo.getAttributes().getNamedItem("Rfc").getTextContent(); // Antes rfc, Version 3.3 AMDA
			}
			else if(nodo instanceof Element && nodo.getNodeName().equals("cfdi:Receptor")){
				strReceptorRFC = nodo.getAttributes().getNamedItem("Rfc").getTextContent(); // Antes rfc, Version 3.3 AMDA
			}
			else if(nodo instanceof Element && nodo.getNodeName().equals("cfdi:Complemento")){
				NodeList hijosComplemento = nodo.getChildNodes();
				for(int j=0; j<hijosComplemento.getLength(); j++){
					Node nodo2 = hijosComplemento.item(j);
					if(nodo2 instanceof Element && nodo2.getNodeName().equals("tfd:TimbreFiscalDigital")){	
						strFechaTimbrado = nodo2.getAttributes().getNamedItem("FechaTimbrado").getTextContent().trim();
						strUUID = nodo2.getAttributes().getNamedItem("UUID").getTextContent().trim();
						strNoCertificadoSAT = nodo2.getAttributes().getNamedItem("NoCertificadoSAT").getTextContent().trim();
						strSelloCFD = nodo2.getAttributes().getNamedItem("SelloCFD").getTextContent().trim();
						strSelloSAT = nodo2.getAttributes().getNamedItem("SelloSAT").getTextContent().trim();
						strVersion = nodo2.getAttributes().getNamedItem("Version").getTextContent().trim(); // Antes version, Version 3.3 AMDA
					}
				}				
			}else if(nodo instanceof Element && nodo.getNodeName().equals("cfdi:Addenda")){
				NodeList hijosAddendaRoot = nodo.getChildNodes();
				
				for(int x=0; x<hijosAddendaRoot.getLength(); x++){
					Node nodox = hijosAddendaRoot.item(x);
					if(nodox instanceof Element && nodox.getNodeName().equals("Santander:addendaECB")){
						NodeList hijosAddenda = nodox.getChildNodes();
						for(int j=0; j<hijosAddenda.getLength(); j++){
							Node nodo2 = hijosAddenda.item(j);
							if(nodo2 instanceof Element && nodo2.getNodeName().equals("Santander:EstadoDeCuentaBancario")){
								Element nodeECB = (Element) nodo2;
								if(!estadoDeCuentaBancariox.getVersion().equals("")){
									nodeECB.setAttribute("version", estadoDeCuentaBancariox.getVersion()); 
								}
								if(!estadoDeCuentaBancariox.getNumeroCuenta().equals("")){
									nodeECB.setAttribute("numeroCuenta", estadoDeCuentaBancariox.getNumeroCuenta());
								}
								if(!estadoDeCuentaBancariox.getNombreCliente().equals("")){
									nodeECB.setAttribute("nombreCliente", estadoDeCuentaBancariox.getNombreCliente());
								}
								if(!estadoDeCuentaBancariox.getPeriodo().equals("")){
									nodeECB.setAttribute("periodo", estadoDeCuentaBancariox.getPeriodo());
								}
								if(!estadoDeCuentaBancariox.getSucursal().equals("")){
									nodeECB.setAttribute("sucursal", estadoDeCuentaBancariox.getSucursal());
								}
								
								NodeList hijosEstadoDeCuentaBancario = nodo2.getChildNodes();
								for(int k=0; k<hijosEstadoDeCuentaBancario.getLength(); k++){
									Node nodo3 = hijosEstadoDeCuentaBancario.item(k);
									if(nodo3 instanceof Element && nodo3.getNodeName().equals("Santander:Movimientos")){
										
										//Ordenar movimientos por fecha de forma descendente
//										System.out.println("Movimientos sin orden");
//										for(int iTest=0; iTest<lstMovimientosECBx.size(); iTest++){
//											System.out.println("fecha:" + lstMovimientosECBx.get(iTest).getFecha());
//											System.out.println("fechaOrden:" + lstMovimientosECBx.get(iTest).getFechaOrden());
//										}
										
										MovimientoECB [] arrayMov = new MovimientoECB[lstMovimientosECBx.size()];
										lstMovimientosECBx.toArray(arrayMov);
										Arrays.sort(arrayMov);
										
//										System.out.println("Movimientos ordenados");
//										for(int iTest=0; iTest<arrayMov.length; iTest++){
//											System.out.println("fecha:" + arrayMov[iTest].getFecha());
//											System.out.println("fechaOrden:" + arrayMov[iTest].getFechaOrden());
//										}
										
										for(int iMov=0; iMov<arrayMov.length; iMov++){
											//Element movEcb = domComprobante.createElement("ecb:MovimientoECB");
											Element movEcb;
											if(arrayMov[iMov].getFiscal()){
												movEcb = domResultado.createElement("Santander:MovimientoECBFiscal");
												
												if(!arrayMov[iMov].getRfcEnajenante().equals("")){
													movEcb.setAttribute("RFCenajenante", arrayMov[iMov].getRfcEnajenante());
												}			
												if(!arrayMov[iMov].getImporte().equals("")){
													movEcb.setAttribute("Importe", arrayMov[iMov].getImporte());
												}										
											}else{
												movEcb = domResultado.createElement("Santander:MovimientoECB");
												
												if(!arrayMov[iMov].getImporte().equals("")){
													movEcb.setAttribute("importe", arrayMov[iMov].getImporte());
												}
											}
																				
											if(!arrayMov[iMov].getFecha().equals("")){
												movEcb.setAttribute("fecha", arrayMov[iMov].getFecha());
											}
											if(!arrayMov[iMov].getReferencia().equals("")){
												movEcb.setAttribute("referencia", arrayMov[iMov].getReferencia());
											}
											if(!arrayMov[iMov].getDescripcion().equals("")){
												movEcb.setAttribute("descripcion", arrayMov[iMov].getDescripcion().toString());
											}									
											if(!arrayMov[iMov].getMoneda().equals("")){
												movEcb.setAttribute("moneda", arrayMov[iMov].getMoneda());
											}
											if(!arrayMov[iMov].getSaldoInicial().equals("")){
												movEcb.setAttribute("saldoInicial", arrayMov[iMov].getSaldoInicial());
											}
											if(!arrayMov[iMov].getSaldoAlCorte().equals("")){
												movEcb.setAttribute("saldoAlCorte", arrayMov[iMov].getSaldoAlCorte());
											}
											
											//domComprobante.getDocumentElement().getChildNodes().item(i).getChildNodes().item(j).getChildNodes().item(k).appendChild(movEcb);
											domResultado.getDocumentElement().getChildNodes().item(0).getChildNodes().item(i).getChildNodes().item(x).getChildNodes().item(j).getChildNodes().item(k).appendChild(movEcb);
										}				            								
									}
								}
							}
						}
					}
				}
			}
		}
		
		
		if ( addendaNew ) {
			String addendaNe = "";
			String addendaOperaciones = "";
			String addendaCobranzas = "";
			
			String complementos = "\n<Santander:Complemento ";
			
			complementos += "CURP=\""+oper.getCurp()+"\" ";
			complementos += "CodBar=\""+oper.getCodBar()+"\" />";
			
			
			if ( lstopEcb != null && lstopEcb.size() != 0 ) { 
				System.out.println("Santander:Operaciones");
				for( Operacion operacion : lstopEcb ) {
					String codBar  = "";
					if ( operacion.getCodBar() != null && !operacion.getCodBar().equals("") )
						codBar = "CodBar=\"" + operacion.getCodBar() + "\" ";
					
					String fechaPagoRecibo  = "";
					if ( operacion.getFechaPagoRecibo() != null && !operacion.getFechaPagoRecibo().equals("") )
						fechaPagoRecibo = "FechaPagoRecibo=\""+ operacion.getFechaPagoRecibo().trim() +"\" ";
					
					String numeroContrato  = "";
					if (  operacion.getNumeroContrato() != null && !operacion.getNumeroContrato().equals("")) 
						numeroContrato = "NumeroContrato=\""+operacion.getNumeroContrato().trim()+"\" ";
					
					String nombreDeudorOpe  = "";
					if (  operacion.getNombreDeudorOpe() != null && !operacion.getNombreDeudorOpe().equals("") ) 
						nombreDeudorOpe = "NombreDeudor=\""+operacion.getNombreDeudorOpe().trim()+"\" ";
					
					String rfcDeudorOpe  = "";
					if (  operacion.getRfcDeudorOpe() != null && !operacion.getRfcDeudorOpe().equals("") )
						rfcDeudorOpe = "RFCDeudor=\""+operacion.getRfcDeudorOpe().trim()+"\" ";
					
					String noDeudor  = "";
					if (  operacion.getNoDeudor() != null && !operacion.getNoDeudor().equals("") )
						noDeudor = "NoDocumento=\""+operacion.getNoDeudor().trim()+"\" ";
					
					String fechaVencimiento  = "";
					if (  operacion.getFechaVencimiento() != null && !operacion.getFechaVencimiento().equals("") ) 
						fechaVencimiento = "FechaVencimiento=\""+operacion.getFechaVencimiento().trim()+"\" ";
					
					String tasaDescInt  = "";
					if (  operacion.getTasaDescInt() != null && !operacion.getTasaDescInt().equals("") )
						tasaDescInt = "TasaDescInt=\""+operacion.getTasaDescInt().trim()+"\" ";
					
					String plazo  = "";
					if (  operacion.getPlazo() != null && !operacion.getPlazo().equals("") )
						plazo = "Plazo=\""+operacion.getPlazo().trim()+"\" ";
					
					String valorNominalOpe  = "";
					if (  operacion.getValorNominalOpe() != null && !operacion.getValorNominalOpe().equals("") )
						valorNominalOpe = "ValorNominal=\""+operacion.getValorNominalOpe()+"\" ";
					
					String descRend  = "";
					if (  operacion.getDescRend() != null && !operacion.getDescRend().equals("") )
						descRend = "DescRend=\""+operacion.getDescRend()+"\" ";
					
					String precioFactoraje  = "";
					if (  operacion.getPrecioFactoraje() != null && !operacion.getPrecioFactoraje().equals("") ) 
						precioFactoraje = "PrecioFactoraje=\""+operacion.getPrecioFactoraje()+"\" ";
					
					
					addendaOperaciones += Util.conctatArguments("\n<Santander:Operacion ", 
							codBar, 
							fechaPagoRecibo,
							numeroContrato,
							nombreDeudorOpe,
							rfcDeudorOpe,
							noDeudor,
							fechaVencimiento,
							tasaDescInt,
							plazo,
							valorNominalOpe,
							descRend,
							precioFactoraje, "/>")
							.toString();
					
				}
				
				addendaNe += "\n<Santander:Operaciones > "
						+ addendaOperaciones 
						+ "\n</Santander:Operaciones > ";
			}
			
			if ( lstcobEcb != null && lstcobEcb.size() != 0 ) { 
				System.out.println("Santander:Cobranzas");
				for ( Cobranza cobranza: lstcobEcb ) {
					
					String fechaCobro  = "";
					if ( cobranza.getFechaCobro() != null && !cobranza.getFechaCobro().equals("") )
						fechaCobro = "FechaCobro=\""+cobranza.getFechaCobro()+"\" ";
					
					String nombreDeudorCob  = "";
					if ( cobranza.getNombreDeudorCob() != null && !cobranza.getNombreDeudorCob().equals("") )
						nombreDeudorCob = "NombreDeudor=\""+cobranza.getNombreDeudorCob()+"\" ";
					
					String rfcDeudorCob  = "";
					if ( cobranza.getRfcDeudorCob() != null && !cobranza.getRfcDeudorCob().equals("") )
						rfcDeudorCob = "RFCDeudor=\""+cobranza.getRfcDeudorCob()+"\" ";
					
					String fechaCesion  = "";
					if ( cobranza.getFechaCesion() != null && !cobranza.getFechaCesion().equals("") )
						fechaCesion = "FechaCesion=\""+cobranza.getFechaCesion()+"\" ";
					
					String noDocumento  = "";
					if ( cobranza.getNoDocumento() != null && !cobranza.getNoDocumento().equals("") )
						noDocumento = "NoDocumento=\""+cobranza.getNoDocumento()+"\" ";
					
					String valorNominalCon  = "";
					if (cobranza.getValorNominalCon() != null && !cobranza.getValorNominalCon().equals("") )
						valorNominalCon = "ValorNominal=\""+cobranza.getValorNominalCon()+"\" ";
					
					String totalCobrado  = "";
					if ( cobranza.getTotalCobrado() != null && !cobranza.getTotalCobrado().equals("") )
						totalCobrado = "TotalCobrado=\""+cobranza.getTotalCobrado()+"\" ";
					
					addendaCobranzas += Util.conctatArguments("\n<Santander:Cobranza ", 
							fechaCobro, 
							nombreDeudorCob,
							rfcDeudorCob,
							fechaCesion,
							noDocumento,
							valorNominalCon,
							totalCobrado, "/>")
							.toString();
					
				}
				
				addendaNe += "\n<Santander:Cobranzas > "
						+ addendaCobranzas
						+ "\n</Santander:Cobranzas > ";
				
			}
			
			if ( !addendaOperaciones.trim().equals("") || !addendaCobranzas.trim().equals("") ) {
				
				String newAddenda = "\n<Santander:addendaConfirming xmlns:Santander=\"http://www.santander.com.mx/schemas/xsd/addendaConfirming\">"
						+ complementos
						+ addendaNe
						+ "\n</Santander:addendaConfirming >"
						+ "\n</cfdi:Addenda>";
				
				
				String xmlString2 = "", xmlFinal = "";
				try {
					xmlString2 = UtilCatalogos.convertDocumentXmlToString(domResultado);
					String strXmlString = "";
					strXmlString = xmlString2.replace("</cfdi:Addenda>", newAddenda);
					
					
					xmlFinal = strXmlString.replaceAll("[\n\r]", "");
					
					domResultado = UtilCatalogos.convertStringToDocument(xmlFinal);
					
				} catch (TransformerConfigurationException e) {
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				
				
			}
			
			
			
			
			
		}
		
		// Agrega la addenda domicilios
		if (addendaDomiciliosNodeStr.peek() != null && !addendaDomiciliosNodeStr.peek().trim().isEmpty() ) {
			String xmlString2 = "", xmlFinal = "";
			try {
				xmlString2 = UtilCatalogos.convertDocumentXmlToString(domResultado);
			} catch (TransformerConfigurationException e) {
				e.printStackTrace();
			} catch (TransformerException e) {
				e.printStackTrace();
			}
			String strAddendaComp = "";
			strAddendaComp = "<cfdi:Addenda>"+addendaDomiciliosNodeStr.poll() + "</cfdi:Addenda></cfdi:Comprobante>";
			String strXmlString = "";
			strXmlString = xmlString2.replace("</cfdi:Comprobante>", strAddendaComp);
			xmlFinal = strXmlString.replaceAll("[\n\r]", "");
			try {
				domResultado = UtilCatalogos.convertStringToDocument(xmlFinal);
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			String folio = UtilCatalogos.getStringValByExpression(domResultado, "//Comprobante/@Folio");
			if (this.nameFile.contains("LMPAMPAA") || this.nameFile.contains("LMPAMPAS")) {
				File file = new File(this.getName(properties.getPathSalida(), "REC"));
				if (file.exists()) {
					if (file.canRead()) {
						DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
						Date dateInicio = new Date();
						System.out.println("TIMEINICIOADDRECXD:" + dateFormat.format(dateInicio) + "M" + System.currentTimeMillis());
						
						try {
							Runtime rt = Runtime.getRuntime();
							String[] cmd = {"/bin/sh", "-c", "grep -a '"+folio+"' " + this.getName(properties.getPathSalida(), "REC") };
							Process proc = rt.exec(cmd);
							BufferedReader is = new BufferedReader(new InputStreamReader(proc.getInputStream()));
							String linea = "";
							if ((linea = is.readLine()) != null) {
								System.out.println(linea);
								String addendaRec = "";
								String datos[] = linea.trim().split("\\|");
								if (!linea.equalsIgnoreCase("") && linea.length() >= 10) {
									
									addendaRec = "<Santander:AddendaRecompensa>" + 
											"<Santander:AddendaRec "
											+ "Mensaje=\"Se le informa que el valor de las redenciones de puntos realizadas durante el periodo:"
											+ " fecha inicio "+datos[2]+" fecha fin "+datos[3]+" es por la cantidad de "+datos[1]+ "\" />"
											+ "</Santander:AddendaRecompensa></Santander:addendaECB>";
								}
								
								System.out.println("ADDENDA: " + addendaRec);
								
								String rec = "<Santander:AddendaRecompensa>" + 
										"<Santander:AddendaRec "
										+ "Mensaje=\"Se le informa que el valor de las redenciones de puntos realizadas durante el periodo:"
										+ " fecha inicio "+datos[2]+" fecha fin "+datos[3]+" es por la cantidad de "+datos[1]+ "\" />"
										+ "</Santander:AddendaRecompensa></Santander:addendaECB>";
								
								String xmlString2 = "", xmlFinal = "";
								xmlString2 = UtilCatalogos.convertDocumentXmlToString(domResultado);
								
								String strXmlString = "";
								strXmlString = xmlString2.replace("</Santander:addendaECB>", rec);
								
								
								xmlFinal = strXmlString.replaceAll("[\n\r]", "");
								
								domResultado = UtilCatalogos.convertStringToDocument(xmlFinal);
								
								
								if ((linea = is.readLine()) != null)
									System.out.println(linea);
							}
							
						} catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
							System.out.println(e.getMessage());
						}
						System.out.println("TIMEFINADDRECXD:" + dateFormat.format(dateInicio) + "M" + System.currentTimeMillis());
					}
				}
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		
		return domResultado;
	}
			
	/**
	 * Finaliza la creacion del ECB
	 * @param decremento
	 * @throws IOException
	 */
	private void end(int decremento, String idProceso, String fechaCtlM, String fileNames, String numeroMalla) 
		throws IOException 
	{
		if (conver.getTags().isComprobante) 
		{
			out = conver.cfdiRelacionado(out);
			out.write("\n</cfdi:Comprobante>".getBytes("UTF-8"));
			conver.getTags().isComprobante = false;
			try 
			{	long t1 = System.currentTimeMillis();
				endLine = "" + (contCFD - decremento);
				if (conver.getTags().isFormat) 
				{
					StringBuffer numberLines = new StringBuffer();
					for (String error : conver.getDescriptionFormat()) 
					{
						//System.out.println("TIME: getDescriptionFormat ERR:" + error + " ms");
						numberLines.append(error);
						numberLines.append(" ");
					}
					throw new Exception("Estructura Incorrecta " + numberLines.toString());
				}
				
				long t2 = t1- System.currentTimeMillis();
				if (conver.getTags().isEntidadFiscal) 
				{	
					t1 = System.currentTimeMillis();
					
					certificate = null;
					Calendar cal = Calendar.getInstance();
					for (SealCertificate obj : lstSeal) 
					{
						if (obj.getFiscalEntity().getId() == conver.getTags().fis.getId()) 
						{
							if (obj.getStartOfValidity().before(cal.getTime())) 
							{
								if (obj.getEndOfValidity().after(cal.getTime())) 
								{
									certificate = obj;
									break;
								}
							}
						}
					}
					
					t2 = t1- System.currentTimeMillis();
					
					if (certificate == null) 
					{
						throw new Exception(
								"No existe certificado para la entidad fiscal"
										+ conver.getTags().fis.getFiscalName());
					} 
					else 
					{					
						/*
						t1 = System.currentTimeMillis();
						
						String fecha = Util.systemDate();
						
						String nameFile = xmlProcess.setName(fecha, true,
								conver.getTags().NUM_CTA,
								conver.getTags().EMISION_PERIODO, false, idProceso);
						
						out = Util.enconding(out);
						logger.info(out.toString());
						xmlProcess.getValidator().valida(out, this.validator);
						conver.getTags().NUM_CERTIFICADO = certificate.getSerialNumber();
						xmlProcess.setTransf(transf);
						
						t2 = t1- System.currentTimeMillis();
						System.out.println("TIME: xmlProcess:" + t2 + " ms");
							
						 
						 */
						out = Util.enconding(out);

						/* Valida Si el ECB esta en Ceros y en caso de ser asi agrega el descuento */
						if(conver.getTags().isECBEnCeros) {
							String xml = out.toString("UTF-8");
							xml = xml.replace("Fecha=", "Descuento=\"0.01\" Fecha=");
							out = UtilCatalogos.convertStringToOutpuStream(xml);
							Document docChangeSubTotal = byteArrayOutputStreamToDocument(out);
							UtilCatalogos.setValueOnDocumentElement(docChangeSubTotal, "//Comprobante/@SubTotal", "0.01");
							out = UtilCatalogos.convertStringToOutpuStream(UtilCatalogos.convertDocumentXmlToString(docChangeSubTotal));
							conver.getTags().isECBEnCeros=false;
						}

						/*Validaciones 3.3*/
						UtilCatalogos.lstErrors = new StringBuffer();
						Document dom = byteArrayOutputStreamToDocument(out);
						Element root = dom.getDocumentElement();
						UtilCatalogos.evaluateNodesError(root);
						if(!UtilCatalogos.lstErrors.toString().isEmpty()){
							throw new Exception(UtilCatalogos.lstErrors.toString());
						}
						String errors = UtilCatalogos.validateCfdiDocument(dom,conver.getTags().decimalesMoneda);
						if (errors != null && !errors.isEmpty()) {
							throw new Exception(errors);
						}
						out = UtilCatalogos.convertStringToOutpuStream(UtilCatalogos.convertDocumentXmlToString(dom));
						/*Fin Validaciones 3.3*/ 
						try{
														
							//if(lstObjECBs.size()<26643 ){
								t1 = System.currentTimeMillis();
								//En caso de ser CFDREPROCESOECB, validar que el nombre de aplicativo informado en cada comprobante, sea correcto
								/*if(fileNames.trim().equals("CFDREPROCESOECB") && ! NombreAplicativo.validaNombreApp(getNombresApps(), conver.getTags().NOMBRE_APP_REPECB))
									throw new Exception(
											"El nombre de Aplicativo informado " + conver.getTags().NOMBRE_APP_REPECB + ", es incorrecto!");
								else if(!fileNames.trim().equals("CFDREPROCESOECB") && conver.getTags().NOMBRE_APP_REPECB.equals(""))
									throw new Exception(
											"El nombre de Aplicativo no existe para la interface " + fileNames);*/
								
								//ecbActual++;
								ECB objEcbActual = new ECB();
								this.lstMovimientosECB = new ArrayList<MovimientoECB>();						
								this.estadoDeCuentaBancario = new EstadoDeCuentaBancario();
								this.lstCobranzas = new ArrayList<Cobranza>();
								this.lstOperaciones = new ArrayList<Operacion>();
								this.complementarios = new Operacion();
								
								//System.out.println("XML formado a partir de la interface: " + out.toString("UTF-8"));
								
								//Valida el parse del XML completo y extrae el nodo addenda							
								
								//Inicio - Quitar todos los movimientos no fiscales del XML almacenado en la variable out
								//Manipular con Document el xml obtenido de la variable out					
//								dom = this.removeAddendaDomicilio(byteArrayOutputStreamToDocument(out));
								dom = this.removeMovimientoECB(byteArrayOutputStreamToDocument(out));
								out = UtilCatalogos.convertStringToOutpuStream(UtilCatalogos.convertDocumentXmlToString(dom));
								dom = this.removeAddendaNew(byteArrayOutputStreamToDocument(out));
								//Fin - Quitar todos los movimientos no fiscales del XML almacenado en la variable out
								//System.out.println("flags: fAttMovIncorrect:" + this.fAttMovIncorrect + " fnombreCliente:" + this.fnombreCliente + " fnumeroCuenta:" + this.fnumeroCuenta + " fperiodo:" + this.fperiodo + " fsucursal:" + this.fsucursal);
								if(!this.fAttMovIncorrect && !this.fnombreCliente && !this.fnumeroCuenta && !this.fperiodo && !this.fsucursal){
									objEcbActual.setLstMovimientosECB(this.lstMovimientosECB);
									objEcbActual.setEstadoDeCuentaBancario(this.estadoDeCuentaBancario);
									objEcbActual.setLstCobranzaECB(this.lstCobranzas);
									objEcbActual.setLstOperacionesECB(this.lstOperaciones);
									objEcbActual.setOperacion(complementarios);
									
									if ( this.lstCobranzas.size() != 0 || this.lstOperaciones.size() != 0 ) {
										objEcbActual.setAddendaNew(true);
									} else {
										objEcbActual.setAddendaNew(false);
									}
									
									
									//Convertir de Document a StringWriter
									StringWriter sw2 = documentToStringWriter(dom);									
												
									StringBuffer sb= new StringBuffer();								
									//Convetir de StringWriter a StringBuffer
									sb = sw2.getBuffer();
									
									//System.out.println("XMLSINADDENDA:" + sb.toString());
									
									//MAnipular el stringbuffer (sin MovimientosECB no fiscales) y convertirlo a ByteArrayOutputStream
									ByteArrayOutputStream xmlFinal =  new ByteArrayOutputStream();
									byte [] xmlFinalBytes = sb.toString().getBytes("UTF-8");
									xmlFinal.write(xmlFinalBytes);		
									
									//Validar estructura del comprobante sin addenda
									
									t1 = System.currentTimeMillis();
									
									/*String fecha = Util.systemDate();
									
									String nameFile = xmlProcess.setName(fecha, true,
											conver.getTags().NUM_CTA,
											conver.getTags().EMISION_PERIODO, false, idProceso);*/
									
									//xmlFinal = Util.enconding(xmlFinal);
									//logger.info("xmlFinal:" + xmlFinal.toString());
									//Valida el XML sin Addenda
									boolean fValidaXMLSinAddenda = true;
									try{ // Solo se comenta para pruebas AMDA
										xmlProcess.getValidator().valida(xmlFinal, this.validator); // Al Parecer Aqui Valida AMDA
									}catch(Exception ex){
										fValidaXMLSinAddenda = false;
										logger.error(ex);
										fileINCIDENCIA(ex.getMessage() + " ", "ERROR", 
												conver.getTags().EMISION_RFC, conver.getTags().NUM_CTE, conver.getTags().NUM_CTA, conver.getTags().EMISION_PERIODO, conver.getTags().NUM_TARJETA, conver.getTags().CFD_TYPE);
										
										//Generar Archivo de incidentes para Cifras (ERR...TXT)
										fileINCIDENCIACIFRAS(ex.getMessage() + " ", "ERROR", 
												conver.getTags().EMISION_RFC, conver.getTags().NUM_CTE, conver.getTags().NUM_CTA, conver.getTags().EMISION_PERIODO, conver.getTags().NUM_TARJETA, conver.getTags().CFD_TYPE,
												conver.getTags().SUBTOTAL_MN, conver.getTags().TOTAL_IMP_TRA, conver.getTags().TOTAL_IMP_RET, fileNames, conver.getTags().SERIE_FISCAL_CFD, numeroMalla);
										System.out.println("XML error validacion: " + xmlFinal.toString("UTF-8"));
									}
									
									if(fValidaXMLSinAddenda){
										conver.getTags().NUM_CERTIFICADO = certificate.getSerialNumber();
										xmlProcess.setTransf(transf);
										
										t2 = System.currentTimeMillis()- t1;
										System.out.println("TIME: xmlProcess:" + t2 + " ms");
										
										Document doc = UtilCatalogos.convertStringToDocument(xmlFinal.toString("UTF-8"));
										
								    	if (UtilCatalogos.getStringValByExpression(doc, "//Comprobante//@Moneda").equalsIgnoreCase("MXN")) {
								    		UtilCatalogos.setValueOnDocumentElement(doc, "//Comprobante/@TipoCambio", "1");
								    	}
								    	System.out.println("XMLtipoxd: " + xmlFinal.toString("UTF-8"));
										/*Se asigna el NoCertificado ya que antes se hacia despues de generar la cadena original*/
										doc = UtilCatalogos.convertStringToDocument(xmlFinal.toString("UTF-8"));
										UtilCatalogos.setValueOnDocumentElement(doc, "//Comprobante/@NoCertificado", certificate.getSerialNumber());
										xmlFinal = UtilCatalogos.convertStringToOutpuStream(UtilCatalogos.convertDocumentXmlToString(doc));
										/*Fin asignaciones*/
										
										ByteArrayOutputStream originalString = xmlProcess.generatesOriginalString(xmlFinal);
										String cadena = originalString.toString("UTF-8");
										//System.out.println("Esta es la cadena: " + cadena);
										//System.out.println("Este es properties.getLabelMetodoPago: " + properties.getLabelMetodoPago());
										//System.out.println("Este es conver.getTags.METODO_PAGO: " + conver.getTags().METODO_PAGO);
										cadena = cadena.replaceFirst(properties.getLabelLugarExpedicion(), conver.getTags().LUGAR_EXPEDICION);
										cadena = cadena.replaceFirst(properties.getLabelMetodoPago(), conver.getTags().METODO_PAGO);
										cadena = cadena.replaceFirst(properties.getlabelFormaPago(), conver.getTags().FORMA_PAGO);
										
										byte[] cadenaBytes = cadena.getBytes("UTF-8");
										originalString = new ByteArrayOutputStream();
										originalString.write(cadenaBytes, 0, cadenaBytes.length);
										objEcbActual.setOriginalString(originalString);
										
										long t1b = System.currentTimeMillis();
										//System.out.println("originalString: " + originalString.toString("UTF-8"));
										String seal = xmlProcess.sealEncryption(originalString, certificate);
										long t2b =  System.currentTimeMillis() - t1b;
										System.out.println("TIME: EncriptionFuera:" + t2b + " ms");

										objEcbActual.setSeal(seal);
										
										byte[] bytesToWrite = xmlProcess.replacesOriginalString(xmlFinal, certificate, seal, 
												conver.getTags().LUGAR_EXPEDICION, conver.getTags().METODO_PAGO, conver.getTags().FORMA_PAGO, true).toByteArray();
										
										ByteArrayOutputStream xmlSinECB = new ByteArrayOutputStream();
										xmlSinECB.write(bytesToWrite, 0, bytesToWrite.length);
										objEcbActual.setXmlSinECB(xmlSinECB);
										
										//System.out.println("XML sin ECB a timbrar: " + xmlSinECB.toString("UTF-8"));
										objEcbActual.setTagEMISON_RFC(conver.getTags().EMISION_RFC);
										objEcbActual.setTagNUM_CTE(conver.getTags().NUM_CTE);
										objEcbActual.setTagNUM_CTA(conver.getTags().NUM_CTA);
										objEcbActual.setTagEMISION_PERIODO(conver.getTags().EMISION_PERIODO);
										objEcbActual.setTagNUM_TARJETA(conver.getTags().NUM_TARJETA);
										//System.out.println("conver.getTags().FECHA_CFD:"+ conver.getTags().FECHA_CFD);
										objEcbActual.setTagFECHA_CFD(conver.getTags().FECHA_CFD);
										objEcbActual.setTagSERIE_FISCAL_CFD(conver.getTags().SERIE_FISCAL_CFD);
										objEcbActual.setTagFOLIO_FISCAL_CFD(conver.getTags().FOLIO_FISCAL_CFD);
										objEcbActual.setTagYEAR_APROBACION(conver.getTags().YEAR_APROBACION);
										objEcbActual.setTagNUM_CERTIFICADO(conver.getTags().NUM_CERTIFICADO);
										objEcbActual.setTagNUM_APROBACION(conver.getTags().NUM_APROBACION);
										objEcbActual.setTagUNIDAD_MEDIDA(conver.getTags().UNIDAD_MEDIDA);
										objEcbActual.setTagLUGAR_EXPEDICION(conver.getTags().LUGAR_EXPEDICION);
										objEcbActual.setTagMETODO_PAGO(conver.getTags().METODO_PAGO);
										objEcbActual.setTagREGIMEN_FISCAL(conver.getTags().REGIMEN_FISCAL);
										objEcbActual.setTagFORMA_PAGO(conver.getTags().FORMA_PAGO);
										objEcbActual.setTagTIPO_CAMBIO(conver.getTags().TIPO_CAMBIO);
										objEcbActual.setTagTIPO_MONEDA(conver.getTags().TIPO_MONEDA);
										objEcbActual.setTagLONGITUD(conver.getTags().LONGITUD);
										
										objEcbActual.setTagFis_ID(conver.getTags().fis.getId());
										objEcbActual.setTagRECEPCION_RFC(conver.getTags().RECEPCION_RFC);
										objEcbActual.setTagIVATOTAL_MN(conver.getTags().IVA_TOTAL_MN);
										objEcbActual.setTagSUBTOTAL_MN(conver.getTags().SUBTOTAL_MN);
										objEcbActual.setTagTOTAL_MN(conver.getTags().TOTAL_MN);
										objEcbActual.setTagTIPO_FORMATO(conver.getTags().TIPO_FORMATO);
										objEcbActual.setTagCFD_TYPE(conver.getTags().CFD_TYPE);
										
										objEcbActual.setTagTOTAL_IMP_RET(conver.getTags().TOTAL_IMP_RET);
										objEcbActual.setTagTOTAL_IMP_TRA(conver.getTags().TOTAL_IMP_TRA);
										
										objEcbActual.setStartLine(this.startLine);
										objEcbActual.setEndLine(this.endLine);
										
										//Guardar Nombre de aplicativo
										objEcbActual.setTagNOMBRE_APP_REPECB(conver.getTags().NOMBRE_APP_REPECB);
										
										this.lstObjECBs.add(objEcbActual);
										
										t2 = System.currentTimeMillis() - t1;
										System.out.println("TIME: Procesar ECB:" + t2 + " ms");									
									}
									
								}else{		
									String strMsgError = "";
									if(this.fnumeroCuenta){
										strMsgError = "Informacion no valida en el atributo numeroCuenta del nodo Santander:EstadoDeCuentaBancario";
									}else if(this.fnombreCliente){
										strMsgError = "Informacion no valida en el atributo nombreCliente del nodo Santander:EstadoDeCuentaBancario";
									}else if(this.fperiodo){
										strMsgError = "Informacion no valida en el atributo periodo del nodo Santander:EstadoDeCuentaBancario";
									}else if(this.fsucursal){
										strMsgError = "Informacion no valida en el atributo sucursal del nodo Santander:EstadoDeCuentaBancario";
									}else{
										strMsgError = "Informacion no valida dentro de Santander:Movimientos";										
									}
									System.out.println("ERROR: " + strMsgError);
									fileINCIDENCIA(strMsgError, "ERROR", 
										conver.getTags().EMISION_RFC, conver.getTags().NUM_CTE, conver.getTags().NUM_CTA, conver.getTags().EMISION_PERIODO, conver.getTags().NUM_TARJETA, conver.getTags().CFD_TYPE);
									
									//Generar Archivo de incidentes para Cifras (ERR...TXT)
									
									fileINCIDENCIACIFRAS(strMsgError, "ERROR", 
											conver.getTags().EMISION_RFC, conver.getTags().NUM_CTE, conver.getTags().NUM_CTA, conver.getTags().EMISION_PERIODO, conver.getTags().NUM_TARJETA, conver.getTags().CFD_TYPE,
											conver.getTags().SUBTOTAL_MN, conver.getTags().TOTAL_IMP_TRA, conver.getTags().TOTAL_IMP_RET, fileNames, conver.getTags().SERIE_FISCAL_CFD, numeroMalla);
								}
								
																				
						}catch(Exception e){							
							e.printStackTrace();
							logger.error(e);
							fileINCIDENCIA(e.getMessage() + " ", "ERROR", 
									conver.getTags().EMISION_RFC, conver.getTags().NUM_CTE, conver.getTags().NUM_CTA, conver.getTags().EMISION_PERIODO, conver.getTags().NUM_TARJETA, conver.getTags().CFD_TYPE);
							
							//Generar Archivo de incidentes para Cifras (ERR...TXT)
							fileINCIDENCIACIFRAS(e.getMessage() + " ", "ERROR", 
									conver.getTags().EMISION_RFC, conver.getTags().NUM_CTE, conver.getTags().NUM_CTA, conver.getTags().EMISION_PERIODO, conver.getTags().NUM_TARJETA, conver.getTags().CFD_TYPE,
									conver.getTags().SUBTOTAL_MN, conver.getTags().TOTAL_IMP_TRA, conver.getTags().TOTAL_IMP_RET, fileNames, conver.getTags().SERIE_FISCAL_CFD, numeroMalla);
						}										
					}
				} 
				else 
				{	throw new Exception("No existe la entidad fiscal con R.F.C.: " + conver.getTags().EMISION_RFC);		}
			} 
			catch (Exception ex) 
			{
				logger.info(out.toString());
				logger.error(ex);
				msgError = ex.getMessage();	
				String typeIncidence="ERROR";
				if(msgError!=null && msgError.contains("The transaction has been rolled back"))
				{	typeIncidence="WARNING";	} 
				if(msgError!=null && msgError.contains("ORA-08177: can't serialize access for this transaction"))
				{	typeIncidence="WARNING";	}
				
				try 
				{	this.fileINCIDENCIA(ex,typeIncidence,
						conver.getTags().EMISION_RFC, conver.getTags().NUM_CTE, conver.getTags().NUM_CTA, conver.getTags().EMISION_PERIODO, conver.getTags().NUM_TARJETA, conver.getTags().CFD_TYPE);
					
					//Generar Archivo de incidentes para Cifras (ERR...TXT)
					this.fileINCIDENCIACIFRAS(ex,typeIncidence,
						conver.getTags().EMISION_RFC, conver.getTags().NUM_CTE, conver.getTags().NUM_CTA, conver.getTags().EMISION_PERIODO, conver.getTags().NUM_TARJETA, conver.getTags().CFD_TYPE,
						conver.getTags().SUBTOTAL_MN, conver.getTags().TOTAL_IMP_TRA, conver.getTags().TOTAL_IMP_RET, fileNames, conver.getTags().SERIE_FISCAL_CFD, numeroMalla);
				} 
				catch (Exception e) 
				{	lstECBIncidence.add(this.setCFDIncidence(msgError,typeIncidence));	} 
				finally 
				{
					conver.setDescriptionFormat(null);
					logger.error(ex.getLocalizedMessage(), ex);
				}
			} 
			finally 
			{	cont += 1;	}
		}
	}
	
	private Document removeAddendaNew(Document dom) throws Exception {
		
		this.attDobleAddIncorrect = false;
		
		Node nodeAddenda = null;

		Element root = dom.getDocumentElement();
		
		String domStr="";
		domStr=UtilCatalogos.convertDocumentXmlToString(dom);
		System.out.println("xml2ADDXD: " + domStr);
		
		//Recorrer los nodos hijos de cfdi:Addenda														
		for(int i=0; i<root.getChildNodes().getLength(); i++){
			
			//Verificar si el hijo actual corresponde a una instancia de Element y se llama cfdi:Addenda
			
			if(root.getChildNodes().item(i) instanceof Element && 
					root.getChildNodes().item(i).getNodeName().equals("cfdi:Addenda")){
				
					nodeAddenda = root.getChildNodes().item(i).cloneNode(true);										
					System.out.println("addendaXD");
					for(int x=0; x<root.getChildNodes().item(i).getChildNodes().getLength(); x++){
						if(root.getChildNodes().item(i).getChildNodes().item(x) instanceof Element && 
								root.getChildNodes().item(i).getChildNodes().item(x).getNodeName().equals("Santander:addendaConfirming")){
							System.out.println("Santander:addendaConfirming");
							for(int j=0; j<root.getChildNodes().item(i).getChildNodes().item(x).getChildNodes().getLength(); j++){
								
								//Verificar si el hijo actual corresponde a una instancia de Element y se llama Santander:Operaciones
								if(root.getChildNodes().item(i).getChildNodes().item(x).getChildNodes().item(j) instanceof Element && 
										root.getChildNodes().item(i).getChildNodes().item(x).getChildNodes().item(j).getNodeName().equals("Santander:Operaciones")){
									System.out.println("Santander:Operaciones");									
									//Recorrer los hijos de Santander:Operaciones
									for(int k=0; k<root.getChildNodes().item(i).getChildNodes().item(x).getChildNodes().item(j).getChildNodes().getLength(); k++){
										
										//Verificar si el hijo actual corresponde a una instancia de Element y se llama Santander:Operacion
										if(root.getChildNodes().item(i).getChildNodes().item(x).getChildNodes().item(j).getChildNodes().item(k) instanceof Element && 
										root.getChildNodes().item(i).getChildNodes().item(x).getChildNodes().item(j).getChildNodes().item(k).getNodeName().equals("Santander:Operacion")){
											System.out.println("Santander:Operacion");
											Operacion operEcb = new Operacion();
												
											NamedNodeMap atributos = root.getChildNodes().item(i).getChildNodes().item(x).getChildNodes().item(j).getChildNodes().item(k).getAttributes();
																							
											for(int iAtt=0; iAtt< atributos.getLength(); iAtt++){
												Attr atributo = (Attr) atributos.item(iAtt);
												if(atributo.getName().equals("CodBar")){	
													this.existFecha = true;
													if (this.valorVacio(atributo.getValue())){
														this.attDobleAddIncorrect = true;	
														break;
													}else{
														operEcb.setCodBar(atributo.getValue());
													}																
												}else if(atributo.getName().equals("FechaPagoRecibo")){	
													operEcb.setFechaPagoRecibo(atributo.getValue());																
												}else if(atributo.getName().equals("NumeroContrato")){																
													operEcb.setNumeroContrato(atributo.getValue());																	
												}else if(atributo.getName().equals("NombreDeudor")){																
													operEcb.setNombreDeudorOpe(atributo.getValue());															
												}else if(atributo.getName().equals("RFCDeudor")){																
													operEcb.setRfcDeudorOpe(atributo.getValue());																	
												}else if(atributo.getName().equals("NoDocumento")){																
													operEcb.setNoDeudor(atributo.getValue());																
												}else if(atributo.getName().equals("FechaVencimiento")){																
													operEcb.setFechaVencimiento(atributo.getValue());															
												}else if(atributo.getName().equals("TasaDescInt")){																
													operEcb.setTasaDescInt(atributo.getValue());
												}else if(atributo.getName().equals("Plazo")){																
													operEcb.setPlazo(atributo.getValue());
												}else if(atributo.getName().equals("ValorNominal")){																
													operEcb.setValorNominalOpe(atributo.getValue());
												}else if(atributo.getName().equals("DescRend")){																
													operEcb.setDescRend(atributo.getValue());
												}else if(atributo.getName().equals("PrecioFactoraje")){																
													operEcb.setPrecioFactoraje(atributo.getValue());
												}
											}
																								
											lstOperaciones.add(operEcb);																											
										}
										
										
										
									} 
									
								} else 	if(root.getChildNodes().item(i).getChildNodes().item(x).getChildNodes().item(j) instanceof Element && 
										root.getChildNodes().item(i).getChildNodes().item(x).getChildNodes().item(j).getNodeName().equals("Santander:Cobranzas")){
									//Recorrer los hijos de Santander:Operaciones
									System.out.println("Santander:Cobranzas");
									for(int k=0; k<root.getChildNodes().item(i).getChildNodes().item(x).getChildNodes().item(j).getChildNodes().getLength(); k++){
										
										//Verificar si el hijo actual corresponde a una instancia de Element y se llama Santander:Operacion
										if(root.getChildNodes().item(i).getChildNodes().item(x).getChildNodes().item(j).getChildNodes().item(k) instanceof Element && 
										root.getChildNodes().item(i).getChildNodes().item(x).getChildNodes().item(j).getChildNodes().item(k).getNodeName().equals("Santander:Cobranza")){
											System.out.println("Santander:Cobranza");
											Cobranza cobECB = new Cobranza();
											
											NamedNodeMap atributos = root.getChildNodes().item(i).getChildNodes().item(x).getChildNodes().item(j).getChildNodes().item(k).getAttributes();
																							
											for(int iAtt=0; iAtt< atributos.getLength(); iAtt++){
												Attr atributo = (Attr) atributos.item(iAtt);
												if(atributo.getName().equals("FechaCobro")){	
													cobECB.setFechaCobro(atributo.getValue());															
												}else if(atributo.getName().equals("NombreDeudor")){	
													cobECB.setNombreDeudorCob(atributo.getValue());																
												}else if(atributo.getName().equals("RFCDeudor")){																
													cobECB.setRfcDeudorCob(atributo.getValue());																	
												}else if(atributo.getName().equals("FechaCesion")){																
													cobECB.setFechaCesion(atributo.getValue());															
												}else if(atributo.getName().equals("NoDocument")){																
													cobECB.setNoDocumento(atributo.getValue());																	
												}else if(atributo.getName().equals("ValorNominal")){																
													cobECB.setValorNominalCon(atributo.getValue());																
												}else if(atributo.getName().equals("TotalCobrado")){																
													cobECB.setTotalCobrado(atributo.getValue());															
												}
																																							
											}
											lstCobranzas.add(cobECB);
										
										} 
									}
								} else if(root.getChildNodes().item(i).getChildNodes().item(x).getChildNodes().item(j) instanceof Element && 
											root.getChildNodes().item(i).getChildNodes().item(x).getChildNodes().item(j).getNodeName().equals("Santander:Complemento")){
									
									NamedNodeMap atributos = root.getChildNodes().item(i).getChildNodes().item(x).getChildNodes().item(j).getAttributes();
									for(int iAtt=0; iAtt< atributos.getLength(); iAtt++){
										Attr atributo = (Attr) atributos.item(iAtt);
										if(atributo.getName().equals("CURP")){	
											complementarios.setCurp(atributo.getValue());															
										}else if(atributo.getName().equals("CodBar")){	
											complementarios.setCodBar(atributo.getValue());																
										}
									}
								}
							}
						}
					}
				}
			}
		
		
			if(nodeAddenda != null){
				//Recorrer los nodos hijos de cfdi:Comprobante
				int i=0;
				boolean fDelAddenda=false;
				System.out.println("removeAddXD:");
				do{			
					//Verificar si el hijo actual corresponde a una instancia de Element y se llama cfdi:Complemento
					if(root.getChildNodes().item(i) instanceof Element && 
							root.getChildNodes().item(i).getNodeName().equals("cfdi:Addenda")){						
							root.removeChild(dom.getDocumentElement().getChildNodes().item(i));											
							fDelAddenda = true;
					}
					i++;
				}while(i<root.getChildNodes().getLength() && !fDelAddenda);
				
			}
			
			
			domStr=UtilCatalogos.convertDocumentXmlToString(dom);
			
			System.out.println("xmlSINADDXD: " + domStr);
			return dom;
		
	}
	
	public void fileSALIDA(List<String> sello, String cadena, String fileNames, String strUUID, String strFechaTimbrado, String strNoCertificadoSAT, String strSelloCFD,
			String strSelloSAT, String strVersion, String strEmisorRFC, String strReceptorRFC, String strTotalZeros) 
		throws IOException, ParseException 
	{
		for  (int i = 0; i < sello.size(); i++)
		{
			//fileSALIDA(sello.get(i), cadena.get(), String fileNames, String strUUID, String strFechaTimbrado, String strNoCertificadoSAT, String strSelloCFD,
			//		String strSelloSAT, String strVersion, String strEmisorRFC, String strReceptorRFC, String strTotalZeros);
		}
	}
	/**
	 * 
	 * @param sello
	 * @param cadena
	 * @throws IOException
	 * @throws ParseException
	 */
	public void fileSALIDA(ECB objECB, String fileNames, String strUUID, String strFechaTimbrado, String strNoCertificadoSAT, String strSelloCFD,
			String strSelloSAT, String strVersion, String strEmisorRFC, String strReceptorRFC, String strTotalZeros) 
		throws IOException, ParseException 
	{/*
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date dateInicio = new Date();
		System.out.println("TIMEINICIOEscritura archivo respuesta:" + dateFormat.format(dateInicio) + " M" + System.currentTimeMillis());
		*/
		long t1 = System.currentTimeMillis();
		temp = "CFD|"
				+ objECB.getTagEMISON_RFC()
				+ "|"
				+ objECB.getTagNUM_CTE()
				+ "|"
				+ objECB.getTagNUM_CTA()
				+ "|"
				+ objECB.getTagEMISION_PERIODO()
				+ "|"
				+ objECB.getTagNUM_TARJETA()
				+ "|"
				+ objECB.getTagFECHA_CFD()
				+ "|"
				+ objECB.getTagSERIE_FISCAL_CFD() + "|"
				+ objECB.getTagFOLIO_FISCAL_CFD() + "|"
				+ objECB.getTagYEAR_APROBACION() + "|"
				+ objECB.getTagNUM_CERTIFICADO() + "|"
				+ objECB.getTagNUM_APROBACION();
		//+ Util.convertirFecha(objECB.getTagEMISION_PERIODO(),
		//objECB.getTagFECHA_CFD()) + "|"
		System.out.println("Bloque AMDA SALIDA 3 : " );
		//Obtener interfaces que llevaran los campos
		String strInterfaces = properties.getInterfaces(); 
		String [] interfacesCfdFields = strInterfaces.split(",");		
		
		//Verificar si se esta evaluando CFDOPMEXD21, CFDOPMEXD22 o CFDCHICAGO
		boolean fMexder = false;
		if(interfacesCfdFields.length > 0){
			String[] fileNamesArr = fileNames.split(",");
			for (int i=0; i < fileNamesArr.length; i++)
			{	
				for(int j=0; j < interfacesCfdFields.length; j++){
					if(fileNamesArr[i].equals(interfacesCfdFields[j].trim().toUpperCase())){
						fMexder=true;	
						break;
					}
				}				
			}
		}
		
		
		if (fMexder) {
			temp = temp + "|"
					+ (this.catalogoCincoCampos33.containsKey("unidadMedida")
							? this.catalogoCincoCampos33.get("unidadMedida")
							: "")
					+ "|"
					+ (this.catalogoCincoCampos33.containsKey("lugarExpedicion")
							? this.catalogoCincoCampos33.get("lugarExpedicion")
							: "")
					+ "|"
					+ (this.catalogoCincoCampos33.containsKey("metodoPago")
							? this.catalogoCincoCampos33.get("metodoPago")
							: "")
					+ "|"
					+ (this.catalogoCincoCampos33.containsKey("regimenFiscal")
							? this.catalogoCincoCampos33.get("regimenFiscal")
							: "")
					+ "||" + objECB.getTagTIPO_CAMBIO() + "|"
					+ objECB.getTagTIPO_MONEDA() + "|\r\n";
		}else{
			temp = temp 
			+ "|"		 
			+ objECB.getTagTIPO_CAMBIO() + "|" 
			+ objECB.getTagTIPO_MONEDA()
			+ "|\r\n";
		}
		
		this.salida.write(temp.getBytes("UTF-8"));
		temp = null;
		
		temp =  "CFDI|" + strUUID + "|" + strFechaTimbrado  + "|" + strNoCertificadoSAT + "|" + "\r\n";
		this.salida.write(temp.getBytes("UTF-8"));
		temp = null;
		//System.out.println("LONGITUD: " + Integer.parseInt(objECB.getTagLONGITUD()));
		this.salida.write(Util.selloCadena(objECB.getSeal(), "SELLO", Integer.parseInt(objECB.getTagLONGITUD())));
		logger.info("strSelloSAT:" + strSelloSAT);
		String fe = "SIN_SELLO";
		if (strSelloSAT.length() > 0) {
			fe = strSelloSAT.substring((strSelloSAT.length() - 8));
		}
		
		this.salida.write(Util.selloCadena(strSelloSAT, "SELLO_SAT", Integer.parseInt(objECB.getTagLONGITUD())));
		
		this.salida.write(Util.selloCadena(
				"||" + strVersion + "|" + strUUID + "|" + strFechaTimbrado + "|" + objECB.getSeal() + "|" + strNoCertificadoSAT + "||", 
				"CADENA_TIMBRE",
				Integer.parseInt(objECB.getTagLONGITUD())));	
		StringBuffer sbConcat = new StringBuffer("https://verificacfdi.facturaelectronica.sat.gob.mx/default.aspx");
		sbConcat.append("?id=").append(strUUID);
		sbConcat.append("&re=").append(strEmisorRFC);
		sbConcat.append("&rr=").append(strReceptorRFC);
		sbConcat.append("&tt=").append(strTotalZeros);
		sbConcat.append("&fe=").append(fe);
		//temp = "COD_B|https://verificacfdi.facturaelectronica.sat.gob.mx/default.aspx&?re=" + strEmisorRFC + "&rr=" + strReceptorRFC  + "&tt=" + strTotalZeros + "&id=" + strUUID + "\r\n";
		this.salida.write(Util.selloCadena(sbConcat.toString(), "COD_B", Integer.parseInt(objECB.getTagLONGITUD())));
		//this.salida.write(temp.getBytes("UTF-8"));
		
		
		if(!fileNames.trim().equals("CFDREPROCESOECB"))
			temp = "TIPO_DOCUMENTO|" + objECB.getTagCFD_TYPE() + "\r\n";
		else
			temp = "TIPO_DOCUMENTO|" + objECB.getTagCFD_TYPE() + "|" + objECB.getTagNOMBRE_APP_REPECB()  + "|\r\n";
		
		this.salida.write(temp.getBytes("UTF-8"));
		temp = null;
		
		long t2 = t1- System.currentTimeMillis();
		System.out.println("TIME: Escritura archivo respuesta " + t2 + " ms");
		/*		
		Date dateInicio2 = new Date();
		System.out.println("TIMEFINALEscritura archivo respuesta:" + dateFormat.format(dateInicio2) + " M" + System.currentTimeMillis());
		*/
		this.setBDv2(strUUID, objECB);
	}
	
	public void setBDv2(String strUUID, ECB objECB) 
		throws ParseException, UnsupportedEncodingException, IOException 
	{/*
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date dateInicio = new Date();
		System.out.println("TIMEINICIOCalculando tiempo lote para BD (archivo):" + dateFormat.format(dateInicio) + " M" + System.currentTimeMillis());
		*/
		long t1 = System.currentTimeMillis();
		System.out.println("TIME: Calculando tiempo lote para BD (archivo)...");
		
		StringBuffer temp = new StringBuffer();
		 
		//17FEB2012 -- Cambio optimizacion Oracle
		//temp.append("r," + conver.getTags().cfd.getFilePath().getRoute() + "\r\n");
		//temp.append("c," + conver.getTags().fis.getId() + ",");
		temp.append("c," + conver.getTags().cfd.getXmlRoute() + ",");
		temp.append(conver.getTags().cfd.getProcessID() + ",");
		temp.append(objECB.getTagFis_ID() + ",");
		
		temp.append(objECB.getTagRECEPCION_RFC() + ",");
		temp.append("masivo,");
		int fechaIdx =objECB.getTagFECHA_CFD().indexOf("T");
		//String dateStr = objECB.getTagEMISION_PERIODO() + " " + objECB.getTagFECHA_CFD().substring(fechaIdx + 1) + ",";
		String strDate = objECB.getTagFECHA_CFD().substring(0, fechaIdx);		
		temp.append(strDate + " " + objECB.getTagFECHA_CFD().substring(fechaIdx + 1) + ",");
		temp.append(strDate + " " + objECB.getTagFECHA_CFD().substring(fechaIdx + 1) + ",");
		temp.append(strDate + " " + objECB.getTagFECHA_CFD().substring(fechaIdx + 1) + ",");
		temp.append(objECB.getTagFOLIO_FISCAL_CFD() + ",");
		temp.append(objECB.getTagSERIE_FISCAL_CFD() + ",");
		temp.append("|" + objECB.getTagFOLIO_FISCAL_CFD()+"|"+objECB.getTagNUM_TARJETA()+ "|" + ",");
		temp.append(",");
		temp.append(",");
		temp.append(objECB.getTagNUM_CTE()+ ",");
		
		////////////////Obtener tipo de cambio
		String strTipoCambio = "";
		if(objECB.getTagTIPO_CAMBIO() != null && objECB.getTagTIPO_CAMBIO().length() > 0){
			strTipoCambio=objECB.getTagTIPO_CAMBIO();
			//System.out.println("conver.getTags().TIPO_CAMBIO:" + objECB.getTagTIPO_CAMBIO());
		}	
				
		/*
		if( conver.getTags().SERIE_FISCAL_CFD != null && conver.getTags().SERIE_FISCAL_CFD.trim() != "" ){
			HashMap<String, String> monedas = (HashMap<String, String>) tipoCambio.get(conver.getTags().EMISION_PERIODO);
			
			if(monedas != null){
				strTipoCambio = (String) monedas.get(conver.getTags().SERIE_FISCAL_CFD);				
			}
						
		}*/		
		//////////////////////////////////////
		
		if(!Util.isNullEmpty(objECB.getTagEMISION_PERIODO()))
		{	temp.append(objECB.getTagEMISION_PERIODO() + ",");	}
		else
		{	temp.append(",");	}
		if (Util.isNullEmpty(objECB.getTagIVATOTAL_MN()) && objECB.getTagIVATOTAL_MN().length() == 0) 
		{	temp.append("0.0" + ",");	} 
		else 
		{	
			if(strTipoCambio != ""){
				double iva = Double.valueOf(objECB.getTagIVATOTAL_MN())*Double.valueOf(strTipoCambio);
				temp.append(String.valueOf(iva) + ",");
			}else{
				temp.append(objECB.getTagIVATOTAL_MN() + ",");
			}				
		}
		temp.append("1" + ",");
		
		if(strTipoCambio != ""){
			double subtotal = Double.valueOf(objECB.getTagSUBTOTAL_MN()) * Double.valueOf(strTipoCambio);
			double total = Double.valueOf(objECB.getTagTOTAL_MN()) * Double.valueOf(strTipoCambio);
			temp.append(subtotal + ",");
			temp.append(total + ",");
		}else{
			temp.append(objECB.getTagSUBTOTAL_MN() + ",");
			temp.append(objECB.getTagTOTAL_MN() + ",");			
		}

		/*System.out.println("strTipoCambio:" + strTipoCambio);
		System.out.println("SUBTOTAL_MN:" + objECB.getTagSUBTOTAL_MN());
		System.out.println("IVA_TOTAL_MN:" + objECB.getTagIVATOTAL_MN());
		System.out.println("TOTAL_MN:" + objECB.getTagTOTAL_MN());
		*/
		temp.append(objECB.getTagTIPO_FORMATO() + ",");
		temp.append(objECB.getTagCFD_TYPE() + ",");
		temp.append(this.nameFile + ",");
		temp.append(strUUID + ",");
		temp.append(1 + ",");
		temp.append(objECB.getStartLine() + ",");
		temp.append(objECB.getEndLine() + "\r\n");		
		//temp.append(this.startLine + ",");
		//temp.append(this.endLine + "\r\n");
		
		this.salidaBD.write(temp.toString().getBytes("UTF-8"));
		long t2 = t1- System.currentTimeMillis();
		System.out.println("TIME: Tiempo Lote para BD (archivo)" + t2 + " ms");
		/*		
		Date dateInicio2 = new Date();
		System.out.println("TIMEFINALTiempo Lote para BD (archivo):" + dateFormat.format(dateInicio2) + " M" + System.currentTimeMillis());
		*/
		//conver.getTags().cfd.setFiscalEntity(conver.getTags().fis);
		//conver.getTags().cfd.setTaxIdReceiver(conver.getTags().RECEPCION_RFC);
		//conver.getTags().cfd.setAuthor("masivo");
		//conver.getTags().cfd.setCreationDate(Util.convertirString(conver.getTags().EMISION_PERIODO, conver.getTags().FECHA_CFD));
		//conver.getTags().cfd.setDateOfIssuance(Util.convertirString(conver.getTags().EMISION_PERIODO, conver.getTags().FECHA_CFD));
		//conver.getTags().cfd.setFolio(conver.getTags().FOLIO_FISCAL_CFD);
		//System.out.println("El numero de Tarjeta es: "+conver.getTags().NUM_TARJETA);
		//System.out.println("El numero de Cuenta es: "+conver.getTags().FOLIO_FISCAL_CFD);
		//conver.getTags().cfd.setComplement("|"+conver.getTags().FOLIO_FISCAL_CFD+"|"+conver.getTags().NUM_TARJETA+"|");
		//conver.getTags().cfd.setContractNumber(conver.getTags().NUM_CTA);
		//conver.getTags().cfd.setCostCenter(conver.getTags().NUM_CTA);
		//conver.getTags().cfd.setCustomerCode(conver.getTags().NUM_CTE);
		//if(!Util.isNullEmpty(conver.getTags().EMISION_PERIODO))
		//{	conver.getTags().cfd.setPeriod(conver.getTags().EMISION_PERIODO);	}
		//if (Util.isNullEmpty(conver.getTags().IVA_TOTAL_MN)
		//		&& conver.getTags().IVA_TOTAL_MN.length() == 0) 
		//{	conver.getTags().cfd.setIva(0.0);	} 
		//else 
		//{	conver.getTags().cfd.setIva(Double.valueOf(conver.getTags().IVA_TOTAL_MN));	}
		//conver.getTags().cfd.setStatus(1);
		//conver.getTags().cfd.setSubTotal(Double.valueOf(conver.getTags().SUBTOTAL_MN));
		//conver.getTags().cfd.setTotal(Double.valueOf(conver.getTags().TOTAL_MN));
		// logger.info("El valor del Tipo de Formato: "+conver.getTags().TIPO_FORMATO.length());
		// logger.info("El valor del Tipo de Formato: "+conver.getTags().TIPO_FORMATO);
		//conver.getTags().cfd.setFormatType(Integer.parseInt(conver.getTags().TIPO_FORMATO));
		//conver.getTags().cfd.setCfdType(conver.getTags().CFD_TYPE);
		//conver.getTags().cfd.setSourceFileName(this.nameFile);
		//conver.getTags().cfd.setStartLine(startLine);
		//conver.getTags().cfd.setEndLine(endLine);
		//Route route = new Route();
		//route.setRoute(nameFile);
		//conver.getTags().cfd.setFilePath(route);
		//lstECB.add(conver.getTags().cfd);
		//if (lstECB.size() >= properties.getSizeECB()) 
		//{
		//	boolean cleanList = true;
		//	try
		//	{	conver.getTags().cFDIssuedManager.update(lstECB);	}
		//	catch (Exception e1)
		//	{
		//		
		//	}
		//	if (cleanList)
		//	{	lstECB = new ArrayList<CFDIssued>();	}
		//}
	}

	/**
	 * 
	 * @throws ParseException
	 */
	public void setBD() 
		throws ParseException 
	{/*
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date dateInicio = new Date();
		System.out.println("TIMEINICIOCalculando tiempo lote BD:" + dateFormat.format(dateInicio) + " M" + System.currentTimeMillis());
		*/
		long t1 = System.currentTimeMillis();
		System.out.println("TIME: Calculando tiempo lote BD...");
		conver.getTags().cfd.setFiscalEntity(conver.getTags().fis);
		conver.getTags().cfd.setTaxIdReceiver(conver.getTags().RECEPCION_RFC);
		conver.getTags().cfd.setAuthor("masivo");
		conver.getTags().cfd.setCreationDate(Util.convertirString(conver.getTags().EMISION_PERIODO, conver.getTags().FECHA_CFD));
		conver.getTags().cfd.setDateOfIssuance(Util.convertirString(conver.getTags().EMISION_PERIODO, conver.getTags().FECHA_CFD));
		conver.getTags().cfd.setFolio(conver.getTags().FOLIO_FISCAL_CFD);
		//System.out.println("El numero de Tarjeta es: "+conver.getTags().NUM_TARJETA);
		//System.out.println("El numero de Cuenta es: "+conver.getTags().FOLIO_FISCAL_CFD);
		conver.getTags().cfd.setComplement("|"+conver.getTags().FOLIO_FISCAL_CFD+"|"+conver.getTags().NUM_TARJETA+"|");
		//conver.getTags().cfd.setContractNumber(conver.getTags().NUM_CTA);
		//conver.getTags().cfd.setCostCenter(conver.getTags().NUM_CTA);
		conver.getTags().cfd.setCustomerCode(conver.getTags().NUM_CTE);
		if(!Util.isNullEmpty(conver.getTags().EMISION_PERIODO))
		{	conver.getTags().cfd.setPeriod(conver.getTags().EMISION_PERIODO);	}
		if (Util.isNullEmpty(conver.getTags().IVA_TOTAL_MN)
				&& conver.getTags().IVA_TOTAL_MN.length() == 0) 
		{	conver.getTags().cfd.setIva(0.0);	} 
		else 
		{	conver.getTags().cfd.setIva(Double.valueOf(conver.getTags().IVA_TOTAL_MN));	}
		conver.getTags().cfd.setStatus(1);
		conver.getTags().cfd.setSubTotal(Double.valueOf(conver.getTags().SUBTOTAL_MN));
		conver.getTags().cfd.setTotal(Double.valueOf(conver.getTags().TOTAL_MN));
		// logger.info("El valor del Tipo de Formato: "+conver.getTags().TIPO_FORMATO.length());
		// logger.info("El valor del Tipo de Formato: "+conver.getTags().TIPO_FORMATO);
		conver.getTags().cfd.setFormatType(Integer.parseInt(conver.getTags().TIPO_FORMATO));
		conver.getTags().cfd.setCfdType(conver.getTags().CFD_TYPE);
		conver.getTags().cfd.setSourceFileName(this.nameFile);
		conver.getTags().cfd.setStartLine(startLine);
		conver.getTags().cfd.setEndLine(endLine);
		logger.debug("Guardar ECB");
		lstECB.add(conver.getTags().cfd);
		if (lstECB.size() >= properties.getSizeECB()) 
		{
			boolean cleanList = true;
			try
			{	conver.getTags().cFDIssuedManager.update(lstECB);	}
			catch (Exception e1)
			{
				String ids = "";
				for (int j = 0; j < lstECB.size(); j++)
				{
					CFDIssued cf = lstECB.get(j);
					ids += cf.getId() + "-" + cf.getFolio() + "|";
				}
				System.out.println("BD: Reintentando ids " + ids);
				try
				{	
					System.out.println("BD: Intento 2");
					conver.getTags().cFDIssuedManager.update(lstECB);	
				}
				catch (Exception e2)
				{
					try
					{	
						System.out.println("BD: Intento 3");
						conver.getTags().cFDIssuedManager.update(lstECB);	
					}
					catch (Exception e3)
					{	
						try
						{	
							System.out.println("BD: Intento 4");
							conver.getTags().cFDIssuedManager.update(lstECB);	
						}
						catch (Exception e4)
						{	
							try
							{	
								System.out.println("BD: Intento 5");
								conver.getTags().cFDIssuedManager.update(lstECB);	
							}
							catch (Exception e5)
							{	
								try
								{	
									System.out.println("BD: Intento 6");
									conver.getTags().cFDIssuedManager.update(lstECB);	
								}
								catch (Exception e6)
								{	
									try
									{	
										System.out.println("BD: Intento 7");
										conver.getTags().cFDIssuedManager.update(lstECB);	
									}
									catch (Exception e7)
									{	
										try
										{
											System.out.println("BD: Intento 8");
											conver.getTags().cFDIssuedManager.update(lstECB);	
										}
										catch (Exception e8)
										{	
											try
											{	
												System.out.println("BD: Intento 9");
												conver.getTags().cFDIssuedManager.update(lstECB);	
											}
											catch (Exception e9)
											{	
												try
												{	
													System.out.println("BD: Intento 10");
													conver.getTags().cFDIssuedManager.update(lstECB);	
												}
												catch (Exception e10)
												{	
													try
													{	
														System.out.println("BD: Intento 11");
														conver.getTags().cFDIssuedManager.update(lstECB);	
													}
													catch (Exception e11)
													{																
														try
														{	
															System.out.println("BD: Intento 12");
															conver.getTags().cFDIssuedManager.update(lstECB);	
														}
														catch (Exception e12)
														{										
															try
															{	
																System.out.println("BD: Intento 13");
																conver.getTags().cFDIssuedManager.update(lstECB);	
															}	
															catch (Exception e13)
															{	
																try
																{	
																	System.out.println("BD: Intento 14");
																	conver.getTags().cFDIssuedManager.update(lstECB);	
																}
																catch (Exception e14)
																{	
																	try
																	{
																		System.out.println("BD: Intento 15");
																		conver.getTags().cFDIssuedManager.update(lstECB);
																	}
																	catch (Exception e15)
																	{
																		e15.printStackTrace();
																		long t2 = t1- System.currentTimeMillis();
																		System.out.println("TIME: Tiempo Lote BD -- No pudo completar transaccion " + t2 + " ms");
																		cleanList = false;
																	}
																}
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
			if (cleanList)
			{	lstECB = new ArrayList<CFDIssued>();	}
		}
		long t2 = t1- System.currentTimeMillis();
		System.out.println("TIME: Tiempo Lote BD " + t2 + " ms");
		/*
		Date dateInicio2 = new Date();
		System.out.println("TIMEFINAL Tiempo Lote BD:" + dateFormat.format(dateInicio2) + " M" + System.currentTimeMillis());
		*/
	}

	/**
	 * 
	 * @param msgError
	 * @param typeIncidence
	 * @return
	 */
	public CFDIssuedIn setCFDIncidence(String msgError, String typeIncidence) 
	{
		CFDIssuedIn cFDIssuedIncidence = new CFDIssuedIn();
		if (msgError != null) 
		{
			if (msgError.length() > sizeT) 
			{	cFDIssuedIncidence.setErrorMessage(msgError.substring(0, sizeT));	} 
			else 
			{	cFDIssuedIncidence.setErrorMessage(msgError);	}
		}
		cFDIssuedIncidence.setSourceFileName(this.nameFile);
		cFDIssuedIncidence.setAuthor("masivo");
		cFDIssuedIncidence.setComplement(typeIncidence);
		cFDIssuedIncidence.setCreationDate(Calendar.getInstance().getTime());
		cFDIssuedIncidence.setStartLine(startLine);
		cFDIssuedIncidence.setEndLine(endLine);
		return cFDIssuedIncidence;
	}

	/**
	 * Guarda una Incidencia en la Base de Datos
	 * @param e
	 * @param typeIncidence
	 * @throws IOException
	 */
	
	public void fileINCIDENCIACIFRAS(Exception e, String typeIncidence,
			String strTagEMISON_RFC, String strTagNUM_CTE, String strTagNUM_CTA, String strTagEMISION_PERIODO, String strTagNUM_TARJETA, String strCFD_TYPE,
			String strComisionesIntereses, String strIvas, String strRetenciones, String strFileNames, String strTagSERIE_FISCAL_CFD, String numeroMalla) 
			throws IOException 
		{
			this.fileINCIDENCIACIFRAS(e.getMessage(), typeIncidence, 
					strTagEMISON_RFC, strTagNUM_CTE, strTagNUM_CTA, strTagEMISION_PERIODO, strTagNUM_TARJETA, strCFD_TYPE, 
					strComisionesIntereses, strIvas, strRetenciones, strFileNames, strTagSERIE_FISCAL_CFD, numeroMalla);
		}
	public void fileINCIDENCIACIFRAS(String e, String typeIncidence, 
			String strTagEMISON_RFC, String strTagNUM_CTE, String strTagNUM_CTA, String strTagEMISION_PERIODO, String strTagNUM_TARJETA, String strCFD_TYPE,
			String strComisionesIntereses, String strIvas, String strRetenciones, String strFileNames, String strTagSERIE_FISCAL_CFD, String numeroMalla) 
		throws IOException 
	{
				
		long t1 = System.currentTimeMillis();
		temp = NombreAplicativo.obtieneNombreApp(getNombresApps(), strFileNames, numeroMalla) + "|CFD|" + strTagEMISON_RFC 
			+ "|" + strTagNUM_CTE
			+ "|" + strTagNUM_CTA
			+ "|" + strTagEMISION_PERIODO
			+ "|" + strTagNUM_TARJETA
			+ "|" + strTagSERIE_FISCAL_CFD
			+ "|" + strCFD_TYPE
			+ "|" + strComisionesIntereses 
			+ "|" + strIvas 
			+ "|" + strRetenciones					 
			+ "|" + "\r\n";
		incidenciaCifras.write(temp.getBytes());
		incidenciaCifras.write("Se presentaron los siguientes errores al validar la estructura del comprobante Cifras: \r\n".getBytes());
		
		
		if(typeIncidence.equals("ERROR"))
		{	temp = "Error: " + e + " ";	} 
		else 
		{	temp = "Warning: " + e + " ";	}
		
		temp += "Inicio de CFD: " + startLine + "|" + "\r\n";
		incidenciaCifras.write(temp.getBytes("UTF-8"));
		temp = null;
		long t2 = t1- System.currentTimeMillis();
		System.out.println("TIME: Escritura archivo incidente " + t2 + " ms");
		
	}

	public void fileINCIDENCIA(Exception e, String typeIncidence,
			String strTagEMISON_RFC, String strTagNUM_CTE, String strTagNUM_CTA, String strTagEMISION_PERIODO, String strTagNUM_TARJETA, String strCFD_TYPE) 
			throws IOException 
		{
			this.fileINCIDENCIA(e.getMessage(), typeIncidence, 
					strTagEMISON_RFC, strTagNUM_CTE, strTagNUM_CTA, strTagEMISION_PERIODO, strTagNUM_TARJETA, strCFD_TYPE);
		}
	public void fileINCIDENCIA(String e, String typeIncidence, 
			String strTagEMISON_RFC, String strTagNUM_CTE, String strTagNUM_CTA, String strTagEMISION_PERIODO, String strTagNUM_TARJETA, String strCFD_TYPE) 
		throws IOException 
	{/*
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date dateInicio = new Date();
		System.out.println("TIMEINICIOEscritura archivo incidente:" + dateFormat.format(dateInicio) + " M" + System.currentTimeMillis());
		*/
		long t1 = System.currentTimeMillis();
		temp = strTagEMISON_RFC 
			+ "|" + strTagNUM_CTE
			+ "|" + strTagNUM_CTA
			+ "|" + strTagEMISION_PERIODO
			+ "|" + strTagNUM_TARJETA
			+ "|" + strCFD_TYPE
			+ "|" + "\r\n";
		incidencia.write(temp.getBytes());
		incidencia.write("Se presentaron los siguientes errores al validar la estructura del comprobante: \r\n".getBytes());
		if(typeIncidence.equals("ERROR"))
		{	temp = "Error: " + e + "\r\n";	} 
		else 
		{	temp = "Warning: " + e + "\r\n";	}
		temp += "Inicio de CFD: " + startLine + "\r\n";
		incidencia.write(temp.getBytes("UTF-8"));
		
		
		
		
		/*incidencia.write(temp.getBytes("UTF-8"));
		incidencia.write("Se presentaron los siguientes errores al validar la estructura del comprobante: \r\n".getBytes("UTF-8"));
		logger.info("mLlovera: typeIncidence:"+typeIncidence);
		logger.info("mLlovera: error:"+incidencia.toString());
		logger.info("mLlovera: Incident:"+e);
		if(typeIncidence.equals("ERROR"))
		{	temp = "Error: \n";	} 
		else 
		{	temp = "Warning: \n";	}
		if(e!= null && !e.isEmpty()){
			logger.info("mLlovera: incident:"+e);
			for (String err : e.split("@@-@@")) {
				logger.info("mLlovera: incidentList:"+err);
				incidencia.write((err+"\n").getBytes());
			}
		}
		temp = "Inicio de CFD: ".concat(startLine).concat("\n");
		logger.info("mLlovera: temp:"+temp);
		//temp = temp.replace("\n", System.getProperty("line.separator"));
		incidencia.write(temp.getBytes());*/
		temp = null;
		long t2 = t1- System.currentTimeMillis();
		System.out.println("TIME: Escritura archivo incidente fileIncidencia " + t2 + " ms");
		/*
		Date dateInicio2 = new Date();
		System.out.println("TIMEFINALEscritura archivo incidente:" + dateFormat.format(dateInicio2) + " M" + System.currentTimeMillis());
		*/
		
		//this.setBDIncidence(setCFDIncidence(e,typeIncidence));
	}	
	
	/**
	 * Guarda la incidencia del ECB
	 * 
	 * @param cFDIssuedIncidence
	 */
	public void setBDIncidence(CFDIssuedIn cFDIssuedIncidence)
	{/*
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date dateInicio = new Date();
		System.out.println("TIMEINICIOActualizacion Incidencia BD:" + dateFormat.format(dateInicio) + " M" + System.currentTimeMillis());
		*/
		long t1 = System.currentTimeMillis();
		logger.debug("Guardar CFD Incidence");
		conver.getTags().cFDIssuedIncidenceManager.update(cFDIssuedIncidence);
		long t2 = t1- System.currentTimeMillis();
		System.out.println("TIME: Actualizacion Incidencia BD " + t2 + " ms");
		/*
		Date dateInicio2 = new Date();
		System.out.println("TIMEFINAL Actualizacion Incidencia BD:" + dateFormat.format(dateInicio2) + " M" + System.currentTimeMillis());
		*/
	}

	public void fillCatalogoCincoCampos33() {
		File catalogoCincoCampos33File = new File(properties.getCatalogoCincoCampos33());
		this.catalogoCincoCampos33.clear();
		if (catalogoCincoCampos33File.exists()) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(catalogoCincoCampos33File));
				String line;
				int count = 0;
				while ((line = br.readLine()) != null) {
					// process the line.
					if (line.length() > 2) {
						line =line.substring(2);
					} 					System.out.println("Valor linea " +count+" "+line);
					switch (count) {
					case 0:
						this.catalogoCincoCampos33.put("unidadMedida", line);
						break;
					case 1:
						this.catalogoCincoCampos33.put("lugarExpedicion", line);
						break;
					case 2:
						this.catalogoCincoCampos33.put("metodoPago", line);
						break;
					case 3:
						this.catalogoCincoCampos33.put("regimenFiscal", line);
						break;
					default:
						break;
					}
					count++;
				}
				br.close();
			} catch (FileNotFoundException e) {
				System.out.println("No se encontro el archivo catalogoCincoCampos33.txt");
			} catch (IOException e) {
				System.out.println("No se pudo leer archivo catalogoCincoCampos33.txt");
			}
		}
	}

	public String getNameFile() 
	{	return nameFile;	}

	public void setNameFile(String nameFile) 
	{	this.nameFile = nameFile;	}

	public HashMap<String, FiscalEntity> getLstFiscal() {
		return lstFiscal;
	}

	public void setLstFiscal(HashMap<String, FiscalEntity> lstFiscal) {
		this.lstFiscal = lstFiscal;
	}

	public HashMap<String, HashMap> getCampos22() {
		return campos22;
	}

	public void setCampos22(HashMap<String, HashMap> campos22) {
		this.campos22 = campos22;
	}

	public HashMap<String, HashMap> getTipoCambio() {
		return tipoCambio;
	}

	public void setTipoCambio(HashMap<String, HashMap> tipoCambio) {
		this.tipoCambio = tipoCambio;
	}

	public List<SealCertificate> getLstSeal() {
		return lstSeal;
	}

	public void setLstSeal(List<SealCertificate> lstSeal) 
	{	this.lstSeal = lstSeal;		}

	public Transformer getTransf() 
	{	return transf;	}

	public void setTransf(Transformer transf) 
	{	this.transf = transf;	}

	public ValidatorHandler getValidator() 
	{	return validator;	}

	public void setValidator(ValidatorHandler validator) 
	{	this.validator = validator;	}

	public String getUrlWebService() {
		return urlWebService;
	}

	public void setUrlWebService(String urlWebService) {
		this.urlWebService = urlWebService;
	}

	public HashMap<String, String> getNombresApps() {
		return nombresApps;
	}

	public void setNombresApps(HashMap<String, String> nombresApps) {
		this.nombresApps = nombresApps;
	}

}
