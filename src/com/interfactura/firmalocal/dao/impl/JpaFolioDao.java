package com.interfactura.firmalocal.dao.impl;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.FolioDao;
import com.interfactura.firmalocal.domain.entities.Folio;

@Component
public class JpaFolioDao extends JpaDao<Long, Folio> implements  FolioDao{
	
	private static final Logger logger = Logger.getLogger(JpaFolioDao.class);

	@SuppressWarnings("unchecked")
	@Override
	public List<Folio> list(int begin, int quantity, Filters<Filter> filters) {
		List<Folio> recordList = null;
		String  currentfolionumber  = "";
		String where = "";
		String stringQuery = "SELECT x FROM Folio x";
		if (filters != null && filters.size() > 0) {
			currentfolionumber = filters.get(0).getPattern();
			if (!"".equals(currentfolionumber)) {
				where = " WHERE x.currentfolionumber = :currentfolionumber";
			}
		}
		if (!"".equals(where)) {
			stringQuery += where;
		}
		Query query = entityManager.createQuery(stringQuery);
		if (!"".equals(where)) {
			query.setParameter("currentfolionumber",currentfolionumber);
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

}
