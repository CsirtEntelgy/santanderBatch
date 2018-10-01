package com.interfactura.firmalocal.dao.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.interfactura.firmalocal.dao.MassiveDao;
import com.interfactura.firmalocal.domain.entities.Massive;


@Component
public class JpaMassiveDao extends JpaDao<Long, Massive> implements MassiveDao {

	//private static final Logger logger = Logger.getLogger(JpaMassiveDao.class);
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Massive> listByStatus(int status, int cfdType) throws Exception {
				
		String and = "";
		List<Massive> list = new ArrayList<Massive>();
		String query = "";
		if(cfdType == 0){
			//Massive Facturas
			query = "SELECT x FROM Massive x WHERE x.cfdtype = 0 and x.status=:status";
		}else if(cfdType == 1){
			//Massive Divisas
			query = "SELECT x FROM Massive x WHERE x.cfdtype = 1 and x.status=:status";
		}else if(cfdType == 2){
			//Massive Donat
			query = "SELECT x FROM Massive x WHERE x.cfdtype = 2 and x.status=:status";
		}else if(cfdType == 3) {
			//Masive ComplementoPago
			query = "SELECT x FROM Massive x WHERE x.cfdtype = 3 and x.status=:status";
		}else if(cfdType == 4) {
			//Masive Quitas
			query = "SELECT x FROM Massive x WHERE x.cfdtype = 4 and x.status=:status";
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


	@SuppressWarnings("unchecked")
	@Override
	public List<Massive> listDepuraStatusNoProcesado(int cfdType) throws Exception {
				
		
		List<Massive> list = new ArrayList<Massive>();
		String query = "";
		if(cfdType == 0){
			//Massive Facturas
			query = "SELECT x FROM Massive x WHERE x.cfdtype = 0 and x.status=0";
		}if(cfdType == 1){
			//Massive Divisas
			query = "SELECT x FROM Massive x WHERE x.cfdtype = 1 and x.status=0";
		}if (cfdType == 4) 
			//Massive quitas
			query = "SELECT x FROM Massive x WHERE x.cfdtype = 4 and x.status=0";
		else{
			//Massive Donat
			query = "SELECT x FROM Massive x WHERE x.cfdtype = 2 and x.status=0";
		}
		
		query += " AND x.creationDate < :fechaCreacion";
			
		Query qC = entityManager.createQuery(query);
			
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        
    	Calendar calendar = Calendar.getInstance();
    	calendar.add(Calendar.DATE, -30);
    	System.out.println("fechaCreacion:" + calendar.getTime());
    	qC.setParameter("fechaCreacion", sdf.parse(sdf.format(calendar.getTime())));
	
		
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
	public boolean updateMassive(Massive massive) throws Exception{
		boolean result = false;
		
		update(massive);
		result = true;
			
		return result;
	}
	
	@Override
	public Massive getById( long id ) throws Exception{
		Massive massive = new Massive();
				
		massive = findById( id );
		
		return massive;
	}
	
}
