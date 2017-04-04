
package com.interfactura.firmalocal.xml.timbrado;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


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
 *         &lt;element name="UUID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="FechaTimbrado" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="SoloTimbrado" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
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
    "uuid",
    "fechaTimbrado",
    "soloTimbrado"
})
@XmlRootElement(name = "ConsultaTimbre")
public class ConsultaTimbre {

    @XmlElement(name = "UUID")
    protected String uuid;
    @XmlElement(name = "FechaTimbrado", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar fechaTimbrado;
    @XmlElement(name = "SoloTimbrado")
    protected boolean soloTimbrado;

    /**
     * Gets the value of the uuid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUUID() {
        return uuid;
    }

    /**
     * Sets the value of the uuid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUUID(String value) {
        this.uuid = value;
    }

    /**
     * Gets the value of the fechaTimbrado property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getFechaTimbrado() {
        return fechaTimbrado;
    }

    /**
     * Sets the value of the fechaTimbrado property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setFechaTimbrado(XMLGregorianCalendar value) {
        this.fechaTimbrado = value;
    }

    /**
     * Gets the value of the soloTimbrado property.
     * 
     */
    public boolean isSoloTimbrado() {
        return soloTimbrado;
    }

    /**
     * Sets the value of the soloTimbrado property.
     * 
     */
    public void setSoloTimbrado(boolean value) {
        this.soloTimbrado = value;
    }

}
