package com.interfactura.firmalocal.dao.impl;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.CfdIssuedDao;
import com.interfactura.firmalocal.dao.CfdIssuedOtrosDao;
import com.interfactura.firmalocal.domain.entities.CFDIssuedOtros;
import com.interfactura.firmalocal.persistence.UtilManager;
import com.interfactura.firmalocal.xml.util.Util;
import java.io.PrintStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class JpaCfdIssuedOtrosDao extends JpaDao<Long, CFDIssuedOtros>
  implements CfdIssuedOtrosDao
{
  private static final Logger logger = Logger.getLogger(CfdIssuedDao.class);

  public List<CFDIssuedOtros> list(int begin, int quantity, Filters<Filter> filters)
  {
    logger.info("listado State");
    List recordList = null;
    Long folio = null;
    String rfcReceptor = "";
    String rfcEmisor = "";
    Date dateBegin = null;
    Date dateEnd = null;
    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
    String where = "";
    String stringQuery = "SELECT x FROM CFDIssuedOtros x";
    if ((filters != null) && (filters.size() > 0)) {
      for (Filter filter : filters) {
        if (filter.getColumn().equals("folio")) {
          try {
            folio = Long.valueOf(Long.parseLong(filter.getPattern()));
            logger.debug(folio);
          } catch (NumberFormatException e) {
            folio = null;
            e.printStackTrace();
          }
        }

        if (filter.getColumn().equals("rfcReceptor")) {
          rfcReceptor = filter.getPattern();
          logger.debug(rfcReceptor);
        }
        if (filter.getColumn().equals("rfcEmisor")) {
          rfcEmisor = filter.getPattern();
          logger.debug(rfcEmisor);
        }
        if (filter.getColumn().equals("rbegin")) {
          try {
            logger.debug(filter.getPattern());
            dateBegin = format.parse(filter.getPattern());
            logger.debug(dateBegin);
          } catch (ParseException ex) {
            ex.printStackTrace();
          }
        }
        if (!filter.getColumn().equals("rend")) continue;
        try {
          logger.debug(filter.getPattern());
          dateEnd = format.parse(filter.getPattern());
          logger.debug(dateEnd);
        } catch (ParseException ex) {
          ex.printStackTrace();
        }
      }
      if ((folio != null) && (folio.longValue() != 0L))
      {
        where = " WHERE x.folio = :folioParam";
        if ((rfcReceptor != null) && (rfcReceptor.length() > 0))
          where = where + " AND x.taxIdReceiver like :rfcReceptor";
        if ((rfcEmisor != null) && (rfcEmisor.length() > 0))
          where = where + " AND x.fiscalEntity.taxID like :rfcEmisor";
        if (dateBegin != null)
          where = where + " AND x.dateOfIssuance >= :dateBegin";
        if (dateEnd != null)
          where = where + " AND x.dateOfIssuance <= :dateEnd";
      }
      else if (!"".equals(rfcReceptor))
      {
        where = " WHERE x.taxIdReceiver like :rfcReceptor";
        if ((rfcEmisor != null) && (rfcEmisor.length() > 0))
          where = where + " AND x.fiscalEntity.taxID like :rfcEmisor";
        if (dateBegin != null)
          where = where + " AND x.dateOfIssuance >= :dateBegin";
        if (dateEnd != null)
          where = where + " AND x.dateOfIssuance <= :dateEnd";
      }
      else if (!"".equals(rfcEmisor))
      {
        where = " WHERE x.fiscalEntity.taxID like :rfcEmisor";
        if (dateBegin != null)
          where = where + " AND x.dateOfIssuance >= :dateBegin";
        if (dateEnd != null)
          where = where + " AND x.dateOfIssuance <= :dateEnd";
      }
      else if (dateBegin != null)
      {
        where = " WHERE x.dateOfIssuance >= :dateBegin";
        if (dateEnd != null)
          where = where + " AND x.dateOfIssuance <= :dateEnd";
      }
      else if (dateEnd != null) {
        where = " WHERE x.dateOfIssuance <= :dateEnd";
      }
    }
    if (!"".equals(where))
      stringQuery = stringQuery + where;
    stringQuery = stringQuery + " ORDER BY x.creationDate desc";
    Query query = this.entityManager.createQuery(stringQuery);
    if (!"".equals(where))
    {
      if (folio != null)
        query.setParameter("folioParam", String.valueOf(folio));
      if (!"".equals(rfcReceptor))
        query.setParameter("rfcReceptor", "%" + rfcReceptor + "%");
      if (!"".equals(rfcEmisor))
        query.setParameter("rfcEmisor", "%" + rfcEmisor + "%");
      if (dateBegin != null)
        query.setParameter("dateBegin", dateBegin);
      if (dateEnd != null)
        query.setParameter("dateEnd", dateEnd);
    }
    if (quantity != 0)
    {
      query.setFirstResult(begin);
      query.setMaxResults(quantity);
    }
    try {
      recordList = query.getResultList();
    } catch (NoResultException e) {
      logger.error(e.getLocalizedMessage(), e);
    }
    return recordList;
  }

  public List<CFDIssuedOtros> list(int begin, int quantity, Filters<Filter> filters, String ids)
  {
    logger.info("listado State");
    List recordList = null;
    Long folio = null;
    String rfcReceptor = "";
    String rfcEmisor = "";
    Date dateBegin = null;
    Date dateEnd = null;
    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
    String where = "";
    String stringQuery = "SELECT x FROM CFDIssuedOtros x";
    if (ids.length() == 0) {
      ids = "-1";
    }

    if ((filters != null) && (filters.size() > 0)) {
      for (Filter filter : filters) {
        if (filter.getColumn().equals("folio")) {
          try {
            folio = Long.valueOf(Long.parseLong(filter.getPattern()));
            logger.debug(folio);
          } catch (NumberFormatException e) {
            folio = null;
            e.printStackTrace();
          }
        }

        if (filter.getColumn().equals("rfcReceptor")) {
          rfcReceptor = filter.getPattern();
          logger.debug(rfcReceptor);
        }
        if (filter.getColumn().equals("rfcEmisor")) {
          rfcEmisor = filter.getPattern();
          logger.debug(rfcEmisor);
        }
        if (filter.getColumn().equals("rbegin")) {
          try {
            logger.debug(filter.getPattern());
            dateBegin = format.parse(filter.getPattern());
            logger.debug(dateBegin);
          } catch (ParseException ex) {
            ex.printStackTrace();
          }
        }
        if (!filter.getColumn().equals("rend")) continue;
        try {
          logger.debug(filter.getPattern());
          dateEnd = format.parse(filter.getPattern());
          logger.debug(dateEnd);
        } catch (ParseException ex) {
          ex.printStackTrace();
        }
      }
      if ((folio != null) && (folio.longValue() != 0L))
      {
        where = " WHERE x.folio = :folioParam";
        if ((rfcReceptor != null) && (rfcReceptor.length() > 0))
          where = where + " AND x.taxIdReceiver like :rfcReceptor";
        if ((rfcEmisor != null) && (rfcEmisor.length() > 0))
          where = where + " AND x.fiscalEntity.taxID like :rfcEmisor";
        if (dateBegin != null)
          where = where + " AND x.dateOfIssuance >= :dateBegin";
        if (dateEnd != null)
          where = where + " AND x.dateOfIssuance <= :dateEnd";
      }
      else if (!"".equals(rfcReceptor))
      {
        where = " WHERE x.taxIdReceiver like :rfcReceptor";
        if ((rfcEmisor != null) && (rfcEmisor.length() > 0))
          where = where + " AND x.fiscalEntity.taxID like :rfcEmisor";
        if (dateBegin != null)
          where = where + " AND x.dateOfIssuance >= :dateBegin";
        if (dateEnd != null)
          where = where + " AND x.dateOfIssuance <= :dateEnd";
      }
      else if (!"".equals(rfcEmisor))
      {
        where = " WHERE x.fiscalEntity.taxID like :rfcEmisor";
        if (dateBegin != null)
          where = where + " AND x.dateOfIssuance >= :dateBegin";
        if (dateEnd != null)
          where = where + " AND x.dateOfIssuance <= :dateEnd";
      }
      else if (dateBegin != null)
      {
        where = " WHERE x.dateOfIssuance >= :dateBegin";
        if (dateEnd != null)
          where = where + " AND x.dateOfIssuance <= :dateEnd";
      }
      else if (dateEnd != null) {
        where = " WHERE x.dateOfIssuance <= :dateEnd";
      }
    }
    if (!"".equals(where))
      stringQuery = stringQuery + where + " AND x.fiscalEntity.id IN (" + ids + ") ";
    else {
      stringQuery = stringQuery + UtilManager.in("x.fiscalEntity.id", ids);
    }
    logger.info("Ids entidades " + ids);
    stringQuery = stringQuery + " ORDER BY x.creationDate desc";
    Query query = this.entityManager.createQuery(stringQuery);
    if (!"".equals(where))
    {
      if (folio != null)
        query.setParameter("folioParam", String.valueOf(folio));
      if (!"".equals(rfcReceptor))
        query.setParameter("rfcReceptor", "%" + rfcReceptor + "%");
      if (!"".equals(rfcEmisor))
        query.setParameter("rfcEmisor", "%" + rfcEmisor + "%");
      if (dateBegin != null)
        query.setParameter("dateBegin", dateBegin);
      if (dateEnd != null)
        query.setParameter("dateEnd", dateEnd);
    }
    if (quantity != 0)
    {
      query.setFirstResult(begin);
      query.setMaxResults(quantity);
    }
    try {
      recordList = query.getResultList();
    } catch (NoResultException e) {
      logger.error(e.getLocalizedMessage(), e);
    }
    return recordList;
  }

  public List<CFDIssuedOtros> list(int year, int month, String rfc)
  {
    String sql = "select X from CFDIssuedOtros X where X.fiscalEntity.taxID = :rfcParam and  X.dateOfIssuance >= :dateBeginParam and X.dateOfIssuance < :dateEndParam";

    List recordList = null;
    Query q = this.entityManager.createQuery(sql);
    q.setParameter("rfcParam", rfc);
    System.out.println("***Params: " + year + "  --- " + month);
    q.setParameter("dateBeginParam", Util.rangoFecha(month, year, false));
    q.setParameter("dateEndParam", Util.rangoFecha(month + 1, year, false));
    System.out.println("***Inicio: " + Util.rangoFecha(month, year, false));
    System.out.println("***Fin: " + Util.rangoFecha(month + 1, year, false));
    try
    {
      recordList = q.getResultList();
    }
    catch (NoResultException e) {
      logger.error(e.getLocalizedMessage(), e);
    }return recordList;
  }

  public List<CFDIssuedOtros> listCancel(int year, int month, String rfc)
  {
    List recordList = null;
    String sqlC = "select X from CFDIssuedOtros X where X.fiscalEntity.taxID = :rfcParam and  X.cancellationDate >= :dateBeginParam and X.cancellationDate < :dateEndParam ";
    Query qC = this.entityManager.createQuery(sqlC);
    qC.setParameter("rfcParam", rfc);
    System.out.println("***Params: " + year + "  --- " + month);
    qC.setParameter("dateBeginParam", Util.rangoFecha(month, year, false));
    qC.setParameter("dateEndParam", Util.rangoFecha(month + 1, year, false));
    System.out.println("***Inicio: " + Util.rangoFecha(month, year, false));
    System.out.println("***Fin: " + Util.rangoFecha(month + 1, year, false));
    try {
      recordList = qC.getResultList();
    } catch (NoResultException e) {
      logger.error(e.getLocalizedMessage(), e);
    }return recordList;
  }

  public List<CFDIssuedOtros> list(String nameFile)
  {
    List recordList = null;
    String sqlC = "select X from CFDIssuedOtros X where X.sourceFileName = :name ORDER BY X.xmlRoute ASC";
    Query qC = this.entityManager.createQuery(sqlC);
    qC.setParameter("name", nameFile);
    try {
      recordList = qC.getResultList();
    } catch (NoResultException e) {
      logger.error(e.getLocalizedMessage(), e);
    }
    return recordList;
  }
}