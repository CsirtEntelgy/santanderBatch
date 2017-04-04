package com.interfactura.firmalocal.dao.impl;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.CodigoISODao;
import com.interfactura.firmalocal.domain.entities.CodigoISO;

@Component
public class JpaCodigoISODao extends JpaDao<Long, CodigoISO> implements CodigoISODao {

	private static final Logger logger = Logger.getLogger(JpaCodigoISODao.class);
	
	@Override
	@SuppressWarnings("unchecked")
	public List<CodigoISO> listar(int inicio, int cantidad,
			Filters<Filter> filters) {
		// TODO Auto-generated method stub
		String descripcion = "";
		String where = "";
		String stringQuery = "SELECT x FROM CodigoISO x";
		if(filters != null && filters.size() > 0){
			descripcion = filters.get(0).getPattern();
			if(!"".equals(descripcion)){
				where = " WHERE x.descripcion like :descripcion";
			}
		}
		if(!"".equals(where)){
			stringQuery += where;
		}
		Query query = entityManager.createQuery(stringQuery);
		if(!"".equals(where)){
			query.setParameter("descripcion", "%" + descripcion + "%");
		}
		if(cantidad != 0){
			query.setFirstResult(inicio);
			query.setMaxResults(cantidad);
		}
		List<CodigoISO> recordList = query.getResultList();
		
		return recordList;
	}
	
	@Override
	public CodigoISO findByDescripcion(String descripcion) {
		// TODO Auto-generated method stub
		try{
			String jsql = "SELECT x FROM CodigoISO x WHERE x.descripcion=:descripcion";
			Query q = entityManager.createQuery(jsql);
			q.setParameter("descripcion", descripcion.trim().toUpperCase());
			try{
				return (CodigoISO) q.getSingleResult();
			}catch(NonUniqueResultException e){
				logger.error(e.getLocalizedMessage(), e);
				return (CodigoISO) q.getResultList().get(0);
			}
		}catch(NoResultException e){
			logger.error(e.getLocalizedMessage(), e);
			return null;
		}		
	}

	@Override
	public CodigoISO findByCodigo(String codigo) {
		// TODO Auto-generated method stub
		try{
			String jsql = "SELECT x FROM CodigoISO x WHERE x.codigo=:codigo";
			Query q = entityManager.createQuery(jsql);
			q.setParameter("codigo", codigo.trim().toUpperCase());
			try{
				return (CodigoISO) q.getSingleResult();
			}catch(NonUniqueResultException e){
				logger.error(e.getLocalizedMessage(), e);
				return (CodigoISO) q.getResultList().get(0);
			}
		}catch(NoResultException e){
			logger.error(e.getLocalizedMessage(), e);
			return null;
		}	
	}

	@Override
	public List<CodigoISO> listar() {
		// TODO Auto-generated method stub
		return listar(0, 0, null);
	}

}
