package com.interfactura.firmalocal.persistence;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.interfactura.firmalocal.dao.MassiveDao;
import com.interfactura.firmalocal.domain.entities.Massive;

@Component
public class MassiveManager {
	@Autowired(required=true)
	MassiveDao massiveDao;
	
	public List<Massive> findByStatus(int status, int cfdType) throws Exception
	{
		return massiveDao.listByStatus(status, cfdType);
	}
	
	public List<Massive> findByDepuraStatusNoProcesado(int cfdType) throws Exception {
		return massiveDao.listDepuraStatusNoProcesado(cfdType);
	}
  //  public List<MassiveReport> list(){
	//	
		//return massiveReportDao.list();
		
//	}
	
	public Massive getById( long id ) throws Exception{
		
		return massiveDao.getById(id);
	}
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public boolean update( Massive massive ) throws Exception{
		
		return massiveDao.updateMassive(massive);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void delete( long id ) throws Exception{
		Massive massive = massiveDao.getById(id);
		massiveDao.remove(massive);
	}
}
