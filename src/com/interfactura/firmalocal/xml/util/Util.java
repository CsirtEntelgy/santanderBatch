package com.interfactura.firmalocal.xml.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.SequenceInputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.interfactura.firmalocal.xml.CatalogosDom;

public class Util 
{
	
	private static Logger logger = Logger.getLogger(Util.class);
	private static String temp;
	private static String[][] acentos = new String[][] { { "Á", "&#193;" },
			{ "É", "&#201;" }, { "Í", "&#205;" }, { "Ó", "&#211;" },
			{ "Ú", "&#218;" }, { "á", "&#225;" }, { "é", "&#233;" },
			{ "í", "&#237;" }, { "ó", "&#243;" }, { "ú", "&#250;" } };

	/**
	 * Agrega un TAG a la pila
	 * 
	 * @param nameTAG
	 * @param pila
	 * @return
	 */
	public static String tags(String nameTAG, Stack<String> pila) {
		//logger.debug("Insertando en Pila");
		temp = "";

		if (!pila.empty()) {
			temp = "\n</cfdi:" + pila.pop() + ">";
			//logger.debug("______Saco elemento de la pila " + temp);
		}

		if (!nameTAG.equals("")) {
			pila.push(nameTAG);
		}
		return temp;
	}

	/**
	 * 
	 * @param nameTAG
	 * @param pila
	 * @param contentHandler
	 * @param uri
	 * @return
	 * @throws SAXException
	 */
	public static String tags(String nameTAG, Stack<String> pila,
			ContentHandler contentHandler, String uri) throws SAXException {
		temp = "";

		if (!pila.empty()) {
			temp = "" + pila.pop();
			contentHandler.endElement(uri, temp, temp);
		}

		if (!nameTAG.equals("")) {
			pila.push(nameTAG);
		}
		return temp;
	}

	/**
	 * Reemplaza los acentos en las vocales mayusculas y minusculas y tambien el
	 * amperson(&)
	 * 
	 * @param contenido
	 * @return
	 */
	public static String convierte(String contenido) 
	{
		// Esta funcion solo reemplaza el & por la secuencia de escape.
		// Si la secuencia de escape ya viene, significa que el documento
		// ya la trae desde la fuente y no se necesita reemplazar de nuevo.
		// Solo en caso de que no venga, es cuando se tiene que reemplazar.
		if (contenido.indexOf("&#038;") < 0)
		{	contenido = contenido.replaceAll("&", "&#038;");	}
		/*for (String[] obj : acentos) {
			contenido = contenido.replaceAll(obj[0], obj[1]);
		}*/

		return contenido;
	}

	/**
	 * Si la cadena que recibe como parametro es nula regresa una cadena vacia
	 * 
	 * @param obj
	 * @return
	 */
	public static String isNull(String obj) 
	{
		if (obj == null) 
		{	return "";	}
		return Util.convierte(obj);
	}
	
	/**
	 * Pregunta si una cadena es nula o vacia y si lo esta
	 * regresa el valor por default que recibe como parametro
	 * @param value
	 * @param defaultSTR
	 * @return
	 */
	public static String isNullOrEmpty(String value, String defaultSTR){
		if(isNullEmpty(value)){
			return defaultSTR;
		} else {
			return value;
		}
	}

	/**
	 * Genera la cadena para atributos
	 * 
	 * @param obj
	 * @param nameAttr
	 * @return
	 */
	public static String isNullEmpity(String obj, String nameAttr) 
	{
		if (isNullEmpty(obj)) 
		{	return "";	}
		return Util.convierte(nameAttr.concat("=\"").concat(obj.trim().toUpperCase()).concat("\" "));
	}

	/**
	 * 
	 * @param obj
	 * @return
	 */
	public static String isNullEmpity(String obj) {
		if (isNullEmpty(obj)) {
			return "";
		}

		return obj;
	}

	/**
	 * Si el argumento viene vacio o nulo regresa true, de lo contrario regresa
	 * false
	 * 
	 * @param obj
	 * @return
	 */
	public static boolean isNullEmpty(String obj) {
		if (obj == null) {
			return true;
		}

		if (obj.length() == 0) {
			return true;
		}

		return false;
	}

