package com.interfactura.firmalocal.persistence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.interfactura.firmalocal.dao.FiscalEntityDao;
import com.interfactura.firmalocal.dao.SeriesDao;
import com.interfactura.firmalocal.domain.entities.FiscalEntity;
import com.interfactura.firmalocal.domain.entities.Series;

@Component
public class SeriesManager {
	
	public SeriesManager() {
	}
	
	@Autowired(required=true)
	SeriesDao seriesDao;
	
	@Autowired(required=true)
	FiscalEntityDao fEDao;
	
	public FiscalEntity getFiscalEntity(long id) {
		FiscalEntity fiscalEntity = fEDao.findById(id);
		return fiscalEntity;
	}
	
	public Series get(long id) {
		Series series = seriesDao.findById(id);
		return series;
	}
	
	public Series findByNameAndFiscalEntity(Series series) {
		return seriesDao.findByNameAndFiscalEntity(series);
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void update(Series series) {
		seriesDao.update(series);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void delete(long id) {
		Series series = seriesDao.findById(id);
		seriesDao.remove(series);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void create(Series series){
		seriesDao.persist(series);
	}

}
