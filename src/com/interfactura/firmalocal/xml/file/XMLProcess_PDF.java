package com.interfactura.firmalocal.xml.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.GeneralSecurityException;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.ssl.PKCS8Key;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.interfactura.firmalocal.domain.entities.SealCertificate;
import com.interfactura.firmalocal.utils.Base64;
import com.interfactura.firmalocal.xml.Certificate;
import com.interfactura.firmalocal.xml.Properties;
import com.interfactura.firmalocal.xml.util.Util;
import com.interfactura.firmalocal.xml.validation.ValidationXML;
import com.interfactura.recepcionmasiva.service.ValidationException;

@Component
public class XMLProcess_PDF {
	
	@Autowired
	private Properties properties;

	private Logger logger = Logger.getLogger(XMLProcess.class);

	
	/**
	 * Genera la cadena Original Texto
	 * @param out
	 * @return
	 * @throws TransformerException
	 * @throws ValidationException 
	 */
	public ByteArrayOutputStream generatesOriginalString(ByteArrayOutputStream out, String version) throws TransformerException, ValidationException {
		logger.info("Generando Cadena Original");
		ByteArrayOutputStream cadena=new ByteArrayOutputStream();
		SAXTransformerFactory tr = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
		Transformer trans = null;
		try {
			trans = tr.newTransformer(new StreamSource(""));
			/*if(version.equals("2.2")) {
				trans = tr.newTransformer(new StreamSource(properties.getPathFileSello22()));
			} else if (version.equals("2.0")) {
				trans = tr.newTransformer(new StreamSource(properties.getPathFileSello()));
			} else if (version.equals("3.0")) {
				trans = tr.newTransformer(new StreamSource(properties.getPathFileSelloCFDI()));
			} else if (version.equals("3.2")) {
				trans = tr.newTransformer(new StreamSource(properties.getPathFileSelloCFDI32()));
			}*/
		} catch (TransformerConfigurationException e) {
			throw new ValidationException("Generating original string error(Transformer config)", e);
		}
		StreamResult result = new StreamResult(cadena);
		try {
			trans.transform(
					new StreamSource(new ByteArrayInputStream(out.toByteArray())),
					result);
		} catch (TransformerException e) {
			throw new ValidationException("Generating original string error(Transformer)", e);
		}

		return cadena;
	}
	
	/**
	 * Genera la cadena Original Texto CFDI
	 * @param out
	 * @return
	 * @throws TransformerException
	 */
	public ByteArrayOutputStream generatesOriginalStringCFDI(ByteArrayOutputStream out) throws TransformerException {
		logger.info("Generando Cadena Original");
		ByteArrayOutputStream cadena=new ByteArrayOutputStream();
		SAXTransformerFactory tr = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
		//Transformer trans = tr.newTransformer(new StreamSource(properties.getPathFileSelloCFDI()));
		Transformer trans = tr.newTransformer(new StreamSource(""));
		StreamResult result = new StreamResult(cadena);
		trans.transform(new StreamSource(new ByteArrayInputStream(out.toByteArray())),result);
		
		return cadena;
	}

