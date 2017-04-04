package com.interfactura.firmalocal.dao.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.CfdReceivedDao;
import com.interfactura.firmalocal.domain.entities.CFDReceived;

@Component
public class JpaCfdReceivedDao extends JpaDao<Long, CFDReceived> implements CfdReceivedDao{
	
	private static final Logger logger = Logger.getLogger(JpaCfdReceivedDao.class);
	
	public CFDReceived findCFD(Long folio, String rfcEmisor, String serie) {
		String strquery = "SELECT x FROM CFDReceived x WHERE x.folio = :folio " +
				                                     "AND x.taxIDEmisor = :rfcEmisor " +
				                                     "AND x.serie = :serie";
        CFDReceived cfdr = null;
	    try{
	    	Query query = entityManager.createQuery(strquery);
			query.setParameter("folio", folio);
			query.setParameter("rfcEmisor", rfcEmisor);
			query.setParameter("serie", serie);
			cfdr = (CFDReceived)query.getSingleResult();
	    }catch( NoResultException e){
	    	logger.error(e.getLocalizedMessage(), e);
		}catch( NonUniqueResultException e){
			logger.error(e.getLocalizedMessage(), e);
		}
	    return cfdr;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<CFDReceived> list(int begin, int quantity, Filters<Filter> filters) {
		logger.info("listado CFD's");
		List<CFDReceived> recordList = null;
		Long folio = null;
		String rfcEmisor = null;
		String rfcReceptor = null;
		Date dateBegin = null;
		Date dateEnd = null;	
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		String where = "";
		String stringQuery = "SELECT x FROM CFDReceived x";
		if (filters != null && filters.size() > 0) {
			for( Filter filter : filters ){
				if ( filter.getColumn().equals("folio") ) {
					try
					{
					folio = Long.parseLong(filter.getPattern());
					logger.debug(folio);
					}
					catch(NumberFormatException ex)
					{
						ex.printStackTrace();
						folio = 0l;
					}
				}
				if ( filter.getColumn().equals("rfcreceptor") ){
		        	rfcReceptor = filter.getPattern();
		        	logger.debug(rfcReceptor);
		        }
				if ( filter.getColumn().equals("rfcemisor") ){
					rfcEmisor = filter.getPattern();
		        	logger.debug(rfcEmisor);
		        }
		        if ( filter.getColumn().equals("rbegin") ){
		        	try {
		        		logger.debug(filter.getPattern());
						dateBegin = format.parse(filter.getPattern());
						logger.debug(dateBegin);
					} catch (ParseException ex) 
					{
						Calendar cal = Calendar.getInstance();
						cal.setTime(new Date());
						cal.add(Calendar.YEAR, 1);
						dateBegin = cal.getTime();
						ex.printStackTrace();
					}
		        }
		        if ( filter.getColumn().equals("rend") ){
		        	try {
		        		logger.debug(filter.getPattern());
						dateEnd = format.parse(filter.getPattern());
						logger.debug(dateEnd);
					} catch (ParseException ex) 
					{
						Calendar cal = Calendar.getInstance();
						cal.setTime(new Date());
						cal.add(Calendar.YEAR, -1);
						dateEnd = cal.getTime();
						ex.printStackTrace();
					}
		        }
			}
			if (folio!= null) {
				where = " WHERE x.folio = :folio";
				if (rfcReceptor!= null) {
					where += " AND x.taxIDReceptor like :rfcReceptor";
				}	
				if (rfcEmisor!= null) {
					where += " AND x.taxIDEmisor like :rfcEmisor";
				}	
				if (dateBegin!= null) {
					where += " AND x.dateOfReception >= :dateBegin";
				}
				if (dateEnd!= null) {
					where += " AND x.dateOfReception <= :dateEnd";
				}
			}else if(rfcReceptor!= null){
				where = " WHERE x.taxIDReceptor like :rfcReceptor";
				if (rfcEmisor!= null) {
					where += " AND x.taxIDEmisor like :rfcEmisor";
				}	
				if (dateBegin!= null) {
					where += " AND x.dateOfReception >= :dateBegin";
				}
				if (dateEnd!= null) {
					where += " AND x.dateOfReception <= :dateEnd";
				}
			}
			else if(rfcEmisor != null){
				where = " WHERE x.taxIDEmisor like :rfcEmisor";
				if (dateBegin != null) {
					where += " AND x.dateOfReception >= :dateBegin";
				}
				if (dateEnd != null) {
					where += " AND x.dateOfReception <= :dateEnd";
				}
			}
			else if(dateBegin != null){
				where = " WHERE x.dateOfReception >= :dateBegin";
				if (dateEnd != null) {
					where += " AND x.dateOfReception <= :dateEnd";
				}
			}
			else if(dateEnd != null) {
				where = " WHERE x.dateOfReception <= :dateEnd";
			}
		}
		if (!"".equals(where)) {
			stringQuery += where;
		}
		
		logger.debug(stringQuery);
		
		Query query = entityManager.createQuery(stringQuery);
		
		if (folio!= null) {
			query.setParameter("folio", folio);
			if (rfcReceptor!= null) {
				query.setParameter("rfcReceptor", "%" + rfcReceptor + "%");
			}	
			if (rfcEmisor!= null) {
				query.setParameter("rfcEmisor", "%" + rfcEmisor + "%");
			}	
			if (dateBegin!= null) {
				query.setParameter("dateBegin", dateBegin);
			}
			if (dateEnd!= null) {
				query.setParameter("dateEnd", dateEnd);
			}
		}else if(rfcReceptor!= null){
			query.setParameter("rfcReceptor", "%" + rfcReceptor + "%");
			if (rfcEmisor!= null) {
				query.setParameter("rfcEmisor", "%" + rfcEmisor + "%");
			}	
			if (dateBegin!= null) {
				query.setParameter("dateBegin", dateBegin);
			}
			if (dateEnd!= null) {
				query.setParameter("dateEnd", dateEnd);
			}
		}
		else if(rfcEmisor != null){
			query.setParameter("rfcEmisor", "%" + rfcEmisor + "%");
			if (dateBegin != null) {
				query.setParameter("dateBegin", dateBegin);
			}
			if (dateEnd != null) {
				query.setParameter("dateEnd", dateEnd);
			}
		}
		else if(dateBegin != null){
			query.setParameter("dateBegin", dateBegin);
			if (dateEnd != null) {
				query.setParameter("dateEnd", dateEnd);
			}
		}
		else if(dateEnd != null){
			query.setParameter("dateEnd", dateEnd);
		}

		if (quantity != 0) {
			query.setFirstResult(begin); 
			query.setMaxResults(quantity);
		}
		logger.debug(query.toString());
		
		try {
			recordList = query.getResultList();
		} catch (NoResultException e) {
			logger.error(e.getLocalizedMessage(),e);
		}
		
		return recordList;
	}

}
