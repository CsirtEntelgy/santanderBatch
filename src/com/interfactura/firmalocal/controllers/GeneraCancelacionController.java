package com.interfactura.firmalocal.controllers;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.interfactura.firmalocal.cifras.WebServiceCifrasCliente;
import com.interfactura.firmalocal.domain.entities.Facturas;
import com.interfactura.firmalocal.cancelacion.WebServiceCancelacion;

@Controller
public class GeneraCancelacionController {

	@Autowired
    private WebServiceCancelacion ws;
	
	public void leerEntradaCancelacion(String path, String oDate){
				
		try{
									
			System.out.println("pathEntrada: " + path);
			
			//Crear archivo de SALIDA
			File fileSalidaCancelacion = new File(path + "SALIDACANCELECB" + oDate + ".TXT");
			FileOutputStream salidaCancelacion = new FileOutputStream(fileSalidaCancelacion);
						
			//Crear archivo de DETALLE
			File fileDetalleCancelacion = new File(path + "DETALLECANCELECB" + oDate + ".TXT");
			FileOutputStream detalleCancelacion = new FileOutputStream(fileDetalleCancelacion);
			
			//Crear archivo de FOLIOS
			File fileFoliosCancelacion = new File(path + "FOLIOSCANCELECB" + oDate + ".TXT");
			FileOutputStream foliosCancelacion = new FileOutputStream(fileFoliosCancelacion);
			
			boolean fNumCuenta, fNomApp, fPeriodo, fRfc, fFolioSat, fFechaTimb; 
			
			//Crear buffer de lectura para el archivo de ENTRADA
			FileInputStream fStream = new FileInputStream(path + "ENTRADACANCELECB" + oDate + ".TXT");			
			DataInputStream dInput = new DataInputStream(fStream);
			BufferedReader bReader = new BufferedReader(new InputStreamReader(dInput));
			
			String line = null;
			while((line = bReader.readLine()) != null){
				String [] valores = line.split("\\|");
				if(valores.length == 6){
					fNumCuenta = false; fNomApp = false; fPeriodo = false; fRfc = false; 
					fFolioSat = false; fFechaTimb = false;
					
					if(!valores[0].trim().equals("")) fNumCuenta = true;
					if(!valores[1].trim().equals("")) fNomApp = true;
					if(!valores[2].trim().equals("")) fPeriodo = true;
					if(!valores[3].trim().equals("")) fRfc = true;
					if(!valores[4].trim().equals("")) fFolioSat = true;
					if(!valores[5].trim().equals("")) fFechaTimb = true;
					
					if(!fNumCuenta || !fNomApp || !fPeriodo || !fRfc || !fFolioSat || !fFechaTimb){
						String strRes = " los siguientes campos son requeridos: ";
						
						if(!fNumCuenta) strRes += "-NumeroDeCuenta";
						if(!fNomApp) strRes += "-NombreDeAplicativo";
						if(!fPeriodo) strRes += "-Periodo";
						if(!fRfc) strRes += "-RFCEmisor";
						if(!fFolioSat) strRes += "-FolioSAT";
						if(!fFechaTimb) strRes += "-FechaTimbrado";
						
						strRes += ".";
						
						detalleCancelacion.write((valores[0].trim() + "|" + valores[1].trim() + "|" + valores[2].trim()
								+ "|" + valores[3].trim() + "|" + valores[4].trim() + "|" + valores[5].trim() 
								+ "|INCIDENTE: " + strRes + "|" 
								+ "" + "|").getBytes("UTF-8"));
						
						detalleCancelacion.write(("\r\n").getBytes("UTF-8"));    
						
					}else{
						String fechaCancelacion = generaFechaCancelacion(); 
						
						System.out.println("rfc: " + valores[3].trim() + " folioSAT: " + valores[4].trim() + 
								" fechaTimbrado: " + valores[5].trim());
						String strXmlCancelar = formarXmlCancelar(valores[3].trim(), valores[4].trim(), valores[5].trim(), fechaCancelacion);
						
						System.out.println("strXmlCancelar:" + strXmlCancelar);
						
						String salidaCancelaTimbre = ws.cancelaTimbre(strXmlCancelar); 
																
						System.out.println("salidaCancelaTimbre: " + salidaCancelaTimbre);
						
						Document doc = stringToDocument(salidaCancelaTimbre);

	                    Element element = doc.getDocumentElement();

	                    String descripcion = element.getAttribute("Descripcion");
	                    String idRespuesta = element.getAttribute("IdRespuesta");

	                    
						//if ((descripcion.toLowerCase().trim().equals("ok") && idRespuesta.equals("1"))  || idRespuesta.equals("601")) {
	                    if (descripcion.toLowerCase().trim().equals("ok") && idRespuesta.equals("1")) {
	                    	//Cancelacion exitosa
							
							//Obtiene montos del ECB cancelado
							
							NodeList nl = element.getElementsByTagName("Montos");
							String comisiones = "0.00", ivas = "0.00", retenciones = "0.00";
							String statusMontos = "";
							
							if(nl != null && nl.getLength() > 0){
								Element el = (Element) nl.item(0);
								statusMontos = el.getAttribute("status");
								
								if(statusMontos.toLowerCase().trim().equals("ok")){
									comisiones = el.getAttribute("comisiones");
									ivas = el.getAttribute("ivas");
									retenciones = el.getAttribute("retenciones");
									
									System.out.println("comisiones:" + comisiones + " ivas:" + ivas + " rentenciones:" + retenciones);
								}else{
									 
									System.out.println("status:" + statusMontos + " descripcion:" + el.getAttribute("descripcion"));
								}
							}
							
							//Obtiene la fecha de Cancelacion, la cual viene en el Acuse
							
							/*
							NodeList nlAcuse = element.getElementsByTagName("Acuse");
							String fechaCancelacionAcuse = "";
													
							if(nlAcuse != null && nlAcuse.getLength() > 0){
								Element elAcuse = (Element) nlAcuse.item(0);
								fechaCancelacionAcuse = elAcuse.getAttribute("Fecha");
														
							}*/
													
							//Escribe en el archivo SALIDA los valores
							//Numero de cuenta, nombre aplicativo, periodo (AAAA-MM), Fecha de Cancelación, FolioSAT, Comisiones, Ivas, Retenciones
							
							/*Número de Cuenta
							Nombre Aplicativo
							Período AAAA-MM
							Fecha de Cancelación
							FOLIO SAT
							Comisiones/Intereses
							Ivas
							Retenciones*/
		
							//Agregar registro de Cancelacion OK, al archivo de SALIDA
							salidaCancelacion.write((valores[0].trim() + "|" + valores[1].trim() + "|" + valores[2].trim()
									+ "|" + fechaCancelacion + "|" + valores[4].trim() + "|" + comisiones + "|" + ivas
									+ "|" + retenciones + "|").getBytes("UTF-8"));
							
							salidaCancelacion.write(("\r\n").getBytes("UTF-8"));
							
							//Agregar registro de Cancelacion OK, al archivo de DETALLE
							/*detalleCancelacion.write((valores[0].trim() + "|" + valores[1].trim() + "|" + valores[2].trim()
									+ "|" + valores[3].trim() + "|" + valores[4].trim() + "|" + valores[5].trim() + "|OK|" 
									+ fechaCancelacionAcuse + "|").getBytes("UTF-8"));
							
							detalleCancelacion.write(("\r\n").getBytes("UTF-8"));
								*/				
							//Agregar lo folios cancelados correctamente, al archivo FOLIOS
							foliosCancelacion.write((valores[4].trim() + "|" + fechaCancelacion + "|fs").getBytes("UTF-8"));
							foliosCancelacion.write(("\r\n").getBytes("UTF-8"));
							
	                    } else if(idRespuesta.equals("202") || idRespuesta.equals("205")) {
	                    	/*Incidentes*/
	                    	//202 UUID Cancelado anteriormente
	                    	//205 UUID no existente
	                    	                    	
	                    	//Incidente al intentar Cancelacion
	                    	//Agregar registro de Cancelacion ERROR, al archivo de DETALLE
	                    	detalleCancelacion.write((valores[0].trim() + "|" + valores[1].trim() + "|" + valores[2].trim()
									+ "|" + valores[3].trim() + "|" + valores[4].trim() + "|" + valores[5].trim() 
									+ "|INCIDENTE: " + idRespuesta + "-" + descripcion + "|" 
									+ "" + "|").getBytes("UTF-8"));
							
							detalleCancelacion.write(("\r\n").getBytes("UTF-8"));                   	
	                    
	                    } else{
	                    	/*Errores IF*/
	                    	//601 Servicio no disponible
	                    	//602 Certificado de autenticación de servicio es incorrecto
	                    	//603 El XMLS proporcionado no contempla es Esquema Adecuado
	                    	//501 Timbrado Agotado
	                    	
	                    	//Error al intentar Cancelacion
	                    	//Agregar registro de Cancelacion ERROR, al archivo de DETALLE
	                    	detalleCancelacion.write((valores[0].trim() + "|" + valores[1].trim() + "|" + valores[2].trim()
									+ "|" + valores[3].trim() + "|" + valores[4].trim() + "|" + valores[5].trim() 
									+ "|ERROR: " + idRespuesta + "-" + descripcion + "|" 
									+ "" + "|").getBytes("UTF-8"));
							
							detalleCancelacion.write(("\r\n").getBytes("UTF-8"));
							
	                    }
					}
					
					
				}else{
					//Error en el número de elementos 
					detalleCancelacion.write((line + " |INCIDENTE: Renlgón incorrecto!").getBytes("UTF-8"));
					
					detalleCancelacion.write(("\r\n").getBytes("UTF-8"));
				}
			}
			
			if(bReader != null)
				bReader.close();
			
			if(dInput != null)
				dInput.close();
			
			if(fStream != null)
				fStream.close();
			
			if(salidaCancelacion != null)
				salidaCancelacion.close();
			
			if(detalleCancelacion != null)
				detalleCancelacion.close();
			
			if(foliosCancelacion != null)
				foliosCancelacion.close();
			
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Exception GeneraCifrasWeb:" + e.getMessage());
		}
	}
	
