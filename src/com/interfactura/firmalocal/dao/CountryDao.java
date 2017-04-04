package com.interfactura.firmalocal.dao;

import java.util.List;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.domain.entities.Country;

public interface CountryDao extends Dao<Long, Country>{
	
	List<Country> listar(int inicio, int cantidad, Filters<Filter> filters);

	List<Country> listar();
	
	Country findByName(String name);

	Country findByCode(String code);

}
