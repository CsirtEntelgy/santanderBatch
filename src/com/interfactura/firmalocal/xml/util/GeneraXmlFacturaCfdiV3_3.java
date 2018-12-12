package com.interfactura.firmalocal.xml.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.validation.ValidatorHandler;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.interfactura.firmalocal.datamodel.CfdiComprobanteFiscal;
import com.interfactura.firmalocal.domain.entities.CFDIssued;
import com.interfactura.firmalocal.domain.entities.CFDIssuedIn;
import com.interfactura.firmalocal.domain.entities.FiscalEntity;
import com.interfactura.firmalocal.domain.entities.Iva;
import com.interfactura.firmalocal.domain.entities.SealCertificate;
import com.interfactura.firmalocal.persistence.OpenJpaManager;
import com.interfactura.firmalocal.xml.Certificate;
import com.interfactura.firmalocal.xml.Properties;
//import com.interfactura.firmalocal.xml.WebServiceCliente;
//import com.interfactura.firmalocal.xml.factura.ConvertirImplV3_3;
//import com.interfactura.firmalocal.xml.factura.GeneraXML_CFDV3_3;
import com.interfactura.firmalocal.xml.file.XMLProcess;
import com.interfactura.recepcionmasiva.service.ValidationException;

@Component
public class GeneraXmlFacturaCfdiV3_3 {

	private Logger logger = Logger.getLogger(GeneraXmlFacturaCfdiV3_3.class);
	@Autowired
	private ConvertirCfdiImplV3_3 conver;
	@Autowired
	private Properties properties;
	@Autowired
	private XMLProcess xmlProcess;
	@Autowired
	public OpenJpaManager openJpaManager;
	private Transformer transf;
	private List<byte[]> lstFactoraje;
	private HashMap<String, FiscalEntity> lstFiscal;
	private List<SealCertificate> lstSeal;
	private List<Iva> lstIva;
	private ValidatorHandler validator;
	private int sizeT = 255;
	private List<CFDIssuedIn> lstCFDIncidence;
	private List<CFDIssued> lstCFD;
	private String msgError;
	private String startLine;
	private String endLine;
	private HashMap<String, HashMap> campos22;
	private HashMap<String, HashMap> tipoCambio;
	
	//private WebServiceCliente servicePort = null ;
	private DocumentBuilderFactory dbf = null;
	private DocumentBuilder db = null;
	private Transformer tx = null;
	
	private String urlWebService = null;
	   
	//Nombres de Aplicativos Facturas
	private HashMap<String, String> nombresApps = new HashMap<String, String>();
	
	@Autowired
	private Certificate certificado;
	@Autowired
	private XMLProcessGeneral xmlProcessGeneral;
	
	public ByteArrayOutputStream convierte(CfdiComprobanteFiscal comp) throws UnsupportedEncodingException{
		
		StringBuilder sbXml = new StringBuilder("");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Date date = Calendar.getInstance().getTime();
		
		//iniciar xml
		sbXml.append(conver.startXml());
		
		//comprobante
		sbXml.append(conver.fComprobante(comp, date));
		//emisor
		sbXml.append(conver.emisor(comp));
		//receptor
		sbXml.append(conver.receptor(comp));
		//concepto
		sbXml.append(conver.startConcepto(comp));
		//impuestos
		sbXml.append(conver.impuestos(comp));
		//complemento
		sbXml.append(conver.complemento(comp));
		//addenda
		//sbXml.append(conver.addenda(comp));
		
		//cerrar comprobante
		sbXml.append(conver.closeFComprobante());
		
		if(sbXml.toString().length() > 0){
			out = UtilCatalogos.convertStringToOutpuStream(sbXml.toString());
		}
		return out;
	}
	
