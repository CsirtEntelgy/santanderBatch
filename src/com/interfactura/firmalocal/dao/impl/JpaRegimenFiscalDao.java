package com.interfactura.firmalocal.dao.impl;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.RegimenFiscalDao;
import com.interfactura.firmalocal.domain.entities.RegimenFiscal;

/**
 * Implementación de {@link RegimenFiscalDao}
 * 
 * @author hlara
 * 
 */
@Component
public class JpaRegimenFiscalDao extends JpaDao<Long, RegimenFiscal> implements	RegimenFiscalDao {
	private static final Logger logger = Logger.getLogger(JpaRegimenFiscalDao.class);

	@Override
	public List<RegimenFiscal> listar() {
		return listar(0, 0, null);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<RegimenFiscal> listar(int inicio, int cantidad, Filters<Filter> filters) {
		String name = "";
		String where = "";
		String stringQuery = "SELECT x FROM RegimenFiscal x";
		if (filters != null && filters.size() > 0) {
			name = filters.get(0).getPattern();
			if (!"".equals(name)) {
				where = " WHERE x.name like :name";
			}
		}
		if (!"".equals(where)) {
			stringQuery += where;
		}
		Query query = entityManager.createQuery(stringQuery);
		if (!"".equals(where)) {
			query.setParameter("name", "%" + name + "%");
		}
		if (cantidad != 0) {
			query.setFirstResult(inicio);
			query.setMaxResults(cantidad);
		}

		List<RegimenFiscal> recordList = query.getResultList();
		return recordList;
	}

	@Override
	public RegimenFiscal findByName(String name) {
		try {
			String jsql = "SELECT x FROM RegimenFiscal x WHERE x.name=:name";
			Query q = entityManager.createQuery(jsql);
			q.setParameter("name", name);
			try {
				return (RegimenFiscal) q.getSingleResult();
			} catch (NonUniqueResultException e) {
				logger.error(e.getLocalizedMessage(), e);
				return (RegimenFiscal) q.getResultList().get(0);
			}
		} catch (NoResultException e) {
			logger.error(e.getLocalizedMessage(), e);
			return null;
		}
	}

	@Override
	public RegimenFiscal findByCode(String code) {
		try {
			String jsql = "SELECT x FROM RegimenFiscal x WHERE x.code=:code";
			Query q = entityManager.createQuery(jsql);
			q.setParameter("code", code);
			try {
				return (RegimenFiscal) q.getSingleResult();
			} catch (NonUniqueResultException e) {
				logger.error(e.getLocalizedMessage(), e);
				return (RegimenFiscal) q.getResultList().get(0);
			}
		} catch (NoResultException e) {
			logger.error(e.getLocalizedMessage(), e);
			return null;
		}
	}

}
