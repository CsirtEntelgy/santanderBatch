package com.interfactura.firmalocal.persistence;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.CountryDao;
import com.interfactura.firmalocal.dao.StateDao;
import com.interfactura.firmalocal.domain.entities.Country;
import com.interfactura.firmalocal.domain.entities.State;

@Component
public class StateManager {
	
	@Autowired(required=true)
	StateDao stateDao;
	
	@Autowired(required=true)
	CountryDao countryDao;
	
	public List<State> listar() {
		return stateDao.listar();
	}

	public List<State> listar(int inicio, int cantidad, Filters<Filter> filters){
		return stateDao.listar(inicio, cantidad, filters);
	}
	
	public List<Country> listAllCountries() {
		return stateDao.listAllCountries();
	}
	
	public State get(long id){
		State state = stateDao.findById(id);
		return state;
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void update(State state) {
		stateDao.update(state);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void delete(long id) {
		State state = stateDao.findById(id);
		stateDao.remove(state);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void create(State state) {
		stateDao.persist(state);
	}
	
	public Country getCountry(long id) {
		Country country = countryDao.findById(id);
		return country;
	}	

	public State findByName(String name, long countryId) {
		return stateDao.findByName(name, countryId);
	}

	public State findByName(String name) {
		return stateDao.findByName(name);
	}
	
}
