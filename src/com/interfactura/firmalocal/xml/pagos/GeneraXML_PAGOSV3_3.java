package com.interfactura.firmalocal.xml.pagos;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

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

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.interfactura.firmalocal.domain.entities.CFDIssued;
import com.interfactura.firmalocal.domain.entities.CFDIssuedIn;
import com.interfactura.firmalocal.domain.entities.FiscalEntity;
import com.interfactura.firmalocal.domain.entities.OpenJpa;
import com.interfactura.firmalocal.domain.entities.SealCertificate;
import com.interfactura.firmalocal.persistence.OpenJpaManager;
import com.interfactura.firmalocal.xml.Properties;
import com.interfactura.firmalocal.xml.WebServiceCliente;
import com.interfactura.firmalocal.xml.file.XMLProcess;
import com.interfactura.firmalocal.xml.util.NombreAplicativo;
import com.interfactura.firmalocal.xml.util.Util;
import com.interfactura.firmalocal.xml.util.UtilCatalogos;

@Component
public class GeneraXML_PAGOSV3_3 {
	private Logger logger = Logger.getLogger(GeneraXML_PAGOSV3_3.class);
	private BufferedReader br;
	private String linea;
	private String token;
	@Autowired
	private ConvertirPV3_3 conver; 
	private int cont;
	@Autowired
	private Properties properties;
	private ByteArrayOutputStream out;
	private FileOutputStream salida;
	private FileOutputStream salidaBD;
	private FileOutputStream salidaODM;
	private FileOutputStream incidencia;
	
	@Autowired
	public OpenJpaManager openJpaManager;
	
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
    private List<XMLPagos> listPagos = new ArrayList<XMLPagos>();
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
    
    //Contadorde XML
    private int contadorMilli;
    private String seconds;
    
   	public GeneraXML_PAGOSV3_3() {

	}
   	

	private static final String RFC_PATTERN = "[A-Z,Ñ,&]{3,4}[0-9]{2}[0-1][0-9][0-3][0-9][A-Z,0-9]?[A-Z,0-9]?[0-9,A-Z]?";
	private static final String RFC_PATTERN_TWO = "[A-Z&Ñ]{3,4}[0-9]{2}(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])[A-Z0-9]{2}[0-9A]";
	private static final String DATE_PATTERN = "[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])T(([01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9])";
	private static final String UUID_PATTERN = "[a-f0-9A-F]{8}-[a-f0-9A-F]{4}-[a-f0-9A-F]{4}-[a-f0-9A-F]{4}-[a-f0-9A-F]{12}";
	
	/**
	 * 
	 * @param nameFile
	 */
	public GeneraXML_PAGOSV3_3(String nameFile) {
		this.nameFile = nameFile;
	}

	/**
	 * Procesa una parte de un archivo en especifico
	 * 
	 * @param byteStart
	 * @param byteEnd
	 * @param path
	 * @param cont
	 * @return
	 */
	public boolean convierteTareas(long byteStart, long byteEnd, String path, long cont, String idProceso, String fecha, String fileNames, String numeroMalla) 
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
		listPagos = new ArrayList<XMLPagos>();
		lstECBIncidence = new ArrayList<CFDIssuedIn>();
		//System.out.println("despueslstECB y lstECBIncidence");
		try 
		{
			this.file=new File(path);
			this.nameFile = this.file.getName();
			 
			// Se crea el archivo de salida
			System.out.println("Creando archivos de salida " );
			//logger.debug("Paso 3.- Creando archivo de salida byte de final: " + byteEnd);
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
			
			System.out.println("BackUp de la parte del archivo");
			copy(byteStart, byteEnd, path, cont, idProceso);
			file = new RandomAccessFile(path, "r");
			int sizeArray = 1024 * 8;
			long byteEndLine = 10;
			
			this.seconds = "0";
			this.contadorMilli = 1;
			
					
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
								this.formateaLineaTareas(idProceso, fecha, fileNames, numeroMalla);							
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
				this.formateaLineaTareas(idProceso, fecha, fileNames, numeroMalla);	
			}
			
			out.write(endComplemento());
			
			this.endPago(0, idProceso, fecha, fileNames, numeroMalla);
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
		
			if(this.servicePort == null){
				this.servicePort = new WebServiceCliente();								
			}
			
