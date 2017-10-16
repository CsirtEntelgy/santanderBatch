package com.interfactura.firmalocal.xml.masivo;

//import static com.interfactura.firmalocal.xml.util.Util.getTASA;
import static com.interfactura.firmalocal.xml.util.Util.systemDate;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.ValidatorHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.interfactura.firmalocal.controllers.MassiveDonatReadController;
import com.interfactura.firmalocal.datamodel.CfdiComprobanteFiscal;
import com.interfactura.firmalocal.datamodel.ElementsInvoice;
import com.interfactura.firmalocal.datamodel.Invoice_Masivo;
import com.interfactura.firmalocal.domain.entities.CFDFieldsV22;
import com.interfactura.firmalocal.domain.entities.CFDIssued;
import com.interfactura.firmalocal.domain.entities.CodigoISO;
import com.interfactura.firmalocal.domain.entities.Customer;
import com.interfactura.firmalocal.domain.entities.FiscalEntity;
import com.interfactura.firmalocal.domain.entities.Iva;
import com.interfactura.firmalocal.domain.entities.Massive;
import com.interfactura.firmalocal.domain.entities.Moneda;
import com.interfactura.firmalocal.domain.entities.OpenJpa;
import com.interfactura.firmalocal.domain.entities.Route;
import com.interfactura.firmalocal.domain.entities.SealCertificate;
import com.interfactura.firmalocal.domain.entities.User;
import com.interfactura.firmalocal.persistence.CFDFieldsV22Manager;
import com.interfactura.firmalocal.persistence.CFDIssuedManager;
import com.interfactura.firmalocal.persistence.CodigoISOManager;
import com.interfactura.firmalocal.persistence.CustomerManager;
import com.interfactura.firmalocal.persistence.FiscalEntityManager;
import com.interfactura.firmalocal.persistence.IvaManager;
import com.interfactura.firmalocal.persistence.MassiveManager;
import com.interfactura.firmalocal.persistence.MonedaManager;
import com.interfactura.firmalocal.persistence.OpenJpaManager;
import com.interfactura.firmalocal.persistence.UserManager;
import com.interfactura.firmalocal.xml.Properties;
import com.interfactura.firmalocal.xml.TagsXML;
import com.interfactura.firmalocal.xml.WebServiceCliente;
import com.interfactura.firmalocal.xml.WebServiceClienteDonat;

import com.interfactura.firmalocal.xml.file.GeneraArchivoDonat_Masivo;
import com.interfactura.firmalocal.xml.file.XMLProcess;
import com.interfactura.firmalocal.xml.util.GeneraXmlFacturaCfdiV3_3;
import com.interfactura.firmalocal.xml.util.Util;
import com.interfactura.firmalocal.xml.util.UtilCFDIDonatarias;
import com.interfactura.firmalocal.xml.util.UtilCFDIValidations;
import com.interfactura.firmalocal.xml.util.UtilCatalogos;
import com.interfactura.firmalocal.xml.util.XMLProcessGeneral;

@Component
public class GeneraDonatXML_Masivo {
	/*private Logger logger = Logger.getLogger(GeneraDonatXML_Masivo.class);
	private BufferedReader br;
	private String linea;
	private String token;*/
	
	@Autowired
	private Properties properties;
	
	private long offSetComprobante = 0;
	@Autowired
	private XMLProcess xmlProcess;
	private Transformer transf;
	
	private String nameFile;
	
	private HashMap<String, FiscalEntity> lstFiscal;
	
	private List<SealCertificate> lstSeal;
	private ValidatorHandler validator;
	
	private WebServiceCliente servicePort = null;
	private DocumentBuilderFactory dbf = null;
	private DocumentBuilder db = null;
	private Transformer tx = null;
	
	private String urlWebService = null;
	
    
    
    ///////////////////////////////////////
    @Autowired(required=true)
	private FiscalEntityManager fiscalEntityManager;
    @Autowired(required = true)
	private CFDFieldsV22Manager cfdFieldsV22Manager;
    @Autowired(required = true)
	private CFDIssuedManager cFDIssuedManager;
	@Autowired(required = true)
	private CustomerManager customerManager;
	@Autowired(required = true)
	private OpenJpaManager openJpaManager;
	@Autowired(required = true)
	private MonedaManager monedaManager;
	@Autowired(required = true)
	private CodigoISOManager codigoISOManager;
	@Autowired
	private GeneraArchivoDonat_Masivo generaXML;
	@Autowired
	private IvaManager ivaManager;
	
	@Autowired(required = true)
	private UtilCFDIValidations validations;
	
	@Autowired(required = true)
	private GeneraXmlFacturaCfdiV3_3 xmlGenerator;
	
	@Autowired
	private XMLProcessGeneral xmlProcessGeneral;

	@Value("${invoice.status.active}")
	private String statusActive;
	
    Invoice_Masivo invoice = null;
	FiscalEntity fiscalEntity = null;
	//Emisor fiscalEntity = null;
	Vector<String> vectorCantidad = null;
	Vector<String> vectorUM = null;
	Vector<String> vectorDesc = null;
	Vector<Double> vectorPrecioUnitario = null;
	Vector<String> vectorAplicaIVA = null;
	Vector<Double> vectorTotal = null;
    
	private static final String RE_DECIMAL = "[0-9]+(\\.[0-9][0-9]?[0-9]?[0-9]?)?";
	//private static final String RE_DECIMAL_NEGATIVO = "[\\-]?[0-9]+(\\.[0-9][0-9]?[0-9]?[0-9]?)?";
	private static final String RE_DECIMAL_QTY = "[0-9]{1,10}(\\.[0-9]{0,3})?";
	private static final String RE_DECIMAL_NEGATIVO = "[\\-]?[0-9]{1,10}(\\.[0-9]{0,4})?";
	private static final String RE_CHAR = "[A-Za-z\\s0-9áéíóúÁÉÍÓÚñÑ\\.,()\\-\\/&]+";
    private static final String RE_CHAR_ONLY = "[A-Za-z]+";
    private static final String RE_CHAR_NUMBER = "[A-Za-z0-9]+";
    
    private static final String RE_CHARNUMBER_IDEXT = "[A-Za-z0-9\\-\\s]*";
    private static final String RE_CHAR_ALL = "[a-zA-ZÑñáéíóúÁÉÍÓÚ0-9\\$\\%\\s\\?\\)\\¡\\¿\\(\\[\\]\\{\\}\\+\\/\\=\\@\\_\\!\\#\\*\\:\\;\\.\\&\\,\\-]*";
    
    private static final String RE_NUMBER = "[0-9]+";
    private static final String RE_MAIL = "^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,3})$";

    private StringBuilder sb;
    
    private List<Invoice_Masivo> listIn = null;
    private List<CfdiComprobanteFiscal> listComprobantes = null;
    
    //private static String strValues = "";
	private static int rows;
	private boolean finArchivo;
	//private static int cols;
        
	private File fileExitXML = null;				
	private File fileExitBD = null;
	private File fileExitBDDonat = null;
	//private File fileExitODM = null;
	private File fileExitINC = null;
		
	private FileOutputStream salidaXML = null;
	private FileOutputStream salidaBD = null;
	private FileOutputStream salidaBDDonat = null;
	private FileOutputStream salidaINC = null;
	private FileOutputStream salidaODM = null;
	
	String facturacionDonatEntrada=MassiveDonatReadController.facturacionDonatEntrada;
	String facturacionDonatProceso=MassiveDonatReadController.facturacionDonatProceso;
	String facturacionDonatSalida=MassiveDonatReadController.facturacionDonatSalida;
	String facturacionDonatOndemand=MassiveDonatReadController.facturacionDonatOndemand;
	
	@Autowired(required = true)
	private MassiveManager massiveManager;
	
	@Autowired(required = true)
	private UserManager userManager;
	
	@Autowired(required = true)
	private UtilCFDIDonatarias fillDonatarias;
	
	@Autowired
	private TagsXML tags;
	
   	public GeneraDonatXML_Masivo() {

	}

	/**
	 * 
	 * @param nameFile
	 */
	public GeneraDonatXML_Masivo(String nameFile) {
		this.nameFile = nameFile;
	}

