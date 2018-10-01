package com.interfactura.firmalocal.xml.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.security.GeneralSecurityException;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.ValidatorHandler;

import org.apache.commons.ssl.PKCS8Key;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.interfactura.firmalocal.domain.entities.OpenJpa;
import com.interfactura.firmalocal.domain.entities.SealCertificate;
import com.interfactura.firmalocal.utils.Base64;
import com.interfactura.firmalocal.xml.Properties;
import com.interfactura.firmalocal.xml.util.Util;
import com.interfactura.firmalocal.xml.validation.ValidationXML;

@Component
public class XMLProcess 
{
	
	@Autowired
	private ValidationXML validator;
	@Autowired
	private Properties properties;
	private Transformer transf;
	private Logger logger = Logger.getLogger(XMLProcess.class);
	
	/**
	 * Valida el XML generado
	 * 
	 * @param out
	 *            file to validate
	 * @throws Exception
	 */
	public void valida(ByteArrayOutputStream out, ValidatorHandler val) 
		throws Exception 
	{
		validator.valida(out, val);
	}

	/**
	 * Genera cadena original del XML
	 * @param out
	 * @return
	 * @throws TransformerException
	 */
	public ByteArrayOutputStream generatesOriginalString(ByteArrayOutputStream out) 
		throws TransformerException 
	{
		
		/*
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date dateInicio = new Date();
		System.out.println("TIMEINICIOTransformacion Cadena:" + dateFormat.format(dateInicio) + " M" + System.currentTimeMillis());
		*/
		logger.debug("Generando Cadena Original");
		
		long t1 = System.currentTimeMillis();
		ByteArrayOutputStream cadena = new ByteArrayOutputStream();
		StreamResult result = new StreamResult(cadena);
		transf.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); 
		transf.transform(new StreamSource(new ByteArrayInputStream(out.toByteArray())), result);
		long t2 = t1- System.currentTimeMillis();
		System.out.println("TIME: Transformacion Cadena " + t2 + " ms");
		/*
		Date dateInicio2 = new Date();
		System.out.println("TIMEFINALTransformacion Cadena:" + dateFormat.format(dateInicio2) + " M" + System.currentTimeMillis());
		*/
		return cadena;
	}
	
	
	
	/**
	 * Sustituye la cadena original
	 * */
	public ByteArrayOutputStream replacesOriginalStringPagos(ByteArrayOutputStream out, SealCertificate certificate, 
			String sello, String lugarExpedicion) 
		throws IOException 
	{
		
		/*
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date dateInicio = new Date();
		System.out.println("TIMEINICIOCadena original reemplazada:" + dateFormat.format(dateInicio) + " M" + System.currentTimeMillis());
		*/
		logger.debug("Reemplazando Cadena Original");
		
		long t1 = System.currentTimeMillis();
		ByteArrayOutputStream outBW = new ByteArrayOutputStream();
		InputStreamReader in = new InputStreamReader(new ByteArrayInputStream(out.toByteArray()), "UTF-8");
		OutputStreamWriter outW = new OutputStreamWriter(outBW, "UTF-8");
		BufferedReader bF = new BufferedReader(in);
		BufferedWriter bW = new BufferedWriter(outW);
		String line=null;
		boolean f1 = false;
		boolean f2 = false;
		boolean f3 = false;
		boolean f4 = false;
		boolean f5 = false;
		boolean f6 = false;
		while (bF.ready()) 
		{
			line = bF.readLine();
			if (!f1) 
			{
				if (line.lastIndexOf(properties.getLblNO_CERTIFICADO()) > 0) 
				{
					line = line.replaceAll(properties.getLblNO_CERTIFICADO(),
							certificate.getSerialNumber());
					f1 = true;
				}
			}

			if (!f2) 
			{
				if (line.lastIndexOf(properties.getLblCERTIFICADO()) > 0) 
				{
					line = line.replaceAll(properties.getLblCERTIFICADO(),
							Util.replaceSR(Base64.encodeToString(certificate.getCertificate(),false)));
					f2 = true;
				}
			}

			if (!f3) 
			{
				if (line.lastIndexOf(properties.getLabelSELLO()) > 0) 
				{
					line = line.replaceAll(properties.getLabelSELLO(), sello);
					f3 = true;
				}
			}
			
			

			
			bW.write(line);
		}
		bF.close();
		bW.close();
		long t2 = t1- System.currentTimeMillis();
		System.out.println("TIME: Cadena Original Reemplazada " + t2 + " ms");
		/*		
		Date dateInicio2 = new Date();
		System.out.println("TIMEFINALCadena original reemplazada:" + dateFormat.format(dateInicio2) + " M" + System.currentTimeMillis());
		*/
		return outBW;
	}


	/**
	 * Sustituye la cadena original El numero de Certificado Y el certificado
	 * 
	 * @param out
	 * @param certificate
	 * @param sello
	 * @return
	 * @throws IOException
	 */
	public ByteArrayOutputStream replacesOriginalString(ByteArrayOutputStream out, SealCertificate certificate, 
			String sello, String lugarExpedicion, String metodoPago, String formaPago, boolean isECB) 
		throws IOException 
	{
		
		/*
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date dateInicio = new Date();
		System.out.println("TIMEINICIOCadena original reemplazada:" + dateFormat.format(dateInicio) + " M" + System.currentTimeMillis());
		*/
		logger.debug("Reemplazando Cadena Original");
		
		long t1 = System.currentTimeMillis();
		ByteArrayOutputStream outBW = new ByteArrayOutputStream();
		InputStreamReader in = new InputStreamReader(new ByteArrayInputStream(out.toByteArray()), "UTF-8");
		OutputStreamWriter outW = new OutputStreamWriter(outBW, "UTF-8");
		BufferedReader bF = new BufferedReader(in);
		BufferedWriter bW = new BufferedWriter(outW);
		String line=null;
		boolean f1 = false;
		boolean f2 = false;
		boolean f3 = false;
		boolean f4 = false;
		boolean f5 = false;
		boolean f6 = false;
		while (bF.ready()) 
		{
			line = bF.readLine();
			if (!f1) 
			{
				if (line.lastIndexOf(properties.getLblNO_CERTIFICADO()) > 0) 
				{
					line = line.replaceAll(properties.getLblNO_CERTIFICADO(),
							certificate.getSerialNumber());
					f1 = true;
				}
			}

			if (!f2) 
			{
				if (line.lastIndexOf(properties.getLblCERTIFICADO()) > 0) 
				{
					line = line.replaceAll(properties.getLblCERTIFICADO(),
							Util.replaceSR(Base64.encodeToString(certificate.getCertificate(),false)));
					f2 = true;
				}
			}

			if (!f3) 
			{
				if (line.lastIndexOf(properties.getLabelSELLO()) > 0) 
				{
					line = line.replaceAll(properties.getLabelSELLO(), sello);
					f3 = true;
				}
			}
			
			if (!f4) 
			{
				if (line.lastIndexOf(properties.getLabelLugarExpedicion()) > 0) 
				{
					line = line.replaceAll(properties.getLabelLugarExpedicion(), lugarExpedicion);
					f4 = true;
				}
			}
			
			if ((!f5)&&(isECB)) 
			{
				if (line.lastIndexOf(properties.getLabelMetodoPago()) > 0) 
				{
					line = line.replaceAll(properties.getLabelMetodoPago(), metodoPago);
					f5 = true;
				}
			}
			
			if ((!f6)&&(isECB)) 
			{
				if (line.lastIndexOf(properties.getlabelFormaPago()) > 0) 
				{
					line = line.replaceAll(properties.getlabelFormaPago(), formaPago);
					f6 = true;
				}
			}
			bW.write(line);
		}
		bF.close();
		bW.close();
		long t2 = t1- System.currentTimeMillis();
		System.out.println("TIME: Cadena Original Reemplazada " + t2 + " ms");
		/*		
		Date dateInicio2 = new Date();
		System.out.println("TIMEFINALCadena original reemplazada:" + dateFormat.format(dateInicio2) + " M" + System.currentTimeMillis());
		*/
		return outBW;
	}

	/**
	 * 
	 * @param cadena
	 * @param cer
	 * @return
	 * @throws GeneralSecurityException
	 */
	public String sealEncryption(ByteArrayOutputStream cadena, SealCertificate cer) 
		throws GeneralSecurityException 
	{	/*
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date dateInicio = new Date();
		System.out.println("TIMEINICIOEncripcion:" + dateFormat.format(dateInicio) + " M" + System.currentTimeMillis());
		*/
		long t1 = System.currentTimeMillis();
		RSAPrivateKey pk = null; 
		if (cer != null) 
		{
			PKCS8Key pk8 = new PKCS8Key(cer.getPrivateKey(), cer
					.getPrivateKeyPassword().toCharArray());			
			if (pk8.isRSA()) 
			{	pk = (RSAPrivateKey) pk8.getPrivateKey();	}

			Signature firma = Signature.getInstance("SHA1withRSA"); 
			try {				
				if(new String(cadena.toByteArray(), "UTF-8").split("\\|")[2].equals("3.3")){
					firma = Signature.getInstance("SHA256withRSA");
				}
				firma.initSign(pk);
			
				logger.info("CadenaOriginal firma:" + firma);
				logger.info("CadenaOriginal CadenaOriginal:" + new String(cadena.toByteArray()));
				logger.info("CadenaOriginal CadenaOriginal:" + new String(cadena.toByteArray(), "UTF-8"));
			} catch (Exception ex) {
				logger.error(ex);
			}
			firma.update(cadena.toByteArray());
			
			long t2 = t1- System.currentTimeMillis();
			System.out.println("TIME: Encripcion Sello " + t2 + " ms");
			/*
			Date dateInicio2 = new Date();
			System.out.println("TIMEFINALEncripcion:" + dateFormat.format(dateInicio2) + " M" + System.currentTimeMillis());
			*/
			//return Util.replaceSR(Base64.encodeToString(firma.sign(), false));
			return Base64.encodeToString(firma.sign(), false);
		} 
		else 
		{	throw new GeneralSecurityException("No hay llaves");	}
	}

	/**
	 * Escribe en disco duro
	 * @param out
	 * @param fecha
	 * @throws IOException
	 */
	public void writeHardDrive(ByteArrayOutputStream out, String fecha, String idProceso)
			throws IOException 
	{
		// OCT 28 Dic
		this.createDirectory(properties.getNameFileXML() + idProceso);
		this.writeHardDrive(properties.getNameFileXML() + fecha + ".xml", out);	
	}
	
	/**
	 * 
	 * @param fecha
	 * @param isECB
	 * @param cuenta
	 * @param periodo
	 * @param incidencia
	 * @return
	 */
	public String setName(String fecha, boolean isECB, String cuenta, String periodo,
			boolean incidencia, String idProceso) 
	{
		if (isECB) 
		{	
			String dirBase = this.getDirectory(incidencia);
			if (!(incidencia))
			{
				String pathToTest = dirBase + File.separator + idProceso;
				this.createDirectory(pathToTest);
				return pathToTest + File.separator + cuenta + periodo + ".xml";
			}
			else
			{
				return dirBase + cuenta + periodo + ".xml";
			}
		} 
		else 
		{	return getDirectory(incidencia) + "fac-" + cuenta + "-" + periodo + "-" + System.currentTimeMillis() + ".xml";	}
	}

	/**
	 * 
	 * @param incidence
	 * @return
	 */
	public String getDirectory(boolean incidence) 
	{
		if (incidence) 
		{	return properties.getPathDirIncd();		} 
		else 
		{	return properties.getPathDirGenr();		}
	}

	/**
	 * 
	 * @param out
	 * @param fecha
	 * @param isECB
	 * @param cont
	 * @param incidencia
	 * @throws IOException
	 */
	public String writeHardDrive(ByteArrayOutputStream out, String fecha,
			boolean isECB, String emisor, String serieFolio, boolean incidencia, String idProceso) 
	throws IOException 
	{
		String fileName = this.setName(fecha, isECB, emisor, serieFolio, incidencia, idProceso);
		this.writeHardDrive(fileName, out);
		return fileName;
	}

	/**
	 * 
	 * @param name
	 * @param out
	 * @throws IOException
	 */
	public void writeHardDrive(String name, ByteArrayOutputStream out)
			throws IOException 
	{/*
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date dateInicio = new Date();
		System.out.println("TIMEINICIOEscritura XML:" + dateFormat.format(dateInicio) + " M" + System.currentTimeMillis());
		*/
		long t1 = System.currentTimeMillis();
		createDirectory(name);
		FileOutputStream r = new FileOutputStream(name);
		
		r.write(out.toByteArray());		
		r.close();
		//File file=new File(name);
		//file.setReadable(true, true);
		long t2 = t1- System.currentTimeMillis();
		System.out.println("TIME: Escritura XML '" + name + "'" + t2 + " ms");
		/*
		Date dateInicio2 = new Date();
		System.out.println("TIMEFINALEscritura XML:" + dateFormat.format(dateInicio2) + " M" + System.currentTimeMillis());
		*/
	}
	
	/**
	 * 
	 * @param name
	 */
	private void createDirectory(String name)
	{
		File file=new File(name.substring(0,name.lastIndexOf(File.separator)));
		file.mkdirs();
	}

	public ValidationXML getValidator() {
		return validator;
	}

	public void setValidator(ValidationXML validator) {
		this.validator = validator;
	}

	public Transformer getTransf() {
		return transf;
	}

	public void setTransf(Transformer transf) {
		this.transf = transf;
	}
}
