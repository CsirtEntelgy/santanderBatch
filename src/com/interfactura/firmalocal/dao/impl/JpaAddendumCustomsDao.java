package com.interfactura.firmalocal.dao.impl;

import java.util.List;

import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.AddendumCustomsDao;
import com.interfactura.firmalocal.domain.entities.AddendumCustoms;

@Component
public class JpaAddendumCustomsDao extends JpaDao<Long, AddendumCustoms> implements AddendumCustomsDao{

	private static final Logger logger = Logger.getLogger(JpaAddendumCustomsDao.class);
	
	@SuppressWarnings("unchecked")
	@Override
	public List<AddendumCustoms> listar(int inicio, int cantidad, Filters<Filter> filters) {
		String pedimento = "";
		String where = "";
		String stringQuery = "SELECT x FROM AddendumCustoms x";
		logger.debug(stringQuery);
		if (filters != null && filters.size() > 0) {
			pedimento = filters.get(0).getPattern();
			if (!"".equals(pedimento)) {
				where = " WHERE x.pedimento like :pedimento";
			}
		}
		if (!"".equals(where)) {
			stringQuery += where;
		}
		Query query = entityManager.createQuery(stringQuery);
		if (!"".equals(where)) {
			query.setParameter("pedimento", "%" + pedimento + "%");
		}
		if (cantidad != 0) {
			query.setFirstResult(inicio); 
			query.setMaxResults(cantidad);
		}
		List<AddendumCustoms> recordList = query.getResultList();
		return recordList;
	}

	
}
