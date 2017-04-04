package com.interfactura.firmalocal.dao.impl;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.CountryDao;
import com.interfactura.firmalocal.domain.entities.Country;

@Component
public class JpaCountryDao extends JpaDao<Long, Country> implements CountryDao{
	
	private static final Logger logger = Logger.getLogger(JpaCountryDao.class);
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Country> listar(int inicio, int cantidad, Filters<Filter> filters) {
		String name = "";
		String where = "";
		String stringQuery = "SELECT x FROM Country x";
		if (filters != null && filters.size() > 0) {
			name = filters.get(0).getPattern();
			if (!"".equals(name)) {
				where = " WHERE x.name like :name";
			}
		}
		if (!"".equals(where)) {
			stringQuery += where;
		}
		Query query = entityManager.createQuery(stringQuery);
		if (!"".equals(where)) {
			query.setParameter("name", "%" + name + "%");
		}
		if (cantidad != 0) {
			query.setFirstResult(inicio); 
			query.setMaxResults(cantidad);
		}
		List<Country> recordList = query.getResultList();
		return recordList;
	}

	@Override
	public Country findByName(String name) 
	{
		try {
		String jsql = "SELECT x FROM Country x WHERE x.name=:name";
		Query q = entityManager.createQuery(jsql);
		q.setParameter("name", name);
		try {                
			return  (Country) q.getSingleResult();            
		} catch (NonUniqueResultException e) {  
			logger.error(e.getLocalizedMessage(), e);
			return (Country) q.getResultList().get(0);            
			}        
		} catch (NoResultException e){ 
			logger.error(e.getLocalizedMessage(), e);
			return null;  }
	}
	
	@Override
	public Country findByCode(String code) 
	{
		try {
		String jsql = "SELECT x FROM Country x WHERE x.countryCode=:code";
		Query q = entityManager.createQuery(jsql);
		q.setParameter("code", code);
		try {                
			return  (Country) q.getSingleResult();            
		} catch (NonUniqueResultException e) { 
			logger.error(e.getLocalizedMessage(), e);
			return (Country) q.getResultList().get(0);            
			}        
		} catch (NoResultException e){ 	
			logger.error(e.getLocalizedMessage(), e);
			return null;  
		}
	}

	@Override
	public List<Country> listar() {
		return listar(0,0,null);
	}

}
