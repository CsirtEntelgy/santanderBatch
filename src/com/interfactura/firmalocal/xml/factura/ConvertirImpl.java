package com.interfactura.firmalocal.xml.factura;

import static com.interfactura.firmalocal.xml.util.Util.convierte;
import static com.interfactura.firmalocal.xml.util.Util.isNullEmpity;
import static com.interfactura.firmalocal.xml.util.Util.isNullEmpty;
import static com.interfactura.firmalocal.xml.util.Util.tags;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.interfactura.firmalocal.domain.entities.AddendumCustoms;
import com.interfactura.firmalocal.domain.entities.FiscalEntity;
import com.interfactura.firmalocal.domain.entities.FolioRange;
import com.interfactura.firmalocal.domain.entities.Iva;
import com.interfactura.firmalocal.persistence.FolioRangeManager;
import com.interfactura.firmalocal.xml.Properties;
import com.interfactura.firmalocal.xml.TagsXML;
import com.interfactura.firmalocal.xml.util.Util;

/**
 * 
 * @author jose luis
 * 
 */
@Component
public class ConvertirImpl 
{

	private TagsXML tags;
	private Stack<String> pila;
	private StringBuilder concat;
	@Autowired
	private Properties properties;
	private Logger logger = Logger.getLogger(ConvertirImpl.class);
	@Autowired(required = true)
	private FolioRangeManager folioRangeManager;
	private String folio;
	private String tmp;
	private Util util = new Util();
	private List<String> descriptionFormat;

	public ConvertirImpl() 
	{
		// tags = new TagsXML();
	}

	/**
	 * Limpia las Banderas
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

	/**
	 * 
	 * @param tokens
	 * @param numberLine
	 * @throws UnsupportedEncodingException 
	 */
	public void set(String tokens[], long numberLine) 
		throws UnsupportedEncodingException 
	{
		logger.debug("Inicio de Formateo de Linea");
		pila = new Stack<String>();
		clearFlag();
		tags.lstCustoms = new HashSet<AddendumCustoms>();
		if (tokens.length >= 18) 
		{
			tags.NUM_PROVEEDOR = tokens[1];
			tags.CTA_DEPOSITO = tokens[2];
			tags.ORDEN_COMPRA = tokens[3];
			tags.INSTITUCION_RECEPTORA = tokens[4];
			tags.NOMBRE_BENIFICIARIO = tokens[5];
			tags.EMISION_CONTRATO = tokens[6];
			tags.EMISION_CODIGO_CLIENTE = tokens[7];
			tags.EMISION_CENTRO_COSTOS = tokens[8];
			tags.EMISION_PERIODO = tokens[9];
			tags.EMISION_CLAVE_SANTANDER = tokens[10];
			tags.EMISION_FOLIO_INTERNO = tokens[11];
			tags.EMAIL = tokens[12];
			tags.TOTAL_REPORTE = tokens[13].trim();
			tags.IVA_TOTAL_REPORTE = tokens[14].trim();
			tags.IVA_TOTAL_MN = "0";
			tags.LONGITUD = tokens[15].trim();
			tags.TIPO_FORMATO = tokens[16].trim();

			if (Util.isNullEmpty(tags.IVA_TOTAL_REPORTE)) 
			{	tags.IVA_TOTAL_REPORTE = "0";	}

			System.out.println("tokens.length" + tokens.length);
			if (tokens.length >= 20) 
			{
				tags.TIPO_MONEDA = tokens[17];
				tags.TIPO_CAMBIO = tokens[18];
			} 
			else 
			{
				tags.TIPO_MONEDA = properties.getCurrency();
				tags.TIPO_CAMBIO = "1.0000";
			}
			System.out.println("tags.TIPO_MONEDA:" + tags.TIPO_MONEDA);
			System.out.println("tags.TIPO_CAMBIO:" + tags.TIPO_CAMBIO);
		} 
		else 
		{	formatCFD(numberLine);	}
	}

