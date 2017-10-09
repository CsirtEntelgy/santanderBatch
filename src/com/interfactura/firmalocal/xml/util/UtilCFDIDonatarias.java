package com.interfactura.firmalocal.xml.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
public class UtilCFDIDonatarias {

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
	
	public CfdiComprobanteFiscal fillComprobanteDonataria(CfdiComprobanteFiscal comp,Row row, FiscalEntity fiscalEntity, int factura,Customer customer, int lastCellNum) {
		/* Emisor Posicion 0--row 0 */
		comp = new CfdiComprobanteFiscal();
		comp.setTipoEmision(TipoEmision.DONATARIAS);
		comp.setEmisor(new CfdiEmisor());
		if (row.getCell(0) == null) {
			comp.getEmisor().setRfc("");
		} else {
			fiscalEntity = new FiscalEntity();
			fiscalEntity.setTaxID(row.getCell(0).toString());
			fiscalEntity = fiscalEntityManager.findByRFCA(fiscalEntity);
			comp.getEmisor().setRfc(row.getCell(0).toString());
			System.out.println("RFC: "+comp.getEmisor().getRfc());
		}
		/* Serie Posicion 1 -- row 1 */
		comp.setSerie(row.getCell(1).toString().trim());
		if (row.getCell(1) == null) {
			comp.setSerie(null);
		} else {
			if (row.getCell(1).toString().trim().length() > 0) {
				comp.setSerie(row.getCell(1).toString().trim());
				System.out.println("Serie: "+comp.getSerie().toString());
			} else {
				comp.setSerie("");
			}
		}
		/* Tipo Comprobante posicion 2 -- row 2 */
		if (row.getCell(2) == null) {
			comp.setTipoDeComprobante("I");
		} else {
			comp.setTipoDeComprobante(row.getCell(2).toString());
			System.out.println("Tipo Comprobante: "+comp.getTipoDeComprobante().toString());
		}
		/* Posicion 3 lugar de expedicion */
		if (row.getCell(3) == null && row.getCell(3).toString().trim().equals("")) {
			comp.setLugarExpedicion("");
		} else {
			comp.setLugarExpedicion(row.getCell(3).toString().trim());
			System.out.println("Lugar de expedicion: "+comp.getLugarExpedicion().toString());
		}
		/* Posicion 4 regimen fiscal */
		if (row.getCell(4) == null || row.getCell(4).toString().trim().equals("")) {
			comp.getEmisor().setRegimenFiscal("");
		} else {
			comp.getEmisor().setRegimenFiscal(row.getCell(4).toString());
			System.out.println("Regimen fiscal: "+comp.getEmisor().getRegimenFiscal().toString());
		}
		
		/* Posicion 5 Numer de cuenta pago*/
		//comp.setAddenda(new CfdiAddendaSantanderV1());
		//comp.getAddenda().setInformacionPago(new CfdiAddendaInformacionPago());
		if(row.getCell(5) == null || row.getCell(5).toString().trim().equals("")){
			comp.setNumeroCuentaPago("");
		}else{
			comp.setNumeroCuentaPago(row.getCell(5).toString().trim());
		}
		/* Metodo de pago */
		if (row.getCell(6) == null || row.getCell(6).toString().trim().equals("")) {
			comp.setMetodoPago("");
		}else{
			comp.setMetodoPago(row.getCell(6).toString());
			System.out.println("Metodo pago: "+comp.getMetodoPago().toString());
		}
		/* Forma de pago */
		if (row.getCell(7) == null || row.getCell(7).toString().trim().equals("")) {
			comp.setFormaPago("");
		} else {
			comp.setFormaPago(row.getCell(7).toString());
			System.out.println("Forma pago: "+comp.getFormaPago().toString());
		}
		/* Posicion 5 Moneda */
		if (row.getCell(8) == null && row.getCell(8).toString().trim().equals("")) {
			comp.setMoneda("");
		} else {
			comp.setMoneda(row.getCell(8).toString().trim());			
			System.out.println("Moneda: "+comp.getMoneda().toString());
		}
		/* Tipo de cambio */
		if (row.getCell(9) == null && row.getCell(9).toString().trim().equals("")) {
			comp.setTipoCambio("");
		} else {
			if (comp.getMoneda() != null) {
				comp.setTipoCambio(row.getCell(9).toString());		
				System.out.println("tipo de cambio: "+comp.getTipoCambio().toString());
			}else{
				comp.setTipoCambio("");			
			}
		}
		
		/* Rfc del cliente */
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
					//evaluacion del id de extranjero
					if (row.getCell(12) == null || row.getCell(12).toString().trim().length() == 0) {
						comp.setStrIDExtranjero("");
					} else {
						System.out.println("Valor de la celda 12: "+row.getCell(12).toString());
						String strIDExtranjero = row.getCell(12).toString().trim();
						comp.setStrIDExtranjero(strIDExtranjero);
						System.out.println("ID Extranjero: " + strIDExtranjero);
						customer = customerManager.findByIdExtranjero(strIDExtranjero);
						
						if (customer != null) {
							//rfc
							comp.getReceptor().setRfc(customer.getTaxId());
							//nombre
							comp.getReceptor().setNombre(customer.getPhysicalName());
							// numRegIDTrib
							if (row.getCell(15) == null || row.getCell(15).toString().equals("")) {
								comp.getReceptor().setNumRegIdTrib("");
							} else {
								comp.getReceptor().setNumRegIdTrib(row.getCell(15).toString());
							}
							// posicion 14 Residencia fiscal
							if (row.getCell(14) == null || row.getCell(14).toString().trim().equals("")) {
								comp.getReceptor().setResidenciaFiscal("");
							} else {
								comp.getReceptor().setResidenciaFiscal(row.getCell(14).toString());
							}
							//uso cfdi
							comp.getReceptor().setRfc(customer.getTaxId());
							if (row.getCell(13) == null || row.getCell(13).toString().trim().length() == 0) {
								comp.getReceptor().setUsoCFDI("D04");
							} else {
								comp.getReceptor().setUsoCFDI(row.getCell(13).toString());									
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
						}else{
							readFromFile = true;
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
			if (row.getCell(11) == null || row.getCell(11).toString().equals("")) {
				comp.getReceptor().setNombre("");
			}else{
				comp.getReceptor().setNombre(row.getCell(11).toString());
			}
			//usoCfdi
			if (row.getCell(13) == null || row.getCell(13).toString().equals("")) {
				comp.getReceptor().setUsoCFDI("D04");
			} else {
				comp.getReceptor().setUsoCFDI(row.getCell(13).toString());
			}
			// posicion 14 Residencia fiscal
			if (row.getCell(14) == null || row.getCell(14).toString().equals("")) {
				comp.getReceptor().setResidenciaFiscal("");
			} else {
				comp.getReceptor().setResidenciaFiscal(row.getCell(14).toString());
			}
			//setNumRegIdTrib
			if (row.getCell(15) == null || row.getCell(15).toString().equals("")) {
				comp.getReceptor().setNumRegIdTrib("");
			} else {
				comp.getReceptor().setNumRegIdTrib(row.getCell(15).toString());
			}
			//calle
			if(row.getCell(18) == null || row.getCell(18).toString().trim().equals("")){
				comp.getReceptor().getDomicilio().setCalle("");
			} else{
				comp.getReceptor().getDomicilio().setCalle(row.getCell(18).toString().trim());
			}
			// posicion 19 numero interior 
			if(row.getCell(19) == null || row.getCell(19).toString().trim().equals("")){
				comp.getReceptor().getDomicilio().setNoInterior("");
			}else{
				comp.getReceptor().getDomicilio().setNoInterior(row.getCell(19).toString().trim());
			}
			// posicion 20 numero exterior
			if(row.getCell(20) == null || row.getCell(20).toString().trim().equals("")){
				comp.getReceptor().getDomicilio().setNoExterior("");
			}else{
				comp.getReceptor().getDomicilio().setNoExterior(row.getCell(20).toString().trim());
			}
			// posicion 21 colonia
			if(row.getCell(21) == null || row.getCell(21).toString().trim().equals("")){
				comp.getReceptor().getDomicilio().setColonia("");
			}else{
				comp.getReceptor().getDomicilio().setColonia(row.getCell(21).toString().trim());
			}
			// posicion 22 localidad
			if(row.getCell(22) == null || row.getCell(22).toString().trim().equals("")){
				comp.getReceptor().getDomicilio().setLocalidad("");
			}else{
				comp.getReceptor().getDomicilio().setLocalidad(row.getCell(22).toString().trim());
			}
			// posicion 23 referencia
			if(row.getCell(23) == null || row.getCell(23).toString().trim().equals("")){
				comp.getReceptor().getDomicilio().setReferencia("");
			}else{
				comp.getReceptor().getDomicilio().setReferencia(row.getCell(23).toString().trim());
			}
			// posicion 24 municipio
			if(row.getCell(24) == null || row.getCell(24).toString().trim().equals("")){
				comp.getReceptor().getDomicilio().setMunicipio("");
			}else{
				comp.getReceptor().getDomicilio().setMunicipio(row.getCell(24).toString().trim());
			}
			// posicion 25 estado
			if(row.getCell(25) == null || row.getCell(25).toString().trim().equals("")){
				comp.getReceptor().getDomicilio().setEstado("");
			}else{
				comp.getReceptor().getDomicilio().setEstado(row.getCell(25).toString().trim());
			}
			// posicion 26 pais
			if(row.getCell(26) == null || row.getCell(26).toString().trim().equals("")){
				comp.getReceptor().getDomicilio().setPais("");
			}else{
				comp.getReceptor().getDomicilio().setPais(row.getCell(26).toString().trim());
			}
			// posicion 27 codigo postal
			if(row.getCell(27) == null || row.getCell(27).toString().trim().equals("")){
				comp.getReceptor().getDomicilio().setCodigoPostal("");
			}else{
				comp.getReceptor().getDomicilio().setCodigoPostal(row.getCell(27).toString().trim());
			}
		}
		
		
		// posicion 16 fecha de recepccion
		if (row.getCell(16) == null || row.getCell(16).toString().trim().equals("")) {
			comp.setFecha("");
		} else {
			String start_dt = row.getCell(16).toString();
			DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
			Date date = null;
			String finalString ="";
			try {
				date = (Date)formatter.parse(start_dt);
				SimpleDateFormat newFormat = new SimpleDateFormat("dd/MM/yyyy");
				finalString = newFormat.format(date);
			} catch (ParseException e) {
				finalString = "";
			}
			comp.setFecha(finalString);
			System.out.println("Fecha de Comp: "+comp.getFecha().toString());
		}
		// posicion 17 numero de empleado
				if (row.getCell(17) == null || row.getCell(17).toString().trim().equals("")) {
					comp.setNumEmpledo("");

				} else {
					comp.setNumEmpledo(row.getCell(17).toString());
					System.out.println("Num Emp de Comp: "+comp.getNumEmpledo().toString());
				}
				// posicion 28 codigo cliente 
				/* Codigo cliente */
				comp.setAddenda(new CfdiAddendaSantanderV1());
				comp.getAddenda().setInformacionEmision(new CfdiAddendaInformacionEmision());
				if (row.getCell(28) == null || 
					row.getCell(28).toString().trim().equals("") || 
					row.getCell(28).toString().trim().length() > 0) {
					comp.getAddenda().getInformacionEmision().setCodigoCliente("");
				} else {
					comp.getAddenda().getInformacionEmision().setCodigoCliente(row.getCell(28).toString().trim());
					System.out.println("Cod. Cliente Comp: "+comp.getAddenda().getInformacionEmision().getCodigoCliente().toString());
				}
				// posicion 29 contrato
				if (row.getCell(29) == null || 
					row.getCell(29).toString().trim().equals("") || 
					row.getCell(29).toString().trim().length() > 0) {
					comp.getAddenda().getInformacionEmision().setContrato("");
				} else {
					comp.getAddenda().getInformacionEmision().setContrato(row.getCell(29).toString().trim());
					System.out.println("Contrato Comp: "+comp.getAddenda().getInformacionEmision().getContrato().toString());
				}
				// posicion 30 periodo
				if (row.getCell(30) == null ||
					row.getCell(30).toString().trim().equals("") ||
					row.getCell(30).toString().trim().length() > 0) {
					comp.getAddenda().getInformacionEmision().setPeriodo("");
				} else {
					comp.getAddenda().getInformacionEmision().setPeriodo(row.getCell(30).toString().trim());
					System.out.println("Periodo Comp: "+comp.getAddenda().getInformacionEmision().getPeriodo().toString());
				}
				// posicion 31 c. contrato
				if (row.getCell(31) == null || 
					row.getCell(31).toString().trim().equals("") ||
					row.getCell(31).toString().trim().length() > 0) {
					comp.getAddenda().getInformacionEmision().setCentroCostos("");
				} else {
					comp.getAddenda().getInformacionEmision().setCentroCostos(row.getCell(31).toString().trim());
					System.out.println("Centro. contrato Comp: "+comp.getAddenda().getInformacionEmision().getCentroCostos().toString());
				}
		
				// inicio de los conceptos 
				int posicionConcepto = 0;
				int posicion = 32; // inicio del concepto
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
				
				boolean isDonataria = false;
				CfdiConcepto cfdi = null;
				
				List<CfdiConcepto> list = new ArrayList<CfdiConcepto>();
			
				
				while (posicion < lastCellNum && !fFinFactura) {
					if(numeroCelda==0){
						cfdi = new CfdiConcepto();
					}
					numeroCelda += 1;
					contadorConceptos = contadorConceptos + 1;
					try{
						if(row.getCell(posicion) !=null ){
							if (row.getCell(posicion).toString().equals("||FINFACTURA||")) {
								fFinFactura = true;//contadorConceptos = contadorConceptos + 1;
								break;
							}
						}else{
							row.getCell(posicion).setCellValue(" ");
						}
					}catch(NullPointerException e){row.getCell(posicion).setCellValue(" ");}
										
					if (numeroCelda == 1) {
						numeroCelda = numeroCelda + 5;
						if (numeroCelda == 6) {
							try{
								posicion = posicion + 5;
								if (row.getCell(posicion).toString().equalsIgnoreCase("Traslado")) {
								}else if (row.getCell(posicion).toString().equalsIgnoreCase("Retencion")) {
									
								} else {
									System.out.println("No se encontro ninguno");
									isDonataria = true;
									fPermisoVector = false;
								}
								posicion = posicion - 5;
								numeroCelda = numeroCelda - 5;
							}catch(NullPointerException e){
								System.out.println("error: "+e);
							}
						}
					}			
					
					if(isDonataria){
						if (numeroCelda == 1) {
							if (row.getCell(posicion) == null || row.getCell(posicion).toString().trim().equals("")) {
								fPermisoVector = false;
								cfdi.setClaveProdServ("");
							} else {
								cfdi.setClaveProdServ(row.getCell(posicion).toString());
								System.out.println("Clave servicio: "+cfdi.getClaveProdServ());
							}
						}
						if (numeroCelda == 2) {
							try{
								if (row.getCell(posicion) == null || row.getCell(posicion).toString().trim().equals("")) {
									fPermisoVector = false;
									cfdi.setCantidad(new BigDecimal("0.0"));
								} else {
									cfdi.setCantidad(new BigDecimal(row.getCell(posicion).toString()));
									System.out.println("Cantidad (n): "+ cfdi.getCantidad());
								}
							}catch(NullPointerException e){
								System.out.println("Error: "+e);
								cfdi.setCantidad(new BigDecimal("0.0"));
							}
							
						}
						if (numeroCelda == 3) {
							try{
								if (row.getCell(posicion) == null || row.getCell(posicion).toString().trim().equals("")) {
									fPermisoVector = false;
									cfdi.setClaveUnidad("0");
								} else {
									cfdi.setClaveUnidad( row.getCell(posicion).toString());
									System.out.println("Clave Unidad: "+  cfdi.getClaveUnidad());
								}
							}catch(NullPointerException e){
								System.out.println("Error: "+e);
								cfdi.setClaveUnidad("0");
							}
						}
						if (numeroCelda == 4) {
							try{
								if (row.getCell(posicion) == null || row.getCell(posicion).toString().trim().equals("")) {
									fPermisoVector = false;
									cfdi.setUnidad("");
								} else {
									cfdi.setUnidad(row.getCell(posicion).toString());
									System.out.println("UM: "+  cfdi.getUnidad());
									
								}
							}catch(NullPointerException e){
								System.out.println("Error: "+e);
								cfdi.setUnidad("");
							}
							
						}
						/** Secccion de concepto de expedicion **/
						 
						if (numeroCelda == 5) {
							try{
								if (row.getCell(posicion) == null || row.getCell(posicion).toString().trim().equals("")) {
									fPermisoVector = false;
									cfdi.setDescripcion("");
									row.getCell(posicion).setCellValue("");
								} else {
									cfdi.setDescripcion(row.getCell(posicion).toString());
									System.out.println("Concepto de expedicion: "+  cfdi.getDescripcion());
								}
							}catch(NullPointerException e){
								System.out.println("Error setDescripcion: "+e);
								cfdi.setDescripcion("");
							}
							
						}
						/** Seccion de valor unitario **/
						try{
							if (numeroCelda == 6) {
								if (row.getCell(posicion) == null || row.getCell(posicion).toString().trim().equals("")) {					
									fPermisoVector = false;
									cfdi.setValorUnitario(new BigDecimal("0.0"));
									numeroCelda = 0;
								}else {
									cfdi.setValorUnitario(new BigDecimal(row.getCell(posicion).toString()));
									System.out.println("Valor unitario: "+  cfdi.getValorUnitario());
									numeroCelda = 0;
								}
								numeroCelda = 0;
								if(cfdi != null){
									list.add(cfdi);
								}
							}
						}catch(NullPointerException e){
							System.out.println("Error en valor unitario: "+e);
							cfdi.setValorUnitario(new BigDecimal("0.0"));
						}
					}// FIN DE LA CONDICION DE DONATARIA				
				
					posicion = posicion + 1;
				}// FIN DEL WHILE
				comp.setFinFactura(fFinFactura);
				comp.setConceptos(list);				
		return comp;
	}
	//Metodo de lectura TXT
	public CfdiComprobanteFiscal fillComprobanteDNTxt(String[] linea) {
		/* Emisor Posicion 0--row 0 */
		CfdiComprobanteFiscal comp =  new CfdiComprobanteFiscal();
		Customer customer = null;
		FiscalEntity fiscalEntity = new FiscalEntity();
		comp.setTipoEmision(TipoEmision.DONATARIAS);
		comp.setEmisor(new CfdiEmisor());
		
		if (linea[0] == null || linea[0].trim().equals("")) {
			comp.getEmisor().setRfc("");
		} else {
			fiscalEntity.setTaxID(linea[0].toString());
			fiscalEntity = fiscalEntityManager.findByRFCA(fiscalEntity);
			comp.getEmisor().setRfc(linea[0].toString());
			System.out.println("RFC: "+comp.getEmisor().getRfc());
		}
		/* Serie Posicion 1 -- row 1 */
		comp.setSerie(linea[1].toString().trim());
		if (linea[1] == null) {
			comp.setSerie(null);
		} else {
			if (linea[1].toString().trim().length() > 0) {
				comp.setSerie(linea[1].toString().trim());
				System.out.println("Serie: "+comp.getSerie().toString());
			} else {
				comp.setSerie("");
			}
		}
		/* Tipo Comprobante posicion 2 -- row 2 */
		if (linea[2] == null) {
			comp.setTipoDeComprobante("I");
		} else {
			comp.setTipoDeComprobante(linea[2].toString());
			System.out.println("Tipo Comprobante: "+comp.getTipoDeComprobante().toString());
		}
		/* Posicion 3 lugar de expedicion */
		if (linea[3]== null || linea[3].toString().trim().equals("")) {
			comp.setLugarExpedicion("");
		} else {
			comp.setLugarExpedicion(linea[3].toString().trim());
			System.out.println("Lugar de expedicion: "+comp.getLugarExpedicion().toString());
		}
		/* Posicion 4 regimen fiscal */
		if (linea[4] == null ||linea[4].toString().trim().equals("")) {
			comp.getEmisor().setRegimenFiscal("");
		} else {
			comp.getEmisor().setRegimenFiscal(linea[4].toString());
			System.out.println("Regimen fiscal: "+comp.getEmisor().getRegimenFiscal().toString());
		}
		/* Posicion 5 Numer de cuenta pago*/
		//comp.setAddenda(new CfdiAddendaSantanderV1());
		//comp.getAddenda().setInformacionPago(new CfdiAddendaInformacionPago());
		if(linea[5] == null || linea[5].toString().trim().equals("")){
			comp.setNumeroCuentaPago("");
		}else{
			comp.setNumeroCuentaPago(linea[5].toString().trim());
		}
		/* Metodo de pago */
		if (linea[6] == null || linea[6].toString().trim().equals("")) {
			comp.setMetodoPago("");
		}else{
			comp.setMetodoPago(linea[6].toString());
			System.out.println("Metodo pago: "+comp.getMetodoPago().toString());
		}
		/* Forma de pago */
		if (linea[7] == null || linea[7].toString().trim().equals("")) {
			comp.setFormaPago("");
		} else {
			comp.setFormaPago(linea[7].toString());
			System.out.println("Forma pago: "+comp.getFormaPago().toString());
		}
		/* Posicion  Moneda */
		if (linea[8] == null || linea[8].toString().trim().equals("")) {
			comp.setMoneda("");
		} else {
			comp.setMoneda(linea[8].toString().trim());			
			System.out.println("Moneda: "+comp.getMoneda().toString());
		}
		/* Tipo de cambio */
		if (linea[9] == null || linea[9].toString().trim().equals("")) {
			comp.setTipoCambio("");
		} else {
			if (comp.getMoneda() != null) {
				comp.setTipoCambio(linea[9].toString());		
				System.out.println("tipo de cambio: "+comp.getTipoCambio().toString());
			}else{
				comp.setTipoCambio("");			
			}
		}
		/* Rfc del cliente */
		boolean readFromFile = false;
		comp.setReceptor(new CfdiReceptor());
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
						readFromFile = true;
					} else {
						System.out.println("Valor de la celda 12: "+linea[12].toString());
						String strIDExtranjero = linea[12].toString().trim();
						comp.setStrIDExtranjero(strIDExtranjero);
						System.out.println("ID Extranjero: " + strIDExtranjero);
						customer = customerManager.findByIdExtranjero(strIDExtranjero);
						
						if (customer != null) {
							//rfc
							comp.getReceptor().setRfc(customer.getTaxId());
							//nombre
							comp.getReceptor().setNombre(customer.getPhysicalName());
							// numRegIDTrib
							if (linea[15] == null || linea[15].toString().equals("")) {
								comp.getReceptor().setNumRegIdTrib("");
							} else {
								comp.getReceptor().setNumRegIdTrib(linea[15].toString());
							}
							// posicion 14 Residencia fiscal
							if (linea[14] == null || linea[14].toString().trim().equals("")) {
								comp.getReceptor().setResidenciaFiscal("");
							} else {
								comp.getReceptor().setResidenciaFiscal(linea[14].toString());
							}
							//uso cfdi
							comp.getReceptor().setRfc(customer.getTaxId());
							if (linea[13] == null || linea[13].toString().trim().length() == 0) {
								comp.getReceptor().setUsoCFDI("D04");
							} else {
								comp.getReceptor().setUsoCFDI(linea[13].toString());									
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
						}else{
							readFromFile = true;
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
								comp.getReceptor().setNumRegIdTrib(linea[14].toString());
							}
							//uso cfdi
							if (linea[13] == null || linea[13].toString().trim().length() == 0) {
								comp.getReceptor().setUsoCFDI("D04");
							} else {
								comp.getReceptor().setUsoCFDI(linea[13].toString());								
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
						}
					}else{
						readFromFile = true;
					}
				}
			}
		}
		// hasta aqui
		if(readFromFile){
			comp.setReceptor(new CfdiReceptor());
			comp.getReceptor().setDomicilio(new CfdiDomicilio());
			comp.getReceptor().setRfc(comp.getCustomerRfcCellValue());
			
			//nombre
			if (linea[11] == null || linea[11].toString().equals("")) {
				comp.getReceptor().setNombre("");
			}else{
				comp.getReceptor().setNombre(linea[11].toString());
			}
			//usoCfdi
			if (linea[13] == null || linea[13].toString().equals("")) {
				comp.getReceptor().setUsoCFDI("D04");
			} else {
				comp.getReceptor().setUsoCFDI(linea[13].toString());
			}
			// posicion 14 Residencia fiscal
			if (linea[14] == null || linea[14].toString().equals("")) {
				comp.getReceptor().setResidenciaFiscal("");
			} else {
				comp.getReceptor().setResidenciaFiscal(linea[14].toString());
			}
			//setNumRegIdTrib
			if (linea[15] == null || linea[15].toString().equals("")) {
				comp.getReceptor().setNumRegIdTrib("");
			} else {
				comp.getReceptor().setNumRegIdTrib(linea[15].toString());
			}
			//calle
			if(linea[18] == null || linea[18].toString().trim().equals("")){
				comp.getReceptor().getDomicilio().setCalle("");
			} else{
				comp.getReceptor().getDomicilio().setCalle(linea[18].toString().trim());
			}
			// posicion 19 numero interior 
			if(linea[19] == null || linea[19].toString().trim().equals("")){
				comp.getReceptor().getDomicilio().setNoInterior("");
			}else{
				comp.getReceptor().getDomicilio().setNoInterior(linea[19].toString().trim());
			}
			// posicion 20 numero exterior
			if(linea[20] == null || linea[20].toString().trim().equals("")){
				comp.getReceptor().getDomicilio().setNoExterior("");
			}else{
				comp.getReceptor().getDomicilio().setNoExterior(linea[20].toString().trim());
			}
			// posicion 21 colonia
			if(linea[21] == null || linea[21].toString().trim().equals("")){
				comp.getReceptor().getDomicilio().setColonia("");
			}else{
				comp.getReceptor().getDomicilio().setColonia(linea[21].toString().trim());
			}
			// posicion 22 localidad
			if(linea[22] == null || linea[22].toString().trim().equals("")){
				comp.getReceptor().getDomicilio().setLocalidad("");
			}else{
				comp.getReceptor().getDomicilio().setLocalidad(linea[22].toString().trim());
			}
			// posicion 23 referencia
			if(linea[23] == null || linea[23].toString().trim().equals("")){
				comp.getReceptor().getDomicilio().setReferencia("");
			}else{
				comp.getReceptor().getDomicilio().setReferencia(linea[23].toString().trim());
			}
			// posicion 24 municipio
			if(linea[24] == null || linea[24].toString().trim().equals("")){
				comp.getReceptor().getDomicilio().setMunicipio("");
			}else{
				comp.getReceptor().getDomicilio().setMunicipio(linea[24].toString().trim());
			}
			// posicion 25 estado
			if(linea[25] == null || linea[25].toString().trim().equals("")){
				comp.getReceptor().getDomicilio().setEstado("");
			}else{
				comp.getReceptor().getDomicilio().setEstado(linea[25].toString().trim());
			}
			// posicion 26 pais
			if(linea[26] == null || linea[26].toString().trim().equals("")){
				comp.getReceptor().getDomicilio().setPais("");
			}else{
				comp.getReceptor().getDomicilio().setPais(linea[26].toString().trim());
			}
			// posicion 27 codigo postal
			if(linea[27] == null || linea[27].toString().trim().equals("")){
				comp.getReceptor().getDomicilio().setCodigoPostal("");
			}else{
				comp.getReceptor().getDomicilio().setCodigoPostal(linea[27].toString().trim());
			}
		}
		// posicion 16 fecha de recepccion
				if (linea[16] == null || linea[16].toString().trim().equals("")) {
					comp.setFecha("");
				} else {
					comp.setFecha(linea[16].toString());
					System.out.println("Fecha de Comp: "+comp.getFecha().toString());
				}
				// posicion 17 numero de empleado
						if (linea[17] == null || linea[17].toString().trim().equals("")) {
							comp.setNumEmpledo("");

						} else {
							comp.setNumEmpledo(linea[17].toString());
							System.out.println("Num Emp de Comp: "+comp.getNumEmpledo().toString());
						}
						// posicion 28 codigo cliente 
						/* Codigo cliente */
						comp.setAddenda(new CfdiAddendaSantanderV1());
						comp.getAddenda().setInformacionEmision(new CfdiAddendaInformacionEmision());
						if (linea[28] == null || 
							linea[28].toString().trim().equals("") || 
							linea[28].toString().trim().length() > 0) {
							comp.getAddenda().getInformacionEmision().setCodigoCliente("");
						} else {
							comp.getAddenda().getInformacionEmision().setCodigoCliente(linea[28].toString().trim());
							System.out.println("Cod. Cliente Comp: "+comp.getAddenda().getInformacionEmision().getCodigoCliente().toString());
						}
						// posicion 29 contrato
						if (linea[29] == null || 
							linea[29].toString().trim().equals("") || 
							linea[29].toString().trim().length() > 0) {
							comp.getAddenda().getInformacionEmision().setContrato("");
						} else {
							comp.getAddenda().getInformacionEmision().setContrato(linea[29].toString().trim());
							System.out.println("Contrato Comp: "+comp.getAddenda().getInformacionEmision().getContrato().toString());
						}
						// posicion 30 periodo
						if (linea[30] == null ||
							linea[30].toString().trim().equals("") ||
							linea[30].toString().trim().length() > 0) {
							comp.getAddenda().getInformacionEmision().setPeriodo("");
						} else {
							comp.getAddenda().getInformacionEmision().setPeriodo(linea[30].toString().trim());
							System.out.println("Periodo Comp: "+comp.getAddenda().getInformacionEmision().getPeriodo().toString());
						}
						// posicion 31 c. contrato
						if (linea[31] == null || 
							linea[31].toString().trim().equals("") ||
							linea[31].toString().trim().length() > 0) {
							comp.getAddenda().getInformacionEmision().setCentroCostos("");
						} else {
							comp.getAddenda().getInformacionEmision().setCentroCostos(linea[31].toString().trim());
							System.out.println("Centro. contrato Comp: "+comp.getAddenda().getInformacionEmision().getCentroCostos().toString());
						}
				
