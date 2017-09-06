package com.interfactura.firmalocal.datamodel;

import java.util.List;

public class CfdiRelacionado {
	private String tipoRelacion;
	private List<String> relacionado;
	
	public String getTipoRelacion() {
		return tipoRelacion;
	}
	public void setTipoRelacion(String tipoRelacion) {
		this.tipoRelacion = tipoRelacion;
	}
	public List<String> getRelacionado() {
		return relacionado;
	}
	public void setRelacionado(List<String> relacionado) {
		this.relacionado = relacionado;
	}	
	
}
