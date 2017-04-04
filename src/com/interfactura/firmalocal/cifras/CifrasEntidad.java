package com.interfactura.firmalocal.cifras;

public class CifrasEntidad {

	private String rfc;
	private String nombre;
	private String moneda;	
	private String periodo;
	private String fechaEmision;
	
	private String tasaIva;
	
	CifrasIE ingresosEmitidos;
	CifrasIE egresosEmitidos;
	
	CifrasIE ingresosIncidentes;
	CifrasIE egresosIncidentes;
	
	CifrasIE ingresosSAT;
	CifrasIE egresosSAT;
	
	CifrasIE ingresosCanceladosSAT;
	CifrasIE egresosCanceladosSAT;
	
	long contadorIngresosEmitidos;
	long contadorEgresosEmitidos;
	
	long contadorIngresosIncidentes;
	long contadorEgresosIncidentes;
	
	long contadorIngresosSAT;
	long contadorEgresosSAT;
	
	long contadorIngresosCanceladosSAT;
	long contadorEgresosCanceladosSAT;
	
	private String nombreAplicativo;
	
	public CifrasEntidad(){
		this.rfc = "";
		this.nombre = "";
		this.moneda = "";
				
		this.ingresosEmitidos = new CifrasIE();
		this.egresosEmitidos = new CifrasIE();
		
		this.ingresosIncidentes = new CifrasIE();
		this.egresosIncidentes = new CifrasIE();
		
		this.ingresosSAT = new CifrasIE();
		this.egresosSAT = new CifrasIE();
		
		this.ingresosCanceladosSAT = new CifrasIE();
		this.egresosCanceladosSAT = new CifrasIE(); 
		
		this.contadorIngresosEmitidos = 0;
		this.contadorEgresosEmitidos = 0;
		
		this.contadorIngresosIncidentes = 0;
		this.contadorEgresosIncidentes = 0;
		
		this.contadorIngresosSAT = 0;
		this.contadorEgresosSAT = 0;
		
		this.contadorIngresosCanceladosSAT = 0;
		this.contadorEgresosCanceladosSAT = 0;
		
		
		this.nombreAplicativo = "";
		
		this.tasaIva = "";
	}

	public String getRfc() {
		return rfc;
	}

	public void setRfc(String rfc) {
		this.rfc = rfc;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getMoneda() {
		return moneda;
	}

	public void setMoneda(String moneda) {
		this.moneda = moneda;
	}

	public CifrasIE getIngresosEmitidos() {
		return ingresosEmitidos;
	}

	public void setIngresosEmitidos(CifrasIE ingresosEmitidos) {
		this.ingresosEmitidos = ingresosEmitidos;
	}

	public CifrasIE getEgresosEmitidos() {
		return egresosEmitidos;
	}

	public void setEgresosEmitidos(CifrasIE egresosEmitidos) {
		this.egresosEmitidos = egresosEmitidos;
	}

	public CifrasIE getIngresosIncidentes() {
		return ingresosIncidentes;
	}

	public void setIngresosIncidentes(CifrasIE ingresosIncidentes) {
		this.ingresosIncidentes = ingresosIncidentes;
	}

	public CifrasIE getEgresosIncidentes() {
		return egresosIncidentes;
	}

	public void setEgresosIncidentes(CifrasIE egresosIncidentes) {
		this.egresosIncidentes = egresosIncidentes;
	}

	public long getContadorIngresosEmitidos() {
		return contadorIngresosEmitidos;
	}

	public void setContadorIngresosEmitidos(long contadorIngresosEmitidos) {
		this.contadorIngresosEmitidos = contadorIngresosEmitidos;
	}

	public long getContadorEgresosEmitidos() {
		return contadorEgresosEmitidos;
	}

	public void setContadorEgresosEmitidos(long contadorEgresosEmitidos) {
		this.contadorEgresosEmitidos = contadorEgresosEmitidos;
	}

	public long getContadorIngresosIncidentes() {
		return contadorIngresosIncidentes;
	}

	public void setContadorIngresosIncidentes(long contadorIngresosIncidentes) {
		this.contadorIngresosIncidentes = contadorIngresosIncidentes;
	}

	public long getContadorEgresosIncidentes() {
		return contadorEgresosIncidentes;
	}

	public void setContadorEgresosIncidentes(long contadorEgresosIncidentes) {
		this.contadorEgresosIncidentes = contadorEgresosIncidentes;
	}

	public CifrasIE getIngresosSAT() {
		return ingresosSAT;
	}

	public void setIngresosSAT(CifrasIE ingresosSAT) {
		this.ingresosSAT = ingresosSAT;
	}

	public CifrasIE getEgresosSAT() {
		return egresosSAT;
	}

	public void setEgresosSAT(CifrasIE egresosSAT) {
		this.egresosSAT = egresosSAT;
	}

	public long getContadorIngresosSAT() {
		return contadorIngresosSAT;
	}

	public void setContadorIngresosSAT(long contadorIngresosSAT) {
		this.contadorIngresosSAT = contadorIngresosSAT;
	}

	public long getContadorEgresosSAT() {
		return contadorEgresosSAT;
	}

	public void setContadorEgresosSAT(long contadorEgresosSAT) {
		this.contadorEgresosSAT = contadorEgresosSAT;
	}

	public String getPeriodo() {
		return periodo;
	}

	public void setPeriodo(String periodo) {
		this.periodo = periodo;
	}

	public String getFechaEmision() {
		return fechaEmision;
	}

	public void setFechaEmision(String fechaEmision) {
		this.fechaEmision = fechaEmision;
	}
	
	public String getNombreAplicativo() {
		return nombreAplicativo;
	}

	public void setNombreAplicativo(String nombreAplicativo) {
		this.nombreAplicativo = nombreAplicativo;
	}

	public CifrasIE getIngresosCanceladosSAT() {
		return ingresosCanceladosSAT;
	}

	public void setIngresosCanceladosSAT(CifrasIE ingresosCanceladosSAT) {
		this.ingresosCanceladosSAT = ingresosCanceladosSAT;
	}

	public CifrasIE getEgresosCanceladosSAT() {
		return egresosCanceladosSAT;
	}

	public void setEgresosCanceladosSAT(CifrasIE egresosCanceladosSAT) {
		this.egresosCanceladosSAT = egresosCanceladosSAT;
	}

	public long getContadorIngresosCanceladosSAT() {
		return contadorIngresosCanceladosSAT;
	}

	public void setContadorIngresosCanceladosSAT(long contadorIngresosCanceladosSAT) {
		this.contadorIngresosCanceladosSAT = contadorIngresosCanceladosSAT;
	}

	public long getContadorEgresosCanceladosSAT() {
		return contadorEgresosCanceladosSAT;
	}

	public void setContadorEgresosCanceladosSAT(long contadorEgresosCanceladosSAT) {
		this.contadorEgresosCanceladosSAT = contadorEgresosCanceladosSAT;
	}

	public String getTasaIva() {
		return tasaIva;
	}

	public void setTasaIva(String tasaIva) {
		this.tasaIva = tasaIva;
	}

		
}
