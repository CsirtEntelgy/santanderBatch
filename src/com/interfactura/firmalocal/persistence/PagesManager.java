package com.interfactura.firmalocal.persistence;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.PageDao;
import com.interfactura.firmalocal.domain.entities.Page;

@Component
public class PagesManager {
	
	public List<Page> listar() {
		return listar(0, 0, null);
	}
	
	public List<Page> listar(int inicio, int cantidad, Filters<Filter> filters) {
		return pageDao.list(inicio, cantidad, filters);
	}

	public Page get(long id) 
	{
		Page p = pageDao.findById(id);
		return p;
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void update(Page page) {
		pageDao.update(page);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void delete(long id) {
		Page page = pageDao.findById(id);
		pageDao.remove(page);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void create(Page page) {
		pageDao.persist(page);
	}
	
	@Autowired(required=true)
	PageDao pageDao;
	
}
