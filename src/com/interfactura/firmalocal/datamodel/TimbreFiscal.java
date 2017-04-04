package com.interfactura.firmalocal.datamodel;

public class TimbreFiscal {
    private String version;
    private String uuid;
    private String fechaTimbrado;
    private String selloSat;
    private String noCertificadoSat;

    public TimbreFiscal(){

    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getFechaTimbrado() {
        return fechaTimbrado;
    }

    public void setFechaTimbrado(String fechaTimbrado) {
        this.fechaTimbrado = fechaTimbrado;
    }

    public String getSelloSat() {
        return selloSat;
    }

    public void setSelloSat(String selloSat) {
        this.selloSat = selloSat;
    }

    public String getNoCertificadoSat() {
        return noCertificadoSat;
    }

    public void setNoCertificadoSat(String noCertificadoSat) {
        this.noCertificadoSat = noCertificadoSat;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}