package com.interfactura.firmalocal.xml.timbradounicodivisas;

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
    "cfDv3",
    "nombreInterfaz",
    "numeroProceso",
    "intentoConexion",
    "tipoFormato",
    "periodo",
    "nombreAplicativo"
})
@XmlRootElement(name = "GeneraTimbreUnicoDivisas")
public class GeneraTimbreUnicoDivisas {

    @XmlElement(name = "CFDv3")
    protected String cfDv3;
    @XmlElement(name = "nombreInterfaz")
    protected String nombreInterfaz;
    @XmlElement(name = "numeroProceso")
    protected int numeroProceso;
    @XmlElement(name = "intentoConexion")
    protected int intentoConexion;
    @XmlElement(name = "tipoFormato")
    protected int tipoFormato;
    @XmlElement(name = "periodo")
    protected String periodo;
    @XmlElement(name = "nombreAplicativo")
    protected String nombreAplicativo;
    /**
     * Gets the value of the cfDv3 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCFDv3() {
        return cfDv3;
    }

    /**
     * Sets the value of the cfDv3 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCFDv3(String value) {
        this.cfDv3 = value;
    }

    /**
     * Gets the value of the nombreInterfaz property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNombreInterfaz() {
        return nombreInterfaz;
    }

    /**
     * Sets the value of the nombreInterfaz property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNombreInterfaz(String value) {
        this.nombreInterfaz = value;
    }

    /**
     * Gets the value of the numeroProceso property.
     * 
     */
    public int getNumeroProceso() {
        return numeroProceso;
    }

    /**
     * Sets the value of the numeroProceso property.
     * 
     */
    public void setNumeroProceso(int value) {
        this.numeroProceso = value;
    }

    /**
     * Gets the value of the intentoConexion property.
     * 
     */
	public int getIntentoConexion() {
		return intentoConexion;
	}
	/**
     * Sets the value of the intentoConexion property.
     * 
     */
	public void setIntentoConexion(int intentoConexion) {
		this.intentoConexion = intentoConexion;
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
	public void setTipoFormato(int tipoFormato) {
		this.tipoFormato = tipoFormato;
	}
	/**
     * Gets the value of the periodo property.
     * 
     */
	public String getPeriodo() {
		return periodo;
	}
	/**
     * Sets the value of the periodo property.
     * 
     */
	public void setPeriodo(String periodo) {
		this.periodo = periodo;
	}
	/**
     * Gets the value of the nombreAplicativo property.
     * 
     */
	public String getNombreAplicativo() {
		return nombreAplicativo;
	}
	/**
     * Sets the value of the nombreAplicativo property.
     * 
     */
	public void setNombreAplicativo(String nombreAplicativo) {
		this.nombreAplicativo = nombreAplicativo;
	}
}
