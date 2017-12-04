package com.interfactura.firmalocal.xml.util;

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
import com.interfactura.firmalocal.persistence.IvaManager;
import com.interfactura.firmalocal.xml.TagsXML;
import com.interfactura.firmalocal.xml.util.ValidationConstants.TipoEmision;


@Service
public class UtilCFDIDivisas {

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
	
	public CfdiComprobanteFiscal fillComprobanteDivisas(CfdiComprobanteFiscal comp,Row row, FiscalEntity fiscalEntity, int factura,Customer customer, int lastCellNum) {
		//
		comp = new CfdiComprobanteFiscal();
		comp.setTipoEmision(TipoEmision.DIVISAS);
		/* Emisor Posicion 0--row 0 */
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
		
		/* Forma de pago */
		if (row.getCell(2) == null || row.getCell(2).toString().trim().equals("")) {
			comp.setFormaPago("");
		} else {
			comp.setFormaPago(row.getCell(2).toString());			
		}
		
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
		

		/* Posicion 5 Moneda */
		if (row.getCell(5) == null && row.getCell(5).toString().trim().equals("")) {
			comp.setMoneda("");
		} else {
			comp.setMoneda(row.getCell(5).toString().trim());	
		}

		/* Tipo de cambio */
		if (row.getCell(6) == null && row.getCell(6).toString().trim().equals("")) {
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
		
		/* Metodo de pago */
		if (row.getCell(8) == null || row.getCell(8).toString().trim().equals("")) {
			comp.setMetodoPago("");
		}else{
			comp.setMetodoPago(row.getCell(8).toString());			
		}

		/* Regimen fiscal */
		if (row.getCell(9) == null || row.getCell(9).toString().trim().equals("")) {
			comp.getEmisor().setRegimenFiscal("");
		} else {
			comp.getEmisor().setRegimenFiscal(row.getCell(9).toString());			
		}

	
		/* RFC del cliente */
		comp.setReceptor(new CfdiReceptor());
		boolean readFromFile = false;
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

					//evaluacion del id de extranjero
					if (row.getCell(12) == null || row.getCell(12).toString().trim().length() == 0) {
						comp.setStrIDExtranjero("");
						readFromFile= true;
					} else {
						String strCellType = checkCellType(row.getCell(12));
						if (!strCellType.equals("")) {
							comp.setReceptor(new CfdiReceptor());
							readFromFile= true;
						} else {
							String strIDExtranjero = row.getCell(12).toString().trim();
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
								if (row.getCell(14) == null || row.getCell(14).toString().trim().equals("")) {
									comp.getReceptor().setNumRegIdTrib("");
								} else {
									comp.getReceptor().setNumRegIdTrib(row.getCell(14).toString());
								}
								//uso cfdi
								if (row.getCell(13) == null || row.getCell(13).toString().trim().length() == 0) {
									comp.getReceptor().setUsoCFDI("D04");
								} else {
									comp.getReceptor().setUsoCFDI(row.getCell(13).toString());									
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
						customer = customerManager.get(row.getCell(10).toString().trim(),String.valueOf(fiscalEntity.getId()));
						
						if (customer != null) {
							//rfc
							comp.getReceptor().setRfc(customer.getTaxId());
							//nombre
							comp.getReceptor().setNombre(customer.getPhysicalName());
							//NumRegIdTrib
							if (row.getCell(14) == null || row.getCell(14).toString().trim().equals("")) {
								comp.getReceptor().setNumRegIdTrib("");
							} else {
								comp.getReceptor().setNumRegIdTrib(row.getCell(14).toString());
							}
							//uso cfdi
							if (row.getCell(13) == null || row.getCell(13).toString().trim().length() == 0) {
								comp.getReceptor().setUsoCFDI("D04");
							} else {
								comp.getReceptor().setUsoCFDI(row.getCell(13).toString());								
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
		if(readFromFile){
			comp.setReceptor(new CfdiReceptor());
			comp.getReceptor().setDomicilio(new CfdiDomicilio());
			comp.getReceptor().setRfc(comp.getCustomerRfcCellValue());
			//nombre
			if (row.getCell(11) == null || row.getCell(11).toString().trim().equals("")) {
				comp.getReceptor().setNombre("");
			}else{
				comp.getReceptor().setNombre(row.getCell(11).toString());
			}
			//referencia
			if (row.getCell(16) == null || row.getCell(16).toString().trim().equals("")) {
				comp.getReceptor().getDomicilio().setReferencia("");
			}else{
				comp.getReceptor().getDomicilio().setReferencia(row.getCell(16).toString());
			}
		}
		
		
		comp.setAddenda(new CfdiAddendaSantanderV1());
		comp.getAddenda().setInformacionPago(new CfdiAddendaInformacionPago());
		/* Numero de cuenta */
		if (row.getCell(15) == null || row.getCell(15).toString().trim().equals("")) {
			comp.setNumeroCuenta("");
		} else {			
			String strCellType = checkCellType(row.getCell(15));
			if (!strCellType.equals("")) {
				comp.setNumeroCuenta("");
			} else {
				String strNumCtaPago = row.getCell(15).toString();
				comp.setNumeroCuenta(strNumCtaPago);
			}	

		}
		
		/* Codigo cliente */
		comp.getAddenda().setInformacionEmision(new CfdiAddendaInformacionEmision());
		if (row.getCell(17) == null || row.getCell(17).toString().trim().equals("")) {
			comp.getAddenda().getInformacionEmision().setCodigoCliente("");
		} else {
			String strCellType = checkCellType(row.getCell(17));
			if (!strCellType.equals("")) {
				comp.getAddenda().getInformacionEmision().setCodigoCliente("");
			} else {
				if (row.getCell(17).toString().trim().length() > 0) {
					String strCodigoCliente = row.getCell(17).toString();
					comp.getAddenda().getInformacionEmision().setCodigoCliente(strCodigoCliente);
				} else {
					comp.getAddenda().getInformacionEmision().setCodigoCliente(row.getCell(17).toString().trim());
			
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
					comp.getAddenda().getInformacionEmision().setContrato("");
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
					comp.getAddenda().getInformacionEmision().setPeriodo("");
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
					comp.getAddenda().getInformacionEmision().setCentroCostos("");
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
		//new variable this
		comp.setIvaCellValue(row.getCell(22).toString());


		/* Tipo adenda */
		comp.getAddenda().setInmuebles(new CfdiAddendaInmuebles());
		if (row.getCell(23) != null) {
			if (row.getCell(23).toString().contains(".")) {
				System.out.println("*** response Dentro IF AMDA: " + row.getCell(23).toString());
				String words[] = row.getCell(23).toString().split("\\.");
				row.getCell(23).setCellValue(words[0]);
				System.out.println("*** response Dentro IF despues AMDA: " + row.getCell(23).toString());
			}
			comp.setTipoAddendaCellValue(row.getCell(23).toString());
			System.out.println("tipoAddenda:" + row.getCell(23).toString());

			if (validaDatoRE(row.getCell(23).toString().trim(), RE_DECIMAL)) {
				String strTipoAddenda = row.getCell(23).toString();
				System.out.println("tipoAddendaClean: " + strTipoAddenda);
				if (strTipoAddenda.equals("1") || strTipoAddenda.equals("2") || strTipoAddenda.equals("3")) {
					comp.getAddenda().getInformacionPago().setNombreBeneficiario("");
					comp.getAddenda().getInformacionPago().setInstitucionReceptora("");
					comp.getAddenda().getInformacionPago().setNumeroCuenta("");
					comp.getAddenda().getInformacionPago().setNumProveedor("");
					//email proveedor
					if (row.getCell(24) == null) {
						comp.getAddenda().getInformacionPago().setEmail("");
					} else {
						if (!row.getCell(24).toString().trim().equals("")) {
							if (validaDatoRE(row.getCell(24).toString().trim(), RE_MAIL)) {
								comp.getAddenda().getInformacionPago().setEmail(row.getCell(24).toString().trim());
							} else {
								comp.getAddenda().getInformacionPago().setEmail("");
							}
						} else {
							comp.getAddenda().getInformacionPago().setEmail("");
						}
					}
					//codigo iso moneda
					if (row.getCell(25) == null) {
						comp.getAddenda().getInformacionPago().setCodigoISOMoneda(null);
					} else {
						if (!row.getCell(25).toString().trim().equals("")) {
							comp.getAddenda().getInformacionPago()
								.setCodigoISOMoneda(row.getCell(25).toString().trim().toUpperCase());

						} else {
							comp.getAddenda().getInformacionPago().setCodigoISOMoneda("");
						}
					}
				}
				if (strTipoAddenda.equals("1")) {
					//orden compra
					if (row.getCell(26) == null) {
						comp.getAddenda().getInformacionPago().setOrdenCompra(null);
					} else {
						if (row.getCell(26).toString().trim().equals("")) {
							comp.getAddenda().getInformacionPago().setOrdenCompra("");
						} else {
							comp.getAddenda().getInformacionPago().setOrdenCompra(row.getCell(26).toString().trim());
//							String strCellTypeOrdenLog = checkCellType(row.getCell(26));
//							if (!strCellTypeOrdenLog.equals("")) {
//								comp.getAddenda().getInformacionPago().setOrdenCompra("");
//							} else {
//								String strOrdenCompra = row.getCell(26).toString();
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
					if (row.getCell(27) == null) {
						comp.getAddenda().getInformacionPago().setPosCompra(null);
					} else {
						if (row.getCell(27).toString().trim().equals("")) {
							comp.getAddenda().getInformacionPago().setPosCompra("");
						} else {
							comp.getAddenda().getInformacionPago().setPosCompra(row.getCell(27).toString().trim());
//							String strCellTypePosicionLog = checkCellType(row.getCell(27));
//							if (!strCellTypePosicionLog.equals("")) {
//								comp.getAddenda().getInformacionPago().setPosCompra("");
//							} else {
//								String strPosicionCompra = row.getCell(27).toString();
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
					if (row.getCell(28) == null) {
						comp.getAddenda().getInformacionPago().setCuentaContable(null);
					} else {
						if (row.getCell(28).toString().trim().equals("")) {
							comp.getAddenda().getInformacionPago().setCuentaContable("");
						} else {
							comp.getAddenda().getInformacionPago().setCuentaContable(row.getCell(28).toString().trim());
//							String strCellTypeContableFin = checkCellType(row.getCell(28));
//							if (!strCellTypeContableFin.equals("")) {
//								comp.getAddenda().getInformacionPago().setCuentaContable("");
//							} else {
//								String strCuentaContableFin = row.getCell(28).toString();
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
					if (row.getCell(29) == null) {
						comp.getAddenda().getInformacionEmision().setCentroCostos("");
					} else {
						if (row.getCell(29).toString().trim().equals("")) {
							comp.getAddenda().getInformacionEmision().setCentroCostos("");
						} else {
							comp.getAddenda().getInformacionEmision().setCentroCostos(row.getCell(29).toString().trim());
//							String strCellTypeCostosFin = checkCellType(row.getCell(29));
//							if (!strCellTypeCostosFin.equals("")) {
//								comp.getAddenda().getInformacionEmision().setCentroCostos("");
//							} else {
//								if (!row.getCell(29).toString().trim().equals("")) {
//									System.out.println("centro costos: " + row.getCell(29).toString());
//									String strCentroCostosFin = row.getCell(29).toString();
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
					//numContrato
					if (row.getCell(30) == null) {
						comp.getAddenda().getInmuebles().setNumContrato(null);
					} else {
						if (row.getCell(30).toString().trim().equals("")) {
							comp.getAddenda().getInmuebles().setNumContrato("");
						} else {
							comp.getAddenda().getInmuebles().setNumContrato(row.getCell(30).toString().trim());
//							String strCellTypeContratoArr = checkCellType(row.getCell(30));
//							if (!strCellTypeContratoArr.equals("")) {
//								comp.getAddenda().getInmuebles().setNumContrato("");
//							} else {
//								String strNumeroContratoArr = row.getCell(30).toString();
//								if (!strNumeroContratoArr.equals("")) {
//									comp.getAddenda().getInmuebles().setNumContrato(strNumeroContratoArr);
//								} else {
//									comp.getAddenda().getInmuebles().setNumContrato("");
//								}
//							}
						}

					}
					//fechaVencimiento
					if (row.getCell(31) == null) {
						comp.getAddenda().getInmuebles().setFechaVencimiento(null);
					} else {
						if (row.getCell(31).toString().trim().equals("")) {
							comp.getAddenda().getInmuebles().setFechaVencimiento("");
						} else {
							comp.getAddenda().getInmuebles().setFechaVencimiento(row.getCell(31).toString().trim());
//							String strCellTypeVencimientoArr = checkCellType(row.getCell(31));
//							if (!strCellTypeVencimientoArr.equals("")) {
//								comp.getAddenda().getInmuebles().setFechaVencimiento("");
//							} else {
//								String strFechaVencimientoArr = row.getCell(31).toString();
//								if (!strFechaVencimientoArr.equals("")) {
//									comp.getAddenda().getInmuebles().setFechaVencimiento(strFechaVencimientoArr);
//									System.out.println("fecha vencimiento Arr:" + strFechaVencimientoArr);
//								} else {
//									comp.getAddenda().getInmuebles().setFechaVencimiento("");
//								}
//							}
						}

					}

				} else if(strTipoAddenda.equals("0") || strTipoAddenda.equals("")) {
					comp.getAddenda().getInformacionPago().setEmail("");
					comp.getAddenda().getInformacionPago().setOrdenCompra("");

					//Nombre Beneficiario
					if (row.getCell(32) == null) {
						comp.getAddenda().getInformacionPago().setNombreBeneficiario("");
					} else {
						comp.getAddenda().getInformacionPago().setNombreBeneficiario(row.getCell(32).toString().trim());
					}
					//institucion receptora
					if (row.getCell(33) == null) {
						comp.getAddenda().getInformacionPago().setInstitucionReceptora("");
					} else {
						comp.getAddenda().getInformacionPago()
								.setInstitucionReceptora(row.getCell(33).toString().trim());
					}
					//Numero de cuenta
					if (row.getCell(34) == null) {
						comp.getAddenda().getInformacionPago().setNumeroCuenta("");
					} else {
						if (row.getCell(34).toString().trim().equals("")) {
							comp.getAddenda().getInformacionPago().setNumeroCuenta(row.getCell(34).toString().trim());
						} else {
							comp.getAddenda().getInformacionPago().setNumeroCuenta(row.getCell(34).toString().trim());
//							String strCellTypeC = checkCellType(row.getCell(34));
//							if (!strCellTypeC.equals("")) {
//								comp.getAddenda().getInformacionPago().setNumeroCuenta("");
//							} else {
//								comp.getAddenda().getInformacionPago()
//										.setNumeroCuenta(row.getCell(34).toString().trim());
//							}
						}
					}
					//num proveedor
					if (row.getCell(35) == null) {
						comp.getAddenda().getInformacionPago().setNumProveedor("");
					} else {
						if (row.getCell(35).toString().trim().equals("")) {
							comp.getAddenda().getInformacionPago().setNumProveedor("");
						} else {
							comp.getAddenda().getInformacionPago().setNumProveedor(row.getCell(35).toString().trim());
//							String strCellTypeP = checkCellType(row.getCell(35));
//							if (!strCellTypeP.equals("")) {
//								comp.getAddenda().getInformacionPago().setNumProveedor("");
//							} else {
//								comp.getAddenda().getInformacionPago()
//										.setNumProveedor(row.getCell(35).toString().trim());
//							}
						}
					}
				} 
			}
		}

		/* Tipo de operacion */
		comp.setComplemento(new CfdiComplemento());
		if (!row.getCell(36).toString().trim().equals("")) {
			if (row.getCell(36).toString().trim().toLowerCase().equals("compra")
					|| row.getCell(36).toString().trim().toLowerCase().equals("venta")) {
				comp.getComplemento().setDivisaTipoOperacion(row.getCell(36).toString().toLowerCase().trim());
			} else {
				comp.getComplemento().setDivisaTipoOperacion("");
			}
		} else {
			comp.getComplemento().setDivisaTipoOperacion("");
		}

		

		/* Valida rfc cliente extranjero */
		if (comp.getReceptor() != null && comp.getReceptor().getRfc() != null){ 
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
		if (row.getCell(37) == null) {
			comp.setNoAutorizacion("");
		} else {
			comp.setNoAutorizacion(row.getCell(37).toString().trim());
		}

		// UUID & tipo relación
		comp.getComplemento().setTimbreFiscalDigital(new CfdiTimbreFiscalDigital());
		if (row.getCell(38) == null || row.getCell(38).toString().trim().equals("")) {
			comp.getComplemento().getTimbreFiscalDigital().setUuid("");
		} else {
			comp.getComplemento().getTimbreFiscalDigital().setUuid(row.getCell(38).toString().trim());
			
			comp.setCfdiRelacionados(new CfdiRelacionado());
			if (row.getCell(39) == null || row.getCell(39).toString().trim().equals("")) {
				comp.getCfdiRelacionados().setTipoRelacion("");
			} else {
				comp.getCfdiRelacionados().setTipoRelacion(row.getCell(39).toString().trim());
			}
		}
		
		int posicion = 40;
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
					} else {
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
						cfdi.setDescripcion(row.getCell(posicion).toString().trim());
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

	public CfdiComprobanteFiscal fillComprobanteDivisasTxt(String[] linea) {
		CfdiComprobanteFiscal comp = new CfdiComprobanteFiscal();
		comp.setTipoEmision(TipoEmision.DIVISAS);
		FiscalEntity fiscalEntity = null;
		Customer customer = null;
		/* Emisor Posicion 0--row 0 */
		comp.setEmisor(new CfdiEmisor());
		if (linea[0] == null) {
			comp.getEmisor().setRfc("");
		} else {
			fiscalEntity = new FiscalEntity();
			fiscalEntity.setTaxID(linea[0].toString().trim());
			fiscalEntity = fiscalEntityManager.findByRFCA(fiscalEntity);
			comp.getEmisor().setRfc(linea[0].toString().trim());
		}
		/* Serie Posicion 1 -- row 1 */
		comp.setSerie(linea[1].toString().trim());
		if (linea[1] == null) {
			comp.setSerie("");
		} else {
			if (linea[1].toString().trim().length() > 0) {
				comp.setSerie(linea[1].toString().trim());
			} else {
				comp.setSerie("");
			}
		}
		/* Forma de pago */
		if (linea[2] == null || linea[2].toString().trim().equals("")) {
			comp.setFormaPago("");
		} else {
			comp.setFormaPago(linea[2].toString());			
		}
		/* Motivo descuento */
		String motivoDesc="";
		if (linea[3] == null) { // Antes 31 ahora 3 AMDA V3.3
			motivoDesc = "";
			System.out.println("row.getCell(31) == null");
		} else {
			motivoDesc = linea[3].toString().trim();
			System.out.println("row.getCell(31) != null");
		}
		comp.setMotivoDescCellValue(motivoDesc);
		if (motivoDesc.equals("")) {
			// invoice.setMotivoDescuento("");
			if (linea[4] == null) {
				comp.setDescuento(BigDecimal.ZERO);
				System.out.println("Descuento cero");
			} else {
				if (!linea[4].toString().trim().equals("")) {
					String strCellTypeDesc = linea[4].toString().trim();
					if (strCellTypeDesc.equals("")) {
						if (validaDatoRE(linea[4].toString().trim(), RE_DECIMAL_NEGATIVO)) {
							if (Double.parseDouble(linea[4].toString().trim()) > 0) {
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
				if (linea[4] == null) {
					comp.setDescuento(BigDecimal.ZERO);
				} else {
					if (linea[4].toString().trim().equals("")) {
						comp.setDescuento(BigDecimal.ZERO);
					} else {
						String strCellTypeDesc = linea[4].toString().trim();
						if (strCellTypeDesc.equals("")) {
							comp.setDescuento(BigDecimal.ZERO);
						} else {
							if (validaDatoRE(linea[4].toString().trim(), RE_DECIMAL_NEGATIVO)) {
								if (Double.parseDouble(linea[4].toString().trim()) > 0) {
									comp.setDescuento(
											new BigDecimal(Double.parseDouble(linea[4].toString().trim())));
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
		/* Posicion 5 Moneda */
		if (linea[5] == null && linea[5].toString().trim().equals("")) {
			comp.setMoneda("");
		} else {
			comp.setMoneda(linea[5].toString().trim());	
		}
		/* Tipo de cambio */
		if (linea[6] == null && linea[6].toString().trim().equals("")) {
			comp.setTipoCambio("");
		} else {
			if (comp.getMoneda() != null) {
				comp.setTipoCambio(linea[6].toString().trim());				
			}else{
				comp.setTipoCambio("");			
			}
		}
		/* Tipo formato posicion 7 -- row 7 */
		if (linea[7] == null) {
			comp.setTipoDeComprobante("I");
		} else {
			comp.setTipoDeComprobante(linea[7].toString().trim());
		}
		/* Metodo de pago */
		if (linea[8] == null || linea[8].toString().trim().equals("")) {
			comp.setMetodoPago("");
		}else{
			comp.setMetodoPago(linea[8].toString().trim());			
		}
		/* Regimen fiscal */
		if (linea[9] == null || linea[9].toString().trim().equals("")) {
			comp.getEmisor().setRegimenFiscal("");
		} else {
			comp.getEmisor().setRegimenFiscal(linea[9].toString().trim());			
		}
		/* RFC del cliente */
		comp.setReceptor(new CfdiReceptor());
		boolean readFromFile = false;
		if (linea[10] == null) {
			comp.setCustomerRfcCellValue("");
		} else {
			if (linea[10].toString().trim().equals("")) {
				comp.setCustomerRfcCellValue("");
			} else {
				comp.setCustomerRfcCellValue(linea[10].toString().trim());
				if (linea[10].toString().trim().toUpperCase().equals("XEXX010101000")
						|| linea[10].toString().trim().toUpperCase().equals("XAXX010101000")
						|| linea[10].toString().trim().equals("XEXE010101000")) {

					//evaluacion del id de extranjero
					if (linea[12] == null || linea[12].toString().trim().length() == 0) {
						comp.setStrIDExtranjero("");
						readFromFile= true;
					} else {
						String strCellType = linea[12].toString().trim();
						if (!strCellType.equals("")) {
							comp.setReceptor(new CfdiReceptor());
							readFromFile= true;
						} else {
							String strIDExtranjero = linea[12].toString().trim();
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
								if (linea[14] == null || linea[14].toString().trim().equals("")) {
									comp.getReceptor().setNumRegIdTrib("");
								} else {
									comp.getReceptor().setNumRegIdTrib(linea[14].toString().trim());
								}
								//uso cfdi
								if (linea[13] == null || linea[13].toString().trim().length() == 0) {
									comp.getReceptor().setUsoCFDI("D04");
								} else {
									comp.getReceptor().setUsoCFDI(linea[13].toString().trim());									
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
									//comp.getReceptor().getDomicilio().setReferencia(customer.getAddress().getReference());
								}
							}else{
								readFromFile = true;
							}
						}
					}
				} else {
					if (fiscalEntity != null) {
						customer = customerManager.get(linea[10].toString().trim(),String.valueOf(fiscalEntity.getId()));
						
						if (customer != null) {
							//rfc
							comp.getReceptor().setRfc(customer.getTaxId());
							//nombre
							comp.getReceptor().setNombre(customer.getPhysicalName());
							//NumRegIdTrib
							if (linea[14] == null || linea[14].toString().trim().equals("")) {
								comp.getReceptor().setNumRegIdTrib("");
							} else {
								comp.getReceptor().setNumRegIdTrib(linea[14].toString().trim());
							}
							//uso cfdi
							if (linea[13] == null || linea[13].toString().trim().length() == 0) {
								comp.getReceptor().setUsoCFDI("D04");
							} else {
								comp.getReceptor().setUsoCFDI(linea[13].toString().trim());								
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
		if(readFromFile){
			comp.setReceptor(new CfdiReceptor());
			comp.getReceptor().setDomicilio(new CfdiDomicilio());
			comp.getReceptor().setRfc(comp.getCustomerRfcCellValue());
			//nombre
			if (linea[11] == null || linea[11].toString().trim().equals("")) {
				comp.getReceptor().setNombre("");
			}else{
				comp.getReceptor().setNombre(linea[11].toString().trim());
			}
			
		}
		//referencia
		if (linea[16] == null || linea[16].toString().trim().equals("")) {
			comp.getReceptor().getDomicilio().setReferencia("");
		}else{
			comp.getReceptor().getDomicilio().setReferencia(linea[16].toString().trim());
		}
		
		comp.setAddenda(new CfdiAddendaSantanderV1());
		comp.getAddenda().setInformacionPago(new CfdiAddendaInformacionPago());
		/* Numero de cuenta */
		if (linea[15] == null || linea[15].toString().trim().equals("")) {
			comp.setNumeroCuenta("");
		} else {			
			comp.setNumeroCuenta(linea[15].toString().trim());
		}
		/* Codigo cliente */
		comp.getAddenda().setInformacionEmision(new CfdiAddendaInformacionEmision());
		if (linea[17] == null || linea[17].toString().trim().equals("")) {
			comp.getAddenda().getInformacionEmision().setCodigoCliente("");
		} else {
				comp.getAddenda().getInformacionEmision().setCodigoCliente(linea[17].toString().trim());
		}
		
		/* Contrato */
		if (linea[18] == null || linea[18].toString().trim().equals("")) {
			comp.getAddenda().getInformacionEmision().setContrato("");
		} else {
			comp.getAddenda().getInformacionEmision().setContrato(linea[18].toString().trim());
		}
		/* Periodo */
		if (linea[19] == null || linea[19].toString().trim().equals("")) {
			comp.getAddenda().getInformacionEmision().setPeriodo("");
		} else {
			comp.getAddenda().getInformacionEmision().setPeriodo(linea[19].toString().trim());
		}
		/* Centro de costos */
		if (linea[20] == null) {
			comp.getAddenda().getInformacionEmision().setCentroCostos("");
		} else {
			comp.getAddenda().getInformacionEmision().setCentroCostos(linea[20].toString().trim());
		}
		comp.getAddenda().setCampoAdicional(new HashMap<String, String>());
		/* Descriptcion concepto */
		if (linea[21] == null) {
			comp.getAddenda().getCampoAdicional().put("DESCRIPCIÓN CONCEPTO", "");
		} else {
			comp.getAddenda().getCampoAdicional().put("DESCRIPCIÓN CONCEPTO", linea[21].toString().trim());
		}
		/* Iva */
		//new variable this
		comp.setIvaCellValue(linea[22].toString().trim());
		/* Tipo adenda */
		comp.getAddenda().setInmuebles(new CfdiAddendaInmuebles());
		if (linea[23] != null) {
			if (linea[23].toString().contains(".")) {
				System.out.println("*** response Dentro IF AMDA: " + linea[23].toString().trim());
				String words[] = linea[23].toString().split("\\.");
				linea[23] = words[0];
				System.out.println("*** response Dentro IF despues AMDA: " + linea[23].toString().trim());
			}
			comp.setTipoAddendaCellValue(linea[23].toString().trim());
			System.out.println("tipoAddenda:" + linea[23].toString().trim());

			if (validaDatoRE(linea[23].toString().trim(), RE_DECIMAL)) {
				String strTipoAddenda = linea[23].toString().trim();
				System.out.println("tipoAddendaClean: " + strTipoAddenda);
				
				//Nombre Beneficiario
				if (linea[32] == null) {
					comp.getAddenda().getInformacionPago().setNombreBeneficiario("");
				} else {
					comp.getAddenda().getInformacionPago().setNombreBeneficiario(linea[32].toString().trim());
				}
				//institucion receptora
				if (linea[33] == null) {
					comp.getAddenda().getInformacionPago().setInstitucionReceptora("");
				} else {
					comp.getAddenda().getInformacionPago()
							.setInstitucionReceptora(linea[33].toString().trim());
				}
				//Numero de cuenta
				if (linea[34] == null) {
					comp.getAddenda().getInformacionPago().setNumeroCuenta("");
				} else {
					if (linea[34].toString().trim().equals("")) {
						comp.getAddenda().getInformacionPago().setNumeroCuenta(linea[34].toString().trim());
					} else {
						comp.getAddenda().getInformacionPago().setNumeroCuenta(linea[34].toString().trim());
					}
				}
				//num proveedor
				if (linea[35] == null) {
					comp.getAddenda().getInformacionPago().setNumProveedor("");
				} else {
					if (linea[35].toString().trim().equals("")) {
						comp.getAddenda().getInformacionPago().setNumProveedor("");
					} else {
						comp.getAddenda().getInformacionPago().setNumProveedor(linea[35].toString().trim());
					}
				}
				
				//centro de costos
				if (linea[29] == null) {
					comp.setCostCenter("");
				} else {
					if (linea[29].toString().trim().equals("")) {
						comp.setCostCenter("");
					} else {
						comp.setCostCenter(linea[29].trim());
					}
				}
				
				if (strTipoAddenda.equals("1") || strTipoAddenda.equals("2") || strTipoAddenda.equals("3")) {
//					comp.getAddenda().getInformacionPago().setNombreBeneficiario("");
//					comp.getAddenda().getInformacionPago().setInstitucionReceptora("");
//					comp.getAddenda().getInformacionPago().setNumeroCuenta("");
//					comp.getAddenda().getInformacionPago().setNumProveedor("");
					//email proveedor
					if (linea[24] == null) {
						comp.getAddenda().getInformacionPago().setEmail("");
					} else {
						if (!linea[24].toString().trim().equals("")) {
							if (validaDatoRE(linea[24].toString().trim(), RE_MAIL)) {
								comp.getAddenda().getInformacionPago().setEmail(linea[24].toString().trim());
							} else {
								comp.getAddenda().getInformacionPago().setEmail("");
							}
						} else {
							comp.getAddenda().getInformacionPago().setEmail("");
						}
					}
					//codigo iso moneda
					if (linea[25] == null) {
						comp.getAddenda().getInformacionPago().setCodigoISOMoneda(null);
					} else {
						if (!linea[25].toString().trim().equals("")) {
							comp.getAddenda().getInformacionPago()
								.setCodigoISOMoneda(linea[25].toString().trim().toUpperCase());
						} else {
							comp.getAddenda().getInformacionPago().setCodigoISOMoneda("");
						}
					}
				}
				if (strTipoAddenda.equals("1")) {
					//orden compra
					if (linea[26] == null) {
						comp.getAddenda().getInformacionPago().setOrdenCompra(null);
					} else {
						if (linea[26].toString().trim().equals("")) {
							comp.getAddenda().getInformacionPago().setOrdenCompra("");
						} else {
							comp.getAddenda().getInformacionPago().setOrdenCompra(linea[26].toString().trim());
						}
					}
					//posicion compra
					if (linea[27] == null) {
						comp.getAddenda().getInformacionPago().setPosCompra(null);
					} else {
						if (linea[27].toString().trim().equals("")) {
							comp.getAddenda().getInformacionPago().setPosCompra("");
						} else {
							comp.getAddenda().getInformacionPago().setPosCompra(linea[27].toString().trim());
						}

					}

				} else if (strTipoAddenda.equals("2")) {

					//cuenta contable
					if (linea[28] == null) {
						comp.getAddenda().getInformacionPago().setCuentaContable(null);
					} else {
						if (linea[28].toString().trim().equals("")) {
							comp.getAddenda().getInformacionPago().setCuentaContable("");
						} else {
							comp.getAddenda().getInformacionPago().setCuentaContable(linea[28].toString().trim());
						}
					}

				} else if (strTipoAddenda.equals("3")) {
					//numContrato
					if (linea[30] == null) {
						comp.getAddenda().getInmuebles().setNumContrato(null);
					} else {
						if (linea[30].toString().trim().equals("")) {
							comp.getAddenda().getInmuebles().setNumContrato("");
						} else {
							comp.getAddenda().getInmuebles().setNumContrato(linea[30].toString().trim());
						}

					}
					//fechaVencimiento
					if ( linea[31] == null) {
						comp.getAddenda().getInmuebles().setFechaVencimiento(null);
					} else {
						if (linea[31].toString().trim().equals("")) {
							comp.getAddenda().getInmuebles().setFechaVencimiento("");
						} else {
							comp.getAddenda().getInmuebles().setFechaVencimiento(linea[31].toString().trim());
						}

					}

				} else if(strTipoAddenda.equals("0") || strTipoAddenda.equals("")) {
					comp.getAddenda().getInformacionPago().setEmail("");
					comp.getAddenda().getInformacionPago().setOrdenCompra("");

				} 
			}
		}
		/* Tipo de operacion */
		comp.setComplemento(new CfdiComplemento());
		if (!linea[36].toString().trim().equals("")) {
			if (linea[36].toString().trim().toLowerCase().equals("compra")
					|| linea[36].toString().trim().toLowerCase().equals("venta")) {
				comp.getComplemento().setDivisaTipoOperacion(linea[36].toString().toLowerCase().trim());
			} else {
				comp.getComplemento().setDivisaTipoOperacion("");
			}
		} else {
			comp.getComplemento().setDivisaTipoOperacion("");
		}

		

		/* Valida rfc cliente extranjero */
		if (comp.getReceptor() != null && comp.getReceptor().getRfc() != null){ 
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
		if (linea[37] == null) {
			comp.setNoAutorizacion("");
		} else {
			comp.setNoAutorizacion(linea[37].toString().trim());
		}

		// UUID & tipo relación
		comp.getComplemento().setTimbreFiscalDigital(new CfdiTimbreFiscalDigital());
		if (linea[38] == null || linea[38].toString().trim().equals("")) {
			comp.getComplemento().getTimbreFiscalDigital().setUuid("");
		} else {
			comp.getComplemento().getTimbreFiscalDigital().setUuid(linea[38].toString().trim());
			
			comp.setCfdiRelacionados(new CfdiRelacionado());
			if (linea[39] == null || linea[39].toString().trim().equals("")) {
				comp.getCfdiRelacionados().setTipoRelacion("");
			} else {
				comp.getCfdiRelacionados().setTipoRelacion(linea[39].toString().trim());
			}
		}
		// seccion de conceptos
		int posicion = 40;
		int contadorConceptos = 0;
		boolean fPermisoVector = true;
		boolean fFinFactura = false;
		Integer numeroCelda = 0;
		String tipoFactorValRow = "";
		List<CfdiConcepto> conceptos =  new ArrayList<CfdiConcepto>();
		CfdiConcepto cfdi = null;
		CfdiConceptoImpuestoTipo cImpuestoTipo = null;
		CfdiConceptoImpuesto impuestos = null;
		
		while (posicion < linea.length && !fFinFactura) {
			if(numeroCelda==0){
				cfdi = new CfdiConcepto();
				impuestos = new CfdiConceptoImpuesto();
				cImpuestoTipo = new CfdiConceptoImpuestoTipo();
				cfdi.setImpuestos(impuestos);
			}
			numeroCelda += 1;
			contadorConceptos = contadorConceptos + 1;
			if (linea[posicion].toString().equals("FINFACTURA")) {
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
					} else {
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
						cImpuestoTipo.setTipoFactor(linea[posicion].toString().trim());
					}
				}

				if (numeroCelda == 11) {
					if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
						cImpuestoTipo.setTasaOCuota("");
					} else {
						if (!tipoFactorValRow.equalsIgnoreCase("Exento")
								&& !tipoFactorValRow.equalsIgnoreCase("Excento")) {
							cImpuestoTipo.setTasaOCuota(linea[posicion].toString().trim());
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
						cfdi.setDescripcion(linea[posicion].toString().trim());
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
						cImpuestoTipo.setImpuesto(linea[posicion].toString().trim());
					}
				}
				
				if (numeroCelda == 10) {
					if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
						fPermisoVector = false;
						cImpuestoTipo.setTipoFactor("");
					} else {
						cImpuestoTipo.setTipoFactor(linea[posicion].toString().trim());
					}
				}

				if (numeroCelda == 11) {
					if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
						fPermisoVector = false;
						cImpuestoTipo.setTasaOCuota("");
					} else {
						cImpuestoTipo.setTasaOCuota(linea[posicion].toString().trim());
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
}
