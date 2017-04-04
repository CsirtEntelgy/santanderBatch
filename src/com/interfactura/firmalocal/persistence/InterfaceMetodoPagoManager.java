package com.interfactura.firmalocal.persistence;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.InterfaceMetodoPagoDao;
import com.interfactura.firmalocal.domain.entities.InterfaceMetodoPago;


@Component
public class InterfaceMetodoPagoManager {

	@Autowired(required=true)
	private InterfaceMetodoPagoDao interfaceMetPagoDao;

	public InterfaceMetodoPagoDao getCfdFieldsV22Dao() {
		return interfaceMetPagoDao;
	}

	public void setCfdFieldsV22Dao(InterfaceMetodoPagoDao cfdFieldsV22Dao) {
		this.interfaceMetPagoDao = cfdFieldsV22Dao;
	}
	
	
	public List<InterfaceMetodoPago> listAll() {
		return interfaceMetPagoDao.listAll();
	}
	
	public List<InterfaceMetodoPago> list(int begin, int quantity, Filters<Filter> filters, String feids){
		return interfaceMetPagoDao.list(begin, quantity, filters, feids);
	}
	
	public List<InterfaceMetodoPago> listByColumn(String nomInterface){
		return interfaceMetPagoDao.listByColumn( nomInterface);
	}
	
	public List<InterfaceMetodoPago> listExcel(int begin, int quantity, Filters<Filter> filters, String feids){
		return interfaceMetPagoDao.listExcel(begin, quantity, filters, feids);
	}
		
	public InterfaceMetodoPago get(long id){
		return interfaceMetPagoDao.findById(id);
	}
	
	public InterfaceMetodoPago findByFiscalID(long fiscalId) {
		return interfaceMetPagoDao.findByFiscalId(fiscalId);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void update(InterfaceMetodoPago interfaceMetPago){
		interfaceMetPagoDao.update(interfaceMetPago);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void delete(long id) {
		InterfaceMetodoPago interfaceMetPago = interfaceMetPagoDao.findById(id);
		interfaceMetPagoDao.remove(interfaceMetPago);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void desActivar(long id) {
		InterfaceMetodoPago interfaceMetPago = interfaceMetPagoDao.findById(id);
		if(interfaceMetPago.getActivo().equals("0")){
			interfaceMetPago.setActivo("1");
		}else if(interfaceMetPago.getActivo().equals("1")){
			interfaceMetPago.setActivo("0");
		}
		
		interfaceMetPagoDao.update(interfaceMetPago);
	}
}
