package com.interfactura.firmalocal.dao;

import java.util.List;

import com.interfactura.firmalocal.domain.entities.CSD;

public interface CsdDao extends Dao<Long, CSD>{
	
	List<CSD> findCSDbyRFC( String rfc );
	
	List<CSD> findCSDToValidate( String serie, String rfc );

}
