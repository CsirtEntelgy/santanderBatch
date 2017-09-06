/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.interfactura.firmalocal.datamodel;

import java.util.List;

/**
 *
 * @author Maximino Llovera
 */
public class CfdiAddendaInformacionEmision {

    private String codigoCliente;
    private String contrato;
    private String periodo;
    private String centroCostos;
    private String folioInterno;
    private String claveSantander;
    List<CfdiAddendaInformacionFactoraje> informacionFactoraje;

    public String getCodigoCliente() {
        return codigoCliente;
    }

    public void setCodigoCliente(String codigoCliente) {
        this.codigoCliente = codigoCliente;
    }

    public String getContrato() {
        return contrato;
    }

    public void setContrato(String contrato) {
        this.contrato = contrato;
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public String getCentroCostos() {
        return centroCostos;
    }

    public void setCentroCostos(String centroCostos) {
        this.centroCostos = centroCostos;
    }

    public String getFolioInterno() {
        return folioInterno;
    }

    public void setFolioInterno(String folioInterno) {
        this.folioInterno = folioInterno;
    }

    public String getClaveSantander() {
        return claveSantander;
    }

    public void setClaveSantander(String claveSantander) {
        this.claveSantander = claveSantander;
    }

    public List<CfdiAddendaInformacionFactoraje> getInformacionFactoraje() {
        return informacionFactoraje;
    }

    public void setInformacionFactoraje(List<CfdiAddendaInformacionFactoraje> informacionFactoraje) {
        this.informacionFactoraje = informacionFactoraje;
    }

}
