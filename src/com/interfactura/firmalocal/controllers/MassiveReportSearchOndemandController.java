package com.interfactura.firmalocal.controllers;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import mx.gob.sat.cfd.x2.TCampoAdicional;
import mx.gob.sat.cfd.x2.TUbicacion;
import mx.gob.sat.cfd.x3.ComprobanteDocument;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.ibm.edms.od.ODFolder;
import com.ibm.edms.od.ODServer;
import com.interfactura.firmalocal.datamodel.ElementsInvoice;
import com.interfactura.firmalocal.datamodel.Invoice_Masivo;
import com.interfactura.firmalocal.domain.entities.CFDIssued;
import com.interfactura.firmalocal.domain.entities.Customer;
import com.interfactura.firmalocal.ondemand.search.impl.BusquedaOnDemandImp;
import com.interfactura.firmalocal.ondemand.util.FilesOndemand;
import com.interfactura.firmalocal.persistence.CFDIssuedManager;
import com.interfactura.firmalocal.persistence.CustomerManager;
import com.interfactura.firmalocal.xml.Properties;
import com.interfactura.firmalocal.xml.util.ParseXML_CFDI;
import com.interfactura.firmalocal.xml.util.Util;



@Controller
public class MassiveReportSearchOndemandController {
	@Autowired
	Properties properties;
	
	private int maxConceptos;
	@Autowired(required = true)
	private CFDIssuedManager cFDIssuedManager;

	@Autowired(required = true)
	private CustomerManager customerManager;
	
