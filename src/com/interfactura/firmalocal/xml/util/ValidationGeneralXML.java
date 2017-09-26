package com.interfactura.firmalocal.xml.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

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

import com.interfactura.firmalocal.xml.validation.ValidationXML;

@Component
public class ValidationGeneralXML {

	private static Map<String, Schema> schemasLst = new HashMap<String, Schema>();
	private Logger logger = Logger.getLogger(ValidationXML.class);
	
	/**
	 * 
	 * @return
	 * @throws SAXException
	 */
	private ValidatorHandler createValidatorHandler(String...path) throws SAXException {
		SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");//(XMLConstants.XML_NS_URI)
		StreamSource[] schemas=new StreamSource[path.length];
		StringBuffer key = new StringBuffer();
		for(int i=0; i<path.length; i++){
			schemas[i] = new StreamSource(new File(path[i]));
			key.append(path[i]);
			logger.info("Esquemas: "+path[i]);
		}
		Schema schema = schemasLst.get(key.toString());
		if(schema == null){
			logger.info("Creando nuevo schema validation. Cargando todos los XSD ...");
			schema = schemaFactory.newSchema(schemas);
			schemasLst.put(key.toString(), schema);
		}
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
