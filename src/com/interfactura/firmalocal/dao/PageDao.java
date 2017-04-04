package com.interfactura.firmalocal.dao;

import java.util.List;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.domain.entities.Page;

public interface PageDao extends Dao<Long, Page>{
	
	List<Page> list(int begin, int quantity, Filters<Filter> filters);

}
