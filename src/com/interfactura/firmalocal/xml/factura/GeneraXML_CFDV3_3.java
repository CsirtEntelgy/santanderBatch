package com.interfactura.firmalocal.xml.factura;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.ValidatorHandler;

//import oracle.net.ano.Service;











import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.interfactura.firmalocal.domain.CfdBean;
import com.interfactura.firmalocal.domain.entities.CFDIssued;
import com.interfactura.firmalocal.domain.entities.CFDIssuedIn;
import com.interfactura.firmalocal.domain.entities.FiscalEntity;
import com.interfactura.firmalocal.domain.entities.Iva;
import com.interfactura.firmalocal.domain.entities.OpenJpa;
import com.interfactura.firmalocal.domain.entities.SealCertificate;
import com.interfactura.firmalocal.persistence.CFDIssuedIncidenceManager;
import com.interfactura.firmalocal.persistence.CFDIssuedManager;
import com.interfactura.firmalocal.persistence.OpenJpaManager;
import com.interfactura.firmalocal.xml.Properties;
import com.interfactura.firmalocal.xml.TagsXML;
import com.interfactura.firmalocal.xml.WebServiceCliente;
import com.interfactura.firmalocal.xml.file.XMLProcess;
import com.interfactura.firmalocal.xml.util.Util;
import com.interfactura.firmalocal.xml.util.NombreAplicativo;
import com.interfactura.firmalocal.xml.util.UtilCatalogos;

/**
 * Procesamiento Masivo Genera el XML para una Factura
 * 
 * @author jose luis
 * 
 */
@Component
public class GeneraXML_CFDV3_3 
{
	private Logger logger = Logger.getLogger(GeneraXML_CFDV3_3.class);
	@Autowired
	private ConvertirImplV3_3 conver;
	@Autowired
	private Properties properties;
	@Autowired
	private XMLProcess xmlProcess;
	@Autowired
	public CFDIssuedManager cFDIssuedManager;
	@Autowired
	public CFDIssuedIncidenceManager cFDIssuedIncidenceManager;
	@Autowired
	public OpenJpaManager openJpaManager;
	private Transformer transf;
	private List<byte[]> lstFactoraje;
	private HashMap<String, FiscalEntity> lstFiscal;
	private List<SealCertificate> lstSeal;
	private List<Iva> lstIva;
	private ValidatorHandler validator;
	private int sizeT = 255;
	private List<CFDIssuedIn> lstCFDIncidence;
	private List<CFDIssued> lstCFD;
	private String msgError;
	private String startLine;
	private String endLine;
	private HashMap<String, HashMap> campos22;
	private HashMap<String, HashMap> tipoCambio;
	
	private WebServiceCliente servicePort = null;
	private DocumentBuilderFactory dbf = null;
	private DocumentBuilder db = null;
	private Transformer tx = null;
	
	private String urlWebService = null;
	   
	//Nombres de Aplicativos Facturas
	private HashMap<String, String> nombresApps = new HashMap<String, String>();
	
	/**
	 * 
	 * @param nameFile
	 * @return
	 */
	public boolean convierte(String nameFile) 
	{
		// este sera el que se utilizara, aqui debe retornar un numero de folios
		// a utilizar
		System.out.println("Inicia convercion");
		List<CfdBean> cfdBeans = new LinkedList<CfdBean>();
		boolean flagProcesado = true;
		File file = null;
		BufferedReader br = null;
		ByteArrayOutputStream out = null;//XML A GENERAR
		int cont = 0;
		long numberLineCFD = 0;//Indica el numero de linea a procesar
		String linea = null;
		Date date = null;
		FileOutputStream salida = null;
		FileOutputStream incidencia = null;
		try 
		{
			File fileExit = new File(this.getNameFile(
					properties.getPathSalida(), -1, "XML", nameFile));
			salida = new FileOutputStream(fileExit);
			// Se crea el archivo de incidencias
			File fileIncidence = new File(this.getNameFile(
					properties.getPathIncidencia(), -1, "INC", nameFile));
			incidencia = new FileOutputStream(fileIncidence);
			date = Calendar.getInstance().getTime();
			file = new File(properties.getPathDirProCFD() + nameFile);
			InputStreamReader isreader = new InputStreamReader(new FileInputStream(file), "UTF-8");
			br = new BufferedReader(isreader);
			cont = 1;
			//logger.debug(nameFile + " " + br);
			out = new ByteArrayOutputStream();
			begin(out);
			lstCFD = new ArrayList<CFDIssued>();
			ArrayList<CfdBean> pendientes = new ArrayList<CfdBean>();
			lstCFDIncidence = new ArrayList<CFDIssuedIn>();
			copy(this.getNameFile(properties.getPathDirBackup(), -1, null,nameFile), file);
			conver.setTags(new TagsXML());
			System.out.println("Inicia formateo de lineas");
			while ((linea = br.readLine()) != null) 
			{
//				System.out.println("Lin AMDA antes:" + linea);
				numberLineCFD += 1;
				if (!linea.startsWith(";")) 
				{	
//					String lin = Util.convierte(linea);
//					System.out.println("Lin AMDA:" + lin);
//					conver.loadInfoV33(lin);
					if (this.formatLinea(cont, incidencia, numberLineCFD,
							linea, nameFile, out, cfdBeans, date)) 
					{
						date = Calendar.getInstance().getTime();
						out = new ByteArrayOutputStream();
						begin(out);
					}
				}
				//logger.debug(nameFile + " . . . " + linea);
			}
			this.endCONCEPTOS(out);
			this.endTAGS(out);
			System.out.println("Termina formateo de lineas");
			System.out.println("Inicia cerrado y validacion de XML");
			this.end(cont, incidencia, numberLineCFD, nameFile, out, cfdBeans, date, 0);
			// aqui cierra el xml, y guarda en la bd, esta finalizado el objeto
			// list cfdBeans
			
			pendientes = finalize(cfdBeans, incidencia, salida, nameFile);
			System.out.println("Termina cerrado y validacion de XML");
			// Manda a guardar los ultimos CFD
			if (lstCFD != null && lstCFD.size() > 0) 
			{
				conver.getTags().cFDIssuedManager.update(lstCFD);
				lstCFD = null;
			}

			// Manda a guardar las incidencias
			if (lstCFDIncidence != null && lstCFDIncidence.size() > 0) 
			{	conver.getTags().cFDIssuedIncidenceManager.update(lstCFDIncidence);	}
			
			if (pendientes.size() > 0)
			{	this.procesaRemanentes(pendientes, incidencia, salida, nameFile);	}
		} 
		catch (Exception e) 
		{
			if (e.getLocalizedMessage() != null)
			{	logger.error(e.getLocalizedMessage().replace("ORA-", "ORACLE-"), e);	}
		} 
	finally 
		{	this.close(flagProcesado, file, br, salida, incidencia, nameFile);	}
		return flagProcesado;
	}
	
	private void procesaRemanentes(ArrayList<CfdBean> pendientes, FileOutputStream incidencia,
			FileOutputStream salida, String nameFile)
	{
		ArrayList<CfdBean> remanentes = finalize(pendientes, incidencia, salida, nameFile);
		if (remanentes.size() > 0)
		{	this.procesaRemanentes(remanentes, incidencia, salida, nameFile);	}
	}

	/**
	 * 
	 * @param flagProcesado
	 * @param file
	 * @param br
	 * @param salida
	 * @param incidencia
	 * @param nameFile
	 */
	private void close(boolean flagProcesado, File file, BufferedReader br,
			FileOutputStream salida, FileOutputStream incidencia,
			String nameFile) {
		try {
			br.close();

			if (salida != null) {
				salida.close();
			}

			if (incidencia != null) {
				incidencia.close();
			}

			if (flagProcesado) {
//				FileCopyUtils.copy(file,
//						new File(properties.getPathDirProcesados() + nameFile));
				// No lo debe borrar este proceso. Se borra despues de que se calculan las cifras de control
				//file.delete();
			}
		} catch (IOException ioe) 
		{	logger.error(ioe.getLocalizedMessage(), ioe);	} 
		catch (Exception e) 
		{	logger.error(e.getLocalizedMessage(), e);	} 
		finally 
		{
			br = null;
			salida = null;
			incidencia = null;
		}
	}

