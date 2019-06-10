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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.interfactura.firmalocal.xml.util.FiltroParam;

public class RoundRobin_D 
{
	 private Logger logger = Logger.getLogger(RoundRobin_D.class);
	  private int idDeProceso;
	  private int malla;
	
	  private HashMap<String, String> listProcesa = new HashMap<String, String>();
	  private List<String> listProcesoFile = new ArrayList<String>(); 
	  public RoundRobin_D()
	  {
	    this.idDeProceso = 0;
	  }
	
	  public RoundRobin_D(int malla, int numeroProcesos) {
	    this.idDeProceso = 0;
	    this.malla = malla;
	
	    if (malla != 1)
	      this.idDeProceso = (numeroProcesos * malla - numeroProcesos);
	  }
	
	  public static void main(String[] args)
	    throws FileNotFoundException, IOException
	  {
	    RoundRobin a = new RoundRobin(Integer.parseInt(args[7]), Integer.parseInt(args[3]));
	    a.leerFichero(
	    						args[0],
	    		Integer.parseInt(args[1]), 
	      Integer.parseInt(args[2]),
	      Integer.parseInt(args[3]), 
						      args[4],
						      args[5],
						      args[6],
						      args[8],
						      args[9]);
	    
	    /*GENERA TAREAS
	     * args[0] ruta hacia las interfaces
	     * args[1] tamano en MG a leer de los archivos
	     * args[2] numero de lineas por tarea 
	     * args[3] numero de procesos 
	     * args[4] ruta en donde se va a guardar el archivo de bloques
	     * args[5] extencion del archivo de bloques
	     * args[6] un archivo para concatenar archibos ??  /salidas/concatenar${5}.txt donde 5 es el numero de malla 
	     * args[8] fecha que se le paso al shell generaTareasDivisas como parametro
	     * args[9] nombre de la interfaz que se le pasa al shell ejm "CFDDIVISAS" 
	     * 
	     * */
	  }
	
	  public void leerFichero(
			  String inputPath,
			  int tam, 
			  int lineasPorArchivo, 
			  int numeroProcesos, 
			  String nombreBase, 
			  String ext,
			  String pathConfiguration, 
			  String fecha, 
			  String fileNames)
			  
			  
			  
