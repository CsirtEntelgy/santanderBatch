package com.interfactura.firmalocal.persistence;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.FiscalEntityDao;
import com.interfactura.firmalocal.dao.StateDao;
import com.interfactura.firmalocal.domain.entities.FiscalEntity;
import com.interfactura.firmalocal.domain.entities.State;

@Component
public class FiscalEntityManager {

	@Autowired(required=true)
	FiscalEntityDao fEDao;
	@Autowired(required=true)
	StateDao stateDao;
	
	public State getState(long id) {
		State state = stateDao.findById(id);
		return state;
	}	
	
	public FiscalEntity get(String rfc) {
		return fEDao.get(rfc);
	}
	
	public FiscalEntity findByRFCA(FiscalEntity fiscalEntity) {
		return fEDao.findByRFCA(fiscalEntity);
	}

	public List<FiscalEntity> listar() {
		return fEDao.listar();
	}
	
	public List<FiscalEntity> listar(String ids) {
		return fEDao.listAllFiscalEntity(ids);
	}
	
	public List<FiscalEntity> listar(int inicio, int cantidad, Filters<Filter> filters) {
		return fEDao.listar(inicio, cantidad, filters);
	}

	public List<State> listAllStates() {
		return fEDao.listAllStates();
	}
	
	public FiscalEntity get(long id) {
		FiscalEntity fiscalEntity = fEDao.findById(id);
		return fiscalEntity;
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void update(FiscalEntity fiscalEntity) {
		fEDao.update(fiscalEntity);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void delete(long id) {
		FiscalEntity fiscalEntity = fEDao.findById(id);
		fEDao.remove(fiscalEntity);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void create(FiscalEntity fiscalEntity) 
	{
		State state = stateDao.findById(fiscalEntity.getAddress().getState().getId());
		fiscalEntity.getAddress().setState(state);
		fEDao.persist(fiscalEntity);
	}	
	
}
