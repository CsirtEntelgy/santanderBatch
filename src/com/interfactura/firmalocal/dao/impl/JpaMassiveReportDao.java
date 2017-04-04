package com.interfactura.firmalocal.dao.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.MassiveReportDao;
import com.interfactura.firmalocal.domain.entities.MassiveReport;

@Component
public class JpaMassiveReportDao extends JpaDao<Long, MassiveReport> implements MassiveReportDao{

	private static final Logger logger = Logger.getLogger(JpaMassiveReportDao.class);
	
	@SuppressWarnings("unchecked")
	@Override
	public List<MassiveReport> listByStatus(int status, int cfdType) throws Exception {
				
		String and = "";
		List<MassiveReport> list = new ArrayList<MassiveReport>();
		String query = "";
		if(cfdType == 0){
			//MassiveReport Facturas
			query = "SELECT x FROM MassiveReport x WHERE x.cfdtype=0 and x.status=:status";
		}else{
			//MassiveReport Divisas
			query = "SELECT x FROM MassiveReport x WHERE x.cfdtype=1 and x.status=:status";
		}
		
		if (status == 2){
			and = " AND x.endprocessdate < :fechaFinProceso";
			
			query += and;
		}
		Query qC = entityManager.createQuery(query);
		qC.setParameter("status", status);
		
		if(status == 2){
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
	        
	    	Calendar calendar = Calendar.getInstance();
	    	calendar.add(Calendar.DATE, -30);
	    	System.out.println("fechaFinProceso:" + calendar.getTime());
	    	qC.setParameter("fechaFinProceso", sdf.parse(sdf.format(calendar.getTime())));
		}
		
		list = qC.getResultList();
				   
		return list;
		
	}

	
		/*try {
			String jsql = "SELECT x FROM MassiveReport x WHERE x.status=:status";
			Query q = entityManager.createQuery(jsql);
			q.setParameter("status", status);
			try {                
				return  (MassiveReport) q.getSingleResult();            
			} catch (NonUniqueResultException e) { 
				logger.error(e.getLocalizedMessage(), e);
				return (MassiveReport) q.getResultList().get(0);            
				}        
			} catch (NoResultException e){ 	
				logger.error(e.getLocalizedMessage(), e);
				return null;  
			}
	}*/
	
	/*@SuppressWarnings("unchecked")
	@Override
	public List<MassiveReport> list() {
		
		List<MassiveReport> list = new ArrayList<MassiveReport>();
		String query = "select X from MassiveReport X where X.status = 0";
		Query qC = entityManager.createQuery(query);
		
		try{
			list = qC.getResultList();
		}
		catch( NoResultException e ){
			logger.error(e.getLocalizedMessage(),e);
		}
		catch( Exception e ){
			logger.error(e.getLocalizedMessage(),e);
		}
		
		return list;
	}*/

	@Override
	public boolean updateMassive(MassiveReport massiveReport) throws Exception{
		boolean result = false;
		
		update(massiveReport);
		result = true;
			
		return result;
	}
	
	@Override
	public MassiveReport getById( long id ) throws Exception{
		MassiveReport massiveReport = new MassiveReport();
		
		massiveReport = findById( id );
		
		return massiveReport;
	}
	

}