	    throws FileNotFoundException, IOException
	  {
	    File iPath = new File(inputPath);
	    if (!iPath.isDirectory()) {
	      throw new IOException(inputPath + " no es un directorio.");
	    }
	    FiltroParam filter = new FiltroParam(
	      new String[] { "XML", "INC", "backUp", "CFDLZELAVON", "CFDOPOPICS", "CFDCONFIRMINGFACTURAS", "CFDFACTORAJEFACTURAS" });
	
	    String[] filesInPath = iPath.list(filter);
	    String fichero = null;
	
	    String[] fileNamesArr = fileNames.split(",");
	    for (int i = 0; i < fileNamesArr.length; i++) {
	      fileNamesArr[i] = (fileNamesArr[i] + fecha + ".TXT");
	    }
	    if (filesInPath != null)
	    {
	      File fileConfiguration = new File(pathConfiguration);
	      fileConfiguration.delete();
	      for (int i = 0; i < filesInPath.length; i++)
	      {
	        File file = new File(filesInPath[i]);
	        if (!file.isDirectory())
	        {
	          System.out.println(" ..archivo: " + filesInPath[i]);
	          boolean isOk = false;
	          for (int y = 0; y < fileNamesArr.length; y++)
	          {
	            System.out.println(" ....comparando con: " + fileNamesArr[y]);
	            if (filesInPath[i].equals(fileNamesArr[y]))
	            {
	              isOk = true;
	              break;
	            }
	          }
	          if (isOk)
	          {
	            this.logger.info("Iniciando proceso de archivo: " + filesInPath[i]);
	            if (File.separatorChar == '/') 
	              fichero = inputPath + filesInPath[i];
	            else
	              fichero = inputPath.replace("\\", "\\\\") + filesInPath[i];
	            FileInputStream f = new FileInputStream(inputPath + file);
	            FileChannel ch = f.getChannel();
	            byte[] barray = new byte[tam * 1024 * 1024];
	            ByteBuffer bb = ByteBuffer.wrap(barray);
	            long numeroDeLineas = 0L;
	            int nRead = 0;
	            long horaDeInicio = new Date().getTime();
	            long inicioDelBloque = 0L;
	            long finDelBloque = 0L;
	            long posicionEnFichero = 0L;
	            long ordenDelBloque = 0L;
	            
	            boolean readStart = true;
	            boolean readEnd = false;
	            char bStart = 0;
	            char bEnd = 0;
	            long numeroDeCFDs = 0L;
	            
	            while ((nRead = ch.read(bb)) != -1)
	            {
	              for (int j = 0; j < nRead; posicionEnFichero += 1L)
	              {
	            	if(readEnd){bEnd = (char)barray[j];readEnd = false;}
	                if(readStart){bStart = (char)barray[j];readStart = false;readEnd = true;}
	                  
	                if (barray[j] == 10)
	                {
	            	  if(String.valueOf(bStart).equals("0") && String.valueOf(bEnd).equals("1")){numeroDeCFDs += 1L;}
	        	      readStart = true;
	        	      readEnd = false;
	                  
	                  numeroDeLineas += 1L;
	                  if (numeroDeLineas == lineasPorArchivo)
	                  {                	
	                    numeroDeLineas = 0L;
	                    finDelBloque = posicionEnFichero;
	
	                    guardarBloque(inicioDelBloque, 
	                      finDelBloque, ordenDelBloque, 
	                      fichero, numeroProcesos, nombreBase, 
	                      ext, numeroDeCFDs);
	                    inicioDelBloque = finDelBloque + 1L;
	                    ordenDelBloque += 1L;
	                    
	                    numeroDeCFDs = 0L;
	                  }
	                }
	                j++;
	              }
	
	              bb.clear();
	            }
	
	            finDelBloque = posicionEnFichero;
	
	            guardarBloque(inicioDelBloque, finDelBloque, 
	              ordenDelBloque, fichero, numeroProcesos, 
	              nombreBase, ext, numeroDeCFDs);
	
	            //Recorrer listProcesa
	            System.out.println("listProcesa");
	            Iterator it = listProcesa.entrySet().iterator();
	            
	            while(it.hasNext()){
	            	Entry e = (Entry) it.next();
	            	System.out.println(e.getKey() + " - " + e.getValue());
	            	if(e.getKey() != null){
	            		if(!e.getKey().toString().isEmpty()){
	            			//Para cada proceso, crear archivo con el número de comprobantes que le toca procesar
	            			File archivoCFD = new File(inputPath + "totalCFD" + e.getKey().toString() + ".TXT");
	            			FileOutputStream salidaProcesa = new FileOutputStream(archivoCFD);
	            			salidaProcesa.write((e.getValue().toString() + "\r\n").getBytes("UTF-8"));
	            			
	            			salidaProcesa.close();            				            			
	            		}
	            	}
	            }

	            //Crear resto de archivos con el número de malla, en caso de no generar los n archivos proceso####.txt
	            System.out.println("idDeProceso:" + this.idDeProceso);
	            System.out.println("malla:" + this.malla);
	            System.out.println("numeroProcesos:" + numeroProcesos);	            	        	    
	            
	            System.out.println("numProcesoIni:" + (this.malla * numeroProcesos - 40));
	            System.out.println("numProcesoFin:" + (this.malla * numeroProcesos - 1));
	            
            	for(int numProcesoIni = this.malla * numeroProcesos - numeroProcesos;
            			numProcesoIni < this.malla * numeroProcesos; 
            			numProcesoIni++){
            		String idDeProcesoConPadding = "0000" + numProcesoIni;
	        	    idDeProcesoConPadding = idDeProcesoConPadding.substring(idDeProcesoConPadding.length() - 5);
	        	    
            		File archivoMalla = new File(inputPath + "numeroMalla" + idDeProcesoConPadding + ".TXT");
        			FileOutputStream salidaMalla = new FileOutputStream(archivoMalla);
        			salidaMalla.write((this.malla + "\r\n").getBytes("UTF-8"));
        			
        			salidaMalla.close();
            	}	            
	            
	            //////////////////////
	            
	            FileOutputStream confg = new FileOutputStream(pathConfiguration, true);
	            Writer outConf = new OutputStreamWriter(confg, "UTF-8");
	            outConf.write(Calendar.getInstance().getTime().getTime() + "|" + fichero + "|" + (ordenDelBloque + 1L));
	            outConf.write(System.getProperty("line.separator"));
	            outConf.close();
	
	            long horaDeFinalizacion = new Date().getTime();
	            this.logger.info("Terminando proceso de archivo: " + 
	              filesInPath[i]);
	            this.logger.info("Duracion: " + (
	              horaDeFinalizacion - horaDeInicio));
	          }
	        }
	      }
	    }
	  }
	
