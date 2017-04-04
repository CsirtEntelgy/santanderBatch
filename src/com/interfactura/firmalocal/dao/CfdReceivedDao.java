package com.interfactura.firmalocal.dao;

import java.util.List;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.domain.entities.CFDReceived;

public interface CfdReceivedDao extends Dao<Long, CFDReceived>{
	
	CFDReceived findCFD(Long folio, String rfcEmisor, String serie);
	
	List<CFDReceived> list(int begin, int quantity, Filters<Filter> filters);

}
