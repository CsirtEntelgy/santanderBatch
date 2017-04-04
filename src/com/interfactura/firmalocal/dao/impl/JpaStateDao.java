package com.interfactura.firmalocal.dao.impl;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.StateDao;
import com.interfactura.firmalocal.domain.entities.Country;
import com.interfactura.firmalocal.domain.entities.State;

@Component
public class JpaStateDao extends JpaDao<Long, State> implements StateDao{
	
	private static final Logger logger = Logger.getLogger(JpaStateDao.class);
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Country> listAllCountries() {
		String strQuery = "SELECT x FROM Country x";
	    logger.debug(strQuery);
	    try{
	    	Query query = entityManager.createQuery(strQuery);
			List<Country> countries= (List<Country>) query.getResultList();
			return countries;
	    }catch( NoResultException e){
	    	logger.error(e.getLocalizedMessage(), e);
			return null;
		}
		
	}

	@Override
	public List<State> listar() {
		return listar(0,0,null);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<State> listar(int inicio, int cantidad, Filters<Filter> filters) {
		String name = "";
		String countryName = "";
		String where = "";
		String stringQuery = "SELECT x FROM State x";
		if (filters != null && filters.size() > 0) {
			for( Filter filter : filters ){
				if ( filter.getColumn().equals("name") ){
			    	   name = filter.getPattern();
				}
				if ( filter.getColumn().equals("countryName") ){
					   countryName = filter.getPattern();
				}
			}
			if (!"".equals(name)) {
				where = " WHERE x.name like :name";
				if (!"".equals(countryName)) {
					where += " AND x.country.name like :countryName";
				}
			}else{
				where = " WHERE x.country.name like :countryName";
			}
		}
		if (!"".equals(where)) {
			stringQuery += where;
		}
		Query query = entityManager.createQuery(stringQuery);
		if (!"".equals(where)) {
			if (!"".equals(name)) {
				query.setParameter("name", "%" + name + "%");
			}
			if (!"".equals(countryName)) {
				query.setParameter("countryName", "%" + countryName + "%");
			}
		}
		if (cantidad != 0) {
			query.setFirstResult(inicio); 
			query.setMaxResults(cantidad);
		}
		List<State> recordList = query.getResultList();
		return recordList;
	}
	
	@Override
	public State findByName(String name, long countryId) 
	{
		try {
		String jsql = "SELECT x FROM State x WHERE x.name=:name and x.country.id =:countryId";
		logger.debug(jsql);
		Query q = entityManager.createQuery(jsql);
		q.setParameter("name", name);
		q.setParameter("countryId", countryId);
		try {                
			return  (State) q.getSingleResult();            
		} catch (NonUniqueResultException e) {     
			logger.error(e.getLocalizedMessage(), e);
			return (State) q.getResultList().get(0);            
			}        
		} catch (NoResultException e){ 
			logger.error(e.getLocalizedMessage(), e);
			return null;  
		}
	}
	
	@Override
	public State findByName(String name) 
	{
		try {
		String jsql = "SELECT x FROM State x WHERE x.name=:name";
		logger.debug(jsql);
		Query q = entityManager.createQuery(jsql);
		q.setParameter("name", name);
		try {                
			return  (State) q.getSingleResult();            
		} catch (NonUniqueResultException e) {     
			logger.error(e.getLocalizedMessage(), e);
			return (State) q.getResultList().get(0);            
			}        
		} catch (NoResultException e){ 
			logger.error(e.getLocalizedMessage(), e);
			return null;  
		}
	}
	
}
