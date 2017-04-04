package com.interfactura.firmalocal.xml.util;

import java.text.DecimalFormat;

import org.apache.log4j.Logger;

public class NumberToLetterConverter {
	private static Logger logger = Logger.getLogger(NumberToLetterConverter.class);
	private static final String[] UNIDADES = { "", "UN ", "DOS ", "TRES ",
			"CUATRO ", "CINCO ", "SEIS ", "SIETE ", "OCHO ", "NUEVE ", "DIEZ ",
			"ONCE ", "DOCE ", "TRECE ", "CATORCE ", "QUINCE ", "DIECISEIS ",
			"DIECISIETE ", "DIECIOCHO ", "DIECINUEVE ", "VEINTE " };
 
	private static final String[] DECENAS = { "VEINTI ", "TREINTA ", "CUARENTA ",
			"CINCUENTA ", "SESENTA ", "SETENTA ", "OCHENTA ", "NOVENTA ",
			"CIEN " };
 
	private static final String[] CENTENAS = { "CIENTO ", "DOSCIENTOS ",
			"TRESCIENTOS ", "CUATROCIENTOS ", "QUINIENTOS ", "SEISCIENTOS ",
			"SETECIENTOS ", "OCHOCIENTOS ", "NOVECIENTOS " };
 
	/**
	 * Convierte a letras un numero de la forma $123,456.32 (StoreMath)
	 * <p>
	 * Creation date 5/06/2006 - 10:20:52 AM
	 * 
	 * @param number
	 *            Numero en representacion texto
	 * @return Numero en letras
	 * @since 1.0
	 */
	public static String convertNumberToLetter(String number) {
		logger.info("Iniciando al conversion de numeros a letras");
		return convertNumberToLetter(Double.parseDouble(number));
	}
 
	/**
	 * Convierte un numero en representacion numerica a uno en representacion de
	 * texto. El numero es valido si esta entre 0 y 999'999.999
	 * <p>
	 * Creation date 3/05/2006 - 05:37:47 PM
	 * 
	 * @param number
	 *            Numero a convertir
	 * @return Numero convertido a texto
	 * @throws NumberFormatException
	 *             Si el numero esta fuera del rango
	 * @since 1.0
	 */
	public static String convertNumberToLetter(double number)
			throws NumberFormatException {
		String converted = "";
 
		String splitNumber[] = String.valueOf(formatNumber(number)).replace('.', '#')
				.split("#");
 
		int billon=Integer.parseInt(String.valueOf(getDigitAt(splitNumber[0],
				14))
				+ String.valueOf(getDigitAt(splitNumber[0], 13))
				+ String.valueOf(getDigitAt(splitNumber[0], 12)));
		
		int milesDeMillones=Integer.parseInt(String.valueOf(getDigitAt(splitNumber[0],
				11))
				+ String.valueOf(getDigitAt(splitNumber[0], 10))
				+ String.valueOf(getDigitAt(splitNumber[0], 9)));
		
		int millon = Integer.parseInt(String.valueOf(getDigitAt(splitNumber[0],
				8))
				+ String.valueOf(getDigitAt(splitNumber[0], 7))
				+ String.valueOf(getDigitAt(splitNumber[0], 6)));
		
		int miles = Integer.parseInt(String.valueOf(getDigitAt(splitNumber[0],
				5))
				+ String.valueOf(getDigitAt(splitNumber[0], 4))
				+ String.valueOf(getDigitAt(splitNumber[0], 3)));
		
		int cientos = Integer.parseInt(String.valueOf(getDigitAt(
				splitNumber[0], 2))
				+ String.valueOf(getDigitAt(splitNumber[0], 1))
				+ String.valueOf(getDigitAt(splitNumber[0], 0)));
		
		if (billon == 1) {
			
			if( milesDeMillones == 1 || milesDeMillones > 1 || millon == 1 || millon > 1 || miles == 1 || miles > 1)
				converted += "UN BILLON ";
			else
				converted += "UN BILLON DE ";
			
		}
		if (billon > 1)
			converted = convertNumber(String.valueOf(billon)) + " BILLONES ";
		
		if(milesDeMillones == 1){
			if(millon > 0){
				converted += "MIL ";
			}else if(miles > 0){
				converted += "MIL MILLONES ";
			}else{
				converted += "MIL MILLONES DE ";
			}
		}else if(milesDeMillones > 1){
			if(millon > 0){				
				converted += convertNumber(String.valueOf(milesDeMillones)) + " MIL ";
			}else if(miles > 0){				
				converted += convertNumber(String.valueOf(milesDeMillones)) + " MIL MILLONES ";
			}else{				
				converted += convertNumber(String.valueOf(milesDeMillones)) + " MIL MILLONES DE ";
			}
		}
		
		// Descompone el trio de millones - ¡SGT!
				
		
		if (millon == 1) {
			if( miles ==1 || miles > 1)
				converted += "UN MILLON ";
			else
				converted += "UN MILLON DE ";
		}
		if (millon > 1)
			converted += convertNumber(String.valueOf(millon)) + " MILLONES ";
		if(miles == 0 && !(cientos >= 1))
			converted += "DE";
 
		// Descompone el trio de miles - ¡SGT!
		
		if (miles == 1)
			converted += "MIL ";
		if (miles > 1)
			converted += convertNumber(String.valueOf(miles)) + " MIL ";
 
		// Descompone el ultimo trio de unidades - ¡SGT!
		
		if (cientos == 1)
			converted += "UN";
 
		if (billon + millon + miles + cientos == 0)
			converted += "CERO";
		if (cientos > 1)
			converted += convertNumber(String.valueOf(cientos));
 
		
		 converted += " MONEDAT ";
		
		// Descompone los centavos - Camilo
		int centavos = Integer.parseInt(String.valueOf(getDigitAt(
				splitNumber[1], 2))
				+ String.valueOf(getDigitAt(splitNumber[1], 1))
				+ String.valueOf(getDigitAt(splitNumber[1], 0)));
		//if (centavos == 1)
		//	converted += " CON UN CENTAVO";
		//if (centavos > 1)
		//	converted += " CON " + convertNumber(String.valueOf(centavos))
		//			+ "CENTAVOS";
		if(String.valueOf(centavos).length() == 1) {
			String centavosString = "0" + centavos;
			converted += centavosString+"/100 ";
			return converted;
		}
		/*System.out.println(String.valueOf(getDigitAt(
				splitNumber[1], 2))
				+ String.valueOf(getDigitAt(splitNumber[1], 1))
				+ String.valueOf(getDigitAt(splitNumber[1], 0)));*/
		converted +=centavos+"/100 ";
		return converted;
	}
 
