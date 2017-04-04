package com.interfactura.firmalocal.cancelacion.ws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import com.interfactura.firmalocal.cancelacion.ws.ObjectFactory;

/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.6 in JDK 6
 * Generated source version: 2.1
 * 
 */

@WebService(name = "WebService1Soap", targetNamespace = "http://tempuri.org/")
@XmlSeeAlso({
    ObjectFactory.class
})
public interface webServiceTimbreSoap {

	 /**
     * 
     * @param soloTimbrado
     * @param fechaTimbrado
     * @param uuid
     * @return
     *     returns java.lang.String
     */
	/*
    @WebMethod(operationName = "ConsultaTimbre", action = "http://tempuri.org/ConsultaTimbre")
    @WebResult(name = "ConsultaTimbreResult", targetNamespace = "http://tempuri.org/")
    @RequestWrapper(localName = "ConsultaTimbre", targetNamespace = "http://tempuri.org/", className = "org.tempuri.ConsultaTimbre")
    @ResponseWrapper(localName = "ConsultaTimbreResponse", targetNamespace = "http://tempuri.org/", className = "org.tempuri.ConsultaTimbreResponse")
    public String consultaTimbre(
        @WebParam(name = "UUID", targetNamespace = "http://tempuri.org/")
        String uuid,
        @WebParam(name = "FechaTimbrado", targetNamespace = "http://tempuri.org/")
        XMLGregorianCalendar fechaTimbrado,
        @WebParam(name = "SoloTimbrado", targetNamespace = "http://tempuri.org/")
        boolean soloTimbrado);
*/
    /**
     * 
     * @param cfDv3
     * @param solotimbre
     * @return
     *     returns java.lang.String
     */
	/*
    @WebMethod(operationName = "GeneraTimbre", action = "http://tempuri.org/GeneraTimbre")
    @WebResult(name = "GeneraTimbreResult", targetNamespace = "http://tempuri.org/")
    @RequestWrapper(localName = "GeneraTimbre", targetNamespace = "http://tempuri.org/", className = "org.tempuri.GeneraTimbre")
    @ResponseWrapper(localName = "GeneraTimbreResponse", targetNamespace = "http://tempuri.org/", className = "org.tempuri.GeneraTimbreResponse")
    public String generaTimbre(
        @WebParam(name = "CFDv3", targetNamespace = "http://tempuri.org/")
        String cfDv3,
        @WebParam(name = "Solotimbre", targetNamespace = "http://tempuri.org/")
        boolean solotimbre);
*/
    /**
     * 
     * @param cancelaXML
     * @return
     *     returns java.lang.String
     */
    @WebMethod(operationName = "CancelaTimbre", action = "http://tempuri.org/CancelaTimbre")
    @WebResult(name = "CancelaTimbreResult", targetNamespace = "http://tempuri.org/")
    @RequestWrapper(localName = "CancelaTimbre", targetNamespace = "http://tempuri.org/", className = "org.tempuri.CancelaTimbre")
    @ResponseWrapper(localName = "CancelaTimbreResponse", targetNamespace = "http://tempuri.org/", className = "org.tempuri.CancelaTimbreResponse")
    public String cancelaTimbre(
        @WebParam(name = "CancelaXML", targetNamespace = "http://tempuri.org/")
        String cancelaXML);
}
