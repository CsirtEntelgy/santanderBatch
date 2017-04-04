package com.interfactura.firmalocal.persistence;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.LogoDao;
import com.interfactura.firmalocal.domain.entities.Customer;
import com.interfactura.firmalocal.domain.entities.Logo;


@Component
public class LogoManager {

	@Autowired(required=true)
	private LogoDao logoDao;
	
	public List<Logo> listAll() {
		return logoDao.listAll();
	}
	
	public List<Logo> list(int begin, int quantity, Filters<Filter> filters, String feids){
		return logoDao.list(begin, quantity, filters, feids);
	}
	
	public Logo getByFEId(long id, Date fecha) {
		return logoDao.getByFEId(id, fecha);
	}

	public LogoDao getLogoDao() {
		return logoDao;
	}

	public void setLogoDao(LogoDao logoDao) {
		this.logoDao = logoDao;
	}
	
	public Logo get(long id){
		return logoDao.findById(id);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void update(Logo logo){
		logoDao.update(logo);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void delete(long id) {
		Logo logo = logoDao.findById(id);
		logoDao.remove(logo);
	}
}
