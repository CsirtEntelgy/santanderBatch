package com.interfactura.firmalocal.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import com.interfactura.firmalocal.xml.util.FiltroParam;

public class RoundRobinJL {

	private Logger logger = Logger.getLogger(RoundRobin.class);
	private int idDeProceso;

	public RoundRobinJL() {
		idDeProceso = 0;
	}

	public static void main(String[] args) throws FileNotFoundException,
			IOException {
		RoundRobinJL a = new RoundRobinJL();

		a.leerFichero(args[0], Integer.parseInt(args[1]),
				Integer.parseInt(args[2]), Integer.parseInt(args[3]),
				args[4], args[5], args[6]);
	}

	/**
	 * 
	 * @param inputPath
	 * @param tam
	 * @param lineasPorArchivo
	 * @param numeroProcesos
	 * @param nombreBase
	 * @param ext
	 * @param pathConfiguration
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void leerFichero(String inputPath, int tam, int lineasPorArchivo,
			int numeroProcesos, String nombreBase, String ext, String pathConfiguration)
			throws FileNotFoundException, IOException {
		File iPath = new File(inputPath);
		if (!iPath.isDirectory()) {
			throw new IOException(inputPath + " no es un directorio.");
		}
		
		FiltroParam filter=new FiltroParam(
				new String[]{"XML","INC","backUp","CFDLZELAVON","CFDOPOPICS","CFDCONFIRMINGFACTURAS"});
		
		String[] filesInPath = iPath.list(filter);
		String fichero=null;
		
		if (filesInPath != null) {
			File fileConfiguration=new File(pathConfiguration);
			fileConfiguration.delete();
			for (int i = 0; i < filesInPath.length; i++) {
				File file = new File(filesInPath[i]);
				if (file.isDirectory()) {
					continue;
				}
				logger.info("Iniciando proceso de archivo: "
						+ filesInPath[i]);
				if (File.separatorChar == '/') {
					fichero=inputPath+filesInPath[i];
				} else {
					fichero=inputPath.replace("\\", "\\\\")+ filesInPath[i];
				}
				FileInputStream f = new FileInputStream(inputPath + file);
				FileChannel ch = f.getChannel();
				byte[] barray = new byte[tam * 1024 * 1024];
				ByteBuffer bb = ByteBuffer.wrap(barray);
				long numeroDeLineas = 0;
				int nRead=0;
				long horaDeInicio = (new Date()).getTime();
				long inicioDelBloque = 0;
				long finDelBloque = 0;
				long posicionEnFichero = 0;

				long ordenDelBloque = 0;
				while ((nRead = ch.read(bb)) != -1) {
					for (int j = 0; j < nRead; j++, posicionEnFichero++) {
						if (barray[j] == '\n') {
							numeroDeLineas++;
							if (numeroDeLineas == lineasPorArchivo) {
								numeroDeLineas = 0;
								finDelBloque = posicionEnFichero;
								
								guardarBloque(inicioDelBloque,
										finDelBloque, ordenDelBloque,
										fichero, numeroProcesos, nombreBase, 
										ext);
								inicioDelBloque = finDelBloque + 1;
								ordenDelBloque++;
							}
						}
					}
					finDelBloque = posicionEnFichero;

					guardarBloque(inicioDelBloque, finDelBloque,
							ordenDelBloque, fichero, numeroProcesos,
							nombreBase, ext);
					bb.clear();
				}
				
				if(fichero!=null){
					FileOutputStream confg = new FileOutputStream(pathConfiguration, true);
					Writer outConf = new OutputStreamWriter(confg, "UTF-8");
					outConf.write(Calendar.getInstance().getTime().getTime()+"|"+fichero+"|"+(ordenDelBloque+1));
					outConf.write(System.getProperty("line.separator"));
					outConf.close();
				}

				long horaDeFinalizacion = (new Date()).getTime();
				logger.info("Terminando proceso de archivo: "
						+ filesInPath[i]);
				logger.info("Duracion: "
						+ (horaDeFinalizacion - horaDeInicio));
			}
		}
	}

	/**
	 * 
	 * @param inicio
	 * @param fin
	 * @param ordenDelBloque
	 * @param fichero
	 * @param numeroProcesos
	 * @param nombreBase
	 * @param extension
	 * @throws UnsupportedEncodingException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void guardarBloque(long inicio, long fin, long ordenDelBloque,
			String fichero, int numeroProcesos, String nombreBase,
			String extension) throws UnsupportedEncodingException,
			FileNotFoundException, IOException {
		
		String idDeProcesoConPadding = "0000" + idDeProceso;
		idDeProcesoConPadding = idDeProcesoConPadding
				.substring(idDeProcesoConPadding.length() - 5);
		String nombreDeFichero = nombreBase + idDeProcesoConPadding + "."
				+ extension;
		logger.info("Nombre del Fichero: "+nombreDeFichero);
		FileOutputStream fos = new FileOutputStream(nombreDeFichero, true);
		Writer out = new OutputStreamWriter(fos, "UTF-8");
		try {
			String textoAGuardar = (new Date()).getTime() + "|" + fichero + "|"
					+ inicio + "|" + fin + "|" + ordenDelBloque
					+ System.getProperty("line.separator");

			out.write(textoAGuardar);
			
		} finally {
			idDeProceso++;
			if (idDeProceso >= numeroProcesos) {
				idDeProceso = 0;
			}
			out.close();
			fos.close();
		}
	}
	
}