	/**
	 * Convierte los trios de numeros que componen las unidades, las decenas y
	 * las centenas del numero.
	 * <p>
	 * Creation date 3/05/2006 - 05:33:40 PM
	 * 
	 * @param number
	 *            Numero a convetir en digitos
	 * @return Numero convertido en letras
	 * @since 1.0
	 */
	private static String convertNumber(String number) {
		if (number.length() > 3)
			throw new NumberFormatException(
					"La longitud maxima debe ser 3 digitos");
 
		String output = "";
		if (getDigitAt(number, 2) != 0)
			output = CENTENAS[getDigitAt(number, 2) - 1];
 
		int k = Integer.parseInt(String.valueOf(getDigitAt(number, 1))
				+ String.valueOf(getDigitAt(number, 0)));
 
		if (k <= 20)
			output += UNIDADES[k];
		else {
			if (k > 30 && getDigitAt(number, 0) != 0)
				output += DECENAS[getDigitAt(number, 1) - 2] + "Y "
						+ UNIDADES[getDigitAt(number, 0)];
			else
				output += DECENAS[getDigitAt(number, 1) - 2].trim()
						+ UNIDADES[getDigitAt(number, 0)];
		}
 
		// Caso especial con el 100
		if (getDigitAt(number, 2) == 1 && k == 0)
			output = "CIEN";
 
		return output;
	}
 
	/**
	 * Retorna el digito numerico en la posicion indicada de derecha a izquierda
	 * <p>
	 * Creation date 3/05/2006 - 05:26:03 PM
	 * 
	 * @param origin
	 *            Cadena en la cual se busca el digito
	 * @param position
	 *            Posicion de derecha a izquierda a retornar
	 * @return Digito ubicado en la posicion indicada
	 * @since 1.0
	 */
	private static int getDigitAt(String origin, int position) {
		if (origin.length() > position && position >= 0)
			return origin.charAt(origin.length() - position - 1) - 48;
		return 0;
	}
	
	private static String formatNumber(double valor){
		DecimalFormat f=new DecimalFormat("#########0.00");
		String strValor = String.valueOf(valor);
		String strNumber = "";
		
		if(strValor.indexOf("-") != -1){
			strValor = strValor.substring(1);
			
			strNumber = f.format(Double.parseDouble(strValor));
		}else{
			strNumber = f.format(valor);
		}
						
		return strNumber;
	}
}