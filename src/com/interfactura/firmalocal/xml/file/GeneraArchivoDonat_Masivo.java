package com.interfactura.firmalocal.xml.file;

import static com.interfactura.firmalocal.xml.util.Util.convertirFecha;
import static com.interfactura.firmalocal.xml.util.Util.isNull;
import static com.interfactura.firmalocal.xml.util.Util.isNullEmpty;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

import com.interfactura.firmalocal.datamodel.CustomsInformation;
import com.interfactura.firmalocal.datamodel.ElementsInvoice;
import com.interfactura.firmalocal.datamodel.FarmAccount;
import com.interfactura.firmalocal.datamodel.Invoice_Masivo;
import com.interfactura.firmalocal.datamodel.Part;
import com.interfactura.firmalocal.domain.entities.FiscalEntity;
import com.interfactura.firmalocal.persistence.FiscalEntityManager;
import com.interfactura.firmalocal.xml.Certificate;
import com.interfactura.firmalocal.xml.Properties;
//import com.interfactura.firmalocal.xml.timbre.webServiceCliente;
//import com.interfactura.firmalocal.xml.timbre.webServiceConnectionException;
//import com.interfactura.firmalocal.xml.timbre.webServiceResponseException;
import com.interfactura.firmalocal.xml.util.Util_Masivo;
import com.interfactura.recepcionmasiva.service.ValidationException;

@Component
public class GeneraArchivoDonat_Masivo implements XMLReader{
	private Logger logger = Logger.getLogger(GeneraArchivoDonat_Masivo.class);

	private ContentHandler contentHandler;
	private AttributesImpl attribs;
	private Invoice_Masivo invoice;
	private ElementsInvoice element;
	private FiscalEntity fe;
	private Date date;
	@Autowired
	private Properties properties;
	@Autowired
	private XMLProcess_Masivo xmlProcess;
	@Autowired
	private Certificate certificado;
	//@Autowired
	//private webServiceCliente ws;
	private Part part;
	private FarmAccount farmAccount;
	private CustomsInformation customsInformation;
	private String campo;
	private String valor;
	private ByteArrayOutputStream out;
	private boolean donataria;
	@Autowired(required=true)
	private FiscalEntityManager fiscalEntityManager;
	private String UUID;

	private String nameSpaceDonatarias = "http://www.sat.gob.mx/donat";
	
	@Override
	public boolean getFeature(String name) throws SAXNotRecognizedException,
			SAXNotSupportedException {
		return false;
	}

	@Override
	public void setFeature(String name, boolean value)
			throws SAXNotRecognizedException, SAXNotSupportedException {
	}

	@Override
	public Object getProperty(String name) throws SAXNotRecognizedException,
			SAXNotSupportedException {
		return null;
	}

	@Override
	public void setProperty(String name, Object value)
			throws SAXNotRecognizedException, SAXNotSupportedException {

	}

	@Override
	public void setEntityResolver(EntityResolver resolver) {
	}

	@Override
	public EntityResolver getEntityResolver() {
		return null;
	}

	@Override
	public void setDTDHandler(DTDHandler handler) {
	}

	@Override
	public DTDHandler getDTDHandler() {

		return null;
	}

	public void setContentHandler(ContentHandler handler) {
		contentHandler = handler;
	}

	public ContentHandler getContentHandler() {
		return contentHandler;
	}

	@Override
	public void setErrorHandler(ErrorHandler handler) {
	}

	@Override
	public ErrorHandler getErrorHandler() {
		return null;
	}

	@Override
	public void parse(InputSource input) throws IOException, SAXException {
		this.parse();
	}

