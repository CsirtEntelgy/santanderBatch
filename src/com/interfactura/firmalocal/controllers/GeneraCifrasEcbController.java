package com.interfactura.firmalocal.controllers;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.interfactura.firmalocal.cifras.CifrasEntidad;
import com.interfactura.firmalocal.cifras.WebServiceCifrasCliente;
import com.interfactura.firmalocal.domain.entities.FiscalEntity;
import com.interfactura.firmalocal.persistence.FiscalEntityManager;
import com.interfactura.firmalocal.xml.Properties;
import com.interfactura.firmalocal.xml.WebServiceCliente;
import com.interfactura.firmalocal.xml.util.Util;
import com.interfactura.firmalocal.xml.util.NombreAplicativo;

@Controller
public class GeneraCifrasEcbController {

	@Autowired
	public FiscalEntityManager fiscal;
	
	public String nombreDefault = "NOMBRE DE EMISOR NO DISPONIBLE";

	private WebServiceCifrasCliente serviceCifrasPort = null;

	private DocumentBuilderFactory dbf = null;
	private DocumentBuilder db = null;
	private Transformer tx = null;
	
	@Autowired(required = true)
    private Properties properties;
	
	public void extractionInfo(String pathCifras, String pathXmlOk, String pathInterface, String strInterface, String strFecha, String numeroMalla){
		
		// TODO Auto-generated method stub
								
				try{
					//SEt default encoding
					System.setProperty("file.encoding", "UTF-8");
					 
					System.out.println("encodig: " + System.getProperty("file.encoding"));
										
					DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
					Date dateInicio = new Date();
					System.out.println("TIMEINICIO:" + dateFormat.format(dateInicio) + "M" + System.currentTimeMillis());
					
					//validating(pathCifras, pathXmlOk, pathInterface, strInterface, strFecha);			
					
					//Cargar nombres de Aplicativo en memoria
					HashMap<String, String> hashApps = new HashMap<String, String>();
					hashApps = NombreAplicativo.cargaNombresApps();
					
					if(strInterface.trim().equals("REPROCESOECB"))
						generaCifrasReprocesoECB(pathCifras, pathXmlOk, pathInterface, strInterface, strFecha, hashApps);
					else
						generaCifras(pathCifras, pathXmlOk, pathInterface, strInterface, strFecha, hashApps, numeroMalla);
					
					Date dateFin = new Date();
					System.out.println("TIMEFIN:" + dateFormat.format(dateFin) + "M" + System.currentTimeMillis());
					
					System.out.println("Fin del procesamiento de las tareas");		
					
					
				}catch(Exception e){
					e.printStackTrace();
					System.out.println("Error:" + e.getMessage());
					
				}
			}

			
			public boolean validaRfc(String rfc)throws PatternSyntaxException
			{
				// TODO Auto-generated method stub
				//Patron del RFC--->>>[A-Z,Ñ,&]{3,4}[0-9]{2}[0-1][0-9][0-3][0-9][A-Z,0-9]{2}[0-9,A]
				
				 Pattern p = Pattern.compile("[A-Z,Ñ,&]{3,4}[0-9]{2}[0-1][0-9][0-3][0-9][A-Z,0-9]{2}[0-9,A]");
				 Matcher m = p.matcher(rfc.trim());
			     
			     if(!m.find()){
			    	 //RFC no valido
			    	 return false;
			     }
			     else{
			    	 //RFC valido
			    	 return true;
			     }
			}
			
			
			HashMap<String, CifrasEntidad> hashXml = new HashMap<String, CifrasEntidad>();
			
			HashMap<String, CifrasEntidad> hashSat = new HashMap<String, CifrasEntidad>();
			
			String periodo = ""; String moneda = ""; String tipo = ""; String nombreAplicativo = "";
						
			double comision = 0.0d;			
			double retencion = 0.0d;
			double iva = 0.0d;
			
			int fExiste=0;
			boolean isOk = false;				

			public void resetValues(){
				this.periodo = "";
				this.moneda = "";
				this.tipo = "";
								
				this.comision = 0.0d;				
				this.retencion = 0.0d;
				this.iva = 0.0d;
				
				this.isOk = true;	
				this.fExiste=0;
			}
			
			public void resetValuesReproceso(){
				this.periodo = "";
				this.moneda = "";
				this.tipo = "";
								
				this.comision = 0.0d;				
				this.retencion = 0.0d;
				this.iva = 0.0d;
				
				this.isOk = true;	
				this.fExiste=0;
				this.nombreAplicativo = "";
				
			}
			
