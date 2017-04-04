package com.interfactura.firmalocal.dao.impl;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.MonedaDao;
import com.interfactura.firmalocal.domain.entities.Moneda;

@Component
public class JpaMonedaDao extends JpaDao<Long, Moneda> implements MonedaDao{

	@SuppressWarnings("unchecked")
	@Override
	public List<Moneda> list(int begin, int quantity, Filters<Filter> filters) {
		String nombreLargo = "";
		String where = "";
		List<Moneda> recordList = null;
		String stringQuery = "SELECT x FROM Moneda x";
		if (filters != null && filters.size() > 0) {
			nombreLargo = filters.get(0).getPattern();
			if (!"".equals(nombreLargo)) {
				where = " WHERE LOWER(x.nombreLargo) like :nombreLargo";
			}
		}
		if (!"".equals(where)) {
			stringQuery += where;
		}
		Query query = entityManager.createQuery(stringQuery);
		if (!"".equals(where)) {
			query.setParameter("nombreLargo", "%" + nombreLargo.toLowerCase() + "%");
		}
		if (quantity != 0) {
			query.setFirstResult(begin); 
			query.setMaxResults(quantity);
		}
		try {
			recordList = query.getResultList();
		} catch (NoResultException e) {
			logger.error(e.getLocalizedMessage(),e);
		}
		return recordList;
	}

	@Override
	public Moneda findByName(String nombreCorto) 
	{
		try {
		String jsql = "SELECT x FROM Moneda x WHERE x.nombreCorto=:nombreCorto";
		Query q = entityManager.createQuery(jsql);
		q.setParameter("nombreCorto", nombreCorto);
		try {                
			return  (Moneda) q.getSingleResult();            
		} catch (NonUniqueResultException e) {  
			logger.error(e.getLocalizedMessage(), e);
			return (Moneda) q.getResultList().get(0);            
			}        
		} catch (NoResultException e){ 
			logger.error(e.getLocalizedMessage(), e);
			return null;  }
	}
	
	private static final Logger logger = Logger.getLogger(JpaMonedaDao.class);

}

