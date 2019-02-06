package com.interfactura.firmalocal.xml.util;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.interfactura.firmalocal.datamodel.CfdiAddendaInformacionEmision;
import com.interfactura.firmalocal.datamodel.CfdiAddendaInformacionPago;
import com.interfactura.firmalocal.datamodel.CfdiAddendaInmuebles;
import com.interfactura.firmalocal.datamodel.CfdiAddendaSantanderV1;
import com.interfactura.firmalocal.datamodel.CfdiComplemento;
import com.interfactura.firmalocal.datamodel.CfdiComprobanteFiscal;
import com.interfactura.firmalocal.datamodel.CfdiConcepto;
import com.interfactura.firmalocal.datamodel.CfdiConceptoImpuesto;
import com.interfactura.firmalocal.datamodel.CfdiConceptoImpuestoTipo;
import com.interfactura.firmalocal.datamodel.CfdiDomicilio;
import com.interfactura.firmalocal.datamodel.CfdiEmisor;
import com.interfactura.firmalocal.datamodel.CfdiReceptor;
import com.interfactura.firmalocal.datamodel.CfdiRelacionado;
import com.interfactura.firmalocal.datamodel.CfdiTimbreFiscalDigital;
import com.interfactura.firmalocal.domain.entities.CodigoISO;
import com.interfactura.firmalocal.domain.entities.Customer;
import com.interfactura.firmalocal.domain.entities.FiscalEntity;
import com.interfactura.firmalocal.persistence.CodigoISOManager;
import com.interfactura.firmalocal.persistence.CustomerManager;
import com.interfactura.firmalocal.persistence.FiscalEntityManager;
import com.interfactura.firmalocal.xml.util.ValidationConstants.TipoEmision;

//import oracle.jrockit.jfr.events.DynamicValueDescriptor;

@Service
public class UtilCFDIFormatoUnico {
	@Autowired(required = true)
	private FiscalEntityManager fiscalEntityManager;

	@Autowired(required = true)
	private CustomerManager customerManager;

	@Autowired(required = true)
	private CodigoISOManager codigoISOManager;

	boolean retencionBol = false;
	boolean trasladoBol = false;

	private static final String RE_DECIMAL = "[0-9]+(\\.[0-9][0-9]?[0-9]?[0-9]?)?";
	private static final String RE_MAIL = "^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,3})$";
	private static final String RE_DECIMAL_NEGATIVO = "[\\-]?[0-9]{1,10}(\\.[0-9]{0,4})?";

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

