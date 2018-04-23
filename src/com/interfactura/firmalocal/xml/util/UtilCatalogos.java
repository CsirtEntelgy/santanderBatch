package com.interfactura.firmalocal.xml.util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.interfactura.firmalocal.datamodel.CfdiComprobanteFiscal;
import com.interfactura.firmalocal.datamodel.CfdiConcepto;
import com.interfactura.firmalocal.datamodel.CfdiConceptoImpuestoTipo;
import com.interfactura.firmalocal.datamodel.CfdiDomicilio;
import com.interfactura.firmalocal.datamodel.CfdiReceptor;
import com.interfactura.firmalocal.datamodel.ComplementoPago;
import com.interfactura.firmalocal.datamodel.CustomsInformation;
import com.interfactura.firmalocal.datamodel.ElementsInvoice;
import com.interfactura.firmalocal.datamodel.FarmAccount;
import com.interfactura.firmalocal.datamodel.Invoice;
import com.interfactura.firmalocal.datamodel.Invoice_Masivo;
import com.interfactura.firmalocal.datamodel.Part;
import com.interfactura.firmalocal.datamodel.TimbreFiscal;
import com.interfactura.firmalocal.pdf.util.NumberToLetterConverter;
import com.interfactura.firmalocal.xml.CatalogosDom;

public class UtilCatalogos 
{
	private static Logger logger = Logger.getLogger(UtilCatalogos.class);
	public static final Map<String, String> errorMessage = new HashMap<String, String>();
	public static StringBuffer lstErrors = new StringBuffer();
	static {
        errorMessage.put("ErrCompMoneda001", "Clave=\"CFDI33112\" Nodo=\"Comprobante\" Mensaje=\"El campo Moneda no contiene un valor del catálogo c_Moneda.\"");
        errorMessage.put("ErrCompMoneda002", "Clave=\"CFDI33112\" Nodo=\"Comprobante\" Mensaje=\"El valor de este campo Moneda excede la cantidad de decimales que soporta la moneda.\"");
        errorMessage.put("ErrCompTipoCambio001", "Clave=\"CFDI33116\" Nodo=\"Comprobante\" Mensaje=\"El campo TipoCambio no cumple con el patrón requerido.\"");
        errorMessage.put("ErrCompTipoCambio002", "Clave=\"CFDI33117\" Nodo=\"Comprobante\" Mensaje=\"El campo TipoCambio no esta dentro de los límites establecidos\"");
        errorMessage.put("ErrCompTipoCambio003", "Clave=\"CFDI33116\" Nodo=\"Comprobante\" Mensaje=\"No se econtro TipoCambio para la moneda relacionada\"");
        errorMessage.put("ErrCompTipoCambio004", "Clave=\"CFDI33113\" Nodo=\"Comprobante\" Mensaje=\"El campo TipoCambio no tiene el valor \"1\" y la moneda indicada es MXN.\"");
        errorMessage.put("ErrCompTipoCambio005", "Clave=\"CFDI33114\" Nodo=\"Comprobante\" Mensaje=\"El campo TipoCambio se debe registrar cuando el campo Moneda tiene un valor distinto de MXN y XXX.\"");
        errorMessage.put("ErrCompTipoCambio006", "Clave=\"CFDI33116\" Nodo=\"Comprobante\" Mensaje=\"El campo TipoCambio no es un valor númerico\"");
        errorMessage.put("ErrCompTipoComprobante001", "Clave=\"CFDI33120\" Nodo=\"Comprobante\" Mensaje=\"El campo TipoDeComprobante, no contiene un valor del catálogo c_TipoDeComprobante.\"");
        errorMessage.put("ErrCompTipoComprobante002", "Clave=\"CFDI33110\" Nodo=\"Comprobante\" Mensaje=\"El TipoDeComprobante NO es I,E o N, y un concepto incluye el campo descuento. \"");
        errorMessage.put("ErrCompTotal001", "Clave=\"ErrCompTotal001\" Nodo=\"Comprobante\" Mensaje=\"El campo Total no debe ser negativo.\"");
        errorMessage.put("ErrCompTotal002", "Clave=\"ErrCompTotal002\" Nodo=\"Comprobante\" Mensaje=\"El campo Total no es númerico.\"");
        errorMessage.put("ErrCompSubTotal001", "Clave=\"ErrCompSubTotal001\" Nodo=\"Comprobante\" Mensaje=\"No se permiten campos negativos en el campo Subtotal\"");
        errorMessage.put("ErrCompSubTotal002", "Clave=\"CFDI33108\" Nodo=\"Comprobante\"  Mensaje=\"El TipoDeComprobante es T o P y el importe no es igual a 0, o cero con decimales.\"");
        errorMessage.put("ErrCompSubTotal003", "Clave=\"CFDI33105\" Nodo=\"Comprobante\"  Mensaje=\"El valor de este campo SubTotal excede la cantidad de decimales que soporta la moneda.\"");
        errorMessage.put("ErrCompSubTotal004", "Clave=\"ErrCompSubTotal004\" Nodo=\"Comprobante\"  Mensaje=\"El valor del campo SubTotal viene vacio o no es numerico.\"");
        errorMessage.put("ErrCompSubTotal005", "Clave=\"CFDI33109\" Nodo=\"Comprobante\"  Mensaje=\"El valor registrado en el campo Descuento no es menor o igual que el campo Subtotal.\"");
        errorMessage.put("ErrReceNumRegIdTrib001", "Clave=\"CFDI33139\" Nodo=\"Receptor\"  Mensaje=\"El Valor RegistroIdAtributario No Cumple Con El Patron Correspondiente.\"");
        errorMessage.put("ErrReceNumRegIdTrib002", "Clave=\"ErrReceNumRegIdTrib002\" Nodo=\"Receptor\"  Mensaje=\"No Se Ha Encontrado El Receptor Relacionado Con NumRegIdTrib El RFC Del Receptor Debe De Ser Un Generico Extranjero.\"");
        errorMessage.put("ErrCompFormaPago001", "Clave=\"CFDI33103\" Nodo=\"Comprobante\" Mensaje=\"El campo FormaPago no contiene un valor del catálogo c_FormaPago. \"");
        errorMessage.put("ErrCompMetodoPago001", "Clave=\"CFDI33121\" Nodo=\"Comprobante\" Mensaje=\"El campo MetodoPago, no contiene un valor del catálogo c_MetodoPago.\"");
        
        errorMessage.put("ErrCompUsoCFDI001", "Clave=\"CFDI33140\" Nodo=\"Receptor\" Mensaje=\"El campo UsoCFDI, no contiene un valor del catálogo c_UsoCFDI.\"");
        errorMessage.put("ErrCompResidenciaFiscal001", "Clave=\"CFDI33135\" Nodo=\"Receptor\" Mensaje=\"El valor del campo ResidenciaFiscal no puede ser MEX\"");
        errorMessage.put("ErrCompResidenciaFiscal002", "Clave=\"CFDI33133\" Nodo=\"Receptor\" Mensaje=\"El campo ResidenciaFiscal, no contiene un valor del catálogo c_Pais\"");
        
        errorMessage.put("ErrCompResidenciaFiscal003", "Clave=\"CFDI33135\" Nodo=\"Receptor\" Mensaje=\"El valor del campo ResidenciaFiscal no puede ser MEX\"");
        errorMessage.put("ErrCompResidenciaFiscal004", "Clave=\"CFDI33135\" Nodo=\"Receptor\" Mensaje=\"El valor del campo ResidenciaFiscal no puede ser MEX\"");
        
        errorMessage.put("ErrImpTraImporte001", "Clave=\"CFDI33195\" Nodo=\"Impuestos\" Mensaje=\"El campo Importe correspondiente a Traslado no es igual a la suma de los importes de los impuestos trasladados registrados en los conceptos donde el impuesto del concepto sea igual al campo impuesto de este elemento, verificar que el Concepto Registrado exista en la tabla EquivalenciaConceptoImpuesto.\"");
        errorMessage.put("ErrImpRetImporte001", "Clave=\"CFDI33189\" Nodo=\"Impuestos\" Mensaje=\"El Campo Importe Correspondiente A Retención No Es Igual A La Suma De Los Importes De Los Impuestos Retenidos Registrados En Los Conceptos Donde El Impuesto Sea Igual Al Campo Impuesto De Este Elemento, verificar que el Concepto Registrado exista en la tabla EquivalenciaConceptoImpuesto.\"");
        errorMessage.put("ErrImpRetConImporte001", "Clave=\"CFDI33166\" Nodo=\"Impuestos\" Mensaje=\"El Valor Del Campo Tipo Factor Que Corresponde A Retencion Debe Ser Distinto De Exento\"");
        errorMessage.put("ErrImpTraConTasaOCuota001", "Clave=\"CFDI33159\" Nodo=\"Impuestos\" Mensaje=\"El Valor Del Campo TasaOCuota Que Corresponde A Traslado No Contiene Un Valor Del Catalogo c_TasaOCuota\"");
        errorMessage.put("ErrImpRetConTasaOCuota001", "Clave=\"CFDI33167\" Nodo=\"Impuestos\" Mensaje=\"ElValorDelCampoTasaOCuotaQueCorrespondeARetencionNoContieneUnValorDelCatalogoc_TasaOCuota\"");
        errorMessage.put("ErrCompFecha001", "Clave=\"CFDI33101\" Nodo=\"Comprobante\" Mensaje=\"El Campo Fecha No Cumple Con El Patron Requerido.\"");
        errorMessage.put("ErrCompValUni001", "Clave=\"CFDI33147\" Nodo=\"Concepto\" Mensaje=\"El Valor Del Campo Valor Unitario Debe Ser Mayor Que Cero (0) Cuando El Tipo De Comprobante Es Ingreso, Egreso O Nomina.\"");
        errorMessage.put("ErrCompValUni002", "Clave=\"CFDI33146\" Nodo=\"Concepto\" Mensaje=\"El Valor Del Campo Valor Unitario Debe Tener Hasta La Cantidad De Decimales Que Soporte La Moneda.\"");
        errorMessage.put("ErrCompValUni003", "Clave=\"CFDI33147\" Nodo=\"Concepto\" Mensaje=\"El valor valor del campo ValorUnitario debe ser mayor que cero (0) cuando el tipo de comprobante es Ingreso, Egreso o Nomina\"");
        errorMessage.put("ErrCompValUni004", "Clave=\"ErrCompValUni004\" Nodo=\"Concepto\" Mensaje=\"El Valor Del Campo Valor Unitario Debe Ser Mayor Que Cero Cuando El Tipo De Comprobante Es Pago.\"");
        errorMessage.put("ErrConcImport001", "Clave=\"CFDI33148\" Nodo=\"Concepto\" Mensaje=\"El Valor Del Campo Importe Debe Tener Hasta La Cantidad De Decimales Que Soporta La Moneda.\"");
        errorMessage.put("ErrConcImport002", "Clave=\"ErrConcImport002\" Nodo=\"Concepto\" Mensaje=\"El valor del campo Descuento es mayor que el campo Importe, importe numero: {0}, descuento:{1}, importe:{2}\"");
        errorMessage.put("ErrConcImpueTra001", "Clave=\"CFDI33159\" Nodo=\"Concepto\" Mensaje=\"El Campo No Contiene Un Valor Del Catalogo Impuesto Para TasaOCuota Traslado.\"");
        errorMessage.put("ErrConcImpueTra002", "Clave=\"ErrConcImpueTra002\" Nodo=\"Concepto\" Mensaje=\"El Campo No Contiene Un Valor Del Catalogo Impuesto Para TasaOCuota Traslado\"");
        errorMessage.put("ErrConConcepTra001", "Clave=\"ErrConConcepTra001\" Nodo=\"Concepto\" Mensaje=\"No Se Encontro Un Concepto Traslados Para Buscar\"");
        errorMessage.put("ErrConImpRet001", "Clave=\"ErrConImpRet001\" Nodo=\"Concepto\" Mensaje=\"El Campo No Contiene Un Valor Del Catalogo Impuesto Para TasaOCuota Retencion\"");
        errorMessage.put("ErrTraImp001", "Clave=\"CFDI33155\" Nodo=\"Traslados\" Mensaje=\"El Campo Impuesto De Traslado No Contiene Un Valor Del Catalogo c_Impuesto\"");
        errorMessage.put("ErrRetImp001", "Clave=\"CFDI33181\" Nodo=\"Retencion\" Mensaje=\"El Valor Del Campo Total Impuestos Retenidos Debe Ser Igual A La Suma De Los Importes Registrados En El Elemento Hijo Retencion\"");
        errorMessage.put("ErrRetImp002", "Clave=\"CFDI33164\" Nodo=\"Retencion\" Mensaje=\"El Campo Impuesto De Retencion No Contiene Un Valor Del Catalogo c_Impuesto\"");
        errorMessage.put("ErrTraImp002", "Clave=\"ErrTraImp002\" Nodo=\"Traslados\" Mensaje=\"El Valor Seleccionado Debe Corresponder A Un Valor Del Catalogo Donde La Columna Impuesto Corresponda Con El Campo Impuesto Y La Coloumna Factor Corresponda Al Campo TipoFactor (Traslados)\"");
        errorMessage.put("ErrTraImp003", "Clave=\"CFDI33183\" Nodo=\"Traslados\" Mensaje=\"El Valor Del Campo Total Impuestos Traslado No Es Igual A La Suma De Los Importes Registrados En El Elemento Hijo Traslado\"");
        errorMessage.put("ErrTraImp004", "Clave=\"CFDI33182\" Nodo=\"Traslados\" Mensaje=\"El Valor Del Campo Importe Correspondiente A Traslado Debe Tener La Cantidad De Decimales Que Soporta La Moneda\"");
        errorMessage.put("ErrTraConTipFac001", "Clave=\"CFDI33156\" Nodo=\"Traslados\" Mensaje=\"El Valor Del Campo Tipo Factor Que Corresponde A Traslado No Contiene Un Valor Del Catalogo c_TipoFactor\"");
        errorMessage.put("ErrTraConImpu001", "Clave=\"CFDI33155\" Nodo=\"Traslados\" Mensaje=\"El Valor Del Campo Impuesto Que Corresponde A Traslado No Contiene Un Valor Del Catalogo c_Impuesto\"");
        errorMessage.put("ErrRetConTipFac001", "Clave=\"CFDI33165\" Nodo=\"Traslados\" Mensaje=\"El Valor Del Campo Tipo Factor Que Corresponde A Retencion No Contiene Un Valor Del Catalogo c_TipoFactor\"");
        errorMessage.put("ErrCompTipCam001", "Clave=\"CFDI33114\" Nodo=\"Comprobante\" Mensaje=\"El Campo Tipo Cambio Se Deberegistrar Cuando El Campo Moneda Tiene Un Valor Distinto De MXN Y XXX\"");
        errorMessage.put("ErrCompDesc001", "Clave=\"CFDI33109\" Nodo=\"Comprobante\" Mensaje=\"El Valor Registrado En El Campo Descuento No Es Menor O Igual Que El Campo Sub Total\"");
        errorMessage.put("ErrCompDesc002", "Clave=\"ErrCompDesc002\" Nodo=\"Comprobante\" Mensaje=\"El Campo Subtotal O Descuento Es Incorrecto Para Calcular Descuento\"");
        errorMessage.put("ErrCompDesc003", "Clave=\"CFDI33111\" Nodo=\"Comprobante\" Mensaje=\"El Valor Del Campo Descuento Excede La Cantidad De Decimales Que Soporte La Moneda\"");
        errorMessage.put("ErrCompTipMon001", "Clave=\"ErrCompTipMon001\" Nodo=\"Comprobante\" Mensaje=\"El Campo Tipo Cambio No Cumple Con El Patron Requerido\"");
        errorMessage.put("ErrCompTipMon002", "Clave=\"ErrCompTipMon002\" Nodo=\"Comprobante\" Mensaje=\"El Campo Tipo Cambio No Esta Dentro De Los Limites\"");
        errorMessage.put("ErrCompTipMon003", "Clave=\"ErrCompTipMon003\" Nodo=\"Comprobante\" Mensaje=\"El Campo Tipo Cambio No Tiene El Valor 1 Y La Moneda Indicada Es MXN\"");
        errorMessage.put("ErrCompTipMon004", "Clave=\"ErrCompTipMon004\" Nodo=\"Comprobante\" Mensaje=\"Tipo Cambio Incorrecto\"");
        errorMessage.put("ErrCompMon001", "Clave=\"CFDI33112\" Nodo=\"Comprobante\" Mensaje=\"El Campo Moneda No Tiene Un Valor En El Catalogo\"");
        errorMessage.put("ErrCompTipCom001", "Clave=\"ErrCompTipCom001\" Nodo=\"Comprobante\" Mensaje=\"El Campo tipo De Comprobante No Contiene Un Valor En El Cataloco c_Tipo De Comprobante\"");
        errorMessage.put("ErrCompForPag001", "Clave=\"CFDI33103\" Nodo=\"Comprobante\" Mensaje=\"El Campo Forma Pago No Contiene Un Valor Del Catalogo C_FormaPago\"");
        errorMessage.put("ErrCompMetPag001", "Clave=\"CFDI33121\" Nodo=\"Comprobante\" Mensaje=\"El Campo Metodo Pago No Contiene Un Valor Del Catalogo C_FormaPago\"");
        errorMessage.put("ErrCompSubTot001", "Clave=\"ErrCompSubTot001\" Nodo=\"Comprobante\" Mensaje=\"No Se Permiten Valores Negativos En Sub Total\"");
        errorMessage.put("ErrCompSubTot002", "Clave=\"CFDI33106\" Nodo=\"Comprobante\" Mensaje=\"El Valor Del Campo Sub Total Excede La Cantidad De Decimales Que Soporta La Moneda\"");
        errorMessage.put("ErrCompSubTot003", "Clave=\"ErrCompSubTot003\" Nodo=\"Comprobante\" Mensaje=\"Sub Total Incorrecto Viene Vacio O No Es Un Numero\"");
        errorMessage.put("ErrCompTipCom002", "Clave=\"CFDI33108\" Nodo=\"Comprobante\" Mensaje=\"El Tipo De Comprobante Es T o P y El Importe No Es Igual A (0) o Cero Con Decimales\"");
        errorMessage.put("ErrRecUsoCfdi001", "Clave=\"CFDI33140\" Nodo=\"Receptor\" Mensaje=\"El Campo UsoCFDI No Contiene Un Valor Del Catalogo c_UsoCFDI\"");
        errorMessage.put("ErrRecResFis001", "Clave=\"CFDI33135\" Nodo=\"Receptor\" Mensaje=\"El Valor Del Campo Residencia Fiscal No Puede Ser MEX\"");
        errorMessage.put("ErrRecResFis002", "Clave=\"CFDI33133\" Nodo=\"Receptor\" Mensaje=\"El Campo Residencia Fiscal No Contiene Un Valor Del Catalogo c_Pais\"");
        errorMessage.put("ErrRecRegId001", "Clave=\"CFDI33139\" Nodo=\"Receptor\" Mensaje=\"El Valor Registro NumRegIdTrib No Cumple Con El Patron Correspondiente\"");
        errorMessage.put("ErrRecRegId002", "Clave=\"ErrRecRegId002\" Nodo=\"Receptor\" Mensaje=\"No Se Ha Encontrado El Receptor Relacionado Con NumRegIdTrib El RFC Del Receptor Debe De Ser Un Generico Extranjero\"");
        errorMessage.put("ErrConTipCom001", "Clave=\"CFDI33147\" Nodo=\"Concepto\" Mensaje=\"El Valor Del Campo Valor Unitario Debe Ser Mayor Que Cero Cuando El Tipo De Comprobante Es Ingreso Egreso O Nomina\"");
        errorMessage.put("ErrConTipCom002", "Clave=\"CFDI33146\" Nodo=\"Concepto\" Mensaje=\"El Valor Del Campo Valor Unitario Debe Tener Hasta La Cantidad De Decimales Que Soporte La Moneda\"");
        errorMessage.put("ErrConTipCom003", "Clave=\"ErrConTipCom003\" Nodo=\"Concepto\" Mensaje=\"El Valor Del Campo Valor Unitario Debe Ser Mayor Que Cero Cuando El Tipo De Comprobante Es Traslado\"");
        errorMessage.put("ErrConTipCom004", "Clave=\"ErrConTipCom004\" Nodo=\"Concepto\" Mensaje=\"El valor valor del campo ValorUnitario debe ser mayor que cero (0) cuando el tipo de comprobante es Pago\"");
        errorMessage.put("ErrTraTasaOCuo001", "Clave=\"ErrTraTasaOCuo001\" Nodo=\"Traslados\" Mensaje=\"El Valor Seleccionado Debe Corresponder A Un Valor Del Catalogo Donde La Columna Impuesto Corresponda Con El Campo Impuesto Y La Coloumna Factor Corresponda Al Campo TipoFactor\"");
        errorMessage.put("ErrImpTotImpRet001", "Clave=\"CFDI33180\" Nodo=\"Impuestos\" Mensaje=\"El Valor Del Campo TotalImpuestosRetenidos Debe Tener Hasta La Cantidad De Decimales Que Soporte La Moneda\"");
        errorMessage.put("ErrImpTotImpTra002", "Clave=\"CFDI33182\" Nodo=\"Impuestos\" Mensaje=\"El Valor Del Campo TotalImpuestosTrasladados Debe Tener Hasta La Cantidad De Decimales Que Soporte La Moneda\"");
        errorMessage.put("ErrImpTotImpTra001", "Clave=\"CFDI33190\" Nodo=\"Impuestos\" Mensaje=\"Debe Existir El Campo TotalImpuestosTraslados\"");
        errorMessage.put("ErrCompSubTot004", "Clave=\"CFDI33107\" Nodo=\"Comprobante\" Mensaje=\"El TipoDeComprobante es I,E o N, el importe registrado en el campo  subtotal no es igual a la suma de los importes de los conceptos registrados\"");
        errorMessage.put("ErrEmiRegFis001", "Clave=\"CFDI33130\" Nodo=\"Emisor\" Mensaje=\"El campo RegimenFiscal, no contiene un valor del catálogo c_RegimenFiscal.\"");
        errorMessage.put("ErrConClavPro001", "Clave=\"CFDI33142\" Nodo=\"Emisor\" Mensaje=\"El campo ClaveProdServ, no contiene un valor del catálogo c_ClaveProdServ.\"");
        errorMessage.put("ErrConTraBas001", "Clave=\"CFDI33154\" Nodo=\"Traslado\" Mensaje=\"El Valor Del Campo Base Que Corresponde A Traslado Debe Ser Mayor Que Cero\"");
        errorMessage.put("ErrConTraBas002", "Clave=\"CFDI33153\" Nodo=\"Traslado\" Mensaje=\"El Valor Del Campo Base Que Corresponde A Traslado Debetener Hasta La Cantidad De Decimales Que Sporte La Moneda\"");
        errorMessage.put("ErrConRetImp001", "Clave=\"CFDI33164\" Nodo=\"Retencion\" Mensaje=\"El Valor Del Campo Impuesto Que Corresponde A Retencion No Contiene Un Valor Del Catalogo c_Impuesto\"");
        errorMessage.put("ErrConRetBas001", "Clave=\"CFDI33163\" Nodo=\"Retencion\" Mensaje=\"El Valor Del Campo Base Que Corresponde A Traslado Debe Ser Mayor Que Cero\"");
        errorMessage.put("ErrConRetBas002", "Clave=\"CFDI33162\" Nodo=\"Retencion\" Mensaje=\"El Valor Del Campo Base Que Corresponde A Traslado Debetener Hasta La Cantidad De Decimales Que Sporte La Moneda\"");
        errorMessage.put("ErrCFDIRel001", "Clave=\"CFDI33168\" Nodo=\"CfdiRelacionados\" Mensaje=\"El Valor Del Campo Tipo Relación Viene Vacio o no se encuentra en el catálogo c_TipoRelacion\"");
        errorMessage.put("ErrCFDIRel002", "Clave=\"CFDI33169\" Nodo=\"CfdiRelacionado\" Mensaje=\"El Valor Del Campo UUID Viene Vacio o no concuerda con el patrón [a-f0-9A-F]{8}-[a-f0-9A-F]{4}-[a-f0-9A-F]{4}-[a-f0-9A-F]{4}-[a-f0-9A-F]{12}\"");
	}
	// Validacion Tipo de comprobante AMDA
		public static String findTipoComprobante(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			
			String response = "";
//			 System.out.println("Entrando a findTipoComprobante");
//			 System.out.println("Tamaño lista catalogos; "+ mapCatalogos.size());
//			 System.out.println("Valor del tipoDeComprobante a buscar: "+ value);
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				for(int i=0; i<mapCatalogos.get("TipoDeComprobante").size(); i++){
					if(mapCatalogos.get("TipoDeComprobante").get(i).getVal1().equalsIgnoreCase(value)){
						response = mapCatalogos.get("TipoDeComprobante").get(i).getVal1();
						break;
					}else{
						response = "tipoDeComprobanteIncorrecto";
					}
				}
			}else{
				response = "tipoDeComprobanteIncorrecto";
			}
			
