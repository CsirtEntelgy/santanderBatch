package com.interfactura.firmalocal.xml.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
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
import java.util.Map;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class Util_Masivo {
	private static Logger logger = Logger.getLogger(Util_Masivo.class);
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
		return Util.convierte(nameAttr.concat("=\"").concat(obj.trim()).concat("\" "));
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
			return "traslado";
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
	public static String formatNumberDivisas(Double d) {
		if (d == null) {
			return "";
		}
		NumberFormat f = new java.text.DecimalFormat("#0.0000");
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
		if (value.equals("Nota de Credito")) {
			return "E";
		} else if (value.equals("Factura")) {
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
	public static void main(String... args) 
	{
		Util.concatFile("CFDOPMEXDER20101215.TXT",
				"D:\\xmlInterfactura\\procesados\\incidencia\\",
				"D:\\xmlInterfactura\\configuration.txt", "INC");
	}
}