	/**
	 * 
	 * @param cont
	 * @param incidencia
	 * @param numberLineCFD
	 * @param linea
	 * @param nameFile
	 * @param out
	 * @param cfdBeans
	 * @param date
	 * @return
	 * @throws Exception
	 */
	private boolean formatLinea(int cont, FileOutputStream incidencia,
			long numberLineCFD, String linea, String nameFile,
			ByteArrayOutputStream out, List<CfdBean> cfdBeans, Date date)
			throws Exception 
	{
		boolean flagNuevo = false;
		try 
		{
			linea = Util.convierte(linea);
			String[] tokens = linea.concat("|temp").split("\\|");
			// aqui inicia el parseo del txt y la generacion del xml
			//AMDA fucnion prueba
//			String lineUp = Util.convierte(linea).concat("|temp");
//			String tok = linea.substring(0, 2);
//			int numElement = 0;
//			try 
//			{	numElement = Integer.parseInt(tok);	} 
//			catch (NumberFormatException numberEx) 
//			{	logger.error("No empieza con un numero " + linea);	}
//			syso
			System.out.println("Carga de info V33");
			conver.loadInfoV33(linea);
			// AMDA termina fucnion prueba

			if (tokens[0].equals(conver.getTags()._CONTROLCFD)) 
			{
				
				
				if (conver.getTags().isComprobante) 
				{
					String temp = Util.tags("", conver.getPila());
					out.write(temp.getBytes("UTF-8"));
					endCONCEPTOS(out);
					endTAGS(out);
					
					end(cont, incidencia, numberLineCFD, nameFile, out,
							cfdBeans, date, 1);
					// aqui cierra el xml con la bandera
					flagNuevo = true;
				}

				startLine = "" + numberLineCFD;
				endLine = null;
				conver.setTags(new TagsXML());
				conver.getTags().isComprobante = true;
				// establece valores para el objeto conver, limpia el objetos
				// TagsXML
				conver.set(tokens, numberLineCFD);
							
				
			} else if (tokens[0].equals(conver.getTags()._CFD)) {
				out.write(conver.fComprobante(tokens, date, numberLineCFD, getCampos22()));
			} else if (tokens[0].equals(conver.getTags()._EMISOR)) {
				byte [] arrayEmisor = conver.emisor(tokens, lstFiscal, numberLineCFD, getCampos22());
				String str = new String (arrayEmisor,"UTF-8");
				//System.out.println("arrayEmisor: " + str);
				//out.write(conver.emisor(tokens, lstFiscal, numberLineCFD, getCampos22()));
				out.write(arrayEmisor);
			} else if (tokens[0].equals(conver.getTags()._RECEPTOR)) {
//				out.write(conver.receptor(tokens, numberLineCFD));
			} else if (tokens[0].equals(conver.getTags()._DOMICILIO)) {
				out.write(conver.receptor(conver.getTags().lineaAnteriorTokens, conver.getTags().contCFDAnterior));
				
				out.write(conver.domicilio(tokens, numberLineCFD));
				this.beginCONCEPTOS(out);
			} else if (tokens[0].equals(conver.getTags()._CONCEPTO)) {
				//System.out.println("LineaConcepto Genera AMDA:  " + tokens);
				conver.getTags().lineaAnteriorConceptoTokens = tokens;
				conver.getTags().contCFDAnteriorConcepto = numberLineCFD;
				String tokenC = "tokenC";
				String numberLineCFDC = "numberLineCFDC";
//				conver.getTags().numControl = 0;
				conver.getTags().numControl = conver.getTags().numControl + 1;
				//System.out.println("Num AMDA:  " + conver.getTags().numControl);
				tokenC = tokenC + conver.getTags().numControl ;
				numberLineCFDC = numberLineCFDC + conver.getTags().numControl;
				//System.out.println("TokenC AMDA:  " + tokenC);
				//System.out.println("numberLineCFDC AMDA:  " + numberLineCFDC);
				
				conver.getTags().mapConcep.put(tokenC, conver.getTags().lineaAnteriorConceptoTokens);
				//System.out.println("Saliendo de CONECPTO AMDA  " + conver.getTags().mapConcep.size());
				conver.getTags().mapConcepL.put(numberLineCFDC, conver.getTags().contCFDAnteriorConcepto);
				//System.out.println("Saliendo de CONECPTOL AMDA  " + conver.getTags().mapConcepL.size());

//				out.write(conver.concepto(tokens, numberLineCFD));
			} else if (tokens[0].equals(conver.getTags()._INFOADUANERA)) {
				//System.out.println("Saliendo de InfoAduanera AMDA  ");
				out.write(conver.infoAduanera(tokens, date, numberLineCFD));
			} else if (tokens[0].equals(conver.getTags()._PREDIAL)) {
				out.write(conver.predial(tokens, numberLineCFD));
			} else if (tokens[0].equals(conver.getTags()._PARTE)) {
				out.write(conver.parte(tokens, numberLineCFD));
			} else if (tokens[0].equals(conver.getTags()._CCONCEPTO)) {

				out.write(conver.complementoConcepto(tokens, numberLineCFD));
			} else if (tokens[0].equals(conver.getTags()._IMPUESTOS)) {
				conver.getTags().lineaAnteriorImpuestoTokens = tokens;
				conver.getTags().contCFDAnteriorImpuesto = numberLineCFD;
				if(!conver.getTags().tipoComprobante.equalsIgnoreCase("T") || !conver.getTags().tipoComprobante.equalsIgnoreCase("P")){
//					out.write(conver.impuestos(tokens, numberLineCFD));
				}
			} else if (tokens[0].equals(conver.getTags()._RETENCION)) {
				conver.getTags().lineaAnteriorRetencionTokens = tokens;
				conver.getTags().contCFDAnteriorRetencion = numberLineCFD;
				if(!conver.getTags().tipoComprobante.equalsIgnoreCase("T") || !conver.getTags().tipoComprobante.equalsIgnoreCase("P")){
//					this.beginRETENCIONES(out);
//					out.write(conver.retenciones(tokens, numberLineCFD));
				}
			} else if (tokens[0].equals(conver.getTags()._TRASLADO)) {
				//System.out.println("MAP CONCEP SIZE: " + conver.getTags().mapConcep.size());
				String tokenCTr = "";
				String numberLineCFDCTr = "" ;
				Integer numTr = 0;
				
				for(int i=0; i<conver.getTags().mapConcep.size(); i++){
					tokenCTr = "tokenC";
					numberLineCFDCTr = "numberLineCFDC";
					
					numTr = numTr + 1;
					System.out.println("NumTr AMDA:  " + numTr);
					tokenCTr = tokenCTr + numTr ;
					numberLineCFDCTr = numberLineCFDCTr + numTr;
					System.out.println("TokenCTr AMDA:  " + tokenCTr);
					System.out.println("numberLineCFDCTr AMDA:  " + numberLineCFDCTr);
					
					String[] tokenKe = (String[])conver.getTags().mapConcep.get(tokenCTr);
					Long valMap = conver.getTags().mapConcepL.get(numberLineCFDCTr);
//			        Long valMapLongLong = Long.parseLong(valMap);
					out.write(conver.concepto(tokenKe, valMap));
				}
				
				if(!conver.getTags().tipoComprobante.equalsIgnoreCase("T") || !conver.getTags().tipoComprobante.equalsIgnoreCase("P")){
					out.write(conver.impuestos(conver.getTags().lineaAnteriorImpuestoTokens, conver.getTags().contCFDAnteriorImpuesto));
					/*SMS-Comentado por agregar nodos a impuestos
					this.beginRETENCIONES(out);
					out.write(conver.retenciones(conver.getTags().lineaAnteriorRetencionTokens, conver.getTags().contCFDAnteriorRetencion));
					
					this.endRETENCIONES(out);
					this.beginTRASLADOS(out);
					out.write(conver.traslados(tokens, this.lstIva, numberLineCFD));
					*/
				}
			} else if (tokens[0].equals(conver.getTags()._FACTORAJE)) {
				this.lstFactoraje.add(conver.factoraje(tokens, numberLineCFD));
			} else if(tokens[0].equals(conver.getTags()._CFDIREL)) {
				conver.getInfoCfdiRelacionado(linea);
			}
			
			conver.getTags().lineaAnteriorTokens = tokens;
			conver.getTags().contCFDAnterior = numberLineCFD;
		} 
		catch (Exception e) 
		{
			logger.error(e.getLocalizedMessage().replace("ORA-", "ORACLE-"), e);
			throw new Exception(e.getMessage(), e);
		}

		return flagNuevo;
	}

	/**
	 * 
	 * @param out
	 * @throws IOException
	 */
	public void beginCONCEPTOS(ByteArrayOutputStream out) throws IOException {
		out.write("\n<cfdi:Conceptos>".getBytes("UTF-8"));
		conver.getTags().isConceptos = true;
	}

	/**
	 * 
	 * @param out
	 * @throws IOException
	 */
	public void endCONCEPTOS(ByteArrayOutputStream out) throws IOException {
		if (conver.getTags().isConceptos) {
			out.write("\n</cfdi:Conceptos>".getBytes("UTF-8"));
		}
	}

	/**
	 * 
	 * @param out
	 * @throws IOException
	 */
	public void beginRETENCIONES(ByteArrayOutputStream out) throws IOException {
		if (!conver.getTags().isRetenciones) {
			out.write("\n<cfdi:Retenciones>".getBytes("UTF-8"));
			conver.getTags().isRetenciones = true;
		}
	}

	/**
	 * 
	 * @param out
	 * @throws IOException
	 */
	public void endRETENCIONES(ByteArrayOutputStream out) throws IOException {
		if (conver.getTags().isRetenciones) {
			out.write("\n</cfdi:Retenciones>".getBytes("UTF-8"));
			conver.getTags().isRetenciones = false;
		}
	}

	/**
	 * 
	 * @param out
	 * @throws IOException
	 */
	public void beginTRASLADOS(ByteArrayOutputStream out) throws IOException {
		if (!conver.getTags().isTralados) {
			out.write("\n<cfdi:Traslados>".getBytes("UTF-8"));
			conver.getTags().isTralados = true;
		}
	}

	/**
	 * 
	 * @param out
	 * @throws IOException
	 */
	public void endTRASLADOS(ByteArrayOutputStream out) throws IOException {
		if (conver.getTags().isTralados) {
			out.write("\n</cfdi:Traslados>".getBytes("UTF-8"));
			conver.getTags().isTralados = false;
		}
	}

	/**
	 * 
	 * @param out
	 * @throws IOException
	 */
	public void endTAGS(ByteArrayOutputStream out) throws IOException {
		this.endRETENCIONES(out);
		this.endTRASLADOS(out);
		if (conver.getTags().isImpuestos
				&& (!conver.getTags().isTralados && !conver.getTags().isRetenciones)) {
			out.write("\n</cfdi:Impuestos>".getBytes("UTF-8"));
			conver.getTags().isImpuestos = false;
		} else if (!conver.getTags().isImpuestos
				&& (!conver.getTags().isTralados && !conver.getTags().isRetenciones)) {
			out.write("\n<cfdi:Impuestos/>".getBytes("UTF-8"));
		}
	}

	/**
	 * 
	 * @param out
	 * @throws IOException
	 */
	private void begin(ByteArrayOutputStream out) throws IOException {
		lstFactoraje = new ArrayList<byte[]>();
		out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>".getBytes("UTF-8"));
//		conver.getTags().mapConcep = new HashMap<String, String[]>();
//		conver.getTags().mapConcepL = new HashMap<String, Long>();
	}

	/**
	 * TAG de Inicio de la Addenda de Santander
	 * 
	 * @param out
	 * @throws IOException
	 */
	private void beginAddenda(ByteArrayOutputStream out) throws IOException {
		out.write("\n<cfdi:Addenda>".getBytes("UTF-8"));
		out.write(conver.getTags().addenda.getBytes("UTF-8"));
		this.beginInformacionPago(out);
		this.beginInformacionEmision(out);
		this.beginCampo("Moneda", conver.getTags().TIPO_MONEDA, out);
		this.beginCampo("Tipo Cambio", conver.getTags().TIPO_CAMBIO, out);
		if (this.conver.getTags().isDescriptionTASA) {
			this.beginCampo("Descripcion IVA",
					conver.getTags().DESCRIPTION_TASA, out);
		}

		if (conver.getTags().TIPO_FORMATO.equals("2")) {
			this.beginCampo("FAC_HORA", conver.getTags().FACTORAJE_HORA, out);
			this.beginCampo("FAC_TIPO", conver.getTags().FACTORAJE_TIPO, out);
			this.beginCampo("FAC_SVN", conver.getTags().FACTORAJE_SVN, out);
			this.beginCampo("FAC_SPB", conver.getTags().FACTORAJE_SPB, out);
			this.beginCampo("FAC_SPF", conver.getTags().FACTORAJE_SPF, out);
			this.beginCampo("FAC_SID", conver.getTags().FACTORAJE_SID, out);
			this.beginCampo("FAC_LCI", conver.getTags().FACTORAJE_LCI, out);
			this.beginCampo("FAC_LIVA", conver.getTags().FACTORAJE_LIVA, out);
			this.beginCampo("FAC_COMISION",
					conver.getTags().FACTORAJE_COMISION, out);
			this.beginCampo("FAC_LETRAS", conver.getTags().FACTORAJE_LETRAS,
					out);
		}
		this.beginDomicilioEmisor(out);
		this.beginDomicilioReceptor(out);
	}

