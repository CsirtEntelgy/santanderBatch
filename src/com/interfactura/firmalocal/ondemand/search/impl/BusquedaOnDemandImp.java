package com.interfactura.firmalocal.ondemand.search.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import com.ibm.edms.od.ODConfig;
import com.ibm.edms.od.ODConstant;
import com.ibm.edms.od.ODFolder;
import com.ibm.edms.od.ODHit;
import com.ibm.edms.od.ODServer;
import com.interfactura.firmalocal.ondemand.search.BusquedaOnDemand;

public class BusquedaOnDemandImp implements BusquedaOnDemand {
	
	/**
	 *  Constantes de Criterios de Busqueda.
	 */
	private enum CriterioBusqueda {
		RFC_EMISOR("RFC_EMISOR"), CONTRATO("CONTRATO"), CODIGO_CLIENTE(
				"CODIGO_CLIENTE"), PERIODO("PERIODO"), FECHA_CFD("FECHA_CFD"), SERIE_FISCAL_CFD(
				"SERIE_FISCAL_CFD"), FOLIO_FISCAL_CFD("FOLIO_FISCAL_CFD"),FECHA("FECHA"), 
				TARJETA("TARJETA"), CUENTA("CUENTA"), NUM_CLIENTE("NUM_CLIENTE"), 
				FECHA_GENERACION("FECHA_GENERACION"), FolioSAT("FolioSAT");
		private String value;
		private CriterioBusqueda(String value) {
			this.value = value;
		}
	}

	/**
	 *  Variables de Configuracion del Servidor.
	 */
	private String serverName;
	private String userName;
	private String password;

	/**
	 *  Variables de Configuracion de la Aplicacions.
	 */
	private String folderNameInterEmision;
	private String folderNameInterRecepcion;
	private String folderNameInterEstadoCuenta;

	/**
	 * Variables para Configuracion de Almacenamiento.
	 */
	private String rutaArchivo;
	private String nombreArchivo;
	private String extencionArchivo;

    private static final String RE_UUID = "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}";
	
	/**
	 * Constructor de la clase, inicializa las variables de conexion.
	 * @param args Datos de configuracion para servidor de onDemand.
	 */
	public BusquedaOnDemandImp(String ...args){
		this.serverName=args[0];
		this.userName=args[1];
		this.password=args[2];
		this.folderNameInterEmision=args[3];
		this.folderNameInterRecepcion=args[4];
		this.folderNameInterEstadoCuenta=args[5];
		this.rutaArchivo=args[6];
		this.nombreArchivo=args[7];
		this.extencionArchivo=args[8];
	}
	/** 
     * Establece la conexion a onDemand con el usuario y password.
     * @return El objeto ODServer con la conexion.
     */
	public ODServer connectOnDemad() throws Exception{
		System.out.println("datos conexion: " + serverName + " " + userName + " " + password);
		
		ODConfig cfg = new ODConfig();
		ODServer serv = new ODServer(cfg);
		serv.initialize("link");
		//serv.initialize("");		
		serv.logon(serverName, userName, password);
		serv.setConnectType(ODConstant.CONNECT_TYPE_TCPIP);
		//serv.setConnectType(ODConstant.CONNECT_TYPE_LOCAL);
		return serv;
	}
	
	/** 
     * Finaliza la conexion establecida previamente.
     * @param serv El objeto con la conexion.
     */
	public void disconnectOnDemand(ODServer serv) throws Exception{
		try {
			if (serv != null)
				serv.logoff();
		} catch (Exception e) {
			System.out.println("Error : "+e);
			e.printStackTrace();
			throw e;
		}
	}
        
