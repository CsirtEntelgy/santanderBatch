package com.interfactura.firmalocal.dao;

import java.util.List;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.domain.entities.Page;
import com.interfactura.firmalocal.domain.entities.Permission;

public interface PermissionDao extends Dao<Long, Permission>{
	
	 List<Page> listAllPages();
	
	 List<Permission> listar();
	
	 List<Permission> listar(int inicio, int cantidad, Filters<Filter> filters);

	 Permission findByName(String name);

	
}