	public ByteArrayOutputStream convierteFU(CfdiComprobanteFiscal comp) throws UnsupportedEncodingException{
		
		StringBuilder sbXml = new StringBuilder("");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Date date = Calendar.getInstance().getTime();
		
		//iniciar xml
		sbXml.append(conver.startXml());
		
		//comprobante
		sbXml.append(conver.fComprobante(comp, date));
		//emisor
		sbXml.append(conver.emisor(comp));
		//receptor
		sbXml.append(conver.receptor(comp));
		//concepto
		sbXml.append(conver.startConceptoFU(comp));
		//impuestos
		sbXml.append(conver.impuestosFU(comp));
		//complemento
		sbXml.append(conver.complemento(comp));
		//addenda
		//sbXml.append(conver.addenda(comp));
		
		//cerrar comprobante
		sbXml.append(conver.closeFComprobante());
		
		if(sbXml.toString().length() > 0){
			out = UtilCatalogos.convertStringToOutpuStream(sbXml.toString());
		}
		return out;
	}
	
	
	public Document agregaAddenda(Document doc, CfdiComprobanteFiscal comp) throws SAXException
	, IOException, ParserConfigurationException, FactoryConfigurationError, XPathExpressionException, TransformerConfigurationException, TransformerException{
	
		String addenda = "";
		comp.setTipoCambio("1");
		if (UtilCatalogos.getStringValByExpression(doc, "//Comprobante//@Moneda").equalsIgnoreCase("MXN")) 
			for (Entry<String, String> entry : comp.getAddenda().getCampoAdicional().entrySet()) {
				if (entry.getKey().trim().equalsIgnoreCase("tipocambio") || entry.getKey().equalsIgnoreCase("tipo cambio"))
					entry.setValue("1");
			}
		addenda = conver.addenda(comp);
		
		if (addenda.trim().length() > 0){
			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			
			Element element = doc.getDocumentElement();
			
			Document addendaDoc = dBuilder.parse(new ByteArrayInputStream(addenda.getBytes("UTF-8")));
			
			Node addendaNode = doc.importNode(addendaDoc.getDocumentElement(), true);
			element.appendChild(addendaNode);
		}
		
		return doc;
	}
	
	public ByteArrayOutputStream reemplazaCadenaOriginal(ByteArrayOutputStream in, FiscalEntity fe) 
			throws UnsupportedEncodingException, ParserConfigurationException, SAXException, IOException
			, XPathExpressionException, TransformerConfigurationException, TransformerException
			, GeneralSecurityException, ValidationException{
	
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		certificado.find(fe.getId());
		
		Document doc = UtilCatalogos.convertStringToDocument(in.toString("UTF-8"));
		UtilCatalogos.setValueOnDocumentElement(doc, "//Comprobante/@NoCertificado", certificado.getCertificado().getSerialNumber());
    	if (UtilCatalogos.getStringValByExpression(doc, "//Comprobante//@Moneda").equalsIgnoreCase("MXN")) {
    		UtilCatalogos.setValueOnDocumentElement(doc, "//Comprobante/@TipoCambio", "1");
    		
    	}
    	
    	
    	String sal  = UtilCatalogos.convertDocumentXmlToString(doc);
    	
    	boolean reteImp = true;
    	boolean traImp = true;
    	if (!UtilCatalogos.getStringValByExpression(doc, "//Comprobante/Impuestos/@TotalImpuestosRetenidos").equalsIgnoreCase("")) {
    			
	        BigDecimal totalImpRet = new BigDecimal(UtilCatalogos.getStringValByExpression(doc, "//Comprobante/Impuestos/@TotalImpuestosRetenidos"));
	                
	    	if (totalImpRet.compareTo(new BigDecimal("0")) == 0) {
	    		UtilCatalogos.setValueOnDocumentElement(doc, "//Comprobante/Impuestos/@TotalImpuestosRetenidos", "0.00");
	    		sal = UtilCatalogos.convertDocumentXmlToString(doc);
	        	sal = sal.replace("TotalImpuestosRetenidos=\"0.00\"", "");
	        	doc = UtilCatalogos.convertStringToDocument(sal);
	        	reteImp = false;
	    	} else
	    		sal = UtilCatalogos.convertDocumentXmlToString(doc);
    	}
    	
    	if (!UtilCatalogos.getStringValByExpression(doc, "//Comprobante/Impuestos/@TotalImpuestosTrasladados").equalsIgnoreCase("")) {
	    	BigDecimal totalImpTra = new BigDecimal(UtilCatalogos.getStringValByExpression(doc, "//Comprobante/Impuestos/@TotalImpuestosTrasladados"));
	    	if (totalImpTra.compareTo(new BigDecimal("0")) == 0) {
	    		UtilCatalogos.setValueOnDocumentElement(doc, "//Comprobante/Impuestos/@TotalImpuestosTrasladados", "0.00");
	    		sal = UtilCatalogos.convertDocumentXmlToString(doc);
	        	sal = sal.replace("TotalImpuestosTrasladados=\"0.00\"", "");
	        	doc = UtilCatalogos.convertStringToDocument(sal);
	        	traImp = false;
	    	} else
	    		sal = UtilCatalogos.convertDocumentXmlToString(doc);
    	}
    	
//    	cfdi:ImpuestosXD
    	sal = UtilCatalogos.convertDocumentXmlToString(doc);
    	
    	
    	
    	if ( !reteImp && !traImp) {
    		sal = sal.replace("<cfdi:Impuestos />", "");
    	}
    	
    	
		out = UtilCatalogos.convertStringToOutpuStream(sal);
		
		out = xmlProcessGeneral.replacesOriginalString(out, xmlProcessGeneral.generatesOriginalString(out, "3.3"), certificado);
		
		return out;
	}
	