	public String formarXmlCancelar(String rfcEmisor, String folioSat, String fechaTimbrado, String fechaCancelacion) throws Exception{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
   
        builder = factory.newDocumentBuilder();

        DOMImplementation impl = builder.getDOMImplementation();

        Document docC = impl.createDocument(null, null, null);

        Element el = docC.createElement("CancelaCFD");

        docC.appendChild(el);
       
        Element el2 = docC.createElement("Cancelacion");
        el2.setAttribute("Fecha", fechaCancelacion);
        el2.setAttribute("RfcEmisor", rfcEmisor);

        el.appendChild(el2);

        Element el3 = docC.createElement("Folios");

        el2.appendChild(el3);

        Element el4 = docC.createElement("Folio");
        el4.setAttribute("FechaTimbrado", fechaTimbrado);
        el4.setAttribute("UUID", folioSat);

        el3.appendChild(el4);

        return DocumentToString(docC);

       
	}
	
	public String generaFechaCancelacion(){
		String fechaCancelacion;		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
        fechaCancelacion = sdf.format(new Date());
        
        StringBuilder sb = new StringBuilder(fechaCancelacion);
        
        fechaCancelacion = sb.insert(10, "T").toString();
        fechaCancelacion = fechaCancelacion.replaceAll(" ", "");
        
        return fechaCancelacion;
	}
	
	public Document stringToDocument(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            return builder.parse(is);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;

    }
	
	public String DocumentToString(Document doc) {
        DOMSource domSource = new DOMSource(doc);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = tf.newTransformer();
            StringWriter sw = new StringWriter();
            StreamResult sr = new StreamResult(sw);
            transformer.transform(domSource, sr);
            return sw.toString();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return "";
    }
}
