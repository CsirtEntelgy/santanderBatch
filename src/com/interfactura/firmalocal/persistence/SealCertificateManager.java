package com.interfactura.firmalocal.persistence;

import java.security.cert.Certificate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.interfactura.firmalocal.dao.FiscalEntityDao;
import com.interfactura.firmalocal.dao.SealCertificateDao;
import com.interfactura.firmalocal.domain.entities.FiscalEntity;
import com.interfactura.firmalocal.domain.entities.SealCertificate;

@Component
public class SealCertificateManager {
	
	public SealCertificateManager() {
	}
	
	@Autowired(required=true)
	SealCertificateDao scDao;
	
	@Autowired(required=true)
	FiscalEntityDao fEDao;
	
	public FiscalEntity getFiscalEntity(long id) {
		FiscalEntity fiscalEntity = fEDao.findById(id);
		return fiscalEntity;
	}
	
	public SealCertificate get(long id) {
		SealCertificate sc = scDao.findById(id);
		return sc;
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void update(SealCertificate sc) {
		scDao.update(sc);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void delete(long id) {
		SealCertificate sc = scDao.findById(id);
		scDao.remove(sc);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void create(SealCertificate sc){
		scDao.persist(sc);
	}

	public List<SealCertificate> findCertificates(Date date, long entidadFiscalId){
		return scDao.findCertificates(date,entidadFiscalId );
	}
	
	public HashMap<byte[], byte[]> findCertificatesActives(Date date, long entidadFiscalId){
		return scDao.findCertificatesActives(date, entidadFiscalId);
	}
	
	public Certificate getValuesCertificate(SealCertificate sealCertificate){
		return scDao.getValuesCertificate(sealCertificate);
	}

	public List<SealCertificate> listar(){
		return scDao.listar();
	}	
}
