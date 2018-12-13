package com.interfactura.firmalocal.xml.util;

import static com.interfactura.firmalocal.xml.util.Util.getTASA;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.interfactura.firmalocal.datamodel.CfdiAddendaInformacionPago;
import com.interfactura.firmalocal.datamodel.CfdiAddendaInmuebles;
import com.interfactura.firmalocal.datamodel.CfdiComprobanteFiscal;
import com.interfactura.firmalocal.datamodel.CfdiConcepto;
import com.interfactura.firmalocal.datamodel.CfdiConceptoImpuestoTipo;
import com.interfactura.firmalocal.datamodel.CfdiDomicilio;
import com.interfactura.firmalocal.datamodel.CfdiEmisor;
import com.interfactura.firmalocal.datamodel.CfdiImpuesto;
import com.interfactura.firmalocal.datamodel.CfdiReceptor;
import com.interfactura.firmalocal.datamodel.ComplementoPago;
import com.interfactura.firmalocal.datamodel.CustomsInformation;
import com.interfactura.firmalocal.datamodel.ElementsInvoice;
import com.interfactura.firmalocal.datamodel.FarmAccount;
import com.interfactura.firmalocal.datamodel.Invoice;
import com.interfactura.firmalocal.datamodel.Part;
import com.interfactura.firmalocal.datamodel.Retenciones;
import com.interfactura.firmalocal.datamodel.TimbreFiscal;
import com.interfactura.firmalocal.datamodel.Traslados;
import com.interfactura.firmalocal.domain.entities.CFDFieldsV22;
import com.interfactura.firmalocal.domain.entities.CodigoISO;
import com.interfactura.firmalocal.domain.entities.Customer;
import com.interfactura.firmalocal.domain.entities.FiscalEntity;
import com.interfactura.firmalocal.domain.entities.Iva;
import com.interfactura.firmalocal.pdf.util.NumberToLetterConverter;
import com.interfactura.firmalocal.persistence.CFDFieldsV22Manager;
import com.interfactura.firmalocal.persistence.CodigoISOManager;
import com.interfactura.firmalocal.persistence.CustomerManager;
import com.interfactura.firmalocal.persistence.FiscalEntityManager;
import com.interfactura.firmalocal.persistence.IvaManager;
import com.interfactura.firmalocal.xml.TagsXML;
import com.interfactura.firmalocal.xml.util.ValidationConstants.TipoComprobante;
import com.interfactura.firmalocal.xml.util.ValidationConstants.TipoEmision;

@Component
public class UtilCFDIValidationsDivisas {
	@Autowired(required = true)
	private FiscalEntityManager fiscalEntityManager;
	@Autowired
	private TagsXML tags;
	@Autowired(required = true)
	private CustomerManager customerManager;
	@Autowired
	private IvaManager ivaManager;
	@Autowired(required = true)
	private CodigoISOManager codigoISOManager;
	
	@Autowired(required = true)
    private CFDFieldsV22Manager cfdFieldsV22Manager;
	
	private static final String RE_DECIMAL = "[0-9]+(\\.[0-9][0-9]?[0-9]?[0-9]?)?";
	private static final String RE_MAIL = "^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,3})$";
	private static final String RE_DECIMAL_NEGATIVO = "[\\-]?[0-9]{1,10}(\\.[0-9]{0,4})?";
	private static final String ID_EXTRANJERO = "[A-za-z0-9]{0,}";
	private static final String SERIE = "[A-za-z0-9]{0,13}";
	private static final String SERIE_25 = "[A-za-z0-9]{0,25}";
	private static final List<String> TIPO_COMPROBANTE = Arrays.asList("EGRESO", "INGRESO");
	private static final List<String> RFC_GENERICO = Arrays.asList("XEXX010101000", "XAXX010101000");
	private static final List<String> TIPOS_ADENDA = Arrays.asList("", "0", "1", "2", "3");
	private static final String NOMBRE_CLIENTE_REGEX = "[\\wÑñáéíóúÁÉÍÓÚ\\s\\Q$%?)¡¿([]{}+/=@_!#*:;.&,-\\E]{250}";
	private static final String NUMBERS_LETTERS_REGEX = "[\\w]+";
	private static final String FOUR_NUMBERS_REGEX = "[0-9]+{4}";
	private static final String NUMBERS_LETTERS_DASH_SLASH_DOT_COMMA_REGEX = "[A-Za-z 0-9áéíóúÁÉÍÓÚñÑ\\.,()\\-\\/&]+";
	private static final String NUMBERS_LETTERS_UNDERSCORE_DASH_SLASH_REGEX_WHITESPACE = " [a-zA-Z0-9&_\\/\\-\\s]*";
	
	private static final String RFC_PATTERN = "[A-Z,Ñ,&]{3,4}[0-9]{2}[0-1][0-9][0-3][0-9][A-Z,0-9]?[A-Z,0-9]?[0-9,A-Z]?";
	private static final String RFC_PATTERN_TWO = "[A-Z&Ñ]{3,4}[0-9]{2}(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])[A-Z0-9]{2}[0-9A]";
	private static final String FECHA_RECEPCION_PATTERN = "([0-2][0-9]||3[0-1])/(0[0-9]||1[0-2])/((19|20)\\d\\d)";
	private static final String POSTAL_CODE_PATTERN = "([0-9]{5})";
	private static final String RE_CHAR = "[A-Za-z 0-9ÑñáÁéÉíÍóÓúÚ\\.,()\\-\\/&]+";
	private static final String RE_CHAR_NUMBER = "[A-Za-z0-9]+";
	private static final String RE_CHAR_ALL = "[A-za-z0-9ÑñáÁéÉíÍóÓúÚ$%?.)¡¿.(.[.].{.}+/=@_!#*:;&,-. ]{0,100}";
	private static final String RE_NUMBER = "[0-9]{0,}";
	
	private static final String APARTADO_COMPLEMENT = "[^|]{1,300}";
	private static final String APART_COMPLEMENT_RFC_ACOUTN = "XEXX010101000|[A-Z&amp;Ñ]{3}[0-9]{2}(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])[A-Z0-9]{2}[0-9A]";
	private static final String APARTADO_COMPLEMENT_NBANCOORD = "[^|]{1,300}";
	private static final String APARTADO_COMPLEMENT_CORDENANTE = "[A-Z0-9_]{10,50}";
	private static final String APARTADO_COMPLEMENT_CBENEFICIARIO = "[A-Z0-9_]{10,50}";
	private static final String APARTADO_COMPLEMENT_ID_DOCUMENTO = "([a-f0-9A-F]{8}-[a-f0-9A-F]{4}-[a-f0-9A-F]{4}-[a-f0-9A-F]{4}-[a-f0-9A-F]{12})|([0-9]{3}-[0-9]{2}-[0-9]{9})";
	private static final String APARTADO_COMPLEMENT_SERIE = "[^|]{1,25}";
	private static final String APARTADO_COMPLEMENT_FOLIO = "[^|]{1,40}";
	private static final String APARTADO_COMPLEMENT_NUM_PARCIALIDAD = "[1-9][0-9]{0,2}";
	private static final String REFERENCIA_FACTURA = "[A-Z0-9]{10,50}";
	
	private static final String CCOSTOS = "[A-za-z0-9_/& -]{0,20}";

	Vector<String> vectorCantidad = null;
	Vector<String> vectorUM = null;
	Vector<String> vectorDesc = null;
	Vector<Double> vectorPrecioUnitario = null;
	Vector<String> vectorAplicaIVA = null;
	Vector<Double> vectorTotal = null;
	Vector<Double> vectorBase = null;
	Vector<String> vectorImpuesto = null;
	Vector<String> vectorTipFactor = null;
	Vector<Double> vectorTasaOCuota = null;
	Vector<Double> vectorImporte = null;
	Vector<String> vectorClaveProdServ = null;
	Vector<String> vectorClaveUnidad = null;
	Vector<String> vectorCantidadRet = null;
	Vector<String> vectorUMRet = null;
	Vector<String> vectorDescRet = null;
	Vector<Double> vectorPrecioUnitarioRet = null;
	Vector<String> vectorAplicaIVARet = null;
	Vector<Double> vectorTotalRet = null;
	Vector<Double> vectorBaseRet = null;
	Vector<String> vectorImpuestoRet = null;
	Vector<String> vectorTipFactorRet = null;
	Vector<Double> vectorTasaOCuotaRet = null;
	Vector<Double> vectorImporteRet = null;
	Vector<String> vectorClaveProdServRet = null;
	Vector<String> vectorClaveUnidadRet = null;
	boolean retencionBol = false;
	boolean trasladoBol = false;

	Vector<String> vectorTipoImpuesto = null;

	public HashMap<String, Object> createCfdiComprobanteFiscalBatch(Iterator<Row> rowIterator) {
		List<CfdiComprobanteFiscal> comprobantes = new ArrayList<CfdiComprobanteFiscal>();
		HashMap<String, Object> result = new HashMap<String, Object>();
		StringBuilder sbError = null;
		int factura = 0;
		int facturasOK = 0;
		int facturasError = 0;
		boolean fExitFinArchivo = false;
		FiscalEntity fiscalEntity = null;
		CfdiComprobanteFiscal comp = null;
		Customer customer = null;
		StringBuilder sb = new StringBuilder();
		while (rowIterator.hasNext() && !fExitFinArchivo) {
			Row row = rowIterator.next();
			if (row.getCell(0) != null) {
				if (row.getCell(0).toString().toUpperCase().trim().equals("||FINARCHIVO||")) {
					fExitFinArchivo = true;
				}
			}

			if (!fExitFinArchivo) {
				sbError = new StringBuilder();

				factura += 1;
				int lastCellNum = row.getLastCellNum();

				if (lastCellNum >= 32) {
					comp = new CfdiComprobanteFiscal();
					sbError.append(fillComprobante(comp, row, fiscalEntity, facturasError, customer, lastCellNum));

				} else {
					sbError.append("Cuerpo de la Factura " + factura + ", incompleto" + "\n");
				}
				if (sbError.length() == 0) {
					Invoice inv = fillInvoice(comp);
					facturasOK += 1;

				} else {
					facturasError += 1;
					sb.append("Factura: " + factura + " -- Lista de Errores: " + "\n" + sbError.toString());
				}
			}
		}
		return result;
	}

	public String validaInvoice(Invoice invoice) {
		StringBuilder result = new StringBuilder();
		/* Falta entidad */
		/* Valida serie */
		if (invoice.getSerie() != null && !validaDatoRELongitud(invoice.getSerie(), SERIE, 13)) {
			result.append("(2) Serie(" + invoice.getSerie() + ") con formato incorrecto ");
		}
		/* Forma de pago */
		if (invoice.getFormaPago() != null && !invoice.getFormaPago().trim().isEmpty()) {
			// TODO: Validar foram de apgo en catalgo_c_FormaPago
		} else {
			result.append("(3) Falta Forma Pago ");
		}
		/* Motivo descuento */
		if (invoice.getMotivoDescuento() != null && invoice.getMotivoDescuento().length() > 1500) {
			result.append("(4) Motivo Descuento longitud excedida. ");

		}
		/* Descuento */
		BigDecimal descuentoTMP = BigDecimal.valueOf(invoice.getDescuento());
		if (descuentoTMP.compareTo(BigDecimal.ZERO) > 0) {
			// TODO:Validacion descuento
		}

		/* Moneda */
		if (invoice.getMoneda() != null && !invoice.getMoneda().trim().isEmpty()) {
			// TODO: validar que modena exista en catalgo c_Moneda
		}

		/* Tipo de cambio */
		if (invoice.getTipoCambio() != null && !invoice.getTipoCambio().trim().isEmpty()) {
			// TODO: validacion tipo de cambio
		} else {
			result.append("(7) Falta Tipo Cambio");
		}

		/* Tipo comprobante */
		if (invoice.getTipoDeComprobante() != null && !invoice.getTipoDeComprobante().trim().isEmpty()) {
			if (!TIPO_COMPROBANTE.contains(invoice)) {
				result.append("(8) Tipo Comprobante no valido");
			}
		} else {
			// asignar valor por default
			invoice.setTipoDeComprobante("INGRESO");
		}
		/* Metodo de pago */
		if (invoice.getMetodoPago() != null && !invoice.getMetodoPago().trim().isEmpty()) {
			// TODO: validar en catalgo c_MetodoPago
		} else {
			result.append("(9) Falta metodo de pago");

		}
		/* Regimen fiscal */
		if (invoice.getRegimenFiscal() != null && !invoice.getRegimenFiscal().trim().isEmpty()) {
			// TODO: validar en catalgo c_RegimenFiscal
		} else {
			result.append("(10) Falta Regimen Fiscal");
		}
		/* RFC */
		if (invoice.getRfc() != null && !invoice.getRfc().trim().isEmpty()) {
			// TODO: validar rfc en bd
		} else {
			result.append("(11) Falta RFC");
		}
		if (RFC_GENERICO.contains(invoice.getRfc()) && invoice.getIdExtranjero().trim().isEmpty()) {
			if (invoice.getName() == null || invoice.getName().trim().isEmpty()
					|| !invoice.getName().matches(NOMBRE_CLIENTE_REGEX)) {
				result.append("(12) Nombre Cliente formato no valido");
			}
		}
		/* Id extranjero */
		if (invoice.getIdExtranjero() != null && !invoice.getIdExtranjero().trim().isEmpty()) {
			// TODO: validar bd
			boolean idExtExist = true;
			if (idExtExist && !invoice.getIdExtranjero().matches(NUMBERS_LETTERS_REGEX)) {
				result.append("(13) Id Extranjero formato no valido");
			}
		}
		/* Uso CFDI */
		if (invoice.getUsoCFDI() != null && !invoice.getUsoCFDI().trim().isEmpty()) {
			// TODO: validar bd
		} else {
			result.append("(14) Falta Uso CFDI");
		}
		/* NumRegIdTrib */
		// TODO: validacion condicional?
		/* Numero de cuenta */
		if (invoice.getAccountNumber() != null && !invoice.getAccountNumber().trim().isEmpty()) {
			if (!invoice.getAccountNumber().matches(FOUR_NUMBERS_REGEX)) {
				result.append("(16) Numero de cuenta formato invalido");
			}
		} else {
			result.append("(16) Falta Numero de cuenta");
		}
		/* Reference */
		if (invoice.getReference() != null && !invoice.getReference().trim().isEmpty()) {
			if (!validaDatoRE(invoice.getReference(), NUMBERS_LETTERS_DASH_SLASH_DOT_COMMA_REGEX + "{250}")) {
				result.append("(17)  Referencia formato invalido");
			}
		}
		if (invoice.getReferencia() != null && !invoice.getReferencia().trim().isEmpty()) {
			if (!validaDatoRE(invoice.getReferencia(), NUMBERS_LETTERS_DASH_SLASH_DOT_COMMA_REGEX + "{250}")) {
				result.append("(18) Referencia formato invalido");
			}
		}
		/* Codigo del cliente */
		// TODO: validar codigo del cliente
		/* Contrato */
		if (invoice.getContractNumber() != null && !invoice.getContractNumber().trim().isEmpty()) {
			if (!invoice.getContractNumber().matches(NUMBERS_LETTERS_UNDERSCORE_DASH_SLASH_REGEX_WHITESPACE + "{20}")) {
				result.append("(19) Contrato formato invalido");
			}
		}
		/* Perido */
		if (invoice.getPeriod() != null && !invoice.getPeriod().trim().isEmpty()) {
			if (!invoice.getPeriod().matches(NUMBERS_LETTERS_UNDERSCORE_DASH_SLASH_REGEX_WHITESPACE + "{19}")) {
				result.append("(20) Perido formato invalido");
			}
		}
		/* C. Costos */
		if (invoice.getCostCenter() != null && !invoice.getCostCenter().trim().isEmpty()) {
			if (!invoice.getCostCenter().matches(NUMBERS_LETTERS_UNDERSCORE_DASH_SLASH_REGEX_WHITESPACE + "{20}")) {
				result.append("(21) C. Costos formato invalido");
			}
		}
		/* Descripcion */
		// TODO: Cual descripcion d-.-b
		/* IVA */
		// if(invoice.getiva){
		//
		// }
		/* Tipo addenda */
		if (!TIPOS_ADENDA.contains(invoice.getTipoAddenda())) {
			result.append("(24) Tipo Addenda no valido");
		}
		/* Email proveedor */
		return result.toString();
	}

