package com.interfactura.firmalocal.xml.ecb;

import static com.interfactura.firmalocal.xml.util.Util.systemDate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.interfactura.firmalocal.datamodel.CfdiComprobanteFiscal;
import com.interfactura.firmalocal.datamodel.Invoice_Masivo;
import com.interfactura.firmalocal.domain.entities.CFDIssued;
import com.interfactura.firmalocal.domain.entities.Customer;
import com.interfactura.firmalocal.domain.entities.FiscalEntity;
import com.interfactura.firmalocal.domain.entities.OpenJpa;
import com.interfactura.firmalocal.domain.entities.Route;
import com.interfactura.firmalocal.persistence.FiscalEntityManager;
import com.interfactura.firmalocal.persistence.OpenJpaManager;
import com.interfactura.firmalocal.xml.Properties;
import com.interfactura.firmalocal.xml.TagsXML;
import com.interfactura.firmalocal.xml.WebServiceCliente;
import com.interfactura.firmalocal.xml.file.GeneraArchivo_Masivo;
import com.interfactura.firmalocal.xml.util.GeneraXmlDivisasCfdiV3_3;
import com.interfactura.firmalocal.xml.util.Util;
import com.interfactura.firmalocal.xml.util.UtilCFDIFormatoUnicoDivisas;
import com.interfactura.firmalocal.xml.util.UtilCFDIValidationsDivisas;
import com.interfactura.firmalocal.xml.util.UtilCatalogos;
import com.interfactura.firmalocal.xml.util.XMLProcessGeneral;

@Component
public class GeneraXML_ECB_DI {
	
	private Logger logger = Logger.getLogger(GeneraXML_ECB_DI.class);
	
	@Autowired
	private Properties properties;
	@Autowired(required = true)
	private UtilCFDIFormatoUnicoDivisas fillFU;
	@Autowired(required = true)
	private UtilCFDIValidationsDivisas validations;
	@Autowired(required=true)
	private FiscalEntityManager fiscalEntityManager;
	@Autowired(required = true)
	private OpenJpaManager openJpaManager;
	@Autowired
	private GeneraXmlDivisasCfdiV3_3 xmlGenerator;
	@Autowired
	private GeneraArchivo_Masivo generaXML;
	@Autowired
	private XMLProcessGeneral xmlProcessGeneral;
	@Autowired
	private TagsXML tags;

	@Value("${invoice.status.active}")
	private String statusActive;
	
	private Invoice_Masivo invoice = null;
	private FiscalEntity fiscalEntity = null;
	private DocumentBuilder db = null;
	private Transformer tx = null;
	private WebServiceCliente servicePort = null;
	private DocumentBuilderFactory dbf = null;
	
	
	
	private HashMap<Integer, String> hashIvas;
	private HashMap<String, String> hashcodigoISO;
	private HashMap<String, String> hashmoneda;
	private HashMap<String,Customer> hashClientes;
	private HashMap<Long,String> hashCfdFieldsV2;
	private HashMap<String, String> nombresApps = new HashMap<String, String>();
	
	private List<Invoice_Masivo> listIn = null;
    private List<CfdiComprobanteFiscal> listComprobantes = null;
	
	private String urlWSTimbrado;
	private String nameFile;
	private String linea;
	private String temp;
	private String startLine;
	
	private boolean processStarted = false;
	private boolean flagProcesado;
	
	private long contCFD;
	private long offSetComprobante = 0;
	
	private File file;
	
	private ByteArrayOutputStream out;
	
