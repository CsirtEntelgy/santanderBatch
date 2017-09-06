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
public class CfdiAddendaInformacionFactoraje {

    private String deudorProveedor;
    private String tipoDocumento;
    private String numeroDocumento;
    private String fechaVencimiento;
    private String plazo;
    private String valorNominal;
    private String aforo;
    private String precioBase;
    private String tasaDescuento;
    private String precioFactoraje;
    private String importeDescuento;

    public String getDeudorProveedor() {
        return deudorProveedor;
    }

    public void setDeudorProveedor(String deudorProveedor) {
        this.deudorProveedor = deudorProveedor;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(String fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public String getPlazo() {
        return plazo;
    }

    public void setPlazo(String plazo) {
        this.plazo = plazo;
    }

    public String getValorNominal() {
        return valorNominal;
    }

    public void setValorNominal(String valorNominal) {
        this.valorNominal = valorNominal;
    }

    public String getAforo() {
        return aforo;
    }

    public void setAforo(String aforo) {
        this.aforo = aforo;
    }

    public String getPrecioBase() {
        return precioBase;
    }

    public void setPrecioBase(String precioBase) {
        this.precioBase = precioBase;
    }

    public String getTasaDescuento() {
        return tasaDescuento;
    }

    public void setTasaDescuento(String tasaDescuento) {
        this.tasaDescuento = tasaDescuento;
    }

    public String getPrecioFactoraje() {
        return precioFactoraje;
    }

    public void setPrecioFactoraje(String precioFactoraje) {
        this.precioFactoraje = precioFactoraje;
    }

    public String getImporteDescuento() {
        return importeDescuento;
    }

    public void setImporteDescuento(String importeDescuento) {
        this.importeDescuento = importeDescuento;
    }
}
