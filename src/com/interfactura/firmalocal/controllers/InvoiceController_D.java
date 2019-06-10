package com.interfactura.firmalocal.controllers;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.validation.ValidatorHandler;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.interfactura.firmalocal.domain.entities.CFDFieldsV22;
import com.interfactura.firmalocal.domain.entities.CodigoISO;
import com.interfactura.firmalocal.domain.entities.Customer;
import com.interfactura.firmalocal.domain.entities.FiscalEntity;
import com.interfactura.firmalocal.domain.entities.Iva;
import com.interfactura.firmalocal.domain.entities.Moneda;
import com.interfactura.firmalocal.domain.entities.SealCertificate;
import com.interfactura.firmalocal.persistence.CFDFieldsV22Manager;
import com.interfactura.firmalocal.persistence.CodigoISOManager;
import com.interfactura.firmalocal.persistence.CustomerManager;
import com.interfactura.firmalocal.persistence.FiscalEntityManager;
import com.interfactura.firmalocal.persistence.IvaManager;
import com.interfactura.firmalocal.persistence.MonedaManager;
import com.interfactura.firmalocal.persistence.OpenJpaManager;
import com.interfactura.firmalocal.xml.Properties;
import com.interfactura.firmalocal.xml.ecb.GeneraXML_ECB_DI;
import com.interfactura.firmalocal.xml.util.NombreAplicativo;


/**
 * @author Equipo
 *
 */
@Controller
public class InvoiceController_D {

	private Logger logger = Logger.getLogger(InvoiceController_D.class);
	
	
    @Autowired(required=true)
	private FiscalEntityManager fiscalEntityManager;
    @Autowired(required = true)
	private CFDFieldsV22Manager cfdFieldsV22Manager;
	@Autowired(required = true)
	private CustomerManager customerManager;
	@Autowired(required = true)
	private OpenJpaManager openJpaManager;
	@Autowired(required = true)
	private MonedaManager monedaManager;
	@Autowired(required = true)
	private CodigoISOManager codigoISOManager;
	@Autowired
	private IvaManager ivaManager;
	@Autowired
	private GeneraXML_ECB_DI xmlDivisasV3;
	
	@Autowired
	private Properties properties;
	private int contFiles;

	
	