	/**
	 * 
	 * @param campo
	 * @param valor
	 * @param out
	 * @throws IOException
	 */
	private void beginCampo(String campo, String valor,
			ByteArrayOutputStream out) throws IOException {
		if (!Util.isNullEmpty(valor)) {
			String temp = "\r\n<as:CampoAdicional campo=\"" + campo
					+ "\" valor=\"" + valor + "\" />";
			out.write(temp.getBytes("UTF-8"));
		}
	}

	/**
	 * 
	 * @param out
	 * @throws IOException
	 */
	private void beginInformacionPago(ByteArrayOutputStream out)
			throws IOException {
		String temp = "";
		temp += Util.isNullEmpity(conver.getTags().NUM_PROVEEDOR,
				"numProveedor");
		temp += Util.isNullEmpity(conver.getTags().ORDEN_COMPRA, "ordenCompra");
		temp += Util.isNullEmpity(conver.getTags().NOMBRE_BENIFICIARIO,
				"nombreBeneficiario");
		temp += Util.isNullEmpity(conver.getTags().INSTITUCION_RECEPTORA,
				"institucionReceptora");
		temp += Util
				.isNullEmpity(conver.getTags().CTA_DEPOSITO, "numeroCuenta");
		temp += Util.isNullEmpity(conver.getTags().EMAIL, "email");
		if (temp.length() > 0) {
			temp = "\n<as:InformacionPago " + temp + "/>";
			out.write(temp.getBytes("UTF-8"));
		}
	}

	/**
	 * 
	 * @param out
	 * @throws IOException
	 */
	private void beginInformacionEmision(ByteArrayOutputStream out)
			throws IOException {
		String temp = "";
		temp += Util.isNullEmpity(conver.getTags().EMISION_CODIGO_CLIENTE,
				"codigoCliente");
		temp += Util
				.isNullEmpity(conver.getTags().EMISION_CONTRATO, "contrato");
		temp += Util.isNullEmpity(conver.getTags().EMISION_PERIODO, "periodo");
		temp += Util.isNullEmpity(conver.getTags().EMISION_CENTRO_COSTOS,
				"centroCostos");
		temp += Util.isNullEmpity(conver.getTags().EMISION_FOLIO_INTERNO,
				"folioInterno");
		temp += Util.isNullEmpity(conver.getTags().EMISION_CLAVE_SANTANDER,
				"claveSantander");
		if (temp.length() > 0) {
			temp = "\n<as:InformacionEmision " + temp + " >";
			out.write(temp.getBytes("UTF-8"));
			this.beginInformacionFactoraje(out);
			this.endInformacionEmision(out);
		}
	}

	/**
	 * 
	 * @param out
	 * @throws IOException
	 */
	private void endInformacionEmision(ByteArrayOutputStream out)
			throws IOException {
		String temp = "</as:InformacionEmision>";
		out.write(temp.getBytes("UTF-8"));
	}

	/**
	 * 
	 * @param out
	 * @throws IOException
	 */
	private void beginInformacionFactoraje(ByteArrayOutputStream out)
			throws IOException {
		for (byte[] fact : this.lstFactoraje) {
			out.write(fact);
		}
	}

	/**
	 * 
	 * @param out
	 * @throws IOException
	 */
	private void endAddenda(ByteArrayOutputStream out) throws IOException {
		out.write("\n</as:AddendaSantanderV1>".getBytes("UTF-8"));
		out.write("\n</cfdi:Addenda>".getBytes("UTF-8"));
	}

	/**
	 * 
	 * @param out
	 * @throws IOException
	 */
	private void beginDomicilioReceptor(ByteArrayOutputStream out)
			throws IOException {
		String temp = "";
		if (conver.getTags()._Calle.split("=").length > 1 && conver.getTags()._Calle.split("=")[1].trim().equals("\"\"")) {
			temp+= conver.getTags()._Calle;
		} else {
			temp += " Calle=\"\" ";
		}
		
		
		//temp+= conver.getTags()._NoExterior;
		if (conver.getTags()._NoExterior.split("=").length > 1 && conver.getTags()._NoExterior.split("=")[1].trim().equals("\"\"")) {
			temp+= conver.getTags()._NoExterior;
		} else {
			temp += " NoExterior=\"\" ";
		}
		
		//temp+= conver.getTags()._NoInterior;
		if (conver.getTags()._NoInterior.split("=").length > 1 && conver.getTags()._NoInterior.split("=")[1].trim().equals("\"\"")) {
			temp+= conver.getTags()._NoInterior;
		}else {
			temp += " NoInterior=\"\" ";
		}
		
		//temp+= conver.getTags()._Colonia;
		if (conver.getTags()._Colonia.split("=").length > 1 && conver.getTags()._Colonia.split("=")[1].trim().equals("\"\"")) {
			temp+= conver.getTags()._Colonia;
		}else {
			temp += " Colonia=\"\" ";
		}
		
		//temp+= conver.getTags()._Localidad;
		if (conver.getTags()._Localidad.split("=").length > 1 && conver.getTags()._Localidad.split("=")[1].trim().equals("\"\"")) {
			temp+= conver.getTags()._Localidad;
		}else {
			temp += " Localidad=\"\" ";
		}
		
		//temp+= conver.getTags()._Referencia;
		if (conver.getTags()._Referencia.split("=").length > 1 && conver.getTags()._Referencia.split("=")[1].trim().equals("\"\"")) {
			temp+= conver.getTags()._Referencia;
		}
		else {
			temp += " Referencia=\"\" ";
		}
		//temp+= conver.getTags()._Municipio;
		if (conver.getTags()._Municipio.split("=").length > 1 && conver.getTags()._Municipio.split("=")[1].trim().equals("\"\"")) {
			temp+= conver.getTags()._Municipio;
		} else {
			temp += " Municipio=\"\" ";
		}
		
		//temp+= conver.getTags()._Estado;
		if (conver.getTags()._Estado.split("=").length > 1 && conver.getTags()._Estado.split("=")[1].trim().equals("\"\"")) {
			temp+= conver.getTags()._Estado;
		}else {
			temp += " Estado=\"\" ";
		}
		
		//temp+= conver.getTags()._Pais;
		if (conver.getTags()._Pais.split("=").length > 1 && conver.getTags()._Pais.split("=")[1].trim().equals("\"\"")) {
			temp+= conver.getTags()._Pais;
		}else {
			temp += " Pais=\"\" ";
		}
		
		//temp+= conver.getTags()._CodigoPostal;
		if (conver.getTags()._CodigoPostal.split("=").length > 1 && conver.getTags()._CodigoPostal.split("=")[1].trim().equals("\"\"")) {
			temp+= conver.getTags()._CodigoPostal;
		}else {
			temp += " CodigoPostal=\"\" ";
		}
		
		
		
		
		
		System.out.println("Domicilio Receptor"+temp);
		if (temp.trim().length() > 0 && !temp.trim().equalsIgnoreCase("")) {
			temp = "\n<as:DomicilioReceptor " + temp + "/>";
			out.write(temp.getBytes("UTF-8"));
		} 
	}

	private void beginDomicilioEmisor(ByteArrayOutputStream out) throws IOException {
		String temp = "";
		temp+=Util.isNullEmpity(Util.isNullEmpity(conver.getTags().fis.getAddress().getStreet().toUpperCase()), "Calle");
		temp+=Util.isNullEmpity(conver.getTags().fis.getAddress().getExternalNumber(), "NoExterior");
		temp+=Util.isNullEmpity(conver.getTags().fis.getAddress().getInternalNumber(), "NoInterior");
		temp+=Util.isNullEmpity(conver.getTags().fis.getAddress().getNeighborhood().toUpperCase(), "Colonia");
		// Localidad vacio
		temp+=Util.isNullEmpity(conver.getTags().fis.getAddress().getReference(), "Referencia");
		temp+=Util.isNullEmpity(Util.isNullEmpity(conver.getTags().fis.getAddress().getRegion().toUpperCase()), "Municipio");
		temp+=Util.isNullEmpity(Util.isNullEmpity(conver.getTags().fis.getAddress().getState().getName().toUpperCase()),
				"Estado");
		temp+=Util.isNullEmpity(conver.getTags().fis.getAddress().getState().getCountry().getName().toUpperCase(), "Pais");
		temp+=Util.isNullEmpity(Util.isNullEmpity(conver.getTags().fis.getAddress().getZipCode()), "CodigoPostal");
		temp+=Util.isNullEmpity(conver.getTags().fis.getAddress().getCity(), "Ciudad");
		System.out.println("Domicilio Emisor" + temp);
		if (temp.length() > 0) {
			temp = "\n<as:DomicilioEmisor " + temp + "/>";
			out.write(temp.getBytes("UTF-8"));
		}
	}
	
