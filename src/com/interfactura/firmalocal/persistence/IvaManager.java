package com.interfactura.firmalocal.persistence;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.IvaDao;
import com.interfactura.firmalocal.domain.entities.Iva;

@Component
public class IvaManager {
	
	public List<Iva> listar() {
		return listar(0, 0, null);
	}
	
	public List<Iva> listar(int inicio, int cantidad, Filters<Filter> filters) {
		return ivaDao.list(inicio, cantidad, filters);
	}

	public Iva get(long id) 
	{
		Iva p = ivaDao.findById(id);
		return p;
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void update(Iva iva) {
		ivaDao.update(iva);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void delete(long id) {
		Iva iva = ivaDao.findById(id);
		ivaDao.remove(iva);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void create(Iva iva) {
		ivaDao.persist(iva);
	}
	
	public Iva findByDescription(String name) { 	
		return ivaDao.findByDescription(name);
	}
	
	public Iva findByTasa(String tasa) { 	
		return ivaDao.findByTasa(tasa);
	}
	
	@Autowired(required=true)
	IvaDao ivaDao;
	
}
