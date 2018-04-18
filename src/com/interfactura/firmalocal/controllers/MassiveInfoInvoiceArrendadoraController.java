package com.interfactura.firmalocal.controllers;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

import com.interfactura.firmalocal.datamodel.ElementsInvoice;
import com.interfactura.firmalocal.datamodel.Invoice_Masivo;
import com.interfactura.firmalocal.domain.entities.AreaResponsable;
import com.interfactura.firmalocal.domain.entities.User;
import com.interfactura.firmalocal.persistence.AreaResponsableManager;
import com.interfactura.firmalocal.persistence.UserManager;
import com.interfactura.firmalocal.xml.Properties;
import com.interfactura.firmalocal.xml.util.ParseXML_CFDI;
import com.interfactura.firmalocal.xml.util.Util;

@Controller
public class MassiveInfoInvoiceArrendadoraController {

	@Autowired
	Properties properties;
	
	String PathArrendadoraEntrada	=	"/salidas/masivo/arrendadora/facturacion/entrada/";
	String PathArrendadoraProceso	=	"/salidas/masivo/arrendadora/facturacion/proceso/";
	String PathArrendadoraSalida	=	"/salidas/masivo/arrendadora/facturacion/salida/";
	String PathArrendadoraOndemand	=	"/salidas/masivo/arrendadora/facturacion/ondemand/";
	

	private int maxConceptos;
	
	@Autowired(required = true)
	private UserManager userManager;
	
	@Autowired(required = true)
	private AreaResponsableManager areaManager;
	
	public void readIdFileProcess(String file, String time){
		try{
			String fileName = file + time;
			//FileInputStream fsExcelsToProcess = new FileInputStream(properties.getPathFacturacionEntrada() + "IDFILEPROCESS_" + nProceso + ".TXT");
			//FileInputStream fsExcelsToProcess = new FileInputStream(PathArrendadoraEntrada + "IDFILEPROCESS.TXT");
			//DataInputStream in = new DataInputStream(fsExcelsToProcess);
			//BufferedReader br = new BufferedReader(new InputStreamReader(in));
			//String strLine;
			//int counter = 0;
			//FileOutputStream fileStatus = new FileOutputStream(properties.getPathFacturacionProceso() + "STATUS_DECOMPRESS_" + nProceso + ".TXT");
			FileOutputStream fileStatus = new FileOutputStream(PathArrendadoraProceso + "massiveInfoInvoiceArrendadora.txt");
			fileStatus.write(("Status del proceso bash massiveInfoInvoiceArrendadora.sh" + "\n").getBytes("UTF-8"));
			
			File fileProcesoTxt = new File(PathArrendadoraProceso + fileName + "/" + fileName + ".TXT" );
			if(fileProcesoTxt.exists()){
				File fileBD = new File(PathArrendadoraProceso + fileName + "/" + "BD" + fileName + ".TXT");
				if(fileBD.exists()){
					readBD(fileBD,fileName);
					fileStatus.write(("Extraccion de informacion exitosa, del archivo BD" + fileName + ".TXT" + "\n").getBytes("UTF-8"));
				}else{
				
					//No existe el archivo BD para la solicitud de FM
					System.out.println("No existe archivo BD para la solicitud de FM: " + fileName);
				
					fileStatus.write(("El archivo BD" + fileName + ".TXT no ha sido leido en la ruta " + PathArrendadoraProceso + fileName + "/" + "\n").getBytes("UTF-8"));
				}
			} else
				fileStatus.write(("El archivo " + fileName + ".TXT no ha sido leido en la ruta " + PathArrendadoraProceso + fileName + "/" + "\n").getBytes("UTF-8"));
			//while((strLine = br.readLine()) != null){
				//if(!strLine.trim().equals("")){
					//String [] arrayValues = strLine.trim().split("\\|");
										
				//}			
				//counter++;
			//}			
			//br.close();
			//in.close();
			//fsExcelsToProcess.close();
			//if(counter == 0){
				//fileStatus.write(("No se encontraron solicitudes a procesar\n").getBytes("UTF-8"));	
			//}
			fileStatus.close();
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Exception massiveInfoInvoice:" + e.getMessage());
			try {
				//FileOutputStream fileError = new FileOutputStream(properties.getPathReportesProceso() + "ERROR_DECOMPRESS_" + nProceso + ".TXT");
				FileOutputStream fileError = new FileOutputStream(PathArrendadoraProceso + "massiveInfoInvoiceError.txt");
				fileError.write((e.getMessage()).getBytes("UTF-8"));
				fileError.close();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				//System.out.println("Exception al crear ERROR_DECOMPRESS_" + nProceso + ".TXT:" + e.getMessage());
				System.out.println("Exception al crear massiveInfoInvoiceError.txt:" + e.getMessage());
			}			
		}
	}
	
