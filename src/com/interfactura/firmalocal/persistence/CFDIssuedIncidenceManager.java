package com.interfactura.firmalocal.persistence;

import java.util.List;

import javax.persistence.LockModeType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.interfactura.firmalocal.dao.CfdIssuedIncidenceDao;
import com.interfactura.firmalocal.domain.entities.CFDIssuedIn;

@Component
public class CFDIssuedIncidenceManager {
	@Autowired(required = true)
	private CfdIssuedIncidenceDao cfdiIncidenceDao;
	@Autowired(required=true)
	private PlatformTransactionManager transactionManager;
	
	@Transactional(propagation = Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
	public CFDIssuedIn update(CFDIssuedIn cFDIssued) {
		//this.lock(cFDIssued, LockModeType.WRITE);
		return cfdiIncidenceDao.update(cFDIssued);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
	public void delete(long id) {
		CFDIssuedIn cfdi = cfdiIncidenceDao.findById(id);
		cfdiIncidenceDao.remove(cfdi);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
	public void create(CFDIssuedIn cFDIssued) {
		cfdiIncidenceDao.persist(cFDIssued);
	}
	
	public void lock(CFDIssuedIn cFDIssued, LockModeType mode) {
		cfdiIncidenceDao.lock(cFDIssued, mode);
	}	
	
	/**
	 * Inserta una lista de Incidencias de CFD
	 * @param lstCFDIn
	 */
	public void update(List<CFDIssuedIn> lstCFDIn) {
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("txManagerCFDIn");
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		TransactionStatus status = transactionManager.getTransaction(def);
		for(CFDIssuedIn obj:lstCFDIn){
			cfdiIncidenceDao.update(obj);
		}
		transactionManager.commit(status);
	}
}
