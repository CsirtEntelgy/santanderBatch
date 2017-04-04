package com.interfactura.firmalocal.persistence;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.AddendumCustomsDao;
import com.interfactura.firmalocal.domain.entities.AddendumCustoms;

@Component
public class AddendumCustomsManager {
	
	@Autowired(required=true)
	AddendumCustomsDao acDao;
	
	public AddendumCustomsManager() {
	}
	
	
	public List<AddendumCustoms> listar(int inicio, int cantidad, Filters<Filter> filters) {
		return acDao.listar(inicio, cantidad, filters);
	}
	
	public AddendumCustoms get(long id) {
		AddendumCustoms addendumCustoms = acDao.findById(id);
		return addendumCustoms;
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void update(AddendumCustoms addendumCustoms) {
		acDao.update(addendumCustoms);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void delete(long id) {
		AddendumCustoms addendumCustoms = acDao.findById(id);
		acDao.remove(addendumCustoms);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public AddendumCustoms create(AddendumCustoms addendumCustoms){
		return acDao.persist(addendumCustoms);
	}
	
}
