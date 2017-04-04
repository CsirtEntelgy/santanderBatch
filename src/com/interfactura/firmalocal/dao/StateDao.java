package com.interfactura.firmalocal.dao;

import java.util.List;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.domain.entities.Country;
import com.interfactura.firmalocal.domain.entities.State;

public interface StateDao extends Dao<Long, State>{
	
	 List<State> listar(int inicio, int cantidad, Filters<Filter> filters);

	 List<State> listar();
	
	 List<Country> listAllCountries();

	 State findByName(String name, long countryId);
	
	 State findByName(String name);

}
