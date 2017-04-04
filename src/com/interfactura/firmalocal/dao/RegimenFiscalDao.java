package com.interfactura.firmalocal.dao;

import java.util.List;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.Dao;
import com.interfactura.firmalocal.domain.entities.RegimenFiscal;

/**
 * Interfaz con las operaciones de la capa de persistencia para la entidad
 * RegimenFiscal.
 * 
 * @author hlara
 * 
 */
public interface RegimenFiscalDao extends Dao<Long, RegimenFiscal> {

	List<RegimenFiscal> listar();
	
	List<RegimenFiscal> listar(int inicio, int cantidad, Filters<Filter> filters);
	
	RegimenFiscal findByName(String name);

	RegimenFiscal findByCode(String code);

}
