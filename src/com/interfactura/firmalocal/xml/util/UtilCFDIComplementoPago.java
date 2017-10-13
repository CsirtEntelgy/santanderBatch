package com.interfactura.firmalocal.xml.util;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.interfactura.firmalocal.datamodel.CfdiAddendaInformacionEmision;
import com.interfactura.firmalocal.datamodel.CfdiAddendaInformacionPago;
import com.interfactura.firmalocal.datamodel.CfdiAddendaInmuebles;
import com.interfactura.firmalocal.datamodel.CfdiAddendaSantanderV1;
import com.interfactura.firmalocal.datamodel.CfdiComplemento;
import com.interfactura.firmalocal.datamodel.CfdiComprobanteFiscal;
import com.interfactura.firmalocal.datamodel.CfdiConcepto;
import com.interfactura.firmalocal.datamodel.CfdiDomicilio;
import com.interfactura.firmalocal.datamodel.CfdiEmisor;
import com.interfactura.firmalocal.datamodel.CfdiImpuesto;
import com.interfactura.firmalocal.datamodel.CfdiReceptor;
import com.interfactura.firmalocal.datamodel.CfdiRelacionado;
import com.interfactura.firmalocal.datamodel.CfdiTimbreFiscalDigital;
import com.interfactura.firmalocal.datamodel.ComplementoPago;
import com.interfactura.firmalocal.domain.entities.CodigoISO;
import com.interfactura.firmalocal.domain.entities.Customer;
import com.interfactura.firmalocal.domain.entities.FiscalEntity;
import com.interfactura.firmalocal.persistence.CodigoISOManager;
import com.interfactura.firmalocal.persistence.CustomerManager;
import com.interfactura.firmalocal.persistence.FiscalEntityManager;
import com.interfactura.firmalocal.xml.util.ValidationConstants.TipoEmision;

@Service
public class UtilCFDIComplementoPago {
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

