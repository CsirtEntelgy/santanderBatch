package com.interfactura.recepcionmasiva.service;

public class ValidationException extends Exception {
	public static final int ARCHIVO_YA_ENVIADO = 1;
	public static final int ERROR_DE_ESTRUCTURA = 2;
	public static final int FOLIO_INVALIDO = 3;
	public static final int ESTATUS_INVALIDO = 4;
	public static final int CERTIFICADO_INVALIDO = 5;
	
	private static final long serialVersionUID = -1992543375572071934L;

	public ValidationException(String mensaje, int errorCode) {
		super(mensaje);
	}

	public ValidationException(String mensaje, Exception e) {
		super(mensaje, e);
	}

	public ValidationException(String mensaje, int errorCode, Exception e) {
		super(mensaje, e);
	}

	public ValidationException(Exception e) {
		super(e);
	}

	public ValidationException(String mensaje) {
		super(mensaje);
	}
}
