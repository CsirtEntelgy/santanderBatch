package com.interfactura.firmalocal.dao.impl;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.interfactura.firmalocal.dao.CfdDao;
import com.interfactura.firmalocal.domain.entities.CFD;

@Component
public class JpaCfdDao extends JpaDao<Long,CFD> implements CfdDao
{
	
	@Override
	public List<CFD> findCFDbyRFC(String rfc) 
	{
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<CFD> findCFDToValidate(String rfc, String serie, int year) 
	{
		String sql = "select X from CFD X where X.rfc = :rfcParam and X.serie = :serieParam and X.approbationYear = :aprobationYearParam";
		List<CFD> recordList = null;
		Query tquery = entityManager.createQuery(sql);
		tquery.setParameter("rfcParam", rfc);
		tquery.setParameter("serieParam", serie);
		tquery.setParameter("approbationYearParam", year );
		try {
			recordList = tquery.getResultList();
		} catch (NoResultException e) {
			logger.error(e.getLocalizedMessage(),e);
		}
		return recordList;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<CFD> findFolioRange(String rfc, String serie, Long approbationNumber, Integer approbationYear) 
	{
		String sql = "select X from CFD X where X.rfc = :rfcParam and X.serie = :serieParam and X.approbationNumber = :approbationNumberParam and X.approbationYear = :aprobationYearParam";		
		List<CFD> recordList = null;		
		Query tquery = entityManager.createQuery(sql);
		tquery.setParameter("rfcParam", rfc);
		tquery.setParameter("serieParam", serie);
		tquery.setParameter("approbationNumberParam", approbationNumber );
		tquery.setParameter("approbationYearParam", approbationYear );
		try {
			recordList = tquery.getResultList();
		} catch (NoResultException e) {
			logger.error(e.getLocalizedMessage(),e);
		}
		return recordList;
	}
	
	private static final Logger logger = Logger.getLogger(JpaCfdDao.class);
	
}
