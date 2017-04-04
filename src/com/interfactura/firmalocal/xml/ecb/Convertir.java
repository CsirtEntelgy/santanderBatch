package com.interfactura.firmalocal.xml.ecb;

import static com.interfactura.firmalocal.xml.util.Util.isNullEmpity;
import static com.interfactura.firmalocal.xml.util.Util.tags;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.interfactura.firmalocal.domain.entities.FiscalEntity;
import com.interfactura.firmalocal.xml.Properties;
import com.interfactura.firmalocal.xml.TagsXML;
import com.interfactura.firmalocal.xml.util.Util;
import com.interfactura.firmalocal.xml.util.NombreAplicativo;

/**
 * Clase que se encargara de convertir las lineas a XML
 * 
 * @author jose luis
 * 
 */
@Component
public class Convertir 
{

	private Logger logger = Logger.getLogger(Convertir.class);

	@Autowired
	private TagsXML tags;
	private Stack<String> pila;
	//private StringBuilder concat;
	private StringBuffer  concat;
	private List<String> descriptionFormat;
	private String[] lineas;
	@Autowired
	private Properties properties;
	
	private List<StringBuffer> lstMovimientosECB = new ArrayList<StringBuffer>();
	
	public void set(String linea, long numberLine, String fileNames, HashMap<String, String> hashApps, String numeroMalla) 
	{
		lineas = linea.split("\\|");
		logger.debug("Iniciando parseo de lineas");
		clearFlag();
		pila = new Stack<String>();
		
		/*if(fileNames.trim().equals("CFDREPROCESOECB")){
			//Solo la malla de Reproceso ECB
			if (lineas.length >= 10) 
			{	System.out.println("entra If");
				tags.NUM_CTE = lineas[1];
				tags.NUM_CTA = lineas[2];
				tags.EMISION_PERIODO = lineas[3];
				tags.NUM_TARJETA = lineas[4];
				tags.TOTAL_MN = lineas[5].trim();
				tags.IVA_TOTAL_MN = lineas[6].trim();
				tags.LONGITUD = lineas[7].trim();
				tags.TIPO_FORMATO = lineas[8].trim();
				
				boolean fDigitOK = true;
				String tipoCambio = lineas[9].trim();
				if(!tipoCambio.equals("")){
					String[] fileNamesArr = tipoCambio.split("\\.");
					System.out.println("lineas[9].trim():" + lineas[9].trim());
					System.out.println("fileNamesArr:" + fileNamesArr);
					for (int i=0; i < fileNamesArr.length; i++)
					{							
						if(i==0){
							//parte entera
							if(fileNamesArr[i].length()==0 || fileNamesArr[i].length()>2){
								fDigitOK = false;
								break;
							}
						}else if(i==1){
							//parte decimal
							if(fileNamesArr[i].length()>4){
								fDigitOK = false;
								break;
							}
						}else if(i>1){
							fDigitOK = false;
							break;
						}
					}
					if(fDigitOK){
						tags.TIPO_CAMBIO = lineas[9].trim();	
					}else{
						tags.TIPO_CAMBIO="TIPODECAMBIO_INCORRECTO";
					}
				}else{
					tags.TIPO_CAMBIO = "";
				}
												
				if ((lineas.length >= 11) && (lineas[10] != null) && (lineas[10].length() > 0) && (!lineas[10].trim().equals("temp"))){
											
					tags.NOMBRE_APP_REPECB = lineas[10].trim();
					
				}else{
					//No se informa el nombre de aplicativo al que pertenece el comprobante
					tags.NOMBRE_APP_REPECB = "";
				}
				System.out.println("tags.TIPO_CAMBIO:" + tags.TIPO_CAMBIO);
				System.out.println("tags.NOMBRE_APP_REPECB:" + tags.NOMBRE_APP_REPECB);
			} 
			else 
			{	System.out.println("entra else");formatECB(numberLine);	}
		}else{*/
			//Cualquier malla de Estados de Cuenta (excepto Reproceso ECB)
			if (lineas.length >= 9) 
			{	System.out.println("entra If");
				tags.NUM_CTE = lineas[1];
				tags.NUM_CTA = lineas[2];
				tags.EMISION_PERIODO = lineas[3];
				tags.NUM_TARJETA = lineas[4];
				tags.TOTAL_MN = lineas[5].trim();
				tags.IVA_TOTAL_MN = lineas[6].trim();
				tags.LONGITUD = lineas[7].trim();
				tags.TIPO_FORMATO = lineas[8].trim();
							
				if ((lineas.length >= 10) && (lineas[9] != null) && (lineas[9].length() > 0) && (!lineas[9].trim().equals("temp"))){				
					boolean fDigitOK = true;
					String tipoCambio = lineas[9].trim();
					String[] fileNamesArr = tipoCambio.split("\\.");
					System.out.println("lineas[9].trim():" + lineas[9].trim());
					System.out.println("fileNamesArr:" + fileNamesArr);
					for (int i=0; i < fileNamesArr.length; i++)
					{		
						
						if(i==0){
							//parte entera
							if(fileNamesArr[i].length()==0 || fileNamesArr[i].length()>2){
								fDigitOK = false;
								break;
							}
						}else if(i==1){
							//parte decimal
							if(fileNamesArr[i].length()>4){
								fDigitOK = false;
								break;
							}
						}else if(i>1){
							fDigitOK = false;
							break;
						}
					}
					if(fDigitOK){
						tags.TIPO_CAMBIO = lineas[9].trim();	
					}else{
						tags.TIPO_CAMBIO="TIPODECAMBIO_INCORRECTO";
					}			
									
				}else{
					tags.TIPO_CAMBIO = "";
				}
												
				tags.NOMBRE_APP_REPECB = NombreAplicativo.obtieneNombreApp(hashApps, fileNames, numeroMalla); 
						
				System.out.println("tags.TIPO_CAMBIO:" + tags.TIPO_CAMBIO);
				System.out.println("tags.NOMBRE_APP_REPECB:" + tags.NOMBRE_APP_REPECB);
			} 
			else 
			{	System.out.println("entra else");formatECB(numberLine);	}
		//}
		
		
	}

