package com.interfactura.firmalocal.dao.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.CfdIssuedDao;
import com.interfactura.firmalocal.domain.entities.CFDIssued;
import com.interfactura.firmalocal.persistence.UtilManager;
import com.interfactura.firmalocal.xml.util.Util;

@Component
public class JpaCfdIssuedDao extends JpaDao<Long, CFDIssued> implements CfdIssuedDao{

	@Override
	@SuppressWarnings("unchecked")
	public List<CFDIssued> list(int begin, int quantity, Filters<Filter> filters) {
		logger.info("listado State");
		List<CFDIssued> recordList = null;
		Long folio = null;
		String rfcReceptor = "";
		String rfcEmisor = "";
		Date dateBegin = null;
		Date dateEnd = null;
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		String where = "";
		String stringQuery = "SELECT x FROM CFDIssued x";
		if (filters != null && filters.size() > 0){
			for( Filter filter : filters ){
				if ( filter.getColumn().equals("folio") ){
					try{
						folio = Long.parseLong(filter.getPattern());
						logger.debug(folio);
					}catch(NumberFormatException e){	
						folio = null;
						e.printStackTrace();	
					}
					
				}
				if ( filter.getColumn().equals("rfcReceptor") ){
		        	rfcReceptor = filter.getPattern();
		        	logger.debug(rfcReceptor);
		        }
				if ( filter.getColumn().equals("rfcEmisor") ){
		        	rfcEmisor = filter.getPattern();
		        	logger.debug(rfcEmisor);
		        }
		        if ( filter.getColumn().equals("rbegin") ){
		        	try{
		        		logger.debug(filter.getPattern());
						dateBegin = format.parse(filter.getPattern());
						logger.debug(dateBegin);
					}catch (ParseException ex){	
						ex.printStackTrace();	
					}
		        }
		        if ( filter.getColumn().equals("rend") ){
		        	try{
		        		logger.debug(filter.getPattern());
						dateEnd = format.parse(filter.getPattern());
						logger.debug(dateEnd);
					}catch (ParseException ex){	
						ex.printStackTrace();	}
		        }
			}
			if ((folio != null)&&(folio != 0)) 
			{
				where = " WHERE x.folio = :folioParam";
				if ((rfcReceptor!=null)&&(rfcReceptor.length() > 0)) 
				{	where += " AND x.taxIdReceiver like :rfcReceptor";	}
				if ((rfcEmisor!=null)&&(rfcEmisor.length() > 0)) 
				{	where += " AND x.fiscalEntity.taxID like :rfcEmisor";	}
				if (dateBegin!=null) 
				{	where += " AND x.dateOfIssuance >= :dateBegin";	}
				if (dateEnd!=null) 
				{	where += " AND x.dateOfIssuance <= :dateEnd";	}
			}
			else if(!"".equals(rfcReceptor))
			{
				where = " WHERE x.taxIdReceiver like :rfcReceptor";
				if ((rfcEmisor!=null)&&(rfcEmisor.length() > 0)) 
				{	where += " AND x.fiscalEntity.taxID like :rfcEmisor";	}
				if (dateBegin!=null) 
				{	where += " AND x.dateOfIssuance >= :dateBegin";	}
				if (dateEnd!=null) 
				{	where += " AND x.dateOfIssuance <= :dateEnd";	}
			}
			else if(!"".equals(rfcEmisor))
			{
				where = " WHERE x.fiscalEntity.taxID like :rfcEmisor";
				if (dateBegin!=null) 
				{	where += " AND x.dateOfIssuance >= :dateBegin";	}
				if (dateEnd!=null) 
				{	where += " AND x.dateOfIssuance <= :dateEnd";	}
			}
			else if(dateBegin!=null)
			{
				where = " WHERE x.dateOfIssuance >= :dateBegin";
				if (dateEnd!=null) 
				{	where += " AND x.dateOfIssuance <= :dateEnd";	}
			}
			else if(dateEnd!=null)
			{	where = " WHERE x.dateOfIssuance <= :dateEnd";	}
		}
		if (!"".equals(where)) 
		{	stringQuery += where;	}
		stringQuery+=" ORDER BY x.creationDate desc";
		Query query = entityManager.createQuery(stringQuery);
		if (!"".equals(where)) 
		{
			if (folio != null) 
			{	query.setParameter("folioParam", String.valueOf(folio));	}
			if (!"".equals(rfcReceptor)) 
			{	query.setParameter("rfcReceptor", "%" + rfcReceptor + "%");	}
			if (!"".equals(rfcEmisor)) 
			{	query.setParameter("rfcEmisor", "%" + rfcEmisor + "%");	}
			if (dateBegin != null) 
			{	query.setParameter("dateBegin", dateBegin);	}
			if (dateEnd != null) 
			{	query.setParameter("dateEnd", dateEnd);	}
		}
		if (quantity != 0) 
		{
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
	@SuppressWarnings("unchecked")
	public List<CFDIssued> list(int begin, int quantity, Filters<Filter> filters, String ids) {
		logger.info("listado State");
		List<CFDIssued> recordList = null;
		Long folio = null;
		String rfcReceptor = "";
		String rfcEmisor = "";
		Date dateBegin = null;
		Date dateEnd = null;
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		String where = "";
		String stringQuery = "SELECT x FROM CFDIssued x";
		if(ids.length()==0){
			ids="-1";
		}
		
		if (filters != null && filters.size() > 0){
			for( Filter filter : filters ){
				if ( filter.getColumn().equals("folio") ){
					try{
						folio = Long.parseLong(filter.getPattern());
						logger.debug(folio);
					}catch(NumberFormatException e){	
						folio = null;
						e.printStackTrace();	
					}
					
				}
				if ( filter.getColumn().equals("rfcReceptor") ){
		        	rfcReceptor = filter.getPattern();
		        	logger.debug(rfcReceptor);
		        }
				if ( filter.getColumn().equals("rfcEmisor") ){
		        	rfcEmisor = filter.getPattern();
		        	logger.debug(rfcEmisor);
		        }
		        if ( filter.getColumn().equals("rbegin") ){
		        	try{
		        		logger.debug(filter.getPattern());
						dateBegin = format.parse(filter.getPattern());
						logger.debug(dateBegin);
					}catch (ParseException ex){	
						ex.printStackTrace();	
					}
		        }
		        if ( filter.getColumn().equals("rend") ){
		        	try{
		        		logger.debug(filter.getPattern());
						dateEnd = format.parse(filter.getPattern());
						logger.debug(dateEnd);
					}catch (ParseException ex){	
						ex.printStackTrace();	}
		        }
			}
			if ((folio != null)&&(folio != 0)) 
			{
				where = " WHERE x.folio = :folioParam";
				if ((rfcReceptor!=null)&&(rfcReceptor.length() > 0)) 
				{	where += " AND x.taxIdReceiver like :rfcReceptor";	}
				if ((rfcEmisor!=null)&&(rfcEmisor.length() > 0)) 
				{	where += " AND x.fiscalEntity.taxID like :rfcEmisor";	}
				if (dateBegin!=null) 
				{	where += " AND x.dateOfIssuance >= :dateBegin";	}
				if (dateEnd!=null) 
				{	where += " AND x.dateOfIssuance <= :dateEnd";	}
			}
			else if(!"".equals(rfcReceptor))
			{
				where = " WHERE x.taxIdReceiver like :rfcReceptor";
				if ((rfcEmisor!=null)&&(rfcEmisor.length() > 0)) 
				{	where += " AND x.fiscalEntity.taxID like :rfcEmisor";	}
				if (dateBegin!=null) 
				{	where += " AND x.dateOfIssuance >= :dateBegin";	}
				if (dateEnd!=null) 
				{	where += " AND x.dateOfIssuance <= :dateEnd";	}
			}
			else if(!"".equals(rfcEmisor))
			{
				where = " WHERE x.fiscalEntity.taxID like :rfcEmisor";
				if (dateBegin!=null) 
				{	where += " AND x.dateOfIssuance >= :dateBegin";	}
				if (dateEnd!=null) 
				{	where += " AND x.dateOfIssuance <= :dateEnd";	}
			}
			else if(dateBegin!=null)
			{
				where = " WHERE x.dateOfIssuance >= :dateBegin";
				if (dateEnd!=null) 
				{	where += " AND x.dateOfIssuance <= :dateEnd";	}
			}
			else if(dateEnd!=null)
			{	where = " WHERE x.dateOfIssuance <= :dateEnd";	}
		}
		if (!"".equals(where)) {	
			stringQuery += where+" AND x.fiscalEntity.id IN ("+ids +") ";	
		} else {
			stringQuery+=UtilManager.in("x.fiscalEntity.id", ids);
		}
		logger.info("Ids entidades "+ids);
		stringQuery+=" ORDER BY x.creationDate desc";
		Query query = entityManager.createQuery(stringQuery);
		if (!"".equals(where)) 
		{
			if (folio != null) 
			{	query.setParameter("folioParam", String.valueOf(folio));	}
			if (!"".equals(rfcReceptor)) 
			{	query.setParameter("rfcReceptor", "%" + rfcReceptor + "%");	}
			if (!"".equals(rfcEmisor)) 
			{	query.setParameter("rfcEmisor", "%" + rfcEmisor + "%");	}
			if (dateBegin != null) 
			{	query.setParameter("dateBegin", dateBegin);	}
			if (dateEnd != null) 
			{	query.setParameter("dateEnd", dateEnd);	}
		}
		if (quantity != 0) 
		{
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
	
	@SuppressWarnings("unchecked")
	@Override
	public List<CFDIssued> list(int year, int month, String rfc) {
		String sql = "select X from CFDIssued X where X.fiscalEntity.taxID = :rfcParam and "
			       + " X.dateOfIssuance >= :dateBeginParam and X.dateOfIssuance < :dateEndParam";
		List<CFDIssued> recordList = null;
		Query q = entityManager.createQuery(sql);
	    q.setParameter("rfcParam", rfc);
	    System.out.println("***Params: " + year + "  --- " + month);
	    q.setParameter("dateBeginParam", Util.rangoFecha(month, year, false));
	    q.setParameter("dateEndParam", Util.rangoFecha(month + 1, year, false));
	    System.out.println("***Inicio: " + Util.rangoFecha(month, year, false));
	    System.out.println("***Fin: " + Util.rangoFecha(month + 1, year, false));
	    try 
	    {
			recordList = q.getResultList();
		} 
	    catch (NoResultException e) 
		{	logger.error(e.getLocalizedMessage(),e);	}
		return recordList;
	}
	
	private static final Logger logger = Logger.getLogger(CfdIssuedDao.class);

	@SuppressWarnings("unchecked")
	@Override
	public List<CFDIssued> listCancel(int year, int month, String rfc) 
	{
		List<CFDIssued> recordList = null;
		String sqlC = "select X from CFDIssued X where X.fiscalEntity.taxID = :rfcParam and  X.cancellationDate >= :dateBeginParam and X.cancellationDate < :dateEndParam ";	
		Query qC = entityManager.createQuery(sqlC);
		qC.setParameter("rfcParam", rfc);
		System.out.println("***Params: " + year + "  --- " + month);
		qC.setParameter("dateBeginParam", Util.rangoFecha(month, year, false));
		qC.setParameter("dateEndParam", Util.rangoFecha(month + 1, year, false));
		System.out.println("***Inicio: " + Util.rangoFecha(month, year, false));
	    System.out.println("***Fin: " + Util.rangoFecha(month + 1, year, false));
		try 
		{	recordList = qC.getResultList();	} 
		catch (NoResultException e) 
		{	logger.error(e.getLocalizedMessage(),e);	}
		return recordList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CFDIssued> list(String nameFile) {
		List<CFDIssued> recordList = null;
		String sqlC = "select X from CFDIssued X where X.sourceFileName = :name ORDER BY X.xmlRoute ASC";	
		Query qC = entityManager.createQuery(sqlC);
		qC.setParameter("name", nameFile);
		try {
			recordList = qC.getResultList();
		} catch (NoResultException e) {
			logger.error(e.getLocalizedMessage(),e);
		}
		return recordList;
	}
	
	@Override
    public CFDIssued getByFolioSat(String folioSat) {
        try{
            String stringQuery = "SELECT x FROM CFDIssued x WHERE x.folioSAT = :folioSat";
            Query query = entityManager.createQuery(stringQuery);
            query.setParameter("folioSat", folioSat);
            try {
                return (CFDIssued) query.getSingleResult();
            } catch (NonUniqueResultException e) {
                return (CFDIssued) query.getResultList().get(0);
            }
        }catch (NoResultException e) {
            return null;
        }
    }
}
