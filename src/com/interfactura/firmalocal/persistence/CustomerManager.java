package com.interfactura.firmalocal.persistence;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.CustomerDao;
import com.interfactura.firmalocal.dao.FiscalEntityDao;
import com.interfactura.firmalocal.dao.StateDao;
import com.interfactura.firmalocal.domain.entities.Customer;
import com.interfactura.firmalocal.domain.entities.FiscalEntity;
import com.interfactura.firmalocal.domain.entities.State;

@Component
public class CustomerManager {

	public CustomerManager() {
	}
	
	@Autowired(required=true)
	CustomerDao customerDao;
	
	@Autowired(required=true)
	FiscalEntityDao fEDao;
	
	@Autowired(required=true)
	StateDao stateDao;

	public State getState(long id) {
		State state = stateDao.findById(id);
		return state;
	}	
	
	public FiscalEntity getFiscalEntity(long id) {
		FiscalEntity fiscalEntity = fEDao.findById(id);
		return fiscalEntity;
	}

	public List<State> listAllStates() {
		return customerDao.listAllStates();
	}
	
	public List<FiscalEntity> listAllFiscalEntity(String ids) {
		return customerDao.listAllFiscalEntity(ids);
	}

	public List<Customer> listar(int inicio, int cantidad, Filters<Filter> filters, String feids) {
		return customerDao.listar(inicio, cantidad, filters, feids);
	}

	public Customer get(long id) {
		Customer customer = customerDao.findById(id);
		return customer;
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void update(Customer customer) {
		customerDao.update(customer);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void delete(long id) {
		Customer customer = customerDao.findById(id);
		customerDao.remove(customer);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void create(Customer customer) 
	{
		State state = this.getState(customer.getAddress().getState().getId());
		customer.getAddress().setState(state);
		customerDao.persist(customer);
	}

	public Customer get(String rfc) {
		return customerDao.get(rfc);
	}
	
	public Customer get(String rfc, String feId) {
		return customerDao.get(rfc, feId);
	}
	
	public Customer get(String rfc, String feId, String idExtranjero) {
		return customerDao.get(rfc, feId, idExtranjero);
	}
	

	public Customer findByRFCAndFiscalEntity(Customer customer) {
		return customerDao.findByRFCAndFiscalEntity(customer);
	}
	
	public Customer findByNumberAndFiscalEntity(Customer customer) {
		return customerDao.findByNumberAndFiscalEntity(customer);
	}
	
	public Customer findByIdExtranjero(String idExtranjero) {
		return customerDao.findByIdExtranjero(idExtranjero);
	}
	
	public List<Customer> listar() {
		return customerDao.listar();
	}
}

