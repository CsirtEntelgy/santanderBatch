package com.interfactura.firmalocal.dao;

import java.util.List;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.domain.entities.CFDFieldsV22;

/**
 * Interfaz para persistir la tabla CFDFieldsV22.
 * 
 * @author lortiz
 * 
 */

public interface CFDFieldsV22Dao extends Dao<Long, CFDFieldsV22> {
	
	List<CFDFieldsV22> listAll();
	
	List<CFDFieldsV22> list(int begin, int quantity, Filters<Filter> filters, String feids);
	
	CFDFieldsV22 findByFiscalId(long id);
}
