package com.interfactura.firmalocal.dao.impl;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.CustomerDao;
import com.interfactura.firmalocal.domain.entities.Customer;
import com.interfactura.firmalocal.domain.entities.FiscalEntity;
import com.interfactura.firmalocal.domain.entities.State;
import com.interfactura.firmalocal.persistence.UtilManager;

@Component
public class JpaCustomerDao extends JpaDao<Long, Customer> implements CustomerDao{

	private static final Logger logger = Logger.getLogger(JpaCustomerDao.class);
	
	@SuppressWarnings("unchecked")
	@Override
	public List<State> listAllStates() {
		String strQuery = "SELECT x FROM State x";
	    logger.debug(strQuery);
	    try{
	    	Query query = entityManager.createQuery(strQuery);
			List<State> states= (List<State>) query.getResultList();
			return states;
	    }catch( NoResultException e){
	    	logger.error(e.getLocalizedMessage(), e);
			return null;
		}
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<FiscalEntity> listAllFiscalEntity(String ids) {
		String strQuery = "SELECT x FROM FiscalEntity x ";
		strQuery += UtilManager.in(ids);
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
	
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Customer> listar(int inicio, int cantidad, Filters<Filter> filters, String feids) {
		String fiscalName = "";
		String rfc = "";
		String efName = "";
		String where = "";
		String stringQuery = "SELECT x FROM Customer x ";
		stringQuery += UtilManager.inListar(feids);
		if (filters != null && filters.size() > 0) {
			for( Filter filter : filters ){
				if ( filter.getColumn().equals("fiscalName") ){
					fiscalName = filter.getPattern();
				}
				if ( filter.getColumn().equals("rfc") ){
					rfc = filter.getPattern();
				}
				if ( filter.getColumn().equals("efName") ){
					efName = filter.getPattern();
				}
			}
			if (!"".equals(fiscalName)) {
				where = " AND x.physicalName like :fiscalName";
			}
			if (!"".equals(rfc)) {
				where += " AND x.taxId like :rfc";
			}
			if (!"".equals(efName)) {
				where += " AND x.fiscalEntity.fiscalName like :efName";
			}
		}
		if (!"".equals(where)) {
			stringQuery += where;
		}
		stringQuery += " ORDER BY x.number";
		Query query = entityManager.createQuery(stringQuery);
		if (!"".equals(where)) {
			if (!"".equals(fiscalName)) {
				query.setParameter("fiscalName", "%" + fiscalName + "%");
			}
			if (!"".equals(rfc)) {
				query.setParameter("rfc", "%" + rfc + "%");
			}
			if (!"".equals(efName)) {
				query.setParameter("efName", "%" + efName + "%");
			}
		}
		if (cantidad != 0) {
			query.setFirstResult(inicio); 
			query.setMaxResults(cantidad);
		}
		List<Customer> recordList = query.getResultList();
		return recordList;
	}

	
	@Override
	public Customer get(String rfc) {
		try {
			String jsql = "SELECT x FROM Customer x WHERE x.taxId=:taxId ";
			logger.debug(jsql + " for RFC: " + rfc);
			Query q = entityManager.createQuery(jsql);
			q.setParameter("taxId", rfc);
			try {                
				return  (Customer) q.getSingleResult();            
			} 
			catch (NonUniqueResultException e) {  
				logger.error(e.getLocalizedMessage(), e);
				return (Customer) q.getResultList().get(0);            
			}        
		} catch (NoResultException e) { 	
			logger.error(e.getLocalizedMessage(), e);
			return null;  
		}
	}
	
	
	public Customer get(String rfc, String feId) {
		try {
			long val = 0;
			try
			{
				val = Long.parseLong(feId);
			}
			catch(Exception e)
			{
				logger.debug(feId);
				e.printStackTrace();
			}
			String jsql = "SELECT x FROM Customer x WHERE x.taxId=:taxId and x.fiscalEntity.id=:feId";
			logger.debug(jsql + " for RFC: " + rfc + " and Fiscal Entity: " + val);
			Query q = entityManager.createQuery(jsql);
			q.setParameter("taxId", rfc);
			q.setParameter("feId", val);
			try {
				
				List list = q.getResultList();
				if(!CollectionUtils.isEmpty(list)){
					return  (Customer) list.get(0);
				}else{
					return null;
				}
				
				
				
				//return  (Customer) q.getResultList().get(0);
							
			} 
			catch (NonUniqueResultException e) {  
				logger.error(e.getLocalizedMessage(), e);
				return (Customer) q.getResultList().get(0);            
			}        
		} catch (NoResultException e) { 	
			logger.error(e.getLocalizedMessage(), e);
			return null;  
		}
	}
	
	public Customer get(String rfc, String feId, String idExtranjero) {
		try {
			long val = 0;
			try
			{
				val = Long.parseLong(feId);
			}
			catch(Exception e)
			{
				logger.debug(feId);
				e.printStackTrace();
			}
			System.out.println("<<<< Id Extranjero DAO: " + idExtranjero);
			System.out.println("<<<< feId DAO: " + feId);
			String jsql = "SELECT x FROM Customer x WHERE x.idExtranjero=:idExtranjero and x.fiscalEntity.id=:feId";
			logger.debug(jsql + " for RFC: " + rfc + " and Fiscal Entity: " + val);
			Query q = entityManager.createQuery(jsql);
			//q.setParameter("taxId", rfc);
			q.setParameter("feId", val);
			q.setParameter("idExtranjero", idExtranjero);
			try {                
				return  (Customer) q.getSingleResult();            
			} 
			catch (NonUniqueResultException e) {  
				logger.error(e.getLocalizedMessage(), e);
				return (Customer) q.getResultList().get(0);            
			}        
		} catch (NoResultException e) { 	
			logger.error(e.getLocalizedMessage(), e);
			return null;  
		}
	}
	
	@Override
	public Customer findByNumberAndFiscalEntity(Customer customer) {
		try
		{
			Integer number = customer.getNumber();
			long fiscalEntityId = customer.getFiscalEntity().getId();
			String stringQuery = "SELECT x FROM Customer x WHERE x.number=:number AND x.fiscalEntity.id=:fiscalEntityId";
			Query query = entityManager.createQuery(stringQuery);
			query.setParameter("number", number);
			query.setParameter("fiscalEntityId", fiscalEntityId);
			try 
			{	return  (Customer) query.getSingleResult();	} 
			catch (NonUniqueResultException e) 
			{	return (Customer) query.getResultList().get(0);	}        
		} 
		catch (NoResultException e) 
		{ return null;  }
	}

	@Override
	public Customer findByRFCAndFiscalEntity(Customer customer) {
		try{
		String rfc = customer.getTaxId();
	    long fiscalEntityId = customer.getFiscalEntity().getId();
		String stringQuery = "SELECT x FROM Customer x WHERE x.taxId=:taxId AND x.fiscalEntity.id=:fiscalEntityId";
		Query query = entityManager.createQuery(stringQuery);
		query.setParameter("taxId", rfc);
		query.setParameter("fiscalEntityId", fiscalEntityId);
			try {                
				return  (Customer) query.getSingleResult();            
			} catch (NonUniqueResultException e) {                
				return (Customer) query.getResultList().get(0);            
				}        
		} catch (NoResultException e) 
		{ return null;  }
	}
	
	@Override
	public Customer findByIdExtranjero(String idExtranjero) {
		try {
			String jsql = "SELECT x FROM Customer x WHERE x.idExtranjero=:idExtranjero";
			Query q = entityManager.createQuery(jsql);
			q.setParameter("idExtranjero", idExtranjero);
			try {
				return (Customer) q.getSingleResult();
			} catch (NonUniqueResultException e) {
				return (Customer) q.getResultList().get(0);
			}
		} catch (NoResultException e) {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Customer> listar() {
		
		String stringQuery = "SELECT x FROM Customer x inner join x.fiscalEntity y where x.fiscalEntity.id = y.id ";
		
		stringQuery += " ORDER BY x.number";
		
		Query query = entityManager.createQuery(stringQuery);
		
		List<Customer> recordList = query.getResultList();
		
		return recordList;
	}

}
