package com.interfactura.firmalocal.xml.ecb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import com.interfactura.firmalocal.domain.entities.CFDIssued;
import com.interfactura.firmalocal.domain.entities.CFDIssuedIn;
import com.interfactura.firmalocal.domain.entities.FiscalEntity;
import com.interfactura.firmalocal.domain.entities.Route;
import com.interfactura.firmalocal.persistence.CFDIssuedIncidenceManager;
import com.interfactura.firmalocal.persistence.CFDIssuedManager;
import com.interfactura.firmalocal.xml.Properties;
import com.interfactura.firmalocal.xml.util.Util;

/**
 * Carga Batch Oracle
 * 
 */
@Component
public class CargaOracleBatch 
{
	
	private Logger logger = Logger.getLogger(CargaOracleBatch.class);
	@Autowired
	public CFDIssuedManager cFDIssuedManager;
	@Autowired
	public CFDIssuedIncidenceManager cFDIssuedIncidenceManager;
	@Autowired
	private Properties properties;
	private String nameFile;
	private String oDate;

	public CargaOracleBatch() {

	}
	
	public CargaOracleBatch(String nameFile, String oDate) 
	{
		this.nameFile = nameFile;
		this.oDate = oDate;
	}

	public void cargaOracle(HashMap<String, FiscalEntity> fis) 
		throws IOException 
	{
		String[] fileNames = this.nameFile.split(",");
		for (int i = 0; i < fileNames.length; i++)
		{
			String composedName = this.properties.getPathSalida() + File.separator + "BD" + fileNames[i].substring(3) + this.oDate + ".TXT";
			String backupName = this.properties.getPathDirBackup() + File.separator + "BD" + fileNames[i].substring(3) + this.oDate + ".TXT";
			File cfdFile = new File(composedName);
			this.write2dbCFD(cfdFile, fis);
			this.copy(composedName, backupName);
		} 
	}

	private void copy(String sourceFile, String destiny) throws IOException 
	{
		long t1 = System.currentTimeMillis();
		FileCopyUtils.copy(new File(sourceFile), new File(destiny));
		long t2 = t1- System.currentTimeMillis();
		System.out.println("TIME: Para copiar archivo a " + destiny + t2 + " ms");
	}
	
	public CFDIssuedIn setCFDIncidence(String msgError, String typeIncidence, String startLine, String endLine) 
	{
		CFDIssuedIn cFDIssuedIncidence = new CFDIssuedIn();
		if (msgError != null) 
		{
			if (msgError.length() > 255) 
			{	cFDIssuedIncidence.setErrorMessage(msgError.substring(0, 255));	} 
			else 
			{	cFDIssuedIncidence.setErrorMessage(msgError);	}
		}
		cFDIssuedIncidence.setSourceFileName(this.nameFile);
		cFDIssuedIncidence.setAuthor("masivo");
		cFDIssuedIncidence.setComplement(typeIncidence);
		cFDIssuedIncidence.setCreationDate(Calendar.getInstance().getTime());
		cFDIssuedIncidence.setStartLine(startLine);
		cFDIssuedIncidence.setEndLine(endLine);
		return cFDIssuedIncidence;
	}

	public void setBDIncidence(CFDIssuedIn cFDIssuedIncidence)
	{
		long t1 = System.currentTimeMillis();
		logger.debug("Guardar CFD Incidence");
		this.cFDIssuedIncidenceManager.update(cFDIssuedIncidence);
		long t2 = t1- System.currentTimeMillis();
		System.out.println("TIME: Actualizacion Incidencia BD " + t2 + " ms");
	}
	
	//Support
	
	public void write2dbCFD(File cfdFile, HashMap<String, FiscalEntity> fis)
	{
		System.out.println("BD: Writing to Oracle");
		FileReader fr = null;
	    BufferedReader br = null;
	    try
	    {
	    	fr = new FileReader(cfdFile);
	    	br = new BufferedReader(fr);
	    	String line = null;
	    	List<CFDIssued> cfds =  new LinkedList<CFDIssued>();
	    	while( ( line = br.readLine()) != null )
	    	{
	    		String values[] = line.split(",");
	    		CFDIssued cfd = convertCFD(values, fis);
	    		cfds.add(cfd);
	    		if(cfds.size() == 10)
	    		{
	    			insertListCFD(cfds);
	    			cfds =  new LinkedList<CFDIssued>();
	    		}
	    	}
	    	insertListCFD(cfds);
		    System.out.println("BD: End Writing to Oracle");
	    }
	    catch(IOException e)
	    {	e.printStackTrace();	}
	    catch (Exception e) 
	    {	e.printStackTrace();	}
	    finally
	    {
	    	try 
	    	{
	    		if ( br != null ) br.close();
	    		if ( fr != null ) fr.close();
	    	}
	    	catch (IOException e) 
	    	{	e.printStackTrace();	}
	    }
	}
	
