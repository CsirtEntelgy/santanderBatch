package com.interfactura.firmalocal.persistence;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.CfdIssuedDao;
import com.interfactura.firmalocal.dao.FiscalEntityDao;
import com.interfactura.firmalocal.dao.FolioRangeDao;
import com.interfactura.firmalocal.domain.entities.CFDIssued;
import com.interfactura.firmalocal.domain.entities.FiscalEntity;
import com.interfactura.firmalocal.domain.entities.FolioRange;
import com.interfactura.firmalocal.xml.util.Util;

@Component
public class CFDIssuedManager 
{

	@Autowired(required = true)
	private CfdIssuedDao cfdiDao;
	@Autowired(required = true)
	private FiscalEntityDao feDao;
	@Autowired(required = true)
	private FolioRangeDao frDao;
	@Autowired(required=true)
	private PlatformTransactionManager transactionManager;

	public List<CFDIssued> listar(int inicio, int cantidad, Filters<Filter> filters) 
	{	return cfdiDao.list(inicio, cantidad, filters);		}

	public List<CFDIssued> listar(int inicio, int cantidad, Filters<Filter> filters, String ids) 
	{	return cfdiDao.list(inicio, cantidad, filters, ids);	}
	
	/**
	 * Busca los CFD por el nombre del archivo masivo que lo
	 * contenia
	 * @param nameFile
	 * @return
	 */
	public List<CFDIssued> listar(String nameFile) 
	{	return cfdiDao.list(nameFile);	}

	/**
	 * Genera el Reporte por MES y A�O
	 * 
	 * @param year
	 * @param month
	 * @param rfc
	 * @return
	 */
	public ByteArrayOutputStream monthReport(int year, int month, String rfc) 
	{

		List<CFDIssued> list = cfdiDao.list(year, month, rfc);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		StringBuffer stringB = new StringBuffer();
		for (CFDIssued objCFD : list) {
			stringB.append("|" + objCFD.getTaxIdReceiver());
			if ((objCFD.getFolioRange() != null)
					&& (objCFD.getFolioRange().getSeries() != null)) 
			{
				stringB.append("|"
						+ objCFD.getFolioRange().getSeries().getName());
			} 
			else 
			{	stringB.append("|" + "");	}
			stringB.append("|" + objCFD.getFolio());
			if (objCFD.getFormatType() == 0) 
			{
				Calendar cal = Calendar.getInstance();
				cal.setTime(objCFD.getDateOfIssuance());
				stringB.append("|" + cal.get(Calendar.YEAR));
			} 
			else 
			{
				stringB.append("|"
						+ objCFD.getFolioRange().getYearOfAuthorization()+""
						+ objCFD.getFolioRange().getAuthorizationNumber());
			}
			stringB.append("|"
					+ Util.convertirFecha(objCFD.getDateOfIssuance(), null));
			stringB.append("|" + Util.formatNumber(objCFD.getTotal()));
			stringB.append("|" + Util.formatNumber(objCFD.getIva()));
			stringB.append("|1" );
			stringB.append("|"+objCFD.getCfdType()+"||||\r\n");
		}

		// Busca los CFDs cancelados en el periodo
		List<CFDIssued> listC = cfdiDao.listCancel(year, month, rfc);
		for (CFDIssued objCFD : listC) {
			stringB.append("|" + objCFD.getTaxIdReceiver());
			if ((objCFD.getFolioRange() != null)
					&& (objCFD.getFolioRange().getSeries() != null)) 
			{
				stringB.append("|"
						+ objCFD.getFolioRange().getSeries().getName());
			} 
			else 
			{	stringB.append("|" + "");	}
			stringB.append("|" + objCFD.getFolio());
			if (objCFD.getFormatType() == 0) 
			{
				Calendar cal = Calendar.getInstance();
				cal.setTime(objCFD.getDateOfIssuance());
				stringB.append("|" + cal.get(Calendar.YEAR));
			} 
			else 
			{
				stringB.append("|"
						+ objCFD.getFolioRange().getYearOfAuthorization()+""
						+ objCFD.getFolioRange().getAuthorizationNumber());
			}
			stringB.append("|"
					+ Util.convertirFecha(objCFD.getDateOfIssuance(), null));
			stringB.append("|" + Util.formatNumber(objCFD.getTotal()));
			stringB.append("|" + Util.formatNumber(objCFD.getIva()));
			stringB.append("|" + "0");
			stringB.append("|"+objCFD.getCfdType()+"||||\r\n");
		}

		try 
		{	out.write(stringB.toString().getBytes());	} 
		catch (IOException e) 
		{	e.printStackTrace();	}
		return out;
	}

	public CFDIssued get(long id) 
	{	return cfdiDao.findById(id);	}

	public FiscalEntity getFiscalEntity(long id) 
	{	return feDao.findById(id);	}

	public FolioRange getFolioRange(long id) 
	{	return frDao.findById(id);	}

	@Transactional(propagation = Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
	public CFDIssued update(CFDIssued cFDIssued) 
	{	return cfdiDao.update(cFDIssued);	}
	
	public void update(List<CFDIssued> lstCFD) 
	{
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("txManagerCFD");
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		//def.setIsolationLevel(DefaultTransactionDefinition.ISOLATION_READ_COMMITTED);
		TransactionStatus status = transactionManager.getTransaction(def);
		for(CFDIssued obj:lstCFD)
		{	cfdiDao.update(obj);	}
		transactionManager.commit(status);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
	public void delete(long id) {
		CFDIssued cfdi = cfdiDao.findById(id);
		cfdiDao.remove(cfdi);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW,rollbackFor=Exception.class)
	public void create(CFDIssued cFDIssued) 
	{	cfdiDao.persist(cFDIssued);		}
	
	public CFDIssued getByFolioSat(String folioSat){
        return cfdiDao.getByFolioSat(folioSat);
    }
}