	private FileOutputStream salida;
	private FileOutputStream salidaBD;
	private FileOutputStream salidaODM;
	private FileOutputStream incidencia;
	private FileOutputStream incidenciaCifras;
	
	
	//Contadorde XML
    private int contadorMilli;
    private String seconds;
	
	
	
	
	/**
	 * @param byteStart
	 * @param byteEnd
	 * @param path
	 * @param cont
	 * @param idProceso
	 * @param fecha
	 * @param fileNames
	 * @param numeroMalla
	 * @return
	 */
	public boolean convierte(long byteStart, long byteEnd, String path, long cont, String idProceso, String fecha, String fileNames, String numeroMalla) {
		
		String fileNameTxt = this.nameFile.split("\\.")[0];
		String interfaces = fileNameTxt + ".TXT";
		System.out.println("Inicia proceso de Divisas");
		RandomAccessFile file=null;
		flagProcesado = true;;
		StringBuffer linea = new StringBuffer();
		//Bandera que indica si se termino de leer divisas completas
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
		this.listIn = new ArrayList<Invoice_Masivo>();
		this.listComprobantes = new ArrayList<CfdiComprobanteFiscal>();
		if(this.servicePort == null){
			this.servicePort = new WebServiceCliente();								
		}	


		try {
			tags.mapCatalogos = Util.readXLSFile(properties.getUrlArchivoCatalogs());
			System.out.println("Creando archivos de salida " );
			File fileExit=new File(this.getNameFile(properties.getPathSalida(), cont,"XML", idProceso));
			this.salida = new FileOutputStream(fileExit);
			File fileExitBD=new File(this.getNameFile(properties.getPathSalida(), cont,"BD", idProceso));
			this.salidaBD = new FileOutputStream(fileExitBD);
			File fileExitODM = new File(properties.getPathDirGenr() + File.separator + fecha + "ODM-" + idProceso);
			
			String idUsuario = "";
			String idArea = "1";
			String nombreUsuario = "masivo";
			
			if (!this.processStarted) {
				
				this.salidaODM = new FileOutputStream(fileExitODM);
				this.processStarted = true;
				
			} else {
				
				this.salidaODM = new FileOutputStream(fileExitODM, true);
				
			}

			System.out.println("Creando archivo de incidencia");
			File fileIncidence=new File(this.getNameFile(properties.getPathIncidencia(), cont,"INC", idProceso));
			this.incidencia = new FileOutputStream(fileIncidence);
			
			System.out.println("BackUp de la parte del archivo");
			copy(byteStart, byteEnd, path, cont, idProceso);
			file = new RandomAccessFile(path, "r");
			int sizeArray = 1024 * 8;
			long byteEndLine = 10;
					
			contCFD = byteStart;
			System.out.println("Comienza el formateo de las lineas");
			this.seconds = "0";
			this.contadorMilli = 1;
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
							procesa = true;
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
								this.linea = linea.toString();
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
			
			if((linea.toString().trim().length()>0)&&(!activo))
			{
				this.linea = linea.toString();
				this.formatLinea(idProceso, fecha, fileNames, numeroMalla);	
			}
			
			for(int index=0; index<listIn.size(); index++){
				
				if(listIn.get(index).getByteArrXMLSinAddenda() != null){
					
					String strXmlATimbrar = listIn.get(index).getByteArrXMLSinAddenda().toString("UTF-8");
					CfdiComprobanteFiscal comprobante = listComprobantes.get(index);
					
					/////////////Inicio Bloque de Timbrado//////////////////

					String xmlTimbradoConPipe = ""; 
					xmlTimbradoConPipe = this.servicePort.generaTimbre(strXmlATimbrar, false, urlWSTimbrado, properties, interfaces.substring(0, interfaces.indexOf(".")), 0, 1, listIn.get(index).getPeriod(), "");
					
					String xmlTimbrado = xmlTimbradoConPipe.substring(0, xmlTimbradoConPipe.length()-1);
					
					System.out.println("XML Timbrado: \n" + xmlTimbrado);
					
					/////////////Fin Bloque de Timbrado//////////////////
					
					Document dom = stringToDocument(xmlTimbrado);
					//Se verifica si la respuesta del web service es correcta, sino se lanza excepci�n
					Element docEle = dom.getDocumentElement();
					String strDescripcion = docEle.getAttribute("Descripcion");
					String strIdRespuesta = docEle.getAttribute("IdRespuesta");
					
					if(strDescripcion.toLowerCase().trim().equals("ok") && strIdRespuesta.equals("1")) {
						
						Transformer transformer = TransformerFactory.newInstance().newTransformer();
						StreamResult result = new StreamResult(new StringWriter());
						DOMSource source = new DOMSource(docEle.getFirstChild());
						transformer.transform(source, result);
						String xmlString = result.getWriter().toString();
						Document doc = db.parse(new InputSource(new StringReader(xmlString)));
						doc = xmlGenerator.agregaAddendaNewDivisas(doc, comprobante);
						
						//Obtener el UUID 
						String strUUID = this.generaXML.getUUID(doc).trim();
																	
						//Convertir de Document a StringWriter
						StringWriter swXmlFinal = documentToStringWriter(doc);											
						StringBuffer sbXmlFinal= new StringBuffer();			
						
						//Convetir de StringWriter a StringBuffer
						sbXmlFinal = swXmlFinal.getBuffer();
						
						//Quitar los saltos de linea
						String strXmlfinalConAddenda = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + sbXmlFinal.toString().replaceAll("[\n\r]", "");
						
						
						//Guardar ruta y posicion inicio y fin
						long length = strXmlfinalConAddenda.getBytes("UTF-8").length;	
						this.salidaODM.write(strXmlfinalConAddenda.toString().getBytes("UTF-8"));
						
						String routeName = properties.getPathDirGenr() + File.separator + fecha + "ODM-" + idProceso;
						
						listIn.get(index).setXmlRoute(routeName + "|" + this.offSetComprobante + "|" + (this.offSetComprobante + length));									
						salida.write(("folioSAT:" + strUUID + ": Correcto!!\r\n").getBytes("UTF-8"));
						salidaBD.write(this.buildRowBDFile(listIn.get(index), strUUID, interfaces, idUsuario, idArea, nombreUsuario).getBytes("UTF-8"));
						
						this.offSetComprobante += length;
						
					} else {
						
						String error = "Error al construir la factura";
						fileINCIDENCIA( strDescripcion, listComprobantes.get(index).getEncabezado()  );
						
					}
					
				} else {
					String error = "Error al construir la factura";
					fileINCIDENCIA( error, listComprobantes.get(index).getEncabezado() );
				}
				
			}
			
			
			
			
			
		} catch (FileNotFoundException e) 	{
			flagProcesado = false;
			e.printStackTrace();
		} catch (IOException e)	{
			flagProcesado = false;
			e.printStackTrace();
		} catch (Exception e) {
			flagProcesado = false;
			e.printStackTrace();
		} finally {
			if(file!=null) {
				try 
				{	file.close();		} 
				catch (IOException e) 
				{	logger.error("No se pudo cerrar el archivo ",e);	}
			}
			this.linea = null;
			
			this.closeByte();

		} 
		
		
		
		
		
		return false;
	}
	
	
	
