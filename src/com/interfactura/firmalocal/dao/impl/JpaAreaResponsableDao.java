package com.interfactura.firmalocal.dao.impl;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.AreaResponsableDao;
import com.interfactura.firmalocal.domain.entities.AreaResponsable;

@Component
public class JpaAreaResponsableDao extends JpaDao<Long, AreaResponsable> implements AreaResponsableDao {

	private static final Logger logger = Logger.getLogger(JpaAreaResponsableDao.class);
	
	@Override
	@SuppressWarnings("unchecked")
	public List<AreaResponsable> listar(int inicio, int cantidad,
			Filters<Filter> filters) {
		// TODO Auto-generated method stub
		String nombre = "";
		String where = "";
		String stringQuery = "SELECT x FROM AreaResponsable x";
		if(filters != null && filters.size() > 0){
			nombre = filters.get(0).getPattern();
			if(!"".equals(nombre)){
				where = " WHERE x.nombre like :nombre";
			}
		}
		if(!"".equals(where)){
			stringQuery += where;
		}
		Query query = entityManager.createQuery(stringQuery);
		if(!"".equals(where)){
			query.setParameter("nombre", "%" + nombre + "%");
		}
		if(cantidad != 0){
			query.setFirstResult(inicio);
			query.setMaxResults(cantidad);
		}
		List<AreaResponsable> recordList = query.getResultList();
		
		return recordList;
	}
	
	@Override
	public AreaResponsable findByNombre(String nombre) {
		// TODO Auto-generated method stub
		try{
			String jsql = "SELECT x FROM AreaResponsable x WHERE x.nombre=:nombre";
			Query q = entityManager.createQuery(jsql);
			q.setParameter("nombre", nombre.trim().toUpperCase());
			try{
				return (AreaResponsable) q.getSingleResult();
			}catch(NonUniqueResultException e){
				logger.error(e.getLocalizedMessage(), e);
				return (AreaResponsable) q.getResultList().get(0);
			}
		}catch(NoResultException e){
			logger.error(e.getLocalizedMessage(), e);
			return null;
		}		
	}

	
	@Override
	public List<AreaResponsable> listar() {
		// TODO Auto-generated method stub
		return listar(0, 0, null);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<AreaResponsable> listarAreas() throws Exception {
			
		String stringQuery = "SELECT x FROM AreaResponsable x WHERE x.status = 1 ";
		
		Query query = entityManager.createQuery(stringQuery);
	
		List<AreaResponsable> recordList = query.getResultList();
		return recordList;
	}

}

