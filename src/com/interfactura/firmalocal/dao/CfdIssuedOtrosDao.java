package com.interfactura.firmalocal.dao;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.domain.entities.CFDIssuedOtros;
import java.util.List;

public abstract interface CfdIssuedOtrosDao extends Dao<Long, CFDIssuedOtros>
{
  public abstract List<CFDIssuedOtros> list(int paramInt1, int paramInt2, Filters<Filter> paramFilters);

  public abstract List<CFDIssuedOtros> list(int paramInt1, int paramInt2, Filters<Filter> paramFilters, String paramString);

  public abstract List<CFDIssuedOtros> list(int paramInt1, int paramInt2, String paramString);

  public abstract List<CFDIssuedOtros> listCancel(int paramInt1, int paramInt2, String paramString);

  public abstract List<CFDIssuedOtros> list(String paramString);
}