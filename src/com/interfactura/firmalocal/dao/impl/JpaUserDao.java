package com.interfactura.firmalocal.dao.impl;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.UserDao;
import com.interfactura.firmalocal.domain.entities.FiscalEntity;
import com.interfactura.firmalocal.domain.entities.Role;
import com.interfactura.firmalocal.domain.entities.User;

@Component
public class JpaUserDao extends JpaDao<Long, User> implements UserDao{

	private static final Logger logger = Logger.getLogger(JpaUserDao.class);
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Role> listAllRoles() {
		String strQuery = "SELECT x FROM Role x ";
	    logger.debug(strQuery);
	    try{
	    	Query query = entityManager.createQuery(strQuery);
			List<Role> recordList= (List<Role>) query.getResultList();
			return recordList;
	    }catch( NoResultException e){
	    	logger.error(e.getLocalizedMessage(), e);
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<FiscalEntity> listAllFiscalEntity() {
		String strQuery = "SELECT x FROM FiscalEntity x ";
	    logger.debug(strQuery);
	    try{
	    	Query query = entityManager.createQuery(strQuery);
			List<FiscalEntity> recordList= (List<FiscalEntity>) query.getResultList();
			return recordList;
	    }catch( NoResultException e){
	    	logger.error(e.getLocalizedMessage(), e);
			return null;
		}
	}
	
	@Override
	public List<User> listar() {
		return listar(0, 0, null);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<User> listar(int inicio, int cantidad, Filters<Filter> filters) {
		String userName = "";
		String name = "";
		String apaterno = "";
		String amaterno = "";
		String where = "";
		String stringQuery = "SELECT x FROM User x";
		if (filters != null && filters.size() > 0) {
			for( Filter filter : filters ){
				if ( filter.getColumn().equals("userName") ){
					userName = filter.getPattern();
				}
				if ( filter.getColumn().equals("name") ){
			    	name = filter.getPattern();
				}
				if ( filter.getColumn().equals("apaterno") ){
					apaterno = filter.getPattern();
				}
				if ( filter.getColumn().equals("amaterno") ){
					amaterno = filter.getPattern();
				}
			}
			if (!"".equals(userName)) {
				where = " WHERE x.userName like :userName";
				if (!"".equals(name)) {
					where += " AND x.name like :name";
				}
				if (!"".equals(apaterno)) {
					where += " AND x.apellidoPaterno like :apaterno";
				}
				if (!"".equals(amaterno)) {
					where += " AND x.apellidoMaterno like :amaterno";
				}
			}
			else if (!"".equals(name)) {
				where = " WHERE x.name like :name";
				if (!"".equals(apaterno)) {
					where += " AND x.apellidoPaterno like :apaterno";
				}
				if (!"".equals(amaterno)) {
					where += " AND x.apellidoMaterno like :amaterno";
				}
			}
			else if (!"".equals(apaterno)) {
				where = " WHERE x.apellidoPaterno like :apaterno";
				if (!"".equals(amaterno)) {
					where += " AND x.apellidoMaterno like :amaterno";
				}
			}
			else{
				where = " WHERE x.apellidoMaterno like :amaterno";
			}
		}
		if (!"".equals(where)) {
			stringQuery += where;
		}
		Query query = entityManager.createQuery(stringQuery);
		if (!"".equals(where)) {
			if (!"".equals(userName)) {
				query.setParameter("userName", "%" + userName + "%");
			}
			if (!"".equals(name)) {
				query.setParameter("name", "%" + name + "%");
			}
			if (!"".equals(apaterno)) {
				query.setParameter("apaterno", "%" + apaterno + "%");
			}
			if (!"".equals(amaterno)) {
				query.setParameter("amaterno", "%" + amaterno + "%");
			}
		}
		if (cantidad != 0) {
			query.setFirstResult(inicio); 
			query.setMaxResults(cantidad);
		}
		List<User> recordList = query.getResultList();
		return recordList;
	}
	
	@Override
	public User findByName(String userName) 
	{
		try {
			String jsql = "SELECT x FROM User x WHERE x.userName=:userName";
			Query q = entityManager.createQuery(jsql);
			q.setParameter("userName", userName);
			try {                
				return  (User) q.getSingleResult();            
			} catch (NonUniqueResultException e) {                
				return (User) q.getResultList().get(0);            
				}        
		} catch (NoResultException e) 
		{ return null;  }
	}
	
}