	/**
	 * Parsea un objeto 'Date' al siguiente formato: yyyy-MM-ddTHH:mm:ss
	 * 
	 * @param date
	 * @return
	 */
	public static String convertirFecha(Date date) {
				
		SimpleDateFormat f1 = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat f2 = new SimpleDateFormat("HH:mm:ss");
		
		return f1.format(date) + "T" + f2.format(date);	
	}

	/**
	 * Parsea un objeto 'String' que viene en este formato 'yyyy-MM-dd' al
	 * siguiente formato: 'yyyy-MM-ddTHH:mm:ss'
	 * 
	 * @param dateS
	 * @return
	 * @throws ParseException
	 */
	public static String convertirFecha(String dateS) throws ParseException {
		
		SimpleDateFormat f1 = new SimpleDateFormat("yyyy-MM-dd");
		Date date = f1.parse(dateS);
		
		SimpleDateFormat f2 = new SimpleDateFormat("HH:mm:ss");
		
		return f1.format(date) + "T" + f2.format(date);
		
	}

	/**
	 * 
	 * @param dateS
	 * @param time
	 * @return
	 * @throws ParseException
	 */
	public static String convertirFecha(String dateS, String time)
			throws ParseException 
	{
		
		SimpleDateFormat f1 = new SimpleDateFormat("yyyy-MM-dd");
		Date date = f1.parse(dateS);
		
		//System.out.println("convertirFecha-Despues synchronized1");
		time = time.split("T")[1];
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR, Integer.parseInt(time.substring(0, 2)));
		calendar.set(Calendar.MINUTE, Integer.parseInt(time.substring(3, 5)));
		calendar.set(Calendar.SECOND, Integer.parseInt(time.substring(6, 8)));
		SimpleDateFormat f2 = new SimpleDateFormat("HH:mm:ss");
		
		//System.out.println("convertirFechaFinal");
		
