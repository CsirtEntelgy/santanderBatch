package com.interfactura.firmalocal.persistence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.interfactura.firmalocal.dao.OpenJpaDao;
import com.interfactura.firmalocal.domain.entities.OpenJpa;

@Component
public class OpenJpaManager {


	@Autowired(required=true)
	private OpenJpaDao openDao;
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void update(OpenJpa open){
		openDao.update(open);
	}
	
	public long getFolioByFiscalEntity(long fiscalEntityId){
		return openDao.getFolioByFiscalEntity(fiscalEntityId);
	}
	
	public OpenJpa getFolioById(long id) {
		return openDao.findById(id);
	}
}
