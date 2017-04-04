package com.interfactura.firmalocal.cancelacion.ws;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceFeature;

import com.interfactura.firmalocal.cancelacion.ws.webServiceTimbreSoap;


public class webServiceTimbre 
	 extends Service
	 {
	     private final static URL WEBSERVICE1_WSDL_LOCATION;
	     private final static Logger logger = Logger.getLogger(com.interfactura.firmalocal.cancelacion.ws.webServiceTimbre .class.getName());

	     static {
	         URL url = null;
	         try {
	             URL baseUrl;
	             baseUrl = com.interfactura.firmalocal.cancelacion.ws.webServiceTimbre .class.getResource(".");
	             url = new URL(baseUrl, "https://santandes.interfactura.com/TimbreServiciosSantander/TimbreServicios.asmx?wsdl");
	             
	         } catch (MalformedURLException e) {
	             logger.warning("Failed to create URL for the wsdl Location: 'https://santandes.interfactura.com/TimbreServiciosSantander/TimbreServicios.asmx?wsdl', retrying as a local file");
	             logger.warning(e.getMessage());
	         }
	         WEBSERVICE1_WSDL_LOCATION = url;
	     }

	     public webServiceTimbre(URL wsdlLocation, QName serviceName) {
	         super(wsdlLocation, serviceName);
	     }

	     public webServiceTimbre() {
	         super(WEBSERVICE1_WSDL_LOCATION, new QName("http://tempuri.org/", "WebService1"));
	     }

	     /**
	      * 
	      * @return
	      *     returns WebService1Soap
	      */
	     @WebEndpoint(name = "WebService1Soap")
	     public webServiceTimbreSoap getWebService1Soap() {
	         return super.getPort(new QName("http://tempuri.org/", "WebService1Soap"), webServiceTimbreSoap.class);
	     }

	     /**
	      * 
	      * @param features
	      *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
	      * @return
	      *     returns WebService1Soap
	      */
	     @WebEndpoint(name = "WebService1Soap")
	     public webServiceTimbreSoap getWebService1Soap(WebServiceFeature... features) {
	         return super.getPort(new QName("http://tempuri.org/", "WebService1Soap"), webServiceTimbreSoap.class, features);
	     }
}
