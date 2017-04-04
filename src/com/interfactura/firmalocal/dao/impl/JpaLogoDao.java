package com.interfactura.firmalocal.dao.impl;

import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.springframework.stereotype.Component;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.LogoDao;
import com.interfactura.firmalocal.domain.entities.Logo;
import com.interfactura.firmalocal.persistence.UtilManager;

@Component
public class JpaLogoDao extends JpaDao<Long, Logo> implements LogoDao {

	@Override
	public List<Logo> listAll() {
		String strQuery = "Select x From Logo x";
		Query query = entityManager.createQuery(strQuery);
		@SuppressWarnings("unchecked")
		List<Logo> logos = (List<Logo>)query.getResultList();
		return logos;
	}

	@Override
	public Logo getByFEId(long id, Date fecha) {
		String strQuery = "Select x From Logo x where x.fiscalEntity.id = :idEntity and x.startDate <= :date and x.finalDate >= :date";
		Query query = entityManager.createQuery(strQuery);
		query.setParameter("idEntity", id);
		query.setParameter("date", fecha);
	try {
		Logo logo = (Logo)query.getSingleResult();
		return logo;
	} catch (NonUniqueResultException e) {
		return (Logo) query.getResultList().get(0);
	} catch (NoResultException e) {
		return null;
	}
	}



	@Override
	public List<Logo> list(int begin, int quantity, Filters<Filter> filters, String feids) {		
		String efName = "";
		String where = "";
		String stringQuery = "SELECT x FROM Logo x ";
		stringQuery += UtilManager.inListar(feids);
		if (filters != null && filters.size() > 0) {
			for( Filter filter : filters ){
				if ( filter.getColumn().equals("efName") ){
					efName = filter.getPattern();
				}
			}
			if (!"".equals(efName)) {
				where += " AND x.fiscalEntity.fiscalName like :efName";
			}
		}
		if (!"".equals(where)) {
			stringQuery += where;
		}
		stringQuery += " ORDER BY x.fiscalEntity.fiscalName";
		Query query = entityManager.createQuery(stringQuery);
		if (!"".equals(where)) {
			if (!"".equals(efName)) {
				query.setParameter("efName", "%" + efName + "%");
			}
		}
		if (quantity != 0) {
			query.setFirstResult(begin); 
			query.setMaxResults(quantity);
		}
		@SuppressWarnings("unchecked")
		List<Logo> recordList = query.getResultList();
		return recordList;
	}

}
