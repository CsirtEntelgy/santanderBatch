package com.interfactura.firmalocal.xml.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.interfactura.firmalocal.datamodel.CustomsInformation;
import com.interfactura.firmalocal.datamodel.ElementsInvoice;
import com.interfactura.firmalocal.datamodel.FarmAccount;
import com.interfactura.firmalocal.datamodel.Invoice_Masivo;
import com.interfactura.firmalocal.datamodel.Part;
import com.interfactura.firmalocal.datamodel.TimbreFiscal;

import mx.gob.sat.cfd.x3.ComprobanteDocument;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante.Conceptos.Concepto;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante.Conceptos.Concepto.CuentaPredial;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante.Conceptos.Concepto.Parte;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante.Impuestos.Traslados.Traslado;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante.Receptor;
import mx.gob.sat.cfd.x3.TCampoAdicional;
import mx.gob.sat.cfd.x3.TInformacionAduanera;
import mx.gob.sat.cfd.x3.TUbicacion;


public class ParseXML_CFDI {

    private Logger logger = Logger.getLogger(ParseXML_CFDI.class);
    private Invoice_Masivo invoice;

    public ParseXML_CFDI() {

    }
    
    
	public Invoice_Masivo parseQuitas(File xmlFile) throws Exception {
    	
        //FileCopyUtils.copy(new FileInputStream(xmlFile), outA);
    	DocumentBuilderFactory docbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder docb = docbf.newDocumentBuilder();
    	Document xml = docb.parse(xmlFile);
    	
    	/*********************************************************************
    	 * Datos principales del comprobante
    	 */
    	 InputStreamReader isreader = new InputStreamReader(new FileInputStream(xmlFile), "UTF-8");
         BufferedReader fr = new BufferedReader(isreader);
         
         StringBuilder s = new StringBuilder();
         while (fr.ready()) {
             s.append(fr.readLine());
         }
         fr.close();
         ComprobanteDocument compDoc = ComprobanteDocument.Factory.parse(
                 s.toString().replace("xmlns=\"http://www.santander.com.mx/schemas/xsd/AddendaSantanderV1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"", ""));
         
    	Element root = xml.getDocumentElement();
    	Element impuestos = null;
    	Element conceptos = null;
    	NodeList comprobantesChilds = root.getChildNodes();
		for (int i = 0; i < comprobantesChilds.getLength(); i++) {
			Node nodo = comprobantesChilds.item(i);
			if (nodo.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) nodo;
				if (element.getNodeName().equalsIgnoreCase("cfdi:Conceptos")) {
					conceptos = element;
				} else if (element.getNodeName().equalsIgnoreCase("cfdi:Impuestos")) {
					impuestos = element;
				}
			}
		}
    	
       
        invoice = new Invoice_Masivo();
        invoice.setPorcentaje(0.00);

        invoice.setVersion(compDoc.getComprobante().getVersion());
        
        if (compDoc.getComprobante().getImpuestos().getTraslados() != null) {
        	NodeList trasladoList = impuestos.getChildNodes();
    		for (int i = 0; i < trasladoList.getLength(); i++) {
    			Node nodo = trasladoList.item(i);
    			if (nodo.getNodeType() == Node.ELEMENT_NODE) {
    				Element element = (Element) nodo;
    				if (element.getNodeName().equalsIgnoreCase("cfdi:Traslado")) {
    					if (element.getAttribute("Impuesto").equalsIgnoreCase(String.valueOf( Traslado.Impuesto.IVA))) {
    						invoice.setPorcentaje(Double.parseDouble(element.getAttribute("TasaOCuota")));    						
    					}
    				}
    			}
    		}
        }

        //Comprobante comprobante = compDoc.getComprobante();
        //asignaInformacionInvoice(compDoc);
        asignaInformacionInvoiceQuitas(xmlFile);
        //asignaConceptos(comprobante);
        asignaConceptosQuitas(xmlFile, conceptos);
        asignaElementosFaltantes(xmlFile);
        return invoice;
    }
	
	
	public void asignaInformacionInvoiceQuitas(File xmlFile) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document dom = db.parse(xmlFile);
        Element comprobante = dom.getDocumentElement();
        Element impuestos = null;
    	Element emisor = null;
    	Element receptor = null;
    	Element conceptos = null;
    	Element complementos = null;
    	Element addenda = null;
    	NodeList comprobantesChilds = comprobante.getChildNodes();
		for (int i = 0; i < comprobantesChilds.getLength(); i++) {
			Node nodo = comprobantesChilds.item(i);
			if (nodo.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) nodo;
				if (element.getNodeName().equalsIgnoreCase("cfdi:Emisor")) {
					emisor = element;
				} else if (element.getNodeName().equalsIgnoreCase("cfdi:Receptor")) {
					receptor = element;
				} else if (element.getNodeName().equalsIgnoreCase("cfdi:Conceptos")) {
					conceptos = element;
				} else if (element.getNodeName().equalsIgnoreCase("cfdi:Impuestos")) {
					impuestos = element;
				} else if (element.getNodeName().equalsIgnoreCase("cfdi:Complemento")) {
					complementos = element;
				} else if (element.getNodeName().equalsIgnoreCase("cfdi:Addenda")) {
					addenda = element;
				}
			}
		}
        /*************************************************************************++
		 * Cabecera del comprobante
		 */
		invoice.setNoCertificado(comprobante.getAttribute("NoCertificado"));
		if (!comprobante.getAttribute("Descuento").equalsIgnoreCase("") && comprobante.getAttribute("Descuento") != null)
			invoice.setDescuento(Double.parseDouble(comprobante.getAttribute("Descuento")));
		else
			invoice.setDescuento(0.00);
		invoice.setFechaHora(comprobante.getAttribute("Fecha"));
		invoice.setFormaPago(comprobante.getAttribute("FormaPago"));
		invoice.setLugarExpedicion(comprobante.getAttribute("LugarExpedicion"));
		invoice.setMetodoPago(comprobante.getAttribute("MetodoPago"));
		invoice.setMoneda(comprobante.getAttribute("Moneda"));
		invoice.setSello(comprobante.getAttribute("Sello"));
		invoice.setSerie(comprobante.getAttribute("Serie"));
		invoice.setSubTotal(Double.parseDouble(comprobante.getAttribute("SubTotal")));
		invoice.setTipoCambio(comprobante.getAttribute("TipoCambio"));
		invoice.setTipoDeComprobante(comprobante.getAttribute("TipoDeComprobante"));
		invoice.setTotal(Double.parseDouble(comprobante.getAttribute("Total")));
		invoice.setVersion(comprobante.getAttribute("Version"));
		invoice.setDate(comprobante.getAttribute("Fecha"));
		invoice.setQuantityWriting(NumberToLetterConverter.convertNumberToLetter(invoice.getTotal()));
		/****************************************************************************
		 * Datos emisor
		 */
		String direccion = emisor.getAttribute("Nombre")
				+ " " + emisor.getAttribute("RegimenFiscal")
				+ " R.F.C."
                + emisor.getAttribute("Rfc");
		invoice.setDireccion(direccion);
		
