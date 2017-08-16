package com.interfactura.firmalocal.dao.impl;

import com.interfactura.firmalocal.dao.RouteDao;
import com.interfactura.firmalocal.domain.entities.Route;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class JpaRouteDao extends JpaDao<Long, Route>
  implements RouteDao
{
  private static final Logger logger = Logger.getLogger(JpaRouteDao.class);
}