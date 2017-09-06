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
public class CfdiConceptoImpuesto {
    private List<CfdiConceptoImpuestoTipo> traslados;
    private List<CfdiConceptoImpuestoTipo> retenciones;

    public List<CfdiConceptoImpuestoTipo> getTraslados() {
        return traslados;
    }

    public void setTraslados(List<CfdiConceptoImpuestoTipo> traslados) {
        this.traslados = traslados;
    }

    public List<CfdiConceptoImpuestoTipo> getRetenciones() {
        return retenciones;
    }

    public void setRetenciones(List<CfdiConceptoImpuestoTipo> retenciones) {
        this.retenciones = retenciones;
    }
    
}