	/**
	 * Proceso de generacion de ECB para un solo archivo de entrada
	 * 
	 * @return
	 */
	public void processControlFile(String urlWSTimbrado){
		String idMassive = "";
		String nameFileExcel = "";
		try{
			
			tags.mapCatalogos = Util.readXLSFile(properties.getUrlArchivoCatalogs());
			
			FileInputStream fsExcelsToProcess = new FileInputStream(facturacionDonatEntrada + "IDFILEPROCESS.TXT");
			DataInputStream in = new DataInputStream(fsExcelsToProcess);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			int counter=0;
			
			sb = new StringBuilder();
			
			//Iniciar conexion con WebService								
			if(this.servicePort == null){
				this.servicePort = new WebServiceCliente();								
			}	

			//Crear hashMaps
			
			HashMap<Integer, String> hashIvas = new HashMap<Integer, String>();
			for(Iva iva:ivaManager.listar()){
				 hashIvas.put(iva.getTasa(), iva.getDescripcion());
				}//catalogchange
			
			HashMap<String, String> hashcodigoISO = new HashMap<String, String>();
			for(CodigoISO codigoiso:codigoISOManager.listar()){
				 hashcodigoISO.put(codigoiso.getCodigo(), codigoiso.getDescripcion());
				}//catalogchange
			
			HashMap<String, String> hashmoneda = new HashMap<String, String>();
			for(Moneda moneda:monedaManager.listar()){
				 hashmoneda.put(moneda.getNombreCorto(), moneda.getNombreLargo());
				}//catalogchange
			
			HashMap<String,FiscalEntity> hashEmisores = new HashMap<String,FiscalEntity>(); 
			for(FiscalEntity fE:fiscalEntityManager.listar()){
				FiscalEntity fENew = fE;
											
				hashEmisores.put(fENew.getTaxID(), fENew);
			}
			
			HashMap<String,Customer> hashClientes = new HashMap<String,Customer>();
			for(Customer customer:customerManager.listar()){
				Customer customerNew = customer;
								
				if(customerNew.getTaxId().toUpperCase().trim().equals("XEXX010101000")){
					//Cliente extranjero
					hashClientes.put(customer.getIdExtranjero(), customerNew);
				}else{
					//Cliente normal
					hashClientes.put(customerNew.getTaxId()+customerNew.getFiscalEntity().getId(), customerNew);
				}
				
			}
			
			HashMap<Long,CFDFieldsV22> hashCfdFieldsV22 = new HashMap<Long,CFDFieldsV22>();
			for(CFDFieldsV22 cfdV22:cfdFieldsV22Manager.listAll()){
				/*String strLugarExp = "";
				String strRegFiscal = "";
				if(cfdV22.getRegimenFiscal()!=null){
					if(cfdV22.getRegimenFiscal().getName()!=null){
						strRegFiscal = cfdV22.getRegimenFiscal().getName(); 
					}
					if(cfdV22.getLugarDeExpedicion()!=null){
						strLugarExp = cfdV22.getLugarDeExpedicion(); 
					}
				}
				hashCfdFieldsV22.put(cfdV22.getFiscalEntity().getId(), strLugarExp+"|"+strRegFiscal);*/
				hashCfdFieldsV22.put(cfdV22.getFiscalEntity().getId(), cfdV22);
			}
			
			
			
			FileOutputStream userlog = null;
			FileOutputStream fileStatus = new FileOutputStream(facturacionDonatProceso + "massiveDonatProcess.txt");
			fileStatus.write(("Status del proceso bash massiveDonatProcess.sh" + "\n").getBytes("UTF-8"));
			while((strLine = br.readLine()) != null){
				System.out.println("lineExcelsToProcess: " + strLine);
				String [] arrayRenglon = strLine.split("\\|");
				if(arrayRenglon.length>1){
					idMassive = arrayRenglon[0];
					
					//Obtener Id del Area y del usuario que hizo la solicitud 
					Massive massive = massiveManager.getById(Long.parseLong(idMassive));
					User user = userManager.findByName(massive.getAuthor());					
					String idUsuario = "0";
					String idArea = "0";
					
					if(user != null){
						idUsuario = String.valueOf(user.getId());
						idArea = String.valueOf(user.getIdArea());
					}
					
					System.out.println("idUsuario:" + idUsuario);
					System.out.println("idArea:" + idArea);
					
					nameFileExcel = arrayRenglon[1] + ".TXT";
					System.out.println("idMassive: " + idMassive);
					System.out.println("nameFileExcel: " + nameFileExcel);
															
					File fileTXT = new File(facturacionDonatProceso + arrayRenglon[1] + "/" + nameFileExcel);
					if(!fileTXT.exists()){
						fileStatus.write(("El archivo " + nameFileExcel + " no se encuentra en la ruta " + facturacionDonatProceso + arrayRenglon[1] + "/" + "\n").getBytes("UTF-8"));
						userlog = new FileOutputStream(facturacionDonatSalida + arrayRenglon[1] + "/LOG" + arrayRenglon[1] + ".TXT",true);
						userlog.write(("El archivo " + nameFileExcel + " no se encuentra en la ruta " + facturacionDonatProceso + arrayRenglon[1] + "/" + "\r\n").getBytes("UTF-8"));
					}else{
						//Crear directorio en ruta ../facturacion/salida/
						/*File fileDirectory = new File(facturacionDonatSalida + arrayRenglon[1] + "/");
						if(!fileDirectory.exists()){
			            	fileDirectory.mkdir();
			            }else{
			            	for(File file:fileDirectory.listFiles()){
			               	 file.delete();
			                }            	
			            }*/
						
						this.offSetComprobante = 0;
						//String strAbsolutePathXML = properties.getPathFacturacionDonatSalida() + arrayRenglon[1] + "/" + "XML" + nameFileExcel.substring(0, nameFileExcel.indexOf(".")) + ".TXT";
						String strAbsolutePathBD = facturacionDonatProceso + arrayRenglon[1] + "/" + "BD" + nameFileExcel.substring(0, nameFileExcel.indexOf(".")) + ".TXT";
						String strAbsolutePathBDDonat = facturacionDonatProceso + arrayRenglon[1] + "/" + "BDDONAT" + nameFileExcel.substring(0, nameFileExcel.indexOf(".")) + ".TXT";
						//String strAbsolutePathODM = properties.getPathFacturacionDonatOndemand() + "ODM-" + nameFileExcel.substring(0, nameFileExcel.indexOf("."));
						//String strAbsolutePathINC = properties.getPathFacturacionDonatSalida() + arrayRenglon[1] + "/" + "INC" + nameFileExcel.substring(0, nameFileExcel.indexOf(".")) + ".TXT";
						
						//this.fileExitXML = new File(strAbsolutePathXML);				
						this.fileExitBD = new File(strAbsolutePathBD);
						this.fileExitBDDonat = new File(strAbsolutePathBDDonat);
						//this.fileExitODM = new File(strAbsolutePathODM);
						//this.fileExitINC = new File(strAbsolutePathINC);
						
						
						//this.salidaXML = new FileOutputStream(this.fileExitXML);
						this.salidaBD = new FileOutputStream(this.fileExitBD);
						this.salidaBDDonat = new FileOutputStream(this.fileExitBDDonat);
						//this.salidaINC = new FileOutputStream(this.fileExitINC);
						//this.salidaODM = new FileOutputStream(this.fileExitODM);
						
						//StringBuilder sbErrorFile = this.processExcelFile("/planCFD/procesos/Interfactura/interfaces/" + nameFileExcel);
						
						//processOneSheet("/planCFD/procesos/Interfactura/interfaces/" + nameFileExcel);
						
						FileInputStream fisTxt = new FileInputStream(facturacionDonatProceso + arrayRenglon[1] + "/" + nameFileExcel);
						DataInputStream disTXT = new DataInputStream(fisTxt);
						BufferedReader brTXT = new BufferedReader(new InputStreamReader(disTXT));
						String strLineTXT;
						int factura=0;
						
						StringBuilder sbStatusOK = new StringBuilder();
						StringBuilder sbStatusNoOK = new StringBuilder();
		
						int counterOk = 0;
						int counterNoOk = 0;
						
						while((strLineTXT = brTXT.readLine()) != null){
							
							String [] arrayValues = strLineTXT.split("\\|");
							System.out.println("strLine:" + strLineTXT);
							System.out.println("length:" + arrayValues.length);
							if(!strLineTXT.equals("")){
								if(arrayValues.length<33){
									System.out.println("Factura " + (factura+1) + ", incompleta!");								
									//salidaINC.write(("ErrorArchivo|" + "Factura " + (factura+1) + ", incompleta!" + "\n").getBytes("UTF-8"));
									sbStatusNoOK.append("ErrorArchivo|" + "Factura " + (factura+1) + ", incompleta!" + "\n");
									
									counterNoOk++;
									
								}else if(!arrayValues[arrayValues.length-1].trim().toUpperCase().equals("FINFACTURA")){
									System.out.println("Estructura de Archivo incorrecta, no fue encontrada la etiqueta de control ||FINFACTURA|| en la factura " + (factura+1));
									//salidaINC.write(("ErrorArchivo|" + "Estructura de Archivo incorrecta, no fue encontrada la etiqueta de control ||FINFACTURA|| en la factura " + (factura+1) + "\n").getBytes("UTF-8"));
									sbStatusNoOK.append("ErrorArchivo|" + "Estructura de Archivo incorrecta, no fue encontrada la etiqueta de control ||FINFACTURA|| en la factura " + (factura+1) + "\n");
									
									counterNoOk++;
									
								}else{
									this.listIn = new ArrayList<Invoice_Masivo>();
									
this.listComprobantes = new ArrayList<CfdiComprobanteFiscal>();
									
									CfdiComprobanteFiscal comp = new CfdiComprobanteFiscal();
									//antiguo metodo fill y validate
									//StringBuilder sbErrorFile = this.processRowExcel(arrayValues, factura+1, hashIvas, hashcodigoISO, hashmoneda, hashEmisores, hashClientes, hashCfdFieldsV22);
									StringBuilder sbErrorFile = new StringBuilder();
									
									//llenar comprobante desde archivo
									comp = fillDonatarias.fillComprobanteDonatTXT(arrayValues);
									
									//validar comprobante
									sbErrorFile.append(validations.validateComprobante(comp, factura+1));
									
									//convertir comprobante a invoice
									invoice = new Invoice_Masivo();
									invoice = UtilCatalogos.fillInvoice(comp);
									
									if(sbErrorFile.toString().length() > 0){
										sb.append("Factura: " + factura + " -- Lista de Errores: " + "\n" + sbErrorFile.toString() + "\n");								
										System.out.println("fError EmisionMasivaFacturas: " + sb.toString());							
										invoice.setSbError(sbErrorFile);
										listIn.add(invoice);
										listComprobantes.add(comp);
									}else{								
										
										try{
											System.out.println("AntesfacturaOK - " + factura);
											System.out.println("Antes de crearFac");
											
											if(!invoice.getSiAplicaIva())								
												invoice.setDescriptionIVA("EXENTO");
											
											//generar xml y asignarlo a invoice
											fiscalEntity = new FiscalEntity();
											fiscalEntity.setTaxID(comp.getEmisor().getRfc());
											fiscalEntity = fiscalEntityManager.findByRFCA(fiscalEntity);
											invoice.setFe_Id(String.valueOf(fiscalEntity.getId()));
											invoice.setFe_taxid(String.valueOf(fiscalEntity.getTaxID()));
											ByteArrayOutputStream baosXml = xmlGenerator.convierte(comp);
											invoice.setByteArrXMLSinAddenda(baosXml);
											
											/* Se obtiene el totalIvaretenido y se asigna al IVA*/
								    		Document document = UtilCatalogos.convertStringToDocument(invoice.getByteArrXMLSinAddenda().toString("UTF-8"));
								    		String totalIvaRet = UtilCatalogos.getStringValByExpression(document, "//Comprobante/Impuestos/@TotalImpuestosTrasladados");
								    		BigDecimal bdIva = new BigDecimal(totalIvaRet);
								    		invoice.setIva(bdIva.doubleValue());
								    		/*Fin Cambio*/
								    		
								    		//doc = UtilCatalogos.convertPathFileToDocument(nameFile);
								            String errors = UtilCatalogos.validateCfdiDocument(document, comp.getDecimalesMoneda());            
								            if(!Util.isNullEmpty(errors)){
								            	throw new Exception(errors);
								            }else{
								            	StringWriter sw = documentToStringWriter(document);
								            	byte[] xmlBytes = sw.toString().getBytes("UTF-8");
												baosXml = new ByteArrayOutputStream(xmlBytes.length);
												baosXml.write(xmlBytes, 0, xmlBytes.length);
												
												System.out.println("---XML despues de validar decimales---");
												System.out.println(baosXml.toString("UTF-8"));
												System.out.println("---Fin XML despues de validar decimales---");
												
												//agregar certificado y sello
												baosXml = xmlGenerator.reemplazaCadenaOriginal(baosXml, fiscalEntity);
												
												System.out.println("---XML despues de reemplazar cadena original---");
												System.out.println(baosXml.toString("UTF-8"));
												System.out.println("---Fin XML despues de reemplazar cadena original---");
												
												invoice.setByteArrXMLSinAddenda(baosXml);
								            }
											
											crearFactura(factura);
											
											System.out.println("facturaOK - " + factura);
											listIn.add(invoice);
											listComprobantes.add(comp);
										}catch (Exception e) {								
											sb.append("Factura: " + factura + " --Exception: " + "\n" + e.getMessage() + "\n");
											System.out.println("Factura: " + factura + " --Exception: " + "\n" + e.getMessage());
											invoice.setSbError(new StringBuilder("Factura: " + factura + " --Exception: " + e.getMessage() + "\n"));
											listIn.add(invoice);
											listComprobantes.add(comp);
										}
										
										
									}
									
									if(sbErrorFile.toString().length() > 0){
										//salidaINC.write(("ErrorArchivo|" + sbErrorFile.toString() + "\n").getBytes("UTF-8"));
										sbStatusNoOK.append("ErrorArchivo|" + sbErrorFile.toString() + "\n");
										
										counterNoOk++;
									}else{
										for(int index=0; index<listIn.size(); index++){
											if(listIn.get(index).getByteArrXMLSinAddenda() != null){
												String strXmlATimbrar = listIn.get(index).getByteArrXMLSinAddenda().toString("UTF-8");
												CfdiComprobanteFiscal comprobante = listComprobantes.get(index);
												/////////////Inicio Bloque de Timbrado//////////////////
																				
												// Invoke the web service operation using the port or stub or proxy
												 
												//String helloMessage = servicePort.consultaTimbre("F7EFFF4D-2816-425C-81CC-B26DF8C177DB", null, true) ;
												System.out.println("Conectandose...........");
																	
												System.out.println("nomInterface" + nameFileExcel.substring(0, nameFileExcel.indexOf(".")) + "--");
												String xmlTimbradoConPipe = ""; 
												xmlTimbradoConPipe = this.servicePort.generaTimbre(strXmlATimbrar, false, urlWSTimbrado, properties, nameFileExcel.substring(0, nameFileExcel.indexOf(".")), 0, 3, "", "");
												
												String xmlTimbrado = xmlTimbradoConPipe.substring(0, xmlTimbradoConPipe.length()-1);
												
												System.out.println("XML Timbrado: " + xmlTimbrado);
												/////////////Fin Bloque de Timbrado//////////////////
												//Convertir de string a Document
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
													//System.out.println("GeneraAddenda");
													//doc = this.generaXML.agregaAddenda(doc, invoice);//agrega addenda anterior
													
													//agrega addenda nuevo 3.3
													doc = xmlGenerator.agregaAddenda(doc, comprobante);
													
													//Obtener el UUID 
													String strUUID = this.generaXML.getUUID(doc).trim();
													
													//Convertir de Document a StringWriter
													StringWriter swXmlFinal = documentToStringWriter(doc);											
													StringBuffer sbXmlFinal= new StringBuffer();			
													
													//Convetir de StringWriter a StringBuffer
													sbXmlFinal = swXmlFinal.getBuffer();
													
													//Quitar los saltos de linea
													String strXmlfinalConAddenda = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + sbXmlFinal.toString().replaceAll("[\n\r]", "");
													
													//System.out.println("Guarda en XML...TXT");
													//salidaXML.write(("Factura " + (factura+1) + " correcta!" + "\r\n").getBytes("UTF-8"));
													
													sbStatusOK.append("Factura " + (factura+1) + " correcta!, folioSAT:" + strUUID + "\r\n");
													
													counterOk++;
													
													//Guardar ruta y posicion inicio y fin
													long length = strXmlfinalConAddenda.getBytes("UTF-8").length;									
													//listIn.get(index).setXmlRoute(strAbsolutePathODM + "|" + this.offSetComprobante + "|" + (this.offSetComprobante + length));
													
													//String strAbsolutePathXml = properties.getPathDirGenr() + idMassive + nameFileExcel.substring(0, nameFileExcel.indexOf(".")) + ".xml";									
													String strAbsolutePathOndemand = facturacionDonatOndemand + nameFileExcel.substring(0, nameFileExcel.indexOf(".")) + "/";
													File  fileDirectoryOndemand = new File(strAbsolutePathOndemand);
													
													if(!fileDirectoryOndemand.exists()){
														fileDirectoryOndemand.mkdir();
													}
													
													String strAbsolutePathXml = strAbsolutePathOndemand + "fac-" + strUUID + ".xml";
																										
													File fileExitXml = new File(strAbsolutePathXml);
													
													FileOutputStream salidaXml = new FileOutputStream(fileExitXml);								
													salidaXml.write(strXmlfinalConAddenda.getBytes("UTF-8"));
													salidaXml.close();
													
													listIn.get(index).setXmlRoute(strAbsolutePathXml + "|" + 0 + "|" + 0);									
																						
													String strDate = getDate();
													System.out.println("Guarda en BD...TXT");
													salidaBD.write(this.buildRowBDFile(listIn.get(index), strUUID, nameFileExcel, strDate, idUsuario, idArea).getBytes("UTF-8"));
													
													System.out.println("Guarda en BDDONAT...TXT");
													salidaBDDonat.write(this.buildRowBDDonatFile(listIn.get(index), strUUID, nameFileExcel, strDate).getBytes("UTF-8"));
													
													//System.out.println("Guarda en ODM...TXT");									
													//salidaODM.write(strXmlfinalConAddenda.getBytes("UTF-8"));
												
													this.offSetComprobante += length; 
												} else {
													System.out.println("INC error durante el timbrado ");
													String temp = "ErrorFactura|" + (factura+1) + "|" + nameFileExcel+ "|"
															+ listIn.get(index).getFe_taxid() + "|" + listIn.get(index).getRfc() + "|" + "\r\n";
														//salidaINC.write(temp.getBytes("UTF-8"));
														//salidaINC.write((strIdRespuesta + strDescripcion + "\r\n").getBytes("UTF-8"));
														sbStatusNoOK.append(temp + strIdRespuesta + strDescripcion + "\r\n");
														
														counterNoOk++;
												}							
											}else{
												System.out.println("INC error al construir la factura ");
												String temp = "ErrorFactura|" + (factura+1) + "|" + nameFileExcel+ "|"
														+ listIn.get(index).getFe_taxid() + "|" + listIn.get(index).getRfc() + "|" + "\r\n";
													//salidaINC.write(temp.getBytes("UTF-8"));
													//salidaINC.write((listIn.get(index).getSbError().toString() + "\r\n").getBytes("UTF-8"));
													sbStatusNoOK.append(temp + listIn.get(index).getSbError().toString() + "\r\n");
													
													counterNoOk++;
											}
										}
									}
								}
								
								factura++;
							}						
							
						}///Lectura del archivo TXT
						
						System.out.println("rows:" + rows);
						
						
						if(finArchivo){
							System.out.println("finArchivo OK");
						}
						
							
								
						if (salidaBD != null)
							salidaBD.close();	
						
						if (salidaBDDonat != null)
							salidaBDDonat.close();
											
						if (salidaODM != null)
							salidaODM.close();	
					
						fileStatus.write(("Archivo " + nameFileExcel + " procesado\n").getBytes("UTF-8"));
						userlog = new FileOutputStream(facturacionDonatSalida + arrayRenglon[1] + "/LOG" + arrayRenglon[1] + ".TXT",true);
						userlog.write(("Archivo " + nameFileExcel + " procesado\r\n").getBytes("UTF-8"));
						
						String strAbsolutePathXML = facturacionDonatSalida + arrayRenglon[1] + "/" + "XML" + nameFileExcel.substring(0, nameFileExcel.indexOf(".")) + ".TXT";
						String strAbsolutePathINC = facturacionDonatSalida + arrayRenglon[1] + "/" + "INC" + nameFileExcel.substring(0, nameFileExcel.indexOf(".")) + ".TXT";
						
						this.fileExitXML = new File(strAbsolutePathXML);
						this.fileExitINC = new File(strAbsolutePathINC);
						
						this.salidaXML = new FileOutputStream(this.fileExitXML);
						this.salidaINC = new FileOutputStream(this.fileExitINC);
						
						int counterProcesadas = counterOk + counterNoOk;
						
						this.salidaXML.write(("--Total de facturas correctas: " + counterOk + " --Total de facturas procesadas: " + counterProcesadas + "\r\n\r\n"+ sbStatusOK.toString()).getBytes("UTF-8"));
						this.salidaINC.write(("--Total de facturas con incidentes: " + counterNoOk + " --Total de facturas procesadas: " + counterProcesadas + "\r\n\r\n"+ sbStatusNoOK.toString()).getBytes("UTF-8"));
						
						if(salidaXML!=null)
							salidaXML.close();
						
						if(salidaINC!=null)
							salidaINC.close();
					}				
				}
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
		}catch(Exception ex){
			ex.printStackTrace();
			System.out.println("Exception massiveDonatProcess:" + ex.getMessage());
			
			try {
				//FileOutputStream fileError = new FileOutputStream(facturacionDonatProceso + "ERROR_PROCESS_" + nProceso + ".TXT");
				FileOutputStream fileError = new FileOutputStream(facturacionDonatProceso + "massiveDonatProcessError.txt");
				fileError.write((ex.getMessage()).getBytes("UTF-8"));
				fileError.close();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				//System.out.println("Exception al crear ERROR_PROCESS_" + nProceso + ".TXT:" + e1.getMessage());
				System.out.println("Exception al crear massiveDonatProcessError.txt:" + e1.getMessage());
			}		
		}
	}
	
