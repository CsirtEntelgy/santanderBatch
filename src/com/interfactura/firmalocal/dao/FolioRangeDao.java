package com.interfactura.firmalocal.dao;

import java.util.List;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.domain.entities.FiscalEntity;
import com.interfactura.firmalocal.domain.entities.FolioRange;

public interface FolioRangeDao extends Dao<Long, FolioRange>{
	
	List<FolioRange> list(int begin, int quantity, Filters<Filter> filters, String ids);
	
	List<FolioRange> listActive(String nameSerie,long idEntityFiscal);
	
	List<FolioRange> list(String nameSerie,long idEntityFiscal);
	
	List<FolioRange> list(FiscalEntity fiscalEntity);
	
	FolioRange findByFiscalEntityAndSeries(long fiscalEntityId, long seriesId);

	void updateFolios(List<FolioRange> foliosToUpdate);

}