	/**
	 * 
	 * @param cont
	 * @param incidencia
	 * @param numberLineCFD
	 * @param nameFile
	 * @param out
	 * @param cfdBeans
	 * @param creationDate
	 * @param decremento
	 * @throws IOException
	 */
	private void end(int cont, FileOutputStream incidencia, long numberLineCFD,
			String nameFile, ByteArrayOutputStream out, List<CfdBean> cfdBeans,
			Date creationDate, int decremento) throws IOException 
	{
		beginAddenda(out);
		endAddenda(out);
		//No aplica para factoraje CDRI Relacionado
//		out=conver.cfdiRelacionado(out);
		out.write("\n</cfdi:Comprobante>".getBytes("UTF-8"));
		// se ha finalizado el xml
		conver.getTags().isComprobante = false;
		// hasta aqui genera los tags del xml, 1era pasada sin validacion

		try 
		{
			endLine = "" + (numberLineCFD - decremento);
			
			if (conver.getTags().isFormat) 
			{
				StringBuilder numberLines = new StringBuilder();
				for (String error : conver.getDescriptionFormat()) 
				{
					numberLines.append(error);
					numberLines.append(" ");
				}
				throw new Exception("Estructura Incorrecta "
						+ numberLines.toString());
			}

			/*Validaciones 3.3*/
			UtilCatalogos.lstErrors = new StringBuffer();
			Document doc = stringToDocument(out.toString());
			Element root = doc.getDocumentElement();
			UtilCatalogos.evaluateNodesError(root);
			if(!UtilCatalogos.lstErrors.toString().isEmpty()){
				throw new Exception(UtilCatalogos.lstErrors.toString());
			}
			
			
			
			BigDecimal diferencia = BigDecimal.ONE;
			
			/*Validacion de IVA a .05*/
			System.out.println("antesValidarTotal: " + UtilCatalogos.convertDocumentXmlToString(doc));
			Boolean ivaOk = true;
			BigDecimal compIva =BigDecimal.valueOf(UtilCatalogos.getDoubleByExpression(doc, "//Comprobante/Impuestos/@TotalImpuestosTrasladados"));
			BigDecimal opIva = UtilCatalogos.getDocTotalIVA(doc);
			if (opIva.compareTo(compIva) > 0) 
				diferencia = opIva.subtract(compIva);
			else
				diferencia = compIva.subtract(opIva);
			
			if(diferencia.compareTo(new BigDecimal("0.05")) < 0) {
	        	UtilCatalogos.setValueOnDocumentElement(doc,  "//Comprobante/Impuestos/@TotalImpuestosTrasladados", opIva.toString());
	        	ivaOk = false;
	        }
			
			
			/*Validacion de subtotal a 0.05*/
			diferencia = BigDecimal.ONE;
			Boolean subTotalOK = true;
			BigDecimal compSubTotal =BigDecimal.valueOf(UtilCatalogos.getDoubleByExpression(doc, "//Comprobante/@SubTotal"));
			BigDecimal opSubTotal = UtilCatalogos.getDocSubtotal(doc);
			
			
			System.out.println("SubTotal: " + compSubTotal + " " + opSubTotal);
			if (opSubTotal.compareTo(compSubTotal) > 0) 
				diferencia = opSubTotal.subtract(compSubTotal);
			else
				diferencia = compSubTotal.subtract(opSubTotal);
			
			if(diferencia.compareTo(new BigDecimal("0.05")) < 0) {
	        	UtilCatalogos.setValueOnDocumentElement(doc,  "//Comprobante/@SubTotal", opSubTotal.toString());
	        	subTotalOK = false;
	        }
			
			
			/*Validacio  de total a 0.05*/
			BigDecimal totalOper = BigDecimal.ZERO;
	        
	        if (!ivaOk || !subTotalOK) {
	        		totalOper = opSubTotal.add(opIva);
	        		UtilCatalogos.setValueOnDocumentElement(doc,  "//Comprobante/@Total", totalOper.toString());
	        }
	        
	        diferencia = BigDecimal.ONE;
			
			BigDecimal compTotal = BigDecimal.valueOf(UtilCatalogos.getDoubleByExpression(doc, "//Comprobante/@Total"));
			        
	
	        totalOper = UtilCatalogos.getDocTotalOper(doc);
	        
	        System.out.println("asjg: " + compTotal + " " +totalOper + " bandera: " + ivaOk + " / " + subTotalOK);
	        if(totalOper.compareTo(compTotal) > 0){
				diferencia = totalOper.subtract(compTotal);
			}else{
				diferencia = compTotal.subtract(totalOper);
			}
	        
	        if(diferencia.compareTo(new BigDecimal("0.05")) < 0) {
	        	UtilCatalogos.setValueOnDocumentElement(doc,  "//Comprobante/@Total", totalOper.toString());
	        }
	        	
	        
			String errors = UtilCatalogos.validateCfdiDocument(doc,conver.getTags().decimalesMoneda);
			if (errors != null && !errors.isEmpty()) {
				throw new Exception(errors);
			}
			out = UtilCatalogos.convertStringToOutpuStream(UtilCatalogos.convertDocumentXmlToString(doc));
			/*Fin Validaciones 3.3*/
			
			// si no existe la entidad fiscal ya no valida y no lo checa
			if (!conver.getTags().isEntidadFiscal) 
			{
				throw new Exception("No existe la entidad fiscal con R.F.C.: "
						+ conver.getTags().EMISION_RFC);
			}
			// aqui tendria que haber un contador de los cfd generados, y
			// despues solicitarlos los folios y asignarlos
			// en esta parte ya se valido que exista la entidad fiscal

			// <objeto out, serieFiscalId, fiscalEntityId> para tener los
			// objetos relacionados y remover objeto conver
			// esto ya no va
			// aqui se pregunta que hay un folio activo, si asi es, se settea al
			String serieFiscalId = conver.getTags().SERIE_FISCAL_CFD;
			long fiscalEntityId = conver.getTags().fis.getId();
			SealCertificate certificate = null;
			Calendar cal = Calendar.getInstance();
			for (SealCertificate obj : lstSeal) 
			{
				if (obj.getFiscalEntity().getId() == fiscalEntityId) 
				{
					if (obj.getStartOfValidity().before(cal.getTime())) 
					{
						if (obj.getEndOfValidity().after(cal.getTime())) 
						{
							certificate = obj;
							break;
						}
					}
				}
			}
			
			if (certificate == null) 
			{
				throw new Exception(
						"No existe certificado para la entidad fiscal "
								+ conver.getTags().fis.getFiscalName());
			}
			
			CfdBean cfdBean = new CfdBean(out, serieFiscalId, fiscalEntityId, certificate, creationDate);
			conver.getTags().NUM_CERTIFICADO = cfdBean.getSealCertificate().getSerialNumber();
			cfdBean.setProviderNumber(conver.getTags().NUM_PROVEEDOR);
			cfdBean.setDepositAccount(conver.getTags().CTA_DEPOSITO);
			cfdBean.setPurchaseOrder(conver.getTags().ORDEN_COMPRA);
			cfdBean.setEmail(conver.getTags().EMAIL);
			cfdBean.setTotalReport(conver.getTags().TOTAL_REPORTE);
			cfdBean.setLengthString(conver.getTags().LONGITUD);
			cfdBean.setIvaTotalReport(conver.getTags().IVA_TOTAL_REPORTE);
			cfdBean.setTypeCurrency(conver.getTags().TIPO_MONEDA);
			cfdBean.setExchangeRate(conver.getTags().TIPO_CAMBIO);
			cfdBean.setTypeCFD(conver.getTags().CFD_TYPE);
			cfdBean.setSubtotalMN(conver.getTags().SUBTOTAL_MN);
			cfdBean.setTotalMN(conver.getTags().TOTAL_MN);
			cfdBean.setLstCustoms(conver.getTags().lstCustoms);
			cfdBean.setStartLine(startLine);
			cfdBean.setEndLine(endLine);
			cfdBean.setReceptorRFC(conver.getTags().RECEPCION_RFC);
			cfdBean.setFormatType(conver.getTags().TIPO_FORMATO);
			cfdBean.setLengthString(conver.getTags().LONGITUD);
			cfdBean.setBroadcastRFC(conver.getTags().EMISION_RFC);
			cfdBean.setContract(conver.getTags().EMISION_CONTRATO);
			cfdBean.setCustomerCode(conver.getTags().EMISION_CODIGO_CLIENTE);
			cfdBean.setCostCenter(conver.getTags().EMISION_CENTRO_COSTOS);
			cfdBean.setPeriod(conver.getTags().EMISION_PERIODO);
			cfdBean.setKeySantander(conver.getTags().EMISION_CLAVE_SANTANDER);
			cfdBean.setInnerSheet(conver.getTags().EMISION_FOLIO_INTERNO);
			cfdBean.setCertificateNumber(conver.getTags().NUM_CERTIFICADO);
			cfdBean.setDateCFD(conver.getTags().FECHA_CFD);
			cfdBean.setSerieCFD(conver.getTags().SERIE_FISCAL_CFD);
			cfdBean.setUnidadMedida(conver.getTags().UNIDAD_MEDIDA);
			cfdBean.setMetodoPago(conver.getTags().METODO_PAGO);
			cfdBean.setLugarExpedicion(conver.getTags().LUGAR_EXPEDICION);
			cfdBean.setRegimenFiscal(conver.getTags().REGIMEN_FISCAL);
			cfdBean.setMoneda(conver.getTags().TIPO_MONEDA);
			cfdBean.setTipoCambio(conver.getTags().TIPO_CAMBIO);
			cfdBean.setFormaPago(conver.getTags().FORMA_PAGO);			
			
			cfdBeans.add(cfdBean);
		} 
		catch (Exception ex) 
		{
			logger.info(out.toString());
			logger.info(ex);
			logger.info(ex.getLocalizedMessage());
			msgError = ex.getMessage();

			String typeIncidence = "ERROR";
			if (msgError != null
					&& msgError
							.contains("The transaction has been rolled back")) {
				typeIncidence = "WARNING";
			}

			try 
			{
				this.fileINCIDENCIA(ex, incidencia, nameFile,
						typeIncidence, this.startLine, this.endLine, 
						conver.getTags().EMISION_RFC, conver.getTags().EMISION_CONTRATO, 
						conver.getTags().EMISION_CODIGO_CLIENTE, conver.getTags().EMISION_PERIODO);
			} 
			catch (Exception e) 
			{
				lstCFDIncidence.add(this.setCFDIncidence(msgError,
						typeIncidence, nameFile, this.startLine, this.endLine));
			} 
			finally 
			{
				conver.setDescriptionFormat(null);
				logger.error(ex.getLocalizedMessage().replace("ORA-", "ORACLE-"), ex);
			}
		} 
		finally 
		{	cont += 1;	}
	}

	/**
	 * 
	 * @param msgError
	 * @param typeIncidence
	 * @param nameFile
	 * @param startLine
	 * @param endLine
	 * @return
	 */
	public CFDIssuedIn setCFDIncidence(String msgError, String typeIncidence,
			String nameFile, String startLine, String endLine) 
	{
		CFDIssuedIn cFDIssuedIncidence = new CFDIssuedIn();
		if (msgError != null) 
		{
			if (msgError.length() > sizeT) 
			{	cFDIssuedIncidence.setErrorMessage(msgError.substring(0, sizeT));	} 
			else 
			{	cFDIssuedIncidence.setErrorMessage(msgError);	}
		}
		cFDIssuedIncidence.setSourceFileName(nameFile);
		cFDIssuedIncidence.setAuthor("masivo");
		cFDIssuedIncidence.setComplement(typeIncidence);
		cFDIssuedIncidence.setCreationDate(Calendar.getInstance().getTime());
		cFDIssuedIncidence.setStartLine(startLine);
		cFDIssuedIncidence.setEndLine(endLine);
		return cFDIssuedIncidence;
	}

