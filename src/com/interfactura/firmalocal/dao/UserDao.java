package com.interfactura.firmalocal.dao;

import java.util.List;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.domain.entities.FiscalEntity;
import com.interfactura.firmalocal.domain.entities.Role;
import com.interfactura.firmalocal.domain.entities.User;

public interface UserDao extends Dao<Long, User>{
	
	 List<Role> listAllRoles();
	
	 List<FiscalEntity> listAllFiscalEntity();
	
	 List<User> listar();
	
	 List<User> listar(int inicio, int cantidad, Filters<Filter> filters);

	 User findByName(String userName);

}
