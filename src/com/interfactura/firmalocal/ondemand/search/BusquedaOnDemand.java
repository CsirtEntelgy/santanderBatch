package com.interfactura.firmalocal.ondemand.search;

import java.util.List;

import com.ibm.edms.od.ODFolder;


public interface BusquedaOnDemand {
	    
    @SuppressWarnings("unchecked")
    List<String> busquedaInterfacturaEmisionCFDICifras(long maxHits, String strPathFolioSat, ODFolder folderObtenido, String prefixXml) throws Exception;
    
}