	/**
	 * 
	 * @param sello
	 * @param cadena
	 * @param salida
	 * @param nameFile
	 * @param cfdBean
	 * @param cfdIssued
	 * @throws IOException
	 */
	public void fileSALIDA(String sello, String cadena,
			FileOutputStream salida, String nameFile, CfdBean cfdBean,
			CFDIssued cfdIssued,
			String strFechaTimbrado, String strNoCertificadoSAT, String strSelloCFD, String strSelloSAT, String strVersion, 
			String strEmisorRFC, String strReceptorRFC, String strTotalZeros, Long newFolio) throws IOException 
	{
		this.setBD(nameFile, cfdBean, cfdIssued, newFolio);
		//int newFolio = cfdBean.getFolioRange().getActualFolio().intValue();
		
		/*
		String tempLinea = "CFD|" + cfdBean.getBroadcastRFC() + "|"
				+ cfdBean.getContract() + "|" + cfdBean.getCustomerCode() + "|"
				+ cfdBean.getCostCenter() + "|" + cfdBean.getPeriod() + "|"
				+ cfdBean.getKeySantander() + "|" + cfdBean.getInnerSheet()
				+ "|" + cfdBean.getFormatType() + "|" + cfdBean.getDateCFD()
				+ "|" + cfdBean.getSerieCFD() + "|"
				+ newFolio + "|"
				+ cfdBean.getFolioRange().getAuthorizationNumber().toString()
				+ "|" + cfdBean.getFolioRange().getYearOfAuthorization() + "|"
				+ cfdBean.getCertificateNumber() + "|" 
				+ cfdBean.getUnidadMedida() + "|" + cfdBean.getLugarExpedicion() + "|"  
				+ cfdBean.getMetodoPago() + "|" + cfdBean.getRegimenFiscal() + "|" + cfdBean.getMoneda() + "|" 
				+ cfdBean.getTipoCambio() + "|" + "\r\n";*/
		String tempLinea = "CFD|" + cfdBean.getBroadcastRFC() + "|"
				+ cfdBean.getContract() + "|" + cfdBean.getCustomerCode() + "|"
				+ cfdBean.getCostCenter() + "|" + cfdBean.getPeriod() + "|"
				+ cfdBean.getKeySantander() + "|" + cfdBean.getInnerSheet()
				+ "|" + cfdBean.getFormatType() + "|" + cfdBean.getDateCFD()
				+ "|" + cfdBean.getSerieCFD() + "|"
				+ newFolio + "|"
				+ ""
				+ "|" + "" + "|"
				+ cfdBean.getCertificateNumber() + "|" 
				+ cfdBean.getUnidadMedida() + "|" + cfdBean.getLugarExpedicion() + "|"  
				+ cfdBean.getFormaPago() + "|" + cfdBean.getRegimenFiscal() + "|" + cfdBean.getMoneda() + "|" 
				+ cfdBean.getTipoCambio() + "|" + "\r\n";
		salida.write(tempLinea.getBytes("UTF-8"));
		tempLinea = null;
		
		tempLinea = "CFDI|" + cfdIssued.getFolioSAT() + "|" + strFechaTimbrado  + "|" + strNoCertificadoSAT + "|" + "\r\n";
		salida.write(tempLinea.getBytes("UTF-8"));
		tempLinea = null;
		
		logger.info("longitud de la cadena: " + cfdBean.getLengthString());
		salida.write(Util.selloCadena(sello, "SELLO",
				(int) Double.parseDouble(cfdBean.getLengthString())));
	
		salida.write(Util.selloCadena(strSelloSAT, "SELLO_SAT",
				(int) Double.parseDouble(cfdBean.getLengthString())));
		
		//salida.write(Util.selloCadena(cadena, "CADENA",
		//		(int) Double.parseDouble(cfdBean.getLengthString())));
		
		salida.write(Util.selloCadena("||" + strVersion + "|" + cfdIssued.getFolioSAT() + "|" + strFechaTimbrado + "|" + strSelloSAT + "|" + strNoCertificadoSAT + "||", "CADENA_TIMBRE",
				(int) Double.parseDouble(cfdBean.getLengthString())));
		
		//tempLinea = "CODIGO_BIDIMENSIONAL|?re=" + strEmisorRFC + "&rr=" + strReceptorRFC  + "&tt=" + strTotalZeros + "&id=" + cfdIssued.getFolioSAT() + "\r\n";
		//salida.write(tempLinea.getBytes("UTF-8"));
		StringBuffer sbConcat = new StringBuffer("https://verificacfdi.facturaelectronica.sat.gob.mx/default.aspx");
		sbConcat.append("&id=").append(cfdIssued.getFolioSAT());
		sbConcat.append("&re=").append(strEmisorRFC);
		sbConcat.append("&rr=").append(strReceptorRFC);
		sbConcat.append("&tt=").append(strTotalZeros);
		sbConcat.append("&fe=").append(strSelloSAT.substring(strSelloSAT.length() - 8));
		salida.write(Util.selloCadena(sbConcat.toString(), "CODIGO_BIDIMENSIONAL", (int) Double.parseDouble(cfdBean.getLengthString())));
		tempLinea = null;	
	}
	/**
	 * 
	 * @param nameFile
	 * @param cfdBean
	 * @param cfdIssued
	 */
	public void setBD(String nameFile, CfdBean cfdBean, CFDIssued cfdIssued, Long newFolio) 
	{
		if (!Util.isNullEmpty(cfdBean.getContract())) 
		{	cfdIssued.setContractNumber(cfdBean.getContract());	}
		if (!Util.isNullEmpty(cfdBean.getCostCenter())) 
		{	cfdIssued.setCostCenter(cfdBean.getCostCenter());	}
		if (!Util.isNullEmpty(cfdBean.getCustomerCode())) 
		{	cfdIssued.setCustomerCode(cfdBean.getCustomerCode());	}
		if (!Util.isNullEmpty(cfdBean.getPeriod())) 
		{	cfdIssued.setPeriod(cfdBean.getPeriod());	}

		cfdIssued.setAuthor("masivo");
		FiscalEntity fiscalEntity = new FiscalEntity();
		fiscalEntity.setId(cfdBean.getFiscalEntityId());
		cfdIssued.setFiscalEntity(fiscalEntity);
		cfdIssued.setTaxIdReceiver(cfdBean.getReceptorRFC());
		//cfdIssued.setFolioRange(cfdBean.getFolioRange());
		
		//int newFolio = cfdBean.getFolioRange().getActualFolio().intValue();
		
		cfdIssued.setFolioInterno(newFolio);
		
		cfdIssued.setFolio(null);
		
		cfdIssued.setFolioRange(null);
		
		cfdIssued.setDateOfIssuance(cfdIssued.getCreationDate());
				
		cfdIssued.setIva(Double.valueOf(cfdBean.getIvaTotalReport()));
		cfdIssued.setTotal(Double.valueOf(cfdBean.getTotalReport()));
		
		//Recalcular el subtotal en MN
		double subTotal = Double.valueOf(cfdBean.getTotalReport()) - Double.valueOf(cfdBean.getIvaTotalReport());
		cfdIssued.setSubTotal(subTotal);
				
		cfdIssued.setStatus(1);
		// cfdIssued.setSubTotal(cfdIssued.getTotal()- cfdIssued.getIva());
		
		
		
		cfdIssued.setFormatType(Integer.parseInt(cfdBean.getFormatType()));
		cfdIssued.setCfdType(cfdBean.getTypeCFD());
		cfdIssued.setStartLine(cfdBean.getStartLine());
		cfdIssued.setEndLine(cfdBean.getEndLine());
		cfdIssued.setSourceFileName(nameFile);
		if (cfdBean.getLstCustoms().size() > 0) 
		{	cfdIssued.setAddendumCustoms(cfdBean.getLstCustoms());	}
		
		logger.debug("Guardar CFDI");
		try{
			System.out.println("BD: Intento 1");
			cFDIssuedManager.update(cfdIssued);
		}catch(Exception e1){
			try{
				System.out.println("BD: Intento 2");
				cFDIssuedManager.update(cfdIssued);
			}catch(Exception e2){
				try{
					System.out.println("BD: Intento 3");
					cFDIssuedManager.update(cfdIssued);
				}catch(Exception e3){
					try{
						System.out.println("BD: Intento 4");
						cFDIssuedManager.update(cfdIssued);
					}catch(Exception e4){
						try{
							System.out.println("BD: Intento 5");
							cFDIssuedManager.update(cfdIssued);
						}catch(Exception e5){
							try{
								System.out.println("BD: Intento 6");
								cFDIssuedManager.update(cfdIssued);
							}catch(Exception e6){
								try{
									System.out.println("BD: Intento 7");
									cFDIssuedManager.update(cfdIssued);
								}catch(Exception e7){
									try{
										System.out.println("BD: Intento 8");
										cFDIssuedManager.update(cfdIssued);
									}catch(Exception e8){
										try{
											System.out.println("BD: Intento 9");
											cFDIssuedManager.update(cfdIssued);
										}catch(Exception e9){
											try{
												System.out.println("BD: Intento 10");
												cFDIssuedManager.update(cfdIssued);
											}catch(Exception e10){
												try{
													System.out.println("BD: Intento 11");
													cFDIssuedManager.update(cfdIssued);
												}catch(Exception e11){
													try{
														System.out.println("BD: Intento 12");
														cFDIssuedManager.update(cfdIssued);
													}catch(Exception e12){
														try{
															System.out.println("BD: Intento 13");
															cFDIssuedManager.update(cfdIssued);
														}catch(Exception e13){
															try{
																System.out.println("BD: Intento 14");
																cFDIssuedManager.update(cfdIssued);
															}catch(Exception e14){
																try{
																	System.out.println("BD: Intento 15");
																	cFDIssuedManager.update(cfdIssued);
																}catch(Exception e15){
																	try{
																		System.out.println("BD: Intento 16");
																		cFDIssuedManager.update(cfdIssued);
																	}catch(Exception e16){
																		try{
																			System.out.println("BD: Intento 17");
																			cFDIssuedManager.update(cfdIssued);
																		}catch(Exception e17){
																			try{
																				System.out.println("BD: Intento 18");
																				cFDIssuedManager.update(cfdIssued);
																			}catch(Exception e18){
																				try{
																					System.out.println("BD: Intento 19");
																					cFDIssuedManager.update(cfdIssued);
																				}catch(Exception e19){
																					try{
																						System.out.println("BD: Intento 20");
																						cFDIssuedManager.update(cfdIssued);
																					}catch(Exception e20){
																						e20.printStackTrace();
																						System.out.println("ERROR BD- " + e20.getMessage());
																					}
																				}
																			}
																		}
																	}
																}
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}		
	}

	/**
	 * Inserta la incidencia en la base de datos
	 * 
	 * @param cFDIssuedIncidence
	 */
	public void setBDIncidence(CFDIssuedIn cFDIssuedIncidence) {
		logger.debug("Guardar CFD Incidencia");
		cFDIssuedIncidenceManager.update(cFDIssuedIncidence);
		cFDIssuedIncidence = null;
	}

	/**
	 * 
	 * @param e
	 * @param incidencia
	 * @param nameFile
	 * @param typeIncidence
	 * @param startLine
	 * @param endLine
	 * @param rfc
	 * @param contrato
	 * @param codigoCliente
	 * @param periodo
	 * @throws IOException
	 */
	public void fileINCIDENCIA(Exception e, FileOutputStream incidencia, String nameFile,
		String typeIncidence, String startLine, String endLine, String rfc, String contrato, 
		String codigoCliente, String periodo)
			throws IOException {
		this.fileINCIDENCIA(e.getMessage(), incidencia, nameFile, typeIncidence, startLine, endLine, rfc, contrato, codigoCliente, periodo);
	}
	
	public void fileINCIDENCIA(String e, FileOutputStream incidencia, String nameFile,
			String typeIncidence, String startLine, String endLine, String rfc, String contrato, 
			String codigoCliente, String periodo)
				throws IOException 
	{
		String temp = rfc + "|" + contrato+ "|"
				+ codigoCliente + "|" + periodo + "|" + "\r\n";
		incidencia.write(temp.getBytes());
		incidencia.write("Se presentaron los siguientes errores al validar la estructura del comprobante: \r\n".getBytes());
		if(typeIncidence.equals("ERROR"))
		{	temp = "Error: " + e + "\r\n";	} 
		else 
		{	temp = "Warning: " + e + "\r\n";	}
		temp += "Inicio de CFD: " + startLine + "\r\n";
		incidencia.write(temp.getBytes("UTF-8"));
		
		
		
		
			/*incidencia.write(temp.getBytes("UTF-8"));
			incidencia
					.write("Se presentaron los siguientes errores al validar la estructura del comprobante: \r\n"
							.getBytes("UTF-8"));
			logger.info("mLlovera: error:"+incidencia.toString());
			if(typeIncidence.equals("ERROR"))
			{	temp = "Error: \n";	} 
			else 
			{	temp = "Warning: \n";	}
			if (e != null && !e.isEmpty()) {
				logger.info("mLlovera: incident:" + e);
				for (String err : e.split("@@-@@")) {
					logger.info("mLlovera: incidentList:" + err);
					incidencia.write((err+"\n").getBytes());
				}
			}
			temp = "Inicio de CFD: ".concat(startLine).concat("\r\n");
			logger.info("mLlovera: temp:"+temp);
			//temp = temp.replace("\n", System.getProperty("line.separator"));
			incidencia.write(temp.getBytes());*/
			temp = null;
			this.setBDIncidence(setCFDIncidence(e, typeIncidence,
					nameFile, startLine, endLine));
		}

	/**
	 * Copia un archivo en el path indicado
	 * 
	 * @param path
	 *            Path del nuevo archivo
	 * @param file
	 *            Archivo a copiar
	 * @throws IOException
	 */
	private void copy(String path, File file) throws IOException {
		FileCopyUtils.copy(file, new File(path));
	}

	/**
	 * 
	 * @param cfdBean
	 * @param folioRange
	 * @throws Exception
	 */
	//public void replacesOriginalString(CfdBean cfdBean, FolioRange folioRange)
	public void replacesOriginalString(CfdBean cfdBean, OpenJpa openJpa)
			throws Exception 
	{
		ByteArrayOutputStream outBW = new ByteArrayOutputStream();
		InputStreamReader in = new InputStreamReader(new ByteArrayInputStream(
				cfdBean.getBaosXml().toByteArray()), "UTF-8");
		OutputStreamWriter outW = new OutputStreamWriter(outBW, "UTF-8");
		BufferedReader bF = new BufferedReader(in);
		BufferedWriter bW = new BufferedWriter(outW);
		String line = null;
		boolean f1 = false;
		
		boolean f4 = false;
		boolean f5 = false;
		boolean f6 = false;
		while (bF.ready()) 
		{
			line = bF.readLine();
			logger.info(line);
			if (!f1) 
			{
				if (line.lastIndexOf(properties.getLblFOLIOCFD()) > 0) 
				{
					// Lo graba con uno antes
					//int newFolio = folioRange.getActualFolio().intValue();
					long newFolio = openJpa.getSequence_value();
					newFolio = newFolio - 1;
					line = line.replaceAll(properties.getLblFOLIOCFD(), String.valueOf(newFolio));
					f1 = true;
				}
			}
			
			
			if (!f4) 
			{
				if (line.lastIndexOf(properties.getLabelLugarExpedicion()) > 0) 
				{
					line = line.replaceAll(properties
							.getLabelLugarExpedicion(), conver.getTags().LUGAR_EXPEDICION);
					f4 = true;
				}
			}
			
			if (!f5) 
			{
				if (line.lastIndexOf(properties.getLabelMetodoPago()) > 0) 
				{
					line = line.replaceAll(properties
							.getLabelMetodoPago(), conver.getTags().METODO_PAGO);
					f5 = true;
				}
			}
			if (!f6) 
			{
				if (line.lastIndexOf(properties.getlabelFormaPago()) > 0) 
				{
					line = line.replaceAll(properties
							.getlabelFormaPago(), conver.getTags().FORMA_PAGO);
					f6 = true;
				}
			}
			bW.write(line);
		}
		bF.close();
		bW.close();
		cfdBean.setBaosXml(outBW);
	}

	
	public void replacesOriginalString(CfdBean cfdBean, String folio)
		    throws Exception
		  {
		    ByteArrayOutputStream outBW = new ByteArrayOutputStream();
		    InputStreamReader in = new InputStreamReader(new ByteArrayInputStream(
		      cfdBean.getBaosXml().toByteArray()), "UTF-8");
		    OutputStreamWriter outW = new OutputStreamWriter(outBW, "UTF-8");
		    BufferedReader bF = new BufferedReader(in);
		    BufferedWriter bW = new BufferedWriter(outW);
		    String line = null;
		    boolean f1 = false;
		    
		    boolean f4 = false;
		    boolean f5 = false;
		    boolean f6 = false;
		    while (bF.ready())
		    {
		      line = bF.readLine();
		      logger.info(line);
		      if (!f1)
		      {
		        if (line.lastIndexOf(properties.getLblFOLIOCFD()) > 0)
		        {




		          line = line.replaceAll(properties.getLblFOLIOCFD(), folio);
		          f1 = true;
		        }
		      }
		      

		      if (!f4)
		      {
		        if (line.lastIndexOf(properties.getLabelLugarExpedicion()) > 0)
		        {
		          line = line.replaceAll(properties
		            .getLabelLugarExpedicion(), conver.getTags().LUGAR_EXPEDICION);
		          f4 = true;
		        }
		      }
		      
		      if (!f5)
		      {
		        if (line.lastIndexOf(properties.getLabelMetodoPago()) > 0)
		        {
		          line = line.replaceAll(properties
		            .getLabelMetodoPago(), conver.getTags().METODO_PAGO);
		          f5 = true;
		        }
		      }
		      if (!f6)
		      {
		        if (line.lastIndexOf(properties.getlabelFormaPago()) > 0)
		        {
		          line = line.replaceAll(properties
		            .getlabelFormaPago(), conver.getTags().FORMA_PAGO);
		          f6 = true;
		        }
		      }
		      bW.write(line);
		    }
		    bF.close();
		    bW.close();
		    cfdBean.setBaosXml(outBW);
		  }
	
	
	public String putZeros(String str){
		String [] total = str.split("\\.");
		System.out.println("size: " + total.length);
		
		int nEnteros = total[0].length();
		int nDecimales = total[1].length();
		
		int nZerosEnteros = 10 - nEnteros;
		int nZerosDecimales = 6 - nDecimales;
		
		String strEnteros = "";
		String strDecimales = "";
		
		if(nZerosEnteros > 0){
			for(int nZero=0; nZero<nZerosEnteros; nZero++){
				strEnteros = strEnteros + "0";
			}
			strEnteros = strEnteros + total[0];
		}else{
			strEnteros =  total[0];
		}
		if(nZerosDecimales > 0){
			strDecimales = total[1];
			for(int nZero=0; nZero<nZerosDecimales; nZero++){
				strDecimales = strDecimales + "0";
			}
		}else{
			strDecimales = total[1];
		}
		return strEnteros + "." + strDecimales;
	}
	
	//Convierte de String a Document
	public Document stringToDocument(String strXML){
		Document domResultado = null;
		try{
			if (this.db == null){
				this.dbf = DocumentBuilderFactory.newInstance();
				this.db = this.dbf.newDocumentBuilder();
			}
			
			domResultado = this.db.parse(new InputSource(new StringReader(strXML)));	
		}catch(Exception e){
			System.out.println(e.getMessage());
			e.printStackTrace();
		}	
		return domResultado;
	}
	
	public StreamResult nodeToStreamResult(Node nodo){
		StreamResult sr = null;
		try{
			if (this.tx == null){
				this.tx = TransformerFactory.newInstance().newTransformer();
				this.tx.setOutputProperty(OutputKeys.INDENT, "yes");
			}
			sr = new StreamResult(new StringWriter());
			DOMSource sourceComprobante = new DOMSource(nodo);
			this.tx.transform(sourceComprobante, sr);	
		}catch(Exception e){
			System.out.println(e.getMessage());
			e.printStackTrace();
		}		
		
		return sr;
	}
	/**
	 * 
	 * @param cfdBeans
	 * @param incidencia
	 * @param salida
	 * @param numberLineCFD
	 * @param cont
	 * @param nameFile
	 */
	public ArrayList<CfdBean> finalize(List<CfdBean> cfdBeans, FileOutputStream incidencia,
			FileOutputStream salida, String nameFile) 
	{
		ArrayList<CfdBean> pendientes = new ArrayList<CfdBean>();
		String seconds = "";
	    int contadorMilli = 1;
		for (CfdBean cfdBean : cfdBeans) 
		{
			CfdBean beanInicial = cfdBean;
			ByteArrayOutputStream os2 = cfdBean.getBaosXml();
			try 
			{
				//FolioRange folioRange = conver.folioActivo(cfdBean.getSerieFiscalId(), cfdBean.getFiscalEntityId());				
//				OpenJpa open = openJpaManager.getFolioById(cfdBean.getFiscalEntityId());
//				if(open == null){
//					open = new OpenJpa();
//					open.setId(cfdBean.getFiscalEntityId());
//					open.setSequence_value(0);
//				}
//				open.setSequence_value(open.getSequence_value()+1);
//				openJpaManager.update(open);
				//System.out.println("sequenceValue: " + open.getSequence_value());
				//if (folioRange != null) 
				//{
					//cfdBean.setFolioRange(folioRange);
				
				String folio = "";
				
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(new Date());
			
				String year = ""+calendar.get(Calendar.YEAR);
				String month = ""+(calendar.get(Calendar.MONTH)+1);
				String day = ""+calendar.get(Calendar.DAY_OF_MONTH);
				String hora = ""+calendar.get(Calendar.HOUR_OF_DAY);
				String minuto = ""+calendar.get(Calendar.MINUTE);
				String segundo = ""+calendar.get(Calendar.SECOND);
				
				if (!seconds.equalsIgnoreCase(segundo)) {
					seconds = segundo;
					contadorMilli = 1;
				}
				
				year = (Integer.parseInt(year) < 10 ? "0" : "") + year;
				month = (Integer.parseInt(month) < 10 ? "0" : "") + month;
				day = (Integer.parseInt(day) < 10 ? "0" : "") + day;
				hora = (Integer.parseInt(hora) < 10 ? "0" : "") + hora;
				minuto = (Integer.parseInt(minuto) < 10 ? "0" : "") + minuto;
				segundo = (Integer.parseInt(segundo) < 10 ? "0" : "") + segundo;
				 
				String contador = String.format("%03d",contadorMilli);
				
				
				
				folio += year.substring(year.length() - 2 );
				folio += month+day+hora+minuto+segundo;
				folio += contador;
				
				
				contadorMilli++;
				
				System.out.println("folioXD: "+folio);
				
					replacesOriginalString(cfdBean, folio);
					ByteArrayOutputStream os = cfdBean.getBaosXml();
					cfdBean.setBaosXml(Util.enconding(os));
					xmlProcess.getValidator().valida(cfdBean.getBaosXml(), this.validator);
					String fecha = Util.systemDate();
					xmlProcess.setTransf(transf);
					
					//Cambiar forma pago para CFDFACTORAJEFACTURAS, CONFIRMING Y NEWCONFIRMING ultimos dos en espera
					String fileVerify = nameFile.split("\\.")[0];
					Document doc = UtilCatalogos.convertStringToDocument(cfdBean.getBaosXml().toString("UTF-8"));
					if (fileVerify.contains("CFDFACTORAJEFACTURAS") || fileVerify.contains("CONFIRMING") || fileVerify.contains("NEWCONFIRMING")) {
						UtilCatalogos.setValueOnDocumentElement(doc, "//Comprobante/@FormaPago", "17");
						//UtilCatalogos.setValueOnDocumentElement(doc, "//Comprobante/@MetodoPago", "PPD");
						//cfdBean.setBaosXml(UtilCatalogos.convertStringToOutpuStream(MetodoPago.convertDocumentXmlToString(doc)));
					}
						
					//System.out.println("XMLTipoxd:" + cfdBean.getBaosXml().toString("UTF-8"));
					/*Se asigna el NoCertificado ya que antes se hacia despues de generar la cadena original*/
					//doc = UtilCatalogos.convertStringToDocument(cfdBean.getBaosXml().toString("UTF-8"));
					UtilCatalogos.setValueOnDocumentElement(doc, "//Comprobante/@NoCertificado", cfdBean.getSealCertificate().getSerialNumber());
					cfdBean.setBaosXml(UtilCatalogos.convertStringToOutpuStream(UtilCatalogos.convertDocumentXmlToString(doc)));
					/*Fin asignaciones*/
					
					
					/*Se asigna tipo de cambio 1 cuando la moneda es MXN*/
					doc = UtilCatalogos.convertStringToDocument(cfdBean.getBaosXml().toString("UTF-8"));
					if (UtilCatalogos.getStringValByExpression(doc, "//Comprobante//@Moneda").equalsIgnoreCase("MXN")) {
			    		UtilCatalogos.setValueOnDocumentElement(doc, "//Comprobante/@TipoCambio", "1");
			    	}
					
					
					
					ByteArrayOutputStream originalString = null;
					originalString= xmlProcess.generatesOriginalString(cfdBean.getBaosXml()); 
					//System.out.println("originalString: " + originalString.toString("UTF-8"));
					String seal = xmlProcess.sealEncryption(originalString, cfdBean.getSealCertificate());
					
					ByteArrayOutputStream replaceOS = null;
					replaceOS = xmlProcess.replacesOriginalString(cfdBean.getBaosXml(),
									cfdBean.getSealCertificate(), seal, conver.getTags().LUGAR_EXPEDICION,
									conver.getTags().METODO_PAGO, conver.getTags().FORMA_PAGO, false);
					cfdBean.setFormaPago(conver.getTags().FORMA_PAGO);
					String emisor = cfdBean.getBroadcastRFC();
					//int newFolio = cfdBean.getFolioRange().getActualFolio().intValue();
					long newFolio = Long.parseLong(folio);
					String serieFolio = "" + newFolio;
					if (cfdBean.getSerieCFD() != null)
					{
						serieFolio = cfdBean.getSerieCFD() + serieFolio; 
					}
					
					/*
					 * 
					 * WEbservice
					 * 
					 * 
					 * 
					 * */
					try{
						//System.out.println("replaceOS: " + replaceOS.toString("UTF-8"));
						
						//Obtener nombre de aplicativo
						//System.out.println("nameFile recibido:" + nameFile);
						
						String nombreAplicativo = NombreAplicativo.obtieneNombreApp(getNombresApps(), nameFile.substring(0, nameFile.length()-12), "");
											
						//System.out.println("nombreAplicativo:" + nombreAplicativo);
						
						/*if(nombreAplicativo.equals("")){
							System.out.println("ERROR: " + "El nombre de Aplicativo no existe para la interface " + nameFile);
			            	fileINCIDENCIA("El nombre de Aplicativo no existe para la interface " + nameFile, incidencia, nameFile,
			            			"ERROR", "0", "0",cfdBean.getBroadcastRFC(), 
									cfdBean.getContract(),cfdBean.getCustomerCode(),cfdBean.getPeriod());
						}else{*/
							//Quitar addenda
							
							StringBuilder sbSinAddenda = new StringBuilder();
							String xmlSinAddenda = "";
							String strAddenda = "";
							if (replaceOS.toString("UTF-8").indexOf("<cfdi:Addenda>") != -1){
								int iStart = replaceOS.toString("UTF-8").indexOf("<cfdi:Addenda>");
								int iEnd = replaceOS.toString("UTF-8").indexOf("</cfdi:Addenda>");
								iEnd = iEnd + 15;
								strAddenda = replaceOS.toString("UTF-8").substring(iStart, iEnd);
								//System.out.println("addendax: " + strAddenda);
								xmlSinAddenda = replaceOS.toString("UTF-8").replace(strAddenda, " ");
							}
							if(xmlSinAddenda.equals("") || xmlSinAddenda == null){
								sbSinAddenda.append(replaceOS.toString("UTF-8"));
							}else{
								sbSinAddenda.append(xmlSinAddenda);
							}
							String strSinAddenda = "";
							strSinAddenda = sbSinAddenda.toString();
							
							//System.out.println("XML antes de Timbrado: " + strSinAddenda.toString());
							
							//System.out.println("Periodo antes de Timbrado: " + conver.getTags().EMISION_PERIODO);
							
							//Instalar certificados
							System.setProperty("javax.net.ssl.keyStoreType", "PKCS12");
							System.setProperty("javax.net.ssl.keyStore", properties.getCertificadoSantander());
							System.setProperty("javax.net.ssl.keyStorePassword", properties.getCertificadoPass());
													
							System.setProperty("javax.net.ssl.trustStore", properties.getCertificadoInterfactura());

							//Iniciar conexion con WebService
							//WebService1 service = null;
							//service = new WebService1();											
							//WebService1Soap servicePort = service.getWebService1Soap();
							if(this.servicePort == null){
								this.servicePort = new WebServiceCliente();								
							}	
							/*
							 * Invoke the web service operation using the port or stub or proxy
							 */
							//String helloMessage = servicePort.consultaTimbre("F7EFFF4D-2816-425C-81CC-B26DF8C177DB", null, true) ;
							System.out.println("Conectandose...........");
												
							String xmlTimbradoConPipe = ""; 
							xmlTimbradoConPipe = this.servicePort.generaTimbre(strSinAddenda, false, this.urlWebService, properties, nameFile, 0, 2, conver.getTags().EMISION_PERIODO, nombreAplicativo);
							// Al parecer aqui se hace el timbrado, AMDA v 3.3 verificar las mayusculas tambien 
							String xmlTimbrado = xmlTimbradoConPipe.substring(0, xmlTimbradoConPipe.length()-1);
							System.out.println("Charly1306: xml timbrado");
							
							System.out.println("Respuesta Web Service: XML \n  " + xmlTimbrado);
							
							//logger.info("XML Timbrado: " + xmlTimbrado);
							
							xmlTimbrado = xmlTimbrado.replace("IdRespuesta=\"1\"><?xml version=\"1.0\" encoding=\"UTF-8\"?>", "IdRespuesta=\"1\">");
				            //Convertir de string a Document
				            Document dom = stringToDocument(xmlTimbrado);
				            // Se verifica si la respuesta del web service fue correcta
				            Element docEle = dom.getDocumentElement();
				           			           	            
				            //Atributos TimbreFiscalDigital
				            String strFechaTimbrado = "";
				            String strUUID = "";
				            String strNoCertificadoSAT = "";
				            String strSelloCFD = "";
				            String strSelloSAT = "";
				            String strVersion = "";
				            
				            //RFC del Emisor y Receptor
				            String strEmisorRFC = "";
				            String strReceptorRFC = "";
				            
				            //Total del xml timbrado
				            String strTotal = "";
				            /*TODO: Poner los valores de los atributos y quitar los valores fijos*/
				            String descripcion = docEle.getAttribute("Descripcion");
				            String idRespuesta = docEle.getAttribute("IdRespuesta");
				            
				            String xmlFinal = "";
				            
				            if(descripcion.toLowerCase().trim().equals("ok") && idRespuesta.equals("1")) {
				            	
				            	 //Obtener nodos hijos (cfdi:Comprobante) del nodo Resultado
					            NodeList hijosResultado = docEle.getChildNodes();
					            
					            for(int i=0; i<hijosResultado.getLength(); i++){
					            	Node nodo = hijosResultado.item(i);
					            	if(nodo instanceof Element && nodo.getNodeName().equals("cfdi:Comprobante")){
					            		strTotal = nodo.getAttributes().getNamedItem("Total").getTextContent(); // AMDA Version 3.3
					            		NodeList hijosComprobante = nodo.getChildNodes();
					            		for(int j=0; j<hijosComprobante.getLength(); j++){
					            			Node nodo2 = hijosComprobante.item(j);
					            			if(nodo2 instanceof Element && nodo2.getNodeName().equals("cfdi:Emisor")){
					            				strEmisorRFC = nodo2.getAttributes().getNamedItem("Rfc").getTextContent(); // AMDA Version 3.3
					            			}
					            			else if(nodo2 instanceof Element && nodo2.getNodeName().equals("cfdi:Receptor")){
					            				strReceptorRFC = nodo2.getAttributes().getNamedItem("Rfc").getTextContent(); // AMDA Version 3.3
					            			}
					            			else if(nodo2 instanceof Element && nodo2.getNodeName().equals("cfdi:Complemento")){
					            				NodeList hijosComplemento = nodo2.getChildNodes();
					            				for(int k=0; k<hijosComplemento.getLength(); k++){
					            					Node nodo3 = hijosComplemento.item(k);
					            					if(nodo3 instanceof Element && nodo3.getNodeName().equals("tfd:TimbreFiscalDigital")){			            						
					            						strFechaTimbrado = nodo3.getAttributes().getNamedItem("FechaTimbrado").getTextContent();
					            						strUUID = nodo3.getAttributes().getNamedItem("UUID").getTextContent();
					            						strNoCertificadoSAT = nodo3.getAttributes().getNamedItem("NoCertificadoSAT").getTextContent();
					            						strSelloCFD = nodo3.getAttributes().getNamedItem("SelloCFD").getTextContent();
					            						strSelloSAT = nodo3.getAttributes().getNamedItem("SelloSAT").getTextContent();
					            						strVersion = nodo3.getAttributes().getNamedItem("Version").getTextContent();
					            					}
					            				}
					            			}
					            		}
					            	}
					            }
					            
					            
								StreamResult result = nodeToStreamResult(docEle.getFirstChild());
								
								String xmlString = ""; 
								xmlString =	result.getWriter().toString();
								String xmlString2 = ""; 
								xmlString2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
								
								xmlString2 += xmlString.substring(39);
								
								//System.out.println("xmlString2: " + xmlString2);
																	            	
								 if(!strAddenda.trim().equals("") && strAddenda != null){
									 String strAddendaComp = "";
									 strAddendaComp = strAddenda + "</cfdi:Comprobante>";
									 String strXmlString = "";
									 strXmlString = xmlString2.replace("</cfdi:Comprobante>", strAddendaComp);									 
									 xmlFinal = strXmlString.replaceAll("[\n\r]", "");
									 
								 }else{
									 xmlFinal = xmlString2.toString();
								 }
								//System.out.println("xmlFinal: " + xmlFinal);							 
				            	
				            	//System.out.println("OK: " + descripcion);
				            			            	
				            	ByteArrayOutputStream replaceOSTimbrado =  new ByteArrayOutputStream();
				            	byte [] replaceOSTimbradoBytes = xmlFinal.getBytes("UTF-8");
								replaceOSTimbrado.write(replaceOSTimbradoBytes);
							
								//System.out.println("replaceOSTimbrado: " + replaceOSTimbrado.toString("UTF-8"));
								//replaceOS xml								
								//String fileName = xmlProcess.writeHardDrive(replaceOS, fecha, false, emisor, serieFolio, false, null);
								String fileName = xmlProcess.writeHardDrive(replaceOSTimbrado, fecha, false, emisor, serieFolio, false, null);
								//Route route = new Route();
								//route.setRoute(fileName);
														
								//Incluir en CFDIssued folioSAT
								CFDIssued cfdIssued = new CFDIssued();
								cfdIssued.setXmlRoute(fileName);
								cfdIssued.setCreationDate(cfdBean.getCreationDate());
								
								//CFDI FolioSat
								cfdIssued.setFolioSAT(strUUID);
								
								//CFDI isCFDI y FolioInterno
								cfdIssued.setIsCFDI(1);
								cfdIssued.setFolioInterno(newFolio);
								
								// aqui debe crearse el objeto CFDIssued, en fileSALIDA se
								// le pasan los demas valores
								
								//foliosat o UUID,
								//FechaTimbrado, 
								//cadena original (reemplazar con la cadena de complemento de certificacion UUID-SELLO), return "||" + timbreFiscal.getVersion() + "|" + timbreFiscal.getUuid() + "|" + timbreFiscal.getFechaTimbrado() +
					            //  "|" + timbreFiscal.getSelloSat() + "|" + timbreFiscal.getNoCertificadoSat() + "||";
								//sello del timbre
								//certificado del sat
								
								//Concatenar los ceros necesarios en el total							
								//System.out.println("total: " + strTotal);
								
								String strTotalZeros = putZeros(strTotal);
								
								//////////////////////////////////////////////////
								
								fileSALIDA(seal, originalString.toString("UTF-8"), salida,nameFile, cfdBean, cfdIssued,
										strFechaTimbrado, strNoCertificadoSAT, strSelloCFD, strSelloSAT, strVersion, strEmisorRFC, strReceptorRFC, strTotalZeros, newFolio);

								// aqui se hace el update a base de datos
								//conver.getFolioRangeManager().update(folioRange);
																				
								
				            } else {
				            	System.out.println("ERROR: " + descripcion + " " + idRespuesta);
				            	fileINCIDENCIA(idRespuesta + "-" + descripcion + " ", incidencia, nameFile,
				            			"ERROR", "0", "0",cfdBean.getBroadcastRFC(), 
										cfdBean.getContract(),cfdBean.getCustomerCode(),cfdBean.getPeriod());
				            }
						//}
						
					}catch(Exception e){
						e.printStackTrace();
						System.out.println(e.getMessage());						
					}
				//} 
				//else 
				//{
				//	throw new Exception(
				//			"No hay Folios Disponibles para la entidad "
				//			+ conver.getTags().fis.getFiscalName());
				//}
			} 
			catch (Exception ex) 
			{
				try 
				{
					String typeIncidence = "ERROR";
					if (ex.getMessage() != null
							&& ex.getMessage().contains(
									"The transaction has been rolled back")) 
					{	
						typeIncidence = "WARNING";	
						beanInicial.setBaosXml(os2);
						pendientes.add(beanInicial);
					}
					fileINCIDENCIA(ex, incidencia, nameFile,
							typeIncidence, cfdBean.getStartLine(), cfdBean.getEndLine(),cfdBean.getBroadcastRFC(), 
							cfdBean.getContract(),cfdBean.getCustomerCode(),cfdBean.getPeriod());
				} 
				catch (Exception exx) 
				{
					if (exx.getLocalizedMessage() != null)
					{	logger.error(exx.getLocalizedMessage().replace("ORA-", "ORACLE-"), exx);	}
				}
				if (ex.getLocalizedMessage() != null)
				{	logger.error(ex.getLocalizedMessage().replace("ORA-", "ORACLE-"), ex);	}
			}
		}
		return pendientes;
	}
	
	

	/**
	 * 
	 * @param path
	 * @param cont
	 * @param prefix
	 * @param nameFile
	 * @return
	 */
	private String getNameFile(String path, long cont, String prefix,
			String nameFile) 
	{
		String nameProcess[] = nameFile.split("\\.");
		String time = null;

		if (File.separatorChar == '/') {
			time = Util.convertirFecha(Calendar.getInstance().getTime(),
					"HH:mm:ss");
		} else {
			time = Util.convertirFecha(Calendar.getInstance().getTime(),
					"HHmmss");
		}

		if (prefix == null) {
			if (cont == -1) {
				return path + nameProcess[0] + "T" + time + "."
						+ nameProcess[1];
			} else {
				return path + nameProcess[0] + "_" + cont + "."
						+ nameProcess[1];
			}
		} else {
			if (cont == -1) {
				return path + prefix
						+ nameProcess[0].substring(3, nameProcess[0].length())
						+ "." + nameProcess[1];
			} else {
				return path + prefix
						+ nameProcess[0].substring(3, nameProcess[0].length())
						+ "_" + cont + "." + nameProcess[1];
			}
		}
	}

	/**
	 * 
	 * @return
	 */
	public HashMap<String, FiscalEntity> getLstFiscal() {
		return lstFiscal;
	}

	/**
	 * 
	 * @param lstFiscal
	 */
	public void setLstFiscal(HashMap<String, FiscalEntity> lstFiscal) {
		this.lstFiscal = lstFiscal;
	}

	/**
	 * 
	 * @return
	 */
	public List<SealCertificate> getLstSeal() {
		return lstSeal;
	}

	/**
	 * 
	 * @param lstSeal
	 */
	public void setLstSeal(List<SealCertificate> lstSeal) {
		this.lstSeal = lstSeal;
	}

	/**
	 * 
	 * @return
	 */
	public List<Iva> getLstIva() {
		return lstIva;
	}

	/**
	 * 
	 * @param lstIva
	 */
	public void setLstIva(List<Iva> lstIva) {
		this.lstIva = lstIva;
	}

	/**
	 * 
	 * @return
	 */
	public Transformer getTransf() {
		return transf;
	}

	/**
	 * 
	 * @param transf
	 */
	public void setTransf(Transformer transf) {
		this.transf = transf;
	}

	/**
	 * 
	 * @return
	 */
	public ValidatorHandler getValidator() {
		return validator;
	}

	/**
	 * 
	 * @param validator
	 */
	public void setValidator(ValidatorHandler validator) {
		this.validator = validator;
	}

	public void setCampos22(HashMap<String, HashMap> campos22) {
		this.campos22 = campos22;
	}

	public HashMap<String, HashMap> getCampos22() {
		return campos22;
	}

	public HashMap<String, HashMap> getTipoCambio() {
		return tipoCambio;
	}

	public void setTipoCambio(HashMap<String, HashMap> tipoCambio) {
		this.tipoCambio = tipoCambio;
	}

	public String getUrlWebService() {
		return urlWebService;
	}

	public void setUrlWebService(String urlWebService) {
		this.urlWebService = urlWebService;
	}

	public HashMap<String, String> getNombresApps() {
		return nombresApps;
	}

	public void setNombresApps(HashMap<String, String> nombresApps) {
		this.nombresApps = nombresApps;
	}
}