	public void readBD(File fileBD, String fileName) throws Exception{
		
		FileOutputStream salidaInfoFacturas = new FileOutputStream(new File(PathArrendadoraProceso + fileName + "/FACTURASDIARIO" + fileName + ".TXT"));
		
		
		FileInputStream fs = new FileInputStream(fileBD);
		DataInputStream in = new DataInputStream(fs);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine = "";
		int counter = 0;
		while((strLine = br.readLine()) != null){
			if(!strLine.trim().equals("")){
				String [] arrayValuesBd = strLine.trim().split("<#EMasfUD,>");		
											
				salidaInfoFacturas.write(extractionInfo(PathArrendadoraOndemand + fileName + "/" + "fac-" + arrayValuesBd[20] + ".xml", arrayValuesBd, fileName, counter).getBytes("UTF-8"));
				
				counter++;
			}			
			
		}
		br.close();
		in.close();
		fs.close();
		
		salidaInfoFacturas.close();
		
	}
	
	/////////////////////////////Extraction Info from XML//////////////////////////////
	
	public String extractionInfo(String strPath, String [] valuesCfd, String fileName, int indexInvoice) throws Exception{
		StringBuilder sb = new StringBuilder();
        
		
        if(!strPath.equals("")){    
            maxConceptos = 1;
            int counterCfd = 0;
            
            	File xmlFile = new File(strPath);
            	            	
                if (xmlFile.exists()) {
                    BufferedReader fr = null;
                    
                    try {                  	                    	
                        
                        	fr = new BufferedReader(new FileReader(xmlFile));
                            StringBuilder s = new StringBuilder();
                            while (fr.ready()) {
                                s.append(fr.readLine());
                            }
                            System.out.println("idXML:" + valuesCfd[0] + " contenido:" + s.toString());
                            ParseXML_CFDI parser = new ParseXML_CFDI();
                            Invoice_Masivo invoice = parser.parseQuitas(xmlFile);
                            
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

                            String renglon = armarRenglon(nombreEmisor, rfcEmisor, invoice, valuesCfd, fileName);
                                                        
                            //String renglonConceptos = armarRenglonConceptos(invoice, indexInvoice);
                            
                            
                            sb.append(renglon);
             
                        
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
                    }
                }else{
                	sb.append("No fue encontrado en Ondemand el XML de la factura con folio " + valuesCfd[4] + " -folioInterno " + valuesCfd[5] + " -folioSAT " + valuesCfd[2] + "\n");
                }
                counterCfd++;
                       
            
        } else {
            
        	sb.append("No fue encontrado en Ondemand el XML de la factura con folio " + valuesCfd[4] + " -folioInterno " + valuesCfd[5] + " -folioSAT " + valuesCfd[2] + "\n");
            
        }
		return sb.toString();
	}
	
