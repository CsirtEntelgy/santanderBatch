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
public class CfdiAddendaInformacionPago {

    private String numProveedor;
    private String ordenCompra;
    private String nombreBeneficiario;
    private String institucionReceptora;
    private String numeroCuenta;
    private String email;
    private String codigoISOMoneda;
    private String posCompra;
    private String cuentaContable;

    public String getNumProveedor() {
        return numProveedor;
    }

    public void setNumProveedor(String numProveedor) {
        this.numProveedor = numProveedor;
    }

    public String getOrdenCompra() {
        return ordenCompra;
    }

    public void setOrdenCompra(String ordenCompra) {
        this.ordenCompra = ordenCompra;
    }

    public String getNombreBeneficiario() {
        return nombreBeneficiario;
    }

    public void setNombreBeneficiario(String nombreBeneficiario) {
        this.nombreBeneficiario = nombreBeneficiario;
    }

    public String getInstitucionReceptora() {
        return institucionReceptora;
    }

    public void setInstitucionReceptora(String institucionReceptora) {
        this.institucionReceptora = institucionReceptora;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public void setNumeroCuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCodigoISOMoneda() {
        return codigoISOMoneda;
    }

    public void setCodigoISOMoneda(String codigoISOMoneda) {
        this.codigoISOMoneda = codigoISOMoneda;
    }

    public String getPosCompra() {
        return posCompra;
    }

    public void setPosCompra(String posCompra) {
        this.posCompra = posCompra;
    }

    public String getCuentaContable() {
        return cuentaContable;
    }

    public void setCuentaContable(String cuentaContable) {
        this.cuentaContable = cuentaContable;
    }

}
