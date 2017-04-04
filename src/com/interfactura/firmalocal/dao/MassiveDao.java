package com.interfactura.firmalocal.dao;

import java.util.List;

import com.interfactura.firmalocal.domain.entities.Massive;

public interface MassiveDao extends Dao<Long, Massive> {

	List<Massive> listByStatus(int status, int cfdType) throws Exception;
	
	List<Massive> listDepuraStatusNoProcesado(int cfdType) throws Exception;
	
    //MassiveReport list();
	
	boolean updateMassive( Massive massive) throws Exception;
	
	Massive getById( long id ) throws Exception;
}
