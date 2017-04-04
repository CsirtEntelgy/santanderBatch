package com.interfactura.firmalocal.dao;

import java.util.List;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.domain.entities.Folio;

public interface FolioDao extends Dao<Long, Folio>{
	
	List<Folio> list(int begin, int quantity, Filters<Filter> filters);

}