			public void agregaCifras(String nombreAplicativo){
				//String key = nombreAplicativo + periodo + moneda;				
				String key = nombreAplicativo + periodo;
				/*System.out.println("key: " + key);
				
				System.out.println("tipo: " + tipo);
				System.out.println("comision: " + comision);
				System.out.println("iva: " + iva);
				System.out.println("retencion: " + comision);
				*/
								
				if(fExiste == 1){
					//Es Sellado
					if(hashXml.containsKey(key)){					
						
						if(tipo.equals("I")){
							/*System.out.println("Antes I");
							System.out.println("comision: " + hashXml.get(key).getIngresosEmitidos().getComision());
							System.out.println("iva: " + hashXml.get(key).getIngresosEmitidos().getIva());
							System.out.println("retencion: " + hashXml.get(key).getIngresosEmitidos().getRetencion());
							*/
							hashXml.get(key).getIngresosEmitidos().setComision(hashXml.get(key).getIngresosEmitidos().getComision() + comision);
							hashXml.get(key).getIngresosEmitidos().setIva(hashXml.get(key).getIngresosEmitidos().getIva() + iva);
							hashXml.get(key).getIngresosEmitidos().setRetencion(hashXml.get(key).getIngresosEmitidos().getRetencion() + retencion);
							hashXml.get(key).setContadorIngresosEmitidos(hashXml.get(key).getContadorIngresosEmitidos() + 1);
							/*
							System.out.println("Despues I");
							System.out.println("comision: " + hashXml.get(key).getIngresosEmitidos().getComision());
							System.out.println("iva: " + hashXml.get(key).getIngresosEmitidos().getIva());
							System.out.println("retencion: " + hashXml.get(key).getIngresosEmitidos().getRetencion());
							*/
						}else if(tipo.equals("E")){
							/*System.out.println("Antes E");
							System.out.println("comision: " + hashXml.get(key).getEgresosEmitidos().getComision());
							System.out.println("iva: " + hashXml.get(key).getEgresosEmitidos().getIva());
							System.out.println("retencion: " + hashXml.get(key).getEgresosEmitidos().getRetencion());
							*/
							hashXml.get(key).getEgresosEmitidos().setComision(hashXml.get(key).getEgresosEmitidos().getComision() + comision);
							hashXml.get(key).getEgresosEmitidos().setIva(hashXml.get(key).getEgresosEmitidos().getIva() + iva);
							hashXml.get(key).getEgresosEmitidos().setRetencion(hashXml.get(key).getEgresosEmitidos().getRetencion() + retencion);
							hashXml.get(key).setContadorEgresosEmitidos(hashXml.get(key).getContadorEgresosEmitidos() + 1);
							/*
							System.out.println("Despues E");
							System.out.println("comision: " + hashXml.get(key).getEgresosEmitidos().getComision());
							System.out.println("iva: " + hashXml.get(key).getEgresosEmitidos().getIva());
							System.out.println("retencion: " + hashXml.get(key).getEgresosEmitidos().getRetencion());
							*/
						}	
						
					}else{											
						
						hashXml.put(key, this.creaCifrasEntidad(periodo, moneda, tipo, "emitido", comision, iva, retencion, 0, nombreAplicativo));																						
						
					}
					//Fin if(fExiste == 1)
				}else{
					//Es incidente
					
					if(hashXml.containsKey(key)){					
						
						if(tipo.equals("I")){
							/*System.out.println("Antes I");
							System.out.println("comision: " + hashXml.get(key).getIngresosIncidentes().getComision());
							System.out.println("iva: " + hashXml.get(key).getIngresosIncidentes().getIva());
							System.out.println("retencion: " + hashXml.get(key).getIngresosIncidentes().getRetencion());
							*/
							hashXml.get(key).getIngresosIncidentes().setComision(hashXml.get(key).getIngresosIncidentes().getComision() + comision);
							hashXml.get(key).getIngresosIncidentes().setIva(hashXml.get(key).getIngresosIncidentes().getIva() + iva);
							hashXml.get(key).getIngresosIncidentes().setRetencion(hashXml.get(key).getIngresosIncidentes().getRetencion() + retencion);
							hashXml.get(key).setContadorIngresosIncidentes(hashXml.get(key).getContadorIngresosIncidentes() + 1);
							/*
							System.out.println("Despues I");
							System.out.println("comision: " + hashXml.get(key).getIngresosIncidentes().getComision());
							System.out.println("iva: " + hashXml.get(key).getIngresosIncidentes().getIva());
							System.out.println("retencion: " + hashXml.get(key).getIngresosIncidentes().getRetencion());
							*/
							
						}else if(tipo.equals("E")){
							/*System.out.println("Antes E");
							System.out.println("comision: " + hashXml.get(key).getEgresosIncidentes().getComision());
							System.out.println("iva: " + hashXml.get(key).getEgresosIncidentes().getIva());
							System.out.println("retencion: " + hashXml.get(key).getEgresosIncidentes().getRetencion());
							*/
							hashXml.get(key).getEgresosIncidentes().setComision(hashXml.get(key).getEgresosIncidentes().getComision() + comision);
							hashXml.get(key).getEgresosIncidentes().setIva(hashXml.get(key).getEgresosIncidentes().getIva() + iva);
							hashXml.get(key).getEgresosIncidentes().setRetencion(hashXml.get(key).getEgresosIncidentes().getRetencion() + retencion);
							hashXml.get(key).setContadorEgresosIncidentes(hashXml.get(key).getContadorEgresosIncidentes() + 1);
							/*
							System.out.println("Despues E");
							System.out.println("comision: " + hashXml.get(key).getEgresosIncidentes().getComision());
							System.out.println("iva: " + hashXml.get(key).getEgresosIncidentes().getIva());
							System.out.println("retencion: " + hashXml.get(key).getEgresosIncidentes().getRetencion());
							*/
						}	
						
					}else{
						
						//hashXml.put(key, this.creaCifrasEntidad(periodo, moneda, tipo, "incidente", comision, iva, retencion, 0, nombreAplicativo));											
						
					}
					//Fin else of if(fExiste == 1)
				}
				
			}
			public void generaCifras(String pathCifras, String pathXmlOk, String pathInterface, String strInterface, String strFecha, HashMap<String, String> hashApps, String numeroMalla) throws Exception{
				//String path = "E:\\folioSAT\\";
				System.out.println("pathCifras: " + pathCifras);
				System.out.println("pathXmlOk: " + pathXmlOk);
				System.out.println("pathInterface: " + pathInterface);							
				
				System.out.println("INTERFACE: " + strInterface);
				System.out.println("FECHA: " + strFecha);
				
				try{
					FileInputStream fStream2 = new FileInputStream(pathXmlOk);			
					DataInputStream dInput2 = new DataInputStream(fStream2);
					BufferedReader bReader2 = new BufferedReader(new InputStreamReader(dInput2));
					
					String line2 = null;
					
					List<String> listSellados = new ArrayList<String>();
					HashMap<String, String> hashSellados= new HashMap<String, String>();
					System.out.println("hashSellados");
					while((line2 = bReader2.readLine()) != null){
						System.out.println("key/value: " + line2);
						hashSellados.put(line2, line2);
						listSellados.add(line2);
					}
					
					HashMap<String, String> hashEmisores= new HashMap<String, String>();
					
					List<FiscalEntity> listFiscal = fiscal.listar();
					
					for(FiscalEntity emisor : listFiscal){
						if(emisor.getFiscalName() != null)
							hashEmisores.put(emisor.getTaxID(), emisor.getFiscalName().trim().toUpperCase());
						else
							hashEmisores.put(emisor.getTaxID(), nombreDefault);
					}
												
					///////////////////////////////////////////////////
					//FileOutputStream fileX = new FileOutputStream(new File(path + "salidaX.txt"));
					//long counter = 0;
					FileInputStream fStream = new FileInputStream(pathInterface);			
					DataInputStream dInput = new DataInputStream(fStream);
					BufferedReader bReader = new BufferedReader(new InputStreamReader(dInput));
					
					int counterLine=0;
					String line = null;				
										
					long counterNoContabilizables = 0;
					
					boolean exit=false;
					
					boolean row01Open = false;boolean row02 = false;boolean row06 = false;
					boolean row08 = false;boolean row09 = false;
					boolean row07 = false;
					
					long counterRow01 = 0;
					
					boolean esFondos = false;
					String numeroFondos = "";
										
					String nombreAplicativo = NombreAplicativo.obtieneNombreApp(hashApps, "CFD" + strInterface, numeroMalla);
					
					if(("CFD" + strInterface).equals("CFDLFFONDOS"))
						esFondos = true;
					
					while(((line = bReader.readLine()) != null) && !exit){
											
						//fileX.write(String.valueOf(counter).getBytes());
						//counter++;
						if(line.length() >= 3){
							////////////////////////////////////////////////////////////////////////////////////////
														
							if(line.substring(0, 3).equals("01|")){
								counterRow01++;
								
								if(row01Open){
									//if(row02 && row06){
									if(row02){
										if(!row07){
											retencion = 0.0d;
											iva = 0.0d;
										}
										//Agregar el comprobante a cifras
										agregaCifras(nombreAplicativo);
																				
									}else{
										//row01Open = false;
										counterNoContabilizables++;
										System.out.println(counterLine + " line:" + line);
										//Reset
										
									}
								}
								
								row01Open = true;							
								row02 = false; 
								row06 = false; 
								row08 = false; 
								row09 = false;
								row07 = false;
								
								resetValues();								
																
								String [] strValues = line.split("\\|");
																
								if(!Util.validaPeriodo(strValues[3])){
									row01Open = false;
									counterNoContabilizables++;
									System.out.println(counterLine + " line:" + line);
								}else{
									periodo = strValues[3].trim();
									
									if(esFondos){
										numeroFondos = strValues[2].trim();
										
										System.out.println(numeroFondos);																				
									}
									
								}
							}else if(line.substring(0, 3).equals("02|")){
								if(row01Open){
									//if(!row06 && !row08 && !row09){
									if(!row07){
										if(row02){
											//Reset
											row01Open = false;
											counterNoContabilizables++;
											System.out.println(counterLine + " line:" + line);
										}else{
											row02 = true;
											String [] strValues = line.split("\\|");
											
											if(!Util.validaTipo(strValues[1]) || !Util.validaMoneda(strValues[3], "CFD" + strInterface) || !Util.validaImporte(strValues[6])){
												row01Open = false;
												counterNoContabilizables++;
												System.out.println(counterLine + " line:" + line);
											}else{
												tipo = strValues[1].trim();
												
												if(strValues[3].trim().equals("BME") || strValues[3].trim().equals(""))
													moneda = "MXN";
												else
													moneda = strValues[3].trim();
																					
												
												System.out.println("comision:" + strValues[6].trim());
												
												comision = Double.parseDouble(strValues[6].trim());
												
												System.out.println("comsionAcc:" + comision);
												//System.out.println("OK - tipo:" + tipo + " moneda:" + moneda + " comision:" + comision + " retencion:" + retencion);
												
												//System.out.println("OK - tipo:" + tipo + " moneda:" + moneda + " comision:" + comision + " retencion:" + retencion);
												
												String numero = "";
												
												if(!esFondos)
													numero = strValues[4].trim();
												else
													numero = numeroFondos;
												
												
												System.out.println(numero + "#" + tipo);
												
												if(hashSellados.containsKey(numero + "#" + tipo)){
													fExiste=1;
													
													System.out.println("existe en el archivo XMLOK - fue sellado");
												}else{
													System.out.println("no existe en el archivo XMLOK - fue incidente");
												}
												
											}											
										}	
									}else{
										//Reset
										row01Open = false;
										counterNoContabilizables++;
										System.out.println(counterLine + " line:" + line);
									}									
								}
							}else if(line.substring(0, 3).equals("07|")){
								if(row01Open && row02){
									
									if(row07){
										//Reset
										row01Open = false;
										counterNoContabilizables++;
										System.out.println(counterLine + " line:" + line);
									}else{
										row07 = true;
									
										String [] strValues = line.split("\\|");
										
										if(!Util.validaImporteImpuestos(strValues[1]) || !Util.validaImporteImpuestos(strValues[2])){
											row01Open = false;
											counterNoContabilizables++;
											System.out.println(counterLine + " line:" + line);
											//System.out.println("NO OK - iva:" + strValues[3].trim());
										}else{
											//System.out.println("OK - iva:" + strValues[3].trim());
											
											if(strValues[1] == null || strValues[1].isEmpty()){
												retencion = 0.0d;
											}else{
												System.out.println("retencion:" + strValues[1].trim());
												
												retencion = Double.parseDouble(strValues[1].trim());
												
												System.out.println("RetencionesAcc:" + retencion);
											}
											
											if(strValues[2] == null || strValues[2].isEmpty()){
												iva = 0.0d;
											}else{
												System.out.println("iva:" + strValues[2].trim());
												
												iva = Double.parseDouble(strValues[2].trim());
												
												System.out.println("ivaAcc:" + iva);	
											}										
										}
									}
								}
							}
							
							////////////////////////////////////////////////////////////////////////////////////////
							
						}
						counterLine+=1;
					}
					
					System.out.println("numero de lineas: " + counterLine);
					
					//Revisar si el último bloque de comprobante, es correcto
					
					if(row01Open){
						//if(row02 && row06){
						if(row02){
							if(!row07){
								retencion = 0.0d;
								iva = 0.0d;
							}
							//Agregar el comprobante a cifras
							agregaCifras(nombreAplicativo);
						}else{
							counterNoContabilizables++;
						}
					}else{
						counterNoContabilizables++;
					}
					
					System.out.println("CounterNoContabilizables: " + counterNoContabilizables);
					System.out.println("CounterRow01: " + counterRow01);
					
					//Consumir web service CifrasControlSantander.asmx
					if(this.serviceCifrasPort == null){
						this.serviceCifrasPort = new WebServiceCifrasCliente();								
					}
					//String strUrlWS = "https://santandes.interfactura.com/CifrasControl/CifrasControlSantander.asmx?wsdl";
					String strUrlWS = properties.getUrlWebServiceCifrasControl();
					
					String bloqueCifras = this.serviceCifrasPort.generaCifrasControl(strUrlWS, "", 0, "CFD" + strInterface + strFecha + ".TXT", 0);
					System.out.println("Respuesta WS CifrasContrlSantander:" + bloqueCifras);
					
					if(bloqueCifras.trim().equals("")){
						System.out.println("No se encontraron en Interfactura, estados de cuenta para la interfaz " + "CFD" + strInterface + strFecha + ".TXT");
					}else if(bloqueCifras.trim().lastIndexOf("ERRORCIFRASECB:") != -1){
						System.out.println(bloqueCifras.trim());
					}else{						
						String [] bloquesCifras = bloqueCifras.split("<CCSantan#>");
						for(String bloque : bloquesCifras){
							System.out.println("bloqueCifra:" + bloque);
							String [] cifras = bloque.split("<CCSantan>");

							long totalComprobantes = Long.parseLong(cifras[0].trim());
							String periodo = cifras[1].trim();
							String moneda = cifras[2].trim();
							String tipoComprobante = cifras[3].trim();
							double subTotalAcumulado = Double.parseDouble(cifras[4].trim());
							double impuestosAcumulados = Double.parseDouble(cifras[5].trim());
							double retencionesAcumuladas = Double.parseDouble(cifras[6].trim());
															
							
							String key = periodo + moneda;
							//RetencionComprobantes, NombreEmisor, RFCEmisor, Moneda, TipoComprobante, ComisionAcumulado, ImpuestosAcumulados, RetencionAcumulado
							if(hashSat.containsKey(key)){
								if(tipoComprobante.equals("ingreso")){
									/*System.out.println("Antes I");
									System.out.println("comision: " + hashSat.get(key).getIngresosSAT().getComision());
									System.out.println("iva: " + hashSat.get(key).getIngresosSAT().getIva());
									System.out.println("retencion: " + hashSat.get(key).getIngresosSAT().getRetencion());
									*/
									hashSat.get(key).getIngresosSAT().setComision(hashSat.get(key).getIngresosSAT().getComision() + subTotalAcumulado);
									hashSat.get(key).getIngresosSAT().setIva(hashSat.get(key).getIngresosSAT().getIva() + impuestosAcumulados);
									hashSat.get(key).getIngresosSAT().setRetencion(hashSat.get(key).getIngresosSAT().getRetencion() + retencionesAcumuladas);
									hashSat.get(key).setContadorIngresosSAT(totalComprobantes);
									/*
									System.out.println("Despues I");
									System.out.println("comision: " + hashSat.get(key).getIngresosSAT().getComision());
									System.out.println("iva: " + hashSat.get(key).getIngresosSAT().getIva());
									System.out.println("retencion: " + hashSat.get(key).getIngresosSAT().getRetencion());
									*/
								}else if(tipoComprobante.equals("egreso")){
									/*System.out.println("Antes E");
									System.out.println("comision: " + hashSat.get(key).getEgresosSAT().getComision());
									System.out.println("iva: " + hashSat.get(key).getEgresosSAT().getIva());
									System.out.println("retencion: " + hashSat.get(key).getEgresosSAT().getRetencion());
									*/
									hashSat.get(key).getEgresosSAT().setComision(hashSat.get(key).getEgresosSAT().getComision() + subTotalAcumulado);
									hashSat.get(key).getEgresosSAT().setIva(hashSat.get(key).getEgresosSAT().getIva() + impuestosAcumulados);
									hashSat.get(key).getEgresosSAT().setRetencion(hashSat.get(key).getEgresosSAT().getRetencion() + retencionesAcumuladas);
									hashSat.get(key).setContadorEgresosSAT(totalComprobantes);
									/*
									System.out.println("Despues E");
									System.out.println("comision: " + hashSat.get(key).getEgresosSAT().getComision());
									System.out.println("iva: " + hashSat.get(key).getEgresosSAT().getIva());
									System.out.println("retencion: " + hashSat.get(key).getEgresosSAT().getRetencion());
									*/
								}
							}else{
								
								hashSat.put(key, this.creaCifrasEntidad(periodo, moneda, tipoComprobante, "SAT", subTotalAcumulado, impuestosAcumulados, retencionesAcumuladas, totalComprobantes, nombreAplicativo));								
								
							}						
							
						}
					}
										
					//Cifras XML
											
					File fileCifras = new File(pathCifras + "SEL" + strInterface + strFecha + ".TXT");
					FileOutputStream salidaCifras = new FileOutputStream(fileCifras);
		
					DecimalFormat df = new DecimalFormat("0.00");
					
					Iterator it = hashXml.entrySet().iterator();
					while (it.hasNext()) {
						Map.Entry e = (Map.Entry)it.next();
						CifrasEntidad cifras = new CifrasEntidad();
						cifras = (CifrasEntidad) e.getValue();
						System.out.println("key: " + e.getKey());
						
						salidaCifras.write((nombreAplicativo + "|CFD|" + cifras.getPeriodo() + "||Ingresos||" + 
								cifras.getContadorIngresosEmitidos() + "|" + 
								df.format(cifras.getIngresosEmitidos().getComision()) + "|" +
								df.format(cifras.getIngresosEmitidos().getIva()) + "|" + 
								df.format(cifras.getIngresosEmitidos().getRetencion()) + "|" +
								"|" + "|" + "|" + "\r\n").getBytes("UTF-8"));
						
						salidaCifras.write((nombreAplicativo + "|CFD|" + cifras.getPeriodo() + "||Egresos||" + 
								cifras.getContadorEgresosEmitidos() + "|" + 
								df.format(cifras.getEgresosEmitidos().getComision()) + "|" +
								df.format(cifras.getEgresosEmitidos().getIva()) + "|" + 
								df.format(cifras.getEgresosEmitidos().getRetencion()) + "|" +
								"|" + "|" + "|" + "\r\n").getBytes("UTF-8"));
						
											
						System.out.println("Cifras para " + cifras.getRfc() + " - " + cifras.getMoneda() + " - " + cifras.getNombre());
											
						System.out.println(nombreAplicativo + "|CFD|" + cifras.getPeriodo() + "||Ingresos||" + 
								cifras.getContadorIngresosEmitidos() + "|" + 
								df.format(cifras.getIngresosEmitidos().getComision()) + "|" +
								df.format(cifras.getIngresosEmitidos().getIva()) + "|" + 
								df.format(cifras.getIngresosEmitidos().getRetencion()) + "|" +
								"|" + "|" + "|" );
						
						System.out.println(nombreAplicativo + "|CFD|" + cifras.getPeriodo() + "||Egresos||" + 
								cifras.getContadorEgresosEmitidos() + "|" + 
								df.format(cifras.getEgresosEmitidos().getComision()) + "|" +
								df.format(cifras.getEgresosEmitidos().getIva()) + "|" + 
								df.format(cifras.getEgresosEmitidos().getRetencion()) + "|" +
								"|" + "|" + "|" );
					}
										
					System.out.println("Retencion de comprobantes no contabilizables: " + counterNoContabilizables);
					
					//salidaCifras.write(("Retencion de comprobantes no contabilizables: " + counterNoContabilizables + "\r\n\r\n").getBytes("UTF-8"));
									
					//Cifras del SAT
					File fileCifrasSAT = new File(pathCifras + "SAT" + strInterface + strFecha + ".TXT");
					FileOutputStream salidaCifrasSAT = new FileOutputStream(fileCifrasSAT);
					
					Iterator itSat = hashSat.entrySet().iterator();
					while (itSat.hasNext()) {
						Map.Entry e = (Map.Entry)itSat.next();
						CifrasEntidad cifras = new CifrasEntidad();
						cifras = (CifrasEntidad) e.getValue();
						System.out.println("key: " + e.getKey());
						
						salidaCifrasSAT.write((nombreAplicativo + "|SAT|" + cifras.getPeriodo() + "|" + cifras.getMoneda() + "|Ingresos||" + 
								cifras.getContadorIngresosSAT() + "|" + 
								df.format(cifras.getIngresosSAT().getComision()) + "|" +
								df.format(cifras.getIngresosSAT().getIva()) + "|" + 
								df.format(cifras.getIngresosSAT().getRetencion()) + "|" +
								 "|" + "|" + "|" + "\r\n").getBytes("UTF-8"));
						
						salidaCifrasSAT.write((nombreAplicativo + "|SAT|" + cifras.getPeriodo() + "|" + cifras.getMoneda() + "|Egresos||" + 
								cifras.getContadorEgresosSAT() + "|" + 
								df.format(cifras.getEgresosSAT().getComision()) + "|" +
								df.format(cifras.getEgresosSAT().getIva()) + "|" + 
								df.format(cifras.getEgresosSAT().getRetencion()) + "|" +
								 "|" + "|" + "|" + "\r\n").getBytes("UTF-8"));
						
						
						System.out.println(nombreAplicativo + "|SAT|" + cifras.getPeriodo() + "|" + cifras.getMoneda() + "|Ingresos||" + 
								cifras.getContadorIngresosSAT() + "|" + 
								df.format(cifras.getIngresosSAT().getComision()) + "|" +
								df.format(cifras.getIngresosSAT().getIva()) + "|" + 
								df.format(cifras.getIngresosSAT().getRetencion()) + "|" +
								 "|" + "|" + "|" + "\r\n");
						
						System.out.println(nombreAplicativo + "|SAT|" + cifras.getPeriodo() + "|" + cifras.getMoneda() + "|Egresos||" + 
								cifras.getContadorEgresosSAT() + "|" + 
								df.format(cifras.getEgresosSAT().getComision()) + "|" +
								df.format(cifras.getEgresosSAT().getIva()) + "|" + 
								df.format(cifras.getEgresosSAT().getRetencion()) + "|" +
								 "|" + "|" + "|" + "\r\n");
						
					}
					
					if(bReader != null)
						bReader.close();
					
					if(dInput != null)
						dInput.close();
					
					if(fStream != null)
						fStream.close();
					
					if(bReader2 != null)
						bReader2.close();
					
					if(dInput2 != null)
						dInput2.close();
					
					if(fStream2 != null)
						fStream2.close();
					
					if(salidaCifras != null)
						salidaCifras.close();
					
					if(salidaCifrasSAT != null)
						salidaCifrasSAT.close();

				}catch(Exception e){
					e.printStackTrace();
					System.out.println("ERROR:" + e.getMessage());
				}
			}
			
