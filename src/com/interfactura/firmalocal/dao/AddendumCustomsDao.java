package com.interfactura.firmalocal.dao;

import java.util.List;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.domain.entities.AddendumCustoms;

public interface AddendumCustomsDao extends Dao<Long, AddendumCustoms>{
	
	 List<AddendumCustoms> listar(int inicio, int cantidad, Filters<Filter> filters);

}
