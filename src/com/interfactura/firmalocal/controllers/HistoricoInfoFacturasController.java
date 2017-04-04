package com.interfactura.firmalocal.controllers;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import mx.gob.sat.cfd.x3.ComprobanteDocument;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.interfactura.firmalocal.datamodel.Invoice_Masivo;
import com.interfactura.firmalocal.xml.Properties;
import com.interfactura.firmalocal.xml.util.ParseXML_CFDI;

@Controller
public class HistoricoInfoFacturasController {

	@Autowired
	Properties properties;

	private String pathHistoricoProceso = "/salidas/CFDHistorico/proceso/";
	
	private String pathHistoricoXml = "/salidas/CFDHistorico/xml/";
		
	public void readBD(String strOdate){
		try{
		File fileBD = new File(pathHistoricoProceso + "CFDSHISTORICO.TXT");
		
		FileOutputStream salidaInfoFacturas = new FileOutputStream(new File(pathHistoricoProceso + "FACTURASHISTORICO" + strOdate + ".TXT"));
				
		FileInputStream fs = new FileInputStream(fileBD);
		DataInputStream in = new DataInputStream(fs);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine = "";
		int counter = 0;
		while((strLine = br.readLine()) != null){
			if(!strLine.trim().equals("")){
				String [] arrayValuesBd = strLine.trim().split(">");		
											
				salidaInfoFacturas.write(extractionInfo(pathHistoricoXml + arrayValuesBd[2], arrayValuesBd, strOdate, counter).getBytes("UTF-8"));
				
				counter++;
			}			
			
		}
		br.close();
		in.close();
		fs.close();
		
		salidaInfoFacturas.close();
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Exception historicoInfoFacturas:" + e.getMessage());
			try {				
				FileOutputStream fileError = new FileOutputStream(pathHistoricoProceso + "historicoInfoFacturasError.txt");
				fileError.write((e.getMessage()).getBytes("UTF-8"));
				fileError.close();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();				
				System.out.println("Exception al crear historicoInfoFacturasError.txt:" + e.getMessage());
			}
		}
	}
	
	/////////////////////////////Extraction Info from XML//////////////////////////////
	
	public String extractionInfo(String strPath, String [] valuesCfd, String strOdate, int indexInvoice) throws Exception{
		StringBuilder sb = new StringBuilder();
        
		
        if(!strPath.equals("")){    
            
            //int counterCfd = 0;
            
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
                            Invoice_Masivo invoice = parser.parse(xmlFile);
                            
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

                            String renglon = armarRenglon(nombreEmisor, rfcEmisor, invoice, valuesCfd, strOdate);
                                             
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
                //counterCfd++;
                       
            
        } else {
            
        	sb.append("No fue encontrado en Ondemand el XML de la factura con folio " + valuesCfd[4] + " -folioInterno " + valuesCfd[5] + " -folioSAT " + valuesCfd[2] + "\n");
            
        }
		return sb.toString();
	}
	
	private String armarRenglon(String nombreEmisor, String rfcEmisor, Invoice_Masivo invoice, String [] valuesCfd, String strOdate) throws Exception{
        StringBuilder sb = new StringBuilder();
        sb.append("c").append(">");  
        
        sb.append(valuesCfd[11]).append(">");											//sourcefilename
        sb.append(valuesCfd[5]).append(">");											//folio interno
        sb.append(valuesCfd[2]).append(">");											//folioSAT
        sb.append(valuesCfd[6]).append(">");											//formattype
        sb.append(valuesCfd[7]).append(">");											//status
        sb.append(valuesCfd[8]).append(">");											//fechaCancelacion
        sb.append(valuesCfd[9]).append(">");											//fechaEmision
        
        sb.append(pathHistoricoXml + valuesCfd[2]).append(">");							//xmlRoute
        sb.append(valuesCfd[10]).append(">");											//fiscalEntity_Id
        
        sb.append(translateCodes(rfcEmisor)).append(">");								//rfc emisor
        sb.append(translateCodes(nombreEmisor)).append(">");							//entidad emisor      
        sb.append(translateCodes(invoice.getSerie())).append(">");						//serie
        if(valuesCfd[6].trim().equals("4")){
        	sb.append(translateCodes(invoice.getTipoDeComprobante()) + 
            		"|" + invoice.getTipoOperacion()).append(">");							//tipoDeComprobante
        }else{
        	sb.append(translateCodes(invoice.getTipoDeComprobante())).append(">");			//tipoDeComprobante
        }
        
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
               
        sb.append("FACTURASHISTORICO" + strOdate + ".TXT").append(">");       				//nombreDeArchivo
        
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
        sb.append(translateCodes(invoice.getProviderNumber()));
        
        sb.append("\n");
        return sb.toString();
    }
	
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