	/**
	 * @param path
	 * @param cont
	 * @param prefix
	 * @param idProceso
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
	private void copy(long byteStart, long byteEnd, String path, long cont, String idProceso) throws IOException {

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

	}

	
	
	private void formatLinea(String idProceso, String fecha, String fileNames, String numeroMalla) throws IOException {	
			
		String [] arrayValues = linea.split("\\|");
		
	
		
		System.out.println("lineaXD: " + linea);
		System.out.println( "lineaUTF8: " + new String( linea.getBytes(), "UTF-8" ) );
		
		
		startLine = "" + contCFD;
		
		String encabezado = "";
		String error = "";
		for (int i = 0; i <= 11; i++) {
			
			encabezado += arrayValues[i] + "|";
			
		}
		
		if(!linea.equals("") && arrayValues.length != 0 && !arrayValues[2].equalsIgnoreCase("FINARCHIVO") && arrayValues[0].equalsIgnoreCase("01")) {
			
			if(arrayValues.length<49) {	
				
				error = "Factura incompleta!";
				fileINCIDENCIA( error, encabezado);
				
			} else if ( !arrayValues[arrayValues.length-1].trim().toUpperCase().equals("FINFACTURA") ) {
				
				error = "Estructura de Archivo incorrecta, no fue encontrada la etiqueta de control ||FINFACTURA|| en la factura \n";
				fileINCIDENCIA( error, encabezado);
				
			} else {
				
				CfdiComprobanteFiscal comp = new CfdiComprobanteFiscal();
				comp = fillFU.fillComprobanteFUDivisasNew(arrayValues);
				StringBuilder sbErrorFile = new StringBuilder();
				
				sbErrorFile.append(validations.validateComprobanteDivisasECB(comp));
				
				//convertir comprobante a invoice
				invoice = new Invoice_Masivo();
				invoice = UtilCatalogos.fillInvoice(comp);
				
				if(sbErrorFile.toString().length() > 0){
													
					System.out.println("fError EmisionMasivaDivisas: " + sbErrorFile.toString());							
					fileINCIDENCIA( sbErrorFile.toString(), encabezado);
					
				} else {
					
					try{
						
						System.out.println("Antes de crearFac");
						
						if(!invoice.getSiAplicaIva())								
							invoice.setDescriptionIVA("EXENTO");
						
						//generar xml y asignarlo a invoice
						fiscalEntity = new FiscalEntity();
						fiscalEntity.setTaxID(comp.getEmisor().getRfc());
						fiscalEntity = fiscalEntityManager.findByRFCA(fiscalEntity);
						invoice.setFe_Id(String.valueOf(fiscalEntity.getId()));
						invoice.setFe_taxid(String.valueOf(fiscalEntity.getTaxID()));
						ByteArrayOutputStream baosXml = xmlGenerator.convierteFU(comp);
						invoice.setByteArrXMLSinAddenda(baosXml);
			    		Document document = UtilCatalogos.convertStringToDocument(invoice.getByteArrXMLSinAddenda().toString("UTF-8"));
			    		String totalIvaRet = UtilCatalogos.getStringValByExpression(document, "//Comprobante/Impuestos/@TotalImpuestosTrasladados");
			    		
			    		if (!totalIvaRet.equals("")) {
				    		BigDecimal bdIva = new BigDecimal(totalIvaRet);
				    		invoice.setIva(bdIva.doubleValue());
						} else {
							BigDecimal bdIva = new BigDecimal(0);
				    		invoice.setIva(bdIva.doubleValue());
						}

			    		 String errors = UtilCatalogos.validateCfdiDocumentDivisasNew(document, comp.getDecimalesMoneda());            
			            if(!Util.isNullEmpty(errors)){
			            	
			            	System.out.println("Factura:  --Exception: " + "\n" + errors);
							fileINCIDENCIA( errors, encabezado );
							
			            } else {
			            	
			            	StringWriter sw = documentToStringWriter(document);
			            	byte[] xmlBytes = sw.toString().getBytes("UTF-8");
							baosXml = new ByteArrayOutputStream(xmlBytes.length);
							baosXml.write(xmlBytes, 0, xmlBytes.length);
							
							System.out.println("---XML despues de validar decimales---");
							System.out.println(baosXml.toString("UTF-8"));
							System.out.println("---Fin XML despues de validar decimales---");
							
							//agregar certificado y sello
							baosXml = xmlGenerator.reemplazaCadenaOriginalNew(baosXml, fiscalEntity, comp.isTasaCero());
							
							System.out.println("---XML despues de reemplazar cadena original---");
							System.out.println(baosXml.toString("UTF-8"));
							System.out.println("---Fin XML despues de reemplazar cadena original---");
							
							invoice.setByteArrXMLSinAddenda(baosXml);
							
							//crearFactura(0);
							
							
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
							
							year = (Integer.parseInt(year) < 10 ? "0" : "") + year;
							month = (Integer.parseInt(month) < 10 ? "0" : "") + month;
							day = (Integer.parseInt(day) < 10 ? "0" : "") + day;
							hora = (Integer.parseInt(hora) < 10 ? "0" : "") + hora;
							minuto = (Integer.parseInt(minuto) < 10 ? "0" : "") + minuto;
							segundo = (Integer.parseInt(segundo) < 10 ? "0" : "") + segundo;
							 
							
							String contador = String.format("%03d",this.contadorMilli);
							
							
							
							folio += year.substring(year.length() - 2 );
							folio += month+day+hora+minuto+segundo;
							folio += idProceso;
							folio += contador;
							
							
							
							
							System.out.println("folioXD: "+folio);
							
							invoice.setFolio(folio);
							
							listIn.add(invoice);
							comp.setEncabezado(encabezado);
							listComprobantes.add(comp);
							this.contadorMilli += 1;
			            	
			            }
						
					}catch (Exception e) {								
						
						System.out.println("Factura:  --Exception: " + "\n" + e.getMessage());
						fileINCIDENCIA( e.getMessage(), encabezado );
						
					}
					
				}
				
			}
			
		} else {
			
			error = "Estructura de Archivo incorrecta \n";
			fileINCIDENCIA( error, encabezado);
			
		}
		
		
	}
	

	/**
	 * @param error
	 * @param encabezado
	 * @throws IOException
	 */
	public void fileINCIDENCIA( String error, String encabezado ) throws IOException {
		
		long t1 = System.currentTimeMillis();
		incidencia.write( (encabezado + "\r\n").getBytes() );
		incidencia.write("Se presentaron los siguientes errores al validar la estructura del comprobante: \r\n".getBytes());
		temp = "Error: " + error + "\r\n";	
		temp += "Inicio de CFD: " + startLine + "\r\n";
		incidencia.write(temp.getBytes("UTF-8"));
		
		
		temp = null;
		long t2 = t1- System.currentTimeMillis();
		System.out.println("TIME: Escritura archivo incidente fileIncidencia " + t2 + " ms");
		
	}	
	

