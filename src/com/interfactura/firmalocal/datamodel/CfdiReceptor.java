/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.interfactura.firmalocal.datamodel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Maximino Llovera
 */
public class CfdiReceptor {

    private String nombre;
    private String numRegIdTrib;
    private String residenciaFiscal;
    private String rfc;
    private String usoCFDI;
    private CfdiDomicilio domicilio;
    
    private static final String rfcPattern = "[A-Z,Ñ,&]{3,4}[0-9]{2}[0-1][0-9][0-3][0-9][A-Z,0-9]?[A-Z,0-9]?[0-9,A-Z]?";
    private Pattern pattern;
    private Matcher matcher;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNumRegIdTrib() {
        return numRegIdTrib;
    }

    public void setNumRegIdTrib(String numRegIdTrib) {
        this.numRegIdTrib = numRegIdTrib;
    }

    public String getResidenciaFiscal() {
        return residenciaFiscal;
    }

    public void setResidenciaFiscal(String residenciaFiscal) {
        this.residenciaFiscal = residenciaFiscal;
    }

    public String getRfc() {
        return rfc;
    }

    public void setRfc(String rfc) {
    	
    	this.pattern = Pattern.compile(rfcPattern);
		this.matcher = this.pattern.matcher(rfc);
		
		if(this.matcher.matches()){
			this.rfc = rfc;
		}else{
			System.out.println("RFC: "+rfc+" no cumple con el formato, es reemplazado por: XAXX010101000");
			this.rfc = "XAXX010101000";
		}
    }

    public String getUsoCFDI() {
        return usoCFDI;
    }

    public void setUsoCFDI(String usoCFDI) {
        this.usoCFDI = usoCFDI;
    }

    public CfdiDomicilio getDomicilio() {
        return domicilio;
    }

    public void setDomicilio(CfdiDomicilio domicilio) {
        this.domicilio = domicilio;
    }
}
