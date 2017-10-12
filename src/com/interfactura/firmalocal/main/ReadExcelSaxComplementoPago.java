package com.interfactura.firmalocal.main;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.interfactura.firmalocal.controllers.MassiveReadComplementoPagoController;

public class ReadExcelSaxComplementoPago {

	/**
	 * @param args
	 */
	private static File fileExitTXT = null;
	private static FileOutputStream salidaTXT = null;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("--Entrando a main de ReadExcelSax--");
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

		String PathFacturacionSalida = MassiveReadComplementoPagoController.PathFacturacionSalida;

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
								OPCPackage pkg = OPCPackage.open(file.getAbsolutePath());
								XSSFWorkbook workbookX = new XSSFWorkbook(pkg);
								XSSFSheet sheetFacturas = workbookX.getSheetAt(0);
								XSSFSheet sheetPagos = workbookX.getSheetAt(1);
								Iterator<Row> rowIteratorCont = sheetFacturas.iterator();
								Iterator<Row> rowIteratorCont2 = sheetPagos.iterator();
								Iterator<Row> rowIterator = sheetFacturas.iterator();
								Iterator<Row> rowIterator2 = sheetPagos.iterator();
								boolean finFacturas = checkEndFile(rowIteratorCont);
								boolean finPagos = checkEndFile(rowIteratorCont2);
								if (finFacturas && finPagos) {
									getLineFactura(rowIterator, rowIterator2, sheetPagos);
								}

								salidaTXT.write("\r\n".getBytes("UTF-8"));
								salidaTXT.close();

								if (finFacturas && finPagos) {
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

	private static boolean checkEndFile(Iterator<Row> rowIteratorCont) {
		boolean finArchivo = false;
		while (rowIteratorCont.hasNext() && !finArchivo) {
			Row rowCont = rowIteratorCont.next();
			if (rowCont.getCell(0) != null) {
				System.out.println(rowCont.getCell(0).toString());
				if (rowCont.getCell(0).toString().toUpperCase().trim().equals("||FINARCHIVO||")) {
					finArchivo = true;
				}
			}

		}
		return finArchivo;
	}

	private static void getLineFactura(Iterator<Row> rowIteratorFacturas, Iterator<Row> rowIteratorPagos,
			XSSFSheet sheetPagos) {
		boolean finArchivo = false;
		StringBuffer result = new StringBuffer();
		StringBuffer currFacReference = new StringBuffer();
		while (rowIteratorFacturas.hasNext() && !finArchivo) {
			Row rowCont = rowIteratorFacturas.next();
			if (rowCont.getCell(0) != null) {
				if (rowCont.getCell(0).toString().toUpperCase().trim().equals("||FINARCHIVO||")) {
					finArchivo = true;
					break;
				}
			}
			for (int i = 0; i < 32; i++) {
				if (rowCont.getCell(i) != null) {
					rowCont.getCell(i).setCellType(Cell.CELL_TYPE_STRING);
					if (i == 0) {
						currFacReference.append(rowCont.getCell(i).toString().trim());
					}
					result.append(rowCont.getCell(i).toString() + "|");
				} else {
					result.append("|");
				}
			}
			Iterator<Row> rowIteratorTmp = sheetPagos.iterator();

			result.append(getLinePago(rowIteratorTmp, sheetPagos, currFacReference.toString()));
			result.append("FINFACTURA|");
			try {
				salidaTXT.write((result.toString() + "\r\n").getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			result = new StringBuffer();
			currFacReference = new StringBuffer();

		}
	}

	private static StringBuffer getLinePago(Iterator<Row> rowIteratorPagos, XSSFSheet sheetPagos,
			String referenciaFactura) {
		boolean finArchivo = false;
		StringBuffer result = new StringBuffer();
		List<Row> rowsToDelete = new ArrayList<Row>();
		while (rowIteratorPagos.hasNext() && !finArchivo) {
			Row rowCont = rowIteratorPagos.next();
			if (rowCont.getCell(0) != null) {
				if (rowCont.getCell(0).toString().toUpperCase().trim().equals("||FINARCHIVO||")) {
					finArchivo = true;
					break;
				}
			}
			for (int i = 0; i < 24; i++) {
				if (rowCont.getCell(i) != null) {
					rowCont.getCell(i).setCellType(Cell.CELL_TYPE_STRING);
					if (i == 0 && !rowCont.getCell(i).toString().trim().equals(referenciaFactura)) {
						break;
					} else if (i == 0 && rowCont.getCell(i).toString().trim().equals(referenciaFactura)) {
						rowsToDelete.add(rowCont);
					}
					result.append(rowCont.getCell(i).toString() + "|");
				} else {
					result.append("|");
				}
			}
		}
		for (Row x : rowsToDelete) {
			sheetPagos.removeRow(x);
		}
		return result;
	}

}
