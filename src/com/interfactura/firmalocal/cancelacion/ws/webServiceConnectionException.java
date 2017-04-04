package com.interfactura.firmalocal.cancelacion.ws;

public class webServiceConnectionException extends Exception {


	private static final long serialVersionUID = 1L;

	public webServiceConnectionException(String mensaje, Exception e) {
		super(mensaje, e);
	}
	
	public webServiceConnectionException(String mensaje) {
		super(mensaje);
	}
}