		return f1.format(calendar.getTime()) + "T"
				+ f2.format(calendar.getTime());
		
		
	}

	/**
	 * 
	 * @param dateS
	 * @return
	 * @throws ParseException
	 */
	public static Date convertirString(String dateS) throws ParseException {
		
		SimpleDateFormat f1 = new SimpleDateFormat("yyyy-MM-dd");
		
		return f1.parse(dateS);
		
	}

	/**
	 * 
	 * @param dateS
	 * @param time
	 * @return
	 * @throws ParseException
	 */
	public static Date convertirString(String dateS, String time)
			throws ParseException {
		
		String fecha = convertirFecha(dateS, time);
		fecha = fecha.replace("T", " ");
		SimpleDateFormat f1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				
		return f1.parse(fecha);
		
		
	}

	/**
	 * Parsea un objeto 'Date' al formato que recibe como segundo parametro, de
	 * ser nulo el parametro el formato a aplicar sera el siguiente: 'dd/MM/yyyy
	 * hh:mm:ss'
	 * 
	 * @param date
	 * @param format
	 * @return
	 */
	public static String convertirFecha(Date date, String format) {
		
		SimpleDateFormat f1 = null;
		if (format == null) {
			f1 = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
		} else {
			f1 = new SimpleDateFormat(format);
		}
		return f1.format(date);
		
		
	}

	/**
	 * Obtiene la fecha del sistema en el siguiente formato: 'yyyyMMddHHmmssSS'
	 * 
	 * @return
	 */
	public static String systemDate() {
		
		SimpleDateFormat f1 = new SimpleDateFormat("yyyyMMddHHmmssSS");
		
		return f1.format(Calendar.getInstance().getTime());		
		
	}

	/**
	 * Obtiene la fecha del sistema en nanosegundos
	 * 
	 * @return
	 */
	public static String systemDateNano() {
		return String.valueOf(System.nanoTime());
	}

	/**
	 * Obtiene la fecha
	 * 
	 * @param month
	 * @param year
	 * @param flag
	 * @return
	 */
	public static Date rangoFecha(int month, int year, boolean flag) {
		Calendar c = Calendar.getInstance();
		c.set(year, month - 1, 1, 0, 0, 0);

		if (flag) {
			c.set(Calendar.DAY_OF_MONTH,
					c.getActualMaximum(Calendar.DAY_OF_MONTH));
		}

		return c.getTime();
	}

	/**
	 * 
	 * @param dn
	 * @return
	 */
	public static Map<String, String> getDNMembers(String dn) {
		Map<String, String> dnames = new HashMap<String, String>();

		dn = dn.replace("\\=", "~INEQUALS~");
		dn = dn.replace("\\\"", "~INQUOTE~");
		dn = dn.replace("\"", "");
		dn = dn.replace("~INQUOTE~", "\"");
		String[] list = dn.split("=");

		boolean isKey = false;
		String tempKey = null;

		for (String m : list) {
			String s = m.replace("~INEQUALS~", "=").replaceAll("(.*)(,) (.*)",
					"$1----$3");

			if (s.contains("----")) {
				String[] commaList = s.split("----");
				if (2 == commaList.length) {
					dnames.put(tempKey, commaList[0]);
					tempKey = commaList[1];
				}
			} else {
				if (!isKey) {
					tempKey = s;
					isKey = true;
				} else {
					dnames.put(tempKey, s);
					isKey = false;
				}
			}
		}
		return dnames;
	}

	/**
	 * 
	 * @param cadena
	 * @return
	 */
	public static String replaceSR(String cadena) {
		// long t0=System.currentTimeMillis();
		//System.out.println("cadena: " + cadena);
		String cad = cadena.replace("\n", "").replace("\r", "");
		//System.out.println("cad: " + cad);
		// long t1=System.currentTimeMillis();
		// logger.info("*****Tiempo Sellado (replace Encoding): "+ (t1 - t0)+
		// " ms.");
		return cad;
	}

	/**
	 * 
	 * @param type
	 * @return
	 */
	public static String proofType(String type) {
		if (type.equals("I")) {
			return "ingreso";
		} else if (type.equals("E")) {
			return "egreso";
		} else {
			return "tipoDeComprobanteIncorrecto";
		}
	}

	/**
	 * 
	 * @param sn
	 * @return
	 */
	public static String serialNumberIES(BigInteger sn) {
		return sn.toString(16).replaceAll("(.{1})(.{1})", "$2");
	}

	/**
	 * 
	 * @param valor
	 * @param tipo
	 * @param enteros
	 * @param decimales
	 * @return
	 */
	public static String espacios(String valor, int tipo, int enteros,
			int decimales) {
		temp = "";
		int longitud = valor.length();

		for (int inicio = (longitud - enteros - decimales); inicio < (enteros + decimales); inicio++) {
			temp += " ";
		}

		if (tipo != 2) {
			return valor.concat(temp);
		} else {
			return temp.concat(valor);
		}
	}

	/**
	 * 
	 * @param d
	 * @return
	 */
	public static String formatNumber(Double d) {
		if (d == null) {
			return "";
		}
		NumberFormat f = new java.text.DecimalFormat("#0.00");
		return f.format(d);
	}

	/**
	 * Concatena N cadenas
	 * 
	 * @param args
	 * @return
	 */
	public static StringBuffer conctatArguments(String... args) 
	{
		StringBuffer concat = new StringBuffer();
		for (String obj : args) 
		{	concat.append(obj);	}

		return concat;
	}

	/**
	 * Regresa el Tipo del CFD E=Egreso I=Ingreso
	 * 
	 * @param value
	 * @return
	 */
	public String getCFDType(String value) {
		if (value.equalsIgnoreCase("Nota de Credito")) {
			return "E";
		} else if (value.equalsIgnoreCase("Factura")) {
			return "I";
		} else {
			// EstadoDeCuentaBancario
			return "E";
		}
	}

	/**
	 * 
	 * @param tasa
	 * @return
	 */
	public static String getTASA(String tasa) {
		if (tasa.contains(".")) {
			String tasas[] = tasa.split("\\.");
			String decimal = "";
			double fracc = Double.parseDouble("0." + tasas[1]);
			int entero = Integer.parseInt(tasas[0]);
			if (fracc > 0) {
				decimal = "" + (entero + fracc);
			} else {
				decimal = "" + entero;
			}
			return decimal;
		} else {
			return tasa;
		}
	}

	/**
	 * Establece el enconding a UTF-8
	 * 
	 * @param out
	 * @return
	 * @throws IOException
	 */
	public static ByteArrayOutputStream enconding(ByteArrayOutputStream out)
			throws IOException 
	{
		OutputStreamWriter outSW = new OutputStreamWriter(out);
		String enconding = outSW.getEncoding();
		logger.debug(enconding);
		//System.out.println("stringUTF8:" + out.toString());
		if (!enconding.equals("UTF-8")) 
		{
			String stringUTF8 = out.toString();
			
			ByteArrayOutputStream outUTF8 = new ByteArrayOutputStream();
			outSW = new OutputStreamWriter(outUTF8, "UTF-8");
			outSW.write(stringUTF8);
			outSW.close();
			logger.debug(outUTF8.toString());
			out = outUTF8;
		}

		return out;
	}

	/**
	 * Convierte una cadena a una cierta longitud de N renglones, los renglones
	 * los determina la longitud que puede tener la cadena por renglon
	 * 
	 * @param cadena
	 * @param inicio
	 * @param longitud
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public static byte[] selloCadena(String cadena, String inicio, int longitud) 
		throws UnsupportedEncodingException 
	{
		int n = cadena.length() / longitud;
		int limitI = 0;
		int limitF = longitud ;
		temp = "";
		for (int i = 1; i <= n; i++) 
		{
			temp += inicio + "|" + cadena.substring(limitI, limitF) + "\r\n";
			limitI = limitF;
			limitF = (i + 1) * longitud;
		}
		if (cadena.length() % longitud > 0) 
		{	temp += inicio + "|" + cadena.substring(limitI, cadena.length()) + "\r\n";	}
		return temp.getBytes("UTF-8");
	}
	
	public static byte[] selloCadena2(String cadena, String inicio, int longitud) 
			throws UnsupportedEncodingException 
		{
			temp = "";			
			getRow(cadena, inicio, (cadena.length() + inicio.length() + 1), longitud - (inicio.length() + 1));		
			return temp.getBytes("UTF-8");
		}
	
	public static void getRow(String cadena, String inicio, int longitudTotal, int longitud){		
		if(longitudTotal>longitud){
			temp += inicio + "|" + cadena.substring(0, longitud) + "\r\n";
			String strCadenaSub = cadena.substring(longitud, cadena.length());
			getRow(strCadenaSub, inicio, (strCadenaSub.length() + inicio.length() + 1), longitud);
		}else{
			temp += inicio + "|" + cadena.substring(0, cadena.length()) + "\r\n"; 
		}		
	}

	/**
	 * 
	 * @param nameFile
	 * @param pathDirectory
	 * @param pathConfiguration
	 * @param startName
	 * @return
	 */
	public static boolean concatFile(String nameFile, String pathDirectory, String pathConfiguration, String startName) 
	{
		File lstFiles = new File(pathDirectory);
		SequenceInputStream concat = null;
		Writer out=null;
		String nameFileConcat=null;
		boolean flagConcat=false;
		try 
		{
			if (lstFiles.isDirectory()) 
			{
				Filtro filter = null;
				logger.debug("Nombre del Archivo Original "+nameFile);
				String ext[]=nameFile.split("\\.");
				if (startName != null) 
				{
					nameFileConcat=startName + ext[0].substring(3, ext[0].length());
					filter = new Filtro(nameFileConcat);
				} 
				else 
				{
					nameFileConcat=ext[0];
					filter = new Filtro(ext[0]);
				}
				logger.debug("Nombre a buscar: "+nameFileConcat);

				File[] files = lstFiles.listFiles(filter);
				logger.debug("Numero de Archivos: "+files.length);
				if (files.length == numberFiles(ext[0], pathConfiguration)) 
				{
					ListOfFiles concatFiles = new ListOfFiles(files);
					concat = new SequenceInputStream(concatFiles);
					int c=-1;
					FileOutputStream fos = new FileOutputStream(pathDirectory+nameFileConcat+"."+ext[1], false);
					out = new OutputStreamWriter(fos, "UTF-8");
			        while ((c = concat.read()) != -1)
			        {	out.write(c);	}
					concat.close();
					out.close();
					for (File file : files) 
					{	file.delete();	}
					flagConcat=true;
				}
			}
		} 
		catch (IOException eio) 
		{
			logger.error(eio.getLocalizedMessage(), eio);
			flagConcat=false;
		} 
		finally
		{
			concat=null;
			out=null;
		}
		return flagConcat;
	}
	
	/**
	 * 
	 * @param nameFile
	 * @param numberFiles
	 * @param pathDirectory
	 * @param startName
	 * @return
	 */
	public static boolean concatFile(String nameFile, int numberFiles, String pathDirectory, String startName) 
	{
		File lstFiles = new File(pathDirectory);
		SequenceInputStream concat = null;
		BufferedOutputStream out = null;
		String nameFileConcat=null;
		boolean flagConcat=false;
		try 
		{
			if (lstFiles.isDirectory()) 
			{
				Filtro filter = null;
				logger.debug("Nombre del Archivo Original "+nameFile);
				String ext[]=nameFile.split("\\.");
				if (startName != null) 
				{
					nameFileConcat=startName + ext[0].substring(3, ext[0].length());
					filter = new Filtro(nameFileConcat+"_");
				} 
				else 
				{
					nameFileConcat=ext[0];
					filter = new Filtro(ext[0]);
				}
				System.out.println("Nombre del Archivo "+nameFileConcat);
				logger.debug("Nombre a buscar: "+nameFileConcat);
				File[] files = lstFiles.listFiles(filter);
				// Los coloca en un Hash para despues poder ordenarlos
				ArrayList<File> sortedFiles = new ArrayList<File>();
				for (int outeri = 0; outeri < files.length; outeri++)
				{
					File nextFile = null;
					for (int i = 0; i < files.length; i++)
					{
						String fileName = files[i].getName();
						int startIdx = fileName.indexOf("_");
						int endIdx = fileName.lastIndexOf("_");
						String sequence = fileName.substring(startIdx + 1, endIdx);
						String compareStr = outeri + "";
						if (compareStr.equals(sequence))
						{	
							nextFile = files[i];
							break;
						}
					}
					if (nextFile != null)
					{	sortedFiles.add(nextFile);	}
				}
				System.out.println("Numero de Archivos: "+sortedFiles.size());
				logger.debug("Numero de Archivos: "+sortedFiles.size());
				if (sortedFiles.size() == numberFiles) 
				{
					logger.debug("***Todas las partes encontradas " + sortedFiles.size());
					// Lo pasa a arreglo
					File[] sortedFilesArr = new File[sortedFiles.size()];
					for (int j = 0; j < sortedFiles.size(); j++)
					{	sortedFilesArr[j] = sortedFiles.get(j);	}
					ListOfFiles concatFiles = new ListOfFiles(sortedFilesArr);
					concat = new SequenceInputStream(concatFiles);
					int c = -1;
					
					FileOutputStream fos = new FileOutputStream(pathDirectory+nameFileConcat+"."+ext[1], false);
					out = new BufferedOutputStream(fos);

			        while ((c = concat.read()) != -1)
			        {	out.write(c);	}

					concat.close();
					out.close();
					
					for (File file : sortedFilesArr) 
					{	file.delete();	}
					flagConcat=true;
				}
				else
				{
					if (numberFiles == 1)
					{	return true;	}
					else
					{	return false;	}
				}
			}
		} 
		catch (IOException eio) 
		{
			eio.printStackTrace();
			logger.error(eio.getLocalizedMessage(), eio);
			flagConcat=false;
		} 
		finally
		{
			concat=null;
			out=null;
		}
		return flagConcat;
	}
	
	/**
	 * 
	 * @return
	 */
	public static String getTime()
	{
		if(File.separatorChar=='/')
		{	return Util.convertirFecha(Calendar.getInstance().getTime(), "HH:mm:ss");	} 
		else 
		{	return Util.convertirFecha(Calendar.getInstance().getTime(), "HHmmss");		}
	}

	/**
	 * 
	 * @param nameFile
	 * @param path
	 * @return
	 */
	public static int numberFiles(String nameFile, String path) 
	{
		int number = 0;
		LineNumberReader line = null;
		try 
		{
			line = new LineNumberReader(new FileReader(path));
			String linea = null;
			String array[] = null;
			while (line.ready()) 
			{
				linea = line.readLine();
				if (linea.contains(nameFile)) 
				{
					array = linea.concat("|temp").split("\\|");
					number = Integer.parseInt(array[2]);
				}
			}
		} 
		catch (FileNotFoundException e) 
		{	number = 0;		} 
		catch (IOException e) 
		{	number = 0;		} 
		finally 
		{
			if (line != null) 
			{
				try 
				{	line.close();	} 
				catch (IOException e) 
				{
				}
			}
			line = null;
		}

		return number;
	}

	public static void main(String... args) 
	{
		Util.concatFile("CFDOPMEXDER20101215.TXT",
				"D:\\xmlInterfactura\\procesados\\incidencia\\",
				"D:\\xmlInterfactura\\configuration.txt", "INC");
	}

	//Métodos para validar durante la generacion de cifras en ECB
	public static boolean validaPeriodo(String periodo)throws PatternSyntaxException
	{
		// TODO Auto-generated method stub
		//Patron del Periodo--->>>[0-9]{4}-[0-9]{2}-[0-9]{2}
		
		 Pattern p = Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}");
		 Matcher m = p.matcher(periodo.trim());
		 
		 Pattern p2 = Pattern.compile("[0-9]{4}-[0-9]{2}");
		 Matcher m2 = p2.matcher(periodo.trim());
	     
	     if(!m.find() && !m2.find()){
	    	 //Periodo no valido
	    	 return false;
	     }
	     else{
	    	 //Periodo valido
	    	 return true;
	     }
	}
	
	public static boolean validaMoneda(String moneda, String strInterface){
		
		if(moneda.trim().equals("BME")){
			if(strInterface.trim().toUpperCase().equals("CFDLFFONDOS"))
				return true;
			else
				return false;
		}else if(moneda.trim().equals("") || moneda.trim().equals("MXN") || moneda.trim().equals("USD") || moneda.trim().equals("EUR") || moneda.trim().equals("UDI")){
			return true;
		}else{
			return false;
		}
	}
	
	public static boolean validaMonedaReproceso(String moneda, String strAplicativo){
		
		if(moneda.trim().equals("BME")){
			if(strAplicativo.trim().toUpperCase().equals("FONDOS"))
				return true;
			else
				return false;
		}else if(moneda.trim().equals("") || moneda.trim().equals("MXN") || moneda.trim().equals("USD") || moneda.trim().equals("EUR") || moneda.trim().equals("UDI")){
			return true;
		}else{
			return false;
		}
	}

	public static boolean validaTipo(String tipo){
		if(tipo.trim().equals("I") || tipo.trim().equals("E"))
			return true;
		else
			return false;
	}
	
	public static boolean validaImporte(String importe){
		if(importe == null || importe.isEmpty())
			return false;
		int i=0;
		/*if(importe.trim().charAt(0) == '-'){
			if(importe.trim().length() > 1)
				i++;
			else
				return false;
		}*/
			
		boolean findPoint = false;
		
		for(; i<importe.trim().length(); i++){
			if(importe.trim().charAt(i) != '.'){
				if(!Character.isDigit(importe.trim().charAt(i)))
					return false;
			}else{
				if(!findPoint)
					findPoint = true;
				else
					return false;
			}
				
		}
			
		return  true;
	}
	
	public static boolean validaImporteImpuestos(String importe){
		if(importe == null || importe.isEmpty())
			return true;
		int i=0;
		
		boolean findPoint = false;
		
		for(; i<importe.trim().length(); i++){
			if(importe.trim().charAt(i) != '.'){
				if(!Character.isDigit(importe.trim().charAt(i)))
					return false;
			}else{
				if(!findPoint)
					findPoint = true;
				else
					return false;
			}
				
		}
			
		return  true;
	}
	
	//Métodos para validar durante la generacion de cifras en Factoraje
	
	public static boolean validaMoneda(String moneda){
		
		if(moneda.trim().equals("") || moneda.trim().equals("M.N.") || moneda.trim().equals("USD") || moneda.trim().equals("EUR") || moneda.trim().equals("UDI")){
			return true;
		}else{
			return false;
		}
	}
	
	public static String ponerCeros(String valor){
		
		boolean findPoint = false;

		int i=0;
		int c=0;
		for(; i< valor.trim().length(); i++){
			if(findPoint)
				c++;
			
			if(valor.trim().charAt(i) == '.')
				findPoint = true;			
		}		
		
		if(!findPoint){
			return valor + ".00";
		}else{
			if(c == 0)
				return valor + "00";
			else if(c == 1)
				return valor + "0";			
		}
		
		return valor;
	}
	
    public static String completeZeroDecimals(String val, int length) {
        String decimals = "";
        int numZeros = 6;
        if (val != null && !val.equals("")) {
            if (val.contains(".")) {
                decimals = val.substring(val.indexOf(".") + 1);
                if (decimals.length() > length) {
                    decimals = decimals.substring(0, length);
                }
                val = val.substring(0, val.indexOf(".") + 1);
            } else {
                val += ".";
            }
        } else {
            val = "0.";
        }
        numZeros = numZeros - decimals.length();
        return val + completeWithCharacter(decimals, "0", numZeros);
    }

    public static String completeWithCharacter(String val, String character, int length) {
        StringBuilder outputBuffer = new StringBuilder(val);
        for (int i = 0; i < length; i++) {
            outputBuffer.append(character);
        }
        return outputBuffer.toString();
    }

	public static boolean validaRFC(String rfc)
			throws PatternSyntaxException
	{
		// TODO Auto-generated method stub
		//Patron del RFC--->>>[A-Z,Ñ,&]{3,4}[0-9]{2}[0-1][0-9][0-3][0-9][A-Z,0-9]{2}[0-9,A]
		
		 Pattern p = Pattern.compile("[A-Z,Ñ,&]{3,4}[0-9]{2}[0-1][0-9][0-3][0-9][A-Z,0-9]{2}[0-9,A]");
		 Matcher m = p.matcher(rfc);
	     
	     if(!m.find()){
	    	 //RFC no valido
	    	 return false;
	     }
	     else{
	    	 //RFC valido
	    	 return true;
	     }
	}
	
	public static Map<String, ArrayList<CatalogosDom>> readXLSFile(String pathFileXLS) {
		// TODO Auto-generated method stub				
		System.out.println("Leyendo Archivo XLS de Catalogs");
		System.out.println("Ubicacion del archivo: "+pathFileXLS);
		String key = "";
		int value = 0;
		Map<String, Object> knownGoodMap = new LinkedHashMap<String, Object>();
		Map<String, ArrayList<CatalogosDom>> mapValDom = new LinkedHashMap<String, ArrayList<CatalogosDom>>();
		        try {

		        	FileInputStream file = new FileInputStream(new File(pathFileXLS));

		            // Get the workbook instance for XLS file
		            HSSFWorkbook workbook = new HSSFWorkbook(file);

		            // Get first sheet from the workbook
		            HSSFSheet sheet = workbook.getSheetAt(0);
		            
		            for (int i=0; i<workbook.getNumberOfSheets(); i++) { // PARA ITERAR VARIAS HOJAS (SHEETS)
		            	key = workbook.getSheetName(i);
		            	HSSFSheet sheetDin = workbook.getSheetAt(i);
		            	System.out.println("Key Sheet : "+ key );
		            	ArrayList<CatalogosDom> listValDom = new ArrayList<CatalogosDom>();

		            // Iterate through each rows from first sheet
		            Iterator<Row> rowIterator = sheetDin.iterator(); // Para las pestaÃ±as dinamicas
		            while (rowIterator.hasNext()) {
		                Row row = rowIterator.next();
		                
		                CatalogosDom valDom = new CatalogosDom();
		                // For each row, iterate through each columns
		                Iterator<Cell> cellIterator = row.cellIterator();
//		                System.out.println("CELL ITERATOR : "+ cellIterator.hasNext() );
		                while (cellIterator.hasNext()) {
		                    Cell cell = cellIterator.next();
//		                    System.out.println("CELL Value Num : "+ cell.getColumnIndex() );
		                    cell.setCellType(Cell.CELL_TYPE_STRING); // Prueba
		                    if(cell.getColumnIndex() == 0){
		                    	switch (cell.getCellType()) {
			                    case Cell.CELL_TYPE_NUMERIC:
			                    	valDom.setVal1(String.valueOf(cell.getNumericCellValue()));
//			                    	System.out.println("val1 KEY : "+ valDom.getVal1() );
			                        break;
			                    case Cell.CELL_TYPE_STRING:
			                    	valDom.setVal1(cell.getStringCellValue());
//			                        System.out.println("val1 KEY : "+ valDom.getVal1() );
			                        break;
			                    }		                    	
		                    }else if((cell.getColumnIndex() == 1)){
		                    	switch (cell.getCellType()) {
			                    case Cell.CELL_TYPE_NUMERIC:
			                    	valDom.setVal2(String.valueOf(cell.getNumericCellValue()));
//			                    	System.out.println("val2 : "+ valDom.getVal2() );
			                        break;
			                    case Cell.CELL_TYPE_STRING:
			                    	valDom.setVal2(cell.getRichStringCellValue().getString());
			                        System.out.println("Normal val2 : "+ cell.getStringCellValue());
			                        System.out.println("UTF-8 val2 : "+ cell.getRichStringCellValue().getString());
			                        System.out.println("---------------");
//			                        System.out.println("val2 : "+ valDom.getVal2() );
			                        break;
			                    }
		                    }else if((cell.getColumnIndex() == 2)){
		                    	switch (cell.getCellType()) {
			                    case Cell.CELL_TYPE_NUMERIC:
			                    	valDom.setVal3(String.valueOf(cell.getNumericCellValue()));
//			                    	System.out.println("val3 : "+ valDom.getVal3() );
			                        break;
			                    case Cell.CELL_TYPE_STRING:
			                    	valDom.setVal3(cell.getStringCellValue());
//			                        System.out.println("val3 : "+ valDom.getVal3() );
			                        break;
			                    }
		                    }else if((cell.getColumnIndex() == 3)){
		                    	switch (cell.getCellType()) {
			                    case Cell.CELL_TYPE_NUMERIC:
			                    	valDom.setVal4(String.valueOf(cell.getNumericCellValue()));
//			                    	System.out.println("val4 : "+ valDom.getVal4() );
			                        break;
			                    case Cell.CELL_TYPE_STRING:
			                    	valDom.setVal4(cell.getStringCellValue());
//			                        System.out.println("val4 : "+ valDom.getVal4() );
			                        break;
			                    }
		                    }else if((cell.getColumnIndex() == 4)){
		                    	switch (cell.getCellType()) {
			                    case Cell.CELL_TYPE_NUMERIC:
			                    	valDom.setVal5(String.valueOf(cell.getNumericCellValue()));
//			                    	System.out.println("val5 : "+ valDom.getVal5() );
			                        break;
			                    case Cell.CELL_TYPE_STRING:
			                    	valDom.setVal5(cell.getStringCellValue());
//			                        System.out.println("val5 : "+ valDom.getVal5() );
			                        break;
			                    }
		                    }else if((cell.getColumnIndex() == 5)){
		                    	switch (cell.getCellType()) {
			                    case Cell.CELL_TYPE_NUMERIC:
			                    	valDom.setVal6(String.valueOf(cell.getNumericCellValue()));
//			                    	System.out.println("val6 : "+ valDom.getVal6() );
			                        break;
			                    case Cell.CELL_TYPE_STRING:
			                    	valDom.setVal6(cell.getStringCellValue());
//			                        System.out.println("val6 : "+ valDom.getVal6() );
			                        break;
			                    }
		                    }else if((cell.getColumnIndex() == 6)){
		                    	switch (cell.getCellType()) {
			                    case Cell.CELL_TYPE_NUMERIC:
			                    	valDom.setVal7(String.valueOf(cell.getNumericCellValue()));
//			                    	System.out.println("val7 : "+ valDom.getVal7() );
			                        break;
			                    case Cell.CELL_TYPE_STRING:
			                    	valDom.setVal7(cell.getStringCellValue());
//			                        System.out.println("val7 : "+ valDom.getVal7() );
			                        break;
			                    }
		                    }
		                    		                  
		                } // Termina el recorrido de las celdas
		                
		                listValDom.add(valDom);
		                mapValDom.put(key, listValDom); 
		                
		            } // Termina de leer los rows		            
		            
		            } // Termina el For de la Lista de Shets (Nombre de las hojas)
		            
		            file.close();
		        } catch (FileNotFoundException e) {
		            e.printStackTrace();
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
		        System.out.println("Principal MapValDom SIZE : "+mapValDom.size());
		        System.out.println("TipoDeComprobante SIZE : "+mapValDom.get("TipoDeComprobante").size());
		        System.out.println("MetodoPago SIZE : "+mapValDom.get("MetodoPago").size());
		        System.out.println("TipoRelacion SIZE : "+mapValDom.get("TipoRelacion").size());
		        System.out.println("RegimenFiscal SIZE : "+mapValDom.get("RegimenFiscal").size());
		        System.out.println("UsoCFDI SIZE : "+mapValDom.get("UsoCFDI").size());
		        System.out.println("Impuesto SIZE : "+mapValDom.get("Impuesto").size());
		        System.out.println("TipoFactor SIZE : "+mapValDom.get("TipoFactor").size());
		        System.out.println("TasaOCuota SIZE : "+mapValDom.get("TasaOCuota").size());
		        return mapValDom;
		        
	}
}