	@Override
	public void parse(String systemId) throws IOException, SAXException {

	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void parse() throws SAXException {
		logger.debug("++++++++++++++++++++++++++++++++++Inicio");
		this.inicio();
		logger.debug("++++++++++++++++++++++++++++++++++Comprobante");
		this.startComprobante();
		logger.debug("++++++++++++++++++++++++++++++++++Emisor");
		this.startEmisor();
		logger.debug("++++++++++++++++++++++++++++++++++Domicilio Fiscal");
		this.startDomicilioFiscal();
		this.endDomicilioFiscal();
		logger.debug("++++++++++++++++++++++++++++++++++Regimen Fiscal");
		this.startRegimenFiscal();
		this.endRegimenFiscal();
		// this.startExpedidoEn();
		// this.endExpedidoEn();
		this.endEmisor();
		logger.debug("++++++++++++++++++++++++++++++++++Receptor");
		this.startReceptor();
		logger.debug("++++++++++++++++++++++++++++++++++Domicilio");
		this.startDomicilio();
		this.endDomicilio();
		this.endReceptor();
		logger.debug("++++++++++++++++++++++++++++++++++Concepto");
		this.startConceptos();
		for (int i = 0; i < invoice.getElements().size(); i++) {
			element = invoice.getElements().get(i);
			this.startConcepto();
			this.contentConcept(element.getPartes(), element.getCuentaPredial(), element.getInformacionAduanera());
			this.endConcepto();
		}
		this.endConceptos();
		logger.debug("++++++++++++++++++++++++++++++++++Impuesto");
		this.startImpuestos();
		// this.startRetenciones();
		// this.startRetencion();
		// this.endRetencion();
		// this.endRetenciones();
		logger.debug("++++++++++++++++++++++++++++++++++Traslado");
		if(this.donataria != true) {
			this.startTraslados();
			this.startTraslado();
			this.endTraslado();
			this.endTraslados();
		}
		this.endImpuestos();
		logger.debug("+++++++++++++++++++++++++++++++++++Complemento");
		this.startComplemento();
		logger.debug("+++++++++++++++++++++++++++++++++++Donatarias");
		//this.startTimbreFiscalDigital();
		//this.endTimbreFiscalDigital();
		if(this.donataria == true) {
			this.startDonatarias();
			this.endDonatarias();
		}
		this.endComplemento();
		
		this.endComprobante();
		this.fin();
	}
	
	/**
	 * 
	 * @param partes
	 * @param cuentaPredial
	 * @param aduana
	 * @throws SAXException
	 */
	public void contentConcept(List<Part> partes, List<FarmAccount> cuentaPredial, List<CustomsInformation> aduana) 
		throws SAXException
	{
		if (partes!= null) {
			for (Part part : element.getPartes()) {
				this.part = part;
				this.startParte();
				this.informacionAduanera(part.getAduana());
				this.endParte();
			}
		}

		if (cuentaPredial != null) {
			for (FarmAccount farmAccount : cuentaPredial) {
				this.farmAccount = farmAccount;
				this.startCuentaPredial();
				this.endCuentaPredial();
			}
		}

		this.informacionAduanera(aduana);
	}
	
	/**
	 * 
	 * @param aduana
	 * @throws SAXException 
	 */
	private void informacionAduanera(List<CustomsInformation> aduana) throws SAXException{
		if (aduana != null) {
			for (CustomsInformation customsInformation : aduana) {
				this.customsInformation = customsInformation;
				this.startInformacionAduanera();
				this.endInformacionAduanera();
			}
		}
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void inicio() throws SAXException {
		contentHandler.startDocument();
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void fin() throws SAXException {
		contentHandler.endDocument();
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void startComprobante() throws SAXException {
		attribs = new AttributesImpl();
		this.date = Calendar.getInstance().getTime();

		attribs.addAttribute(properties.getUri(), "version", "version",
					"string", "3.2");
		
		logger.debug("*****SERIE: " + this.invoice.getSerie());
		logger.debug("*****FOLIO: " + this.invoice.getFolio());
		if ((this.invoice.getSerie() != null)
				&& (this.invoice.getSerie().length() > 0)) {
			attribs.addAttribute(properties.getUri(), "serie", "serie",
					"string", this.invoice.getSerie());
		}
		attribs.addAttribute(properties.getUri(), "folio", "folio", "string",
				this.invoice.getFolio());
		attribs.addAttribute(properties.getUri(), "fecha", "fecha", "dateTime",
				convertirFecha(date));
		attribs.addAttribute(properties.getUri(), "sello", "sello", "string",
				properties.getLabelSELLO());
		
		if("".equals(invoice.getFormaPago())) {
			attribs.addAttribute(properties.getUri(), "formaDePago", "formaDePago",
					"string", "Pago en una sola exhibici\u00F3n");
		} else {
			attribs.addAttribute(properties.getUri(), "formaDePago", "formaDePago", 
					"string", invoice.getFormaPago());
		}
		attribs.addAttribute(properties.getUri(), "noCertificado",
				"noCertificado", "string", properties.getLblNO_CERTIFICADO());
		attribs.addAttribute(properties.getUri(), "certificado", "certificado",
				"string", properties.getLblCERTIFICADO());
		// attribs.addAttribute(uri, "condicionesDePago", "condicionesDePago",
		// "string", "");
		attribs.addAttribute(properties.getUri(), "subTotal", "subTotal",
				"decimal", Util_Masivo.formatNumber(this.invoice.getSubTotal()));
		// attribs.addAttribute(uri, "descuento", "descuento", "string", "");
		// attribs.addAttribute(uri, "motivoDescuento", "motivoDescuento",
		// "string", "");
		attribs.addAttribute(properties.getUri(), "total", "total", "decimal",
				Util_Masivo.formatNumber(this.invoice.getTotal()));
		// attribs.addAttribute(uri, "metodoDePago", "metodoDePago", "string",
		// "");
		attribs.addAttribute(properties.getUri(), "tipoDeComprobante",
				"tipoDeComprobante", "string", invoice.getTipoFormato());
		
		attribs.addAttribute(properties.getUri(), "metodoDePago", "metodoDePago",
				"string", invoice.getMetodoPago());
		attribs.addAttribute(properties.getUri(), "LugarExpedicion", "LugarExpedicion", 
				"string", invoice.getLugarExpedicion());

        if(invoice.getNumCtaPago()!= null && invoice.getNumCtaPago().trim().length()>0){
            attribs.addAttribute(properties.getUri(), "NumCtaPago", "NumCtaPago",
                    "string", invoice.getNumCtaPago());
        }else{
            attribs.addAttribute(properties.getUri(), "NumCtaPago", "NumCtaPago",
                    "string", "NO IDENTIFICADO");
        }

	
		attribs.addAttribute(properties.getUri(), "xmlns:xsi", "xmlns:xsi",
				"string", "http://www.w3.org/2001/XMLSchema-instance");
		if(this.donataria) {
			attribs.addAttribute(properties.getUri(), "xsi:schemaLocation", "xsi:schemaLocation", 
					"string", "http://www.sat.gob.mx/cfd/3 http://www.sat.gob.mx/sitio_internet/cfd/3/cfdv32.xsd http://www.sat.gob.mx/donat http://www.sat.gob.mx/sitio_internet/cfd/donat/donat11.xsd");
			attribs.addAttribute(properties.getUri(), "xmlns:ecb", "xmlns:ecb",
					"string", "http://www.sat.gob.mx/ecb");
			attribs.addAttribute(properties.getUri(), "xmlns:donat", "xmlns:donat", "string", "http://www.sat.gob.mx/donat");
			contentHandler.startElement(properties.getNameSpace(), "Comprobante",
					"cfdi:Comprobante", attribs);
		} else {
			attribs.addAttribute(properties.getUri(), "xsi:schemaLocation", "xsi:schemaLocation", 
					"string", "http://www.sat.gob.mx/cfd/3 http://www.sat.gob.mx/sitio_internet/cfd/3/cfdv32.xsd");
			attribs.addAttribute(properties.getUri(), "xmlns:ecb", "xmlns:ecb",
					"string", "http://www.sat.gob.mx/ecb");
			contentHandler.startElement(properties.getNameSpace(), "Comprobante",
					"cfdi:Comprobante", attribs);
		}
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void endComprobante() throws SAXException {
		contentHandler.endElement(properties.getNameSpace(), "Comprobante",
				"cfdi:Comprobante");
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void startEmisor() throws SAXException {
		attribs = new AttributesImpl();
		attribs.addAttribute(properties.getUri(), "rfc", "rfc", "string",
				isNull(fe.getTaxID()));
		attribs.addAttribute(properties.getUri(), "nombre", "nombre", "string",
				isNull(fe.getFiscalName()));
		contentHandler.startElement(properties.getNameSpace(), "Emisor",
					"cfdi:Emisor", attribs);
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void endEmisor() throws SAXException {
			contentHandler
					.endElement(properties.getNameSpace(), "Emisor", "cfdi:Emisor");
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void startDomicilioFiscal() throws SAXException {
		attribs = new AttributesImpl();
		logger.debug("++++++++++++++++++++++++++++++++++Domicilio Fiscal (calle)");
		attribs.addAttribute(properties.getUri(), "calle", "calle", "string",
				isNull(fe.getAddress().getStreet()));
		logger.debug("++++++++++++++++++++++++++++++++++Domicilio Fiscal (No Exterior)");
		agregaAtributo("noExterior", "string", fe.getAddress()
				.getExternalNumber());
		logger.debug("++++++++++++++++++++++++++++++++++Domicilio Fiscal (No Interior)");
		agregaAtributo("noInterior", "string", fe.getAddress()
				.getInternalNumber());
		logger.debug("++++++++++++++++++++++++++++++++++Domicilio Fiscal (Colonia)");
		agregaAtributo("colonia", "string", fe.getAddress().getNeighborhood());
		logger.debug("++++++++++++++++++++++++++++++++++Domicilio Fiscal (Localidad)");
		agregaAtributo("localidad", "string", "");
		logger.debug("++++++++++++++++++++++++++++++++++Domicilio Fiscal (Referencia)");
		agregaAtributo("referencia", "string", fe.getAddress().getReference());
		logger.debug("++++++++++++++++++++++++++++++++++Domicilio Fiscal (Municipio)");
		attribs.addAttribute(properties.getUri(), "municipio", "municipio",
				"string", isNull(fe.getAddress().getRegion()));
		logger.debug("++++++++++++++++++++++++++++++++++Domicilio Fiscal (estado)");
		attribs.addAttribute(properties.getUri(), "estado", "estado", "string",
				isNull(fe.getAddress().getState().getName()));
		logger.debug("++++++++++++++++++++++++++++++++++Domicilio Fiscal (Pais)");
		attribs.addAttribute(properties.getUri(), "pais", "pais", "string",
				fe.getAddress().getState().getCountry().getName());
		logger.debug("++++++++++++++++++++++++++++++++++Domicilio Fiscal (Codigo Postal)");
		attribs.addAttribute(properties.getUri(), "codigoPostal",
				"codigoPostal", "string", isNull(fe.getAddress().getZipCode()));
		contentHandler.startElement(properties.getNameSpace(),
					"DomicilioFiscal", "cfdi:DomicilioFiscal", attribs);
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void endDomicilioFiscal() throws SAXException {
			contentHandler.endElement(properties.getNameSpace(), "DomicilioFiscal",
					"cfdi:DomicilioFiscal");
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void startExpedidoEn() throws SAXException {
		attribs = new AttributesImpl();
		attribs.addAttribute(properties.getUri(), "calle", "calle", "string",
				"");
		attribs.addAttribute(properties.getUri(), "noExterior", "noExterior",
				"string", "");
		attribs.addAttribute(properties.getUri(), "noInterior", "noInterior",
				"string", "");
		attribs.addAttribute(properties.getUri(), "colonia", "colonia",
				"string", "");
		attribs.addAttribute(properties.getUri(), "localidad", "localidad",
				"string", "");
		attribs.addAttribute(properties.getUri(), "referencia", "referencia",
				"string", "");
		attribs.addAttribute(properties.getUri(), "municipio", "municipio",
				"string", "");
		attribs.addAttribute(properties.getUri(), "estado", "estado", "string",
				"");
		attribs.addAttribute(properties.getUri(), "pais", "pais", "string", "");
		attribs.addAttribute(properties.getUri(), "codigoPostal",
				"codigoPostal", "string", "");
		contentHandler.startElement(properties.getNameSpace(), "ExpedidoEn",
				"ExpedidoEn", attribs);
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void endExpedidoEn() throws SAXException {
		contentHandler.endElement(properties.getNameSpace(), "ExpedidoEn",
				"ExpedidoEn");
	}
	
	/**
	 * 
	 * @throws SAXException
	 */
	public void startRegimenFiscal() throws SAXException {
		attribs = new AttributesImpl();
		attribs.addAttribute(properties.getUri(), "Regimen", "Regimen", "string",
				invoice.getRegimenFiscal());
        	contentHandler.startElement(properties.getNameSpace(),
        			"RegimenFiscal", "cfdi:RegimenFiscal", attribs);
	}
	
	/**
	 * 
	 * @throws SAXException
	 */
	public void endRegimenFiscal() throws SAXException {
			 contentHandler.endElement(properties.getNameSpace(), "RegimenFiscal",
					 "cfdi:RegimenFiscal");
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void startReceptor() throws SAXException {
		attribs = new AttributesImpl();
		attribs.addAttribute(properties.getUri(), "rfc", "rfc", "string",
				isNull(this.invoice.getRfc()));
		agregaAtributo("nombre", "string", this.invoice.getName());
			contentHandler.startElement(properties.getNameSpace(), "Receptor",
					"cfdi:Receptor", attribs);
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void endReceptor() throws SAXException {
			contentHandler.endElement(properties.getNameSpace(), "Receptor",
					"cfdi:Receptor");
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void startDomicilio() throws SAXException {
		attribs = new AttributesImpl();
		agregaAtributo("calle", "string", this.invoice.getCalle());
		agregaAtributo("noExterior", "string", this.invoice.getExterior());
		agregaAtributo("noInterior", "string", this.invoice.getInterior());
		agregaAtributo("colonia", "string", this.invoice.getColonia());
		agregaAtributo("localidad", "string", this.invoice.getLocalidad());
		agregaAtributo("referencia", "string", this.invoice.getReferencia());
		agregaAtributo("municipio", "string", this.invoice.getMunicipio());
		agregaAtributo("estado", "string", this.invoice.getEstado());
		attribs.addAttribute(properties.getUri(), "pais", "pais", "string",
				this.invoice.getPais());
		agregaAtributo("codigoPostal", "string", this.invoice.getCodigoPostal());
			contentHandler.startElement(properties.getNameSpace(), "Domicilio",
					"cfdi:Domicilio", attribs);
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void endDomicilio() throws SAXException {
			contentHandler.endElement(properties.getNameSpace(), "Domicilio",
					"cfdi:Domicilio");
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void startConceptos() throws SAXException {
		attribs = new AttributesImpl();
			contentHandler.startElement(properties.getNameSpace(), "Conceptos",
					"cfdi:Conceptos", attribs);
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void endConceptos() throws SAXException {
			contentHandler.endElement(properties.getNameSpace(), "Conceptos",
					"cfdi:Conceptos");
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void startConcepto() throws SAXException {
		attribs = new AttributesImpl();
		attribs.addAttribute(properties.getUri(), "cantidad", "cantidad",
				"decimal", Util_Masivo.formatNumberQuantity(element.getQuantity()));
		agregaAtributo("unidad", "string", element.getUnitMeasure());
		agregaAtributo("noIdentificacion", "noIdentificacion", "");
		attribs.addAttribute(properties.getUri(), "descripcion", "descripcion",
				"string", element.getDescription());
		attribs.addAttribute(properties.getUri(), "valorUnitario",
				"valorUnitario", "string",
				Util_Masivo.formatNumber(element.getUnitPrice()));
		attribs.addAttribute(properties.getUri(), "importe", "importe",
				"decimal", Util_Masivo.formatNumber(element.getAmount()));
			contentHandler.startElement(properties.getNameSpace(), "Concepto",
					"cfdi:Concepto", attribs);
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void endConcepto() throws SAXException {
			contentHandler.endElement(properties.getNameSpace(), "Concepto",
					"cfdi:Concepto");
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void startInformacionAduanera() throws SAXException {
		attribs = new AttributesImpl();
		attribs.addAttribute(properties.getUri(), "numero", "numero", "string",
				this.customsInformation.getNumero());
		attribs.addAttribute(properties.getUri(), "fecha", "fecha", "date",
				this.customsInformation.getFecha());
		attribs.addAttribute(properties.getUri(), "aduana", "aduana", "string",
				this.customsInformation.getAduana());
		contentHandler.startElement(properties.getNameSpace(),
				"InformacionAduanera", "InformacionAduanera", attribs);
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void endInformacionAduanera() throws SAXException {
		contentHandler.endElement(properties.getNameSpace(),
				"InformacionAduanera", "InformacionAduanera");
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void startCuentaPredial() throws SAXException {
		attribs = new AttributesImpl();
		attribs.addAttribute(properties.getUri(), "numero", "numero", "string",
				this.farmAccount.getNumero());
		contentHandler.startElement(properties.getNameSpace(), "CuentaPredial",
				"CuentaPredial", attribs);
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void endCuentaPredial() throws SAXException {
		contentHandler.endElement(properties.getNameSpace(), "CuentaPredial",
				"CuentaPredial");
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void startComplementoConcepto() throws SAXException {
		attribs = new AttributesImpl();
		contentHandler.startElement(properties.getNameSpace(),
				"ComplementoConcepto", "ComplementoConcepto", attribs);
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void endComplementoConcepto() throws SAXException {
		contentHandler.endElement(properties.getNameSpace(),
				"ComplementoConcepto", "ComplementoConcepto");
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void startParte() throws SAXException {
		attribs = new AttributesImpl();
		attribs.addAttribute(properties.getUri(), "cantidad", "cantidad",
				"decimal", this.part.getCantidad());
		attribs.addAttribute(properties.getUri(), "unidad", "unidad", "string",
				this.part.getUnidad());
		attribs.addAttribute(properties.getUri(), "noIdentificacion",
				"noIdentificacion", "string", this.part.getNoIdentificacion());
		attribs.addAttribute(properties.getUri(), "descripcion", "descripcion",
				"string", this.part.getDescripcion());
		attribs.addAttribute(properties.getUri(), "valorUnitario",
				"valorUnitario", "string", this.part.getValorUnitario());
		attribs.addAttribute(properties.getUri(), "importe", "importe",
				"decimal", this.part.getImporte());
		contentHandler.startElement(properties.getNameSpace(), "Parte",
				"Parte", attribs);
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void endParte() throws SAXException {
		contentHandler.endElement(properties.getNameSpace(), "Parte", "Parte");
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void startImpuestos() throws SAXException {
		attribs = new AttributesImpl();
		if(this.donataria != true) {
			agregaAtributo("totalImpuestosRetenidos", "decimal", "");
			agregaAtributo("totalImpuestosTrasladados", "decimal",
					Util_Masivo.formatNumber(this.invoice.getIva()));
		}
		contentHandler.startElement(properties.getNameSpace(), "Impuestos",
					"cfdi:Impuestos", attribs);
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void endImpuestos() throws SAXException {
		contentHandler.endElement(properties.getNameSpace(), "Impuestos",
					"cfdi:Impuestos");
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void startRetenciones() throws SAXException {
		attribs = new AttributesImpl();
		contentHandler.startElement(properties.getNameSpace(), "Retenciones",
				"Retenciones", attribs);
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void endRetenciones() throws SAXException {
		contentHandler.endElement(properties.getNameSpace(), "Retenciones",
				"Retenciones");
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void startRetencion() throws SAXException {
		attribs = new AttributesImpl();
		attribs.addAttribute(properties.getUri(), "impuesto", "impuesto",
				"string", "");
		attribs.addAttribute(properties.getUri(), "importe", "importe",
				"decimal", "");
		contentHandler.startElement(properties.getNameSpace(), "Retencion",
				"Retencion", attribs);
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void endRetencion() throws SAXException {
		contentHandler.endElement(properties.getNameSpace(), "Retencion",
				"Retencion");
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void startTraslados() throws SAXException {
		attribs = new AttributesImpl();
			contentHandler.startElement(properties.getNameSpace(), "Traslados",
					"cfdi:Traslados", attribs);
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void endTraslados() throws SAXException {
			contentHandler.endElement(properties.getNameSpace(), "Traslados",
					"cfdi:Traslados");
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void startTraslado() throws SAXException {
		attribs = new AttributesImpl();
		attribs.addAttribute(properties.getUri(), "impuesto", "impuesto",
				"string", "IVA");
		attribs.addAttribute(properties.getUri(), "tasa", "tasa", "decimal",
				String.valueOf(invoice.getPorcentaje()));
		attribs.addAttribute(properties.getUri(), "importe", "importe",
				"decimal", Util_Masivo.formatNumber(this.invoice.getIva()));
			contentHandler.startElement(properties.getNameSpace(), "Traslado",
					"cfdi:Traslado", attribs);
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void endTraslado() throws SAXException {
			contentHandler.endElement(properties.getNameSpace(), "Traslado",
					"cfdi:Traslado");
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void startComplemento() throws SAXException {
		attribs = new AttributesImpl();
		contentHandler.startElement(properties.getNameSpace(), "Complemento",
				"cfdi:Complemento", attribs);
	}
	
	/**
	 * 
	 * @throws SAXException
	 */
	public void startDonatarias() throws SAXException {
		attribs = new AttributesImpl();
		attribs.addAttribute(properties.getUri(), "version", "version", "string", fe.getVersionDonataria());
		//TODO: Asignar valor desde el objeto
		attribs.addAttribute(properties.getUri(), "noAutorizacion", "noAutorizacion", "string", fe.getNoAutorizacionDonataria());
		attribs.addAttribute(properties.getUri(), "fechaAutorizacion", "fechaAutorizacion", "dateTime", new SimpleDateFormat("yyyy-MM-dd").format(fe.getFechaAutorizacionDonataria()));
		attribs.addAttribute(properties.getUri(), "leyenda", "leyenda", "string", fe.getLeyendaDonataria());
		attribs.addAttribute(properties.getUri(), "xmlns:donat", "xmlns:donat", "string", "http://www.sat.gob.mx/donat");
		contentHandler.startElement(this.getNameSpaceDonatarias(), "Donatarias", "donat:Donatarias", attribs);
	}
	
	/**
	 * 
	 * @throws SAXException
	 */
	public void endDonatarias() throws SAXException {
		attribs = new AttributesImpl();
		contentHandler.endElement(this.getNameSpaceDonatarias(), "Donatarias", "donat:Donatarias");
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void endComplemento() throws SAXException {
		contentHandler.endElement(properties.getNameSpace(), "Complemento",
				"cfdi:Complemento");
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void startAddenda() throws SAXException {
		attribs = new AttributesImpl();
		//attribs.addAttribute(properties.getUri(), "xmlns:as", "xmlns:as", "string", "http://www.santander.com.mx/schemas/xsd/AddendaSantanderV1");
			contentHandler.startElement(properties.getNameSpace(), "Addenda",
					"cfdi:Addenda", attribs);
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void endAddenda() throws SAXException {
			contentHandler.endElement(properties.getNameSpace(), "Addenda",
					"cfdi:Addenda");
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void startAddendaSantanderV1() throws SAXException {
		attribs = new AttributesImpl();
			contentHandler.startElement(properties.getNameSpaceAddenda(),
					"AddendaSantanderV1", "as:AddendaSantanderV1", attribs);
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void startInformacionPago() throws SAXException {
		attribs = new AttributesImpl();
		attribs.addAttribute(properties.getUri(), "numProveedor",
				"numProveedor", "string", invoice.getProviderNumber());
		attribs.addAttribute(properties.getUri(), "ordenCompra", "ordenCompra",
				"string", invoice.getPurchaseOrder());
		attribs.addAttribute(properties.getUri(), "nombreBeneficiario",
				"nombreBeneficiario", "string", invoice.getBeneficiaryName());
		attribs.addAttribute(properties.getUri(), "institucionReceptora",
				"institucionReceptora", "string", invoice.getReceivingInstitution());
		attribs.addAttribute(properties.getUri(), "numeroCuenta",
				"numeroCuenta", "string", invoice.getAccountNumber());
		agregaAtributo("email", "string", invoice.getEmail());
		contentHandler.startElement(properties.getNameSpaceAddenda(),
				"InformacionPago", "as:InformacionPago", attribs);

	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void endInformacionPago() throws SAXException {
		contentHandler.endElement(properties.getNameSpaceAddenda(), "InformacionPago",
				"as:InformacionPago");
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void startInformacionEmision() throws SAXException {
		attribs = new AttributesImpl();
		agregaAtributo("codigoCliente", "string", invoice.getCustomerCode());
		//attribs.addAttribute(properties.getUri(), "codigoCliente", "codigoCliente", "string", invoice.getCustomerCode());
		agregaAtributo("contrato", "string", invoice.getContractNumber());
		//attribs.addAttribute(properties.getUri(), "contrato", "contrato", "string", invoice.getContractNumber());
		agregaAtributo("periodo", "string", invoice.getPeriod());
		//attribs.addAttribute(properties.getUri(), "periodo", "periodo", "string", invoice.getPeriod());
		agregaAtributo("centroCostos", "string", invoice.getCostCenter());
		//attribs.addAttribute(properties.getUri(), "centroCostos", "centroCostos", "string", invoice.getCostCenter());
		agregaAtributo("folioInterno", "string", "");
		//attribs.addAttribute(properties.getUri(), "folioInterno", "folioInterno", "string", "");
		agregaAtributo("claveSantander", "string", "");
		//attribs.addAttribute(properties.getUri(), "claveSantander", "claveSantander", "string", "");
			contentHandler.startElement(properties.getNameSpaceAddenda(),
					"InformacionEmision", "as:InformacionEmision", attribs);
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void startInformacionFactoraje() throws SAXException {
		attribs = new AttributesImpl();
		agregaAtributo("deudorProveedor", "string", "");
		agregaAtributo("tipoDocumento", "string", "");
		agregaAtributo("numeroDocumento", "string", "");
		agregaAtributo("fechaVencimiento", "string", "");
		agregaAtributo("plazo", "decimal", "");
		agregaAtributo("valorNominal", "decimal", "");
		agregaAtributo("aforo", "decimal", "");
		agregaAtributo("precioBase", "decimal", "");
		agregaAtributo("tasaDescuento", "decimal", "");
		agregaAtributo("precioFactoraje", "decimal", "");
		agregaAtributo("importeDescuento", "decimal", "");
			contentHandler.startElement(properties.getNameSpaceAddenda(),
					"InformacionFactoraje", "as:InformacionFactoraje", attribs);
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void endInformacionFactoraje() throws SAXException {
			contentHandler.endElement(properties.getNameSpaceAddenda(),
					"InformacionFactoraje", "as:InformacionFactoraje");
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void endInformacionEmision() throws SAXException {
			contentHandler.endElement(properties.getNameSpaceAddenda(),
					"InformacionEmision", "as:InformacionEmision");
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void startCampoAdicional() throws SAXException {
		attribs = new AttributesImpl();
		attribs.addAttribute(properties.getUri(), "campo", "campo", "string",
				this.campo);
		attribs.addAttribute(properties.getUri(), "valor", "valor", "string",
				this.valor);
			contentHandler.startElement(properties.getNameSpaceAddenda(),
					"CampoAdicional", "as:CampoAdicional", attribs);
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void endCampoAdicional() throws SAXException {
			contentHandler.endElement(properties.getNameSpaceAddenda(), "CampoAdicional",
					"as:CampoAdicional");
	}

	/**
	 * 
	 * @throws SAXException
	 */
	public void endAddendaSantanderV1() throws SAXException {
			contentHandler.endElement(properties.getNameSpaceAddenda(),
					"AddendaSantanderV1", "as:AddendaSantanderV1");
	}

	public FiscalEntity getFe() {
		return fe;
	}

	public void setFe(FiscalEntity fe) {
		this.fe = fe;
	}

	/**
	 * 
	 * @param invoice
	 * @param fe
	 * @param fecha
	 * @return
	 * @throws Exception
	 */
	public String generaXMLHandler(Invoice_Masivo invoice, FiscalEntity fe, String fecha, boolean donataria)
			throws Exception {
		logger.debug("Generando XML");
		// ***** Genera XML (Inicio)*****//
		this.invoice = invoice;
		this.fe = fe;
		this.donataria = donataria;
		SAXSource saxSource = new SAXSource(this, null);
		saxSource.setXMLReader(this);
		SAXTransformerFactory tr = (SAXTransformerFactory) SAXTransformerFactory
				.newInstance();
		out = new ByteArrayOutputStream();
		TransformerHandler trH = tr.newTransformerHandler();
		trH.setResult(new StreamResult(out));

		Transformer trans = tr.newTransformer();
		SAXResult saxR = new SAXResult(trH);
		trans.transform(saxSource, saxR);
		
		// ***** Genera XML (Fin)*****//
		certificado.find(fe.getId());
		if(out.size()==0){
			logger.debug("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ Archivo Vacio");
			System.out.println("El archivo esta vacio ++++");
		} else {
			logger.debug("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ Archivo");
			logger.debug("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ Validando Archivo");
			System.out.println("El archivo pesa ++++ "+out.size());
			xmlProcess.validaCFDI32(out);
		}
		
		return xmlProcess.generateFileName(fecha, false, 0, false);
	}

	/**
	 * 
	 * @param path
	 * @param fecha
	 * @throws IOException
	 * @throws GeneralSecurityException
	 * @throws TransformerException
	 */
	/*
	public void guarda(String path, String fecha) throws IOException,
			GeneralSecurityException, TransformerException {
		logger.debug("Fecha:"+fecha);
		try {
			xmlProcess.writeHardDrive(
					xmlProcess.replacesOriginalString(out,
							xmlProcess.generatesOriginalString(out, "3.2"), certificado),
					fecha, false, 0, false);
		} catch (ValidationException e) {
			e.printStackTrace();
		}
	}
	*/
	public ByteArrayOutputStream guarda(String path, String fecha) throws IOException,
			GeneralSecurityException, TransformerException, ValidationException {
		logger.debug("Fecha:"+fecha);
		//try {
			/*xmlProcess.writeHardDrive(
					xmlProcess.replacesOriginalString(out,
							xmlProcess.generatesOriginalString(out, "3.2"), certificado),
					fecha, false, 0, false);*/
			System.out.println("start save XML (ByteArray)");
			ByteArrayOutputStream xmlSinAddenda = xmlProcess.replacesOriginalString(out,
							xmlProcess.generatesOriginalString(out, "3.2"), certificado);
					
			System.out.println("xmlSinAddenda(ByteArray): " + xmlSinAddenda.toString("UTF-8"));
			return xmlSinAddenda;
		/*} catch (ValidationException e) {
			e.printStackTrace();
		}
		return null;*/
	}
	
	public void cambiaFolio(String path, String folioInterno) {
		try {
			File file = new File(path);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document dom = db.parse(file);
			
			Element docEle = dom.getDocumentElement();
			docEle.setAttribute("folio", folioInterno);
			
			Source source = new DOMSource(docEle);
			Result result2 = new StreamResult(file);
			Transformer xformer;
			
			xformer = TransformerFactory.newInstance().newTransformer();
			xformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			xformer.transform(source, result2);
				
		} catch (TransformerException e) {
			e.printStackTrace();
		}  catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//public void timbraFactura(String path, Invoice_Masivo invoice) throws IOException, FileNotFoundException, UnsupportedEncodingException, webServiceResponseException, TransformerException, webServiceConnectionException {
	public void timbraFactura(String path, Invoice_Masivo invoice) throws IOException, FileNotFoundException, UnsupportedEncodingException, TransformerException {
		File file = new File(path);
		//webServiceCliente ws = new webServiceCliente();
		//ws.generaTimbre(file);
		
		Document doc = verificaStatusCFDI(file);
		doc = agregaAddenda(doc, invoice);
		this.UUID = getUUID(doc);
		Source source = new DOMSource(doc);
		Result result2 = new StreamResult(file);
		Transformer xformer;
		try {
			xformer = TransformerFactory.newInstance().newTransformer();
			xformer.transform(source, result2);
			
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		}
	
	}
	
	public String getUUID(Document doc) {
		Element element = doc.getDocumentElement();
		
		NodeList nl = element.getElementsByTagName("tfd:TimbreFiscalDigital");
		
		if(nl != null && nl.getLength() > 0) {
			Element el = (Element) nl.item(0);
			return el.getAttribute("UUID");
		}
		return "";
	}
	
	public Document agregaAddenda(Document doc, Invoice_Masivo invoice) {
		Element element = doc.getDocumentElement();
		
		Element addenda = doc.createElement("cfdi:Addenda");
		addenda.setAttribute("xmlns:as", "http://www.santander.com.mx/schemas/xsd/AddendaSantanderV1");
		addenda.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		
		element.appendChild(addenda);
		Element addendaSantander = doc.createElement("as:AddendaSantanderV1");
		addendaSantander.setAttribute("xmlns:as", "http://www.santander.com.mx/schemas/xsd/AddendaSantanderV1");
		addendaSantander.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		
		//Nodo InformacionPago
		if(!Util_Masivo.isNullEmpty(invoice.getBeneficiaryName())
				||!Util_Masivo.isNullEmpty(invoice.getAccountNumber())
				||!Util_Masivo.isNullEmpty(invoice.getProviderNumber())
				||!Util_Masivo.isNullEmpty(invoice.getPurchaseOrder())
				||!Util_Masivo.isNullEmpty(invoice.getReceivingInstitution())){
			Element informacionPago = doc.createElement("as:InformacionPago");
			informacionPago.setAttribute("numProveedor", invoice.getProviderNumber());
			informacionPago.setAttribute("ordenCompra", invoice.getPurchaseOrder());
			informacionPago.setAttribute("nombreBeneficiario", invoice.getBeneficiaryName());
			informacionPago.setAttribute("institucionReceptora", invoice.getReceivingInstitution());
			informacionPago.setAttribute("numeroCuenta", invoice.getAccountNumber());
			if(invoice.getEmail() != null){
				if(!invoice.getEmail().trim().equals("")){
					informacionPago.setAttribute("email", invoice.getEmail());
				}
			}
			
			
			addendaSantander.appendChild(informacionPago);
		}
		//Nodo InformacionEmision
		Element informacionEmision = doc.createElement("as:InformacionEmision");
		informacionEmision = agregaAtributoDOM("codigoCliente", invoice.getCustomerCode(), informacionEmision);
		informacionEmision = agregaAtributoDOM("contrato", invoice.getContractNumber(), informacionEmision);
		informacionEmision = agregaAtributoDOM("periodo", invoice.getPeriod(), informacionEmision);
		informacionEmision = agregaAtributoDOM("centroCostos", invoice.getCostCenter(), informacionEmision);
		informacionEmision = agregaAtributoDOM("folioInterno", "", informacionEmision);
		informacionEmision = agregaAtributoDOM("claveSantander", "", informacionEmision);
		
		addendaSantander.appendChild(informacionEmision);
		
		//Nodo CampoAdicional
		this.campo = "Moneda";
		this.valor = invoice.getTipoMoneda();
		
		Element campoAdicional1 = doc.createElement("as:CampoAdicional");
		campoAdicional1.setAttribute("campo", this.campo);
		campoAdicional1.setAttribute("valor", this.valor);
		
		addendaSantander.appendChild(campoAdicional1);
		
		this.campo = "Tipo Cambio";
		this.valor = String.valueOf(invoice.getExchange());
		
		Element campoAdicional2 = doc.createElement("as:CampoAdicional");
		campoAdicional2.setAttribute("campo", this.campo);
		campoAdicional2.setAttribute("valor", this.valor);
		
		addendaSantander.appendChild(campoAdicional2);

        //Se agrega campo adicional a la addenda de complemento de leyenda.
        this.campo = "leyendaDonataria1";
        this.valor = "EN EL CASO DE QUE LOS BIENES DONADOS HAYAN SIDO DEDUCIDOS PREVIAMENTE PARA LOS EFECTOS DEL IMPUESTO" +
                " SOBRE LA RENTA, ESTE DONATIVO NO ES DEDUCIBLE.";
        Element campoAdicionalLeyenda = doc.createElement("as:CampoAdicional");
        campoAdicionalLeyenda.setAttribute("campo", this.campo);
        campoAdicionalLeyenda.setAttribute("valor", this.valor);
        addendaSantander.appendChild(campoAdicionalLeyenda);

        //Se agrega campo adicional a la addenda de numero de empleado.
        if(!Util_Masivo.isNullEmpty(invoice.getNumeroEmpleado())) {
            this.campo = "numeroEmpleado";
            this.valor = invoice.getNumeroEmpleado();
            Element numeroEmpleado = doc.createElement("as:CampoAdicional");
            numeroEmpleado.setAttribute("campo", this.campo);
            numeroEmpleado.setAttribute("valor", this.valor);
            addendaSantander.appendChild(numeroEmpleado);
        }

		if(!Util_Masivo.isNullEmpty(invoice.getDescriptionConcept())) {
			this.campo = "Descripcion Concepto";
			this.valor = invoice.getDescriptionConcept();
			
			Element campoAdicional3 = doc.createElement("as:CampoAdicional");
			campoAdicional3.setAttribute("campo", this.campo);
			campoAdicional3.setAttribute("valor", this.valor);
			
			addendaSantander.appendChild(campoAdicional3);
		}
		
		if(!Util_Masivo.isNullEmpty(invoice.getDescriptionIVA())) {
			this.campo = "Descripci\u00F3n IVA";
			this.valor = invoice.getDescriptionIVA();
			
			Element campoAdicional4 = doc.createElement("as:CampoAdicional");
			campoAdicional4.setAttribute("campo", this.campo);
			campoAdicional4.setAttribute("valor", this.valor);
			
			addendaSantander.appendChild(campoAdicional4);
		}
		
		if(!Util_Masivo.isNullEmpty(invoice.getIdExtranjero())) {
			this.campo = "Id Extranjero";
			this.valor = invoice.getIdExtranjero();
			
			Element campoAdicional5 = doc.createElement("as:CampoAdicional");
			campoAdicional5.setAttribute("campo", this.campo);
			campoAdicional5.setAttribute("valor", this.valor);
			
			addendaSantander.appendChild(campoAdicional5);
		}
		
		addenda.appendChild(addendaSantander);
		
		return doc;
	}
	
	public Element agregaAtributoDOM(String field, String value, Element element) {
		if(!Util_Masivo.isNullEmpty(value)) {
			element.setAttribute(field, value);
		}
		return element;
	}
	
	//public Document verificaStatusCFDI(File file) throws webServiceResponseException, TransformerException {
	public Document verificaStatusCFDI(File file) throws TransformerException {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document dom = db.parse(file);
			
			//Se verifica si la respuesta del web service es correcta, sino se lanza excepcion
			Element docEle = dom.getDocumentElement();
			String descripcion = docEle.getAttribute("Descripcion");
			String idRespuesta = docEle.getAttribute("IdRespuesta");
			if(descripcion.toLowerCase().trim().equals("ok") && idRespuesta.equals("1")) {
				Transformer transformer = TransformerFactory.newInstance().newTransformer();
				StreamResult result = new StreamResult(new StringWriter());
				DOMSource source = new DOMSource(docEle.getFirstChild());
				transformer.transform(source, result);
				String xmlString = result.getWriter().toString();
				Document doc = db.parse(new InputSource(new StringReader(xmlString)));
				return doc;
			} else {
				//throw new webServiceResponseException(descripcion);
			}
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * @param name
	 * @param type
	 * @param value
	 */
	public void agregaAtributo(String name, String type, String value) {
		if (!isNullEmpty(value)) {
			attribs.addAttribute(properties.getUri(), name, name, type, value);
		}
	}

	public XMLProcess_Masivo getXmlProcess() {
		return xmlProcess;
	}

	public void setXmlProcess(XMLProcess_Masivo xmlProcess) {
		this.xmlProcess = xmlProcess;
	}

	public boolean isDonataria() {
		return donataria;
	}

	public void setDonataria(boolean donataria) {
		this.donataria = donataria;
	}

	public String getUUID() {
		return UUID;
	}

	public void setUUID(String uUID) {
		UUID = uUID;
	}

	public String getNameSpaceDonatarias() {
		return nameSpaceDonatarias;
	}

	public void setNameSpaceDonatarias(String nameSpaceDonatarias) {
		this.nameSpaceDonatarias = nameSpaceDonatarias;
	}
	
	public ByteArrayOutputStream getOut(){
		return out;
	}
	public void setOut(ByteArrayOutputStream out){
		this.out = out;
	}

}