	private String armarRenglon(String nombreEmisor, String rfcEmisor, Invoice_Masivo invoice, String [] valuesCfd, String fileName) throws Exception{
        StringBuilder sb = new StringBuilder();
        sb.append("c").append(">");  
        sb.append(valuesCfd[5]).append(">");											//sourcefilename
        sb.append(valuesCfd[19]).append(">");											//folio interno
        sb.append(valuesCfd[20]).append(">");											//folioSAT
        sb.append(valuesCfd[7]).append(">");											//formattype
        sb.append(valuesCfd[6]).append(">");											//status
        sb.append(valuesCfd[3]).append(">");											//fechaEmision
        sb.append(valuesCfd[4]).append(">");											//xmlRoute
        sb.append(valuesCfd[17]).append(">");											//fiscalEntity_Id
        
        sb.append(translateCodes(rfcEmisor)).append(">");								//rfc emisor
        sb.append(translateCodes(nombreEmisor)).append(">");							//entidad emisor      
        sb.append(translateCodes(invoice.getSerie())).append(">");						//serie      
        sb.append(translateCodes(invoice.getTipoDeComprobante())).append(">");			//tipoDeComprobante
        sb.append(translateCodes(invoice.getMoneda())).append(">");						//moneda
        sb.append(translateCodes(invoice.getTipoCambio())).append(">");					//tipo de cambio
        sb.append(translateCodes(invoice.getRfc())).append(">");						//RFC de Cliente
        sb.append(translateCodes(invoice.getName())).append(">");						//Nombre de Cliente
        sb.append(translateCodes(invoice.getMetodoPago())).append(">");					//Metodo de Pago
        sb.append(translateCodes(invoice.getRegimenFiscal())).append(">");				//Regimen Fiscal
        sb.append(translateCodes(invoice.getLugarExpedicion())).append(">");			//Lugar de Expedicion
        sb.append(translateCodes(invoice.getFormaPago())).append(">");					//Forma de Pago
        sb.append(translateCodes(invoice.getNumCtaPago())).append(">");					//NumCtaPago
        sb.append(translateCodes(invoice.getCalle())).append(">");						//Calle
        sb.append(translateCodes(invoice.getInterior())).append(">");					//Num Interior
        sb.append(translateCodes(invoice.getExterior())).append(">");					//Num Exterior
        sb.append(translateCodes(invoice.getColonia())).append(">");					//Colonia
        sb.append(translateCodes(invoice.getLocalidad())).append(">");					//Localidad
        sb.append(translateCodes(invoice.getReferencia())).append(">");					//Referencia
        sb.append(translateCodes(invoice.getMunicipio())).append(">");					//Municipio
        sb.append(translateCodes(invoice.getEstado())).append(">");						//Estado
        sb.append(translateCodes(invoice.getPais())).append(">");						//Pais
        sb.append(translateCodes(invoice.getCodigoPostal())).append(">");				//CP
        sb.append(translateCodes(invoice.getCustomerCode())).append(">");				//Codigo de Cliente
        sb.append(translateCodes(invoice.getContractNumber())).append(">");				//Numero de contrato
        sb.append(translateCodes(invoice.getPeriod())).append(">");						//Periodo
        //sb.append(translateCodes(invoice.getCostCenter())).append(">");					//Centro de Costos
        sb.append(translateCodes(invoice.getDescriptionConcept())).append(">");			//Descripcion Concepto (encabezado)
        sb.append(invoice.getPorcentaje()).append(">");									//Tasa IVA 
        sb.append(invoice.getSubTotal()).append(">");									//Subtotal
        sb.append(invoice.getIva()).append(">");        								//Iva
        sb.append(invoice.getTotal()).append(">");       								//Total
               
        sb.append("FACTURASDIARIO" + fileName + ".TXT").append(">");       				//nombreDeArchivo
        
        //tipoAddenda 1, 2, 3 o 0
        sb.append(translateCodes(invoice.getTipoAddenda())).append(">");
        //emailProveedor   as:InformacionPago  email
        sb.append(translateCodes(invoice.getEmail())).append(">");
        //codigoISOMoneda  asant:InformacionPago  codigoISOMoneda tipo 1, 2 o 3
        sb.append(translateCodes(invoice.getCodigoISO())).append(">");
        //OrdenCompra      as:InformacionPago  ordenCompra tipo 0
        sb.append(translateCodes(invoice.getPurchaseOrder())).append(">");
        //PosicionCompra   asant:InformacionPago  posCompra tipo 1
        sb.append(translateCodes(invoice.getPosicioncompraLog())).append(">");
        //CuentaContable   asant:InformacionPago  cuentaContable tipo 2
        sb.append(translateCodes(invoice.getCuentacontableFin())).append(">");
        //CentroCostos     asant:InformacionEmision  centroCostos tipo 2
        sb.append(translateCodes(invoice.getCostCenter())).append(">");
        //NumeroContrato   asant:Inmuebles numContrato tipo 3
        sb.append(translateCodes(invoice.getNumerocontratoArr())).append(">");
        //FechaVencimiento asant:Inmuebles fechaVencimiento tipo3
        sb.append(translateCodes(invoice.getFechavencimientoArr())).append(">");
        //NombreBeneficiario   as:InformacionPago nombreBeneficiario tipo 0
        sb.append(translateCodes(invoice.getBeneficiaryName())).append(">");
        //InstitucionReceptora  as:InformacionPago  institucionReceptora tipo 0
        sb.append(translateCodes(invoice.getReceivingInstitution())).append(">");
        //NumeroCuenta  as:InformacionPago  numeroCuenta tipo 0
        sb.append(translateCodes(invoice.getAccountNumber())).append(">");
        //NumeroProveedor  as:InformacionPago  numProveedor tipo 0
        sb.append(translateCodes(invoice.getProviderNumber())).append(">");
        
        sb.append(valuesCfd[21]).append(">");											//Motivo de Descuento
        sb.append(valuesCfd[22]).append(">");											//Descuento        
        
        sb.append(valuesCfd[24]).append(">");											//IdUsuario
        String nombreUsuario = "";
        
        if (valuesCfd[24] != null && !valuesCfd[24].equalsIgnoreCase("")) {
        	User user = userManager.get(Long.parseLong(valuesCfd[24]));
            if(user != null){
            	nombreUsuario = user.getUserName();
            }
            sb.append(nombreUsuario).append(">");											//NombreUsuario
        } else
        	sb.append("masivo").append(">");											//NombreUsuario
        
        
        sb.append(valuesCfd[25]).append(">");											//IdArea               
        String nombreArea = "";
        
        AreaResponsable area = areaManager.get(Long.parseLong(valuesCfd[25]));
        if(area != null){
        	nombreArea = area.getNombre();
        }
        sb.append(nombreArea);															//NombreAreaResponsable
        
        /*if (invoice.getElements() != null && !invoice.getElements().isEmpty()) {
            if (invoice.getElements().size() > maxConceptos) {
                maxConceptos = invoice.getElements().size();
            }
            for (ElementsInvoice concepto : invoice.getElements()) {
                sb.append(concepto.getQuantity()).append(">");
                sb.append(concepto.getUnitMeasure()).append(">");
                sb.append(translateCodes(concepto.getDescription())).append(">");
                sb.append(concepto.getUnitPrice()).append(">");
                sb.append(concepto.getAmount()).append(">");
            }
        }*/
        
        sb.append("\n");
        return sb.toString();
    }
	
