package com.interfactura.firmalocal.generapdf.impl;

import com.interfactura.firmalocal.datamodel.*;
import com.interfactura.firmalocal.domain.entities.CFDIssued;
import com.interfactura.firmalocal.domain.entities.Logo;
import com.interfactura.firmalocal.domain.entities.Moneda;
import com.interfactura.firmalocal.generapdf.impl.PDFCreationImpl;
import com.interfactura.firmalocal.generapdf.util.UtilPDF;
import com.interfactura.firmalocal.ondemand.search.impl.BusquedaOnDemandImp;
import com.interfactura.firmalocal.persistence.CFDIssuedManager;
import com.interfactura.firmalocal.persistence.LogoManager;
import com.interfactura.firmalocal.persistence.MonedaManager;
import com.interfactura.firmalocal.xml.Properties;
import com.interfactura.firmalocal.xml.util.ParseXML_CFDI_PDF;
import com.interfactura.firmalocal.xml.util.Util;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.AcroFields.FieldPosition;
import com.itextpdf.text.pdf.qrcode.EncodeHintType;
import com.itextpdf.text.pdf.qrcode.ErrorCorrectionLevel;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerException;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.interfactura.firmalocal.generapdf.util.UtilPDF.path;
import static com.interfactura.firmalocal.generapdf.util.UtilPDF.templateSantanderCFDI;


@Component
public class PDFCreation32Impl {

    private PdfCopyFields copy;
    private PdfStamper stamper;
    private Logger logger = Logger.getLogger(PDFCreationImpl.class);
    @Autowired
    private Properties properties;
    @Autowired(required = true)
    private MonedaManager monedaManager;
    private Invoice_PDF invoice;
    private String rfc = "";

    private int conceptTotalRows = -1;
    private String[] remainingRows = new String[0];
    private String remainingAmount = null;
    private String remainingMU = null;
    private String remainingQuantity = null;
    private String remainingUnitPrice = null;
    private int numberConcept = 23;
    private long fiscalEntityId = 0;

    
    //@Value("${ondemand.serverName}")
    private String serverName;
    //@Value("${ondemand.userName}")
    private String userName;
    //@Value("${ondemand.password}")
    private String password;
    //@Value("${ondemand.folderNameInterEmision}")
    private String folderNameInterEmision;
    //@Value("${ondemand.folderNameInterRecepcion}")
    private String folderNameInterRecepcion;
    //@Value("${ondemand.folderNameInterEstadoCuenta}")
    private String folderNameInterEstadoCuenta;
    //@Value("${ondemand.rutaArchivo}")
    private String rutaArchivo;
    //@Value("${ondemand.nombreArchivo}")
    private String nombreArchivo;
    //@Value("${ondemand.extencionArchivo}")
    private String extencionArchivo;

    @Autowired(required = true)
    private LogoManager logoManager;
    @Autowired(required = true)
    private CFDIssuedManager cFDIssuedManager;


    public PDFCreation32Impl() {

    }

