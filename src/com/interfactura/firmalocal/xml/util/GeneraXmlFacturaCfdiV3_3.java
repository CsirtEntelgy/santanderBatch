package com.interfactura.firmalocal.xml.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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
	
	//private WebServiceCliente servicePort = null;
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
	
	public ByteArrayOutputStream convierte(CfdiComprobanteFiscal comp, FiscalEntity fe) throws UnsupportedEncodingException
		, ParserConfigurationException, SAXException, IOException, XPathExpressionException
		, TransformerConfigurationException, TransformerException, GeneralSecurityException, ValidationException{
		
		StringBuilder sbXml = new StringBuilder("");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Date date = Calendar.getInstance().getTime();
		certificado.find(fe.getId());
		
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
		sbXml.append(conver.addenda(comp));
		
		//cerrar comprobante
		sbXml.append(conver.closeFComprobante());
		
		System.out.println("---XML Generado---");
		System.out.println(sbXml.toString());
		System.out.println("---Fin XML Generado---");
		
		if(sbXml.toString().length() > 0){
			System.out.println("---Reemplazando certificado y sello---");
			out = UtilCatalogos.convertStringToOutpuStream(sbXml.toString());
			Document doc = UtilCatalogos.convertStringToDocument(out.toString("UTF-8"));
			UtilCatalogos.setValueOnDocumentElement(doc, "//Comprobante/@NoCertificado", certificado.getCertificado().getSerialNumber());
			out = UtilCatalogos.convertStringToOutpuStream(UtilCatalogos.convertDocumentXmlToString(doc));
			out = xmlProcessGeneral.replacesOriginalString(out, xmlProcessGeneral.generatesOriginalString(out, "3.3"), certificado);
		}
		
		System.out.println("---XML Generado despues de reemplazo---");
		System.out.println(out.toString("UTF-8"));
		System.out.println("---Fin XML Generado despues de reemplazo---");
		
		return out;
	}
	
}