	/**
	 * Sustituye la cadena original
	 * El numero de Certificado
	 * Y el certificado
	 * @param out
	 * @param cadena
	 * @param cer
	 * @return
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public ByteArrayOutputStream replacesOriginalString(ByteArrayOutputStream out, ByteArrayOutputStream cadena, Certificate cer) 
		throws IOException, GeneralSecurityException {
		logger.info("Reemplazando Cadena Original");
		ByteArrayOutputStream outBW = new ByteArrayOutputStream();
		InputStreamReader in = new InputStreamReader(new ByteArrayInputStream(out.toByteArray()));
		OutputStreamWriter outW = new OutputStreamWriter(outBW);
		BufferedReader bF = new BufferedReader(in);
		BufferedWriter bW = new BufferedWriter(outW);
		
		String sello=sealEncryption(cadena,cer.getCertificado());
		sello=Util.replaceSR(sello);
		String line=null;
		boolean f1=false;
		boolean f2=false;
		boolean f3=false;
		while (bF.ready()) {
			line=bF.readLine();
	
			if(!f1){
				if(line.lastIndexOf(properties.getLblNO_CERTIFICADO())>0){
					line=line.replaceAll(properties.getLblNO_CERTIFICADO(),cer.getCertificado().getSerialNumber());
					f1=true;
				}
			}
			
			if(!f2){
				if(line.lastIndexOf(properties.getLblCERTIFICADO())>0){
					line=line.replaceAll(properties.getLblCERTIFICADO(),cer.encripta());
					f2=true;
				}
			}
			
			if(!f3){
				if(line.lastIndexOf(properties.getLabelSELLO())>0){
					line=line.replaceAll(properties.getLabelSELLO(),sello);
					f3=true;
				}
			}
			
			bW.write(line);
		}
		bF.close();
		bW.close();

		return outBW;
	}
	
	/**
	 * 
	 * @param cadena
	 * @param cer
	 * @return
	 * @throws GeneralSecurityException
	 */
	public String sealEncryption(ByteArrayOutputStream cadena, SealCertificate cer) throws GeneralSecurityException {
		RSAPrivateKey pk = null;
		if (cer!=null){
			
			PKCS8Key pk8 = new PKCS8Key(cer.getPrivateKey(), cer.getPrivateKeyPassword().toCharArray());
	
			if (pk8.isRSA()) {
				pk = (RSAPrivateKey) pk8.getPrivateKey();
			}
	
			Signature firma = Signature.getInstance("SHA1withRSA");
			firma.initSign(pk);
			firma.update(cadena.toByteArray());
			
			return Base64.encodeToString(firma.sign(), false);
		} else {
			throw new GeneralSecurityException("No hay llaves");
		}
	}

	/**
	 * Escribe en disco duro
	 * @param out
	 * @param fecha
	 * @throws IOException
	 */
	public void writeHardDrive(ByteArrayOutputStream out,String fecha) throws IOException {
		this.writeHardDrive(properties.getNameFileXML()+fecha+".xml", out);
	}
	
	/**
	 * 
	 * @param fecha
	 * @param isECB
	 * @param cont
	 * @param incidencia
	 * @return
	 */
	public String generateFileName(String fecha, boolean isECB, int cont, boolean incidencia){
		int endS=6;
		if(isECB)
		{
			String subfolder = fecha.substring(0,4) + File.separator + fecha.substring(4,endS);
			return getDirectory(incidencia)+ subfolder + File.separator + "ecb"+fecha+cont+".xml";
		} 
		else 
		{
			String subfolder = fecha.substring(0,4) + File.separator + fecha.substring(4,endS);
			return getDirectory(incidencia)+ subfolder + File.separator + "factura" + fecha + cont + ".xml";
		}
	}
	
	/**
	 * 
	 * @param incidence
	 * @return
	 */
	public String getDirectory(boolean incidence){
		if(incidence) {
			return properties.getPathDirIncd();
		} else {
			return properties.getPathDirGenr();
		}
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
	public void writeHardDrive(ByteArrayOutputStream out,String fecha, boolean isECB, 
			int cont, boolean incidencia) throws IOException {
		this.writeHardDrive(this.generateFileName(fecha, isECB, cont, incidencia), out);
	}
	
	/**
	 * 
	 * @param name
	 * @param out
	 * @throws IOException
	 */
	public void writeHardDrive(String name,ByteArrayOutputStream out) 
		throws IOException
	{
		String directory = name.substring(0, name.lastIndexOf(File.separator));
		File file1 = new File(directory);
		boolean exists = file1.exists();
		if (!exists) 
		{
			  String directoryA = directory.substring(0, directory.lastIndexOf(File.separator));
		      File file2 = new File(directoryA);
		      boolean exists2 = file2.exists();
		      if (!exists2)
		      {		file2.mkdir();		}
		      file1.mkdir();  
		}
		
		FileOutputStream r = new FileOutputStream(name);
		r.write(out.toByteArray());
		r.close();
	}


}
