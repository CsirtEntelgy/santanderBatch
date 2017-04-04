package com.interfactura.firmalocal.dao.impl;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.FacturasDao;
import com.interfactura.firmalocal.domain.entities.Facturas;

@Component
public class JpaFacturasDao extends JpaDao<Long, Facturas> implements FacturasDao {

	private static final Logger logger = Logger.getLogger(JpaCodigoISODao.class);
	
	
	@Override
	public Facturas findByFolioSat(String folioSat) throws Exception{
		// TODO Auto-generated method stub
	
		String jsql = "SELECT x FROM Facturas x WHERE x.FOLIOSAT=:foliosat";
		Query q = entityManager.createQuery(jsql);
		q.setParameter("foliosat", folioSat.trim());
		
		return (Facturas) q.getSingleResult();
	}


	@SuppressWarnings("unchecked")
	@Override
	public List<Facturas> findByNombreDeArchivo(String nombreDeArchivo) throws Exception {
		// TODO Auto-generated method stub
		String jsql = "SELECT x FROM Facturas x WHERE x.NOMBREDEARCHIVO=:nombreDeArchivo";
		Query q = entityManager.createQuery(jsql);
		q.setParameter("nombreDeArchivo", nombreDeArchivo.trim());
		
		return q.getResultList();
			
	}
}

