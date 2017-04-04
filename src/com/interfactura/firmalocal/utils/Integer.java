package com.interfactura.firmalocal.utils;

public class Integer {
	public static int parseInt(String cadena) {
		return com.interfactura.firmalocal.utils.Integer.parseInt(cadena, 0);
	}
	public static int parseInt(String cadena, int valorPorDefecto) {
		try {
			return java.lang.Integer.parseInt(cadena);
		} catch (NumberFormatException nfe) {
			return valorPorDefecto;
		}
	}
}