	/**
	 * Reinicia las Banderas
	 */
	public void clearFlag() 
	{
		tags.isEntidadFiscal = false;
		tags.isEmisor = false;
		tags.isReceptor = false;
		tags.isConceptos = false;
		tags.isConcepto = false;
		tags.isPredial = false;
		tags.isParte = false;
		tags.isComplementoConcepto = false;
		tags.isAduanera = false;
		tags.isRetenciones = false;
		tags.isTralados = false;
		tags.isImpuestos = false;
		tags.isComplemento = false;
		tags.isMovimiento = false;
		// tags.isComprobante=false;
		tags.isAdenda = false;
		tags.isDescriptionTASA = false;
		tags.isFormat = false;
		descriptionFormat = new ArrayList<String>();
	}

	//24 de Abril 2013 Verificar si una cadena es numérica
	public boolean isNotNumeric(String strNumber){
		boolean fNotNumber = false;
		int i=0;
		while(!fNotNumber && i < strNumber.length()){
			try{
				Integer.parseInt(Character.toString(strNumber.charAt(i)));				
			}catch(NumberFormatException ex){
				fNotNumber = true;
				break;
			}
			i++;
		};
		return fNotNumber;		
	}
	
	/**
	 * Genera el TAG 'Comprobante' que tiene la siguiente estructura:
	 * 
	 * @param linea
	 * @param numberLine
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] fComprobante(String linea, long numberLine, HashMap<String, HashMap> tipoCambio, HashMap fiscalEntities, HashMap campos22, String fileNames) 
		throws UnsupportedEncodingException 
	{
		
			
		
			lineas = linea.split("\\|");
			System.out.println("linea " + linea);
			//System.out.println("lineas[9]" + lineas[9]);
			
			if (lineas.length >= 8) 
			{
				
				concat = new StringBuffer();
				
				tags.CFD_TYPE = lineas[1].trim().toUpperCase();
				
				tags.SERIE_FISCAL_CFD = lineas[3].trim();
				
				if (!Util.isNullEmpty(tags.SERIE_FISCAL_CFD)) 
				{
					
					if(tags.SERIE_FISCAL_CFD.length() >= 3){
						if(tags.SERIE_FISCAL_CFD.substring(0, 3).trim().equals("MXN") || 
							tags.SERIE_FISCAL_CFD.substring(0, 3).trim().equals("USD") || 
							tags.SERIE_FISCAL_CFD.substring(0, 3).trim().equals("EUR") || 
							tags.SERIE_FISCAL_CFD.substring(0, 3).trim().equals("UDI")){
							concat.append(" serie=\"" + tags.SERIE_FISCAL_CFD + "\"");	
							concat.append(" Moneda=\"" + tags.SERIE_FISCAL_CFD.substring(0, 3).trim() + "\"");	
							tags.TIPO_MONEDA = tags.SERIE_FISCAL_CFD.substring(0, 3).trim();
						}else if(tags.SERIE_FISCAL_CFD.substring(0, 3).trim().equals("BME") && fileNames.equals("CFDLFFONDOS")){
							concat.append(" serie=\"" + tags.SERIE_FISCAL_CFD + "\"");	
							concat.append(" Moneda=\"" + "MXN" + "\"");	
							tags.TIPO_MONEDA = "MXN";
						}else{
							//tags.SERIE_FISCAL_CFD="MONEDA INCORRECTA " + tags.SERIE_FISCAL_CFD + "";
							tags.TIPO_MONEDA = tags.SERIE_FISCAL_CFD;
							concat.append(" MonedaIncorrecta" + tags.TIPO_MONEDA + "=\"" + tags.SERIE_FISCAL_CFD + "\"");
						}
					}else if(tags.SERIE_FISCAL_CFD.trim() == ""){
						concat.append(" serie=\"" + tags.SERIE_FISCAL_CFD + "\"");	
						concat.append(" Moneda=\"" + tags.SERIE_FISCAL_CFD + "\"");
						tags.TIPO_MONEDA = tags.SERIE_FISCAL_CFD;
					}else{
						//tags.SERIE_FISCAL_CFD="MONEDA INCORRECTA " + tags.SERIE_FISCAL_CFD + "";
						tags.TIPO_MONEDA = tags.SERIE_FISCAL_CFD;
						concat.append(" MonedaIncorrecta" + tags.TIPO_MONEDA + "=\"" + tags.SERIE_FISCAL_CFD + "\"");
					}
								
								
					if(fileNames.equals("CFDLFFONDOS")){
						if(!tags.TIPO_CAMBIO.isEmpty()){//////condición de prueba"""""""""""""""
							//HashMap<String, String> monedas = (HashMap<String, String>) tipoCambio.get(tags.EMISION_PERIODO);
							System.out.println("tags.TIPO_CAMBIO: " + tags.TIPO_CAMBIO);
							if (!tags.TIPO_CAMBIO.trim().equals("TIPODECAMBIO_INCORRECTO"))
							{
								concat.append(" TipoCambio=\"" + tags.TIPO_CAMBIO + "\"");					
							}
							else{
								//tags.TIPO_CAMBIO="TIPO_CAMBIO INCORRECTO " + tags.TIPO_CAMBIO + "";
								concat.append(" TipoCambioIncorrecto" + tags.TIPO_CAMBIO + "=\"" + tags.TIPO_CAMBIO + "\"");
							}							
						}else{
							tags.TIPO_CAMBIO = "1.0000";
							concat.append(" TipoCambio=\"" + tags.TIPO_CAMBIO + "\"");
						}							
					}else{
						//HashMap<String, String> monedas = (HashMap<String, String>) tipoCambio.get(tags.EMISION_PERIODO);
						System.out.println("tags.TIPO_CAMBIO: " + tags.TIPO_CAMBIO);
						if (tags.TIPO_CAMBIO.length() > 0 && !tags.TIPO_CAMBIO.trim().equals("TIPODECAMBIO_INCORRECTO"))
						{
							concat.append(" TipoCambio=\"" + tags.TIPO_CAMBIO + "\"");					
						}
						else if(tags.TIPO_CAMBIO.trim().equals("TIPODECAMBIO_INCORRECTO")){
							//tags.TIPO_CAMBIO="TIPO_CAMBIO INCORRECTO " + tags.TIPO_CAMBIO + "";
							concat.append(" TipoCambioIncorrecto" + tags.TIPO_CAMBIO + "=\"" + tags.TIPO_CAMBIO + "\"");
						}
						else
						{
							//tags.TIPO_CAMBIO="NO EXISTEN TIPOS DE CAMBIO PARA EL DIA " + tags.EMISION_PERIODO + "";
							concat.append(" TipoCambioNoDefinido" + tags.TIPO_CAMBIO + "=\"" + tags.TIPO_CAMBIO + "\"");
						}
					}				
				}
				else
				{
					tags.TIPO_CAMBIO="";
					tags.TIPO_MONEDA="";
				}
							
				if(!isNotNumeric(lineas[4].trim())){	
					tags.FOLIO_FISCAL_CFD=lineas[4].trim();
					concat.append(" folio=\"" + tags.FOLIO_FISCAL_CFD + "\"");
				}else{
					tags.FOLIO_FISCAL_CFD=lineas[4].trim();
					concat.append(" CuentaIncorrecta" + tags.FOLIO_FISCAL_CFD + "=\"" + tags.FOLIO_FISCAL_CFD + "\"");
				}
				
				
				//System.out.println("antesCalendar ");
				
				tags.FECHA_CFD = Util.convertirFecha(Calendar.getInstance().getTime());
						
				//System.out.println("despuesCalendar " + tags.FECHA_CFD );
				
				System.out.println("tags.EMISION_PERIODO " + tags.EMISION_PERIODO);
				System.out.println("FECHA_CFD " + tags.FECHA_CFD);
				//concat.append(" fecha=\"" + Util.convertirFecha(tags.EMISION_PERIODO, tags.FECHA_CFD) + "\"");
				concat.append(" fecha=\"" + tags.FECHA_CFD + "\"");
				
				tags.SUBTOTAL_MN = lineas[6].trim();
				concat.append(" subTotal=\"" + tags.SUBTOTAL_MN + "\"");
				concat.append(" total=\"" + lineas[7].trim() + "\"");
				
				//Doble Sellado
				if(Util.proofType(lineas[1].trim().toUpperCase()).equals("tipoDeComprobanteIncorrecto")){
					concat.append(" tipoDeComprobanteIncorrecto" + lineas[1].trim().toUpperCase() + "=\"" + lineas[1].trim().toUpperCase() + "\" ");
				}else{
					concat.append(" tipoDeComprobante=\""
							+ Util.proofType(lineas[1].trim().toUpperCase()) + "\" ");					
				}
				
				tags.NUM_APROBACION = tags.EMISION_PERIODO.split("-")[0];
				tags.YEAR_APROBACION = tags.NUM_APROBACION;
				
				
				/*
				return Util
						.conctatArguments(
								"\n<cfdi:Comprobante version=\"3.2\" ",
								concat.toString(),
								"xmlns:ecb=\"http://www.sat.gob.mx/ecb\" ",
								"xmlns:cfdi=\"http://www.sat.gob.mx/cfd/3\"  ",								
								"xsi:schemaLocation=\"http://www.sat.gob.mx/cfd/3 ",
								"http://www.sat.gob.mx/sitio_internet/cfd/3/cfdv32.xsd ",
								"http://www.sat.gob.mx/ecb http://www.sat.gob.mx/sitio_internet/cfd/ecb/ecb.xsd\" ",
								"sello=\"", properties.getLabelSELLO(), "\" ",
								"noCertificado=\"",
								properties.getLblNO_CERTIFICADO(), "\" ",
								"certificado=\"", properties.getLblCERTIFICADO(),
								"\" ","formaDePago=\"" + "PAGO EN UNA SOLA EXHIBICION" + "\" ",
								"metodoDePago=\"" + properties.getLabelMetodoPago() + "\" ",
								"LugarExpedicion=\"" + properties.getLabelLugarExpedicion() + "\" ",
								"NumCtaPago=\"" + properties.getlabelFormaPago() + "\" ",
								"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">")
						.toString().getBytes("UTF-8");
						*/
				return Util
						.conctatArguments(
								"\n<cfdi:Comprobante version=\"3.2\" ",
								concat.toString(),
								"xmlns:Santander=\"http://www.santander.com.mx/addendaECB\" ",		
								"xmlns:cfdi=\"http://www.sat.gob.mx/cfd/3\"  ",		
								"xsi:schemaLocation=\"http://www.sat.gob.mx/cfd/3 ",
								"http://www.sat.gob.mx/sitio_internet/cfd/3/cfdv32.xsd ",
								"http://www.santander.com.mx/addendaECB http://www.santander.com.mx/cfdi/addendaECB.xsd\" ",
								"sello=\"", properties.getLabelSELLO(), "\" ",
								"noCertificado=\"",
								properties.getLblNO_CERTIFICADO(), "\" ",
								"certificado=\"", properties.getLblCERTIFICADO(),
								"\" ","formaDePago=\"" + "PAGO EN UNA SOLA EXHIBICION" + "\" ",
								"metodoDePago=\"" + properties.getLabelMetodoPago() + "\" ",
								"LugarExpedicion=\"" + properties.getLabelLugarExpedicion() + "\" ",
								"NumCtaPago=\"" + properties.getlabelFormaPago() + "\" ",
								"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">")
						.toString().getBytes("UTF-8");
			} 
			else 
			{	return formatECB(numberLine);	}
		
	}

	/**
	 * 
	 * @param linea
	 * @param lstFiscal
	 * @param numberLine
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] emisor(String linea, HashMap<String, FiscalEntity> lstFiscal, 
			long numberLine, HashMap campos22) 
		throws UnsupportedEncodingException 
	{
		lineas = linea.split("\\|");
		if (lineas.length >= 2) 
		{
			tags.EMISION_RFC = lineas[1].trim();
			tags.fis = null;
			tags.fis = lstFiscal.get(tags.EMISION_RFC);
			
			//String expedidoStr = "\n<ExpedidoEn calle=\"AVE. VASCONCELOS\" noExterior=\"142 OTE.\" colonia=\"COL DEL VALLE\" localidad=\"S.PEDRO GARZA G.\" municipio=\"S.PEDRO GARZA G.\" estado=\"N.L.\" pais=\"Mexico\" codigoPostal=\"66220\" xmlns=\"http://www.sat.gob.mx/cfd/2\" />";
			
			StringBuffer emisorStr = Util
			.conctatArguments(tags("Emisor", pila), "\n<cfdi:Emisor rfc=\"",
					tags.EMISION_RFC, "\"", getNameEntityFiscal(),
					" >", domicilioFiscal(campos22));
			
			try
			{
				HashMap map1 = (HashMap) campos22.get(tags.EMISION_RFC);
				if (map1 != null)
				{
					String LugarExpedicion = (String) map1.get("LugarExpedicion");
					tags.LUGAR_EXPEDICION = LugarExpedicion;
				}
				
				//El metodo de Pago para la fase 1 será 99 para todas las interfaces de Estados de Cuenta
				
				HashMap map2 =  (HashMap) campos22.get(tags.EMISION_RFC);
				
				if (map2 != null)
				{
					String MetodoDePago = (String) map2.get("metodoDePago");
					tags.METODO_PAGO = MetodoDePago;
				}
				else
				{
					tags.METODO_PAGO = "99";
				}
				
				
				HashMap map3 =  (HashMap) campos22.get(tags.EMISION_RFC);
				if (map3 != null)
				{
					String formaDePago = (String) map3.get("formaDePago");
					tags.FORMA_PAGO = formaDePago;
				}	
				
			}
			catch (Throwable e)
			{	e.printStackTrace();	}
						
			return emisorStr.toString().getBytes("UTF-8");
		} 
		else 
		{	return formatECB(numberLine);	}
	}

	public String getNameEntityFiscal() 
	{
		if (tags.fis == null) 
		{
			tags.isEntidadFiscal = false;
			return " nombre=\"No existe la Entidad Fiscal\"";
		} 
		else 
		{
			tags.isEntidadFiscal = true;
			return " nombre=\"" + Util.isNull(tags.fis.getFiscalName()) + "\"";
		}
	}

	public byte[] receptor(String linea, long numberLine) 
		throws UnsupportedEncodingException 
	{
		lineas = linea.split("\\|");
		if (lineas.length >= 3) 
		{
			tags.RECEPCION_RFC = lineas[1].trim();
			//Doble Sellado
			String nombreReceptor = "";
			
			if(lineas.length > 2){
				if(!lineas[2].trim().equals("")){
					nombreReceptor = " nombre=\"" + Util.convierte(lineas[2].trim()) + "\"";
				}				
			}
			
			return Util
					.conctatArguments(
							tags("Receptor", pila),
							"\n<cfdi:Receptor rfc=\"",
							Util.convierte(lineas[1].trim()),
							"\"",
							nombreReceptor, " >").toString().getBytes("UTF-8");
			/*return Util
					.conctatArguments(
							tags("Receptor", pila),
							"\n<cfdi:Receptor rfc=\"",
							Util.convierte(lineas[1].trim()),
							"\"",
							lineas.length > 2 ? " nombre=\"" + Util.convierte(lineas[2].trim())
									+ "\"" : "", " >").toString().getBytes("UTF-8");*/
		} 
		else 
		{	return formatECB(numberLine);	}
	}

	/**
	 * 
	 * @param linea
	 * @param numberLine
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] concepto(String linea, long numberLine, HashMap fiscalEntities, HashMap campos22) 
		throws UnsupportedEncodingException 
	{
		lineas = linea.split("\\|");
		if (lineas.length >= 3) 
		{			
			//HashMap campos = (HashMap) campos22.get(tags.fis.getTaxID());
			//System.out.println("concepto RFC:" + tags.EMISION_RFC);
			HashMap campos = (HashMap) campos22.get(tags.EMISION_RFC);
			System.out.println("tags.EMISION_RFC:" + tags.EMISION_RFC);
			String unidadVal; 	
			if(campos !=null)
			{				
				System.out.println("campos(campos !=null):" + campos);
				unidadVal= (String) campos.get("unidadMedida");
				tags.UNIDAD_MEDIDA=unidadVal;
			}
			else
			{
				System.out.println("campos(campos==null):" + campos);
				tags.UNIDAD_MEDIDA="***NO EXISTE UNIDAD DE MEDIDA DEFINIDA***";
				unidadVal=tags.UNIDAD_MEDIDA;
			}
				
			return Util
					.conctatArguments(
							"\n<cfdi:Concepto cantidad=\"1\" descripcion=\"",
							Util.convierte(lineas[1]).trim(), "\" valorUnitario=\"",
							lineas[2].trim(), 
							"\" importe=\"", lineas[2].trim(), 
							"\" unidad=\"", unidadVal,
							"\"/>").toString().getBytes("UTF-8");
		} 
		else 
		{	return formatECB(numberLine);	}
	}

	public String domicilioFiscal(HashMap campos22) 
	{
		if (tags.isEntidadFiscal) 
		{
			tags._Calle = "calle=\""
					+ Util.isNull(tags.fis.getAddress().getStreet()) + "\" ";
			tags._NoExterior = Util.isNullEmpity(tags.fis.getAddress()
					.getExternalNumber(), "noExterior");
			tags._NoInterior = Util.isNullEmpity(tags.fis.getAddress()
					.getInternalNumber(), "noInterior");
			tags._Colonia = Util.isNullEmpity(tags.fis.getAddress()
					.getNeighborhood(), "colonia");
			tags._Localidad = Util.isNullEmpity("", "localidad");
			tags._Referencia = Util.isNullEmpity(tags.fis.getAddress()
					.getReference(), "referencia");
			tags._Municipio = "municipio=\""
					+ Util.convierte(tags.fis.getAddress().getRegion()) + "\" ";
			if(tags.fis.getAddress().getState()!=null)
			{
				tags._Estado = "estado=\""
					+ Util.convierte(tags.fis.getAddress().getState().getName()) + "\" ";
				tags._Pais = " pais=\""
					+ Util.convierte(tags.fis.getAddress().getState().getCountry().getName())
					+ "\" ";
			} 
			else 
			{
				tags._Estado = "estado=\"\" ";
				tags._Pais = " pais=\"\" ";
			}

			tags._CodigoPostal = "codigoPostal=\""
					+ tags.fis.getAddress().getZipCode() + "\" ";
		} 
		else 
		{
			tags._Calle = "calle=\"\" ";
			tags._NoExterior = "";
			tags._NoInterior = "";
			tags._Colonia = "";
			tags._Localidad = "";
			tags._Referencia = "";
			tags._Municipio = "municipio=\"\" ";
			tags._Estado = "estado=\"\" ";
			tags._Pais = " pais=\"\" ";
			tags._CodigoPostal = "codigoPostal=\"\" ";
		}

		String regimenStr = "";
		HashMap map = (HashMap) campos22.get(tags.EMISION_RFC);
		System.out.println("***Buscando campos cfd22 para: " + tags.EMISION_RFC);
		if (map != null)
		{	
			String regVal = (String) map.get("regimenFiscal");
			regimenStr = "\n<cfdi:RegimenFiscal Regimen=\"" + regVal + "\" />";
			tags.REGIMEN_FISCAL = regVal;	
		}
		
		return Util.conctatArguments("\n<cfdi:DomicilioFiscal ", tags._Calle,
				tags._NoExterior, tags._NoInterior, tags._Colonia,
				tags._Localidad, tags._Referencia, tags._Municipio,
				tags._Estado, tags._Pais, tags._CodigoPostal, " />" + regimenStr.toString(),
				tags("", pila)).toString();
	}

	/**
	 * 
	 * @param linea
	 * @param numberLine
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] impuestos(String linea, long numberLine) 
		throws UnsupportedEncodingException 
	{
		lineas = linea.split("\\|");
		tags.isImpuestos = true;
		tags.isConceptos = false;
		if (lineas.length >= 3) 
		{
			tags.TOTAL_IMP_RET = lineas[1].trim();
			tags.TOTAL_IMP_TRA = lineas[2].trim();
			return Util
					.conctatArguments(
							tags("", pila),
							"\n<cfdi:Impuestos  ",
							isNullEmpity(lineas[1], "totalImpuestosRetenidos"),
							isNullEmpity(lineas[2], "totalImpuestosTrasladados"),
							">").toString().getBytes("UTF-8");
		} 
		else 
		{	return formatECB(numberLine);	}
	}

	public byte[] traslados(String linea, long numberLine) 
		throws UnsupportedEncodingException 
	{
		lineas = linea.split("\\|");
		if (lineas.length >= 4) 
		{
			return Util
					.conctatArguments("\n<cfdi:Traslado impuesto=\"",
							lineas[1].trim(), "\" tasa=\"", lineas[2].trim(),
							"\" importe=\"", lineas[3].trim(), "\"/>")
					.toString().getBytes("UTF-8");
		} 
		else 
		{	return formatECB(numberLine);	}
	}

	/**
	 * 
	 * @param linea
	 * @param numberLine
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] retenciones(String linea, long numberLine) 
		throws UnsupportedEncodingException 
	{
		lineas = linea.split("\\|");
		if (lineas.length >= 3) 
		{
			return Util
					.conctatArguments("\n<cfdi:Retencion impuesto=\"",
							lineas[1].trim(), "\" importe=\"",
							lineas[2].trim(), "\"/>").toString().getBytes("UTF-8");
		} 
		else 
		{	return formatECB(numberLine);	}
	}

	/**
	 * 
	 * @param linea
	 * @param numberLine
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] complemento(String linea, long numberLine) 
		throws UnsupportedEncodingException 
	{
		lineas = linea.split("\\|");
		if (lineas.length >= 5) {
			String nombreCliente = "";
			
			if(!lineas[2].trim().equals("")){
				nombreCliente = "nombreCliente=\"" + Util.convierte(lineas[2].trim()) + "\" ";
			}
			return Util
					.conctatArguments(
							"\n<Santander:EstadoDeCuentaBancario version=\"1.0\" ",
							"numeroCuenta=\"", lineas[1].trim(), "\" ",
							nombreCliente,
							"periodo=\"", lineas[3].trim(), "\">").toString()
					.getBytes("UTF-8");
		} 
		else 
		{	return formatECB(numberLine);	}
	}

	/**
	 * 
	 * @param linea
	 * @return flg
	 * @throws PatternSyntaxException
	 */
	public boolean validarRFC(String rfc)
		throws PatternSyntaxException
	{
		// TODO Auto-generated method stub
		//Patron del RFC--->>>[A-Z,Ñ,&]{3,4}[0-9]{2}[0-1][0-9][0-3][0-9][A-Z,0-9]{2}[0-9,A]
		
		 Pattern p = Pattern.compile("[A-Z,Ñ,&]{3,4}[0-9]{2}[0-1][0-9][0-3][0-9][A-Z,0-9]{2}[0-9,A]");
		 Matcher m = p.matcher(rfc);
	     
	     if(!m.find()){
	    	 //RFC no valido
	    	 return false;
	     }
	     else{
	    	 //RFC valido
	    	 return true;
	     }
	}
	/**
	 * 
	 * @param linea
	 * @param numberLine
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] movimeinto(String linea, long numberLine) 
		throws UnsupportedEncodingException 
	{
		
			
			System.out.println("length: " + lineas.length);
			lineas = linea.split("\\|");
			if (lineas.length >= 7) 
			{
				Calendar c = Calendar.getInstance();
				
				String[] date = lineas[1].trim().split("-");
				String fechaCal = "";
				try
				{
					c.set(Integer.parseInt(date[0]), Integer.parseInt(date[1]) - 1,
						Integer.parseInt(date[2]), 0, 0, 0);
					fechaCal = "fecha=\"" + Util.convertirFecha(c.getTime()) + "\"";
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				
				
				String rfcEnajenante = lineas[4];
				
				//Validar el RFC
				boolean flgRfcOk;
				flgRfcOk = validarRFC(rfcEnajenante);
				
							
				if ((rfcEnajenante != null)&&(rfcEnajenante.trim().length() > 0) && flgRfcOk)
				{
					
					return Util
					.conctatArguments("\n<Santander:MovimientoECBFiscal ", fechaCal,
							" descripcion=\"", Util.convierte(lineas[3].trim()), "\"",
							" RFCenajenante=\"", Util.convierte(lineas[4].trim()), "\"",
							" Importe=\"", lineas[5].trim(), "\"/>").toString()
					.getBytes("UTF-8");
				}
				else
				{			
					/*
					StringBuffer sb = new StringBuffer();
					sb = Util.conctatArguments("\n<ecb:MovimientoECB ", fechaCal,
									" descripcion=\"", Util.convierte(lineas[3].trim()), "\"",
									" importe=\"", lineas[5].trim(), "\"/>");
					this.lstMovimientosECB.add(sb);
									
					return "".getBytes("UTF-8");
					*/
					
					return Util
						.conctatArguments("\n<Santander:MovimientoECB ", fechaCal,
								" descripcion=\"", Util.convierte(lineas[3].trim()), "\"",
								" importe=\"", lineas[5].trim(), "\"/>").toString()
						.getBytes("UTF-8");
						
				}
			} 
			else 
			{	return formatECB(numberLine);	}
		
	}

	/**
	 * 
	 * @param linea
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] domicilio(String linea, long numberLine) 
		throws UnsupportedEncodingException 
	{
		lineas = linea.split("\\|");
		if (lineas.length >= 11) {
			tags._Calle = Util.isNullEmpity(lineas[1].trim(), "calle");
			tags._NoExterior = Util
					.isNullEmpity(lineas[2].trim(), "noExterior");
			tags._NoInterior = Util
					.isNullEmpity(lineas[3].trim(), "noInterior");
			tags._Colonia = Util.isNullEmpity(lineas[4].trim(), "colonia");
			tags._Localidad = Util.isNullEmpity(lineas[5].trim(), "localidad");
			tags._Referencia = Util
					.isNullEmpity(lineas[6].trim(), "referencia");
			tags._Municipio = Util.isNullEmpity(lineas[7].trim(), "municipio");
			tags._Estado = Util.isNullEmpity(lineas[8].trim(), "estado");
			tags._Pais = " pais=\"" + lineas[9].trim() + "\" ";
			tags._CodigoPostal = lineas.length >= 11 ? Util.isNullEmpity(
					lineas[10].trim(), "codigoPostal") : "";
			return Util
					.conctatArguments("\n<cfdi:Domicilio ", tags._Calle,
							tags._NoExterior, tags._NoInterior, tags._Colonia,
							tags._Localidad, tags._Referencia, tags._Municipio,
							tags._Estado, tags._Pais, tags._CodigoPostal,
							" />", tags("", pila)).toString().getBytes("UTF-8");
		} 
		else 
		{	return formatECB(numberLine);	}
	}

	public TagsXML getTags() 
	{	return tags;	}

	public void setTags(TagsXML tags) 
	{	this.tags = tags;	}

	public Stack<String> getPila() 
	{	return pila;	}

	public void setPila(Stack<String> pila) 
	{	this.pila = pila;	}

	public List<String> getDescriptionFormat() 
	{	return descriptionFormat;	}

	public void setDescriptionFormat(List<String> descriptionFormat) 
	{	this.descriptionFormat = descriptionFormat;		}
	
	public byte[] formatECB(long numberLine)
	{
		tags.isFormat = true;
		descriptionFormat.add("" + numberLine);
		return "".getBytes();
	}

	public List<StringBuffer> getLstMovimientosECB() {
		return lstMovimientosECB;
	}

	public void setLstMovimientosECB(List<StringBuffer> lstMovimientosECB) {
		this.lstMovimientosECB = lstMovimientosECB;
	}
		
}
