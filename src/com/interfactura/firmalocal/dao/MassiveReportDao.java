package com.interfactura.firmalocal.dao;

import java.util.List;
import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.domain.entities.MassiveReport;

public interface MassiveReportDao extends Dao<Long, MassiveReport> {
	
	List<MassiveReport> listByStatus(int status, int cfdType) throws Exception;
	
    //MassiveReport list();
	
	boolean updateMassive( MassiveReport massiveReport) throws Exception;
	
	MassiveReport getById( long id ) throws Exception;


}