			//Crea nuevo objeto CifrasEntidad
			public CifrasEntidad creaCifrasEntidad(String periodo, String moneda, String tipo, String tipoInformacion, double comision, double iva, double retencion, long totalComprobantesSAT, String nombreAplicativo){
				CifrasEntidad cifrasEntidad = new CifrasEntidad();
				cifrasEntidad.setPeriodo(periodo);				
				cifrasEntidad.setMoneda(moneda);
				cifrasEntidad.setNombreAplicativo(nombreAplicativo);

				if(tipoInformacion.equals("emitido")){
					//Emitido
					
					if(tipo.equals("I")){						
						/*System.out.println("Antes I");
						System.out.println("comision: " + cifrasEntidad.getIngresosEmitidos().getComision());
						System.out.println("iva: " + cifrasEntidad.getIngresosEmitidos().getIva());
						System.out.println("retencion: " + cifrasEntidad.getIngresosEmitidos().getRetencion());
						*/
						cifrasEntidad.getIngresosEmitidos().setComision(comision);
						cifrasEntidad.getIngresosEmitidos().setIva(iva);
						cifrasEntidad.getIngresosEmitidos().setRetencion(retencion);
						
						cifrasEntidad.setContadorIngresosEmitidos(cifrasEntidad.getContadorIngresosEmitidos() + 1);
						/*
						System.out.println("Despues I");
						System.out.println("comision: " + cifrasEntidad.getIngresosEmitidos().getComision());
						System.out.println("iva: " + cifrasEntidad.getIngresosEmitidos().getIva());
						System.out.println("retencion: " + cifrasEntidad.getIngresosEmitidos().getRetencion());
						*/
					}else if(tipo.equals("E")){
						/*System.out.println("Antes E");
						System.out.println("comision: " + cifrasEntidad.getEgresosEmitidos().getComision());
						System.out.println("iva: " + cifrasEntidad.getEgresosEmitidos().getIva());
						System.out.println("retencion: " + cifrasEntidad.getEgresosEmitidos().getRetencion());
						*/
						cifrasEntidad.getEgresosEmitidos().setComision(comision);
						cifrasEntidad.getEgresosEmitidos().setIva(iva);
						cifrasEntidad.getEgresosEmitidos().setRetencion(retencion);
						
						cifrasEntidad.setContadorEgresosEmitidos(cifrasEntidad.getContadorEgresosEmitidos() + 1);
						/*
						System.out.println("Despues E");
						System.out.println("comision: " + cifrasEntidad.getEgresosEmitidos().getComision());
						System.out.println("iva: " + cifrasEntidad.getEgresosEmitidos().getIva());
						System.out.println("retencion: " + cifrasEntidad.getEgresosEmitidos().getRetencion());
						*/
					}	
				}else if(tipoInformacion.equals("incidente")){
					//Incidente
					
					if(tipo.equals("I")){						
						/*System.out.println("Antes I");
						System.out.println("comision: " + cifrasEntidad.getIngresosIncidentes().getComision());
						System.out.println("iva: " + cifrasEntidad.getIngresosIncidentes().getIva());
						System.out.println("retencion: " + cifrasEntidad.getIngresosIncidentes().getRetencion());
						*/
						cifrasEntidad.getIngresosIncidentes().setComision(comision);
						cifrasEntidad.getIngresosIncidentes().setIva(iva);
						cifrasEntidad.getIngresosIncidentes().setRetencion(retencion);
						cifrasEntidad.setContadorIngresosIncidentes(cifrasEntidad.getContadorIngresosIncidentes() + 1);
						
						/*System.out.println("Despues I");
						System.out.println("comision: " + cifrasEntidad.getIngresosIncidentes().getComision());
						System.out.println("iva: " + cifrasEntidad.getIngresosIncidentes().getIva());
						System.out.println("retencion: " + cifrasEntidad.getIngresosIncidentes().getRetencion());
						*/
						
					}else if(tipo.equals("E")){
						/*System.out.println("Antes E");
						System.out.println("comision: " + cifrasEntidad.getEgresosIncidentes().getComision());
						System.out.println("iva: " + cifrasEntidad.getEgresosIncidentes().getIva());
						System.out.println("retencion: " + cifrasEntidad.getEgresosIncidentes().getRetencion());
						*/
						cifrasEntidad.getEgresosIncidentes().setComision(comision);
						cifrasEntidad.getEgresosIncidentes().setIva(iva);
						cifrasEntidad.getEgresosIncidentes().setRetencion(retencion);
						cifrasEntidad.setContadorEgresosIncidentes(cifrasEntidad.getContadorEgresosIncidentes() + 1);
						/*
						System.out.println("Despues E");
						System.out.println("comision: " + cifrasEntidad.getEgresosIncidentes().getComision());
						System.out.println("iva: " + cifrasEntidad.getEgresosIncidentes().getIva());
						System.out.println("retencion: " + cifrasEntidad.getEgresosIncidentes().getRetencion());
						*/
					}
				}else{
					//SAT
					
					if(tipo.equals("ingreso")){						
						/*System.out.println("Antes I");
						System.out.println("comision: " + cifrasEntidad.getIngresosSAT().getComision());
						System.out.println("iva: " + cifrasEntidad.getIngresosSAT().getIva());
						System.out.println("retencion: " + cifrasEntidad.getIngresosSAT().getRetencion());
						*/
						
						cifrasEntidad.getIngresosSAT().setComision(comision);								
						cifrasEntidad.getIngresosSAT().setIva(iva);								
						cifrasEntidad.getIngresosSAT().setRetencion(retencion);
						
						cifrasEntidad.setContadorIngresosSAT(totalComprobantesSAT);
						/*
						System.out.println("Despues I");
						System.out.println("comision: " + cifrasEntidad.getIngresosSAT().getComision());
						System.out.println("iva: " + cifrasEntidad.getIngresosSAT().getIva());
						System.out.println("retencion: " + cifrasEntidad.getIngresosSAT().getRetencion());
						*/
					}else if(tipo.equals("egreso")){
						/*System.out.println("Antes E");
						System.out.println("comision: " + cifrasEntidad.getEgresosSAT().getComision());
						System.out.println("iva: " + cifrasEntidad.getEgresosSAT().getIva());
						System.out.println("retencion: " + cifrasEntidad.getEgresosSAT().getRetencion());
						*/
						cifrasEntidad.getEgresosSAT().setComision(comision);
						cifrasEntidad.getEgresosSAT().setIva(iva);
						cifrasEntidad.getEgresosSAT().setRetencion(retencion);
						
						cifrasEntidad.setContadorEgresosSAT(totalComprobantesSAT);
						/*
						System.out.println("Despues E");
						System.out.println("comision: " + cifrasEntidad.getEgresosSAT().getComision());
						System.out.println("iva: " + cifrasEntidad.getEgresosSAT().getIva());
						System.out.println("retencion: " + cifrasEntidad.getEgresosSAT().getRetencion());
						*/
					}
				}
								
				return cifrasEntidad;
			}
			
