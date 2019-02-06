package com.interfactura.firmalocal.generapdf.impl;

import com.interfactura.firmalocal.datamodel.*;
import com.interfactura.firmalocal.domain.entities.CFDIssued;
import com.interfactura.firmalocal.domain.entities.Logo;
import com.interfactura.firmalocal.generapdf.util.NumberToLetterConverter;
import com.interfactura.firmalocal.generapdf.util.UtilPDF;
import com.interfactura.firmalocal.ondemand.search.impl.BusquedaOnDemandImp;
import com.interfactura.firmalocal.persistence.CFDIssuedManager;
import com.interfactura.firmalocal.persistence.LogoManager;
import com.interfactura.firmalocal.persistence.MonedaManager;
import com.interfactura.firmalocal.xml.Properties;
import com.interfactura.firmalocal.xml.file.XMLProcess_PDF;
import com.interfactura.firmalocal.xml.util.Util;
import com.interfactura.recepcionmasiva.service.ValidationException;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.AcroFields.FieldPosition;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfCopyFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import mx.gob.sat.cfd.x2.ComprobanteDocument;
import mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante;
import mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante.Conceptos.Concepto;
import mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante.Conceptos.Concepto.CuentaPredial;
import mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante.Conceptos.Concepto.Parte;
import mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante.Impuestos.Traslados.Traslado;
import mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante.Receptor;
import mx.gob.sat.cfd.x2.TCampoAdicional;
import mx.gob.sat.cfd.x2.TInformacionAduanera;
import mx.gob.sat.cfd.x2.TUbicacion;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import javax.xml.transform.TransformerException;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.interfactura.firmalocal.generapdf.util.UtilPDF.*;

@Component
public class PDFCreationImpl {

    private StringBuffer description;
    private StringBuffer unitPrice;
    private StringBuffer quantity;
    private StringBuffer measurementUnit;
    private StringBuffer amount;
    private ByteArrayOutputStream out;
    private ByteArrayOutputStream tmpOUT;
    private PdfReader pdfTemplate;
    private PdfCopyFields copy;
    private PdfStamper stamper;
    private Logger logger = Logger.getLogger(PDFCreationImpl.class);
    @Autowired
    private XMLProcess_PDF xmlPro;
    @Autowired
    private Properties properties;
    @Autowired(required = true)
    private MonedaManager monedaManager;
    private ComprobanteDocument compDoc;
    private Comprobante comprobante;
    private Receptor receptor;
    private TUbicacion tUbicacion;
    private Invoice_PDF invoice;
    private ByteArrayOutputStream outA;
    private ByteArrayOutputStream outCadena;
    private File fileXML;
    private ElementsInvoice element;
    private String direccion;
    private String moneda;
    private String tipoCambio;
    private String ivaDescription;
    private List<CustomsInformation> informacionAduanera;
    private List<FarmAccount> cuentaPredial;
    private List<Part> partes;
    private CustomsInformation customsInformation;
    private FarmAccount farmAccount;
    private Part part;
    private String rfc = "test.png";

    private int conceptTotalRows = -1;
    private String[] remainingRows = new String[0];
    private String remainingAmount = null;
    private String remainingMU = null;
    private String remainingQuantity = null;
    private String remainingUnitPrice = null;

    private String[] remainingCadenaRows = new String[0];
    private boolean longCadena = false;
    private boolean cadenaCreated = false;
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
    private String numCtaPago;
    private String metodoDePago;
    private String regimenFiscal;
    private String formaDePago;
    private String version = "2.2";

    public PDFCreationImpl() {

    }

    /**
     * @param invoice
     * @return
     * @throws DocumentException
     * @throws IOException
     */
    public ByteArrayOutputStream create(Invoice_PDF invoice)
            throws DocumentException, IOException {
        out = new ByteArrayOutputStream();
        copy = new PdfCopyFields(out);
        copy.open();
        this.concatenate(invoice);
        copy.close();
        return out;
    }

    /**
     * @param invoice
     * @throws DocumentException
     * @throws IOException
     */
    public void concatenate(Invoice_PDF invoice) throws DocumentException,
            IOException {
        this.template1(invoice);
    }

    /**
     * @param invoice
     * @throws IOException
     * @throws DocumentException
     */
    public void template1(Invoice_PDF invoice) throws IOException,
            DocumentException {
        String TEMPLATECFD20 = "templateSantanderCFD20.pdf";
        if (this.version.equals("2.0")) {
            pdfTemplate = new PdfReader(TEMPLATECFD20);
        } else {
            pdfTemplate = new PdfReader(path + templateC);
        }

        PdfReader copyR = new PdfReader(pdfTemplate);
        tmpOUT = new ByteArrayOutputStream();
        stamper = new PdfStamper(copyR, tmpOUT);
        stamper.setFormFlattening(true);

        // Proceso el documento la primera vez. Inicializo variables de control
        this.conceptTotalRows = -1;
        this.remainingRows = new String[0];
        this.remainingAmount = null;
        this.remainingMU = null;
        this.remainingQuantity = null;
        this.remainingUnitPrice = null;

        this.remainingCadenaRows = new String[0];
        this.longCadena = false;
        this.cadenaCreated = false;

        this.header(stamper, invoice);
        this.detail(stamper, invoice);
        this.footer(stamper, invoice);

        stamper.close();
        copyR.close();

        copy.addDocument(new PdfReader(new ByteArrayInputStream(tmpOUT
                .toByteArray())));
        // Verifico si se requiere mas de una hoja
        if (this.conceptTotalRows > numberConcept) {
            while (this.remainingRows.length > 0) {
                ByteArrayOutputStream bas = new ByteArrayOutputStream();
                if (this.version.equals("2.0")) {
                    copyR = new PdfReader(TEMPLATECFD20);
                } else {
                    copyR = new PdfReader(path + templateC);
                }

                PdfStamper s = new PdfStamper(copyR, bas);
                s.setFormFlattening(true);

                this.header(s, invoice);
                this.detail(s, invoice);
                this.footer(s, invoice);
                s.close();
                copyR.close();
                copy.addDocument(new PdfReader(new ByteArrayInputStream(bas
                        .toByteArray())));
            }
        }

        // Verifico si requiero mas paginas para cadena
        while (this.remainingCadenaRows.length > 0) {
            this.longCadena = true;
            ByteArrayOutputStream bas = new ByteArrayOutputStream();
            copyR = new PdfReader(path + templateE);
            PdfStamper s = new PdfStamper(copyR, bas);
            s.setFormFlattening(true);
            this.header(s, invoice);
            this.detail(s, invoice);
            this.footer(s, invoice);
            s.close();
            copyR.close();
            copy.addDocument(new PdfReader(new ByteArrayInputStream(bas
                    .toByteArray())));
        }

        pdfTemplate.close();
    }