	/**
	 * 
	 * @param tokens
	 * @param date
	 * @param numberLine
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] fComprobante(String[] tokens, Date date, long numberLine, HashMap campos22) 
		throws UnsupportedEncodingException 
	{
		if (tokens.length >= 21) 
		{
			tags.CFD_TYPE = util.getCFDType(tokens[1]);
			tags.FOLIO_REFERENCE = tokens[2];

			concat = new StringBuilder();
			
			concat.append(" TipoCambio=\"" + tags.TIPO_CAMBIO + "\"");
			concat.append(" Moneda=\"" + tags.TIPO_MONEDA + "\"");
			
			if (!isNullEmpty(tokens[3])) 
			{
				tags.SERIE_FISCAL_CFD = tokens[3].trim();
				concat.append(" serie=\"" + tags.SERIE_FISCAL_CFD + "\"");
			}
			folio = null;
			concat.append(" folio=\"" + properties.getLblFOLIOCFD() + "\"");
			concat.append(" formaDePago=\"" + tokens[4] + "\"");
			tags.SUBTOTAL_MN = tokens[5];
			concat.append(" subTotal=\"" + tokens[5] + "\"");
			if (!isNullEmpty(tokens[6])) 
			{	concat.append(" descuento=\"" + tokens[6] + "\"");	}
			if (!isNullEmpty(tokens[7])) 
			{	concat.append(" motivoDescuento=\"" + tokens[7] + "\"");	}

			tags.TOTAL_MN = tokens[8];
			concat.append(" total=\"" + tags.TOTAL_MN + "\"");

			if (!isNullEmpty(tokens[9])) 
			{	
				tags.METODO_PAGO = tokens[9];
				concat.append(" metodoDePago=\"" + tokens[9] + "\"");	
			}

			tags.FECHA_CFD = Util.convertirFecha(date);
			concat.append(" fecha=\"" + tags.FECHA_CFD + "\"");

			if (tokens[1].equalsIgnoreCase("Nota de Credito")) 
			{	concat.append(" tipoDeComprobante=\"egreso\"");		} 
			else 
			{	concat.append(" tipoDeComprobante=\"ingreso\"");	}

			tags.FACTORAJE_HORA = tokens[10];
			tags.FACTORAJE_TIPO = tokens[11];
			tags.FACTORAJE_SVN = tokens[12];
			tags.FACTORAJE_SPB = tokens[13];
			tags.FACTORAJE_SPF = tokens[14];
			tags.FACTORAJE_SID = tokens[15];
			tags.FACTORAJE_LCI = tokens[16];
			tags.FACTORAJE_LIVA = tokens[17];
			tags.FACTORAJE_COMISION = tokens[18];
			tags.FACTORAJE_LETRAS = tokens[19];
			
			return Util
					.conctatArguments(
							"\n<cfdi:Comprobante version=\"3.2\" ",
							concat.toString(),
							" xmlns:ecb=\"http://www.sat.gob.mx/ecb\" ",
							" xmlns:cfdi=\"http://www.sat.gob.mx/cfd/3\"  ",
							" xsi:schemaLocation=\"http://www.sat.gob.mx/cfd/3",
							" http://www.sat.gob.mx/sitio_internet/cfd/3/cfdv32.xsd ",
							" http://www.sat.gob.mx/ecb http://www.sat.gob.mx/sitio_internet/cfd/ecb/ecb.xsd\" ",
							" sello=\"", properties.getLabelSELLO(), "\" ",
							" noCertificado=\"",
							properties.getLblNO_CERTIFICADO(),
							"\" " + " certificado=\"",
							properties.getLblCERTIFICADO(), "\" " + " ",														
							"LugarExpedicion=\"" + properties.getLabelLugarExpedicion()  + "\" ",
							"NumCtaPago=\"" + properties.getlabelFormaPago()  + "\" ",
							" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" >")
					.toString().getBytes("UTF-8");
		}
		else 
		{	return formatCFD(numberLine);	}
	}

	/**
	 * 
	 * @param numberLine
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] formatCFD(long numberLine) 
		throws UnsupportedEncodingException 
	{
		tags.isFormat = true;
		descriptionFormat.add("" + numberLine);
		return "".getBytes("UTF-8");
	}

	/**
	 * 
	 * @param tokens
	 * @param lstFiscal
	 * @param numberLine
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] emisor(String tokens[], HashMap<String, FiscalEntity> lstFiscal, long numberLine
			,HashMap<String, HashMap> campos22) 
		throws UnsupportedEncodingException 
	{
		if (tokens.length >= 3) 
		{
			tags.EMISION_RFC = tokens[1].trim();
			logger.debug("RFC EMISOR: " + tags.EMISION_RFC);
			tags.fis = null;
			tags.fis = lstFiscal.get(tags.EMISION_RFC);
			if(tags.fis != null){
				tags.LUGAR_EXPEDICION = (String) campos22.get(tags.EMISION_RFC).get("LugarExpedicion");
				tags.FORMA_PAGO = (String) campos22.get(tags.EMISION_RFC).get("formaDePago");
			}else{
				tags.LUGAR_EXPEDICION = null;
				tags.FORMA_PAGO = null;
			}			
			
			return Util
					.conctatArguments(tags("Emisor", pila), "\n<cfdi:Emisor rfc=\"",
							tags.EMISION_RFC, "\"", getNameEntityFiscal().toString(), "> ",
							domicilioFiscal(campos22)).toString().getBytes("UTF-8");
			
		} 
		else 
		{	return formatCFD(numberLine);	}
	}

	/**
	 * 
	 * @return
	 */
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

