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
public class CfdiComplemento {

    private CfdiTimbreFiscalDigital timbreFiscalDigital;
    private String divisaTipoOperacion;
    private Donataria donataria;

    public CfdiTimbreFiscalDigital getTimbreFiscalDigital() {
        return timbreFiscalDigital;
    }

    public void setTimbreFiscalDigital(CfdiTimbreFiscalDigital timbreFiscalDigital) {
        this.timbreFiscalDigital = timbreFiscalDigital;
    }  
    
	public String getDivisaTipoOperacion() {
		return divisaTipoOperacion;
	}

	public void setDivisaTipoOperacion(String divisaTipoOperacion) {
		this.divisaTipoOperacion = divisaTipoOperacion;
	}

	public Donataria getDonataria() {
		return donataria;
	}

	public void setDonataria(Donataria donataria) {
		this.donataria = donataria;
	}
    

}