    /**
     * @param invoice
     * @throws IOException
     * @throws DocumentException
     */
    public void header(Invoice_PDF invoice) throws IOException, DocumentException {
        pdfTemplate = new PdfReader(path + templateB);
        tmpOUT = new ByteArrayOutputStream();
        stamper = new PdfStamper(pdfTemplate, tmpOUT);
        stamper.setFormFlattening(true);
        this.header(stamper, invoice);
        this.detail(stamper, invoice, 1, recordNumberB);
        stamper.close();
        pdfTemplate.close();
        copy.addDocument(new PdfReader(new ByteArrayInputStream(tmpOUT
                .toByteArray())));
    }

    /**
     * @param invoice invoice
     * @param begin   begin
     * @param limit   limit
     * @throws IOException       IOException
     * @throws DocumentException DocumentException
     */
    public void detail(Invoice_PDF invoice, int begin, int limit)
            throws IOException, DocumentException {
        pdfTemplate = new PdfReader(path + templateB);
        tmpOUT = new ByteArrayOutputStream();
        stamper = new PdfStamper(pdfTemplate, tmpOUT);
        stamper.setFormFlattening(true);
        this.detail(stamper, invoice, (begin + 1), limit);
        stamper.close();
        pdfTemplate.close();
        copy.addDocument(new PdfReader(new ByteArrayInputStream(tmpOUT
                .toByteArray())));
    }

    /**
     * Pie de pagina del PDF
     *
     * @param invoice invoice
     * @param begin   begin
     * @param limit   limit
     * @throws IOException       IOException
     * @throws DocumentException DocumentException
     */
    public void footer(Invoice_PDF invoice, int begin, int limit)
            throws IOException, DocumentException {
        pdfTemplate = new PdfReader(path + templateE);
        tmpOUT = new ByteArrayOutputStream();
        stamper = new PdfStamper(pdfTemplate, tmpOUT);
        stamper.setFormFlattening(true);
        this.detail(stamper, invoice, (begin + 1), limit);
        this.footer(stamper, invoice);
        stamper.close();
        pdfTemplate.close();
        copy.addDocument(new PdfReader(new ByteArrayInputStream(tmpOUT
                .toByteArray())));
    }

