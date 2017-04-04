package com.interfactura.firmalocal.persistence;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.interfactura.firmalocal.dao.FacturasDao;
import com.interfactura.firmalocal.domain.entities.Facturas;

@Component
public class FacturasManager {

	//@Autowired
		@Autowired(required=true)
		FacturasDao facturasDao;
				
		public Facturas get(long id){
			Facturas factura = facturasDao.findById(id);
			return factura;
		}
		@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
		public void update(Facturas factura){
			facturasDao.update(factura);
		}
		@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
		public void delete(long id){
			Facturas factura = facturasDao.findById(id);
			facturasDao.remove(factura);
		}
		@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
		public void create(Facturas factura){
			facturasDao.persist(factura);
		}
		
		public Facturas findByFolioSat(String folioSat) throws Exception{
			return facturasDao.findByFolioSat(folioSat);
		}
		
		public List<Facturas> findByNombreDeArchivo(String nombreDeArchivo) throws Exception{
			return facturasDao.findByNombreDeArchivo(nombreDeArchivo);
		}
}
