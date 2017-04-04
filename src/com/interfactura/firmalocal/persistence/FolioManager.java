package com.interfactura.firmalocal.persistence;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.FolioDao;
import com.interfactura.firmalocal.domain.entities.Folio;

@Component
public class FolioManager {
	
	@Autowired(required=true)
	private FolioDao fDao;

	public List<Folio> listar() {
		return listar(0, 0, null);
	}
	
	public List<Folio> listar(int inicio, int cantidad, Filters<Filter> filters) {
		return fDao.list(inicio, cantidad, filters);
	}
	
	public Folio get(long id) {
		return fDao.findById(id);
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void update(Folio folio) {
		fDao.update(folio);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void delete(long id) {
		Folio folio = fDao.findById(id);
		fDao.remove(folio);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void create(Folio folio) {
		fDao.persist(folio);
	}
}
