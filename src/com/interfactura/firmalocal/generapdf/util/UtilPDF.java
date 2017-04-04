package com.interfactura.firmalocal.generapdf.util;

import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;


public class UtilPDF {
	private static Logger logger = Logger.getLogger(UtilPDF.class);
	public static final String templateC="templateSantander.pdf";
	public static final String templateB="templateSantanderB.pdf";
	public static final String templateI="templateSantanderI.pdf";
	public static final String templateE="templateSantanderE.pdf";
	public static final String templateCF="templatefactoraje.pdf";
	public static final String templateBF="templateSantanderB.pdf";
	public static final String templateIF="templateSantanderI.pdf";
	public static final String templateEF="templatefactorajeE.pdf";
    public static final String templateCFD20 = "templateSantanderCFD20.pdf";
    public static final String templateSantanderCFDI = "templateSantanderCFDI.pdf";
    public static final String templateFactorajeCFDI = "templateFactorajeCFDI.pdf";
	
	public static final String path="";
	public static final int recordNumberC=19;
	public static final int recordNumberB=32;
	public static final int recordNumberI=31;
	public static final int recordNumberE=35;
	
	public static final int recordNumberCF=19;
	public static final int recordNumberBF=32;
	public static final int recordNumberIF=31;
	public static final int recordNumberEF=35;
	
	private static NumberFormat formatoMoneda;
	
	/**
	 * 
	 * @param request
	 */
	public static void verParametros(HttpServletRequest request){
		Enumeration enumt=request.getParameterNames();

		logger.info("Buscando Elementos entro");
		while(enumt.hasMoreElements()){
			logger.info(enumt.nextElement().toString());
		}
		logger.info("Buscando Elementos fin");
	}
	
	/**
	 * 
	 * @param request
	 */
	public static void verAtributos(HttpServletRequest request){
		Enumeration enumt=request.getAttributeNames();

		logger.info("Buscando Atributos entro");
		while(enumt.hasMoreElements()){
			logger.info(enumt.nextElement().toString());
		}
		logger.info("Buscando Atributos fin");
	}
	
	/**
	 * 
	 * @param desc
	 * @param numberl
	 * @return
	 */
	public static int getLineas(String desc, int numberl){
		int i=desc.length()/numberl;
		if((desc.length()%numberl)>0){
			i+=1;
		}
		
		return i;
	}
	
	/**
	 * 
	 * @param cadena
	 * @param longitud
	 * @return
	 */
	public static String[] longitudFija(String cadena, int longitud){
		String temp[]={"","0"};
		
		int numero=cadena.length()/longitud;
		
		System.out.println("cadenaOriginal:" + cadena);
		System.out.println("longitudMax:" + longitud);
		System.out.println("lengthCadena:" + cadena.length());
		System.out.println("numero:" + numero);
		
		for(int i=0; i<numero;i++){
			temp[0]+=cadena.substring((longitud*i),longitud*(i+1))+"\n";
			System.out.println("i:" + i);
			System.out.println("longitud*i:" + longitud*i);
			System.out.println("longitud*(i+1):" + longitud*(i+1));
			System.out.println("subString:" + cadena.substring((longitud*i),longitud*(i+1)));
		}
		
		if(cadena.length()%longitud!=0){
			temp[0]+=cadena.substring((longitud*numero),cadena.length())+"\n";
			System.out.println("numero:" + numero);
			System.out.println("longitud*numero:" + longitud*numero);
			System.out.println("cadena.length():" + cadena.length());
			System.out.println("subString:" + cadena.substring((longitud*numero),cadena.length()));
			
			numero+=1;
		}
		
		temp[1]=String.valueOf(numero);
		
		return temp;
	}
	
	/**
	 * 
	 * @param cadena
	 * @return
	 */
	public static String getUTF8(String cadena){
		try {
			return new String(cadena.getBytes(),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	/**
	 * 
	 * @param nombreLargo
	 * @return
	 */
	public static String getMoneda(String nombreLargo, double cantidad){
		if(cantidad>1.9){
			if(UtilPDF.getMoneda(nombreLargo)){
				nombreLargo+="S";
			} else {
				nombreLargo+="ES";
			}
		}
		
		return nombreLargo;
	}
	
	/**
	 * 
	 * @param nombreLargo
	 * @return
	 */
	public static boolean getMoneda(String nombreLargo){
		char c=nombreLargo.charAt(nombreLargo.length()-1);
		if(c=='E'||c=='I'||c=='O'||c=='A'||c=='U'){
			return true;
		}
		
		return false;
	}
	
	/**
	 * 
	 * @param valor
	 * @return
	 */
	public static String formatNumber(double valor)
	{
		formatoMoneda=NumberFormat.getNumberInstance();
		formatoMoneda.setMinimumFractionDigits(2);
		return formatoMoneda.format(valor);
	}
	
	/**
	 * 
	 * @param valor
	 * @return
	 */
	public static String formatNumberDivisas(double valor)
	{
		formatoMoneda=NumberFormat.getNumberInstance();
		formatoMoneda.setMinimumFractionDigits(2);
		formatoMoneda.setMaximumFractionDigits(4);
		return formatoMoneda.format(valor);
	}
	/**
	 * 
	 * @param args
	 */
	public static void main(String...args){
		
		String temp="||2.0|4|2010-12-16T15:32:15|12345678|2010|ingreso|Pago en una sola exhibición|15000.0|17400.0|GSC950906H37|Gestion Santander, S.A. de C.V.,"
				+"Sociedad Operadora de Sociedades de Inversion, Grupo Financiero Santander|Prolongacion Paseo de la Reforma|500|Piso 2 Mod 206|Lomas de Santa"
				+"Fe|Temporal|Distrito Federal|mexico|01219|SAVM7105287U3|asdasd asdad|asda|asdsa|asd|qasda|qasda|qasd|aa|Queretaro|Mexico|520|10|asdsad|asd asd"
				+"asda|1500.0|15000.0|IVA|16|2400.0|2400.0||";
		System.out.println(longitudFija(temp,140));
	}
	
	public static String formatNumberQuantity(Double d) {
        if (d == null) {
            return "";
        }
        NumberFormat f;
        if (hasDecimals(d.toString())) {
            f = new java.text.DecimalFormat("#0.00");
        } else {
            f = new java.text.DecimalFormat("#0");
        }
        return f.format(d);
    }
	
	 private static boolean hasDecimals(String number) {
	        String[] partes = number.split("\\.");
	        if (partes != null && partes.length == 2) {
	            long numberInt = Long.parseLong(partes[0]);
	            double numberCom = Double.parseDouble(number);
	            return numberCom - numberInt > 0;
	        } else {
	            return false;
	        }

	    }
}
