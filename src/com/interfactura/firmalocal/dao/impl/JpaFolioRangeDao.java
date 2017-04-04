package com.interfactura.firmalocal.dao.impl;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.FolioRangeDao;
import com.interfactura.firmalocal.domain.entities.FiscalEntity;
import com.interfactura.firmalocal.domain.entities.FolioRange;
import com.interfactura.firmalocal.persistence.UtilManager;

@Component
public class JpaFolioRangeDao extends JpaDao<Long, FolioRange> implements FolioRangeDao{

	@SuppressWarnings("unchecked")
	@Override
	public List<FolioRange> list(int begin, int quantity, Filters<Filter> filters, String ids) {
		List<FolioRange> recordList = null;
		//Integer authorizationNumber = null;
		String authorizationNumber = "";
		String sName = "";
		String efName = "";
		String where = "";
		String stringQuery = "SELECT x FROM FolioRange x ";
		stringQuery += UtilManager.inListar(ids);
		if (filters != null && filters.size() > 0) {
			for( Filter filter : filters ){
				if ( filter.getColumn().equals("authorizationNumber") ){
					//authorizationNumber = Integer.parseInt(filter.getPattern());
					authorizationNumber = filter.getPattern();
				}
				if ( filter.getColumn().equals("sName") ){
					sName = filter.getPattern();
				}
				if ( filter.getColumn().equals("efName") ){
					efName = filter.getPattern();
				}
			}
			if (authorizationNumber!=null) {
				where = " AND x.authorizationNumber like :authorizationNumber";
			}
			if (!"".equals(sName)) {
				where += " AND x.series.name like :sName";
			}
			if (!"".equals(efName)) {
				where += " AND x.fiscalEntity.fiscalName like :efName";
			}
		}
		if (!"".equals(where)) {
			stringQuery += where;
		}
		Query query = entityManager.createQuery(stringQuery);
		if (!"".equals(where)) {
			if (authorizationNumber!=null) {
				query.setParameter("authorizationNumber",  authorizationNumber + "%");
			}
			if (!"".equals(sName)) {
				query.setParameter("sName", "%" + sName + "%");
			}
			if (!"".equals(efName)) {
				query.setParameter("efName", "%" + efName + "%");
			}
		}
		if (quantity != 0) {
			query.setFirstResult(begin); 
			query.setMaxResults(quantity);
		}
		try {
			recordList = query.getResultList();
		} catch (NoResultException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
		return recordList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<FolioRange> listActive(String nameSerie, long idEntityFiscal) {
		List<FolioRange> recordList = null;
		String sql = "SELECT x FROM FolioRange x where x.estatus='ACTIVO' AND x.fiscalEntity.id = :idFiscal and ( ( x.finalFolio - x.actualFolio ) > 0 )";
		if ((nameSerie != null) && (nameSerie.length() > 0)) {
			sql += " AND x.series.name=:nameSerie ";
		}
		Query query = entityManager.createQuery(sql);
		query.setParameter("idFiscal", idEntityFiscal);
		if ((nameSerie != null) && (nameSerie.length() > 0)) {
			query.setParameter("nameSerie", nameSerie);
		}
		try {
			recordList = query.getResultList();
		} catch (NoResultException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
		return recordList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<FolioRange> list(String nameSerie, long idEntityFiscal) {
		List<FolioRange> recordList = null;
		Query query = entityManager.createQuery("SELECT x FROM FolioRange x " +
												"where x.series.name=:nameSerie " +
												"AND x.estatus='ACTIVO' " +
												"AND x.fiscalEntity.id=:idFiscal");
		query.setParameter("nameSerie", nameSerie);
		query.setParameter("idFiscal", idEntityFiscal);
		try {
			recordList = query.getResultList();
		} catch (NoResultException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
		return recordList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<FolioRange> list(FiscalEntity fiscalEntity) {
		List<FolioRange> recordList = null;
		Query q = entityManager.createQuery("SELECT x FROM FolioRange x where x.fiscalEntity.id = :fiscalEntityId");
		q.setParameter("fiscalEntityId", fiscalEntity.getId());
		try {
			recordList = q.getResultList();
		} catch (NoResultException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
		return recordList;
	}

	@Override
	public FolioRange findByFiscalEntityAndSeries(long fiscalEntityId, long seriesId) {
		FolioRange folioRange = null;
		Query q = null;
		try{
			q = entityManager.createQuery("SELECT x FROM FolioRange x " +
												"where x.series.id=:seriesId " +
												"AND x.estatus='ACTIVO' " +
												"AND x.fiscalEntity.id=:fiscalEntityId");
			q.setParameter("fiscalEntityId", fiscalEntityId);
			q.setParameter("seriesId", seriesId);
			folioRange = (FolioRange) q.getSingleResult();
	 	}catch( NoResultException e){
	    	logger.error(e.getLocalizedMessage(), e);
		}catch( NonUniqueResultException e){
			logger.error(e.getLocalizedMessage(), e);
			folioRange = (FolioRange) q.getResultList().get(0); 
		}
		return folioRange;
	}
	
	@Override
	public void updateFolios(List<FolioRange> foliosToUpdate) {
		for( FolioRange folioRange : foliosToUpdate ){
			super.update(folioRange);
		}
		
	}
	
	private static final Logger logger = Logger.getLogger(JpaFolioRangeDao.class);

	

}