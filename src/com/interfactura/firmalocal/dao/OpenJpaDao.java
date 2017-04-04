package com.interfactura.firmalocal.dao;

import com.interfactura.firmalocal.domain.entities.OpenJpa;

public interface OpenJpaDao extends Dao<Long, OpenJpa> {
	
	 public long getFolioByFiscalEntity(long fiscalEntityId);
}
