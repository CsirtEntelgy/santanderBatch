package com.interfactura.firmalocal.dao;

import java.util.List;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.domain.entities.Moneda;

public interface MonedaDao extends Dao<Long, Moneda>{
	
	List<Moneda> list(int begin, int quantity, Filters<Filter> filters);

	Moneda findByName(String name);
}
