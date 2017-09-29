package com.interfactura.firmalocal.persistence;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.CfdIssuedDao;
import com.interfactura.firmalocal.dao.CfdIssuedOtrosDao;
import com.interfactura.firmalocal.dao.FiscalEntityDao;
import com.interfactura.firmalocal.dao.FolioRangeDao;
import com.interfactura.firmalocal.dao.RouteDao;
import com.interfactura.firmalocal.datamodel.ComplementoPago;
import com.interfactura.firmalocal.domain.entities.CFDIssued;
import com.interfactura.firmalocal.domain.entities.CFDIssuedOtros;
import com.interfactura.firmalocal.domain.entities.FiscalEntity;
import com.interfactura.firmalocal.domain.entities.FolioRange;
import com.interfactura.firmalocal.domain.entities.Route;
import com.interfactura.firmalocal.domain.entities.Series;
import com.interfactura.firmalocal.xml.util.Util;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Component
public class CFDIssuedManager
{

  @Autowired(required=true)
  private CfdIssuedDao cfdiDao;

  @Autowired(required=true)
  private CfdIssuedOtrosDao cfdiOtrosDao;

  @Autowired(required=true)
  private FiscalEntityDao feDao;

  @Autowired(required=true)
  private FolioRangeDao frDao;

  @Autowired(required=true)
  private PlatformTransactionManager transactionManager;

  @Autowired(required=true)
  private RouteDao routeDao;

  public List<CFDIssued> listar(int inicio, int cantidad, Filters<Filter> filters)
  {
    return this.cfdiDao.list(inicio, cantidad, filters);
  }

  public List<CFDIssued> listar(int inicio, int cantidad, Filters<Filter> filters, String ids)
  {
    return this.cfdiDao.list(inicio, cantidad, filters, ids);
  }

  public List<CFDIssued> listar(String nameFile)
  {
    return this.cfdiDao.list(nameFile);
  }

  public ByteArrayOutputStream monthReport(int year, int month, String rfc)
  {
    List<CFDIssued> list = this.cfdiDao.list(year, month, rfc);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    StringBuffer stringB = new StringBuffer();
    Calendar cal;
    for (CFDIssued objCFD : list) {
      stringB.append("|" + objCFD.getTaxIdReceiver());
      if ((objCFD.getFolioRange() != null) && 
        (objCFD.getFolioRange().getSeries() != null))
      {
        stringB.append("|" + 
          objCFD.getFolioRange().getSeries().getName());
      }
      else
        stringB.append("|");
      stringB.append("|" + objCFD.getFolio());
      if (objCFD.getFormatType() == 0)
      {
        cal = Calendar.getInstance();
        cal.setTime(objCFD.getDateOfIssuance());
        stringB.append("|" + cal.get(1));
      }
      else
      {
        stringB.append("|" + 
          objCFD.getFolioRange().getYearOfAuthorization() + 
          objCFD.getFolioRange().getAuthorizationNumber());
      }
      stringB.append("|" + 
        Util.convertirFecha(objCFD.getDateOfIssuance(), null));
      stringB.append("|" + Util.formatNumber(objCFD.getTotal()));
      stringB.append("|" + Util.formatNumber(objCFD.getIva()));
      stringB.append("|1");
      stringB.append("|" + objCFD.getCfdType() + "||||\r\n");
    }

    List<CFDIssued> listC = this.cfdiDao.listCancel(year, month, rfc);
    for (CFDIssued objCFD : listC) {
      stringB.append("|" + objCFD.getTaxIdReceiver());
      if ((objCFD.getFolioRange() != null) && 
        (objCFD.getFolioRange().getSeries() != null))
      {
        stringB.append("|" + 
          objCFD.getFolioRange().getSeries().getName());
      }
      else
        stringB.append("|");
      stringB.append("|" + objCFD.getFolio());
      if (objCFD.getFormatType() == 0)
      {
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(objCFD.getDateOfIssuance());
        stringB.append("|" + cal2.get(1));
      }
      else
      {
        stringB.append("|" + 
          objCFD.getFolioRange().getYearOfAuthorization() + 
          objCFD.getFolioRange().getAuthorizationNumber());
      }
      stringB.append("|" + 
        Util.convertirFecha(objCFD.getDateOfIssuance(), null));
      stringB.append("|" + Util.formatNumber(objCFD.getTotal()));
      stringB.append("|" + Util.formatNumber(objCFD.getIva()));
      stringB.append("|0");
      stringB.append("|" + objCFD.getCfdType() + "||||\r\n");
    }
    try
    {
      out.write(stringB.toString().getBytes());
    } catch (IOException e) {
      e.printStackTrace();
    }return out;
  }

