package com.interfactura.firmalocal.generapdf.impl;

import com.interfactura.firmalocal.datamodel.InformacionFactoraje;
import com.interfactura.firmalocal.datamodel.TimbreFiscal;
import com.interfactura.firmalocal.domain.entities.CFDFieldsV22;
import com.interfactura.firmalocal.domain.entities.CFDIssued;
import com.interfactura.firmalocal.domain.entities.Logo;
import com.interfactura.firmalocal.generapdf.util.UtilPDF;
import com.interfactura.firmalocal.ondemand.search.impl.BusquedaOnDemandImp;
import com.interfactura.firmalocal.persistence.CFDFieldsV22Manager;
import com.interfactura.firmalocal.persistence.CFDIssuedManager;
import com.interfactura.firmalocal.persistence.LogoManager;
import com.interfactura.firmalocal.xml.Properties;
import com.interfactura.firmalocal.xml.util.Util;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.qrcode.EncodeHintType;
import com.itextpdf.text.pdf.qrcode.ErrorCorrectionLevel;
import mx.gob.sat.cfd.x3.ComprobanteDocument;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;

import static com.interfactura.firmalocal.generapdf.util.UtilPDF.*;

/**
 * Clase para la creacion de PDF de factoraje version 3.2
 *
 * @author hlara
 */
@Component
public class PDFFactoraje32Impl {

    private Logger logger = Logger.getLogger(PDFFactoraje32Impl.class);

    @Autowired
    private Properties properties;
    @Autowired
    private LogoManager logoManager;
    @Autowired(required = true)
    private CFDIssuedManager cFDIssuedManager;
    @Autowired
    private CFDFieldsV22Manager cfdFieldsManager;
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

    private PdfCopyFields copy;
    private PdfStamper stamper;
    private ComprobanteDocument.Comprobante comprobante;
    private String cadena;
    private String direccion;
    private String domicilio;
    private String rfc;
    private List<InformacionFactoraje> elements;
    private String fac_hora;
    private String fac_tipo;
    private String fac_svn;
    private String fac_spb;
    private String fac_spf;
    private String fac_sid;
    private String fac_lci;
    private String fac_liva;
    private String fac_comision;
    private String fac_cletras;
    private long fiscalEntityId;
    private int conceptTotalRows = -1;
    private String[] remainingRows = new String[0];
    private String[] col2 = new String[0];
    private String[] col3 = new String[0];
    private String[] col4 = new String[0];
    private String[] col5 = new String[0];
    private String[] col6 = new String[0];
    private String[] col7 = new String[0];
    private String[] col8 = new String[0];
    private String[] col9 = new String[0];
    private String[] col10 = new String[0];
    private String[] col11 = new String[0];
    private boolean totalFlag = false;
    private String regimenFiscal;
    private String lugarExpedicion;
    private String metodoPago;
    private String numCtaPago;
    private TimbreFiscal timbre;
    private String contrato;
    private String periodo;
    private String folioInterno;

    public PDFFactoraje32Impl() {

    }

