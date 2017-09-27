package com.interfactura.firmalocal.xml.masivo;

import static com.interfactura.firmalocal.xml.util.Util.getTASA;
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

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.interfactura.firmalocal.controllers.MassiveReadController;
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
import com.interfactura.firmalocal.xml.Properties_FD;
import com.interfactura.firmalocal.xml.WebServiceClienteUnicoDivisas;
import com.interfactura.firmalocal.xml.file.GeneraArchivo_Masivo;
import com.interfactura.firmalocal.xml.file.XMLProcess;
import com.interfactura.firmalocal.xml.file.XMLProcess_Masivo;
import com.interfactura.firmalocal.xml.util.GeneraXmlFacturaCfdiV3_3;
import com.interfactura.firmalocal.xml.util.Util;
import com.interfactura.firmalocal.xml.util.UtilCFDIFormatoUnico;
import com.interfactura.firmalocal.xml.util.UtilCFDIValidations;
import com.interfactura.firmalocal.xml.util.UtilCatalogos;
import com.interfactura.firmalocal.xml.util.XMLProcessGeneral;

@Component
public class GeneraXMLProceso_Masivo {
	private Logger logger = Logger.getLogger(GeneraXMLProceso_Masivo.class);
	
	@Autowired
	private Properties properties;
	
	@Autowired
	private Properties_FD properties_fd;
	
	private long offSetComprobante = 0;
	@Autowired
	private XMLProcess xmlProcess;
	
	private WebServiceClienteUnicoDivisas servicePort = null;
	private DocumentBuilder db = null;
	private Transformer tx = null;
	private DocumentBuilderFactory dbf = null;
	
	
    
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
	private GeneraArchivo_Masivo generaXML;
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
	//Emisor fiscalEntity =null;
	Vector<String> vectorCantidad = null;
	Vector<String> vectorUM = null;
	Vector<String> vectorDesc = null;
	Vector<Double> vectorPrecioUnitario = null;
	Vector<String> vectorAplicaIVA = null;
	Vector<Double> vectorTotal = null;
    
	private static final String RE_DECIMAL = "[0-9]+(\\.[0-9][0-9]?[0-9]?[0-9]?)?";
	//private static final String RE_DECIMAL_NEGATIVO = "[\\-]?[0-9]+(\\.[0-9][0-9]?[0-9]?[0-9]?)?";
	private static final String RE_DECIMAL_QTY = "[0-9]{1,10}(\\.[0-9]{0,3})?";
	private static final String RE_DECIMAL_NEGATIVO = "[\\-]?[0-9]{1,10}(\\.[0-9]{0,3})?";
	private static final String RE_CHAR = "[A-Za-z\\s0-9áéíóúÁÉÍÓÚñÑ\\.,()\\-\\/&]+";	
    private static final String RE_CHAR_NUMBER = "[A-Za-z0-9]+";
    
    private static final String RE_CHARNUMBER_IDEXT = "[A-Za-z0-9\\-\\s]*";
    private static final String RE_CHAR_ALL = "[a-zA-ZÑñáéíóúÁÉÍÓÚ0-9\\$\\%\\s\\?\\)\\¡\\¿\\(\\[\\]\\{\\}\\+\\/\\=\\@\\_\\!\\#\\*\\:\\;\\.\\&\\,\\-]*";
    
    
    private static final String RE_NUMBER = "[0-9]+";
    private static final String RE_MAIL = "^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,3})$";

    private StringBuilder sb;
    
    private List<Invoice_Masivo> listIn = null;
    
    private static int rows;
	private boolean finArchivo;
	    
	private File fileExitXML = null;				
	private File fileExitBD = null;
	//private File fileExitODM = null;
	private File fileExitINC = null;
	
	
	
	
	private FileOutputStream salidaXML = null;
	private FileOutputStream salidaBD = null;
	private FileOutputStream salidaINC = null;
	//private FileOutputStream salidaODM = null;
	
	String PathFacturacionEntrada=MassiveReadController.PathFacturacionEntrada;
	String PathFacturacionProceso=MassiveReadController.PathFacturacionProceso;
	String PathFacturacionSalida=MassiveReadController.PathFacturacionSalida;
	String PathFacturacionOndemand=MassiveReadController.PathFacturacionOndemand;
	
	@Autowired(required = true)
	private MassiveManager massiveManager;
	
	@Autowired(required = true)
	private UserManager userManager;
	
	@Autowired(required = true)
	private UtilCFDIFormatoUnico fillFU;
	