	/**
	 * 
	 * @param tokens
	 * @param numberLine
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] receptor(String tokens[], long numberLine) 
		throws UnsupportedEncodingException 
	{
		if (tokens.length >= 4) 
		{
			tags.RECEPCION_RFC = tokens[1].trim();
			return Util
					.conctatArguments(
							tags("Receptor", pila),
							"\n<cfdi:Receptor rfc=\"",
							tokens[1].trim(),
							"\"",
							tokens.length > 2 ? " nombre=\"" + convierte(tokens[2].trim())
									+ "\"" : "", " >").toString().getBytes("UTF-8");
		} else {
			return formatCFD(numberLine);
		}
	}

	/**
	 * 
	 * @param tokens
	 * @param numberLine
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] concepto(String[] tokens, long numberLine) 
		throws UnsupportedEncodingException 
	{
		if (tokens.length >= 8) 
		{
			tags.UNIDAD_MEDIDA=tokens[2];
		return Util
				.conctatArguments(tags("Concepto", pila),
						"\n<cfdi:Concepto cantidad=\"", tokens[1].trim(), "\" ",
						isNullEmpity(tokens[2], "unidad"),
						isNullEmpity(tokens[3], "noIdentificacion"),
						" descripcion=\"", convierte(tokens[4]),
						"\" valorUnitario=\"", tokens[5], "\" importe=\"",
						tokens[6], "\">").toString().getBytes("UTF-8");
		} 
		else 
		{	return formatCFD(numberLine);	}
	}

	/**
	 * 
	 * @return
	 */
	public String domicilioFiscal(HashMap campos22) 
	{
		if (tags.isEntidadFiscal) 
		{
			tags._Calle = "calle=\"" + tags.fis.getAddress().getStreet()
					+ "\" ";
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
					+ tags.fis.getAddress().getRegion() + "\" ";
			if(tags.fis.getAddress().getState()!=null)
			{
				tags._Estado = "estado=\""
						+ tags.fis.getAddress().getState().getName() + "\" ";
				tags._Pais = " pais=\""
						+ tags.fis.getAddress().getState().getCountry().getName()
						+ "\" ";
			} 
			else 
			{
				tags._Estado = "estado=\"\" ";
				tags._Pais = " pais=\"\" ";
			}
			tags._CodigoPostal = "codigoPostal=\""
					+ tags.fis.getAddress().getZipCode() + "\" ";
		} else 
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
		String regVal =null;
		String regimenStr="";
		try{
			if(tags.fis != null){
				System.out.println("tags.fis.getTaxID() " + tags.fis.getTaxID());
				
				regVal = (String) ((HashMap) campos22.get(tags.fis.getTaxID())).get("regimenFiscal");
				tags.REGIMEN_FISCAL=regVal;
				regimenStr = "\n<cfdi:RegimenFiscal Regimen=\"" + regVal + "\" />";
			}
			
		}catch(Throwable e){
			e.printStackTrace();
		}
		
		return Util.conctatArguments("\n<cfdi:DomicilioFiscal ", tags._Calle,
				tags._NoExterior, tags._NoInterior, tags._Colonia,
				tags._Localidad, tags._Referencia, tags._Municipio,
				tags._Estado, tags._Pais, tags._CodigoPostal, " />" + regimenStr.toString(),
				tags("", pila)).toString();
	}

