package com.interfactura.firmalocal.controllers;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.interfactura.firmalocal.domain.entities.FiscalEntity;
import com.interfactura.firmalocal.persistence.CFDIssuedManager;
import com.interfactura.firmalocal.persistence.FiscalEntityManager;

@Controller
public class CFDIssuedController {
	@Autowired(required = true)
	private CFDIssuedManager cFDIssuedManager;
	@Autowired(required = true)
	private FiscalEntityManager fiscal;
	public CFDIssuedController() {
	}
	public void reporte(int year, int month) {		
		try {
			String nombreBase = "/planCFD/procesos/Interfactura/interfaces/";
			//Get List FiscalEntity
			List<FiscalEntity> lstFiscal = fiscal.listar();
			for(FiscalEntity obj : lstFiscal){			
				String nameReporte = 1 + obj.getTaxID() + (month<10 ? "0" + month : month)
					+ (year < 10 ? "0" + 9 : year);								
				OutputStream outputStream = new FileOutputStream (nombreBase + nameReporte + ".TXT");				
				cFDIssuedManager.monthReport(year, month, obj.getTaxID()).writeTo(outputStream);							
				outputStream.close();
			}		
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
}