   	public GeneraXMLProceso_Masivo() {

	}
   	
   	

	
	public void processControlFile(String urlWSTimbrado){
		String idMassive = "";
		String nameFileExcel = "";
		
		
		try{
			
			//FileInputStream fsExcelsToProcess = new FileInputStream(properties.getPathFacturacionEntrada() + "IDFILEPROCESS_" + nProceso + ".TXT");
			FileInputStream fsExcelsToProcess = new FileInputStream(PathFacturacionEntrada + "IDFILEPROCESS.TXT");
			DataInputStream in = new DataInputStream(fsExcelsToProcess);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			int counter=0;
			//Iniciar conexion con WebService								
			if(this.servicePort == null){
				this.servicePort = new WebServiceClienteUnicoDivisas();								
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
				
				if(customerNew.getTaxId().toUpperCase().trim().equals("XEXX010101000") || customerNew.getTaxId().toUpperCase().trim().equals("XAXX010101000")){
					//Cliente extranjero
					hashClientes.put(customerNew.getIdExtranjero(), customerNew);
				}else{
					//Cliente normal
					hashClientes.put(customerNew.getTaxId()+customerNew.getFiscalEntity().getId(), customerNew);
				}
				
			}
			
			HashMap<Long,String> hashCfdFieldsV22 = new HashMap<Long,String>();
			for(CFDFieldsV22 cfdV22:cfdFieldsV22Manager.listAll()){
				String strLugarExp = "";
				String strRegFiscal = "";
				if(cfdV22.getRegimenFiscal()!=null){
					if(cfdV22.getRegimenFiscal().getName()!=null){
						strRegFiscal = cfdV22.getRegimenFiscal().getName(); 
					}
					if(cfdV22.getLugarDeExpedicion()!=null){
						strLugarExp = cfdV22.getLugarDeExpedicion(); 
					}
				}
				hashCfdFieldsV22.put(cfdV22.getFiscalEntity().getId(), strRegFiscal+"|"+strLugarExp);
			}
			
			//FileOutputStream fileStatus = new FileOutputStream(PathFacturacionProceso + "STATUS_PROCESS_" + nProceso + ".TXT");
			FileOutputStream userlog = null;
			FileOutputStream fileStatus = new FileOutputStream(PathFacturacionProceso + "massiveProcess.txt");
			fileStatus.write(("Status del proceso bash massiveProcess.sh" + "\n").getBytes("UTF-8"));
			while((strLine = br.readLine()) != null){
				//System.out.println("lineExcelsToProcess: " + strLine);
				String [] arrayRenglon = strLine.split("\\|");
				if(arrayRenglon.length>1){
					idMassive = arrayRenglon[0];
					
					//Obtener Id del Area y del usuario que hizo la solicitud 
					Massive massive = massiveManager.getById(Long.parseLong(idMassive));
					User user = userManager.findByName(massive.getAuthor());					
					String idUsuario = "0";
					String idArea = "0";
					String nombreUsuario = "";
					
					if(user != null){
						idUsuario = String.valueOf(user.getId());
						idArea = String.valueOf(user.getIdArea());
						nombreUsuario = user.getUserName();
					}
					
					System.out.println("idUsuario:" + idUsuario);
					System.out.println("idArea:" + idArea);
					
					nameFileExcel = arrayRenglon[1] + ".TXT";
					//System.out.println("idMassive: " + idMassive);
					//System.out.println("nameFileExcel: " + nameFileExcel);
															
					File fileTXT = new File(PathFacturacionProceso + arrayRenglon[1] + "/" + nameFileExcel);
					if(!fileTXT.exists()){
						fileStatus.write(("El archivo " + nameFileExcel + " no se encuentra en la ruta " + PathFacturacionProceso + arrayRenglon[1] + "/" + "\n").getBytes("UTF-8"));
						userlog = new FileOutputStream(PathFacturacionSalida + arrayRenglon[1] + "/LOG" + arrayRenglon[1] + ".TXT",true);
						userlog.write(("El archivo " + nameFileExcel + " no se encuentra en la ruta " + PathFacturacionProceso + arrayRenglon[1] + "/" + "\r\n").getBytes("UTF-8"));
					}else{
						//Crear directorio en ruta ../facturacion/salida/
						/*File fileDirectory = new File(PathFacturacionSalida + arrayRenglon[1] + "/");
						if(!fileDirectory.exists()){
			            	fileDirectory.mkdir();
			            }else{
			            	for(File file:fileDirectory.listFiles()){
			               	 file.delete();
			                }            	
			            }*/
						
						this.offSetComprobante = 0;
						//String strAbsolutePathXML = PathFacturacionSalida + arrayRenglon[1] + "/" + "XML" + nameFileExcel.substring(0, nameFileExcel.indexOf(".")) + ".TXT";
						String strAbsolutePathBD = PathFacturacionProceso + arrayRenglon[1] + "/" + "BD" + nameFileExcel.substring(0, nameFileExcel.indexOf(".")) + ".TXT";
						
						//String strAbsolutePathODM = properties.getPathFacturacionOndemand() + "ODM-" + nameFileExcel.substring(0, nameFileExcel.indexOf("."));
						//String strAbsolutePathINC = PathFacturacionSalida + arrayRenglon[1] + "/" + "INC" + nameFileExcel.substring(0, nameFileExcel.indexOf(".")) + ".TXT";
						
						//this.fileExitXML = new File(strAbsolutePathXML);				
						this.fileExitBD = new File(strAbsolutePathBD);
						//this.fileExitODM = new File(strAbsolutePathODM);
						//this.fileExitINC = new File(strAbsolutePathINC);
						
						
						//this.salidaXML = new FileOutputStream(this.fileExitXML);
						this.salidaBD = new FileOutputStream(this.fileExitBD);
						//this.salidaINC = new FileOutputStream(this.fileExitINC);
						//this.salidaODM = new FileOutputStream(this.fileExitODM);
						
						//StringBuilder sbErrorFile = this.processExcelFile("/planCFD/procesos/Interfactura/interfaces/" + nameFileExcel);
						
						//processOneSheet("/planCFD/procesos/Interfactura/interfaces/" + nameFileExcel);
						
						FileInputStream fisTxt = new FileInputStream(PathFacturacionProceso + arrayRenglon[1] + "/" + nameFileExcel);
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
							//System.out.println("strLine:" + strLineTXT);
							//System.out.println("length:" + arrayValues.length);
							if(!strLineTXT.equals("")){
								if(arrayValues.length<47){
									//System.out.println("Factura " + (factura+1) + ", incompleta!");								
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
									
									CfdiComprobanteFiscal comp = new CfdiComprobanteFiscal();
									//antiguo metodo fill y validate
									//StringBuilder sbErrorFile = this.processRowExcel(arrayValues, factura+1, hashIvas, hashcodigoISO, hashmoneda, hashEmisores, hashClientes, hashCfdFieldsV22);
									StringBuilder sbErrorFile = new StringBuilder();
									
									//llenar comprobante desde archivo
									comp = fillFU.fillComprobanteFUTxt(arrayValues);
									
									//validar comprobante
									sbErrorFile.append(validations.validateComprobante(comp, factura));
									
									//convertir comprobante a invoice
									invoice = UtilCatalogos.fillInvoice(comp);
									
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
						            }
						    		
									
									if(sbErrorFile.toString().length() > 0){
										sb.append("Factura: " + factura + " -- Lista de Errores: " + "\n" + sbErrorFile.toString() + "\n");								
										System.out.println("fError EmisionMasivaFacturas: " + sb.toString());							
										invoice.setSbError(sbErrorFile);
										listIn.add(invoice);
									}else{								
										
										try{
											System.out.println("AntesfacturaOK - " + factura);
											System.out.println("Antes de crearFac");
											
											if(!invoice.getSiAplicaIva())								
												invoice.setDescriptionIVA("EXENTO");
											
											//generar xml y asignarlo a invoice
											byte[] xmlBytes = xmlGenerator.convierte(comp).getBytes("UTF-8");
											ByteArrayOutputStream baosXml = new ByteArrayOutputStream(xmlBytes.length);
											baosXml.write(xmlBytes, 0, xmlBytes.length);
											invoice.setByteArrXMLSinAddenda(baosXml);
											
											crearFactura(factura);
											
											System.out.println("facturaOK - " + factura);
											listIn.add(invoice);
										}catch (Exception e) {								
											sb.append("Factura: " + factura + " --Exception: " + "\n" + e.getMessage() + "\n");
											System.out.println("Factura: " + factura + " --Exception: " + "\n" + e.getMessage());
											invoice.setSbError(new StringBuilder("Factura: " + factura + " --Exception: " + e.getMessage() + "\n"));
											listIn.add(invoice);
										}
										
										
									}
									
									
									if(sbErrorFile.toString().length() > 0 ){
										//salidaINC.write(("ErrorArchivo|" + sbErrorFile.toString() + "\n").getBytes("UTF-8"));
										sbStatusNoOK.append("ErrorArchivo|" + sbErrorFile.toString() + "\n");
										
										counterNoOk++;
										
									}else{
										for(int index=0; index<listIn.size(); index++){
											if(listIn.get(index).getByteArrXMLSinAddenda() != null){
												String strXmlATimbrar = listIn.get(index).getByteArrXMLSinAddenda().toString("UTF-8");
												
												/////////////Inicio Bloque de Timbrado//////////////////
																				
												// Invoke the web service operation using the port or stub or proxy
												 
												//String helloMessage = servicePort.consultaTimbre("F7EFFF4D-2816-425C-81CC-B26DF8C177DB", null, true) ;
												//System.out.println("Conectandose...........");
																	
												//System.out.println("nomInterface" + nameFileExcel.substring(0, nameFileExcel.indexOf(".")) + "--");
												String xmlTimbradoConPipe = ""; 
												xmlTimbradoConPipe = this.servicePort.generaTimbre(strXmlATimbrar, false, properties_fd.getServiceUnicoDivisas(), properties, nameFileExcel.substring(0, nameFileExcel.indexOf(".")), 0, 1, listIn.get(index).getPeriod(), "");
												
												String xmlTimbrado = xmlTimbradoConPipe.substring(0, xmlTimbradoConPipe.length()-1);
												
												//.out.println("XML Timbrado: " + xmlTimbrado);
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
													doc = this.generaXML.agregaAddenda(doc, invoice);
													
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
													String strAbsolutePathOndemand = PathFacturacionOndemand + nameFileExcel.substring(0, nameFileExcel.indexOf(".")) + "/";
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
													
													//System.out.println("Guarda en BD...TXT");
													salidaBD.write(this.buildRowBDFile(listIn.get(index), strUUID, nameFileExcel, idUsuario, idArea, nombreUsuario).getBytes("UTF-8"));
													
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
											
						//if (salidaODM != null)
						//	salidaODM.close();	
					
						fileStatus.write(("Archivo " + nameFileExcel + " procesado\n").getBytes("UTF-8"));
						userlog = new FileOutputStream(PathFacturacionSalida + arrayRenglon[1] + "/LOG" + arrayRenglon[1] + ".TXT",true);
						userlog.write(("Archivo " + nameFileExcel + " procesado\r\n").getBytes("UTF-8"));
						
						String strAbsolutePathXML = PathFacturacionSalida + arrayRenglon[1] + "/" + "XML" + nameFileExcel.substring(0, nameFileExcel.indexOf(".")) + ".TXT";
						String strAbsolutePathINC = PathFacturacionSalida + arrayRenglon[1] + "/" + "INC" + nameFileExcel.substring(0, nameFileExcel.indexOf(".")) + ".TXT";
						
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
			System.out.println("Exception massiveProcess:" + ex.getMessage());
			
			try {
				//FileOutputStream fileError = new FileOutputStream(PathFacturacionProceso + "ERROR_PROCESS_" + nProceso + ".TXT");
				FileOutputStream fileError = new FileOutputStream(PathFacturacionProceso + "massiveProcessError.txt");
				fileError.write((ex.getMessage()).getBytes("UTF-8"));
				fileError.close();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				//System.out.println("Exception al crear ERROR_PROCESS_" + nProceso + ".TXT:" + e1.getMessage());
				System.out.println("Exception al crear massiveProcessError.txt:" + e1.getMessage());
			}		
		}
	}
	
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
		temp.append(1 + "<#EMasfUD,>");
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
		
	private boolean validaDatoRELongitud(String dato, String expReg, int longitud) {
        return dato != null && dato.trim().length() > 0 && dato.length() <= longitud && dato.matches(expReg);
    }
	
	
	private boolean validaDatoRE(String dato, String expReg) {
        return dato != null && dato.trim().length() > 0 && dato.matches(expReg);
    }

	
	public StringBuilder processRowExcel(String [] strValues, int factura, 
			HashMap<Integer, String> hashIvas, HashMap<String, String> hashcodigoISO, 
			HashMap<String, String> hashmoneda, HashMap<String,FiscalEntity> hashEmisores, 
			HashMap<String,Customer> hashClientes, HashMap<Long,String> hashCfdFieldsV22) {
		
		
		
    		StringBuilder sbError = null;
    		StringBuilder sbErrorFile = null;
    		boolean fError;
			try{
				/*for(int index=0; index<strValues.length; index++){
					System.out.println("index:" + index + " value:" + strValues[index] + "--");
				}*/ //all fields debug
				
				/*if(!strValues[strValues.length-1].trim().toUpperCase().equals("FINFACTURA")){
					System.out.println("Estructura de Archivo incorrecta, no fue encontrada la etiqueta de control ||FINFACTURA||");
					sbErrorFile = new StringBuilder();
					sbErrorFile.append("Estructura de Archivo incorrecta, no fue encontrada la etiqueta de control ||FINFACTURA||");
				}else if(strValues.length<30){
					System.out.println("Factura " + factura + ", incompleta!");
					sbErrorFile = new StringBuilder();
					sbErrorFile.append("Factura " + factura + ", incompleta!");
				}else{*/
					fError = false;
					sbError = new StringBuilder();							
					sb = new StringBuilder();
					int facturasOK = 0;
					int facturasError = 0;
					
					//System.out.println("Procesa los primeros 30 campos");
					
					//fiscalEntity = new FiscalEntity();
					Customer customer = null;
					//CFDFieldsV22 cfdFieldsV22 = null;								
					
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
						sbError.append("(1) Posicion Entidad Fiscal requerida (Null) - Factura " + factura + "\n");
						System.out.println("(1) Posicion Entidad Fiscal requerida (Null) - Factura " + factura + "\n");
						fError = true;
					}else{
						
						//System.out.println("Emisor: " + strValues[0].toString());
						
						if(hashEmisores.containsKey(strValues[0].toString().toUpperCase().trim())){
							fiscalEntity = hashEmisores.get(strValues[0].toString().toUpperCase().trim()); 
						}
						
														
						if(fiscalEntity == null) {
						
							sbError.append("(1) Entidad Fiscal no existe en BD - Factura " + factura + "\n");
							System.out.println("(1) Entidad Fiscal no existe en BD - Factura " + factura + "\n");
							fError = true;
												
						}else{
							if(fiscalEntity.getIsDonataria() == 1) {
								
								sbError.append("(1) Entidad Fiscal incorrecta, es donataria - Factura " + factura + "\n");
								System.out.println("(1) Entidad Fiscal incorrecta, es donataria - Factura " + factura + "\n");
								fError = true;
							
							}else{
								invoice.setFe_Id(String.valueOf(fiscalEntity.getId()));
								invoice.setFe_taxid(String.valueOf(fiscalEntity.getTaxID()));
							}
						}	
																										
					}					
					
					if(strValues[1] == null){
						invoice.setSerie("");		                            
					}else{
						
						if(strValues[1].toString().trim().length() > 0){
	                        if(validaDatoRELongitud(strValues[1].toString().trim(), RE_CHAR_NUMBER,13)){
	                        	//System.out.println("serie: " + strValues[1].toString());
	                            invoice.setSerie(strValues[1].toString().trim());
	                        }else{
	                    
	                            sbError.append("(2) Serie con formato incorrecto - Factura " + factura + "\n");
	                            System.out.println("(2) Serie con formato incorrecto - Factura " + factura + "\n");
	                            fError = true;
	                    
	                        }
	                    } else{
	                        invoice.setSerie("");
	                    }
						
					}								
					
					if(strValues[2] == null){
						invoice.setTipoFormato("ingreso");
					}else{									
						
						if(strValues[2].toString().trim().equals("")) {
							invoice.setTipoFormato("ingreso");											
							//System.out.println("tipoComprobante: " + invoice.getTipoFormato());
						}else if(strValues[2].toString().trim().toUpperCase().equals("INGRESO") || strValues[2].toString().trim().toUpperCase().equals("EGRESO") ){
							invoice.setTipoFormato(strValues[2].toString().trim());											
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
	                        String mon=strValues[3].toString().trim();
	                        
	                        if(!hashmoneda.containsKey(mon)){
	                        	sbError.append("(4) Moneda no existe en BD - Factura " + factura + "\n");
		                        System.out.println("(4) Moneda no existe en BD - Factura " + factura + "\n");
		                        fError = true;
	                        }else{
	                        	System.out.println("moneda: " + mon);
								invoice.setMoneda(mon);
		                        invoice.setTipoMoneda(mon);
	                        }										
	                    }else{
	                    
	                        sbError.append("(4) Moneda requerida - Factura " + factura + "\n");
	                        System.out.println("(4) Moneda requerida - Factura " + factura + "\n");
	                        fError = true;
	                    
	                    }
					
					}
					
					if(strValues[4] == null){
						
                        sbError.append("(5) Posicion Tipo de Cambio requerida (Null) - Factura " + factura + "\n");
                        System.out.println("(5) Posicion Tipo de Cambio es requerida (Null) - Factura " + factura + "\n");
                        fError = true;
					}else{
						
						if(strValues[4].toString().trim().length() >0){
							
							if(validaDatoRELongitud(strValues[4].toString().trim(),RE_DECIMAL,7)) {
								//System.out.println("tipo de cambio: " + strValues[4].toString());
								invoice.setTipoCambio(strValues[4].toString().trim());
								invoice.setExchange(Double.valueOf(strValues[4].toString().trim()));
							} else {
						
		                        sbError.append("(5) Tipo de Cambio tiene formato incorrecto - Factura " + factura + "\n");
		                        System.out.println("(5) Tipo de Cambio tiene formato incorrecto - Factura " + factura + "\n");
		                        fError = true;
		                
							}
						
	                    }else{
	                    
	                        sbError.append("(5) Tipo de Cambio es requerido - Factura " + factura + "\n");
	                        System.out.println("(5) Tipo de Cambio es requerido - Factura " + factura + "\n");
	                        fError = true;
	                    
	                    }
					
					}
					
					if(strValues[5] == null){
						
                        sbError.append("(6) Posicion RFC del Cliente requerida (Null) - Factura " + factura + "\n");
                        System.out.println("(6) Posicion RFC del Cliente requerida (Null) - Factura " + factura + "\n");
                        fError = true;
					}else{
						
						invoice.setRfc(strValues[5].toString().trim());
						//System.out.println("RFC Cliente: " + strValues[5].toString());
						if(invoice.getRfc().trim().equals("")) {
						
							sbError.append("(6) RFC de Cliente requerido - Factura " + factura + "\n");
							System.out.println("(6) RFC de Cliente requerido - Factura " + factura + "\n");
							fError = true;
						
						} else {
							if(!invoice.getRfc().toUpperCase().equals("XEXX010101000") && !invoice.getRfc().toUpperCase().equals("XAXX010101000")){
								if(fiscalEntity != null){
	                        		System.out.println("CustomerRFC: " + invoice.getRfc() + " feId: " + String.valueOf(fiscalEntity.getId()));
	                        		if(hashClientes.containsKey(invoice.getRfc() + String.valueOf(fiscalEntity.getId()))){
	                        			customer = hashClientes.get(invoice.getRfc() + String.valueOf(fiscalEntity.getId()));
	                        		}					                        		
	                        	}
													                            
	                        }else{
	                        	if(strValues[6] == null || strValues[6].toString().trim().length() == 0){
	        						
	                        		invoice.setIdExtranjero("");
								}else{
									if(validaDatoRE(strValues[6].toString().trim(), RE_CHARNUMBER_IDEXT)){
	                            		String strIDExtranjero = strValues[6].toString().trim();
	                            		
	                            		if(hashClientes.containsKey(strIDExtranjero)){
	                            			customer = hashClientes.get(strIDExtranjero);
	                            			
	                            			//System.out.println("ID Extranjero: " + strIDExtranjero);
			                            	
			                            	invoice.setIdExtranjero(strIDExtranjero);
		                            		
	                            		}else{
	                            			sbError.append("(7) Id extranjero no existe en BD - Factura " + factura + "\n");
			                                System.out.println("(7) Id extranjero no existe en BD - Factura " + factura + "\n");
			                                fError = true;
	                            		}                        		
	                            		
	                            	}else{
	                            		sbError.append("(7) Id extranjero con formato incorrecto - Factura " + factura + "\n");
		                                System.out.println("(7) Id extranjero con formato incorrecto - Factura " + factura + "\n");
		                                fError = true;
	                            	}
								}			                        	
	                        }
						}
						
					}
					
					if(customer != null){
						//System.out.println("Cliente " + invoice.getRfc() + " EXISTE");
											
						if(customer.getPhysicalName() == null){
						
	                        sbError.append("Nombre del Cliente no existe en BD - Factura " + factura + "\n");
	                        System.out.println("Nombre del Cliente no existe en BD - Factura " + factura + "\n");
	                        fError = true;
						}else{
							//Set customer's name
							//System.out.println("Nombre Cliente: " + customer.getPhysicalName());
	                        invoice.setName(customer.getPhysicalName().trim());
						}
						
						//Verificar si existe direccion
						if(customer.getAddress() == null){
						
	                        sbError.append("Direccion del Cliente no existe en BD - Factura " + factura + "\n");
	                        System.out.println("Direccion del Cliente no existe en BD - Factura " + factura + "\n");
	                        fError = true;
	                    
						}else{
							//Set customer's street
							if(customer.getAddress().getStreet() == null){
								//System.out.println("calleigualanull");
						
	                            sbError.append("Calle del Cliente no existe en BD - Factura " + factura + "\n");
	                            System.out.println("Calle del Cliente no existe en BD - Factura " + factura + "\n");
	                            fError = true;
							}else{
								//System.out.println("Calle: " + customer.getAddress().getStreet());
								invoice.setCalle(customer.getAddress().getStreet().trim());
							}
							
							//Set customer's internal number
							if(customer.getAddress().getInternalNumber() == null){
						
	                           // sbError.append("Num Interior del Cliente no existe en BD - Factura " + factura + "\n");
	                           // System.out.println("Num Interior del Cliente no existe en BD - Factura " + factura + "\n");
	                           // fError = true;
								invoice.setInterior("");
							}else{
								//System.out.println("Numero interno: " + customer.getAddress().getInternalNumber());												
								invoice.setInterior(customer.getAddress().getInternalNumber().trim());
							}
							
							//Set customer's external number
							if(customer.getAddress().getExternalNumber() == null){
						
	                            sbError.append("Num Exterior del Cliente no existe en BD - Factura " + factura + "\n");
	                            System.out.println("Num Exterior del Cliente no existe en BD - Factura " + factura + "\n");
	                            fError = true;
							}else{
								//System.out.println("Numero exterior: " + customer.getAddress().getExternalNumber());
								invoice.setExterior(customer.getAddress().getExternalNumber().trim());
							}	
																	
							//Set customer's neighborhood
							
							if(customer.getAddress().getNeighborhood() == null){
						
	                            sbError.append("Colonia del Cliente no existe en BD - Factura " + factura + "\n");
	                            System.out.println("Colonia del Cliente no existe en BD - Factura " + factura + "\n");
	                            fError = true;
	                    
							}else{
								//System.out.println("Colonia: " + customer.getAddress().getNeighborhood());
								invoice.setColonia(customer.getAddress().getNeighborhood().trim());
							}
																	
							
							//Set customer's region
							if(customer.getAddress().getRegion() == null){
						
	                            sbError.append("Localidad del Cliente no existe en BD - Factura " + factura + "\n");
	                            System.out.println("Localidad del Cliente no existe en BD - Factura " + factura + "\n");
	                            fError = true;
	                    
							}else{
								//System.out.println("Localidad: " + customer.getAddress().getRegion());
								invoice.setLocalidad(customer.getAddress().getRegion().trim());
							}
							
																
							//Set customer's city
							
							if(customer.getAddress().getCity() == null){
						
	                            sbError.append("Municipio del Cliente no existe en BD - Factura " + factura + "\n");
	                            System.out.println("Municipio del Cliente no existe en BD - Factura " + factura + "\n");
	                            fError = true;
	                    
							}else{
								//System.out.println("Municipio: " + customer.getAddress().getCity());
								invoice.setMunicipio(customer.getAddress().getCity().trim());
							}
																
							//Set customer's state
							if(customer.getAddress().getState() == null){
								sbError.append("Estado del Cliente no existe en BD - Factura " + factura + "\n");
	                            System.out.println("Estado del Cliente no existe en BD - Factura " + factura + "\n");
	                            fError = true;
							}else{
								if(customer.getAddress().getState().getName() == null){
									
		                            sbError.append("Estado del Cliente no existe en BD - Factura " + factura + "\n");
		                            System.out.println("Estado del Cliente no existe en BD - Factura " + factura + "\n");
		                            fError = true;
		                    
								}else{
									//System.out.println("Estado: " + customer.getAddress().getState().getName());
									invoice.setEstado(customer.getAddress().getState().getName().trim());
								}
								
								//Set customer's country
								if(customer.getAddress().getState().getCountry() == null){
									sbError.append("Pais del Cliente no existe en BD - Factura " + factura + "\n");
		                            System.out.println("Pais del Cliente no existe en BD - Factura " + factura + "\n");
		                            fError = true;
								}else{
									if(customer.getAddress().getState().getCountry().getName() == null){
										
			                            sbError.append("Pais del Cliente no existe en BD - Factura " + factura + "\n");
			                            System.out.println("Pais del Cliente no existe en BD - Factura " + factura + "\n");
			                            fError = true;
			                    
									}else{
										//System.out.println("Pais: " + customer.getAddress().getState().getCountry().getName());
										invoice.setPais(customer.getAddress().getState().getCountry().getName().trim());
									}
								}							
																									
								//Set customer's zip code
								if(customer.getAddress().getZipCode() == null){
							        sbError.append("Codigo postal del Cliente no existe en BD - Factura " + factura + "\n");
	                                System.out.println("Codigo postal del Cliente no existe en BD - Factura " + factura + "\n");
	                                fError = true;
	                        
								}else{
									//System.out.println("Codigo Postal: " + customer.getAddress().getZipCode());
									invoice.setCodigoPostal(customer.getAddress().getZipCode().trim());
								}
							}
							
										                    	
	                    }										
					}else{
						if(
						(!invoice.getRfc().toUpperCase().equals("XEXX010101000") && !invoice.getRfc().toUpperCase().equals("XAXX010101000"))
						||
						((invoice.getRfc().toUpperCase().equals("XEXX010101000") || invoice.getRfc().toUpperCase().equals("XAXX010101000")) 
						&& 
						invoice.getIdExtranjero() != null && !invoice.getIdExtranjero().trim().equals(""))
						){
							sbError.append("(6) RFC del Cliente no existe en BD - Factura " + factura + "\n");
	                        fError = true;
						}                        
					}
					
					if(fiscalEntity == null){
						
            			sbError.append("Regimen Fiscal  del Emisor no existe en BD - Factura " + factura + "\n");
            			System.out.println("Regimen Fiscal  del Emisor no existe en BD - Factura " + factura + "\n");
            			sbError.append("Lugar de Expedicion  del Emisor no existe en BD - Factura " + factura + "\n");
            			System.out.println("Lugar de Expedicion  del Emisor no existe en BD - Factura " + factura + "\n");
            			fError = true;
					}else{
						
						
						if(hashCfdFieldsV22.containsKey(fiscalEntity.getId())){
						
							String [] arrayValues = hashCfdFieldsV22.get(fiscalEntity.getId()).split("\\|");
							
							if(arrayValues.length>1){
								invoice.setRegimenFiscal(arrayValues[0]);
                            	//System.out.println("Regimen Fiscal: " + arrayValues[0]);
                            	
                            	invoice.setLugarExpedicion(arrayValues[1]);
                            	//System.out.println("Lugar Expedicion: " + arrayValues[1]);
							}else{
								invoice.setRegimenFiscal("");
                            	                            	
                            	invoice.setLugarExpedicion("");
                            	
							}
							
	            			  
	            			
	            		}else{
	            			
	            			sbError.append("Regimen Fiscal del Emisor no existe en BD - Factura " + factura + "\n");
	            			System.out.println("Regimen Fiscal del Emisor no existe en BD - Factura " + factura + "\n");
	            			sbError.append("Lugar de Expedicion del Emisor no existe en BD - Factura " + factura + "\n");
	            			System.out.println("Lugar de Expedicion del Emisor no existe en BD - Factura " + factura + "\n");
	            			fError = true;
	                        
	            		}
					}
					
					
					if(strValues[7] == null){									
						sbError.append("(8) Posicion Metodo de Pago requerida (Null) - Factura " + factura  + "\n");
                        System.out.println("(8) Posicion Metodo de Pago requerida (Null) - Factura " + factura  + "\n");
                        fError = true;																
					}else{
						
						if(strValues[7].toString().trim().equals("")){
							sbError.append("(8) Metodo de Pago requerido - Factura " + factura  + "\n");
                            System.out.println("(8) Metodo de Pago requerido - Factura " + factura  + "\n");
                            fError = true;
						}else{
							if(validaDatoRE(strValues[7].toString(), RE_CHAR_ALL)){
								invoice.setMetodoPago(strValues[7].toString().trim());
                            	//System.out.println("Metodo de pago: " + strValues[7].toString());
                            }else{                    	
                            	
                                sbError.append("(8) Metodo de Pago con formato incorrecto - Factura " + factura  + "\n");
                                System.out.println("(8) Metodo de Pago con formato incorrecto - Factura " + factura  + "\n");
                                fError = true;
                                
                            }
						}	
					
					}
					
					if(strValues[8] == null){									
						invoice.setFormaPago("PAGO EN UNA SOLA EXHIBICION");
                        //System.out.println("Forma de pago: PAGO EN UNA SOLA EXHIBICION");
					}else{								
						
	                    if(strValues[8].toString().trim().equals("")) {
	                        invoice.setFormaPago("PAGO EN UNA SOLA EXHIBICION");
	                        //System.out.println("Forma de pago: PAGO EN UNA SOLA EXHIBICION");
	                    }else{
	                        if(validaDatoRE(strValues[8].toString(), RE_CHAR_ALL)){
	                        	//System.out.println("Forma de pago: " + strValues[8].toString());
	                        	invoice.setFormaPago(strValues[8].toString().trim());
	                        }else{			                        	
	                    
	                            sbError.append("(9) Forma de Pago con formato incorrecto - Factura " + factura + "\n");
	                            System.out.println("(9) Forma de Pago con formato incorrecto - Factura " + factura + "\n");
	                            fError = true;
	                    
	                        }
	                    }
						
					}
					
					if(strValues[9] == null){
						
                        sbError.append("(10) Posicion Numero de Cuenta de Pago requerida (Null) - Factura " + factura  + "\n");
                        System.out.println("(10) Posicion Numero de Cuenta de Pago requerida (Null) - Factura " + factura  + "\n");
                        fError = true;
					}else{
						if(strValues[9].toString().trim().equals("")){
							sbError.append("(10) Numero de Cuenta de Pago requerida - Factura " + factura  + "\n");
	                        System.out.println("(10) Numero de Cuenta de Pago requerida - Factura " + factura  + "\n");
	                        fError = true;
						}else{
							
							
								String strNumCtaPago = strValues[9].toString();
								//System.out.println("strNumCtaPago: " + strNumCtaPago);
		                        if(validaDatoRE(strNumCtaPago, RE_CHAR_ALL)){
		                        	invoice.setNumCtaPago(strNumCtaPago);
									//System.out.println("Numero de cuenta de pago: " + strNumCtaPago);
		                        	
		                        	/*if(strNumCtaPago.length() == 4){
		                        		invoice.setNumCtaPago(strNumCtaPago);
										//System.out.println("Numero de cuenta de pago: " + strNumCtaPago);
		                        	}else{
		                        		sbError.append("(10) Numero de Cuenta de Pago debe ser de 4 digitos - Factura " + factura + "\n");
			                            System.out.println("(10) Numero de Cuenta de Pago debe ser de 4 digitos - Factura " + factura + "\n");
			                            fError = true;
		                        	}*/
		                        	
		                        }else{
		                        
		                            sbError.append("(10) Numero de Cuenta de Pago con formato incorrecto - Factura " + factura + "\n");
		                            System.out.println("(10) Numero de Cuenta de Pago con formato incorrecto - Factura " + factura + "\n");
		                            fError = true;
		                        
		                        }
							
						}
						
					}
					
					if(strValues[10] == null){
						
						invoice.setReferencia("");
					}else{	
						
						if(strValues[10].toString().trim().equals("")){
							invoice.setReferencia(strValues[10].toString().trim());	
						}else{
							if(validaDatoRE(strValues[10].toString(), RE_CHAR_ALL)){
						    	invoice.setReferencia(strValues[10].toString().trim());
								//System.out.println("Referencia: " + strValues[10].toString());									
	                            
	                        }else{
	                        	
	                            sbError.append("(11) Referencia con formato incorrecto - Factura " + factura + "\n");
	                            System.out.println("(11) Referencia con formato incorrecto - Factura " + factura + "\n");
	                            fError = true;
	                            
	                        }
						}
						
					}
					if(strValues[11] == null){									
						invoice.setCustomerCode("");
					}else{
						if(strValues[11].toString().trim().equals("")){
							invoice.setCustomerCode("");
						}else{
							
								if(strValues[11].toString().trim().length() > 0 ) {
									String strCodigoCliente = strValues[11].toString();
			                        if(validaDatoRELongitud(strCodigoCliente, "[a-zA-Z0-9&_\\/\\-\\s]*", 10)){
			                        	invoice.setCustomerCode(strCodigoCliente);
				                        //System.out.println("Codigo cliente:" + strCodigoCliente);
			                        }else{
			                        	sbError.append("(12) Codigo Cliente con formato incorrecto - Factura " + factura + "\n");
			                            System.out.println("(12) Codigo Cliente con formato incorrecto - Factura " + factura + "\n");
			                            fError = true;
			                        }
								}else{
									invoice.setCustomerCode(strValues[11].toString().trim());
								}
							
						}
						
					}
					
					if(strValues[12] == null){
						  
						invoice.setContractNumber("");
					}else{
						if(strValues[12].toString().trim().equals("")){
							invoice.setContractNumber("");
						}else{
							
								if(strValues[12].toString().trim().length() > 0 ) {			
									String strContrato = strValues[12].toString();
			                        if(validaDatoRELongitud(strContrato, "[a-zA-Z0-9&_\\/\\-\\s]*", 20)){
			                        	//System.out.println("Contrato: " + strContrato);
				                        invoice.setContractNumber(strContrato);
			                            
			                        }else{				                        	
			                            sbError.append("(13) Contrato con formato incorrecto" + "\n");
			                            System.out.println("(13) Contrato con formato incorrecto" + "\n");
			                            fError = true;				                            
			                        }
			                    } else {
			                    	invoice.setContractNumber(strValues[12].toString().trim());
			                    }
							
						}
						
					}
					
					if(strValues[13] == null){									
						invoice.setPeriod("");
					}else{
						if(strValues[13].toString().trim().equals("")){
							invoice.setPeriod("");
						}else{
							
								if(strValues[13].toString().trim().length() > 0 ) {
									String strPeriodo = strValues[13].toString();
			                        if(validaDatoRELongitud(strPeriodo, "[a-zA-Z0-9&_\\/\\-\\s]*", 19)){
			                        	//System.out.println("Periodo: " + strPeriodo);
				                        invoice.setPeriod(strPeriodo);
			                            
			                        }else{				                        	
			                            sbError.append("(14) Periodo con formato incorrecto - Factura " + factura + "\n");
			                            System.out.println("(14) Periodo con formato incorrecto - Factura " + factura + "\n");
			                            fError = true;				                            
			                        }
			                    } else {
			                    	invoice.setPeriod(strValues[13].toString().trim());
			                    }
							
						}
						
					}
					
					if(strValues[14] == null){									
						invoice.setCostCenter("");
					}else{
						if(strValues[14].toString().trim().equals("")){
							invoice.setCostCenter("");
						}else{
							
								if(strValues[14].toString().trim().length() > 0 ) {
									String strCentroCostos = strValues[14].toString();
			                        if(validaDatoRELongitud(strCentroCostos, "[a-zA-Z0-9&_\\/\\-\\s]*", 20)){
			                        	//System.out.println("Centro de costos: " + strCentroCostos);
				                        invoice.setCostCenter(strCentroCostos);
			                            
			                        }else{				                        	
			                            sbError.append("(15) Centro de Costos con formato incorrecto - Factura " + factura + "\n");
			                            System.out.println("(15) Centro de Costos con formato incorrecto - Factura " + factura + "\n");
			                            fError = true;				                            
			                        }
			                    } else {
			                    	invoice.setCostCenter(strValues[14].toString().trim());
			                    }
							
						}
						
					}
					
					if(strValues[15] == null){
						invoice.setDescriptionConcept("");
					}else{
					
						if(strValues[15].toString().trim().length()>0){
							if(strValues[15].toString().trim().length() <= 250){
		                        invoice.setDescriptionConcept(strValues[15].toString().trim());
		                        //System.out.println("Descripcion Concept: " + strValues[15].toString());
		                    }else{
		                    	sbError.append("(16) Descripcion con formato incorrecto - Factura " + factura + "\n");
		                    	System.out.println("(16) Descripcion con formato incorrecto - Factura " + factura + "\n");
		                    	fError = true;				                        
		                    }
						}else{
							invoice.setDescriptionConcept(strValues[15].toString().trim());
						}
					
					}
					boolean fErrorIVA = false;
					StringBuilder sbErrorIVA = new StringBuilder();
					if(strValues[16] == null){									
						sbErrorIVA.append("(17) Posicion IVA requerida (Null) - Factura " + factura + "\n");
						System.out.println("(17) Posicion IVA requerida (Null) - Factura " + factura + "\n");
						fErrorIVA = true;
					}else{
						
						
						//System.out.println("IVA: " + strValues[16].toString());
						if(!strValues[16].toString().trim().equals("")){										
	                    	if(validaDatoRE(strValues[16].toString().trim(), RE_DECIMAL)) {				                    		
								invoice.setPorcentaje(Double.valueOf(strValues[16].toString().trim()));
								//catalogchange
	                    		
								String tasa=getTASA(String.valueOf(invoice.getPorcentaje()));
								System.out.println("tasaIVA:" + tasa);
	                    		if(!hashIvas.containsKey(Integer.parseInt(tasa))) {												
									sbErrorIVA.append("Descripcion de IVA no existe en BD - Factura " + factura + "\n");
									System.out.println("Descripcion de IVA no existe en BD - Factura " + factura + "\n");
									fErrorIVA = true;												
								} else {
									invoice.setDescriptionIVA(hashIvas.get(Integer.parseInt(tasa)));
									System.out.println("DescriptionIVA: " + hashIvas.get(Integer.parseInt(tasa)));
								}
							} else {											
								sbErrorIVA.append("(17) IVA incorrecto - Factura " + factura + "\n");
								System.out.println("(17) IVA incorrecto - Factura " + factura + "\n");
								fErrorIVA = true;											
							}
	                    }else{				                    	
	                    	sbErrorIVA.append("(17) IVA requerido - Factura " + factura + "\n");
	                    	System.out.println("(17) IVA requerido - Factura " + factura + "\n");
	                    	fErrorIVA = true;										
	                    }
					
					}
					if(strValues[17] != null){
						
						//System.out.println("tipoAddenda:" + strValues[17].toString());
						
						if(validaDatoRE(strValues[17].toString().trim(), RE_DECIMAL)){
							String strTipoAddenda = strValues[17].toString();
							//System.out.println("tipoAddendaClean: " + strTipoAddenda);
							if(strTipoAddenda.equals("1") || strTipoAddenda.equals("2") || strTipoAddenda.equals("3")){
								invoice.setBeneficiaryName("");
								invoice.setReceivingInstitution("");
								invoice.setAccountNumber("");
								invoice.setProviderNumber("");
								if(strValues[18] ==null){			                    				
	                    			sbError.append("(19) Posicion Email Proveedor requerida (Null)" + "\n");
	                    			System.out.println("(19) Posicion Email Proveedor requerida (Null)" + "\n");
	                    			fError = true;
	        						//throw new Exception("Email Proveedor no encontrado");
                    			}else{
                    				
                    				if(!strValues[18].toString().trim().equals("")){
                    					if(validaDatoRE(strValues[18].toString().trim(), RE_MAIL)){
                    						invoice.setEmail(strValues[18].toString().trim());
			                    			//System.out.println("email proveedor: " + strValues[18].toString().trim());
                    					}else{
                    						sbError.append("(19) Email Proveedor con estructura incorrecta" + "\n");
			                    			System.out.println("(19) Email Proveedor con estructura incorrecta" + "\n");
			                    			fError = true;
                    					}		                    					
                    				}else{			                    					
		                    			sbError.append("(19) Email Proveedor requerido" + "\n");
		                    			System.out.println("(19) Email Proveedor requerido" + "\n");
		                    			fError = true;
		        						//throw new Exception("Email Proveedor no encontrado");
                    				}
								
                    			}			                    						                    			
							}
							if(strTipoAddenda.equals("1")){
                    			invoice.setTipoAddenda(strTipoAddenda.trim());
                    			if(strValues[19] == null){			                    				
                        			sbError.append("(20) Posicion Codigo ISO Moneda requerida (Null)" + "\n");
                        			System.out.println("(20) Posicion Codigo ISO Moneda requerida (Null)" + "\n");
                        			fError = true;			            						
                    			}else{
                    				
                    				
                    				
                    				if(!strValues[19].toString().trim().equals("")){
                    					
                    					String code=strValues[19].toString().trim().toUpperCase();
                    					
                    					if(hashcodigoISO.containsKey(code)){
                    						invoice.setCodigoisomonedaLog(code);
		                        			//System.out.println("codigoISO Log: " + code);
                    					}else{
                    						sbError.append("(20) Codigo ISO Moneda no existe en BD" + "\n");
		                        			System.out.println("(20) Codigo ISO Moneda no existe en BD" + "\n");
		                        			fError = true;
                    					}
                    					//invoice.setCodigoisomonedaLog(strValues[19].toString().trim().toUpperCase());
	                        			//System.out.println("codigoISO Log: " + strValues[19].toString().trim().toUpperCase());
                    				}else{			                    					
	                        			sbError.append("(20) Codigo ISO Moneda requerido" + "\n");
	                        			System.out.println("(20) Codigo ISO Moneda requerido" + "\n");
	                        			fError = true;				            						
                    				}
								
                    			}

                    			if(strValues[20] == null){			                    				
                        			sbError.append("(21) Posicion Orden Compra requerida (Null)" + "\n");
                        			System.out.println("(21) Posicion Orden Compra requerida (Null)" + "\n");
                        			fError = true;
            						
                    			}else{
                    				if(strValues[20].toString().trim().equals("")){
                    					sbError.append("(21) Orden Compra requerida" + "\n");
	                        			System.out.println("(21) Orden Compra requerida" + "\n");
	                        			fError = true;
                    				}else{
                    					
		                    				String strOrdenCompra = strValues[20].toString();
		                    				if(!strOrdenCompra.equals("")){
			                        			invoice.setPurchaseOrder(strOrdenCompra);
			                        			//System.out.println("orden compra log: " + strOrdenCompra);
			                        		}else{				                        			
			                        			sbError.append("(21) Orden Compra requerida" + "\n");
			                        			System.out.println("(21) Orden Compra requerida" + "\n");
			                        			fError = true;
			            					
			                        		}
										
                    				}
                    				
                    			}
                    			
                    			if(strValues[21] == null){			                    				
                        			sbError.append("(22) Posicion Compra requerida (Null)" + "\n");
                        			System.out.println("(22) Posicion Compra requerida (Null)" + "\n");
                        			fError = true;
            						
                    			}else{
                    				if(strValues[21].toString().trim().equals("")){
                    					sbError.append("(22) Posicion Compra requerida" + "\n");
	                        			System.out.println("(22) Posicion Compra requerida" + "\n");
	                        			fError = true;
                    				}else{
                    					
		                    				String strPosicionCompra = strValues[21].toString();
		                    				if(!strPosicionCompra.equals("")){
			                        			invoice.setPosicioncompraLog(strPosicionCompra);
			                        			//System.out.println("posicion compra: " + strPosicionCompra);
			                        		}else{				                        			
			                        			sbError.append("(22) Posicion Compra requerida" + "\n");
			                        			System.out.println("(22) Posicion Compra requerida" + "\n");
			                        			fError = true;
			            						
			                        		}
										
                    				}
                    				
                    			}
                    			
                    		}else if(strTipoAddenda.equals("2")){
                    			invoice.setTipoAddenda(strTipoAddenda.trim());
                    			if(strValues[19] == null){			                    				
                        			sbError.append("(20) Posicion Codigo ISO Moneda requerida (Null)" + "\n");
                        			System.out.println("(20) Posicion Codigo ISO Moneda requerida (Null)" + "\n");     
                        			fError = true;
            						
                    			}else{
                    				
                    				if(!strValues[19].toString().trim().equals("")){
                    					//GOODONE
                    					String code=strValues[19].toString().trim().toUpperCase();
                    					
                    					if(hashcodigoISO.containsKey(code)){
                    						invoice.setCodigoisomonedaFin(code);
		                        			//System.out.println("codigoISO Fin: " + code);
                    					}else{
                    						sbError.append("(20) Codigo ISO Moneda no existe en BD" + "\n");
		                        			System.out.println("(20) Codigo ISO Moneda no existe en BD" + "\n");
		                        			fError = true;
                    					}				                    					
                    					//invoice.setCodigoisomonedaFin(strValues[19].toString().trim().toUpperCase());
	                        			System.out.println("codigoISO Fin: " + strValues[19].toString().trim().toUpperCase());
                    				}else{			                    					
	                        			sbError.append("(20) Codigo ISO Moneda no encontrado" + "\n");
	                        			System.out.println("(20) Codigo ISO Moneda no encontrado" + "\n");
	                        			fError = true;
	            						
                    				}
									
                    			}

                    			if(strValues[22] ==null ){			                    				
                        			sbError.append("(23) Posicion Cuenta Contable requerida (Null)" + "\n");
                        			System.out.println("(23) Posicion Cuenta Contable requerida (Null)" + "\n");
                        			fError = true;
            						
                    			}else{
                    				if(strValues[22].toString().trim().equals("")){
                    					sbError.append("(23) Cuenta Contable requerida" + "\n");
	                        			System.out.println("(23) Cuenta Contable requerida" + "\n");
	                        			fError = true;
                    				}else{
                    					
		                    				String strCuentaContableFin = strValues[22].toString();
		                    				if(!strCuentaContableFin.equals("")){
			                        			invoice.setCuentacontableFin(strCuentaContableFin);
			                        			//System.out.println("cuenta contable Fin: " + strCuentaContableFin);
			                        		}else{				                        			
			                        			sbError.append("(23) Cuenta Contable requerida" + "\n");
			                        			System.out.println("(23) Cuenta Contable requerida" + "\n");
			                        			fError = true;
			            						
			                        		}
										
                    				}
                    				
                    			}
                    			if(strValues[23] ==null ){			                    				
                        			sbError.append("(24) Posicion Centro costos requerido (Null)" + "\n");
                        			System.out.println("(24) Posicion Centro costos requerido (Null)" + "\n");
                        			fError = true;
            						
                    			}else{
                    				if(strValues[23].toString().trim().equals("")){
                    					sbError.append("(24) Centro costos requerido" + "\n");
	                        			System.out.println("(24) Centro costos requerido" + "\n");
	                        			fError = true;

                    				}else{
                    					
		                    				if(!strValues[23].toString().trim().equals("")){
		                    					//System.out.println("centro costos: " + strValues[23].toString());
		                    					String strCentroCostosFin = strValues[23].toString();
		                    					if(!strCentroCostosFin.equals(invoice.getCostCenter())){			                    						
				                        			sbError.append("Centros costos diferentes" + "\n");
				                        			System.out.println("Centros costos diferentes" + "\n");
				                        			fError = true;
				            						
		                    					}				                        							                        			
			                        		}else{				                        			
			                        			sbError.append("Centro costos requerido" + "\n");
			                        			System.out.println("Centro costos requerido" + "\n");
			                        			fError = true;
			            						
			                        		}
										
                    				}
                    				
                    			}
                    			
                        	}else if(strTipoAddenda.equals("3")){
                        		invoice.setTipoAddenda("3");
                        		if(strValues[19] == null){			                    				
                        			sbError.append("(20) Posicion Codigo ISO Moneda requerida (Null)" + "\n");
                        			System.out.println("(20) Posicion Codigo ISO Moneda requerida (Null)" + "\n");
                        			fError = true;
            						
                    			}else{
                    				
                    				if(!strValues[19].toString().trim().equals("")){
                    					
                    					String code=strValues[19].toString().trim().toUpperCase();
                    					
                    					if(hashcodigoISO.containsKey(code)){
                    						invoice.setCodigoisomonedaArr(code);
		                        			//System.out.println("codigoISO Arr: " + code);
                    					}else{
                    						sbError.append("(20) Codigo ISO Moneda no existe en BD" + "\n");
		                        			System.out.println("(20) Codigo ISO Moneda no existe en BD" + "\n");
		                        			fError = true;
                    					}
                    					//invoice.setCodigoisomonedaArr(strValues[19].toString().trim().toUpperCase());
	                        			//System.out.println("codigoISO Arr: " + strValues[19].toString().trim().toUpperCase());
                    				}else{			                    					
	                        			sbError.append("(20) Codigo ISO Moneda no encontrado" + "\n");
	                        			System.out.println("(20) Codigo ISO Moneda no encontrado" + "\n");
	                        			fError = true;
	            					
                    				}
								
                    			}

                        		if(strValues[24] ==null ){			                        			
                        			sbError.append("(25) Posicion Numero de Contrato requerida (Null)" + "\n");
                        			System.out.println("(25) Posicion Numero de Contrato requerida (Null)" + "\n");
                        			fError = true;
            						
                        		}else{
                        			if(strValues[24].toString().trim().equals("")){
                        				sbError.append("(25) Numero de Contrato requerido" + "\n");
	                        			System.out.println("(25) Numero de Contrato requerido" + "\n");
	                        			fError = true;
                        			}else{
                        				
		                        			String strNumeroContratoArr = strValues[24].toString();
		                        			if(!strNumeroContratoArr.equals("")){
			                        			invoice.setNumerocontratoArr(strNumeroContratoArr);
			                        			//System.out.println("numero contrato Arr: " + strNumeroContratoArr);
			                        		}else{				                        			
			                        			sbError.append("(25) Numero de Contrato no encontrado" + "\n");
			                        			System.out.println("(25) Numero de Contrato no encontrado" + "\n");
			                        			fError = true;
			            					
			                        		}
										
                        			}
                        			
                        		}
                    			if(strValues[25] ==null){			                    				
                        			sbError.append("(26) Posicion Fecha de vencimiento requerida (Null)" + "\n");
                        			System.out.println("(26) Posicion Fecha de vencimiento requerida (Null)" + "\n");
                        			fError = true;
            						
                    			}else{
                    				if(strValues[25].toString().trim().equals("")){
                    					sbError.append("(26) Fecha de vencimiento requerida" + "\n");
	                        			System.out.println("(26) Fecha de vencimiento requerida" + "\n");
	                        			fError = true;
                    				}else{
                    					
		                    				String strFechaVencimientoArr = strValues[25].toString(); 
		                    				if(!strFechaVencimientoArr.equals("")){
			                        			invoice.setFechavencimientoArr(strFechaVencimientoArr);
			                        			//System.out.println("fecha vencimiento Arr:" + strFechaVencimientoArr);
			                        		}else{				                        			
			                        			sbError.append("(26) Fecha de vencimiento requerida" + "\n");
			                        			System.out.println("(26) Fecha de vencimiento requerida" + "\n");
			                        			fError = true;
			            					
			                        		}
										
                    				}
                    				
                    			}
                    			
                        	}else if(strTipoAddenda.equals("0")){
                        		invoice.setEmail("");
								invoice.setPurchaseOrder("");
	                    		invoice.setTipoAddenda(strTipoAddenda.trim());
	                    		//System.out.println("tipoaddenda 0:" + invoice.getTipoAddenda());
		                    	//if(strValues[17] != null && strTipoAddenda.equals("0")){
	                    			if(strValues[26] ==null){				                    				
	                    				invoice.setBeneficiaryName("");
	                    			}else{
	                    				
	                    				invoice.setBeneficiaryName(strValues[26].toString().trim());
										
	                    			}
		                    		if(strValues[27] ==null ){					                    			
		                    			invoice.setReceivingInstitution("");
		                    		}else{
		                    			
		                    			invoice.setReceivingInstitution(strValues[27].toString().trim());
									
		                    		}
			                    	
		                    		if(strValues[28] ==null){					                    			
		                    			invoice.setAccountNumber("");
		                    		}else{
		                    			if(strValues[28].toString().trim().equals("")){
		                    				invoice.setAccountNumber(strValues[28].toString().trim());
		                    			}else{
		                    				
												invoice.setAccountNumber(strValues[28].toString().trim());
											
		                    			}
		                    			
		                    		}
			                    	if(strValues[29] ==null ){					                    		
			                    		invoice.setProviderNumber("");
			                    	}else{
			                    		if(strValues[29].toString().trim().equals("")){
			                    			invoice.setProviderNumber(strValues[29].toString().trim());
			                    		}else{
			                    			
												invoice.setProviderNumber(strValues[29].toString().trim());
											
			                    		}
			                    		
			                    	}
			                    	
		                    	//}
	                    	}else{				                    		
	                        	sbError.append("(18) Tipo de Addenda incorrecto - factura " + factura + "\n");
	                        	System.out.println("(18) Tipo de Addenda incorrecto - factura " + factura + "\n");
	                        	fError = true;
	    						
	                    	}
						}else{
							if(strValues[17].toString().trim().equals("")){
								invoice.setEmail("");
								invoice.setPurchaseOrder("");
								invoice.setTipoAddenda("0");
	                    		//System.out.println("tipoaddenda 0:" + invoice.getTipoAddenda());
		                    	//if(strValues[17] != null && strTipoAddenda.equals("0")){
	                    			if(strValues[26] ==null){				                    				
	                    				invoice.setBeneficiaryName("");
	                    			}else{
	                    				invoice.setBeneficiaryName(strValues[26].toString().trim());
	                    			}
		                    		if(strValues[27] ==null ){					                    			
		                    			invoice.setReceivingInstitution("");
		                    		}else{
		                    			
		                    			invoice.setReceivingInstitution(strValues[27].toString().trim());
										
		                    		}
			                    	
		                    		if(strValues[28] ==null){					                    			
		                    			invoice.setAccountNumber("");
		                    		}else{
		                    			if(strValues[28].toString().trim().equals("")){
		                    				invoice.setAccountNumber(strValues[28].toString().trim());
		                    			}else{
		                    				
												invoice.setAccountNumber(strValues[28].toString().trim());
											
		                    			}
		                    			
		                    		}
			                    	if(strValues[29] ==null ){					                    		
			                    		invoice.setProviderNumber("");
			                    	}else{
			                    		if(strValues[29].toString().trim().equals("")){
			                    			invoice.setProviderNumber(strValues[29].toString().trim());
			                    		}else{
			                    			
												invoice.setProviderNumber(strValues[29].toString().trim());
											
			                    		}
			                    		
			                    	}

							}else{											
	                        	sbError.append("(18) Tipo de Addenda incorrecto - factura " + factura + "\n");
	                        	System.out.println("(18) Tipo de Addenda incorrecto - factura " + factura + "\n");
	                        	fError = true;					    						
							}									
						}	
						
					}else{
						invoice.setEmail("");
						invoice.setPurchaseOrder("");
						invoice.setTipoAddenda("0");
                		//System.out.println("tipoaddenda 0:" + invoice.getTipoAddenda());
                		if(strValues[26] ==null){	                    				
                			invoice.setBeneficiaryName("");
            			}else{
            				invoice.setBeneficiaryName(strValues[26].toString().trim());
            			}
                		if(strValues[27] ==null ){		                    			
                			invoice.setReceivingInstitution("");
                		}else{
                			invoice.setReceivingInstitution(strValues[27].toString().trim());
                		}
                    	
                		if(strValues[28] ==null){		                    			
                			invoice.setAccountNumber("");
                		}else{
                			if(strValues[28].toString().trim().equals("")){
                				invoice.setAccountNumber(strValues[28].toString().trim());
                			}else{
                				
									invoice.setAccountNumber(strValues[28].toString().trim());
								
                			}
                			
                		}
                    	if(strValues[29] ==null ){		                    		
                    		invoice.setProviderNumber("");
                    	}else{
                    		if(strValues[29].toString().trim().equals("")){
                    			invoice.setProviderNumber(strValues[29].toString().trim());
                    		}else{
                    			
									invoice.setProviderNumber(strValues[29].toString().trim());
							
                    		}
                    		
                    	}
					}
					
					//Motivo de Descuento
					if(strValues[30].toString().trim().equals("")){
						invoice.setMotivoDescuento("");
						
						//Descuento
						if(strValues[31] == null){
							invoice.setDescuento(0);
						}else{
							if(strValues[31].toString().trim().equals("")){										
								invoice.setDescuento(0);
							}else{
								if(validaDatoRE(strValues[31].toString().trim(), RE_DECIMAL_NEGATIVO)){	
									
									if(Double.parseDouble(strValues[31].toString().trim()) > 0){
										fError = true;
					                	sbError.append("(31) " + "Favor de informar el Motivo de descuento correspondiente al Descuento " + " - factura " + factura + "\n");
									}else{
										invoice.setDescuento(Double.parseDouble(strValues[31].toString().trim()));
									}
																																	
				                 }else{
				                	 fError = true;
				                	 sbError.append("(32) " + "Descuento con formato incorrecto " + " - factura " + factura + "\n");												 
				                 }
							}
						}
						
					}else{										
						if(strValues[30].toString().trim() != null && strValues[30].toString().trim().length() > 0 && strValues[30].toString().trim().length() <= 1500){											
							invoice.setMotivoDescuento(strValues[30].toString().trim());
							
							//Descuento
							if(strValues[31] == null){
								invoice.setDescuento(0);
								fError = true;
			                	sbError.append("(32) " + "Favor de informar el Descuento correspondiente al Motido de descuento " + " - factura " + factura + "\n");
							}else{
								if(strValues[31].toString().trim().equals("")){										
									invoice.setDescuento(0);
									fError = true;
				                	sbError.append("(32) " + "Favor de informar el Descuento correspondiente al Motido de descuento " + " - factura " + factura + "\n");
								}else{
									if(validaDatoRE(strValues[31].toString().trim(), RE_DECIMAL_NEGATIVO)){											
										
										if(Double.parseDouble(strValues[31].toString().trim()) > 0){
											invoice.setDescuento(Double.parseDouble(strValues[31].toString().trim()));
										}else{
											fError = true;
						                	sbError.append("(32) " + "Favor de informar el Descuento correspondiente al Motido de descuento " + " - factura " + factura + "\n");
										}
					                 }else{
					                	 fError = true;
					                	 sbError.append("(32) " + "Descuento con formato incorrecto " + " - factura " + factura + "\n");												 
					                 }
								}
							}							
							
							
		                 }else{						                	 
							 sbError.append("(31) " + "Motivo de descuento con formato incorrecto " + " - factura " + factura + "\n");											 
							 fError = true;
		                 }
					}
					
					
					
					//Leer los valores del Cliente cuando el RFC sea generico y el IdExtranjero venga vacio
					
					if(invoice.getRfc() != null){						
						if(invoice.getRfc().trim().toUpperCase().equals("XEXX010101000") || invoice.getRfc().trim().toUpperCase().equals("XAXX010101000")){
							if(invoice.getIdExtranjero() == null || invoice.getIdExtranjero().trim().equals("")){
								
								//Nombre de cliente
								if(strValues[32] == null){									
									sbError.append("(33) Posicion Nombre de Cliente requerida (Null) - Factura " + factura  + "\n");
			                        System.out.println("(33) Posicion Nombre de Cliente requerida (Null) - Factura " + factura  + "\n");
			                        fError = true;																
								}else{
									
									if(strValues[32].toString().trim().equals("")){
										sbError.append("(33) Nombre de Cliente requerido - Factura " + factura  + "\n");
			                            System.out.println("(33) Nombre de Cliente requerido - Factura " + factura  + "\n");
			                            fError = true;
									}else{
										if(validaDatoRELongitud(strValues[32].toString(), RE_CHAR_ALL, 250)){
											invoice.setName(strValues[32].toString().trim());
			                            	//System.out.println("Metodo de pago: " + strValues[7].toString());
			                            }else{                    	
			                            	
			                                sbError.append("(33) Nombre de Cliente con formato incorrecto - Factura " + factura  + "\n");
			                                System.out.println("(33) Nombre de Cliente con formato incorrecto - Factura " + factura  + "\n");
			                                fError = true;
			                                
			                            }
									}										
								}
								//Direccion de Cliente - Calle
								if(strValues[33] == null){									
									sbError.append("(34) Posicion Nombre de Calle requerida (Null) - Factura " + factura  + "\n");
			                        System.out.println("(34) Posicion Nombre de Calle requerida (Null) - Factura " + factura  + "\n");
			                        fError = true;																
								}else{
									
									if(strValues[33].toString().trim().equals("")){
										sbError.append("(34) Nombre de Calle requerido - Factura " + factura  + "\n");
			                            System.out.println("(34) Nombre de Calle requerido - Factura " + factura  + "\n");
			                            fError = true;
									}else{
										if(validaDatoRELongitud(strValues[33].toString(), RE_CHAR_ALL, 250)){
											invoice.setCalle(strValues[33].toString().trim());
			                            	//System.out.println("Metodo de pago: " + strValues[7].toString());
			                            }else{                    	
			                            	
			                                sbError.append("(34) Nombre de Calle con formato incorrecto - Factura " + factura  + "\n");
			                                System.out.println("(34) Nombre de Calle con formato incorrecto - Factura " + factura  + "\n");
			                                fError = true;
			                                
			                            }
									}										
								}
								//Direccion de Cliente - Num Exterior
								if(strValues[34] == null){									
									sbError.append("(35) Posicion Num Exterior requerida (Null) - Factura " + factura  + "\n");
			                        System.out.println("(35) Posicion Num Exterior requerida (Null) - Factura " + factura  + "\n");
			                        fError = true;																
								}else{
									
									if(strValues[34].toString().trim().equals("")){
										sbError.append("(35) Num Exterior requerido - Factura " + factura  + "\n");
			                            System.out.println("(35) Num Exterior requerido - Factura " + factura  + "\n");
			                            fError = true;
									}else{
										if(validaDatoRELongitud(strValues[34].toString(), RE_CHAR_ALL, 250)){
											invoice.setExterior(strValues[34].toString().trim());
			                            	//System.out.println("Metodo de pago: " + strValues[7].toString());
			                            }else{                    	
			                            	
			                                sbError.append("(35) Num Exterior con formato incorrecto - Factura " + factura  + "\n");
			                                System.out.println("(35) Num Exterior con formato incorrecto - Factura " + factura  + "\n");
			                                fError = true;
			                                
			                            }
									}										
								}
								//Direccion de Cliente - Num Interior
								if(strValues[35] == null){									
									sbError.append("(36) Posicion Num Interior requerida (Null) - Factura " + factura  + "\n");
			                        System.out.println("(36) Posicion Num Interior requerida (Null) - Factura " + factura  + "\n");
			                        fError = true;																
								}else{
									
									if(strValues[35].toString().trim().equals("")){
										sbError.append("(36) Num Interior requerido - Factura " + factura  + "\n");
			                            System.out.println("(36) Num Interior requerido - Factura " + factura  + "\n");
			                            fError = true;
									}else{
										if(validaDatoRELongitud(strValues[35].toString(), RE_CHAR_ALL, 250)){
											invoice.setInterior(strValues[35].toString().trim());
			                            	//System.out.println("Metodo de pago: " + strValues[7].toString());
			                            }else{                    	
			                            	
			                                sbError.append("(36) Num Interior con formato incorrecto - Factura " + factura  + "\n");
			                                System.out.println("(36) Num Interior con formato incorrecto - Factura " + factura  + "\n");
			                                fError = true;
			                                
			                            }
									}										
								}
								//Direccion de Cliente - Colonia
								if(strValues[36] == null){									
									sbError.append("(37) Posicion Colonia requerida (Null) - Factura " + factura  + "\n");
			                        System.out.println("(37) Posicion Colonia requerida (Null) - Factura " + factura  + "\n");
			                        fError = true;																
								}else{
									
									if(strValues[36].toString().trim().equals("")){
										sbError.append("(37) Colonia requerido - Factura " + factura  + "\n");
			                            System.out.println("(37) Colonia requerido - Factura " + factura  + "\n");
			                            fError = true;
									}else{
										if(validaDatoRELongitud(strValues[36].toString(), RE_CHAR_ALL, 250)){
											invoice.setColonia(strValues[36].toString().trim());
			                            	//System.out.println("Metodo de pago: " + strValues[7].toString());
			                            }else{                    	
			                            	
			                                sbError.append("(37) Colonia con formato incorrecto - Factura " + factura  + "\n");
			                                System.out.println("(37) Colonia con formato incorrecto - Factura " + factura  + "\n");
			                                fError = true;
			                                
			                            }
									}										
								}
								//Direccion de Cliente - Codigo Postal
								if(strValues[37] == null){									
									sbError.append("(38) Posicion Codigo Postal requerida (Null) - Factura " + factura  + "\n");
			                        System.out.println("(38) Posicion Codigo Postal requerida (Null) - Factura " + factura  + "\n");
			                        fError = true;																
								}else{
									
									if(strValues[37].toString().trim().equals("")){
										sbError.append("(38) Codigo Postal requerido - Factura " + factura  + "\n");
			                            System.out.println("(38) Codigo Postal requerido - Factura " + factura  + "\n");
			                            fError = true;
									}else{
										if(validaDatoRELongitud(strValues[37].toString(), RE_NUMBER, 5)){
											invoice.setCodigoPostal(strValues[37].toString().trim());
			                            	//System.out.println("Metodo de pago: " + strValues[7].toString());
			                            }else{                    	
			                            	
			                                sbError.append("(38) Codigo Postal con formato incorrecto - Factura " + factura  + "\n");
			                                System.out.println("(38) Codigo Postal con formato incorrecto - Factura " + factura  + "\n");
			                                fError = true;
			                                
			                            }
									}										
								}
								//Direccion de Cliente - Localidad
								if(strValues[38] == null){									
									sbError.append("(39) Posicion Localidad requerida (Null) - Factura " + factura  + "\n");
			                        System.out.println("(39) Posicion Localidad requerida (Null) - Factura " + factura  + "\n");
			                        fError = true;																
								}else{
									
									if(strValues[38].toString().trim().equals("")){
										sbError.append("(39) Localidad requerido - Factura " + factura  + "\n");
			                            System.out.println("(39) Localidad requerido - Factura " + factura  + "\n");
			                            fError = true;
									}else{
										if(validaDatoRELongitud(strValues[38].toString(), RE_CHAR_ALL, 250)){
											invoice.setLocalidad(strValues[38].toString().trim());
			                            	//System.out.println("Metodo de pago: " + strValues[7].toString());
			                            }else{                    	
			                            	
			                                sbError.append("(39) Localidad con formato incorrecto - Factura " + factura  + "\n");
			                                System.out.println("(39) Localidad con formato incorrecto - Factura " + factura  + "\n");
			                                fError = true;
			                                
			                            }
									}										
								}
								//Direccion de Cliente - Municipio
								if(strValues[39] == null){									
									sbError.append("(40) Posicion Municipio requerida (Null) - Factura " + factura  + "\n");
			                        System.out.println("(40) Posicion Municipio requerida (Null) - Factura " + factura  + "\n");
			                        fError = true;																
								}else{
									
									if(strValues[39].toString().trim().equals("")){
										sbError.append("(40) Municipio requerido - Factura " + factura  + "\n");
			                            System.out.println("(40) Municipio requerido - Factura " + factura  + "\n");
			                            fError = true;
									}else{
										if(validaDatoRELongitud(strValues[39].toString(), RE_CHAR_ALL, 250)){
											invoice.setMunicipio(strValues[39].toString().trim());
			                            	//System.out.println("Metodo de pago: " + strValues[7].toString());
			                            }else{                    	
			                            	
			                                sbError.append("(40) Municipio con formato incorrecto - Factura " + factura  + "\n");
			                                System.out.println("(40) Municipio con formato incorrecto - Factura " + factura  + "\n");
			                                fError = true;
			                                
			                            }
									}										
								}
								//Direccion de Cliente - Estado
								if(strValues[40] == null){									
									sbError.append("(41) Posicion Estado requerida (Null) - Factura " + factura  + "\n");
			                        System.out.println("(41) Posicion Estado requerida (Null) - Factura " + factura  + "\n");
			                        fError = true;																
								}else{
									
									if(strValues[40].toString().trim().equals("")){
										sbError.append("(41) Estado requerido - Factura " + factura  + "\n");
			                            System.out.println("(41) Estado requerido - Factura " + factura  + "\n");
			                            fError = true;
									}else{
										if(validaDatoRELongitud(strValues[40].toString(), RE_CHAR_ALL, 250)){
											invoice.setEstado(strValues[40].toString().trim());
			                            	//System.out.println("Metodo de pago: " + strValues[7].toString());
			                            }else{                    	
			                            	
			                                sbError.append("(41) Estado con formato incorrecto - Factura " + factura  + "\n");
			                                System.out.println("(41) Estado con formato incorrecto - Factura " + factura  + "\n");
			                                fError = true;
			                                
			                            }
									}										
								}
								//Direccion de Cliente - País
								if(strValues[41] == null){									
									sbError.append("(42) Posicion País requerida (Null) - Factura " + factura  + "\n");
			                        System.out.println("(42) Posicion País requerida (Null) - Factura " + factura  + "\n");
			                        fError = true;																
								}else{
									
									if(strValues[41].toString().trim().equals("")){
										sbError.append("(42) País requerido - Factura " + factura  + "\n");
			                            System.out.println("(42) País requerido - Factura " + factura  + "\n");
			                            fError = true;
									}else{
										if(validaDatoRELongitud(strValues[41].toString(), RE_CHAR_ALL, 250)){
											invoice.setPais(strValues[41].toString().trim());
			                            	//System.out.println("Metodo de pago: " + strValues[7].toString());
			                            }else{                    	
			                            	
			                                sbError.append("(42) País con formato incorrecto - Factura " + factura  + "\n");
			                                System.out.println("(42) País con formato incorrecto - Factura " + factura  + "\n");
			                                fError = true;
			                                
			                            }
									}										
								}
									
								
							}
						}
					}
					
					
					
					//Procesar Conceptos
					//Contar posiciones de concepto
					int posicionConcepto = 0;
					int posicion = 42;
					
					int contadorConceptos = 1;
					boolean fPermisoVector = true;
					boolean fFinFactura = false;
					String strItemConcepto = "";
					for(int index=42; index<strValues.length-1; index++ ){
						
						if(strValues[index] == null){
							System.out.println("bloqueConceptos: nulo posicion: " + posicion);
							strItemConcepto = "";
						}else{
							System.out.println("bloqueConceptos: " + strValues[index] + " posicion: " + posicion);
							strItemConcepto = strValues[index].toString().trim();
						}
						
						
						if(posicionConcepto == 5){
							posicionConcepto = 1;
						}else{
							posicionConcepto += 1;
						}
						
						if(posicionConcepto == 1){
							
							if(strItemConcepto.equals("")){
								fPermisoVector = false;
								sbError.append("(" + (posicion+1) +") " + "Cantidad requerida " + " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
								//System.out.println("(" + (posicion+1) +") " + "Cantidad requerida " + " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
							}else{
									//System.out.println("cantidad:" + strItemConcepto);
									if(validaDatoRE(strItemConcepto, RE_DECIMAL_QTY)) {
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
								if(validaDatoRELongitud(strItemConcepto, RE_CHAR_ALL, 250)){
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
								//if(validaDatoRELongitud(strItemConcepto, RE_CHAR, 1500)){
								if(strItemConcepto != null && strItemConcepto.trim().length() > 0 && strItemConcepto.length() <= 1500){
									//System.out.println("Concepto Expedicion: " + strItemConcepto + " de la factura " + factura);
									if(fPermisoVector)
										vectorDesc.add(strItemConcepto.toUpperCase());
				                 }else{
				                	 fPermisoVector = false;
									 sbError.append("(" + (posicion+1) +") " + "Concepto Expedicion con formato incorrecto " + " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
									 System.out.println("(" + (posicion+1) +") " + "Concepto Expedicion con formato incorrecto " + " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
				                 }
							}
							
							
						}else if(posicionConcepto == 4){
							
							if(strItemConcepto.equals("")){
								fPermisoVector = false;
								sbError.append("(" + (posicion+1) +") " + "Precio Unitario requerido " + " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
								//System.out.println("(" + (posicion+1) +") " + "Precio Unitario requerido " + " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
							}else{
									//System.out.println("precioUnitario:" + strItemConcepto);
									if(validaDatoRE(strItemConcepto, RE_DECIMAL_NEGATIVO)){											
										//System.out.println("Precio Unitario: " + strItemConcepto + " de la factura " + factura);
										if(fPermisoVector)
											vectorPrecioUnitario.add(Double.parseDouble(strItemConcepto));
					                 }else{
					                	 fPermisoVector = false;
					                	 sbError.append("(" + (posicion+1) +") " + "Precio Unitario con formato incorrecto " + " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
											System.out.println("(" + (posicion+1) +") " + "Precio Unitario con formato incorrecto " + " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
					                 }
								
							}
							
							
						}else{
							
							if(strItemConcepto.equals("")){
								//System.out.println("APLICA IVA: " + strItemConcepto + " de la factura " + factura);
								if(fPermisoVector)
									vectorAplicaIVA.add(strItemConcepto);
							}else{
								
									if(validaDatoRE(strItemConcepto, RE_NUMBER)){
										
										if(strItemConcepto.equals("1")){
											//System.out.println("APLICA IVA: " + strItemConcepto + " de la factura " + factura);
											if(fPermisoVector)
												vectorAplicaIVA.add(strItemConcepto);
										}else{
											fPermisoVector = false;
											sbError.append("(" + (posicion+1) +") " + "APLICA IVA con formato incorrecto " + " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
											System.out.println("(" + (posicion+1) +") " + "APLICA IVA con formato incorrecto " + " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
										}
									}else{
										fPermisoVector = false;
										sbError.append("(" + (posicion+1) +") " + "APLICA IVA con formato incorrecto " + " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
										System.out.println("(" + (posicion+1) +") " + "APLICA IVA con formato incorrecto " + " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
									}
								
							}							
																							
							contadorConceptos+=1;
						}
					
						
						posicion +=1;
					}
					
					//System.out.println("contadorConceptos: " + contadorConceptos);
					//System.out.println("posicionConcepto: " + posicionConcepto);
					boolean fAplicaIVA = false;
					if(posicionConcepto != 5 && posicionConcepto != 4){
						sbError.append("El Concepto " + contadorConceptos + ", está incompleto" + " en la factura " + factura + "\n");
						System.out.println("El Concepto " + contadorConceptos + ", está incompleto" + " en la factura " + factura + "\n");
						fError = true;
					}else{
						if(fPermisoVector){
							if(posicionConcepto == 4){
								vectorAplicaIVA.add("");
							}
							List<ElementsInvoice> elementosIn = new ArrayList<ElementsInvoice>();
						    Vector<Double> vectorIVA = new Vector<Double>(); 
					        if(vectorCantidad != null && vectorUM != null && vectorDesc != null && vectorPrecioUnitario != null && vectorAplicaIVA != null && 
					        		vectorCantidad.size() == vectorUM.size() && vectorUM.size() == vectorDesc.size() &&
					                vectorDesc.size() == vectorPrecioUnitario.size() && vectorPrecioUnitario.size() == vectorAplicaIVA.size()) {
					        	for(int v=0; v<vectorCantidad.size(); v++) {
					        		ElementsInvoice ei = new ElementsInvoice();
					        		ei.setQuantity(Double.valueOf(vectorCantidad.get(v)));
					        		ei.setUnitMeasure(vectorUM.get(v));
					        		ei.setDescription(vectorDesc.get(v));
					        		ei.setUnitPrice(vectorPrecioUnitario.get(v));
					        		
					        		ei.setAmount(ei.getQuantity() * ei.getUnitPrice());
					        										        		
					        		if(vectorAplicaIVA.get(v).trim().equals("1")){
					        			//No Aplica IVA
					        			vectorIVA.add(0.0);
					        		}else{
					        			//Aplica IVA
					        			fAplicaIVA = true;
					        			Double ivaItem = 0.0;
					        			ivaItem = ei.getQuantity() * ei.getUnitPrice() * (invoice.getPorcentaje()/100);
					        			vectorIVA.add(ivaItem);
					        			invoice.setSiAplicaIva(true);
					        		}							     
					        		
					        		elementosIn.add(ei);
					        	}
							
					        	if(fErrorIVA && fAplicaIVA)
					        		sbError.append(sbErrorIVA.toString());
					        		
					        	Double subtotal = 0.0;
					        	Double iva = 0.0;
					        	for(int iSub=0; iSub<elementosIn.size(); iSub++){
					        		subtotal += elementosIn.get(iSub).getAmount();
					        		iva += vectorIVA.get(iSub);
					        	}
					        	
					        	//Aplicar Descuento
					        	Double porcentajeDescuento = invoice.getDescuento()*(invoice.getPorcentaje()/100);

					        	if(iva -porcentajeDescuento < 0){
					        		iva = 0.0;
					        	}else {
					        		iva = iva - porcentajeDescuento;
					        	}
					        	System.out.println("Este es el subtotal: " + subtotal);
					        	Double Total = 0.0;
					        	
					        	Total = subtotal + iva;
					        	invoice.setSubTotal(subtotal);
					        	invoice.setIva(iva);
					        	invoice.setTotal(Total - invoice.getDescuento());
					        	invoice.setElements(elementosIn);
					        	//System.out.println("subtotal: " + subtotal);
					        	//System.out.println("iva: " + iva);
					        	//System.out.println("total: " + Total);
					        }
						}else{
							fError = true;
							System.out.println("fPermisoVector false (Error en campos de conceptos)");
						}
					}
										
					
					if(fError){
						facturasError+=1;								
						//System.out.println("sbError: " + sbError.toString());
						sb.append("Factura: " + factura + " -- Lista de Errores: " + "\n" + sbError.toString() + "\n");								
						System.out.println("fError EmisionMasivaFacturas: " + sb.toString());							
						invoice.setSbError(sbError);
						listIn.add(invoice);
					}else{								
						
						try{
							System.out.println("AntesfacturaOK - " + factura);
							/*System.out.println("fserie: " + invoice.getSerie() + "\t" +
							"fformaPago: " + invoice.getFormaPago() + "\t" +
							"fsubtotal: " + invoice.getSubTotal() + "\t" +
							"ftotal: " + invoice.getTotal() + "\t" +
							"ftipoComprobante: " + invoice.getTipoFormato() + "\t" +
							"fmetodoPago: " + invoice.getMetodoPago() + "\t" +
							"flugarExpedicion: " + invoice.getLugarExpedicion() + "\t" +
							"fnumCtaPago: " + invoice.getNumCtaPago() + "\t" +										
							"emisorRFC: " + fiscalEntity.getTaxID() + "\t" +
							"emisorNombre: " + fiscalEntity.getFiscalName() + "\t" +										
							"emisorCalle: " + fiscalEntity.getAddress().getStreet().toUpperCase() + "\t" +										
							"emisornoExterior: " + fiscalEntity.getAddress().getExternalNumber() + "\t" +									
							"emisornoInterior: " + fiscalEntity.getAddress().getInternalNumber() + "\t" +										
							"emisorcolonia: " + fiscalEntity.getAddress().getNeighborhood() + "\t" +										
							"emisorreferencia: " + fiscalEntity.getAddress().getReference() + "\t" +										
							"emisormunicipio: " + fiscalEntity.getAddress().getRegion() + "\t" +										
							"emisorestado: " + fiscalEntity.getAddress().getState().getName() + "\t" +										
							"emisorpais: " + fiscalEntity.getAddress().getState().getCountry().getName() + "\t" +										
							"emisorcodigoPostal: " + fiscalEntity.getAddress().getZipCode() + "\t" +																				
							"regimenfiscal: " + invoice.getRegimenFiscal() + "\t" +										
							"recptorRFC: " + invoice.getRfc() + "\t" +										
							"recptorNombre: " + invoice.getName() + "\t" +										
							"recptorcalle: " + invoice.getCalle() + "\t" +										
							"recptornoExterior: " + invoice.getExterior() + "\t" +										
							"recptornoInterior: " + invoice.getInterior() + "\t" +										
							"recptorColonia: " + invoice.getColonia() + "\t" +										
							"recptorlocalidad: " + invoice.getLocalidad() + "\t" +										
							"recptorreferencia: " + invoice.getReferencia() + "\t" +										
							"recptormunicipio: " + invoice.getMunicipio() + "\t" +										
							"recptorestado: " + invoice.getEstado() + "\t" +										
							"recptorpais: " + invoice.getPais() + "\t" +									
							"recptorcodigoPostal: " + invoice.getCodigoPostal() + "\t" +
							"iva: " + invoice.getIva() + "\t" +										
							"getBeneficiaryName: " + invoice.getBeneficiaryName() + "\t" +
							"getAccountNumber: " + invoice.getAccountNumber() + "\t" +
							"getProviderNumber: " + invoice.getProviderNumber() + "\t" +
							"getPurchaseOrder: " + invoice.getPurchaseOrder() + "\t" +
							"getReceivingInstitution: " + invoice.getReceivingInstitution() + "\t" +
							"getEmail: " + invoice.getEmail() +  "\t" +
							"feCalle: " + fiscalEntity.getAddress().getStreet() + "\t" +
							"feNExterior: " + fiscalEntity.getAddress().getExternalNumber() + "\t" +
							"feIExterior: " + fiscalEntity.getAddress().getInternalNumber() + "\t" +
							"feIColonia: " + fiscalEntity.getAddress().getNeighborhood() + "\t" +
							"feIRef: " + fiscalEntity.getAddress().getReference() + "\t" +
							"feIRegion: " + fiscalEntity.getAddress().getRegion() + "\t" +
							"feIEstado: " + fiscalEntity.getAddress().getState().getName() + "\t" +
							"feIPais: " + fiscalEntity.getAddress().getState().getCountry().getName() + "\t" +
							"feIZipCod: " + fiscalEntity.getAddress().getZipCode() + "\t" +
							"\n");*/
							/*for(int iSub=0; iSub<vectorCantidad.size(); iSub++){
				        		System.out.println("conceptocantidad: " + vectorCantidad.get(iSub) + " um: " + vectorUM.get(iSub) + " descripcion: " + vectorDesc.get(iSub) + " preciounitario: " + vectorPrecioUnitario.get(iSub) + " aplicaiva: " + vectorAplicaIVA.get(iSub) + "\n");
				        		
				        	}*/ //all fields debug
							System.out.println("Antes de crearFac");
							
							if(!invoice.getSiAplicaIva())								
								invoice.setDescriptionIVA("EXENTO");
							
							crearFactura(factura);
							facturasOK+=1;									
							System.out.println("facturaOK - " + factura);
							listIn.add(invoice);
						} /*catch (webServiceResponseException ex) {						            
							facturasError+=1;																		
							sb.append("Factura: " + factura + " -- Lista de Errores (WSResponseException): " + "\n" + ex.getMessage() + "\n");
				            System.out.println("webServiceResponseException: " + ex.getMessage());
						} */catch (Exception e) {
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
			    /*
			    mV = new ModelAndView("cFDIssued/resultadosEmisionFactura");						
				mV.addObject("resumen", "Facturas procesadas: 0 -- Facturas correctas: 0 -- Facturas Incidencias: 0");
				mV.addObject("resultados", "Lista de Errores (Exception General - Global): " + "\n" + e.getMessage() + "\n");						
				return mV;*/
			} 
			return sbErrorFile;
        
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
		 	
		 	System.out.println("antes generaXML");
		 	String nameFile = "";
			//nameFile = generaXML.generaXMLHandler(invoice, fiscalEntity, fecha);
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
				//xmlProcess.valida22(out);
				
				System.out.println("XML antes de validar: "+generaXML.getOut().toString("UTF-8"));
				xmlProcessGeneral.validaCFDI33(generaXML.getOut());
			}
			
			System.out.println("despues generaXML");
			cFDIssued.setCreationDate(date);
			cFDIssued.setIssueDate(date);
			cFDIssued.setDateOfIssuance(date);
			Route route = new Route();
			route.setRoute(nameFile);
			cFDIssued.setFilePath(route);
			
			cFDIssued.setStatus(Integer.parseInt(statusActive));
			
			cFDIssued.setFormatType(1);
			
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
			
			
			//System.out.println("****** Guardado en Base de Datos");
			
			//System.out.println("****** Datos enviados a Ondemand");
			//System.out.println("****** Nombre del archivo: "+nameFile);
			//System.out.println("****** RFC Emisor: "+ fiscalEntity.getTaxID());
			//System.out.println("****** Numero de Contrato: "+invoice.getContractNumber());
			//System.out.println("****** Numero de Cliente: "+invoice.getCustomerCode());
			//System.out.println("****** Periodo: "+ invoice.getPeriod());
			//System.out.println("****** Fecha: "+date);
			//String serie=null;
			
            cFDIssued.setCreationDate(new Date());
            cFDIssued.setAuthor("masivo");
            cFDIssued.setModifiedBy("masivo");


			ByteArrayOutputStream xmlSinAddenda = generaXML.guarda(null, fecha);
			
			if(xmlSinAddenda!=null){
				this.invoice.setByteArrXMLSinAddenda(xmlSinAddenda);	
				//System.out.println("XML devuelto por generaXML.guarda: " + this.invoice.getByteArrXMLSinAddenda());								
			}else{
				this.invoice.setByteArrXMLSinAddenda(null);	
			}
			
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
	
	public StringWriter documentToStringWriter(Document dom) throws Exception{
		StringWriter sw2 = new StringWriter();		
		
		if(this.tx == null){
			this.tx = TransformerFactory.newInstance().newTransformer();
			this.tx.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		}					
		this.tx.transform(new DOMSource(dom), new StreamResult(sw2));
		
		return sw2;
	}
	public Document stringToDocument(String strXML) throws Exception{
		Document domResultado = null;
	
		if (this.db == null){
			this.dbf = DocumentBuilderFactory.newInstance();
			this.db = this.dbf.newDocumentBuilder();
		}
		
		domResultado = this.db.parse(new InputSource(new StringReader(strXML)));	
		
		return domResultado;
	}
}