	public MassiveReportSearchOndemandController(){
		
	}
	/*
	public void readIdReportProcess(String nProceso){
		try{
			BusquedaOnDemandImp searchXML = new BusquedaOnDemandImp(properties.getOndemandServer(), properties.getOndemandUser(), properties.getOndemandPass(),
	    			properties.getOndemandFolderEmision(), properties.getOndemandFolderRecepcion(), properties.getOndemandFolderEstadoCuenta(),
	                properties.getPathReportesProceso(), "xmlTemporal", ".xml");
			
			FileInputStream fsIdReportProcess = new FileInputStream(properties.getPathReportesEntrada() + "IDREPORTPROCESS_" + nProceso + ".TXT");
			DataInputStream in = new DataInputStream(fsIdReportProcess);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			FileOutputStream fileStatus = new FileOutputStream(properties.getPathReportesProceso() + "STATUS_SEARCHONDEMAND_" + nProceso + ".TXT");
			fileStatus.write("Status del proceso bash reportSearchOndemand.sh\n".getBytes("UTF-8"));
			
			//Conexion a ondemand
			ODServer serv = null;
			int maxHits = 1000000;
			serv = searchXML.connectOnDemad();
            ODFolder folderObtenido = serv.openFolder(properties.getOndemandFolderEmision());
            if (maxHits > 0) {
                folderObtenido.setMaxHits((int) maxHits);// Se modifica num max respuesta
            }
            
			String strID;
			int counter = 0;
			while((strID = br.readLine()) != null){
				File fileDirectory = new File(properties.getPathReportesProceso() + strID + "/");
				if(fileDirectory.exists()){
					List<FilesOndemand> listFiles = searchXML.busquedaInterfacturaEmisionCFDIReporteMasivo(1000000, properties.getPathReportesProceso(), strID, folderObtenido);
									
					if(listFiles.size()>0){			
						
						FileOutputStream fileReport = new FileOutputStream(properties.getPathReportesProceso() + strID + "/" + strID + "REPORT.TXT");		        
						for(int index=0; index<listFiles.size(); index++){
							//fileReport.write(extractionInfo(listFiles.get(index).getFiles(), listFiles.get(index).getValuesCfd(), strID).getBytes("UTF-8"));
							fileReport.write(extractionInfo(listFiles.get(index).getStrPath(), listFiles.get(index).getValuesCfd(), strID).getBytes("UTF-8"));
						}
						fileReport.close();
						
						fileStatus.write(("El archivo " + strID + "REPORT.TXT ha sido generado exitosamente\n").getBytes("UTF-8"));
											
					}else{
						
						FileOutputStream fileReport = new FileOutputStream(properties.getPathReportesProceso() + strID + "/" + strID + "REPORT.TXT");
						fileReport.write(("No se encontraron facturas XML en Ondemand, para la solicitud con ID " + strID + "\n").getBytes("UTF-8"));
						fileReport.close();
						
						fileStatus.write(("No se encontraron facturas XML en Ondemand, para la solicitud con ID " + strID + "\n").getBytes("UTF-8"));
					}
				}else{
					fileStatus.write(("No se encontro el directorio " + properties.getPathReportesProceso() + strID + "/\n").getBytes("UTF-8"));
				}
				//if(buildReportTxt(strID)){
				//	fileStatus.write(("Informacion generada para la solicitud con ID " + strID + "\n").getBytes("UTF-8"));
				//}else{
				//	fileStatus.write(("No se encontraron registros en base de datos, para la solicitud con ID " + strID + "\n").getBytes("UTF-8"));
				//}
				
				counter++;
			}
			br.close();
			in.close();
			fsIdReportProcess.close();
			if(counter == 0){
				fileStatus.write(("No se encontraron solicitudes a procesar\n").getBytes("UTF-8"));	
			}
			fileStatus.close();
			searchXML.disconnectOnDemand(serv);
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Exception reportSearchOndemand:" + e.getMessage());
			try {
				//FileOutputStream fileError = new FileOutputStream(properties.getPathReportesProceso() + "ERROR_SEARCHONDEMAND_" + nProceso + ".TXT");
				//FileWriter fwError = new FileWriter(properties.getPathReportesProceso() + "ERROR_SEARCHONDEMAND.TXT");
				//BufferedWriter bwError = new BufferedWriter(fwError);
				//bwError.write(e.getMessage());
				//bwError.close();
				//fileError.write((e.getMessage()).getBytes("UTF-8"));
				//fileError.close();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.out.println("Exception al crear ERROR_SEARCHONDEMAND_" + nProceso + ".TXT:" + e.getMessage());
			}
			
		}
	}
	*()
	/*
	public boolean buildReportTxt(String ID) throws Exception{
		RandomAccessFile aFile = new RandomAccessFile(properties.getPathReportesProceso() + ID + "CFDS.TXT", "r");
        FileChannel inChannel = aFile.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
                
        FileOutputStream fileReport = new FileOutputStream(properties.getPathReportesProceso() + ID + "REPORT.TXT");
        boolean findInfo = false;
        
        String strLine = "";
        int counter =0;
        while(inChannel.read(buffer) > 0)
        {
            buffer.flip();
            for (int i = 0; i < buffer.limit(); i++)
            {
            	byte valor = buffer.get();            	
            	if(valor != '\n'){
            		strLine+=String.valueOf((char)valor).trim();            		
            	}else{
            		System.out.println("linea " + counter + ":--"+strLine+"--");
            		if(!"".equals(strLine)){
            			String [] valuesCfd = strLine.split("<delimiter>"); 
            			
            			List<File> files = searchCFDOndemand(valuesCfd);            			
                        fileReport.write(extractionInfo(files, valuesCfd, ID).getBytes("UTF-8"));
            		}
            		strLine="";
            		counter++;
            	}
            }
            buffer.clear(); // do something with the data and clear/compact it.
        }
        inChannel.close();
        aFile.close();

        
        if(counter == 0){        
        	fileReport.write(("No se encontraron registros en base de datos, para la solicitud con ID " + ID + "\n").getBytes("UTF-8"));        	
        }else{
        	findInfo = true;
        }
        
        if(fileReport!=null)
			fileReport.close();
        
        return findInfo;
	}
	*/
	public List<File> searchCFDOndemand(String [] valuesCfd) throws Exception{		
		
        List<File> files = null;
           	
        /*
    	    ondemand.serverName = 180.176.17.70
			ondemand.userName = dehtoadm
			ondemand.password = Kalidad3
			ondemand.folderNameInterEmision = Interfactura_Emision
			ondemand.folderNameInterRecepcion = Interfactura_Recepcion
			ondemand.folderNameInterEstadoCuenta = Interfactura_Estados_Cuenta
			ondemand.rutaArchivo = /planCFD/procesos/CFDOndemand/interfaces/
			ondemand.nombreArchivo = onDemand
			ondemand.extencionArchivo = .xml
			ondemand.appl_grp_e = Interfactura_Emision
			ondemand.appl_grp_r = Interfactura_Recepcion 
			
			BusquedaOnDemandImp searchXML = new BusquedaOnDemandImp("dbdev01.mx.corp", "dehtoadm", "Kalidad3",
    			"Interfactura_Emision", "Interfactura_Recepcion", "Interfactura_Estados_Cuenta",
                properties.getPathReportesProceso(), String.valueOf(cfds.get(0).getId()), ".xml");
    	  */
    	/*BusquedaOnDemandImp searchXML = new BusquedaOnDemandImp(properties.getOndemandServer(), properties.getOndemandUser(), properties.getOndemandPass(),
    			properties.getOndemandFolderEmision(), properties.getOndemandFolderRecepcion(), properties.getOndemandFolderEstadoCuenta(),
                properties.getPathReportesProceso(), "xmlTemporal", ".xml");
    	*/
        try {
        	System.out.println("Antes de buscar en ondemand");
            // Buscar xml en OnDemand
            //files = searchXML.busquedaInterfacturaEmisionCFDIReporteMasivo(valuesCfd, 500);
            System.out.println("Despues de buscar en ondemand");
        } catch (Exception e) {
            System.out.println("Error en consulta OnDemand:");
            System.out.println(Arrays.toString(e.getStackTrace()));
            throw e;
        }
    
        return files;
	}
	
