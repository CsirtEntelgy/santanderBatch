package com.interfactura.firmalocal.dao.impl;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.RoleDao;
import com.interfactura.firmalocal.domain.entities.Role;

@Component
public class JpaRoleDao extends JpaDao<Long, Role> implements RoleDao{

	private static final Logger logger = Logger.getLogger(JpaRoleDao.class);
		
	@Override
	public List<Role> listar() {
		return listar(0, 0, null);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Role> listar(int inicio, int cantidad, Filters<Filter> filters) {
		String name = "";
		String where = "";
		String stringQuery = "SELECT x FROM Role x";
		logger.debug(stringQuery);
		if (filters != null && filters.size() > 0) {
			name = filters.get(0).getPattern();
			if (!"".equals(name)) {
				where = " WHERE LOWER(x.name) like :name";
			}
		}
		if (!"".equals(where)) {
			stringQuery += where;
		}
		Query query = entityManager.createQuery(stringQuery);
		if (!"".equals(where)) {
			query.setParameter("name", "%" + name.toLowerCase() + "%");
		}
		if (cantidad != 0) {
			query.setFirstResult(inicio); 
			query.setMaxResults(cantidad);
		}
		List<Role> recordList = query.getResultList();
		return recordList;
	}

	
	@Override
	public Role findByName(String name) 
	{
		try {
		String jsql = "SELECT x FROM Role x WHERE x.name=:name";
		Query q = entityManager.createQuery(jsql);
		q.setParameter("name", name);
		try {                
			return  (Role) q.getSingleResult();            
		} catch (NonUniqueResultException e) {                
			return (Role) q.getResultList().get(0);            
			}        
		} catch (NoResultException e) 
		{ return null;  }
	}
	
}
