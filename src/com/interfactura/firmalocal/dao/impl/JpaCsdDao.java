package com.interfactura.firmalocal.dao.impl;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.interfactura.firmalocal.dao.CsdDao;
import com.interfactura.firmalocal.domain.entities.CSD;

@Component
public class JpaCsdDao extends JpaDao<Long,CSD> implements CsdDao
{	
	@Override
	public List<CSD> findCSDbyRFC(String rfc) {
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<CSD> findCSDToValidate( String certificado, String rfc )
	{
		String sql = "select X from CSD X where X.rfc = :rfcParam and X.certificateNumber = :certificateNumberParam";
		List<CSD> recordList = null;	
		Query tquery = entityManager.createQuery(sql);
		tquery.setParameter("rfcParam", rfc);
		tquery.setParameter("certificateNumberParam", certificado);
		try {
			recordList = tquery.getResultList();
		} catch (NoResultException e) {
			logger.error(e.getLocalizedMessage(),e);
		}
		return recordList;
	}
	
	private static final Logger logger = Logger.getLogger(JpaCsdDao.class);
	
}