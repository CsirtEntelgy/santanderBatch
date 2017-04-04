package com.interfactura.firmalocal.dao;


import java.util.List;

import com.interfactura.firmalocal.domain.entities.Facturas;

public interface FacturasDao extends Dao<Long, Facturas> {
	
	Facturas findByFolioSat(String folioSat) throws Exception;
	
	List<Facturas> findByNombreDeArchivo(String nombreDeArchivo) throws Exception;
	
}
