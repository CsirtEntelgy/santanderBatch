package com.interfactura.firmalocal.xml.validation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.TypeInfoProvider;
import javax.xml.validation.ValidatorHandler;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

@Component
public class ValidationXML_Masivo {
	private Logger logger = Logger.getLogger(ValidationXML_Masivo.class);
	
	/**
	 * 
	 * @return
	 * @throws SAXException
	 */
	private ValidatorHandler createValidatorHandler(String...path) throws SAXException {
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);//(XMLConstants.XML_NS_URI)
		StreamSource[] schemas=new StreamSource[path.length];
		for(int i=0; i<path.length; i++){
			schemas[i] = new StreamSource(new File(path[i]));
			logger.info("Esquemas: "+path[i]);
		}
		Schema schema = schemaFactory.newSchema(schemas);
		return schema.newValidatorHandler();
	}

	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	private XMLReader createXMLReader() throws Exception {
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		parserFactory.setNamespaceAware(true);
		return parserFactory.newSAXParser().getXMLReader();
	}

	/**
	 * 
	 * @param xmlReader
	 * @param out
	 * @throws Exception
	 */
	private void parse(XMLReader xmlReader, ByteArrayOutputStream out) 
	throws SAXParseException,SAXException,Exception 
	{
		InputSource inSource = new InputSource(new ByteArrayInputStream(out.toByteArray()));
		xmlReader.parse(inSource);
	}

	/**
	 * 
	 * @param out
	 * @return
	 * @throws Exception
	 */
	public boolean valida(ByteArrayOutputStream out, String ...path) throws Exception {
		OutputStreamWriter outSW = new OutputStreamWriter(out);
		String enconding = outSW.getEncoding();
		logger.debug(enconding);
		if (!enconding.equals("UTF8")) {
			String stringUTF8 = out.toString();
			ByteArrayOutputStream outUTF8 = new ByteArrayOutputStream();
			outSW = new OutputStreamWriter(outUTF8, "UTF-8");
			outSW.write(stringUTF8);
			outSW.close();
			logger.debug(outUTF8.toString());
			out = outUTF8;
		}
		
		XMLReader xmlReader = createXMLReader();
		final ValidatorHandler validatorHandler = createValidatorHandler(path);
		xmlReader.setContentHandler(validatorHandler);

		DefaultHandler handler = new DefaultHandler() {
			public void characters(char[] ch, int start, int length) throws SAXException {
				TypeInfoProvider infoProvider = null;
				synchronized (validatorHandler) {
					infoProvider = validatorHandler.getTypeInfoProvider();
				}
				if (infoProvider == null) {
					try {
						throw new Exception("Can't obtain TypeInfo object.");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				try {
					infoProvider.getElementTypeInfo();
					throw new Exception("IllegalStateException was not thrown.");
				} catch (IllegalStateException e) {
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		
		validatorHandler.setContentHandler(handler);
		parse(xmlReader, out);
		
		return true;
	}
}

