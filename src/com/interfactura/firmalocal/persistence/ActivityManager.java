package com.interfactura.firmalocal.persistence;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.ActivityDao;
import com.interfactura.firmalocal.domain.entities.Activity;

@Component
public class ActivityManager {

	@Autowired(required=true)
	private ActivityDao aDao;
	
	public List<Activity> listar() {
		return listar(0, 0, null);
	}
	
	@Transactional(readOnly=true)
	public List<Activity> listar(int inicio, int cantidad, Filters<Filter> filters) {
		return aDao.list(inicio, cantidad, filters);
	}

	@Transactional(readOnly=true)
	public Activity get(long id) {
		return aDao.findById(id);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void update(Activity activity) {
		aDao.update(activity);
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void delete(long id) {
		Activity activity = aDao.findById(id);
		aDao.remove(activity);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void create(Activity activity) {
		aDao.persist(activity);
	}
}
