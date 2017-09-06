/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.interfactura.firmalocal.datamodel;

import java.util.HashMap;

/**
 *
 * @author Maximino Llovera
 */
public class CfdiAddendaSantanderV1 {

    private HashMap<String, String> campoAdicional;
    private CfdiAddendaInformacionEmision informacionEmision;
    private CfdiAddendaInformacionPago informacionPago;
    private CfdiAddendaInmuebles inmuebles;

    public HashMap<String, String> getCampoAdicional() {
        return campoAdicional;
    }

    public void setCampoAdicional(HashMap<String, String> campoAdicional) {
        this.campoAdicional = campoAdicional;
    }

    public CfdiAddendaInformacionEmision getInformacionEmision() {
        return informacionEmision;
    }

    public void setInformacionEmision(CfdiAddendaInformacionEmision informacionEmision) {
        this.informacionEmision = informacionEmision;
    }

    public CfdiAddendaInformacionPago getInformacionPago() {
        return informacionPago;
    }

    public void setInformacionPago(CfdiAddendaInformacionPago informacionPago) {
        this.informacionPago = informacionPago;
    }

    public CfdiAddendaInmuebles getInmuebles() {
        return inmuebles;
    }

    public void setInmuebles(CfdiAddendaInmuebles inmuebles) {
        this.inmuebles = inmuebles;
    }

}
