package com.interfactura.firmalocal.dao;

import java.util.List;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.domain.entities.Iva;

public interface IvaDao extends Dao<Long, Iva>{
	
	List<Iva> list(int begin, int quantity, Filters<Filter> filters);

	Iva findByDescription(String descripcion);
	
	Iva findByTasa(String tasa);
}
