package com.interfactura.firmalocal.persistence;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.MonedaDao;
import com.interfactura.firmalocal.domain.entities.Moneda;

@Component
public class MonedaManager {
	
	public List<Moneda> listar() {
		return listar(0, 0, null);
	}
	
	public List<Moneda> listar(int inicio, int cantidad, Filters<Filter> filters) {
		return monedaDao.list(inicio, cantidad, filters);
	}

	public Moneda get(long id) 
	{
		Moneda p = monedaDao.findById(id);
		return p;
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void update(Moneda moneda) {
		monedaDao.update(moneda);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void delete(long id) {
		Moneda moneda = monedaDao.findById(id);
		monedaDao.remove(moneda);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void create(Moneda moneda) {
		monedaDao.persist(moneda);
	}
	
	public Moneda findByName(String name) { 	
		return monedaDao.findByName(name);
	}
	
	@Autowired(required=true)
	MonedaDao monedaDao;
	
	
}
