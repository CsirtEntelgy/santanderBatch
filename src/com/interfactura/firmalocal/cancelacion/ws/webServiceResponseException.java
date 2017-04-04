package com.interfactura.firmalocal.cancelacion.ws;

public class webServiceResponseException extends Exception {
	
	
	private static final long serialVersionUID = -1992543375572071934L;
	
	public webServiceResponseException(String mensaje, Exception e) {
		super(mensaje, e);
	}
	
	public webServiceResponseException(String mensaje) {
		super(mensaje);
	}
}