	  private void guardarBloque(long inicio, long fin, long ordenDelBloque, String fichero, int numeroProcesos, String nombreBase, String extension, long numeroDeCFDs)
	    throws UnsupportedEncodingException, FileNotFoundException, IOException
	  {
	    String idDeProcesoConPadding = "0000" + this.idDeProceso;
	    idDeProcesoConPadding = idDeProcesoConPadding
	      .substring(idDeProcesoConPadding.length() - 5);
	    
	    //Acumular el numero de comprobantes por cada procesa 
	    boolean existeProcesa = false;
	    
	    Iterator it = listProcesa.entrySet().iterator();
	    
	    while(it.hasNext()){
	    	Entry e = (Entry) it.next();
	    	if(e.getKey().equals(idDeProcesoConPadding)){
	    		long accumCfds = Long.valueOf(String.valueOf(e.getValue())) + numeroDeCFDs;
	    		listProcesa.put(idDeProcesoConPadding, String.valueOf(accumCfds));
	    		existeProcesa = true;
	    	}
	    }
	        
	    if(!existeProcesa){
	    	listProcesa.put(idDeProcesoConPadding, String.valueOf(numeroDeCFDs));
	    }
	    
	    String nombreDeFichero = nombreBase + idDeProcesoConPadding + "." + 
	      extension;
	    this.logger.info("Nombre del Fichero: " + nombreDeFichero);
	    
	    //Verificar si ya existe algun archivo proceso#####.txt, si es asi borrarlo antes de iniciar la escritura
	    boolean existProceso = false;
	    for(int iProceso=0; iProceso<listProcesoFile.size(); iProceso++){
	    	if(listProcesoFile.get(iProceso).equals(idDeProcesoConPadding)){
	    		existProceso = true;
	    		break;
	    	}
	    }
	    
	    if(!existProceso){
	    	listProcesoFile.add(idDeProcesoConPadding);
	    	File fileProceso = new File(nombreDeFichero);
	    	if(fileProceso.exists()){
	    		fileProceso.delete();
	    	}
	    }
	    
	    FileOutputStream fos = new FileOutputStream(nombreDeFichero, true);
	    Writer out = new OutputStreamWriter(fos, "UTF-8");
	    try
	    { 
	      String textoAGuardar = new Date().getTime() + "|" + fichero + "|" + 
	        inicio + "|" + fin + "|" + ordenDelBloque +
	        System.getProperty("line.separator");
	
	      out.write(textoAGuardar);
	      out.flush();
	    }
	    finally
	    {
	      this.idDeProceso += 1;
	      if (this.idDeProceso >= this.malla * numeroProcesos)
	      {
	        if (this.malla != 1)
	          this.idDeProceso = (numeroProcesos * this.malla - numeroProcesos);
	        else
	          this.idDeProceso = 0;
	      }
	      out.close();
	      fos.close();
	    }
	  }
}
