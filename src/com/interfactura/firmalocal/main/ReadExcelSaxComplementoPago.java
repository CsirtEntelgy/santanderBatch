package com.interfactura.firmalocal.main;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import com.interfactura.firmalocal.controllers.MassiveReadController;
import com.interfactura.firmalocal.xml.Properties;

public class ReadExcelSaxComplementoPago {

	/**
	 * @param args
	 */
	private static String strValues = "";
	private static int rows;
	private static boolean finArchivo;
	private static boolean finFactura;
	private static int cols;
	private static File fileExitTXT = null;
	private static FileOutputStream salidaTXT = null;
	private static String strAbsolutePathTXT;
	private static String currentFile;

	private static String currentCol = "";

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("--Entrando a main de ReadExcelSax Complemento Pago--");
		try {
			readIdFileProcess(args[0], args[1]);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception massiveExcel:" + e.getMessage());
			try {
				FileOutputStream fileError = new FileOutputStream(args[1] + "massiveExcelError.txt");
				fileError.write((e.getMessage()).getBytes("UTF-8"));
				fileError.close();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.out.println("Exception al crear massiveExcelError.txt:" + e.getMessage());
			}
		}
	}

	public static void readIdFileProcess(String pathFacturacionEntrada, String pathFacturacionProceso)
			throws Exception {
		System.out.println(pathFacturacionEntrada + "IDFILEPROCESS.TXT");

		String PathFacturacionSalida = MassiveReadController.PathFacturacionSalida;

		FileInputStream fsIdFileProcess = new FileInputStream(pathFacturacionEntrada + "IDFILEPROCESS.TXT");
		DataInputStream in = new DataInputStream(fsIdFileProcess);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;

		FileOutputStream userlog = null;
		FileOutputStream fileStatus = new FileOutputStream(pathFacturacionProceso + "massiveExcel.txt");
		fileStatus.write(("Status del proceso bash massiveExcel.sh" + "\n").getBytes("UTF-8"));
		int counter = 0;
		while ((strLine = br.readLine()) != null) {
			if (!strLine.trim().equals("")) {
				String[] arrayValues = strLine.trim().split("\\|");
				File fileDirectory = new File(pathFacturacionProceso + arrayValues[1]);
				if (fileDirectory.exists()) {
					if (fileDirectory.listFiles().length == 1) {
						for (File file : fileDirectory.listFiles()) {

							String strExt = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("."),
									file.getAbsolutePath().length());
							System.out.println("AbsolutePath:" + file.getAbsolutePath());

							if (strExt.toUpperCase().equals(".XLSX")) {
								fileExitTXT = new File(
										pathFacturacionProceso + arrayValues[1] + "/" + arrayValues[1] + ".TXT");
								salidaTXT = new FileOutputStream(fileExitTXT);
								currentFile = file.getAbsolutePath();
								processOneSheet(currentFile, "1");
								System.out.println("rows:" + rows);

								salidaTXT.write("\r\n".getBytes("UTF-8"));
								salidaTXT.close();

								if (finArchivo) {
									System.out.println("finArchivo OK");
									fileStatus
											.write(("Archivo " + arrayValues[1] + ".TXT generado\n").getBytes("UTF-8"));
									userlog = new FileOutputStream(
											PathFacturacionSalida + arrayValues[1] + "/LOG" + arrayValues[1] + ".TXT",
											true);
									userlog.write(
											("Archivo " + arrayValues[1] + ".TXT generado\r\n").getBytes("UTF-8"));
								} else {
									System.out.println("finArchivo No encontrado");
									fileExitTXT.delete();
									fileStatus.write(
											("Estrictura incorrecta, no se encontro la etiqueta de control ||FINARCHIVO|| en el archivo excel de la solicitud "
													+ arrayValues[1] + "\n").getBytes("UTF-8"));
									userlog = new FileOutputStream(
											PathFacturacionSalida + arrayValues[1] + "/LOG" + arrayValues[1] + ".TXT",
											true);
									userlog.write(
											("Estrictura incorrecta, no se encontro la etiqueta de control ||FINARCHIVO|| en el archivo excel de la solicitud "
													+ arrayValues[1] + "\r\n").getBytes("UTF-8"));
								}

							} else {
								fileStatus.write(("Solo se permiten archivos XLSX\n").getBytes("UTF-8"));
								userlog = new FileOutputStream(
										PathFacturacionSalida + arrayValues[1] + "/LOG" + arrayValues[1] + ".TXT",
										true);
								userlog.write(("Solo se permiten archivos XLSX\r\n").getBytes("UTF-8"));
							}

						}
					} else if (fileDirectory.listFiles().length == 0) {
						fileStatus.write(("No se encontro el arhivo xlsx en la ruta " + pathFacturacionProceso
								+ arrayValues[1] + "/" + "\n").getBytes("UTF-8"));
						userlog = new FileOutputStream(
								PathFacturacionSalida + arrayValues[1] + "/LOG" + arrayValues[1] + ".TXT", true);
						userlog.write(("No se encontro el arhivo xlsx en la ruta " + pathFacturacionProceso
								+ arrayValues[1] + "/" + "\r\n").getBytes("UTF-8"));
					} else {
						fileStatus.write(("Solo se permite un archivo xlsx por solicitud\n").getBytes("UTF-8"));
						userlog = new FileOutputStream(
								PathFacturacionSalida + arrayValues[1] + "/LOG" + arrayValues[1] + ".TXT", true);
						userlog.write(("Solo se permite un archivo xlsx por solicitud\r\n").getBytes("UTF-8"));
					}
				} else {
					fileStatus.write(("No existe el directorio " + pathFacturacionProceso + arrayValues[1] + "\n")
							.getBytes("UTF-8"));
				}
			}
			counter++;
		}
		br.close();
		in.close();
		fsIdFileProcess.close();
		if (counter == 0) {
			fileStatus.write(("No se encontraron solicitudes a procesar\n").getBytes("UTF-8"));
		}
		fileStatus.close();
		if (userlog != null)
			userlog.close();

	}

	public static void processOneSheet(String filename, String sheetNumber) throws Exception {
		finArchivo = false;
		rows = 0;
		cols = 0;

		OPCPackage pkg = OPCPackage.open(filename);
		XSSFReader r = new XSSFReader(pkg);
		SharedStringsTable sst = r.getSharedStringsTable();

		XMLReader parser = fetchSheetParser(sst);

		// rId2 found by processing the Workbook
		// Seems to either be rId# or rSheet#
		// InputStream sheet2 = r.getSheet("rId" + sheetNumber);
		InputStream sheet2 = r.getSheet("rId1");
		InputSource sheetSource = new InputSource(sheet2);
		parser.parse(sheetSource);
		sheet2.close();
	}

	public void processAllSheets(String filename) throws Exception {
		OPCPackage pkg = OPCPackage.open(filename);
		XSSFReader r = new XSSFReader(pkg);
		SharedStringsTable sst = r.getSharedStringsTable();

		XMLReader parser = fetchSheetParser(sst);

		Iterator<InputStream> sheets = r.getSheetsData();
		while (sheets.hasNext()) {
			System.out.println("Processing new sheet:\n");
			InputStream sheet = sheets.next();
			InputSource sheetSource = new InputSource(sheet);
			parser.parse(sheetSource);
			sheet.close();
			System.out.println("");
		}
	}

	public static void printValues(String sbValues) {
		String[] arrayValues = sbValues.split("\\|");
		for (int index = 0; index < arrayValues.length; index++) {
			System.out.println("value " + index + ":" + arrayValues[index]);
		}
	}

	public static XMLReader fetchSheetParser(SharedStringsTable sst) throws SAXException {
		XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
		ContentHandler handler = new SheetHandler(sst);
		parser.setContentHandler(handler);
		return parser;
	}

	/**
	 * See org.xml.sax.helpers.DefaultHandler javadocs
	 */
	private static class SheetHandler extends DefaultHandler {
		static private enum CellType {
			non, num, staticText, sharedText
		};

		private SharedStringsTable sst;
		private String lastContents;
		private boolean nextIsString;
		private boolean nextIsNull;
		private CellType cellType;
		private ArrayList<Object> values;
		private int currentIdx = -1;

		private SheetHandler(SharedStringsTable sst) {
			this.sst = sst;
		}

		public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
			if (name.equals("c")) {
				// Print the cell reference
				System.out.print("r: " + attributes.getValue("r") + " - ");

				checkNumberRow(attributes.getValue("r"));

				if (!finFactura) {
					checkNulls(attributes.getValue("r"));
				}

				// Figure out if the value is an index in the SST
				// String cellType = attributes.getValue("t");
				String type = attributes.getValue("t");
				String cellTypeN = attributes.getValue("n");
				// System.out.println("value: " + attributes.getValue(0));

				if (type == null) {
					cellType = CellType.num;
					nextIsString = true;
				} else if (type.equals("s")) {
					cellType = CellType.sharedText;
					nextIsString = true;
				} else {
					cellType = CellType.non;
				}
				cols++;

			}
			// Clear contents cache
			lastContents = "";
		}

		public void checkNumberRow(String strCol) {
			// Get Row Number
			String strRowNumber = "";
			for (int i = 0; i < strCol.length(); i++) {
				try {
					System.out.println("char " + i + ": " + strCol.charAt(i));
					strRowNumber = strRowNumber + Integer.parseInt(String.valueOf(strCol.charAt(i)));
				} catch (NumberFormatException ex) {
					// El caracter es una letra
				}
			}

			System.out.println("rows: " + rows);
			System.out.println("rowNumber: " + strRowNumber);

			if (rows != 0 && rows != Integer.parseInt(strRowNumber)) {
				rows = Integer.parseInt(strRowNumber);
				currentCol = "";
				finFactura = false;
				if (!finArchivo) {

					try {

						strValues = strValues.replace("\n", " ");
						 processOneSheet(currentFile, "2");

						System.out.println("sbValues: " + strValues);

						salidaTXT.write((strValues + "\r\n").getBytes("UTF-8"));
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// printValues(strValues);
					strValues = "";

					// rows++;
				}

			} else if (rows == 0) {
				rows = Integer.parseInt(strRowNumber);
			}
		}

		public void checkNulls(String strCol) {
			CellReference celRefNew = new CellReference(strCol);
			int diff = 0;
			boolean fA1 = true;

			String strLetter = "";

			String strNumber = "";
			for (int i = 0; i < strCol.length(); i++) {
				try {
					System.out.println("char " + i + ": " + strCol.charAt(i));
					strNumber = strNumber + Integer.parseInt(String.valueOf(strCol.charAt(i)));
				} catch (NumberFormatException ex) {
					// El caracter es una letra
					strLetter = strLetter + strCol.charAt(i);
				}
			}

			System.out.println("strLetter:" + strLetter);
			System.out.println("strNumber:" + strNumber);

			if (currentCol.equals("")) {
				if (strLetter.toUpperCase().equals("A")) {
					currentCol = strCol;
				} else {
					currentCol = strCol;

					CellReference celRefCurr = new CellReference("A" + strNumber);
					diff = celRefNew.getCol() - celRefCurr.getCol();
					System.out.println("diff con currentCol vacio:" + diff);
					fA1 = false;
				}
			} else {
				if (strLetter.toUpperCase().equals("A")) {
					currentCol = strCol;
				} else {
					CellReference celRefCurr = new CellReference(currentCol);

					diff = celRefNew.getCol() - celRefCurr.getCol();
					System.out.println("diff con currentCol no vacio:" + diff);

					currentCol = strCol;

				}
			}

			if (fA1) {
				if (diff > 1) {
					for (int i = 0; i < diff - 1; i++) {
						strValues = strValues + "|";
					}
				}
			} else {
				for (int i = 0; i < diff; i++) {
					strValues = strValues + "|";
				}
			}
		}

		public void endElement(String uri, String localName, String name) throws SAXException {
			// Process the last contents as required.
			// Do now, as characters() may be called more than once
			if (nextIsString && !finArchivo && !finFactura) {
				if (name.equals("row")) {
					System.out.println("ENDrow");
					nextIsString = false;
				} else if (name.equals("c")) {
					if (!lastContents.equals("")) {
						int idx = Integer.parseInt(lastContents);
						// System.out.println("idx:" + idx);
						lastContents = new XSSFRichTextString(sst.getEntryAt(idx)).toString();
						System.out.println("String: " + lastContents);
						if (lastContents.toUpperCase().trim().equals("||FINARCHIVO||")) {
							System.out.println("FINARCHIVO encontrado");
							finArchivo = true;
						} else if (lastContents.toUpperCase().trim().equals("||FINFACTURA||")) {
							System.out.println("FINFACTURA encontrada");
							strValues = strValues + "FINFACTURA|";
							finFactura = true;

							currentCol = "";
						} else {
							strValues = strValues + lastContents + "|";
						}

					} else {
						System.out.println("String vacio: " + lastContents);
						strValues = strValues + lastContents + "|";
					}

					nextIsString = false;
				} else if (name.equals("v")) {
					// Process the last contents as required.
					// Do now, as characters() may be called more than once
					if (cellType == CellType.staticText || cellType == CellType.num) {
						System.out.println("staticText || num: " + lastContents);
						strValues = strValues + lastContents + "|";
						nextIsString = false;
					}
				}
			}

			if (nextIsNull) {

				lastContents = "NULL";
				nextIsNull = false;
				System.out.println("contenido null:" + lastContents);
			}

			// v => contents of a cell
			// Output after we've seen the string contents
			if (name.equals("v")) {
				// System.out.println("contenido v:" + lastContents);
			}
		}

		public void characters(char[] ch, int start, int length) throws SAXException {
			lastContents += new String(ch, start, length);
		}
	}

}
