/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.interfactura.firmalocal.datamodel;

import java.math.BigDecimal;

/**
 *
 * @author Maximino Llovera
 */
public class CfdiConcepto {

    private BigDecimal cantidad;
    private String claveProdServ;
    private String claveUnidad;
    private String descripcion;
    private BigDecimal importe;
    private String unidad;
    private BigDecimal valorUnitario;
    private CfdiConceptoImpuesto impuestos;

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public String getClaveProdServ() {
        return claveProdServ;
    }

    public void setClaveProdServ(String claveProdServ) {
        this.claveProdServ = claveProdServ;
    }

    public String getClaveUnidad() {
        return claveUnidad;
    }

    public void setClaveUnidad(String claveUnidad) {
        this.claveUnidad = claveUnidad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(BigDecimal valorUnitario) {
        this.valorUnitario = valorUnitario;
    }

    public CfdiConceptoImpuesto getImpuestos() {
        return impuestos;
    }

    public void setImpuestos(CfdiConceptoImpuesto impuestos) {
        this.impuestos = impuestos;
    }

}
