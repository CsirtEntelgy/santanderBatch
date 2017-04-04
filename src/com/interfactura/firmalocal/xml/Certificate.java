package com.interfactura.firmalocal.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.ssl.PKCS8Key;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import com.interfactura.firmalocal.domain.entities.SealCertificate;
import com.interfactura.firmalocal.persistence.SealCertificateManager;
import com.interfactura.firmalocal.utils.Base64;
import com.interfactura.firmalocal.xml.util.Util;

import altec.infra.StringEncrypter;

@Component
public class Certificate {
	@Autowired
	private SealCertificateManager sealCertificateManager;
	private String pwd;
	private InputStream in;
	private SealCertificate certificado;
	private List<SealCertificate> lstCertificados;
	@Autowired
	private Properties properties;
	private static Logger logger = Logger.getLogger(Certificate.class);

	public void getCertificadoContigencia()
			throws FileNotFoundException, IOException, GeneralSecurityException {
		logger.info("Obtencion del certificado de Contigencia");
		/**** De test ****/
		certificado = new SealCertificate();
		ByteArrayOutputStream outC = new ByteArrayOutputStream();
		ByteArrayOutputStream outK = new ByteArrayOutputStream();
		FileCopyUtils.copy(
				new FileInputStream(properties.getCertificado()), outC);
		FileCopyUtils.copy(
				new FileInputStream(properties.getKeyCertificado()), outK);
		certificado.setCertificate(outC.toByteArray());
		certificado.setPrivateKey(outK.toByteArray());
		certificado.setPrivateKeyPassword(properties.getPwd());
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		ByteArrayInputStream is = new ByteArrayInputStream(certificado.getCertificate());
		java.security.cert.Certificate c509 = cf.generateCertificate(is);
		RSAPrivateKey pk;
		RSAPublicKey puk;
		PKCS8Key pk8 = new PKCS8Key(certificado.getPrivateKey(), certificado.getPrivateKeyPassword().toCharArray());
		if (pk8.isRSA()) {
			pk = (RSAPrivateKey) pk8.getPrivateKey();
		}

		if (c509 instanceof X509Certificate) {
			X509Certificate cert2 = (X509Certificate) c509;
			if (c509.getPublicKey() instanceof RSAPublicKey) {
				puk = (RSAPublicKey) cert2.getPublicKey();
			}

			/*
			Map<String, String> oidMap = new HashMap<String, String>();
			oidMap.put("2.5.4.5", "serialNumber");
			oidMap.put("2.5.4.41", "name");
			oidMap.put("2.5.4.17", "postalCode");
			oidMap.put("2.5.4.45", "uniqueIdentifier");
			oidMap.put("1.2.840.113549.1.9.2", "unstructuredName");
			oidMap.put("1.2.840.113549.1.9.1", "emailAddress");
			
			X500Principal subject = cert2.getSubjectX500Principal();

			String dn = subject.getName(X500Principal.RFC1779, oidMap);

			Map<String, String> dnames = Util.getDNMembers(dn);

			
			for (Map.Entry<String, String> e : dnames.entrySet()) {
				if (e.getKey().equals("serialNumber")) {
					certificado.setSerialNumber(e.getValue());
				}
			}*/
			certificado.setSerialNumber(Util.serialNumberIES(cert2.getSerialNumber()));
		}
	}

	public void find(long idEntityFiscal) {
		this.certificado=null;
		for (SealCertificate objC : sealCertificateManager.findCertificates(Calendar.getInstance().getTime(), idEntityFiscal)) {
			try {
	            StringEncrypter encrypter = new StringEncrypter(StringEncrypter.DES_ENCRYPTION_SCHEME);
	            String plainPassword = encrypter.decrypt(objC.getPrivateKeyPassword());
	            objC.setPrivateKeyPassword(plainPassword);
			} catch (Exception e) {
	           logger.error(e.getLocalizedMessage(), e);
	        }
			this.certificado = objC;
			break;
		}
	}

	public String encripta() {
		return Util.replaceSR(Base64.encodeToString(certificado.getCertificate(),false));
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public InputStream getIn() {
		return in;
	}

	public void setIn(InputStream in) {
		this.in = in;
	}

	public SealCertificateManager getSealCertificateManager() {
		return sealCertificateManager;
	}

	public void setSealCertificateManager(
			SealCertificateManager sealCertificateManager) {
		this.sealCertificateManager = sealCertificateManager;
	}

	public SealCertificate getCertificado() {
		return certificado;
	}

	public void setCertificado(SealCertificate certificado) {
		this.certificado = certificado;
	}

	public List<SealCertificate> getLstCertificados() {
		return lstCertificados;
	}

	public void setLstCertificados(List<SealCertificate> lstCertificados) {
		this.lstCertificados = lstCertificados;
	}

}