	private CFDIssued convertCFD(String values[], HashMap<String, FiscalEntity> fis) 
		throws ParseException
	{
		CFDIssued cfd = new CFDIssued();
		cfd.setFiscalEntity(fis.get(values[0]));
		cfd.setTaxIdReceiver(values[1]);
		cfd.setAuthor(values[2]);
		int idx = values[3].indexOf("T");
		Date d = Util.convertirString(values[3].substring(0, idx), values[3]);
		cfd.setCreationDate(d);
		int idx2 = values[4].indexOf("T");
		Date d2 = Util.convertirString(values[4].substring(0, idx), values[4]);
		cfd.setIssueDate(d2);
		cfd.setFolio(values[5]);
		cfd.setComplement(values[6]);
		if (values[7] != null)
		{ 	cfd.setContractNumber(values[7]);		}
		if (values[8] != null)
		{ 	cfd.setCostCenter(values[8]);		}
		if (values[9] != null)
		{ 	cfd.setCustomerCode(values[9]);		}
		if (values[10] != null)
		{ 	cfd.setPeriod(values[10]);		}
		cfd.setIva(Double.parseDouble(values[11]));
		cfd.setStatus(Integer.parseInt(values[12]));
		cfd.setSubTotal(Double.parseDouble(values[13]));
		cfd.setTotal(Double.parseDouble(values[14]));
		cfd.setFormatType(Integer.parseInt(values[15]));
		cfd.setCfdType(values[16]);
		cfd.setSourceFileName(values[17]);
		cfd.setStartLine(values[18]);
		cfd.setEndLine(values[19]);		
		Route r = new Route();
		r.setRoute(values[20]);
		r.setCreationDate(new Date());
		r.setIssueDate(d);
		r.setAuthor(values[2]);
		cfd.setFilePath(r);
		return cfd;
	}
	
	public void insertListCFD(List<CFDIssued> cfds) 
		throws Exception
	{		
		long t1 = System.currentTimeMillis();
		System.out.println("TIME: Calculando tiempo lote BD...");
		boolean cleanList = true;
		try
		{	this.cFDIssuedManager.update(cfds);	}
		catch (Exception e1)
		{
			String ids = "";
			for (int j = 0; j < cfds.size(); j++)
			{
				CFDIssued cf = cfds.get(j);
				ids += cf.getId() + "-" + cf.getFolio() + "|";
			}
			System.out.println("BD: Reintentando ids " + ids);
			try
			{	
				System.out.println("BD: Intento 2");
				this.cFDIssuedManager.update(cfds);	
			}
			catch (Exception e2)
			{
				try
				{	
					System.out.println("BD: Intento 3");
					this.cFDIssuedManager.update(cfds);	
				}
				catch (Exception e3)
				{	
					try
					{	
						System.out.println("BD: Intento 4");
						this.cFDIssuedManager.update(cfds);	
					}
					catch (Exception e4)
					{	
						try
						{	
							System.out.println("BD: Intento 5");
							this.cFDIssuedManager.update(cfds);	
						}
						catch (Exception e5)
						{	
							try
							{	
								System.out.println("BD: Intento 6");
								this.cFDIssuedManager.update(cfds);	
							}
							catch (Exception e6)
							{	
								try
								{	
									System.out.println("BD: Intento 7");
									this.cFDIssuedManager.update(cfds);	
								}
								catch (Exception e7)
								{	
									try
									{
										System.out.println("BD: Intento 8");
										this.cFDIssuedManager.update(cfds);	
									}
									catch (Exception e8)
									{	
										try
										{	
											System.out.println("BD: Intento 9");
											this.cFDIssuedManager.update(cfds);	
										}
										catch (Exception e9)
										{	
											try
											{	
												System.out.println("BD: Intento 10");
												this.cFDIssuedManager.update(cfds);	
											}
											catch (Exception e10)
											{	
												try
												{	
													System.out.println("BD: Intento 11");
													this.cFDIssuedManager.update(cfds);	
												}
												catch (Exception e11)
												{																
													try
													{	
														System.out.println("BD: Intento 12");
														this.cFDIssuedManager.update(cfds);	
													}
													catch (Exception e12)
													{										
														try
														{	
															System.out.println("BD: Intento 13");
															this.cFDIssuedManager.update(cfds);	
														}	
														catch (Exception e13)
														{	
															try
															{	
																System.out.println("BD: Intento 14");
																this.cFDIssuedManager.update(cfds);	
															}
															catch (Exception e14)
															{	
																try
																{
																	System.out.println("BD: Intento 15");
																	this.cFDIssuedManager.update(cfds);
																}
																catch (Exception e15)
																{
																	e15.printStackTrace();
																	long t2 = t1- System.currentTimeMillis();
																	System.out.println("TIME: Tiempo Lote BD -- No pudo completar transaccion " + t2 + " ms");
																	cleanList = false;
																	throw new Exception(e15);
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
		long t2 = t1- System.currentTimeMillis();
		System.out.println("TIME: Tiempo Lote BD " + t2 + " ms");
	}
	
	//--- Accesors

	public String getNameFile() {
		return nameFile;
	}

	public void setNameFile(String nameFile) {
		this.nameFile = nameFile;
	}

	public String getoDate() {
		return oDate;
	}

	public void setoDate(String oDate) {
		this.oDate = oDate;
	}
	
}