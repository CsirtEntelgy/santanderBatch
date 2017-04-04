package com.interfactura.firmalocal.persistence;

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
import com.interfactura.firmalocal.dao.FiscalEntityDao;
import com.interfactura.firmalocal.dao.FolioRangeDao;
import com.interfactura.firmalocal.dao.SeriesDao;
import com.interfactura.firmalocal.domain.entities.FiscalEntity;
import com.interfactura.firmalocal.domain.entities.FolioRange;
import com.interfactura.firmalocal.domain.entities.Series;

@Component
public class FolioRangeManager {
	
	public Series getSeries(long id) {
		return sDao.findById(id);
	}
	
	public FiscalEntity getFiscalEntity(long id) {
		return feDao.findById(id);
	}

	public List<Series> listAllSeries(){
		return sDao.listAllSeries();
	}
	
	public List<Series> listOfSeries(long fiscalEntityId){
		return sDao.listOfSeries(fiscalEntityId);
	}
	
	public List<FiscalEntity> listAllFiscalEntity(String ids) 
	{
		return feDao.listAllFiscalEntity(ids);
	}
	
	public List<FolioRange> listar(int inicio, int cantidad, Filters<Filter> filters, String feids) {
		return frDao.list(inicio, cantidad, filters, feids);
	}
	
	/**
	 * Busca el Rango de FOLIOS activos
	 * @param nameSerie
	 * @return
	 */
	public List<FolioRange> listarActivos(String nameSerie,long idEntityFiscal){
		return frDao.listActive(nameSerie, idEntityFiscal);
	}

	/**
	 * Busca el Rango de FOLIOS activos
	 * @param nameSerie
	 * @return
	 */
	public List<FolioRange> listar(String nameSerie,long idEntityFiscal) {
		return frDao.list(nameSerie, idEntityFiscal);
	}
	
	public FolioRange get(long id) {
		return frDao.findById(id);
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public synchronized void update(FolioRange folioRange ) {
		frDao.update(folioRange);
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void delete(long id) {
		FolioRange folioRange = frDao.findById(id);
		frDao.remove(folioRange);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void create(FolioRange folioRange) {
		frDao.persist(folioRange);
	}

	public List<FolioRange> list(FiscalEntity fiscalEntity) {
		return frDao.list(fiscalEntity);
	}

	public FolioRange findByFiscalEntityAndSeries(long fiscalEntityId, long seriesId) {
		return frDao.findByFiscalEntityAndSeries(fiscalEntityId, seriesId);
	}
	
	public void updateFolios(List<FolioRange> foliosToUpdate) {
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("txManagerUpdateFolios");
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		TransactionStatus status = transactionManager.getTransaction(def);
		frDao.updateFolios(foliosToUpdate);
		transactionManager.commit(status);
	}	
	
	@Autowired(required=true)
	private FolioRangeDao frDao;
	@Autowired(required=true)
	private SeriesDao sDao;
	@Autowired(required=true)
	private FiscalEntityDao feDao;
	@Autowired(required=true)
	private PlatformTransactionManager transactionManager;
	
}