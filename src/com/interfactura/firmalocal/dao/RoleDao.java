package com.interfactura.firmalocal.dao;

import java.util.List;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.domain.entities.Role;

public interface RoleDao extends Dao<Long, Role>{
	
	 List<Role> listar();
	
	 List<Role> listar(int inicio, int cantidad, Filters<Filter> filters);

	 Role findByName(String name);

}
