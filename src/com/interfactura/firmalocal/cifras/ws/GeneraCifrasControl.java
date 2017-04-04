
package com.interfactura.firmalocal.cifras.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="CFDv3" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Solotimbre" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "fecha",
    "tipoFormato",
    "nombreInterfaz",
    "esReproceso"
})
@XmlRootElement(name = "GeneraCifrasControl")
public class GeneraCifrasControl {

    @XmlElement(name = "fecha")
    protected String fecha;        
    @XmlElement(name = "tipoFormato")
    protected int tipoFormato;
    @XmlElement(name = "nombreInterfaz")
    protected String nombreInterfaz;
    @XmlElement(name = "esReproceso")
    protected int esReproceso;
    /**
     * Gets the value of the fecha property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFecha() {
        return fecha;
    }

    /**
     * Sets the value of the fecha property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFecha(String value) {
        this.fecha = value;
    }
    /**
     * Gets the value of the tipoFormato property.
     * 
     */
    public int getTipoFormato() {
        return tipoFormato;
    }

    /**
     * Sets the value of the tipoFormato property.
     * 
     */
    public void setTipoFormato(int value) {
        this.tipoFormato = value;
    }

    /**
     * Gets the value of the intentoConexion property.
     * 
     */

	public String getNombreInterfaz() {
		return nombreInterfaz;
	}

    /**
     * Sets the value of the intentoConexion property.
     * 
     */
	public void setNombreInterfaz(String nombreInterfaz) {
		this.nombreInterfaz = nombreInterfaz;
	}
	/**
     * Gets the value of the esReproceso property.
     * 
     */
    public int getEsReproceso() {
        return esReproceso;
    }

    /**
     * Sets the value of the esReproceso property.
     * 
     */
    public void setEsReproceso(int value) {
        this.esReproceso = value;
    }

}