	public String getDate(){		
		return Util.convertirFecha(Calendar.getInstance().getTime()).replaceAll("T", " ");		
	}
	public String getDate(Date date){
		return Util.convertirFecha(date).replaceAll("T", " ");
	}
	
	public String buildRowBDDonatFile(Invoice_Masivo invoiceM, String strUUID, String strSourceFileName, String strDate){
		StringBuffer temp = new StringBuffer();
		
		temp.append("c<rptDonatOra>");
	
		//Asignar los datos del reporte
               
        //CREATIONDATE
        temp.append(strDate + "<rptDonatOra>");
        
        //RFCEMISOR
        temp.append(invoiceM.getFe_taxid() + "<rptDonatOra>");
        
        //RFCDONANTE
        temp.append(invoiceM.getRfc() + "<rptDonatOra>");
        
        //FECHAEXPEDICION
        temp.append(strDate + "<rptDonatOra>");
        
        //FECHARECEPCION
        temp.append(getDate(invoiceM.getFechaRecepcion()) + "<rptDonatOra>");
        
        //NOMBREDONANTE
        
        if(invoiceM.getName().length() > 255){
        	temp.append(invoiceM.getName().substring(0, 255) + "<rptDonatOra>");	        	
        }else{
        	temp.append(invoiceM.getName() + "<rptDonatOra>");
        }
        
        //DOMICILIODONANTE
        
        if(invoiceM.getAddress().length() > 512){
        	temp.append(invoiceM.getAddress().substring(0, 512) + "<rptDonatOra>");	        	
        }else{
        	temp.append(invoiceM.getAddress() + "<rptDonatOra>");
        }
        
        //IMPORTE
        temp.append(invoiceM.getTotal() + "<rptDonatOra>");
        
        //METODOPAGO
        if(invoiceM.getMetodoPago().length() > 255){
        	temp.append(invoiceM.getMetodoPago().substring(0, 255) + "<rptDonatOra>");	        	
        }else{
        	temp.append(invoiceM.getMetodoPago() + "<rptDonatOra>");
        }
        
        //NUMCTAPAGO
        
        if(invoiceM.getNumCtaPago().length() > 255){
        	temp.append(invoiceM.getNumCtaPago().substring(0, 255) + "<rptDonatOra>");	        	
        }else{
        	temp.append(invoiceM.getNumCtaPago() + "<rptDonatOra>");
        }
        
        //FOLIO
        temp.append(strUUID + "<rptDonatOra>");
        
        //SOURCEFILENAME
        temp.append(strSourceFileName + "<rptDonatOra>");
        
        //AUTHOR        
        temp.append("masivo" + "\r\n");
		
		return temp.toString();
	}
	public String buildRowBDFile(Invoice_Masivo invoiceM, String strUUID, String strSourceFileName, String strDate, String idUsuario, String idArea){		
		StringBuffer temp = new StringBuffer();
				
		temp.append("c,");
		temp.append(strDate + ",");
		temp.append(strDate + ",");
		temp.append(strDate + ",");
		temp.append(invoiceM.getXmlRoute() + ",");
		temp.append(strSourceFileName + ",");
		temp.append(Integer.parseInt(this.statusActive) + ",");
		temp.append(3 + ",");
		temp.append(invoiceM.getRfc() + ",");
		temp.append(invoiceM.getSubTotal()*invoiceM.getExchange() + ",");
		temp.append(invoiceM.getIva()*invoiceM.getExchange() + ",");
		temp.append(invoiceM.getTotal()*invoiceM.getExchange() + ",");
		temp.append(invoiceM.getContractNumber() + ",");
		temp.append(invoiceM.getCostCenter() + ",");
		temp.append(invoiceM.getCustomerCode() + ",");
		temp.append(invoiceM.getPeriod() + ",");
		temp.append(invoiceM.getTipoFormato().toUpperCase().substring(0, 1) + ",");
		temp.append(invoiceM.getFe_Id() + ",");				
		temp.append(1 + ",");
		temp.append(invoiceM.getFolio() + ",");
		temp.append(strUUID + ",");
		temp.append("masivo" + ",");		
		temp.append(idUsuario + ",");
		temp.append(idArea + ",");
		temp.append("masivo" + "\r\n");
				
		return temp.toString();
	}
		
