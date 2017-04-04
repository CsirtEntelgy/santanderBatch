package com.interfactura.firmalocal.dao;

import java.util.List;

import com.interfactura.firmalocal.domain.entities.Series;

public interface SeriesDao extends Dao<Long, Series>{
	
	Series findByNameAndFiscalEntity(Series series);
	
	List<Series> listAllSeries();
	
	List<Series> listOfSeries(long fiscalEntityId);
	
}
