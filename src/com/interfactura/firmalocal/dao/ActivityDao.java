package com.interfactura.firmalocal.dao;

import java.util.List;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.domain.entities.Activity;

public interface ActivityDao extends Dao<Long, Activity>{
	
	List<Activity> list(int begin, int quantity, Filters<Filter> filters);

}