						// inicio de los conceptos 
						int posicionConcepto = 0;
						int posicion = 32; // inicio del concepto
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
						boolean isDonataria = false;
						CfdiConcepto cfdi = null;
						List<CfdiConcepto> list = new ArrayList<CfdiConcepto>();
						
						while (posicion < linea.length && !fFinFactura) {
							if(numeroCelda==0){
								cfdi = new CfdiConcepto();
							}
							numeroCelda += 1;
							contadorConceptos = contadorConceptos + 1;
							try{
								if(linea[posicion] !=null ){
									if (linea[posicion].toString().equals("FINFACTURA")) {
										fFinFactura = true;//contadorConceptos = contadorConceptos + 1;
										break;
									}
								}else{
									linea[posicion] = "";
								}
							}catch(NullPointerException e){linea[posicion] = "";}
												
							if (numeroCelda == 1) {
								numeroCelda = numeroCelda + 5;
								if (numeroCelda == 6) {
									try{
										posicion = posicion + 5;
										if (linea[posicion].toString().equalsIgnoreCase("Traslado")) {
										}else if (linea[posicion].toString().equalsIgnoreCase("Retencion")) {
											
										} else {
											System.out.println("No se encontro ninguno");
											isDonataria = true;
											fPermisoVector = false;
										}
										posicion = posicion - 5;
										numeroCelda = numeroCelda - 5;
									}catch(NullPointerException e){
										System.out.println("error: "+e);
									}
								}
							}			
							
							if(isDonataria){
								if (numeroCelda == 1) {
									if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
										fPermisoVector = false;
										cfdi.setClaveProdServ("");
									} else {
										cfdi.setClaveProdServ(linea[posicion].toString().trim());
										System.out.println("Clave servicio: "+cfdi.getClaveProdServ());
									}
								}
								if (numeroCelda == 2) {
									try{
										if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
											fPermisoVector = false;
											cfdi.setCantidad(new BigDecimal("0.0"));
										} else {
											cfdi.setCantidad(new BigDecimal(linea[posicion].toString()));
											System.out.println("Cantidad (n): "+ cfdi.getCantidad());
										}
									}catch(NullPointerException e){
										System.out.println("Error: "+e);
										cfdi.setCantidad(new BigDecimal("0.0"));
									}
									
								}
								if (numeroCelda == 3) {
									try{
										if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
											fPermisoVector = false;
											cfdi.setClaveUnidad("0");
										} else {
											cfdi.setClaveUnidad( linea[posicion].toString());
											System.out.println("Clave Unidad: "+  cfdi.getClaveUnidad());
										}
									}catch(NullPointerException e){
										System.out.println("Error: "+e);
										cfdi.setClaveUnidad("0");
									}
								}
								if (numeroCelda == 4) {
									try{
										if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
											fPermisoVector = false;
											cfdi.setUnidad("");
										} else {
											cfdi.setUnidad(linea[posicion].toString());
											System.out.println("UM: "+  cfdi.getUnidad());
											
										}
									}catch(NullPointerException e){
										System.out.println("Error: "+e);
										cfdi.setUnidad("");
									}
									
								}
								/** Secccion de concepto de expedicion **/
								 
								if (numeroCelda == 5) {
									try{
										if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
											fPermisoVector = false;
											cfdi.setDescripcion("");
											linea[posicion] = "";
										} else {
											cfdi.setDescripcion(linea[posicion].toString());
											System.out.println("Concepto de expedicion: "+  cfdi.getDescripcion());
										}
									}catch(NullPointerException e){
										System.out.println("Error setDescripcion: "+e);
										cfdi.setDescripcion("");
									}
									
								}
								/** Seccion de valor unitario **/
								try{
									if (numeroCelda == 6) {
										if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {					
											fPermisoVector = false;
											cfdi.setValorUnitario(new BigDecimal("0.0"));
											numeroCelda = 0;
										}else {
											cfdi.setValorUnitario(new BigDecimal(linea[posicion].toString()));
											System.out.println("Valor unitario: "+  cfdi.getValorUnitario());
											numeroCelda = 0;
										}
										numeroCelda = 0;
										if(cfdi != null){
											list.add(cfdi);
										}
									}
								}catch(NullPointerException e){
									System.out.println("Error en valor unitario: "+e);
									cfdi.setValorUnitario(new BigDecimal("0.0"));
								}
							}// FIN DE LA CONDICION DE DONATARIA				
						
							posicion = posicion + 1;
						}// FIN DEL WHILE
						comp.setFinFactura(fFinFactura);
						comp.setConceptos(list);
		return comp;
	}

	public CfdiComprobanteFiscal fillComprobanteDonatTXT(String[] linea){

		/* Emisor Posicion 0--row 0 */
		CfdiComprobanteFiscal comp =  new CfdiComprobanteFiscal();
		Customer customer = null;
		FiscalEntity fiscalEntity = new FiscalEntity();
		comp.setTipoEmision(TipoEmision.DONATARIAS);
		comp.setEmisor(new CfdiEmisor());
		
		if (linea[0] == null || linea[0].trim().equals("")) {
			comp.getEmisor().setRfc("");
		} else {
			fiscalEntity.setTaxID(linea[0].toString());
			fiscalEntity = fiscalEntityManager.findByRFCA(fiscalEntity);
			comp.getEmisor().setRfc(linea[0].toString());
			System.out.println("RFC: "+comp.getEmisor().getRfc());
		}
		/* Serie Posicion 1 -- row 1 */
		comp.setSerie(linea[1].toString().trim());
		if (linea[1] == null) {
			comp.setSerie(null);
		} else {
			if (linea[1].toString().trim().length() > 0) {
				comp.setSerie(linea[1].toString().trim());
				System.out.println("Serie: "+comp.getSerie().toString());
			} else {
				comp.setSerie("");
			}
		}
		/* Tipo Comprobante posicion 2 -- row 2 */
		if (linea[2] == null) {
			comp.setTipoDeComprobante("I");
		} else {
			comp.setTipoDeComprobante(linea[2].toString());
			System.out.println("Tipo Comprobante: "+comp.getTipoDeComprobante().toString());
		}
		
		/* Posicion 3 Moneda */
		if (linea[3] == null || linea[3].toString().trim().equals("")) {
			comp.setMoneda("");
		} else {
			comp.setMoneda(linea[3].toString().trim());			
			System.out.println("Moneda: "+comp.getMoneda().toString());
		}
		
		/* Posicion 4 Tipo de cambio */
		if (linea[4] == null || linea[4].toString().trim().equals("")) {
			comp.setTipoCambio("");
		} else {
			if (comp.getMoneda() != null) {
				comp.setTipoCambio(linea[4].toString());		
				System.out.println("tipo de cambio: "+comp.getTipoCambio().toString());
			}else{
				comp.setTipoCambio("");			
			}
		}

		/* Posicion 11 Metodo de pago */
		if (linea[11] == null || linea[11].toString().trim().equals("")) {
			comp.setMetodoPago("");
		}else{
			comp.setMetodoPago(linea[11].toString());
			System.out.println("Metodo pago: "+comp.getMetodoPago().toString());
		}
		
		/* Posicion 12 regimen fiscal */
		if (linea[12] == null ||linea[12].toString().trim().equals("")) {
			comp.getEmisor().setRegimenFiscal("");
		} else {
			comp.getEmisor().setRegimenFiscal(linea[12].toString());
			System.out.println("Regimen fiscal: "+comp.getEmisor().getRegimenFiscal().toString());
		}
		
		/* Posicion 13 lugar de expedicion */
		if (linea[13]== null || linea[13].toString().trim().equals("")) {
			comp.setLugarExpedicion("");
		} else {
			comp.setLugarExpedicion(linea[13].toString().trim());
			System.out.println("Lugar de expedicion: "+comp.getLugarExpedicion().toString());
		}
		
		/*Posicion 14 Forma de pago */
		if (linea[14] == null || linea[14].toString().trim().equals("")) {
			comp.setFormaPago("");
		} else {
			comp.setFormaPago(linea[14].toString());
			System.out.println("Forma pago: "+comp.getFormaPago().toString());
		}
		
		/* Posicion 15 Numer de cuenta pago*/
		if(linea[15] == null || linea[15].toString().trim().equals("")){
			comp.setNumeroCuentaPago("");
		}else{
			comp.setNumeroCuentaPago(linea[15].toString().trim());
		}
		
		/* Posicion 5 Rfc del cliente */
		boolean readFromFile = false;
		comp.setReceptor(new CfdiReceptor());
		if (linea[5] == null) {
			comp.setCustomerRfcCellValue("");
		} else {
			if (linea[5].toString().trim().equals("")) {
				comp.setCustomerRfcCellValue("");
			} else {
				comp.setCustomerRfcCellValue(linea[5].toString().trim());
				if (linea[5].toString().trim().toUpperCase().equals("XEXX010101000")
						|| linea[5].toString().trim().toUpperCase().equals("XAXX010101000")
						|| linea[5].toString().trim().equals("XEXE010101000")) {
					// Posicion 6 evaluacion del id de extranjero
					if (linea[6] == null || linea[6].toString().trim().length() == 0) {
						comp.setStrIDExtranjero("");
						readFromFile = true;
					} else {
						System.out.println("Valor de la celda 6: "+linea[6].toString());
						String strIDExtranjero = linea[6].toString().trim();
						comp.setStrIDExtranjero(strIDExtranjero);
						System.out.println("ID Extranjero: " + strIDExtranjero);
						customer = customerManager.findByIdExtranjero(strIDExtranjero);
						
						if (customer != null) {
							//rfc 
							comp.getReceptor().setRfc(customer.getTaxId());
							//nombre
							comp.getReceptor().setNombre(customer.getPhysicalName());
							// numRegIDTrib 9
							if (linea[9] == null || linea[9].toString().equals("")) {
								comp.getReceptor().setNumRegIdTrib("");
							} else {
								comp.getReceptor().setNumRegIdTrib(linea[9].toString());
							}
							// posicion 8 Residencia fiscal
							if (linea[8] == null || linea[8].toString().trim().equals("")) {
								comp.getReceptor().setResidenciaFiscal("");
							} else {
								comp.getReceptor().setResidenciaFiscal(linea[8].toString());
							}
							//uso cfdi 7
							comp.getReceptor().setRfc(customer.getTaxId());
							if (linea[7] == null || linea[7].toString().trim().length() == 0) {
								comp.getReceptor().setUsoCFDI("D04");
							} else {
								comp.getReceptor().setUsoCFDI(linea[7].toString());									
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
						}else{
							readFromFile = true;
						}
					}
				} else {
					if (fiscalEntity != null) {
						customer = customerManager.get(linea[5].toString().trim(),String.valueOf(fiscalEntity.getId()));
						if (customer != null) {
							//rfc
							comp.getReceptor().setRfc(customer.getTaxId());
							//nombre
							comp.getReceptor().setNombre(customer.getPhysicalName());
							//NumRegIdTrib 9
							if (linea[9] == null || linea[9].toString().trim().equals("")) {
								comp.getReceptor().setNumRegIdTrib("");
							} else {
								comp.getReceptor().setNumRegIdTrib(linea[9].toString());
							}
							//uso cfdi 7
							if (linea[7] == null || linea[7].toString().trim().length() == 0) {
								comp.getReceptor().setUsoCFDI("D04");
							} else {
								comp.getReceptor().setUsoCFDI(linea[7].toString());								
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
		// hasta aqui
		if(readFromFile){
			comp.setReceptor(new CfdiReceptor());
			comp.getReceptor().setDomicilio(new CfdiDomicilio());
			comp.getReceptor().setRfc(comp.getCustomerRfcCellValue());
			
			//nombre 10
			if (linea[10] == null || linea[10].toString().equals("")) {
				comp.getReceptor().setNombre("");
			}else{
				comp.getReceptor().setNombre(linea[10].toString());
			}
			//usoCfdi 7
			if (linea[7] == null || linea[7].toString().equals("")) {
				comp.getReceptor().setUsoCFDI("D04");
			} else {
				comp.getReceptor().setUsoCFDI(linea[7].toString());
			}
			// posicion 8 Residencia fiscal
			if (linea[8] == null || linea[8].toString().equals("")) {
				comp.getReceptor().setResidenciaFiscal("");
			} else {
				comp.getReceptor().setResidenciaFiscal(linea[8].toString());
			}
			//setNumRegIdTrib 9
			if (linea[9] == null || linea[9].toString().equals("")) {
				comp.getReceptor().setNumRegIdTrib("");
			} else {
				comp.getReceptor().setNumRegIdTrib(linea[9].toString());
			}
			//calle
			if(linea[18] == null || linea[18].toString().trim().equals("")){
				comp.getReceptor().getDomicilio().setCalle("");
			} else{
				comp.getReceptor().getDomicilio().setCalle(linea[18].toString().trim());
			}
			// posicion 19 numero interior 
			if(linea[19] == null || linea[19].toString().trim().equals("")){
				comp.getReceptor().getDomicilio().setNoInterior("");
			}else{
				comp.getReceptor().getDomicilio().setNoInterior(linea[19].toString().trim());
			}
			// posicion 20 numero exterior
			if(linea[20] == null || linea[20].toString().trim().equals("")){
				comp.getReceptor().getDomicilio().setNoExterior("");
			}else{
				comp.getReceptor().getDomicilio().setNoExterior(linea[20].toString().trim());
			}
			// posicion 21 colonia
			if(linea[21] == null || linea[21].toString().trim().equals("")){
				comp.getReceptor().getDomicilio().setColonia("");
			}else{
				comp.getReceptor().getDomicilio().setColonia(linea[21].toString().trim());
			}
			// posicion 22 localidad
			if(linea[22] == null || linea[22].toString().trim().equals("")){
				comp.getReceptor().getDomicilio().setLocalidad("");
			}else{
				comp.getReceptor().getDomicilio().setLocalidad(linea[22].toString().trim());
			}
			// posicion 23 referencia
			if(linea[23] == null || linea[23].toString().trim().equals("")){
				comp.getReceptor().getDomicilio().setReferencia("");
			}else{
				comp.getReceptor().getDomicilio().setReferencia(linea[23].toString().trim());
			}
			// posicion 24 municipio
			if(linea[24] == null || linea[24].toString().trim().equals("")){
				comp.getReceptor().getDomicilio().setMunicipio("");
			}else{
				comp.getReceptor().getDomicilio().setMunicipio(linea[24].toString().trim());
			}
			// posicion 25 estado
			if(linea[25] == null || linea[25].toString().trim().equals("")){
				comp.getReceptor().getDomicilio().setEstado("");
			}else{
				comp.getReceptor().getDomicilio().setEstado(linea[25].toString().trim());
			}
			// posicion 26 pais
			if(linea[26] == null || linea[26].toString().trim().equals("")){
				comp.getReceptor().getDomicilio().setPais("");
			}else{
				comp.getReceptor().getDomicilio().setPais(linea[26].toString().trim());
			}
			// posicion 27 codigo postal
			if(linea[27] == null || linea[27].toString().trim().equals("")){
				comp.getReceptor().getDomicilio().setCodigoPostal("");
			}else{
				comp.getReceptor().getDomicilio().setCodigoPostal(linea[27].toString().trim());
			}
		}
		// posicion 16 fecha de recepccion
		if (linea[16] == null || linea[16].toString().trim().equals("")) {
			comp.setFecha("");
		} else {
			comp.setFecha(linea[16].toString());
			System.out.println("Fecha de Comp: "+comp.getFecha().toString());
		}
		
		// posicion 17 numero de empleado
		if (linea[17] == null || linea[17].toString().trim().equals("")) {
			comp.setNumEmpledo("");

		} else {
			comp.setNumEmpledo(linea[17].toString());
			System.out.println("Num Emp de Comp: "+comp.getNumEmpledo().toString());
		}
		
		// posicion 28 codigo cliente 
		/* Codigo cliente */
		comp.setAddenda(new CfdiAddendaSantanderV1());
		comp.getAddenda().setInformacionEmision(new CfdiAddendaInformacionEmision());
		if (linea[28] == null || 
			linea[28].toString().trim().equals("") || 
			linea[28].toString().trim().length() > 0) {
			comp.getAddenda().getInformacionEmision().setCodigoCliente("");
		} else {
			comp.getAddenda().getInformacionEmision().setCodigoCliente(linea[28].toString().trim());
			System.out.println("Cod. Cliente Comp: "+comp.getAddenda().getInformacionEmision().getCodigoCliente().toString());
		}
		
		// posicion 29 contrato
		if (linea[29] == null || 
			linea[29].toString().trim().equals("") || 
			linea[29].toString().trim().length() > 0) {
			comp.getAddenda().getInformacionEmision().setContrato("");
		} else {
			comp.getAddenda().getInformacionEmision().setContrato(linea[29].toString().trim());
			System.out.println("Contrato Comp: "+comp.getAddenda().getInformacionEmision().getContrato().toString());
		}
		
		// posicion 30 periodo
		if (linea[30] == null ||
			linea[30].toString().trim().equals("") ||
			linea[30].toString().trim().length() > 0) {
			comp.getAddenda().getInformacionEmision().setPeriodo("");
		} else {
			comp.getAddenda().getInformacionEmision().setPeriodo(linea[30].toString().trim());
			System.out.println("Periodo Comp: "+comp.getAddenda().getInformacionEmision().getPeriodo().toString());
		}
		
		// posicion 31 c. costos
		if (linea[31] == null || 
			linea[31].toString().trim().equals("") ||
			linea[31].toString().trim().length() > 0) {
			comp.getAddenda().getInformacionEmision().setCentroCostos("");
		} else {
			comp.getAddenda().getInformacionEmision().setCentroCostos(linea[31].toString().trim());
			System.out.println("Centro. contrato Comp: "+comp.getAddenda().getInformacionEmision().getCentroCostos().toString());
		}
				
		// inicio de los conceptos 
		int posicionConcepto = 0;
		int posicion = 32; // inicio del concepto
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
		boolean isDonataria = false;
		CfdiConcepto cfdi = null;
		List<CfdiConcepto> list = new ArrayList<CfdiConcepto>();
		
		while (posicion < linea.length && !fFinFactura) {
			if(numeroCelda==0){
				cfdi = new CfdiConcepto();
			}
			numeroCelda += 1;
			contadorConceptos = contadorConceptos + 1;
			try{
				if(linea[posicion] !=null ){
					if (linea[posicion].toString().equals("FINFACTURA")) {
						fFinFactura = true;//contadorConceptos = contadorConceptos + 1;
						break;
					}
				}else{
					linea[posicion] = "";
				}
			}catch(NullPointerException e){linea[posicion] = "";}
								
			if (numeroCelda == 1) {
				numeroCelda = numeroCelda + 5;
				if (numeroCelda == 6) {
					try{
						posicion = posicion + 5;
						if (linea[posicion].toString().equalsIgnoreCase("Traslado")) {
						}else if (linea[posicion].toString().equalsIgnoreCase("Retencion")) {
							
						} else {
							System.out.println("No se encontro ninguno");
							isDonataria = true;
							fPermisoVector = false;
						}
						posicion = posicion - 5;
						numeroCelda = numeroCelda - 5;
					}catch(NullPointerException e){
						System.out.println("error: "+e);
					}
				}
			}			
			
			if(isDonataria){
				if (numeroCelda == 1) {
					if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
						fPermisoVector = false;
						cfdi.setClaveProdServ("");
					} else {
						cfdi.setClaveProdServ(linea[posicion].toString().trim());
						System.out.println("Clave servicio: "+cfdi.getClaveProdServ());
					}
				}
				if (numeroCelda == 2) {
					try{
						if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
							fPermisoVector = false;
							cfdi.setCantidad(new BigDecimal("0.0"));
						} else {
							cfdi.setCantidad(new BigDecimal(linea[posicion].toString()));
							System.out.println("Cantidad (n): "+ cfdi.getCantidad());
						}
					}catch(NullPointerException e){
						System.out.println("Error: "+e);
						cfdi.setCantidad(new BigDecimal("0.0"));
					}
					
				}
				if (numeroCelda == 3) {
					try{
						if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
							fPermisoVector = false;
							cfdi.setClaveUnidad("0");
						} else {
							cfdi.setClaveUnidad( linea[posicion].toString());
							System.out.println("Clave Unidad: "+  cfdi.getClaveUnidad());
						}
					}catch(NullPointerException e){
						System.out.println("Error: "+e);
						cfdi.setClaveUnidad("0");
					}
				}
				if (numeroCelda == 4) {
					try{
						if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
							fPermisoVector = false;
							cfdi.setUnidad("");
						} else {
							cfdi.setUnidad(linea[posicion].toString());
							System.out.println("UM: "+  cfdi.getUnidad());
							
						}
					}catch(NullPointerException e){
						System.out.println("Error: "+e);
						cfdi.setUnidad("");
					}
					
				}
				/** Secccion de concepto de expedicion **/
				 
				if (numeroCelda == 5) {
					try{
						if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {
							fPermisoVector = false;
							cfdi.setDescripcion("");
							linea[posicion] = "";
						} else {
							cfdi.setDescripcion(linea[posicion].toString());
							System.out.println("Concepto de expedicion: "+  cfdi.getDescripcion());
						}
					}catch(NullPointerException e){
						System.out.println("Error setDescripcion: "+e);
						cfdi.setDescripcion("");
					}
					
				}
				/** Seccion de valor unitario **/
				try{
					if (numeroCelda == 6) {
						if (linea[posicion] == null || linea[posicion].toString().trim().equals("")) {					
							fPermisoVector = false;
							cfdi.setValorUnitario(new BigDecimal("0.0"));
							numeroCelda = 0;
						}else {
							cfdi.setValorUnitario(new BigDecimal(linea[posicion].toString()));
							System.out.println("Valor unitario: "+  cfdi.getValorUnitario());
							numeroCelda = 0;
						}
						numeroCelda = 0;
						if(cfdi != null){
							list.add(cfdi);
						}
					}
				}catch(NullPointerException e){
					System.out.println("Error en valor unitario: "+e);
					cfdi.setValorUnitario(new BigDecimal("0.0"));
				}
			}// FIN DE LA CONDICION DE DONATARIA				
		
			posicion = posicion + 1;
		}// FIN DEL WHILE
		comp.setFinFactura(fFinFactura);
		comp.setConceptos(list);
		return comp;
	}
}