			for (XMLPagos pago : listPagos) {
				System.out.println("folio: "+pago.folio+" xmlOriginalxd: " + pago.xmlOriginal);
			}
			
			
			for (int inc = 0 ; inc < listPagos.size(); inc++) {
				if (inc < listPagos.size()-1) {
					sbXmlATimbrar.append(listPagos.get(inc).xmlOriginal.toString("UTF-8")+"|");
					sbPeriodos.append(listPagos.get(inc).fecha + "|");
					sbNombresAplicativo.append(listPagos.get(inc).nombreAPP + "|");
				} else {
					sbXmlATimbrar.append(listPagos.get(inc).xmlOriginal.toString("UTF-8"));
					sbPeriodos.append(listPagos.get(inc).fecha );
					sbNombresAplicativo.append(listPagos.get(inc).nombreAPP );
				}
			}
			
									
			if(!sbXmlATimbrar.toString().trim().equals("")){ 
				
				timbrados = "";
				
				System.out.println("EnvioWebService" + sbXmlATimbrar.toString());
				
				timbrados = this.servicePort.generaTimbre(sbXmlATimbrar.toString(), false, this.urlWebService, properties, this.nameFile, Integer.parseInt(idProceso), 0, sbPeriodos.toString(), sbNombresAplicativo.toString());

				
				String [] xmlsTimbrados = timbrados.split("\\|");
				
				
				t1 = System.currentTimeMillis();
				StringBuffer sbFoliosSAT = new StringBuffer();
				for(int index=0; index<xmlsTimbrados.length; index++){
					listPagos.get(index).setDocResultado(stringToDocument(xmlsTimbrados[index]));
					//Obtenemos la etiqueta raiz (Resultado)
					Element docEleResultado = listPagos.get(index).getDocResultado().getDocumentElement();
					
					String descripcion = docEleResultado.getAttribute("Descripcion");
					String idRespuesta = docEleResultado.getAttribute("IdRespuesta");
					
					if(descripcion.toLowerCase().trim().equals("ok") && idRespuesta.trim().equals("1")){
						//Obtenemos la etiqueta raiz (Comprobante)								
						//Element docEleComprobante = domComprobante.getDocumentElement();
						Element docEleComprobante = (Element) docEleResultado.getFirstChild();
						
						if(index < xmlsTimbrados.length-1){
							sbFoliosSAT.append(this.getFolioSAT(docEleComprobante, listPagos.get(index).getDocResultado()) + ",");
						}else{
							sbFoliosSAT.append(this.getFolioSAT(docEleComprobante, listPagos.get(index).getDocResultado()));
						}					
					}
				}
				
				
				for(int index=0; index<xmlsTimbrados.length; index++){
					//System.out.println("xmlTimbrado " + index + " :" + xmlsTimbrados[index]);
					//Convertir xmlTimbrado a objeto Document						
					//Document domResultado = stringToDocument(xmlsTimbrados[index]);
				
					//Obtenemos la etiqueta raiz (Resultado)
					//Element docEleResultado = domResultado.getDocumentElement();
					Element docEleResultado = listPagos.get(index).getDocResultado().getDocumentElement();
					
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
			            listPagos.get(index).setDocResultado(this.putMovimientoECB(docEleComprobante, listPagos.get(index).getDocResultado(), listPagos.get(index) ));
			            
						//Concatenar foliosSAT
						sbFoliosSAT.append(this.strUUID + "||");
						
						//Transformar el hijo del nodo Resultado (document Comprobante) a StringResult								
						//StreamResult resultComprobanteTimbrado = this.documentToStreamResult(domComprobante);
						
						//Obtenemos la etiqueta raiz (Resultado con MovimientosECB) 								
						//Element docEleResultadoConMovimientosECB = domResultado.getDocumentElement();
						Element docEleResultadoConMovimientosECB = listPagos.get(index).getDocResultado().getDocumentElement();
						
						StreamResult resultComprobanteTimbrado = this.nodeToStreamResult(docEleResultadoConMovimientosECB.getFirstChild());
										
						StringBuffer sbTimbradoFinal = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + resultComprobanteTimbrado.getWriter().toString().replaceAll("[\n\r]", ""));
						
						long length = sbTimbradoFinal.toString().getBytes("UTF-8").length;
						this.salidaODM.write(sbTimbradoFinal.toString().getBytes("UTF-8"));

						String routeName = properties.getPathDirGenr() + File.separator + fecha + "ODM-" + idProceso;

						conver.getTags().cfd.setXmlRoute(routeName + "|" + this.offSetComprobante + "|" + (this.offSetComprobante + length));
						conver.getTags().cfd.setProcessID(idProceso);
						
						String strTotalZeros = putZeros(this.strTotal);
										
						this.fileSALIDA(listPagos.get(index), fileNames, 
								this.strUUID, this.strFechaTimbrado, this.strNoCertificadoSAT, this.strSelloCFD, this.strSelloSAT,
								this.strVersion, this.strEmisorRFC, this.strReceptorRFC, strTotalZeros);
						this.offSetComprobante += length;
					}else{
						System.out.println("ERROR: " + descripcion + " " + idRespuesta);
									
						registroIncidencia(descripcion + " " + idRespuesta, "ERROR", conver.getPagosTags().rfcEmisor, conver.getPagosTags().contrato, conver.getPagosTags().numeroCliente);
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
			if (salidaBD != null)
			{	salidaBD.close();	}
			if (salidaODM != null)
			{	salidaODM.close();	}
						
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
		}
	}

	/**
	 * 
	 * @throws IOException
	 */
	
	private static long generaXmlTime = 0;
	
	
	private void formateaLineaTareas(String idProceso, String fecha, String fileNames, String numeroMalla) throws IOException {
		
		linea = Util.convierte(linea).concat("|temp");
		
		token = linea.substring(0, 2);
		
		int numElement = 0;
		
		try {	
			numElement = Integer.parseInt(token);	
		} catch (NumberFormatException numberEx) {	
			logger.error("No empieza con un numero " + linea);	
		} 
	
		
		switch (numElement) {
			case 1:
				
				if (conver.getTags().isComprobante) {
					out.write(endComplemento());
					
					this.endPago(1, idProceso, fecha, fileNames, numeroMalla);
					this.begin();
				}
				
				startLine = "" + contCFD;
				endLine = null;
				conver.getTags().isComprobante = true;
				
				out.write( conver.setComprobante(linea, fileNames, getNombresApps(), numeroMalla) );
				break;
			case 2:
				out.write( conver.setRelacionado(linea) );
				break;
			case 3:
				out.write( conver.setEmisor(linea, lstFiscal, campos22) );
				break;
			case 4:
				out.write( conver.setReceptor(linea) );
				break;
			case 5:
				out.write( conver.setConcepto(linea) );
				out.write(beginComplemento());
				break;
			case 6:
				out.write( conver.setPago(linea));
				break;
			case 7:
				out.write( conver.setDocRelacionado(linea) );
				break;
		}
	}
	
	
	public byte[] beginComplemento() throws UnsupportedEncodingException {

			String ret = "\n<cfdi:Complemento>"
					+ "\n<pago10:Pagos"
					+ " Version=\"1.0\""
					+ " xmlns:catCFDI=\"http://www.sat.gob.mx/sitio_internet/cfd/catalogos\""
					+ " xmlns:catPagos=\"http://www.sat.gob.mx/sitio_internet/cfd/catalogos/Pagos\""
					+ " xsi:schemaLocation=\"http://www.sat.gob.mx/Pagos"
					+ " http://www.sat.gob.mx/sitio_internet/cfd/Pagos/Pagos10.xsd\"" 
					+ " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
					+ " xmlns:tdCFDI=\"http://www.sat.gob.mx/sitio_internet/cfd/tipoDatos/tdCFDI\">";
			return ret.getBytes("UTF-8");

	}
			
	
	public byte[] endComplemento() throws UnsupportedEncodingException {
		return "\n</pago10:Pagos>\n</cfdi:Complemento>".getBytes("UTF-8");
	}
	
	private void endPago(int decremento, String idProceso, String fechaCtlM, String fileNames, String numeroMalla) throws IOException {
		
		out.write("\n</cfdi:Comprobante>".getBytes("UTF-8"));
		conver.getTags().isComprobante = false;
		
		try {
			
			endLine = "" + (contCFD - decremento);
			
			if (conver.getErrFormat() != null && conver.getErrFormat().size() > 0) {
				
				StringBuffer numberLines = new StringBuffer();
				
				for (String error : conver.getErrFormat()) 
				{
					numberLines.append(error+"\n");
				}
				throw new Exception("Estructura Incorrecta en las lineas \n" + numberLines.toString());
			}
			
			
			
			if (conver.getTags().fis != null) {
				
				SealCertificate certificate = null;
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
				
				
				if (certificate != null) {
					
					
					String listErrores = validaPago();
					
					if (listErrores == null) {
						out = Util.enconding(out);
						
						
						String folio = "";
						
						Calendar calendar = Calendar.getInstance();
						calendar.setTime(new Date());
					
						String year = ""+calendar.get(Calendar.YEAR);
						String month = ""+(calendar.get(Calendar.MONTH)+1);
						String day = ""+calendar.get(Calendar.DAY_OF_MONTH);
						String hora = ""+calendar.get(Calendar.HOUR_OF_DAY);
						String minuto = ""+calendar.get(Calendar.MINUTE);
						String segundo = ""+calendar.get(Calendar.SECOND);
						
						if (!this.seconds.equalsIgnoreCase(segundo)) {
							this.seconds = segundo;
							this.contadorMilli = 1;
						}
						
						String contador = String.format("%04d",this.contadorMilli);
						
						
						
						folio += year.substring(year.length() - 2 );
						folio += month+day+hora+minuto+segundo;
						folio += idProceso;
						folio += contador;
						
						
						
						
						System.out.println("folioXD: "+folio);
						
						conver.getPagosTags().folio =  folio;
						System.currentTimeMillis();
						
						
						String xml = out.toString();
						
						
						xml = xml.replace(properties.getLblLUGAREXPEDICION(), conver.getPagosTags().lugarExpedicion);
						xml = xml.replace(properties.getLblFOLIOCFD(), conver.getPagosTags().folio);
						
						
						out = UtilCatalogos.convertStringToOutpuStream(xml);
						
						
						
						StringWriter sw2 = documentToStringWriter(byteArrayOutputStreamToDocument(out));									
						
						StringBuffer sb= new StringBuffer();								
						
						sb = sw2.getBuffer();
						
						ByteArrayOutputStream xmlFinal =  new ByteArrayOutputStream();
						
						byte [] xmlFinalBytes = sb.toString().getBytes("UTF-8");
						
						xmlFinal.write(xmlFinalBytes);		
						
						
						
						boolean xmlValido = true;
						String err = "";
						try{ 
							xmlProcess.getValidator().valida(xmlFinal, this.validator); 
						}catch(Exception ex){
							xmlValido = false;
							logger.error(ex);
							err = "Error en la estructura del XML " + ex.getMessage();
						}
						
						if (xmlValido) {
							
							conver.getPagosTags().certificado = certificate.getSerialNumber();
							xmlProcess.setTransf(transf);
							
							
							ECB objEcbActual = new ECB();
							
							XMLPagos pagoActual = new XMLPagos();
							
							pagoActual = null;
							
							pagoActual = conver.getPagosTags();
							
//							Se asigna el certificado
							Document doc = UtilCatalogos.convertStringToDocument(xmlFinal.toString("UTF-8"));
							UtilCatalogos.setValueOnDocumentElement(doc, "//Comprobante/@NoCertificado", certificate.getSerialNumber());
							xmlFinal = UtilCatalogos.convertStringToOutpuStream(UtilCatalogos.convertDocumentXmlToString(doc));
							
							
							
							ByteArrayOutputStream originalString = xmlProcess.generatesOriginalString(xmlFinal);
							
							
							String seal = xmlProcess.sealEncryption(originalString, certificate);
							
							
							System.out.println("AntesReplaceXD: ");
							byte[] bytesToWrite = xmlProcess.replacesOriginalStringPagos(xmlFinal, certificate, seal, 
									conver.getPagosTags().lugarExpedicion).toByteArray();
							
							
							Calendar ca = Calendar.getInstance();
							
							
							
							
							int hour = ca.get(Calendar.HOUR);
							int minute = ca.get(Calendar.MINUTE);
							int second = ca.get(Calendar.SECOND);
							
						
							pagoActual.fechaSellado = hour+":"+minute+":"+second;
							ByteArrayOutputStream xmlSinECB = new ByteArrayOutputStream();
							xmlSinECB.write(bytesToWrite, 0, bytesToWrite.length);
							pagoActual.xmlOriginal = xmlSinECB;
							pagoActual.startLine = this.startLine;
							pagoActual.endLine = this.endLine;
							pagoActual.nombreAPP = NombreAplicativo.obtieneNombreApp(getNombresApps(), fileNames, numeroMalla);
							pagoActual.sello = seal;
							pagoActual.noCertificado = certificate.getSerialNumber();
							pagoActual.fiscalId = conver.getTags().fis.getId();
							
							
							
							if (conver.getTags().fis.getAddress() != null) {
								if (conver.getTags().fis.getAddress().getStreet() != null) {
									pagoActual.calle = conver.getTags().fis.getAddress().getStreet().toUpperCase();
								}
								if (conver.getTags().fis.getAddress().getExternalNumber() != null) {
									pagoActual.noExterior = conver.getTags().fis.getAddress().getExternalNumber();
								}
								if (conver.getTags().fis.getAddress().getInternalNumber() != null) {
									pagoActual.noInterior = conver.getTags().fis.getAddress().getInternalNumber();
								}
								if (conver.getTags().fis.getAddress().getNeighborhood() != null) {
									pagoActual.colonia = conver.getTags().fis.getAddress().getNeighborhood().toUpperCase();
								}
								if (conver.getTags().fis.getAddress().getReference() != null) {
									pagoActual.referencia = conver.getTags().fis.getAddress().getReference();
								}
								if (conver.getTags().fis.getAddress().getRegion() != null) {
									pagoActual.municipio = conver.getTags().fis.getAddress().getRegion().toUpperCase();
								}
								if (conver.getTags().fis.getAddress().getState() != null) {
									if (conver.getTags().fis.getAddress().getState().getName() != null) {
										pagoActual.estado = conver.getTags().fis.getAddress().getState().getName().toUpperCase();
									}
									if (conver.getTags().fis.getAddress().getState().getCountry() != null) {
										if (conver.getTags().fis.getAddress().getState().getCountry().getName() != null) {
											pagoActual.pais =
													conver.getTags().fis.getAddress().getState().getCountry().getName().toUpperCase();
										}
									}
								}
								if (conver.getTags().fis.getAddress().getZipCode() != null) {
									sb.append(Util.isNullEmpity(conver.getTags().fis.getAddress().getZipCode(), "CodigoPostal"));
								}
								if (conver.getTags().fis.getAddress().getZipCode() != null) {
									sb.append(Util.isNullEmpity(conver.getTags().fis.getAddress().getCity(), "Ciudad"));
								}

							}
							
							
							listPagos.add(pagoActual);
							
							
						} else {
							throw new Exception(err);
						}
						
					} else {
						throw new Exception(listErrores);
					}
					
				} else {
					throw new Exception("No existe certificado para la entidad fiscal: "+ conver.getTags().fis.getFiscalName());
				}
			} else {
				throw new Exception("No existe la entidad fiscal con R.F.C.: " + conver.getPagosTags().rfcEmisor);
			}
		} catch (Exception ex) {
			logger.info(out.toString());
			logger.error(ex);
			msgError = ex.getMessage();	
			String typeIncidence="ERROR";
			if(msgError!=null && msgError.contains("The transaction has been rolled back"))
			{	typeIncidence="WARNING";	} 
			if(msgError!=null && msgError.contains("ORA-08177: can't serialize access for this transaction"))
			{	typeIncidence="WARNING";	}
			
			try 
			{
				
				this.registroIncidencia(ex.getMessage(), typeIncidence, conver.getPagosTags().rfcReceptor 
						, conver.getPagosTags().contrato, conver.getPagosTags().numeroCliente);
			} 
			catch (Exception e) 
			{	lstECBIncidence.add(this.setCFDIncidence(msgError,typeIncidence));	} 
			finally 
			{
				conver.setDescriptionFormat(new ArrayList<String>());
				logger.error(ex.getLocalizedMessage(), ex);
			}
		} 
		finally 
		{
			cont += 1;
			this.contadorMilli +=1;
		}
		
		
	}
	
	
	
	public String validaPago() {
		StringBuilder errores = new StringBuilder();
		
		errores.append("");
		
		
		if (conver.getPagosTags().fecha.indexOf("Err")  != -1) {
			errores.append("\n" + getTypeError(conver.getPagosTags().fecha, "Fecha", DATE_PATTERN));
		}
		
		if (conver.getPagosTags().rfcEmisor.indexOf("Err")  != -1) {
			errores.append("\n" + getTypeError(conver.getPagosTags().rfcEmisor, "RFC del Emisor", RFC_PATTERN ));
		}
		
		if (conver.getPagosTags().nombreEmisor.indexOf("Err")  != -1) {
			errores.append("\n" + getTypeError(conver.getPagosTags().nombreEmisor, "Nombre del Emisor", "" ));
		}
		
		if (conver.getPagosTags().rfcReceptor.indexOf("Err")  != -1) {
			errores.append("\n" + getTypeError(conver.getPagosTags().rfcReceptor, "RFC del Receptor", "" ));
		}
		
		if (conver.getPagosTags().nombreReceptor.indexOf("Err")  != -1) {
			errores.append("\n" + getTypeError(conver.getPagosTags().nombreReceptor, "Nombre del Receptor", "" ));
		}
				
		if (conver.getPagosTags().contrato.indexOf("Err")  != -1) {
			errores.append("\n" + getTypeError(conver.getPagosTags().contrato, "Contrato", "" ));
		}
		
		boolean relacionadoErr = false;
		
		for (Map.Entry<String, String> map: conver.getPagosTags().cfdiRelacionados.entrySet() ) {
			String key = map.getKey();
			String value = map.getValue();
			if (key.indexOf("Err")  != -1) {
				errores.append("\n" + getTypeError(key, "UUID Relacionado", UUID_PATTERN));
				relacionadoErr = true;
				break;
			}
				
		}
		
		
		if ((conver.getPagosTags().pagos.size() == conver.getPagosTags().documentos.size()) && !relacionadoErr ) {
			
			int i = 0;
			for(Pago pago : conver.getPagosTags().pagos) {
				Documento doc = conver.getPagosTags().documentos.get(i);
				
				
				boolean error = false;
				
				if (pago.getIdDocumento().indexOf("Err")  != -1) {
					errores.append("\n" + getTypeError(pago.getIdDocumento(), (i + 1) + " Pago IdDocumento", UUID_PATTERN ));
					error = true;
				}
				
				
				String monto ="";
				if (pago.getMonto().indexOf("Err")  != -1) {
					errores.append("\n" + getTypeError(pago.getMonto(), (i + 1) + " Pago Monto", "" ));
				} else
					monto = pago.getMonto();
				
				if (pago.getMonedaP().indexOf("Err")  != -1) {
					errores.append("\n" + getTypeError(pago.getMonedaP(), (i + 1) + " Pago MonedaP", "" ));
				}
				
				if (pago.getTipoCambioP().indexOf("Err")  != -1) {
					errores.append("\n" + getTypeError(pago.getTipoCambioP(), (i + 1) + " Pago TipoCambioP", "" ));
				}
				
				if (pago.getFechaPago().indexOf("Err")  != -1) {
					errores.append("\n" + getTypeError(pago.getFechaPago(), (i + 1) + " Pago FechaDePago", DATE_PATTERN ));
				}
				
				if (pago.getFormaDePagoP().indexOf("Err")  != -1) {
					errores.append("\n" + getTypeError(pago.getFormaDePagoP(), (i + 1) + " Pago FormaDePago", "" ));
				}
				
				if (pago.getRfcEmisorCtaOrd().indexOf("Err")  != -1) {
					errores.append("\n" + getTypeError(pago.getRfcEmisorCtaOrd(), (i + 1) + " Pago rfcEmisorOrd",RFC_PATTERN ));
				}
				
				if (pago.getNomBancoOrdExt().indexOf("Err")  != -1) {
					errores.append("\n" + getTypeError(pago.getNomBancoOrdExt(), (i + 1) + " Pago NombBancoOrdExt", "" ));
				}
				
				if (pago.getCtaOrdenante().indexOf("Err")  != -1) {
					errores.append("\n" + getTypeError(pago.getCtaOrdenante(), (i + 1) + " Pago CtaOrdenante", "" ));
				}
				
				if (pago.getRfcEmisorCtaBen().indexOf("Err")  != -1) {
					errores.append("\n" + getTypeError(pago.getRfcEmisorCtaBen(), (i + 1) + " Pago RfcEmisorCtaBen", RFC_PATTERN ));
				}
				
				if (pago.getCtaBeneficiario().indexOf("Err")  != -1) {
					errores.append("\n" + getTypeError(pago.getCtaBeneficiario(), (i + 1) + " Pago CtaBeneficiario", "" ));
				}
				
				
				
				
				
//				DocRelacionado
				
				if (doc.getIdDocumento().indexOf("Err")  != -1) {
					errores.append("\n" + getTypeError(doc.getIdDocumento(), (i + 1) + " DocRel IdDocumento", UUID_PATTERN ));
					error = true;
				}
				
				if (doc.getMonedaDR().indexOf("Err")  != -1) {
					errores.append("\n" + getTypeError(doc.getMonedaDR(), (i + 1) + " DocRel MonedaDR", "" ));
				}
				
				if (doc.getMetodoDePagoDR().indexOf("Err")  != -1) {
					errores.append("\n" + getTypeError(doc.getMetodoDePagoDR(), (i + 1) + " DocRel MetodoDePagoDR", "" ));
				}
				
				if (!monto.equals("") && !doc.getImpPagado().equals("")) {
					if (!monto.equalsIgnoreCase(doc.getImpPagado()))
						errores.append("\nEl monto del pago No. "+ (i + 1) +" no es igual al importe pagado del documento" );
				}
				
				if (!doc.getImpPagado().equals("") && !doc.getImpSaldoAnt().equals("") && !doc.getImpSaldoInsoluto().equals("") ) {
				
					Double pagado = new Double(doc.getImpPagado().trim());
					Double saldoAnt = new Double(doc.getImpSaldoAnt().trim());
					Double insoluto = new Double(doc.getImpSaldoInsoluto().trim());
					
					Double res = saldoAnt -pagado;
					res = Math.rint(res*100)/100;
					if (!insoluto.equals(res)) {
						errores.append("\nEl impInsoluto="+ insoluto +" del pago No. "+ (i + 1) +" no concuerda con la operacion: " 
								+ saldoAnt + "-" + pagado + "=" +res  );
					}
				}
				
				if ( !error ) {
					if (!pago.getIdDocumento().equalsIgnoreCase(doc.getIdDocumento())) { 
						errores.append("\n El idDocumento del pago y documento " + (i + 1) + " no concuerdan" );
					}
					
				}
				
				i++;
			}
			
		} else {
			if (!relacionadoErr)
				errores.append("\n No concuerda la cantidad de pagos con los documentos relacionados" );
		}
		
		
		return (errores.length() > 0 ? errores.toString() : null);
	}
	
	
	
	public String getTypeError(String type, String attrib, String estructura) {
		String msj = "";
		
		if (type.equalsIgnoreCase("vacioErr")) {
//			Campo vacio
			msj = "" + attrib + " no puede estar vacio";
		} else if (type.equalsIgnoreCase("patronErr")) {
//			Patron incorrecto
			msj = "" + attrib + " debe tener la siguiente estructura: " +estructura; 
		} else if (type.equalsIgnoreCase("noexisteErr")) {
			msj = attrib + " no esta registrado en la DB";
		}
		
		return msj;
		
	}
	
	
	
	

	
	/**
	 * Se agregan las incidencias al archivo INC
	 * @throws IOException 
	 * */
	
	public void registroIncidencia(String error, String type, String rfcReceptor, String contrato, String numeroCliente) throws IOException {
		
		String temp = rfcReceptor
				+"|"+contrato
				+"|"+numeroCliente
				+ "|" + "\r\n";
		
		incidencia.write(temp.getBytes());
		incidencia.write("Se presentaron los siguientes errores al validar la estructura del comprobante: \r\n".getBytes());
		
		if (type.equalsIgnoreCase("ERROR")) 
			temp = "Error: " + error + "\r\n";
		else
			temp = "Warning: " + error + "\r\n";
		
		temp += "Inicio de CFD: " + startLine + "\r\n";
		incidencia.write(temp.getBytes("UTF-8"));
		
		
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

	private Document putMovimientoECB(Element docEleComprobante, Document domResultado, XMLPagos datosPagos) throws TransformerConfigurationException, TransformerException{
		
	
		
		Element rootAddenda = domResultado.createElement("cfdi:Addenda");
		rootAddenda.setAttribute("xmlns:as", "http://www.santander.com.mx/schemas/xsd/AddendaSantanderV1");
		
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
			}
		}
		// Agrega la addenda domicilios
		
			String xmlString2 = "", xmlFinal = "";
			try {
				xmlString2 = UtilCatalogos.convertDocumentXmlToString(domResultado);
			} catch (TransformerConfigurationException e) {
				e.printStackTrace();
			} catch (TransformerException e) {
				e.printStackTrace();
			}
			
			String datosAddenda = "<cfdi:Addenda>"
					+ "<as:AddendaSantanderV1 xmlns:as=\"http://www.santander.com.mx/schemas/xsd/AddendaSantanderV1\" >" + 
					"<as:InformacionEmision codigoCliente=\""+ datosPagos.numeroCliente +"\" contrato=\""+ datosPagos.contrato +"\" />" + 
					"<as:DomicilioEmisor Calle=\""+ datosPagos.calle +"\" Ciudad=\""+datosPagos.ciudad+"\" CodigoPostal=\""+datosPagos.cp+"\" Colonia=\""+datosPagos.colonia+"\" Estado=\""+datosPagos.estado+"\" Localidad=\"\" Municipio=\""+datosPagos.municipio+"\" NoExterior=\""+datosPagos.noExterior+"\" NoInterior=\""+datosPagos.noInterior+"\" Referencia=\""+datosPagos.referencia+"\" pais=\""+datosPagos.pais+"\"/>" +  
					"</as:AddendaSantanderV1>" + 
					"</cfdi:Addenda></cfdi:Comprobante>";
			
			
			String strXmlString = "";
			strXmlString = xmlString2.replace("</cfdi:Comprobante>", datosAddenda);
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
			
			System.out.println("DatosXD: " + strFechaTimbrado + "\n"
					+ strUUID +"\n"
					+ strNoCertificadoSAT +"\n"
					+ strSelloCFD +"\n"
					+ strSelloSAT +"\n"
					+ strVersion +"\n"
					+ "\n" );
			
			
			
		return domResultado;
	}
			
	
	/**
	 * 
	 * @param sello
	 * @param cadena
	 * @throws IOException
	 * @throws ParseException
	 */
	public void fileSALIDA(XMLPagos pago, String fileNames, String strUUID, String strFechaTimbrado, String strNoCertificadoSAT, String strSelloCFD,
			String strSelloSAT, String strVersion, String strEmisorRFC, String strReceptorRFC, String strTotalZeros) 
		throws IOException, ParseException 
	{/*
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date dateInicio = new Date();
		System.out.println("TIMEINICIOEscritura archivo respuesta:" + dateFormat.format(dateInicio) + " M" + System.currentTimeMillis());
		*/
		
		System.out.println("Datos2XD: " + strFechaTimbrado + "\n"
				+ strUUID +"\n"
				+ strNoCertificadoSAT +"\n"
				+ strSelloCFD +"\n"
				+ strSelloSAT +"\n"
				+ strVersion +"\n"
				+ "\n" );
		
		
		long t1 = System.currentTimeMillis();
		temp = "CFD|"
				+ pago.rfcEmisor
				+ "|"
				+ pago.numeroCliente
//				+ "|"
//				+ objECB.getTagNUM_CTA()
				+ "|"
				+ pago.fecha
//				+ "|"
//				+  objECB.getTagNUM_TARJETA()
				+ "|"
				+ pago.fecha
				+ "|"
				+ pago.serie  
				+ "|"
				+ pago.folio 
				+ "|"
				+ pago.yearAprobacion 
				+ "|"
				+ pago.noCertificado  
				+ "|"
				+ pago.numAprobacion;
		//+ Util.convertirFecha(objECB.getTagEMISION_PERIODO(),
		//objECB.getTagFECHA_CFD()) + "|"
		System.out.println("Bloque AMDA SALIDA 3 : " );
		//Obtener interfaces que llevaran los campos
		String strInterfaces = properties.getInterfaces(); 
		String [] interfacesCfdFields = strInterfaces.split(",");		
		
		

			temp = temp 
			+ "|" 
			+ pago.moneda
			+ "|\r\n";
			
			
		this.salida.write(temp.getBytes("UTF-8"));
		temp = null;
		
		temp =  "CFDI|" + strUUID + "|" + strFechaTimbrado  + "|" + strNoCertificadoSAT + "|" + "\r\n";
		this.salida.write(temp.getBytes("UTF-8"));
		temp = null;
		//System.out.println("LONGITUD: " + Integer.parseInt(objECB.getTagLONGITUD()));
		this.salida.write(Util.selloCadena(pago.sello, "SELLO", 162));
		logger.info("strSelloSAT:" + strSelloSAT);
		String fe = "SIN_SELLO";
		if (strSelloSAT.length() > 0) {
			fe = strSelloSAT.substring((strSelloSAT.length() - 8));
		}
		
		this.salida.write(Util.selloCadena(strSelloSAT, "SELLO_SAT", 162));
		
		this.salida.write(Util.selloCadena(
				"||" + strVersion + "|" + strUUID + "|" + strFechaTimbrado + "|" + pago.sello + "|" + strNoCertificadoSAT + "||", 
				"CADENA_TIMBRE",
				162));	
		StringBuffer sbConcat = new StringBuffer("https://verificacfdi.facturaelectronica.sat.gob.mx/default.aspx");
		sbConcat.append("?id=").append(strUUID);
		sbConcat.append("&re=").append(strEmisorRFC);
		sbConcat.append("&rr=").append(strReceptorRFC);
		sbConcat.append("&tt=").append(strTotalZeros);
		sbConcat.append("&fe=").append(fe);
		//temp = "COD_B|https://verificacfdi.facturaelectronica.sat.gob.mx/default.aspx&?re=" + strEmisorRFC + "&rr=" + strReceptorRFC  + "&tt=" + strTotalZeros + "&id=" + strUUID + "\r\n";
		this.salida.write(Util.selloCadena(sbConcat.toString(), "COD_B", 162));
		//this.salida.write(temp.getBytes("UTF-8"));
		
		
		if(!fileNames.trim().equals("CFDREPROCESOECB"))
			temp = "TIPO_DOCUMENTO|" + pago.tipoComprobante + "\r\n";
		else
			temp = "TIPO_DOCUMENTO|" + pago.tipoComprobante + "|" + pago.nombreAPP  + "|\r\n";
		
		this.salida.write(temp.getBytes("UTF-8"));
		temp = null;
		
		long t2 = t1- System.currentTimeMillis();
		System.out.println("TIME: Escritura archivo respuesta " + t2 + " ms");
		/*		
		Date dateInicio2 = new Date();
		System.out.println("TIMEFINALEscritura archivo respuesta:" + dateFormat.format(dateInicio2) + " M" + System.currentTimeMillis());
		*/
		this.setBDv2(strUUID, pago, strFechaTimbrado);
	}
	
	public void setBDv2(String strUUID, XMLPagos pago, String fechaTi) 
		throws ParseException, UnsupportedEncodingException, IOException 
	{
		
		long t1 = System.currentTimeMillis();
		System.out.println("TIME: Calculando tiempo lote para BD (archivo)...");
		
		StringBuffer temp = new StringBuffer();
		int fechaIdx =pago.fecha.indexOf("T");

		
		String strDate = pago.fecha.substring(0, fechaIdx);
		long t2 = t1- System.currentTimeMillis();
		
		System.out.println("TIME: Tiempo Lote para BD (archivo)" + t2 + " ms");
		
		
		
		temp.append("c<#EMasfUD,>");
		temp.append(strDate + " " + pago.fecha.substring(fechaIdx + 1) + "<#EMasfUD,>");
		temp.append(fechaTi.split("T")[0] + " " + pago.fechaSellado + "<#EMasfUD,>");
		temp.append(fechaTi.split("T")[0] + " " + fechaTi.split("T")[1] + "<#EMasfUD,>");
		temp.append(conver.getTags().cfd.getXmlRoute() + "<#EMasfUD,>");
		temp.append(nameFile + "<#EMasfUD,>");
		temp.append("1" + "<#EMasfUD,>");
		temp.append(10 + "<#EMasfUD,>");
		temp.append(pago.rfcReceptor + "<#EMasfUD,>");
		temp.append("0" + "<#EMasfUD,>");
		temp.append("0" + "<#EMasfUD,>");
		temp.append("0" + "<#EMasfUD,>");
		temp.append(pago.contrato + "<#EMasfUD,>");
		temp.append("" + "<#EMasfUD,>");
		temp.append(pago.numeroCliente + "<#EMasfUD,>");
		temp.append(strDate + "<#EMasfUD,>");
		temp.append("|"+ pago.folio+"||" + "<#EMasfUD,>");
		temp.append("P" + "<#EMasfUD,>");
		temp.append(pago.fiscalId + "<#EMasfUD,>");				
		temp.append(1 + "<#EMasfUD,>");		
		temp.append(pago.folio + "<#EMasfUD,>");
		temp.append(strUUID + "<#EMasfUD,>");
		temp.append( "" + "<#EMasfUD,>");
		temp.append("" + "<#EMasfUD,>");
		temp.append("" + "<#EMasfUD,>");
		temp.append("" + "<#EMasfUD,>");
		temp.append("" + "<#EMasfUD,>");
		temp.append("masivo" + "<#EMasfUD,>");
		temp.append("masivo" + "\r\n");
		
		this.salidaBD.write(temp.toString().getBytes("UTF-8"));
		
	}

	
	/**
	 * 
	 * @throws IOException
	 */
	private void begin() throws IOException {	
		out = new ByteArrayOutputStream();
		out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>".getBytes());
		conver.getTags().cfd = new CFDIssued();
		conver.setPagosTags(null);
		conver.setPagosTags(new XMLPagos());
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
