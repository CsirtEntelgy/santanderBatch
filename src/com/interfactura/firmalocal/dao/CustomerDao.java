package com.interfactura.firmalocal.dao;

import java.util.List;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.domain.entities.Customer;
import com.interfactura.firmalocal.domain.entities.FiscalEntity;
import com.interfactura.firmalocal.domain.entities.State;

public interface CustomerDao extends Dao<Long, Customer>{

	 List<State> listAllStates();
	
	 List<FiscalEntity> listAllFiscalEntity(String ids);
	
	 List<Customer> listar(int inicio, int cantidad, Filters<Filter> filters, String ids);

	 Customer findByNumberAndFiscalEntity(Customer customer);

	 Customer findByRFCAndFiscalEntity(Customer customer);
	 
	 Customer findByIdExtranjero(String idExtranjero);

	 Customer get(String rfc);
	 
	 Customer get(String rfc, String feId);
	 
	 Customer get(String rfc, String feId, String idExtranjero);
	
	 List<Customer> listar();
}