	public String fillComprobante(CfdiComprobanteFiscal comp, Row row, FiscalEntity fiscalEntity, int factura,
			Customer customer, int lastCellNum) {
		StringBuilder sbError = new StringBuilder();
		// fiscalEntity = new FiscalEntity();
		/* Emisor Posicion 0--row 0 */
		if (row.getCell(0) == null) {
			sbError.append("(1) Posicion Entidad Fiscal requerida (Null) - Factura " + factura + "\n");
		} else {
			fiscalEntity.setTaxID(row.getCell(0).toString());
			fiscalEntity = fiscalEntityManager.findByRFCA(fiscalEntity);
			if (fiscalEntity == null) {
				sbError.append("(1) Entidad Fiscal no existe en BD - Factura " + factura + "\n");
			} else {
				if (fiscalEntity.getIsDonataria() == 1) {
					sbError.append("(1) Entidad Fiscal incorrecta, es donataria - Factura " + factura + "\n");
				} else {
					comp.setEmisor(new CfdiEmisor());
					comp.getEmisor().setNombre(fiscalEntity.getFiscalName());
					comp.getEmisor().setRfc(fiscalEntity.getTaxID());
					comp.getEmisor().setDomicilio(new CfdiDomicilio());
					if (fiscalEntity.getAddress() != null) {
						comp.getEmisor().getDomicilio().setCalle(fiscalEntity.getAddress().getStreet());
						comp.getEmisor().getDomicilio().setCodigoPostal(fiscalEntity.getAddress().getZipCode());
						comp.getEmisor().getDomicilio().setColonia(fiscalEntity.getAddress().getNeighborhood());
						comp.getEmisor().getDomicilio().setEstado(fiscalEntity.getAddress().getState().getName());
						comp.getEmisor().getDomicilio().setLocalidad(fiscalEntity.getAddress().getRegion());
						comp.getEmisor().getDomicilio().setMunicipio(fiscalEntity.getAddress().getCity());
						comp.getEmisor().getDomicilio().setNoExterior(fiscalEntity.getAddress().getExternalNumber());
						comp.getEmisor().getDomicilio().setNoInterior(fiscalEntity.getAddress().getExternalNumber());
						comp.getEmisor().getDomicilio()
								.setPais(fiscalEntity.getAddress().getState().getCountry().getName());
						comp.getEmisor().getDomicilio().setReferencia(fiscalEntity.getAddress().getReference());
					}
				}
			}
		}

		/* Serie Posicion 1 -- row 1 */
		comp.setSerie(row.getCell(1).toString().trim());
		if (row.getCell(1) == null) {
			comp.setSerie("");
		} else {

			if (row.getCell(1).toString().trim().length() > 0) {
				comp.setSerie(row.getCell(1).toString().trim());
			} else {
				comp.setSerie("");
			}

		}
		/* Tipo formato posicion 7 -- row 7 */
		if (row.getCell(7) == null) {
			comp.setTipoDeComprobante("I");
		} else {
			Map<String, Object> tipoComp = UtilValidationsXML.validTipoComprobante(tags.mapCatalogos,
					row.getCell(7).toString());
			if (!tipoComp.get("value").toString().equalsIgnoreCase("vacio")) {
				comp.setTipoDeComprobante(tipoComp.get("value").toString());
			} else {
				sbError.append(tipoComp.get("message").toString() + factura + "\n");
			}

		}

		/* Posicion 5 Moneda */
		if (row.getCell(5) != null && row.getCell(5).toString().trim().length() > 0) {
			Map<String, Object> tipoMon = UtilValidationsXML.validMoneda(tags.mapCatalogos, row.getCell(5).toString());
			if (!tipoMon.get("value").toString().equalsIgnoreCase("vacio")) {
				comp.setMoneda(tipoMon.get("value").toString());
			} else {
				sbError.append(tipoMon.get("message").toString() + factura + "\n");
			}
		} else {
			sbError.append("(CFDI33112) El campo Moneda no contiene un valor del catalogo c_Moneda " + factura + "\n");
		}

		/* Decimales moneda */
		if (comp.getMoneda() != null || comp.getMoneda().trim().length() > 0) {
			tags.decimalesMoneda = UtilCatalogos.findDecimalesMoneda(tags.mapCatalogos, comp.getMoneda());
		} else {
			tags.decimalesMoneda = 2;
		}

		/* Tipo de cambio */
		if (row.getCell(6) != null && row.getCell(6).toString().trim().length() > 0) {
			if (comp.getMoneda() != null) {
				if (!comp.getMoneda().trim().equalsIgnoreCase("XXX")) {
					Map<String, Object> tipoCam = UtilValidationsXML.validTipoCambio(tags.mapCatalogos,
							row.getCell(6).toString(), comp.getMoneda());
					if (!tipoCam.get("value").toString().equalsIgnoreCase("vacio")) {
						comp.setTipoCambio(tipoCam.get("value").toString());
					} else {
						sbError.append(tipoCam.get("message").toString() + factura + "\n");
					}
				} else {
					comp.setTipoCambio("");
				}
			}

		} else {
			sbError.append("(5) Tipo de Cambio es requerido - Factura " + factura + "\n");
		}

		/* RFC del cliente */
		if (row.getCell(10) == null) {
			sbError.append("Posicion RFC del Cliente requerida (Null) - Factura " + factura + "\n");
		} else {

			if (row.getCell(10).toString().trim().equals("")) {

				sbError.append(" RFC de Cliente requerido - Factura " + factura + "\n");
			} else {
				if (row.getCell(10).toString().trim().toUpperCase().equals("XEXX010101000")
						|| row.getCell(10).toString().trim().toUpperCase().equals("XAXX010101000")
						|| row.getCell(10).toString().trim().equals("XEXE010101000")) {

					if (row.getCell(12) == null || row.getCell(12).toString().trim().length() == 0) {

						sbError.append(" Id extranjero requerido - Factura " + factura + "\n");
					} else {
						String strCellType = checkCellType(row.getCell(12));
						if (!strCellType.equals("")) {
							sbError.append("Id Extranjero " + strCellType + " - Factura " + factura + "\n");
						} else {
							String strIDExtranjero = row.getCell(12).toString();
							System.out.println("ID Extranjero: " + strIDExtranjero);
							customer = customerManager.findByIdExtranjero(strIDExtranjero);
							comp.setReceptor(new CfdiReceptor());
							if (customer != null) {
								comp.getReceptor().setNombre(customer.getPhysicalName());
								if (row.getCell(14) != null || row.getCell(14).toString().length() > 0) {
									comp.getReceptor().setNumRegIdTrib(row.getCell(14).toString());
								} else {
									comp.getReceptor().setNumRegIdTrib("");
								}
								// comp.getReceptor().setResidenciaFiscal(customer.getFiscalEntity().geta);
								comp.getReceptor().setRfc(customer.getTaxId());
								if (row.getCell(13) == null || row.getCell(13).toString().trim().length() == 0) {
									comp.getReceptor().setUsoCFDI("D04");
								} else {
									Map<String, Object> tipoUsoCfdi = UtilValidationsXML.validUsoCFDI(tags.mapCatalogos,
											row.getCell(13).toString());
									if (!tipoUsoCfdi.get("value").toString().equalsIgnoreCase("vacio")) {
										comp.getReceptor().setUsoCFDI(tipoUsoCfdi.get("value").toString());
									} else {
										sbError.append(tipoUsoCfdi.get("message").toString() + factura + "\n");
									}
								}
								if (customer.getAddress() != null) {
									comp.getReceptor().setDomicilio(new CfdiDomicilio());
									comp.getReceptor().getDomicilio().setCalle(customer.getAddress().getStreet());
									comp.getReceptor().getDomicilio()
											.setCodigoPostal(customer.getAddress().getZipCode());
									comp.getReceptor().getDomicilio()
											.setColonia(customer.getAddress().getNeighborhood());
									comp.getReceptor().getDomicilio()
											.setEstado(customer.getAddress().getState().getName());
									comp.getReceptor().getDomicilio().setLocalidad(customer.getAddress().getRegion());
									comp.getReceptor().getDomicilio().setMunicipio(customer.getAddress().getCity());
									comp.getReceptor().getDomicilio()
											.setNoExterior(customer.getAddress().getExternalNumber());
									comp.getReceptor().getDomicilio()
											.setNoInterior(customer.getAddress().getInternalNumber());
									comp.getReceptor().getDomicilio()
											.setPais(customer.getAddress().getState().getCountry().getName());
									comp.getReceptor().getDomicilio()
											.setReferencia(customer.getAddress().getReference());
								}
							}
						}
					}
				} else {
					if (fiscalEntity != null) {
						customer = customerManager.get(row.getCell(10).toString().trim(),
								String.valueOf(fiscalEntity.getId()));
						comp.setReceptor(new CfdiReceptor());
						if (customer != null) {
							comp.getReceptor().setNombre(customer.getPhysicalName());
							if (row.getCell(14) != null || row.getCell(14).toString().length() > 0) {
								comp.getReceptor().setNumRegIdTrib(row.getCell(14).toString());
							} else {
								comp.getReceptor().setNumRegIdTrib("");
							}
							// comp.getReceptor().setResidenciaFiscal(customer.getFiscalEntity().geta);
							comp.getReceptor().setRfc(customer.getTaxId());
							if (row.getCell(13) == null || row.getCell(13).toString().trim().length() == 0) {
								comp.getReceptor().setUsoCFDI("D04");
							} else {
								Map<String, Object> tipoUsoCfdi = UtilValidationsXML.validUsoCFDI(tags.mapCatalogos,
										row.getCell(13).toString());
								if (!tipoUsoCfdi.get("value").toString().equalsIgnoreCase("vacio")) {
									comp.getReceptor().setUsoCFDI(tipoUsoCfdi.get("value").toString());
								} else {
									sbError.append(tipoUsoCfdi.get("message").toString() + factura + "\n");
								}
							}
							if (customer.getAddress() != null) {
								comp.getReceptor().setDomicilio(new CfdiDomicilio());
								comp.getReceptor().getDomicilio().setCalle(customer.getAddress().getStreet());
								comp.getReceptor().getDomicilio().setCodigoPostal(customer.getAddress().getZipCode());
								comp.getReceptor().getDomicilio().setColonia(customer.getAddress().getNeighborhood());
								comp.getReceptor().getDomicilio().setEstado(customer.getAddress().getState().getName());
								comp.getReceptor().getDomicilio().setLocalidad(customer.getAddress().getRegion());
								comp.getReceptor().getDomicilio().setMunicipio(customer.getAddress().getCity());
								comp.getReceptor().getDomicilio()
										.setNoExterior(customer.getAddress().getExternalNumber());
								comp.getReceptor().getDomicilio()
										.setNoInterior(customer.getAddress().getInternalNumber());
								comp.getReceptor().getDomicilio()
										.setPais(customer.getAddress().getState().getCountry().getName());
								comp.getReceptor().getDomicilio().setReferencia(customer.getAddress().getReference());
							}
						}
					}

				}
			}

		}

		/* Regimen fiscal */
		if (row.getCell(9) == null || row.getCell(9).toString().trim().equals("")) {
			sbError.append("(CFDI33130) El campo RegimenFiscal, no contiene un valor del catálogo c_RegimenFiscal-"
					+ " Factura " + factura + "\n");
		} else {

			Map<String, Object> tipoRegFis = UtilValidationsXML.validRegFiscal(tags.mapCatalogos,
					row.getCell(9).toString());
			if (!tipoRegFis.get("value").toString().equalsIgnoreCase("vacio")) {
				comp.getEmisor().setRegimenFiscal(tipoRegFis.get("value").toString());
			} else {
				sbError.append(tipoRegFis.get("message").toString() + " Factura " + factura + "\n");
			}
		}

		/* Metodo de pago */
		if (row.getCell(8) == null || row.getCell(8).toString().trim().equals("")) {
			sbError.append("(CFDI33121) El Campo Metodo Pago No Contiene Un Valor Del Catalogo C_MetodoPago - Factura "
					+ factura + "\n");
		} else {
			Map<String, Object> tipoMetPag = UtilValidationsXML.validMetodPago(tags.mapCatalogos,
					row.getCell(8).toString());
			if (tipoMetPag.get("value").toString().equals("vacio")) {
				sbError.append(tipoMetPag.get("message").toString() + factura + "\n");
			} else {
				comp.setMetodoPago(tipoMetPag.get("value").toString());
			}
		}

		Map<String, Object> tipoFormaPago = UtilValidationsXML.validFormaPago(tags.mapCatalogos,
				row.getCell(2).toString());
		/* Forma de pago */
		if (row.getCell(2) == null || row.getCell(2).toString().trim().equals("")) {
			sbError.append("(CFDI33103) El Campo Forma Pago No Contiene Un Valor Del Catalogo C_FormaPago - Factura "
					+ factura + "\n");
		} else {
			if (!tipoFormaPago.get("value").toString().equalsIgnoreCase("vacio")) {
				comp.setFormaPago(tipoFormaPago.get("value").toString());
			} else {
				sbError.append(tipoFormaPago.get("message").toString() + "Factura " + factura + "\n");
			}
		}

		comp.getAddenda().setInformacionPago(new CfdiAddendaInformacionPago());
		/* Numero de cuenta */
		if (row.getCell(15) == null) {
			sbError.append("(10) Posicion Numero de Cuenta de Pago requerida (Null) - Factura " + factura + "\n");
		} else {
			if (row.getCell(15).toString().trim().equals("")) {
				sbError.append("(10) Numero de Cuenta de Pago requerida - Factura " + factura + "\n");
			} else {
				String strCellType = checkCellType(row.getCell(15));
				if (!strCellType.equals("")) {
					sbError.append("(10) " + strCellType + " - Factura " + factura + "\n");
				} else {
					String strNumCtaPago = row.getCell(15).toString();
					comp.getAddenda().getInformacionPago().setNumeroCuenta(strNumCtaPago);
				}
			}

		}
		// TODO: esta propiedad no se encuentra en el nuevo objecto
		// if(row.getCell(16) == null){
		//
		// comp.setReferencia("");
		// }else{
		//
		// if(row.getCell(16).toString().trim().equals("")){
		// invoice.setReferencia(row.getCell(16).toString().trim());
		// }else{
		// if(validaDatoRE(row.getCell(16).toString(),
		// RE_CHAR_ALL)){
		// invoice.setReferencia(row.getCell(16).toString().trim());
		// System.out.println("Referencia: " +
		// row.getCell(16).toString());
		//
		// }else{
		//
		// sbError.append("(11) Referencia con formato incorrecto -
		// Factura " + factura + "\n");
		// //System.out.println("(11) Referencia con formato
		// incorrecto - Factura " + factura + "\n");
		// fError = true;
		//
		// }
		// }
		//
		// }
		/* Codigo cliente */
		if (row.getCell(17) == null) {
			comp.getAddenda().getInformacionEmision().setCodigoCliente("");
		} else {
			if (row.getCell(17).toString().trim().equals("")) {
				comp.getAddenda().getInformacionEmision().setCodigoCliente("");
			} else {
				String strCellType = checkCellType(row.getCell(17));
				if (!strCellType.equals("")) {
					sbError.append("(12) " + strCellType + " - Factura " + factura + "\n");
				} else {
					if (row.getCell(17).toString().trim().length() > 0) {
						String strCodigoCliente = row.getCell(17).toString();
						comp.getAddenda().getInformacionEmision().setCodigoCliente(strCodigoCliente);
					} else {
						comp.getAddenda().getInformacionEmision().setCodigoCliente(row.getCell(17).toString().trim());
					}
				}
			}
		}
		/* Contrato */
		if (row.getCell(18) == null) {
			comp.getAddenda().getInformacionEmision().setContrato("");
		} else {
			if (row.getCell(18).toString().trim().equals("")) {
				comp.getAddenda().getInformacionEmision().setContrato("");
			} else {
				String strCellType = checkCellType(row.getCell(18));
				if (!strCellType.equals("")) {
					sbError.append("(13) " + strCellType + " - Factura " + factura + "\n");
				} else {
					if (row.getCell(18).toString().trim().length() > 0) {
						String strContrato = row.getCell(18).toString();
						comp.getAddenda().getInformacionEmision().setContrato(strContrato);
					} else {
						comp.getAddenda().getInformacionEmision().setContrato(row.getCell(18).toString().trim());
					}
				}
			}

		}
		/* Periodo */
		if (row.getCell(19) == null) {
			comp.getAddenda().getInformacionEmision().setPeriodo("");
		} else {
			if (row.getCell(19).toString().trim().equals("")) {
				comp.getAddenda().getInformacionEmision().setPeriodo("");
			} else {
				String strCellType = checkCellType(row.getCell(19));
				if (!strCellType.equals("")) {
					sbError.append("(14) " + strCellType + " - Factura " + factura + "\n");
				} else {
					if (row.getCell(19).toString().trim().length() > 0) {
						String strPeriodo = row.getCell(19).toString();
						comp.getAddenda().getInformacionEmision().setPeriodo(strPeriodo);
					} else {
						comp.getAddenda().getInformacionEmision().setPeriodo(row.getCell(19).toString().trim());
					}
				}
			}

		}
		/* Centro de costos */
		if (row.getCell(20) == null) {
			comp.getAddenda().getInformacionEmision().setCentroCostos("");
		} else {
			if (row.getCell(20).toString().trim().equals("")) {
				comp.getAddenda().getInformacionEmision().setCentroCostos("");
			} else {
				String strCellType = checkCellType(row.getCell(20));
				if (!strCellType.equals("")) {
					sbError.append("(15) " + strCellType + " - Factura " + factura + "\n");
				} else {
					if (row.getCell(21).toString().trim().length() > 0) {
						String strCentroCostos = row.getCell(20).toString();
						comp.getAddenda().getInformacionEmision().setCentroCostos(strCentroCostos);
					} else {
						comp.getAddenda().getInformacionEmision().setCentroCostos(row.getCell(20).toString().trim());
					}
				}
			}

		}
		comp.getAddenda().setCampoAdicional(new HashMap<String, String>());
		/* Descriptcion concepto */
		if (row.getCell(21) == null) {
			comp.getAddenda().getCampoAdicional().put("DESCRIPCIÓN CONCEPTO", "");
		} else {
			comp.getAddenda().getCampoAdicional().put("DESCRIPCIÓN CONCEPTO", row.getCell(21).toString().trim());
		}
		/* Iva */
		boolean fErrorIVA = false;
		StringBuilder sbErrorIVA = new StringBuilder();
		if (row.getCell(22) == null) {
			sbErrorIVA.append("(17) Posicion IVA requerida (Null) - Factura " + factura + "\n");
			fErrorIVA = true;
		} else {
			System.out.println("IVA: " + row.getCell(22).toString());
			if (!row.getCell(22).toString().trim().equals("")) {
				if (validaDatoRE(row.getCell(22).toString().trim(), RE_DECIMAL)) {
					Iva iva = ivaManager.findByTasa(getTASA(String.valueOf(row.getCell(22).toString().trim())));

					if (iva == null) {
						sbErrorIVA.append("Descripcion de IVA no existe en BD - Factura " + factura + "\n");
						System.out.println("Descripcion de IVA no existe en BD - Factura " + factura + "\n");
						fErrorIVA = true;
					} else {
						comp.getAddenda().getCampoAdicional().put("DESCRIPCIÓN IVA", iva.getDescripcion().trim());
					}
				} else {
					sbErrorIVA.append("(17) IVA incorrecto - Factura " + factura + "\n");
					fErrorIVA = true;
				}
			} else {
				sbErrorIVA.append("(17) IVA requerido - Factura " + factura + "\n");
				fErrorIVA = true;
			}

		}
		/* Tipo adenda */
		comp.getAddenda().setInmuebles(new CfdiAddendaInmuebles());
		if (row.getCell(23) != null) {
			if (row.getCell(23).toString().contains(".")) {
				System.out.println("*** response Dentro IF AMDA: " + row.getCell(23).toString());
				String words[] = row.getCell(23).toString().split("\\.");
				row.getCell(23).setCellValue(words[0]);
				System.out.println("*** response Dentro IF despues AMDA: " + row.getCell(23).toString());
			}
			System.out.println("tipoAddenda:" + row.getCell(23).toString());

			if (validaDatoRE(row.getCell(23).toString().trim(), RE_DECIMAL)) {
				String strTipoAddenda = row.getCell(23).toString();
				System.out.println("tipoAddendaClean: " + strTipoAddenda);
				if (strTipoAddenda.equals("1") || strTipoAddenda.equals("2") || strTipoAddenda.equals("3")) {
					comp.getAddenda().getInformacionPago().setNombreBeneficiario("");
					comp.getAddenda().getInformacionPago().setInstitucionReceptora("");
					comp.getAddenda().getInformacionPago().setNumeroCuenta("");
					comp.getAddenda().getInformacionPago().setNumProveedor("");
					if (row.getCell(24) == null) {
						sbError.append("(19) Posicion Email Proveedor requerida (Null)" + "\n");
					} else {

						if (!row.getCell(24).toString().trim().equals("")) {
							if (validaDatoRE(row.getCell(24).toString().trim(), RE_MAIL)) {
								comp.getAddenda().getInformacionPago().setEmail(row.getCell(24).toString().trim());
							} else {
								sbError.append("(19) Email Proveedor con estructura incorrecta" + "\n");
							}
						} else {
							sbError.append("(19) Email Proveedor requerido" + "\n");
						}

					}
				}
				if (strTipoAddenda.equals("1")) {
					// invoice.setTipoAddenda(strTipoAddenda.trim());
					if (row.getCell(25) == null) {
						sbError.append("(20) Posicion Codigo ISO Moneda requerida (Null)" + "\n");
					} else {

						if (!row.getCell(25).toString().trim().equals("")) {
							CodigoISO codigoISOLog = new CodigoISO();
							codigoISOLog = codigoISOManager
									.findByCodigo(row.getCell(25).toString().trim().toUpperCase());
							if (codigoISOLog != null) {
								comp.getAddenda().getInformacionPago()
										.setCodigoISOMoneda(row.getCell(25).toString().trim().toUpperCase());
							} else {
								sbError.append("(20) Codigo ISO Moneda no existe en BD" + "\n");
							}
						} else {
							sbError.append("(20) Codigo ISO Moneda requerido" + "\n");
						}

					}

					if (row.getCell(26) == null) {
						sbError.append("(21) Posicion Orden Compra requerida (Null)" + "\n");
					} else {
						if (row.getCell(26).toString().trim().equals("")) {
							sbError.append("(21) Orden Compra requerida" + "\n");
						} else {
							String strCellTypeOrdenLog = checkCellType(row.getCell(26));
							if (!strCellTypeOrdenLog.equals("")) {
								sbError.append("(21) " + strCellTypeOrdenLog + " - Factura " + factura + "\n");
							} else {
								String strOrdenCompra = row.getCell(26).toString();
								if (!strOrdenCompra.equals("")) {
									comp.getAddenda().getInformacionPago().setOrdenCompra(strOrdenCompra);
									System.out.println("orden compra log: " + strOrdenCompra);
								} else {
									sbError.append("(21) Orden Compra requerida" + "\n");
								}
							}
						}

					}

					if (row.getCell(27) == null) {
						sbError.append("(22) Posicion Compra requerida (Null)" + "\n");
					} else {
						if (row.getCell(27).toString().trim().equals("")) {
							sbError.append("(22) Posicion Compra requerida" + "\n");
						} else {
							String strCellTypePosicionLog = checkCellType(row.getCell(27));
							if (!strCellTypePosicionLog.equals("")) {
								sbError.append("(22) " + strCellTypePosicionLog + " - Factura " + factura + "\n");
							} else {
								String strPosicionCompra = row.getCell(27).toString();
								if (!strPosicionCompra.equals("")) {
									comp.getAddenda().getInformacionPago().setPosCompra(strPosicionCompra);
									System.out.println("posicion compra: " + strPosicionCompra);
								} else {
									sbError.append("(22) Posicion Compra requerida" + "\n");
								}
							}
						}

					}

				} else if (strTipoAddenda.equals("2")) {
					// invoice.setTipoAddenda(strTipoAddenda.trim());
					if (row.getCell(25) == null) {
						sbError.append("(20) Posicion Codigo ISO Moneda requerida (Null)" + "\n");
					} else {

						if (!row.getCell(25).toString().trim().equals("")) {
							CodigoISO codigoISOFin = new CodigoISO();
							codigoISOFin = codigoISOManager
									.findByCodigo(row.getCell(25).toString().trim().toUpperCase());
							if (codigoISOFin != null) {
								comp.getAddenda().getInformacionPago()
										.setCodigoISOMoneda(row.getCell(25).toString().trim().toUpperCase());
							} else {
								sbError.append("(20) Codigo ISO Moneda no existe en BD" + "\n");
							}
						} else {
							sbError.append("(20) Codigo ISO Moneda no encontrado" + "\n");
						}

					}

					if (row.getCell(28) == null) {
						sbError.append("(23) Posicion Cuenta Contable requerida (Null)" + "\n");
					} else {
						if (row.getCell(28).toString().trim().equals("")) {
							sbError.append("(23) Cuenta Contable requerida" + "\n");
						} else {
							String strCellTypeContableFin = checkCellType(row.getCell(28));
							if (!strCellTypeContableFin.equals("")) {
								sbError.append("(23) " + strCellTypeContableFin + " - Factura " + factura + "\n");
							} else {
								String strCuentaContableFin = row.getCell(28).toString();
								if (!strCuentaContableFin.equals("")) {
									comp.getAddenda().getInformacionPago().setCuentaContable(strCuentaContableFin);
									System.out.println("cuenta contable Fin: " + strCuentaContableFin);
								} else {
									sbError.append("(23) Cuenta Contable requerida" + "\n");
								}
							}
						}

					}

					if (row.getCell(29) == null) {
						sbError.append("(24) Posicion Centro costos requerido (Null)" + "\n");

					} else {
						if (row.getCell(29).toString().trim().equals("")) {
							sbError.append("(24) Centro costos requerido" + "\n");
						} else {
							String strCellTypeCostosFin = checkCellType(row.getCell(29));
							if (!strCellTypeCostosFin.equals("")) {
								sbError.append("(24) " + strCellTypeCostosFin + " - Factura " + factura + "\n");
							} else {
								if (!row.getCell(29).toString().trim().equals("")) {
									System.out.println("centro costos: " + row.getCell(29).toString());
									String strCentroCostosFin = row.getCell(29).toString();
									if (!strCentroCostosFin
											.equals(comp.getAddenda().getInformacionEmision().getCentroCostos())) {
										sbError.append("Centros costos diferentes" + "\n");
									}
								} else {
									sbError.append("Centro costos requerido" + "\n");
								}
							}
						}

					}

				} else if (strTipoAddenda.equals("3")) {
					// invoice.setTipoAddenda("3");
					if (row.getCell(25) == null) {
						sbError.append("(20) Posicion Codigo ISO Moneda requerida (Null)" + "\n");
					} else {

						if (!row.getCell(25).toString().trim().equals("")) {
							CodigoISO codigoISOArr = new CodigoISO();
							codigoISOArr = codigoISOManager
									.findByCodigo(row.getCell(25).toString().trim().toUpperCase());
							if (codigoISOArr != null) {
								comp.getAddenda().getInformacionPago()
										.setCodigoISOMoneda(row.getCell(25).toString().trim().toUpperCase());
							} else {
								sbError.append("(20) Codigo ISO Moneda no existe en BD" + "\n");
							}
						} else {
							sbError.append("(20) Codigo ISO Moneda no encontrado" + "\n");
						}

					}

					if (row.getCell(30) == null) {
						sbError.append("(25) Posicion Numero de Contrato requerida (Null)" + "\n");
					} else {
						if (row.getCell(30).toString().trim().equals("")) {
							sbError.append("(25) Numero de Contrato requerido" + "\n");
						} else {
							String strCellTypeContratoArr = checkCellType(row.getCell(30));
							if (!strCellTypeContratoArr.equals("")) {
								sbError.append("(25) " + strCellTypeContratoArr + " - Factura " + factura + "\n");
							} else {
								String strNumeroContratoArr = row.getCell(30).toString();
								if (!strNumeroContratoArr.equals("")) {
									comp.getAddenda().getInmuebles().setNumContrato(strNumeroContratoArr);
								} else {
									sbError.append("(25) Numero de Contrato no encontrado" + "\n");
								}
							}
						}

					}
					if (row.getCell(31) == null) {
						sbError.append("(26) Posicion Fecha de vencimiento requerida (Null)" + "\n");

					} else {
						if (row.getCell(31).toString().trim().equals("")) {
							sbError.append("(26) Fecha de vencimiento requerida" + "\n");
						} else {
							String strCellTypeVencimientoArr = checkCellType(row.getCell(31));
							if (!strCellTypeVencimientoArr.equals("")) {
								sbError.append("(26) " + strCellTypeVencimientoArr + " - Factura " + factura + "\n");
							} else {
								String strFechaVencimientoArr = row.getCell(31).toString();
								if (!strFechaVencimientoArr.equals("")) {
									comp.getAddenda().getInmuebles().setFechaVencimiento(strFechaVencimientoArr);
									System.out.println("fecha vencimiento Arr:" + strFechaVencimientoArr);
								} else {
									sbError.append("(26) Fecha de vencimiento requerida" + "\n");
								}
							}
						}

					}

				} else if (strTipoAddenda.equals("0")) {
					comp.getAddenda().getInformacionPago().setEmail("");
					comp.getAddenda().getInformacionPago().setOrdenCompra("");
					// invoice.setTipoAddenda(strTipoAddenda.trim());
					if (row.getCell(32) == null) {
						comp.getAddenda().getInformacionPago().setNombreBeneficiario("");
					} else {
						comp.getAddenda().getInformacionPago().setNombreBeneficiario(row.getCell(32).toString().trim());
					}
					if (row.getCell(33) == null) {
						comp.getAddenda().getInformacionPago().setInstitucionReceptora("");
					} else {
						comp.getAddenda().getInformacionPago()
								.setInstitucionReceptora(row.getCell(33).toString().trim());
					}

					if (row.getCell(34) == null) {
						comp.getAddenda().getInformacionPago().setNumeroCuenta("");
					} else {
						if (row.getCell(34).toString().trim().equals("")) {
							comp.getAddenda().getInformacionPago().setNumeroCuenta(row.getCell(34).toString().trim());
						} else {
							String strCellTypeC = checkCellType(row.getCell(34));
							if (!strCellTypeC.equals("")) {
								sbError.append("(29) " + strCellTypeC + " - Factura " + factura + "\n");
							} else {
								comp.getAddenda().getInformacionPago()
										.setNumeroCuenta(row.getCell(34).toString().trim());
							}
						}
					}
					if (row.getCell(35) == null) {
						comp.getAddenda().getInformacionPago().setNumProveedor("");
					} else {
						if (row.getCell(35).toString().trim().equals("")) {
							comp.getAddenda().getInformacionPago().setNumProveedor(row.getCell(35).toString().trim());
						} else {
							String strCellTypeP = checkCellType(row.getCell(35));
							if (!strCellTypeP.equals("")) {
								sbError.append("(30) " + strCellTypeP + " - Factura " + factura + "\n");
							} else {
								comp.getAddenda().getInformacionPago()
										.setNumProveedor(row.getCell(35).toString().trim());
							}
						}
					}

				} else {
					sbError.append("(18) Tipo de Addenda incorrecto - factura " + factura + "\n");
				}
			} else {
				if (row.getCell(17).toString().trim().equals("")) {
					comp.getAddenda().getInformacionPago().setEmail("");
					comp.getAddenda().getInformacionPago().setOrdenCompra("");
					// invoice.setTipoAddenda("0");
					if (row.getCell(32) == null) {
						comp.getAddenda().getInformacionPago().setNombreBeneficiario("");
					} else {
						comp.getAddenda().getInformacionPago().setNombreBeneficiario(row.getCell(32).toString().trim());
					}
					if (row.getCell(33) == null) {
						comp.getAddenda().getInformacionPago().setInstitucionReceptora("");
					} else {
						comp.getAddenda().getInformacionPago()
								.setInstitucionReceptora(row.getCell(33).toString().trim());
					}

					if (row.getCell(34) == null) {
						comp.getAddenda().getInformacionPago().setNumeroCuenta("");
					} else {
						if (row.getCell(34).toString().trim().equals("")) {
							comp.getAddenda().getInformacionPago().setNumeroCuenta(row.getCell(34).toString().trim());
						} else {
							String strCellTypeP = checkCellType(row.getCell(34));
							if (!strCellTypeP.equals("")) {
								sbError.append("(29) " + strCellTypeP + " - Factura " + factura + "\n");
							} else {
								comp.getAddenda().getInformacionPago()
										.setNumeroCuenta(row.getCell(34).toString().trim());
							}
						}

					}
					if (row.getCell(35) == null) {
						comp.getAddenda().getInformacionPago().setNumProveedor("");
					} else {
						if (row.getCell(35).toString().trim().equals("")) {
							comp.getAddenda().getInformacionPago().setNumProveedor(row.getCell(35).toString().trim());
						} else {
							String strCellTypeP = checkCellType(row.getCell(35));
							if (!strCellTypeP.equals("")) {
								sbError.append("(30) " + strCellTypeP + " - Factura " + factura + "\n");
							} else {
								comp.getAddenda().getInformacionPago()
										.setNumProveedor(row.getCell(35).toString().trim());
							}
						}

					}

				} else {
					sbError.append("(18) Tipo de Addenda incorrecto - factura " + factura + "\n");
				}
			}

		} else {
			comp.getAddenda().getInformacionPago().setEmail("");
			comp.getAddenda().getInformacionPago().setOrdenCompra("");
			// invoice.setTipoAddenda("0");
			if (row.getCell(32) == null) {
				comp.getAddenda().getInformacionPago().setNombreBeneficiario("");
			} else {
				comp.getAddenda().getInformacionPago().setNombreBeneficiario(row.getCell(32).toString().trim());
			}
			if (row.getCell(33) == null) {
				comp.getAddenda().getInformacionPago().setInstitucionReceptora("");
			} else {
				comp.getAddenda().getInformacionPago().setInstitucionReceptora(row.getCell(33).toString().trim());
			}
			if (row.getCell(34) == null) {
				comp.getAddenda().getInformacionPago().setNumeroCuenta("");
			} else {
				if (row.getCell(34).toString().trim().equals("")) {
					comp.getAddenda().getInformacionPago().setNumeroCuenta(row.getCell(34).toString().trim());
				} else {
					String strCellTypeP = checkCellType(row.getCell(34));
					if (!strCellTypeP.equals("")) {
						sbError.append("(29) " + strCellTypeP + " - Factura " + factura + "\n");
					} else {
						comp.getAddenda().getInformacionPago().setNumeroCuenta(row.getCell(34).toString().trim());
					}
				}

			}
			if (row.getCell(35) == null) {
				comp.getAddenda().getInformacionPago().setNumProveedor("");
			} else {
				if (row.getCell(25).toString().trim().equals("")) {
					comp.getAddenda().getInformacionPago().setNumProveedor(row.getCell(35).toString().trim());
				} else {
					String strCellTypeP = checkCellType(row.getCell(35));
					if (!strCellTypeP.equals("")) {
						sbError.append("(30) " + strCellTypeP + " - Factura " + factura + "\n");
					} else {
						comp.getAddenda().getInformacionPago().setNumProveedor(row.getCell(35).toString().trim());
					}
				}

			}
		}

		/* Tipo de operacion */
		if (!row.getCell(36).toString().trim().equals("")) {
			if (row.getCell(36).toString().trim().toLowerCase().equals("compra")
					|| row.getCell(36).toString().trim().toLowerCase().equals("venta")) {
				comp.getComplemento().setDivisaTipoOperacion(row.getCell(36).toString().toLowerCase().trim());
			} else {
				sbError.append("(31) Tipo de Operacion incorrecto - Factura " + factura + "\n");
			}
		} else {
			sbError.append("(31) Tipo de Operacion requerido - Factura " + factura + "\n");
		}

		/* Motivo descuento */
		String motivoDesc;
		if (row.getCell(3) == null) { // Antes 31 ahora 3 AMDA V3.3
			motivoDesc = "";
			System.out.println("row.getCell(31) == null");
		} else {
			motivoDesc = row.getCell(3).toString().trim();
			System.out.println("row.getCell(31) != null");
		}
		if (motivoDesc.equals("")) {
			// invoice.setMotivoDescuento("");
			if (row.getCell(4) == null) {
				comp.setDescuento(BigDecimal.ZERO);
				System.out.println("Descuento cero");
			} else {
				if (!row.getCell(4).toString().trim().equals("")) {
					String strCellTypeDesc = checkCellType(row.getCell(4));
					if (strCellTypeDesc.equals("")) {
						if (validaDatoRE(row.getCell(4).toString().trim(), RE_DECIMAL_NEGATIVO)) {
							if (Double.parseDouble(row.getCell(4).toString().trim()) > 0) {
								sbError.append("(32) "
										+ "Favor de informar el Motivo de descuento correspondiente al Descuento "
										+ " - factura " + factura + "\n");
							} else {
								comp.setDescuento(BigDecimal.ZERO);
							}

						} else {
							sbError.append(
									"(33) " + "Descuento con formato incorrecto " + " - factura " + factura + "\n");
						}
					} else {
						sbError.append("(33) " + strCellTypeDesc + " - Factura " + factura + "\n");
					}
				} else {
					comp.setDescuento(BigDecimal.ZERO);
				}
			}

		} else {
			if (motivoDesc.length() > 0 && motivoDesc.length() <= 1500) {
				// invoice.setMotivoDescuento(motivoDesc);
				if (row.getCell(4) == null) {
					sbError.append("(33) " + "Favor de informar el Descuento correspondiente al Motido de descuento "
							+ " - factura " + factura + "\n");
				} else {
					if (row.getCell(4).toString().trim().equals("")) {
						sbError.append(
								"(33) " + "Favor de informar el Descuento correspondiente al Motido de descuento "
										+ " - factura " + factura + "\n");
					} else {
						String strCellTypeDesc = checkCellType(row.getCell(4));
						if (!strCellTypeDesc.equals("")) {
							sbError.append("(33) " + strCellTypeDesc + " - Factura " + factura + "\n");
						} else {
							if (validaDatoRE(row.getCell(4).toString().trim(), RE_DECIMAL_NEGATIVO)) {
								if (Double.parseDouble(row.getCell(4).toString().trim()) > 0) {
									comp.setDescuento(
											new BigDecimal(Double.parseDouble(row.getCell(4).toString().trim())));
								} else {
									sbError.append("(33) "
											+ "Favor de informar el Descuento correspondiente al Motido de descuento "
											+ " - factura " + factura + "\n");
								}
							} else {
								sbError.append(
										"(33) " + "Descuento con formato incorrecto " + " - factura " + factura + "\n");
							}
						}
					}
				}

			} else {
				sbError.append(
						"(32) " + "Motivo de descuento con formato incorrecto " + " - factura " + factura + "\n");
			}
		}
		/* Valida rfc cliente extranjero */
		if (comp.getReceptor().getRfc() != null) {
			if (comp.getReceptor().getRfc().toUpperCase().equals("XEXX010101000")
					|| comp.getReceptor().getRfc().toUpperCase().equals("XAXX010101000")
					|| comp.getReceptor().getRfc().toUpperCase().equals("XEXE010101000")) {
				if (comp.getReceptor().getDomicilio().getPais() != null) {
					Map<String, Object> tipoClaveProdServ = UtilValidationsXML.validPais(tags.mapCatalogos,
							comp.getReceptor().getDomicilio().getPais());
					System.out.println("*********AMDAController Divisas Validando Pais RFC Respuesta: "
							+ tipoClaveProdServ.get("value").toString());
					if (!tipoClaveProdServ.get("value").toString().equalsIgnoreCase("vacio")) {
						comp.getReceptor().setResidenciaFiscal(tipoClaveProdServ.get("value").toString());
						if (comp.getReceptor().getResidenciaFiscal().trim().equalsIgnoreCase("MEX")
								|| comp.getReceptor().getResidenciaFiscal().trim().equalsIgnoreCase("MEXICO")
								|| comp.getReceptor().getResidenciaFiscal().trim().equalsIgnoreCase("México")
								|| comp.getReceptor().getResidenciaFiscal().trim().equalsIgnoreCase("MÉXICO")) {
							sbError.append("(0) " + "El Pais no puede ser Mexico para un RFC Generico Extranjero "
									+ " - factura " + factura + "\n");
						}
					} else {
						sbError.append(
								"(0) " + tipoClaveProdServ.get("message").toString() + " - factura " + factura + "\n");
					}
					Map<String, Object> tipoRFC = UtilValidationsXML.validRFCNumRegIdTrib(tags.mapCatalogos,
							comp.getReceptor().getRfc(), comp.getReceptor().getResidenciaFiscal(),
							row.getCell(12).toString(), comp.getReceptor().getNombre(),
							comp.getReceptor().getNumRegIdTrib());
					if (!tipoRFC.get("value").toString().equalsIgnoreCase("vacio")) {
						// invoice.setIdExtranjero(tipoRFC.get("value").toString());
						comp.getReceptor().setNumRegIdTrib(tipoRFC.get("value").toString());
					} else {
						sbError.append("(0) " + tipoRFC.get("message").toString() + " - factura " + factura + "\n");
					}
				}
			}
		}
		// TODO: Este campo no esta en el nuevo objeto
		if (row.getCell(34) != null || row.getCell(34).toString().length() > 0) {
			// invoice.setAccountNumber(row.getCell(34).toString());
		}

		if (row.getCell(28) != null || row.getCell(28).toString().length() > 0) {
			comp.getAddenda().getInformacionPago().setCuentaContable(row.getCell(28).toString());
		}

		if (row.getCell(29) != null || row.getCell(29).toString().length() > 0) {
			comp.getAddenda().getInformacionEmision().setCentroCostos(row.getCell(29).toString());
		}
		if (row.getCell(30) != null || row.getCell(30).toString().length() > 0) {
			comp.getAddenda().getInmuebles().setNumContrato(row.getCell(30).toString());
		}
		if (row.getCell(31) != null || row.getCell(31).toString().length() > 0) {
			comp.getAddenda().getInmuebles().setFechaVencimiento(row.getCell(31).toString());
		}

		if (row.getCell(32) != null || row.getCell(32).toString().length() > 0) {
			comp.getAddenda().getInformacionPago().setNombreBeneficiario(row.getCell(32).toString());
		}
		if (row.getCell(33) != null || row.getCell(33).toString().length() > 0) {
			comp.getAddenda().getInformacionPago().setInstitucionReceptora(row.getCell(33).toString());
		}
		if (row.getCell(35) != null || row.getCell(35).toString().length() > 0) {
			comp.getAddenda().getInformacionPago().setNumProveedor(row.getCell(35).toString());
		}
		int posicionConcepto = 0;
		int posicion = 37;
		int contadorConceptos = 0;
		boolean fPermisoVector = true;
		boolean fFinFactura = false;
		String strItemConcepto = "";
		Integer numeroCelda = 0;
		Integer cicloNum = 0;
		Integer cicloNumRet = 0;
		String tipoFactorValRow = "";
		String impuestoValRow = "";
		String tipoFactorValRowRet = "";
		String impuestoValRowRet = "";
		while (posicion < lastCellNum && !fFinFactura) {
			numeroCelda += 1;
			contadorConceptos = contadorConceptos + 1;
			if (row.getCell(posicion).toString().equals("||FINFACTURA||")) {
				fFinFactura = true;
				break;
			}
			if (numeroCelda == 1) {
				numeroCelda = numeroCelda + 7;

				if (numeroCelda == 8) {
					posicion = posicion + 7;
					if (row.getCell(posicion).toString().equalsIgnoreCase("Traslado")) {
						trasladoBol = true;
						retencionBol = false;
					} else if (row.getCell(posicion).toString().equalsIgnoreCase("Retencion")) {
						retencionBol = true;
						trasladoBol = false;
					} else {
						fPermisoVector = false;
						sbError.append("(" + (1) + ") " + "No se encontro un Tipo de Impuesto (Traslado o Retencion)"
								+ " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
					}
					posicion = posicion - 7;
					numeroCelda = numeroCelda - 7;
				}

			} else if (numeroCelda == 8) {
				if (row.getCell(posicion).toString().equalsIgnoreCase("Traslado")) {
					trasladoBol = true;
					retencionBol = false;
				} else if (row.getCell(posicion).toString().equalsIgnoreCase("Retencion")) {
					retencionBol = true;
					trasladoBol = false;
				} else {
				}
			}

			if (trasladoBol) {

				if (numeroCelda == 1) {
					if (row.getCell(posicion) != null || row.getCell(posicion).toString().length() > 0) {

						Map<String, Object> tipoClaveProdServ = UtilValidationsXML.validClaveProdServ(tags.mapCatalogos,
								row.getCell(posicion).toString());
						if (tipoClaveProdServ.get("value").toString().equalsIgnoreCase("vacio")) {
							fPermisoVector = false;
							sbError.append("(" + (1) + ") " + tipoClaveProdServ.get("message").toString()
									+ " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
						} else {
							vectorClaveProdServ.add(tipoClaveProdServ.get("value").toString());
						}
					} else {
						fPermisoVector = false;
						sbError.append("(" + (1) + ") " + "ClaveProdServ con formato incorrecto " + " en el Concepto "
								+ contadorConceptos + " - factura " + factura + "\n");
					}
				}

				if (numeroCelda == 2) {
					if (row.getCell(posicion) != null || row.getCell(posicion).toString().length() > 0) {
						vectorCantidad.add(row.getCell(posicion).toString());
					} else {
						fPermisoVector = false;
						sbError.append("(" + (1) + ") " + "Cantidad con formato incorrecto " + " en el Concepto "
								+ contadorConceptos + " - factura " + factura + "\n");
					}
				}

				if (numeroCelda == 3) {
					if (row.getCell(posicion) != null || row.getCell(posicion).toString().length() > 0) {

						Map<String, Object> tipoClaveUnidad = UtilValidationsXML.validClaveUnidad(tags.mapCatalogos,
								row.getCell(posicion).toString());
						if (tipoClaveUnidad.get("value").toString().equalsIgnoreCase("vacio")) {
							fPermisoVector = false;
							sbError.append("(" + (1) + ") " + tipoClaveUnidad.get("message").toString()
									+ " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
						} else {
							vectorClaveUnidad.add(tipoClaveUnidad.get("value").toString());
						}
					} else {
						fPermisoVector = false;
						sbError.append("(" + (1) + ") " + "ClaveUnidad con formato incorrecto " + " en el Concepto "
								+ contadorConceptos + " - factura " + factura + "\n");
					}
				}

				if (numeroCelda == 4) {
					if (row.getCell(posicion) != null || row.getCell(posicion).toString().length() > 0) {
						vectorUM.add(row.getCell(posicion).toString());
					} else {
						fPermisoVector = false;
						sbError.append("(" + (1) + ") " + "UM con formato incorrecto " + " en el Concepto "
								+ contadorConceptos + " - factura " + factura + "\n");
					}
				}

				if (numeroCelda == 5) {
					if (row.getCell(posicion) != null || row.getCell(posicion).toString().length() > 0) {
						vectorDesc.add(row.getCell(posicion).toString());
					} else {
						fPermisoVector = false;
						sbError.append("(" + (1) + ") " + "Concepto Expedicion con formato incorrecto "
								+ " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
					}
				}

				if (numeroCelda == 6) {
					if (row.getCell(posicion) != null || row.getCell(posicion).toString().length() > 0) {
						Map<String, Object> tipoPrecioUnit = UtilValidationsXML.validValorUnitario(tags.mapCatalogos,
								row.getCell(posicion).toString(), tags.decimalesMoneda, comp.getTipoDeComprobante());
						if (!tipoPrecioUnit.get("value").toString().equalsIgnoreCase("vacio")) {
							vectorPrecioUnitario.add(Double.parseDouble(row.getCell(posicion).toString()));
						} else {
							fPermisoVector = false;
							sbError.append("(" + (1) + ") " + tipoPrecioUnit.get("message").toString()
									+ " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
						}

					} else {
						fPermisoVector = false;
						sbError.append("(" + (1) + ") " + "Precio Unitario con formato incorrecto " + " en el Concepto "
								+ contadorConceptos + " - factura " + factura + "\n");
					}
				}

				if (numeroCelda == 7) {
					if (row.getCell(posicion).toString().equals("1")) {
						if (fPermisoVector)
							vectorAplicaIVA.add(row.getCell(posicion).toString());
					} else {
						fPermisoVector = false;
						sbError.append("(" + (1) + ") " + "APLICA IVA con formato incorrecto " + " en el Concepto "
								+ contadorConceptos + " - factura " + factura + "\n");
					}
				}
				if (numeroCelda == 9) {
					if (row.getCell(posicion) != null || row.getCell(posicion).toString().length() > 0) {
						impuestoValRow = row.getCell(posicion).toString();
						String tipoImp = UtilCatalogos.findValClaveImpuesto(tags.mapCatalogos,
								row.getCell(posicion).toString());
						if (!tipoImp.equalsIgnoreCase("vacio")) {
							vectorImpuesto.add(tipoImp);
							impuestoValRow = row.getCell(posicion).toString();
						} else {
							fPermisoVector = false;
							sbError.append("(" + (1) + ") " + "No se encotro el Impuesto en el catalogo C_Impuestos "
									+ " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
						}

					} else {
						fPermisoVector = false;
						sbError.append("(" + (1) + ") " + "Impuesto con formato incorrecto " + " en el Concepto "
								+ contadorConceptos + " - factura " + factura + "\n");
					}
				}
				if (numeroCelda == 10) {
					if (row.getCell(posicion) != null || row.getCell(posicion).toString().length() > 0) {
						if (!row.getCell(posicion).toString().equalsIgnoreCase("Exento")
								&& !row.getCell(posicion).toString().equalsIgnoreCase("Excento")) {
							Map<String, Object> tipoTipoFact = UtilValidationsXML.validTipoFactorTra(tags.mapCatalogos,
									row.getCell(posicion).toString());
							if (!tipoTipoFact.get("value").toString().equalsIgnoreCase("vacio")) {
								tipoFactorValRow = row.getCell(posicion).toString();
								vectorTipFactor.add(row.getCell(posicion).toString());
							} else {
								fPermisoVector = false;
								sbError.append("(" + (1) + ") " + tipoTipoFact.get("message").toString()
										+ " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
							}
						} else {
							tipoFactorValRow = row.getCell(posicion).toString();
							vectorTipFactor.add(row.getCell(posicion).toString());
						}

					} else {
						fPermisoVector = false;
						sbError.append("(" + (1) + ") "
								+ "El valor del campo TipoFactor que corresponde a Traslado no contiene un valor del catálogo c_TipoFactor "
								+ " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
					}
				}

				if (numeroCelda == 11) {
					if (row.getCell(posicion) != null || row.getCell(posicion).toString().length() > 0) {
						if (!tipoFactorValRow.equalsIgnoreCase("Exento")
								&& !tipoFactorValRow.equalsIgnoreCase("Excento")) {
							Map<String, Object> tipoTasaOCuota = UtilValidationsXML.validTasaOCuotaTra(
									tags.mapCatalogos, impuestoValRow, tipoFactorValRow,
									row.getCell(posicion).toString(), tipoFactorValRow);
							if (!tipoTasaOCuota.get("value").toString().equalsIgnoreCase("vacio")) {
								vectorTasaOCuota.add(Double.parseDouble(row.getCell(posicion).toString()));
								cicloNum = cicloNum + 1;
							} else {
								fPermisoVector = false;
								sbError.append("(" + (1) + ") " + tipoTasaOCuota.get("message").toString()
										+ " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
							}
						} else {
							vectorTasaOCuota.add(Double.parseDouble("0.00"));
							cicloNum = cicloNum + 1;
						}

					} else {
						fPermisoVector = false;
						sbError.append("(" + (1) + ") "
								+ "El valor del campo Tasa o Cuota que corresponde a Traslado no contiene un valor del catalogo c_Tasa o Cuota "
								+ " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
					}
					numeroCelda = 0;

					if (!vectorTasaOCuota.isEmpty() && !vectorPrecioUnitario.isEmpty()) {
						if (vectorTasaOCuota.size() == cicloNum && vectorPrecioUnitario.size() == cicloNum) {
							vectorBase.add(vectorPrecioUnitario.get(cicloNum - 1));
							Map<String, Object> tipoBaseTra = UtilValidationsXML.validBaseTra(tags.mapCatalogos,
									vectorPrecioUnitario.get(cicloNum - 1).toString());
							if (tipoBaseTra.get("value").toString().equalsIgnoreCase("vacio")) {
								fPermisoVector = false;
								sbError.append("(" + (48 + 1) + ") " + tipoBaseTra.get("message").toString()
										+ " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
							}
						}

					} else {
						fPermisoVector = false;
						sbError.append("(" + (48 + 1) + ") " + "No se pudo calcular Base para el Impuesto Traslado "
								+ " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
					}

					if (!vectorBase.isEmpty() && !vectorTasaOCuota.isEmpty() && !vectorCantidad.isEmpty()) {
						if (vectorBase.size() == cicloNum && vectorTasaOCuota.size() == cicloNum) {
							if (!tipoFactorValRow.equalsIgnoreCase("Exento")
									&& !tipoFactorValRow.equalsIgnoreCase("Excento")) {
								vectorImporte.add((vectorBase.get(cicloNum - 1)
										* Double.valueOf(vectorCantidad.get(cicloNum - 1)))
										* vectorTasaOCuota.get(cicloNum - 1)); // vectorTasaOCuota.get(cicloNum-1)
							} else {
								vectorImporte.add(0.00); // vectorTasaOCuota.get(cicloNum-1)
							}

						}
					} else {
						vectorImporte.add(0.00);
					}

				}

			} else if (retencionBol) {

				if (numeroCelda == 1) {
					if (row.getCell(posicion) != null || row.getCell(posicion).toString().length() > 0) {
						Map<String, Object> tipoClaveProdServ = UtilValidationsXML.validClaveProdServ(tags.mapCatalogos,
								row.getCell(posicion).toString());
						if (tipoClaveProdServ.get("value").toString().equalsIgnoreCase("vacio")) {
							fPermisoVector = false;
							sbError.append("(" + (1) + ") " + tipoClaveProdServ.get("message").toString()
									+ " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
						} else {
							vectorClaveProdServRet.add(tipoClaveProdServ.get("value").toString());
						}
					} else {
						fPermisoVector = false;
						sbError.append("(" + (1) + ") " + "ClaveProdServ con formato incorrecto " + " en el Concepto "
								+ contadorConceptos + " - factura " + factura + "\n");
					}
				}

				if (numeroCelda == 2) {
					if (row.getCell(posicion) != null || row.getCell(posicion).toString().length() > 0) {
						vectorCantidadRet.add(row.getCell(posicion).toString());
					} else {
						fPermisoVector = false;
						sbError.append("(" + (1) + ") " + "Cantidad con formato incorrecto " + " en el Concepto "
								+ contadorConceptos + " - factura " + factura + "\n");
					}
				}

				if (numeroCelda == 3) {
					if (row.getCell(posicion) != null || row.getCell(posicion).toString().length() > 0) {

						Map<String, Object> tipoClaveUnidad = UtilValidationsXML.validClaveUnidad(tags.mapCatalogos,
								row.getCell(posicion).toString());
						if (tipoClaveUnidad.get("value").toString().equalsIgnoreCase("vacio")) {
							fPermisoVector = false;
							sbError.append("(" + (1) + ") " + tipoClaveUnidad.get("message").toString()
									+ " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
						} else {
							vectorClaveUnidadRet.add(tipoClaveUnidad.get("value").toString());
						}
					} else {
						fPermisoVector = false;
						sbError.append("(" + (1) + ") " + "ClaveUnidad con formato incorrecto " + " en el Concepto "
								+ contadorConceptos + " - factura " + factura + "\n");
					}
				}

				if (numeroCelda == 4) {
					if (row.getCell(posicion) != null || row.getCell(posicion).toString().length() > 0) {
						vectorUMRet.add(row.getCell(posicion).toString());
					} else {
						fPermisoVector = false;
						sbError.append("(" + (1) + ") " + "UM con formato incorrecto " + " en el Concepto "
								+ contadorConceptos + " - factura " + factura + "\n");
					}
				}

				if (numeroCelda == 5) {
					if (row.getCell(posicion) != null || row.getCell(posicion).toString().length() > 0) {
						vectorDescRet.add(row.getCell(posicion).toString());
					} else {
						fPermisoVector = false;
						sbError.append("(" + (1) + ") " + "Concepto Expedicion con formato incorrecto "
								+ " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
					}
				}

				if (numeroCelda == 6) {
					if (row.getCell(posicion) != null || row.getCell(posicion).toString().length() > 0) {
						Map<String, Object> tipoPrecioUnit = UtilValidationsXML.validValorUnitario(tags.mapCatalogos,
								row.getCell(posicion).toString(), tags.decimalesMoneda, comp.getTipoDeComprobante());
						if (!tipoPrecioUnit.get("value").toString().equalsIgnoreCase("vacio")) {
							vectorPrecioUnitarioRet.add(Double.parseDouble(row.getCell(posicion).toString()));
						} else {
							fPermisoVector = false;
							sbError.append("(" + (1) + ") " + tipoPrecioUnit.get("message").toString()
									+ " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
						}
					} else {
						fPermisoVector = false;
						sbError.append("(" + (1) + ") " + "Precio Unitario con formato incorrecto " + " en el Concepto "
								+ contadorConceptos + " - factura " + factura + "\n");
					}
				}

				if (numeroCelda == 7) {
					if (row.getCell(posicion).toString().equals("1")) {
						if (fPermisoVector)
							vectorAplicaIVARet.add(row.getCell(posicion).toString());
					} else {
						fPermisoVector = false;
						sbError.append("(" + (1) + ") " + "APLICA IVA con formato incorrecto " + " en el Concepto "
								+ contadorConceptos + " - factura " + factura + "\n");
					}
				}
				if (numeroCelda == 9) {
					if (row.getCell(posicion) != null || row.getCell(posicion).toString().length() > 0) {
						String tipoImp = UtilCatalogos.findValClaveImpuesto(tags.mapCatalogos,
								row.getCell(posicion).toString());
						if (!tipoImp.equalsIgnoreCase("vacio")) {
							vectorImpuestoRet.add(tipoImp);
							impuestoValRowRet = row.getCell(posicion).toString();
						} else {
							fPermisoVector = false;
							sbError.append("(" + (1) + ") " + "No se encotro el Impuesto en el catalogo C_Impuestos "
									+ " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
						}

					} else {
						fPermisoVector = false;
						sbError.append("(" + (1) + ") " + "Impuesto con formato incorrecto " + " en el Concepto "
								+ contadorConceptos + " - factura " + factura + "\n");
					}
				}
				if (numeroCelda == 10) {
					if (row.getCell(posicion) != null || row.getCell(posicion).toString().length() > 0) {
						Map<String, Object> tipoTipoFact = UtilValidationsXML.validTipoFactorRet(tags.mapCatalogos,
								row.getCell(posicion).toString());
						if (!tipoTipoFact.get("value").toString().equalsIgnoreCase("vacio")) {
							tipoFactorValRowRet = row.getCell(posicion).toString();
							vectorTipFactorRet.add(row.getCell(posicion).toString());
						} else {
							fPermisoVector = false;
							sbError.append("(" + (1) + ") " + tipoTipoFact.get("message").toString()
									+ " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
						}

					} else {
						fPermisoVector = false;
						sbError.append("(" + (1) + ") "
								+ "El valor del campo TipoFactor que corresponde a Retencion no contiene un valor del catalogo c_TipoFactor "
								+ " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
					}
				}

				if (numeroCelda == 11) {
					if (row.getCell(posicion) != null || row.getCell(posicion).toString().length() > 0) {
						Map<String, Object> tipoTasaOCuota = UtilValidationsXML.validTasaOCuotaRet(tags.mapCatalogos,
								impuestoValRowRet, tipoFactorValRowRet, row.getCell(posicion).toString(),
								tipoFactorValRowRet);
						if (!tipoTasaOCuota.get("value").toString().equalsIgnoreCase("vacio")) {
							vectorTasaOCuotaRet.add(Double.parseDouble(row.getCell(posicion).toString()));
							cicloNumRet = cicloNumRet + 1;
						} else {
							fPermisoVector = false;
							sbError.append("(" + (1) + ") " + tipoTasaOCuota.get("message").toString()
									+ " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
						}

					} else {
						fPermisoVector = false;
						sbError.append("(" + (1) + ") "
								+ "El valor del campo Tasa o Cuota que corresponde a Retencion no contiene un valor del catalogo c_Tasa o Cuota "
								+ " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
					}
					numeroCelda = 0;

					if (!vectorTasaOCuotaRet.isEmpty() && !vectorPrecioUnitarioRet.isEmpty()) {
						if (vectorTasaOCuotaRet.size() == cicloNumRet
								&& vectorPrecioUnitarioRet.size() == cicloNumRet) {
							vectorBaseRet.add(vectorPrecioUnitarioRet.get(cicloNumRet - 1));
							Map<String, Object> tipoBaseRet = UtilValidationsXML.validBaseRet(tags.mapCatalogos,
									vectorPrecioUnitarioRet.get(cicloNumRet - 1).toString());
							if (tipoBaseRet.get("value").toString().equalsIgnoreCase("vacio")) {
								fPermisoVector = false;
								sbError.append("(" + (48 + 1) + ") " + tipoBaseRet.get("message").toString()
										+ " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
							}
						}

					} else {
						fPermisoVector = false;
						sbError.append("(" + (48 + 1) + ") " + "No se pudo calcular Base para el Impuesto Traslado "
								+ " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
					}

					if (!vectorBaseRet.isEmpty() && !vectorTasaOCuotaRet.isEmpty() && !vectorCantidadRet.isEmpty()) {
						if (vectorBaseRet.size() == cicloNumRet && vectorTasaOCuotaRet.size() == cicloNumRet) {
							vectorImporteRet.add((vectorBaseRet.get(cicloNumRet - 1)
									* Double.valueOf(vectorCantidadRet.get(cicloNumRet - 1)))
									* vectorTasaOCuotaRet.get(cicloNumRet - 1)); // vectorTasaOCuotaRet.get(cicloNumRet-1)
						}
					} else {
						vectorImporteRet.add(0.00);
					}

				}

				if (row.getCell(posicion).toString().equals("||FINFACTURA||")) {
					fFinFactura = true;
				}

			}

			posicion = posicion + 1;

		}
		boolean fAplicaIVA = false;
		if (fPermisoVector) {
			List<ElementsInvoice> elementosIn = new ArrayList<ElementsInvoice>();
			Vector<Double> vectorIVA = new Vector<Double>();
			if (vectorCantidad != null && vectorUM != null && vectorDesc != null && vectorPrecioUnitario != null
					&& vectorAplicaIVA != null && vectorCantidad.size() == vectorUM.size()
					&& vectorUM.size() == vectorDesc.size() && vectorDesc.size() == vectorPrecioUnitario.size()
					&& vectorPrecioUnitario.size() == vectorAplicaIVA.size() && vectorCantidadRet != null
					&& vectorUMRet != null && vectorDescRet != null && vectorPrecioUnitarioRet != null
					&& vectorAplicaIVARet != null && vectorCantidadRet.size() == vectorUMRet.size()
					&& vectorUMRet.size() == vectorDescRet.size()
					&& vectorDescRet.size() == vectorPrecioUnitarioRet.size()
					&& vectorPrecioUnitarioRet.size() == vectorAplicaIVARet.size()) {

				List<Traslados> tras = new ArrayList<Traslados>();
				List<Retenciones> ret = new ArrayList<Retenciones>();
				Retenciones retMod = new Retenciones();

				Integer size = 0;
				if (vectorCantidad.size() > 0) {
					if (vectorCantidad.size() >= vectorCantidadRet.size()) {
						System.out.println("*********AMDAController Divisas Vector Dentro Traslado Size: ");
						size = vectorCantidad.size();
					}
				}

				if (vectorCantidadRet.size() > 0) {
					if (vectorCantidadRet.size() >= vectorCantidad.size()) {
						System.out.println("*********AMDAController Divisas Vector Dentro Retencion Size: ");
						size = vectorCantidadRet.size();
					}

				}
				Double sumatoriaImporte = 0.00;
				Double sumatoriaImporteRet = 0.00;
				Integer idRow = 0;
				for (int v = 0; v < size; v++) {
					if ((vectorCantidad != null && vectorUM != null && vectorDesc != null
							&& vectorPrecioUnitario != null && vectorAplicaIVA != null && vectorClaveProdServ != null
							&& vectorClaveUnidad != null)
							&& (vectorCantidad.size() > 0 && vectorUM.size() > 0 && vectorDesc.size() > 0
									&& vectorPrecioUnitario.size() > 0 && vectorAplicaIVA.size() > 0
									&& vectorClaveProdServ.size() > 0 && vectorClaveUnidad.size() > 0)) {
						ElementsInvoice ei = new ElementsInvoice();
						ei.setQuantity(Double.valueOf(vectorCantidad.get(v)));
						ei.setUnitMeasure(vectorUM.get(v));
						ei.setDescription(vectorDesc.get(v));
						ei.setUnitPrice(vectorPrecioUnitario.get(v));
						ei.setClaveProdServ(vectorClaveProdServ.get(v));
						ei.setClaveUnidad(vectorClaveUnidad.get(v));

						ei.setAmount(ei.getQuantity() * ei.getUnitPrice());
						if (vectorAplicaIVA.get(v).trim().equals("1")) {
							// No Aplica IVA
							vectorIVA.add(0.0);
						} else {
							// Aplica IVA
							fAplicaIVA = true;
							Double ivaItem = 0.0;
							// ivaItem = ei.getQuantity() *
							// ei.getUnitPrice() *
							// (invoice.getPorcentaje()/100);TODO:Falta
							// este campo
							vectorIVA.add(ivaItem);
							// invoice.setSiAplicaIva(true); TODO:
							// Falta este campo
						}
						elementosIn.add(ei);
					}

					if ((vectorCantidadRet != null && vectorUMRet != null && vectorDescRet != null
							&& vectorPrecioUnitarioRet != null && vectorAplicaIVARet != null
							&& vectorClaveProdServRet != null && vectorClaveUnidadRet != null)
							&& (vectorCantidadRet.size() > 0 && vectorUMRet.size() > 0 && vectorDescRet.size() > 0
									&& vectorPrecioUnitarioRet.size() > 0 && vectorAplicaIVARet.size() > 0
									&& vectorClaveProdServRet.size() > 0 && vectorClaveUnidadRet.size() > 0)) {
						ElementsInvoice eiRet = new ElementsInvoice();
						eiRet.setQuantity(Double.valueOf(vectorCantidadRet.get(v)));
						eiRet.setUnitMeasure(vectorUMRet.get(v));
						eiRet.setDescription(vectorDescRet.get(v));
						eiRet.setUnitPrice(vectorPrecioUnitarioRet.get(v));
						eiRet.setClaveProdServ(vectorClaveProdServRet.get(v));
						eiRet.setClaveUnidad(vectorClaveUnidadRet.get(v));

						eiRet.setAmount(eiRet.getQuantity() * eiRet.getUnitPrice());
						if (vectorAplicaIVARet.get(v).trim().equals("1")) {
							// No Aplica IVA
							vectorIVA.add(0.0);
						} else {
							// Aplica IVA
							fAplicaIVA = true;
							Double ivaItem = 0.0;
							// ivaItem = eiRet.getQuantity() *
							// eiRet.getUnitPrice() *
							// (invoice.getPorcentaje()/100); TODO:
							// Falta este campo
							vectorIVA.add(ivaItem);
							// invoice.setSiAplicaIva(true); TODO:
							// Falta este campo
						}
						elementosIn.add(eiRet);
					}

					if (vectorImpuesto != null && vectorImpuesto.size() > 0) {
						Traslados traMod = new Traslados();
						if (vectorBase != null && vectorImpuesto != null && vectorTipFactor != null
								&& vectorTasaOCuota != null && vectorImporte != null) {
							String tasaCoutaFormat = "0.000000";
							if (vectorTasaOCuota.get(v).toString().length() == 4) {
								tasaCoutaFormat = vectorTasaOCuota.get(v).toString() + "0000";
							} else if (vectorTasaOCuota.get(v).toString().length() == 5) {
								tasaCoutaFormat = vectorTasaOCuota.get(v).toString() + "000";
							} else if (vectorTasaOCuota.get(v).toString().length() == 6) {
								tasaCoutaFormat = vectorTasaOCuota.get(v).toString() + "00";
							} else if (vectorTasaOCuota.get(v).toString().length() == 7) {
								tasaCoutaFormat = vectorTasaOCuota.get(v).toString() + "0";
							} else if (vectorTasaOCuota.get(v).toString().length() == 8) {
								tasaCoutaFormat = vectorTasaOCuota.get(v).toString();
							} else if (vectorTasaOCuota.get(v).toString().length() == 3) {
								tasaCoutaFormat = vectorTasaOCuota.get(v).toString() + "00000";
							}

							traMod.setTipoImpuestos("1"); // BASE
							traMod.setImpuesto(vectorImpuesto.get(v));
							traMod.setTipoFactor(vectorTipFactor.get(v));
							traMod.setTasaOCuota(tasaCoutaFormat);
							traMod.setImporte(vectorImporte.get(v).toString());
							idRow = idRow + 1;
							traMod.setId(String.valueOf(idRow));
							tras.add(traMod);
							sumatoriaImporte = sumatoriaImporte + vectorImporte.get(v);
						}

					}
					if (vectorImpuestoRet != null && vectorImpuestoRet.size() > 0) {
						Traslados traModRet = new Traslados();
						if (vectorBaseRet != null && vectorImpuestoRet != null && vectorTipFactorRet != null
								&& vectorTasaOCuotaRet != null && vectorImporteRet != null) {
							String tasaCoutaFormat = "0.000000";
							if (vectorTasaOCuotaRet.get(v).toString().length() == 4) {
								tasaCoutaFormat = vectorTasaOCuotaRet.get(v).toString() + "0000";
							} else if (vectorTasaOCuotaRet.get(v).toString().length() == 5) {
								tasaCoutaFormat = vectorTasaOCuotaRet.get(v).toString() + "000";
							} else if (vectorTasaOCuotaRet.get(v).toString().length() == 6) {
								tasaCoutaFormat = vectorTasaOCuotaRet.get(v).toString() + "00";
							} else if (vectorTasaOCuotaRet.get(v).toString().length() == 7) {
								tasaCoutaFormat = vectorTasaOCuotaRet.get(v).toString() + "0";
							} else if (vectorTasaOCuotaRet.get(v).toString().length() == 8) {
								tasaCoutaFormat = vectorTasaOCuotaRet.get(v).toString();
							} else if (vectorTasaOCuotaRet.get(v).toString().length() == 3) {
								tasaCoutaFormat = vectorTasaOCuotaRet.get(v).toString() + "00000";
							}
							traModRet.setTipoImpuestos("0"); // BASE
							traModRet.setImpuesto(vectorImpuestoRet.get(v));
							traModRet.setTipoFactor(vectorTipFactorRet.get(v));
							traModRet.setTasaOCuota(tasaCoutaFormat);
							traModRet.setImporte(vectorImporteRet.get(v).toString());
							idRow = idRow + 1;
							traModRet.setId(String.valueOf(idRow));
							tras.add(traModRet);
							sumatoriaImporteRet = sumatoriaImporteRet + vectorImporteRet.get(v);
						}
					}

				}
				// invoice.setTraslados(tras); TODO: Falta este
				// campo

				if (fErrorIVA && fAplicaIVA)
					sbError.append(sbErrorIVA.toString());

				Double subtotal = 0.0;
				Double iva = 0.0;

				for (int iSub = 0; iSub < elementosIn.size(); iSub++) {
					subtotal += elementosIn.get(iSub).getAmount();
					iva += vectorIVA.get(iSub);
				}

				// Double porcentajeDescuento =
				// comp.getDescuento()*(invoice.getPorcentaje()/100);
				// TODO: falta campo porcentaje

				Double Total = 0.0;
				// TODO: porcentaje 1776
				// if(iva -porcentajeDescuento < 0){
				// iva = 0.0;
				// }else {
				// iva = iva -porcentajeDescuento;
				// }

				Total = subtotal + iva;
				// invoice.setSubTotal(subtotal); TODO: falta campo
				// invoice.setIva(Util.castToDouble(row.getCell(22).toString()));
				// TODO: falta campo
				BigDecimal totalDou = BigDecimal.ZERO;
				totalDou = new BigDecimal(Total).subtract(comp.getDescuento());
				if (vectorImporte != null && vectorImporte.size() > 0) {
					// invoice.setTotal((Total -
					// invoice.getDescuento()) +
					// (sumatoriaImporte));TODO: falta campo
					totalDou = totalDou.add(new BigDecimal(sumatoriaImporte));
				}

				if (vectorImporteRet != null && vectorImporteRet.size() > 0) {
					// invoice.setTotal((Total -
					// invoice.getDescuento()) -
					// (sumatoriaImporteRet));TODO: falta campo
					totalDou = totalDou.subtract(new BigDecimal(sumatoriaImporteRet));
				}
				comp.setTotal(totalDou);
				// invoice.setElements(elementosIn);TODO:Falta ver
				// informacion
			}
		}

		if (!fFinFactura) {
			sbError.append(
					"Estructura de Renglon incorrecta, no fue encontrada la etiqueta de control ||FINFACTURA||, en el Renglon "
							+ factura + "\n");
		}
		return sbError.toString();
	}

	public static Invoice fillInvoice(CfdiComprobanteFiscal comprobante) {
		Invoice invoice = new Invoice();

		invoice.setPorcentaje(0.00);
		invoice.setVersion(comprobante.getVersion());
		invoice.setMoneda("");
		invoice.setIvaDescription("");
		for (String key : comprobante.getAddenda().getCampoAdicional().keySet()) {
			if (key.equalsIgnoreCase("MONEDA")) {
				invoice.setMoneda(comprobante.getAddenda().getCampoAdicional().get(key));
			} else if (key.equalsIgnoreCase("DESCRIPCIÓN CONCEPTO")) {
				invoice.setDescriptionConcept(comprobante.getAddenda().getCampoAdicional().get(key));
			} else if (key.equalsIgnoreCase("DESCRIPCIÓN IVA")) {
				invoice.setIvaDescription(comprobante.getAddenda().getCampoAdicional().get(key));
			} else if (key.equalsIgnoreCase("TIPO CAMBIO")) {
				invoice.setTipoCambio(comprobante.getAddenda().getCampoAdicional().get(key));
			}
		}

		invoice.setDescriptionConcept("");

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
		invoice.setNoCertificado(comprobante.getNoCertificado());
		invoice.setFechaHora(comprobante.getFecha());

		invoice.setFolio(comprobante.getFolio());
		invoice.setDate(comprobante.getFecha());

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
			invoice.setPais(tUbicacion.getPais());
		}

		invoice.setSubTotal(comprobante.getSubTotal().doubleValue());

		if (comprobante.getDescuento() != null) {
			invoice.setDescuento(comprobante.getDescuento().doubleValue());
		}

		invoice.setTotal(comprobante.getTotal().doubleValue());

		if (comprobante.getImpuestos().getTotalImpuestosTrasladados() != null) {
			invoice.setIvaDescription("TOTAL IMP TRASLADO");
			invoice.setIva(comprobante.getImpuestos().getTotalImpuestosTrasladados().doubleValue());
			invoice.setTotalImpuestoRetenido(comprobante.getImpuestos().getTotalImpuestosRetenidos().doubleValue());
		} else {
			invoice.setIva(0.0);
		}

		invoice.setQuantityWriting(NumberToLetterConverter.convertNumberToLetter(invoice.getTotal()));
		invoice.setMetodoPago(comprobante.getMetodoPago());
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
						desc.append(breakLine.intern()).append("Base:").append(item.getBase());
						if (!Util.isNullEmpty(item.getImpuesto())) {
							desc.append(", Impuesto:").append(item.getImpuesto());
						}
						if (!Util.isNullEmpty(item.getTasaOCuota())) {
							desc.append(", TasaOCuota:").append(item.getTasaOCuota());
						}
						if (!Util.isNullEmpty(item.getTipoFactor())) {
							desc.append(", TipoFactor:").append(item.getTipoFactor());
						}
						if (!Util.isNullEmpty(item.getImporte())) {
							desc.append(", Importe:").append(item.getImporte());
						}
					}
				}
				if (objConcepto.getImpuestos().getRetenciones() != null
						&& objConcepto.getImpuestos().getRetenciones().size() > 0) {
					desc.append(breakLine.intern()).append("Retenciones");
					for (CfdiConceptoImpuestoTipo item : objConcepto.getImpuestos().getRetenciones()) {
						desc.append(breakLine.intern()).append("Base:").append(item.getBase());
						if (!Util.isNullEmpty(item.getImpuesto())) {
							desc.append(", Impuesto:").append(item.getImpuesto());
						}
						if (!Util.isNullEmpty(item.getTasaOCuota())) {
							desc.append(", TasaOCuota:").append(item.getTasaOCuota());
						}
						if (!Util.isNullEmpty(item.getTipoFactor())) {
							desc.append(", TipoFactor:").append(item.getTipoFactor());
						}
						if (!Util.isNullEmpty(item.getImporte())) {
							desc.append(", Importe:").append(item.getImporte());
						}
					}
				}
			}
			element.setDescription(desc.toString());
			element.setQuantity(objConcepto.getCantidad().doubleValue());
			element.setUnitMeasure(objConcepto.getUnidad());
			element.setUnitPrice(objConcepto.getValorUnitario().doubleValue());
			/* TODO:Falta asignar esta seccion que no se tiene */
			elements.add(element);
		}
		boolean descuento = false;
		if (comprobante.getDescuento().doubleValue() > 0) {
			// invoice.setMotivoDescuento(docEle.getAttribute("motivoDescuento"));
			invoice.setDescuento(comprobante.getDescuento().doubleValue());
			descuento = true;
		}

		if (descuento) {
			ElementsInvoice elementDescuento = new ElementsInvoice();
			elementDescuento.setDescription(invoice.getMotivoDescuento());
			elementDescuento.setUnitPrice(invoice.getDescuento());
			elementDescuento.setAmount(invoice.getDescuento());
			elementDescuento.setQuantity(1);
			elements.add(elementDescuento);
		}
		invoice.setElements(elements);

		/* Elementos faltantes */
		invoice.setMetodoPago(comprobante.getMetodoPago());
		invoice.setLugarExpedicion(comprobante.getLugarExpedicion());
		invoice.setNumCtaPago("");// No existe en 3.3
									// docEle.getAttribute("NumCtaPago"));
		invoice.setRegimenFiscal(comprobante.getEmisor().getRegimenFiscal());
		TimbreFiscal timbre = new TimbreFiscal();
		timbre.setVersion(comprobante.getComplemento().getTimbreFiscalDigital().getVersion());
		timbre.setUuid(comprobante.getComplemento().getTimbreFiscalDigital().getUuid());
		invoice.setSello(comprobante.getComplemento().getTimbreFiscalDigital().getSelloCFD());
		timbre.setSelloSat(comprobante.getComplemento().getTimbreFiscalDigital().getSelloSAT());
		timbre.setNoCertificadoSat(comprobante.getComplemento().getTimbreFiscalDigital().getNoCertificadoSAT());
		timbre.setFechaTimbrado(comprobante.getComplemento().getTimbreFiscalDigital().getFechaTimbrado());
		invoice.setTimbreFiscal(timbre);
		invoice.setDonataria(comprobante.getComplemento().getDonataria());

		invoice.setCostCenter(comprobante.getAddenda().getInformacionEmision().getCentroCostos());
		invoice.setCustomerCode(comprobante.getAddenda().getInformacionEmision().getCodigoCliente());
		invoice.setContractNumber(comprobante.getAddenda().getInformacionEmision().getContrato());
		invoice.setPeriod(comprobante.getAddenda().getInformacionEmision().getPeriodo());
		// Datos Addendas Filiales
		invoice.setTipoAddenda("0");
		// Va para la addenda logistica
		invoice.setCodigoISO(comprobante.getAddenda().getInformacionPago().getCodigoISOMoneda());
		// Va para las tres addendas filiales
		if (!comprobante.getAddenda().getInformacionPago().getPosCompra().equals("")) {
			invoice.setTipoAddenda("1");
			invoice.setPosicioncompraLog(comprobante.getAddenda().getInformacionPago().getPosCompra());
		}
		// Va para la addenda financiera
		if (!comprobante.getAddenda().getInformacionPago().getCuentaContable().equals("")) {
			invoice.setTipoAddenda("2");
			invoice.setCuentacontableFin(comprobante.getAddenda().getInformacionPago().getCuentaContable());
		}
		// Va para la addenda arrendamiento
		if (!comprobante.getAddenda().getInmuebles().getNumContrato().equals("")
				&& !comprobante.getAddenda().getInmuebles().getFechaVencimiento().equals("")) {
			invoice.setTipoAddenda("3");
			invoice.setNumerocontratoArr(comprobante.getAddenda().getInmuebles().getNumContrato());
			invoice.setFechavencimientoArr(comprobante.getAddenda().getInmuebles().getFechaVencimiento());
		}

		invoice.setTipoDeComprobante(comprobante.getTipoDeComprobante());
		invoice.setSerie(comprobante.getSerie());

		invoice.setEmail(comprobante.getAddenda().getInformacionPago().getEmail());
		invoice.setPurchaseOrder(comprobante.getAddenda().getInformacionPago().getOrdenCompra());
		invoice.setBeneficiaryName(comprobante.getAddenda().getInformacionPago().getNombreBeneficiario());
		invoice.setReceivingInstitution(comprobante.getAddenda().getInformacionPago().getInstitucionReceptora());
		invoice.setNumCtaPago(comprobante.getAddenda().getInformacionPago().getNumeroCuenta());
		invoice.setProviderNumber(comprobante.getAddenda().getInformacionPago().getNumProveedor());

		invoice.setTipoOperacion(comprobante.getComplemento().getDivisaTipoOperacion());

		return invoice;
	}

	public String checkCellType(Cell campoFactura) {
		if (campoFactura.getCellType() == Cell.CELL_TYPE_STRING) {
			return "";
		} else {
			return "Formato de celda incorrecto";
		}
	}

	private boolean validaDatoRE(String dato, String expReg) {
		return dato != null && dato.trim().length() > 0 && dato.matches(expReg);
	}

	private boolean validaDatoRELongitud(String dato, String expReg, int longitud) {
		return dato != null && dato.trim().length() > 0 && dato.length() <= longitud && dato.matches(expReg);
	}

	public static void main(String[] args) {
		System.out.println("asd");
	}
	
	public String validateComprobanteDivisas(CfdiComprobanteFiscal comp, int factura) {

	FiscalEntity fiscalEntity = null;
	Customer customer = null;
	StringBuilder sbError = new StringBuilder();
	CFDFieldsV22 cfdFieldsV22 = null;
	System.out.println("Tipo Emision factura(" + factura + "): " + comp.getTipoEmision());

	// validar etiqueta de control fin factura
	if (!comp.getFinFactura()) {
		sbError.append(
				"Estructura de Renglon incorrecta, no fue encontrada la etiqueta de control ||FINFACTURA||, en el Renglon "
						+ factura + "\n");
	}

	if(comp.getAddenda().getCampoAdicional() == null){
		comp.getAddenda().setCampoAdicional(new HashMap<String, String>());
	}
	// fiscalEntity = new FiscalEntity();
	/* Emisor Posicion 0--row 0 */
	if (comp.getEmisor() == null || comp.getEmisor().getRfc() == null || comp.getEmisor().getRfc() == "") {
		sbError.append("Posicion Entidad Fiscal requerida (Null) - Factura " + factura + "\n");
	} else {

		fiscalEntity = new FiscalEntity();
		fiscalEntity.setTaxID(comp.getEmisor().getRfc());
		fiscalEntity = fiscalEntityManager.findByRFCA(fiscalEntity);
		System.out.println("rfcEnt: " + comp.getEmisor().getRfc());
		if (fiscalEntity == null) {
			System.out.println("ErrorDB");
			sbError.append("Entidad Fiscal no existe en BD - Factura " + factura + "\n");
		} else {
			cfdFieldsV22 = cfdFieldsV22Manager.findByFiscalID(fiscalEntity.getId());
			if (fiscalEntity.getIsDonataria() == 1 && !comp.getTipoEmision().equals(TipoEmision.DONATARIAS)) {
				sbError.append("Entidad Fiscal incorrecta, es donataria - Factura " + factura + "\n");
			} else if (fiscalEntity.getIsDonataria() == 0 && comp.getTipoEmision().equals(TipoEmision.DONATARIAS)) {
				sbError.append("Entidad Fiscal incorrecta, no es donataria - Factura " + factura + "\n");
			} else {
				if(fiscalEntity.getIsDonataria() == 1 
						&& fiscalEntity.getLeyendaDonataria() != null
						&& !fiscalEntity.getLeyendaDonataria().isEmpty()){
					comp.getAddenda().getCampoAdicional().put("LeyendaDonataria", fiscalEntity.getLeyendaDonataria());
				}
				comp.getEmisor().setNombre(fiscalEntity.getFiscalName());
				comp.getEmisor().setRfc(fiscalEntity.getTaxID());
				if (fiscalEntity.getAddress() != null) {
					comp.getEmisor().setDomicilio(new CfdiDomicilio());
					comp.getEmisor().getDomicilio().setCalle(fiscalEntity.getAddress().getStreet());
					comp.getEmisor().getDomicilio().setCodigoPostal(fiscalEntity.getAddress().getZipCode());
					comp.getEmisor().getDomicilio().setColonia(fiscalEntity.getAddress().getNeighborhood());
					comp.getEmisor().getDomicilio().setEstado(fiscalEntity.getAddress().getState().getName());
					comp.getEmisor().getDomicilio().setLocalidad(fiscalEntity.getAddress().getCity());
					comp.getEmisor().getDomicilio().setMunicipio(fiscalEntity.getAddress().getRegion());
					comp.getEmisor().getDomicilio().setNoExterior(fiscalEntity.getAddress().getExternalNumber());
					comp.getEmisor().getDomicilio().setNoInterior(fiscalEntity.getAddress().getInternalNumber());
					comp.getEmisor().getDomicilio()
							.setPais(fiscalEntity.getAddress().getState().getCountry().getName());
					comp.getEmisor().getDomicilio().setReferencia(fiscalEntity.getAddress().getReference());
				}
			}
		}
	}

	/* Serie Posicion 1 -- row 1 */
	if (comp.getSerie() != null && !comp.getSerie().equals("")) {
		if (!validaDatoRE(comp.getSerie(), SERIE_25)) {
			sbError.append("El campo Serie tiene un formato incorrecto " + factura + "\n");
		}
	}else{
		comp.setSerie("");
	}

	/* Forma de pago */

	System.out.println("antes de del if forma de pago: " + comp.getTipoEmision());
	System.out.println("antes de del if forma de pago tipo emision: " + TipoEmision.RECEPCION_PAGOS);
	
	if (!comp.getTipoEmision().equals(TipoEmision.RECEPCION_PAGOS)) {
		System.out.println("forma de pago validate: " + comp.getFormaPago());
		if (comp.getFormaPago() == null || comp.getFormaPago().trim().equals("")) {
			sbError.append(
					"El Campo Forma Pago No Contiene Un Valor Del Catalogo C_FormaPago - Factura "
							+ factura + "\n");
		} else {
			Map<String, Object> tipoFormaPago = UtilValidationsXML.validFormaPago(tags.mapCatalogos,
					comp.getFormaPago());
			if (!tipoFormaPago.get("value").toString().equalsIgnoreCase("vacio")) {
				comp.setFormaPago(tipoFormaPago.get("value").toString());
			} else {
				sbError.append(tipoFormaPago.get("message").toString() + "Factura " + factura + "\n");
			}
		}
	}

	/* Motivo descuento */
	// opcional
	if (comp.getMotivoDescCellValue() != null) {
		if (comp.getMotivoDescCellValue().equals("")) {
			// Descuento
			if (comp.getDescuento() == null) {
				comp.setDescuento(BigDecimal.ZERO);
				System.out.println("Descuento cero");
			} else {
				if (validaDatoRE(comp.getDescuento().toString(), RE_DECIMAL_NEGATIVO)) {
					if (comp.getDescuento().doubleValue() > 0) {
						sbError.append("Favor de informar el Motivo de descuento correspondiente al Descuento "
								+ " - factura " + factura + "\n");
					} else {
						comp.setDescuento(BigDecimal.ZERO);
					}
				} else {
					sbError.append("Descuento con formato incorrecto, se espera ("+RE_DECIMAL_NEGATIVO+") - factura " + factura + "\n");
				}
			}
		} else {
			if (comp.getMotivoDescCellValue().length() > 0 && comp.getMotivoDescCellValue().length() <= 1500) {

				// Descuento
				if (comp.getDescuento() == null) {
					sbError.append("Favor de informar el Descuento correspondiente al Motido de descuento "
							+ " - factura " + factura + "\n");
				} else {

					if (validaDatoRE(comp.getDescuento().toString(), RE_DECIMAL_NEGATIVO)) {
						if (!(comp.getDescuento().doubleValue() > 0)) {
							sbError.append("Favor de informar el Descuento correspondiente al Motido de descuento "
									+ " - factura " + factura + "\n");
						}
					} else {
						sbError.append("Descuento con formato incorrecto, se espera ("+RE_DECIMAL_NEGATIVO+") - factura " + factura + "\n");
					}

				}

			} else {
				sbError.append("Motivo de descuento no debe exceder 1500 caracteres  - factura " + factura + "\n");
			}
		}
	}

	/* Posicion 5 Moneda */
	if (comp.getTipoEmision().equalsIgnoreCase(TipoEmision.RECEPCION_PAGOS)) {
		comp.setMoneda("XXX");
	} else {
		if (comp.getMoneda() != null && comp.getMoneda().trim().length() > 0) {
			Map<String, Object> tipoMon = UtilValidationsXML.validMoneda(tags.mapCatalogos, comp.getMoneda());
			if (!tipoMon.get("value").toString().equalsIgnoreCase("vacio")) {
				comp.setMoneda(tipoMon.get("value").toString());
			} else {
				sbError.append(tipoMon.get("message").toString() + factura + "\n");
			}
		} else {
			sbError.append("El campo Moneda(Null,Vacio) no contiene un valor del catalogo c_Moneda "
					+ factura + "\n");
		}
	}
	//campo adicional moneda
	if(comp.getAddenda().getCampoAdicional() != null){
		comp.getAddenda().getCampoAdicional().put("Moneda", comp.getMoneda().trim());
	}

	/* Tipo de cambio */
	if (!comp.getTipoEmision().equalsIgnoreCase(TipoEmision.RECEPCION_PAGOS)) {
		if (comp.getTipoCambio() != null && comp.getTipoCambio().trim().length() > 0) {
			if (comp.getMoneda() != null) {
				if (!comp.getMoneda().trim().equalsIgnoreCase("XXX")) {


					Map<String, Object> tipoCam = UtilValidationsXML.validTipoCambio(tags.mapCatalogos,
							comp.getTipoCambio(), comp.getMoneda());
					if (!tipoCam.get("value").toString().equalsIgnoreCase("vacio")) {
						
//						comp.setTipoCambio(tipoCam.get("value").toString());
						
						if (comp.getMoneda().equalsIgnoreCase("MXN")) {
							comp.setTipoCambio("1");
						}
						
						//campo adicional tipo de cambio
						DecimalFormat df = new DecimalFormat("0.0000");
						if(comp.getAddenda().getCampoAdicional() != null){
							Double tipoCambio = new Double(comp.getTipoCambio());
							comp.getAddenda().getCampoAdicional().put("Tipo Cambio", df.format(tipoCambio));
						}
					} else {
						sbError.append(tipoCam.get("message").toString() + " factura: " + factura + "\n");
					}
				} else {
					comp.setTipoCambio("");
				}
			}
		} else {
			sbError.append("Tipo de Cambio es requerido - Factura " + factura + "\n");
		}
	}

	// Lugar de expedicion(donatarias)
	if (comp.getTipoEmision().equalsIgnoreCase(TipoEmision.DONATARIAS)) {
		if (comp.getLugarExpedicion() == null || comp.getLugarExpedicion().isEmpty()) {
			if(cfdFieldsV22 != null && cfdFieldsV22.getLugarDeExpedicion() != null){
				comp.setLugarExpedicion(cfdFieldsV22.getLugarDeExpedicion());
			}else{
				sbError.append("Lugar de Expedicion es requerido - Factura " + factura + "\n");
			}
		}else{
			if (!validaDatoRELongitud(comp.getLugarExpedicion(), RE_CHAR, 250)) {
				sbError.append("Lugar de expedicion con formato incorrecto, se espera ("+RE_CHAR+") - Factura " + factura + "\n");
            }
		}
	}

	/* Tipo comprobante posicion 7 -- row 7 */
	// String strTipoComprobante = "";
	if (comp.getTipoEmision().equalsIgnoreCase(TipoEmision.RECEPCION_PAGOS)) {
		comp.setTipoDeComprobante(TipoComprobante.PAGO);
	} else {
		if (!comp.getTipoDeComprobante().equals(TipoComprobante.INGRESO)) {
			if(comp.getTipoDeComprobante() != null && !comp.getTipoDeComprobante().toString().trim().isEmpty()){
				Map<String, Object> tipoComp = UtilValidationsXML.validTipoComprobante(tags.mapCatalogos,
						comp.getTipoDeComprobante());
				if (!tipoComp.get("value").toString().equalsIgnoreCase("tipoDeComprobanteIncorrecto")) {
					// strTipoComprobante = comp.getTipoDeComprobante();
					comp.setTipoDeComprobante(tipoComp.get("value").toString());
				} else {
					sbError.append(tipoComp.get("message").toString() + factura + "\n");
				}
			}else{
				comp.setTipoDeComprobante(TipoComprobante.INGRESO);
			}
		}
	}

	/* Decimales moneda */
	if (comp.getMoneda() == null || comp.getMoneda().trim().equals("")
			|| comp.getMoneda().trim().equalsIgnoreCase("XXX")) {
		tags.decimalesMoneda = 2;
		comp.setDecimalesMoneda(tags.decimalesMoneda);
	} else {
		tags.decimalesMoneda = UtilCatalogos.findDecimalesMoneda(tags.mapCatalogos, comp.getMoneda());
		comp.setDecimalesMoneda(tags.decimalesMoneda);
	}
	if (comp.getMoneda().trim().equalsIgnoreCase("XXX")) {
		tags.decimalesMoneda = 0;
		comp.setDecimalesMoneda(tags.decimalesMoneda);
	}

	/* Metodo de pago */
	if (!comp.getTipoEmision().equalsIgnoreCase(TipoEmision.RECEPCION_PAGOS)) {
		if (comp.getMetodoPago() == null || comp.getMetodoPago().trim().equals("")) {
			sbError.append(
					"El Campo Metodo Pago No Contiene Un Valor Del Catalogo C_MetodoPago - Factura "
							+ factura + "\n");
		} else {
			Map<String, Object> tipoMetPag = UtilValidationsXML.validMetodPago(tags.mapCatalogos,
					comp.getMetodoPago());
			if (tipoMetPag.get("value").toString().equals("vacio")) {
				sbError.append(tipoMetPag.get("message").toString() + factura + "\n");
			} else {
				comp.setMetodoPago(tipoMetPag.get("value").toString());
			}
		}
	}

	/* Regimen fiscal */
	if (comp.getEmisor().getRegimenFiscal() == null || comp.getEmisor().getRegimenFiscal().trim().equals("")) {
		sbError.append(
				"El campo RegimenFiscal, no contiene un valor(null, vacio) del catálogo c_RegimenFiscal-"
						+ " Factura " + factura + "\n");
	} else {
		Map<String, Object> tipoRegFis = UtilValidationsXML.validRegFiscal(tags.mapCatalogos,
				comp.getEmisor().getRegimenFiscal());
		if (!tipoRegFis.get("value").toString().equalsIgnoreCase("vacio")) {
			comp.getEmisor().setRegimenFiscal(tipoRegFis.get("value").toString());
		} else {
			sbError.append(tipoRegFis.get("message").toString() + " Factura " + factura + "\n");
		}
	}
	boolean foreingWhitoutId = false;
	/* RFC del cliente */
	if (comp.getCustomerRfcCellValue() == null) {
		sbError.append("Posicion RFC del Cliente requerida (Null) - Factura " + factura + "\n");
	} else {
		if (comp.getCustomerRfcCellValue().equals("")) {
			sbError.append("RFC de Cliente requerido - Factura " + factura + "\n");
		} else {
			// reempazar RFC incorrecto por generico
			if (validaDatoRE(comp.getCustomerRfcCellValue().trim().toUpperCase(), RFC_PATTERN)
					&& validaDatoRE(comp.getCustomerRfcCellValue().trim().toUpperCase(), RFC_PATTERN_TWO)) {
				System.out.println("RFC valido:" + comp.getCustomerRfcCellValue().trim().toUpperCase());
			} else {
				System.out.println("Reemplazar RFC incorrecto: "
						+ comp.getCustomerRfcCellValue().trim().toUpperCase() + " por generico: XAXX010101000");
				comp.setCustomerRfcCellValue("XAXX010101000");
			}
			if (comp.getCustomerRfcCellValue().trim().toUpperCase().equals("XEXX010101000")
					|| comp.getCustomerRfcCellValue().trim().toUpperCase().equals("XAXX010101000")
					|| comp.getCustomerRfcCellValue().trim().equals("XEXE010101000")) {
				if(comp.getStrIDExtranjero() == null || comp.getStrIDExtranjero().isEmpty()){
					foreingWhitoutId = true;
				}
			}
		}
	}
	
	//validaciones de datos de cliente
	// Uso CFDI
	if (comp.getTipoEmision().equalsIgnoreCase(TipoEmision.RECEPCION_PAGOS)) {
		comp.getReceptor().setUsoCFDI("P01");
	} else {
		if(comp.getReceptor().getUsoCFDI() != null && !comp.getReceptor().getUsoCFDI().trim().isEmpty()){
			if (!comp.getReceptor().getUsoCFDI().equals("D04")) {
				Map<String, Object> tipoUsoCfdi = UtilValidationsXML.validUsoCFDI(tags.mapCatalogos,
						comp.getReceptor().getUsoCFDI());
				if (!tipoUsoCfdi.get("value").toString().equalsIgnoreCase("vacio")) {
					comp.getReceptor().setUsoCFDI(tipoUsoCfdi.get("value").toString());
				} else {
					sbError.append(tipoUsoCfdi.get("message").toString() + factura + "\n");
				}
			}
		}else{
			sbError.append("El campo Uso CFDI es requerido" + " - Factura " + factura + "\n");
		}
	}
	//nombre
	if(comp.getReceptor().getNombre() == null || comp.getReceptor().getNombre().equals("")){
		sbError.append("Nombre de Cliente requerido - Factura " + factura + "\n");
	}else{
		if(comp.getReceptor().getNombre().length()>100){
			sbError.append("Nombre de Cliente no puede contener mas de 100 caracteres - Factura " + factura + "\n");
		}
		if(!validaDatoRE(comp.getReceptor().getNombre(), RE_CHAR_ALL)){
			sbError.append("Nombre de cliente con formato incorrecto, se espera ("+RE_CHAR_ALL+") - Factura " + factura  + "\n");
		}

	}
	//No exterior
	if(comp.getReceptor().getDomicilio().getNoExterior() != null 
			&& !comp.getReceptor().getDomicilio().getNoExterior().equals("")){
		if(comp.getReceptor().getDomicilio().getNoExterior().length() > 50){
			sbError.append("No.Exterior no puede contener mas de 50 caracteres - Factura " + factura + "\n");
		}
		if(!validaDatoRE(comp.getReceptor().getDomicilio().getNoExterior(), RE_NUMBER)){
			sbError.append("No.Exterior con formato incorrecto, se espera ("+RE_NUMBER+") - Factura " + factura  + "\n");
		}
	}
	//No interior
	if(comp.getReceptor().getDomicilio().getNoInterior() != null 
			&& !comp.getReceptor().getDomicilio().getNoInterior().equals("")){
		if(comp.getReceptor().getDomicilio().getNoInterior().length() > 100){
			sbError.append("No.interior no puede contener mas de 100 caracteres - Factura " + factura + "\n");
		}
		if(!validaDatoRE(comp.getReceptor().getDomicilio().getNoExterior(), RE_CHAR_ALL)){
			sbError.append("No.interior con formato incorrecto, se espera ("+RE_CHAR_ALL+") - Factura " + factura  + "\n");
		}
	}
	
	//localidad
	if(comp.getTipoEmision().equalsIgnoreCase(TipoEmision.DONATARIAS) 
			|| comp.getTipoEmision().equalsIgnoreCase(TipoEmision.FORMATO_UNICO)){
		
		if(comp.getReceptor().getDomicilio().getLocalidad() != null 
				&& !comp.getReceptor().getDomicilio().getLocalidad().equals("")){
			if(comp.getReceptor().getDomicilio().getLocalidad().length() > 100){
				sbError.append("Localidad no puede contener mas de 100 caracteres - Factura " + factura + "\n");
			}
			if(!validaDatoRE(comp.getReceptor().getDomicilio().getLocalidad(), RE_CHAR_ALL)){
				sbError.append("Localidad con formato incorrecto, se espera ("+RE_CHAR_ALL+") - Factura " + factura  + "\n");
			}
		}
	}
	//referencia
	if(comp.getReceptor().getDomicilio().getReferencia() != null 
			&& !comp.getReceptor().getDomicilio().getReferencia().equals("")){
		if(comp.getReceptor().getDomicilio().getReferencia().length() <= 250){
			if (!validaDatoRE(comp.getReceptor().getDomicilio().getReferencia(), RE_CHAR)) {
				sbError.append("Referencia con formato incorrecto, se espera ("+RE_CHAR+") - Factura " + factura + "\n");
            }
		}else{
			sbError.append("Referencia no puede contener mas de 250 caracteres - Factura " + factura + "\n");
		}
	}
	
	//fin validaciones de datos de cliente


	/* Codigo cliente */
	if(comp.getAddenda().getInformacionEmision().getCodigoCliente() != null 
			&& !comp.getAddenda().getInformacionEmision().getCodigoCliente().trim().isEmpty()){
		if(comp.getAddenda().getInformacionEmision().getCodigoCliente().trim().length() <= 10){
			if (!validaDatoRE(comp.getAddenda().getInformacionEmision().getCodigoCliente(), RE_CHAR)) {
				sbError.append("Codigo de Cliente con formato incorrecto, se espera ("+RE_CHAR+") - Factura " + factura + "\n");
            }
		}else{
			sbError.append("Codigo de Cliente no puede contener mas de 10 caracteres - Factura " + factura + "\n");
		}
	}

	/* Contrato */
	if(comp.getAddenda().getInformacionEmision().getContrato() != null 
			&& !comp.getAddenda().getInformacionEmision().getContrato().trim().isEmpty()){
		if(comp.getAddenda().getInformacionEmision().getContrato().trim().length() <= 20){
			if (!validaDatoRE(comp.getAddenda().getInformacionEmision().getContrato(), RE_CHAR)) {
				sbError.append("Contrato con formato incorrecto, se espera ("+RE_CHAR+") - Factura " + factura + "\n");
            }
		}else{
			sbError.append("Contrato no puede contener mas de 20 caracteres - Factura " + factura + "\n");
		}
	}

	/* Periodo */
	if(comp.getAddenda().getInformacionEmision().getPeriodo() != null 
			&& !comp.getAddenda().getInformacionEmision().getPeriodo().trim().isEmpty()){
		if(comp.getAddenda().getInformacionEmision().getPeriodo().trim().length() <= 19){
			if (!validaDatoRE(comp.getAddenda().getInformacionEmision().getPeriodo(), RE_CHAR)) {
				sbError.append("Periodo con formato incorrecto, se espera ("+RE_CHAR+") - Factura " + factura + "\n");
            }
		}else{
			sbError.append("Periodo no puede contener mas de 19 caracteres - Factura " + factura + "\n");
		}
	}

	/* Centro de costos */
	if(comp.getAddenda().getInformacionEmision().getCentroCostos() != null 
			&& !comp.getAddenda().getInformacionEmision().getCentroCostos().trim().isEmpty()){
			if (!validaDatoRELongitud(comp.getAddenda().getInformacionEmision().getCentroCostos(), CCOSTOS, 20)) {
				sbError.append("Centro de Costos con formato incorrecto, se espera ("+CCOSTOS+") - Factura " + factura + "\n");
            }
	}

	/* Descripcion concepto */
	if(comp.getAddenda().getCampoAdicional() != null 
			&& !comp.getTipoEmision().equalsIgnoreCase(TipoEmision.DONATARIAS)){
		String descripcionConcepto = comp.getAddenda().getCampoAdicional().get("DESCRIPCIÓN CONCEPTO");
		if(descripcionConcepto != null && !descripcionConcepto.trim().isEmpty()){
			if(descripcionConcepto.trim().length() > 250){
				sbError.append("Descripcion Concepto no puede contener mas de 250 caracteres - Factura " + factura + "\n");
			}
		}
	}

	/* Iva */
	boolean fErrorIVA = false;
	StringBuilder sbErrorIVA = new StringBuilder();
	if (!comp.getTipoEmision().equalsIgnoreCase(TipoEmision.DONATARIAS)) {
		if (comp.getIvaCellValue() == null) {
			sbErrorIVA.append("Posicion IVA requerida (Null) - Factura " + factura + "\n");
			fErrorIVA = true;
		} else {
			System.out.println("IVA: " + comp.getIvaCellValue());
			if (!comp.getIvaCellValue().trim().equals("")) {
				if (validaDatoRE(comp.getIvaCellValue().trim(), RE_DECIMAL)) {
					String tasa = "0";
					if(comp.getIvaCellValue().contains(".")){
						tasa = Util.getTASA(comp.getIvaCellValue().trim());
					}else{
						tasa = comp.getIvaCellValue().trim();
					}
					Iva iva = ivaManager.findByTasa(tasa);
					if (iva == null) {
						sbErrorIVA.append("Descripcion de IVA no existe en BD - Factura " + factura + "\n");
						System.out.println("Descripcion de IVA no existe en BD - Factura " + factura + "\n");
						fErrorIVA = true;
					} else {
						//comp.getAddenda().getCampoAdicional().put("DESCRIPCIÓN IVA", iva.getDescripcion().trim());
					}
				} else {
					sbErrorIVA.append("IVA incorrecto - Factura " + factura + "\n");
					fErrorIVA = true;
				}
			} else {
				sbErrorIVA.append("IVA requerido - Factura " + factura + "\n");
				fErrorIVA = true;
			}
		}
	}

	/* Tipo addenda */
	if (!comp.getTipoEmision().equalsIgnoreCase(TipoEmision.DONATARIAS)) {
		if (comp.getTipoAddendaCellValue() != null) {
			if (comp.getTipoAddendaCellValue().toString().contains(".")) {
				System.out.println("*** response Dentro IF AMDA: " + comp.getTipoAddendaCellValue());
				String words[] = comp.getTipoAddendaCellValue().split("\\.");
				comp.setTipoAddendaCellValue(words[0]);
				System.out.println("*** response Dentro IF despues AMDA: " + comp.getTipoAddendaCellValue());
			}
			System.out.println("tipoAddenda:" + comp.getTipoAddendaCellValue());

			if (validaDatoRE(comp.getTipoAddendaCellValue().trim(), RE_DECIMAL) || comp.getTipoAddendaCellValue().trim().equals("")) {
				String strTipoAddenda = comp.getTipoAddendaCellValue();
				System.out.println("tipoAddendaClean: " + strTipoAddenda);

				if (strTipoAddenda.equals("1") || strTipoAddenda.equals("2") || strTipoAddenda.equals("3")) {
//					comp.getAddenda().getInformacionPago().setNombreBeneficiario("");
//					comp.getAddenda().getInformacionPago().setInstitucionReceptora("");
//					comp.getAddenda().getInformacionPago().setNumeroCuenta("");
//					comp.getAddenda().getInformacionPago().setNumProveedor("");

					// Email Proveedor
					if (comp.getAddenda().getInformacionPago().getEmail() == null) {
						sbError.append("Posicion Email Proveedor requerida (Null) - factura " + factura + "\n");
					} else {
						if (!comp.getAddenda().getInformacionPago().getEmail().trim().equals("")) {
							if (!validaDatoRE(comp.getAddenda().getInformacionPago().getEmail().trim(), RE_MAIL)) {
								sbError.append(
										"Email Proveedor con estructura incorrecta - factura " + factura + "\n");
							}
						} else {
							sbError.append("Email Proveedor requerido - factura " + factura + "\n");
						}
					}

					// Codigo ISO moneda
					if (comp.getAddenda().getInformacionPago().getCodigoISOMoneda() == null) {
						sbError.append("Posicion Codigo ISO Moneda requerida (Null) - factura " + factura + "\n");
					} else {

						if (!comp.getAddenda().getInformacionPago().getCodigoISOMoneda().trim().equals("")) {
							CodigoISO codigoISOLog = new CodigoISO();

							codigoISOLog = codigoISOManager.findByCodigo(comp.getAddenda().getInformacionPago()
									.getCodigoISOMoneda().trim().toUpperCase());

							if (codigoISOLog == null) {
								sbError.append("Codigo ISO Moneda no existe en BD - factura " + factura + "\n");
							}
						} else {
							sbError.append("Codigo ISO Moneda requerido - factura " + factura + "\n");
						}
					}
				}

				if (strTipoAddenda.equals("1")) {

					// Orden Compra
					if (comp.getAddenda().getInformacionPago().getOrdenCompra() == null) {
						sbError.append("Posicion Orden Compra requerida (Null) - factura " + factura + "\n");
					} else {
						if (comp.getAddenda().getInformacionPago().getOrdenCompra().trim().equals("")) {
							sbError.append("Orden Compra requerida - factura " + factura + "\n");
						} else {
							System.out.println(
									"orden compra log: " + comp.getAddenda().getInformacionPago().getOrdenCompra());
						}

					}

					// Posicion Compra
					if (comp.getAddenda().getInformacionPago().getPosCompra() == null) {
						sbError.append("Posicion Compra requerida (Null) - factura " + factura + "\n");
					} else {
						if (comp.getAddenda().getInformacionPago().getPosCompra().trim().equals("")) {
							sbError.append("Posicion Compra requerida - factura " + factura + "\n");
						} else {
							System.out.println(
									"posicion compra: " + comp.getAddenda().getInformacionPago().getPosCompra());
						}

					}

				} else if (strTipoAddenda.equals("2")) {

					// Cuenta contable
					if (comp.getAddenda().getInformacionPago().getCuentaContable() == null) {
						sbError.append("Posicion Cuenta Contable requerida (Null) - factura " + factura + "\n");
					} else {
						if (comp.getAddenda().getInformacionPago().getCuentaContable().trim().equals("")) {
							sbError.append("Cuenta Contable requerida - factura " + factura + "\n");
						} else {
							System.out.println("cuenta contable Fin: "
									+ comp.getAddenda().getInformacionPago().getCuentaContable());
						}
					}

					// Centro Costos
					if (comp.getCostCenter() == null) {
						sbError.append("Posicion Centro costos addenda requerido (Null) - factura " + factura + "\n");
					} else if (comp.getCostCenter().isEmpty()){
						sbError.append("Centro costos requerido addenda - factura " + factura + "\n");
					}else{
						String strCentroCostosFin = comp.getCostCenter();
						if(!strCentroCostosFin
								.equals(comp.getAddenda().getInformacionEmision().getCentroCostos())){			                    						
                			sbError.append("Centros costos diferentes - factura " + factura + "\n");
    					}
					}

				} else if (strTipoAddenda.equals("3")) {

					// Numero de contrato
					if (comp.getAddenda().getInmuebles().getNumContrato() == null) {
						sbError.append("Posicion Numero de Contrato requerida (Null) - factura " + factura + "\n");
					} else {
						if (comp.getAddenda().getInmuebles().getNumContrato().trim().equals("")) {
							sbError.append("Numero de Contrato requerido - factura " + factura + "\n");
						} else {
							String strNumeroContratoArr = comp.getAddenda().getInmuebles().getNumContrato();
							if (strNumeroContratoArr.equals("")) {
								sbError.append("Numero de Contrato no encontrado - factura " + factura + "\n");
							}
						}

					}

					// Fecha de vencimiento
					if (comp.getAddenda().getInmuebles().getFechaVencimiento() == null) {
						sbError.append(
								"Posicion Fecha de vencimiento requerida (Null) - factura " + factura + "\n");
					} else {
						if (comp.getAddenda().getInmuebles().getFechaVencimiento().equals("")) {
							sbError.append("Fecha de vencimiento requerida - factura " + factura + "\n");
						} else {
							String strFechaVencimientoArr = comp.getAddenda().getInmuebles().getFechaVencimiento();
							if (strFechaVencimientoArr.equals("")) {
								sbError.append("Fecha de vencimiento requerida - factura " + factura + "\n");
							}
						}

					}

				} else if (strTipoAddenda == null || strTipoAddenda.equals("0") || strTipoAddenda.equals("")) {
					comp.setTipoAddendaCellValue("0");
					comp.getAddenda().getInformacionPago().setEmail("");
					comp.getAddenda().getInformacionPago().setOrdenCompra("");
					

//					if (comp.getAddenda().getInformacionPago().getNombreBeneficiario() == null
//							|| comp.getAddenda().getInformacionPago().getNombreBeneficiario() == "") {
//						sbError.append("Nombre beneficiario requerido - factura " + factura + "\n");
//					}
//					if (comp.getAddenda().getInformacionPago().getInstitucionReceptora() == null
//							|| comp.getAddenda().getInformacionPago().getInstitucionReceptora() == "") {
//						sbError.append("Institucion receptora requerida - factura " + factura + "\n");
//					}
//					if (comp.getAddenda().getInformacionPago().getNumeroCuenta() == null
//							|| comp.getAddenda().getInformacionPago().getNumeroCuenta() == "") {
//						sbError.append("Numero de cuenta requerida - factura " + factura + "\n");
//					}
//					if (comp.getAddenda().getInformacionPago().getNumProveedor() == null
//							|| comp.getAddenda().getInformacionPago().getNumProveedor() == "") {
//						sbError.append("Numero de proveedor requerido - factura " + factura + "\n");
//					}

				} else {
					sbError.append("Tipo de Addenda incorrecto - factura " + factura + "\n");
				}
			} else {
				sbError.append("Tipo de Addenda incorrecto  factura " + factura + "\n");
			}
		} else {
			sbError.append("Tipo de Addenda requerido - factura " + factura + "\n");
		}
	}

	if(!comp.getTipoEmision().equalsIgnoreCase(TipoEmision.DONATARIAS)){
		//AMDA Centro Costos V3.3 Prueba para datos Addenda
		if(comp.getCostCenter() != null ) {
			System.out.println("*********AMDAController Centro Costos Addenda: " + comp.getCostCenter());
			comp.getAddenda().getInformacionEmision().setCentroCostos(comp.getCostCenter());
		}
	}
	
	/* Tipo de operacion */
	if (comp.getTipoEmision().equalsIgnoreCase(TipoEmision.DIVISAS)) {
		if (!comp.getComplemento().getDivisaTipoOperacion().trim().equals("")) {
			if (!(comp.getComplemento().getDivisaTipoOperacion().trim().toLowerCase().equals("compra")
					|| comp.getComplemento().getDivisaTipoOperacion().trim().toLowerCase().equals("venta"))) {
				sbError.append("Tipo de Operacion incorrecto - Factura " + factura + "\n");
			}
		} else {
			sbError.append("Tipo de Operacion requerido - Factura " + factura + "\n");
		}
	}

	/* Valida rfc cliente extranjero */
	if (comp.getReceptor().getRfc() != null) {
		if (comp.getReceptor().getRfc().toUpperCase().equals("XEXX010101000")
				|| comp.getReceptor().getRfc().toUpperCase().equals("XAXX010101000")
				|| comp.getReceptor().getRfc().toUpperCase().equals("XEXE010101000")) {
			if (comp.getReceptor().getDomicilio().getPais() != null) {
				// Residencia fiscal
				Map<String, Object> tipoClaveProdServ = UtilValidationsXML.validPais(tags.mapCatalogos,
						comp.getReceptor().getDomicilio().getPais());
				System.out.println("*********AMDAController Divisas Validando Pais RFC Respuesta: "
						+ tipoClaveProdServ.get("value").toString());
				if (!tipoClaveProdServ.get("value").toString().equalsIgnoreCase("vacio")) {
					comp.getReceptor().setResidenciaFiscal(tipoClaveProdServ.get("value").toString());
					if (comp.getReceptor().getResidenciaFiscal().trim().equalsIgnoreCase("MEX")
							|| comp.getReceptor().getResidenciaFiscal().trim().equalsIgnoreCase("MEXICO")
							|| comp.getReceptor().getResidenciaFiscal().trim().equalsIgnoreCase("México")
							|| comp.getReceptor().getResidenciaFiscal().trim().equalsIgnoreCase("MÉXICO")) {
						sbError.append("El Pais no puede ser Mexico para un RFC Generico Extranjero "
								+ " - factura " + factura + "\n");
					}
				} else {
					sbError.append(tipoClaveProdServ.get("message").toString() + " - factura " + factura + "\n");
				}
				// NumRegIdTrib
				Map<String, Object> tipoRFC = UtilValidationsXML.validRFCNumRegIdTrib(tags.mapCatalogos,
						comp.getReceptor().getRfc(), comp.getReceptor().getResidenciaFiscal(),
						comp.getStrIDExtranjero(), comp.getReceptor().getNombre(),
						comp.getReceptor().getNumRegIdTrib());
				if (!tipoRFC.get("value").toString().equalsIgnoreCase("vacio")) {
					// invoice.setIdExtranjero(tipoRFC.get("value").toString());
					comp.getReceptor().setNumRegIdTrib(tipoRFC.get("value").toString());
				} else {
					sbError.append(tipoRFC.get("message").toString() + " - factura " + factura + "\n");
				}
			}
		}else{
			comp.getReceptor().setNumRegIdTrib("");
			comp.getReceptor().setResidenciaFiscal("");
		}
	}

	if (comp.getTipoEmision().equals(TipoEmision.DONATARIAS)) {
		// fecha de recepcion-donatarias
		if (comp.getFecha() != null && !comp.getFecha().equals("")) {
			if (!validaDatoRE(comp.getFecha(), FECHA_RECEPCION_PATTERN)) {
				sbError.append("La fecha de recepcion debe tener formato DD/MM/AAAA " + " - factura " + factura + "\n");
			}
		} else {
			sbError.append("La fecha de recepcion es requerida " + " - factura " + factura + "\n");
		}
		//Numero de empleado-donatarias
		if (comp.getNumEmpledo() != null && !comp.getNumEmpledo().trim().isEmpty()) {
            if (!validaDatoRELongitud(comp.getNumEmpledo(), RE_CHAR_NUMBER, 50)) {
            	sbError.append("El Numero de Empleado tiene formato incorrecto" + " - factura " + factura + "\n");
            }else{
            	comp.getAddenda().getCampoAdicional().put("NumeroEmpleado", comp.getNumEmpledo());
            }
        }
	}
	
	//No. de Autorizacion
	if(comp.getNoAutorizacion() != null && !comp.getNoAutorizacion().trim().isEmpty()) {
		boolean resVal = UtilCatalogos.validaNumAuth(tags.mapCatalogos, comp.getNoAutorizacion().trim());
		if(!resVal){
			sbError.append( "El campo Numero de Autorizacion no cumple con el patron [0-9a-zA-Z]{5} con 5 caracteres obligatorios" + " - factura " + factura + "\n");
		}
	}
	
	if(comp.getComplemento() != null && comp.getComplemento().getTimbreFiscalDigital() != null){
		//UUID
		System.out.println("uuid: " + comp.getComplemento().getTimbreFiscalDigital().getUuid());
		if(comp.getComplemento().getTimbreFiscalDigital().getUuid() != null 
				&& !comp.getComplemento().getTimbreFiscalDigital().getUuid().trim().isEmpty()) {
			System.out.println("uuid: True");
			boolean resVal = UtilCatalogos.validaCFDIRelacional(tags.mapCatalogos
					, comp.getComplemento().getTimbreFiscalDigital().getUuid().trim());
			if(!resVal){
				sbError.append( "El campo UUID no cumple con el patron [a-f0-9A-F]{8}-[a-f0-9A-F]{4}-[a-f0-9A-F]{4}-[a-f0-9A-F]{4}-[a-f0-9A-F]{12}" + " - factura " + factura + "\n");
			}
		}else{
			System.out.println("uuid: FALSE");
			if(comp.getTipoEmision().equalsIgnoreCase(TipoEmision.RECEPCION_PAGOS)){
				sbError.append( "El campo UUID es obligatorio - factura " + factura + "\n");
			}
		}
		
		//Tipo Relacion
		if (comp.getTipoEmision().equalsIgnoreCase(TipoEmision.RECEPCION_PAGOS)){
			comp.getCfdiRelacionados().setTipoRelacion("04");
		}else{
			if(comp.getCfdiRelacionados() != null && comp.getCfdiRelacionados().getTipoRelacion() != null 
					&& !comp.getCfdiRelacionados().getTipoRelacion().trim().isEmpty()) {
				
				if(comp.getComplemento().getTimbreFiscalDigital().getUuid() != null 
						&& !comp.getComplemento().getTimbreFiscalDigital().getUuid().trim().isEmpty()) {
					if(!comp.getCfdiRelacionados().getTipoRelacion().trim().equalsIgnoreCase("04")){
						String resVal = UtilCatalogos.validaTipoRelacion(tags.mapCatalogos
								, comp.getCfdiRelacionados().getTipoRelacion().trim(), 0);
						if(!resVal.equalsIgnoreCase("No se encontro el Tipo de Relacion en el catalogo c_TipoRelacion")
								&& !resVal.equalsIgnoreCase("Es requerido un Tipo de Relacion ya que el campo UUID tiene informacion")){
							comp.getCfdiRelacionados().setTipoRelacion(resVal);
						}else{
							sbError.append( resVal + " - factura " + factura + "\n");
						}
					}
				}else{
					sbError.append( "Para agregar un Tipo Relacion debe de traer informacion el campo UUID" + " - factura " + factura + "\n");
				}
				
			}else{
				if(comp.getComplemento().getTimbreFiscalDigital().getUuid() != null 
						&& !comp.getComplemento().getTimbreFiscalDigital().getUuid().trim().isEmpty()) {
					sbError.append( "Es requerido un Tipo de Relacion ya que el campo UUID tiene informacion" + " - factura " + factura + "\n");
				}
			}
		}
	}
	
	//Validacion de Conceptos
	int contadorConceptos = 0;
	String tipoFactorValRow = "";
	String impuestoValRow = "";
	boolean fAplicaIVA = false;

	BigDecimal total = new BigDecimal(0.00);
	BigDecimal subtotal = new BigDecimal(0.00);
	BigDecimal totalRetencion = new BigDecimal(0.00);
	BigDecimal totalTraslado = new BigDecimal(0.00);
	BigDecimal importeConcepto;
	BigDecimal importeTrasRet;

	if (comp.getTipoEmision().equalsIgnoreCase(TipoEmision.RECEPCION_PAGOS)) {
		if (comp.getConceptos() != null && comp.getConceptos().size() > 1) {
			sbError.append("Esta emision(Recepcion de pagos) solo admite un concepto - factura " + factura + "\n");
		}
	} else {
		System.out.println("conceptosSize: "+ comp.getConceptos().size());
		if (comp.getConceptos() != null && comp.getConceptos().size() > 0) {
			for (CfdiConcepto concepto : comp.getConceptos()) {
				// validaciones conceptos
				contadorConceptos++;
				tipoFactorValRow = "";
				impuestoValRow = "";
				importeConcepto = new BigDecimal(0.00);

				// tipo clave prodServ
				if (concepto.getClaveProdServ() != null && concepto.getClaveProdServ().length() > 0) {

					Map<String, Object> tipoClaveProdServ = UtilValidationsXML.validClaveProdServ(tags.mapCatalogos,
							concepto.getClaveProdServ());

					if (tipoClaveProdServ.get("value").toString().equalsIgnoreCase("vacio")) {
						sbError.append(tipoClaveProdServ.get("message").toString()
								+ " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
					} else {
						concepto.setClaveProdServ(tipoClaveProdServ.get("value").toString());
					}
				} else {
					sbError.append("ClaveProdServ null o vacio " + " en el Concepto "
							+ contadorConceptos + " - factura " + factura + "\n");
				}

				// Cantidad
				if (concepto.getCantidad() == null) {
					sbError.append("Cantidad null " + " en el Concepto "
							+ contadorConceptos + " - factura " + factura + "\n");
				} else if (!validaDatoRE(concepto.getCantidad().toString(), RE_DECIMAL)) {
					sbError.append("Cantidad con formato incorrecto, se espera ("+RE_DECIMAL+") en el Concepto "
							+ contadorConceptos + " - factura " + factura + "\n");
				}

				// ClaveUnidad
				if (concepto.getClaveUnidad() != null && concepto.getClaveUnidad().length() > 0) {

					Map<String, Object> tipoClaveUnidad = UtilValidationsXML.validClaveUnidad(tags.mapCatalogos,
							concepto.getClaveUnidad());

					if (tipoClaveUnidad.get("value").toString().equalsIgnoreCase("vacio")) {
						sbError.append(tipoClaveUnidad.get("message").toString()
								+ " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
					} else {
						concepto.setClaveUnidad(tipoClaveUnidad.get("value").toString());
					}
				} else {
					sbError.append("ClaveUnidad null o vacia" + " en el Concepto "
							+ contadorConceptos + " - factura " + factura + "\n");
				}

				// UM
				if (concepto.getUnidad() == null || concepto.getUnidad().trim().isEmpty()) {
					sbError.append("UM es requerido " + " en el Concepto "
							+ contadorConceptos + " - factura " + factura + "\n");
				}else{
					if(concepto.getUnidad().trim().length()<=250){
						if (!validaDatoRE(concepto.getUnidad() , RE_CHAR)) {
							sbError.append("UM tiene formato incorrecto "
									+ " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
						}
					}else{
						sbError.append("UM supera el limite de 250 caracteres "
								+ " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
					}
				}

				// Descripcion
				if (concepto.getDescripcion() == null || concepto.getDescripcion().length() <= 0
						|| concepto.getDescripcion().length() > 1000) {
					sbError.append("Concepto descripcion null o vacio "
							+ " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
				}

				// Precio unitario
				if (concepto.getValorUnitario() != null && concepto.getValorUnitario().toString().length() > 0) {

					Map<String, Object> tipoPrecioUnit = UtilValidationsXML.validValorUnitario(tags.mapCatalogos,
							concepto.getValorUnitario().toString(), tags.decimalesMoneda,
							comp.getTipoDeComprobante());

					if (!tipoPrecioUnit.get("value").toString().equalsIgnoreCase("vacio")) {
						// vectorPrecioUnitario.set(contadorConceptos,
						// vectorPrecioUnitario.get(contadorConceptos));
					} else {
						sbError.append(tipoPrecioUnit.get("message").toString()
								+ " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
					}
				} else {
					sbError.append("Precio Unitario null o vacio " + " en el Concepto "
							+ contadorConceptos + " - factura " + factura + "\n");
				}

				try {
					importeConcepto = concepto.getCantidad().multiply(concepto.getValorUnitario());
					// redondear importe concepto
					importeConcepto = new BigDecimal(
							UtilCatalogos.decimales(String.format("%f", importeConcepto), tags.decimalesMoneda));
					concepto.setImporte(importeConcepto);
					subtotal = subtotal.add(importeConcepto);
				} catch (NumberFormatException nfe) {
					System.out.println(nfe.getStackTrace());
					concepto.setImporte(new BigDecimal(0.00));
				}

				// campos de concepto que no estan en donatarias
				if (!comp.getTipoEmision().equalsIgnoreCase(TipoEmision.DONATARIAS)) {

					if (concepto.getImpuestos() != null) {
						if (concepto.getImpuestos().getTraslados() != null
								&& concepto.getImpuestos().getTraslados().size() == 1) {
							trasladoBol = true;
							retencionBol = false;
						} else if (concepto.getImpuestos().getRetenciones() != null
								&& concepto.getImpuestos().getRetenciones().size() == 1) {
							retencionBol = true;
							trasladoBol = false;
						} else {
							retencionBol = false;
							trasladoBol = false;
						}
					} else {
						retencionBol = false;
						trasladoBol = false;
					}

					// Aplica iva
					if (concepto.getAplicaIva() == null) {
						concepto.setAplicaIva("");
					} else if (!concepto.getAplicaIva().equals("1") && !concepto.getAplicaIva().equals("")) {
						sbError.append("APLICA IVA null " + " en el Concepto "
								+ contadorConceptos + " - factura " + factura + "\n");
					} else if (concepto.getAplicaIva().equals("1")) {
						fAplicaIVA = true;
					}

					// Tipo impuesto no se puede validar no se almacena la propiedad(duda)

					String impuestoVal = "";
					String tipoFactorVal = "";
					String tasaOCuotaVal = "";
					// Impuesto
					if (trasladoBol) {
						impuestoVal = concepto.getImpuestos().getTraslados().get(0).getImpuesto();
						tipoFactorVal = concepto.getImpuestos().getTraslados().get(0).getTipoFactor();
						tasaOCuotaVal = concepto.getImpuestos().getTraslados().get(0).getTasaOCuota();
					} else if (retencionBol) {
						impuestoVal = concepto.getImpuestos().getRetenciones().get(0).getImpuesto();
						tipoFactorVal = concepto.getImpuestos().getRetenciones().get(0).getTipoFactor();
						tasaOCuotaVal = concepto.getImpuestos().getRetenciones().get(0).getTasaOCuota();
					}

					if (impuestoVal != null && !impuestoVal.equals("")) {

						String tipoImp = UtilCatalogos.findValClaveImpuesto(tags.mapCatalogos, impuestoVal);

						if (!tipoImp.equalsIgnoreCase("vacio")) {
							impuestoValRow = impuestoVal;
							if (trasladoBol) {
								concepto.getImpuestos().getTraslados().get(0).setImpuesto(tipoImp);
							} else if (retencionBol) {
								concepto.getImpuestos().getRetenciones().get(0).setImpuesto(tipoImp);
							}
						} else {
							sbError.append("No se encotro el Impuesto en el catalogo C_Impuestos " + " en el Concepto "
									+ contadorConceptos + " - factura " + factura + "\n");
						}
					} else {
						sbError.append("Impuesto con null o vacio " + " en el Concepto "
								+ contadorConceptos + " - factura " + factura + "\n");
					}

					// tipo factor
					if (tipoFactorVal != null && !tipoFactorVal.equals("")) {
						if (!tipoFactorVal.equalsIgnoreCase("Exento")
								&& !tipoFactorVal.equalsIgnoreCase("Excento")) {

							Map<String, Object> tipoTipoFact = UtilValidationsXML
									.validTipoFactorTra(tags.mapCatalogos, tipoFactorVal);

							if (!tipoTipoFact.get("value").toString().equalsIgnoreCase("vacio")) {
								tipoFactorValRow = tipoFactorVal;
								if (trasladoBol) {
									concepto.getImpuestos().getTraslados().get(0)
											.setTipoFactor(tipoTipoFact.get("value").toString());
								} else if (retencionBol) {
									concepto.getImpuestos().getRetenciones().get(0)
											.setTipoFactor(tipoTipoFact.get("value").toString());
								}
							} else {
								sbError.append(tipoTipoFact.get("message").toString()
										+ " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
							}
						} else {
							tipoFactorValRow = tipoFactorVal;
						}
					} else {
						sbError.append("El valor del campo TipoFactor que corresponde a Traslado no contiene un valor del catálogo c_TipoFactor "
								+ " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
					}

					// Tasa o cuota
					if (tasaOCuotaVal != null && !tasaOCuotaVal.equals("")) {
						if (!tipoFactorValRow.equalsIgnoreCase("Exento")
								&& !tipoFactorValRow.equalsIgnoreCase("Excento")) {

							Map<String, Object> tipoTasaOCuota = UtilValidationsXML.validTasaOCuotaTra(
									tags.mapCatalogos, impuestoValRow, tipoFactorValRow, tasaOCuotaVal,
									tipoFactorValRow);

							if (!tipoTasaOCuota.get("value").toString().equalsIgnoreCase("vacio")) {
								
								 tasaOCuotaVal = tipoTasaOCuota.get("value").toString();
								 DecimalFormat df = new DecimalFormat("0.000000");
								 String tasaOCuotaValFormat = df.format(Double.parseDouble(tasaOCuotaVal));
								 if (trasladoBol) {
									 concepto.getImpuestos().getTraslados().get(0).setTasaOCuota(tasaOCuotaValFormat);
								 } else if (retencionBol) {
									concepto.getImpuestos().getRetenciones().get(0).setTasaOCuota(tasaOCuotaValFormat);
								 }
								 
							} else {
								sbError.append(tipoTasaOCuota.get("message").toString()
										+ " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
							}
						} else {
							tasaOCuotaVal = "0.00";
							if (trasladoBol) {
								concepto.getImpuestos().getTraslados().get(0).setTasaOCuota("0.000000");
							} else if (retencionBol) {
								concepto.getImpuestos().getRetenciones().get(0).setTasaOCuota("0.000000");
							}
						}

					} else {
						sbError.append("El valor del campo Tasa o Cuota que corresponde a Traslado no contiene un valor del catalogo c_Tasa o Cuota "
								+ " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
					}

					// Base
					String baseVal = "";
					if (tasaOCuotaVal != null && !tasaOCuotaVal.equals("") && concepto.getValorUnitario() != null) {
						
						String baseString  = String.format("%f", concepto.getImporte());
						baseVal = UtilCatalogos.decimales(baseString, tags.decimalesMoneda);
						
						if (trasladoBol) {
							concepto.getImpuestos().getTraslados().get(0)
									.setBase(baseVal);
						} else if (retencionBol) {
							concepto.getImpuestos().getRetenciones().get(0)
									.setBase(baseVal);
						}

						Map<String, Object> tipoBaseTra = UtilValidationsXML.validBaseTra(tags.mapCatalogos,
								concepto.getValorUnitario().toString());

						if (tipoBaseTra.get("value").toString().equalsIgnoreCase("vacio")) {
							baseVal = "";
							sbError.append(tipoBaseTra.get("message").toString()
									+ " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
						}
					} else {
						sbError.append("No se pudo calcular Base para el Impuesto Traslado "
								+ " en el Concepto " + contadorConceptos + " - factura " + factura + "\n");
					}

					// Importe retencion/traslado
					importeTrasRet = new BigDecimal(0.00);
					String importeValStr = "0.00";
					if (!baseVal.equals("") && !tasaOCuotaVal.equals("") && concepto.getCantidad() != null) {
						if (!tipoFactorValRow.equalsIgnoreCase("Exento")
								&& !tipoFactorValRow.equalsIgnoreCase("Excento")) {

							Double importeVal = (Double.valueOf(baseVal) * Double.valueOf(tasaOCuotaVal));

							// redondear importe retencion/traslado
							importeValStr = UtilCatalogos.decimales(String.format("%f", importeVal),
									tags.decimalesMoneda);
							importeTrasRet = new BigDecimal(importeValStr);
						}
					}
					if (trasladoBol) {
						totalTraslado = totalTraslado.add(importeTrasRet);
						concepto.getImpuestos().getTraslados().get(0).setImporte(importeValStr);
					} else if (retencionBol) {
						totalRetencion = totalRetencion.add(importeTrasRet);
						concepto.getImpuestos().getRetenciones().get(0).setImporte(importeValStr);
					}

				}
			}
			// Calcular totales
			String stringDescuento = "";
			System.out.println();
			try {
				if (subtotal.doubleValue() > 0) {
					total = ((subtotal.add(totalTraslado)).subtract(totalRetencion));

				}
				if (comp.getDescuento() != null) {
					stringDescuento = " Descuento: " + comp.getDescuento();
					total = total.subtract(comp.getDescuento());
				}

			} catch (NumberFormatException nfe) {
				System.out.println(nfe.getStackTrace());
			}
			// redondear y asignar totales
			subtotal = new BigDecimal(UtilCatalogos.decimales(String.format("%f", subtotal), tags.decimalesMoneda));
			total = new BigDecimal(UtilCatalogos.decimales(String.format("%f", total), tags.decimalesMoneda));
			System.out.println("Factura(" + factura + ") - Subtotal: " + subtotal + " Total: " + total
					+ " totalTraslado:" + totalTraslado + " totalRetencion: " + totalRetencion + stringDescuento);
			comp.setImpuestos(new CfdiImpuesto());
			comp.getImpuestos().setTotalImpuestosRetenidos(totalRetencion);
			comp.getImpuestos().setTotalImpuestosTrasladados(totalTraslado);
			comp.setSubTotal(subtotal);
			comp.setTotal(total);
		}
	}
	
	if (fErrorIVA && fAplicaIVA)
		sbError.append(sbErrorIVA.toString());
	//Validacion de complemento
	int complementos = 0;
	if(comp.getTipoEmision().equalsIgnoreCase(TipoEmision.RECEPCION_PAGOS)){
		
		//validacion referencia factura
		if (comp.getReferenciaFactura() != null && !comp.getReferenciaFactura().isEmpty()) {
			if (!validaDatoRELongitud(comp.getReferenciaFactura(), REFERENCIA_FACTURA, 50)) {
				sbError.append("Referencia factura  con formato incorrecto - Factura " + factura + "\n");
            }
		}else{
			sbError.append("Referencia factura requerida - factura " + factura + "\n");
		}
		
		if(comp.getComplementPagos() != null && comp.getComplementPagos().size() > 0){
			for(ComplementoPago complementoPago : comp.getComplementPagos()){
				complementos++;
				
				// 2 Fecha de pago 
				if(complementoPago.getFechaPago() ==  null || complementoPago.getFechaPago().toString().trim().length() == 0){
					sbError.append("Posicion fecha vacia o con formato incorrecto, se espera ISO 8601(aaaa-mm-ddThh:mm:ss) - Factura " + factura 
							+ " - Complemento " + complementos + "\n");
				}
				
				/* 3 Forma de pago P */
				if (complementoPago.getFormaPagoP() == null || complementoPago.getFormaPagoP().trim().equals("")) {
					sbError.append(
							"El Campo Forma Pago P. se encuentra (Null,Vacio) no contiene un valor - Factura " + factura
									+ " - Complemento " + complementos + "\n");
				}else{
					Map<String, Object> tipoFormaPago = UtilValidationsXML.validFormaPago(tags.mapCatalogos,
							complementoPago.getFormaPagoP().trim());
					if (!tipoFormaPago.get("value").toString().equalsIgnoreCase("vacio")) {
						complementoPago.setFormaPagoP(tipoFormaPago.get("value").toString());
					} else {
						sbError.append(tipoFormaPago.get("message").toString() + " - Factura" + factura 
								+ " - Complemento " + complementos + "\n");
					}
				}
			
				/* 4 Moneda de pago */
				if ( complementoPago.getMonedaPago() != null && complementoPago.getMonedaPago().trim().length() > 0) {
					Map<String, Object> tipoMon_cp = UtilValidationsXML.validMoneda(tags.mapCatalogos, complementoPago.getMonedaPago());
					if (!tipoMon_cp.get("value").toString().equalsIgnoreCase("vacio")) {
						complementoPago.setMonedaPago(tipoMon_cp.get("value").toString());
						Integer decimalesMonedaP = UtilCatalogos.findDecimalesMoneda(tags.mapCatalogos, complementoPago.getMonedaPago());
						complementoPago.setDecimalesMonedaPago(decimalesMonedaP);
					} else {
						sbError.append(tipoMon_cp.get("message").toString() + " - Factura " + factura 
								+ " - Complemento " + complementos + "\n");
					}
				} else {
					sbError.append("El campo Moneda de pago (Null,Vacio) no contiene un valor del catalogo c_Moneda - Factura " + factura
							+ " - Complemento " + complementos + "\n");
				}
				
				/* 5 Tipo de Cambio Pago */
				if(complementoPago.getMonedaPago().equalsIgnoreCase("MXN")){
					complementoPago.setTipoCambioPago(null);
				}else{
					if(complementoPago.getTipoCambioPago() == null || complementoPago.getTipoCadenaPago().isEmpty()){
						sbError.append("El campo Tipo de Cambio P. no tiene un valor (Null,Vacio) - Factura " + factura
								+ " - Complemento " + complementos + "\n");
					}
				}
				
				/* 6 monto */
				if(complementoPago.getMonto() == null){
					sbError.append("El importe se encuentra (Null,Vacio) no contiene un valor - Factura " + factura
								+ " - Complemento " + complementos + "\n");
				}
				
				/* 7 numero operacion */
				if(complementoPago.getNumeroOperacion() != null || complementoPago.getNumeroOperacion().trim().length() > 0){
					if(complementoPago.getNumeroOperacion().trim().length() < 100){
						if(!validaDatoRE(complementoPago.getNumeroOperacion().trim(), APARTADO_COMPLEMENT)){
							sbError.append("El campo Numero Operacion tiene un formato incorrecto - Factura " + factura
								+ " - Complemento " + complementos + "\n");
						}
					}else{
						sbError.append("la logitud del campo Numero Operación ha pasado el maximo de 100 caracteres. - Factura " + factura
								+ " - Complemento " + complementos + "\n");
					}
				}
				
				/* 8 RFC Emisor Cuenta Orden */
				if(complementoPago.getRfcEmisorCuentaOrden() != null && !complementoPago.getRfcEmisorCuentaOrden().trim().isEmpty()){
					if(complementoPago.getRfcEmisorCuentaOrden().trim().length() <= 13 ){
						if(!validaDatoRE(complementoPago.getRfcEmisorCuentaOrden().trim(),APART_COMPLEMENT_RFC_ACOUTN ) ){
							sbError.append("El campo RFC Emisor Cuenta Orden tiene un formato incorrecto. - Factura " + factura
									+ " - Complemento " + complementos + "\n");
						}
					}else{
						sbError.append("El campo RFC Emisor Cuenta Orden ha pasado el maximo de 13 caracteres. - Factura " + factura
								+ " - Complemento " + complementos + "\n");
					}
				}
				
				/* 9 Nombre banco ordinario Ext*/
				if (comp.getCustomerRfcCellValue().trim().toUpperCase().equals("XEXX010101000")
						|| comp.getCustomerRfcCellValue().trim().toUpperCase().equals("XAXX010101000")
						|| comp.getCustomerRfcCellValue().trim().equals("XEXE010101000")) {
					if(complementoPago.getNombreBancoOrdinarioExt() == null 
							|| complementoPago.getNombreBancoOrdinarioExt().isEmpty()){
						sbError.append("El campo Nombre Banco Ordinario Ext. es requerido para RFC extranjero - Factura " + factura
								+ " - Complemento " + complementos + "\n");
					}else{
						if(complementoPago.getNombreBancoOrdinarioExt().length() <= 300){
							if(!validaDatoRE(complementoPago.getNombreBancoOrdinarioExt().trim(), APARTADO_COMPLEMENT_NBANCOORD )){
								sbError.append("El campo Nombre Banco Ordinario Ext. tiene un formato incorrecto - Factura " + factura
										+ " - Complemento " + complementos + "\n");
							}
						}else{
							sbError.append("El campo Nombre Banco Ordinario Ext. ha superado el maximo de 300 caracteres - Factura " + factura
									+ " - Complemento " + complementos + "\n");
						}
					}
				}
				
				/* 10 cuenta ordenante*/
				if(complementoPago.getCuentaOrdenante() != null && !complementoPago.getCuentaOrdenante().isEmpty()){
					if(complementoPago.getCuentaOrdenante().length() <= 50){
						if(!validaDatoRE(complementoPago.getCuentaOrdenante().trim(), APARTADO_COMPLEMENT_CORDENANTE )){
							sbError.append("El campo Cuenta Ordenante tiene un formato incorrecto - Factura " + factura
									+ " - Complemento " + complementos + "\n");
						}
					}else{
						sbError.append("El campo Cuenta Ordenante ha superado el maximo de 50 caracteres - Factura " + factura
								+ " - Complemento " + complementos + "\n");
					}
				}
				
				/* 11 RFC Emisor Cuenta Beneficiario */
				if(complementoPago.getRfcEmisorCtaBeneficiario() != null && !complementoPago.getRfcEmisorCtaBeneficiario().trim().isEmpty()){
					if(complementoPago.getRfcEmisorCtaBeneficiario().trim().length() <= 13 ){
						// APART_COMPLEMENT_RFC_ACOUTN
						if(!validaDatoRE(complementoPago.getRfcEmisorCtaBeneficiario().trim(),APART_COMPLEMENT_RFC_ACOUTN ) ){
							sbError.append("El campo RFC Emisor Cuenta Beneficiario tiene un formato incorrecto. - Factura " + factura
								+ " - Complemento " + complementos + "\n");
						}
					}else{
						sbError.append("El campo RFC Emisor Cuenta Beneficiario ha pasado el maximo de 13 caracteres - Factura " + factura
								+ " - Complemento " + complementos + "\n");
					}
				}
				
				/* 12 Cuenta beneficiario */
				if(complementoPago.getCuentaBeneficiario() != null && !complementoPago.getCuentaBeneficiario().trim().isEmpty()){
					if(complementoPago.getCuentaBeneficiario().trim().length() <= 50){
						if(!validaDatoRE(complementoPago.getCuentaBeneficiario().trim(), APARTADO_COMPLEMENT_CBENEFICIARIO ) ){
							sbError.append("El campo Cuenta Beneficiario tiene un formato incorrecto. - Factura " + factura
								+ " - Complemento " + complementos + "\n");
						}
					}else{
						sbError.append("El campo Cuenta beneficiario ha pasado el maximo de 50 caracteres. - Factura " + factura
								+ " - Complemento " + complementos + "\n");
					}
				}
				
				/* 13 Tipo Cadena de Pago */
				if(complementoPago.getTipoCadenaPago() != null && !complementoPago.getTipoCadenaPago().trim().isEmpty() ){
						String tipoCadenaP = UtilCatalogos
								.findDescripcionTipoCadenaPagoByDescripcion(tags.mapCatalogos, 
										complementoPago.getTipoCadenaPago().trim());
						
						if(tipoCadenaP.equalsIgnoreCase("vacio")){
							sbError.append("El campo Tipo Cadena de Pago no tiene un valor en el catalogo c_TipocadenaPago. - Factura "
									+ factura + " - Complemento " + complementos + "\n");
						}else{
							complementoPago.setTipoCadenaPago(tipoCadenaP);
						}
				}
				
				/* 14 Cadena de Pago */
				if(complementoPago.getCadenaPago() != null && !complementoPago.getCadenaPago().trim().isEmpty() ){
					if(complementoPago.getCadenaPago().trim().length() > 8192){
						sbError.append("El campo Cadena de Pago supera el limite de 1892 caracteres. - Factura "
								+ factura + " - Complemento " + complementos + "\n");
					}
				}
				
				/* 15 ID Documento */
				if (complementoPago.getIdDocumento() != null && !complementoPago.getIdDocumento().trim().isEmpty()) {
					if(complementoPago.getIdDocumento().trim().length() <= 36){
						if(!validaDatoRE(complementoPago.getIdDocumento().trim(), APARTADO_COMPLEMENT_ID_DOCUMENTO ) ){
							sbError.append("El campo ID Documento tiene un formato incorrecto. - Factura " + factura
								+ " - Complemento " + complementos + "\n");
						}
					}else{
						sbError.append("El campo ID Documento ha pasado el maximo de 36 caracteres. - Factura " + factura
								+ " - Complemento " + complementos + "\n");
					}
				} else {
					sbError.append("El campo ID Documento es requerido. - Factura "
							+ factura + " - Complemento " + complementos + "\n");
				}
				
				/* 16 Serie */
				if (complementoPago.getSeriePago() != null && !complementoPago.getSeriePago().trim().isEmpty()) {
					if(complementoPago.getSeriePago().trim().length() <= 25){
						if(!validaDatoRE(complementoPago.getSeriePago().trim(), APARTADO_COMPLEMENT_SERIE ) ){
							sbError.append("El campo Serie tiene un formato incorrecto. - Factura " + factura
								+ " - Complemento " + complementos + "\n");
						}
					}else{
						sbError.append("El campo Serie ha pasado el maximo de 25 caracteres. - Factura " + factura
								+ " - Complemento " + complementos + "\n");
					}
				}
				
				/* 17 Folio */
				if (complementoPago.getFolioPago() != null && !complementoPago.getFolioPago().trim().isEmpty()) {
					if(complementoPago.getFolioPago().trim().length() <= 40){
						if(!validaDatoRE(complementoPago.getFolioPago().trim(), APARTADO_COMPLEMENT_FOLIO ) ){
							sbError.append("El campo Folio tiene un formato incorrecto. - Factura " + factura
								+ " - Complemento " + complementos + "\n");
						}
					}else{
						sbError.append("El campo Folio ha pasado el maximo de 40 caracteres. - Factura " + factura
								+ " - Complemento " + complementos + "\n");
					}
				}
				
				/* 18 Moneda de DR */
				if ( complementoPago.getMonedaDR() != null && !complementoPago.getMonedaDR().trim().isEmpty()) {
					Map<String, Object> tipoMon_cp = UtilValidationsXML.validMoneda(tags.mapCatalogos, complementoPago.getMonedaDR());
					if (!tipoMon_cp.get("value").toString().equalsIgnoreCase("vacio")) {
						complementoPago.setMonedaDR(tipoMon_cp.get("value").toString());
						Integer decimalesMonedaDR = UtilCatalogos.findDecimalesMoneda(tags.mapCatalogos, complementoPago.getMonedaDR());
						complementoPago.setDecimalesMonedaDr(decimalesMonedaDR);
					} else {
						sbError.append(tipoMon_cp.get("message").toString() + " - Factura " + factura
								+ " - Complemento " + complementos + "\n");
					}
				} else {
					sbError.append("El campo Moneda DR (Null,Vacio) no contiene un valor del catalogo c_Moneda - Factura " + factura
								+ " - Complemento " + complementos + "\n");
				}
				
				/* 19 Tipo de Cambio DR */
				if(complementoPago.getMonedaPago() != null){
					if(complementoPago.getMonedaDR().equalsIgnoreCase("MXN") 
							&& complementoPago.getMonedaDR().equalsIgnoreCase(complementoPago.getMonedaPago())){
						complementoPago.setTipoCambioDR(null);
					}else{
						sbError.append("Tipo de Cambio DR es requerido cuando MonedaDR es diferente a Moneda de Pago. - Factura " + factura
									+ " - Complemento " + complementos + "\n");
					}
				}else{
					complementoPago.setTipoCambioDR(null);
				}
				
				
				/* 20 Metodo de pago DR*/
				if (complementoPago.getMetodoPagoDR() == null || complementoPago.getMetodoPagoDR().trim().isEmpty()) {
					sbError.append(
							"El Campo Metodo Pago DR No Contiene Un Valor Del Catalogo C_MetodoPago. - Factura " + factura
								+ " - Complemento " + complementos + "\n");
				} else {
					Map<String, Object> tipoMetPag = UtilValidationsXML.validMetodPago(tags.mapCatalogos,
							complementoPago.getMetodoPagoDR());
					if (tipoMetPag.get("value").toString().equals("vacio")) {
						sbError.append(tipoMetPag.get("message").toString() +  " - Factura " + factura
								+ " - Complemento " + complementos + "\n");
					} else {
						complementoPago.setMetodoPagoDR(tipoMetPag.get("value").toString());
					}
				}
				
				/* 21 Numero Parcialidad*/
				if(complementoPago.getMetodoPagoDR().equalsIgnoreCase("PPD")){
					if (complementoPago.getNumParcialidad() == null || complementoPago.getNumParcialidad().trim().isEmpty()) {
						sbError.append("El campo Numero Parcialidad es requerido para el metodo de pago \"Pago en parcialidades o diferido\" - Factura " + factura
								+ " - Complemento " + complementos + "\n");
					}else{
						if(!validaDatoRE(complementoPago.getNumParcialidad().trim(), APARTADO_COMPLEMENT_NUM_PARCIALIDAD)){
							sbError.append("El campo Numero Parcialidad tiene un formato incorrecto. - Factura " + factura
								+ " - Complemento " + complementos + "\n");
						}
					}
				}
				
				/* 22 Imp. Saldo Anterior*/
				if(complementoPago.getMetodoPagoDR().equalsIgnoreCase("PPD")){
					if (complementoPago.getImpSaldoAnterior() == null) {
						sbError.append("El campo Imp. Saldo Anterior es requerido para el metodo de pago \"Pago en parcialidades o diferido\" - Factura " + factura
								+ " - Complemento " + complementos + "\n");
					}
				}
				
				/* 23 Impuesto Pagado*/
				if(complementoPago.getTipoCambioDR() != null){
					if (complementoPago.getImpuestoPagado() == null) {
						sbError.append("El campo Impuesto Pagado es requerido - Factura " + factura
								+ " - Complemento " + complementos + "\n");
					}
				}
				
				/* 24 Imp. Saldo Insoluto*/
				if(complementoPago.getMetodoPagoDR().equalsIgnoreCase("PPD")){
					if (complementoPago.getImpSaldoInsoluto() == null) {
						sbError.append("El campo Imp. Saldo Insoluto es requerido para el metodo de pago \"Pago en parcialidades o diferido\" - Factura " + factura
								+ " - Complemento " + complementos + "\n");
					}
				}
				
			}
		}
	}// fin de la evaluacion de recepccion de pagos
	return sbError.toString();
}


}



// TODO: Propiedades en el invoice
// Serie del comprobante
// Uso cfdi
// Moneda
// Tipo de cambio
// rfc
// idextranjero
