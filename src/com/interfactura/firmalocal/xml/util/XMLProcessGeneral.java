package com.interfactura.firmalocal.xml.util;

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
import com.interfactura.firmalocal.xml.file.XMLProcess;
import com.interfactura.firmalocal.xml.validation.ValidationXML;
import com.interfactura.recepcionmasiva.service.ValidationException;

@Component
public class XMLProcessGeneral {

	@Autowired
	private ValidationGeneralXML validator;
	@Autowired
	private Properties properties;

	private Logger logger = Logger.getLogger(XMLProcess.class);

	/**
	 * Valida el XML generado CFD
	 * 
	 * @param out file to validate
	 * @throws Exception
	 */
	public void valida(ByteArrayOutputStream out) throws Exception  {
		logger.info("Validando archivo xml: " + properties.getPathFileValidation());
		validator.valida(out,properties.getPathFileValidation(),properties.getPathFileValidationECB(),
				properties.getPathFileValidaationADD());
	}
	
	public void valida22(ByteArrayOutputStream out) throws Exception  {
		logger.info("Validando archivo xml: " + properties.getPathFileValidation22());
		validator.valida(out,properties.getPathFileValidation22(),properties.getPathFileValidationECB(),
				properties.getPathFileValidaationADD());
	}
	
	/**
	 * Valida el XML generado CFDI
	 * 
	 * @param out file to validate
	 * @throws Exception
	 */
	public void validaCFDI(ByteArrayOutputStream out) throws Exception  {
		logger.info("Validando archivo xml: " + properties.getPathFileValidationCFDI());
		
		validator.valida(out,properties.getPathFileValidationCFDI(), properties.getPathTimbres(),
				properties.getPathFileValidationECB(),properties.getPathFileValidaationADD());
	}
	
	public void validaCFDI32(ByteArrayOutputStream out) throws Exception  {
		logger.info("Validando archivo xml: " + properties.getPathFileValidationCFDI32());
		//System.out.println("xsdDivisas: " + "/planCFD/procesos/Schemas/interfaces/divisas.xsd");
		//System.out.println("Validando archivo xml: " + properties.getPathFileValidationCFDI32());
		//System.out.println("xmlValidar: " + out.toString("UTF-8"));
		
		validator.valida(out,properties.getPathFileValidationCFDI32(), properties.getPathTimbres2(),
				properties.getPathFileValidationECB(),properties.getPathFileValidaationADD(), properties.getPathDonataria(), properties.getPathDivisas());
	}

	public void validaCFDI33(ByteArrayOutputStream out) throws Exception  {
		try{
			logger.info("Validando archivo v3.3 xml: " + properties.getPathFileValidationCFDI33());
			validator.valida(out,properties.getPathFileValidationCFDI33(), properties.getPathTimbres33(),
					properties.getPathFileValidationECB(),properties.getPathFileValidaationADD(), properties.getPathDonataria(), properties.getPathDivisas(), properties.getPathPagos());
		}catch(Exception e){
			throw new Exception("Hubo un problema al validar con el XSD V3.3.  Detalle del msg\n"+e.getMessage());
		}
	}
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
			if(version.equals("2.2")) {
				trans = tr.newTransformer(new StreamSource(properties.getPathFileSello22()));
			} else if (version.equals("2.0")) {
				trans = tr.newTransformer(new StreamSource(properties.getPathFileSello()));
			} else if (version.equals("3.0")) {
				trans = tr.newTransformer(new StreamSource(properties.getPathFileSelloCFDI()));
			} else if (version.equals("3.2")) {
				trans = tr.newTransformer(new StreamSource(properties.getPathFileSelloCFDI32()));
			}else if (version.equals("3.3")) {
				trans = tr.newTransformer(new StreamSource(properties.getPathFileSelloCFDI33()));
			}
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
		try {
			logger.info("CadenaOriginal StreamResult:" + result);
			logger.info("CadenaOriginal Version:" + version);
			logger.info("CadenaOriginal CadenaOriginal:" + new String(cadena.toByteArray()));
			logger.info("CadenaOriginal CadenaOriginal:" + new String(cadena.toByteArray(), "UTF-8"));
		} catch (Exception ex) {
			logger.error(ex);
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
		Transformer trans = tr.newTransformer(new StreamSource(properties.getPathFileSelloCFDI()));
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

	public ValidationGeneralXML getValidator() {
		return validator;
	}

	public void setValidator(ValidationGeneralXML validator) {
		this.validator = validator;
	}

}