    private String escribirArchivoCifras(ODHit hit, String strFolioSat) throws Exception {
        File file = null;
        String strPath = "";
        BufferedReader docString = null;
        Writer output = null;
        try {
            if (hit != null) {
                byte[] docByte = hit.retrieve(ODConstant.NATIVE);
                String s1 = new String(docByte, "ISO-8859-1");
                byte[] utf8 = s1.getBytes("UTF-8");

                ByteArrayInputStream flujo = new ByteArrayInputStream(utf8);
                InputStreamReader leer = new InputStreamReader(flujo, "UTF-8");
                docString = new BufferedReader(leer);

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = docString.readLine()) != null) {
                    sb.append(line);
                }
                String xml = sb.toString();
                ByteArrayInputStream flujo2 = new ByteArrayInputStream(xml.getBytes("ISO-8859-1"));
                InputStreamReader leer2 = new InputStreamReader(flujo2, "UTF-8");
                BufferedReader docString2 = new BufferedReader(leer2);

                Date fechaActual = new Date();
                SimpleDateFormat formato = new SimpleDateFormat("MMddyyHH:mm:ss");
                String cadenaFecha = formato.format(fechaActual);
                /*String folioSat = hit.getDisplayValue(CriterioBusqueda.FolioSAT.toString());
                String idCFDI = "";
                if (folioSat != null && folioSat.trim().length() > 0 && folioSat.matches(RE_UUID)) {
                    idCFDI = "CFDI";
                }*/
                int numRandom = (int) Math.floor(Math.random() * 1000000);
                file = new File(rutaArchivo + nombreArchivo + strFolioSat + "_" + cadenaFecha + numRandom + extencionArchivo);

                output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
                output.write(docString2.readLine());
                strPath = rutaArchivo + nombreArchivo + strFolioSat + "_" + cadenaFecha + numRandom + extencionArchivo;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("escribirArchivoReporte exception:" + e.getMessage());
        } finally {
            try {
                if (docString != null) {
                    docString.close();
                }
                if (output != null) {
                    output.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //return file;
        return strPath;
    }
	
    
    /**
     * Metodo para consultar facturas en la aplicacion de InterfacturaEmision en onDemand.
     * 
     * @param folioSat      UUID
     * @return Regresa una lista de rutas de archivos resultado de la consulta.
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<String> busquedaInterfacturaEmisionCFDICifras(long maxHits, String strPathFolioSat, ODFolder folderObtenido, String prefixXml) throws Exception {
    	
        List<String> listFiles = new ArrayList<String>();
        Vector<ODHit> hits;
        //ODServer serv = null;
        try {
            //serv = connectOnDemad();
            //ODFolder folderObtenido = serv.openFolder(folderNameInterEmision);
            //if (maxHits > 0) {
            //    folderObtenido.setMaxHits((int) maxHits);// Se modifica num max respuesta
            //}

            /************************************/
        	
        	FileInputStream fStream = null;
        	DataInputStream dInput = null;
    		BufferedReader bReader = null;
    		
    		fStream = new FileInputStream(strPathFolioSat);			
    		dInput = new DataInputStream(fStream);
    		bReader = new BufferedReader(new InputStreamReader(dInput));
    		
    		String line = "";
    		
    		while(((line = bReader.readLine()) != null)){
    			if(line.trim().length()>0){
    				        			 
        			//List<File> files = null;
        			//List<File> files = searchCFDOndemand(valuesCfd);
        			StringBuilder whereClause = new StringBuilder("WHERE ");
                    
        			whereClause.append(CriterioBusqueda.FolioSAT.toString() + "='" + line.trim() + "' OR ");
        			
                    System.out.println("whereOndemand:" + whereClause.toString().substring(0, whereClause.toString().length() - 4));
                    String strPath = "";
                    hits = folderObtenido.search(whereClause.toString().substring(0, whereClause.toString().length() - 4));
                    
                    if (!hits.isEmpty()) {
                        //files = new ArrayList<File>();
                        for (ODHit hit : hits) {
                            //files.add(escribirArchivoReporte(hit));
                        	strPath = escribirArchivoCifras(hit, line.trim());
                        }
                    } else {
                        System.out.println("BusquedaOnDemand: No se encontraron registros.");
                    }
                    
                    //filesOndemand.setValuesCfd(valuesCfd);
                    //filesOndemand.setFiles(files);
                    
                    listFiles.add(strPath);
    			}
    		}
    		
    		if(bReader != null)
    			bReader.close();
    		
    		if(dInput != null)
    			dInput.close();
    		
    		if(fStream != null)
    			fStream.close();
    		           
            /***********************************/
            
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            //disconnectOnDemand(serv);
        }
        return listFiles;
    }

}
