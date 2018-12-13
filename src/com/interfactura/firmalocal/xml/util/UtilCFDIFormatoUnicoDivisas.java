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

import oracle.jrockit.jfr.events.DynamicValueDescriptor;

@Service
public class UtilCFDIFormatoUnicoDivisas {
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

	public CfdiComprobanteFiscal fillComprobanteFUDivisas(String[] linea){
		CfdiComprobanteFiscal comp =  new CfdiComprobanteFiscal();
		comp.setTipoEmision(TipoEmision.FORMATO_UNICO);
		
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
		System.out.println("antes de forma de pago: " + linea[2]);
		
		if (linea[2] == null || linea[2].trim().equals("")) {
			comp.setFormaPago("");
		} else {

//			comp.setFormaPago(linea[2].trim());
//			String[] pal = linea[2].split(" ");
//			if(pal.length == 4 && pal[0].equals("Transferencia")) {
//				comp.setFormaPago(pal[0] + " electrónica " + linea[2] + " " + linea[3]);
//			}else {
//				comp.setFormaPago(linea[2].trim());
//			}
			comp.setFormaPago(linea[2].trim());
//			try {
//				comp.setFormaPago(new String(linea[2].trim().getBytes("ISO-8859-1"),"UTF-8"));
//			} catch (UnsupportedEncodingException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		System.out.println("Forma de pago: " + comp.getFormaPago());
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
		
		// pos 10
		/* RFC del cliente */
		boolean readFromFile = false;
		
		Customer customer = null;
		
		comp.setReceptor(new CfdiReceptor());
		
		if (linea[10] == null) {
			comp.setCustomerRfcCellValue("");
		} else {
			if (linea[10].trim().equals("")) {
				comp.setCustomerRfcCellValue("");
			} else {
				comp.setCustomerRfcCellValue(linea[10].trim());
			}
		}
		
		comp.setReceptor(new CfdiReceptor());
		comp.getReceptor().setDomicilio(new CfdiDomicilio());
		
		comp.getReceptor().setRfc(comp.getCustomerRfcCellValue());
		// pos 11
		/*Nombre del cliente */
		if (linea[11] == null || linea[11].trim().equals("")) {
			comp.getReceptor().setNombre("");
		} else {
			comp.getReceptor().setNombre(linea[11].trim());
		}
		// pos 12
		/* ID extranjero*/
		if (linea[12] == null || linea[12].trim().equals("")) {
			comp.setStrIDExtranjero("");
		} else {
			String strIDExtranjero = linea[12].trim();
			comp.setStrIDExtranjero(strIDExtranjero);
		}
		// pos 13
		/*Uso de CFDI */
		if (linea[13] == null || linea[13].trim().equals("")) {
			comp.getReceptor().setUsoCFDI("D04");
		} else {
			comp.getReceptor().setUsoCFDI(linea[13].trim());
		}
		// Pos 14
		/*NumRegIDTrib */
		if (linea[14] == null || linea[14].trim().equals("")) {
			comp.getReceptor().setNumRegIdTrib("");
		} else {
			comp.getReceptor().setNumRegIdTrib(linea[14].trim());
		}

		if(comp.getReceptor() != null && comp.getReceptor().getDomicilio() == null){
			comp.getReceptor().setDomicilio(new CfdiDomicilio());
		}
		
		comp.setAddenda(new CfdiAddendaSantanderV1());
		comp.getAddenda().setInformacionPago(new CfdiAddendaInformacionPago());
		
		// pos 15
		/* Numero de cuenta */
		if (linea[15] == null || linea[15].trim().equals("")) {
			comp.setNumeroCuenta("");
		} else {
			comp.setNumeroCuenta(linea[15].trim());
		}
		
		/* Referencia */
		if (linea[16] == null || linea[16].trim().equals("")) {
			comp.getReceptor().getDomicilio().setReferencia("");
		} else {
			comp.getReceptor().getDomicilio().setReferencia(linea[16].trim());
		}
		
		comp.getAddenda().setInformacionEmision(new CfdiAddendaInformacionEmision());
		// pos 17
		//codigo cliente
		if (linea[17] == null || linea[17].trim().equals("")) {
			comp.getAddenda().getInformacionEmision().setCodigoCliente("");
		} else {
			comp.getAddenda().getInformacionEmision().setCodigoCliente(linea[17].trim());
		}
		
		// pos 18
		/* Contrato */
		if (linea[18] == null || linea[18].trim().equals("")) {
			comp.getAddenda().getInformacionEmision().setContrato("");
		} else {
			comp.getAddenda().getInformacionEmision().setContrato(linea[18].trim());
		}
		// pos 19
		/* Periodo */
		if (linea[19] == null || linea[19].trim().equals("")) {
			comp.getAddenda().getInformacionEmision().setPeriodo("");
		} else {
			comp.getAddenda().getInformacionEmision().setPeriodo(linea[19].trim());
		}
		
		// pos 20
		/* Centro de costos */
		if (linea[20] == null || linea[20].trim().equals("")) {
			comp.getAddenda().getInformacionEmision().setCentroCostos("");
		} else {
			comp.getAddenda().getInformacionEmision().setCentroCostos(linea[20].trim());
		}

		comp.getAddenda().setCampoAdicional(new HashMap<String, String>());
		// pos 21
		/* Descriptcion concepto */
		if (linea[21] == null || linea[21].trim().equals("")) {
			comp.getAddenda().getCampoAdicional().put("DESCRIPCIÓN CONCEPTO", "");
		} else {
			comp.getAddenda().getCampoAdicional().put("DESCRIPCIÓN CONCEPTO", linea[21].trim());
		}
		
		// pos 22
		/* Iva */
		if(linea[22] == null || linea[22].trim().equals("")){
			comp.setIvaCellValue("");
		}else{
			comp.setIvaCellValue(linea[22].trim());
		}
		// pos 23
		/* tipo addenda */
		comp.getAddenda().setInmuebles(new CfdiAddendaInmuebles());
		if(linea[23] != null){
			if(linea[23].toString().contains(".")){
				System.out.println("*** response Dentro IF AMDA: " + linea[23].toString());
				String words[] = linea[23].toString().split("\\.");
				linea[23] = words[0];
				System.out.println("*** response Dentro IF despues AMDA: " + linea[23].toString());
			}
			comp.setTipoAddendaCellValue(linea[23].toString());
			System.out.println("tipoAddenda:" + linea[23].toString());
			if (validaDatoRE(linea[23].toString().trim(), RE_DECIMAL)) {
				String strTipoAddenda = linea[23].toString();
				System.out.println("tipoAddendaClean: " + strTipoAddenda);
				
				//Nombre beneficiario
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
				//numero de cuenta
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
						comp.getAddenda().getInformacionPago().setNumProveedor(linea[35].toString().trim());
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
					
					//email
					if (linea[24].toString() == null) {
						comp.getAddenda().getInformacionPago().setEmail("");
					} else {
						// email
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
						comp.getAddenda().getInformacionPago().setCodigoISOMoneda("");
					} else {
						if (!linea[25].toString().trim().equals("")) {
							comp.getAddenda().getInformacionPago()
								.setCodigoISOMoneda(linea[25].toString().trim().toUpperCase());
						} else {
							comp.getAddenda().getInformacionPago().setCodigoISOMoneda("");
						}
					}
				}// fin de la evaluacion tipo addenda 1, 2 o 3
				if (strTipoAddenda.equals("1")) {
					//orden compra
					if (linea[26] == null) {
						comp.getAddenda().getInformacionPago().setOrdenCompra("");
					} else {
						if (linea[26].toString().trim().equals("")) {
							comp.getAddenda().getInformacionPago().setOrdenCompra("");
						} else {
							comp.getAddenda().getInformacionPago().setOrdenCompra(linea[26].toString().trim());
						}
					}
					//posicion compra
					if (linea[27] == null) {
						comp.getAddenda().getInformacionPago().setPosCompra("");
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
					
				}else if (strTipoAddenda.equals("3")) {
					//num contrato
					if (linea[30] == null) {
						comp.getAddenda().getInmuebles().setNumContrato("");
					} else {
						if (linea[30].toString().trim().equals("")) {
							comp.getAddenda().getInmuebles().setNumContrato("");
						} else {
							comp.getAddenda().getInmuebles().setNumContrato(linea[30].toString().trim());
						}
					}
					//fecha vencimiento
					if (linea[31] == null) {
						comp.getAddenda().getInmuebles().setFechaVencimiento("");
					} else {
						if (linea[31].toString().trim().equals("")) {
							comp.getAddenda().getInmuebles().setFechaVencimiento("");
						} else {
							comp.getAddenda().getInmuebles().setFechaVencimiento(linea[31].toString().trim());
						}
					}
					
				} else if (strTipoAddenda.equals("0") || strTipoAddenda.equals("")) {
					comp.getAddenda().getInformacionPago().setEmail("");
					comp.getAddenda().getInformacionPago().setOrdenCompra("");
					
				} // fin de la evaluacion de addenda 0
			}// final de evaluacion de RE_DECIMAL
		}// fin de la evaluacion lina 22
		
		// pos 37
		if(linea[37] ==  null){
			comp.setNoAutorizacion("");
		}else {
			comp.setNoAutorizacion(linea[37].toString().trim());
		}
		// pos 38 y 39
		comp.setComplemento(new CfdiComplemento());
		comp.getComplemento().setTimbreFiscalDigital(new CfdiTimbreFiscalDigital());
		if (linea[38] == null) {
			comp.getComplemento().getTimbreFiscalDigital().setUuid("");
		} else {
			comp.getComplemento().getTimbreFiscalDigital().setUuid(linea[38].toString().trim());
			comp.setCfdiRelacionados(new CfdiRelacionado());
			if (linea[39] == null) {
				comp.getCfdiRelacionados().setTipoRelacion("");
			} else {
				comp.getCfdiRelacionados().setTipoRelacion(linea[39].toString().trim());
				System.out.println("* setTipoRelacion: " + comp.getCfdiRelacionados().getTipoRelacion());
			}
		}
		// pos 30 
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
				
				while( posicion < linea.length && !fFinFactura ){
					if(numeroCelda==0){
						cfdi = new CfdiConcepto();
						impuestos = new CfdiConceptoImpuesto();
						cImpuestoTipo = new CfdiConceptoImpuestoTipo();
						cfdi.setImpuestos(impuestos);
					}
					numeroCelda += 1;
					contadorConceptos = contadorConceptos + 1;
					if ( linea[posicion+1].toString().equals("FINFACTURA")) {
						fFinFactura = true;
						break;
					}
					
					System.out.println("lineaXD: " + linea[posicion].toString());
					System.out.println("numeroXD: " + numeroCelda);
					System.out.println("posicionXD: " + posicion);
					
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
							System.out.println("numerounoXD: " + numeroCelda);
							System.out.println("posicionunoXD: " + posicion);
							System.out.println("lineaunoXD: " + linea[posicion].toString());
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