	private boolean validaDatoRELongitud(String dato, String expReg, int longitud) {
        return dato != null && dato.trim().length() > 0 && dato.length() <= longitud && dato.matches(expReg);
    }
	
	
	private boolean validaDatoRE(String dato, String expReg) {
        return dato != null && dato.trim().length() > 0 && dato.matches(expReg);
    }

	
	public StringBuilder processRowExcel(String [] strValues, int factura, 
			HashMap<Integer, String> hashIvas, HashMap<String, String> hashcodigoISO, 
			HashMap<String, String> hashmoneda, HashMap<String,FiscalEntity> hashEmisores, 
			HashMap<String,Customer> hashClientes, HashMap<Long,CFDFieldsV22> hashCfdFieldsV22) {
				
    		StringBuilder sbError = null;
    		StringBuilder sbErrorFile = null;
    		boolean fError;
			try{
				for(int index=0; index<strValues.length; index++){
					System.out.println("index:" + index + " value:" + strValues[index] + "--");
				}
				
				
					fError = false;
					sbError = new StringBuilder();							
					sb = new StringBuilder();
					int facturasOK = 0;
					int facturasError = 0;
					
					System.out.println("Procesa los primeros 29 campos");
					
					CFDFieldsV22 cfdFieldsV22 = null;
					
					//fiscalEntity = new FiscalEntity();
					Customer customer = null;
					
					fiscalEntity = null;
					//Cliente customer = null;
					
					invoice = new Invoice_Masivo();
										
					vectorCantidad = new Vector<String>();
					vectorUM = new Vector<String>();
					vectorDesc = new Vector<String>();
					vectorPrecioUnitario = new Vector<Double>();
					vectorAplicaIVA = new Vector<String>();
					vectorTotal = new Vector<Double>();												
					
					if(strValues[0] == null){										
						sbError.append("(1) Entidad Fiscal requerida - Factura " + factura + "\n");
						System.out.println("(1) Entidad Fiscal requerida - Factura " + factura + "\n");
						fError = true;
					}else{
						
						//System.out.println("Emisor: " + strValues[0].toString());
						
						if(hashEmisores.containsKey(strValues[0].toString().toUpperCase().trim())){
							fiscalEntity = hashEmisores.get(strValues[0].toString().toUpperCase().trim()); 
						}
						
						if(fiscalEntity == null) {
						
							sbError.append("(1) La Entidad Fiscal no existe en BD - Factura " + factura + "\n");
							System.out.println("(1) La Entidad Fiscal no existe en BD - Factura " + factura + "\n");
							fError = true;
												
						}else{
							if(fiscalEntity.getIsDonataria() != 1) {
								
								sbError.append("(1) La Entidad Fiscal no es donataria - Factura " + factura + "\n");
								System.out.println("(1) La Entidad Fiscal no es donataria - Factura " + factura + "\n");
								fError = true;
							
							}else{
								invoice.setFe_Id(String.valueOf(fiscalEntity.getId()));
								invoice.setFe_taxid(String.valueOf(fiscalEntity.getTaxID()));
							}
						}	
																										
					}					
					
					if(strValues[1] == null || strValues[1].toString().trim().length() == 0){
						invoice.setSerie("");		                            
					}else{				
						
                        if(validaDatoRELongitud(strValues[1].toString().trim(), RE_CHAR_ONLY, 25)){
                        	//System.out.println("serie: " + strValues[1].toString());
                            invoice.setSerie(strValues[1].toString().trim());
                        }else{                    
                            sbError.append("(2) Serie con formato incorrecto - Factura " + factura + "\n");
                            System.out.println("(2) Serie con formato incorrecto - Factura " + factura + "\n");
                            fError = true;                    
                        }   
						
					}								
					
					if(strValues[2] == null){
						invoice.setTipoFormato("ingreso");
					}else{									
						
						if(strValues[2].toString().trim().equals("")) {
							invoice.setTipoFormato("ingreso");											
							//System.out.println("tipoComprobante: " + invoice.getTipoFormato());
						}else if(strValues[2].toString().trim().toUpperCase().equals("INGRESO") || strValues[2].toString().trim().toUpperCase().equals("EGRESO") ){
							invoice.setTipoFormato(strValues[2].toString().trim().toLowerCase());											
							//System.out.println("tipoComprobante: " + invoice.getTipoFormato());
	                        
	                    
	                    }else{
	                    	sbError.append("(3) Tipo de Comprobante incorrecto - Factura " + factura + "\n");
	                        System.out.println("(3) Tipo de Comprobante incorrecto - Factura " + factura + "\n");
	                        fError = true;
	                    }
						
					}
					
					if(strValues[3] == null){									
                        sbError.append("(4) Posicion Moneda requerida (Null) - Factura " + factura + "\n");
                        System.out.println("(4) Posicion Moneda requerida (Null) - Factura " + factura + "\n");
                        fError = true;
					}else{
						
						if(!strValues[3].toString().trim().equals("")){			
							
	                        //Moneda moneda = monedaManager.findByName(strValues[3].toString().trim());
												
	                        if(!hashmoneda.containsKey(strValues[3].toString().trim())){
	                        	sbError.append("(4) Moneda no existe en BD - Factura " + factura + "\n");
		                        System.out.println("(4) Moneda no existe en BD - Factura " + factura + "\n");
		                        fError = true;
	                        }else{
	                        	//System.out.println("moneda: " + mon);
								invoice.setMoneda(strValues[3].toString().trim());
		                        invoice.setTipoMoneda(strValues[3].toString().trim());
	                        }										
	                    }else{
	                    
	                        sbError.append("(4) Moneda requerida - Factura " + factura + "\n");
	                        System.out.println("(4) Moneda requerida - Factura " + factura + "\n");
	                        fError = true;
	                    
	                    }
					
					}
					
					if(strValues[4] == null || strValues[4].toString().trim().length() == 0){
						
                        sbError.append("(5) Tipo de Cambio requerida - Factura " + factura + "\n");
                        System.out.println("(5) Tipo de Cambio es requerida - Factura " + factura + "\n");
                        fError = true;
					}else{						
													
						if(validaDatoRE(strValues[4].toString().trim(),RE_DECIMAL)) {
							//System.out.println("tipo de cambio: " + strValues[4].toString());
							invoice.setTipoCambio(strValues[4].toString().trim());
							invoice.setExchange(Double.valueOf(strValues[4].toString().trim()));
						} else {
					
	                        sbError.append("(5) Tipo de Cambio tiene formato incorrecto - Factura " + factura + "\n");
	                        System.out.println("(5) Tipo de Cambio tiene formato incorrecto - Factura " + factura + "\n");
	                        fError = true;
	                
						}					
	                    					
					}
					
					if(strValues[5] == null || strValues[5].toString().trim().equals("")){
						
                        sbError.append("(6) RFC del Cliente requerida - Factura " + factura + "\n");
                        System.out.println("(6) RFC del Cliente requerida - Factura " + factura + "\n");
                        fError = true;
					}else{
						
						invoice.setRfc(strValues[5].toString().trim());
						//System.out.println("RFC Cliente: " + strValues[5].toString());
						if(invoice.getRfc().toUpperCase().equals("XEXX010101000")){
							if(strValues[6] == null || strValues[6].toString().trim().length() == 0){
					
                                sbError.append("(7) Id extranjero requerido - Factura " + factura + "\n");
                                System.out.println("(7) Id extranjero requerido - Factura " + factura + "\n");
                                fError = true;
							}else{
								if(validaDatoRE(strValues[6].toString().trim(), RE_CHAR_NUMBER)){					                            		
                            		
                            		
                            		invoice.setIdExtranjero(strValues[6].toString().trim());
                            		
                            		/*String strIDExtranjero = strValues[6].toString();
                            		if(hashClientes.containsKey(strIDExtranjero)){
                            			customer = hashClientes.get(strIDExtranjero);                            			
		                            	invoice.setIdExtranjero(strIDExtranjero);	                            		
                            		}else{
                            			sbError.append("(7) Id extranjero no existe en BD - Factura " + factura + "\n");
		                                System.out.println("(7) Id extranjero no existe en BD - Factura " + factura + "\n");
		                                fError = true;
                            		}*/		                            		
	                            	
                            	}else{
                            		sbError.append("(7) Id extranjero con formato incorrecto - Factura " + factura + "\n");
	                                System.out.println("(7) Id extranjero con formato incorrecto - Factura " + factura + "\n");
	                                fError = true;
                            	}
							}					                            
                        }else{
                        	/*if(fiscalEntity != null){
                        		//System.out.println("CustomerRFC: " + invoice.getRfc() + " feId: " + String.valueOf(fiscalEntity.getId()));
                        		if(hashClientes.containsKey(invoice.getRfc() + String.valueOf(fiscalEntity.getId()))){
                        			customer = hashClientes.get(invoice.getRfc() + String.valueOf(fiscalEntity.getId()));
                        		}					                        		
                        	}*/
                        	invoice.setIdExtranjero("");
                        }
						
					}
					
					if(validaDatoRELongitud(strValues[7].toString(), RE_CHAR, 250)){
						invoice.setName(strValues[7].toString().trim());
					}else{
						sbError.append("(8) Nombre con formato incorrecto - Factura " + factura + "\n");
                        System.out.println("(8) Nombre con formato incorrecto - Factura " + factura + "\n");
                        fError = true;
					}
					
					if(strValues[8] == null || strValues[8].trim().equals("")){
						if(hashCfdFieldsV22.containsKey(fiscalEntity.getId())){
							CFDFieldsV22 cfdFieldV22 = hashCfdFieldsV22.get(fiscalEntity.getId());							
							if(cfdFieldV22 != null){
								invoice.setMetodoPago(cfdFieldV22.getMetodoDePago());
							}else{
								sbError.append("(9) Metodo de pago no existe en BD - Factura " + factura + "\n");
		            			System.out.println("(9) Metodo de pago no existe en BD - Factura " + factura + "\n");
		            			fError = true;
							}				            			
	            		}else{	            				            			
	            			sbError.append("(9) Metodo de pago no existe en BD - Factura " + factura + "\n");
	            			System.out.println("(9) Metodo de pago no existe en BD - Factura " + factura + "\n");
	            			fError = true;	                        
	            		}
					}else{						
						if(validaDatoRELongitud(strValues[8], RE_CHAR, 250)){
							invoice.setMetodoPago(strValues[8].trim());
						}else{
							sbError.append("(9) Metodo de pago con formato incorrecto - Factura " + factura + "\n");
	            			System.out.println("(9) Metodo de pago con formato incorrecto - Factura " + factura + "\n");
	            			fError = true;
						}						
					}
					
					if(strValues[9] == null || strValues[9].trim().equals("")){
						if(hashCfdFieldsV22.containsKey(fiscalEntity.getId())){
							CFDFieldsV22 cfdFieldV22 = hashCfdFieldsV22.get(fiscalEntity.getId());							
							if(cfdFieldV22 != null){
								invoice.setRegimenFiscal(cfdFieldV22.getRegimenFiscal().getName());
							}else{
								sbError.append("(10) Regimen Fiscal no existe en BD - Factura " + factura + "\n");
		            			System.out.println("(10) Regimen Fiscal no existe en BD - Factura " + factura + "\n");
		            			fError = true;
							}				            			
	            		}else{	            				            			
	            			sbError.append("(10) Regimen Fiscal no existe en BD - Factura " + factura + "\n");
	            			System.out.println("(10) Regimen Fiscal no existe en BD - Factura " + factura + "\n");
	            			fError = true;	                        
	            		}
					}else{						
						if(validaDatoRELongitud(strValues[9], RE_CHAR, 250)){
							invoice.setRegimenFiscal(strValues[9].trim());
						}else{
							sbError.append("(10) Regimen Fiscal con formato incorrecto - Factura " + factura + "\n");
	            			System.out.println("(10) Regimen Fiscal con formato incorrecto - Factura " + factura + "\n");
	            			fError = true;
						}						
					}
					
					if(strValues[10] == null || strValues[10].trim().equals("")){
						if(hashCfdFieldsV22.containsKey(fiscalEntity.getId())){
							CFDFieldsV22 cfdFieldV22 = hashCfdFieldsV22.get(fiscalEntity.getId());							
							if(cfdFieldV22 != null){
								invoice.setLugarExpedicion(cfdFieldV22.getLugarDeExpedicion());
							}else{
								sbError.append("(11) Lugar de Expedicion no existe en BD - Factura " + factura + "\n");
		            			System.out.println("(11) Lugar de Expedicion no existe en BD - Factura " + factura + "\n");
		            			fError = true;
							}				            			
	            		}else{	            				            			
	            			sbError.append("(11) Lugar de Expedicion no existe en BD - Factura " + factura + "\n");
	            			System.out.println("(11) Lugar de Expedicion no existe en BD - Factura " + factura + "\n");
	            			fError = true;	                        
	            		}
					}else{						
						if(validaDatoRELongitud(strValues[10], RE_CHAR, 250)){
							invoice.setLugarExpedicion(strValues[10].trim());
						}else{
							sbError.append("(11) Lugar de Expedicion con formato incorrecto - Factura " + factura + "\n");
	            			System.out.println("(11) Lugar de Expedicion con formato incorrecto - Factura " + factura + "\n");
	            			fError = true;
						}						
					}
					
					if(strValues[11] == null || strValues[11].toString().trim().equals("")){									
						invoice.setFormaPago("PAGO EN UNA SOLA EXHIBICION");
                        //System.out.println("Forma de pago: PAGO EN UNA SOLA EXHIBICION");
					}else{								
						if(validaDatoRELongitud(strValues[11], RE_CHAR, 250)){
                        	//System.out.println("Forma de pago: " + strValues[8].toString());
                        	invoice.setFormaPago(strValues[11].toString().trim());
                        }else{			                        	                    
                            sbError.append("(12) Forma de Pago con formato incorrecto - Factura " + factura + "\n");
                            System.out.println("(12) Forma de Pago con formato incorrecto - Factura " + factura + "\n");
                            fError = true;                    
                        }
					}
					
					if(strValues[12] == null || strValues[12].trim().equals("")){
						if(hashCfdFieldsV22.containsKey(fiscalEntity.getId())){
							CFDFieldsV22 cfdFieldV22 = hashCfdFieldsV22.get(fiscalEntity.getId());							
							if(cfdFieldV22 != null){
								invoice.setNumCtaPago(cfdFieldV22.getFormaDePago());
							}else{
								sbError.append("(13) Numero de cuenta de pago no existe en BD - Factura " + factura + "\n");
		            			System.out.println("(13) Numero de cuenta de pago no existe en BD - Factura " + factura + "\n");
		            			fError = true;
							}				            			
	            		}else{	            				            			
	            			sbError.append("(13) Numero de cuenta de pago no existe en BD - Factura " + factura + "\n");
	            			System.out.println("(13) Numero de cuenta de pago no existe en BD - Factura " + factura + "\n");
	            			fError = true;	                        
	            		}
					}else{						
						if(validaDatoRELongitud(strValues[12], RE_CHAR, 250)){
							invoice.setNumCtaPago(strValues[12].trim());
						}else{
							sbError.append("(13) Numero de cuenta de pago con formato incorrecto - Factura " + factura + "\n");
	            			System.out.println("(13) Numero de cuenta de pago con formato incorrecto - Factura " + factura + "\n");
	            			fError = true;
						}						
					}
					
					if(strValues[13] == null || strValues[13].toString().trim().equals("")){			                    				
            			sbError.append("(14) Fecha de Recepción requerida - Factura " + factura + "\n");
            			System.out.println("(14) Fecha de Recepción requerida - Factura " + factura + "\n");
            			fError = true;
						
        			}else{
        				
        				SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                        try {
                            Date date = format.parse(strValues[13].trim());
                            invoice.setFechaRecepcion(date);
                        } catch (ParseException e) {
                        	sbError.append("(14) Fecha de Recepción con formato incorrecto - Factura " + factura + "\n");
                			System.out.println("(14) Fecha de Recepción con formato incorrecto - Factura " + factura + "\n");
                			fError = true;
                        }  
        					
        			}
					
					if(strValues[14] == null || strValues[14].toString().trim().length() == 0){
						invoice.setNumeroEmpleado("");		                            
					}else{				
						
                        if(validaDatoRELongitud(strValues[14].toString().trim(), RE_CHAR_NUMBER, 50)){
                        	//System.out.println("serie: " + strValues[1].toString());
                            invoice.setNumeroEmpleado(strValues[14].toString().trim());
                        }else{                    
                            sbError.append("(15) Numero de empleado con formato incorrecto - Factura " + factura + "\n");
                            System.out.println("(15) Numero de empleado con formato incorrecto - Factura " + factura + "\n");
                            fError = true;                    
                        }   
						
					}	
					
					if(strValues[15] == null || strValues[15].toString().trim().length() == 0){
						invoice.setCalle("");		                            
					}else{				
						
                        if(validaDatoRELongitud(strValues[15].toString().trim(), RE_CHAR, 250)){
                        	//System.out.println("serie: " + strValues[1].toString());
                            invoice.setCalle(strValues[15].toString().trim());
                        }else{                    
                            sbError.append("(16) Calle con formato incorrecto - Factura " + factura + "\n");
                            System.out.println("(16) Calle con formato incorrecto - Factura " + factura + "\n");
                            fError = true;                    
                        }   
						
					}
					
					if(strValues[16] == null || strValues[16].toString().trim().length() == 0){
						invoice.setInterior("");		                            
					}else{				
						
                        if(validaDatoRELongitud(strValues[16].toString().trim(), RE_CHAR, 50)){
                        	//System.out.println("serie: " + strValues[1].toString());
                            invoice.setInterior(strValues[16].toString().trim());
                        }else{                    
                            sbError.append("(17) Num Interior con formato incorrecto - Factura " + factura + "\n");
                            System.out.println("(17) Num Interior con formato incorrecto - Factura " + factura + "\n");
                            fError = true;                    
                        }   
						
					}
					
					if(strValues[17] == null || strValues[17].toString().trim().length() == 0){
						invoice.setExterior("");		                            
					}else{				
						
                        if(validaDatoRELongitud(strValues[17].toString().trim(), RE_CHAR, 50)){
                        	//System.out.println("serie: " + strValues[1].toString());
                            invoice.setExterior(strValues[17].toString().trim());
                        }else{                    
                            sbError.append("(18) Num Exterior con formato incorrecto - Factura " + factura + "\n");
                            System.out.println("(18) Num Exterior con formato incorrecto - Factura " + factura + "\n");
                            fError = true;                    
                        }   
						
					}
					
					if(strValues[18] == null || strValues[18].toString().trim().length() == 0){
						invoice.setColonia("");		                            
					}else{				
						
                        if(validaDatoRELongitud(strValues[18].toString().trim(), RE_CHAR, 100)){
                        	//System.out.println("serie: " + strValues[1].toString());
                            invoice.setColonia(strValues[18].toString().trim());
                        }else{                    
                            sbError.append("(19) Colonia con formato incorrecto - Factura " + factura + "\n");
                            System.out.println("(19) Colonia con formato incorrecto - Factura " + factura + "\n");
                            fError = true;                    
                        }   
						
					}
					
					if(strValues[19] == null || strValues[19].toString().trim().length() == 0){
						invoice.setLocalidad("");		                            
					}else{				
						
                        if(validaDatoRELongitud(strValues[19].toString().trim(), RE_CHAR, 100)){
                        	//System.out.println("serie: " + strValues[1].toString());
                            invoice.setLocalidad(strValues[19].toString().trim());
                        }else{                    
                            sbError.append("(20) Localidad con formato incorrecto - Factura " + factura + "\n");
                            System.out.println("(20) Localidad con formato incorrecto - Factura " + factura + "\n");
                            fError = true;                    
                        }   
						
					}
					
					if(strValues[20] == null || strValues[20].toString().trim().length() == 0){
						invoice.setReferencia("");		                            
					}else{				
						
                        if(validaDatoRELongitud(strValues[20].toString().trim(), RE_CHAR, 250)){
                        	//System.out.println("serie: " + strValues[1].toString());
                            invoice.setReferencia(strValues[20].toString().trim());
                        }else{                    
                            sbError.append("(21) Referencia con formato incorrecto - Factura " + factura + "\n");
                            System.out.println("(21) Referencia con formato incorrecto - Factura " + factura + "\n");
                            fError = true;                    
                        }   
						
					}
					
					if(strValues[21] == null || strValues[21].toString().trim().length() == 0){
						invoice.setMunicipio("");		                            
					}else{				
						
                        if(validaDatoRELongitud(strValues[21].toString().trim(), RE_CHAR, 100)){
                        	//System.out.println("serie: " + strValues[1].toString());
                            invoice.setMunicipio(strValues[21].toString().trim());
                        }else{                    
                            sbError.append("(22) Municipio con formato incorrecto - Factura " + factura + "\n");
                            System.out.println("(22) Municipio con formato incorrecto - Factura " + factura + "\n");
                            fError = true;                    
                        }   
						
					}
					
					if(strValues[22] == null || strValues[22].toString().trim().length() == 0){
						invoice.setEstado("");		                            
					}else{				
						
                        if(validaDatoRELongitud(strValues[22].toString().trim(), RE_CHAR, 100)){
                        	//System.out.println("serie: " + strValues[1].toString());
                            invoice.setEstado(strValues[22].toString().trim());
                        }else{                    
                            sbError.append("(23) Estado con formato incorrecto - Factura " + factura + "\n");
                            System.out.println("(23) Estado con formato incorrecto - Factura " + factura + "\n");
                            fError = true;                    
                        }   
						
					}
					
					if(strValues[23] == null || strValues[23].toString().trim().length() == 0){
						sbError.append("(24) Pais requerido - Factura " + factura + "\n");
                        System.out.println("(24) Pais requerido - Factura " + factura + "\n");
                        fError = true; 		                            
					}else{				
						
                        if(validaDatoRELongitud(strValues[23].toString().trim(), RE_CHAR, 100)){
                        	//System.out.println("serie: " + strValues[1].toString());
                            invoice.setPais(strValues[23].toString().trim());
                        }else{                    
                            sbError.append("(24) Pais con formato incorrecto - Factura " + factura + "\n");
                            System.out.println("(24) Pais con formato incorrecto - Factura " + factura + "\n");
                            fError = true;                    
                        }   
						
					}
					
					if(strValues[24] == null || strValues[24].toString().trim().length() == 0){
						invoice.setCodigoPostal("");		                            
					}else{				
						
                        if(validaDatoRELongitud(strValues[24].toString().trim(), RE_NUMBER, 5)){
                        	//System.out.println("serie: " + strValues[1].toString());
                            invoice.setCodigoPostal(strValues[24].toString().trim());
                        }else{                    
                            sbError.append("(25) Codigo Postal con formato incorrecto - Factura " + factura + "\n");
                            System.out.println("(25) Codigo Postal con formato incorrecto - Factura " + factura + "\n");
                            fError = true;                    
                        }   
						
					}
					
					if(strValues[25] == null || strValues[25].toString().trim().length() == 0){
						invoice.setCustomerCode("");		                            
					}else{				
						
                        if(validaDatoRELongitud(strValues[25].toString().trim(), RE_CHAR, 250)){
                        	//System.out.println("serie: " + strValues[1].toString());
                            invoice.setCustomerCode(strValues[25].toString().trim());
                        }else{                    
                            sbError.append("(26) Codigo de Cliente con formato incorrecto - Factura " + factura + "\n");
                            System.out.println("(26) Codigo de Cliente con formato incorrecto - Factura " + factura + "\n");
                            fError = true;                    
                        }   
						
					}
					
					if(strValues[26] == null || strValues[26].toString().trim().length() == 0){
						invoice.setContractNumber("");		                            
					}else{				
						
                        if(validaDatoRELongitud(strValues[26].toString().trim(), RE_CHAR, 250)){
                        	//System.out.println("serie: " + strValues[1].toString());
                            invoice.setContractNumber(strValues[26].toString().trim());
                        }else{                    
                            sbError.append("(27) Contrato con formato incorrecto - Factura " + factura + "\n");
                            System.out.println("(27) Contrato con formato incorrecto - Factura " + factura + "\n");
                            fError = true;                    
                        }   
						
					}

					if(strValues[27] == null || strValues[27].toString().trim().length() == 0){
						invoice.setPeriod("");		                            
					}else{				
						
                        if(validaDatoRELongitud(strValues[27].toString().trim(), RE_CHAR, 250)){
                        	//System.out.println("serie: " + strValues[1].toString());
                            invoice.setPeriod(strValues[27].toString().trim());
                        }else{                    
                            sbError.append("(28) Periodo con formato incorrecto - Factura " + factura + "\n");
                            System.out.println("(28) Periodo con formato incorrecto - Factura " + factura + "\n");
                            fError = true;                    
                        }   
						
					}
					
					if(strValues[28] == null || strValues[28].toString().trim().length() == 0){
						invoice.setCostCenter("");		                            
					}else{				
						
                        if(validaDatoRELongitud(strValues[28].toString().trim(), RE_CHAR, 250)){
                        	//System.out.println("serie: " + strValues[1].toString());
                            invoice.setCostCenter(strValues[28].toString().trim());
                        }else{                    
                            sbError.append("(29) Centro de Costos con formato incorrecto - Factura " + factura + "\n");
                            System.out.println("(29) Centro de Costos con formato incorrecto - Factura " + factura + "\n");
                            fError = true;                    
                        }   
						
					}
									
					
                	
					//Procesar Conceptos
					//Contar posiciones de concepto
					int posicionConcepto = 0;
					int posicion = 29;
					
					int contadorConceptos = 1;
					boolean fPermisoVector = true;
					
					String strItemConcepto = "";
					for(int index=29; index<strValues.length-1; index++ ){
						
						if(strValues[index] == null){
							//System.out.println("bloqueConceptos: nulo posicion: " + posicion);
							strItemConcepto = "";
						}else{
							//System.out.println("bloqueConceptos: " + strValues[index] + " posicion: " + posicion);
							strItemConcepto = strValues[index].toString().trim();
						}
						
						
						if(posicionConcepto == 4){
							posicionConcepto = 1;
						}else{
							posicionConcepto += 1;
						}
						
						if(posicionConcepto == 1){
							
							if(strItemConcepto.equals("")){
								fPermisoVector = false;
								sbError.append("(" + (posicion+1) +") " + "Cantidad requerida " + " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
								System.out.println("(" + (posicion+1) +") " + "Cantidad requerida " + " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
							}else{
									//System.out.println("cantidad:" + strItemConcepto);
									if(validaDatoRE(strItemConcepto, RE_DECIMAL)) {
										//System.out.println("Cantidad: " + strItemConcepto + " de la factura " + factura);
										if(fPermisoVector)
											vectorCantidad.add(strItemConcepto);
					                 }else {
					                	fPermisoVector = false;
										sbError.append("(" + (posicion+1) +") " + "Cantidad con formato incorrecto " + " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
										System.out.println("(" + (posicion+1) +") " + "Cantidad con formato incorrecto " + " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
					                 }
								
							}
																		
						}else if(posicionConcepto == 2){
							
							if(strItemConcepto.equals("")){
								fPermisoVector = false;
								sbError.append("(" + (posicion+1) +") " + "UM requerida " + " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
								System.out.println("(" + (posicion+1) +") " + "UM requerida " + " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
							}else{
								if(validaDatoRELongitud(strItemConcepto, RE_CHAR, 250)){
									//System.out.println("UM: " + strItemConcepto + " de la factura " + factura);
									if(fPermisoVector)
										vectorUM.add(strItemConcepto);
				                 }else{
				                	 fPermisoVector = false;
				                	 sbError.append("(" + (posicion+1) +") " + "UM con formato incorrecto " + " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
									 System.out.println("(" + (posicion+1) +") " + "UM con formato incorrecto " + " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
				                 }
							}
							
							
						}else if(posicionConcepto == 3){
							
							if(strItemConcepto.equals("")){
								vectorDesc.add(strItemConcepto);
							}else{
								if(validaDatoRELongitud(strItemConcepto, RE_CHAR, 250)){
								//if(strItemConcepto != null && strItemConcepto.trim().length() > 0 && strItemConcepto.length() <= 1500){
									//System.out.println("Concepto Expedicion: " + strItemConcepto + " de la factura " + factura);
									if(fPermisoVector)
										vectorDesc.add(strItemConcepto.toUpperCase());
				                 }else{
				                	 fPermisoVector = false;
									 sbError.append("(" + (posicion+1) +") " + "Concepto Expedicion con formato incorrecto " + " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
									 System.out.println("(" + (posicion+1) +") " + "Concepto Expedicion con formato incorrecto " + " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
				                 }
							}
							
							
						}else{
							
							if(strItemConcepto.equals("")){
								fPermisoVector = false;
								sbError.append("(" + (posicion+1) +") " + "Precio Unitario requerido " + " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
								System.out.println("(" + (posicion+1) +") " + "Precio Unitario requerido " + " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
							}else{
									//System.out.println("precioUnitario:" + strItemConcepto);
									if(validaDatoRE(strItemConcepto, RE_DECIMAL)){											
										//System.out.println("Precio Unitario: " + strItemConcepto + " de la factura " + factura);
										if(fPermisoVector)
											vectorPrecioUnitario.add(Double.parseDouble(strItemConcepto));
					                 }else{
					                	 fPermisoVector = false;
					                	 sbError.append("(" + (posicion+1) +") " + "Precio Unitario con formato incorrecto " + " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
											System.out.println("(" + (posicion+1) +") " + "Precio Unitario con formato incorrecto " + " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
					                 }
								
							}
							contadorConceptos+=1;
							
						}
						
						posicion +=1;
					}
					
					System.out.println("contadorConceptos: " + contadorConceptos);
					System.out.println("posicionConcepto: " + posicionConcepto);
					
					if(posicionConcepto != 4){
						sbError.append("El Concepto " + contadorConceptos + ", está incompleto" + " en la factura " + factura + "\n");
						System.out.println("El Concepto " + contadorConceptos + ", está incompleto" + " en la factura " + factura + "\n");
						fError = true;
					}else{
						if(fPermisoVector){
							
							List<ElementsInvoice> elementosIn = new ArrayList<ElementsInvoice>();
						    
					        if(vectorCantidad != null && vectorUM != null && vectorDesc != null && vectorPrecioUnitario != null && vectorAplicaIVA != null && 
					        		vectorCantidad.size() == vectorUM.size() && vectorUM.size() == vectorDesc.size() &&
					                vectorDesc.size() == vectorPrecioUnitario.size()) {
					        	for(int v=0; v<vectorCantidad.size(); v++) {
					        		ElementsInvoice ei = new ElementsInvoice();
					        		ei.setQuantity(Double.valueOf(vectorCantidad.get(v)));
					        		ei.setUnitMeasure(vectorUM.get(v));
					        		ei.setDescription(vectorDesc.get(v));
					        		ei.setUnitPrice(vectorPrecioUnitario.get(v));
					        		
					        		ei.setAmount(ei.getQuantity() * ei.getUnitPrice());
					        										        		
					        							        		
					        		elementosIn.add(ei);
					        	}
												        		
					        	Double subtotal = 0.0;
					        	Double iva = 0.0;
					        	
					        	for(int iSub=0; iSub<elementosIn.size(); iSub++){
					        		subtotal += elementosIn.get(iSub).getAmount();					        		
					        	}
					        	
					        	Double Total = 0.0;
					        	Total = subtotal + iva;
					        	invoice.setSubTotal(subtotal);
					        	invoice.setIva(iva);
					        	invoice.setTotal(Total);
					        	invoice.setElements(elementosIn);
				
					        }
						}else{
							fError = true;
							System.out.println("fPermisoVector false (Error en campos de conceptos)");
						}
					}
										
					
					if(fError){
						facturasError+=1;								
						System.out.println("sbError: " + sbError.toString());
						sb.append("Factura: " + factura + " -- Lista de Errores: " + "\n" + sbError.toString() + "\n");								
						System.out.println("fError EmisionMasivaFacturas: " + sb.toString());							
						invoice.setSbError(sbError);
						listIn.add(invoice);
					}else{								
						
						try{
							
							crearFactura(factura);
							facturasOK+=1;									
							System.out.println("facturaOK - " + factura);
							listIn.add(invoice);
						} catch (Exception e) {
							facturasError+=1;									
							sb.append("Factura: " + factura + " --Exception: " + "\n" + e.getMessage() + "\n");
							System.out.println("Factura: " + factura + " --Exception: " + "\n" + e.getMessage());
							//invoice.setSbError(sbError);
							invoice.setSbError(new StringBuilder("Factura: " + factura + " --Exception: " + e.getMessage() + "\n"));
							listIn.add(invoice);
						}
						
						
					}
				//}////Fin del procesa los primeros 30
				
				
				
	        } catch (Exception e) {	        							
								
			    System.out.println("Factura: " + factura + " --Exception Global: " + e.getMessage());
			    e.printStackTrace();
			    sbErrorFile = new StringBuilder();
			    sbErrorFile.append("Factura: " + factura + " --Exception Global: " + e.getMessage());
			    return sbErrorFile;
			    
			} 
			return sbErrorFile;
        
    }
	
	public Date parseToDate(String strDate) throws Exception{
		SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
    	return formato.parse(strDate);
    	
	}
	
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
		 	FiscalEntity fe = fiscalEntityManager.get(fiscalEntity.getId());
		 
		 	System.out.println("antes valida XML");
		 	String nameFile = "";
			//nameFile = generaXML.generaXMLHandler(invoice, fiscalEntity, fecha);
		 	nameFile = xmlProcessGeneral.generateFileName(fecha, false, 0, false);
		 	ByteArrayOutputStream out = invoice.getByteArrXMLSinAddenda();
		 	generaXML.setOut(out);
		 	
		 	//validar xml
		 	if(generaXML.getOut().size()==0){
				System.out.println("El archivo esta vacio ++++");
			} else {
				System.out.println("El archivo pesa ++++ "+generaXML.getOut().size());
				//xmlProcess.valida22(out);
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
			
			cFDIssued.setFormatType(3);
			
			cFDIssued.setTaxIdReceiver(invoice.getRfc());
			cFDIssued.setSubTotal(invoice.getSubTotal()	* invoice.getExchange());
			cFDIssued.setIva(invoice.getIva() * invoice.getExchange());
			cFDIssued.setTotal(invoice.getTotal() * invoice.getExchange());
			cFDIssued.setContractNumber(invoice.getContractNumber());
			cFDIssued.setCostCenter(invoice.getCostCenter());
			cFDIssued.setCustomerCode(invoice.getCustomerCode());
			cFDIssued.setPeriod(invoice.getPeriod());
			cFDIssued.setCfdType(invoice.getTipoFormato().toUpperCase().substring(0, 1));
			cFDIssued.setFiscalEntity(fe);
			
			
			System.out.println("****** Guardado en Base de Datos");
			
			System.out.println("****** Datos enviados a Ondemand");
			System.out.println("****** Nombre del archivo: "+nameFile);
			System.out.println("****** RFC Emisor: "+ fiscalEntity.getTaxID());
			System.out.println("****** Numero de Contrato: "+invoice.getContractNumber());
			System.out.println("****** Numero de Cliente: "+invoice.getCustomerCode());
			System.out.println("****** Periodo: "+ invoice.getPeriod());
			System.out.println("****** Fecha: "+date);
			String serie=null;
			
            cFDIssued.setCreationDate(new Date());
            cFDIssued.setAuthor("masivo");
            cFDIssued.setModifiedBy("masivo");


//			ByteArrayOutputStream xmlSinAddenda = generaXML.guarda(null, fecha);
//			
//			if(xmlSinAddenda!=null){
//				this.invoice.setByteArrXMLSinAddenda(xmlSinAddenda);	
//				System.out.println("XML devuelto por generaXML.guarda: " + this.invoice.getByteArrXMLSinAddenda());								
//			}else{
//				this.invoice.setByteArrXMLSinAddenda(null);	
//			}
			
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

            
            
			/*Codigo de timbrado
			generaXML.timbraFactura(nameFile, invoice);
			
            cFDIssued.setFolioSAT(generaXML.getUUID());
            cFDIssued.setIsCFDI(1);
            
            
            cFDIssued.setBit1(ipServerName.obtenerIPServer());
            cFDIssued.setBit2(com.ibm.websphere.runtime.ServerName.getDisplayName());
            
            System.out.println("****** Guardado en Disco el XML");
            
            System.out.println("****** FiscalEntityId " + fiscalEntity.getId());
           
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
            
            continuarIntentando=true;
            contIntentos=0;
            while(continuarIntentando==true) {
            	contIntentos++;
            	try {
            		cFDIssued.setFolioInterno(String.valueOf(open.getSequence_value()));
            		cFDIssued=cFDIssuedManager.update(cFDIssued);
            		continuarIntentando=false;
            	} catch (Exception e) {
            		if(contIntentos<=5) {
            			continuarIntentando=true;
            			Thread.sleep(1000);
            		} else {
            			continuarIntentando=false;
            			throw new Exception("Documento timbrado. Problema al actualizar comprobante en la base de datos");
            		}
            	}
            }
            	*/	 			
           //Fin codigo de timbrado 		
            	            		
		 /*} catch (webServiceResponseException ex) {
            
             sb.append("RENGLON: " + renglon + " -- " + ".... -- ERROR: " + ex.getMessage() + "\n");
			 numeroFacturasError++;
			 System.out.println("webServiceResponseException: " + ex.getMessage());
		} catch (Exception e) {
             
			 sb.append("RENGLON: " + renglon + " -- " + ".... -- ERROR: " + e.getMessage() + "\n");
			 numeroFacturasError++;
			 System.out.println("Exception Gral: " + e.getMessage());
		}*/
	}
	
	
	//////////////////////Validaciones de los 30 campos////////////////////////////
	
	
	
	//////////////////////////////////////////////////////////////////////////////
	
	
	
	

	

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

	//Validar fecha AAAA-MM-DD
	public boolean validaFecha(String strFechaReg){
		String fechaReg = "[0-9]{4}\\-[0-9]{2}\\-[0-9]{2}";
		
		System.out.println("**fechaReg" + strFechaReg);
    	
    	if(strFechaReg.matches(fechaReg)){
    		String [] arrayDate = strFechaReg.split("-");
    		
    		String strDay = arrayDate[2];
    		String strMonth = arrayDate[1];
    		String strYear = arrayDate[0];
    		
    		boolean bisiesto = false;
    		
    		if ((Integer.parseInt(strYear) % 4 == 0) && ((Integer.parseInt(strYear) % 100 != 0) || (Integer.parseInt(strYear) % 400 == 0)))
    			bisiesto = true;
    		   		
    		if(strMonth.equals("02") || strMonth.equals("04") || strMonth.equals("06") || strMonth.equals("09")
    				 || strMonth.equals("11")){
    			if(!(Integer.parseInt(strDay) > 0 && Integer.parseInt(strDay) <= 30)){
    				System.out.println("fecha fuera de rango (1-30): " + strFechaReg);
    				return false;
    			}
    		}else if(strMonth.equals("02")){
    			if(bisiesto){
    				if(!(Integer.parseInt(strDay) > 0 && Integer.parseInt(strDay) <= 29)){
    					System.out.println("fecha fuera de rango (1-29): " + strFechaReg);
    					return false;
	    			}
    			}else{
    				if(!(Integer.parseInt(strDay) > 0 && Integer.parseInt(strDay) <= 28)){
    					System.out.println("fecha fuera de rango (1-28): " + strFechaReg);
    					return false;
	    			}
    			}
    		}else if(strMonth.equals("01") || strMonth.equals("03") || strMonth.equals("05") || strMonth.equals("07")
   				 || strMonth.equals("08") || strMonth.equals("10") || strMonth.equals("12")){
    			if(!(Integer.parseInt(strDay) > 0 && Integer.parseInt(strDay) <= 31)){
    				System.out.println("fecha fuera de rango (1-31): " + strFechaReg);
    				return false;
    			}	    			
    		}else{
    			System.out.println("fecha fuera de rango (1-12): " + strFechaReg);
    			return false;
    		}
    		
    	}else{
    		System.out.println("fecha con formato incorrecto " + strFechaReg);
    		return false;
    	}
    	
    	System.out.println("**");
    	return true;
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
/*
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
*/
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

}