	/**
	 * 
	 * @param tokens
	 * @param numberLine
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] impuestos(String tokens[], long numberLine) 
		throws UnsupportedEncodingException 
	{
		tags.isImpuestos = true;
		tmp = "";
		if (tokens.length >= 4) 
		{
			if (tags.isParte) 
			{	tmp += "\n</cfdi:Parte>";	}
			tmp += tags("", pila);
			if (tags.isConceptos) 
			{
				tmp += "\n</cfdi:Conceptos>";
				tags.isConceptos = false;
			}
	
			if (!Util.isNullEmpty(tokens[2])) 
			{	tags.IVA_TOTAL_MN = tokens[2];	}
	
			tags.TOTAL_IMP_RET = tokens[1].trim();
			tags.TOTAL_IMP_TRA = tokens[2].trim();
			
			return Util
					.conctatArguments(tmp, "\n<cfdi:Impuestos ",
							isNullEmpity(tokens[1], "totalImpuestosRetenidos"),
							isNullEmpity(tokens[2], "totalImpuestosTrasladados"),
							">").toString().getBytes("UTF-8");
		} 
		else 
		{	return formatCFD(numberLine);	}
	}

	/**
	 * 
	 * @param tokens
	 * @param lstIva
	 * @param numberLine
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] traslados(String tokens[], List<Iva> lstIva, long numberLine) 
		throws UnsupportedEncodingException 
	{
		if (tokens.length >= 5) 
		{
			if (tokens[1].trim().toUpperCase().equals("IVA")) 
			{
				tags.DESCRIPTION_TASA = "";
				Integer tasa = new Integer(Util.getTASA(tokens[2]));
				Iva iva = null;
				for (Iva obj : lstIva) 
				{
					if (obj.getTasa().equals(tasa)) 
					{
						iva = obj;
						break;
					}
				}
	
				if (iva != null) 
				{
					tags.DESCRIPTION_TASA = iva.getDescripcion();
					if (tags.DESCRIPTION_TASA.length() > 0) 
					{	tags.isDescriptionTASA = true;	}
				}
			}
			return Util
					.conctatArguments("\n<cfdi:Traslado impuesto=\"", tokens[1],
							"\" tasa=\"", tokens[2], "\" importe=\"", tokens[3],
							"\"/>").toString().getBytes("UTF-8");
		} 
		else 
		{	return formatCFD(numberLine);	}
	}

	/**
	 * 
	 * @param tokens
	 * @param numberLine
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] retenciones(String tokens[], long numberLine) 
		throws UnsupportedEncodingException 
	{
		if (tokens.length >= 4) 
		{
			return Util
					.conctatArguments("\n<cfdi:Retencion impuesto=\"", tokens[1],
							"\" importe=\"", tokens[2], "\"/>").toString()
					.getBytes("UTF-8");
		} 
		else 
		{	return formatCFD(numberLine);	}
	}

	/**
	 * 
	 * @param tokens
	 * @param numberLine
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] domicilio(String tokens[], long numberLine) 
		throws UnsupportedEncodingException 
	{
		if (tokens.length >= 12) 
		{
			tags._Calle = " calle=\"" + convierte(tokens[1]) + "\" ";
			tags._NoExterior = isNullEmpity(tokens[2], " noExterior");
			tags._NoInterior = isNullEmpity(tokens[3], " noInterior");
			tags._Colonia = isNullEmpity(tokens[4], " colonia");
			tags._Localidad = isNullEmpity(tokens[5], " localidad");
			tags._Referencia = isNullEmpity(tokens[6], " referencia");
			tags._Municipio = isNullEmpity(tokens[7], " municipio");
			tags._Estado = isNullEmpity(tokens[8], " estado");
			tags._Pais = " pais=\"" + convierte(tokens[9]) + "\"";
			tags._CodigoPostal = tokens.length >= 11 ? isNullEmpity(tokens[10],
					" codigoPostal") : "";
			return Util
					.conctatArguments("\n<cfdi:Domicilio ", tags._Calle,
							tags._NoExterior, tags._NoInterior, tags._Colonia,
							tags._Localidad, tags._Referencia, tags._Municipio,
							tags._Estado, tags._Pais, tags._CodigoPostal, " />",
							tags("", pila)).toString().getBytes("UTF-8");
		} 
		else 
		{	return formatCFD(numberLine);	}
	}

	/**
	 * 
	 * @param tokens
	 * @param date
	 * @param numberLine
	 * @return
	 * @throws ParseException
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] infoAduanera(String[] tokens, Date date, long numberLine)
			throws ParseException, UnsupportedEncodingException 
	{
		if (tokens.length >= 5) 
		{
			AddendumCustoms customs = new AddendumCustoms();
			customs.setAuthor("masivo");
			customs.setCreationDate(date);
			customs.setDateCustoms(Util.convertirString(tokens[2]));
			customs.setNameOfCustoms(tokens[3]);
			customs.setPedimento(tokens[1]);
			if (this.tags.isParte) 
			{	customs.setSource("IP");	}
			else 
			{	customs.setSource("I");	}
			tags.lstCustoms.add(customs);
			return Util
					.conctatArguments("\n<cfdi:InformacionAduanera numero=\"",
							tokens[1], "\"", " fecha=\"", tokens[2], "\" ",
							" aduana=\"", tokens[3], "\"/>").toString().getBytes("UTF-8");
		} else {
			return formatCFD(numberLine);
		}
	}

	/**
	 * 
	 * @param tokens
	 * @param numberLine
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] parte(String[] tokens, long numberLine) 
		throws UnsupportedEncodingException 
	{
		if (tokens.length >= 8) 
		{
			tmp = "";
			if (tags.isParte) 
			{	tmp = "\n</cfdi:Parte>";	}
			if (!tags.isParte) 
			{	tags.isParte = true;	}
	
			return Util
					.conctatArguments(tmp, "\n<cfdi:Parte cantidad=\"", tokens[1],
							"\" unidad=\"", tokens[2], "\" ",
							" noIdentificacion=\"", tokens[3],
							"\" " + "descripcion=\"", convierte(tokens[4]), "\" ",
							" valorUnitario=\"", tokens[5], "\" ", " importe=\"",
							tokens[6], "\">").toString().getBytes("UTF-8");
		} 
		else 
		{	return formatCFD(numberLine);	}
	}

	/**
	 * 
	 * @param tokens
	 * @param numberLine
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] predial(String[] tokens, long numberLine) 
		throws UnsupportedEncodingException 
	{
		if (tokens.length >= 3) 
		{
			return Util
					.conctatArguments("\n<cfdi:CuentaPredial numero=\"", tokens[1],
							"\" />").toString().getBytes("UTF-8");
		} 
		else 
		{	return formatCFD(numberLine);	}
	}

	/**
	 * 
	 * @param tokens
	 * @param numberLine
	 * @return
	 */
	public byte[] complementoConcepto(String[] tokens, long numberLine) 
	{	return null;	}

