package com.interfactura.firmalocal.dao;

import javax.persistence.LockModeType;

import com.interfactura.firmalocal.domain.entities.CFDIssuedIn;

public interface CfdIssuedIncidenceDao extends Dao<Long, CFDIssuedIn>{
	void lock(CFDIssuedIn obj, LockModeType mode);
}
