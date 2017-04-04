package com.interfactura.firmalocal.dao.impl;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.FiscalEntityDao;
import com.interfactura.firmalocal.domain.entities.FiscalEntity;
import com.interfactura.firmalocal.domain.entities.State;
import com.interfactura.firmalocal.persistence.UtilManager;

@Component
public class JpaFiscalEntityDao extends JpaDao<Long, FiscalEntity> implements FiscalEntityDao{

	private static final Logger logger = Logger.getLogger(JpaFiscalEntityDao.class);
	
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

	@Override
	public List<FiscalEntity> listar() {
		return listar(0, 0, null) ;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<FiscalEntity> listar(int inicio, int cantidad, Filters<Filter> filters) {
		String fiscalName = "";
		String where = "";
		String stringQuery = "SELECT x FROM FiscalEntity x";
		if (filters != null && filters.size() > 0) {
			fiscalName = filters.get(0).getPattern();
			if (!"".equals(fiscalName)) {
				where = " WHERE LOWER(x.fiscalName) like :fiscalName";
			}
		}
		if (!"".equals(where)) {
			stringQuery += where;
		}
		try{
			Query query = entityManager.createQuery(stringQuery);
			if (!"".equals(where)) {
				query.setParameter("fiscalName", "%" + fiscalName.toLowerCase() + "%");
			}
			if (cantidad != 0) {
				query.setFirstResult(inicio); 
				query.setMaxResults(cantidad);
			}
			List<FiscalEntity> recordList = (List<FiscalEntity>)query.getResultList();
			return recordList;
		}catch( NoResultException e){
	    	logger.error(e.getLocalizedMessage(), e);
			return null;
		}
		
	}

	@Override
	public FiscalEntity get(String rfc) {
		try {
			String jsql = "SELECT x FROM FiscalEntity x WHERE x.taxID=:taxId";
			Query q = entityManager.createQuery(jsql);
			q.setParameter("taxId", rfc);
			try {                
				return  (FiscalEntity) q.getSingleResult();            
			} 
			catch (NonUniqueResultException e) {   
				logger.info("Hay mas de una entidad fiscal con un el mismo rfc:"+rfc);
				e.printStackTrace();
				return (FiscalEntity) q.getResultList().get(0);            
			}        
		} 
		catch (NoResultException e) {
			logger.info("Hay mas de una entidad fiscal con un el mismo rfc:"+rfc);
			e.printStackTrace();
			return null;  
		}
	}

	@Override
	public FiscalEntity findByRFCA(FiscalEntity fiscalEntity) {
		try{
		String rfc = fiscalEntity.getTaxID();
		String stringQuery = "SELECT x FROM FiscalEntity x WHERE x.taxID=:taxID";
		Query query = entityManager.createQuery(stringQuery);
		query.setParameter("taxID", rfc);
			try {                
				return  (FiscalEntity) query.getSingleResult();            
			} catch (NonUniqueResultException e) {                
				return (FiscalEntity) query.getResultList().get(0);            
				}        
		} catch (NoResultException e) 
		{ return null;  }
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<FiscalEntity> listAllFiscalEntity(String ids) {
		List<FiscalEntity> recordList = null;
		String stringQuery = "SELECT x FROM FiscalEntity x ";
		stringQuery += UtilManager.in(ids);
		Query query = entityManager.createQuery(stringQuery);
		try {
			recordList = query.getResultList();
		} catch (NoResultException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
		return recordList;
	}
	
	
}
