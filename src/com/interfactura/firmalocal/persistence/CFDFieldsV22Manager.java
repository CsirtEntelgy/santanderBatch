package com.interfactura.firmalocal.persistence;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.CFDFieldsV22Dao;
import com.interfactura.firmalocal.domain.entities.CFDFieldsV22;


@Component
public class CFDFieldsV22Manager {

	@Autowired(required=true)
	private CFDFieldsV22Dao cfdFieldsV22Dao;

	public CFDFieldsV22Dao getCfdFieldsV22Dao() {
		return cfdFieldsV22Dao;
	}

	public void setCfdFieldsV22Dao(CFDFieldsV22Dao cfdFieldsV22Dao) {
		this.cfdFieldsV22Dao = cfdFieldsV22Dao;
	}
	
	public List<CFDFieldsV22> listAll() {
		return cfdFieldsV22Dao.listAll();
	}
	
	public List<CFDFieldsV22> list(int begin, int quantity, Filters<Filter> filters, String feids){
		return cfdFieldsV22Dao.list(begin, quantity, filters, feids);
	}
	
	public CFDFieldsV22 get(long id){
		return cfdFieldsV22Dao.findById(id);
	}
	
	public CFDFieldsV22 findByFiscalID(long fiscalId) {
		return cfdFieldsV22Dao.findByFiscalId(fiscalId);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void update(CFDFieldsV22 cfdFieldsV22){
		cfdFieldsV22Dao.update(cfdFieldsV22);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void delete(long id) {
		CFDFieldsV22 cfdFieldsV22 = cfdFieldsV22Dao.findById(id);
		cfdFieldsV22Dao.remove(cfdFieldsV22);
	}
}