	public ByteArrayOutputStream reemplazaCadenaOriginalNew(ByteArrayOutputStream in, FiscalEntity fe, boolean tasaCero) 
			throws UnsupportedEncodingException, ParserConfigurationException, SAXException, IOException
			, XPathExpressionException, TransformerConfigurationException, TransformerException
			, GeneralSecurityException, ValidationException{
	
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		certificado.find(fe.getId());
		
		Document doc = UtilCatalogos.convertStringToDocument(in.toString("UTF-8"));
		UtilCatalogos.setValueOnDocumentElement(doc, "//Comprobante/@NoCertificado", certificado.getCertificado().getSerialNumber());
    	if (UtilCatalogos.getStringValByExpression(doc, "//Comprobante//@Moneda").equalsIgnoreCase("MXN")) {
    		UtilCatalogos.setValueOnDocumentElement(doc, "//Comprobante/@TipoCambio", "1");
    		
    	}
    	
    	
    	String sal  = UtilCatalogos.convertDocumentXmlToString(doc);
    	
    	boolean reteImp = true;
    	boolean traImp = true;
    	if (!UtilCatalogos.getStringValByExpression(doc, "//Comprobante/Impuestos/@TotalImpuestosRetenidos").equalsIgnoreCase("")) {
    			
	        BigDecimal totalImpRet = new BigDecimal(UtilCatalogos.getStringValByExpression(doc, "//Comprobante/Impuestos/@TotalImpuestosRetenidos"));
	                
	    	if (totalImpRet.compareTo(new BigDecimal("0")) == 0) {
	    		UtilCatalogos.setValueOnDocumentElement(doc, "//Comprobante/Impuestos/@TotalImpuestosRetenidos", "0.00");
	    		sal = UtilCatalogos.convertDocumentXmlToString(doc);
	        	sal = sal.replace("TotalImpuestosRetenidos=\"0.00\"", "");
	        	doc = UtilCatalogos.convertStringToDocument(sal);
	        	reteImp = false;
	    	} else
	    		sal = UtilCatalogos.convertDocumentXmlToString(doc);
    	}
    	
    	
    	 
    	if (!UtilCatalogos.getStringValByExpression(doc, "//Comprobante/Impuestos/@TotalImpuestosTrasladados").equalsIgnoreCase("") && !tasaCero) {
	    	BigDecimal totalImpTra = new BigDecimal(UtilCatalogos.getStringValByExpression(doc, "//Comprobante/Impuestos/@TotalImpuestosTrasladados"));
	    	if (totalImpTra.compareTo(new BigDecimal("0")) == 0) {
	    		UtilCatalogos.setValueOnDocumentElement(doc, "//Comprobante/Impuestos/@TotalImpuestosTrasladados", "0.00");
	    		sal = UtilCatalogos.convertDocumentXmlToString(doc);
	        	sal = sal.replace("TotalImpuestosTrasladados=\"0.00\"", "");
	        	doc = UtilCatalogos.convertStringToDocument(sal);
	        	traImp = false;
	    	} else
	    		sal = UtilCatalogos.convertDocumentXmlToString(doc);
    	}
    	
//    	cfdi:ImpuestosXD
    	sal = UtilCatalogos.convertDocumentXmlToString(doc);
    	
    	
    	
    	if ( !reteImp && !traImp) {
    		sal = sal.replace("<cfdi:Impuestos />", "");
    	}
    	
    	
		out = UtilCatalogos.convertStringToOutpuStream(sal);
		
		out = xmlProcessGeneral.replacesOriginalString(out, xmlProcessGeneral.generatesOriginalString(out, "3.3"), certificado);
		
		return out;
	}
	
	
}