    private ByteArrayOutputStream create() throws DocumentException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copy = new PdfCopyFields(out);
        copy.open();
        this.concatenate();
        copy.close();
        return out;
    }

    private void concatenate() throws IOException, DocumentException {
        PdfReader pdfTemplate = new PdfReader(path + templateFactorajeCFDI);

        PdfReader copyR = new PdfReader(pdfTemplate);
        ByteArrayOutputStream tmpOUT = new ByteArrayOutputStream();
        stamper = new PdfStamper(copyR, tmpOUT);
        stamper.setFormFlattening(true);

        this.conceptTotalRows = -1;
        this.remainingRows = new String[0];
        this.col2 = new String[0];
        this.col3 = new String[0];
        this.col4 = new String[0];
        this.col5 = new String[0];
        this.col6 = new String[0];
        this.col7 = new String[0];
        this.col8 = new String[0];
        this.col9 = new String[0];
        this.col10 = new String[0];
        this.col11 = new String[0];


        this.totalFlag = false;

        this.header();
        this.conceptos(0, 0);
        this.footer();

        stamper.close();
        copyR.close();

        copy.addDocument(new PdfReader(new ByteArrayInputStream(tmpOUT.toByteArray())));

        // Verifico si se requiere mas de una hoja
        if (this.conceptTotalRows > 16) {
            while (this.remainingRows.length > 0) {
                ByteArrayOutputStream bas = new ByteArrayOutputStream();
                copyR = new PdfReader(path + templateFactorajeCFDI);
                stamper = new PdfStamper(copyR, bas);
                stamper.setFormFlattening(true);

                this.header();
                this.conceptos(0, 0);
                this.footer();
                stamper.close();
                copyR.close();
                copy.addDocument(new PdfReader(new ByteArrayInputStream(bas.toByteArray())));
            }
        }

        logger.debug("*************** Bandera de totales " + this.totalFlag);
        if (!this.totalFlag) {
            ByteArrayOutputStream bas = new ByteArrayOutputStream();
            copyR = new PdfReader(path + templateFactorajeCFDI);

            stamper = new PdfStamper(copyR, bas);
            stamper.setFormFlattening(true);

            this.header();
            this.conceptos(0, 0);
            this.footer();
            stamper.close();
            copyR.close();
            copy.addDocument(new PdfReader(new ByteArrayInputStream(bas.toByteArray())));
        }
        pdfTemplate.close();
    }

    private void header() throws IOException, DocumentException {
        creaLogoHeader();
        if(comprobante.getReceptor().getNombre() != null){
        	stamper.getAcroFields().setField("nombre", UtilPDF.getUTF8(comprobante.getReceptor().getNombre()));
        }else{
        	stamper.getAcroFields().setField("nombre", "");
        }
        
        stamper.getAcroFields().setField("rfc", comprobante.getReceptor().getRfc());
        stamper.getAcroFields().setField("domicilio", UtilPDF.getUTF8(domicilio));
        stamper.getAcroFields().setField("contrato", contrato);

        stamper.getAcroFields().setField("folio", comprobante.getFolio());
        stamper.getAcroFields().setField("operacion", folioInterno);
        stamper.getAcroFields().setField("fechaOperacion", periodo);
        stamper.getAcroFields().setField("horaOperacion", this.fac_hora);
        stamper.getAcroFields().setField("tipo", this.fac_tipo);
        stamper.getAcroFields().setField("fecha", Util.convertirFecha(comprobante.getFecha().getTime(), "dd/MM/yyyy"));

        if (this.fiscalEntityId != 0) {
            CFDFieldsV22 campos = cfdFieldsManager.findByFiscalID(this.fiscalEntityId);
            if (campos != null) {
                stamper.getAcroFields().setField("unidadMedida", campos.getUnidadDeMedida());
            }
        }

        stamper.getAcroFields().setField("regimenFiscal", this.regimenFiscal);
        stamper.getAcroFields().setField("lugarExpedicion", this.lugarExpedicion);
        stamper.getAcroFields().setField("metodoPago", this.metodoPago);
        stamper.getAcroFields().setField("numCtaPago", this.numCtaPago);
    }

    private void creaLogoHeader() throws DocumentException {
        Image chartImg = null;
        List<AcroFields.FieldPosition> position = stamper.getAcroFields().getFieldPositions("image");
        AcroFields.FieldPosition p = position.get(0);
        float page = p.page;
        Date dateEmision = comprobante.getFecha().getTime();
        Logo logo = null;
        if (this.fiscalEntityId > 0) {
            logo = logoManager.getByFEId(this.fiscalEntityId, dateEmision);
        }
        try {
            if (logo != null && logo.getImage() != null && logo.getImage().length > 0) {
                // Se toma la imagen proveniente de la entidad logo la cual esta
                // en un arreglo de bytes
                chartImg = Image.getInstance(logo.getImage());
                chartImg.scaleAbsoluteWidth(p.position.getWidth());
                chartImg.scaleAbsoluteHeight(p.position.getHeight());
                chartImg.setAbsolutePosition(p.position.getLeft(), p.position.getBottom());
                logger.debug("Desplegando la imagen: " + properties.getPathLogoPDF() + logo.getLogoName());
                logger.debug("Desplegando la imagen(Width): " + chartImg.getScaledWidth());
                logger.debug("Desplegando la imagen(Height): " + chartImg.getScaledHeight());
            } else {
                // Se toma la imagen configurada anteriormente
                chartImg = Image.getInstance(properties.getPathLogoPDF() + this.rfc + ".png");
                chartImg.scaleAbsoluteWidth(p.position.getWidth());
                chartImg.scaleAbsoluteHeight(p.position.getHeight());
                chartImg.setAbsolutePosition(p.position.getLeft(), p.position.getBottom());
                logger.debug("Desplegando la imagen: " + properties.getPathLogoPDF() + this.rfc + ".jpg");
                logger.debug("Desplegando la imagen(Width): " + chartImg.getScaledWidth());
                logger.debug("Desplegando la imagen(Height): " + chartImg.getScaledHeight());

            }
        } catch (IOException e) {
            try {
                chartImg = Image.getInstance(properties.getPathLogoPDF() + "logoFactoraje.png");
                logger.debug("Desplegando la imagen(Width): " + chartImg.getScaledWidth());
                logger.debug("Desplegando la imagen(Height): " + chartImg.getScaledHeight());
                chartImg.scaleAbsoluteWidth(p.position.getWidth());
                chartImg.scaleAbsoluteHeight(p.position.getHeight());
                chartImg.setAbsolutePosition(p.position.getLeft(), p.position.getBottom());
            } catch (IOException ioe) {
                System.out.println("<<<< Error al obtener la imagen por default: " + ioe.getMessage());
                System.out.println(Arrays.toString(ioe.getStackTrace()));
            }
        }

        if (chartImg != null) {
            PdfContentByte ourContent = stamper.getOverContent((int) page);
            ourContent.addImage(chartImg);
        }
    }

    private int conceptos(int begin, int limit) throws IOException, DocumentException {
        int numberConcept = 0;
        StringBuilder _1 = new StringBuilder();
        StringBuilder _2 = new StringBuilder();
        StringBuilder _3 = new StringBuilder();
        StringBuilder _4 = new StringBuilder();
        StringBuilder _5 = new StringBuilder();
        StringBuilder _6 = new StringBuilder();
        StringBuilder _7 = new StringBuilder();
        StringBuilder _8 = new StringBuilder();
        StringBuilder _9 = new StringBuilder();
        StringBuilder _10 = new StringBuilder();
        StringBuilder _11 = new StringBuilder();
        
        if (this.conceptTotalRows == -1) {
            List<InformacionFactoraje> objSubList = null;
            if (limit != 0) {
                objSubList = elements.subList(begin - 1, limit);
            } else {
                objSubList = elements;
            }

            List<String> conceptosArr = new ArrayList<String>();
            List<String> lstCol2 = new ArrayList<String>();
            List<String> lstCol3 = new ArrayList<String>();
            List<String> lstCol4 = new ArrayList<String>();
            List<String> lstCol5 = new ArrayList<String>();
            List<String> lstCol6 = new ArrayList<String>();
            List<String> lstCol7 = new ArrayList<String>();
            List<String> lstCol8 = new ArrayList<String>();
            List<String> lstCol9 = new ArrayList<String>();
            List<String> lstCol10 = new ArrayList<String>();
            List<String> lstCol11 = new ArrayList<String>();
            for (InformacionFactoraje obj : objSubList) {
                String descRows[] = UtilPDF.longitudFija(obj.getDeudorProveedor(), 30)[0].split("\n");
                numberConcept = numberConcept + descRows.length;

                int conceptEnters = 0;

                for (String descRow : descRows) {
                    logger.debug("***************" + descRow);
                    conceptosArr.add(Util.isNullEmpity(descRow) + "\n");
                    conceptEnters += 1;
                }
                
                setListElements(lstCol2, conceptEnters, obj.getTipoDocumento());
                setListElements(lstCol3, conceptEnters, obj.getNumeroDocumento());
                setListElements(lstCol4, conceptEnters, obj.getFechaVencimiento());
                setListElements(lstCol5, conceptEnters, obj.getPlazo());
                if(obj.getValorNominal() != null && !obj.getValorNominal().equals("")){
                    setListElements(lstCol6, conceptEnters, this.formatMonto(new BigDecimal(obj.getValorNominal())));
                }else{
                    setListElements(lstCol6, conceptEnters, "");
                }
                setListElements(lstCol7, conceptEnters, obj.getAforo());
                if(obj.getPrecioBase() != null && !obj.getPrecioBase().equals("")){
                    setListElements(lstCol8, conceptEnters, this.formatMonto(new BigDecimal(obj.getPrecioBase())));
                }else{
                    setListElements(lstCol8, conceptEnters, "");
                }
                setListElements(lstCol9, conceptEnters, obj.getTasaDescuento());
                if(obj.getPrecioFactoraje() != null && !obj.getPrecioFactoraje().equals("")){
                    setListElements(lstCol10, conceptEnters, this.formatMonto(new BigDecimal(obj.getPrecioFactoraje())));
                }else{
                    setListElements(lstCol10, conceptEnters, "");
                }
                if(obj.getImporteDescuento() != null && !obj.getImporteDescuento().equals("")){
                    setListElements(lstCol11, conceptEnters, this.formatMonto(new BigDecimal(obj.getImporteDescuento())));
                }else{
                    setListElements(lstCol11, conceptEnters, "");
                }
                this.conceptTotalRows = numberConcept;
            }

            this.remainingRows = conceptosArr.toArray(new String[conceptosArr.size()]);
            logger.debug("************************* Tamano lista 2: " + lstCol2.size());
            this.col2 = lstCol2.toArray(new String[lstCol2.size()]);
            this.col3 = lstCol3.toArray(new String[lstCol3.size()]);
            this.col4 = lstCol4.toArray(new String[lstCol4.size()]);
            this.col5 = lstCol5.toArray(new String[lstCol5.size()]);
            this.col6 = lstCol6.toArray(new String[lstCol6.size()]);
            this.col7 = lstCol7.toArray(new String[lstCol7.size()]);
            this.col8 = lstCol8.toArray(new String[lstCol8.size()]);
            this.col9 = lstCol9.toArray(new String[lstCol9.size()]);
            this.col10 = lstCol10.toArray(new String[lstCol10.size()]);
            this.col11 = lstCol11.toArray(new String[lstCol11.size()]);
        }

        // En descRows almaceno lo que voy a imprimir en la hoja.
        // Una variable por cada columna se requiere
        String[] descRows;
        String[] descRows2;
        String[] descRows3;
        String[] descRows4;
        String[] descRows5;
        String[] descRows6;
        String[] descRows7;
        String[] descRows8;
        String[] descRows9;
        String[] descRows10;
        String[] descRows11;
        // Si quedan mas de 16, entonces proceso solo 16 y almaceno los
        // faltantes en remainingRows
        int numRows;
        if (remainingRows.length > 16) {
            descRows = getPrintElements(1);
            numRows = descRows.length;
            descRows2 = getPrintElements(2);
            descRows3 = getPrintElements(3);
            descRows4 = getPrintElements(4);
            descRows5 = getPrintElements(5);
            descRows6 = getPrintElements(6);
            descRows7 = getPrintElements(7);
            descRows8 = getPrintElements(8);
            descRows9 = getPrintElements(9);
            descRows10 = getPrintElements(10);
            descRows11 = getPrintElements(11);
        }
        // Si no, entonces proceso los faltantes
        else {
            // Pongo en descRows los que faltan
            descRows = remainingRows;
            numRows = descRows.length;
            descRows2 = this.col2;
            descRows3 = this.col3;
            descRows4 = this.col4;
            descRows5 = this.col5;
            descRows6 = this.col6;
            descRows7 = this.col7;
            descRows8 = this.col8;
            descRows9 = this.col9;
            descRows10 = this.col10;
            descRows11 = this.col11;
            // Inicializo remainingRows a cero para que ya no vuelva a entrar
            remainingRows = new String[0];
        }

        // Pinto en el pdf los registros de los conceptos
        for (int i = 0; i < descRows.length; i++) {
            _1.append(descRows[i]);
            _2.append(descRows2[i]);
            _3.append(descRows3[i]);
            _4.append(descRows4[i]);
            _5.append(descRows5[i]);
            _6.append(descRows6[i]);
            _7.append(descRows7[i]);
            _8.append(descRows8[i]);
            _9.append(descRows9[i]);
            _10.append(descRows10[i]);
            _11.append(descRows11[i]);
        }

        
        if (numRows <= 14 && (!this.totalFlag)) {
            logger.debug("********** Se modifico la bandera de Total");
            this.totalFlag = true;
            if (!Util.isNullEmpty(this.fac_svn)) {
                _6.append("__________________\n").append(this.formatMontoS(this.fac_svn));
                _7.append("_______\n");
            }

            if (!Util.isNullEmpty(this.fac_spb)) {
                _8.append("____________\n").append(this.formatMontoS(this.fac_spb));
                _9.append("_________\n");
            }

            if (!Util.isNullEmpty(this.fac_spf)) {
                _10.append("_________\n").append(this.formatMontoS(this.fac_spf));
            }

            if (!Util.isNullEmpty(this.fac_sid)) {
                _11.append("____________________\n").append(this.formatMontoS(this.fac_sid));
            }
        }
        
        stamper.getAcroFields().setField("_1", UtilPDF.getUTF8(_1.toString()));
        stamper.getAcroFields().setField("_2", UtilPDF.getUTF8(_2.toString()));
        stamper.getAcroFields().setField("_3", UtilPDF.getUTF8(_3.toString()));
        stamper.getAcroFields().setField("_4", UtilPDF.getUTF8(_4.toString()));
        stamper.getAcroFields().setField("_5", UtilPDF.getUTF8(_5.toString()));
        stamper.getAcroFields().setField("_6", UtilPDF.getUTF8(_6.toString()));
        stamper.getAcroFields().setField("_7", UtilPDF.getUTF8(_7.toString()));
        stamper.getAcroFields().setField("_8", UtilPDF.getUTF8(_8.toString()));
        stamper.getAcroFields().setField("_9", UtilPDF.getUTF8(_9.toString()));
        stamper.getAcroFields().setField("_10", UtilPDF.getUTF8(_10.toString()));
        stamper.getAcroFields().setField("_11", UtilPDF.getUTF8(_11.toString()));

        return numberConcept;
    }

    private String[] getPrintElements(int type) {
        String[] descRows = new String[16];
        String[] elements = null;

        if (type == 1) {
            elements = remainingRows;
        } else if (type == 2) {
            elements = this.col2;
        } else if (type == 3) {
            elements = this.col3;
        } else if (type == 4) {
            elements = this.col4;
        } else if (type == 5) {
            elements = this.col5;
        } else if (type == 6) {
            elements = this.col6;
        } else if (type == 7) {
            elements = this.col7;
        } else if (type == 8) {
            elements = this.col8;
        } else if (type == 9) {
            elements = this.col9;
        } else if (type == 10) {
            elements = this.col10;
        } else if (type == 11) {
            elements = this.col11;
        }

        // Le asigno los que son
        System.arraycopy(elements, 0, descRows, 0, 16);

        // Actualizo remainingRows a que solo tenga los que me van a faltar
        String[] tempR = new String[0];
        if (elements != null) {
            tempR = new String[elements.length - 16];
        }
        System.arraycopy(elements, 16, tempR, 0, tempR.length);
        elements = tempR;

        if (type == 1) {
            remainingRows = elements;
        } else if (type == 2) {
            this.col2 = elements;
        } else if (type == 3) {
            this.col3 = elements;
        } else if (type == 4) {
            this.col4 = elements;
        } else if (type == 5) {
            this.col5 = elements;
        } else if (type == 6) {
            this.col6 = elements;
        } else if (type == 7) {
            this.col7 = elements;
        } else if (type == 8) {
            this.col8 = elements;
        } else if (type == 9) {
            this.col9 = elements;
        } else if (type == 10) {
            this.col10 = elements;
        } else {
            this.col11 = elements;
        }

        return descRows;
    }

    private void setListElements(List<String> lst, int conceptEnters, String value) {
        int begin = 0;
        
        if (!Util.isNullEmpty(value)) {
            lst.add(value.concat("\n"));
            begin = 1;
        }

        for (; begin < conceptEnters; begin++) {
            lst.add("\n");
        }
    }

    private void footer() throws IOException, DocumentException {
        stamper.getAcroFields().setField("direccion", UtilPDF.getUTF8(direccion));
        stamper.getAcroFields().setField("total", this.formatMonto(comprobante.getTotal()));
        stamper.getAcroFields().setField("lblComision", this.fac_lci);
        stamper.getAcroFields().setField("lblIva", this.fac_liva);
        if (!Util.isNullEmpty(this.fac_comision)) {
            stamper.getAcroFields().setField("valorComision", this.formatMontoS(this.fac_comision));
        }
        stamper.getAcroFields().setField("total_Letras", this.fac_cletras);

        stamper.getAcroFields().setField("valorIva", comprobante.getImpuestos().getTotalImpuestosTrasladados() != null ? this
                .formatMonto(comprobante.getImpuestos().getTotalImpuestosTrasladados()) : "");

        stamper.getAcroFields().setField("certificadoEmisor", comprobante.getNoCertificado());
        stamper.getAcroFields().setField("certificadoSat", timbre.getNoCertificadoSat());
        stamper.getAcroFields().setField("uuid", timbre.getUuid());
        stamper.getAcroFields().setField("fechaTimbrado", timbre.getFechaTimbrado());
        stamper.getAcroFields().setField("fechahora", Util.convertirFecha(comprobante.getFecha().getTime()));
        stamper.getAcroFields().setField("selloEmisor", comprobante.getSello());
        stamper.getAcroFields().setField("selloSat", timbre.getSelloSat());
        stamper.getAcroFields().setField("cadena", cadena);
        generaCodigoQR();
    }


    /*public ByteArrayOutputStream create(CFDIssued cer) throws DocumentException, IOException, XmlException, TransformerException {

        if (cer.getFiscalEntity() != null && cer.getFiscalEntity().getId() > 0) {
            this.fiscalEntityId = cer.getFiscalEntity().getId();
        } else {
            CFDIssued cfdIssued = cFDIssuedManager.get(cer.getId());
            if (cfdIssued != null && cfdIssued.getFiscalEntity() != null) {
                this.fiscalEntityId = cfdIssued.getFiscalEntity().getId();
            }
        }

        logger.debug("Iniciando la creacion del PDF Factoraje");

        ByteArrayOutputStream outA = new ByteArrayOutputStream();
        BusquedaOnDemandImp searchXML = new BusquedaOnDemandImp(this.serverName, this.userName, this.password,
                this.folderNameInterEmision, this.folderNameInterRecepcion, this.folderNameInterEstadoCuenta,
                this.rutaArchivo, this.nombreArchivo, this.extencionArchivo);
        File fileXML;
        try {
            fileXML = searchXML.busquedaInterfacturaEmisionCFDI("", "", "", cer.getFolioSAT());
        } catch (Exception e) {
            fileXML = null;
            System.out.println("<<<< El archivo es nulo: " + e.getMessage());
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
        if (fileXML != null) {
            BufferedReader fr = new BufferedReader(new FileReader(fileXML));

            StringBuilder s = new StringBuilder();
            while (fr.ready()) {
                s.append(fr.readLine());
            }

            ComprobanteDocument compDoc = ComprobanteDocument.Factory.parse(s.toString()
                    .replace("xmlns=\"http://www.santander.com.mx/schemas/xsd/AddendaSantanderV1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"", ""));

            try {
                asignaElementosFaltantes(fileXML);
            } catch (Exception e) {
                System.out.println("<<<< Error al parsear el xml");
            }

            FileCopyUtils.copy(new FileInputStream(fileXML), outA);
            this.rfc = cer.getFiscalEntity().getTaxID();

            comprobante = compDoc.getComprobante();
            this.cadena = generaCadena(timbre);

            direccion = comprobante.getEmisor().getNombre() + "\n" + comprobante.getEmisor().getDomicilioFiscal().getCalle();
            if (comprobante.getEmisor().getDomicilioFiscal().getNoExterior() != null) {
                direccion += " " + comprobante.getEmisor().getDomicilioFiscal().getNoExterior();
            }

            if (comprobante.getEmisor().getDomicilioFiscal().getNoInterior() != null) {
                direccion += " " + comprobante.getEmisor().getDomicilioFiscal().getNoInterior();
            }
            direccion += ", " + Util.isNull(comprobante.getEmisor().getDomicilioFiscal().getReferencia())
                    + " COL. " + comprobante.getEmisor().getDomicilioFiscal().getColonia()
                    + ", " + comprobante.getEmisor().getDomicilioFiscal().getMunicipio()
                    + ", C.P. " + comprobante.getEmisor().getDomicilioFiscal().getCodigoPostal()
                    + ", " + comprobante.getEmisor().getDomicilioFiscal().getEstado()
                    + ", " + comprobante.getEmisor().getDomicilioFiscal().getPais()
                    + " R.F.C. " + comprobante.getEmisor().getRfc();

            String interior = "";
            String exterior = "";
            String colonia = "";
            String cp = "";
            String municipio = "";
            String estado = "";
            String calle = "";
            if (!Util.isNullEmpty(comprobante.getReceptor().getDomicilio().getNoInterior())) {
                interior = " NO. INTERIOR: " + interior;
            }

            if (!Util.isNullEmpty(comprobante.getReceptor().getDomicilio().getCalle())) {
                calle = comprobante.getReceptor().getDomicilio().getCalle();
            }

            if (!Util.isNullEmpty(comprobante.getReceptor().getDomicilio().getNoExterior())) {
                exterior = " NO. EXTERIOR: " + comprobante.getReceptor().getDomicilio().getNoExterior();
            }

            if (!Util.isNullEmpty(comprobante.getReceptor().getDomicilio().getColonia())) {
                colonia = " COL. " + comprobante.getReceptor().getDomicilio().getColonia();
            }

            if (!Util.isNullEmpty(comprobante.getReceptor().getDomicilio().getCodigoPostal())) {
                cp = " C.P. " + comprobante.getReceptor().getDomicilio().getCodigoPostal();
            }

            if (!Util.isNullEmpty(comprobante.getReceptor().getDomicilio().getMunicipio())) {
                municipio = " " + comprobante.getReceptor().getDomicilio().getMunicipio();
            }

            if (!Util.isNullEmpty(comprobante.getReceptor().getDomicilio().getEstado())) {
                estado = ", " + comprobante.getReceptor().getDomicilio().getEstado();
            }

            domicilio = calle + exterior + interior + colonia + cp + municipio + estado;
            comprobante.setFolio(String.valueOf(cer.getFolioInterno()));
            return create();
        } else {
            return null;
        }

    }*/

    
    public ByteArrayOutputStream create(File fileXML) throws DocumentException, IOException, XmlException, TransformerException {
    	/*
        if (cer.getFiscalEntity() != null && cer.getFiscalEntity().getId() > 0) {
            this.fiscalEntityId = cer.getFiscalEntity().getId();
        } else {
            CFDIssued cfdIssued = cFDIssuedManager.get(cer.getId());
            if (cfdIssued != null && cfdIssued.getFiscalEntity() != null) {
                this.fiscalEntityId = cfdIssued.getFiscalEntity().getId();
            }
        }

        logger.debug("Iniciando la creacion del PDF Factoraje");

        ByteArrayOutputStream outA = new ByteArrayOutputStream();
        BusquedaOnDemandImp searchXML = new BusquedaOnDemandImp(this.serverName, this.userName, this.password,
                this.folderNameInterEmision, this.folderNameInterRecepcion, this.folderNameInterEstadoCuenta,
                this.rutaArchivo, this.nombreArchivo, this.extencionArchivo);
        File fileXML;
        
        try {
            fileXML = searchXML.busquedaInterfacturaEmisionCFDI("", "", "", cer.getFolioSAT());
        } catch (Exception e) {
            fileXML = null;
            System.out.println("<<<< El archivo es nulo: " + e.getMessage());
            System.out.println(Arrays.toString(e.getStackTrace()));
        }*/
        if (fileXML != null) {
            BufferedReader fr = new BufferedReader(new FileReader(fileXML));

            StringBuilder s = new StringBuilder();
            while (fr.ready()) {
                s.append(fr.readLine());
            }

            ComprobanteDocument compDoc = ComprobanteDocument.Factory.parse(s.toString()
                    .replace("xmlns=\"http://www.santander.com.mx/schemas/xsd/AddendaSantanderV1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"", ""));

            try {
                asignaElementosFaltantes(fileXML);
            } catch (Exception e) {
                System.out.println("<<<< Error al parsear el xml");
            }

            //FileCopyUtils.copy(new FileInputStream(fileXML), outA);
            //this.rfc = cer.getFiscalEntity().getTaxID();

            comprobante = compDoc.getComprobante();
            this.cadena = generaCadena(timbre);

            this.rfc = comprobante.getEmisor().getRfc(); 
            
            direccion = comprobante.getEmisor().getNombre() + "\n" + comprobante.getEmisor().getDomicilioFiscal().getCalle();
            if (comprobante.getEmisor().getDomicilioFiscal().getNoExterior() != null) {
                direccion += " " + comprobante.getEmisor().getDomicilioFiscal().getNoExterior();
            }

            if (comprobante.getEmisor().getDomicilioFiscal().getNoInterior() != null) {
                direccion += " " + comprobante.getEmisor().getDomicilioFiscal().getNoInterior();
            }
            direccion += ", " + Util.isNull(comprobante.getEmisor().getDomicilioFiscal().getReferencia())
                    + " COL. " + comprobante.getEmisor().getDomicilioFiscal().getColonia()
                    + ", " + comprobante.getEmisor().getDomicilioFiscal().getMunicipio()
                    + ", C.P. " + comprobante.getEmisor().getDomicilioFiscal().getCodigoPostal()
                    + ", " + comprobante.getEmisor().getDomicilioFiscal().getEstado()
                    + ", " + comprobante.getEmisor().getDomicilioFiscal().getPais()
                    + " R.F.C. " + comprobante.getEmisor().getRfc();

            String interior = "";
            String exterior = "";
            String colonia = "";
            String cp = "";
            String municipio = "";
            String estado = "";
            String calle = "";
            if (!Util.isNullEmpty(comprobante.getReceptor().getDomicilio().getNoInterior())) {
                interior = " NO. INTERIOR: " + interior;
            }

            if (!Util.isNullEmpty(comprobante.getReceptor().getDomicilio().getCalle())) {
                calle = comprobante.getReceptor().getDomicilio().getCalle();
            }

            if (!Util.isNullEmpty(comprobante.getReceptor().getDomicilio().getNoExterior())) {
                exterior = " NO. EXTERIOR: " + comprobante.getReceptor().getDomicilio().getNoExterior();
            }

            if (!Util.isNullEmpty(comprobante.getReceptor().getDomicilio().getColonia())) {
                colonia = " COL. " + comprobante.getReceptor().getDomicilio().getColonia();
            }

            if (!Util.isNullEmpty(comprobante.getReceptor().getDomicilio().getCodigoPostal())) {
                cp = " C.P. " + comprobante.getReceptor().getDomicilio().getCodigoPostal();
            }

            if (!Util.isNullEmpty(comprobante.getReceptor().getDomicilio().getMunicipio())) {
                municipio = " " + comprobante.getReceptor().getDomicilio().getMunicipio();
            }

            if (!Util.isNullEmpty(comprobante.getReceptor().getDomicilio().getEstado())) {
                estado = ", " + comprobante.getReceptor().getDomicilio().getEstado();
            }

            domicilio = calle + exterior + interior + colonia + cp + municipio + estado;
            //comprobante.setFolio(String.valueOf(cer.getFolioInterno()));
            comprobante.setFolio(folioInterno);
            return create();
        } else {
            return null;
        }

    }
    
    private String formatMonto(BigDecimal val) {
        String res = "";
        if (val != null) {
            res = val + "";
        }
        try {
            NumberFormat nf = NumberFormat.getCurrencyInstance();
            res = nf.format(val);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    private String formatMontoS(String val) {
        String res = val + "";
        try {
            BigDecimal monto = BigDecimal.valueOf(Double.parseDouble(val));
            return this.formatMonto(monto);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    private void generaCodigoQR() throws DocumentException {
        String contenido = "";
        contenido += "?re=" + this.rfc;
        contenido += "&rr=" + comprobante.getReceptor().getRfc();
        contenido += "&tt=" + formatTotal(quitaExponente(String.valueOf(comprobante.getTotal())));
        contenido += "&id=" + timbre.getUuid();
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

    private void asignaElementosFaltantes(File file) throws Exception {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document dom = db.parse(file);
        // Se asignan al objeto invoice los nuevos elementos de CFD 22
        // se leen con SAX ya que el objeto de XML no esta actualizado

        Element docEle = dom.getDocumentElement();

        metodoPago = docEle.getAttribute("metodoDePago");
        lugarExpedicion = docEle.getAttribute("LugarExpedicion");
        numCtaPago = docEle.getAttribute("NumCtaPago");

        NodeList nl = docEle.getElementsByTagName("cfdi:RegimenFiscal");
        if (nl != null && nl.getLength() > 0) {
            for (int i = 0; i < nl.getLength(); i++) {
                Element el = (Element) nl.item(i);
                regimenFiscal = el.getAttribute("Regimen");
            }
        }

        //Datos del timbre fiscal digital
        nl = docEle.getElementsByTagName("tfd:TimbreFiscalDigital");
        timbre = new TimbreFiscal();
        if (nl != null && nl.getLength() > 0) {
            Element el = (Element) nl.item(0);
            timbre.setVersion(el.getAttribute("version"));
            timbre.setUuid(el.getAttribute("UUID"));
            timbre.setSelloSat(el.getAttribute("selloSAT"));
            timbre.setNoCertificadoSat(el.getAttribute("noCertificadoSAT"));
            timbre.setFechaTimbrado(el.getAttribute("FechaTimbrado"));
        }

        //Datos Addenda
        nl = docEle.getElementsByTagName("as:InformacionEmision");
        if (nl != null && nl.getLength() > 0) {
            Element el = (Element) nl.item(0);

            contrato = el.getAttribute("contrato");
            periodo = el.getAttribute("periodo");
            folioInterno = el.getAttribute("folioInterno");

        }

        this.fac_hora = "";
        this.fac_tipo = "";
        this.fac_svn = "";
        this.fac_spb = "";
        this.fac_spf = "";
        this.fac_sid = "";
        this.fac_lci = "";
        this.fac_liva = "";
        this.fac_comision = "";
        this.fac_cletras = "";
        
        //Datos Addenda
        nl = docEle.getElementsByTagName("as:CampoAdicional");
        if (nl != null && nl.getLength() > 0) {
            for (int i = 0; i < nl.getLength(); i++) {
                Element el = (Element) nl.item(i);
                String campo = el.getAttribute("campo");
                String valor = el.getAttribute("valor");

                if (campo.equals("FAC_HORA")) {
                    fac_hora = valor;
                } else if (campo.equals("FAC_TIPO")) {
                    fac_tipo = valor;
                } else if (campo.equals("FAC_SVN")) {
                    fac_svn = valor;
                } else if (campo.equals("FAC_SPB")) {
                    fac_spb = valor;
                } else if (campo.equals("FAC_SPF")) {
                    fac_spf = valor;
                } else if (campo.equals("FAC_SID")) {
                    fac_sid = valor;
                } else if (campo.equals("FAC_LCI")) {
                    fac_lci = valor;
                } else if (campo.equals("FAC_LIVA")) {
                    fac_liva = valor;
                } else if (campo.equals("FAC_COMISION")) {
                    fac_comision = valor;
                } else if (campo.equals("FAC_LETRAS")) {
                    fac_cletras = valor;
                }
            }

            elements = new ArrayList<InformacionFactoraje>();

            nl = docEle.getElementsByTagName("as:InformacionFactoraje");
            if (nl != null && nl.getLength() > 0) {
                for (int i = 0; i < nl.getLength(); i++) {
                    Element el = (Element) nl.item(i);
                    InformacionFactoraje inf = new InformacionFactoraje();
                    inf.setDeudorProveedor(el.getAttribute("deudorProveedor"));
                    inf.setTipoDocumento(el.getAttribute("tipoDocumento"));
                    inf.setNumeroDocumento(el.getAttribute("numeroDocumento"));
                    inf.setFechaVencimiento(el.getAttribute("fechaVencimiento"));
                    inf.setPlazo(el.getAttribute("plazo"));
                    inf.setValorNominal(el.getAttribute("valorNominal"));
                    inf.setAforo(el.getAttribute("aforo"));
                    inf.setPrecioBase(el.getAttribute("precioBase"));
                    inf.setTasaDescuento(el.getAttribute("tasaDescuento"));
                    inf.setPrecioFactoraje(el.getAttribute("precioFactoraje"));
                    inf.setImporteDescuento(el.getAttribute("importeDescuento"));
                    elements.add(inf);
                }
            }
        }


    }

    private String generaCadena(TimbreFiscal timbreFiscal) {
        return "||" + timbreFiscal.getVersion() + "|" + timbreFiscal.getUuid() + "|" + timbreFiscal.getFechaTimbrado() +
                "|" +  comprobante.getSello() + "|" + timbreFiscal.getNoCertificadoSat() + "||";
    }
}
