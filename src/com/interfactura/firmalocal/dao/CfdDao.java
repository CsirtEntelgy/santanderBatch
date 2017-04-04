package com.interfactura.firmalocal.dao;

import java.util.List;

import com.interfactura.firmalocal.domain.entities.CFD;

public interface CfdDao extends Dao<Long, CFD>{
	
	List<CFD> findCFDbyRFC( String rfc );
	
	List<CFD> findCFDToValidate( String rfc, String serie, int year );
	
	List<CFD> findFolioRange( String rfc, String serie, Long approbationNumber, Integer approbationYear );

}