    /**
     * Encabezado del PDF
     *
     * @param stamper stamper
     * @param invoice invoice
     * @throws IOException       IOException
     * @throws DocumentException DocumentException
     */
    public void header(PdfStamper stamper, Invoice_PDF invoice) throws IOException,
            DocumentException {
        Image chartImg = null;
        List<FieldPosition> position = stamper.getAcroFields().getFieldPositions("image");
        FieldPosition p = position.get(0);
        float page = p.page;

        try {
            SimpleDateFormat formatEmision = new SimpleDateFormat("dd/MM/yyyy");
            Date dateEmision = new Date();

            dateEmision = formatEmision.parse(invoice.getFechaHora());
            Logo logo = null;
            if (this.fiscalEntityId > 0) {
                logo = logoManager.getByFEId(this.fiscalEntityId, dateEmision);
            }

            if (logo != null && logo.getImage() != null && logo.getImage().length > 0) {
                // Se toma la imagen proveniente de la entidad logo la cual est�
                // en un arreglo de bytes
                chartImg = Image.getInstance(logo.getImage());
                chartImg.scaleAbsoluteWidth(p.position.getWidth());
                chartImg.scaleAbsoluteHeight(p.position.getHeight());
                chartImg.setAbsolutePosition(p.position.getLeft(),
                        p.position.getBottom());
                logger.debug("Desplegando la imagen: " + logo.getLogoName());
                logger.debug("Desplegando la imagen(Width): "
                        + chartImg.getScaledWidth());
                logger.debug("Desplegando la imagen(Height): "
                        + chartImg.getScaledHeight());
            } else {
                // Se toma la imagen configurada anteriormente

                chartImg = Image.getInstance(properties.getPathLogoPDF()
                        + this.rfc + ".png");
                chartImg.scaleAbsoluteWidth(p.position.getWidth());
                chartImg.scaleAbsoluteHeight(p.position.getHeight());
                chartImg.setAbsolutePosition(p.position.getLeft(),
                        p.position.getBottom());
                logger.debug("Desplegando la imagen: "
                        + properties.getPathLogoPDF() + this.rfc + ".jpg");
                logger.debug("Desplegando la imagen(Width): "
                        + chartImg.getScaledWidth());
                logger.debug("Desplegando la imagen(Height): "
                        + chartImg.getScaledHeight());

            }
        } catch (Exception e) {
            try {
                chartImg = Image.getInstance(properties.getPathLogoPDF()
                        + "logo.jpg");
                logger.debug("Desplegando la imagen(Width): "
                        + chartImg.getScaledWidth());
                logger.debug("Desplegando la imagen(Height): "
                        + chartImg.getScaledHeight());
                chartImg.scaleAbsoluteWidth(p.position.getWidth());
                chartImg.scaleAbsoluteHeight(p.position.getHeight());
                chartImg.setAbsolutePosition(p.position.getLeft(),
                        p.position.getBottom());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        if (chartImg != null) {
            PdfContentByte ourContent = stamper.getOverContent((int) page);
            ourContent.addImage(chartImg);
        }

        stamper.getAcroFields().setField("lugar", invoice.getLugarExpedicion());

        stamper.getAcroFields().setField("nombre", invoice.getName());
        stamper.getAcroFields().setField("rfc", invoice.getRfc());
        stamper.getAcroFields().setField("domicilio", invoice.getAddress());
        stamper.getAcroFields().setField("codigo", invoice.getCustomerCode());
        stamper.getAcroFields().setField("contrato",
                invoice.getContractNumber());
        stamper.getAcroFields().setField("periodo", invoice.getPeriod());
        stamper.getAcroFields().setField("costos", invoice.getCostCenter());

        // Nuevos campos CFDV22
        if (this.version.equals("2.2")) {
            stamper.getAcroFields().setField("regimenFiscal", this.regimenFiscal);
            stamper.getAcroFields().setField("metodoPago", this.metodoDePago);
            stamper.getAcroFields().setField("numCtaPago", this.numCtaPago);
            stamper.getAcroFields().setField("tipoMoneda", monedaManager.findByName(this.moneda).getNombreLargo());
            stamper.getAcroFields().setField("tipoCambio", this.tipoCambio);
            stamper.getAcroFields().setField("formaDePago", this.formaDePago);
        }

        stamper.getAcroFields().setField("folio", invoice.getFolio());
        stamper.getAcroFields().setField("d1",
                invoice.getDate().substring(8, 9));
        stamper.getAcroFields().setField("d2",
                invoice.getDate().substring(9, 10));
        stamper.getAcroFields().setField("m1",
                invoice.getDate().substring(5, 6));
        stamper.getAcroFields().setField("m2",
                invoice.getDate().substring(6, 7));
        stamper.getAcroFields().setField("a1",
                invoice.getDate().substring(0, 1));
        stamper.getAcroFields().setField("a2",
                invoice.getDate().substring(1, 2));
        stamper.getAcroFields().setField("a3",
                invoice.getDate().substring(2, 3));
        stamper.getAcroFields().setField("a4",
                invoice.getDate().substring(3, 4));
        stamper.getAcroFields().setField("a4",
                invoice.getDate().substring(3, 4));
        String tipo = invoice.getTipoFormato();
        String tipoValue = "";
        if ("I".equals(tipo)) {
            tipoValue = "Factura";
        }
        if ("E".equals(tipo)) {
            tipoValue = "Nota de Cr�dito";
        }
        stamper.getAcroFields().setField("tipoComprobante", tipoValue);
        stamper.getAcroFields().setField("encabezadoConcepto",
                invoice.getDescriptionConcept());
        stamper.getAcroFields().setField("totalEncabezado",
                UtilPDF.formatNumber(invoice.getTotal()));

    }

    /**
     * Cuerpo del PDF
     *
     * @param stamper
     * @param invoice
     * @throws IOException
     * @throws DocumentException
     */
    public void detail(PdfStamper stamper, Invoice_PDF invoice) throws IOException,
            DocumentException {
        this.conceptos(invoice.getElements(), stamper, 0, 0);
    }

    /**
     * Cuerpo del PDF
     *
     * @param stamper
     * @param invoice
     * @param begin
     * @param limit
     * @throws IOException
     * @throws DocumentException
     */
    public void detail(PdfStamper stamper, Invoice_PDF invoice, int begin, int limit)
            throws IOException, DocumentException {
        this.conceptos(invoice.getElements(), stamper, begin, limit);
    }

    /**
     * @param elements
     * @param stamper
     * @param begin
     * @param limit
     * @throws IOException
     * @throws DocumentException
     */
    public void conceptos(List<ElementsInvoice> elements, PdfStamper stamper,
                          int begin, int limit) throws IOException, DocumentException {
        description = new StringBuffer();
        amount = new StringBuffer();
        unitPrice = new StringBuffer();
        quantity = new StringBuffer();
        measurementUnit = new StringBuffer();

        String descripcionRows = null;

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
                quantity.append(UtilPDF.formatNumber(obj.getQuantity()));
                measurementUnit.append("");
                if (obj.getUnitMeasure() != null) {
                    measurementUnit.append("  ");
                    if (obj.getUnitMeasure().length() < 10) {
                        measurementUnit.append(obj.getUnitMeasure());
                    } else {
                        measurementUnit.append(obj.getUnitMeasure().substring(
                                0, 10));
                    }
                }
                unitPrice.append(UtilPDF.formatNumber(obj.getUnitPrice()));
                amount.append(UtilPDF.formatNumber(obj.getAmount()));

                description.append(data[0]);

                int number = Integer.parseInt(data[1]);

                for (; numberLines <= number; numberLines++) {
                    quantity.append("\n");
                    measurementUnit.append("\n");
                    unitPrice.append("\n");
                    amount.append("\n");
                }

                if (obj.getCuentaPredial() != null
                        && obj.getCuentaPredial().size() > 0) {
                    for (FarmAccount obj2 : obj.getCuentaPredial()) {
                        if (!Util.isNullEmpty(obj2.getNumero())) {
                            description
                                    .append("Predial: "
                                            + Util.espacios(obj2.getNumero(),
                                            1, 10, 0));
                            amount.append("\n");
                            quantity.append("\n");
                            measurementUnit.append("\n");
                            unitPrice.append("\n");
                            description.append("\n");
                        }
                    }
                } else if (obj.getPartes() != null
                        && obj.getPartes().size() > 0) {
                    for (Part obj2 : obj.getPartes()) {
                        description.append("  Parte: ");
                        if (!Util.isNullEmpty(obj2.getCantidad())) {
                            description.append(" " + obj2.getCantidad());
                        }

                        if (!Util.isNullEmpty(obj2.getUnidad())) {
                            description.append(" " + obj2.getUnidad());
                        }

                        if (!Util.isNullEmpty(obj2.getDescripcion())) {
                            description.append(" " + obj2.getDescripcion());
                        }
                        description.append("\n");
                        if (!Util.isNullEmpty(obj2.getValorUnitario())) {
                            description.append("         "
                                    + obj2.getValorUnitario());
                        }

                        if (!Util.isNullEmpty(obj2.getNoIdentificacion())) {
                            description
                                    .append(" " + obj2.getNoIdentificacion());
                        }

                        if (!Util.isNullEmpty(obj2.getImporte())) {
                            description.append(" " + obj2.getImporte());
                        }

                        amount.append("\n\n");
                        quantity.append("\n\n");
                        measurementUnit.append("\n\n");
                        unitPrice.append("\n\n");
                        description.append("\n");

                        // Despues de que agrego la parte, agrega la info
                        // aduanera
                        // de la misma

                        if ((obj2.getAduana() != null)
                                && (obj2.getAduana().size() > 0)) {
                            for (CustomsInformation obj3 : obj2.getAduana()) {
                                if (!Util.isNullEmpty(obj3.getNumero())) {
                                    description.append("  Pedimento: "
                                            + Util.espacios(obj3.getNumero(),
                                            1, 10, 0));
                                } else {
                                    description.append("  Pedimento: "
                                            + Util.espacios("", 1, 10, 0));
                                }

                                if (!Util.isNullEmpty(obj3.getFecha())) {
                                    // YYYY-MM-DD
                                    description.append(" " + obj3.getFecha());
                                } else {
                                    description.append("           ");
                                }

                                if (!Util.isNullEmpty(obj3.getAduana())) {
                                    description.append(" "
                                            + Util.espacios(obj3.getAduana(),
                                            1, 10, 0));
                                } else {
                                    description.append(" "
                                            + Util.espacios("", 1, 10, 0));
                                }
                                amount.append("\n");
                                quantity.append("\n");
                                measurementUnit.append("\n");
                                unitPrice.append("\n");
                                description.append("\n");
                            }
                        }
                    }
                } else if (obj.getInformacionAduanera() != null
                        && obj.getInformacionAduanera().size() > 0) {
                    for (CustomsInformation obj2 : obj.getInformacionAduanera()) {
                        if (!Util.isNullEmpty(obj2.getNumero())) {
                            description
                                    .append("Pedimento: "
                                            + Util.espacios(obj2.getNumero(),
                                            1, 10, 0));
                        } else {
                            description.append("Pedimento: "
                                    + Util.espacios("", 1, 10, 0));
                        }

                        if (!Util.isNullEmpty(obj2.getFecha())) {
                            // YYYY-MM-DD
                            description.append(" " + obj2.getFecha());
                        } else {
                            description.append("           ");
                        }

                        if (!Util.isNullEmpty(obj2.getAduana())) {
                            description
                                    .append(" "
                                            + Util.espacios(obj2.getAduana(),
                                            1, 10, 0));
                        } else {
                            description.append(" "
                                    + Util.espacios("", 1, 10, 0));
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
            this.remainingRows = descripcionRows.split("\\n");
            this.conceptTotalRows = this.remainingRows.length;
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
            for (int i = 0; i < numberConcept; i++) {
                descRows[i] = remainingRows[i];
            }
            String[] tempR = new String[remainingRows.length - numberConcept];
            for (int i = 0; i < tempR.length; i++) {
                tempR[i] = remainingRows[numberConcept + i];
            }
            remainingRows = tempR;

            // Actualizo el resto de las variables del conceto

            int idxA = this.calculateIndexStr(this.remainingAmount, "\n",
                    numberConcept);
            descAmount = this.remainingAmount.substring(0);
            if (idxA < this.remainingAmount.length()) {
                this.remainingAmount = this.remainingAmount.substring(idxA + 1);
            } else {
                this.remainingAmount = "";
            }

            int idxM = this.calculateIndexStr(this.remainingMU, "\n",
                    numberConcept);
            descMeasurement = this.remainingMU.substring(0);
            if (idxM < this.remainingMU.length()) {
                this.remainingMU = this.remainingMU.substring(idxM + 1);
            } else {
                this.remainingMU = "";
            }

            int idxQ = this.calculateIndexStr(this.remainingQuantity, "\n",
                    numberConcept);
            descQuantity = this.remainingQuantity.substring(0);
            if (idxQ < this.remainingQuantity.length()) {
                this.remainingQuantity = this.remainingQuantity
                        .substring(idxQ + 1);
            } else {
                this.remainingQuantity = "";
            }

            int idxU = this.calculateIndexStr(this.remainingUnitPrice, "\n",
                    numberConcept);
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
        // Pinto en el pdf los registros de los conceptos
        for (int i = 0; i < numberConcept; i++) {
            if (descRows.length > i) {
                stamper.getAcroFields()
                        .setField("descripcion" + i, descRows[i]);
            } else {
                stamper.getAcroFields().setField("descripcion" + i, "");
            }
        }

        stamper.getAcroFields().setField("precioUnitario", descUnitPrice);
        stamper.getAcroFields().setField("cantidad", descQuantity);
        stamper.getAcroFields().setField("unidad", descMeasurement);
        stamper.getAcroFields().setField("importe", descAmount);
    }

    /**
     * @param stamper
     * @param invoice
     * @throws IOException
     * @throws DocumentException
     */
    public void footer(PdfStamper stamper, Invoice_PDF invoice) throws IOException,
            DocumentException {
        stamper.getAcroFields().setField("letra",
                UtilPDF.getUTF8(invoice.getQuantityWriting() + " " + moneda));
        stamper.getAcroFields().setField("direccion", direccion);
        stamper.getAcroFields().setField("subTotal",
                UtilPDF.formatNumber(invoice.getSubTotal()));
        stamper.getAcroFields().setField("iva",
                UtilPDF.formatNumber(invoice.getIva()));
        stamper.getAcroFields().setField("descIVA", ivaDescription);
        stamper.getAcroFields().setField("total",
                UtilPDF.formatNumber(invoice.getTotal()));
        stamper.getAcroFields().setField("nocertificado",
                invoice.getNoCertificado());
        stamper.getAcroFields().setField("noaprobacion",
                invoice.getNoAprobacion() + " " + invoice.getYearAprobacion());
        stamper.getAcroFields().setField("fechahora", invoice.getFechaHora());

        if ((!this.cadenaCreated)
                || (this.remainingCadenaRows.length > 0)) {
            int cadenaFields = 8;

            if (!this.cadenaCreated) {
                String cadenaRows = UtilPDF.longitudFija(
                        UtilPDF.getUTF8(invoice.getCadena()), 145)[0];
                String[] cadenaRowsArr = cadenaRows.split("\\n");
                this.remainingCadenaRows = cadenaRowsArr;
                this.cadenaCreated = true;
            }

            if (this.longCadena) {
                cadenaFields = 50;
            }

            for (int i = 0; i < cadenaFields; i++) {
                if (remainingCadenaRows.length > i) {
                    stamper.getAcroFields().setField("cadenaOriginal" + i,
                            remainingCadenaRows[i]);
                } else {
                    stamper.getAcroFields().setField("cadenaOriginal" + i, "");
                }
            }

            // Actualiza el remainingCadenaRows

            String[] tempCadenaRows;
            if (this.remainingCadenaRows.length > cadenaFields) {
                tempCadenaRows = new String[this.remainingCadenaRows.length
                        - cadenaFields];
                for (int i = 0; i < tempCadenaRows.length; i++) {
                    tempCadenaRows[i] = this.remainingCadenaRows[cadenaFields
                            + i];
                }
                this.remainingCadenaRows = tempCadenaRows;
            } else {
                this.remainingCadenaRows = new String[0];
            }

        }

        stamper.getAcroFields().setField("sello0",
                invoice.getSello().substring(0, 145));
        stamper.getAcroFields().setField("sello1",
                invoice.getSello().substring(145, invoice.getSello().length()));
    }

    /**
     * @param cer
     * @return
     * @throws DocumentException
     * @throws IOException
     * @throws XmlException
     * @throws TransformerException
     */
/*    public ByteArrayOutputStream create(CFDIssued cer)
            throws DocumentException, IOException, XmlException,
            TransformerException {
        logger.info("Iniciando la creacion del PDF");
        if (cer.getFiscalEntity() != null && cer.getFiscalEntity().getId() > 0) {
            this.fiscalEntityId = cer.getFiscalEntity().getId();
        } else {
            CFDIssued cfdIssued = cFDIssuedManager.get(cer.getId());
            if (cfdIssued != null && cfdIssued.getFiscalEntity() != null) {
                this.fiscalEntityId = cfdIssued.getFiscalEntity().getId();
            }
        }
        invoice = new Invoice();
        // fileXML = new File(cer.getFilePath().getRoute());
        outA = new ByteArrayOutputStream();
        BusquedaOnDemandImp searchXML = new BusquedaOnDemandImp(
                this.serverName, this.userName, this.password,
                this.folderNameInterEmision, this.folderNameInterRecepcion,
                this.folderNameInterEstadoCuenta, this.rutaArchivo,
                this.nombreArchivo, this.extencionArchivo);
        try {
            // fileXML=searchXML.busquedaInterfacturaEmision(cer.getFiscalEntity().getTaxID(),
            // cer.getFolioRange().getSeries().getName(), cer.getFolio());
            if (cer.getFormatType() == 0) {
                String information[] = cer.getComplement().concat("|remp")
                        .split("\\|");

                String cardNumber = "null";
                if (information[2] != null && information[2].length() > 0) {
                    cardNumber = information[2];
                }

                System.out.println("Buscando XML(PDF) para Estados de Cuenta");

                fileXML = searchXML.busquedaInterfacturaEstadosCuenta(cer
                        .getFiscalEntity().getTaxID(), cer.getComplement(), cer
                        .getComplement(), cer.getCreationDate());
            } else {
                String serie = "null";
                if (cer.getSourceFileName() == null) {
                    serie = "";
                }

                if (cer.getFolioRange() != null) {
                    if (cer.getFolioRange().getSeries() != null) {
                        serie = cer.getFolioRange().getSeries().getName();
                    }
                }

                System.out.println("Buscando XML(PDF) para Factura Manual");

                fileXML = searchXML.busquedaInterfacturaEmision(cer
                        .getFiscalEntity().getTaxID(), serie, cer.getFolio());
            }
        } catch (Exception e) {
            fileXML = null;
        }
        if (fileXML != null) {
            FileCopyUtils.copy(new FileInputStream(fileXML), outA);

            this.rfc = cer.getFiscalEntity().getTaxID();

            List<ElementsInvoice> elements = new ArrayList<ElementsInvoice>();

            InputStreamReader isreader = new InputStreamReader(new FileInputStream(
                    fileXML), "UTF-8");
            BufferedReader fr = new BufferedReader(isreader);

            StringBuilder s = new StringBuilder();
            while (fr.ready()) {
                s.append(fr.readLine());
            }
            this.numCtaPago = obtenerCampo(s.toString(), "NumCtaPago", 12);
            this.metodoDePago = obtenerCampo(s.toString(), "metodoDePago", 14);
            this.regimenFiscal = obtenerCampo(s.toString(), "Regimen=\"", 9);
            this.formaDePago = obtenerCampo(s.toString(), "formaDePago=\"", 13);

            compDoc = ComprobanteDocument.Factory.parse(
                    s.toString().replace("xmlns=\"http://www.santander.com.mx/schemas/xsd/AddendaSantanderV1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"", ""));

            if (compDoc.getComprobante().getVersion() != null && !compDoc.getComprobante().getVersion().equals("")) {
                this.version = compDoc.getComprobante().getVersion();
            }
            if (this.version == "2.0") {
                invoice.setLugarExpedicion(compDoc.getComprobante().getEmisor()
                        .getDomicilioFiscal().getEstado()
                        + ", "
                        + compDoc.getComprobante().getEmisor().getDomicilioFiscal()
                        .getPais());
            } else {
                invoice.setLugarExpedicion(obtenerCampo(s.toString(), "LugarExpedicion=", 17));
            }
            try {
                outCadena = xmlPro.generatesOriginalString(outA, this.version);
            } catch (ValidationException e) {
                e.printStackTrace();
            }
            logger.debug("Cadena Original: " + outCadena.toString());
            invoice.setCadena(outCadena.toString());
            invoice.setContractNumber(cer.getContractNumber());
            invoice.setCustomerCode(cer.getCustomerCode());
            invoice.setPeriod(cer.getPeriod());
            invoice.setCostCenter(cer.getCostCenter());
            invoice.setPorcentaje(0.00);
            if (compDoc.getComprobante().getImpuestos().getTraslados() != null) {
                for (Traslado objT : compDoc.getComprobante().getImpuestos()
                        .getTraslados().getTrasladoArray()) {
                    if (objT.getImpuesto().equals(Traslado.Impuesto.IVA)) {
                        invoice.setPorcentaje(objT.getTasa().doubleValue());
                    }
                }
            }

            comprobante = compDoc.getComprobante();
            moneda = "";
            ivaDescription = "";
            String encabezadoConcepto = "";
            if (comprobante.getAddenda().getAddendaSantanderV1()
                    .getCampoAdicionalArray() != null) {
                for (TCampoAdicional campo : comprobante.getAddenda()
                        .getAddendaSantanderV1().getCampoAdicionalArray()) {
                    if (campo.getCampo().equals("Descripcion Concepto")) {
                        encabezadoConcepto = campo.getValor();
                    }

                    if (campo.getCampo().equals("Moneda")) {
                        moneda = campo.getValor();
                    }

                    if (campo.getCampo().equals("Descripcion IVA")) {
                        ivaDescription = campo.getValor();
                    }
                    if (campo.getCampo().equals("Tipo Cambio")) {
                        tipoCambio = campo.getValor();
                    }
                }
            }

            invoice.setDescriptionConcept(encabezadoConcepto);

            direccion = compDoc.getComprobante().getEmisor().getNombre()
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

            invoice.setNoAprobacion(comprobante.getNoAprobacion().toString());
            invoice.setNoCertificado(comprobante.getNoCertificado());
            invoice.setFechaHora(Util.convertirFecha(comprobante.getFecha()
                    .getTime(), null));
            invoice.setSello(comprobante.getSello());
            invoice.setFolio(comprobante.getFolio());
            // invoice.setReference(comprobante.getSerie());
            invoice.setDate(Util.convertirFecha(comprobante.getFecha().getTime()));
            receptor = comprobante.getReceptor();

            invoice.setRfc(receptor.getRfc());

            invoice.setName(receptor.getNombre());
            tUbicacion = receptor.getDomicilio();
            invoice.setCalle(tUbicacion.getCalle());
            invoice.setCodigoPostal(tUbicacion.getCodigoPostal());
            invoice.setColonia(tUbicacion.getColonia());
            invoice.setEstado(tUbicacion.getEstado());
            invoice.setExterior(tUbicacion.getNoExterior());
            invoice.setInterior(tUbicacion.getNoInterior());
            invoice.setMunicipio(tUbicacion.getMunicipio());
            invoice.setReferencia(tUbicacion.getReferencia());
            invoice.setSubTotal(comprobante.getSubTotal().doubleValue());
            invoice.setTotal(comprobante.getTotal().doubleValue());
            invoice.setIva(comprobante.getImpuestos()
                    .getTotalImpuestosTrasladados().doubleValue());
            invoice.setQuantityWriting(NumberToLetterConverter
                    .convertNumberToLetter(invoice.getTotal()));
            invoice.setYearAprobacion(String.valueOf(comprobante.getAnoAprobacion()));
            invoice.setQuantityWriting(invoice.getQuantityWriting()
                    .replaceAll(
                            "MONEDAT",
                            UtilPDF.getMoneda(monedaManager.findByName(this.moneda)
                                    .getNombreLargo(), comprobante.getTotal()
                                    .doubleValue())));

            for (Concepto objConcepto : comprobante.getConceptos()
                    .getConceptoArray()) {
                element = new ElementsInvoice();
                informacionAduanera = new ArrayList<CustomsInformation>();
                cuentaPredial = new ArrayList<FarmAccount>();
                partes = new ArrayList<Part>();
                element.setAmount(objConcepto.getImporte().doubleValue());
                element.setDescription(objConcepto.getDescripcion());
                element.setQuantity(objConcepto.getCantidad().intValue());
                element.setUnitMeasure(objConcepto.getUnidad());
                element.setUnitPrice(objConcepto.getValorUnitario().doubleValue());
                logger.debug("Concepto: " + objConcepto);
                if (objConcepto.getCuentaPredial() != null) {
                    CuentaPredial cp = objConcepto.getCuentaPredial();
                    farmAccount = new FarmAccount();
                    farmAccount.setNumero(cp.getNumero());
                    cuentaPredial.add(farmAccount);
                    element.setCuentaPredial(cuentaPredial);
                    logger.debug("--Predial: " + cuentaPredial);
                } else if ((objConcepto.getInformacionAduaneraArray() != null)
                        && (objConcepto.getInformacionAduaneraArray().length > 0)) {
                    for (TInformacionAduanera ia : objConcepto
                            .getInformacionAduaneraArray()) {
                        customsInformation = new CustomsInformation();
                        customsInformation.setAduana(ia.getAduana());
                        customsInformation.setFecha(Util.convertirFecha(ia
                                .getFecha().getTime(), "dd/MM/yyyy"));
                        customsInformation.setNumero(ia.getNumero());
                        informacionAduanera.add(customsInformation);
                    }
                    element.setInformacionAduanera(informacionAduanera);
                    logger.debug("--Aduana: " + informacionAduanera);
                } else if (objConcepto.getParteArray() != null) {
                    for (Parte p : objConcepto.getParteArray()) {
                        part = new Part();
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
            invoice.setTipoFormato(cer.getCfdType());
            return create(invoice);
        } else {
            return null;
        }
    }*/

    public ByteArrayOutputStream create(File xmlFile)
            throws DocumentException, IOException, XmlException,
            TransformerException {
        logger.info("Iniciando la creacion del PDF");
        
        invoice = new Invoice_PDF();
        
        if (fileXML != null) {
        	
        	CFDIssued cfd = null;
//        	CFDIssued cfd = cFDIssuedManager.getByFolioSat(xmlFile.getName());
        	
        	this.fiscalEntityId = cfd.getFiscalEntity().getId();
        	
            FileCopyUtils.copy(new FileInputStream(fileXML), outA);

            this.rfc = cfd.getFiscalEntity().getTaxID();

            List<ElementsInvoice> elements = new ArrayList<ElementsInvoice>();

            InputStreamReader isreader = new InputStreamReader(new FileInputStream(
                    fileXML), "UTF-8");
            BufferedReader fr = new BufferedReader(isreader);

            StringBuilder s = new StringBuilder();
            while (fr.ready()) {
                s.append(fr.readLine());
            }
            this.numCtaPago = obtenerCampo(s.toString(), "NumCtaPago", 12);
            this.metodoDePago = obtenerCampo(s.toString(), "metodoDePago", 14);
            this.regimenFiscal = obtenerCampo(s.toString(), "Regimen=\"", 9);
            this.formaDePago = obtenerCampo(s.toString(), "formaDePago=\"", 13);

            compDoc = ComprobanteDocument.Factory.parse(
                    s.toString().replace("xmlns=\"http://www.santander.com.mx/schemas/xsd/AddendaSantanderV1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"", ""));

            if (compDoc.getComprobante().getVersion() != null && !compDoc.getComprobante().getVersion().equals("")) {
                this.version = compDoc.getComprobante().getVersion();
            }
            if (this.version == "2.0") {
                invoice.setLugarExpedicion(compDoc.getComprobante().getEmisor()
                        .getDomicilioFiscal().getEstado()
                        + ", "
                        + compDoc.getComprobante().getEmisor().getDomicilioFiscal()
                        .getPais());
            } else {
                invoice.setLugarExpedicion(obtenerCampo(s.toString(), "LugarExpedicion=", 17));
            }
            try {
                outCadena = xmlPro.generatesOriginalString(outA, this.version);
            } catch (ValidationException e) {
                e.printStackTrace();
            }
            logger.debug("Cadena Original: " + outCadena.toString());
            invoice.setCadena(outCadena.toString());
            invoice.setContractNumber(cfd.getContractNumber());
            invoice.setCustomerCode(cfd.getCustomerCode());
            invoice.setPeriod(cfd.getPeriod());
            invoice.setCostCenter(cfd.getCostCenter());
            invoice.setPorcentaje(0.00);
            if (compDoc.getComprobante().getImpuestos().getTraslados() != null) {
                for (Traslado objT : compDoc.getComprobante().getImpuestos()
                        .getTraslados().getTrasladoArray()) {
                    if (objT.getImpuesto().equals(Traslado.Impuesto.IVA)) {
                        invoice.setPorcentaje(objT.getTasa().doubleValue());
                    }
                }
            }

            comprobante = compDoc.getComprobante();
            moneda = "";
            ivaDescription = "";
            String encabezadoConcepto = "";
            if (comprobante.getAddenda().getAddendaSantanderV1()
                    .getCampoAdicionalArray() != null) {
                for (TCampoAdicional campo : comprobante.getAddenda()
                        .getAddendaSantanderV1().getCampoAdicionalArray()) {
                    if (campo.getCampo().equals("Descripcion Concepto")) {
                        encabezadoConcepto = campo.getValor();
                    }

                    if (campo.getCampo().equals("Moneda")) {
                        moneda = campo.getValor();
                    }

                    if (campo.getCampo().equals("Descripcion IVA")) {
                        ivaDescription = campo.getValor();
                    }
                    if (campo.getCampo().equals("Tipo Cambio")) {
                        tipoCambio = campo.getValor();
                    }
                }
            }

            invoice.setDescriptionConcept(encabezadoConcepto);

            direccion = compDoc.getComprobante().getEmisor().getNombre()
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

            invoice.setNoAprobacion(comprobante.getNoAprobacion().toString());
            invoice.setNoCertificado(comprobante.getNoCertificado());
            invoice.setFechaHora(Util.convertirFecha(comprobante.getFecha()
                    .getTime(), null));
            invoice.setSello(comprobante.getSello());
            invoice.setFolio(comprobante.getFolio());
            // invoice.setReference(comprobante.getSerie());
            invoice.setDate(Util.convertirFecha(comprobante.getFecha().getTime()));
            receptor = comprobante.getReceptor();

            invoice.setRfc(receptor.getRfc());

            invoice.setName(receptor.getNombre());
            tUbicacion = receptor.getDomicilio();
            invoice.setCalle(tUbicacion.getCalle());
            invoice.setCodigoPostal(tUbicacion.getCodigoPostal());
            invoice.setColonia(tUbicacion.getColonia());
            invoice.setEstado(tUbicacion.getEstado());
            invoice.setExterior(tUbicacion.getNoExterior());
            invoice.setInterior(tUbicacion.getNoInterior());
            invoice.setMunicipio(tUbicacion.getMunicipio());
            invoice.setReferencia(tUbicacion.getReferencia());
            invoice.setSubTotal(comprobante.getSubTotal().doubleValue());
            invoice.setTotal(comprobante.getTotal().doubleValue());
            invoice.setIva(comprobante.getImpuestos()
                    .getTotalImpuestosTrasladados().doubleValue());
            invoice.setQuantityWriting(NumberToLetterConverter
                    .convertNumberToLetter(invoice.getTotal()));
            invoice.setYearAprobacion(String.valueOf(comprobante.getAnoAprobacion()));
            invoice.setQuantityWriting(invoice.getQuantityWriting()
                    .replaceAll(
                            "MONEDAT",
                            UtilPDF.getMoneda(monedaManager.findByName(this.moneda)
                                    .getNombreLargo(), comprobante.getTotal()
                                    .doubleValue())));

            for (Concepto objConcepto : comprobante.getConceptos()
                    .getConceptoArray()) {
                element = new ElementsInvoice();
                informacionAduanera = new ArrayList<CustomsInformation>();
                cuentaPredial = new ArrayList<FarmAccount>();
                partes = new ArrayList<Part>();
                element.setAmount(objConcepto.getImporte().doubleValue());
                element.setDescription(objConcepto.getDescripcion());
                element.setQuantity(objConcepto.getCantidad().intValue());
                element.setUnitMeasure(objConcepto.getUnidad());
                element.setUnitPrice(objConcepto.getValorUnitario().doubleValue());
                logger.debug("Concepto: " + objConcepto);
                if (objConcepto.getCuentaPredial() != null) {
                    CuentaPredial cp = objConcepto.getCuentaPredial();
                    farmAccount = new FarmAccount();
                    farmAccount.setNumero(cp.getNumero());
                    cuentaPredial.add(farmAccount);
                    element.setCuentaPredial(cuentaPredial);
                    logger.debug("--Predial: " + cuentaPredial);
                } else if ((objConcepto.getInformacionAduaneraArray() != null)
                        && (objConcepto.getInformacionAduaneraArray().length > 0)) {
                    for (TInformacionAduanera ia : objConcepto
                            .getInformacionAduaneraArray()) {
                        customsInformation = new CustomsInformation();
                        customsInformation.setAduana(ia.getAduana());
                        customsInformation.setFecha(Util.convertirFecha(ia
                                .getFecha().getTime(), "dd/MM/yyyy"));
                        customsInformation.setNumero(ia.getNumero());
                        informacionAduanera.add(customsInformation);
                    }
                    element.setInformacionAduanera(informacionAduanera);
                    logger.debug("--Aduana: " + informacionAduanera);
                } else if (objConcepto.getParteArray() != null) {
                    for (Parte p : objConcepto.getParteArray()) {
                        part = new Part();
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
            invoice.setTipoFormato(cfd.getCfdType());
            return create(invoice);
        } else {
            return null;
        }
    }

    
    private String obtenerCampo(String xml, String valor, int suma) {
        int primera = xml.indexOf(valor);
        String cta = "";
        if (primera != -1) {
            primera += suma;
            while (xml.charAt(primera) != '"') {
                cta += xml.charAt(primera);
                primera++;
            }
        }
        if (cta.length() > 0)
            return cta;
        else
            return "NO IDENTIFICADO";
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    /**
     * @param str
     * @param token
     * @param number
     * @return
     */
    private int calculateIndexStr(String str, String token, int number) {
        int idx = 0;
        int res = 0;
        String valStr = str;
        for (int i = 0; i < number; i++) {
            int val = valStr.indexOf(token, idx);
            if (val < 0) {
                res = 0;
                break;
            }
            res = val;
            idx = val + 1;
        }
        return res;
    }

    public String getTipoCambio() {
        return tipoCambio;
    }

    public void setTipoCambio(String tipoCambio) {
        this.tipoCambio = tipoCambio;
    }
}