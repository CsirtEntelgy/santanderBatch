package com.interfactura.firmalocal.dao;

import java.security.cert.Certificate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.interfactura.firmalocal.domain.entities.SealCertificate;

public interface SealCertificateDao extends Dao<Long, SealCertificate>{
	
	 List<SealCertificate> findCertificates(Date date, long entidadFiscalId);

	 HashMap<byte[], byte[]> findCertificatesActives(Date date, long entidadFiscalId);

	 Certificate getValuesCertificate(SealCertificate sealCertificate);

	 List<SealCertificate> listar();
}