	/*private String armarRenglonConceptos(Invoice_Masivo invoice, int indexInvoice) throws Exception{
        StringBuilder sb = new StringBuilder();
                        
        if (invoice.getElements() != null && !invoice.getElements().isEmpty()) {
            if (invoice.getElements().size() > maxConceptos) {
                maxConceptos = invoice.getElements().size();
            }
            for (ElementsInvoice concepto : invoice.getElements()) {
            	sb.append("c").append(">");
            	sb.append("<idx>" + indexInvoice + "<idx>").append(">");											//numero de Factura
                sb.append(concepto.getQuantity()).append(">");
                sb.append(concepto.getUnitMeasure()).append(">");
                sb.append(translateCodes(concepto.getDescription())).append(">");
                sb.append(concepto.getUnitPrice()).append(">");
                sb.append(concepto.getAmount()).append(">");
                sb.append("\n");
            }
        }
        
        
        return sb.toString();
    }*/
	
	
	public String translateCodes(String cadena) throws Exception{
		org.apache.commons.lang3.text.translate.UnicodeEscaper escaper = org.apache.commons.lang3.text.translate.UnicodeEscaper.above(127);
		org.apache.commons.lang3.text.translate.UnicodeEscaper escaperMayorQue = org.apache.commons.lang3.text.translate.UnicodeEscaper.between(62, 62);
		
		String strEscaped = "";
		if(cadena != null && !cadena.equals("")){
			strEscaped = escaper.translate(cadena);
			strEscaped = escaperMayorQue.translate(strEscaped);
		}
		
		return strEscaped;
		
		
	}
}
