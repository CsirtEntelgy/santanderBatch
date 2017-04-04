package com.interfactura.firmalocal.dao.impl;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.PermissionDao;
import com.interfactura.firmalocal.domain.entities.Page;
import com.interfactura.firmalocal.domain.entities.Permission;

@Component
public class JpaPermissionDao extends JpaDao<Long, Permission> implements PermissionDao{

	private static final Logger logger = Logger.getLogger(JpaPermissionDao.class);
	
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Page> listAllPages() {
		String strQuery = "SELECT x FROM Page x ";
	    logger.debug(strQuery);
	    try{
	    	Query query = entityManager.createQuery(strQuery);
			List<Page> recordList= (List<Page>) query.getResultList();
			return recordList;
	    }catch( NoResultException e){
	    	logger.error(e.getLocalizedMessage(), e);
			return null;
		}
	}
	
	@Override
	public List<Permission> listar() {
		return listar(0, 0, null);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Permission> listar(int inicio, int cantidad, Filters<Filter> filters) {
		String name = "";
		String where = "";
		String stringQuery = "SELECT x FROM Permission x";
		if (filters != null && filters.size() > 0) {
			for( Filter filter : filters ){
				if ( filter.getColumn().equals("name") ){
			    	   name = filter.getPattern();
				}
			}
			if (!"".equals(name)) {
				where = " WHERE x.name like :name";
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
		}
		if (cantidad != 0) {
			query.setFirstResult(inicio); 
			query.setMaxResults(cantidad);
		}
		List<Permission> recordList = query.getResultList();
		return recordList;
	}

	
	@Override
	public Permission findByName(String name) 
	{
		try
		{
			String stringQuery = "SELECT x FROM Permission x WHERE x.name=:name";
			Query query = entityManager.createQuery(stringQuery);
			query.setParameter("name", name);
			try 
			{	return  (Permission) query.getSingleResult();	} 
			catch (NonUniqueResultException e) 
			{	return (Permission) query.getResultList().get(0);	}        
		} 
		catch (NoResultException e) 
		{ return null;  }
	}


	
	
}
