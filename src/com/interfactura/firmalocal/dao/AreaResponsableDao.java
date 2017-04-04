package com.interfactura.firmalocal.dao;

import java.util.List;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.domain.entities.AreaResponsable;

public interface AreaResponsableDao extends Dao<Long, AreaResponsable> {
	List<AreaResponsable> listar(int inicio, int cantidad, Filters<Filter> filters);
	
	List<AreaResponsable> listar();
	
	AreaResponsable findByNombre(String nombre);
	
	List<AreaResponsable> listarAreas() throws Exception;
	
}