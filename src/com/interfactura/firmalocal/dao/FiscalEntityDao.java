package com.interfactura.firmalocal.dao;

import java.util.List;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.domain.entities.FiscalEntity;
import com.interfactura.firmalocal.domain.entities.State;

public interface FiscalEntityDao extends Dao<Long, FiscalEntity>{

	List<State> listAllStates();
	
	List<FiscalEntity> listar(int inicio, int cantidad, Filters<Filter> filters);

	List<FiscalEntity> listar();
	
	FiscalEntity get(String rfc);
	
	FiscalEntity findByRFCA(FiscalEntity fiscalEntity);
	
	List<FiscalEntity> listAllFiscalEntity( String ids );
	
}
