package com.interfactura.firmalocal.cifras;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

import org.springframework.stereotype.Component;

import com.interfactura.firmalocal.cifras.ws.WebServiceCifras;
import com.interfactura.firmalocal.cifras.ws.WebServiceCifrasSoap;


@Component
public class WebServiceCifrasCliente {
	private final static Logger logger = Logger.getLogger(com.interfactura.firmalocal.xml.WebServiceCliente.class.getName());
    //@Autowired(required = true)
    // private Properties properties;

    WebServiceCifras service = null;
    WebServiceCifrasSoap servicePort = null;
    
    public WebServiceCifrasCliente() {

    }
    
    public String generaCifrasControl(String urlWebService, String fecha, int tipoFormato, String nombreInterfaz, int esReproceso) throws IOException, UnsupportedEncodingException {
    	String xml = "";
        try {
            URL url = null;
            try {
                URL baseUrl;
                baseUrl = com.interfactura.firmalocal.cifras.ws.WebServiceCifras.class.getResource(".");
                url = new URL(baseUrl, urlWebService);

            } catch (MalformedURLException e) {
                logger.warning("Failed to create URL for the wsdl Location: " + urlWebService +", retrying as a local file");
                logger.warning(e.getMessage());
            }
            
            System.out.println("*********Conectándose con web service");
            
            //service = new WebServiceCifras(url, new QName("http://tempuri.org/", "WebServiceCifras"));
            
            try{
            	long t1 = System.currentTimeMillis();
            	service = new WebServiceCifras(url, new QName("http://tempuri.org/", "Service1"));
            	long t2 = t1- System.currentTimeMillis();
				System.out.println("TIME: ConexionWebService:" + t2 + " ms");
            }catch(Exception e){
            	e.printStackTrace();
            	System.out.println(e.getMessage());
	        	//reconexion(url, new QName("http://tempuri.org/", "Service1"), 1, Integer.parseInt(properties.getIntentosconexion()));
            	try{
            		System.out.println("intento 1");
            		service = new WebServiceCifras(url, new QName("http://tempuri.org/", "Service1"));
            	}catch(Exception e1){
            		try{
            			System.out.println("intento 2");
                		service = new WebServiceCifras(url, new QName("http://tempuri.org/", "Service1"));
                	}catch(Exception e2){
                		try{
                			System.out.println("intento 3");
                    		service = new WebServiceCifras(url, new QName("http://tempuri.org/", "Service1"));
                    	}catch(Exception e3){
                    		try{
                    			System.out.println("intento 4");
                        		service = new WebServiceCifras(url, new QName("http://tempuri.org/", "Service1"));
                        	}catch(Exception e4){
                        		try{
                        			System.out.println("intento 5");
                            		service = new WebServiceCifras(url, new QName("http://tempuri.org/", "Service1"));
                            	}catch(Exception e5){
                            		try{
                            			System.out.println("intento 6");
                                		service = new WebServiceCifras(url, new QName("http://tempuri.org/", "Service1"));
                                	}catch(Exception e6){
                                		try{
                                			System.out.println("intento 7");
                                    		service = new WebServiceCifras(url, new QName("http://tempuri.org/", "Service1"));
                                    	}catch(Exception e7){
                                    		try{
                                    			System.out.println("intento 8");
                                        		service = new WebServiceCifras(url, new QName("http://tempuri.org/", "Service1"));
                                        	}catch(Exception e8){
                                        		try{
                                        			System.out.println("intento 9");
                                            		service = new WebServiceCifras(url, new QName("http://tempuri.org/", "Service1"));
                                            	}catch(Exception e9){
                                            		try{
                                            			System.out.println("intento 10");
                                                		service = new WebServiceCifras(url, new QName("http://tempuri.org/", "Service1"));
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
            servicePort = service.getWebServiceCifrasSoap();
                
            try{
            	long t1 = System.currentTimeMillis();
            	xml = servicePort.generaCifrasControl(fecha, tipoFormato, nombreInterfaz, esReproceso);
            	long t2 = t1- System.currentTimeMillis();
				System.out.println("TIME: LlamadaGeneraTimbre:" + t2 + " ms");
            }catch(Exception e){
            	e.printStackTrace();
            	System.out.println(e.getMessage());
            	
            	//xml = reconexion(strXml, regresaTimbre, url, new QName("http://tempuri.org/", "Service1"), 1, Integer.parseInt(properties.getIntentosconexion()));
            	try{
            		System.out.println("intento 1");
            		xml = servicePort.generaCifrasControl(fecha, tipoFormato, nombreInterfaz, esReproceso);
            	}catch(Exception e1){
            		try{
            			System.out.println("intento 2");
            			xml = servicePort.generaCifrasControl(fecha, tipoFormato, nombreInterfaz, esReproceso);
                	}catch(Exception e2){
                		try{
                			System.out.println("intento 3");
                			xml = servicePort.generaCifrasControl(fecha, tipoFormato, nombreInterfaz, esReproceso);
                    	}catch(Exception e3){
                    		try{
                    			System.out.println("intento 4");
                    			xml = servicePort.generaCifrasControl(fecha, tipoFormato, nombreInterfaz, esReproceso);
                        	}catch(Exception e4){
                        		try{
                        			System.out.println("intento 5");
                        			xml = servicePort.generaCifrasControl(fecha, tipoFormato, nombreInterfaz, esReproceso);
                            	}catch(Exception e5){
                            		try{
                            			System.out.println("intento 6");
                            			xml = servicePort.generaCifrasControl(fecha, tipoFormato, nombreInterfaz, esReproceso);
                                	}catch(Exception e6){
                                		try{
                                			System.out.println("intento 7");
                                			xml = servicePort.generaCifrasControl(fecha, tipoFormato, nombreInterfaz, esReproceso);
                                    	}catch(Exception e7){
                                    		try{
                                    			System.out.println("intento 8");
                                    			xml = servicePort.generaCifrasControl(fecha, tipoFormato, nombreInterfaz, esReproceso);
                                        	}catch(Exception e8){
                                        		try{
                                        			System.out.println("intento 9");
                                        			xml = servicePort.generaCifrasControl(fecha, tipoFormato, nombreInterfaz, esReproceso);
                                            	}catch(Exception e9){
                                            		try{
                                            			System.out.println("intento 10");
                                            			xml = servicePort.generaCifrasControl(fecha, tipoFormato, nombreInterfaz, esReproceso);
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
			service = new WebServiceCifras(_url, _qName);
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
    
    public String reconexion(String _fecha, URL _url, QName _qName, int nConexion, int intentosConexion, int _tipoFormato, String _nombreInterfaz, int _esReproceso){
    	String timbrados = "";
		System.out.println("RECONEXION2");
		
		try {
			long t1 = System.currentTimeMillis();
			service = new WebServiceCifras(_url, _qName);
			timbrados = servicePort.generaCifrasControl(_fecha, _tipoFormato, _nombreInterfaz, _esReproceso);
			long t2 = t1- System.currentTimeMillis();
			System.out.println("TIME: Reconexion2:" + t2 + " ms - intento: " + nConexion);
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block			
			if(nConexion <= intentosConexion){
				try{
					System.out.println("Intento de conexion a webService... " + nConexion);
					Thread.sleep(500 * nConexion);
					reconexion(_fecha, _url, _qName, nConexion + 1, intentosConexion, _tipoFormato, _nombreInterfaz, _esReproceso);	
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
}