	/**
	 * @param dom
	 * @return
	 * @throws Exception
	 */
	public StringWriter documentToStringWriter(Document dom) throws Exception{
		StringWriter sw2 = new StringWriter();		
		
		if(this.tx == null){
			this.tx = TransformerFactory.newInstance().newTransformer();
			this.tx.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		}					
		this.tx.transform(new DOMSource(dom), new StreamResult(sw2));
		
		return sw2;
	}
	
	/**
	 * @param renglon
	 * @throws Exception
	 */
	public void crearFactura(int renglon)throws Exception {
		CFDIssued cFDIssued = new CFDIssued();
		
		String fecha = systemDate();
		Date date=Calendar.getInstance().getTime();
		 OpenJpa open = openJpaManager.getFolioById(fiscalEntity.getId());
		 System.out.println("fiscalEntityID: " + fiscalEntity.getId() + " taxid: " + fiscalEntity.getTaxID());
         if(open==null) {
             open = new OpenJpa();
             open.setId(fiscalEntity.getId());
             open.setSequence_value(0);
         }
         open.setSequence_value(open.getSequence_value()+1);
		 invoice.setFolio(String.valueOf(open.getSequence_value()));		 
		 //try {
		 	
		 	System.out.println("antes valida XML");
		 	String nameFile = "";

		 	nameFile = xmlProcessGeneral.generateFileName(fecha, false, 0, false);
		 	ByteArrayOutputStream out = invoice.getByteArrXMLSinAddenda();
		 	generaXML.setOut(out);
		 	
		 	//validar xml
		 	if(generaXML.getOut().size()==0){
				logger.debug("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ Archivo Vacio");
				System.out.println("El archivo esta vacio ++++");
			} else {
				logger.debug("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ Archivo");
				logger.debug("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ Validando Archivo");
				System.out.println("El archivo pesa ++++ "+generaXML.getOut().size());
				
				System.out.println("XML antes de validar: "+generaXML.getOut().toString("UTF-8"));
				xmlProcessGeneral.validaCFDI33(generaXML.getOut());
			}
			
			System.out.println("despues valida XML");
			cFDIssued.setCreationDate(date);
			cFDIssued.setIssueDate(date);
			cFDIssued.setDateOfIssuance(date);
			Route route = new Route();
			route.setRoute(nameFile);
			cFDIssued.setFilePath(route);
			
			cFDIssued.setStatus(Integer.parseInt(statusActive));
			
			cFDIssued.setFormatType(4);
			
			cFDIssued.setTaxIdReceiver(invoice.getRfc());
			cFDIssued.setSubTotal(invoice.getSubTotal()	* invoice.getExchange());
			cFDIssued.setIva(invoice.getIva() * invoice.getExchange());
			cFDIssued.setTotal(invoice.getTotal() * invoice.getExchange());
			cFDIssued.setContractNumber(invoice.getContractNumber());
			cFDIssued.setCostCenter(invoice.getCostCenter());
			cFDIssued.setCustomerCode(invoice.getCustomerCode());
			cFDIssued.setPeriod(invoice.getPeriod());
			cFDIssued.setCfdType(invoice.getTipoFormato().toUpperCase().substring(0, 1));
			
			cFDIssued.setFiscalEntity(fiscalEntity);
			

            cFDIssued.setCreationDate(new Date());
            cFDIssued.setAuthor("masivo");
            cFDIssued.setModifiedBy("masivo");

			
			boolean continuarIntentando=true;
            int contIntentos=0;
            while(continuarIntentando==true) {
            	contIntentos++;
            	try {
            		openJpaManager.update(open);            		
            		continuarIntentando=false;
            	}catch(Exception e) {
            		if(contIntentos<=5) {
            			continuarIntentando=true;
            			Thread.sleep(1000);
            		} else {
            			continuarIntentando=false;
            			throw new Exception("Documento timbrado. Problema al actualizar secuencia de folio en la base de datos");
            		}
            	}
            }

            
          
	}
	
