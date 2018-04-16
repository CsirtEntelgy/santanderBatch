package com.interfactura.firmalocal.xml.validation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.TypeInfoProvider;
import javax.xml.validation.ValidatorHandler;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

@Component
public class ValidationXML {
	private Logger logger = Logger.getLogger(ValidationXML.class);

	/**
	 * 
	 * @return
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	private XMLReader createXMLReader() throws SAXException, ParserConfigurationException {
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		parserFactory.setNamespaceAware(true);
		return parserFactory.newSAXParser().getXMLReader();
	}

	/**
	 * 
	 * @param xmlReader
	 * @param out
	 * @throws IOException
	 * @throws SAXException
	 */
	private void parse(XMLReader xmlReader, ByteArrayOutputStream out) 
		throws IOException, SAXException 
	{
		InputSource inSource = new InputSource(new ByteArrayInputStream(
				out.toByteArray()));
		xmlReader.parse(inSource);
	}

	/**
	 * Valida un XML
	 * @param out
	 * @param val
	 * @return
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public boolean valida(ByteArrayOutputStream out, ValidatorHandler val) 
		throws SAXException, ParserConfigurationException, IOException
	{

		//logger.debug("Validando XML");
		System.out.println("Validando XML");
		XMLReader xmlReader = createXMLReader();
		final ValidatorHandler validatorHandler = val;
		xmlReader.setContentHandler(validatorHandler);

		DefaultHandler handler = new DefaultHandler() 
		{
			public void characters(char[] ch, int start, int length)
					throws SAXException 
			{
				TypeInfoProvider infoProvider = null;
				synchronized (validatorHandler) 
				{	infoProvider = validatorHandler.getTypeInfoProvider();	}
				if (infoProvider == null) 
				{
					try 
					{	throw new Exception("Can't obtain TypeInfo object.");	} 
					catch (Exception e) 
					{	logger.error("",e);		}
				}

				try 
				{
					infoProvider.getElementTypeInfo();
					throw new Exception("IllegalStateException was not thrown.");
				} 
				catch (IllegalStateException e) 
				{	logger.error("",e);		} 
				catch (Exception e) 
				{	logger.error("",e);		}
			}
		};

		validatorHandler.setContentHandler(handler);
		parse(xmlReader, out);

		return true;
	}
}
