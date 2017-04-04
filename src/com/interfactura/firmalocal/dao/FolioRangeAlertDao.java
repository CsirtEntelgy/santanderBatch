package com.interfactura.firmalocal.dao;

import java.util.List;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.domain.entities.FolioRangeAlerts;

public interface FolioRangeAlertDao extends Dao<Long, FolioRangeAlerts> {
	
	List<FolioRangeAlerts> list(int begin, int quantity, Filters<Filter> filters, String ids);

}