	/**
	 * @param strXML
	 * @return
	 * @throws Exception
	 */
	public Document stringToDocument(String strXML) throws Exception{
		Document domResultado = null;
	
		if (this.db == null){
			this.dbf = DocumentBuilderFactory.newInstance();
			this.db = this.dbf.newDocumentBuilder();
		}
		
		domResultado = this.db.parse(new InputSource(new StringReader(strXML)));	
		
		return domResultado;
	}
	
	/**
	 * @param invoiceM
	 * @param strUUID
	 * @param strSourceFileName
	 * @param idUsuario
	 * @param idArea
	 * @param nombreUsuario
	 * @return
	 */
	public String buildRowBDFile(Invoice_Masivo invoiceM, String strUUID, String strSourceFileName, String idUsuario, String idArea, String nombreUsuario){		
		StringBuffer temp = new StringBuffer();
		Date date=Calendar.getInstance().getTime();
		String strDate = Util.convertirFecha(date);
		strDate = strDate.replaceAll("T", " ");
		temp.append("c<#EMasfUD,>");
		temp.append(strDate + "<#EMasfUD,>");
		temp.append(strDate + "<#EMasfUD,>");
		temp.append(strDate + "<#EMasfUD,>");
		temp.append(invoiceM.getXmlRoute() + "<#EMasfUD,>");
		temp.append(strSourceFileName + "<#EMasfUD,>");
		temp.append(Integer.parseInt(this.statusActive) + "<#EMasfUD,>");
		temp.append(4 + "<#EMasfUD,>");
		temp.append(invoiceM.getRfc() + "<#EMasfUD,>");
		temp.append(invoiceM.getSubTotal()*invoiceM.getExchange() + "<#EMasfUD,>");
		temp.append(invoiceM.getIva()*invoiceM.getExchange() + "<#EMasfUD,>");
		temp.append(invoiceM.getTotal()*invoiceM.getExchange() + "<#EMasfUD,>");
		temp.append(invoiceM.getContractNumber() + "<#EMasfUD,>");
		temp.append(invoiceM.getCostCenter() + "<#EMasfUD,>");
		temp.append(invoiceM.getCustomerCode() + "<#EMasfUD,>");
		temp.append(invoiceM.getPeriod() + "<#EMasfUD,>");
		temp.append(invoiceM.getTipoFormato().toUpperCase().substring(0, 1) + "<#EMasfUD,>");
		temp.append(invoiceM.getFe_Id() + "<#EMasfUD,>");				
		temp.append(1 + "<#EMasfUD,>");		
		temp.append(invoiceM.getFolio() + "<#EMasfUD,>");
		temp.append(strUUID + "<#EMasfUD,>");
		temp.append(invoiceM.getMotivoDescuento() + "<#EMasfUD,>");
		temp.append(invoiceM.getDescuento() + "<#EMasfUD,>");
		temp.append(invoiceM.getExchange() + "<#EMasfUD,>");
		temp.append(idUsuario + "<#EMasfUD,>");
		temp.append(idArea + "<#EMasfUD,>");
		temp.append(nombreUsuario + "<#EMasfUD,>");
		temp.append(nombreUsuario + "\r\n");
		
		return temp.toString();
	}
	
	
	/**
	 * 
	 */
	private void closeByte() {
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
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public HashMap<Integer, String> getHashIvas() {
		return hashIvas;
	}
	public void setHashIvas(HashMap<Integer, String> hashIvas) {
		this.hashIvas = hashIvas;
	}
	public HashMap<String, String> getHashcodigoISO() {
		return hashcodigoISO;
	}
	public void setHashcodigoISO(HashMap<String, String> hashcodigoISO) {
		this.hashcodigoISO = hashcodigoISO;
	}
	public HashMap<String, String> getHashmoneda() {
		return hashmoneda;
	}
	public void setHashmoneda(HashMap<String, String> hashmoneda) {
		this.hashmoneda = hashmoneda;
	}
	public HashMap<String, Customer> getHashClientes() {
		return hashClientes;
	}
	public void setHashClientes(HashMap<String, Customer> hashClientes) {
		this.hashClientes = hashClientes;
	}
	public HashMap<Long, String> getHashCfdFieldsV2() {
		return hashCfdFieldsV2;
	}
	public void setHashCfdFieldsV2(HashMap<Long, String> hashCfdFieldsV2) {
		this.hashCfdFieldsV2 = hashCfdFieldsV2;
	}
	public String getUrlWSTimbrado() {
		return urlWSTimbrado;
	}
	public void setUrlWSTimbrado(String urlWSTimbrado) {
		this.urlWSTimbrado = urlWSTimbrado;
	}
	public String getNameFile() {
		return nameFile;
	}
	public void setNameFile(String nameFile) {
		this.nameFile = nameFile;
	}
	public HashMap<String, String> getNombresApps() {
		return nombresApps;
	}
	public void setNombresApps(HashMap<String, String> nombresApps) {
		this.nombresApps = nombresApps;
	}
	
	

}
