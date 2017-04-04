package com.interfactura.firmalocal.dao;

import java.util.List;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.domain.entities.CodigoISO;

public interface CodigoISODao extends Dao<Long, CodigoISO> {
	List<CodigoISO> listar(int inicio, int cantidad, Filters<Filter> filters);
	
	List<CodigoISO> listar();
	
	CodigoISO findByDescripcion(String descripcion);
	
	CodigoISO findByCodigo(String codigo);
}
