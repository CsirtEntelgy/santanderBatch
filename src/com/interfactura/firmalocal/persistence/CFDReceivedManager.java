package com.interfactura.firmalocal.persistence;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.CfdReceivedDao;
import com.interfactura.firmalocal.domain.entities.CFDReceived;

@Component
public class CFDReceivedManager {
	
	public List<CFDReceived> listar() {
		return listar(0, 0, null);
	}
	
	public List<CFDReceived> listar(int inicio, int cantidad, Filters<Filter> filters) {
		return cfdrDao.list(inicio, cantidad, filters);
	}

	public CFDReceived get(long id) {
		return cfdrDao.findById(id);
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void update(CFDReceived cFDReceived) {
		cfdrDao.update(cFDReceived);
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void delete(long id) {
		CFDReceived cfdr = cfdrDao.findById(id);
		cfdrDao.remove(cfdr);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void create(CFDReceived cFDReceived) {
		cfdrDao.persist(cFDReceived);
	}
	
	@Autowired(required=true)
	private CfdReceivedDao cfdrDao;
			
}
