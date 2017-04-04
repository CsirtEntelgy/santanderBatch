package com.interfactura.firmalocal.dao.impl;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.FolioRangeAlertDao;
import com.interfactura.firmalocal.domain.entities.FolioRangeAlerts;
import com.interfactura.firmalocal.persistence.UtilManager;

@Component
public class JpaFolioRangeAlertDao extends JpaDao<Long, FolioRangeAlerts> implements FolioRangeAlertDao{
	
	private static final Logger logger = Logger.getLogger(JpaFolioRangeAlertDao.class);

	@SuppressWarnings("unchecked")
	@Override
	public List<FolioRangeAlerts> list(int begin, int quantity, Filters<Filter> filters, String ids) {
		List<FolioRangeAlerts> recordList = null;
		String fiscalEntityName = "";
		String where = "";
		String stringQuery = "SELECT x FROM FolioRangeAlerts x ";
		stringQuery += UtilManager.inListar(ids);
		if (filters != null && filters.size() > 0) {
			fiscalEntityName = filters.get(0).getPattern();
			if (!"".equals(fiscalEntityName)) {
				where = " AND x.fiscalEntity.fiscalName like :fiscalEntityName";
			}
		}
		if (!"".equals(where)) {
			stringQuery += where;
		}
		Query query = entityManager.createQuery(stringQuery);
		if (!"".equals(where)) {
			query.setParameter("fiscalEntityName", "%" + fiscalEntityName +  "%");
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
