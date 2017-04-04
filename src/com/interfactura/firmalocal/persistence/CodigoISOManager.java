package com.interfactura.firmalocal.persistence;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.CodigoISODao;
import com.interfactura.firmalocal.domain.entities.CodigoISO;

@Component
public class CodigoISOManager {
	
	//@Autowired
	@Autowired(required=true)
	CodigoISODao codigoISODao;
	
	public List<CodigoISO> listar(){
		return codigoISODao.listar();
	}
	public List<CodigoISO> listar(int inicio, int cantidad, Filters<Filter> filters){
		return codigoISODao.listar(inicio, cantidad, filters);
	}
	public CodigoISO get(long id){
		CodigoISO codigoISO = codigoISODao.findById(id);
		return codigoISO;
	}
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void update(CodigoISO codigoISO){
		codigoISODao.update(codigoISO);
	}
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void delete(long id){
		CodigoISO codigoISO = codigoISODao.findById(id);
		codigoISODao.remove(codigoISO);
	}
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void create(CodigoISO codigoISO){
		codigoISODao.persist(codigoISO);
	}
	public CodigoISO findByDescripcion(String descripcion){
		return codigoISODao.findByDescripcion(descripcion);
	}
	public CodigoISO findByCodigo(String codigo){
		return codigoISODao.findByCodigo(codigo);
	}
}