  public CFDIssued get(long id) {
    CFDIssued cfdIssued = (CFDIssued)this.cfdiDao.findById(Long.valueOf(id));
    if (cfdIssued == null) {
      CFDIssuedOtros cfdOtros = (CFDIssuedOtros)this.cfdiOtrosDao.findById(Long.valueOf(id));
      cfdIssued = cfdOtrosToCfdIssued(cfdOtros);
    }
    return cfdIssued;
  }

  public FiscalEntity getFiscalEntity(long id) {
    return (FiscalEntity)this.feDao.findById(Long.valueOf(id));
  }
  public FolioRange getFolioRange(long id) {
    return (FolioRange)this.frDao.findById(Long.valueOf(id));
  }

	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = { Exception.class })
	public CFDIssued update(CFDIssued cFDIssued) {
		/*mLlovera Cambio: Se coloca la fecha para PK*/
		cFDIssued.setIssueDate(Calendar.getInstance().getTime());
		if (cFDIssued.getFormatType() != 0) {
			CFDIssuedOtros cfdOtros = cfdIssuedToCfdOtros(cFDIssued);
			cfdOtros.setFoliosComplPago(getConcatenatedFolioPagosComplementoPago(cFDIssued));
			Route route = (Route) this.routeDao.update(cfdOtros.getFilePath());
			cfdOtros.setFilePath(route);
			cfdOtros = (CFDIssuedOtros) this.cfdiOtrosDao.update(cfdOtros);
			CFDIssued cFDIssuedNew =cfdOtrosToCfdIssued(cfdOtros);
			if(cFDIssued.getPagos()!=null && !cFDIssued.getPagos().isEmpty()) {
				cFDIssuedNew.setPagos(cFDIssued.getPagos());
			}
			return cFDIssuedNew;
		}
		return this.cfdiDao.update(cFDIssued);
	}

	public void update(List<CFDIssued> lstCFD) {
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("txManagerCFD");
		def.setPropagationBehavior(3);

		TransactionStatus status = this.transactionManager.getTransaction(def);
		for (CFDIssued obj : lstCFD) {
			if (obj.getFormatType() != 0) {
				CFDIssuedOtros cfdOtros = cfdIssuedToCfdOtros(obj);
				Route route = (Route) this.routeDao.update(cfdOtros
						.getFilePath());
				cfdOtros.setFilePath(route);
				cfdOtros.setFoliosComplPago(getConcatenatedFolioPagosComplementoPago(obj));
				cfdOtros = (CFDIssuedOtros) this.cfdiOtrosDao.update(cfdOtros);
				cfdOtrosToCfdIssued(cfdOtros);
			} else {
				/*mLlovera Cambio: Se coloca la fecha para PK*/
				obj.setIssueDate(Calendar.getInstance().getTime());
				this.cfdiDao.update(obj);
			}
		}
		this.transactionManager.commit(status);
	}
  @Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor={Exception.class})
  public void delete(long id) {
    CFDIssued cfdi = (CFDIssued)this.cfdiDao.findById(Long.valueOf(id));
    if (cfdi == null) {
      CFDIssuedOtros cfdOtros = (CFDIssuedOtros)this.cfdiOtrosDao.findById(Long.valueOf(id));
      this.cfdiOtrosDao.remove(cfdOtros);
    } else {
      this.cfdiDao.remove(cfdi);
    }
  }

  @Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor={Exception.class})
  public void create(CFDIssued cFDIssued) {
    if ((cFDIssued != null) && (cFDIssued.getFormatType() != 0)) {
      CFDIssuedOtros cfdOtros = cfdIssuedToCfdOtros(cFDIssued);
      this.cfdiOtrosDao.persist(cfdOtros);
    } else {
      this.cfdiDao.persist(cFDIssued);
    }
  }

  private CFDIssued cfdOtrosToCfdIssued(CFDIssuedOtros cfdOtros) {
    CFDIssued cfdIssued = new CFDIssued();
    if (cfdOtros != null) {
      cfdIssued.setAddendumCustoms(cfdOtros.getAddendumCustoms());
      cfdIssued.setAuthor(cfdOtros.getAuthor());

      cfdIssued.setCancellationDate(cfdOtros.getCancellationDate());
      cfdIssued.setCfdType(cfdOtros.getCfdType());
      cfdIssued.setComplement(cfdOtros.getComplement());
      cfdIssued.setContractNumber(cfdOtros.getContractNumber());
      cfdIssued.setCostCenter(cfdOtros.getCostCenter());
      cfdIssued.setCreationDate(cfdOtros.getCreationDate());
      cfdIssued.setCustomerCode(cfdOtros.getCustomerCode());
      cfdIssued.setDateOfIssuance(cfdOtros.getDateOfIssuance());
      cfdIssued.setEndLine(cfdOtros.getEndLine());
      cfdIssued.setFilePath(cfdOtros.getFilePath());
      cfdIssued.setFiscalEntity(cfdOtros.getFiscalEntity());
      cfdIssued.setFolio(cfdOtros.getFolio());
      cfdIssued.setFolioInterno(cfdOtros.getFolioInterno() != null ? Long.valueOf(cfdOtros.getFolioInterno()).longValue() : 0L);
      cfdIssued.setFolioRange(cfdOtros.getFolioRange());
      cfdIssued.setFolioSAT(cfdOtros.getFolioSAT());
      cfdIssued.setFormatType(cfdOtros.getFormatType());
      cfdIssued.setId(cfdOtros.getId());
      cfdIssued.setIsCFDI(cfdOtros.getIsCFDI());
      cfdIssued.setIssueDate(cfdOtros.getIssueDate());
      cfdIssued.setIva(cfdOtros.getIva());
      cfdIssued.setModifiedBy(cfdOtros.getModifiedBy());
      cfdIssued.setPeriod(cfdOtros.getPeriod());
      cfdIssued.setserieInfo(cfdOtros.getSerieInfo());
      cfdIssued.setSourceFileName(cfdOtros.getSourceFileName());
      cfdIssued.setStartLine(cfdOtros.getStartLine());
      cfdIssued.setStatus(cfdOtros.getStatus());
      cfdIssued.setSubTotal(cfdOtros.getSubTotal());
      cfdIssued.setTaxIdReceiver(cfdOtros.getTaxIdReceiver());

      cfdIssued.setTotal(cfdOtros.getTotal());
      cfdIssued.setProcessID(cfdOtros.getProcessID());
      cfdIssued.setXmlRoute(cfdOtros.getXmlRoute());
    }
    return cfdIssued;
  }

  private CFDIssuedOtros cfdIssuedToCfdOtros(CFDIssued cfdIssued) {
    CFDIssuedOtros cfdIssuedOtros = new CFDIssuedOtros();
    if (cfdIssued != null) {
      cfdIssuedOtros.setAddendumCustoms(cfdIssued.getAddendumCustoms());
      cfdIssuedOtros.setAuthor(cfdIssued.getAuthor());

      cfdIssuedOtros.setCancellationDate(cfdIssued.getCancellationDate());
      cfdIssuedOtros.setCfdType(cfdIssued.getCfdType());
      cfdIssuedOtros.setComplement(cfdIssued.getComplement());
      cfdIssuedOtros.setContractNumber(cfdIssued.getContractNumber());
      cfdIssuedOtros.setCostCenter(cfdIssued.getCostCenter());
      cfdIssuedOtros.setCreationDate(cfdIssued.getCreationDate());
      cfdIssuedOtros.setCustomerCode(cfdIssued.getCustomerCode());
      cfdIssuedOtros.setDateOfIssuance(cfdIssued.getDateOfIssuance());
      cfdIssuedOtros.setEndLine(cfdIssued.getEndLine());
      cfdIssuedOtros.setFilePath(cfdIssued.getFilePath());
      cfdIssuedOtros.setFiscalEntity(cfdIssued.getFiscalEntity());
      cfdIssuedOtros.setFolio(cfdIssued.getFolio());
      cfdIssuedOtros.setFolioInterno(String.valueOf(cfdIssued.getFolioInterno()));
      cfdIssuedOtros.setFolioRange(cfdIssued.getFolioRange());
      cfdIssuedOtros.setFolioSAT(cfdIssued.getFolioSAT());
      cfdIssuedOtros.setFormatType(cfdIssued.getFormatType());
      cfdIssuedOtros.setId(cfdIssued.getId());
      cfdIssuedOtros.setIsCFDI(cfdIssued.getIsCFDI());
      cfdIssuedOtros.setIssueDate(cfdIssued.getIssueDate());
      cfdIssuedOtros.setIva(cfdIssued.getIva());
      cfdIssuedOtros.setModifiedBy(cfdIssued.getModifiedBy());
      cfdIssuedOtros.setPeriod(cfdIssued.getPeriod());
      cfdIssuedOtros.setSerieInfo(cfdIssued.getserieInfo());
      cfdIssuedOtros.setSourceFileName(cfdIssued.getSourceFileName());
      cfdIssuedOtros.setStartLine(cfdIssued.getStartLine());
      cfdIssuedOtros.setStatus(cfdIssued.getStatus());
      cfdIssuedOtros.setSubTotal(cfdIssued.getSubTotal());
      cfdIssuedOtros.setTaxIdReceiver(cfdIssued.getTaxIdReceiver());

      cfdIssuedOtros.setTotal(cfdIssued.getTotal());
      cfdIssuedOtros.setProcessID(cfdIssued.getProcessID());
      cfdIssuedOtros.setXmlRoute(cfdIssued.getXmlRoute());
    }
    return cfdIssuedOtros;
  }

  private void copyCfdOtrosToCfdIssuedList(List<CFDIssued> cfdIssuedList, List<CFDIssuedOtros> cfdIssuedOtrosList) {
    if ((cfdIssuedList != null) && (cfdIssuedOtrosList != null))
      for (CFDIssuedOtros cfdOtros : cfdIssuedOtrosList) {
        CFDIssued cfdIssued = cfdOtrosToCfdIssued(cfdOtros);
        cfdIssuedList.add(cfdIssued);
      }
  }
  
	/**
	 * Metodo para concatener los folios de los pagos separados por comas
	 * 
	 * @param cfdIssued
	 * @return
	 */
	public String getConcatenatedFolioPagosComplementoPago(CFDIssued cfdIssued) {
		StringBuilder result = new StringBuilder();
		for (ComplementoPago pago : cfdIssued.getPagos()) {
			if (pago.getFolioPago() != null && !pago.getFolioPago().trim().isEmpty()) {
				result.append(pago.getFolioPago() + ",");
			}
		}
		if (result.length() > 1000) {
			result.setLength(1000);
		}
		int lastIndex = result.lastIndexOf(",");
		if (lastIndex != -1) {
			result.delete(lastIndex, result.length());
		}
		return result.toString();
	}
}