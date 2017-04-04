package com.interfactura.firmalocal.persistence;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.AreaResponsableDao;
import com.interfactura.firmalocal.domain.entities.AreaResponsable;

@Component
public class AreaResponsableManager {

	//@Autowired
	@Autowired(required=true)
	AreaResponsableDao areaResponsableDao;
	
	public List<AreaResponsable> listar(){
		return areaResponsableDao.listar();
	}
	public List<AreaResponsable> listar(int inicio, int cantidad, Filters<Filter> filters){
		return areaResponsableDao.listar(inicio, cantidad, filters);
	}
	public AreaResponsable get(long id){
		AreaResponsable areaResponsable = areaResponsableDao.findById(id);
		return areaResponsable;
	}
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void update(AreaResponsable areaResponsable){
		areaResponsableDao.update(areaResponsable);
	}
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void delete(long id){
		AreaResponsable areaResponsable = areaResponsableDao.findById(id);
		areaResponsableDao.remove(areaResponsable);
	}
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void create(AreaResponsable areaResponsable){
		areaResponsableDao.persist(areaResponsable);
	}
	public AreaResponsable findByNombre(String nombre){
		return areaResponsableDao.findByNombre(nombre);
	}
}
