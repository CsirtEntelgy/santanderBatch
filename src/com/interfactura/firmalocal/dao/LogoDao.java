package com.interfactura.firmalocal.dao;

import java.util.Date;
import java.util.List;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.domain.entities.Logo;


public interface LogoDao extends Dao<Long, Logo> {

	List<Logo> listAll();
	
	Logo getByFEId(long id, Date fecha);
	
	List<Logo> list(int begin, int quantity, Filters<Filter> filters, String feids);
}
