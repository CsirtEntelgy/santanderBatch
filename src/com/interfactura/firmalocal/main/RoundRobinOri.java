package com.interfactura.firmalocal.main;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Date;

public class RoundRobinOri {

	public final String RUTA_BASE = "D:\\esteban\\NetBeansProjects\\PruebasDeAplicaciones\\varios\\";
	public final String NOMBRE_DEL_FICHERO_ORIGEN = "CFDLZELAVON20110104.TXT";
	public final String NOMBRE_BASE_PARA_EL_FICHERO_DESTINO = "proceso";
	public final String EXTENSION_DEL_FICHERO_DESTINO = "txt";
	public final String CODIFICACION = "utf8";
	public final String RUTA_COMPLETA_A_FICHERO = RUTA_BASE + NOMBRE_DEL_FICHERO_ORIGEN;
	public final int TAMANO_DEL_BUFFER = 128 * 1024; // 128Kb
	public final int NUMERO_DE_LINEAS_EN_UN_GRUPO = 100;
	public final int NUMERO_MAXIMO_DE_PROCESOS = 40;
	private int idDeProceso;

	public RoundRobinOri() {
		idDeProceso = 0;
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		RoundRobinOri a = new RoundRobinOri();
		a.leerFichero();
	}

	public void leerFichero() throws FileNotFoundException, IOException {

		FileInputStream f = new FileInputStream(RUTA_COMPLETA_A_FICHERO);
		FileChannel ch = f.getChannel();
		byte[] barray = new byte[TAMANO_DEL_BUFFER];
		ByteBuffer bb = ByteBuffer.wrap(barray);
		long numeroDeLineas = 0;
		int nRead;
		long horaDeInicio = (new Date()).getTime();
		long inicioDelBloque = 0;
		long finDelBloque = 0;
		long posicionEnFichero = 0;

		while ((nRead = ch.read(bb)) != -1) {
			for (int i = 0; i < nRead; i++, posicionEnFichero++) {
				if (barray[i] == '\n') {
					numeroDeLineas++;
					if (numeroDeLineas == NUMERO_DE_LINEAS_EN_UN_GRUPO) {
						numeroDeLineas = 0;
						finDelBloque = posicionEnFichero;
						guardarBloque(inicioDelBloque, finDelBloque);
						inicioDelBloque = finDelBloque + 1;
					}
				}
			}
			finDelBloque = posicionEnFichero;
			guardarBloque(inicioDelBloque, finDelBloque);
			bb.clear();
		}
		long horaDeFinalizacion = (new Date()).getTime();
		System.out.println("duracion: " + (horaDeFinalizacion - horaDeInicio));
	}

	private void guardarBloque(long inicio, long fin)
		throws UnsupportedEncodingException, FileNotFoundException, IOException {
		String idDeProcesoConPadding = "0000" + idDeProceso;
		idDeProcesoConPadding = idDeProcesoConPadding.substring(idDeProcesoConPadding.length() - 5);
		String nombreDeFichero =
			RUTA_BASE +
			NOMBRE_BASE_PARA_EL_FICHERO_DESTINO +
			idDeProcesoConPadding + "." + EXTENSION_DEL_FICHERO_DESTINO;
		FileOutputStream fos = new FileOutputStream(nombreDeFichero, true);
		Writer out = new OutputStreamWriter(fos, CODIFICACION);
		try {
			String textoAGuardar = 
				(new Date()).getTime() + "|" +
				RUTA_BASE + NOMBRE_DEL_FICHERO_ORIGEN + "|" +
				inicio + "|" +
				fin + System.getProperty("line.separator");
			out.write(textoAGuardar);
		} finally {
			idDeProceso++;
			if (idDeProceso >= NUMERO_MAXIMO_DE_PROCESOS) {
				idDeProceso = 0;
			}
			out.close();
		}
	}
}
