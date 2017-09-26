package com.interfactura.firmalocal.xml.util;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.validation.ValidatorHandler;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.interfactura.firmalocal.datamodel.CfdiComprobanteFiscal;
import com.interfactura.firmalocal.domain.entities.CFDIssued;
import com.interfactura.firmalocal.domain.entities.CFDIssuedIn;
import com.interfactura.firmalocal.domain.entities.FiscalEntity;
import com.interfactura.firmalocal.domain.entities.Iva;
import com.interfactura.firmalocal.domain.entities.SealCertificate;
import com.interfactura.firmalocal.persistence.OpenJpaManager;
import com.interfactura.firmalocal.xml.Properties;
//import com.interfactura.firmalocal.xml.WebServiceCliente;
//import com.interfactura.firmalocal.xml.factura.ConvertirImplV3_3;
//import com.interfactura.firmalocal.xml.factura.GeneraXML_CFDV3_3;
import com.interfactura.firmalocal.xml.file.XMLProcess;

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
	
	public String convierte(CfdiComprobanteFiscal comp){
		StringBuilder sbXml = new StringBuilder("");
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
		sbXml.append(conver.addenda(comp));
		
		//cerrar comprobante
		sbXml.append(conver.closeFComprobante());
		
		System.out.println("---XML Generado---");
		System.out.println(sbXml.toString());
		System.out.println("---Fin XML Generado---");
		
		
		return sbXml.toString();
	}
	
}
