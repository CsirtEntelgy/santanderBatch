package com.interfactura.firmalocal.dao.impl;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.PageDao;
import com.interfactura.firmalocal.domain.entities.Page;

@Component
public class JpaPageDao extends JpaDao<Long, Page> implements PageDao{

	@SuppressWarnings("unchecked")
	@Override
	public List<Page> list(int begin, int quantity, Filters<Filter> filters) {
		String name = "";
		String where = "";
		List<Page> recordList = null;
		String stringQuery = "SELECT x FROM Page x";
		if (filters != null && filters.size() > 0) {
			name = filters.get(0).getPattern();
			if (!"".equals(name)) {
				where = " WHERE LOWER(x.name) like :name";
			}
		}
		if (!"".equals(where)) {
			stringQuery += where;
		}
		Query query = entityManager.createQuery(stringQuery);
		if (!"".equals(where)) {
			query.setParameter("name", "%" + name.toLowerCase() + "%");
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

	private static final Logger logger = Logger.getLogger(JpaPageDao.class);

}

