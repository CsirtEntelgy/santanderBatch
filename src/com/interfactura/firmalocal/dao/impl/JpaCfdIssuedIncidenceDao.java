package com.interfactura.firmalocal.dao.impl;

import javax.persistence.LockModeType;

import org.springframework.stereotype.Component;

import com.interfactura.firmalocal.dao.CfdIssuedIncidenceDao;
import com.interfactura.firmalocal.domain.entities.CFDIssuedIn;

@Component
public class JpaCfdIssuedIncidenceDao extends JpaDao<Long, CFDIssuedIn> 
	implements CfdIssuedIncidenceDao{

	@Override
	public void lock(CFDIssuedIn obj, LockModeType mode) {
		entityManager.lock(obj, mode);
	}

	
}