			//Convierte de String a Document
			public Document stringToDocument(String strXML) throws Exception{
				Document domResultado = null;
			
				if (this.db == null){
					this.dbf = DocumentBuilderFactory.newInstance();
					this.db = this.dbf.newDocumentBuilder();
				}
				
				domResultado = this.db.parse(new InputSource(new StringReader(strXML)));	
				
				return domResultado;
			}

			public StreamResult nodeToStreamResult(Node nodo) throws Exception{
				StreamResult sr = null;
				
				if (this.tx == null){
					this.tx = TransformerFactory.newInstance().newTransformer();
					this.tx.setOutputProperty(OutputKeys.INDENT, "yes");
				}
				sr = new StreamResult(new StringWriter());
				DOMSource sourceComprobante = new DOMSource(nodo);
				this.tx.transform(sourceComprobante, sr);	
					
				return sr;
			}
				
			//Genera CIfras Control para malla de REPROCESO ECB

			public void generaCifrasReprocesoECB(String pathCifras, String pathXmlOk, String pathInterface, String strInterface, String strFecha, HashMap<String, String> hashApps) throws Exception{
				//String path = "E:\\folioSAT\\";
				System.out.println("pathCifras: " + pathCifras);
				System.out.println("pathXmlOk: " + pathXmlOk);
				System.out.println("pathInterface: " + pathInterface);							
				
				System.out.println("INTERFACE: " + strInterface);
				System.out.println("FECHA: " + strFecha);
				
				try{
					FileInputStream fStream2 = new FileInputStream(pathXmlOk);			
					DataInputStream dInput2 = new DataInputStream(fStream2);
					BufferedReader bReader2 = new BufferedReader(new InputStreamReader(dInput2));
					
					String line2 = null;
					
					List<String> listSellados = new ArrayList<String>();
					HashMap<String, String> hashSellados= new HashMap<String, String>();
					System.out.println("hashSellados");
					while((line2 = bReader2.readLine()) != null){
						System.out.println("key/value: " + line2);
						hashSellados.put(line2, line2);
						listSellados.add(line2);
					}
					
					HashMap<String, String> hashEmisores= new HashMap<String, String>();
					
					List<FiscalEntity> listFiscal = fiscal.listar();
					
					for(FiscalEntity emisor : listFiscal){
						if(emisor.getFiscalName() != null)
							hashEmisores.put(emisor.getTaxID(), emisor.getFiscalName().trim().toUpperCase());
						else
							hashEmisores.put(emisor.getTaxID(), nombreDefault);
					}
												
					///////////////////////////////////////////////////
					//FileOutputStream fileX = new FileOutputStream(new File(path + "salidaX.txt"));
					//long counter = 0;
					FileInputStream fStream = new FileInputStream(pathInterface);			
					DataInputStream dInput = new DataInputStream(fStream);
					BufferedReader bReader = new BufferedReader(new InputStreamReader(dInput));
					
					int counterLine=0;
					String line = null;				
										
					long counterNoContabilizables = 0;
					
					boolean exit=false;
					
					boolean row01Open = false;boolean row02 = false;boolean row06 = false;
					boolean row08 = false;boolean row09 = false;
					boolean row07 = false;
					
					long counterRow01 = 0;
					
					//boolean esFondos = false;
					String numeroFondos = "";
					
					/*
					if(("CFD" + strInterface).equals("CFDLFFONDOS"))
						esFondos = true;
					*/
					
					while(((line = bReader.readLine()) != null) && !exit){
											
						//fileX.write(String.valueOf(counter).getBytes());
						//counter++;
						if(line.length() >= 3){
							////////////////////////////////////////////////////////////////////////////////////////
														
							if(line.substring(0, 3).equals("01|")){
								counterRow01++;
								
								if(row01Open){
									//if(row02 && row06){
									if(row02){
										if(!row07){
											retencion = 0.0d;
											iva = 0.0d;
										}
										//Agregar el comprobante a cifras
										agregaCifras(nombreAplicativo);
																				
									}else{
										//row01Open = false;
										counterNoContabilizables++;
										System.out.println(counterLine + " line:" + line);
										//Reset
										
									}
								}
								
								row01Open = true;							
								row02 = false; 
								row06 = false; 
								row08 = false; 
								row09 = false;
								row07 = false;
								
								resetValuesReproceso();								
																
								String [] strValues = line.split("\\|");
																
								if(!Util.validaPeriodo(strValues[3])){
									row01Open = false;
									counterNoContabilizables++;
									System.out.println(counterLine + " line:" + line);
								}else{
									periodo = strValues[3].trim();
					
									if(!NombreAplicativo.validaNombreApp(hashApps, strValues[10].trim())){
										row01Open = false;
										counterNoContabilizables++;
										System.out.println(counterLine + " line:" + line);
									}else{
										System.out.println("lineNimbreAplicativo:" + strValues[10].trim());
										nombreAplicativo = strValues[10].trim();
										if(nombreAplicativo.equals("FONDOS")){
											numeroFondos = strValues[2].trim();
											
											System.out.println(numeroFondos);																				
										}
									}
									
									
									
								}
							}else if(line.substring(0, 3).equals("02|")){
								if(row01Open){
									//if(!row06 && !row08 && !row09){
									if(!row07){
										if(row02){
											//Reset
											row01Open = false;
											counterNoContabilizables++;
											System.out.println(counterLine + " line:" + line);
										}else{
											row02 = true;
											String [] strValues = line.split("\\|");
											
											if(!Util.validaTipo(strValues[1]) || !Util.validaMonedaReproceso(strValues[3], nombreAplicativo) || !Util.validaImporte(strValues[6])){
												row01Open = false;
												counterNoContabilizables++;
												System.out.println(counterLine + " line:" + line);
											}else{
												tipo = strValues[1].trim();
												
												if(strValues[3].trim().equals("BME") || strValues[3].trim().equals(""))
													moneda = "MXN";
												else
													moneda = strValues[3].trim();
																					
												
												System.out.println("comision:" + strValues[6].trim());
												
												comision = Double.parseDouble(strValues[6].trim());
												
												System.out.println("comsionAcc:" + comision);
												//System.out.println("OK - tipo:" + tipo + " moneda:" + moneda + " comision:" + comision + " retencion:" + retencion);
												
												//System.out.println("OK - tipo:" + tipo + " moneda:" + moneda + " comision:" + comision + " retencion:" + retencion);
												
												String numero = "";
												
												if(!nombreAplicativo.equals("FONDOS"))
													numero = strValues[4].trim();
												else
													numero = numeroFondos;
												
												
												System.out.println(numero + "#" + tipo);
												
												if(hashSellados.containsKey(numero + "#" + tipo)){
													fExiste=1;
													
													System.out.println("existe en el archivo XMLOK - fue sellado");
												}else{
													System.out.println("no existe en el archivo XMLOK - fue incidente");
												}
												
											}											
										}	
									}else{
										//Reset
										row01Open = false;
										counterNoContabilizables++;
										System.out.println(counterLine + " line:" + line);
									}									
								}
							}else if(line.substring(0, 3).equals("07|")){
								if(row01Open && row02){
									
									if(row07){
										//Reset
										row01Open = false;
										counterNoContabilizables++;
										System.out.println(counterLine + " line:" + line);
									}else{
										row07 = true;
									
										String [] strValues = line.split("\\|");
										
										if(!Util.validaImporteImpuestos(strValues[1]) || !Util.validaImporteImpuestos(strValues[2])){
											row01Open = false;
											counterNoContabilizables++;
											System.out.println(counterLine + " line:" + line);
											//System.out.println("NO OK - iva:" + strValues[3].trim());
										}else{
											//System.out.println("OK - iva:" + strValues[3].trim());
											
											if(strValues[1] == null || strValues[1].isEmpty()){
												retencion = 0.0d;
											}else{
												System.out.println("retencion:" + strValues[1].trim());
												
												retencion = Double.parseDouble(strValues[1].trim());
												
												System.out.println("RetencionesAcc:" + retencion);
											}
											
											if(strValues[2] == null || strValues[2].isEmpty()){
												iva = 0.0d;
											}else{
												System.out.println("iva:" + strValues[2].trim());
												
												iva = Double.parseDouble(strValues[2].trim());
												
												System.out.println("ivaAcc:" + iva);	
											}										
										}
									}
								}
							}
							
							////////////////////////////////////////////////////////////////////////////////////////
							
						}
						counterLine+=1;
					}
					
					System.out.println("numero de lineas: " + counterLine);
					
					//Revisar si el último bloque de comprobante, es correcto
					
					if(row01Open){
						//if(row02 && row06){
						if(row02){
							if(!row07){
								retencion = 0.0d;
								iva = 0.0d;
							}
							//Agregar el comprobante a cifras
							agregaCifras(nombreAplicativo);
						}else{
							counterNoContabilizables++;
						}
					}else{
						counterNoContabilizables++;
					}
					
					System.out.println("CounterNoContabilizables: " + counterNoContabilizables);
					System.out.println("CounterRow01: " + counterRow01);
					
					//Consumir web service CifrasControlSantander.asmx
					if(this.serviceCifrasPort == null){
						this.serviceCifrasPort = new WebServiceCifrasCliente();								
					}
					//String strUrlWS = "https://santandes.interfactura.com/CifrasControl/CifrasControlSantander.asmx?wsdl";
					String strUrlWS = properties.getUrlWebServiceCifrasControl();
					
					String bloqueCifras = this.serviceCifrasPort.generaCifrasControl(strUrlWS, "", 0, "CFD" + strInterface + strFecha + ".TXT", 1);
					System.out.println("Respuesta WS CifrasContrlSantander:" + bloqueCifras);
					
					if(bloqueCifras.trim().equals("")){
						System.out.println("No se encontraron en Interfactura, estados de cuenta para la interfaz " + "CFD" + strInterface + strFecha + ".TXT");
					}else if(bloqueCifras.trim().lastIndexOf("ERRORCIFRASECB:") != -1){
						System.out.println(bloqueCifras.trim());
					}else{
						String [] bloquesCifras = bloqueCifras.split("<CCSantan#>");
						for(String bloque : bloquesCifras){
							System.out.println("bloqueCifra:" + bloque);
							String [] cifras = bloque.split("<CCSantan>");

							long totalComprobantes = Long.parseLong(cifras[0].trim());
							String nombreAplicativo = cifras[1].trim();
							String periodo = cifras[2].trim();
							String moneda = cifras[3].trim();
							String tipoComprobante = cifras[4].trim();
							double subTotalAcumulado = Double.parseDouble(cifras[5].trim());
							double impuestosAcumulados = Double.parseDouble(cifras[6].trim());
							double retencionesAcumuladas = Double.parseDouble(cifras[7].trim());
															
							
							String key = nombreAplicativo + periodo + moneda;
							//RetencionComprobantes, NombreEmisor, RFCEmisor, Moneda, TipoComprobante, ComisionAcumulado, ImpuestosAcumulados, RetencionAcumulado
							if(hashSat.containsKey(key)){
								if(tipoComprobante.equals("ingreso")){
									/*System.out.println("Antes I");
									System.out.println("comision: " + hashSat.get(key).getIngresosSAT().getComision());
									System.out.println("iva: " + hashSat.get(key).getIngresosSAT().getIva());
									System.out.println("retencion: " + hashSat.get(key).getIngresosSAT().getRetencion());
									*/
									hashSat.get(key).getIngresosSAT().setComision(hashSat.get(key).getIngresosSAT().getComision() + subTotalAcumulado);
									hashSat.get(key).getIngresosSAT().setIva(hashSat.get(key).getIngresosSAT().getIva() + impuestosAcumulados);
									hashSat.get(key).getIngresosSAT().setRetencion(hashSat.get(key).getIngresosSAT().getRetencion() + retencionesAcumuladas);
									hashSat.get(key).setContadorIngresosSAT(totalComprobantes);
									/*
									System.out.println("Despues I");
									System.out.println("comision: " + hashSat.get(key).getIngresosSAT().getComision());
									System.out.println("iva: " + hashSat.get(key).getIngresosSAT().getIva());
									System.out.println("retencion: " + hashSat.get(key).getIngresosSAT().getRetencion());
									*/
								}else if(tipoComprobante.equals("egreso")){
									/*System.out.println("Antes E");
									System.out.println("comision: " + hashSat.get(key).getEgresosSAT().getComision());
									System.out.println("iva: " + hashSat.get(key).getEgresosSAT().getIva());
									System.out.println("retencion: " + hashSat.get(key).getEgresosSAT().getRetencion());
									*/
									hashSat.get(key).getEgresosSAT().setComision(hashSat.get(key).getEgresosSAT().getComision() + subTotalAcumulado);
									hashSat.get(key).getEgresosSAT().setIva(hashSat.get(key).getEgresosSAT().getIva() + impuestosAcumulados);
									hashSat.get(key).getEgresosSAT().setRetencion(hashSat.get(key).getEgresosSAT().getRetencion() + retencionesAcumuladas);
									hashSat.get(key).setContadorEgresosSAT(totalComprobantes);
									/*
									System.out.println("Despues E");
									System.out.println("comision: " + hashSat.get(key).getEgresosSAT().getComision());
									System.out.println("iva: " + hashSat.get(key).getEgresosSAT().getIva());
									System.out.println("retencion: " + hashSat.get(key).getEgresosSAT().getRetencion());
									*/
								}
							}else{
								
								hashSat.put(key, this.creaCifrasEntidad(periodo, moneda, tipoComprobante, "SAT", subTotalAcumulado, impuestosAcumulados, retencionesAcumuladas, totalComprobantes, nombreAplicativo));								
								
							}						
							
						}
					}
										
					
					//Cifras XML
					//String nombreAplicativo = Util.otieneNombreAplicativo("CFD" + strInterface);
						
					File fileCifras = new File(pathCifras + "SEL" + strInterface + strFecha + ".TXT");
					FileOutputStream salidaCifras = new FileOutputStream(fileCifras);
		
					DecimalFormat df = new DecimalFormat("0.00");
					
					Iterator it = hashXml.entrySet().iterator();
					while (it.hasNext()) {
						Map.Entry e = (Map.Entry)it.next();
						CifrasEntidad cifras = new CifrasEntidad();
						cifras = (CifrasEntidad) e.getValue();
						System.out.println("key: " + e.getKey());
						
						salidaCifras.write((cifras.getNombreAplicativo() + "|CFD|" + cifras.getPeriodo() + "||Ingresos||" + 
								cifras.getContadorIngresosEmitidos() + "|" + 
								df.format(cifras.getIngresosEmitidos().getComision()) + "|" +
								df.format(cifras.getIngresosEmitidos().getIva()) + "|" + 
								df.format(cifras.getIngresosEmitidos().getRetencion()) + "|" +								 
								"|" + "|" + "|" + "\r\n").getBytes("UTF-8"));
						
						salidaCifras.write((cifras.getNombreAplicativo() + "|CFD|" + cifras.getPeriodo() + "||Egresos||" + 
								cifras.getContadorEgresosEmitidos() + "|" + 
								df.format(cifras.getEgresosEmitidos().getComision()) + "|" +
								df.format(cifras.getEgresosEmitidos().getIva()) + "|" + 
								df.format(cifras.getEgresosEmitidos().getRetencion()) + "|" +
								"|" + "|" + "|" + "\r\n").getBytes("UTF-8"));
						
											
						System.out.println("Cifras para " + cifras.getRfc() + " - " + cifras.getMoneda() + " - " + cifras.getNombre());
											
						System.out.println(cifras.getNombreAplicativo() + "|CFD|" + cifras.getPeriodo() + "||Ingresos||" + 
								cifras.getContadorIngresosEmitidos() + "|" + 
								df.format(cifras.getIngresosEmitidos().getComision()) + "|" +
								df.format(cifras.getIngresosEmitidos().getIva()) + "|" + 
								df.format(cifras.getIngresosEmitidos().getRetencion()) + "|" +
								"|" + "|" + "|");
						
						System.out.println(cifras.getNombreAplicativo() + "|CFD|" + cifras.getPeriodo() + "||Egresos||" + 
								cifras.getContadorEgresosEmitidos() + "|" + 
								df.format(cifras.getEgresosEmitidos().getComision()) + "|" +
								df.format(cifras.getEgresosEmitidos().getIva()) + "|" + 
								df.format(cifras.getEgresosEmitidos().getRetencion()) + "|" +								 
								"|" + "|" + "|");
					}
										
					System.out.println("Retencion de comprobantes no contabilizables: " + counterNoContabilizables);
					
					//salidaCifras.write(("Retencion de comprobantes no contabilizables: " + counterNoContabilizables + "\r\n\r\n").getBytes("UTF-8"));
									
					//Cifras del SAT
					File fileCifrasSAT = new File(pathCifras + "SAT" + strInterface + strFecha + ".TXT");
					FileOutputStream salidaCifrasSAT = new FileOutputStream(fileCifrasSAT);
					
					Iterator itSat = hashSat.entrySet().iterator();
					while (itSat.hasNext()) {
						Map.Entry e = (Map.Entry)itSat.next();
						CifrasEntidad cifras = new CifrasEntidad();
						cifras = (CifrasEntidad) e.getValue();
						System.out.println("key: " + e.getKey());
						
						salidaCifrasSAT.write((cifras.getNombreAplicativo() + "|SAT|" + cifras.getPeriodo() + "|" + cifras.getMoneda() + "|Ingresos||" + 
								cifras.getContadorIngresosSAT() + "|" + 
								df.format(cifras.getIngresosSAT().getComision()) + "|" +
								df.format(cifras.getIngresosSAT().getIva()) + "|" + 
								df.format(cifras.getIngresosSAT().getRetencion()) + "|" +
								"|" + "|" + "|" + "\r\n").getBytes("UTF-8"));
						
						salidaCifrasSAT.write((cifras.getNombreAplicativo() + "|SAT|" + cifras.getPeriodo() + "|" + cifras.getMoneda() + "|Egresos||" + 
								cifras.getContadorEgresosSAT() + "|" + 
								df.format(cifras.getEgresosSAT().getComision()) + "|" +
								df.format(cifras.getEgresosSAT().getIva()) + "|" + 
								df.format(cifras.getEgresosSAT().getRetencion()) + "|" +
								"|" + "|" + "|" + "\r\n").getBytes("UTF-8"));
						
						
						System.out.println(cifras.getNombreAplicativo() + "|SAT|" + cifras.getPeriodo() + "|" + cifras.getMoneda() + "|Ingresos||" + 
								cifras.getContadorIngresosSAT() + "|" + 
								df.format(cifras.getIngresosSAT().getComision()) + "|" +
								df.format(cifras.getIngresosSAT().getIva()) + "|" + 
								df.format(cifras.getIngresosSAT().getRetencion()) + "|" +
								"|" + "|" + "|" + "\r\n");
						
						System.out.println(cifras.getNombreAplicativo() + "|SAT|" + cifras.getPeriodo() + "|" + cifras.getMoneda() + "|Egresos||" + 
								cifras.getContadorEgresosSAT() + "|" + 
								df.format(cifras.getEgresosSAT().getComision()) + "|" +
								df.format(cifras.getEgresosSAT().getIva()) + "|" + 
								df.format(cifras.getEgresosSAT().getRetencion()) + "|" +
								"|" + "|" + "|" + "\r\n");
												
					}
					
					if(bReader != null)
						bReader.close();
					
					if(dInput != null)
						dInput.close();
					
					if(fStream != null)
						fStream.close();
					
					if(bReader2 != null)
						bReader2.close();
					
					if(dInput2 != null)
						dInput2.close();
					
					if(fStream2 != null)
						fStream2.close();
					
					if(salidaCifras != null)
						salidaCifras.close();

				}catch(Exception e){
					e.printStackTrace();
					System.out.println("ERROR:" + e.getMessage());
				}
			}
						
}
