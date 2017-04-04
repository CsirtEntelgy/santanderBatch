package com.interfactura.firmalocal.dao.impl;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.interfactura.firmalocal.dao.SeriesDao;
import com.interfactura.firmalocal.domain.entities.Series;

@Component
public class JpaSeriesDao extends JpaDao<Long, Series> implements SeriesDao{

	private static final Logger logger = Logger.getLogger(JpaSeriesDao.class);
	
	@Override
	public Series findByNameAndFiscalEntity(Series series) 
	{
		try
		{
			String name = series.getName();
			long fiscalEntityId = series.getFiscalEntity().getId();
			String stringQuery = "SELECT x FROM Series x WHERE x.name=:name AND x.fiscalEntity.id=:fiscalEntityId";
			Query query = entityManager.createQuery(stringQuery);
			query.setParameter("name", name);
			query.setParameter("fiscalEntityId", fiscalEntityId);
			try 
			{	return  (Series) query.getSingleResult();	} 
			catch (NonUniqueResultException e) 
			{	return (Series) query.getResultList().get(0);	}        
		} 
		catch (NoResultException e) 
		{ return null;  }
	}


	@SuppressWarnings("unchecked")
	@Override
	public List<Series> listAllSeries() {
		String stringQuery = "SELECT x FROM Series x";
		Query query = entityManager.createQuery(stringQuery);
		List<Series> recordList = null;
		try {
			recordList = query.getResultList();
		} catch (NoResultException e) {
			logger.error(e.getLocalizedMessage(),e);
		}
		return recordList;
	}


	@SuppressWarnings("unchecked")
	@Override
	public List<Series> listOfSeries(long fiscalEntityId) {
		List<Series> recordList = null;
		String stringQuery = "SELECT x FROM Series x WHERE x.fiscalEntity.id =: fiscalEntityId";
		Query query = entityManager.createQuery(stringQuery);
		query.setParameter("fiscalEntityId", fiscalEntityId);
		try {
			recordList = query.getResultList();
		} catch (NoResultException e) {
			logger.error(e.getLocalizedMessage(),e);
		}
		return recordList;
		
	}
}