			return response;
			
		}
		
		// Validacion Tipo de comprobante por descripcion AMDA
			public static String findTipoComprobanteByDescription(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
				
				String response = "";
//				 System.out.println("Entrando a findTipoComprobante");
//				 System.out.println("Tamaño lista catalogos; "+ mapCatalogos.size());
//				 System.out.println("Valor del tipoDeComprobante a buscar: "+ value);
				if(mapCatalogos.size() > 0 && value.trim() != ""){
					for(int i=0; i<mapCatalogos.get("TipoDeComprobante").size(); i++){
						if(mapCatalogos.get("TipoDeComprobante").get(i).getVal2().equalsIgnoreCase(value)){
							response = mapCatalogos.get("TipoDeComprobante").get(i).getVal1();
							break;
						}else{
							response = "tipoDeComprobanteIncorrecto";
						}
					}
				}else{
					response = "tipoDeComprobanteIncorrecto";
				}
				
				return response;
				
			}
		
		// Validacion Encuentra Valor Maximo en Tipo de Comprobante AMDA
		public static String findValMaxTipoComprobanteByTotal(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			String response = "";
			
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				for(int i=0; i<mapCatalogos.get("TipoDeComprobante").size(); i++){
					if(mapCatalogos.get("TipoDeComprobante").get(i).getVal1().equalsIgnoreCase(value)){
						response = mapCatalogos.get("TipoDeComprobante").get(i).getVal3();
						break;
					}else{
						response = "vacio";
					}
				}
			}else{
				response = "vacio";
			}
			
			return response;
		}
		
		// Validacion Encuentra Valor Abreviado de el Pais AMDA
		public static String findValPais(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			String response = "";
				
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				for(int i=0; i<mapCatalogos.get("Pais").size(); i++){
					if(mapCatalogos.get("Pais").get(i).getVal2().equalsIgnoreCase(value)){
						response = mapCatalogos.get("Pais").get(i).getVal1();
						//System.out.println("Pais encontrado: recibido - "+value+", encontrado - "
						//		+mapCatalogos.get("Pais").get(i).getVal2());
						break;
					}else{
						//System.out.println("Pais no encontrado: recibido - "+value+", comparado con - "
						//		+mapCatalogos.get("Pais").get(i).getVal2());
						response = "vacio";
					}
				}
			}else{
				response = "vacio";
			}
				
			return response;
		}
		
		// Validacion Encuentra Valor Equivalencia de el Pais AMDA
		public static String findEquivalenciaPais(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			String response = "";
				
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				for(int i=0; i<mapCatalogos.get("EquivalenciaPais").size(); i++){
					if(mapCatalogos.get("EquivalenciaPais").get(i).getVal2().equalsIgnoreCase(value)){
						response = mapCatalogos.get("EquivalenciaPais").get(i).getVal1();
						break;
					}else{
						response = "vacio";
					}
				}
			}else{
				response = "vacio";
			}
				
			return response;
		}
		
		// Validacion Encuentra Valor del codigo de Regimen Fiscal AMDA
		public static String findRegFiscalCode(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			String response = "";
			
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				for(int i=0; i<mapCatalogos.get("RegimenFiscal").size(); i++){
					if(mapCatalogos.get("RegimenFiscal").get(i).getVal2().equalsIgnoreCase(value)){
						response = mapCatalogos.get("RegimenFiscal").get(i).getVal1();
//						System.out.println("*** response AMDA: " + response);
						if(response.contains(".")){
							//System.out.println("*** response Dentro IF AMDA: " + response);
							String words[] = response.split("\\.");
							response = words[0];
							//System.out.println("*** response Dentro IF despues AMDA: " + response);
						}
						
						break;
					}else{
						response = "vacio";
					}
				}
			}else{
				response = "vacio";
			}
			
			return response;
		}	
		
		//Validacion Encuentra Valor del Numero de Registro Tributario AMDA
		public static String findNumRegIdTrib(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			String response = "000000000";
			
//			if(mapCatalogos.size() > 0 && value.trim().length() > 0){
//				for(int i=0; i<mapCatalogos.get("NumRegIdTrib").size(); i++){
//					System.out.println("findNumRegIdTrib Catalogo:  " + mapCatalogos.get("NumRegIdTrib").get(i).getVal3());
//					if(mapCatalogos.get("NumRegIdTrib").get(i).getVal3() != null){
//						if(mapCatalogos.get("NumRegIdTrib").get(i).getVal3().equalsIgnoreCase(value)){
//							response = mapCatalogos.get("NumRegIdTrib").get(i).getVal1();
//							break;
//						}else{
//							response = "vacio";
//						}
//					}else{
//						response = "vacio";
//					}
//				}
//			}else{
//				response = "vacio";
//			}
			
			return response;
		}
		
		//Validacion Encuentra Valor del Patron del RFC expresado en el catalogo de Pais AMDA
		public static String findPatternRFCPais(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			String response = "";
			
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				for(int i=0; i<mapCatalogos.get("Pais").size(); i++){
					if(mapCatalogos.get("Pais").get(i).getVal1().equalsIgnoreCase(value)){
						response = mapCatalogos.get("Pais").get(i).getVal4();
						break;
					}else{
						response = "vacio";
					}
				}
			}else{
				response = "vacio";
			}
			
			return response;
		}
		
		//Validacion Encuentra Valor Clave de la Unidad AMDA
		public static String findValClaveUnidad(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			String response = "";
			
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				for(int i=0; i<mapCatalogos.get("ClaveUnidad").size(); i++){
					if(mapCatalogos.get("ClaveUnidad").get(i).getVal2().equalsIgnoreCase(value)){
						response = mapCatalogos.get("ClaveUnidad").get(i).getVal1();
						break;
					}else{
						response = "vacio";
					}
				}
			}else{
				response = "vacio";
			}
			
			return response;
		}
		
		//Validacion Encuentra Valor Clave del Impuesto AMDA
		public static String findValClaveImpuesto(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			String response = "";
			
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				for(int i=0; i<mapCatalogos.get("Impuesto").size(); i++){
					if(mapCatalogos.get("Impuesto").get(i).getVal2() != null){
						if(mapCatalogos.get("Impuesto").get(i).getVal2().equalsIgnoreCase(value)){
							response = mapCatalogos.get("Impuesto").get(i).getVal1();
							break;
						}else{
							response = "vacio";
						}
					}else{
						response = "vacio";
					}

				}
			}else{
				response = "vacio";
			}
			
			return response;
		}
		
		//Validacion Encuentra Valor del Impuesto por Clave AMDA
		public static String findValImpuestoByClave(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			String response = "";
			
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				for(int i=0; i<mapCatalogos.get("Impuesto").size(); i++){
					String impValCat = mapCatalogos.get("Impuesto").get(i).getVal1();
					if(impValCat != null){
						if(impValCat.equalsIgnoreCase(value)){
							response = mapCatalogos.get("Impuesto").get(i).getVal1();
							break;
						}else{
							response = "vacio";
						}
					}else{
						response = "vacio";
					}
					
				}
			}else{
				response = "vacio";
			}
			
			return response;
		}
		
		//Validacion Encuentra Descripcion del Impuesto por Clave AMDA
		public static String findDescImpuestoByClave(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			String response = "";
			
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				for(int i=0; i<mapCatalogos.get("Impuesto").size(); i++){
					if(mapCatalogos.get("Impuesto").get(i).getVal1().equalsIgnoreCase(value)){
						response = mapCatalogos.get("Impuesto").get(i).getVal2();
						break;
					}else{
						response = "vacio";
					}
				}
			}else{
				response = "vacio";
			}
			
			return response;
		}
		
		//Validacion Encuentra Valor del TipoFactor por descripcion AMDA
		public static String findValTipoFactorByDesc(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			String response = "";
			
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				for(int i=0; i<mapCatalogos.get("TipoFactor").size(); i++){
					if(mapCatalogos.get("TipoFactor").get(i).getVal1().equalsIgnoreCase(value)){
						response = mapCatalogos.get("TipoFactor").get(i).getVal1();
						break;
					}else{
						response = "vacio";
					}
				}
			}else{
				response = "vacio";
			}
			
			return response;
		}
		
		//Validacion Encuentra Valor del TipoFactor por descripcion AMDA
		public static String findValTipoFactorByDescRet(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			String response = "";
			
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				for(int i=0; i<mapCatalogos.get("TipoFactor").size(); i++){
//					if(mapCatalogos.get("TipoFactor").get(i).getVal1().equalsIgnoreCase(value) && !mapCatalogos.get("TipoFactor").get(i).getVal1().equalsIgnoreCase("Exento")){
					if(mapCatalogos.get("TipoFactor").get(i).getVal1().equalsIgnoreCase(value)){
						response = mapCatalogos.get("TipoFactor").get(i).getVal1();
						break;
					}else{
						response = "vacio";
					}
				}
			}else{
				response = "vacio";
			}
			
			return response;
		}
		
		//Validacion Encuentra Valor Maximo de Tasa o Cuota del catalogo TasaOCuota AMDA
		public static String findValMaxTasaOCuota(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value1, String value2){
			String response = "";
//			System.out.println("Validacion findValMaxTasaOCuota AMDA : " + value1 + " : " + value2);
			if(mapCatalogos.size() > 0 && value1.trim() != "" && value2.trim() != ""){
				for(int i=0; i<mapCatalogos.get("TasaOCuota").size(); i++){
					if(mapCatalogos.get("TasaOCuota").get(i).getVal4() != null && mapCatalogos.get("TasaOCuota").get(i).getVal5() != null){
						if(mapCatalogos.get("TasaOCuota").get(i).getVal4().equalsIgnoreCase(value1) && mapCatalogos.get("TasaOCuota").get(i).getVal5().equalsIgnoreCase(value2)){
							response = mapCatalogos.get("TasaOCuota").get(i).getVal3();
							break;
						}else{
							response = "vacio";
						}
					}else{
						response = "vacio";
					}
					
				}
			}else{
				response = "vacio";
			}
			
			return response;
		}
		
		// descImp(Iva, IEPS, ISR), Descripcion(Tasa, Cuota O Exento), valor de la tasa, descripcion(Traslado O retencion)
		//Validacion Si se Encuentra un Valor de Tasa o Cuota del catalogo TasaOCuota AMDA
		public static String findTasaOCuotaExist(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String descImp, String desc, String valorTasa, String descTraORet){
			String response = "";
//			System.out.println("Validacion findTasaOCuotaExist AMDA : " + descImp + " : " + desc + " : " + valorTasa + " : " + descTraORet);
			if(mapCatalogos.size() > 0 && descImp.trim() != "" && desc.trim() != "" && valorTasa.trim() != "" && descTraORet.trim() != ""){
				for(int i=0; i<mapCatalogos.get("TasaOCuota").size(); i++){
					String uno = mapCatalogos.get("TasaOCuota").get(i).getVal4() ;
					String dos = mapCatalogos.get("TasaOCuota").get(i).getVal5();
					String tres = mapCatalogos.get("TasaOCuota").get(i).getVal3();
//					System.out.println("Validacion findTasaOCuotaExist Uno Dos Tres AMDA : " + uno + " : " + dos + " : " + tres );
					if(uno != null && dos != null && tres != null){
						if(uno.equalsIgnoreCase(descImp) && dos.equalsIgnoreCase(desc) && tres.equalsIgnoreCase(valorTasa)){
							response = mapCatalogos.get("TasaOCuota").get(i).getVal3();
							break;
						}else{
							response = "vacio";
						}
					}else{
						response = "vacio";
					}
				}
			}else{
				response = "vacio";
			}
			
			return response;
		}
		
		//Validacion Encuentra Valor Maximo de Tasa o Cuota del catalogo TasaOCuota para Traslado AMDA
		public static String findValMaxTasaOCuotaTraslado(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value1, String value2){
			String response = "";
			String perce = "";
			//System.out.println("Validacion findValMaxTasaOCuotaTraslado AMDA : " + value1 + " : " + value2);
			if(value1.equalsIgnoreCase("IEPS")){
				perce = "0.265";
			}else{
				perce = "0.16";
			}
			if(mapCatalogos.size() > 0 && value1.trim() != "" && value2.trim() != ""){
				for(int i=0; i<mapCatalogos.get("TasaOCuota").size(); i++){
					if(mapCatalogos.get("TasaOCuota").get(i).getVal4() != null && mapCatalogos.get("TasaOCuota").get(i).getVal5() != null && mapCatalogos.get("TasaOCuota").get(i).getVal3() !=  null){
						if(mapCatalogos.get("TasaOCuota").get(i).getVal4().equalsIgnoreCase(value1) && mapCatalogos.get("TasaOCuota").get(i).getVal5().equalsIgnoreCase(value2) && mapCatalogos.get("TasaOCuota").get(i).getVal3().equalsIgnoreCase(perce)){
							response = mapCatalogos.get("TasaOCuota").get(i).getVal3();
							break;
						}else{
							response = "vacio";
						}
					}else{
						response = "vacio";
					}
				}
			}else{
				response = "vacio";
			}
			//System.out.println("Validacion findValMaxTasaOCuotaTraslado AMDA Saliendo : " + response);
			return response;
		}
		
		//Validacion Encuentra Valor de la Moneda AMDA
		public static String findEquivalenciaMoneda(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			String response = "";
//			System.out.println("Equivalencia Moneda A Buscar : " + value);
//			System.out.println("Mapa Contenido EquivalenciaMoneda : " + mapCatalogos.get("EquivalenciaMoneda").size());
			if(mapCatalogos.size() > 0 && value.trim() != ""){
//				System.out.println("Dentro If Equivalencia Moneda A Buscar : " + value);
				for(int i=0; i<mapCatalogos.get("EquivalenciaMoneda").size(); i++){
//					System.out.println("Dentro For Equivalencia Moneda A Buscar : " + value);
					if(mapCatalogos.get("EquivalenciaMoneda").get(i).getVal2() != null){
//						System.out.println("Dentro no null Equivalencia Moneda A Buscar : " + value);
						if(mapCatalogos.get("EquivalenciaMoneda").get(i).getVal2().equalsIgnoreCase(value)){
							response = mapCatalogos.get("EquivalenciaMoneda").get(i).getVal1();	
							break;
						}else{
							response = "vacio";
						}
					}else{
						response = "vacio";
					}

				}
			}else{
				response = "vacio";
			}
			
			return response;
		}
		
		//Validacion Encuentra Valor de la Moneda en catalogo Moneda AMDA
		public static String findMonedaCatalogo(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			String response = "";
			
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				for(int i=0; i<mapCatalogos.get("Moneda").size(); i++){
					if(mapCatalogos.get("Moneda").get(i).getVal2().equalsIgnoreCase(value)){
						response = mapCatalogos.get("Moneda").get(i).getVal1();	
						break;
					}else{
						response = "vacio";
					}
				}
			}else{
				response = "vacio";
			}
			
			return response;
		}
		
		//Validacion Encuentra Valor Tipo cambio a travez de moneda AMDA
		public static String findTipoCambioByMoneda(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			String response = "";
			
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				for(int i=0; i<mapCatalogos.get("EquivalenciaTipoCambio").size(); i++){
					if(mapCatalogos.get("EquivalenciaTipoCambio").get(i).getVal1().equalsIgnoreCase(value)){
						response = mapCatalogos.get("EquivalenciaTipoCambio").get(i).getVal3();
						break;
					}else{
						response = "vacio";
					}
				}
			}else{
				response = "vacio";
			}
			
			return response;
		}
		
		//Validacion Encuentra Valor Porcentaje de catalogo moneda AMDA
		public static String findTipoCambioPorcentaje(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value, String tipoCam){
			String response = "";
			String porcentaje = "";
			String tipoCamCatalog = "";
			double porcentajeDoub = 0.00;
			double tipoCamDoub = 0.00;
			double tipoCamCatalogDoub = 0.00;
			double total = 0.00;
			double totalArriba = 0.00;
			double totalAbajo = 0.00;
			
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				for(int i=0; i<mapCatalogos.get("Moneda").size(); i++){
					if(mapCatalogos.get("Moneda").get(i).getVal1().equalsIgnoreCase(value)){
						response = mapCatalogos.get("Moneda").get(i).getVal3();
						porcentaje = mapCatalogos.get("Moneda").get(i).getVal4();
						tipoCamCatalog = mapCatalogos.get("Moneda").get(i).getVal5();
//						System.out.println("Porcentaje AMDA: " + porcentaje);
//						System.out.println("Tipo Cambio AMDA: " + tipoCam);
//						System.out.println("Tipo Cambio Catalogo AMDA: " + tipoCamCatalog);
//						total = 120*(50.0f/100.0f); // 50% de 120
						
						
						try{
							porcentajeDoub = Double.parseDouble(porcentaje);
//							System.out.println("Porcentaje Double AMDA: " + porcentajeDoub);
							tipoCamDoub = Double.parseDouble(tipoCam);
//							System.out.println("Tipo Cambio Double AMDA: " + tipoCamDoub);
							tipoCamCatalogDoub = Double.parseDouble(tipoCamCatalog);
//							System.out.println("Tipo Cambio Catalogo Double AMDA: " + tipoCamCatalogDoub);
							
							total = tipoCamCatalogDoub*(porcentajeDoub/100.0);
//							System.out.println("Total AMDA PORCENTAJE: " + total);
							
							totalArriba = tipoCamCatalogDoub+total;
//							System.out.println("TotalArriba AMDA PORCENTAJE: " + totalArriba);
							totalAbajo = tipoCamCatalogDoub-total;
//							System.out.println("TotalAbajo AMDA PORCENTAJE: " + totalAbajo);
							
							if(tipoCamDoub >= totalAbajo && tipoCamDoub <= totalArriba ){
								response = "OK";
							}else{
								response = "vacio";
							}

//							System.out.println("Decimales moneda Conversion numerico: " + response);
						}catch(NumberFormatException e){
							System.out.println("Problema al convertir datos de tipoCambio: ");
							response = "vacio";
						}
						
						break;
					}else{
						response = "vacio";
					}
				}
			}else{
				response = "vacio";
			}
			
			return response;
		}
		
		//Validacion Encuentra Valor de Decimales soportados por la Moneda AMDA
		public static Integer findDecimalesMoneda(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			Integer response = 0;
			String responseFile = "";
			
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				for(int i=0; i<mapCatalogos.get("Moneda").size(); i++){
					if(mapCatalogos.get("Moneda").get(i).getVal1().equalsIgnoreCase(value)){
						responseFile = mapCatalogos.get("Moneda").get(i).getVal3();
						try{
							Integer intVal = Integer.parseInt(responseFile);
							response = intVal;
							//System.out.println("Decimales moneda Conversion numerico: " + response);
						}catch(NumberFormatException e){
							System.out.println("Decimales moneda No era numero la respuesta: " + responseFile);
						}
						break;
					}else{
						response = 0;
					}
				}
			}else{
				response = 0;
			}
			
			return response;
		}
		
		//Agrega equivalencia de conceptos con traslados en conceptos AMDA
		public static Map<String, ArrayList<CatalogosDom>> arregloConceptos(Map<String, ArrayList<CatalogosDom>> mapCatalogos){
			Map<String, ArrayList<CatalogosDom>> response = mapCatalogos;
			//System.out.println("Equvalencia arreglo con: " + mapCatalogos.size());
			String idCatalog = "";
			ArrayList<CatalogosDom> catalogDetail = new ArrayList<CatalogosDom>();//
			if(mapCatalogos.size() > 0 ){
				
				for(int i=0; i<mapCatalogos.get("EquivalenciaConceptoImpuesto").size(); i++){
					
					idCatalog = mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal5();
//					ArrayList<CatalogosDom> catalogDetail = new ArrayList<CatalogosDom>();//
					catalogDetail.add(mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i));
					
				}
				response.put(idCatalog, catalogDetail);
				
//				HashMap<String, List<CatalogosDom>> lstCatalogs = new HashMap();
//	            while (mapCatalogos) {
//	                String idCatalog = mapCatalogos.get("EquivalenciaImpuestosTraslados");
//	                List<CatalogosDom> catalogDetail = lstCatalogs.get(idCatalog);
//	                if (catalogDetail == null) {
//	                    catalogDetail = new ArrayList<>();
//	                }
//	                catalogDetail.add(catalogoDetalle);
//	                lstCatalogs.put(idCatalog, catalogDetail);
//	            }

			}
			//System.out.println("Salida Equvalencia arreglo con: " + response.size());
			return response;
		}
		
		//Encuentra los traslados en los catalogos de equivalencia AMDA
		public static Map<String, Object> findTraslados(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String importeCon, String descCon, Integer decimalesMoneda, String tipoComprobante){
			Map<String, Object> responseMap = new LinkedHashMap<String, Object>();
			String response = "";
			String valTasa = "";
			Double valTasaNum = 0.00 ;
			Double importeConNum = 0.00 ;
			Double importeTrasladoMul = 0.00;
			String nodocon = "";
			Double sumTotal = 0.00 ;
			Double sumTotalIva = 0.00;
			Double sumTotalIsr= 0.00;
			Double sumTotalIeps = 0.00;
			boolean exento = false;
			boolean noExentoT = false;
//			System.out.println("findTraslados Inicio TipoComprobante " + tipoComprobante);
//			System.out.println("findTraslados Inicio " + descCon);
//			System.out.println("findTraslados Inicio " + mapCatalogos.get("EquivalenciaConceptoImpuesto").size());
			if(mapCatalogos.size() > 0 && descCon.trim().length() > 0){
				
				for(int i=0; i<mapCatalogos.get("EquivalenciaConceptoImpuesto").size(); i++){
//					System.out.println("findTraslados Dentro For " + mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal5() + " : " + mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal4());
					if(mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal5().equalsIgnoreCase(descCon) && mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal4().equalsIgnoreCase("Traslado")){
						
						if(mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal3() != null){
							//System.out.println("Val Tasa No nulo");
							if(mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal3().length() > 0){
								valTasa = mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal3();
							}else{
								//System.out.println("Val Tasa vacio length");
								valTasa = "vacio";
							}
						}else{
							//System.out.println("Val Tasa nulo");
							valTasa = "vacio";
						}

						try{
							if(!valTasa.equalsIgnoreCase("vacio")){
								valTasaNum = Double.parseDouble(valTasa);
								//System.out.println("Val Tasa " + valTasaNum);
							}else{
//								valTasaNum = Double.parseDouble(valTasa);
								//System.out.println("Val Tasa Vacio " + valTasa);
							}
							valTasaNum = Double.parseDouble(valTasa);
//							System.out.println("Val Tasa " + valTasaNum);
//							System.out.println("Val Impor Conce Antes " + importeCon);
							importeConNum = Double.parseDouble(importeCon);
//							System.out.println("Val Impor Conce " + valTasaNum);
//							importeTrasladoMul = (valTasaNum*importeConNum) + importeConNum;
							importeTrasladoMul = (valTasaNum*importeConNum);
//							System.out.println("Val Impor Traslado " + importeTrasladoMul);
							String tipoFactorValueMontos = findValTipoFactorByDesc(mapCatalogos, mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal2());
//							System.out.println("Val Impor tipoFactorValueMontos Tra O:  " + tipoFactorValueMontos);
							if(!tipoFactorValueMontos.equalsIgnoreCase("Exento")){
								if(mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal1().equalsIgnoreCase("001")){
									responseMap.put("ISR", true);
									sumTotalIsr = sumTotalIsr + importeTrasladoMul;
//									System.out.println("Val sumTotalIsr Traslado " + sumTotalIsr);
								}else if(mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal1().equalsIgnoreCase("002")){
									responseMap.put("IVA", true);
									sumTotalIva = sumTotalIva + importeTrasladoMul;
//									System.out.println("Val sumTotalIva Traslado " + sumTotalIva);
								}else if(mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal1().equalsIgnoreCase("003")){
									responseMap.put("IEPS", true);
									sumTotalIeps = sumTotalIeps + importeTrasladoMul;
//									System.out.println("Val sumTotalIeps Traslado " + sumTotalIeps);
								}
							}
							
							
							String baseCamp = "";
							if(UtilCatalogos.decimalesValidationMsj(importeCon, decimalesMoneda)){
								if(tipoComprobante.equalsIgnoreCase("T") || tipoComprobante.equalsIgnoreCase("P")){
									baseCamp = "Base=\"" + importeCon;
								}else{
									if(importeConNum > 0){
										baseCamp = "Base=\"" + importeCon;
									}else{
										baseCamp = "ErrConTraBas001=\"" + importeCon;
									}
								}
								
							}else{
								baseCamp = "ErrConTraBas002=\"" + importeCon;
							}
							
							String impuestoLine = "";
							if(mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal1() != null){
								if(!findValImpuestoByClave(mapCatalogos, mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal1()).equalsIgnoreCase("vacio")){
									impuestoLine = "\" Impuesto=\"" + mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal1() ;
								}else{
									impuestoLine = "\" ErrTraConImpu001=\"" + mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal1() ;
								}
							}else{
								impuestoLine = "\" ErrTraConImpu001=\"" + mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal1() ;
							}
							
							String tipoFactorLine = "";
							String tipoFactorValue = "";
							String tasaOCutoaLine = "";
							String importeLine = "";
							if(mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal2() != null){
								tipoFactorValue= findValTipoFactorByDesc(mapCatalogos, mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal2());
								if(!tipoFactorValue.equalsIgnoreCase("vacio")){
									tipoFactorLine = "\" TipoFactor=\"" + tipoFactorValue;
									String descImp = findDescImpuestoByClave(mapCatalogos, mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal1());
									if(!tipoFactorValue.equalsIgnoreCase("Exento")){
										noExentoT = true;
										// descImp(Iva, IEPS, ISR), Descripcion(Tasa, Cuota O Exento), valor de la tasa, descripcion(Traslado O retencion)
										//Buscando TasaOcuotaCatalogo findTasaOcuotaVal(mapCatalogos, descImp, mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal2(), valTasa, mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal4())
//									System.out.println("Antes de Validacion findTasaOCuotaExist AMDA : " + descImp);
//									System.out.println("Antes de Validacion findTasaOCuotaExist AMDA : " + mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal2());
//									System.out.println("Antes de Validacion findTasaOCuotaExist AMDA : " + valTasa);
//									System.out.println("Antes de Validacion findTasaOCuotaExist AMDA : " + mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal4());
									String findValTasa = findTasaOCuotaExist(mapCatalogos, descImp, mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal2(), valTasa, mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal4());
										if(!findValTasa.equalsIgnoreCase("vacio")){
											tasaOCutoaLine = "\" TasaOCuota=\"" + Util.completeZeroDecimals(valTasa, 6);
										}else{
//											tasaOCutoaLine = "\" ElValorDelCampoTasaOCuotaQueCorrespondeATrasladoNoContieneUnValorDelCatalogoc_TasaOCuota=\"" + Util.completeZeroDecimals(valTasa, 6);
											tasaOCutoaLine = "\" ErrImpTraConTasaOCuota001=\"" + Util.completeZeroDecimals(valTasa, 6);
										}
										
										importeLine = "\" Importe=\"" + decimales(importeTrasladoMul.toString(), decimalesMoneda);
										System.out.println("DecimalesMoneda = " +decimalesMoneda);
									}else{
										exento = true;
									}
									
								}else{
									tipoFactorLine = "\" ErrTraConTipFac001=\"" + tipoFactorValue;
								}
							}else{
								tipoFactorLine = "\" ErrTraConTipFac001=\"" + mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal2();
							}
							
							nodocon +=  "\n<cfdi:Traslado " + baseCamp +
									   impuestoLine +
									   tipoFactorLine +
									   tasaOCutoaLine +
									   importeLine + "\" " +
									   " />" ;
//							nodocon +=  "\n<cfdi:Traslado Base=\"" + importeCon +
//									   "\" Impuesto=\"" + mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal1() +
//									   "\" TipoFactor=\"" + mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal2() +
//									   "\" TasaOCuota=\"" + Util.completeZeroDecimals(valTasa, 6) +
//									   "\" Importe=\"" + decimales(importeTrasladoMul.toString(), decimalesMoneda) + "\" " +
//									   " />" ;
							sumTotal += importeTrasladoMul;
//							System.out.println("Val Suma Total Traslado " + sumTotal);
//							System.out.println("Val NodoCon Traslado " + nodocon);
						}catch(NumberFormatException e){
							logger.error(e);
//							System.out.println("No es numero findTraslados: " + valTasa);
						}
//						response = response + nodocon;
//						break;
					}else{
//						if(mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal5().equalsIgnoreCase(descCon) && !mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal4().equalsIgnoreCase("Retencion")){
//							nodocon += "\n<cfdi:Traslado Base=\"" + "0.00" +
//							   "\" Impuesto=\"" + "000" +
//							   "\" TipoFactor=\"" + "Tasa" +
//							   "\" ElValorDelCampoTasaOCuotaQueCorrespondeATrasladoNoContieneUnValorDelCatalogoc_TasaOCuota=\"" + "0.000" +
//							   "\" Importe=\"" + "0.00" + "\" " +
//							   " />" ;
//						}else if(!mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal5().equalsIgnoreCase(descCon) && mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal4().equalsIgnoreCase("Retencion")){
//							nodocon += "\n<cfdi:Traslado Base=\"" + "0.00" +
//									   "\" ErrTraImp001=\"" + "000" +
//									   "\" TipoFactor=\"" + "Tasa" +
//									   "\" TasaOCuota=\"" + "0.000" +
//									   "\" Importe=\"" + "0.00" + "\" " +
//									   " />" ;
//						}else{
							response = "";
//						}
						
						// La tasaOCuota no se encontro en el catalogo
//						nodocon = "\n<cfdi:Traslado Base=\"" + "0.00" +
//								   "\" ErrTraImp001=\"" + "000" +
//								   "\" TipoFactor=\"" + "Tasa" +
//								   "\" ErrImpTratConTasaOCuota001=\"" + "0.000" +
//								   "\" Importe=\"" + "0.00" + "\" " +
//								   " />" ;
//						response = "";
					}
					response = nodocon;
				}
				
//				for(int i=0; i<mapCatalogos.get(descCon).size(); i++){
//					System.out.println("findTraslados Dentro For " + mapCatalogos.get(descCon).get(i).getVal5());
//					if(mapCatalogos.get(descCon).get(i).getVal5().equalsIgnoreCase(descCon) && mapCatalogos.get(descCon).get(i).getVal4().equalsIgnoreCase("Traslado")){
//						valTasa = mapCatalogos.get(descCon).get(i).getVal3();
//						try{
//							valTasaNum = Double.parseDouble(valTasa);
//							System.out.println("Val Tasa " + valTasaNum);
//							importeConNum = Double.parseDouble(importeCon);
//							System.out.println("Val Impor Conce " + valTasaNum);
//							importeTrasladoMul = (valTasaNum*importeConNum) + importeConNum;
//							System.out.println("Val Impor Traslado " + importeTrasladoMul);
//							
//							nodocon = "\n<cfdi:Traslado Base=\"" + importeCon +
//									   "\" Impuesto=\"" + mapCatalogos.get(descCon).get(i).getVal1() +
//									   "\" TipoFactor=\"" + mapCatalogos.get(descCon).get(i).getVal2() +
//									   "\" TasaOcuota=\"" + valTasa +
//									   "\" Importe=\"" + importeTrasladoMul.toString() + "\" " +
//									   " />" ;
//							System.out.println("Val NodoCon Traslado " + nodocon);
//						}catch(NumberFormatException e){
//							System.out.println("No es numero findTraslados: " + valTasa);
//						}
//						response = response + nodocon;
//						break;
//					}else{
//						response = "";
//					}
//				}
			}else{
				response = "";
			}

			responseMap.put("valNodoStr", response);
			responseMap.put("sumaTotal", decimales(sumTotal.toString(), decimalesMoneda));
			responseMap.put("sumTotalIsr", decimales(sumTotalIsr.toString(), decimalesMoneda));
			responseMap.put("sumTotalIva", decimales(sumTotalIva.toString(), decimalesMoneda));
			responseMap.put("sumTotalIeps", decimales(sumTotalIeps.toString(), decimalesMoneda));
			responseMap.put("exento", exento);
			responseMap.put("noExentoT", noExentoT);
//			System.out.println("Response Get Traslado valNodoStr: " + responseMap.get("valNodoStr"));
//			System.out.println("Response Get Traslado: " + responseMap.get("sumaTotal"));
//			System.out.println("Response Get Traslado sumTotalIsr: " + responseMap.get("sumTotalIsr"));
//			System.out.println("Response Get Traslado sumTotalIva: " + responseMap.get("sumTotalIva"));
//			System.out.println("Response Get Traslado sumTotalIeps: " + responseMap.get("sumTotalIeps"));
			return responseMap;
		}
		
		//Encuentra las retenciones en los catalogos de equivalencia AMDA
		public static Map<String, Object> findRetencion(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String importeCon, String descCon, Integer decimalesMoneda, String tipoComprobante){
			Map<String, Object> responseMap = new LinkedHashMap<String, Object>();
			String response = "";
			String valTasa = "";
			Double valTasaNum ;
			Double importeConNum ;
			Double importeTrasladoMul;
			String nodocon = "";
			Double sumTotal = 0.00 ;
			Double sumTotalIva = 0.00;
			Double sumTotalIsr= 0.00;
			Double sumTotalIeps = 0.00;
//			System.out.println("findRetencion Inicio TipoComprobante " + tipoComprobante);
//			System.out.println("findRetencion Inicio " + descCon);
//			System.out.println("findRetencion Inicio 2 " + mapCatalogos.get("EquivalenciaConceptoImpuesto").size());
			if(mapCatalogos.size() > 0 && descCon.trim().length() > 0){
				
				for(int i=0; i<mapCatalogos.get("EquivalenciaConceptoImpuesto").size(); i++){
					//System.out.println("findTraslados Dentro For " + mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal5() + " : " + mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal4());
					if(mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal5().equalsIgnoreCase(descCon) && mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal4().equalsIgnoreCase("Retencion")){
						valTasa = mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal3();
						
						if(mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal3() != null){
							//System.out.println("Val Tasa No nulo Reten");
							if(mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal3().length() > 0){
								valTasa = mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal3();
							}else{
								//System.out.println("Val Tasa vacio length Reten");
								valTasa = "vacio";
							}
							
						}else{
							//System.out.println("Val Tasa nulo Reten");
							valTasa = "vacio";
						}
						
						try{
							if(!valTasa.equalsIgnoreCase("vacio")){
								valTasaNum = Double.parseDouble(valTasa);
								//System.out.println("Val Tasa Reten " + valTasaNum);
							}else{
//								valTasaNum = Double.parseDouble(valTasa);
								//System.out.println("Val Tasa Vacio Reten " + valTasa);
							}
							valTasaNum = Double.parseDouble(valTasa);
//							System.out.println("Val Tasa Reten " + valTasaNum);
//							System.out.println("Val Impor Conce Antes Reten " + importeCon);
							importeConNum = Double.parseDouble(importeCon);
//							System.out.println("Val Impor Conce Reten " + valTasaNum);
//							importeTrasladoMul = (valTasaNum*importeConNum) + importeConNum;
							importeTrasladoMul = (valTasaNum*importeConNum);
//							System.out.println("Val Impor Retencion " + importeTrasladoMul);
							if(mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal1().equalsIgnoreCase("001")){
								responseMap.put("ISR", true);
								sumTotalIsr = sumTotalIsr + importeTrasladoMul;
//								System.out.println("Val sumTotalIsr Retencion " + sumTotalIsr);
							}else if(mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal1().equalsIgnoreCase("002")){
								responseMap.put("IVA", true);
								sumTotalIva = sumTotalIva + importeTrasladoMul;
//								System.out.println("Val sumTotalIva Retencion " + sumTotalIva);
							}else if(mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal1().equalsIgnoreCase("003")){
								responseMap.put("IEPS", true);
								sumTotalIeps = sumTotalIeps + importeTrasladoMul;
//								System.out.println("Val sumTotalIeps Retencion " + sumTotalIeps);
							}
							
							String baseCamp = "";
							if(UtilCatalogos.decimalesValidationMsj(importeCon, decimalesMoneda)){
								
								if(tipoComprobante.equalsIgnoreCase("T") || tipoComprobante.equalsIgnoreCase("P")){
									baseCamp = "Base=\"" + importeCon;
								}else{
									if(importeConNum > 0){
										baseCamp = "Base=\"" + importeCon;
									}else{
										baseCamp = "ErrConRetBas001=\"" + importeCon;
									}
								}
								
							}else{
								baseCamp = "ErrConRetBas002=\"" + importeCon;
							}
							
							String impuestoLine = "";
							if(mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal1() != null){
								if(!findValImpuestoByClave(mapCatalogos, mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal1()).equalsIgnoreCase("vacio")){
									impuestoLine = "\" Impuesto=\"" + mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal1() ;
								}else{
									impuestoLine = "\" ErrConRetImp001=\"" + mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal1() ;
								}
							}else{
								impuestoLine = "\" ErrConRetImp001=\"" + mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal1() ;
							}
							
							String tipoFactorLine = "";
							String tipoFactorValue = "";
							String tasaOCutoaLine = "";
							String importeLine = "";
							if(mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal2() != null){
//								System.out.println("Antes BuscarTipoFactor : " + mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal2());
								tipoFactorValue= findValTipoFactorByDescRet(mapCatalogos, mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal2());
//								System.out.println("Despues BuscarTipoFactor : " + tipoFactorValue);
								if(!tipoFactorValue.equalsIgnoreCase("vacio")){
									tipoFactorLine = "\" TipoFactor=\"" + tipoFactorValue;
									String descImp = findDescImpuestoByClave(mapCatalogos, mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal1());
									String findValTasa = "";
									if(!tipoFactorValue.equalsIgnoreCase("Exento")){
//										System.out.println("Antes de Validacion findTasaOCuotaExist RET AMDA : " + descImp);
//										System.out.println("Antes de Validacion findTasaOCuotaExist RET AMDA : " + mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal2());
//										System.out.println("Antes de Validacion findTasaOCuotaExist RET AMDA : " + valTasa);
//										System.out.println("Antes de Validacion findTasaOCuotaExist RET AMDA : " + mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal4());
										findValTasa = findTasaOCuotaExist(mapCatalogos, descImp, mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal2(), valTasa, mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal4());
										if(!findValTasa.equalsIgnoreCase("vacio")){
											tasaOCutoaLine = "\" TasaOCuota=\"" + Util.completeZeroDecimals(valTasa, 6);
										}else{
											tasaOCutoaLine = "\" ErrImpRetConTasaOCuota001=\"" + " ";
										}
										
										importeLine = "\" Importe=\"" + decimales(importeTrasladoMul.toString(), decimalesMoneda);
									}else{
										if(!findValTasa.equalsIgnoreCase("vacio")){
											tasaOCutoaLine = "\" TasaOCuota=\"" + Util.completeZeroDecimals(valTasa, 6);
										}else{
											tasaOCutoaLine = "\" ErrImpRetConTasaOCuota001=\"" + " ";
										}
										importeLine = "\" Importe=\"" + decimales(importeTrasladoMul.toString(), decimalesMoneda);
										
										tipoFactorLine = "\" ErrImpRetConImporte001=\"" + tipoFactorValue;
									}
									
								}else{
									tipoFactorLine = "\" ErrRetConTipFac001=\"" + tipoFactorValue;
								}
							}else{
								tipoFactorLine = "\" ErrRetConTipFac001=\"" + mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal2();
							}
							
							nodocon +=  "\n<cfdi:Retencion " + baseCamp +
									   impuestoLine +
									   tipoFactorLine +
									   tasaOCutoaLine +
									   importeLine + "\" " +
									   " />" ;
							
//							nodocon +=  "\n<cfdi:Retencion Base=\"" + importeCon +
//									   "\" Impuesto=\"" + mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal1() +
//									   "\" TipoFactor=\"" + mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal2() +
//									   "\" TasaOCuota=\"" +  Util.completeZeroDecimals(valTasa, 6) +
//									   "\" Importe=\"" + decimales(importeTrasladoMul.toString(), decimalesMoneda) + "\" " +
//									   " />" ;
							sumTotal += importeTrasladoMul;
//							System.out.println("Val Suma Total Retencion " + sumTotal);
//							System.out.println("Val NodoCon findRetencion " + nodocon);
						}catch(NumberFormatException e){
							System.out.println("No es numero findRetencion: " + valTasa);
						}
//						response = response + nodocon;
//						break;
					}else{
//						if(mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal5().equalsIgnoreCase(descCon) && !mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal4().equalsIgnoreCase("Retencion")){
//							nodocon += "\n<cfdi:Retencion Base=\"" + "0.00" +
//							   "\" Impuesto=\"" + "000" +
//							   "\" TipoFactor=\"" + "Tasa" +
//							   "\" ElValorDelCampoTasaOCuotaQueCorrespondeARetencionNoContieneUnValorDelCatalogoc_TasaOCuota=\"" + "0.000" +
//							   "\" Importe=\"" + "0.00" + "\" " +
//							   " />" ;
//						}else if(!mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal5().equalsIgnoreCase(descCon) && mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal4().equalsIgnoreCase("Retencion")){
//							nodocon += "\n<cfdi:Retencion Base=\"" + "0.00" +
//									   "\" ErrRetImp002=\"" + "000" +
//									   "\" TipoFactor=\"" + "Tasa" +
//									   "\" TasaOCuota=\"" + "0.000" +
//									   "\" Importe=\"" + "0.00" + "\" " +
//									   " />" ;
//						}else{
							response = "";
//						}
						
//						nodocon = "\n<cfdi:Retencion Base=\"" + "0.00" +
//								   "\" ErrRetImp002=\"" + "000" +
//								   "\" TipoFactor=\"" + "Tasa" +
//								   "\" ElValorDelCampoTasaOCuotaQueCorrespondeARetencionNoContieneUnValorDelCatalogoc_TasaOCuota=\"" + "0.000" +
//								   "\" Importe=\"" + "0.00" + "\" " +
//								   " />" ;
//						response = "";
					}
					response = nodocon;
				}
				
//				for(int i=0; i<mapCatalogos.get(descCon).size(); i++){
//					System.out.println("findTraslados Dentro For " + mapCatalogos.get(descCon).get(i).getVal5());
//					if(mapCatalogos.get(descCon).get(i).getVal5().equalsIgnoreCase(descCon) && mapCatalogos.get(descCon).get(i).getVal4().equalsIgnoreCase("Traslado")){
//						valTasa = mapCatalogos.get(descCon).get(i).getVal3();
//						try{
//							valTasaNum = Double.parseDouble(valTasa);
//							System.out.println("Val Tasa " + valTasaNum);
//							importeConNum = Double.parseDouble(importeCon);
//							System.out.println("Val Impor Conce " + valTasaNum);
//							importeTrasladoMul = (valTasaNum*importeConNum) + importeConNum;
//							System.out.println("Val Impor Traslado " + importeTrasladoMul);
//							
//							nodocon = "\n<cfdi:Traslado Base=\"" + importeCon +
//									   "\" Impuesto=\"" + mapCatalogos.get(descCon).get(i).getVal1() +
//									   "\" TipoFactor=\"" + mapCatalogos.get(descCon).get(i).getVal2() +
//									   "\" TasaOcuota=\"" + valTasa +
//									   "\" Importe=\"" + importeTrasladoMul.toString() + "\" " +
//									   " />" ;
//							System.out.println("Val NodoCon Traslado " + nodocon);
//						}catch(NumberFormatException e){
//							System.out.println("No es numero findTraslados: " + valTasa);
//						}
//						response = response + nodocon;
//						break;
//					}else{
//						response = "";
//					}
//				}
			}else{
				response = "";
			}
			responseMap.put("valNodoStr", response);
			responseMap.put("sumaTotal", decimales(sumTotal.toString(), decimalesMoneda));
			responseMap.put("sumTotalIsr", decimales(sumTotalIsr.toString(), decimalesMoneda));
			responseMap.put("sumTotalIva", decimales(sumTotalIva.toString(), decimalesMoneda));
			responseMap.put("sumTotalIeps", decimales(sumTotalIeps.toString(), decimalesMoneda));
//			System.out.println("Response Get Retencion sumaTotal: " + responseMap.get("sumaTotal"));
//			System.out.println("Response Get Retencion sumTotalIsr: " + responseMap.get("sumTotalIsr"));
//			System.out.println("Response Get Retencion sumTotalIva: " + responseMap.get("sumTotalIva"));
//			System.out.println("Response Get Retencion sumTotalIeps: " + responseMap.get("sumTotalIeps"));
			return responseMap;
		}
		
		
		public static String decimales(String importeval, Integer decimalesMoneda){
			String response = "";
			String importeValDer = "";
			String importeValIzq = "";
			//System.out.println("Entrando funcion Decimales: " + importeval + " : " + decimalesMoneda);
			
			//Redondear
			if(importeval!= null && Double.parseDouble(importeval)>0){
				String dFormat = "";
				if(decimalesMoneda>0){
					dFormat=".";
					for(int i = 0; i<decimalesMoneda;i++){
						dFormat+="0";
					}
				}
				try{
					DecimalFormat df = new DecimalFormat("0"+dFormat);
					df.setRoundingMode(RoundingMode.HALF_EVEN);
					Double dImporteValue = new Double(importeval);
				    importeval = df.format(dImporteValue);
				}catch(Exception e){
					System.out.print("Error Decimales message: "+ e.getMessage());
					System.out.print("Error Decimales cause: "+ e.getCause());
					System.out.print("Error Decimales trace: "+ e.getStackTrace().toString());
				}
			}
			
			if(importeval.contains(".")){			    
				String deci[] = importeval.split("\\.");
				importeValIzq = deci[0];
				importeValDer = deci[1];
				if(decimalesMoneda > 0){
					if(importeValDer.length() > decimalesMoneda){
						response = importeValIzq+"."+importeValDer.substring(0,decimalesMoneda);
					}else if(importeValDer.length() < decimalesMoneda){
						Integer result = decimalesMoneda - importeValDer.length();
//						System.out.println("Result Decimales: " + result);
						if(result.equals(4)){
							response = importeValIzq+"."+importeValDer + "0000";
						}else if(result.equals(3)){
							response = importeValIzq+"."+importeValDer + "000";
						}else if(result.equals(2)){
							response = importeValIzq+"."+importeValDer + "00";
						}else if(result.equals(1)){
							response = importeValIzq+"."+importeValDer + "0";
						}
					}else{
						response = importeval;
					}
				}else{
//					response = importeval.substring(0,2);
					response = importeval;
				}
			}else {
				response = importeval;
			}
//			System.out.println("Regreso Importe Substring: " + response);
			return response;
		}
		
		public static boolean decimalesValidationMsj(String importeval, Integer decimalesMoneda){
			boolean response = false;
			String importeValDer = "";
			String importeValIzq = "";
//			System.out.println("Entrando funcion Decimales: " + importeval + " : " + decimalesMoneda);
			if(importeval.contains(".")){
				String deci[] = importeval.split("\\.");
				importeValIzq = deci[0];
				importeValDer = deci[1];
				if(decimalesMoneda > 0){
					if(importeValDer.length() > decimalesMoneda){
//						response = importeValIzq+"."+importeValDer.substring(0,decimalesMoneda);
						response = false;
					}else if(importeValDer.length() < decimalesMoneda){
						response = false;
					}else{
//						response = importeval;
						response = true;
					}
				}else{
//					response = importeval.substring(0,2);
//					response = importeval;
					response = true;
				}
			}else{
				response = true;
			}
				
//			System.out.println("Regreso Importe Substring: " + response);
			return response;
		}
		
		//Validacion Encuentra Valor de la Forma De Pago a travez de la descripcion AMDA
		public static String findFormaPago(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			String response = "";
			
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				for(int i=0; i<mapCatalogos.get("FormaPago").size(); i++){
					if(mapCatalogos.get("FormaPago").get(i).getVal2().equalsIgnoreCase(value)){
						response = mapCatalogos.get("FormaPago").get(i).getVal1();	
						break;
					}else{
						response = "vacio";
					}
				}
			}else{
				response = "vacio";
			}
			
			return response;
		}
		
		//Validacion Encuentra Valor de la Forma De Pago a travez de la descripcion AMDA
		public static String findMetodoPago(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			String response = "";
			
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				for(int i=0; i<mapCatalogos.get("MetodoPago").size(); i++){
					if(mapCatalogos.get("MetodoPago").get(i).getVal1().equalsIgnoreCase(value)
							|| mapCatalogos.get("MetodoPago").get(i).getVal2().equalsIgnoreCase(value)){
						response = mapCatalogos.get("MetodoPago").get(i).getVal1();	
						break;
					}else{
						response = "vacio";
					}
				}
			}else{
				response = "vacio";
			}
			
			return response;
		}
		
		//Validacion Encuentra Valor del UsoCFDI a travez de la descripcion AMDA
		public static String findUsoCfdi(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			String response = "";
			
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				for(int i=0; i<mapCatalogos.get("UsoCFDI").size(); i++){
					if(mapCatalogos.get("UsoCFDI").get(i).getVal2().equalsIgnoreCase(value)){
						response = mapCatalogos.get("UsoCFDI").get(i).getVal1();	
						break;
					}else{
						response = "vacio";
					}
				}
			}else{
				response = "vacio";
			}
			
			return response;
		}
		
		//Validacion Encuentra Valor de la clave del catalogo claveProdServ a travez de la descripcion AMDA
		public static String findClaveProdServbyDesc(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			String response = "";
			
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				for(int i=0; i<mapCatalogos.get("ClaveProdServ").size(); i++){
					if(mapCatalogos.get("ClaveProdServ").get(i).getVal2().equalsIgnoreCase(value)){
						response = mapCatalogos.get("ClaveProdServ").get(i).getVal1();	
						break;
					}else{
						response = "vacio";
					}
				}
			}else{
				response = "vacio";
			}
			
			return response;
		}
		
		//Valida que exista un Tipo Relacion a partir de la clave proporcionada
		public static boolean existClaveInTipoRelacion(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			boolean response = false;
			
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				for(int i=0; i<mapCatalogos.get("TipoRelacion").size(); i++){
					if(mapCatalogos.get("TipoRelacion").get(i).getVal1().equalsIgnoreCase(value)){
						response= true;
						break;
					}else{
						response = false;
					}
				}
			}else{
				response = false;
			}
			
			return response;
		}
		
		//Validacion Encuentra Valor de Traslado del catalogo claveProdServ a travez de la descripcion AMDA
		public static boolean findClaveProdServTrasladoIVAbyDesc(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			boolean response = false;
			String valres = "";
			
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				for(int i=0; i<mapCatalogos.get("ClaveProdServ").size(); i++){
					if(mapCatalogos.get("ClaveProdServ").get(i).getVal2().equalsIgnoreCase(value)){
						valres = mapCatalogos.get("ClaveProdServ").get(i).getVal5();	
						if(valres.equalsIgnoreCase("No")){
							response = false;
						}else{
							response = true;
						}
						break;
					}else{
						response = true;
					}
				}
			}else{
				response = true;
			}
			
			return response;
		}
		
		//Validacion Encuentra Valor de Retencion del catalogo claveProdServ a travez de la descripcion AMDA
		public static boolean findClaveProdServTrasladoIEPSbyDesc(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value){
			boolean response = false;
			String valres = "";
			
			if(mapCatalogos.size() > 0 && value.trim() != ""){
				for(int i=0; i<mapCatalogos.get("ClaveProdServ").size(); i++){
					if(mapCatalogos.get("ClaveProdServ").get(i).getVal2().equalsIgnoreCase(value)){
						valres = mapCatalogos.get("ClaveProdServ").get(i).getVal6();	
						if(valres.equalsIgnoreCase("No")){
							response = false;
						}else{
							response = true;
						}
						break;
					}else{
						response = true;
					}
				}
			}else{
				response = true;
			}
			
			return response;
		}

	    
		public static void setValueOnDocumentElement(Document doc, String expression, String value) throws XPathExpressionException {
			NodeList nl = getNodesByExpression(doc, expression);
			if (nl != null && nl.getLength() > 0) {
				nl.item(0).setNodeValue(value);
			}
		}

		public static ByteArrayOutputStream convertStringToOutpuStream(String data) {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(byteArrayOutputStream);				
			try {
				out.write(data.getBytes("UTF-8"));
				byteArrayOutputStream.flush();
				byteArrayOutputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return byteArrayOutputStream;

		}

	    public static Document convertPathFileToDocument(String pathFileXml) throws SAXException, IOException, ParserConfigurationException {
	    	File file = new File(pathFileXml);
	    	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document dom = db.parse(file);
			return dom;
	    }
	    
	    public static Document convertStringToDocument(String xmlStr) throws ParserConfigurationException, SAXException, IOException {
	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder builder;
	        builder = factory.newDocumentBuilder();
	        Document doc = builder.parse(new InputSource(new StringReader(xmlStr)));
	        return doc;
	    }

	    public static String convertDocumentXmlToString(Document doc) throws TransformerConfigurationException, TransformerException {
	        TransformerFactory tf = TransformerFactory.newInstance();
	        Transformer transformer = tf.newTransformer();
	        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	        StringWriter writer = new StringWriter();
	        transformer.transform(new DOMSource(doc), new StreamResult(writer));
	        return writer.getBuffer().toString();//.replaceAll("\n|\r", "");
	    }

		public static void evaluateAddError(String item) {
			String err = UtilCatalogos.errorMessage.get(item);
			if (err != null && !err.isEmpty()) {
				UtilCatalogos.lstErrors.append(err)
						.append("@@-@@".intern());
			}
		}
		public static void evaluateNodesError(Element docEle) {
			if (docEle != null) {
				NodeList nl = docEle.getChildNodes();
				// System.out.println("Root element :" + docEle.getNodeName());
				evaluateAddError(docEle.getNodeName());
				NamedNodeMap attributes = docEle.getAttributes();
				for (int idxAttr = 0; idxAttr < attributes.getLength(); idxAttr++) {
					Attr attr = (Attr) attributes.item(idxAttr);
					evaluateAddError(attr.getNodeName());
					// String attrName = attr.getNodeName();
					// String attrValue = attr.getNodeValue();
					// System.out.println("\t" + attrName + " : " + attrValue);
				}
				if (nl != null) {
					for (int i = 0; i < nl.getLength(); i++) {
						attributes = docEle.getAttributes();
						if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
							evaluateNodesError((Element) nl.item(i));
						}
					}
				}
			}
		}
		
		public static NodeList getNodesByExpression(Document doc, String expression) throws XPathExpressionException {
	        XPathFactory xPathfactory = XPathFactory.newInstance();
	        XPath xpath = xPathfactory.newXPath();
	        XPathExpression expr = xpath.compile(expression);
	        NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
	        return nl;
	    }

	    public static String getStringValByExpression(Document doc, String expression) throws XPathExpressionException {
	        NodeList nl = getNodesByExpression(doc, expression);
	        if (nl != null && nl.getLength() > 0) {
	            return nl.item(0).getNodeValue();
	        }
	        return "";
	    }

	    private static BigDecimal getBigDecimalByNodeExpression(Document doc, String expression) throws XPathExpressionException {
	        NodeList nl = getNodesByExpression(doc, expression);
	        BigDecimal value = new BigDecimal(0);
	        if (nl != null) {
	            for (int i = 0; i < nl.getLength(); i++) {
	                value = value.add(new BigDecimal(nl.item(i).getNodeValue()));
	            }
	        }
	        return value;
	    }
	    
		public static Double getDoubleByExpression(Document doc, String expression) throws XPathExpressionException {
	        XPathFactory xPathfactory = XPathFactory.newInstance();
	        XPath xpath = xPathfactory.newXPath();
	        XPathExpression expr = xpath.compile(expression);
	        Double nl = (Double)expr.evaluate(doc, XPathConstants.NUMBER);
	        return nl;
	    }
	    
	    public static void evaluateCalulation(Document doc, int maxDecimals) throws XPathExpressionException, Exception {
	    	logger.info("Iniciando Validaciones de Calculos");
	    	BigDecimal compTotal = BigDecimal.valueOf(getDoubleByExpression(doc, "//Comprobante/@Total"));
	        StringBuilder sb = new StringBuilder("(//Comprobante/Conceptos/Concepto/@Importe)");
	        BigDecimal conceptTotal = getBigDecimalByNodeExpression(doc, sb.toString());
	        sb = new StringBuilder("(//Comprobante/Conceptos/Concepto/Impuestos/Traslados/Traslado/@Importe)");
	        BigDecimal traslTotal = getBigDecimalByNodeExpression(doc, sb.toString());
	        BigDecimal traslados = conceptTotal.add(traslTotal);
	        sb = new StringBuilder("(//Comprobante/Conceptos/Concepto/Impuestos/Retenciones/Retencion/@Importe)");
	        BigDecimal retenciones = getBigDecimalByNodeExpression(doc, sb.toString());
	        BigDecimal discount = getBigDecimalByNodeExpression(doc, "//Comprobante/@Descuento");
	        retenciones = retenciones.add(discount);	        
	        
	        BigDecimal totalOper = retenciones.equals(BigDecimal.valueOf(0)) ? traslados : traslados.add(retenciones.multiply(BigDecimal.valueOf(-1)));
	        if (compTotal.doubleValue() == totalOper.doubleValue()) {

			} else {
				System.out.println("totalOper=" + totalOper);
				System.out.println("compTotal=" + compTotal);
				System.out.println("retenciones=" + retenciones);
				System.out.println("traslados=" + traslados);
				System.out.println("Comptaracion=" + (compTotal.doubleValue() == totalOper.doubleValue()));
				throw new Exception(
						"El campo Total no corresponde con la suma del subtotal, menos los descuentos aplicables, más las contribuciones recibidas (impuestos trasladados - federales o locales, derechos, productos, aprovechamientos, aportaciones de seguridad social, contribuciones de mejoras) menos los impuestos retenidos.");
			}

	        logger.info("Validando descuentos");
	        /*Asignacion y validacion de Descuento*/
	        String voucherType = getStringValByExpression(doc, "//Comprobante/@TipoDeComprobante");
	        System.out.println("discount:" + discount);
	        boolean validateDiscount = false;
	        if (discount.doubleValue() > 0) {//Si no viene con valor omitimos este paso
	            validateDiscount = true;
	        }
	        if (voucherType.equalsIgnoreCase("I")
	                || voucherType.equalsIgnoreCase("E")
	                || voucherType.equalsIgnoreCase("N")) {
	            if (validateDiscount) {
	                //El subtotal debe ser mayor o igual al descuento
	                BigDecimal subTotal = BigDecimal.valueOf(getDoubleByExpression(doc, "//Comprobante/@SubTotal"));
	                if (discount.doubleValue() > subTotal.doubleValue()) {
	                    throw new Exception(errorMessage.get("ErrCompSubTotal005"));
	                }
	            }
	            String concepts = "//Comprobante/Conceptos/Concepto";
	            BigDecimal totalConcept = BigDecimal.valueOf(getDoubleByExpression(doc, "count(".concat(concepts.intern()).concat(")")));
	            System.out.println("Conceptos:" + totalConcept);
	            if (totalConcept.doubleValue() > 0) {//Verificamos que vengan conceptos
	                //Obtenemos el decuento por concepto
	                BigDecimal discPerConcept = discount.divide(totalConcept, maxDecimals, RoundingMode.HALF_UP);
	                BigDecimal last = discount.subtract(discPerConcept.multiply(totalConcept));
	                NodeList nl = getNodesByExpression(doc, concepts.intern());
	                if (nl != null) {
	                    String disc = discPerConcept.toString();
	                    for (int i = 0; i < nl.getLength(); i++) {
	                        Element el = ((Element) nl.item(i));
	                        BigDecimal importe = new BigDecimal(el.getAttribute("Importe"));
	                        BigDecimal valorUnitario = new BigDecimal(el.getAttribute("ValorUnitario"));
	                        if (valorUnitario.doubleValue() <= 0) {
	                            throw new Exception(errorMessage.get("ErrCompValUni003"));
	                        }
	                        if (validateDiscount) {
	                            if ((i + 1) == nl.getLength() && last.doubleValue() > 0) {
	                                //Se suma el descuento por concepto mas
	                                discPerConcept = discPerConcept.add(last);
	                                disc = discPerConcept.toString();
	                            }
	                            if (discPerConcept.doubleValue() > importe.doubleValue()) {
	                                throw new Exception(MessageFormat.format(errorMessage.get("ErrConcImport002"), (i + 1), discPerConcept, importe));
	                            }
	                            el.setAttribute("Descuento", disc.intern());
	                        }
	                    }
	                }
	            }
	        } else {
	            if (validateDiscount) {
	                throw new Exception(errorMessage.get("ErrCompTipoComprobante002"));
	            }
	        }
	    }
	    
		//Validacion Encuentra Valor de Retencion del catalogo claveProdServ a travez de la descripcion AMDA
		public static String findStringAcento(String value){
			String response = "";
			String valres = "";
			//System.out.println("findStringAcento AMDA: " + value);
			if(value.trim().length() > 0){
				
				String stringa = Normalizer.normalize(value, Normalizer.Form.NFD);
				String stringU = stringa.replaceAll("[^\\p{ASCII}]", "");
				//System.out.println("findStringAcento AMDAII: " + stringU);
				if(stringU.trim().equalsIgnoreCase("Canad")){
					response = "Canada";
				}else if(stringU.trim().equalsIgnoreCase("Florn antillano neerlands")){
					response = "Florin antillano neerlandes";
				}else if(stringU.trim().equalsIgnoreCase("Dlar de Barbados")){
					response = "Dolar de Barbados";
				}else if(stringU.trim().equalsIgnoreCase("Lev bulgaro")){
					response = "Lev bulgaro";
				}else if(stringU.trim().equalsIgnoreCase("Dlar de Bermudas")){
					response = "Dolar de Bermudas";
				}else if(stringU.trim().equalsIgnoreCase("Dlar de Brunei")){
					response = "Dolar de Brunei";
				}else if(stringU.trim().equalsIgnoreCase("Dlar de las Bahamas")){
					response = "Dolar de las Bahamas";
				}else if(stringU.trim().equalsIgnoreCase("Dlar de Belice")){
					response = "Dolar de Belice";
				}else if(stringU.trim().equalsIgnoreCase("Coln costarricense")){
					response = "Colon costarricense";
				}else if(stringU.trim().equalsIgnoreCase("Birr etope")){
					response = "Birr etiope";
				}else if(stringU.trim().equalsIgnoreCase("Dlar de Fiji")){
					response = "Dolar de Fiji";
				}else if(stringU.trim().equalsIgnoreCase("Dlar guyans")){
					response = "Dolar guyanes";
				}else if(stringU.trim().equalsIgnoreCase("Florn")){
					response = "Florin";
				}else if(stringU.trim().equalsIgnoreCase("Nuevo Shekel Israel")){
					response = "Nuevo Shekel Israeli";
				}else if(stringU.trim().equalsIgnoreCase("Dinar iraqu")){
					response = "Dinar iraquí";
				}else if(stringU.trim().equalsIgnoreCase("Rial irani")){
					response = "Rial irani";
				}else if(stringU.trim().equalsIgnoreCase("Dlar Jamaiquino")){
					response = "Dolar Jamaiquino";
				}else if(stringU.trim().equalsIgnoreCase("Cheln keniano")){
					response = "Chelin keniano";
				}else if(stringU.trim().equalsIgnoreCase("Corea del Norte gan")){
					response = "Corea del Norte gano";
				}else if(stringU.trim().equalsIgnoreCase("Dinar kuwaiti")){
					response = "Dinar kuwaiti";
				}else if(stringU.trim().equalsIgnoreCase("Dlar de las Islas Caimn")){
					response = "Dolar de las Islas Caiman";
				}else if(stringU.trim().equalsIgnoreCase("Dlar liberiano")){
					response = "Dolar liberiano";
				}else if(stringU.trim().equalsIgnoreCase("Dirham marroqu")){
					response = "Dirham marroqui";
				}else if(stringU.trim().equalsIgnoreCase("Mxico Unidad de Inversin (UDI)")){
					response = "Mexico Unidad de Inversion (UDI)";
				}else if(stringU.trim().equalsIgnoreCase("Dlar de Namibia")){
					response = "Dlar de Namibia";
				}else if(stringU.trim().equalsIgnoreCase("Crdoba Oro")){
					response = "Cordoba Oro";
				}else if(stringU.trim().equalsIgnoreCase("Rupia nepal")){
					response = "Rupia nepali";
				}else if(stringU.trim().equalsIgnoreCase("Dlar de Nueva Zelanda")){
					response = "Dolar de Nueva Zelanda";
				}else if(stringU.trim().equalsIgnoreCase("Rial oman")){
					response = "Rial omani";
				}else if(stringU.trim().equalsIgnoreCase("Rupia de Pakistn")){
					response = "Rupia de Pakistan";
				}else if(stringU.trim().equalsIgnoreCase("Guaran")){
					response = "Guarani";
				}else if(stringU.trim().equalsIgnoreCase("Franco ruands")){
					response = "Franco ruandes";
				}else if(stringU.trim().equalsIgnoreCase("Riyal saud")){
					response = "Riyal saudi";
				}else if(stringU.trim().equalsIgnoreCase("Dlar de las Islas Salomn")){
					response = "Dolar de las Islas Salomon";
				}else if(stringU.trim().equalsIgnoreCase("Cheln somal")){
					response = "Chelin somali";
				}else if(stringU.trim().equalsIgnoreCase("Dlar de Suriname")){
					response = "Dolar de Suriname";
				}else if(stringU.trim().equalsIgnoreCase("Turkmenistn nuevo manat")){
					response = "Turkmenistan nuevo manat";
				}else if(stringU.trim().equalsIgnoreCase("Paanga")){
					response = "Pa'anga";
				}else if(stringU.trim().equalsIgnoreCase("Dlar de Trinidad y Tobago")){
					response = "Dolar de Trinidad y Tobago";
				}else if(stringU.trim().equalsIgnoreCase("Nuevo dlar de Taiwn")){
					response = "Nuevo dolar de Taiwan";
				}else if(stringU.trim().equalsIgnoreCase("Dlar estadounidense (da siguiente)")){
					response = "Dolar estadounidense (dia siguiente)";
				}else if(stringU.trim().equalsIgnoreCase("Uzbekistn Sum")){
					response = "Uzbekistan Sum";
				}else if(stringU.trim().equalsIgnoreCase("Bolvar")){
					response = "Bolivar";
				}else if(stringU.trim().equalsIgnoreCase("Cdigos reservados especficamente para propsitos de prueba")){
					response = "Códigos reservados específicamente para propósitos de prueba";
				}else if(stringU.trim().equalsIgnoreCase("Los cdigos asignados para las transacciones en que intervenga ninguna moneda")){
					response = "Los códigos asignados para las transacciones en que intervenga ninguna moneda";
				}else if(stringU.trim().equalsIgnoreCase("Rial yemen")){
					response = "Rial yemeni";
				}else if(stringU.trim().equalsIgnoreCase("Zimbabwe Dlar")){
					response = "Zimbabwe Dólar";
				}else if(stringU.trim().equalsIgnoreCase("MXN")){
					response = "Peso";
				}else if(stringU.trim().equalsIgnoreCase("Peso")){
					response = "Peso";
				}else if(stringU.trim().equalsIgnoreCase("M.N.")){
					response = "M.N.";
				}else if(stringU.trim().equalsIgnoreCase("M.N")){
					response = "M.N";
				}else if(stringU.trim().equalsIgnoreCase("m.n.")){
					response = "m.n.";
				}else if(stringU.trim().equalsIgnoreCase("Afganistn")){
					response = "Afganistán";
				}else if(stringU.trim().equalsIgnoreCase("Islas land")){
					response = "Islas Åland";
				}else if(stringU.trim().equalsIgnoreCase("Azerbaiyn")){
					response = "Azerbaiyán";
				}else if(stringU.trim().equalsIgnoreCase("Antrtida")){
					response = "Antártida";
				}else if(stringU.trim().equalsIgnoreCase("Banglads")){
					response = "Bangladés";
				}else if(stringU.trim().equalsIgnoreCase("Banglads")){
					response = "Bangladés";
				}else if(stringU.trim().equalsIgnoreCase("Barin")){
					response = "Baréin";
				}else if(stringU.trim().equalsIgnoreCase("Blgica")){
					response = "Bélgica";
				}else if(stringU.trim().equalsIgnoreCase("Benn")){
					response = "Benín";
				}else if(stringU.trim().equalsIgnoreCase("Bruni Darussalam")){
					response = "Brunéi Darussalam";
				}else if(stringU.trim().equalsIgnoreCase("Camern")){
					response = "Camerún";
				}else if(stringU.trim().equalsIgnoreCase("Corea (la Repblica de)")){
					response = "Corea (la República de)";
				}else if(stringU.trim().equalsIgnoreCase("Cte dIvoire")){
					response = "Côte d'Ivoire";
				}else if(stringU.trim().equalsIgnoreCase("Curaao")){
					response = "Curaçao";
				}else if(stringU.trim().equalsIgnoreCase("Emiratos rabes Unidos (Los)")){
					response = "Emiratos Árabes Unidos (Los)";
				}

			}else{
				response = "vacio";
			}
			//System.out.println("findStringAcento response AMDAII: " + response);
			return response;
		}
	    
		public static Invoice_Masivo fillInvoice(CfdiComprobanteFiscal comprobante) {
			Invoice_Masivo invoice = new Invoice_Masivo();
	        invoice.setPorcentaje(0.00);
	        invoice.setVersion(comprobante.getVersion());
	        invoice.setMoneda("");
	        invoice.setIvaDescription("");
	        
	        if(comprobante.getAddenda() != null && comprobante.getAddenda().getCampoAdicional() != null){
		        for (String key : comprobante.getAddenda().getCampoAdicional().keySet()) {
		            if (key.equalsIgnoreCase("MONEDA")) {
		                invoice.setMoneda(comprobante.getAddenda().getCampoAdicional().get(key));
		            } else if (key.equalsIgnoreCase("DESCRIPCI�N CONCEPTO") || key.equalsIgnoreCase("DESCRIPCION CONCEPTO") ) {
		                invoice.setDescriptionConcept(comprobante.getAddenda().getCampoAdicional().get(key));
		            } else if (key.equalsIgnoreCase("DESCRIPCI�N IVA")) {
		                invoice.setIvaDescription(comprobante.getAddenda().getCampoAdicional().get(key));
		            } else if (key.equalsIgnoreCase("TIPO CAMBIO")) {
		                invoice.setTipoCambio(comprobante.getAddenda().getCampoAdicional().get(key));
		            }else if (key.equalsIgnoreCase("NumeroEmpleado")){
		            	invoice.setNumeroEmpleado(comprobante.getAddenda().getCampoAdicional().get(key));
		            }
		        }
	        }

	        StringBuffer direccion = new StringBuffer(comprobante.getEmisor().getNombre());
	        direccion.append("    ");
	        CfdiDomicilio emDomicilio = comprobante.getEmisor().getDomicilio();
	        if (emDomicilio != null) {
	            direccion.append(emDomicilio.getCalle());
	            if (!Util.isNullEmpty(emDomicilio.getNoExterior())) {
	                direccion.append(" ").append(emDomicilio.getNoExterior());
	            }
	            if (!Util.isNullEmpty(emDomicilio.getNoInterior())) {
	                direccion.append(" ").append(emDomicilio.getNoInterior());
	            }
	            if (!Util.isNullEmpty(emDomicilio.getReferencia())) {
	                direccion.append(", ").append(emDomicilio.getReferencia());
	            }
	            if (!Util.isNullEmpty(emDomicilio.getColonia())) {
	                direccion.append(" COL. ").append(emDomicilio.getColonia());
	                if (!Util.isNullEmpty(emDomicilio.getMunicipio())) {
	                    direccion.append(", ").append(emDomicilio.getMunicipio());
	                }
	            }
	            if (!Util.isNullEmpty(emDomicilio.getCodigoPostal())) {
	                direccion.append(", C.P. ").append(emDomicilio.getCodigoPostal());
	            }
	            if (!Util.isNullEmpty(emDomicilio.getEstado())) {
	                direccion.append(", ").append(emDomicilio.getEstado());
	            }
	            if (!Util.isNullEmpty(emDomicilio.getPais())) {
	                direccion.append(", ").append(emDomicilio.getPais());
	            }
	        }
	        direccion.append(" R.F.C.").append(comprobante.getEmisor().getRfc());
	        invoice.setDireccion(direccion.toString());
	        invoice.setEstadoEmisor(emDomicilio.getEstado());
	        invoice.setNoCertificado(comprobante.getNoCertificado());
	        invoice.setFechaHora(comprobante.getFecha());

	        invoice.setFolio(comprobante.getFolio());
	        invoice.setDate(comprobante.getFecha());
	        invoice.setNoAprobacion(comprobante.getConfirmacion());

	        CfdiReceptor receptor = comprobante.getReceptor();

	        invoice.setRfc(receptor.getRfc());
	        invoice.setFormaPago(comprobante.getFormaPago());
	        invoice.setName(receptor.getNombre());
	        invoice.setUsoCFDI(receptor.getUsoCFDI());
	        invoice.setResidenciaFiscal(receptor.getResidenciaFiscal());
	        invoice.setNumRegIdTrib(receptor.getNumRegIdTrib());
	        if (receptor.getDomicilio() != null) {
	            CfdiDomicilio tUbicacion = receptor.getDomicilio();
	            invoice.setCalle(Util.isNull(tUbicacion.getCalle()));
	            invoice.setCodigoPostal(Util.isNull(tUbicacion.getCodigoPostal()));
	            invoice.setColonia(Util.isNull(tUbicacion.getColonia()));
	            invoice.setEstado(Util.isNull(tUbicacion.getEstado()));
	            invoice.setExterior(Util.isNull(tUbicacion.getNoExterior()));
	            invoice.setInterior(Util.isNull(tUbicacion.getNoInterior()));
	            invoice.setMunicipio(Util.isNull(tUbicacion.getMunicipio()));
	            invoice.setReferencia(Util.isNull(tUbicacion.getReferencia()));
	            invoice.setLocalidad(Util.isNull(tUbicacion.getLocalidad()));
	            invoice.setPais(Util.isNull(tUbicacion.getPais()));
	        }

	        invoice.setSubTotal(comprobante.getSubTotal().doubleValue());

	        if (comprobante.getDescuento() != null) {
	            invoice.setDescuento(comprobante.getDescuento().doubleValue());
	        }

	        invoice.setTotal(comprobante.getTotal().doubleValue());

	        if(comprobante.getImpuestos() != null){
		        if (comprobante.getImpuestos().getTotalImpuestosTrasladados() != null) {
		            invoice.setIvaDescription("TOTAL IMP TRASLADO");
		            invoice.setIva(comprobante.getImpuestos().getTotalImpuestosTrasladados().doubleValue());
		            invoice.setTotalImpuestoRetenido(comprobante.getImpuestos().getTotalImpuestosRetenidos().doubleValue());
		        } else {
		            invoice.setIva(0.0);
		        }
	        }else{
	        	invoice.setIva(0.0);
	        }

	        invoice.setQuantityWriting(NumberToLetterConverter.convertNumberToLetter(invoice.getTotal()));
	        invoice.setMetodoPago(comprobante.getMetodoPago());
	        /**
	         * **********CFDI Relacionado********************
	         */
	        if(comprobante.getCfdiRelacionados() != null 
	        		&& comprobante.getCfdiRelacionados().getRelacionado() != null
	        		&& !comprobante.getCfdiRelacionados().getRelacionado().isEmpty()){
	        	/*Solo obtenemos el primero ya que se preparo solo para uno.*/
	        	invoice.setTipoRelacion(comprobante.getCfdiRelacionados().getTipoRelacion());
	        	invoice.setCfdiRelacional(comprobante.getCfdiRelacionados().getRelacionado().get(0));
	        }
	        /**
	         * **********Conceptos********************
	         */
	        List<ElementsInvoice> elements = new LinkedList<ElementsInvoice>();
	        String breakLine = new String("\n");
	        for (CfdiConcepto objConcepto : comprobante.getConceptos()) {
	            ElementsInvoice element = new ElementsInvoice();
	            List<CustomsInformation> informacionAduanera = new LinkedList<CustomsInformation>();
	            List<FarmAccount> cuentaPredial = new LinkedList<FarmAccount>();
	            List<Part> partes = new LinkedList<Part>();
	            element.setAmount(objConcepto.getImporte().doubleValue());
	            StringBuffer desc = new StringBuffer(objConcepto.getDescripcion());
	            if (objConcepto.getImpuestos() != null) {
	                if (objConcepto.getImpuestos().getTraslados() != null
	                        && objConcepto.getImpuestos().getTraslados().size() > 0) {
	                    desc.append(breakLine.intern()).append("Traslados");
	                    for (CfdiConceptoImpuestoTipo item : objConcepto.getImpuestos().getTraslados()) {
	                        desc.append(breakLine.intern()).append("Base: ").append(item.getBase());
	                        if (!Util.isNullEmpty(item.getImpuesto())) {
	                            desc.append(", Impuesto: ").append(item.getImpuesto());
	                        }
	                        if (!Util.isNullEmpty(item.getTasaOCuota())) {
	                            desc.append(", TasaOCuota: ").append(item.getTasaOCuota());
	                        }
	                        if (!Util.isNullEmpty(item.getTipoFactor())) {
	                            desc.append(", TipoFactor: ").append(item.getTipoFactor());
	                        }
	                        if (!Util.isNullEmpty(item.getImporte())) {
	                            desc.append(", Importe: ").append(item.getImporte());
	                        }
	                    }
	                }
	                if (objConcepto.getImpuestos().getRetenciones() != null
	                        && objConcepto.getImpuestos().getRetenciones().size() > 0) {
	                    desc.append(breakLine.intern()).append("Retenciones");
	                    for (CfdiConceptoImpuestoTipo item : objConcepto.getImpuestos().getRetenciones()) {
	                        desc.append(breakLine.intern()).append("Base: ").append(item.getBase());
	                        if (!Util.isNullEmpty(item.getImpuesto())) {
	                            desc.append(", Impuesto: ").append(item.getImpuesto());
	                        }
	                        if (!Util.isNullEmpty(item.getTasaOCuota())) {
	                            desc.append(", TasaOCuota: ").append(item.getTasaOCuota());
	                        }
	                        if (!Util.isNullEmpty(item.getTipoFactor())) {
	                            desc.append(", TipoFactor: ").append(item.getTipoFactor());
	                        }
	                        if (!Util.isNullEmpty(item.getImporte())) {
	                            desc.append(", Importe: ").append(item.getImporte());
	                        }
	                    }
	                }
	            }
	            element.setDescription(desc.toString());
	            element.setQuantity(objConcepto.getCantidad().doubleValue());
	            element.setUnitMeasure(objConcepto.getUnidad());
	            element.setUnitPrice(objConcepto.getValorUnitario().doubleValue());
	            /*TODO:Falta asignar esta seccion que no se tiene*/
	            elements.add(element);
	        }
	        boolean descuento = false;
	        if(comprobante.getDescuento() != null){
		        if (comprobante.getDescuento().doubleValue() > 0) {
		            //invoice.setMotivoDescuento(docEle.getAttribute("motivoDescuento"));
		            invoice.setDescuento(comprobante.getDescuento().doubleValue());
		            descuento = true;
		        }
	        }

	        if (descuento) {
	            ElementsInvoice elementDescuento = new ElementsInvoice();
//	            elementDescuento.setDescription(invoice.getMotivoDescuento());
	            elementDescuento.setDescription("Descuento aplicado");
	            elementDescuento.setUnitPrice(invoice.getDescuento());
	            elementDescuento.setAmount(invoice.getDescuento());
	            elementDescuento.setQuantity(1);
	            elements.add(elementDescuento);
	        }
	        invoice.setElements(elements);

	        /*Elementos faltantes*/
	        invoice.setMetodoPago(comprobante.getMetodoPago());
	        invoice.setLugarExpedicion(comprobante.getLugarExpedicion()); 
	        invoice.setNumCtaPago("");//No existe en 3.3 docEle.getAttribute("NumCtaPago"));
	        invoice.setRegimenFiscal(comprobante.getEmisor().getRegimenFiscal());
	        invoice.setRfcEmisor(comprobante.getEmisor().getRfc());
	        if(comprobante.getComplemento() != null){
		        TimbreFiscal timbre = new TimbreFiscal();
		        timbre.setVersion(comprobante.getComplemento().getTimbreFiscalDigital().getVersion());
		        timbre.setUuid(comprobante.getComplemento().getTimbreFiscalDigital().getUuid());
		        invoice.setSello(comprobante.getComplemento().getTimbreFiscalDigital().getSelloCFD());
		        timbre.setSelloSat(comprobante.getComplemento().getTimbreFiscalDigital().getSelloSAT());
		        timbre.setNoCertificadoSat(comprobante.getComplemento().getTimbreFiscalDigital().getNoCertificadoSAT());
		        timbre.setFechaTimbrado(comprobante.getComplemento().getTimbreFiscalDigital().getFechaTimbrado());
		        invoice.setTimbreFiscal(timbre);
		        invoice.setDonataria(comprobante.getComplemento().getDonataria());
		        
		      //añadir pagos en complemento
		        invoice.setTipoOperacion(comprobante.getComplemento().getDivisaTipoOperacion());
		        if(comprobante.getComplementPagos() != null && comprobante.getComplementPagos().size() > 0){
		        	invoice.setPagos(new ArrayList<ComplementoPago>());
		        	for(ComplementoPago pago : comprobante.getComplementPagos()){
		        		invoice.getPagos().add(pago);
		        	}
		        }
	        }
	        invoice.setCostCenter(comprobante.getAddenda().getInformacionEmision().getCentroCostos());
	        invoice.setCustomerCode(comprobante.getAddenda().getInformacionEmision().getCodigoCliente());
	        invoice.setContractNumber(comprobante.getAddenda().getInformacionEmision().getContrato());
	        invoice.setPeriod(comprobante.getAddenda().getInformacionEmision().getPeriodo());
	        //Datos Addendas Filiales
	        invoice.setTipoAddenda("0");
	        if(comprobante.getAddenda().getInformacionPago() != null){
		        //Va para la addenda logistica
		        invoice.setCodigoISO(comprobante.getAddenda().getInformacionPago().getCodigoISOMoneda());
				invoice.setCodigoisomonedaFin(comprobante.getAddenda().getInformacionPago().getCodigoISOMoneda());
				invoice.setCodigoisomonedaLog(comprobante.getAddenda().getInformacionPago().getCodigoISOMoneda());
				invoice.setCodigoisomonedaArr(comprobante.getAddenda().getInformacionPago().getCodigoISOMoneda());
		        //Va para las tres addendas filiales
				if(comprobante.getAddenda().getInformacionPago().getPosCompra() != null){
			        if (!comprobante.getAddenda().getInformacionPago().getPosCompra().equals("")) {
			            invoice.setTipoAddenda("1");
			            invoice.setPosicioncompraLog(comprobante.getAddenda().getInformacionPago().getPosCompra());
			        }
				}
		        //Va para la addenda financiera
		        if(comprobante.getAddenda().getInformacionPago().getCuentaContable() != null){
			        if (!comprobante.getAddenda().getInformacionPago().getCuentaContable().equals("")) {
			            invoice.setTipoAddenda("2");
			            invoice.setCuentacontableFin(comprobante.getAddenda().getInformacionPago().getCuentaContable());
			        }
		        }
	        }
	        //Va para la addenda arrendamiento
	        if(comprobante.getAddenda().getInmuebles() != null 
	        		&& comprobante.getAddenda().getInmuebles().getNumContrato() != null){
		        if (!comprobante.getAddenda().getInmuebles().getNumContrato().equals("")
		                && !comprobante.getAddenda().getInmuebles().getFechaVencimiento().equals("")) {
		            invoice.setTipoAddenda("3");
		            invoice.setNumerocontratoArr(comprobante.getAddenda().getInmuebles().getNumContrato());
		            invoice.setFechavencimientoArr(comprobante.getAddenda().getInmuebles().getFechaVencimiento());
		        }
	        }

	        invoice.setTipoDeComprobante(comprobante.getTipoDeComprobante());
	        invoice.setSerie(comprobante.getSerie());

	        if(comprobante.getAddenda().getInformacionPago() != null){
		        invoice.setEmail(comprobante.getAddenda().getInformacionPago().getEmail());
		        invoice.setPurchaseOrder(comprobante.getAddenda().getInformacionPago().getOrdenCompra());
		        invoice.setBeneficiaryName(comprobante.getAddenda().getInformacionPago().getNombreBeneficiario());
		        invoice.setReceivingInstitution(comprobante.getAddenda().getInformacionPago().getInstitucionReceptora());
		//        invoice.setNumCtaPago(comprobante.getAddenda().getInformacionPago().getNumeroCuenta());
		        //System.out.println("*** Id Fiscal Util Catalogos: " + comprobante.getAddenda().getInformacionPago().getCuentaContable());
		        if(comprobante.getAddenda().getInformacionPago().getCuentaContable() != null){
		        	invoice.setNumCtaPago(comprobante.getAddenda().getInformacionPago().getCuentaContable());
		        }
		        invoice.setProviderNumber(comprobante.getAddenda().getInformacionPago().getNumProveedor());
	        }
	        
	        if(comprobante.getMoneda() != null){
	        	invoice.setTipoMoneda(comprobante.getMoneda());
	        }
	        if(comprobante.getTipoDeComprobante() != null){
	        	invoice.setTipoFormato(comprobante.getTipoDeComprobante());
	        }
	        
	        if (comprobante.getFecha() != null) {
				invoice.setFechaRecepcionString(comprobante.getFecha());
				try {
					DateFormat format = new SimpleDateFormat("dd/mm/yyyy");
					Date date = format.parse(comprobante.getFecha());
					invoice.setFechaRecepcion(date);
				} catch (ParseException pe) {
	        		System.out.println("error al convertir fechaRecepcion: ");
	        		pe.printStackTrace();
	        	}
	        }
	        
	        if(comprobante.getTipoCambio() != null && !comprobante.getTipoCambio().isEmpty()){
	        	invoice.setExchange(0);
	        	try{
	        		BigDecimal tipocambio = new BigDecimal(comprobante.getTipoCambio());
		        	invoice.setExchange(tipocambio.doubleValue());
	        	}catch(NumberFormatException  nfe){
	        		System.out.println("error al convertir tipo de cambio: ");
	        		nfe.printStackTrace();
	        	}
	        }
	        return invoice;
	    }
		
		public static String validateCfdiDocument(Document doc, int maxDecimals) throws XPathExpressionException, TransformerConfigurationException, TransformerException{
	    	//logger.info("validateCfdiDocument:"+convertDocumentXmlToString(doc));
	    	//System.out.println("******************************************************");
	    	StringBuilder sbError = new StringBuilder();
	    	//System.out.println("******************************************************");
	    	sbError.append(validateDecimals(doc, maxDecimals));
	    	//logger.info("validateCfdiDocument:"+convertDocumentXmlToString(doc));
	    	//System.out.println("******************************************************");
	    	/*En esta seccion se agregaran todas las validciones que se les necesite hacer al comprobante*/
        	//System.out.println("******************************************************");
	    	sbError.append(evaluateCalulationMasiva(doc, maxDecimals));
	    	//logger.info("validateCfdiDocument:"+convertDocumentXmlToString(doc));
	    	//System.out.println("******************************************************");
	    	
	    	if (sbError.length() == 0) {
	            //ogger.info("Complementando los impuestos:");
	            complementTaxes(doc);
	            //logger.info(":" + convertDocumentXmlToString(doc));
	            //System.out.println("******************************************************");
	            //logger.info("Limpiando nodo impuestos de Conceptos vacios");
	            clearTaxes(doc);
	        }else{
	        	System.out.println("******************************************************");
	        	logger.info("validateCfdiDocument:" + convertDocumentXmlToString(doc));
	        	System.out.println("******************************************************");
	        }
	    	return sbError.toString();
	    }
		
	    public static void clearTaxes(Document doc) throws XPathExpressionException {
	        String taxes = "//Comprobante/Conceptos/Concepto/Impuestos";
	        NodeList nlTraslados = getNodesByExpression(doc, taxes);
	        if (nlTraslados != null && nlTraslados.getLength() > 0) {
	            for (int idx = 0; idx < nlTraslados.getLength(); idx++) {
	                if (nlTraslados.item(idx).getNodeType() == Node.ELEMENT_NODE) {
	                    boolean hasChilds = false;
	                    NodeList nlChild = nlTraslados.item(idx).getChildNodes();
	                    if (nlChild != null && nlChild.getLength() > 0) {
	                        for (int idxC = 0; (idxC < nlChild.getLength() && !hasChilds); idxC++) {
	                            if (nlChild.item(idxC).getNodeType() == Node.ELEMENT_NODE) {
	                                hasChilds = true;
	                            }
	                        }
	                    }
	                    if (!hasChilds) {
	                        nlTraslados.item(idx).getParentNode().removeChild(nlTraslados.item(idx));
	                    }
	                }
	            }
	        }
	    }
	    
	    public static void complementTaxes(Document doc) throws XPathExpressionException {
	        String traslados = "//Comprobante/Conceptos/Concepto/Impuestos/Traslados/Traslado";
	        String retencion = "//Comprobante/Conceptos/Concepto/Impuestos/Retenciones/Retencion";
	        boolean hasTraslados = false;
	        boolean hasRetenciones = false;
	        Element eTraslados = doc.createElement("cfdi:Traslados");
	        Element eRetenciones = doc.createElement("cfdi:Retenciones");
	        NodeList nlTraslados = getNodesByExpression(doc, traslados);
	        Map<String, Element> taxesMap = new HashMap<String, Element>();
	        if (nlTraslados != null && nlTraslados.getLength() > 0) {
	            for (int idx = 0; idx < nlTraslados.getLength(); idx++) {
	                Element el = (Element) nlTraslados.item(idx).cloneNode(true);
	                /*Si el tipoFactor es excento no se toma en cuenta*/
	                if (el.hasAttribute("TipoFactor")
	                        && !el.getAttribute("TipoFactor").equalsIgnoreCase("Exento")) {
	                    String key = el.getAttribute("Impuesto") + "_"
	                            + el.getAttribute("TasaOCuota") + "_"
	                            + el.getAttribute("TipoFactor");
	                    Element elAux = taxesMap.get(key);
	                    if (elAux == null) {
	                        el.removeAttribute("Base");
	                        taxesMap.put(key, el);
	                    } else {
	                        Node value = elAux.getAttributes().getNamedItem("Importe");
	                        BigDecimal importeAux = new BigDecimal(value.getNodeValue());
	                        BigDecimal importe = new BigDecimal(el.getAttribute("Importe"));
	                        value.setNodeValue(importeAux.add(importe).toString());
	                        taxesMap.put(key, elAux);
	                    }
	                }
	            }
	            if (!taxesMap.isEmpty()) {
	                hasTraslados = true;
	                for (String key : taxesMap.keySet()) {
	                    eTraslados.appendChild(taxesMap.get(key).cloneNode(true));
	                }
	            }
	        }
	        taxesMap = new HashMap<String, Element>();
	        NodeList nlRetenciones = getNodesByExpression(doc, retencion);
	        if (nlRetenciones != null && nlRetenciones.getLength() > 0) {
	            for (int idx = 0; idx < nlRetenciones.getLength(); idx++) {
	                Element el = (Element) nlRetenciones.item(idx).cloneNode(true);
	                /*Si el tipoFactor es excento no se toma en cuenta*/
	                if (el.hasAttribute("TipoFactor")
	                        && !el.getAttribute("TipoFactor").equalsIgnoreCase("Exento")) {
	                    String key = el.getAttribute("Impuesto");
	                    Element elAux = taxesMap.get(key);
	                    if (elAux == null) {
	                        el.removeAttribute("Base");
	                        el.removeAttribute("TipoFactor");
	                        el.removeAttribute("TasaOCuota");
	                        taxesMap.put(key, el);
	                    } else {
	                        Node value = elAux.getAttributes().getNamedItem("Importe");
	                        BigDecimal importeAux = new BigDecimal(value.getNodeValue());
	                        BigDecimal importe = new BigDecimal(el.getAttribute("Importe"));
	                        value.setNodeValue(importeAux.add(importe).toString());
	                        taxesMap.put(key, elAux);
	                    }
	                }
	            }
	            if (!taxesMap.isEmpty()) {
	                hasRetenciones = true;
	                for (String key : taxesMap.keySet()) {
	                    eRetenciones.appendChild(taxesMap.get(key).cloneNode(true));
	                }
	            }
	        }
	        if (hasTraslados || hasRetenciones) {
	            String taxes = "//Comprobante/Impuestos";
	            NodeList nlTaxes = getNodesByExpression(doc, taxes);
	            if (nlTaxes != null && nlTaxes.getLength() > 0) {
	                Element elCon = (Element) nlTaxes.item(0);
	                if (hasRetenciones) {
	                    elCon.appendChild(eRetenciones);
	                }
	                if (hasTraslados) {
	                    elCon.appendChild(eTraslados);
	                }
	            }
	        }
	    }
		
		public static String validateDecimals(Document doc, int maxDecimals) throws XPathExpressionException {
	        StringBuilder sbError = new StringBuilder();
	        evaluateDecimals(doc, "//Comprobante/@SubTotal", maxDecimals, "Subtotal", sbError);
	        evaluateDecimals(doc, "//Comprobante/@Total", maxDecimals, "Total", sbError);
	        
	        if (!Util.isNullEmpty(getStringValByExpression(doc, "//Comprobante/@TipoCambio"))) 
	        	if (!getStringValByExpression(doc, "//Comprobante//@Moneda").equalsIgnoreCase("MXN"))
	        		evaluateDecimals(doc, "//Comprobante/@TipoCambio", maxDecimals, "Tipo de Cambio", sbError);
	            
		        
	        if (!Util.isNullEmpty(getStringValByExpression(doc, "//Comprobante/@Descuento"))) {
	            evaluateDecimals(doc, "//Comprobante/@Descuento", maxDecimals, "Descuento", sbError);
	        }
	        String concepts = "//Comprobante/Conceptos/Concepto";
	        BigDecimal totalConcept = BigDecimal.valueOf(getDoubleByExpression(doc, "count(".concat(concepts.intern()).concat(")")));
	        if (totalConcept.intValue() > 0) {
	            for (int idxConcept = 0; idxConcept < totalConcept.intValue(); idxConcept++) {
	                String currentConcept = concepts.intern().concat("[" + (idxConcept + 1) + "]");
	                String currDescConcept = "Concepto[" + (idxConcept + 1) + "]";
	                evaluateDecimals(doc, currentConcept.intern().concat("/@Importe"), maxDecimals, currDescConcept.concat("-Importe"), sbError);
	                evaluateDecimals(doc, currentConcept.intern().concat("/@ValorUnitario"), maxDecimals, currDescConcept.concat("-Importe"), sbError);
	                String traslados = currentConcept.intern().concat("/Impuestos/Traslados/Traslado");
	                String retenciones = currentConcept.intern().concat("/Impuestos/Retenciones/Retencion");
	                BigDecimal totalTraslados = BigDecimal.valueOf(getDoubleByExpression(doc, "count(".concat(traslados.intern()).concat(")")));
	                BigDecimal totalRetenciones = BigDecimal.valueOf(getDoubleByExpression(doc, "count(".concat(retenciones.intern()).concat(")")));
	                for (int idxTras = 0; idxTras < totalTraslados.intValue(); idxTras++) {
	                    String currentTras = traslados.concat("[" + (idxTras + 1) + "]");
	                    String currDescTras = currDescConcept.intern().concat("-Traslado[").concat(String.valueOf(idxTras + 1)).concat("]");
	                    evaluateDecimals(doc, currentTras.intern().concat("/@Base"), maxDecimals, currDescTras.concat("-Base"), sbError);
	                    evaluateDecimals(doc, currentTras.intern().concat("/@Importe"), maxDecimals, currDescTras.concat("-Importe"), sbError);
	                    //evaluateDecimals(doc, currentTras.intern().concat("/@TasaOCuota"), maxDecimals, currDescTras.concat("-Tasa o Cuota"), sbError);
	                }
	                for (int idxRet = 0; idxRet < totalRetenciones.intValue(); idxRet++) {
	                    String currentRet = retenciones.concat("[" + (idxRet + 1) + "]");
	                    String currDescRet = currDescConcept.intern().concat("-Retencion[").concat(String.valueOf(idxRet + 1)).concat("]");
	                    evaluateDecimals(doc, currentRet.intern().concat("/@Base"), maxDecimals, currDescRet.concat("-Base"), sbError);
	                    evaluateDecimals(doc, currentRet.intern().concat("/@Importe"), maxDecimals, currDescRet.concat("-Importe"), sbError);
	                    //evaluateDecimals(doc, currentRet.intern().concat("/@TasaOCuota"), maxDecimals, currDescRet.concat("-Tasa o Cuota"), sbError);
	                }
	            }
	        }
	        evaluateDecimals(doc, "//Comprobante/Impuestos/@TotalImpuestosRetenidos", maxDecimals, "Total Imp Retenidos", sbError);
	        evaluateDecimals(doc, "//Comprobante/Impuestos/@TotalImpuestosTrasladados", maxDecimals, "Total Imp Traslados", sbError);
	        if (sbError.length() > 0) {
	            sbError = new StringBuilder("Los siguientes valores no tienen la cantidad de decimales para la moneda (Permitidos=")
	                    .append(maxDecimals).append("):\n").append(sbError);
	        }
	        System.out.println(sbError);
	        return sbError.toString();
	    }
		private static void evaluateDecimals(Document doc, String expression, int maxDecimals, String messageError, StringBuilder sbError) throws XPathExpressionException {
	        NodeList nl = getNodesByExpression(doc, expression);
	        if (nl != null && nl.getLength() > 0) {
	            String val = getStringValByExpression(doc, expression);
	            BigDecimal bd = new BigDecimal(val);
	            //Los ceros a la derecha no se toman en cuenta para evaluar 
	            val = bd.stripTrailingZeros().toPlainString();
	            if (!hasNDecimals(val, maxDecimals)) {
	                int countDecimals = countDecimals(val);
	                if (countDecimals < maxDecimals) {		                	
			            if (val == null || val.isEmpty()) {
			                val = "0.0";
			                maxDecimals--;//Se resta uno para que se ajuste por el que grega;
			            } else if (!val.contains(".")) {
			                val += ".0";//Se resta uno para que se ajuste por el que grega;
			                maxDecimals--;
			            }
	                    val = completeWithCharacter(val, "0", (maxDecimals - countDecimals));
	                    nl.item(0).setNodeValue(val);
	                } else {
	                    sbError.append("   -").append(messageError)
	                            .append(", valor=").append(val).append("\n");
	                }
	            }
	        }
	    }
		public static String evaluateCalulationMasiva(Document doc, int maxDecimals){
	    	try{    	
		        logger.info("Iniciando Validaciones de Calculos");
		        BigDecimal compTotal = BigDecimal.valueOf(getDoubleByExpression(doc, "//Comprobante/@Total"));
		        StringBuilder sb = new StringBuilder("(//Comprobante/Conceptos/Concepto/@Importe)");
		        BigDecimal conceptTotal = getBigDecimalByNodeExpression(doc, sb.toString());
		        sb = new StringBuilder("(//Comprobante/Conceptos/Concepto/Impuestos/Traslados/Traslado/@Importe)");
		        BigDecimal traslTotal = getBigDecimalByNodeExpression(doc, sb.toString());
		        BigDecimal traslados = conceptTotal.add(traslTotal);
		        sb = new StringBuilder("(//Comprobante/Conceptos/Concepto/Impuestos/Retenciones/Retencion/@Importe)");
		        BigDecimal retenciones = getBigDecimalByNodeExpression(doc, sb.toString());
		        BigDecimal discount = getBigDecimalByNodeExpression(doc, "//Comprobante/@Descuento");
		        retenciones = retenciones.add(discount);	        
		
		        BigDecimal totalOper = retenciones.equals(BigDecimal.valueOf(0)) ? traslados : traslados.add(retenciones.multiply(BigDecimal.valueOf(-1)));
		        if (compTotal.doubleValue() == totalOper.doubleValue()) {
		
		        } else {
		            System.out.println("totalOper=" + totalOper);
		            System.out.println("compTotal=" + compTotal);
		            System.out.println("retenciones=" + retenciones);
		            System.out.println("traslados=" + traslados);
		            System.out.println("Comptaracion=" + (compTotal.doubleValue() == totalOper.doubleValue()));
		            throw new Exception(
		                    "El campo Total no corresponde con la suma del subtotal, menos los descuentos aplicables, más las contribuciones recibidas (impuestos trasladados - federales o locales, derechos, productos, aprovechamientos, aportaciones de seguridad social, contribuciones de mejoras) menos los impuestos retenidos");
		        }
		
		        //logger.info("Validando descuentos");
		        /*Asignacion y validacion de Descuento*/
		        String voucherType = getStringValByExpression(doc, "//Comprobante/@TipoDeComprobante");
		        //System.out.println("discount:" + discount);
		        boolean validateDiscount = false;
		        if (discount.doubleValue() > 0) {//Si no viene con valor omitimos este paso
		            validateDiscount = true;
		        }
		        if (voucherType.equalsIgnoreCase("I")
		                || voucherType.equalsIgnoreCase("E")
		                || voucherType.equalsIgnoreCase("N")) {
		            if (validateDiscount) {
		                //El subtotal debe ser mayor o igual al descuento
		                BigDecimal subTotal = BigDecimal.valueOf(getDoubleByExpression(doc, "//Comprobante/@SubTotal"));
		                if (discount.doubleValue() > subTotal.doubleValue()) {
		                    throw new Exception(errorMessage.get("ErrCompSubTotal005"));
		                }
		            }
		            String concepts = "//Comprobante/Conceptos/Concepto";
		            BigDecimal totalConcept = BigDecimal.valueOf(getDoubleByExpression(doc, "count(".concat(concepts.intern()).concat(")")));
		            //System.out.println("Conceptos:" + totalConcept);
		            if (totalConcept.doubleValue() > 0) {//Verificamos que vengan conceptos
		                //Obtenemos el decuento por concepto
		                BigDecimal discPerConcept = discount.divide(totalConcept, maxDecimals, RoundingMode.HALF_UP);
		                BigDecimal last = discount.subtract(discPerConcept.multiply(totalConcept));
		                NodeList nl = getNodesByExpression(doc, concepts.intern());
		                if (nl != null) {
		                    String disc = discPerConcept.toString();
		                    for (int i = 0; i < nl.getLength(); i++) {
		                        Element el = ((Element) nl.item(i));
		                        BigDecimal importe = new BigDecimal(el.getAttribute("Importe"));
		                        BigDecimal valorUnitario = new BigDecimal(el.getAttribute("ValorUnitario"));
		                        if (valorUnitario.doubleValue() <= 0) {
		                            throw new Exception(errorMessage.get("ErrCompValUni003"));
		                        }
		                        if (validateDiscount) {
		                            if ((i + 1) == nl.getLength() && last.doubleValue() > 0) {
		                                //Se suma el descuento por concepto mas
		                                discPerConcept = discPerConcept.add(last);
		                                disc = discPerConcept.toString();
		                            }
		                            if (discPerConcept.doubleValue() > importe.doubleValue()) {
		                                throw new Exception(MessageFormat.format(errorMessage.get("ErrConcImport002"), (i + 1), discPerConcept, importe));
		                            }
		                            el.setAttribute("Descuento", disc.intern());
		                        }
		                    }
		                }
		            }
		        } else {
		            if (validateDiscount) {
		                throw new Exception(errorMessage.get("ErrCompTipoComprobante002"));
		            }
		        }
	    	}catch(Exception ex){
	    		logger.error(ex);
	    		return ex.getMessage();
	    	}
	    	return "";
	    }
		public static String completeWithCharacter(String val, String character, int length) {
	        StringBuilder outputBuffer = new StringBuilder(val);
	        for (int i = 0; i < length; i++) {
	            outputBuffer.append(character);
	        }
	        return outputBuffer.toString();
	    }
		
		private static int countDecimals(double val) {
	        String text = Double.toString(Math.abs(val));
	        return countDecimals(text);
	    }
		public static int countDecimals(String val) {
	        int integerPlaces = val.indexOf('.');
	        if (integerPlaces == -1) {
	            return 0;
	        }
	        int decimalPlaces = val.length() - integerPlaces - 1;
	        return decimalPlaces;
	    }
		
		private static boolean hasNDecimals(String value, int maxDecimals) {
	        String regex = "^\\d+\\.?\\d{" + maxDecimals + "}$";
	        Pattern pattern = Pattern.compile(regex);
	        Matcher matcher = pattern.matcher(value);
	        return matcher.find();
	    }

	    public static String parseWsError(String xmlResponse) {
	        StringBuilder sbError = new StringBuilder();
	        try {
	            if (xmlResponse == null || xmlResponse.trim().isEmpty()) {
	                return "";
	            }
	            Document docErr = UtilCatalogos.convertStringToDocument(xmlResponse);
	            if (docErr != null
	                    && docErr.getDocumentElement() != null
	                    && "A".equalsIgnoreCase(docErr.getDocumentElement().getNodeName())) {
	                String valErr = docErr.getDocumentElement().getAttribute("ErrorMax");
	              //Se suman 15 por la logngitud de "</Validaciones>"
	                if (!valErr.contains("<Validaciones") || !valErr.contains("</Validaciones>")) {
	                    return "";
	                }
	                valErr = valErr.substring(valErr.indexOf("<Validaciones"), valErr.indexOf("</Validaciones>") + 15);
	                if (valErr != null && !valErr.trim().isEmpty()) {
	                    valErr = valErr.trim();
	                    Document respose = UtilCatalogos.convertStringToDocument(valErr);
	                    NodeList nErrors = respose.getElementsByTagName("Error");
	                    if (nErrors != null && nErrors.getLength() > 0) {
	                        for (int idx = 0; idx < nErrors.getLength(); idx++) {
	                            Element eElement = (Element) nErrors.item(idx);
	                            NodeList messageClient = eElement.getElementsByTagName("MensajeParaCliente");
	                            if (messageClient != null
	                                    && messageClient.getLength() > 0
	                                    && messageClient.item(0).getFirstChild().getNodeValue() != null
	                                    && !messageClient.item(0).getFirstChild().getNodeValue().trim().isEmpty()) {
	                                sbError.append("\n   -").append(messageClient.item(0).getFirstChild().getNodeValue());
	                            } else {
	                                NodeList messageGrl = eElement.getElementsByTagName("Mensaje");
	                                if (messageGrl != null
	                                        && messageGrl.getLength() > 0
	                                        && messageGrl.item(0).getFirstChild().getNodeValue() != null
	                                        && !messageGrl.item(0).getFirstChild().getNodeValue().trim().isEmpty()) {
	                                    sbError.append("\n   -").append(messageGrl.item(0).getFirstChild().getNodeValue());
	                                }
	                            }
	                        }
	                    }
	                }
	            }
	            if (sbError.length() > 0) {
	                sbError.insert(0, "Se encontraron los siguientes errores:");
	            }
	        } catch (ParserConfigurationException ex) {
	            logger.error(ex);
	        } catch (SAXException ex) {
	            logger.error(ex);
	        } catch (IOException ex) {
	            logger.error(ex);
	        }
	        return sbError.toString();
	    }
	    
	  //encuentra clave por descripcion
		public static String findDescripcionTipoCadenaPagoByDescripcion(Map<String, ArrayList<CatalogosDom>> mapCatalogos,
				String tipoCadenaPago) {
			String response = "";
			if (mapCatalogos.size() > 0 && tipoCadenaPago.trim() != "") {
				for (int i = 0; i < mapCatalogos.get("TipoCadenaPago").size(); i++) {
					if (mapCatalogos.get("TipoCadenaPago").get(i).getVal2() != null) {
						if (mapCatalogos.get("TipoCadenaPago").get(i).getVal2().equalsIgnoreCase(tipoCadenaPago)) {
							if (mapCatalogos.get("TipoCadenaPago").get(i).getVal1() != null) {
								response = mapCatalogos.get("TipoCadenaPago").get(i).getVal1();
							} else {
								response = "vacio";
							}
							break;
						} else {
							response = "vacio";
						}
					} else {
						response = "vacio";
					}
				}
			} else {
				response = "vacio";
			}
			return response;
		}
		
		// Validacion Encuentra Valor de Retencion del catalogo claveProdServ a travez
		// de la descripcion AMDA
		public static boolean validaNumAuth(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value) {
			boolean response = false;
			logger.info("validaNumAuth AMDA: " + value);

			if (value != null && value.trim().length() > 0) {
				String regex = "^[0-9a-zA-Z]{5}";
				if (value.matches(regex)) {
					response = true;
				} else {
					response = false;
				}
			} else {
				response = false;
			}
			logger.info("validaNumAuth AMDA response: " + response);
			return response;
		}
		
		// Validacion Encuentra Valor de Retencion del catalogo claveProdServ a travez
		// de la descripcion AMDA
		public static boolean validaCFDIRelacional(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value) {
			boolean response = false;
			String valres = "";
			logger.info("validaCFDIRelacional AMDA: " + value);

			if (value != null && value.trim().length() > 0) {
				String regex = "^[a-f0-9A-F]{8}-[a-f0-9A-F]{4}-[a-f0-9A-F]{4}-[a-f0-9A-F]{4}-[a-f0-9A-F]{12}";

				if (value.matches(regex)) {
					response = true;
				} else {
					response = false;
				}
			} else {
				response = false;
			}
			logger.info("validaCFDIRelacional AMDA response: " + response);
			return response;
		}
		
		// Validacion Encuentra Valor de Tipo Relacion del catalogo tipoRelacion a
		// travez de la descripcion AMDA
		public static String validaTipoRelacion(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String value,
				Integer cfdiRelacion) {
			String response = "";
			String valres = "";
			logger.info("validaCFDIRelacional AMDA: " + value);
			if (mapCatalogos.size() > 0 && value != null) {
				for (int i = 0; i < mapCatalogos.get("TipoRelacion").size(); i++) {
					if (mapCatalogos.get("TipoRelacion").get(i).getVal2() != null) {
						if (mapCatalogos.get("TipoRelacion").get(i).getVal2().equalsIgnoreCase(value)) {
							response = mapCatalogos.get("TipoRelacion").get(i).getVal1();
							break;
						} else {
							response = "No se encontro el Tipo de Relacion en el catalogo c_TipoRelacion";
						}

					} else {
						response = "No se encontro el Tipo de Relacion en el catalogo c_TipoRelacion";
					}

				}

			} else {
				if (value == null) {
					response = "Es requerido un Tipo de Relacion ya que el campo CFDI Relacional tiene informacion";
				} else {
					response = "No se encontro el Tipo de Relacion en el catalogo c_TipoRelacion";
				}

			}
			logger.info("validaCFDIRelacional AMDA response: " + response);
			return response;
		}
		
		//retorna true si un concepto aplica iva: impuesto 002, !Exento
		public static boolean conceptoAplicaIva(Map<String, ArrayList<CatalogosDom>> mapCatalogos, String descCon){
			boolean response = false;

			if(mapCatalogos.size() > 0 && descCon.trim().length() > 0){
				
				for(int i=0; i<mapCatalogos.get("EquivalenciaConceptoImpuesto").size(); i++){
					if( mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal1().equalsIgnoreCase("002")
							&& !mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal2().equalsIgnoreCase("Exento")
							&& mapCatalogos.get("EquivalenciaConceptoImpuesto").get(i).getVal5().equalsIgnoreCase(descCon)){
						response = true;
						break;
					}
				}
			}
			return response;
		}
}
