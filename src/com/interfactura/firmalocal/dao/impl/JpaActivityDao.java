package com.interfactura.firmalocal.dao.impl;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.ActivityDao;
import com.interfactura.firmalocal.domain.entities.Activity;

@Component
public class JpaActivityDao extends JpaDao<Long, Activity> implements ActivityDao{

	private static final Logger logger = Logger.getLogger(JpaActivityDao.class);
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Activity> list(int begin, int quantity, Filters<Filter> filters) {
		List<Activity> recordList = null;
		String ipaddress = "";
		String where = "";
		String stringQuery = "SELECT x FROM Activity x";
		if (filters != null && filters.size() > 0) {
			ipaddress = filters.get(0).getPattern();
			if (!"".equals(ipaddress)) {
				where = " WHERE x.IPADDRESS like :ipaddress";
			}
		}
		if (!"".equals(where)) {
			stringQuery += where;
		}
		Query query = entityManager.createQuery(stringQuery);
		if (!"".equals(where)) {
			query.setParameter("ipaddress", "%" + ipaddress + "%");
		}
		if (quantity != 0) {
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

}
