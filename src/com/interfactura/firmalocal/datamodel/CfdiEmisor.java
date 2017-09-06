/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.interfactura.firmalocal.datamodel;

/**
 *
 * @author Maximino Llovera
 */
public class CfdiEmisor {

    private String nombre;
    private String regimenFiscal;
    private String Rfc;
    private CfdiDomicilio domicilio;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRegimenFiscal() {
        return regimenFiscal;
    }

    public void setRegimenFiscal(String regimenFiscal) {
        this.regimenFiscal = regimenFiscal;
    }

    public String getRfc() {
        return Rfc;
    }

    public void setRfc(String Rfc) {
        this.Rfc = Rfc;
    }

    public CfdiDomicilio getDomicilio() {
        return domicilio;
    }

    public void setDomicilio(CfdiDomicilio domicilio) {
        this.domicilio = domicilio;
    }
}