	//public String extractionInfo(List<File> files, String [] valuesCfd, String ID) throws Exception{
	public String extractionInfo(String strPath, String [] valuesCfd, String ID) throws Exception{
		StringBuilder sb = new StringBuilder();
        
		//if (files != null && !files.isEmpty()) {
        if(!strPath.equals("")){    
            maxConceptos = 1;
            int counterCfd = 0;
            //for (File xmlFile : files) {
            	File xmlFile = new File(strPath);
            	//CFDIssued cfd = cfds.get(counterCfd);            	
                if (xmlFile != null) {
                    BufferedReader fr = null;
                    
                    try {
                    	                    	
                        if (xmlFile.getAbsolutePath().contains("CFDI")) {//Verifica si es CFDI
                        	fr = new BufferedReader(new FileReader(xmlFile));
                            StringBuilder s = new StringBuilder();
                            while (fr.ready()) {
                                s.append(fr.readLine());
                            }
                            System.out.println("idXML:" + valuesCfd[0] + " contenido:" + s.toString());
                            ParseXML_CFDI parser = new ParseXML_CFDI();
                            Invoice_Masivo invoice = parser.parse(xmlFile);

                            /*fr = new BufferedReader(new FileReader(xmlFile));
                            StringBuilder s = new StringBuilder();
                            while (fr.ready()) {
                                s.append(fr.readLine());
                            }*/
                            ComprobanteDocument compDoc = ComprobanteDocument.Factory.parse(s.toString().replace("xmlns=\"http://www.santander.com.mx/schemas/xsd/AddendaSantanderV1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"", ""));
                            String nombreEmisor;
                            if (compDoc.getComprobante().getEmisor() != null) {
                                nombreEmisor = compDoc.getComprobante().getEmisor().getNombre();
                            } else {
                                nombreEmisor = "NO DISPONIBLE";
                            }
                            String rfcEmisor;
                            if (compDoc.getComprobante().getEmisor() != null) {
                                rfcEmisor = compDoc.getComprobante().getEmisor().getRfc();
                            } else {
                                rfcEmisor = "NO DISPONIBLE";
                            }

                            String renglon = armarRenglon(nombreEmisor, rfcEmisor, invoice, valuesCfd);
                                                        
                            sb.append(renglon);
                            
                            
                        } else {//Es CFD
                            fr = new BufferedReader(new FileReader(xmlFile));
                            StringBuilder s = new StringBuilder();
                            while (fr.ready()) {
                                s.append(fr.readLine());
                            }
                            mx.gob.sat.cfd.x2.ComprobanteDocument compDoc = mx.gob.sat.cfd.x2.ComprobanteDocument.Factory.parse(
                                    s.toString().replace("xmlns=\"http://www.santander.com.mx/schemas/xsd/AddendaSantanderV1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"", ""));
                            Invoice_Masivo invoice = new Invoice_Masivo();
                            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                            DocumentBuilder db = dbf.newDocumentBuilder();
                            Document dom = db.parse(xmlFile);

                            Element docEle = dom.getDocumentElement();
                            invoice.setMetodoPago(docEle.getAttribute("metodoDePago"));
                            invoice.setNumCtaPago(docEle.getAttribute("NumCtaPago"));
                            NodeList nl = docEle.getElementsByTagName("InformacionEmision");
                            if (nl != null && nl.getLength() > 0) {
                                Element el = (Element) nl.item(0);
                                invoice.setCostCenter(el.getAttribute("centroCostos"));
                                invoice.setCustomerCode(el.getAttribute("codigoCliente"));
                                invoice.setContractNumber(el.getAttribute("contrato"));
                                invoice.setPeriod(el.getAttribute("periodo"));
                            }
                            mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante comprobante = compDoc.getComprobante();
                            invoice.setFormaPago(comprobante.getFormaDePago());
                            if (comprobante.getAddenda().getAddendaSantanderV1().getCampoAdicionalArray() != null) {
                                for (TCampoAdicional campo : comprobante.getAddenda()
                                        .getAddendaSantanderV1().getCampoAdicionalArray()) {
                                    if (campo.getCampo().equals("Descripcion Concepto")) {
                                        invoice.setDescriptionConcept(campo.getValor());
                                    }
                                    if (campo.getCampo().equals("Moneda")) {
                                        invoice.setMoneda(campo.getValor());
                                    }
                                    if (campo.getCampo().equals("Tipo Cambio")) {
                                        invoice.setTipoCambio(campo.getValor());
                                    }
                                }
                            }
                            invoice.setFolio(comprobante.getFolio());
                            invoice.setDate(Util.convertirFecha(comprobante.getFecha().getTime()));
                            mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante.Receptor receptor = comprobante.getReceptor();

                            invoice.setRfc(receptor.getRfc());
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
                            invoice.setSubTotal(comprobante.getSubTotal().doubleValue());
                            invoice.setTotal(comprobante.getTotal().doubleValue());
                            invoice.setIva(comprobante.getImpuestos().getTotalImpuestosTrasladados().doubleValue());
                            invoice.setYearAprobacion(String.valueOf(comprobante.getAnoAprobacion()));

                            List<ElementsInvoice> elements = new ArrayList<ElementsInvoice>();
                            for (mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante.Conceptos.Concepto objConcepto :
                                    comprobante.getConceptos().getConceptoArray()) {
                                ElementsInvoice element = new ElementsInvoice();
                                element.setAmount(objConcepto.getImporte().doubleValue());
                                element.setDescription(objConcepto.getDescripcion());
                                element.setQuantity(objConcepto.getCantidad().intValue());
                                element.setUnitMeasure(objConcepto.getUnidad());
                                element.setUnitPrice(objConcepto.getValorUnitario().doubleValue());
                                elements.add(element);
                            }
                            invoice.setElements(elements);

                            String nombreEmisor;
                            if (compDoc.getComprobante().getEmisor() != null) {
                                nombreEmisor = compDoc.getComprobante().getEmisor().getNombre();
                            } else {
                                nombreEmisor = "NO DISPONIBLE";
                            }
                            String rfcEmisor;
                            if (compDoc.getComprobante().getEmisor() != null) {
                                rfcEmisor = compDoc.getComprobante().getEmisor().getRfc();
                            } else {
                                rfcEmisor = "NO DISPONIBLE";
                            }
                            String renglon = armarRenglon(nombreEmisor, rfcEmisor, invoice, valuesCfd);
                                                        
                            sb.append(renglon);
                            
                        }
                    } catch (Exception e) {
                        System.out.println("Error parsing archivo reporte para la factura con folio " + valuesCfd[4] + " -folioInterno " + valuesCfd[5] + " -folioSAT " + valuesCfd[2]);
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                        System.out.println(Arrays.toString(e.getStackTrace()));
                        sb.append("Informacion no disponible para la factura con folio " + valuesCfd[4] + " -folioInterno " + valuesCfd[5] + " -folioSAT " + valuesCfd[2] + "\n");
                        
                    } finally {
                        if (fr != null) {
                            try {
                                fr.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (xmlFile != null && xmlFile.exists()) {
                            xmlFile.delete();
                        }
                    }
                }else{
                	sb.append("No fue encontrado en Ondemand el XML de la factura con folio " + valuesCfd[4] + " -folioInterno " + valuesCfd[5] + " -folioSAT " + valuesCfd[2] + "\n");
                }
                counterCfd++;
            //}           
            
        } else {
            
        	sb.append("No fue encontrado en Ondemand el XML de la factura con folio " + valuesCfd[4] + " -folioInterno " + valuesCfd[5] + " -folioSAT " + valuesCfd[2] + "\n");
            
        }
		return sb.toString();
	}
	
	public String searchCfdOndemand(CFDIssued cfd) throws Exception{
		StringBuilder sb = null;
		List<CFDIssued> cfds = new ArrayList<CFDIssued>();
		cfds.add(cfd);
        List<File> files = null;
        if (!cfds.isEmpty()) {
        	
            /*
        	    ondemand.serverName = 180.176.17.70
				ondemand.userName = dehtoadm
				ondemand.password = Kalidad3
				ondemand.folderNameInterEmision = Interfactura_Emision
				ondemand.folderNameInterRecepcion = Interfactura_Recepcion
				ondemand.folderNameInterEstadoCuenta = Interfactura_Estados_Cuenta
				ondemand.rutaArchivo = /planCFD/procesos/CFDOndemand/interfaces/
				ondemand.nombreArchivo = onDemand
				ondemand.extencionArchivo = .xml
				ondemand.appl_grp_e = Interfactura_Emision
				ondemand.appl_grp_r = Interfactura_Recepcion 
				
				BusquedaOnDemandImp searchXML = new BusquedaOnDemandImp("dbdev01.mx.corp", "dehtoadm", "Kalidad3",
        			"Interfactura_Emision", "Interfactura_Recepcion", "Interfactura_Estados_Cuenta",
                    properties.getPathReportesProceso(), String.valueOf(cfds.get(0).getId()), ".xml");
        	  */
        	/*BusquedaOnDemandImp searchXML = new BusquedaOnDemandImp(properties.getOndemandServer(), properties.getOndemandUser(), properties.getOndemandPass(),
        			properties.getOndemandFolderEmision(), properties.getOndemandFolderRecepcion(), properties.getOndemandFolderEstadoCuenta(),
                    properties.getPathReportesProceso(), String.valueOf(cfds.get(0).getId()), ".xml");
        	*/
            try {
            	System.out.println("Antes de buscar");
                // Buscar xml en OnDemand
                //files = searchXML.busquedaInterfacturaEmisionCFDIReporte(cfds, 50000);
                System.out.println("Despues de buscar");
            } catch (Exception e) {
                System.out.println("Error en consulta OnDemand:");
                System.out.println(Arrays.toString(e.getStackTrace()));
                throw e;
            }
        }

        if (files != null && !files.isEmpty()) {
            sb = new StringBuilder();
            maxConceptos = 1;
            for (File xmlFile : files) {
                if (xmlFile != null) {
                    BufferedReader fr = null;
                    try {
                        if (xmlFile.getAbsolutePath().contains("CFDI")) {//Verifica si es CFDI

                            ParseXML_CFDI parser = new ParseXML_CFDI();
                            Invoice_Masivo invoice = parser.parse(xmlFile);

                            fr = new BufferedReader(new FileReader(xmlFile));
                            StringBuilder s = new StringBuilder();
                            while (fr.ready()) {
                                s.append(fr.readLine());
                            }
                            ComprobanteDocument compDoc = ComprobanteDocument.Factory.parse(s.toString().replace("xmlns=\"http://www.santander.com.mx/schemas/xsd/AddendaSantanderV1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"", ""));
                            String nombreEmisor;
                            if (compDoc.getComprobante().getEmisor() != null) {
                                nombreEmisor = compDoc.getComprobante().getEmisor().getNombre();
                            } else {
                                nombreEmisor = "NO DISPONIBLE";
                            }
                            String rfcEmisor;
                            if (compDoc.getComprobante().getEmisor() != null) {
                                rfcEmisor = compDoc.getComprobante().getEmisor().getRfc();
                            } else {
                                rfcEmisor = "NO DISPONIBLE";
                            }

                            //String renglon = armarRenglon(nombreEmisor, rfcEmisor, invoice, cfd);
                            //sb.append(renglon);
                        } else {//Es CFD
                            fr = new BufferedReader(new FileReader(xmlFile));
                            StringBuilder s = new StringBuilder();
                            while (fr.ready()) {
                                s.append(fr.readLine());
                            }
                            mx.gob.sat.cfd.x2.ComprobanteDocument compDoc = mx.gob.sat.cfd.x2.ComprobanteDocument.Factory.parse(
                                    s.toString().replace("xmlns=\"http://www.santander.com.mx/schemas/xsd/AddendaSantanderV1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"", ""));
                            Invoice_Masivo invoice = new Invoice_Masivo();
                            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                            DocumentBuilder db = dbf.newDocumentBuilder();
                            Document dom = db.parse(xmlFile);

                            Element docEle = dom.getDocumentElement();
                            invoice.setMetodoPago(docEle.getAttribute("metodoDePago"));
                            invoice.setNumCtaPago(docEle.getAttribute("NumCtaPago"));
                            NodeList nl = docEle.getElementsByTagName("InformacionEmision");
                            if (nl != null && nl.getLength() > 0) {
                                Element el = (Element) nl.item(0);
                                invoice.setCostCenter(el.getAttribute("centroCostos"));
                                invoice.setCustomerCode(el.getAttribute("codigoCliente"));
                                invoice.setContractNumber(el.getAttribute("contrato"));
                                invoice.setPeriod(el.getAttribute("periodo"));
                            }
                            mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante comprobante = compDoc.getComprobante();
                            invoice.setFormaPago(comprobante.getFormaDePago());
                            if (comprobante.getAddenda().getAddendaSantanderV1().getCampoAdicionalArray() != null) {
                                for (TCampoAdicional campo : comprobante.getAddenda()
                                        .getAddendaSantanderV1().getCampoAdicionalArray()) {
                                    if (campo.getCampo().equals("Descripcion Concepto")) {
                                        invoice.setDescriptionConcept(campo.getValor());
                                    }
                                    if (campo.getCampo().equals("Moneda")) {
                                        invoice.setMoneda(campo.getValor());
                                    }
                                    if (campo.getCampo().equals("Tipo Cambio")) {
                                        invoice.setTipoCambio(campo.getValor());
                                    }
                                }
                            }
                            invoice.setFolio(comprobante.getFolio());
                            invoice.setDate(Util.convertirFecha(comprobante.getFecha().getTime()));
                            mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante.Receptor receptor = comprobante.getReceptor();

                            invoice.setRfc(receptor.getRfc());
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
                            invoice.setSubTotal(comprobante.getSubTotal().doubleValue());
                            invoice.setTotal(comprobante.getTotal().doubleValue());
                            invoice.setIva(comprobante.getImpuestos().getTotalImpuestosTrasladados().doubleValue());
                            invoice.setYearAprobacion(String.valueOf(comprobante.getAnoAprobacion()));

                            List<ElementsInvoice> elements = new ArrayList<ElementsInvoice>();
                            for (mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante.Conceptos.Concepto objConcepto :
                                    comprobante.getConceptos().getConceptoArray()) {
                                ElementsInvoice element = new ElementsInvoice();
                                element.setAmount(objConcepto.getImporte().doubleValue());
                                element.setDescription(objConcepto.getDescripcion());
                                element.setQuantity(objConcepto.getCantidad().intValue());
                                element.setUnitMeasure(objConcepto.getUnidad());
                                element.setUnitPrice(objConcepto.getValorUnitario().doubleValue());
                                elements.add(element);
                            }
                            invoice.setElements(elements);

                            String nombreEmisor;
                            if (compDoc.getComprobante().getEmisor() != null) {
                                nombreEmisor = compDoc.getComprobante().getEmisor().getNombre();
                            } else {
                                nombreEmisor = "NO DISPONIBLE";
                            }
                            String rfcEmisor;
                            if (compDoc.getComprobante().getEmisor() != null) {
                                rfcEmisor = compDoc.getComprobante().getEmisor().getRfc();
                            } else {
                                rfcEmisor = "NO DISPONIBLE";
                            }
                            //String renglon = armarRenglon(nombreEmisor, rfcEmisor, invoice, cfd);
                            //sb.append(renglon);
                        }
                    } catch (Exception e) {
                        System.out.println("Error parsing archivo reporte.");
                        System.out.println(Arrays.toString(e.getStackTrace()));
                        sb.append("INFORMACION NO DISPONIBLE PARA LA FACTURA").append("\n");
                        
                    } finally {
                        if (fr != null) {
                            try {
                                fr.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (xmlFile != null && xmlFile.exists()) {
                            xmlFile.delete();
                        }
                    }
                }
            }
            
            return sb.toString();
        } else {
            
            return "INFORMACION NO DISPONIBLE PARA LA FACTURA\n";
            
        }

		
	}
	
	private String armarRenglon(String nombreEmisor, String rfcEmisor, Invoice_Masivo invoice, String [] valuesCfd) throws Exception{
        StringBuilder sb = new StringBuilder();
                
        sb.append(translateCodes(valuesCfd[4].trim())).append("<<delimiter>>");//folio del cfd
        sb.append(valuesCfd[5].trim()).append("<<delimiter>>");//folioInterno del cfd
        sb.append(translateCodes(valuesCfd[2].trim())).append("<<delimiter>>");//folioSat del cfd
        //formattype del cfd
        if(Integer.parseInt(valuesCfd[6].trim()) == 1){
        	sb.append("FORMATO UNICO").append("<<delimiter>>");	
        }else if(Integer.parseInt(valuesCfd[6].trim()) == 2){
        	sb.append("FACTORAJE").append("<<delimiter>>");
        }
        
        sb.append(valuesCfd[7].trim()).append("<<delimiter>>");//status del cfd
        sb.append(translateCodes(rfcEmisor)).append("<<delimiter>>");
        sb.append(valuesCfd[8].trim()).append("<<delimiter>>");
        sb.append(valuesCfd[9].trim()).append("<<delimiter>>");
        sb.append(invoice.getTotal()).append("<<delimiter>>");       
        sb.append(translateCodes(nombreEmisor)).append("<<delimiter>>");
        //Incluir serie leido directamente del XML
        sb.append(translateCodes(invoice.getSerie())).append("<<delimiter>>");
        //Incluir tipoDeComprobante leido directamente del XML
        sb.append(translateCodes(invoice.getTipoDeComprobante())).append("<<delimiter>>");
        sb.append(translateCodes(invoice.getMoneda())).append("<<delimiter>>");
        sb.append(translateCodes(invoice.getTipoCambio())).append("<<delimiter>>");
        
        sb.append(translateCodes(invoice.getRfc())).append("<<delimiter>>");
        //Incluir el IDExtranjero
        System.out.println("clienteRFC: " + invoice.getRfc() + "FiscalEntity ID:" + valuesCfd[10].trim());
        //Customer customer = customerManager.get(invoice.getRfc(), valuesCfd[10].trim());
        /*if(customer!=null){
        	if(customer.getIdExtranjero()!=null){
        		sb.append(customer.getIdExtranjero()).append("<<delimiter>>");
        	}else{
        		sb.append("").append("<<delimiter>>");
        	}        		
        }else{
        	sb.append("").append("<<delimiter>>");
        }*/
        sb.append("").append("<<delimiter>>");
        
        sb.append(translateCodes(invoice.getName())).append("<<delimiter>>");
        sb.append(translateCodes(invoice.getMetodoPago())).append("<<delimiter>>");
        //Incluir el Regimen Fiscal
        sb.append(translateCodes(invoice.getRegimenFiscal())).append("<<delimiter>>");
        sb.append(translateCodes(invoice.getLugarExpedicion())).append("<<delimiter>>");
        sb.append(translateCodes(invoice.getFormaPago())).append("<<delimiter>>");
        sb.append(translateCodes(invoice.getNumCtaPago())).append("<<delimiter>>");
        sb.append(translateCodes(invoice.getCalle())).append("<<delimiter>>");
        sb.append(translateCodes(invoice.getInterior())).append("<<delimiter>>");
        sb.append(translateCodes(invoice.getExterior())).append("<<delimiter>>");        
        sb.append(translateCodes(invoice.getColonia())).append("<<delimiter>>");
        sb.append(translateCodes(invoice.getLocalidad())).append("<<delimiter>>");
        sb.append(translateCodes(invoice.getReferencia())).append("<<delimiter>>");        
        sb.append(translateCodes(invoice.getMunicipio())).append("<<delimiter>>");
        sb.append(translateCodes(invoice.getEstado())).append("<<delimiter>>");
        sb.append(translateCodes(invoice.getPais())).append("<<delimiter>>");
        sb.append(translateCodes(invoice.getCodigoPostal())).append("<<delimiter>>");
        sb.append(translateCodes(invoice.getCustomerCode())).append("<<delimiter>>");
        sb.append(translateCodes(invoice.getContractNumber())).append("<<delimiter>>");
        sb.append(translateCodes(invoice.getPeriod())).append("<<delimiter>>");
        sb.append(translateCodes(invoice.getCostCenter())).append("<<delimiter>>");
        sb.append(translateCodes(invoice.getDescriptionConcept())).append("<<delimiter>>");
        
        sb.append(invoice.getSubTotal()).append("<<delimiter>>");
        sb.append(invoice.getIva()).append("<<delimiter>>");
        
        //tipoAddenda 1, 2, 3 o 0
        sb.append(translateCodes(invoice.getTipoAddenda())).append("<<delimiter>>");
        //emailProveedor   as:InformacionPago  email
        sb.append(translateCodes(invoice.getEmail())).append("<<delimiter>>");
        //codigoISOMoneda  asant:InformacionPago  codigoISOMoneda tipo 1, 2 o 3
        sb.append(translateCodes(invoice.getCodigoISO())).append("<<delimiter>>");
        //OrdenCompra      as:InformacionPago  ordenCompra tipo 0
        sb.append(translateCodes(invoice.getPurchaseOrder())).append("<<delimiter>>");
        //PosicionCompra   asant:InformacionPago  posCompra tipo 1
        sb.append(translateCodes(invoice.getPosicioncompraLog())).append("<<delimiter>>");
        //CuentaContable   asant:InformacionPago  cuentaContable tipo 2
        sb.append(translateCodes(invoice.getCuentacontableFin())).append("<<delimiter>>");
        //CentroCostos     asant:InformacionEmision  centroCostos tipo 2
        sb.append(translateCodes(invoice.getCostCenter())).append("<<delimiter>>");
        //NumeroContrato   asant:Inmuebles numContrato tipo 3
        sb.append(translateCodes(invoice.getNumerocontratoArr())).append("<<delimiter>>");
        //FechaVencimiento asant:Inmuebles fechaVencimiento tipo3
        sb.append(translateCodes(invoice.getFechavencimientoArr())).append("<<delimiter>>");
        //NombreBeneficiario   as:InformacionPago nombreBeneficiario tipo 0
        sb.append(translateCodes(invoice.getBeneficiaryName())).append("<<delimiter>>");
        //InstitucionReceptora  as:InformacionPago  institucionReceptora tipo 0
        sb.append(translateCodes(invoice.getReceivingInstitution())).append("<<delimiter>>");
        //NumeroCuenta  as:InformacionPago  numeroCuenta tipo 0
        sb.append(translateCodes(invoice.getAccountNumber())).append("<<delimiter>>");
        //NumeroProveedor  as:InformacionPago  numProveedor tipo 0
        sb.append(translateCodes(invoice.getProviderNumber())).append("<<delimiter>>");
        
        if (invoice.getElements() != null && !invoice.getElements().isEmpty()) {
            if (invoice.getElements().size() > maxConceptos) {
                maxConceptos = invoice.getElements().size();
            }
            for (ElementsInvoice concepto : invoice.getElements()) {
                sb.append(concepto.getQuantity()).append("<<delimiter>>");
                sb.append(concepto.getUnitMeasure()).append("<<delimiter>>");
                sb.append(translateCodes(concepto.getDescription())).append("<<delimiter>>");
                sb.append(concepto.getUnitPrice()).append("<<delimiter>>");
                sb.append(concepto.getAmount()).append("<<delimiter>>");
            }
        }
        
        sb.append("\n");
        return sb.toString();
    }
	
	public String translateCodes(String cadena) throws Exception{
		org.apache.commons.lang3.text.translate.UnicodeEscaper escaper = org.apache.commons.lang3.text.translate.UnicodeEscaper.above(127);
		
		String strEscaped = "";
		if(cadena != null && !cadena.equals("")){
			strEscaped = escaper.translate(cadena);
		}
		
		return strEscaped;
		
		
	}
	
}
