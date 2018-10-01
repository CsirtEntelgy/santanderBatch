package com.interfactura.firmalocal.xml.pagos;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import com.interfactura.firmalocal.domain.entities.CFDIssued;

@Component
public class XMLPagos {
	
//	Cabecera del comprobante
	public  String version;
	public  String total;
	public  String tipoComprobante;
	public  String subTotal;
	public  String serie;
	public  String sello;
	public  String noCertificado;
	public  String moneda;
	public  String lugarExpedicion;
	public  String confirmacion;
	public  String folio;
	public  String fecha;
	public  String certificado;
	public  String fechaSellado; 
	
//	FiscalEntitity Id
	public long fiscalId = 0;
	
//	CFDI relacionados
	public  HashMap<String, String> cfdiRelacionados;
	
//	Emisor
	public  String rfcEmisor;
	public  String nombreEmisor;
	public  String regimenFiscal;
	
//	Receptor 
	public  String rfcReceptor;
	public  String nombreReceptor;
	public  String usoCFDI;
	public  String contrato;
	public  String numeroCliente;
	public  String numRegIdTrib;
	public  String residenciaFiscal;
	
//	Concepto
	public  String valorUnitario;
	public  String importe;
	public  String descripcion;
	public  String claveUnidad;
	public  String claveProdServ;
	public  String cantidad;
	
//	Pago
	public List<Pago> pagos = new ArrayList<Pago>();
	
//	Documento Relacionado
	public List<Documento> documentos = new ArrayList<Documento>();
	
//	String xmlOriginal
	public ByteArrayOutputStream xmlOriginal = new ByteArrayOutputStream();
	public String startLine = "";
	public String endLine = "";
	public String nombreAPP = "";
	public Document docResultado;
	public String addenda = ""; 
	
	
//	Addenda
//	Domicilio
	
	public String calle = "";
	public String noInterior = "";
	public String noExterior = "";
	public String colonia = "";
	public String referencia = "";
	public String municipio = "";
	public String estado = "";
	public String pais = "";
	public String cp = "";
	public String ciudad = "";
	
//	Aprobacion
	public String numAprobacion;
	public String yearAprobacion;
	
	
	
	public CFDIssued cfd;
	
	
	public Document getDocResultado() {
		return docResultado;
	}
	public void setDocResultado(Document docResultado) {
		this.docResultado = docResultado;
	}
	
	
	
	
	
	
	 
	
	

}
