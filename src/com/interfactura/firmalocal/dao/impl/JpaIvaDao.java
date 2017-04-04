package com.interfactura.firmalocal.dao.impl;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.IvaDao;
import com.interfactura.firmalocal.domain.entities.Iva;

@Component
public class JpaIvaDao extends JpaDao<Long, Iva> implements IvaDao{

	@SuppressWarnings("unchecked")
	@Override
	public List<Iva> list(int begin, int quantity, Filters<Filter> filters) {
		String descripcion = "";
		String where = "";
		List<Iva> recordList = null;
		String stringQuery = "SELECT x FROM Iva x";
		if (filters != null && filters.size() > 0) {
			descripcion = filters.get(0).getPattern();
			if (!"".equals(descripcion)) {
				where = " WHERE LOWER(x.descripcion) like :descripcion";
			}
		}
		if (!"".equals(where)) {
			stringQuery += where;
		}
		Query query = entityManager.createQuery(stringQuery);
		if (!"".equals(where)) {
			query.setParameter("descripcion", "%" + descripcion + "%");
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
	public Iva findByDescription(String descripcion) 
	{
		try {
		String jsql = "SELECT x FROM Iva x WHERE x.descripcion=:descripcion";
		Query q = entityManager.createQuery(jsql);
		q.setParameter("descripcion", descripcion);
		try {                
			return  (Iva) q.getSingleResult();            
		} catch (NonUniqueResultException e) {  
			logger.error(e.getLocalizedMessage(), e);
			return (Iva) q.getResultList().get(0);            
			}        
		} catch (NoResultException e){ 
			logger.error(e.getLocalizedMessage(), e);
			return null;  }
	}
	
	@Override
	public Iva findByTasa(String tasa) 
	{
		
		try {
		String jsql = "SELECT x FROM Iva x WHERE x.tasa=:tasa";
		Query q = entityManager.createQuery(jsql);
		q.setParameter("tasa", Integer.parseInt(tasa));
		try {                
			return  (Iva) q.getSingleResult();            
		} catch (NonUniqueResultException e) {  
			logger.error(e.getLocalizedMessage(), e);
			return (Iva) q.getResultList().get(0);            
			}        
		} catch (NoResultException e){ 
			logger.error(e.getLocalizedMessage(), e);
			return null;  }
	}
	
	private static final Logger logger = Logger.getLogger(JpaIvaDao.class);

}

