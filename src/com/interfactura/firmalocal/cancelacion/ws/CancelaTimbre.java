
package com.interfactura.firmalocal.cancelacion.ws;

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
 *         &lt;element name="CancelaXML" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "cancelaXML"
})
@XmlRootElement(name = "CancelaTimbre")
public class CancelaTimbre {

    @XmlElement(name = "CancelaXML")
    protected String cancelaXML;

    /**
     * Gets the value of the cancelaXML property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCancelaXML() {
        return cancelaXML;
    }

    /**
     * Sets the value of the cancelaXML property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCancelaXML(String value) {
        this.cancelaXML = value;
    }

}
