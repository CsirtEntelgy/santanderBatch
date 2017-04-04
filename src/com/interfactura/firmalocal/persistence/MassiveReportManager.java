package com.interfactura.firmalocal.persistence;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.MassiveReportDao;
import com.interfactura.firmalocal.domain.entities.MassiveReport;

@Component
public class MassiveReportManager {
	@Autowired(required=true)
	MassiveReportDao massiveReportDao;
	
	
	public List<MassiveReport> findByStatus(int status, int cfdType) throws Exception 
	{
		return massiveReportDao.listByStatus(status, cfdType);
	}
	
  //  public List<MassiveReport> list(){
	//	
		//return massiveReportDao.list();
		
//	}
	
	public MassiveReport getById( long id ) throws Exception{
		
		return massiveReportDao.getById(id);
	}
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public boolean update( MassiveReport massiveReport ) throws Exception{
		
		return massiveReportDao.updateMassive(massiveReport);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void delete( long id ) throws Exception{
		MassiveReport massiveReport = massiveReportDao.getById(id);
		massiveReportDao.remove(massiveReport);
	}
	
	
}
