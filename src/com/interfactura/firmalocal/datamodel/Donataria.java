package com.interfactura.firmalocal.datamodel;

/**
 * Clase con la información del complemento donatarias.
 */
public class Donataria {
    private String version;
    private String noAutorizacion;
    private String fechaAutorizacion;
    private String leyenda;

    public Donataria(){

    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getNoAutorizacion() {
        return noAutorizacion;
    }

    public void setNoAutorizacion(String noAutorizacion) {
        this.noAutorizacion = noAutorizacion;
    }

    public String getFechaAutorizacion() {
        return fechaAutorizacion;
    }

    public void setFechaAutorizacion(String fechaAutorizacion) {
        this.fechaAutorizacion = fechaAutorizacion;
    }

    public String getLeyenda() {
        return leyenda;
    }

    public void setLeyenda(String leyenda) {
        this.leyenda = leyenda;
    }
}
