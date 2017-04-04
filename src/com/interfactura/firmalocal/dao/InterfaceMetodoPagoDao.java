package com.interfactura.firmalocal.dao;

import java.util.List;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.domain.entities.CFDFieldsV22;
import com.interfactura.firmalocal.domain.entities.InterfaceMetodoPago;

/**
 * Interfaz para persistir la tabla CFDFieldsV22.
 * 
 * @author lortiz
 * 
 */

public interface InterfaceMetodoPagoDao extends Dao<Long, InterfaceMetodoPago> {
	
	List<InterfaceMetodoPago> listAll();
	
	List<InterfaceMetodoPago> list(int begin, int quantity, Filters<Filter> filters, String feids);
	
	List<InterfaceMetodoPago> listExcel(int begin, int quantity, Filters<Filter> filters, String feids);
		
	InterfaceMetodoPago findByFiscalId(long id);
	
	List<InterfaceMetodoPago> listByColumn(String nomInterface);
}
