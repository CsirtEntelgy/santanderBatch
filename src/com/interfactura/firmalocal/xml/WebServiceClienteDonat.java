package com.interfactura.firmalocal.xml;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.springframework.stereotype.Component;

import com.interfactura.firmalocal.xml.timbradonat.WebService1;
import com.interfactura.firmalocal.xml.timbradonat.WebService1Soap;

@Component
public class WebServiceClienteDonat {
	private final static Logger logger = Logger.getLogger(com.interfactura.firmalocal.xml.WebServiceClienteDonat.class.getName());
    //@Autowired(required = true)
   // private Properties properties;

    WebService1 service = null;
    WebService1Soap servicePort = null;
    
    public String generaTimbreDonat(String strXml, boolean regresaTimbre, String urlWebService, Properties properties, String nombreInterfaz, int numeroProceso, int tipoFormato, String periodo, String nombreAplicativo) throws IOException, UnsupportedEncodingException {
    	String xml = "";
        try {
            URL url = null;
            try {
                URL baseUrl;
                baseUrl = com.interfactura.firmalocal.xml.timbrado.WebService1.class.getResource(".");
                url = new URL(baseUrl, urlWebService);

            } catch (MalformedURLException e) {
                logger.warning("Failed to create URL for the wsdl Location: " + urlWebService +", retrying as a local file");
                logger.warning(e.getMessage());
            }
            
            System.out.println("*********Conectándose con web service");
            
            //service = new WebService1(url, new QName("http://tempuri.org/", "WebService1"));
            
            try{
            	long t1 = System.currentTimeMillis();
            	service = new WebService1(url, new QName("http://tempuri.org/", "Service1"));
            	long t2 = t1- System.currentTimeMillis();
				System.out.println("TIME: ConexionWebService:" + t2 + " ms");
            }catch(Exception e){
            	e.printStackTrace();
            	System.out.println(e.getMessage());
	        	//reconexion(url, new QName("http://tempuri.org/", "Service1"), 1, Integer.parseInt(properties.getIntentosconexion()));
            	try{
            		System.out.println("intento 1");
            		service = new WebService1(url, new QName("http://tempuri.org/", "Service1"));
            	}catch(Exception e1){
            		try{
            			System.out.println("intento 2");
                		service = new WebService1(url, new QName("http://tempuri.org/", "Service1"));
                	}catch(Exception e2){
                		try{
                			System.out.println("intento 3");
                    		service = new WebService1(url, new QName("http://tempuri.org/", "Service1"));
                    	}catch(Exception e3){
                    		try{
                    			System.out.println("intento 4");
                        		service = new WebService1(url, new QName("http://tempuri.org/", "Service1"));
                        	}catch(Exception e4){
                        		try{
                        			System.out.println("intento 5");
                            		service = new WebService1(url, new QName("http://tempuri.org/", "Service1"));
                            	}catch(Exception e5){
                            		try{
                            			System.out.println("intento 6");
                                		service = new WebService1(url, new QName("http://tempuri.org/", "Service1"));
                                	}catch(Exception e6){
                                		try{
                                			System.out.println("intento 7");
                                    		service = new WebService1(url, new QName("http://tempuri.org/", "Service1"));
                                    	}catch(Exception e7){
                                    		try{
                                    			System.out.println("intento 8");
                                        		service = new WebService1(url, new QName("http://tempuri.org/", "Service1"));
                                        	}catch(Exception e8){
                                        		try{
                                        			System.out.println("intento 9");
                                            		service = new WebService1(url, new QName("http://tempuri.org/", "Service1"));
                                            	}catch(Exception e9){
                                            		try{
                                            			System.out.println("intento 10");
                                                		service = new WebService1(url, new QName("http://tempuri.org/", "Service1"));
                                                	}catch(Exception e10){
                                                		e.printStackTrace();
                                                    	System.out.println(e.getMessage());
                                        	        	
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

            /*System.setProperty("javax.net.ssl.keyStoreType", "PKCS12");
            System.setProperty("javax.net.ssl.keyStore", properties.getCertificadoSantander());
            System.setProperty("javax.net.ssl.keyStorePassword", properties.getCertificadoPass());
            */
            servicePort = service.getWebService1Soap();
                
            try{
            	long t1 = System.currentTimeMillis();
            	xml = servicePort.generaTimbre(strXml, nombreInterfaz, numeroProceso, 0, tipoFormato, periodo, nombreAplicativo);
            	long t2 = t1- System.currentTimeMillis();
				System.out.println("TIME: LlamadaGeneraTimbre:" + t2 + " ms");
            }catch(Exception e){
            	e.printStackTrace();
            	System.out.println(e.getMessage());
            	
            	//xml = reconexion(strXml, regresaTimbre, url, new QName("http://tempuri.org/", "Service1"), 1, Integer.parseInt(properties.getIntentosconexion()));
            	try{
            		System.out.println("intento 1");
            		xml = servicePort.generaTimbre(strXml, nombreInterfaz, numeroProceso, 1, tipoFormato, periodo, nombreAplicativo);
            	}catch(Exception e1){
            		try{
            			System.out.println("intento 2");
            			xml = servicePort.generaTimbre(strXml, nombreInterfaz, numeroProceso, 2, tipoFormato, periodo, nombreAplicativo);
                	}catch(Exception e2){
                		try{
                			System.out.println("intento 3");
                			xml = servicePort.generaTimbre(strXml, nombreInterfaz, numeroProceso, 3, tipoFormato, periodo, nombreAplicativo);
                    	}catch(Exception e3){
                    		try{
                    			System.out.println("intento 4");
                    			xml = servicePort.generaTimbre(strXml, nombreInterfaz, numeroProceso, 4, tipoFormato, periodo, nombreAplicativo);
                        	}catch(Exception e4){
                        		try{
                        			System.out.println("intento 5");
                        			xml = servicePort.generaTimbre(strXml, nombreInterfaz, numeroProceso, 5, tipoFormato, periodo, nombreAplicativo);
                            	}catch(Exception e5){
                            		try{
                            			System.out.println("intento 6");
                            			xml = servicePort.generaTimbre(strXml, nombreInterfaz, numeroProceso, 6, tipoFormato, periodo, nombreAplicativo);
                                	}catch(Exception e6){
                                		try{
                                			System.out.println("intento 7");
                                			xml = servicePort.generaTimbre(strXml, nombreInterfaz, numeroProceso, 7, tipoFormato, periodo, nombreAplicativo);
                                    	}catch(Exception e7){
                                    		try{
                                    			System.out.println("intento 8");
                                    			xml = servicePort.generaTimbre(strXml, nombreInterfaz, numeroProceso, 8, tipoFormato, periodo, nombreAplicativo);
                                        	}catch(Exception e8){
                                        		try{
                                        			System.out.println("intento 9");
                                        			xml = servicePort.generaTimbre(strXml, nombreInterfaz, numeroProceso, 9, tipoFormato, periodo, nombreAplicativo);
                                            	}catch(Exception e9){
                                            		try{
                                            			System.out.println("intento 10");
                                            			xml = servicePort.generaTimbre(strXml, nombreInterfaz, numeroProceso, 10, tipoFormato, periodo, nombreAplicativo);
                                                	}catch(Exception e10){
                                                		e.printStackTrace();
                                                    	System.out.println(e.getMessage());
                                                    	
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
                        
        } catch (Exception e) {       	
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return xml;
    }

    public void reconexion(URL _url, QName _qName, int nConexion, int intentosConexion){		
		System.out.println("RECONEXION");
		
		try {
			long t1 = System.currentTimeMillis();
			service = new WebService1(_url, _qName);
			long t2 = t1- System.currentTimeMillis();
			System.out.println("TIME: Reconexion:" + t2 + " ms - intento: " + nConexion);
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block			
			if(nConexion <= intentosConexion){
				try{
					System.out.println("Intento de conexion a webService... " + nConexion);
					Thread.sleep(500 * nConexion);
					reconexion(_url, _qName, nConexion + 1, intentosConexion);
				}catch(Exception e2){
					e2.printStackTrace();
					System.out.println(e2.getMessage());	
				}				
			}else{
				e.printStackTrace();
				System.out.println(e.getMessage());
			}
		}		
		
	}
    
    public String reconexion(String _strXml, boolean _fRegresaTimbre, URL _url, QName _qName, int nConexion, int intentosConexion, String _nombreInterfaz, int _numeroProceso, int _tipoFormato, String _periodo, String _nombreAplicativo){
    	String timbrados = "";
		System.out.println("RECONEXION2");
		
		try {
			long t1 = System.currentTimeMillis();
			service = new WebService1(_url, _qName);
			timbrados = servicePort.generaTimbre(_strXml, _nombreInterfaz, _numeroProceso, 0, _tipoFormato, _periodo, _nombreAplicativo);
			long t2 = t1- System.currentTimeMillis();
			System.out.println("TIME: Reconexion2:" + t2 + " ms - intento: " + nConexion);
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block			
			if(nConexion <= intentosConexion){
				try{
					System.out.println("Intento de conexion a webService... " + nConexion);
					Thread.sleep(500 * nConexion);
					reconexion(_strXml, _fRegresaTimbre, _url, _qName, nConexion + 1, intentosConexion, _nombreInterfaz, _numeroProceso, _tipoFormato, _periodo, _nombreAplicativo);	
				}catch(Exception e2){
					e2.printStackTrace();
					System.out.println(e2.getMessage());
				}				
			}else{
				e.printStackTrace();
				System.out.println(e.getMessage());
			}
		}		
		
		return timbrados;
	}
	    
    public String cancelaTimbre(String xml, String urlWebService, Properties properties) {

        URL url = null;
        try {
            URL baseUrl;
            baseUrl = com.interfactura.firmalocal.xml.timbrado.WebService1.class.getResource(".");
            url = new URL(baseUrl, urlWebService);

        } catch (MalformedURLException e) {
            System.out.println("Failed to create URL for the wsdl Location: " + urlWebService +", retrying as a local file");
            System.out.println(e.getMessage());
        }
        //service = new WebService1(url, new QName("http://tempuri.org/", "WebService1"));
        service = new WebService1(url, new QName("http://tempuri.org/", "Service1"));
        System.setProperty("javax.net.ssl.keyStoreType", "PKCS12");
        System.setProperty("javax.net.ssl.keyStore", properties.getCertificadoSantander());
        System.setProperty("javax.net.ssl.keyStorePassword", properties.getCertificadoPass());

        WebService1Soap servicePort = service.getWebService1Soap();

        System.out.println("*********Conectándose con web service");

        //String respuesta = servicePort.cancelaTimbre(xml);
        String respuesta = "";
        return respuesta;
    }


    public WebServiceClienteDonat() {

    }
}
