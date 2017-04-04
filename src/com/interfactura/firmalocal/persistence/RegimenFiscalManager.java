package com.interfactura.firmalocal.persistence;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.RegimenFiscalDao;
import com.interfactura.firmalocal.domain.entities.RegimenFiscal;

/**
 * Brinda acceso a las operaciones de la capa de persistencia.
 * 
 * @author hlara
 *
 */
@Component
public class RegimenFiscalManager {
	
	@Autowired(required = true)
	private RegimenFiscalDao regimenFiscalDao;

	public List<RegimenFiscal> listar() {
		return regimenFiscalDao.listar();
	}

	public List<RegimenFiscal> listar(int inicio, int cantidad, Filters<Filter> filters) {
		return regimenFiscalDao.listar(inicio, cantidad, filters);
	}

	public RegimenFiscal get(long id) {
		return regimenFiscalDao.findById(id);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public void update(RegimenFiscal country) {
		regimenFiscalDao.update(country);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public void delete(long id) {
		RegimenFiscal rf = regimenFiscalDao.findById(id);
		regimenFiscalDao.remove(rf);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public void create(RegimenFiscal country) {
		regimenFiscalDao.persist(country);
	}
	
	public RegimenFiscal findByName(String name) {
		return regimenFiscalDao.findByName(name);
	}

	public RegimenFiscal findByCode(String code) {
		return regimenFiscalDao.findByCode(code);
	}

}
