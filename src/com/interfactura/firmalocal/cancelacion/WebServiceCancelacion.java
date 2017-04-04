package com.interfactura.firmalocal.cancelacion;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.interfactura.firmalocal.cancelacion.ws.webServiceConnectionException;
import com.interfactura.firmalocal.cancelacion.ws.webServiceTimbre;
import com.interfactura.firmalocal.cancelacion.ws.webServiceTimbreSoap;
import com.interfactura.firmalocal.xml.Properties;

import javax.xml.namespace.QName;

@Component
public class WebServiceCancelacion {
    private final static Logger logger = Logger.getLogger(com.interfactura.firmalocal.cancelacion.WebServiceCancelacion.class.getName());
    @Autowired(required = true)
    private Properties properties;

    webServiceTimbre service = null;

    /*
    public void generaTimbre(File file) throws IOException, UnsupportedEncodingException, webServiceConnectionException {
        try {
            URL url = null;
            try {
                URL baseUrl;
                baseUrl = com.interfactura.firmalocal.cancelacion.ws.webServiceTimbre.class.getResource(".");
                url = new URL(baseUrl, properties.getUrlWebServiceCancelacion());

            } catch (MalformedURLException e) {
                logger.warning("Failed to create URL for the wsdl Location: " + properties.getUrlWebServiceCancelacion() +", retrying as a local file");
                logger.warning(e.getMessage());
                throw new webServiceConnectionException("Problema de formado de url del web service de timbrado");
            }
            service = new webServiceTimbre(url, new QName("http://tempuri.org/", "WebService1"));


            //System.setProperty("javax.net.ssl.keyStoreType", "PKCS12");
            //System.setProperty("javax.net.ssl.keyStore", properties.getCertificadoTimbrado());
            //System.setProperty("javax.net.ssl.keyStorePassword", properties.getPassCertificado());

           
            webServiceTimbreSoap servicePort = service.getWebService1Soap();

            StringBuilder sb = null;


            InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            sb = new StringBuilder();

            while (br.ready()) {
                sb.append(br.readLine());
            }


            System.out.println("*********Conectandose con web service");
            System.out.println("XML a timbrar:" + sb.toString());
         
            String xml = servicePort.generaTimbre(sb.toString(), false);

            file.delete();

            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            //byte buf[] = xml.getBytes("ISO-8859-1");
            byte buf[] = xml.getBytes();
            bs.write(buf);
            //OutputStreamWriter out = new OutputStreamWriter(bs, "UTF-8");
            OutputStreamWriter out = new OutputStreamWriter(bs);
            String encoding = out.getEncoding();


            if(!encoding.equals("UTF-8")) {
                String stringUTF8 = bs.toString();
                ByteArrayOutputStream outUTF8 = new ByteArrayOutputStream();
                out = new OutputStreamWriter(outUTF8, "UTF-8");
                out.write(stringUTF8);
                out.close();
                bs = outUTF8;

            }

            OutputStream os = new FileOutputStream(file.getPath());
            bs.writeTo(os);
            os.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            throw new webServiceConnectionException("Problema de conexion con web service de timbrado");
        }

    }
     */
    public String cancelaTimbre(String xml) {

        URL url = null;
        try {
            URL baseUrl;
            baseUrl = com.interfactura.firmalocal.cancelacion.ws.webServiceTimbre.class.getResource(".");
            url = new URL(baseUrl, properties.getUrlWebServiceCancelacion());

        } catch (MalformedURLException e) {
            System.out.println("Failed to create URL for the wsdl Location: " + properties.getUrlWebServiceCancelacion() +", retrying as a local file");
            System.out.println(e.getMessage());
        }
        service = new webServiceTimbre(url, new QName("http://tempuri.org/", "WebService1"));
      
        /*System.setProperty("javax.net.ssl.keyStoreType", "PKCS12");
      
        System.setProperty("javax.net.ssl.keyStore", properties.getCertificadoCancelacion());
        System.setProperty("javax.net.ssl.keyStorePassword", properties.getCertificadoCancelacionPass());
        */
        webServiceTimbreSoap servicePort = service.getWebService1Soap();

        System.out.println("*********Conect�ndose con web service");

        String respuesta = servicePort.cancelaTimbre(xml);

        return respuesta;
    }


    public WebServiceCancelacion() {

    }
}
