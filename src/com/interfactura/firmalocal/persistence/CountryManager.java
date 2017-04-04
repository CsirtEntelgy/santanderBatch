package com.interfactura.firmalocal.persistence;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.CountryDao;
import com.interfactura.firmalocal.domain.entities.Country;

@Component
public class CountryManager {

	@Autowired(required=true)
	CountryDao countryDao;
	
	public List<Country> listar() {
		return countryDao.listar();
	}
	
	public List<Country> listar(int inicio, int cantidad, Filters<Filter> filters) {
		return countryDao.listar(inicio, cantidad, filters);
	}

	public Country get(long id){
		Country country = countryDao.findById(id);
		return country;
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void update(Country country) {
		countryDao.update(country);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void delete(long id) {
		Country country = countryDao.findById(id);
		countryDao.remove(country);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void create(Country country) {
		countryDao.persist(country);
	}
	
	public Country findByName(String name) 
	{
		return countryDao.findByName(name);
	}
	
	public Country findByCode(String code) 
	{
		return countryDao.findByCode(code);
	}
	
}