	public CfdiComprobanteFiscal fillComprobanteFU(CfdiComprobanteFiscal comp, Row row, FiscalEntity fiscalEntity,
			int facturasError, Customer customer, int lastCellNum) {
		comp = new CfdiComprobanteFiscal();
		comp.setTipoEmision(TipoEmision.FORMATO_UNICO);
		// Pos 0 Emisor
		comp.setEmisor(new CfdiEmisor());
		if (row.getCell(0) == null) {
			comp.getEmisor().setRfc("");
		} else {
			fiscalEntity = new FiscalEntity();
			fiscalEntity.setTaxID(row.getCell(0).toString());
			fiscalEntity = fiscalEntityManager.findByRFCA(fiscalEntity);
			comp.getEmisor().setRfc(row.getCell(0).toString());
		}

		/* Serie Posicion 1 -- row 1 */
		if (row.getCell(1) == null) {
			comp.setSerie("");
		} else {
			if (row.getCell(1).toString().trim().length() > 0) {
				comp.setSerie(row.getCell(1).toString().trim());
			} else {
				comp.setSerie("");
			}

		}
		// Pos 2

		// Map<String, Object> tipoFormaPago =
		// UtilValidationsXML.validFormaPago(tags.mapCatalogos,row.getCell(2).toString());
		/* Forma de pago */
		if (row.getCell(2) == null || row.getCell(2).toString().trim().equals("")) {
			comp.setFormaPago("");
		} else {
			comp.setFormaPago(row.getCell(2).toString());			
		}

		// pos 3 y 4
		/* Motivo descuento */
		String motivoDesc="";
		if (row.getCell(3) == null) { // Antes 31 ahora 3 AMDA V3.3
			motivoDesc = "";
			System.out.println("row.getCell(31) == null");
		} else {
			motivoDesc = row.getCell(3).toString().trim();
			System.out.println("row.getCell(31) != null");
		}
		comp.setMotivoDescCellValue(motivoDesc);
		
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
								comp.setDescuento(BigDecimal.ZERO);
							} else {
								comp.setDescuento(BigDecimal.ZERO);
							}

						} else {
							comp.setDescuento(BigDecimal.ZERO);
						}
					} else {
						comp.setDescuento(BigDecimal.ZERO);
					}
				} else {
					comp.setDescuento(BigDecimal.ZERO);
				}
			}

		} else {
			if (motivoDesc.length() > 0 && motivoDesc.length() <= 1500) {
				// invoice.setMotivoDescuento(motivoDesc);
				if (row.getCell(4) == null) {
					comp.setDescuento(BigDecimal.ZERO);
				} else {
					if (row.getCell(4).toString().trim().equals("")) {
						comp.setDescuento(BigDecimal.ZERO);
					} else {
						String strCellTypeDesc = checkCellType(row.getCell(4));
						if (!strCellTypeDesc.equals("")) {
							comp.setDescuento(BigDecimal.ZERO);
						} else {
							if (validaDatoRE(row.getCell(4).toString().trim(), RE_DECIMAL_NEGATIVO)) {
								if (Double.parseDouble(row.getCell(4).toString().trim()) > 0) {
									comp.setDescuento(
											new BigDecimal(Double.parseDouble(row.getCell(4).toString().trim())));
								} else {
									comp.setDescuento(BigDecimal.ZERO);
								}
							} else {
								comp.setDescuento(BigDecimal.ZERO);
							}
						}
					}
				}

			} else {
				comp.setDescuento(BigDecimal.ZERO);
			}
		}

		// pos 5 Moneda
		if (row.getCell(5) == null || row.getCell(5).toString().trim().equals("")) {
			comp.setMoneda("");
		} else {
			comp.setMoneda(row.getCell(5).toString().trim());
		}

		// pos 6
		/* Tipo de cambio */
		if (row.getCell(6) == null || row.getCell(6).toString().trim().equals("")) {
			comp.setTipoCambio("");
		} else {
			if (comp.getMoneda() != null) {
				comp.setTipoCambio(row.getCell(6).toString());				
			}else{
				comp.setTipoCambio("");			
			}
		}

		/* Tipo formato posicion 7 -- row 7 */
		if (row.getCell(7) == null) {
			comp.setTipoDeComprobante("I");
		} else {
			comp.setTipoDeComprobante(row.getCell(7).toString());
		}

		// pos 8
		/* Metodo de pago */
		if (row.getCell(8) == null || row.getCell(8).toString().trim().equals("")) {
			comp.setMetodoPago("");
		} else {
			comp.setMetodoPago(row.getCell(8).toString());
		}

		// pos 9
		/* Regimen fiscal */
		if (row.getCell(9) == null || row.getCell(9).toString().trim().equals("")) {
			comp.getEmisor().setRegimenFiscal("");
		} else {
			comp.getEmisor().setRegimenFiscal(row.getCell(9).toString());
		}

		// pos 10, 11, 12 y 13
		/* RFC del cliente */
		boolean readFromFile = false;
		comp.setReceptor(new CfdiReceptor());
		if (row.getCell(10) == null) {
			comp.setCustomerRfcCellValue("");
		} else {
			if (row.getCell(10).toString().trim().equals("")) {
				comp.setCustomerRfcCellValue("");
			} else {
				comp.setCustomerRfcCellValue(row.getCell(10).toString().trim());
				if (row.getCell(10).toString().trim().toUpperCase().equals("XEXX010101000")
						|| row.getCell(10).toString().trim().toUpperCase().equals("XAXX010101000")
						|| row.getCell(10).toString().trim().equals("XEXE010101000")) {

					// evaluacion del id de extranjero
					if (row.getCell(11) == null || row.getCell(11).toString().trim().length() == 0) {
						comp.setStrIDExtranjero("");
						readFromFile= true;
					} else {
						String strCellType = checkCellType(row.getCell(11));
						if (!strCellType.equals("")) {
							comp.setReceptor(new CfdiReceptor());
							readFromFile= true;
						} else {
							String strIDExtranjero = row.getCell(11).toString().trim();
							comp.setStrIDExtranjero(strIDExtranjero);
							System.out.println("ID Extranjero: " + strIDExtranjero);
							customer = customerManager.findByIdExtranjero(strIDExtranjero);
							comp.setReceptor(new CfdiReceptor());
							if (customer != null) {
								//rfc
								comp.getReceptor().setRfc(customer.getTaxId());
								//nombre
								comp.getReceptor().setNombre(customer.getPhysicalName());
								//NumRegIdTrib
								if (row.getCell(13) == null || row.getCell(13).toString().trim().equals("")) {
									comp.getReceptor().setNumRegIdTrib("");
								} else {
									comp.getReceptor().setNumRegIdTrib(row.getCell(13).toString());
								}
								//uso cfdi
								if (row.getCell(12) == null || row.getCell(12).toString().trim().length() == 0) {
									comp.getReceptor().setUsoCFDI("D04");
								} else {
									comp.getReceptor().setUsoCFDI(row.getCell(12).toString());
								}
								//domicilio
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
							}else{
								readFromFile = true;
							}
						}
					}
				} else {
					if (fiscalEntity != null) {
						customer = customerManager.get(row.getCell(10).toString().trim(),
								String.valueOf(fiscalEntity.getId()));
						comp.setReceptor(new CfdiReceptor());
						if (customer != null) {
							//rfc
							comp.getReceptor().setRfc(customer.getTaxId());
							//nombre
							comp.getReceptor().setNombre(customer.getPhysicalName());
							//NumRegIdTrib
							if (row.getCell(13) == null || row.getCell(13).toString().trim().equals("")) {
								comp.getReceptor().setNumRegIdTrib("");
							} else {
								comp.getReceptor().setNumRegIdTrib(row.getCell(13).toString());
							}
							//uso cfdi
							if (row.getCell(12) == null || row.getCell(12).toString().trim().length() == 0) {
								comp.getReceptor().setUsoCFDI("D04");
							} else {
								comp.getReceptor().setUsoCFDI(row.getCell(12).toString());
							}
							//domicilio
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
						}else{
							readFromFile = true;
						}
					}

				}
			}

		}
		
		// pos 35-44
		if (readFromFile) {
			comp.setReceptor(new CfdiReceptor());
			comp.getReceptor().setDomicilio(new CfdiDomicilio());
			//rfc
			comp.getReceptor().setRfc(comp.getCustomerRfcCellValue());
			//nombre cliente
			if (row.getCell(35) == null || row.getCell(35).toString().trim().equals("")) {
				comp.getReceptor().setNombre("");
			} else {
				comp.getReceptor().setNombre(row.getCell(35).toString().trim());
			}
			//usoCfdi
			if (row.getCell(12) == null || row.getCell(12).toString().trim().length() == 0) {
				comp.getReceptor().setUsoCFDI("D04");
			} else {
				comp.getReceptor().setUsoCFDI(row.getCell(12).toString());
			}
			//setNumRegIdTrib
			if (row.getCell(13) == null || row.getCell(13).toString().trim().equals("")) {
				comp.getReceptor().setNumRegIdTrib("");
			} else {
				comp.getReceptor().setNumRegIdTrib(row.getCell(13).toString());
			}
			
			//calle
			if (row.getCell(36) == null || row.getCell(36).toString().trim().equals("")) {
				comp.getReceptor().getDomicilio().setCalle("");
			} else {
				comp.getReceptor().getDomicilio().setCalle(row.getCell(36).toString().trim());
			}
			//NoExterior
			if (row.getCell(37) == null || row.getCell(37).toString().trim().equals("")) {
				comp.getReceptor().getDomicilio().setNoExterior("");
			} else {
				comp.getReceptor().getDomicilio().setNoExterior(row.getCell(37).toString().trim());
			}
			//NoInterior
			if (row.getCell(38) == null || row.getCell(38).toString().trim().equals("")) {
				comp.getReceptor().getDomicilio().setNoInterior("");
			} else {
				comp.getReceptor().getDomicilio().setNoInterior(row.getCell(38).toString().trim());
			}
			//Colonia
			if (row.getCell(39) == null || row.getCell(39).toString().trim().equals("")) {
				comp.getReceptor().getDomicilio().setColonia("");
			} else {
				comp.getReceptor().getDomicilio().setColonia(row.getCell(39).toString().trim());
			}
			//Codigo postal
			if (row.getCell(40) == null || row.getCell(40).toString().trim().equals("")) {
				comp.getReceptor().getDomicilio().setCodigoPostal("");
			} else {
				comp.getReceptor().getDomicilio().setCodigoPostal(row.getCell(40).toString().trim());
			}
			//localidad
			if (row.getCell(41) == null || row.getCell(41).toString().trim().equals("")) {
				comp.getReceptor().getDomicilio().setLocalidad("");
			} else {
				comp.getReceptor().getDomicilio().setLocalidad(row.getCell(41).toString().trim());
			}
			//Municipio
			if (row.getCell(42) == null || row.getCell(42).toString().trim().equals("")) {
				comp.getReceptor().getDomicilio().setMunicipio("");
			} else {
				comp.getReceptor().getDomicilio().setMunicipio(row.getCell(42).toString().trim());
			}
			//Estado
			if (row.getCell(43) == null || row.getCell(43).toString().trim().equals("")) {
				comp.getReceptor().getDomicilio().setEstado("");
			} else {
				comp.getReceptor().getDomicilio().setEstado(row.getCell(43).toString().trim());
			}
			//Pais
			if (row.getCell(44) == null || row.getCell(44).toString().trim().equals("")) {
				comp.getReceptor().getDomicilio().setPais("");
			} else {
				comp.getReceptor().getDomicilio().setPais(row.getCell(44).toString().trim());
			}
			/* Referencia */
			if (row.getCell(15) == null || row.getCell(15).toString().trim().equals("")) {
				comp.getReceptor().getDomicilio().setReferencia("");
			} else {
				comp.getReceptor().getDomicilio().setReferencia(row.getCell(15).toString().trim());
			}
		}
		
		comp.setAddenda(new CfdiAddendaSantanderV1());
		comp.getAddenda().setInformacionPago(new CfdiAddendaInformacionPago());
		// pos 14
		/* Numero de cuenta */
		if (row.getCell(14) == null || row.getCell(14).toString().trim().equals("")) {
			comp.setNumeroCuenta("");
		} else {			
			String strCellType = checkCellType(row.getCell(14));
			if (!strCellType.equals("")) {
				comp.setNumeroCuenta("");
			} else {
				String strNumCtaPago = row.getCell(14).toString();
				comp.setNumeroCuenta(strNumCtaPago);
			}	

		}
		
		comp.getAddenda().setInformacionEmision(new CfdiAddendaInformacionEmision());
		// pos 16
		//codigo cliente
		if (row.getCell(16) == null || row.getCell(16).toString().trim().equals("")) {
			comp.getAddenda().getInformacionEmision().setCodigoCliente("");
		} else {
			String strCellType = checkCellType(row.getCell(16));
			if (!strCellType.equals("")) {
				comp.getAddenda().getInformacionEmision().setCodigoCliente("");
			} else {
				if (row.getCell(16).toString().trim().length() > 0) {
					String strCodigoCliente = row.getCell(16).toString();
					comp.getAddenda().getInformacionEmision().setCodigoCliente(strCodigoCliente);
				} else {
					comp.getAddenda().getInformacionEmision().setCodigoCliente(row.getCell(16).toString().trim());
			
				}
			}
		}
		
		// pos 17
		/* Contrato */
		if (row.getCell(17) == null) {
			comp.getAddenda().getInformacionEmision().setContrato("");
		} else {
			if (row.getCell(17).toString().trim().equals("")) {
				comp.getAddenda().getInformacionEmision().setContrato("");
			} else {
				String strCellType = checkCellType(row.getCell(17));
				if (!strCellType.equals("")) {
					comp.getAddenda().getInformacionEmision().setContrato("");
				} else {
					if (row.getCell(17).toString().trim().length() > 0) {
						String strContrato = row.getCell(17).toString();
						comp.getAddenda().getInformacionEmision().setContrato(strContrato);
					} else {
						comp.getAddenda().getInformacionEmision().setContrato(row.getCell(17).toString().trim());
					}
				}
			}
		}
		// pos 18
		/* Periodo */
		if (row.getCell(18) == null) {
			comp.getAddenda().getInformacionEmision().setPeriodo("");
		} else {
			if (row.getCell(18).toString().trim().equals("")) {
				comp.getAddenda().getInformacionEmision().setPeriodo("");
			} else {
				String strCellType = checkCellType(row.getCell(18));
				if (!strCellType.equals("")) {
					comp.getAddenda().getInformacionEmision().setPeriodo("");
				} else {
					if (row.getCell(18).toString().trim().length() > 0) {
						String strPeriodo = row.getCell(18).toString();
						comp.getAddenda().getInformacionEmision().setPeriodo(strPeriodo);
					} else {
						comp.getAddenda().getInformacionEmision().setPeriodo(row.getCell(18).toString().trim());
					}
				}
			}
		}
		
		// pos 19
		/* Centro de costos */
		if (row.getCell(19) == null) {
			comp.getAddenda().getInformacionEmision().setCentroCostos("");
		} else {
			if (row.getCell(19).toString().trim().equals("")) {
				comp.getAddenda().getInformacionEmision().setCentroCostos("");
			} else {
				String strCellType = checkCellType(row.getCell(19));
				if (!strCellType.equals("")) {
					comp.getAddenda().getInformacionEmision().setCentroCostos("");
				} else {
					if (row.getCell(19).toString().trim().length() > 0) {
						String strCentroCostos = row.getCell(19).toString();
						comp.getAddenda().getInformacionEmision().setCentroCostos(strCentroCostos);
					} else {
						comp.getAddenda().getInformacionEmision().setCentroCostos(row.getCell(19).toString().trim());
					}
				}
			}
		}

		comp.getAddenda().setCampoAdicional(new HashMap<String, String>());
		// pos 20
		/* Descriptcion concepto */
		if (row.getCell(20) == null) {
			comp.getAddenda().getCampoAdicional().put("DESCRIPCIÓN CONCEPTO", "");
		} else {
			comp.getAddenda().getCampoAdicional().put("DESCRIPCIÓN CONCEPTO", row.getCell(20).toString().trim());
		}
		
		// pos 21
		/* Iva */
		comp.setIvaCellValue(row.getCell(21).toString().trim());

		// pos 22-34
		comp.getAddenda().setInmuebles(new CfdiAddendaInmuebles());
		if (row.getCell(22) != null) {
			if (row.getCell(22).toString().contains(".")) {
				System.out.println("*** response Dentro IF AMDA: " + row.getCell(22).toString());
				String words[] = row.getCell(22).toString().split("\\.");
				row.getCell(22).setCellValue(words[0]);
				System.out.println("*** response Dentro IF despues AMDA: " + row.getCell(22).toString());
			}
			comp.setTipoAddendaCellValue(row.getCell(22).toString());
			System.out.println("tipoAddenda:" + row.getCell(22).toString());

			if (validaDatoRE(row.getCell(22).toString().trim(), RE_DECIMAL)) {
				String strTipoAddenda = row.getCell(22).toString();
				System.out.println("tipoAddendaClean: " + strTipoAddenda);
				if (strTipoAddenda.equals("1") || strTipoAddenda.equals("2") || strTipoAddenda.equals("3")) {
					comp.getAddenda().getInformacionPago().setNombreBeneficiario("");
					comp.getAddenda().getInformacionPago().setInstitucionReceptora("");
					comp.getAddenda().getInformacionPago().setNumeroCuenta("");
					comp.getAddenda().getInformacionPago().setNumProveedor("");
					
					//email
					if (row.getCell(23) == null) {
						comp.getAddenda().getInformacionPago().setEmail("");
					} else {
						if (!row.getCell(23).toString().trim().equals("")) {
							if (validaDatoRE(row.getCell(23).toString().trim(), RE_MAIL)) {
								comp.getAddenda().getInformacionPago().setEmail(row.getCell(23).toString().trim());
							} else {
								comp.getAddenda().getInformacionPago().setEmail("");
							}
						} else {
							comp.getAddenda().getInformacionPago().setEmail("");
						}
					}
					//codigo iso moneda
					if (row.getCell(24) == null) {
						comp.getAddenda().getInformacionPago().setCodigoISOMoneda("");
					} else {
						if (!row.getCell(24).toString().trim().equals("")) {
							comp.getAddenda().getInformacionPago()
								.setCodigoISOMoneda(row.getCell(24).toString().trim().toUpperCase());
						} else {
							comp.getAddenda().getInformacionPago().setCodigoISOMoneda("");
						}
					}
				}

				if (strTipoAddenda.equals("1")) {
					//orden compra
					if (row.getCell(25) == null) {
						comp.getAddenda().getInformacionPago().setOrdenCompra("");
					} else {
						if (row.getCell(25).toString().trim().equals("")) {
							comp.getAddenda().getInformacionPago().setOrdenCompra("");
						} else {
							comp.getAddenda().getInformacionPago().setOrdenCompra(row.getCell(25).toString().trim());
//							String strCellTypeOrdenLog = checkCellType(row.getCell(25));
//							if (!strCellTypeOrdenLog.equals("")) {
//								comp.getAddenda().getInformacionPago().setOrdenCompra("");
//							} else {
//								String strOrdenCompra = row.getCell(25).toString();
//								if (!strOrdenCompra.equals("")) {
//									comp.getAddenda().getInformacionPago().setOrdenCompra(strOrdenCompra);
//									System.out.println("orden compra log: " + strOrdenCompra);
//								} else {
//									comp.getAddenda().getInformacionPago().setOrdenCompra("");
//								}
//							}
						}
					}
					//posicion compra
					if (row.getCell(26) == null) {
						comp.getAddenda().getInformacionPago().setPosCompra("");
					} else {
						if (row.getCell(26).toString().trim().equals("")) {
							comp.getAddenda().getInformacionPago().setPosCompra("");
						} else {
							comp.getAddenda().getInformacionPago().setPosCompra(row.getCell(26).toString().trim());
//							String strCellTypePosicionLog = checkCellType(row.getCell(26));
//							if (!strCellTypePosicionLog.equals("")) {
//								comp.getAddenda().getInformacionPago().setPosCompra("");
//							} else {
//								String strPosicionCompra = row.getCell(26).toString();
//								if (!strPosicionCompra.equals("")) {
//									comp.getAddenda().getInformacionPago().setPosCompra(strPosicionCompra);
//									System.out.println("posicion compra: " + strPosicionCompra);
//								} else {
//									comp.getAddenda().getInformacionPago().setPosCompra("");
//								}
//							}
						}
					}

				} else if (strTipoAddenda.equals("2")) {
					//cuenta contable
					if (row.getCell(27) == null) {
						comp.getAddenda().getInformacionPago().setCuentaContable(null);
					} else {
						if (row.getCell(27).toString().trim().equals("")) {
							comp.getAddenda().getInformacionPago().setCuentaContable("");
						} else {
							comp.getAddenda().getInformacionPago().setCuentaContable(row.getCell(27).toString().trim());
//							String strCellTypeContableFin = checkCellType(row.getCell(27));
//							if (!strCellTypeContableFin.equals("")) {
//								comp.getAddenda().getInformacionPago().setCuentaContable("");
//							} else {
//								String strCuentaContableFin = row.getCell(27).toString();
//								if (!strCuentaContableFin.equals("")) {
//									comp.getAddenda().getInformacionPago().setCuentaContable(strCuentaContableFin);
//									System.out.println("cuenta contable Fin: " + strCuentaContableFin);
//								} else {
//									comp.getAddenda().getInformacionPago().setCuentaContable("");
//								}
//							}
						}
					}
					//centro de costos
					if (row.getCell(28) == null) {
						comp.getAddenda().getInformacionEmision().setCentroCostos("");
					} else {
						if (row.getCell(28).toString().trim().equals("")) {
							comp.getAddenda().getInformacionEmision().setCentroCostos("");
						} else {
							comp.getAddenda().getInformacionEmision().setCentroCostos(row.getCell(28).toString().trim());
//							String strCellTypeCostosFin = checkCellType(row.getCell(28));
//							if (!strCellTypeCostosFin.equals("")) {
//								comp.getAddenda().getInformacionEmision().setCentroCostos("");
//							} else {
//								if (!row.getCell(28).toString().trim().equals("")) {
//									System.out.println("centro costos: " + row.getCell(28).toString());
//									String strCentroCostosFin = row.getCell(28).toString();
//									if (!strCentroCostosFin.equals(comp.getAddenda().getInformacionEmision().getCentroCostos())) {
//										comp.getAddenda().getInformacionEmision().setCentroCostos("");
//									}
//								} else {
//									comp.getAddenda().getInformacionEmision().setCentroCostos("");
//								}
//							}
						}
					}

				} else if (strTipoAddenda.equals("3")) {
					//num contrato
					if (row.getCell(29) == null) {
						comp.getAddenda().getInmuebles().setNumContrato("");
					} else {
						if (row.getCell(29).toString().trim().equals("")) {
							comp.getAddenda().getInmuebles().setNumContrato("");
						} else {
							comp.getAddenda().getInmuebles().setNumContrato(row.getCell(29).toString().trim());
//							String strCellTypeContratoArr = checkCellType(row.getCell(29));
//							if (!strCellTypeContratoArr.equals("")) {
//								comp.getAddenda().getInmuebles().setNumContrato("");
//							} else {
//								String strNumeroContratoArr = row.getCell(29).toString();
//								if (!strNumeroContratoArr.equals("")) {
//									comp.getAddenda().getInmuebles().setNumContrato(strNumeroContratoArr);
//								} else {
//									comp.getAddenda().getInmuebles().setNumContrato("");
//								}
//							}
						}
					}
					//fecha vencimiento
					if (row.getCell(30) == null) {
						comp.getAddenda().getInmuebles().setFechaVencimiento("");
					} else {
						if (row.getCell(30).toString().trim().equals("")) {
							comp.getAddenda().getInmuebles().setFechaVencimiento("");
						} else {
							comp.getAddenda().getInmuebles().setFechaVencimiento(row.getCell(30).toString().trim());
//							String strCellTypeVencimientoArr = checkCellType(row.getCell(30));
//							if (!strCellTypeVencimientoArr.equals("")) {
//								comp.getAddenda().getInmuebles().setFechaVencimiento("");
//							} else {
//								String strFechaVencimientoArr = row.getCell(30).toString();
//								if (!strFechaVencimientoArr.equals("")) {
//									comp.getAddenda().getInmuebles().setFechaVencimiento(strFechaVencimientoArr);
//									System.out.println("fecha vencimiento Arr:" + strFechaVencimientoArr);
//								} else {
//									comp.getAddenda().getInmuebles().setFechaVencimiento("");
//								}
//							}
						}

					}

				} else if (strTipoAddenda.equals("0") || strTipoAddenda.equals("")) {
					comp.getAddenda().getInformacionPago().setEmail("");
					comp.getAddenda().getInformacionPago().setOrdenCompra("");
					//Nombre beneficiario
					if (row.getCell(31) == null) {
						comp.getAddenda().getInformacionPago().setNombreBeneficiario("");
					} else {
						comp.getAddenda().getInformacionPago().setNombreBeneficiario(row.getCell(32).toString().trim());
					}
					//institucion receptora
					if (row.getCell(32) == null) {
						comp.getAddenda().getInformacionPago().setInstitucionReceptora("");
					} else {
						comp.getAddenda().getInformacionPago()
								.setInstitucionReceptora(row.getCell(32).toString().trim());
					}
					//numero de cuenta
					if (row.getCell(33) == null) {
						comp.getAddenda().getInformacionPago().setNumeroCuenta("");
					} else {
						if (row.getCell(33).toString().trim().equals("")) {
							comp.getAddenda().getInformacionPago().setNumeroCuenta(row.getCell(33).toString().trim());
						} else {
							comp.getAddenda().getInformacionPago().setNumeroCuenta(row.getCell(33).toString().trim());
//							String strCellTypeC = checkCellType(row.getCell(33));
//							if (!strCellTypeC.equals("")) {
//								comp.getAddenda().getInformacionPago().setNumeroCuenta("");
//							} else {
//								comp.getAddenda().getInformacionPago()
//										.setNumeroCuenta(row.getCell(33).toString().trim());
//							}
						}
					}
					//num proveedor
					if (row.getCell(34) == null) {
						comp.getAddenda().getInformacionPago().setNumProveedor("");
					} else {
						if (row.getCell(34).toString().trim().equals("")) {
							comp.getAddenda().getInformacionPago().setNumProveedor(row.getCell(34).toString().trim());
						} else {
							comp.getAddenda().getInformacionPago().setNumProveedor(row.getCell(34).toString().trim());
//							String strCellTypeP = checkCellType(row.getCell(34));
//							if (!strCellTypeP.equals("")) {
//								comp.getAddenda().getInformacionPago().setNumProveedor("");
//							} else {
//								comp.getAddenda().getInformacionPago()
//										.setNumProveedor(row.getCell(34).toString().trim());
//							}
						}
					}
				}
			}
		}

		// pos 45
		if (row.getCell(45) == null) {
			comp.setNoAutorizacion("");
		} else {
			comp.setNoAutorizacion(row.getCell(45).toString().trim());
		}

		// pos 46 y 47
		comp.setComplemento(new CfdiComplemento());
		comp.getComplemento().setTimbreFiscalDigital(new CfdiTimbreFiscalDigital());
		if (row.getCell(46) == null) {

			comp.getComplemento().getTimbreFiscalDigital().setUuid("");
		} else {
			comp.getComplemento().getTimbreFiscalDigital().setUuid(row.getCell(46).toString().trim());
			comp.setCfdiRelacionados(new CfdiRelacionado());
			if (row.getCell(47) == null) {

				comp.getCfdiRelacionados().setTipoRelacion("");
			} else {
				comp.getCfdiRelacionados().setTipoRelacion(row.getCell(47).toString().trim());
			}
		}

		// pos 48
		int posicion = 48;
		int contadorConceptos = 0;
		boolean fPermisoVector = true;
		boolean fFinFactura = false;
		Integer numeroCelda = 0;

		String tipoFactorValRow = "";

		List<CfdiConcepto> conceptos =  new ArrayList<CfdiConcepto>();
		
		CfdiConcepto cfdi = null;
		CfdiConceptoImpuestoTipo cImpuestoTipo = null;
		CfdiConceptoImpuesto impuestos = null;
		
		while (posicion < lastCellNum && !fFinFactura) {
			if(numeroCelda==0){
				cfdi = new CfdiConcepto();
				impuestos = new CfdiConceptoImpuesto();
				cImpuestoTipo = new CfdiConceptoImpuestoTipo();
				cfdi.setImpuestos(impuestos);
			}
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
						// new vvector added vectorTipoImpuesto
						//cfdi.setTipoImpuesto("Traslado");

						trasladoBol = true;
						retencionBol = false;
					} else if (row.getCell(posicion).toString().equalsIgnoreCase("Retencion")) {
						// new vvector added vectorTipoImpuesto
						//cfdi.setTipoImpuesto("Retención");
						retencionBol = true;
						trasladoBol = false;
					} else {
						fPermisoVector = false;

					}
					posicion = posicion - 7;
					numeroCelda = numeroCelda - 7;
				}

			}

			if (trasladoBol) {
				if (numeroCelda == 1) {
					if (row.getCell(posicion) == null || row.getCell(posicion).toString().trim().equals("")) {
						fPermisoVector = false;
						cfdi.setClaveProdServ("");
					} else {
						cfdi.setClaveProdServ(row.getCell(posicion).toString().trim());
					}
				}

				if (numeroCelda == 2) {
					if (row.getCell(posicion) == null || row.getCell(posicion).toString().trim().equals("")) {
						fPermisoVector = false;
						cfdi.setCantidad(BigDecimal.ZERO);
					} else {
						cfdi.setCantidad(new BigDecimal(row.getCell(posicion).toString().trim()));
					}
				}

				if (numeroCelda == 3) {
					if (row.getCell(posicion) == null || row.getCell(posicion).toString().trim().equals("")) {
						fPermisoVector = false;
						cfdi.setClaveUnidad("");
					} else {
						cfdi.setClaveUnidad(row.getCell(posicion).toString().trim());
					}
				}

				if (numeroCelda == 4) {
					if (row.getCell(posicion) == null || row.getCell(posicion).toString().trim().equals("")) {
						fPermisoVector = false;
						cfdi.setUnidad("");
					} else {
						cfdi.setUnidad(row.getCell(posicion).toString().trim());
					}
				}

				if (numeroCelda == 5) {
					if (row.getCell(posicion) == null || row.getCell(posicion).toString().trim().equals("")) {
						fPermisoVector = false;
						cfdi.setDescripcion("");
					} else {
						cfdi.setDescripcion(row.getCell(posicion).toString().trim());
					}
				}

				if (numeroCelda == 6) {
					if (row.getCell(posicion) == null || row.getCell(posicion).toString().trim().equals("")) {
						fPermisoVector = false;
						cfdi.setValorUnitario(BigDecimal.ZERO);
					}else {
						cfdi.setValorUnitario(new BigDecimal(row.getCell(posicion).toString().trim()));
					}
				}

				if (numeroCelda == 7) {
					if (row.getCell(posicion).toString().equals("1")) {
						if (fPermisoVector)
							cfdi.setAplicaIva("1");
					} else {
						fPermisoVector = false;
						cfdi.setAplicaIva("");
					}
				}

				if (numeroCelda == 9) {
					if (row.getCell(posicion) == null || row.getCell(posicion).toString().trim().equals("")) {
						fPermisoVector = false;
						cImpuestoTipo.setImpuesto("");
					} else {
						cImpuestoTipo.setImpuesto(row.getCell(posicion).toString().trim());
					}
				}
				
				if (numeroCelda == 10) {
					if (row.getCell(posicion) == null || row.getCell(posicion).toString().trim().equals("")) {
						cImpuestoTipo.setTipoFactor("");
					} else {
						tipoFactorValRow = row.getCell(posicion).toString();
						cImpuestoTipo.setTipoFactor(tipoFactorValRow);
					}
				}

				if (numeroCelda == 11) {
					if (row.getCell(posicion) == null || row.getCell(posicion).toString().trim().equals("")) {
						cImpuestoTipo.setTasaOCuota("");
					} else {
						if (!tipoFactorValRow.equalsIgnoreCase("Exento")
								&& !tipoFactorValRow.equalsIgnoreCase("Excento")) {
							cImpuestoTipo.setTasaOCuota(row.getCell(posicion).toString());
						} else {
							cImpuestoTipo.setTasaOCuota("0.0");
						}
					}
					
					numeroCelda = 0;
					if(cfdi != null){
						if(cImpuestoTipo != null && impuestos != null){
							List<CfdiConceptoImpuestoTipo> traslados =  new ArrayList<CfdiConceptoImpuestoTipo>();
							traslados.add(cImpuestoTipo);
							impuestos.setTraslados(traslados);
							cfdi.setImpuestos(impuestos);
						}
						conceptos.add(cfdi);
					}
				}

			} else if (retencionBol) {

				if (numeroCelda == 1) {
					if (row.getCell(posicion) == null || row.getCell(posicion).toString().trim().equals("")) {
						fPermisoVector = false;
						cfdi.setClaveProdServ("");
					} else {
						cfdi.setClaveProdServ(row.getCell(posicion).toString().trim());
					}
				}

				if (numeroCelda == 2) {
					if (row.getCell(posicion) == null || row.getCell(posicion).toString().trim().equals("")) {
						fPermisoVector = false;
						cfdi.setCantidad(BigDecimal.ZERO);
					} else {
						cfdi.setCantidad(new BigDecimal(row.getCell(posicion).toString().trim()));
					}
				}

				if (numeroCelda == 3) {
					if (row.getCell(posicion) == null || row.getCell(posicion).toString().trim().equals("")) {
						fPermisoVector = false;
						cfdi.setClaveUnidad("");
					} else {
						cfdi.setClaveUnidad(row.getCell(posicion).toString().trim());
					}
				}

				if (numeroCelda == 4) {
					if (row.getCell(posicion) == null || row.getCell(posicion).toString().trim().equals("")) {
						fPermisoVector = false;
						cfdi.setUnidad("");
					} else {
						cfdi.setUnidad(row.getCell(posicion).toString().trim());
					}
				}

				if (numeroCelda == 5) {
					if (row.getCell(posicion) == null || row.getCell(posicion).toString().trim().equals("")) {
						fPermisoVector = false;
						cfdi.setDescripcion("");
					} else {
						cfdi.setDescripcion(row.getCell(posicion).toString().trim());
					}
				}

				if (numeroCelda == 6) {
					if (row.getCell(posicion) == null || row.getCell(posicion).toString().trim().equals("")) {
						fPermisoVector = false;
						cfdi.setValorUnitario(BigDecimal.ZERO);
					}
					else {
						cfdi.setValorUnitario(new BigDecimal(row.getCell(posicion).toString().trim()));
					}
				}

				if (numeroCelda == 7) {
					if (row.getCell(posicion).toString().equals("1")) {
						if (fPermisoVector)
							cfdi.setAplicaIva("1");
					} else {
						fPermisoVector = false;
						cfdi.setAplicaIva("");
					}
				}
				
				if (numeroCelda == 9) {
					if (row.getCell(posicion) == null || row.getCell(posicion).toString().trim().equals("")) {
						cImpuestoTipo.setImpuesto("");
					} else {
						cImpuestoTipo.setImpuesto(row.getCell(posicion).toString());
					}
				}
				
				if (numeroCelda == 10) {
					if (row.getCell(posicion) == null || row.getCell(posicion).toString().trim().equals("")) {
						fPermisoVector = false;
						cImpuestoTipo.setTipoFactor("");
					} else {
						cImpuestoTipo.setTipoFactor(row.getCell(posicion).toString());
					}
				}

				if (numeroCelda == 11) {
					if (row.getCell(posicion) == null || row.getCell(posicion).toString().trim().equals("")) {
						fPermisoVector = false;
						cImpuestoTipo.setTasaOCuota("");
					} else {
						cImpuestoTipo.setTasaOCuota(row.getCell(posicion).toString());
					}
					
					numeroCelda = 0;
					if(cfdi != null){
						if(cImpuestoTipo != null && impuestos != null){
							List<CfdiConceptoImpuestoTipo> traslados =  new ArrayList<CfdiConceptoImpuestoTipo>();
							traslados.add(cImpuestoTipo);
							impuestos.setRetenciones(traslados);
							cfdi.setImpuestos(impuestos);
						}
						conceptos.add(cfdi);
					}
				}
			}
			posicion = posicion + 1;
		}
		comp.setConceptos(conceptos);
		comp.setFinFactura(fFinFactura);
		return comp;
	}
	
	public CfdiComprobanteFiscal fillComprobanteFUQuitasTxt (String[] linea) {
		CfdiComprobanteFiscal comp =  new CfdiComprobanteFiscal();
		comp.setTipoEmision(TipoEmision.QUITAS);
		comp.setTasaCero(false);
		comp.setTotalExcento(true);
		// Pos 0 Emisor
		FiscalEntity fiscalEntity = null;
		comp.setEmisor(new CfdiEmisor());
		if (linea[0] == null || linea[0].trim().equals("")) {
			comp.getEmisor().setRfc("");
		} else {
			fiscalEntity = new FiscalEntity();
			fiscalEntity.setTaxID(linea[0]);
			fiscalEntity = fiscalEntityManager.findByRFCA(fiscalEntity);
			comp.getEmisor().setRfc(linea[0].trim());
		}
		
		/* Serie Posicion 1 -- row 1 */
		if (linea[1] == null || linea[1].trim().equals("")) {
			comp.setSerie("");
		} else {
			if (linea[1].trim().length() > 0) {
				comp.setSerie(linea[1].trim());
			} else {
				comp.setSerie("");
			}
		}
		
		// Pos 2
		/* Forma de pago */
		if (linea[2] == null || linea[2].trim().equals("")) {
			comp.setFormaPago("");
		} else {
			comp.setFormaPago(linea[2].trim());			
		}

		// pos 3 y 4
		/* Motivo descuento */
		String motivoDesc="";
		if (linea[3] == null || linea[3].trim().equals("")) {
			motivoDesc = "";
		} else {
			motivoDesc = linea[3].trim();
		}
		comp.setMotivoDescCellValue(motivoDesc);
		
		//descuento
		if(linea[4] == null || linea[4].trim().equals("")){
			comp.setDescuento(new BigDecimal("0.00"));
		}else{
			try{
				comp.setDescuento(new BigDecimal(linea[4].trim()));
			}catch(NumberFormatException e){
				System.out.println("Error al convertir Descuento en numerico se asigna 0. factura");
				comp.setDescuento(new BigDecimal("0.00"));
			}
		}
		
		// pos 5 Moneda
		if (linea[5] == null && linea[5].trim().equals("")) {
			comp.setMoneda("");
		} else {
			comp.setMoneda(linea[5].trim());
		}

		// pos 6
		/* Tipo de cambio */
		if (linea[6] == null || linea[6].trim().equals("")) {
			comp.setTipoCambio("");
		} else {
			comp.setTipoCambio(linea[6].trim());			
		}

		/* Tipo formato posicion 7 -- row 7 */
		if (linea[7] == null || linea[7].trim().equals("")) {
			comp.setTipoDeComprobante("I");
		} else {
			comp.setTipoDeComprobante(linea[7].trim());
		}

		// pos 8
		/* Metodo de pago */
		if (linea[8] == null || linea[8].trim().equals("")) {
			comp.setMetodoPago("");
		} else {
			comp.setMetodoPago(linea[8].trim());
		}

		// pos 9
		/* Regimen fiscal */
		if (linea[9] == null || linea[9].trim().equals("")) {
			comp.getEmisor().setRegimenFiscal("");
		} else {
			comp.getEmisor().setRegimenFiscal(linea[9].trim());
		}
		
		// pos 10, 11, 12 y 13
		/* RFC del cliente */
		boolean readFromFile = false;
		Customer customer = null;
		comp.setReceptor(new CfdiReceptor());
		comp.getReceptor().setDomicilio(new CfdiDomicilio());
		if (linea[10] == null) {
			comp.setCustomerRfcCellValue("");
		} else {
			if (linea[10].trim().equals("")) {
				comp.setCustomerRfcCellValue("");
			} else {
				comp.setCustomerRfcCellValue(linea[10].trim());
				if (linea[10].trim().toUpperCase().equals("XEXX010101000")
						|| linea[10].trim().toUpperCase().equals("XAXX010101000")
						|| linea[10].trim().equals("XEXE010101000")) {

					// evaluacion del id de extranjero
					if (linea[11] == null || linea[11].trim().equals("")) {
						comp.setStrIDExtranjero("");
						readFromFile= true;
					} else {
						String strIDExtranjero = linea[11].trim();
						comp.setStrIDExtranjero(strIDExtranjero);
						readFromFile = true;
					}
				} else {
						readFromFile = true;
					
				}
			}
		}
		
		// pos 35-44
		if (readFromFile) {
			comp.setReceptor(new CfdiReceptor());
			comp.getReceptor().setDomicilio(new CfdiDomicilio());
			//rfc
			comp.getReceptor().setRfc(comp.getCustomerRfcCellValue());
			//nombre cliente
			if (linea[35] == null || linea[35].trim().equals("")) {
				comp.getReceptor().setNombre("");
			} else {
				comp.getReceptor().setNombre(linea[35].trim());
			}
			//usoCfdi
			if (linea[12] == null || linea[12].trim().equals("")) {
				comp.getReceptor().setUsoCFDI("D04");
			} else {
				comp.getReceptor().setUsoCFDI(linea[12].trim());
			}
			//setNumRegIdTrib
			if (linea[13] == null || linea[13].trim().equals("")) {
				comp.getReceptor().setNumRegIdTrib("");
			} else {
				comp.getReceptor().setNumRegIdTrib(linea[13].trim());
			}
			
			//calle
			if (linea[36] == null || linea[36].trim().equals("")) {
				comp.getReceptor().getDomicilio().setCalle("");
			} else {
				comp.getReceptor().getDomicilio().setCalle(linea[36].trim());
			}
			//NoExterior
			if (linea[37] == null || linea[37].trim().equals("")) {
				comp.getReceptor().getDomicilio().setNoExterior("");
			} else {
				comp.getReceptor().getDomicilio().setNoExterior(linea[37].trim());
			}
			//NoInterior
			if (linea[38] == null || linea[38].trim().equals("")) {
				comp.getReceptor().getDomicilio().setNoInterior("");
			} else {
				comp.getReceptor().getDomicilio().setNoInterior(linea[38].trim());
			}
			//Colonia
			if (linea[39] == null || linea[39].trim().equals("")) {
				comp.getReceptor().getDomicilio().setColonia("");
			} else {
				comp.getReceptor().getDomicilio().setColonia(linea[39].trim());
			}
			//Codigo postal
			if (linea[40] == null || linea[40].trim().equals("")) {
				comp.getReceptor().getDomicilio().setCodigoPostal("");
			} else {
				comp.getReceptor().getDomicilio().setCodigoPostal(linea[40].trim());
			}
			//localidad
			if (linea[41] == null || linea[41].trim().equals("")) {
				comp.getReceptor().getDomicilio().setLocalidad("");
			} else {
				comp.getReceptor().getDomicilio().setLocalidad(linea[41].trim());
			}
			//Municipio
			if (linea[42] == null || linea[42].trim().equals("")) {
				comp.getReceptor().getDomicilio().setMunicipio("");
			} else {
				comp.getReceptor().getDomicilio().setMunicipio(linea[42].trim());
			}
			//Estado
			if (linea[43] == null || linea[43].trim().equals("")) {
				comp.getReceptor().getDomicilio().setEstado("");
			} else {
				comp.getReceptor().getDomicilio().setEstado(linea[43].trim());
			}
			//Pais
			if (linea[43] == null || linea[43].trim().equals("")) {
				comp.getReceptor().getDomicilio().setPais("");
			} else {
				comp.getReceptor().getDomicilio().setPais(linea[43].trim());
			}
		}
		if(comp.getReceptor() != null && comp.getReceptor().getDomicilio() == null){
			comp.getReceptor().setDomicilio(new CfdiDomicilio());
		}
		/* Referencia */
		if (linea[15] == null || linea[15].trim().equals("")) {
			comp.getReceptor().getDomicilio().setReferencia("");
		} else {
			comp.getReceptor().getDomicilio().setReferencia(linea[15].trim());
		}
		
		comp.setAddenda(new CfdiAddendaSantanderV1());
		comp.getAddenda().setInformacionPago(new CfdiAddendaInformacionPago());
		// pos 14
		/* Numero de cuenta */
		if (linea[14] == null || linea[14].trim().equals("")) {
			comp.setNumeroCuenta("");
		} else {
			comp.setNumeroCuenta(linea[14].trim());
		}
		
		comp.getAddenda().setInformacionEmision(new CfdiAddendaInformacionEmision());
		// pos 16
		//codigo cliente
		if (linea[16] == null || linea[16].trim().equals("")) {
			comp.getAddenda().getInformacionEmision().setCodigoCliente("");
		} else {
			comp.getAddenda().getInformacionEmision().setCodigoCliente(linea[16].trim());
		}
		
		// pos 17
		/* Contrato */
		if (linea[17] == null || linea[17].trim().equals("")) {
			comp.getAddenda().getInformacionEmision().setContrato("");
		} else {
			comp.getAddenda().getInformacionEmision().setContrato(linea[17].trim());
		}
		// pos 18
		/* Periodo */
		if (linea[18] == null || linea[18].trim().equals("")) {
			comp.getAddenda().getInformacionEmision().setPeriodo("");
		} else {
			comp.getAddenda().getInformacionEmision().setPeriodo(linea[18].trim());
		}
		
		// pos 19
		/* Centro de costos */
		if (linea[19] == null || linea[19].trim().equals("")) {
			comp.getAddenda().getInformacionEmision().setCentroCostos("");
		} else {
			comp.getAddenda().getInformacionEmision().setCentroCostos(linea[19].trim());
		}

		comp.getAddenda().setCampoAdicional(new HashMap<String, String>());
		// pos 20
		/* Descriptcion concepto */
		if (linea[20] == null || linea[20].trim().equals("")) {
			comp.getAddenda().getCampoAdicional().put("DESCRIPCIÓN CONCEPTO", "");
		} else {
			comp.getAddenda().getCampoAdicional().put("DESCRIPCIÓN CONCEPTO", linea[20].trim());
		}
		
		// pos 21
		/* Iva */
		if(linea[21] == null || linea[21].trim().equals("")){
			comp.setIvaCellValue("");
		}else{
			comp.setIvaCellValue(linea[21].trim());
		}
		// pos 22
		/* tipo addenda */
		comp.getAddenda().setInmuebles(new CfdiAddendaInmuebles());
		if(linea[22] != null){
			if(linea[22].toString().contains(".")){
				System.out.println("*** response Dentro IF AMDA: " + linea[22].toString());
				String words[] = linea[22].toString().split("\\.");
				linea[22] = words[0];
				System.out.println("*** response Dentro IF despues AMDA: " + linea[22].toString());
			}
			comp.setTipoAddendaCellValue(linea[22].toString());
			System.out.println("tipoAddenda:" + linea[22].toString());
			if (validaDatoRE(linea[22].toString().trim(), RE_DECIMAL)) {
				String strTipoAddenda = linea[22].toString();
				System.out.println("tipoAddendaClean: " + strTipoAddenda);
				
				//Nombre beneficiario
				if (linea[31] == null) {
					comp.getAddenda().getInformacionPago().setNombreBeneficiario("");
				} else {
					comp.getAddenda().getInformacionPago().setNombreBeneficiario(linea[31].toString().trim());
				}
				//institucion receptora
				if (linea[32] == null) {
					comp.getAddenda().getInformacionPago().setInstitucionReceptora("");
				} else {
					comp.getAddenda().getInformacionPago()
							.setInstitucionReceptora(linea[32].toString().trim());
				}
				//numero de cuenta
				if (linea[33] == null) {
					comp.getAddenda().getInformacionPago().setNumeroCuenta("");
				} else {
					if (linea[33].toString().trim().equals("")) {
						comp.getAddenda().getInformacionPago().setNumeroCuenta(linea[33].toString().trim());
					} else {
						comp.getAddenda().getInformacionPago().setNumeroCuenta(linea[33].toString().trim());
					}
				}
				//num proveedor
				if (linea[34] == null) {
					comp.getAddenda().getInformacionPago().setNumProveedor("");
				} else {
					if (linea[34].toString().trim().equals("")) {
						comp.getAddenda().getInformacionPago().setNumProveedor(linea[34].toString().trim());
					} else {
						comp.getAddenda().getInformacionPago().setNumProveedor(linea[34].toString().trim());
					}
				}
				
				//centro de costos
				if (linea[28] == null) {
					comp.setCostCenter("");
				} else {
					if (linea[28].toString().trim().equals("")) {
						comp.setCostCenter("");
					} else {
						comp.setCostCenter(linea[28].trim());
					}
				}
				
				if (strTipoAddenda.equals("1") || strTipoAddenda.equals("2") || strTipoAddenda.equals("3")) {
//					comp.getAddenda().getInformacionPago().setNombreBeneficiario("");
//					comp.getAddenda().getInformacionPago().setInstitucionReceptora("");
//					comp.getAddenda().getInformacionPago().setNumeroCuenta("");
//					comp.getAddenda().getInformacionPago().setNumProveedor("");
					
					//email
					if (linea[22].toString() == null) {
						comp.getAddenda().getInformacionPago().setEmail("");
					} else {
						// email
						if (!linea[23].toString().trim().equals("")) {
							if (validaDatoRE(linea[23].toString().trim(), RE_MAIL)) {
								comp.getAddenda().getInformacionPago().setEmail(linea[23].toString().trim());
							} else {
								comp.getAddenda().getInformacionPago().setEmail("");
							}
						} else {
							comp.getAddenda().getInformacionPago().setEmail("");
						}
					}
					//codigo iso moneda
					if (linea[24] == null) {
						comp.getAddenda().getInformacionPago().setCodigoISOMoneda("");
					} else {
						if (!linea[24].toString().trim().equals("")) {
							comp.getAddenda().getInformacionPago()
								.setCodigoISOMoneda(linea[24].toString().trim().toUpperCase());
						} else {
							comp.getAddenda().getInformacionPago().setCodigoISOMoneda("");
						}
					}
				}// fin de la evaluacion tipo addenda 1, 2 o 3
				if (strTipoAddenda.equals("1")) {
					//orden compra
					if (linea[25] == null) {
						comp.getAddenda().getInformacionPago().setOrdenCompra("");
					} else {
						if (linea[25].toString().trim().equals("")) {
							comp.getAddenda().getInformacionPago().setOrdenCompra("");
						} else {
							comp.getAddenda().getInformacionPago().setOrdenCompra(linea[25].toString().trim());
						}
					}
					//posicion compra
					if (linea[26] == null) {
						comp.getAddenda().getInformacionPago().setPosCompra("");
					} else {
						if (linea[26].toString().trim().equals("")) {
							comp.getAddenda().getInformacionPago().setPosCompra("");
						} else {
							comp.getAddenda().getInformacionPago().setPosCompra(linea[26].toString().trim());
						}
					}
				} else if (strTipoAddenda.equals("2")) {
					//cuenta contable
					if (linea[27] == null) {
						comp.getAddenda().getInformacionPago().setCuentaContable(null);
					} else {
						if (linea[27].toString().trim().equals("")) {
							comp.getAddenda().getInformacionPago().setCuentaContable("");
						} else {
							comp.getAddenda().getInformacionPago().setCuentaContable(linea[27].toString().trim());
						}
					}
					
				}else if (strTipoAddenda.equals("3")) {
					//num contrato
					if (linea[29] == null) {
						comp.getAddenda().getInmuebles().setNumContrato("");
					} else {
						if (linea[29].toString().trim().equals("")) {
							comp.getAddenda().getInmuebles().setNumContrato("");
						} else {
							comp.getAddenda().getInmuebles().setNumContrato(linea[29].toString().trim());
						}
					}
					//fecha vencimiento
					if (linea[30] == null) {
						comp.getAddenda().getInmuebles().setFechaVencimiento("");
					} else {
						if (linea[30].toString().trim().equals("")) {
							comp.getAddenda().getInmuebles().setFechaVencimiento("");
						} else {
							comp.getAddenda().getInmuebles().setFechaVencimiento(linea[30].toString().trim());
						}
					}
					
				} else if (strTipoAddenda.equals("0") || strTipoAddenda.equals("")) {
					comp.getAddenda().getInformacionPago().setEmail("");
					comp.getAddenda().getInformacionPago().setOrdenCompra("");
					
				} // fin de la evaluacion de addenda 0
			}// final de evaluacion de RE_DECIMAL
		}// fin de la evaluacion lina 22
		
		// pos 45
		if(linea[45] ==  null){
			comp.setNoAutorizacion("");
		}else {
			comp.setNoAutorizacion(linea[45].toString().trim());
		}
		// pos 46 y 47
		comp.setComplemento(new CfdiComplemento());
		comp.getComplemento().setTimbreFiscalDigital(new CfdiTimbreFiscalDigital());
		if (linea[46] == null) {
			comp.getComplemento().getTimbreFiscalDigital().setUuid("");
		} else {
			comp.getComplemento().getTimbreFiscalDigital().setUuid(linea[46].toString().trim());
			comp.setCfdiRelacionados(new CfdiRelacionado());
			if (linea[47] == null) {
				comp.getCfdiRelacionados().setTipoRelacion("");
			} else {
				comp.getCfdiRelacionados().setTipoRelacion(linea[47].toString().trim());
				System.out.println("* setTipoRelacion: " + comp.getCfdiRelacionados().getTipoRelacion());
			}
		}
		// pos 48
				int posicion = 48;
				int contadorConceptos = 0;
				boolean fPermisoVector = true;
				boolean fFinFactura = false;
				Integer numeroCelda = 0;
				String tipoFactorValRow = "";
				List<CfdiConcepto> conceptos =  new ArrayList<CfdiConcepto>();
				CfdiConcepto cfdi = null;
				CfdiConceptoImpuestoTipo cImpuestoTipo = null;
				CfdiConceptoImpuesto impuestos = null;
				
				while( posicion < linea.length && !fFinFactura ){
					System.out.println("datosPo:" + linea[posicion] + "pocision: " + posicion);
					if(numeroCelda==0){
						cfdi = new CfdiConcepto();
						impuestos = new CfdiConceptoImpuesto();
						cImpuestoTipo = new CfdiConceptoImpuestoTipo();
						cfdi.setImpuestos(impuestos);
					}
					numeroCelda += 1;
					contadorConceptos = contadorConceptos + 1;
					if ( linea[posicion].toString().equals("FINFACTURA")) {
						fFinFactura = true;
						break;
					}
					if (numeroCelda == 1) {
						numeroCelda = numeroCelda + 7;
						if (numeroCelda == 8) {
							posicion = posicion + 7;
							
							if (linea[posicion].toString().equalsIgnoreCase("Traslado")) {
								trasladoBol = true;
								retencionBol = false;
							} else if (linea[posicion].toString().equalsIgnoreCase("Retencion")) {
								retencionBol = true;
								trasladoBol = false;
							} else {
								fPermisoVector = false;
							}
							posicion = posicion - 7;
							numeroCelda = numeroCelda - 7;
						}
					}					
					if (trasladoBol) {						
						if (numeroCelda == 1) {
							System.out.println("claveProdServ: " + linea[posicion]);
							if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
								fPermisoVector = false;
								cfdi.setClaveProdServ("");
							} else {
								cfdi.setClaveProdServ(linea[posicion].toString().trim());
							}
						}

						if (numeroCelda == 2) {
							System.out.println("claveProdServ: " + linea[posicion]);
							if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
								fPermisoVector = false;
								cfdi.setCantidad(BigDecimal.ZERO);
							} else {
								cfdi.setCantidad(new BigDecimal(linea[posicion].toString().trim()));
							}
						}

						if (numeroCelda == 3) {
							System.out.println("claveProdServ: " + linea[posicion]);
							if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
								fPermisoVector = false;
								cfdi.setClaveUnidad("");
							} else {
								cfdi.setClaveUnidad(linea[posicion].toString().trim());
							}
						}

						if (numeroCelda == 4) {
							System.out.println("claveProdServ: " + linea[posicion]);
							if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
								fPermisoVector = false;
								cfdi.setUnidad("");
							} else {
								cfdi.setUnidad(linea[posicion].toString().trim());
							}
						}

						if (numeroCelda == 5) {
							System.out.println("claveProdServ: " + linea[posicion]);
							if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
								fPermisoVector = false;
								cfdi.setDescripcion("");
							} else {
								cfdi.setDescripcion(linea[posicion].toString().trim());
							}
						}

						if (numeroCelda == 6) {
							System.out.println("claveProdServ: " + linea[posicion]);
							if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
								fPermisoVector = false;
								cfdi.setValorUnitario(BigDecimal.ZERO);
							}else {
								cfdi.setValorUnitario(new BigDecimal(linea[posicion].toString().trim()));
							}
						}

						if (numeroCelda == 7) {
							System.out.println("dato8: " + linea[posicion]);
							if (linea[posicion].toString().equals("1")) {
								if (fPermisoVector)
									cfdi.setAplicaIva("1");
							} else {
								fPermisoVector = false;
								cfdi.setAplicaIva("");
							}
						}

						if (numeroCelda == 9) {
							System.out.println("dato9: " + linea[posicion]);
							if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
								fPermisoVector = false;
								cImpuestoTipo.setImpuesto("");
							} else {
								cImpuestoTipo.setImpuesto(linea[posicion].toString().trim());
							}
						}
						
						if (numeroCelda == 10) {
							System.out.println("dato10: " + linea[posicion]);
							if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
								cImpuestoTipo.setTipoFactor("");
							} else {
								tipoFactorValRow = linea[posicion].toString();
								cImpuestoTipo.setTipoFactor(tipoFactorValRow);
							}
						}

						if (numeroCelda == 11) {
							System.out.println("dato11: " + linea[posicion]);
							if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
								cImpuestoTipo.setTasaOCuota("");
							} else {
								if (!tipoFactorValRow.equalsIgnoreCase("Exento")
										&& !tipoFactorValRow.equalsIgnoreCase("Excento")) {
									cImpuestoTipo.setTasaOCuota(linea[posicion].toString());
								} else {
									cImpuestoTipo.setTasaOCuota("0.0");
								}
							}
							
							numeroCelda = 0;
							if(cfdi != null){
								if(cImpuestoTipo != null && impuestos != null){
									List<CfdiConceptoImpuestoTipo> traslados =  new ArrayList<CfdiConceptoImpuestoTipo>();
									traslados.add(cImpuestoTipo);
									impuestos.setTraslados(traslados);
									cfdi.setImpuestos(impuestos);
								}
								if ( cfdi.getAplicaIva() != null && cfdi.getAplicaIva().trim().equals("1"))
									comp.setTasaCero(true);
								if ( cImpuestoTipo != null  &&  !cImpuestoTipo.getTipoFactor().equalsIgnoreCase("Exento"))
									comp.setTotalExcento(false);
								conceptos.add(cfdi);
							}
						}

					} else if (retencionBol) {
						System.out.println("retencionBo");
						if (numeroCelda == 1) { 
							if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
								fPermisoVector = false;
								cfdi.setClaveProdServ("");
							} else {
								cfdi.setClaveProdServ(linea[posicion].toString().trim());
							}
						}

						if (numeroCelda == 2) {
							if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
								fPermisoVector = false;
								cfdi.setCantidad(BigDecimal.ZERO);
							} else {
								cfdi.setCantidad(new BigDecimal(linea[posicion].toString().trim()));
							}
						}

						if (numeroCelda == 3) {
							if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
								fPermisoVector = false;
								cfdi.setClaveUnidad("");
							} else {
								cfdi.setClaveUnidad(linea[posicion].toString().trim());
							}
						}

						if (numeroCelda == 4) {
							if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
								fPermisoVector = false;
								cfdi.setUnidad("");
							} else {
								cfdi.setUnidad(linea[posicion].toString().trim());
							}
						}

						if (numeroCelda == 5) {
							if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
								fPermisoVector = false;
								cfdi.setDescripcion("");
							} else {
								cfdi.setDescripcion(linea[posicion].toString().trim());
							}
						}

						if (numeroCelda == 6) {
							if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
								fPermisoVector = false;
								cfdi.setValorUnitario(BigDecimal.ZERO);
							}
							else {
								cfdi.setValorUnitario(new BigDecimal(linea[posicion].toString().trim()));
							}
						}

						if (numeroCelda == 7) {
							if (linea[posicion].toString().equals("1")) {
								if (fPermisoVector)
									cfdi.setAplicaIva("1");
							} else {
								fPermisoVector = false;
								cfdi.setAplicaIva("");
							}
						}
						
						if (numeroCelda == 9) {
							if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
								cImpuestoTipo.setImpuesto("");
							} else {
								cImpuestoTipo.setImpuesto(linea[posicion].toString());
							}
						}
						
						if (numeroCelda == 10) {
							if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
								fPermisoVector = false;
								cImpuestoTipo.setTipoFactor("");
							} else {
								cImpuestoTipo.setTipoFactor(linea[posicion].toString());
							}
						}

						if (numeroCelda == 11) {
							if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
								fPermisoVector = false;
								cImpuestoTipo.setTasaOCuota("");
							} else {
								cImpuestoTipo.setTasaOCuota(linea[posicion].toString());
							}
							
							numeroCelda = 0;
							if(cfdi != null){
								if(cImpuestoTipo != null && impuestos != null){
									List<CfdiConceptoImpuestoTipo> traslados =  new ArrayList<CfdiConceptoImpuestoTipo>();
									traslados.add(cImpuestoTipo);
									impuestos.setRetenciones(traslados);
									cfdi.setImpuestos(impuestos);
								}
							
								if ( cfdi.getAplicaIva() != null && cfdi.getAplicaIva().trim().equals("1"))
									comp.setTasaCero(true);
								conceptos.add(cfdi);
							}
						}
					}
					
					
					
					posicion = posicion + 1;
				}// fin del while
				
				System.out.println("tamaño de el arreglo: "+ linea.length);
				System.out.println("tamaño de conceptos: "+ conceptos.size());
				comp.setConceptos(conceptos);
				comp.setFinFactura(fFinFactura);
		return comp;
	}

	public CfdiComprobanteFiscal fillComprobanteFUTxt(String[] linea){
		CfdiComprobanteFiscal comp =  new CfdiComprobanteFiscal();
		comp.setTipoEmision(TipoEmision.FORMATO_UNICO);
		comp.setTasaCero(false);
		comp.setTotalExcento(true);
		// Pos 0 Emisor
		FiscalEntity fiscalEntity = null;
		comp.setEmisor(new CfdiEmisor());
		if (linea[0] == null || linea[0].trim().equals("")) {
			comp.getEmisor().setRfc("");
		} else {
			fiscalEntity = new FiscalEntity();
			fiscalEntity.setTaxID(linea[0]);
			fiscalEntity = fiscalEntityManager.findByRFCA(fiscalEntity);
			comp.getEmisor().setRfc(linea[0].trim());
		}
		
		/* Serie Posicion 1 -- row 1 */
		if (linea[1] == null || linea[1].trim().equals("")) {
			comp.setSerie("");
		} else {
			if (linea[1].trim().length() > 0) {
				comp.setSerie(linea[1].trim());
			} else {
				comp.setSerie("");
			}
		}
		
		// Pos 2
		/* Forma de pago */
		if (linea[2] == null || linea[2].trim().equals("")) {
			comp.setFormaPago("");
		} else {
			comp.setFormaPago(linea[2].trim());			
		}

		// pos 3 y 4
		/* Motivo descuento */
		String motivoDesc="";
		if (linea[3] == null || linea[3].trim().equals("")) {
			motivoDesc = "";
		} else {
			motivoDesc = linea[3].trim();
		}
		comp.setMotivoDescCellValue(motivoDesc);
		
		//descuento
		if(linea[4] == null || linea[4].trim().equals("")){
			comp.setDescuento(new BigDecimal("0.00"));
		}else{
			try{
				comp.setDescuento(new BigDecimal(linea[4].trim()));
			}catch(NumberFormatException e){
				System.out.println("Error al convertir Descuento en numerico se asigna 0. factura");
				comp.setDescuento(new BigDecimal("0.00"));
			}
		}
		
		// pos 5 Moneda
		if (linea[5] == null && linea[5].trim().equals("")) {
			comp.setMoneda("");
		} else {
			comp.setMoneda(linea[5].trim());
		}

		// pos 6
		/* Tipo de cambio */
		if (linea[6] == null || linea[6].trim().equals("")) {
			comp.setTipoCambio("");
		} else {
			comp.setTipoCambio(linea[6].trim());			
		}

		/* Tipo formato posicion 7 -- row 7 */
		if (linea[7] == null || linea[7].trim().equals("")) {
			comp.setTipoDeComprobante("I");
		} else {
			comp.setTipoDeComprobante(linea[7].trim());
		}

		// pos 8
		/* Metodo de pago */
		if (linea[8] == null || linea[8].trim().equals("")) {
			comp.setMetodoPago("");
		} else {
			comp.setMetodoPago(linea[8].trim());
		}

		// pos 9
		/* Regimen fiscal */
		if (linea[9] == null || linea[9].trim().equals("")) {
			comp.getEmisor().setRegimenFiscal("");
		} else {
			comp.getEmisor().setRegimenFiscal(linea[9].trim());
		}
		
		// pos 10, 11, 12 y 13
		/* RFC del cliente */
		boolean readFromFile = false;
		Customer customer = null;
		comp.setReceptor(new CfdiReceptor());
		comp.getReceptor().setDomicilio(new CfdiDomicilio());
		if (linea[10] == null) {
			comp.setCustomerRfcCellValue("");
		} else {
			if (linea[10].trim().equals("")) {
				comp.setCustomerRfcCellValue("");
			} else {
				comp.setCustomerRfcCellValue(linea[10].trim());
				if (linea[10].trim().toUpperCase().equals("XEXX010101000")
						|| linea[10].trim().toUpperCase().equals("XAXX010101000")
						|| linea[10].trim().equals("XEXE010101000")) {

					// evaluacion del id de extranjero
					if (linea[11] == null || linea[11].trim().equals("")) {
						comp.setStrIDExtranjero("");
						readFromFile= true;
					} else {
						String strIDExtranjero = linea[11].trim();
						comp.setStrIDExtranjero(strIDExtranjero);
						System.out.println("ID Extranjero: " + strIDExtranjero);
						customer = customerManager.findByIdExtranjero(strIDExtranjero);
						comp.setReceptor(new CfdiReceptor());
						if (customer != null) {
							//rfc
							comp.getReceptor().setRfc(customer.getTaxId());
							//nombre
							comp.getReceptor().setNombre(customer.getPhysicalName());
							//uso cfdi
							if (linea[12] == null || linea[12].trim().length() == 0) {
								comp.getReceptor().setUsoCFDI("D04");
							} else {
								comp.getReceptor().setUsoCFDI(linea[12].trim());
							}
							//NumRegIdTrib
							if (linea[13] == null || linea[13].trim().equals("")) {
								comp.getReceptor().setNumRegIdTrib("");
							} else {
								comp.getReceptor().setNumRegIdTrib(linea[13].trim());
							}
							
							//domicilio
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
//								comp.getReceptor().getDomicilio()
//										.setReferencia(customer.getAddress().getReference());
							}
						}else{
							readFromFile = true;
						}
					}
				} else {
					if (fiscalEntity != null) {
						customer = customerManager.get(linea[10].trim(),
								String.valueOf(fiscalEntity.getId()));
						
						comp.setReceptor(new CfdiReceptor());
						if (customer != null) {
							//rfc
							comp.getReceptor().setRfc(customer.getTaxId());
							//nombre
							comp.getReceptor().setNombre(customer.getPhysicalName());
							//uso cfdi
							if (linea[12] == null || linea[12].trim().equals("")) {
								comp.getReceptor().setUsoCFDI("D04");
							} else {
								comp.getReceptor().setUsoCFDI(linea[12].trim());
							}
							//NumRegIdTrib
							if (linea[13]== null || linea[13].trim().equals("")) {
								comp.getReceptor().setNumRegIdTrib("");
							} else {
								comp.getReceptor().setNumRegIdTrib(linea[13].trim());
							}
							
							//domicilio
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
								//comp.getReceptor().getDomicilio().setReferencia(customer.getAddress().getReference());
							}
						}else{
							readFromFile = true;
						}
					}
				}
			}
		}
		
		// pos 35-44
		if (readFromFile) {
			comp.setReceptor(new CfdiReceptor());
			comp.getReceptor().setDomicilio(new CfdiDomicilio());
			//rfc
			comp.getReceptor().setRfc(comp.getCustomerRfcCellValue());
			//nombre cliente
			if (linea[35] == null || linea[35].trim().equals("")) {
				comp.getReceptor().setNombre("");
			} else {
				comp.getReceptor().setNombre(linea[35].trim());
			}
			//usoCfdi
			if (linea[12] == null || linea[12].trim().equals("")) {
				comp.getReceptor().setUsoCFDI("D04");
			} else {
				comp.getReceptor().setUsoCFDI(linea[12].trim());
			}
			//setNumRegIdTrib
			if (linea[13] == null || linea[13].trim().equals("")) {
				comp.getReceptor().setNumRegIdTrib("");
			} else {
				comp.getReceptor().setNumRegIdTrib(linea[13].trim());
			}
			
			//calle
			if (linea[36] == null || linea[36].trim().equals("")) {
				comp.getReceptor().getDomicilio().setCalle("");
			} else {
				comp.getReceptor().getDomicilio().setCalle(linea[36].trim());
			}
			//NoExterior
			if (linea[37] == null || linea[37].trim().equals("")) {
				comp.getReceptor().getDomicilio().setNoExterior("");
			} else {
				comp.getReceptor().getDomicilio().setNoExterior(linea[37].trim());
			}
			//NoInterior
			if (linea[38] == null || linea[38].trim().equals("")) {
				comp.getReceptor().getDomicilio().setNoInterior("");
			} else {
				comp.getReceptor().getDomicilio().setNoInterior(linea[38].trim());
			}
			//Colonia
			if (linea[39] == null || linea[39].trim().equals("")) {
				comp.getReceptor().getDomicilio().setColonia("");
			} else {
				comp.getReceptor().getDomicilio().setColonia(linea[39].trim());
			}
			//Codigo postal
			if (linea[40] == null || linea[40].trim().equals("")) {
				comp.getReceptor().getDomicilio().setCodigoPostal("");
			} else {
				comp.getReceptor().getDomicilio().setCodigoPostal(linea[40].trim());
			}
			//localidad
			if (linea[41] == null || linea[41].trim().equals("")) {
				comp.getReceptor().getDomicilio().setLocalidad("");
			} else {
				comp.getReceptor().getDomicilio().setLocalidad(linea[41].trim());
			}
			//Municipio
			if (linea[42] == null || linea[42].trim().equals("")) {
				comp.getReceptor().getDomicilio().setMunicipio("");
			} else {
				comp.getReceptor().getDomicilio().setMunicipio(linea[42].trim());
			}
			//Estado
			if (linea[43] == null || linea[43].trim().equals("")) {
				comp.getReceptor().getDomicilio().setEstado("");
			} else {
				comp.getReceptor().getDomicilio().setEstado(linea[43].trim());
			}
			//Pais
			if (linea[43] == null || linea[43].trim().equals("")) {
				comp.getReceptor().getDomicilio().setPais("");
			} else {
				comp.getReceptor().getDomicilio().setPais(linea[43].trim());
			}
		}
		if(comp.getReceptor() != null && comp.getReceptor().getDomicilio() == null){
			comp.getReceptor().setDomicilio(new CfdiDomicilio());
		}
		/* Referencia */
		if (linea[15] == null || linea[15].trim().equals("")) {
			comp.getReceptor().getDomicilio().setReferencia("");
		} else {
			comp.getReceptor().getDomicilio().setReferencia(linea[15].trim());
		}
		
		comp.setAddenda(new CfdiAddendaSantanderV1());
		comp.getAddenda().setInformacionPago(new CfdiAddendaInformacionPago());
		// pos 14
		/* Numero de cuenta */
		if (linea[14] == null || linea[14].trim().equals("")) {
			comp.setNumeroCuenta("");
		} else {
			comp.setNumeroCuenta(linea[14].trim());
		}
		
		comp.getAddenda().setInformacionEmision(new CfdiAddendaInformacionEmision());
		// pos 16
		//codigo cliente
		if (linea[16] == null || linea[16].trim().equals("")) {
			comp.getAddenda().getInformacionEmision().setCodigoCliente("");
		} else {
			comp.getAddenda().getInformacionEmision().setCodigoCliente(linea[16].trim());
		}
		
		// pos 17
		/* Contrato */
		if (linea[17] == null || linea[17].trim().equals("")) {
			comp.getAddenda().getInformacionEmision().setContrato("");
		} else {
			comp.getAddenda().getInformacionEmision().setContrato(linea[17].trim());
		}
		// pos 18
		/* Periodo */
		if (linea[18] == null || linea[18].trim().equals("")) {
			comp.getAddenda().getInformacionEmision().setPeriodo("");
		} else {
			comp.getAddenda().getInformacionEmision().setPeriodo(linea[18].trim());
		}
		
		// pos 19
		/* Centro de costos */
		if (linea[19] == null || linea[19].trim().equals("")) {
			comp.getAddenda().getInformacionEmision().setCentroCostos("");
		} else {
			comp.getAddenda().getInformacionEmision().setCentroCostos(linea[19].trim());
		}

		comp.getAddenda().setCampoAdicional(new HashMap<String, String>());
		// pos 20
		/* Descriptcion concepto */
		if (linea[20] == null || linea[20].trim().equals("")) {
			comp.getAddenda().getCampoAdicional().put("DESCRIPCIÓN CONCEPTO", "");
		} else {
			comp.getAddenda().getCampoAdicional().put("DESCRIPCIÓN CONCEPTO", linea[20].trim());
		}
		
		// pos 21
		/* Iva */
		if(linea[21] == null || linea[21].trim().equals("")){
			comp.setIvaCellValue("");
		}else{
			comp.setIvaCellValue(linea[21].trim());
		}
		// pos 22
		/* tipo addenda */
		comp.getAddenda().setInmuebles(new CfdiAddendaInmuebles());
		if(linea[22] != null){
			if(linea[22].toString().contains(".")){
				System.out.println("*** response Dentro IF AMDA: " + linea[22].toString());
				String words[] = linea[22].toString().split("\\.");
				linea[22] = words[0];
				System.out.println("*** response Dentro IF despues AMDA: " + linea[22].toString());
			}
			comp.setTipoAddendaCellValue(linea[22].toString());
			System.out.println("tipoAddenda:" + linea[22].toString());
			if (validaDatoRE(linea[22].toString().trim(), RE_DECIMAL)) {
				String strTipoAddenda = linea[22].toString();
				System.out.println("tipoAddendaClean: " + strTipoAddenda);
				
				//Nombre beneficiario
				if (linea[31] == null) {
					comp.getAddenda().getInformacionPago().setNombreBeneficiario("");
				} else {
					comp.getAddenda().getInformacionPago().setNombreBeneficiario(linea[31].toString().trim());
				}
				//institucion receptora
				if (linea[32] == null) {
					comp.getAddenda().getInformacionPago().setInstitucionReceptora("");
				} else {
					comp.getAddenda().getInformacionPago()
							.setInstitucionReceptora(linea[32].toString().trim());
				}
				//numero de cuenta
				if (linea[33] == null) {
					comp.getAddenda().getInformacionPago().setNumeroCuenta("");
				} else {
					if (linea[33].toString().trim().equals("")) {
						comp.getAddenda().getInformacionPago().setNumeroCuenta(linea[33].toString().trim());
					} else {
						comp.getAddenda().getInformacionPago().setNumeroCuenta(linea[33].toString().trim());
					}
				}
				//num proveedor
				if (linea[34] == null) {
					comp.getAddenda().getInformacionPago().setNumProveedor("");
				} else {
					if (linea[34].toString().trim().equals("")) {
						comp.getAddenda().getInformacionPago().setNumProveedor(linea[34].toString().trim());
					} else {
						comp.getAddenda().getInformacionPago().setNumProveedor(linea[34].toString().trim());
					}
				}
				
				//centro de costos
				if (linea[28] == null) {
					comp.setCostCenter("");
				} else {
					if (linea[28].toString().trim().equals("")) {
						comp.setCostCenter("");
					} else {
						comp.setCostCenter(linea[28].trim());
					}
				}
				
				if (strTipoAddenda.equals("1") || strTipoAddenda.equals("2") || strTipoAddenda.equals("3")) {
//					comp.getAddenda().getInformacionPago().setNombreBeneficiario("");
//					comp.getAddenda().getInformacionPago().setInstitucionReceptora("");
//					comp.getAddenda().getInformacionPago().setNumeroCuenta("");
//					comp.getAddenda().getInformacionPago().setNumProveedor("");
					
					//email
					if (linea[22].toString() == null) {
						comp.getAddenda().getInformacionPago().setEmail("");
					} else {
						// email
						if (!linea[23].toString().trim().equals("")) {
							if (validaDatoRE(linea[23].toString().trim(), RE_MAIL)) {
								comp.getAddenda().getInformacionPago().setEmail(linea[23].toString().trim());
							} else {
								comp.getAddenda().getInformacionPago().setEmail("");
							}
						} else {
							comp.getAddenda().getInformacionPago().setEmail("");
						}
					}
					//codigo iso moneda
					if (linea[24] == null) {
						comp.getAddenda().getInformacionPago().setCodigoISOMoneda("");
					} else {
						if (!linea[24].toString().trim().equals("")) {
							comp.getAddenda().getInformacionPago()
								.setCodigoISOMoneda(linea[24].toString().trim().toUpperCase());
						} else {
							comp.getAddenda().getInformacionPago().setCodigoISOMoneda("");
						}
					}
				}// fin de la evaluacion tipo addenda 1, 2 o 3
				if (strTipoAddenda.equals("1")) {
					//orden compra
					if (linea[25] == null) {
						comp.getAddenda().getInformacionPago().setOrdenCompra("");
					} else {
						if (linea[25].toString().trim().equals("")) {
							comp.getAddenda().getInformacionPago().setOrdenCompra("");
						} else {
							comp.getAddenda().getInformacionPago().setOrdenCompra(linea[25].toString().trim());
						}
					}
					//posicion compra
					if (linea[26] == null) {
						comp.getAddenda().getInformacionPago().setPosCompra("");
					} else {
						if (linea[26].toString().trim().equals("")) {
							comp.getAddenda().getInformacionPago().setPosCompra("");
						} else {
							comp.getAddenda().getInformacionPago().setPosCompra(linea[26].toString().trim());
						}
					}
				} else if (strTipoAddenda.equals("2")) {
					//cuenta contable
					if (linea[27] == null) {
						comp.getAddenda().getInformacionPago().setCuentaContable(null);
					} else {
						if (linea[27].toString().trim().equals("")) {
							comp.getAddenda().getInformacionPago().setCuentaContable("");
						} else {
							comp.getAddenda().getInformacionPago().setCuentaContable(linea[27].toString().trim());
						}
					}
					
				}else if (strTipoAddenda.equals("3")) {
					//num contrato
					if (linea[29] == null) {
						comp.getAddenda().getInmuebles().setNumContrato("");
					} else {
						if (linea[29].toString().trim().equals("")) {
							comp.getAddenda().getInmuebles().setNumContrato("");
						} else {
							comp.getAddenda().getInmuebles().setNumContrato(linea[29].toString().trim());
						}
					}
					//fecha vencimiento
					if (linea[30] == null) {
						comp.getAddenda().getInmuebles().setFechaVencimiento("");
					} else {
						if (linea[30].toString().trim().equals("")) {
							comp.getAddenda().getInmuebles().setFechaVencimiento("");
						} else {
							comp.getAddenda().getInmuebles().setFechaVencimiento(linea[30].toString().trim());
						}
					}
					
				} else if (strTipoAddenda.equals("0") || strTipoAddenda.equals("")) {
					comp.getAddenda().getInformacionPago().setEmail("");
					comp.getAddenda().getInformacionPago().setOrdenCompra("");
					
				} // fin de la evaluacion de addenda 0
			}// final de evaluacion de RE_DECIMAL
		}// fin de la evaluacion lina 22
		
		// pos 45
		if(linea[45] ==  null){
			comp.setNoAutorizacion("");
		}else {
			comp.setNoAutorizacion(linea[45].toString().trim());
		}
		// pos 46 y 47
		comp.setComplemento(new CfdiComplemento());
		comp.getComplemento().setTimbreFiscalDigital(new CfdiTimbreFiscalDigital());
		if (linea[46] == null) {
			comp.getComplemento().getTimbreFiscalDigital().setUuid("");
		} else {
			comp.getComplemento().getTimbreFiscalDigital().setUuid(linea[46].toString().trim());
			comp.setCfdiRelacionados(new CfdiRelacionado());
			if (linea[47] == null) {
				comp.getCfdiRelacionados().setTipoRelacion("");
			} else {
				comp.getCfdiRelacionados().setTipoRelacion(linea[47].toString().trim());
				System.out.println("* setTipoRelacion: " + comp.getCfdiRelacionados().getTipoRelacion());
			}
		}
		// pos 48
				int posicion = 48;
				int contadorConceptos = 0;
				boolean fPermisoVector = true;
				boolean fFinFactura = false;
				Integer numeroCelda = 0;
				String tipoFactorValRow = "";
				List<CfdiConcepto> conceptos =  new ArrayList<CfdiConcepto>();
				CfdiConcepto cfdi = null;
				CfdiConceptoImpuestoTipo cImpuestoTipo = null;
				CfdiConceptoImpuesto impuestos = null;
				
				while( posicion < linea.length && !fFinFactura ){
					if(numeroCelda==0){
						cfdi = new CfdiConcepto();
						impuestos = new CfdiConceptoImpuesto();
						cImpuestoTipo = new CfdiConceptoImpuestoTipo();
						cfdi.setImpuestos(impuestos);
					}
					numeroCelda += 1;
					contadorConceptos = contadorConceptos + 1;
					if ( linea[posicion].toString().equals("FINFACTURA")) {
						fFinFactura = true;
						break;
					}
					if (numeroCelda == 1) {
						numeroCelda = numeroCelda + 7;
						if (numeroCelda == 8) {
							posicion = posicion + 7;
							if (linea[posicion].toString().equalsIgnoreCase("Traslado")) {
								trasladoBol = true;
								retencionBol = false;
							} else if (linea[posicion].toString().equalsIgnoreCase("Retencion")) {
								retencionBol = true;
								trasladoBol = false;
							} else {
								fPermisoVector = false;
							}
							posicion = posicion - 7;
							numeroCelda = numeroCelda - 7;
						}
					}
					if (trasladoBol) {
						if (numeroCelda == 1) {
							if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
								fPermisoVector = false;
								cfdi.setClaveProdServ("");
							} else {
								cfdi.setClaveProdServ(linea[posicion].toString().trim());
							}
						}

						if (numeroCelda == 2) {
							if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
								fPermisoVector = false;
								cfdi.setCantidad(BigDecimal.ZERO);
							} else {
								cfdi.setCantidad(new BigDecimal(linea[posicion].toString().trim()));
							}
						}

						if (numeroCelda == 3) {
							if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
								fPermisoVector = false;
								cfdi.setClaveUnidad("");
							} else {
								cfdi.setClaveUnidad(linea[posicion].toString().trim());
							}
						}

						if (numeroCelda == 4) {
							if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
								fPermisoVector = false;
								cfdi.setUnidad("");
							} else {
								cfdi.setUnidad(linea[posicion].toString().trim());
							}
						}

						if (numeroCelda == 5) {
							if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
								fPermisoVector = false;
								cfdi.setDescripcion("");
							} else {
								cfdi.setDescripcion(linea[posicion].toString().trim());
							}
						}

						if (numeroCelda == 6) {
							if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
								fPermisoVector = false;
								cfdi.setValorUnitario(BigDecimal.ZERO);
							}else {
								cfdi.setValorUnitario(new BigDecimal(linea[posicion].toString().trim()));
							}
						}

						if (numeroCelda == 7) {
							if (linea[posicion].toString().equals("1")) {
								if (fPermisoVector)
									cfdi.setAplicaIva("1");
							} else {
								fPermisoVector = false;
								cfdi.setAplicaIva("");
							}
						}

						if (numeroCelda == 9) {
							if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
								fPermisoVector = false;
								cImpuestoTipo.setImpuesto("");
							} else {
								cImpuestoTipo.setImpuesto(linea[posicion].toString().trim());
							}
						}
						
						if (numeroCelda == 10) {
							if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
								cImpuestoTipo.setTipoFactor("");
							} else {
								tipoFactorValRow = linea[posicion].toString();
								cImpuestoTipo.setTipoFactor(tipoFactorValRow);
							}
						}

						if (numeroCelda == 11) {
							if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
								System.out.println("tasaVaciaXD:");
								cImpuestoTipo.setTasaOCuota("");
							} else {
								System.out.println("tasaVaciaXD: " + linea[posicion].toString());
								if (!tipoFactorValRow.equalsIgnoreCase("Exento")
										&& !tipoFactorValRow.equalsIgnoreCase("Excento")) {
									cImpuestoTipo.setTasaOCuota(linea[posicion].toString());
								} else {
									cImpuestoTipo.setTasaOCuota("0.0");
								}
							}
							
							numeroCelda = 0;
							if(cfdi != null){
								if(cImpuestoTipo != null && impuestos != null){
									List<CfdiConceptoImpuestoTipo> traslados =  new ArrayList<CfdiConceptoImpuestoTipo>();
									traslados.add(cImpuestoTipo);
									impuestos.setTraslados(traslados);
									cfdi.setImpuestos(impuestos);
								}
								
								if ( cfdi.getAplicaIva() != null && cfdi.getAplicaIva().trim().equals("1"))
									comp.setTasaCero(true);

								if ( cImpuestoTipo != null  &&  !cImpuestoTipo.getTipoFactor().equalsIgnoreCase("Exento"))
									comp.setTotalExcento(false);
								conceptos.add(cfdi);
							}
						}

					} else if (retencionBol) {

						if (numeroCelda == 1) {
							if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
								fPermisoVector = false;
								cfdi.setClaveProdServ("");
							} else {
								cfdi.setClaveProdServ(linea[posicion].toString().trim());
							}
						}

						if (numeroCelda == 2) {
							if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
								fPermisoVector = false;
								cfdi.setCantidad(BigDecimal.ZERO);
							} else {
								cfdi.setCantidad(new BigDecimal(linea[posicion].toString().trim()));
							}
						}

						if (numeroCelda == 3) {
							if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
								fPermisoVector = false;
								cfdi.setClaveUnidad("");
							} else {
								cfdi.setClaveUnidad(linea[posicion].toString().trim());
							}
						}

						if (numeroCelda == 4) {
							if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
								fPermisoVector = false;
								cfdi.setUnidad("");
							} else {
								cfdi.setUnidad(linea[posicion].toString().trim());
							}
						}

						if (numeroCelda == 5) {
							if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
								fPermisoVector = false;
								cfdi.setDescripcion("");
							} else {
								cfdi.setDescripcion(linea[posicion].toString().trim());
							}
						}

						if (numeroCelda == 6) {
							if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
								fPermisoVector = false;
								cfdi.setValorUnitario(BigDecimal.ZERO);
							}
							else {
								cfdi.setValorUnitario(new BigDecimal(linea[posicion].toString().trim()));
							}
						}

						if (numeroCelda == 7) {
							if (linea[posicion].toString().equals("1")) {
								if (fPermisoVector)
									cfdi.setAplicaIva("1");
							} else {
								fPermisoVector = false;
								cfdi.setAplicaIva("");
							}
						}
						
						if (numeroCelda == 9) {
							if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
								cImpuestoTipo.setImpuesto("");
							} else {
								cImpuestoTipo.setImpuesto(linea[posicion].toString());
							}
						}
						
						if (numeroCelda == 10) {
							if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
								fPermisoVector = false;
								cImpuestoTipo.setTipoFactor("");
							} else {
								cImpuestoTipo.setTipoFactor(linea[posicion].toString());
							}
						}

						if (numeroCelda == 11) {
							if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
								fPermisoVector = false;
								cImpuestoTipo.setTasaOCuota("");
							} else {
								cImpuestoTipo.setTasaOCuota(linea[posicion].toString());
							}
							
							numeroCelda = 0;
							if(cfdi != null){
								if(cImpuestoTipo != null && impuestos != null){
									List<CfdiConceptoImpuestoTipo> traslados =  new ArrayList<CfdiConceptoImpuestoTipo>();
									traslados.add(cImpuestoTipo);
									impuestos.setRetenciones(traslados);
									cfdi.setImpuestos(impuestos);
								}
								
								if ( cfdi.getAplicaIva() != null && cfdi.getAplicaIva().trim().equals("1"))
									comp.setTasaCero(true);
								conceptos.add(cfdi);
							}
						}
					}
					
					
					
					posicion = posicion + 1;
				}// fin del while
				
				System.out.println("tamaño de el arreglo: "+ linea.length);
				System.out.println("tamaño de conceptos: "+ conceptos.size());
				comp.setConceptos(conceptos);
				comp.setFinFactura(fFinFactura);
		return comp;
	}

}