	/**
	 * 
	 * @param tokens
	 * @param numberLine
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public byte[] factoraje(String[] tokens, long numberLine) 
		throws UnsupportedEncodingException 
	{
		if (tokens.length >= 13) 
		{
			return Util
				.conctatArguments("\n<as:InformacionFactoraje ",
						Util.isNullEmpity(tokens[1], "deudorProveedor"),
						Util.isNullEmpity(tokens[2], "tipoDocumento"),
						Util.isNullEmpity(tokens[3], "numeroDocumento"),
						Util.isNullEmpity(tokens[4], "fechaVencimiento"),
						Util.isNullEmpity(tokens[5], "plazo"),
						Util.isNullEmpity(tokens[6], "valorNominal"),
						Util.isNullEmpity(tokens[7], "aforo"),
						Util.isNullEmpity(tokens[8], "precioBase"),
						Util.isNullEmpity(tokens[9], "tasaDescuento"),
						Util.isNullEmpity(tokens[10], "precioFactoraje"),
						Util.isNullEmpity(tokens[11], "importeDescuento"),
						">\n</as:InformacionFactoraje>").toString().getBytes("UTF-8");
		} 
		else 
		{	return formatCFD(numberLine);	}
	}

	/**
	 * Consume el folio
	 * 
	 * @param serie
	 * @param idEntityFiscal
	 * @return
	 */
	public FolioRange folioActivo(String serie, long idEntityFiscal) 
	{
		boolean flagOcupados = false;
		FolioRange p_folioRange = null;
		
		logger.info("Serie de la entidad Fiscal: "+serie);
		logger.info("Entidad Fiscal ID: "+idEntityFiscal);
		for (FolioRange objF : folioRangeManager.listarActivos(serie, idEntityFiscal)) 
		{
			Integer p_folio = objF.getActualFolio();
			tags.NUM_APROBACION = objF.getAuthorizationNumber().toString();
			tags.YEAR_APROBACION = String.valueOf(objF.getYearOfAuthorization());
			p_folioRange = objF;
			p_folioRange.setActualFolio(p_folio + 1);
			if (p_folioRange.getActualFolio() == p_folioRange.getFinalFolio()) 
			{
				p_folioRange.setEstatus("OCUPADOS");
				flagOcupados = true;
			}
		}

		if (flagOcupados) 
		{
			int folioI = Integer.parseInt(folio);
			for (FolioRange objF : folioRangeManager.listar(serie,
					idEntityFiscal)) {
				if (objF.getActualFolio() - folioI == 1) 
				{
					objF.setEstatus("ACTIVO");
					folioRangeManager.update(objF);
				}
			}
		}

		return p_folioRange;
	}

	public TagsXML getTags() 
	{	return tags;	}

	public void setTags(TagsXML tags) 
	{	this.tags = tags;	}

	public Stack<String> getPila() 
	{	return pila;	}

	public void setPila(Stack<String> pila) 
	{	this.pila = pila;	}

	public String getFolio() 
	{	return folio;	}

	public void setFolio(String folio) 
	{	this.folio = folio;	}

	public FolioRangeManager getFolioRangeManager() 
	{	return folioRangeManager;	}

	public void setFolioRangeManager(FolioRangeManager folioRangeManager) 
	{	this.folioRangeManager = folioRangeManager;		}

	public List<String> getDescriptionFormat() 
	{	return descriptionFormat;	}

	public void setDescriptionFormat(List<String> descriptionFormat) 
	{	this.descriptionFormat = descriptionFormat;		}
	
}