	public synchronized void processingInvoices(String idProceso, String fileNames, String fecha, String urlWebService, String numeroMalla)	{
		
		
		
		
		String path=properties.getConfigurationPath();
		
		String zeros="";
		if(!idProceso.equals("-1"))
		{
			int max=5-idProceso.length();
			for(int c=0;c<max;c++)
			{	zeros+="0";	}
			path+=zeros+idProceso+".txt";
		}

		String args[]=null;
		LineNumberReader reader=null;
		File fileout=null;
		String line =null;
		File file = new File(path);
		contFiles = 0;
		long byteStart=-1;
		long byteEnd=-1;
		long cont=-1;
		
		
		//Crear hashMaps
		HashMap<Integer, String> hashIvas = new HashMap<Integer, String>();
		for(Iva iva:ivaManager.listar()){
			 hashIvas.put(iva.getTasa(), iva.getDescripcion());
			}
		
		HashMap<String, String> hashcodigoISO = new HashMap<String, String>();
		for(CodigoISO codigoiso:codigoISOManager.listar()){
			 hashcodigoISO.put(codigoiso.getCodigo(), codigoiso.getDescripcion());
			}
		
		HashMap<String, String> hashmoneda = new HashMap<String, String>();
		for(Moneda moneda:monedaManager.listar()){
			 hashmoneda.put(moneda.getNombreCorto(), moneda.getNombreLargo());
			}
		
		HashMap<String,FiscalEntity> hashEmisores = new HashMap<String,FiscalEntity>(); 
		for(FiscalEntity fE:fiscalEntityManager.listar()){
			FiscalEntity fENew = fE;
			hashEmisores.put(fENew.getTaxID(), fENew);
		}
		
		HashMap<String,Customer> hashClientes = new HashMap<String,Customer>();
		for(Customer customer:customerManager.listar()){
			Customer customerNew = customer;
			
			if(customerNew.getTaxId().toUpperCase().trim().equals("XEXX010101000") || customerNew.getTaxId().toUpperCase().trim().equals("XAXX010101000")){
				//Cliente extranjero
				hashClientes.put(customerNew.getIdExtranjero(), customerNew);
			}else{
				//Cliente normal
				hashClientes.put(customerNew.getTaxId()+customerNew.getFiscalEntity().getId(), customerNew);
			}
			
		}
		
		HashMap<Long,String> hashCfdFieldsV22 = new HashMap<Long,String>();
		for(CFDFieldsV22 cfdV22:cfdFieldsV22Manager.listAll()){
			String strLugarExp = "";
			String strRegFiscal = "";
			if(cfdV22.getRegimenFiscal()!=null){
				if(cfdV22.getRegimenFiscal().getName()!=null){
					strRegFiscal = cfdV22.getRegimenFiscal().getName(); 
				}
				if(cfdV22.getLugarDeExpedicion()!=null){
					strLugarExp = cfdV22.getLugarDeExpedicion(); 
				}
			}
			hashCfdFieldsV22.put(cfdV22.getFiscalEntity().getId(), strRegFiscal+"|"+strLugarExp);
		}
		
		
		try {
			
			System.out.println("Procesando Divisas por Tareas: "+idProceso);
			
			if(file.exists()&&file.length()>0)
			{
				reader = new LineNumberReader(new FileReader(file));
	            line = null;

	            while ((line = reader.readLine()) != null) 	{
	            	
					args=line.split("\\|");
					if(args!=null&&args.length>=5)	{	

						byteStart = Long.parseLong(args[2]);
						byteEnd = Long.parseLong(args[3]);
						cont = Long.parseLong(args[4]);
				    	
						this.processing(hashIvas, 
								hashcodigoISO, 
								hashmoneda, 
				    			hashEmisores, 
				    			hashClientes, 
				    			hashCfdFieldsV22, 
				    			byteStart, 
				    			byteEnd, 
				    			args[1], 
				    			cont, 
				    			idProceso, 
				    			fecha, 
				    			fileNames, 
				    			urlWebService, 
				    			numeroMalla);
					}

				}
	            					
	            reader.close();
	            
			}
			
		} catch ( Exception e ) {
			
			e.printStackTrace();
			logger.error(e.getLocalizedMessage().replace("ORA-", "ORACLE-"));
			System.exit(-1);
			
		}
		
		
		
	}
	
	
	
	/**
	 * @param hashIvas
	 * @param hashcodigoISO
	 * @param hashmoneda
	 * @param hashEmisores
	 * @param hashClientes
	 * @param hashCfdFieldsV22
	 * @param byteStart
	 * @param byteEnd
	 * @param path
	 * @param cont
	 * @param idProceso
	 * @param fecha
	 * @param fileNames
	 * @param urlWebService
	 * @param numeroMalla
	 * @throws Exception
	 */
	public void processing(HashMap<Integer, String> hashIvas,
			HashMap<String, String> hashcodigoISO,
			HashMap<String, String> hashmoneda,
			HashMap<String,FiscalEntity> hashEmisores, 
			HashMap<String,Customer> hashClientes, 
			HashMap<Long,String> hashCfdFieldsV22, 
			long byteStart, 
			long byteEnd, 
			String path,
			long cont, 
			String idProceso, 
			String fecha, 
			String fileNames, 
			String urlWebService, 
			String numeroMalla) throws Exception {
		
		File file = new File(path);
		boolean versionTypo = true; // Tipo version AMDA
		if(file.length()>0) {
			
			logger.debug("El archivo a procesar es: " + path);
			xmlDivisasV3.setHashCfdFieldsV2(hashCfdFieldsV22);
			xmlDivisasV3.setHashClientes(hashClientes);
			xmlDivisasV3.setHashcodigoISO(hashcodigoISO);
			xmlDivisasV3.setHashIvas(hashIvas);
			xmlDivisasV3.setHashmoneda(hashmoneda);
			xmlDivisasV3.setUrlWSTimbrado(urlWebService);
			xmlDivisasV3.setNameFile(file.getName());
			xmlDivisasV3.setNombresApps(NombreAplicativo.cargaNombresApps());
			xmlDivisasV3.convierte(byteStart, byteEnd, path, cont, idProceso, fecha, fileNames, numeroMalla);
	
		}
	}
	
	
	

	
	
}
