package com.interfactura.firmalocal.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.interfactura.firmalocal.domain.entities.FiscalEntity;
import com.interfactura.firmalocal.persistence.FiscalEntityManager;
import com.interfactura.firmalocal.persistence.IvaManager;
import com.interfactura.firmalocal.xml.ecb.CargaOracleBatch;

@Controller
public class CargaOracleController 
{
	@Autowired
	private CargaOracleBatch cargaOracle;
	@Autowired
	public FiscalEntityManager fiscal;
	@Autowired
	public IvaManager ivaManager;
	private Logger logger = Logger.getLogger(CargaOracleController.class);

	public synchronized void processOracle(String fileNames, String fecha) 
		throws IOException 
	{
		HashMap<String, FiscalEntity> setF = new HashMap<String, FiscalEntity>();
		List<FiscalEntity> lstFiscal = fiscal.listar();
		for (FiscalEntity obj : lstFiscal) 
		{	setF.put(obj.getId() + "", obj);	}

		cargaOracle.setNameFile(fileNames);
		cargaOracle.setoDate(fecha);
		
		cargaOracle.cargaOracle(setF);
	}
	
}