	public CfdiComprobanteFiscal fillComprobanteComplementoPagoFromTxt(String[] linea) {
		// TODO Auto-generated method stub
		String referencia = "";
		CfdiComprobanteFiscal comp = new CfdiComprobanteFiscal();
		comp.setTipoEmision(TipoEmision.RECEPCION_PAGOS);
		FiscalEntity fiscalEntity = null;
		Customer customer = null;

		// referencia
		if (linea[0] == null) {
			referencia = "";
		} else {
			referencia = linea[0].toString().trim();
			System.out.println("Referencia: " + referencia);
		}

		/* Emisor Posicion 1--row 1 */
		comp.setEmisor(new CfdiEmisor());
		if (linea[1] == null) {
			comp.getEmisor().setRfc("");
		} else {
			fiscalEntity = new FiscalEntity();
			fiscalEntity.setTaxID(linea[1].toString());
			fiscalEntity = fiscalEntityManager.findByRFCA(fiscalEntity);
			comp.getEmisor().setRfc(linea[1].toString());
		}

		/* Entidad Posicion 1 */
		if (linea[1] == null) {
			comp.getEmisor().setRfc("");
		} else {
			comp.getEmisor().setRfc(linea[1].toString());
			System.out.println("RFC: " + comp.getEmisor().getRfc());
		}
		/* Serie Posicion 1 */
		comp.setSerie(linea[2].toString().trim());
		if (linea[2] == null) {
			comp.setSerie(null);
		} else {
			if (linea[2].toString().trim().length() > 0) {
				comp.setSerie(linea[2].toString().trim());
				System.out.println("Serie: " + comp.getSerie());
			} else {
				comp.setSerie("");
			}
		}
		/* Moneda */
		comp.setMoneda("XXX");
		/* Tipo Comprobante */
		comp.setTipoDeComprobante("P");

		/* Regimen fiscal Posicion 3 */
		if (linea[3] == null || linea[3].toString().trim().equals("")) {
			comp.getEmisor().setRegimenFiscal("");
		} else {
			comp.getEmisor().setRegimenFiscal(linea[3].toString());
			System.out.println("Regimen fiscal: " + comp.getEmisor().getRegimenFiscal());
		}

		/* RFC Posicion 4 */
		boolean readFromFile = false;
		comp.setReceptor(new CfdiReceptor());
		if (linea[4] == null) {
			comp.setCustomerRfcCellValue("");
		} else {
			if (linea[4].toString().trim().equals("")) {
				comp.setReceptor(new CfdiReceptor());
				comp.setCustomerRfcCellValue("");
			} else {
				comp.setCustomerRfcCellValue(linea[4].toString().trim());
				if (linea[4].toString().trim().toUpperCase().equals("XEXX010101000")
						|| linea[4].toString().trim().toUpperCase().equals("XAXX010101000")
						|| linea[4].toString().trim().equals("XEXE010101000")) {
					// evaluacion del id de extranjero
					if (linea[6] == null || linea[3].toString().trim().length() == 0) {
						comp.setStrIDExtranjero("");
						readFromFile = true;
					} else {
						if (!linea[6].equals("")) {
							comp.setReceptor(new CfdiReceptor());
							readFromFile = true;
						} else {
							String strIDExtranjero = linea[6].toString().trim();
							comp.setStrIDExtranjero(strIDExtranjero);
							System.out.println("ID Extranjero: " + strIDExtranjero);
							customer = customerManager.findByIdExtranjero(strIDExtranjero);
							comp.setReceptor(new CfdiReceptor());
							if (customer != null) {
								// rfc
								comp.getReceptor().setRfc(customer.getTaxId());
								// nombre
								comp.getReceptor().setNombre(customer.getPhysicalName());
								// numRegIDTrib
								if (linea[7] != null || linea[7].toString().length() > 0) {
									comp.getReceptor().setNumRegIdTrib(linea[7].toString());
								} else {
									comp.getReceptor().setNumRegIdTrib("");
								}
								// asignacion de USOCFDI
								comp.getReceptor().setUsoCFDI("P01");
								// domicilio
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
							} else {
								readFromFile = true;
							}
						}
					}
				} else {
					if (fiscalEntity != null) {
						customer = customerManager.get(linea[4].toString().trim(),
								String.valueOf(fiscalEntity.getId()));
						if (customer != null) {
							// rfc
							comp.getReceptor().setRfc(customer.getTaxId());
							// nombre
							comp.getReceptor().setNombre(customer.getPhysicalName());
							// numRegIDTrib
							if (linea[7] != null || linea[7].toString().length() > 0) {
								comp.getReceptor().setNumRegIdTrib(linea[7].toString());
							} else {
								comp.getReceptor().setNumRegIdTrib("");
							}
							// asignacion de USOCFDI
							comp.getReceptor().setUsoCFDI("P01");
							// domicilio
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
						} // fin del if customer != null
					} else {
						readFromFile = true;
					}
				}
			}
		} // fin del IF

		if (readFromFile) {
			comp.setReceptor(new CfdiReceptor());
			comp.getReceptor().setDomicilio(new CfdiDomicilio());
			comp.getReceptor().setRfc(comp.getCustomerRfcCellValue());
			// nombre
			if (linea[5] == null || linea[5].toString().trim().equals("")) {
				comp.getReceptor().setNombre("");
			} else {
				comp.getReceptor().setNombre(linea[5].toString());
			}
			// numRegIDTrib
			if (linea[7] != null || linea[7].toString().length() > 0) {
				comp.getReceptor().setNumRegIdTrib(linea[7].toString());
			} else {
				comp.getReceptor().setNumRegIdTrib("");
			}
			// asignacion de USOCFDI
			comp.getReceptor().setUsoCFDI("P01");
			// referencia
			if (linea[9] == null || linea[9].toString().trim().equals("")) {
				comp.getReceptor().getDomicilio().setReferencia("");
			} else {
				comp.getReceptor().getDomicilio().setReferencia(linea[9].toString());
			}
		}

		comp.setAddenda(new CfdiAddendaSantanderV1());
		comp.getAddenda().setInformacionPago(new CfdiAddendaInformacionPago());
		/* Numero de cuenta */
		if (linea[8] == null || linea[8].toString().trim().equals("")) {
			comp.setNumeroCuenta("");
		} else {
			String strNumCtaPago = linea[8].toString();
			comp.setNumeroCuenta(strNumCtaPago);
			System.out.println("Numero de cuenta: " + comp.getNumeroCuenta());
		}
		/* Codigo cliente */
		comp.getAddenda().setInformacionEmision(new CfdiAddendaInformacionEmision());
		if (linea[10] == null || linea[10].toString().trim().equals("") || linea[10].toString().trim().length() == 0) {
			comp.getAddenda().getInformacionEmision().setCodigoCliente("");
		} else {
			comp.getAddenda().getInformacionEmision().setCodigoCliente(linea[10].toString().trim());
			System.out.println(
					"Cod. Cliente Comp: " + comp.getAddenda().getInformacionEmision().getCodigoCliente().toString());
		}
		// posicion 11 contrato
		if (linea[11] == null || linea[11].toString().trim().equals("") || linea[11].toString().trim().length() == 0) {
			comp.getAddenda().getInformacionEmision().setContrato("");
		} else {
			comp.getAddenda().getInformacionEmision().setContrato(linea[11].toString().trim());
			System.out.println("Contrato Comp: " + comp.getAddenda().getInformacionEmision().getContrato().toString());
		}
		// posicion 12 periodo
		if (linea[12] == null || linea[12].toString().trim().equals("") || linea[12].toString().trim().length() == 0) {
			comp.getAddenda().getInformacionEmision().setPeriodo("");
		} else {
			comp.getAddenda().getInformacionEmision().setPeriodo(linea[12].toString().trim());
			System.out.println("Periodo Comp: " + comp.getAddenda().getInformacionEmision().getPeriodo().toString());
		}
		/* Centro de costos */
		if (linea[13] == null || linea[13].toString().trim().equals("") || linea[13].toString().trim().length() == 0) {
			comp.getAddenda().getInformacionEmision().setCentroCostos("");
		} else {
			comp.getAddenda().getInformacionEmision().setCentroCostos(linea[13].toString().toString().trim());
			System.out.println("Centro costos: " + comp.getAddenda().getInformacionEmision().getCentroCostos());
		}
		comp.getAddenda().setCampoAdicional(new HashMap<String, String>());
		/* Descriptcion concepto */
		if (linea[14] == null) {
			comp.getAddenda().getCampoAdicional().put("DESCRIPCIÓN CONCEPTO", "");
		} else {
			comp.getAddenda().getCampoAdicional().put("DESCRIPCIÓN CONCEPTO", linea[14].toString().trim());
			System.out.println("Campo adicional: " + comp.getAddenda().getCampoAdicional());
		}

		comp.setIvaCellValue(linea[15].toString());
		/* Tipo adenda */
		comp.getAddenda().setInmuebles(new CfdiAddendaInmuebles());
		if (linea[16] != null) {
			if (linea[16].toString().contains(".")) {
				System.out.println("*** response Dentro IF AMDA: " + linea[16].toString());
				String words[] = linea[16].toString().split("\\.");
				linea[16] = words[0];
				System.out.println("*** response Dentro IF despues AMDA: " + linea[16].toString());
			}
			comp.setTipoAddendaCellValue(linea[16].toString());
			System.out.println("tipoAddenda:" + linea[16].toString());
			if (validaDatoRE(linea[16].toString().trim(), RE_DECIMAL)
					|| linea[16].trim().equals("")) {
				String strTipoAddenda = linea[16].toString();
				System.out.println("tipoAddendaClean: " + strTipoAddenda);
				if (strTipoAddenda.equals("1") || strTipoAddenda.equals("2") || strTipoAddenda.equals("3")) {
					comp.getAddenda().getInformacionPago().setNombreBeneficiario("");
					comp.getAddenda().getInformacionPago().setInstitucionReceptora("");
					comp.getAddenda().getInformacionPago().setNumeroCuenta("");
					comp.getAddenda().getInformacionPago().setNumProveedor("");
					if (linea[17] == null) {
						comp.getAddenda().getInformacionPago().setEmail("");
					} else {

						if (!linea[17].toString().trim().equals("")) {
							if (validaDatoRE(linea[17].toString().trim(), RE_MAIL)) {
								comp.getAddenda().getInformacionPago().setEmail(linea[17].toString().trim());
							} else {
								comp.getAddenda().getInformacionPago().setEmail("");
							}
						} else {
							comp.getAddenda().getInformacionPago().setEmail("");
							System.out.println("Email: " + comp.getAddenda().getInformacionPago().getEmail());
						}

					}
				}
				if (strTipoAddenda.equals("1")) {
					// invoice.setTipoAddenda(strTipoAddenda.trim());
					if (linea[18] == null) {
						comp.getAddenda().getInformacionPago().setCodigoISOMoneda(null);
					} else {
						if (!linea[18].toString().trim().equals("")) {
							CodigoISO codigoISOLog = new CodigoISO();
							codigoISOLog = codigoISOManager.findByCodigo(linea[18].toString().trim().toUpperCase());
							if (codigoISOLog != null) {
								comp.getAddenda().getInformacionPago()
										.setCodigoISOMoneda(linea[18].toString().trim().toUpperCase());
							} else {
								comp.getAddenda().getInformacionPago().setCodigoISOMoneda("");
							}
						} else {
							comp.getAddenda().getInformacionPago().setCodigoISOMoneda("");
						}

					}

					if (linea[19] == null) {
						comp.getAddenda().getInformacionPago().setOrdenCompra(null);
					} else {
						if (linea[19].toString().trim().equals("")) {
							comp.getAddenda().getInformacionPago().setOrdenCompra("");
						} else {
							String strOrdenCompra = linea[19].toString();
							if (!strOrdenCompra.equals("")) {
								comp.getAddenda().getInformacionPago().setOrdenCompra(strOrdenCompra);
								System.out.println("orden compra log: " + strOrdenCompra);
							} else {
								comp.getAddenda().getInformacionPago().setOrdenCompra("");
							}
						}
					}

					if (linea[20] == null) {
						comp.getAddenda().getInformacionPago().setPosCompra(null);
					} else {
						if (linea[20].toString().trim().equals("")) {
							comp.getAddenda().getInformacionPago().setPosCompra("");
						} else {
							String strPosicionCompra = linea[20].toString().trim();
							comp.getAddenda().getInformacionPago().setPosCompra(strPosicionCompra);
							System.out.println("posicion compra: " + strPosicionCompra);
						}
					}

				} else if (strTipoAddenda.equals("2")) {
					// invoice.setTipoAddenda(strTipoAddenda.trim());
					if (linea[18] == null) {
						comp.getAddenda().getInformacionPago().setCodigoISOMoneda(null);
					} else {

						if (!linea[18].toString().trim().equals("")) {
							CodigoISO codigoISOFin = new CodigoISO();
							codigoISOFin = codigoISOManager.findByCodigo(linea[18].toString().trim().toUpperCase());
							if (codigoISOFin != null) {
								comp.getAddenda().getInformacionPago()
										.setCodigoISOMoneda(linea[18].toString().trim().toUpperCase());
							} else {
								comp.getAddenda().getInformacionPago().setCodigoISOMoneda("");
							}
						} else {
							comp.getAddenda().getInformacionPago().setCodigoISOMoneda("");
						}

					}

					if (linea[21] == null) {
						comp.getAddenda().getInformacionPago().setCuentaContable(null);
					} else {
						if (linea[21].toString().trim().equals("")) {
							comp.getAddenda().getInformacionPago().setCuentaContable("");
						} else {
							String strCuentaContableFin = linea[21].toString().trim();
							comp.getAddenda().getInformacionPago().setCuentaContable(strCuentaContableFin);
							System.out.println("cuenta contable Fin: " + strCuentaContableFin);
						}
					}
					if (linea[22] == null) {
						comp.getAddenda().getInformacionEmision().setCentroCostos("");
					} else {
						if (linea[22].toString().trim().equals("")) {
							comp.getAddenda().getInformacionEmision().setCentroCostos("");
						} else {
							System.out.println("centro costos: " + linea[22].trim());
							String strCentroCostosFin = linea[22].trim();
//							if (!strCentroCostosFin
//									.equals(comp.getAddenda().getInformacionEmision().getCentroCostos())) {
//								comp.getAddenda().getInformacionEmision().setCentroCostos("");
//							}
							comp.getAddenda().getInformacionEmision().setCentroCostos(strCentroCostosFin);
						}
					}

				} else if (strTipoAddenda.equals("3")) {
					// invoice.setTipoAddenda("3");
					if (linea[18] == null) {
						comp.getAddenda().getInformacionPago().setCodigoISOMoneda(null);
					} else {

						if (!linea[18].toString().trim().equals("")) {
							CodigoISO codigoISOArr = new CodigoISO();
							codigoISOArr = codigoISOManager.findByCodigo(linea[18].toString().trim().toUpperCase());
							if (codigoISOArr != null) {
								comp.getAddenda().getInformacionPago()
										.setCodigoISOMoneda(linea[18].toString().trim().toUpperCase());
							} else {
								comp.getAddenda().getInformacionPago().setCodigoISOMoneda("");
							}
						} else {
							comp.getAddenda().getInformacionPago().setCodigoISOMoneda("");
						}

					}

					if (linea[23] == null) {
						comp.getAddenda().getInmuebles().setNumContrato(null);
					} else {
						if (linea[23].toString().trim().equals("")) {
							comp.getAddenda().getInmuebles().setNumContrato("");
						} else {
							String strNumeroContratoArr = linea[23].trim();
							comp.getAddenda().getInmuebles().setNumContrato(strNumeroContratoArr);
						}
					}
					if (linea[24] == null) {
						comp.getAddenda().getInmuebles().setFechaVencimiento(null);
					} else {
						if (linea[24].trim().equals("")) {
							comp.getAddenda().getInmuebles().setFechaVencimiento("");
						} else {
							String strFechaVencimientoArr = linea[24].trim();
							comp.getAddenda().getInmuebles().setFechaVencimiento(strFechaVencimientoArr);
							System.out.println("fecha vencimiento Arr:" + strFechaVencimientoArr);

						}

					}
				} else if (strTipoAddenda.equals("0") || strTipoAddenda.equals("")) {
					comp.getAddenda().getInformacionPago().setEmail("");
					comp.getAddenda().getInformacionPago().setOrdenCompra("");
					// invoice.setTipoAddenda(strTipoAddenda.trim());
					
					if (linea[25] == null) {
						comp.getAddenda().getInformacionPago().setNombreBeneficiario("");
					} else {
						comp.getAddenda().getInformacionPago().setNombreBeneficiario(linea[25].trim());
					}
					
					if (linea[26] == null) {
						comp.getAddenda().getInformacionPago().setInstitucionReceptora("");
					} else {
						comp.getAddenda().getInformacionPago().setInstitucionReceptora(linea[26].trim());
					}

					if (linea[27] == null) {
						comp.getAddenda().getInformacionPago().setNumeroCuenta("");
					} else {
						comp.getAddenda().getInformacionPago().setNumeroCuenta(linea[27].trim());
					}
					
					if (linea[28] == null) {
						comp.getAddenda().getInformacionPago().setNumProveedor("");
					} else {
						comp.getAddenda().getInformacionPago().setNumProveedor(linea[28].toString().trim());
					}

				}
			}
		}
		/* Tipo de operacion */
		comp.setComplemento(new CfdiComplemento());
		if (!linea[29].toString().trim().equals("")) {
			if (linea[29].toString().trim().toLowerCase().equals("compra")
					|| linea[29].toString().trim().toLowerCase().equals("venta")) {
				comp.getComplemento().setDivisaTipoOperacion(linea[29].toString().toLowerCase().trim());
			} else {
				comp.getComplemento().setDivisaTipoOperacion("");
			}
		} else {
			comp.getComplemento().setDivisaTipoOperacion("");
		}

		/* Valida rfc cliente extranjero */
		if (comp.getReceptor() != null && comp.getReceptor().getRfc() != null) {
			if (comp.getReceptor().getRfc().toUpperCase().equals("XEXX010101000")
					|| comp.getReceptor().getRfc().toUpperCase().equals("XAXX010101000")
					|| comp.getReceptor().getRfc().toUpperCase().equals("XEXE010101000")) {
				if (comp.getReceptor().getDomicilio().getPais() != null) {
					comp.getReceptor().setResidenciaFiscal(comp.getReceptor().getDomicilio().getPais());
					comp.getReceptor().setNumRegIdTrib(comp.getReceptor().getNumRegIdTrib());
				}
			}
		}
		// No de autorización
		if (linea[30] == null) {
			comp.setNoAutorizacion("");
		} else {
			comp.setNoAutorizacion(linea[30].toString().trim());
		}
		// UUID
		comp.getComplemento().setTimbreFiscalDigital(new CfdiTimbreFiscalDigital());
		if (linea[31] == null || linea[31].toString() != "") {
			comp.getComplemento().getTimbreFiscalDigital().setUuid("");
		} else {
			comp.getComplemento().getTimbreFiscalDigital().setUuid(linea[31].toString().trim());

		}

		comp.setCfdiRelacionados(new CfdiRelacionado());
		comp.getCfdiRelacionados().setTipoRelacion("04");

		/* SECCION DEL LLENADO DEL CONCEPTO */

		List<CfdiConcepto> conceptos = new ArrayList<CfdiConcepto>();
		CfdiConcepto cfdi = new CfdiConcepto();
		// valores estaticos
		if (comp.getTipoDeComprobante().equals("P")) {
			cfdi.setClaveProdServ("84111506");
			cfdi.setCantidad(new BigDecimal("1"));
			cfdi.setClaveUnidad("ACT");
			cfdi.setDescripcion("Pago");
			cfdi.setValorUnitario(BigDecimal.ZERO);
			cfdi.setImporte(new BigDecimal("0.00"));
			//cfdi.setUnidad("SERVICIO");
		}

		// evaluacion del segunda hoja COMPLEMENTOS

		conceptos.add(cfdi);
		comp.setConceptos(conceptos);
		comp.setTotal(new BigDecimal("0.00"));
		comp.setSubTotal(new BigDecimal("0.00"));
		comp.setDescuento(new BigDecimal("0.00"));
		
		comp.setLugarExpedicion("01219");
		System.out.println("Tamaño de conceptos: " + conceptos.size());
		int nextPosition = 32, maxPosition = linea.length - 1, posicionComplemento = 1;
		ComplementoPago complemento = null;
		List<ComplementoPago> pagos = new ArrayList<ComplementoPago>();
		boolean finFactura = false;
		while (nextPosition <= maxPosition) {
			if (linea[nextPosition].trim().toUpperCase().equals("FINFACTURA")) {
				finFactura = true;
				break;
			}
			// Posicion 1: Referencia Factura
			if (posicionComplemento == 1) {
				complemento = new ComplementoPago();
			}
			// Posicion 2: Fecha Pago
			if (posicionComplemento == 2) {
				if(linea[nextPosition].trim().isEmpty()) {
					complemento.setFechaPago(new Date());
				}else {
					Date date1= new Date();
					try {
						date1 = new SimpleDateFormat("yyyy-mm-dd'T'hh:mm:ss").parse(linea[nextPosition].trim());
					} catch (ParseException e) {
						e.printStackTrace();
					}
					complemento.setFechaPago(date1);
				}
			}
			// Posicion 3: Forma Pago
			if (posicionComplemento == 3) {
				complemento.setFormaPagoP(linea[nextPosition]);
			}
			// Posicion 4: Moneda Pago
			if (posicionComplemento == 4) {
				complemento.setMonedaPago(linea[nextPosition]);
			}
			// Posicion 5: Tipo Cambio Pago
			if (posicionComplemento == 5) {
				BigDecimal tcPago = new BigDecimal(linea[nextPosition].trim().equals("") ? "0" : linea[nextPosition]);
				complemento.setTipoCambioPago(tcPago);
			}
			// Posicion 6: Monto
			if (posicionComplemento == 6) {
				BigDecimal monto = new BigDecimal(linea[nextPosition].trim().equals("") ? "0" : linea[nextPosition]);
				complemento.setMonto(monto);
			}
			// Posicion 7: Numero Operacion
			if (posicionComplemento == 7) {
				complemento.setNumeroOperacion(linea[nextPosition]);
			}
			// Posicion 8: RFC Emisor Cuenta Orden
			if (posicionComplemento == 8) {
				complemento.setRfcEmisorCuentaOrden(linea[nextPosition]);
			}
			// Posicion 9: Nombre Banco Ordinario Ext.
			if (posicionComplemento == 9) {
				complemento.setNombreBancoOrdinarioExt(linea[nextPosition]);
			}
			// Posicion 10: Cuenta Ordenante
			if (posicionComplemento == 10) {
				complemento.setCuentaOrdenante(linea[nextPosition]);
			}
			// Posicion 11: RFC Emisor Cuenta Beneficiario
			if (posicionComplemento == 11) {
				complemento.setRfcEmisorCtaBeneficiario(linea[nextPosition]);
			}
			// Posicion 12: Cuenta Beneficiario
			if (posicionComplemento == 12) {
				complemento.setCuentaBeneficiario(linea[nextPosition]);
			}
			// Posicion 13: Tipo Cadena Pago
			if (posicionComplemento == 13) {
				complemento.setTipoCadenaPago(linea[nextPosition]);
			}
			// Posicion 14: Cadena Pago
			if (posicionComplemento == 14) {
				complemento.setCadenaPago(linea[nextPosition]);
			}
			// Posicion 15: ID Documento
			if (posicionComplemento == 15) {
				complemento.setIdDocumento(linea[nextPosition]);
			}
			// Posicion 16: Serie
			if (posicionComplemento == 16) {
				complemento.setSeriePago(linea[nextPosition]);
			}
			// Posicion 17: Folio
			if (posicionComplemento == 17) {
				complemento.setFolioPago(linea[nextPosition]);
			}
			// Posicion 18: Moneda DR
			if (posicionComplemento == 18) {
				complemento.setMonedaDR(linea[nextPosition]);
			}
			// Posicion 19: Tipo Cambio DR
			if (posicionComplemento == 19) {
				BigDecimal tcDr = new BigDecimal(linea[nextPosition].trim().equals("") ? "0" : linea[nextPosition]);
				complemento.setTipoCambioDR(tcDr);
			}
			// Posicion 20: Metodo Pago DR
			if (posicionComplemento == 20) {
				complemento.setMetodoPagoDR(linea[nextPosition]);
			}
			// Posicion 21: Num Parcialidad
			if (posicionComplemento == 21) {
				complemento.setNumParcialidad(linea[nextPosition]);
			}
			// Posicion 22: Imp Saldo Anterior
			if (posicionComplemento == 22) {
				BigDecimal impSalAnt = new BigDecimal(
						linea[nextPosition].trim().equals("") ? "0" : linea[nextPosition]);
				complemento.setImpSaldoAnterior(impSalAnt);
			}
			// Posicion 23: Impuesto Pagado
			if (posicionComplemento == 23) {
				BigDecimal impSalPag = new BigDecimal(
						linea[nextPosition].trim().equals("") ? "0" : linea[nextPosition]);
				complemento.setImpuestoPagado(impSalPag);
			}
			// Posicion 24: Imp Saldo Insoluto
			if (posicionComplemento == 24) {
				BigDecimal impSalInso = new BigDecimal(
						linea[nextPosition].trim().equals("") ? "0" : linea[nextPosition]);
				complemento.setImpSaldoInsoluto(impSalInso);
			}

			nextPosition++;
			if (posicionComplemento == 24) {
				pagos.add(complemento);
				posicionComplemento = 1;
			} else {
				posicionComplemento++;
			}
		}
		comp.setComplementPagos(pagos);
		comp.setFinFactura(finFactura);
		return comp;
	}
}
