package com.interfactura.firmalocal.dao;

import java.util.List;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.domain.entities.CFDIssued;

public interface CfdIssuedDao extends Dao<Long, CFDIssued>{
	
	List<CFDIssued> list (int begin, int quantity, Filters<Filter> filters);
	
	List<CFDIssued> list (int begin, int quantity, Filters<Filter> filters, String ids);
	
	List<CFDIssued> list (int year, int month, String rfc);
	
	List<CFDIssued> listCancel(int year, int month, String rfc);
	
	List<CFDIssued> list(String nameFile);
	
    CFDIssued getByFolioSat(String folioSat);
}
