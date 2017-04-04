package com.interfactura.firmalocal.dao.impl;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.ssl.PKCS8Key;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.interfactura.firmalocal.dao.SealCertificateDao;
import com.interfactura.firmalocal.domain.entities.SealCertificate;

@Component
public class JpaSealCertificateDao extends JpaDao<Long, SealCertificate> implements SealCertificateDao{

	private static final Logger logger = Logger.getLogger(JpaSealCertificateDao.class);
	
	@Override
	public List<SealCertificate> findCertificates(Date date, long entidadFiscalId){
		return this.findByDateAndFiscalEntity(date,entidadFiscalId );
	}
	
	
	@SuppressWarnings("unchecked")
	public List<SealCertificate> findByDateAndFiscalEntity(Date date, long entidadFiscalId) {
		String jsql = "SELECT x FROM SealCertificate x WHERE x.fiscalEntity.id=:entidadFiscalId AND x.startOfValidity <= :date AND x.endOfValidity >= :date";
		Query query = entityManager.createQuery(jsql);
		query.setParameter("entidadFiscalId", entidadFiscalId).setParameter("date", date);
		try {                
			return  (List<SealCertificate>) query.getResultList();           
		}
		catch (NoResultException e) 
		{ return null;  } 
	}
	
	@Override
	public HashMap<byte[], byte[]> findCertificatesActives(Date date, long entidadFiscalId){
		HashMap<byte[], byte[]> crAndPk = new HashMap<byte[], byte[]>();
		List<SealCertificate> sealCertificateList = this.findByDateAndFiscalEntity(date,entidadFiscalId );
		for(SealCertificate sC: sealCertificateList)
		{
			crAndPk.put(sC.getCertificate(), sC.getPrivateKey());
		}
		return crAndPk;
	}
	
	@SuppressWarnings("unused")
	@Override
	public Certificate getValuesCertificate(SealCertificate sealCertificate){
		RSAPrivateKey pk = null;
		X509Certificate c509 = null;
		ByteArrayInputStream baiPk = new ByteArrayInputStream(sealCertificate.getPrivateKey());
		PKCS8Key pk8;
		try {

			pk8 = new PKCS8Key(baiPk, sealCertificate.getPrivateKeyPassword().toCharArray());
			int keySize = 1;
			if (pk8.isRSA()){
				pk = (RSAPrivateKey) pk8.getPrivateKey();
				keySize = pk.getPrivateExponent().toByteArray().length;
			}	

			ByteArrayInputStream baiCr = new ByteArrayInputStream(sealCertificate.getCertificate());
			BufferedInputStream bis = new BufferedInputStream(baiCr);
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			try {
				while (bis.available() > 0) {
				    Certificate cert = cf.generateCertificate(bis);
				    System.out.println("*****Cert Type: " + cert.getType());
				    if (cert instanceof X509Certificate)
				    {
				    	c509 = (X509Certificate) cert;
				    }
				}
			} catch (IOException e) {
				e.printStackTrace();
			}		
		} catch (GeneralSecurityException e1) {
			e1.printStackTrace();
		}
		return c509;
	}
	
	@SuppressWarnings("unchecked")
	public List<SealCertificate> listar() {
		String jsql = "SELECT x FROM SealCertificate x ";
		Query query = entityManager.createQuery(jsql);
		logger.debug("Obteniendo certificados en general");
		try {                
			return  (List<SealCertificate>) query.getResultList();           
		}
		catch (NoResultException e) 
		{ return null;  } 
	}
}