    /**
     * @return ByteArrayOutputStream
     * @throws DocumentException DocumentException
     * @throws IOException       IOException
     */
    private ByteArrayOutputStream create() throws DocumentException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copy = new PdfCopyFields(out);
        copy.open();
        this.concatenate();
        copy.close();
        return out;
    }

    /**
     * @throws IOException       IOException
     * @throws DocumentException DocumentException
     */
    private void concatenate() throws IOException, DocumentException {
        PdfReader pdfTemplate = new PdfReader(path + templateSantanderCFDI);

        PdfReader copyR = new PdfReader(pdfTemplate);
        ByteArrayOutputStream tmpOUT = new ByteArrayOutputStream();
        stamper = new PdfStamper(copyR, tmpOUT);
        stamper.setFormFlattening(true);

        // Proceso el documento la primera vez. Inicializo variables de control
        this.conceptTotalRows = -1;
        this.remainingRows = new String[0];
        this.remainingAmount = null;
        this.remainingMU = null;
        this.remainingQuantity = null;
        this.remainingUnitPrice = null;

        this.header();
        this.conceptos(invoice.getElements(), 0, 0);
        this.footer();


        stamper.close();
        copyR.close();

        copy.addDocument(new PdfReader(new ByteArrayInputStream(tmpOUT.toByteArray())));
        // Verifico si se requiere mas de una hoja
        if (this.conceptTotalRows > numberConcept) {
            while (this.remainingRows.length > 0) {
                ByteArrayOutputStream bas = new ByteArrayOutputStream();
                copyR = new PdfReader(path + templateSantanderCFDI);


                stamper = new PdfStamper(copyR, bas);
                stamper.setFormFlattening(true);

                this.header();
                this.conceptos(invoice.getElements(), 0, 0);
                this.footer();

                stamper.close();
                copyR.close();
                copy.addDocument(new PdfReader(new ByteArrayInputStream(bas.toByteArray())));
            }
        }
        pdfTemplate.close();
    }

    /**
     * Encabezado del PDF
     *
     * @throws IOException       IOException
     * @throws DocumentException DocumentException
     */
    private void header() throws IOException, DocumentException {
        creaLogoHeader();

        stamper.getAcroFields().setField("certificadoEmisor", invoice.getNoCertificado());
        stamper.getAcroFields().setField("fechaEmision", invoice.getDate());
        stamper.getAcroFields().setField("fechaTimbrado", invoice.getTimbreFiscal().getFechaTimbrado());
        stamper.getAcroFields().setField("uuid", invoice.getTimbreFiscal().getUuid());

        stamper.getAcroFields().setField("lugar", invoice.getLugarExpedicion().toUpperCase());
        
        if(invoice.getName() != null){
        	stamper.getAcroFields().setField("nombre", invoice.getName().toUpperCase());
        }else{
        	stamper.getAcroFields().setField("nombre", "");
        }
                
        stamper.getAcroFields().setField("rfc", invoice.getRfc().toUpperCase());
        stamper.getAcroFields().setField("domicilio", invoice.getAddress().toUpperCase());
        
        String codigo= invoice.getCustomerCode();        
        if(codigo == null){
        	stamper.getAcroFields().setField("codigo", invoice.getCustomerCode());
        }else{
        	stamper.getAcroFields().setField("codigo", invoice.getCustomerCode().toUpperCase());
        }
        
        String contrato= invoice.getContractNumber();        
        if(contrato == null){
        	stamper.getAcroFields().setField("contrato", invoice.getContractNumber());
        }else{
        	stamper.getAcroFields().setField("contrato", invoice.getContractNumber().toUpperCase());
        }
        
        String periodo= invoice.getPeriod();
        
        if(periodo == null){
        	stamper.getAcroFields().setField("periodo", invoice.getPeriod());
        }else{
        	stamper.getAcroFields().setField("periodo", invoice.getPeriod().toUpperCase());
        }
        
        String costos= invoice.getCostCenter();
        
        if(costos == null){
        	stamper.getAcroFields().setField("costos", invoice.getCostCenter());
        }else{
        	stamper.getAcroFields().setField("costos", invoice.getCostCenter().toUpperCase());
        }

        stamper.getAcroFields().setField("folio", invoice.getFolio()); 

        String tipo = invoice.getTipoFormato(); 	
        String tipoValue = "";
        if ("I".equals(tipo)) {
            tipoValue = "Factura";
        }
        if ("E".equals(tipo)) {
            tipoValue = "Nota de Cr\u00E9dito";
        }
        stamper.getAcroFields().setField("tipoComprobante", tipoValue);
        stamper.getAcroFields().setField("encabezadoConcepto", invoice.getDescriptionConcept().toUpperCase());
        stamper.getAcroFields().setField("totalEncabezado", UtilPDF.formatNumber(invoice.getTotal()));
        stamper.getAcroFields().setField("regimenFiscal", invoice.getRegimenFiscal().toUpperCase());
        stamper.getAcroFields().setField("metodoPago", invoice.getMetodoPago().toUpperCase());
        stamper.getAcroFields().setField("numCtaPago", invoice.getNumCtaPago().toUpperCase());
        try {
        	 String moneda= invoice.getMoneda();             
            stamper.getAcroFields().setField("tipoMoneda", monedaManager.findByName(moneda).getNombreLargo().toUpperCase());  
              
        } catch (Exception e) {
            System.out.println("<<<< Exception al obtener la moneda 1 de 2: " + e.getMessage());
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
        stamper.getAcroFields().setField("tipoCambio", invoice.getTipoCambio());
        stamper.getAcroFields().setField("formaDePago", invoice.getFormaPago().toUpperCase());
    }

    private void creaLogoHeader() throws DocumentException {
        Image chartImg = null;
        List<FieldPosition> position = stamper.getAcroFields().getFieldPositions("image");
        FieldPosition p = position.get(0);
        float page = p.page;

        SimpleDateFormat formatEmision = new SimpleDateFormat("dd/MM/yyyy");
        Date dateEmision;
        Logo logo = null;
        try {
            dateEmision = formatEmision.parse(invoice.getFechaHora());
            if (this.fiscalEntityId > 0) {
                logo = logoManager.getByFEId(this.fiscalEntityId, dateEmision);
            }
        } catch (ParseException e) {
            logger.error("Error al parsear la fecha: " + invoice.getFechaHora());
            logger.error(e.getMessage());
        }

        try {
            if (logo != null && logo.getImage() != null && logo.getImage().length > 0) {
                // Se toma la imagen proveniente de la entidad logo la cual estÃ¡
                // en un arreglo de bytes
                chartImg = Image.getInstance(logo.getImage());
                chartImg.scaleAbsoluteWidth(p.position.getWidth());
                chartImg.scaleAbsoluteHeight(p.position.getHeight());
                chartImg.setAbsolutePosition(p.position.getLeft(), p.position.getBottom());
                logger.debug("Desplegando la imagen: " + logo.getLogoName());
                logger.debug("Desplegando la imagen(Width): " + chartImg.getScaledWidth());
                logger.debug("Desplegando la imagen(Height): " + chartImg.getScaledHeight());
            } else {
                // Se toma la imagen configurada anteriormente
                chartImg = Image.getInstance(properties.getPathLogoPDF() + this.rfc + ".png");
                chartImg.scaleAbsoluteWidth(p.position.getWidth());
                chartImg.scaleAbsoluteHeight(p.position.getHeight());
                chartImg.setAbsolutePosition(p.position.getLeft(), p.position.getBottom());
                logger.debug("Desplegando la imagen: " + properties.getPathLogoPDF() + this.rfc + ".png");
                logger.debug("Desplegando la imagen(Width): " + chartImg.getScaledWidth());
                logger.debug("Desplegando la imagen(Height): " + chartImg.getScaledHeight());

            }
        } catch (IOException e) {
            logger.error("No se pudo obtener la imagen por BD o RFC: " + e);
            try {
                chartImg = Image.getInstance(properties.getPathLogoPDF() + "logo.jpg");
                logger.debug("Desplegando la imagen(Width): " + chartImg.getScaledWidth());
                logger.debug("Desplegando la imagen(Height): " + chartImg.getScaledHeight());
                chartImg.scaleAbsoluteWidth(p.position.getWidth());
                chartImg.scaleAbsoluteHeight(p.position.getHeight());
                chartImg.setAbsolutePosition(p.position.getLeft(), p.position.getBottom());
            } catch (IOException ioe) {
                logger.error("No se pudo obtener la imagen default: " + ioe.getMessage());
            }
        }

        if (chartImg != null) {
            PdfContentByte ourContent = stamper.getOverContent((int) page);
            ourContent.addImage(chartImg);
        }
    }


    /**
     * @param elements elements
     * @param begin    begin
     * @param limit    limit
     * @throws IOException       IOException
     * @throws DocumentException DocumentException
     */
    private void conceptos(List<ElementsInvoice> elements, int begin, int limit) throws IOException, DocumentException {
        StringBuilder description = new StringBuilder();
        StringBuilder amount = new StringBuilder();
        StringBuilder unitPrice = new StringBuilder();
        StringBuilder quantity = new StringBuilder();
        StringBuilder measurementUnit = new StringBuilder();

        String descripcionRows;

        if (this.conceptTotalRows == -1) {
            if (limit != 0) {
                elements = invoice.getElements().subList(begin - 1, limit);
            }

            for (ElementsInvoice obj : elements) {
                int numberLines = 1;
                String data[] = UtilPDF.longitudFija(obj.getDescription(), 43);
                logger.debug("-------------------------------------------------");
                logger.debug(data[0]);
                logger.debug("-------------------------------------------------");
                quantity.append(UtilPDF.formatNumberQuantity(obj.getQuantity()));
                measurementUnit.append("");
                if (obj.getUnitMeasure() != null) {
                    if (obj.getUnitMeasure().length() < 19) {
                        measurementUnit.append(obj.getUnitMeasure());
                    } else {
                        measurementUnit.append(obj.getUnitMeasure().substring(
                                0, 19));
                    }
                }
                
                
                if(this.invoice.getTipoOperacion() != null){
                	System.out.println("this.invoice.getTipoOperacion():" + this.invoice.getTipoOperacion());
                	if(this.invoice.getTipoOperacion().trim().toLowerCase().equals("compra") || this.invoice.getTipoOperacion().trim().toLowerCase().equals("venta")){
                		unitPrice.append(UtilPDF.formatNumberDivisas(obj.getUnitPrice()));
                	}else{
                		unitPrice.append(UtilPDF.formatNumber(obj.getUnitPrice()));
                	}
                }else{
                	System.out.println("this.invoice.getTipoOperacion():null");
                	unitPrice.append(UtilPDF.formatNumber(obj.getUnitPrice()));
                }
                                                
                amount.append(UtilPDF.formatNumber(obj.getAmount()));

                description.append(data[0]);
                System.out.println("descriptionSB:" + description.toString());
                int number = Integer.parseInt(data[1]);
                System.out.println("numberSB:" + number);
                for (; numberLines <= number; numberLines++) {
                    quantity.append("\n");
                    measurementUnit.append("\n");
                    unitPrice.append("\n");
                    amount.append("\n");
                }

                if (obj.getCuentaPredial() != null && obj.getCuentaPredial().size() > 0) {
                    for (FarmAccount obj2 : obj.getCuentaPredial()) {
                        if (!Util.isNullEmpty(obj2.getNumero())) {
                            description.append("Predial: ").append(Util.espacios(obj2.getNumero(), 1, 10, 0));
                            amount.append("\n");
                            quantity.append("\n");
                            measurementUnit.append("\n");
                            unitPrice.append("\n");
                            description.append("\n");
                        }
                    }
                } else if (obj.getPartes() != null && obj.getPartes().size() > 0) {
                    for (Part obj2 : obj.getPartes()) {
                        description.append("  Parte: ");
                        if (!Util.isNullEmpty(obj2.getCantidad())) {
                            description.append(" ").append(obj2.getCantidad());
                        }

                        if (!Util.isNullEmpty(obj2.getUnidad())) {
                            description.append(" ").append(obj2.getUnidad());
                        }

                        if (!Util.isNullEmpty(obj2.getDescripcion())) {
                            description.append(" ").append(obj2.getDescripcion());
                        }
                        description.append("\n");
                        if (!Util.isNullEmpty(obj2.getValorUnitario())) {
                            description.append("         ").append(obj2.getValorUnitario());
                        }

                        if (!Util.isNullEmpty(obj2.getNoIdentificacion())) {
                            description.append(" ").append(obj2.getNoIdentificacion());
                        }

                        if (!Util.isNullEmpty(obj2.getImporte())) {
                            description.append(" ").append(obj2.getImporte());
                        }

                        amount.append("\n\n");
                        quantity.append("\n\n");
                        measurementUnit.append("\n\n");
                        unitPrice.append("\n\n");
                        description.append("\n");

                        // Despues de que agrego la parte, agrega la info
                        // aduanera
                        // de la misma

                        if ((obj2.getAduana() != null) && (obj2.getAduana().size() > 0)) {
                            for (CustomsInformation obj3 : obj2.getAduana()) {
                                if (!Util.isNullEmpty(obj3.getNumero())) {
                                    description.append("  Pedimento: ").append(Util.espacios(obj3.getNumero(),
                                            1, 10, 0));
                                } else {
                                    description.append("  Pedimento: ").append(Util.espacios("", 1, 10, 0));
                                }

                                if (!Util.isNullEmpty(obj3.getFecha())) {
                                    // YYYY-MM-DD
                                    description.append(" ").append(obj3.getFecha());
                                } else {
                                    description.append("           ");
                                }

                                if (!Util.isNullEmpty(obj3.getAduana())) {
                                    description.append(" ").append(Util.espacios(obj3.getAduana(), 1, 10, 0));
                                } else {
                                    description.append(" ").append(Util.espacios("", 1, 10, 0));
                                }
                                amount.append("\n");
                                quantity.append("\n");
                                measurementUnit.append("\n");
                                unitPrice.append("\n");
                                description.append("\n");
                            }
                        }
                    }
                } else if (obj.getInformacionAduanera() != null && obj.getInformacionAduanera().size() > 0) {
                    for (CustomsInformation obj2 : obj.getInformacionAduanera()) {
                        if (!Util.isNullEmpty(obj2.getNumero())) {
                            description.append("Pedimento: ").append(Util.espacios(obj2.getNumero(), 1, 10, 0));
                        } else {
                            description.append("Pedimento: ").append(Util.espacios("", 1, 10, 0));
                        }

                        if (!Util.isNullEmpty(obj2.getFecha())) {
                            // YYYY-MM-DD
                            description.append(" ").append(obj2.getFecha());
                        } else {
                            description.append("           ");
                        }

                        if (!Util.isNullEmpty(obj2.getAduana())) {
                            description.append(" ").append(Util.espacios(obj2.getAduana(),
                                    1, 10, 0));
                        } else {
                            description.append(" ").append(Util.espacios("", 1, 10, 0));
                        }

                        amount.append("\n");
                        quantity.append("\n");
                        measurementUnit.append("\n");
                        unitPrice.append("\n");
                        description.append("\n");
                    }
                }
            }

            descripcionRows = description.toString();
            System.out.println("descripcionRows:" + descripcionRows);
            
            this.remainingRows = descripcionRows.split("\\n");
            
            for(int ii=0; ii< remainingRows.length; ii++){
            	System.out.println("remainingRows " + ii + ":" + remainingRows[ii]);
            }
            this.conceptTotalRows = this.remainingRows.length;
            
            System.out.println("conceptTotalRows:" + conceptTotalRows);
            
            this.remainingAmount = amount.toString();
            this.remainingMU = measurementUnit.toString();
            this.remainingQuantity = quantity.toString();
            this.remainingUnitPrice = unitPrice.toString();
        }

        
        
        String[] descRows;
        String descAmount;
        String descMeasurement;
        String descQuantity;
        String descUnitPrice;
        // Si quedan mas de numberConcept, entonces proceso solo numberConcept y
        // almaceno los faltantes
        // en remainingRows
        if (remainingRows.length > numberConcept) {
            descRows = new String[numberConcept];
            System.arraycopy(remainingRows, 0, descRows, 0, numberConcept);
            for(int ii=0; ii< descRows.length; ii++){
            	System.out.println("descRows " + ii + ":" + descRows[ii]);
            }
            String[] tempR = new String[remainingRows.length - numberConcept];
            System.arraycopy(remainingRows, numberConcept, tempR, 0, tempR.length);
            remainingRows = tempR;
            for(int ii=0; ii< tempR.length; ii++){
            	System.out.println("tempR " + ii + ":" + tempR[ii]);
            }
            // Actualizo el resto de las variables del concepto

            int idxA = this.calculateIndexStr(this.remainingAmount, "\n", numberConcept);
            descAmount = this.remainingAmount.substring(0);
            if (idxA < this.remainingAmount.length()) {
                this.remainingAmount = this.remainingAmount.substring(idxA + 1);
            } else {
                this.remainingAmount = "";
            }

            int idxM = this.calculateIndexStr(this.remainingMU, "\n", numberConcept);
            descMeasurement = this.remainingMU.substring(0);
            if (idxM < this.remainingMU.length()) {
                this.remainingMU = this.remainingMU.substring(idxM + 1);
            } else {
                this.remainingMU = "";
            }

            int idxQ = this.calculateIndexStr(this.remainingQuantity, "\n", numberConcept);
            descQuantity = this.remainingQuantity.substring(0);
            if (idxQ < this.remainingQuantity.length()) {
                this.remainingQuantity = this.remainingQuantity
                        .substring(idxQ + 1);
            } else {
                this.remainingQuantity = "";
            }

            int idxU = this.calculateIndexStr(this.remainingUnitPrice, "\n", numberConcept);
            descUnitPrice = this.remainingUnitPrice.substring(0);
            if (idxU < this.remainingUnitPrice.length()) {
                this.remainingUnitPrice = this.remainingUnitPrice
                        .substring(idxU + 1);
            } else {
                this.remainingUnitPrice = "";
            }
        }
        // Si no, entonces proceso los numberConcept faltantes
        else {
            descRows = remainingRows;
            remainingRows = new String[0];
            descAmount = this.remainingAmount;
            this.remainingAmount = null;
            descMeasurement = this.remainingMU;
            this.remainingMU = null;
            descQuantity = this.remainingQuantity;
            this.remainingQuantity = null;
            descUnitPrice = this.remainingUnitPrice;
            this.remainingUnitPrice = null;
        }
        String desc = "";
        for (int i = 0; i < numberConcept; i++) {
            if (descRows.length > i) {
                desc += descRows[i] + "\n";
            } else {
                desc += "\n";
            }
        }
        System.out.println("descripcionFinal:" + desc.toUpperCase());
        stamper.getAcroFields().setField("descripcion", desc.toUpperCase());
        stamper.getAcroFields().setField("precioUnitario", descUnitPrice);
        stamper.getAcroFields().setField("cantidad", descQuantity);
        stamper.getAcroFields().setField("unidad", descMeasurement.toUpperCase());
        stamper.getAcroFields().setField("importe", descAmount);
    }

    /**
     * @throws IOException       IOException
     * @throws DocumentException DocumentException
     */
    private void footer() throws IOException, DocumentException {
        try{        	
        	String mone=null;
        	mone=invoice.getMoneda();
        	Moneda moneda = monedaManager.findByName(mone);
        	if(moneda != null){
                invoice.setQuantityWriting(invoice.getQuantityWriting().replaceAll("MONEDAT",
                        UtilPDF.getMoneda(moneda.getNombreLargo(), invoice.getTotal())));
            } 
            
        }catch (Exception e){
            logger.error("Error al obtener la moneda 2 de 2: " + e.getMessage());
        }
        stamper.getAcroFields().setField("letra", UtilPDF.getUTF8(invoice.getQuantityWriting().toUpperCase() + " " + invoice.getMoneda()));
        stamper.getAcroFields().setField("direccion", invoice.getDireccion().toUpperCase());
        stamper.getAcroFields().setField("subTotal", UtilPDF.formatNumber(invoice.getSubTotal()));
        
        
        
        if(!invoice.getIvaDescription().trim().equalsIgnoreCase("EXENTO")){
        	stamper.getAcroFields().setField("descIVA", invoice.getIvaDescription());
        	stamper.getAcroFields().setField("iva", UtilPDF.formatNumber(invoice.getIva()));        	
        }else{
        	stamper.getAcroFields().setField("descIVA", "IVA");
        	stamper.getAcroFields().setField("iva", "EXENTO");        	
        }
                   
        stamper.getAcroFields().setField("total", UtilPDF.formatNumber(invoice.getTotal()));
        stamper.getAcroFields().setField("nocertificado", invoice.getNoCertificado());
        stamper.getAcroFields().setField("noaprobacion", invoice.getNoAprobacion() + " " + invoice.getYearAprobacion());
        stamper.getAcroFields().setField("fechahora", invoice.getFechaHora());

        // Campos CFDI
        stamper.getAcroFields().setField("cadena", invoice.getCadena());
        stamper.getAcroFields().setField("selloEmisor", invoice.getSello());
        stamper.getAcroFields().setField("selloSat", invoice.getTimbreFiscal().getSelloSat());
        stamper.getAcroFields().setField("certificadoSat", invoice.getTimbreFiscal().getNoCertificadoSat());
        this.generaCodigoQR();
    }

    /**
     * @param cer ccer
     * @return ByteArrayOutputStream
     * @throws DocumentException    DocumentException
     * @throws IOException          IOException
     * @throws XmlException         XmlException
     * @throws TransformerException TransformerException
     */
    /*public ByteArrayOutputStream create(CFDIssued cer) throws Exception {
        logger.info("Iniciando la creacion del PDF");
        invoice = new Invoice_PDF();

        // Asegurarse que el id de la entidad este, si no, obtenerlo
        if (cer.getFiscalEntity() != null && cer.getFiscalEntity().getId() > 0) {
            this.fiscalEntityId = cer.getFiscalEntity().getId();
        } else {
            CFDIssued cfdIssued = cFDIssuedManager.get(cer.getId());
            if (cfdIssued != null && cfdIssued.getFiscalEntity() != null) {
                this.fiscalEntityId = cfdIssued.getFiscalEntity().getId();
            }
        }


        BusquedaOnDemandImp searchXML = new BusquedaOnDemandImp(this.serverName, this.userName, this.password,
                this.folderNameInterEmision, this.folderNameInterRecepcion, this.folderNameInterEstadoCuenta,
                this.rutaArchivo, this.nombreArchivo, this.extencionArchivo);
        File xmlFile;
        try {
            System.out.println("Buscando XML(PDF) para Factura Manual CFDI 3.2");
            // Buscar xml en OnDemand
            xmlFile = searchXML.busquedaInterfacturaEmisionCFDI("", "", "", cer.getFolioSAT());

        } catch (Exception e) {
            System.out.println("<<<< Excepcion al obtener el archivo: " + e.getMessage());
            System.out.println(Arrays.toString(e.getStackTrace()));
            xmlFile = null;
        }
        if (xmlFile != null) {
            this.rfc = cer.getFiscalEntity().getTaxID();
            ByteArrayOutputStream outA = new ByteArrayOutputStream();
            FileCopyUtils.copy(new FileInputStream(xmlFile), outA);

            ParseXML_CFDI_PDF parser = new ParseXML_CFDI_PDF();
            invoice = parser.parse(xmlFile);
            //Asigna la cadena
            invoice.setCadena(generaCadena(invoice.getTimbreFiscal()));
            invoice.setContractNumber(cer.getContractNumber());
            invoice.setCustomerCode(cer.getCustomerCode());
            invoice.setPeriod(cer.getPeriod());
            invoice.setCostCenter(cer.getCostCenter());
            invoice.setTipoFormato(cer.getCfdType());
            invoice.setFolio(cer.getFolioInterno());
            return create();
        } else {
            return null;
        }
    }*/
    
    public ByteArrayOutputStream create(File xmlFile) throws Exception {
        logger.info("Iniciando la creacion del PDF");
        invoice = new Invoice_PDF();
     
        if (xmlFile != null) {
        	CFDIssued cfd = null;
//        	CFDIssued cfd = cFDIssuedManager.getByFolioSat(xmlFile.getName());
        	
        	this.fiscalEntityId = cfd.getFiscalEntity().getId();
        	 
            this.rfc = cfd.getFiscalEntity().getTaxID();
            ByteArrayOutputStream outA = new ByteArrayOutputStream();
            FileCopyUtils.copy(new FileInputStream(xmlFile), outA);

            ParseXML_CFDI_PDF parser = new ParseXML_CFDI_PDF();
            invoice = parser.parse(xmlFile);
            //Asigna la cadena
            invoice.setCadena(generaCadena(invoice.getTimbreFiscal()));
            invoice.setContractNumber(cfd.getContractNumber());
            invoice.setCustomerCode(cfd.getCustomerCode());
            invoice.setPeriod(cfd.getPeriod());
            invoice.setCostCenter(cfd.getCostCenter());
            invoice.setTipoFormato(cfd.getCfdType());
            
            invoice.setFolio(String.valueOf(cfd.getFolioInterno()));
            
            return create();
        } else {
            return null;
        }
    }
    
    public ByteArrayOutputStream createMassivePDFs(File xmlFile) throws Exception {
        logger.info("Iniciando la creacion del PDF");
        invoice = new Invoice_PDF();
        String[] data = getDatos(xmlFile);
        
        System.out.println("Tengo los datos del XML");
        
        if (xmlFile != null) {
        	//CFDIssued cfd = this.cFDIssuedManager.getByFolioSat(xmlFile.getName());
        	
        	//this.fiscalEntityId = cfd.getFiscalEntity().getId();
        	 
            this.rfc = data[0];
            ByteArrayOutputStream outA = new ByteArrayOutputStream();
            FileCopyUtils.copy(new FileInputStream(xmlFile), outA);

            ParseXML_CFDI_PDF parser = new ParseXML_CFDI_PDF();
            invoice = parser.parse(xmlFile);
            //Asigna la cadena
            invoice.setCadena(generaCadena(invoice.getTimbreFiscal()));
            invoice.setContractNumber(data[4]);
            invoice.setCustomerCode(data[2]);
            invoice.setPeriod(data[3]);
            invoice.setCostCenter(data[1]);
            invoice.setTipoFormato(data[6]);
            
            invoice.setFolio(String.valueOf(data[5]));
            
            return create();
        } else {
            return null;
        }
    }
    
    public String[] getDatos(File xml) throws ParserConfigurationException, SAXException, IOException{
    	String[] data = null;
    	
    	File tmpXML = xml;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document d = db.parse(tmpXML);
        d.getDocumentElement().normalize();
        
        NodeList emisor = d.getElementsByTagName("cfdi:Emisor");
        NodeList infoEmision = d.getElementsByTagName("as:InformacionEmision");
        NodeList comprobante = d.getElementsByTagName("cfdi:Comprobante");
        
        
        for (int j = 0; j < emisor.getLength(); j++) {
            Element datos = (Element) emisor.item(j);
            //System.out.println("Esta es la fecha: " + comprobanteEl.getAttribute("fecha"));
            data[0] = datos.getAttribute("rfc");
        }
        for (int j = 0; j < infoEmision.getLength(); j++) {
            Element datos = (Element) infoEmision.item(j);
            //System.out.println("Esta es la fecha: " + comprobanteEl.getAttribute("fecha"));
            data[1] = datos.getAttribute("centroCostos");
            data[2] = datos.getAttribute("codigoCliente");
            data[3] = datos.getAttribute("periodo");
            data[4] = datos.getAttribute("contrato");
        }
        for (int j = 0; j < comprobante.getLength(); j++) {
            Element datos = (Element) comprobante.item(j);
            //System.out.println("Esta es la fecha: " + comprobanteEl.getAttribute("fecha"));
            data[5] = datos.getAttribute("folio");
            data[6] = datos.getAttribute("tipoDeComprobante");
        }
    	
    	
    	
    	return data;
    }

    private String generaCadena(TimbreFiscal timbreFiscal) {
        return "||" + timbreFiscal.getVersion() + "|" + timbreFiscal.getUuid() + "|" + timbreFiscal.getFechaTimbrado() +
                "|" + invoice.getSello() + "|" + timbreFiscal.getNoCertificadoSat() + "||";
    }

    /**
     * @param str    str
     * @param token  token
     * @param number number
     * @return int
     */
    private int calculateIndexStr(String str, String token, int number) {
        int idx = 0;
        int res = 0;
        for (int i = 0; i < number; i++) {
            int val = str.indexOf(token, idx);
            if (val < 0) {
                res = 0;
                break;
            }
            res = val;
            idx = val + 1;
        }
        return res;
    }

    /**
     * Genera el codigo QR y lo imprime en el PDF.
     * @throws DocumentException DocumentException
     */
    private void generaCodigoQR() throws DocumentException {
        String contenido = "";
        contenido += "?re=" + this.rfc;
        contenido += "&rr=" + invoice.getRfc().toUpperCase();
        contenido += "&tt=" + formatTotal(quitaExponente(String.valueOf(invoice.getTotal())));
        contenido += "&id=" + invoice.getTimbreFiscal().getUuid();
        logger.debug("Contenido: " + contenido);
        Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        //318 son los pixeles para lograr la resolucion de 200dpi
        //ya que el area es de 1.59in
        BarcodeQRCode bar = new BarcodeQRCode(contenido, 318, 318, hints);
        Image image = bar.getImage();
        List<AcroFields.FieldPosition> position = stamper.getAcroFields().getFieldPositions("codigoBD");
        AcroFields.FieldPosition p = position.get(0);
        float page = p.page;
        image.scaleAbsoluteWidth(p.position.getWidth());
        image.scaleAbsoluteHeight(p.position.getHeight());
        image.setAbsolutePosition(p.position.getLeft(), p.position.getBottom());
        PdfContentByte ourContent = stamper.getOverContent((int) page);
        ourContent.addImage(image);
    }

    private String formatTotal(String total) {
        String[] partes = total.split("\\.");
        if (partes.length == 2) {
            String totalF = partes[0] + "." + partes[1];
            if (partes[1].length() < 6) {
                for (int i = 1; i <= (6 - partes[1].length()); i++) {
                    totalF += "0";
                }
            }
            return totalF;
        } else {
            return total;
        }
    }

    private String quitaExponente(String total) {
        String cantidadSinExp = total;
        if (total.contains("E")) {
            String[] partes = total.split("\\.");
            String decimal = partes[1].substring(0, partes[1].indexOf('E'));
            int exponente = Integer.parseInt(partes[1].substring(partes[1].indexOf('E') + 1));
            int vueltas = exponente - decimal.length();
            if (vueltas > 0 || vueltas == 0) {
                for (int i = 0; i < vueltas; i++) {
                    decimal += "0";
                }
                cantidadSinExp = partes[0] + decimal + ".0";
            } else {
                cantidadSinExp = partes[0] + decimal.substring(0, exponente) + "." + decimal.substring(exponente);
            }
        }
        return cantidadSinExp;
    }
}