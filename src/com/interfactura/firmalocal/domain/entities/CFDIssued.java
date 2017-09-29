package com.interfactura.firmalocal.domain.entities;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

import com.interfactura.firmalocal.datamodel.ComplementoPago;


@Entity
@SequenceGenerator(sequenceName = "CFD_ISSUED_SEQ", name = "CFD_ISSUED_SEQ_GEN")
public class CFDIssued implements Serializable 
	//extends BaseEntity implements Serializable 
{

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator = "CFD_ISSUED_SEQ_GEN", strategy = GenerationType.SEQUENCE)
	private long id;
	private String author;
	private Date creationDate;
	private Date issueDate;
	private String modifiedBy;
	
	private String folio;
	private int status;
	@Size(min = 1)
	private String taxIdReceiver;
	private Date cancellationDate;
	private Double subTotal;
	private Double iva;
	@OneToOne(cascade=CascadeType.MERGE)
	private Route filePath;
	private Date dateOfIssuance;
	private Double total;
	@OneToOne(cascade=CascadeType.PERSIST)
	private FolioRange folioRange;
	@OneToOne(cascade=CascadeType.PERSIST)
	private FiscalEntity fiscalEntity;
	@OneToMany(targetEntity = com.interfactura.firmalocal.domain.entities.AddendumCustoms.class, cascade=CascadeType.ALL, fetch = FetchType.EAGER)
	private Set <AddendumCustoms> addendumCustoms;
	private String customerCode;
	private String period;
	private String contractNumber;
	private String costCenter;
	private int formatType;
	private String cfdType;
	private String sourceFileName;
	private String startLine;
	private String endLine;
	private String complement;
	private String processID;
	private String xmlRoute;

	private String serieInfo;

	//CFDI 
	private String folioSAT;
	private int isCFDI;
	private long folioInterno;
	@Transient
	private List<ComplementoPago>	pagos;
	
	
	public String getserieInfo() {
		return serieInfo;
	}

	public void setserieInfo(String serieInfo) {
		this.serieInfo = serieInfo;
	}
	
	public String getProcessID() {
		return processID;
	}

	public void setProcessID(String processID) {
		this.processID = processID;
	}

	public String getXmlRoute() {
		return xmlRoute;
	}

	public void setXmlRoute(String xmlRoute) {
		this.xmlRoute = xmlRoute;
	}

	public String getFolio() {
		return folio;
	}

	public void setFolio(String folio) {
		this.folio = folio;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getTaxIdReceiver() {
		return taxIdReceiver;
	}

	public void setTaxIdReceiver(String taxIdReceiver) {
		this.taxIdReceiver = taxIdReceiver;
	}

	public Date getCancellationDate() {
		return cancellationDate;
	}

	public void setCancellationDate(Date cancellationDate) {
		this.cancellationDate = cancellationDate;
	}

	public Double getSubTotal() {
		return subTotal;
	}

	public void setSubTotal(Double subTotal) {
		this.subTotal = subTotal;
	}

	public Double getIva() {
		return iva;
	}

	public void setIva(Double iva) {
		this.iva = iva;
	}

	public Route getFilePath() {
		return filePath;
	}

	public void setFilePath(Route filePath) {
		this.filePath = filePath;
	}

	public Date getDateOfIssuance() {
		return dateOfIssuance;
	}

	public void setDateOfIssuance(Date dateOfIssuance) {
		this.dateOfIssuance = dateOfIssuance;
	}

	public Double getTotal() {
		return total;
	}

	public void setTotal(Double total) {
		this.total = total;
	}

	public FolioRange getFolioRange() {
		return folioRange;
	}

	public void setFolioRange(FolioRange folioRange) {
		this.folioRange = folioRange;
	}

	public String getCustomerCode() {
		return customerCode;
	}

	public void setCustomerCode(String customerCode) {
		this.customerCode = customerCode;
	}

	public String getPeriod() {
		return period;
	}

	public void setPeriod(String period) {
		this.period = period;
	}

	public String getContractNumber() {
		return contractNumber;
	}

	public void setContractNumber(String contractNumber) {
		this.contractNumber = contractNumber;
	}

	public String getCostCenter() {
		return costCenter;
	}

	public void setCostCenter(String costCenter) {
		this.costCenter = costCenter;
	}

	public FiscalEntity getFiscalEntity() {
		return fiscalEntity;
	}

	public void setFiscalEntity(FiscalEntity fiscalEntity) {
		this.fiscalEntity = fiscalEntity;
	}

	public int getFormatType() {
		return formatType;
	}

	public void setFormatType(int formatType) {
		this.formatType = formatType;
	}
	
	public String getDateOfIssuanceString() {
		if( dateOfIssuance != null )
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dateOfIssuance);
		else
			return new String("");
	}
	
	public String getCancellationDateString() {
		if( cancellationDate != null )
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cancellationDate);
		else
			return new String("");
	}

	public String getCfdType() {
		return cfdType;
	}

	public void setCfdType(String cfdType) {
		this.cfdType = cfdType;
	}

	public String getSourceFileName() {
		return sourceFileName;
	}

	public void setSourceFileName(String sourceFileName) {
		this.sourceFileName = sourceFileName;
	}

	public String getStartLine() {
		return startLine;
	}

	public void setStartLine(String startLine) {
		this.startLine = startLine;
	}

	public String getEndLine() {
		return endLine;
	}

	public void setEndLine(String endLine) {
		this.endLine = endLine;
	}

	public String getComplement() {
		return complement;
	}

	public void setComplement(String complement) {
		this.complement = complement;
	}

	public Set<AddendumCustoms> getAddendumCustoms() {
		return addendumCustoms;
	}

	public void setAddendumCustoms(Set<AddendumCustoms> addendumCustoms) {
		this.addendumCustoms = addendumCustoms;
	}
	
	//OCT 28 Dic Secuencias Santander
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getIssueDate() {
		return issueDate;
	}

	public void setIssueDate(Date issueDate) {
		this.issueDate = issueDate;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public String getFolioSAT() {
		return folioSAT;
	}

	public void setFolioSAT(String folioSAT) {
		this.folioSAT = folioSAT;
	}

	public int getIsCFDI() {
		return isCFDI;
	}

	public void setIsCFDI(int isCFDI) {
		this.isCFDI = isCFDI;
	}

	public long getFolioInterno() {
		return folioInterno;
	}

	public void setFolioInterno(long folioInterno) {
		this.folioInterno = folioInterno;
	}

	public List<ComplementoPago> getPagos() {
		return pagos;
	}

	public void setPagos(List<ComplementoPago> pagos) {
		this.pagos = pagos;
	}
	
}