		/****************************************************************************
		 * Datos Receptor
		 */
		invoice.setRfc(receptor.getAttribute("Rfc"));
        //invoice.setFormaPago(comprobante.getFormaDePago());
        invoice.setName(receptor.getAttribute("Nombre"));
        invoice.setUsoCFDI(receptor.getAttribute("UsoCFDI"));
        /*invoice.setCalle(tUbicacion.getCalle());
        invoice.setCodigoPostal(tUbicacion.getCodigoPostal());
        invoice.setColonia(tUbicacion.getColonia());
        invoice.setEstado(tUbicacion.getEstado());
        invoice.setExterior(tUbicacion.getNoExterior());
        invoice.setInterior(tUbicacion.getNoInterior());
        invoice.setMunicipio(tUbicacion.getMunicipio());
        invoice.setReferencia(tUbicacion.getReferencia());
        invoice.setLocalidad(tUbicacion.getLocalidad());
        invoice.setPais(tUbicacion.getPais());*/
        if (impuestos != null) {
        	if (impuestos.getAttribute("TotalImpuestosTrasladados") != null && !impuestos.getAttribute("TotalImpuestosTrasladados").equalsIgnoreCase(""))
        		invoice.setIva(Double.parseDouble(impuestos.getAttribute("TotalImpuestosTrasladados")));
        	else
        		invoice.setIva(0.00);
        } else 
        	invoice.setIva(0.00);
        
		
	}
    
	public void asignaConceptosQuitas(File xmlFile, Element conceptos) throws Exception {
		List<ElementsInvoice> elements = new ArrayList<ElementsInvoice>();
		NodeList nl = conceptos.getElementsByTagName("cfdi:Concepto");
        if (nl != null && nl.getLength() > 0) {
            for (int i = 0; i < nl.getLength(); i++) {
                Element concepto = (Element) nl.item(i);
                ElementsInvoice element = new ElementsInvoice();
                element.setAmount(Double.parseDouble(concepto.getAttribute("Importe")));
                element.setDescription(concepto.getAttribute("DEscripcion"));
                element.setQuantity(Double.parseDouble(concepto.getAttribute("Cantidad")));
                element.setUnitMeasure(concepto.getAttribute("Unidad"));
                element.setUnitPrice(Double.parseDouble(concepto.getAttribute("ValorUnitario")));
                //invoice.setRegimenFiscal(el.getAttribute("Regimen"));
                elements.add(element);
            }
            
        }
        invoice.setElements(elements);
	}

    public Invoice_Masivo parse(File xmlFile) throws Exception {        
        //FileCopyUtils.copy(new FileInputStream(xmlFile), outA);
        InputStreamReader isreader = new InputStreamReader(new FileInputStream(xmlFile), "UTF-8");
        BufferedReader fr = new BufferedReader(isreader);

        StringBuilder s = new StringBuilder();
        while (fr.ready()) {
            s.append(fr.readLine());
        }
        fr.close();
        ComprobanteDocument compDoc = ComprobanteDocument.Factory.parse(
                s.toString().replace("xmlns=\"http://www.santander.com.mx/schemas/xsd/AddendaSantanderV1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"", ""));

        invoice = new Invoice_Masivo();
        invoice.setPorcentaje(0.00);

        invoice.setVersion(compDoc.getComprobante().getVersion());
        
        if (compDoc.getComprobante().getImpuestos().getTraslados() != null) {        	
            for (Traslado objT : compDoc.getComprobante().getImpuestos().getTraslados().getTrasladoArray()) {
            	
            	if (objT != null)
            		System.out.println(objT.getImpuesto()+" importeImpuestos = " +objT.getImporte());
            	else
            		System.out.println("El objeto esta vacio");
                if (objT.getImpuesto().equals(Traslado.Impuesto.IVA)) {
                    invoice.setPorcentaje(objT.getTasa().doubleValue());
                }
            }
        }

        Comprobante comprobante = compDoc.getComprobante();
        asignaInformacionInvoice(compDoc);
        asignaConceptos(comprobante);
        asignaElementosFaltantes(xmlFile);
        return invoice;
    }


    private void asignaInformacionInvoice(ComprobanteDocument compDoc) {
        Comprobante comprobante = compDoc.getComprobante();
        //invoice.setMoneda("");
        invoice.setIvaDescription("");
        String encabezadoConcepto = "";
        if (comprobante.getAddenda()!= null && comprobante.getAddenda().getAddendaSantanderV1()!=null && comprobante.getAddenda().getAddendaSantanderV1().getCampoAdicionalArray() != null) {
            for (TCampoAdicional campo : comprobante.getAddenda()
                    .getAddendaSantanderV1().getCampoAdicionalArray()) {
                if (campo.getCampo().equals("Descripcion Concepto")) {
                    encabezadoConcepto = campo.getValor();
                }
                if (campo.getCampo().equals("Moneda")) {
                    //invoice.setMoneda(campo.getValor());
                }
                if (campo.getCampo().equals("Descripcion IVA")) {
                    invoice.setIvaDescription(campo.getValor());
                }
                if (campo.getCampo().equals("Tipo Cambio")) {
                    ///invoice.setTipoCambio(campo.getValor());
                }
            }
        }

        invoice.setDescriptionConcept(encabezadoConcepto);
        String direccion = compDoc.getComprobante().getEmisor().getNombre()
                + "    "
                + compDoc.getComprobante().getEmisor().getDomicilioFiscal()
                .getCalle();
        if (compDoc.getComprobante().getEmisor().getDomicilioFiscal()
                .getNoExterior() != null) {
            direccion += " "
                    + compDoc.getComprobante().getEmisor().getDomicilioFiscal()
                    .getNoExterior();
        }

        if (compDoc.getComprobante().getEmisor().getDomicilioFiscal()
                .getNoInterior() != null) {
            direccion += " "
                    + compDoc.getComprobante().getEmisor().getDomicilioFiscal()
                    .getNoInterior();
        }
        direccion += ", "
                + Util.isNull(compDoc.getComprobante().getEmisor()
                .getDomicilioFiscal().getReferencia())
                + " COL. "
                + compDoc.getComprobante().getEmisor().getDomicilioFiscal()
                .getColonia()
                + ", "
                + compDoc.getComprobante().getEmisor().getDomicilioFiscal()
                .getMunicipio()
                + ", C.P. "
                + compDoc.getComprobante().getEmisor().getDomicilioFiscal()
                .getCodigoPostal()
                + ", "
                + compDoc.getComprobante().getEmisor().getDomicilioFiscal()
                .getEstado()
                + ", "
                + compDoc.getComprobante().getEmisor().getDomicilioFiscal()
                .getPais() + " R.F.C."
                + compDoc.getComprobante().getEmisor().getRfc();

        invoice.setDireccion(direccion);
        //invoice.setNoCertificado(comprobante.getNoCertificado());
        //invoice.setFechaHora(Util.convertirFecha(comprobante.getFecha().getTime(), null));

        invoice.setFolio(comprobante.getFolio());
        //invoice.setDate(Util.convertirFecha(comprobante.getFecha().getTime()));
        Receptor receptor = comprobante.getReceptor();

        invoice.setRfc(receptor.getRfc());
        //invoice.setFormaPago(comprobante.getFormaDePago());
        invoice.setName(receptor.getNombre());
        TUbicacion tUbicacion = receptor.getDomicilio();
        invoice.setCalle(tUbicacion.getCalle());
        invoice.setCodigoPostal(tUbicacion.getCodigoPostal());
        invoice.setColonia(tUbicacion.getColonia());
        invoice.setEstado(tUbicacion.getEstado());
        invoice.setExterior(tUbicacion.getNoExterior());
        invoice.setInterior(tUbicacion.getNoInterior());
        invoice.setMunicipio(tUbicacion.getMunicipio());
        invoice.setReferencia(tUbicacion.getReferencia());
        invoice.setLocalidad(tUbicacion.getLocalidad());
        invoice.setPais(tUbicacion.getPais());
        //invoice.setSubTotal(comprobante.getSubTotal().doubleValue());
        System.out.println("--------------->TOTAL Org:" + comprobante.getTotal().doubleValue() + ":");
        //invoice.setTotal(comprobante.getTotal().doubleValue());
        
        if(comprobante.getImpuestos() != null){
        	if(comprobante.getImpuestos()
                    .getTotalImpuestosTrasladados() != null){
        		
        		invoice.setIva(comprobante.getImpuestos()
                        .getTotalImpuestosTrasladados().doubleValue());
        		
        	}else{
        		invoice.setIva(0.0);
        	}        	
        }else{
        	invoice.setIva(0.0);
        }       
       System.out.println("----------------->TOTAL:" + invoice.getTotal() + ":");
        //invoice.setQuantityWriting(NumberToLetterConverter.convertNumberToLetter(invoice.getTotal()));
        //invoice.setMetodoPago(comprobante.getMetodoDePago());
    }

    private void asignaConceptos(Comprobante comprobante) {
        List<ElementsInvoice> elements = new ArrayList<ElementsInvoice>();
        for (Concepto objConcepto : comprobante.getConceptos().getConceptoArray()) {
            ElementsInvoice element = new ElementsInvoice();
            List<CustomsInformation> informacionAduanera = new ArrayList<CustomsInformation>();
            List<FarmAccount> cuentaPredial = new ArrayList<FarmAccount>();
            List<Part> partes = new ArrayList<Part>();
            element.setAmount(objConcepto.getImporte().doubleValue());
            element.setDescription(objConcepto.getDescripcion());
            element.setQuantity(objConcepto.getCantidad().doubleValue());
            element.setUnitMeasure(objConcepto.getUnidad());
            element.setUnitPrice(objConcepto.getValorUnitario().doubleValue());
            logger.debug("Concepto: " + objConcepto);
            if (objConcepto.getCuentaPredial() != null) {
                CuentaPredial cp = objConcepto.getCuentaPredial();
                FarmAccount farmAccount = new FarmAccount();
                farmAccount.setNumero(cp.getNumero());
                cuentaPredial.add(farmAccount);
                element.setCuentaPredial(cuentaPredial);
                logger.debug("--Predial: " + cuentaPredial);
            } else if ((objConcepto.getInformacionAduaneraArray() != null)
                    && (objConcepto.getInformacionAduaneraArray().length > 0)) {
                for (TInformacionAduanera ia : objConcepto.getInformacionAduaneraArray()) {
                    CustomsInformation customsInformation = new CustomsInformation();
                    customsInformation.setAduana(ia.getAduana());
                    customsInformation.setFecha(Util.convertirFecha(ia.getFecha().getTime(), "dd/MM/yyyy"));
                    customsInformation.setNumero(ia.getNumero());
                    informacionAduanera.add(customsInformation);
                }
                element.setInformacionAduanera(informacionAduanera);
                logger.debug("--Aduana: " + informacionAduanera);
            } else if (objConcepto.getParteArray() != null) {
                for (Parte p : objConcepto.getParteArray()) {
                    Part part = new Part();
                    part.setCantidad(p.getCantidad().toString());
                    part.setDescripcion(p.getDescripcion());
                    part.setImporte(p.getImporte().toString());
                    part.setNoIdentificacion(p.getNoIdentificacion());
                    part.setUnidad(p.getUnidad());
                    part.setValorUnitario(p.getValorUnitario().toString());
                    logger.debug("--Partes: " + partes);
                    // Agrega las aduanas de las partes
                    if ((p.getInformacionAduaneraArray() != null)
                            && (p.getInformacionAduaneraArray().length > 0)) {
                        List<CustomsInformation> aduanaParte = new ArrayList<CustomsInformation>();
                        for (TInformacionAduanera ia : p
                                .getInformacionAduaneraArray()) {
                            CustomsInformation parteCustoms = new CustomsInformation();
                            parteCustoms.setAduana(ia.getAduana());
                            parteCustoms.setFecha(Util.convertirFecha(ia
                                    .getFecha().getTime(), "dd/MM/yyyy"));
                            parteCustoms.setNumero(ia.getNumero());
                            aduanaParte.add(parteCustoms);
                            logger.debug("--Aduana: " + parteCustoms);
                        }
                        part.setAduana(aduanaParte);
                    }
                    partes.add(part);
                }
                element.setPartes(partes);
            }
            objConcepto.getInformacionAduaneraArray();
            objConcepto.getParteArray();
            elements.add(element);
        }
        invoice.setElements(elements);
    }

    private void asignaElementosFaltantes(File is) throws Exception{
        
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document dom = db.parse(is);
            // Se asignan al objeto invoice los nuevos elementos de CFD 22
            // se leen con SAX ya que el objeto de XML no esta actualizado
            Element docEle = dom.getDocumentElement();
            

            invoice.setMetodoPago(docEle.getAttribute("metodoDePago"));

            invoice.setLugarExpedicion(docEle.getAttribute("LugarExpedicion"));

            invoice.setNumCtaPago(docEle.getAttribute("NumCtaPago"));

            NodeList nl = docEle.getElementsByTagName("cfdi:RegimenFiscal");
            if (nl != null && nl.getLength() > 0) {
                for (int i = 0; i < nl.getLength(); i++) {
                    Element el = (Element) nl.item(i);
                    invoice.setRegimenFiscal(el.getAttribute("Regimen"));
                }
            }
            //Datos del timbre fiscal digital
            nl = docEle.getElementsByTagName("tfd:TimbreFiscalDigital");
            TimbreFiscal timbre = new TimbreFiscal();
            if (nl != null && nl.getLength() > 0) {
                Element el = (Element) nl.item(0);
                timbre.setVersion(el.getAttribute("version"));
                timbre.setUuid(el.getAttribute("UUID"));
                invoice.setSello(el.getAttribute("selloCFD"));
                timbre.setSelloSat(el.getAttribute("selloSAT"));
                timbre.setNoCertificadoSat(el.getAttribute("noCertificadoSAT"));
                timbre.setFechaTimbrado(el.getAttribute("FechaTimbrado"));
            }
            invoice.setTimbreFiscal(timbre);
            //InformacionEmision centroCostos codigoCliente contrato periodo
            nl = docEle.getElementsByTagName("as:InformacionEmision");
            if (nl != null && nl.getLength() > 0) {
                Element el = (Element) nl.item(0);
                invoice.setCostCenter(el.getAttribute("centroCostos"));
                invoice.setCustomerCode(el.getAttribute("codigoCliente"));
                invoice.setContractNumber(el.getAttribute("contrato"));
                invoice.setPeriod(el.getAttribute("periodo"));
            }
            //Datos Addenda
            nl = docEle.getElementsByTagName("as:CampoAdicional");
            if (nl != null && nl.getLength() > 0) {
                for (int i = 0; i < nl.getLength(); i++) {
                    Element el = (Element) nl.item(i);
                    String campo = el.getAttribute("campo");
                    String valor = el.getAttribute("valor");
                    if (campo.equals("Moneda")) {
                        invoice.setMoneda(valor);
                    }
                    if (campo.equals("Tipo Cambio")) {
                        invoice.setTipoCambio(valor);
                    }
                    if (campo.equals("Descripci\u00F3n IVA")) {
                        invoice.setIvaDescription(valor);
                    }
                    if (campo.equals("Descripcion Concepto")) {
                        invoice.setDescriptionConcept(valor);
                    }
                }
            }
            //Datos Addendas Filiales
            invoice.setTipoAddenda("0");
            nl = docEle.getElementsByTagName("asant:InformacionPago");
            if (nl != null && nl.getLength() > 0) {
                Element el = (Element) nl.item(0);
                //Va para las tres addendas filiales
                System.out.println("CodigoISOMoneda:" + el.getAttribute("codigoISOMoneda"));
                invoice.setCodigoISO(el.getAttribute("codigoISOMoneda"));
                //Va para la addenda logistica
                if(!el.getAttribute("posCompra").equals("")){
                	invoice.setTipoAddenda("1");
                	System.out.println("posCompra:" + el.getAttribute("posCompra"));
                	invoice.setPosicioncompraLog(el.getAttribute("posCompra"));
                	System.out.println("Addenda Filial: Logistica");
                }
                
                
                //Va para la addenda financiera
                if(!el.getAttribute("cuentaContable").equals("")){
                	invoice.setTipoAddenda("2");
                	System.out.println("cuentaContable:" + el.getAttribute("cuentaContable"));
                	invoice.setCuentacontableFin(el.getAttribute("cuentaContable"));                	
                	System.out.println("Addenda Filial: Financiera");
                }                
                
                                
            }
            //Va para la addenda arrendamiento
            nl = docEle.getElementsByTagName("asant:Inmuebles");
            if (nl != null && nl.getLength() > 0) {
                Element el = (Element) nl.item(0);
                if(!el.getAttribute("numContrato").equals("") && !el.getAttribute("fechaVencimiento").equals("")){
                	invoice.setTipoAddenda("3");
                	System.out.println("numContrato:" + el.getAttribute("numContrato"));
                	invoice.setNumerocontratoArr(el.getAttribute("numContrato"));
                    System.out.println("fechaVencimiento:" + el.getAttribute("fechaVencimiento"));
                    invoice.setFechavencimientoArr(el.getAttribute("fechaVencimiento"));
                    System.out.println("Addenda Filial: Arrendamiento");
                }            
                
            }
            
            //Obtener tipoDeComprobante
            System.out.println("tipoDeComprobante:" + docEle.getAttribute("tipoDeComprobante"));
            invoice.setTipoDeComprobante(docEle.getAttribute("tipoDeComprobante"));
            
            //Obtener serie
            System.out.println("serie:" + docEle.getAttribute("serie"));
            invoice.setSerie(docEle.getAttribute("serie"));            
            
            //Obtener información beneficiario
            nl = docEle.getElementsByTagName("as:InformacionPago");
            if (nl != null && nl.getLength() > 0) {
                Element el = (Element) nl.item(0);
                
                System.out.println("email:" + el.getAttribute("email"));
                invoice.setEmail(el.getAttribute("email"));
                System.out.println("ordenCompra:" + el.getAttribute("ordenCompra"));
                invoice.setPurchaseOrder(el.getAttribute("ordenCompra"));
                System.out.println("nombreBeneficiario:" + el.getAttribute("nombreBeneficiario"));
                invoice.setBeneficiaryName(el.getAttribute("nombreBeneficiario"));
                System.out.println("institucionReceptora:" + el.getAttribute("institucionReceptora"));
                invoice.setReceivingInstitution(el.getAttribute("institucionReceptora"));
                System.out.println("numeroCuenta:" + el.getAttribute("numeroCuenta"));
                invoice.setAccountNumber(el.getAttribute("numeroCuenta"));
                System.out.println("numProveedor:" + el.getAttribute("numProveedor"));          
                invoice.setProviderNumber(el.getAttribute("numProveedor"));
            }
            
            
            //Obtener información Tipo de Operacion (Divisas)            
            nl = docEle.getElementsByTagName("divisas:Divisas");
            if (nl != null && nl.getLength() > 0) {
                Element el = (Element) nl.item(0);
                
                System.out.println("tipoOperacion:" + el.getAttribute("tipoOperacion"));
                invoice.setTipoOperacion(el.getAttribute("tipoOperacion"));
                
            }
            
    	}

    }

