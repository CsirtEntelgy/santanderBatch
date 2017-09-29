package com.interfactura.firmalocal.domain.entities;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

@Entity
@SequenceGenerator(sequenceName="CFD_ISSUED_SEQ", name="CFD_ISSUED_SEQ_GEN")
public class CFDIssuedOtros
  implements Serializable
{
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue
  @Column(name="ID_CFD")
  private long id;

  @Column(name="TXT_AUTH")
  private String author;

  @Column(name="FCH_CREAT_DATE")
  private Date creationDate;

  @Column(name="FCH_ISSUE_DATE")
  private Date issueDate;

  @Column(name="TXT_MODIF_BY")
  private String modifiedBy;

  @Column(name="NUM_FOLIO")
  private String folio;

  @Column(name="NUM_STATS")
  private int status;

  @Column(name="COD_TAX_ID_RECVR")
  @Size(min=1)
  private String taxIdReceiver;

  @Column(name="FCH_CNCEL_DATE")
  private Date cancellationDate;

  @Column(name="IMP_SUB_TOTAL")
  private Double subTotal;

  @Column(name="POR_IVA")
  private Double iva;

  @OneToOne(cascade={javax.persistence.CascadeType.MERGE})
  @JoinColumn(name="ID_FILE_PATH")
  private Route filePath;

  @Column(name="FCH_DATE_ISSNC")
  private Date dateOfIssuance;

  @Column(name="IMP_TOTAL")
  private Double total;

  @OneToOne(cascade={javax.persistence.CascadeType.PERSIST})
  @JoinColumn(name="ID_FOLIO_RANGE")
  private FolioRange folioRange;

  @OneToOne(cascade={javax.persistence.CascadeType.PERSIST})
  @JoinColumn(name="ID_FSCAL_ENTTY")
  private FiscalEntity fiscalEntity;

  @OneToMany(targetEntity=AddendumCustoms.class, cascade={javax.persistence.CascadeType.ALL}, fetch=FetchType.EAGER)
  @JoinTable(name="CFDISSUED_ADDENDUMCUSTOMS", joinColumns={@JoinColumn(name="CFDISSUED_ID")}, inverseJoinColumns={@JoinColumn(name="ADDENDUMCUSTOMS_ID")})
  private Set<AddendumCustoms> addendumCustoms;

  @Column(name="COD_CSTMR")
  private String customerCode;

  @Column(name="TXT_PERIO")
  private String period;

  @Column(name="NUM_CTRCT")
  private String contractNumber;

  @Column(name="COD_COST_CENTR")
  private String costCenter;

  @Column(name="COD_FRMAT_TYPE")
  private int formatType;

  @Column(name="COD_CFD_TYPE")
  private String cfdType;

  @Column(name="TXT_SRC_FILE_NAME")
  private String sourceFileName;

  @Column(name="TXT_START_LINE")
  private String startLine;

  @Column(name="TXT_END_LINE")
  private String endLine;

  @Column(name="TXT_COMPL")
  private String complement;

  @Column(name="TXT_SERIE_INFO")
  private String serieInfo;

  @Column(name="TXT_FOLIO_SAT")
  private String folioSAT;

  @Column(name="FLG_IS_CFDI")
  private int isCFDI;

  @Column(name="NUM_FOLIO_INT")
  private String folioInterno;

  @Column(name="TXT_XML_ROUTE")
  private String xmlRoute;

  @Column(name="TXT_BIT1")
  private String bit1;

  @Column(name="TXT_BIT2")
  private String bit2;

  @Column(name="ID_PROC")
  private String processID;

  @Transient
  private String tipo;

  @Transient
  private String estatus;
  
	@Column(name = "FOLIOS_COMPL_PAGO")
	private String foliosComplPago;

  public CFDIssuedOtros(long id, String folio, int formatType, int status, long taxId, String taxIdReceiver, Date cancellationDate, Date dateOfIssuance, Double subTotal, Double iva, Double total, String serieInfo)
  {
    setId(id);
    this.folio = folio;
    this.formatType = formatType;
    this.status = status;
    this.fiscalEntity.setId(taxId);
    this.taxIdReceiver = taxIdReceiver;
    this.cancellationDate = cancellationDate;
    this.dateOfIssuance = dateOfIssuance;
    this.subTotal = subTotal;
    this.iva = iva;
    this.total = total;
    this.serieInfo = serieInfo;
  }

  public CFDIssuedOtros()
  {
  }

  public String getFolio() {
    return this.folio;
  }

  public void setFolio(String folio) {
    this.folio = folio;
  }

  public int getStatus() {
    return this.status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public String getTaxIdReceiver() {
    return this.taxIdReceiver;
  }

  public void setTaxIdReceiver(String taxIdReceiver) {
    this.taxIdReceiver = taxIdReceiver;
  }

  public Double getSubTotal() {
    return this.subTotal;
  }

  public void setSubTotal(Double subTotal) {
    this.subTotal = subTotal;
  }

  public Double getIva() {
    return this.iva;
  }

  public void setIva(Double iva) {
    this.iva = iva;
  }

  public Route getFilePath() {
    return this.filePath;
  }

  public void setFilePath(Route filePath) {
    this.filePath = filePath;
  }

  public Double getTotal() {
    return this.total;
  }

  public void setTotal(Double total) {
    this.total = total;
  }

  public FolioRange getFolioRange() {
    return this.folioRange;
  }

  public void setFolioRange(FolioRange folioRange) {
    this.folioRange = folioRange;
  }

  public String getCustomerCode() {
    return this.customerCode;
  }

  public void setCustomerCode(String customerCode) {
    this.customerCode = customerCode;
  }

  public String getPeriod() {
    return this.period;
  }

  public void setPeriod(String period) {
    this.period = period;
  }

  public String getContractNumber() {
    return this.contractNumber;
  }

  public void setContractNumber(String contractNumber) {
    this.contractNumber = contractNumber;
  }

  public String getCostCenter() {
    return this.costCenter;
  }

  public void setCostCenter(String costCenter) {
    this.costCenter = costCenter;
  }

  public FiscalEntity getFiscalEntity() {
    return this.fiscalEntity;
  }

  public void setFiscalEntity(FiscalEntity fiscalEntity) {
    this.fiscalEntity = fiscalEntity;
  }

  public int getFormatType() {
    return this.formatType;
  }

  public void setFormatType(int formatType) {
    this.formatType = formatType;
  }

  public String getDateOfIssuanceString() {
    if (this.dateOfIssuance != null) {
      return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(this.dateOfIssuance);
    }
    return new String("");
  }

  public String getCancellationDateString() {
    if (getCancellationDate() != null) {
      return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(getCancellationDate());
    }
    return new String("");
  }

  public String getCfdType() {
    if (this.cfdType == null) {
      return "I";
    }
    return this.cfdType;
  }

  public void setCfdType(String cfdType)
  {
    this.cfdType = cfdType;
  }

  public String getSourceFileName() {
    return this.sourceFileName;
  }

  public void setSourceFileName(String sourceFileName) {
    this.sourceFileName = sourceFileName;
  }

  public String getStartLine() {
    return this.startLine;
  }

  public void setStartLine(String startLine) {
    this.startLine = startLine;
  }

  public String getEndLine() {
    return this.endLine;
  }

  public void setEndLine(String endLine) {
    this.endLine = endLine;
  }

  public String getComplement() {
    return this.complement;
  }

  public void setComplement(String complement) {
    this.complement = complement;
  }

  public Set<AddendumCustoms> getAddendumCustoms() {
    return this.addendumCustoms;
  }

  public void setAddendumCustoms(Set<AddendumCustoms> addendumCustoms) {
    this.addendumCustoms = addendumCustoms;
  }

  public void setCancellationDate(Date cancellationDate) {
    this.cancellationDate = cancellationDate;
  }

  public Date getCancellationDate() {
    return this.cancellationDate;
  }

  public void setDateOfIssuance(Date dateOfIssuance) {
    this.dateOfIssuance = dateOfIssuance;
  }

  public Date getDateOfIssuance() {
    return this.dateOfIssuance;
  }

  public String getSerieInfo() {
    return this.serieInfo;
  }

  public void setSerieInfo(String serieInfo) {
    this.serieInfo = serieInfo;
  }

  public String getTipo() {
    return this.tipo;
  }

  public void setTipo(String tipo) {
    this.tipo = tipo;
  }

  public String getEstatus() {
    return this.estatus;
  }

  public void setEstatus(String estatus) {
    this.estatus = estatus;
  }

  public String getFolioSAT() {
    return this.folioSAT;
  }

  public void setFolioSAT(String folioSAT) {
    this.folioSAT = folioSAT;
  }

  public int getIsCFDI() {
    return this.isCFDI;
  }

  public void setIsCFDI(int isCFDI) {
    this.isCFDI = isCFDI;
  }

  public String getFolioInterno() {
    return this.folioInterno;
  }

  public void setFolioInterno(String folioInterno) {
    this.folioInterno = folioInterno;
  }

  public String getBit1() {
    return this.bit1;
  }

  public void setBit1(String bit1) {
    this.bit1 = bit1;
  }

  public String getBit2() {
    return this.bit2;
  }

  public void setBit2(String bit2) {
    this.bit2 = bit2;
  }

  public long getId() {
    return this.id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getAuthor() {
    return this.author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public Date getCreationDate() {
    return this.creationDate;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public Date getIssueDate() {
    return this.issueDate;
  }

  public void setIssueDate(Date issueDate) {
    this.issueDate = issueDate;
  }

  public String getModifiedBy() {
    return this.modifiedBy;
  }

  public void setModifiedBy(String modifiedBy) {
    this.modifiedBy = modifiedBy;
  }

  public String getXmlRoute() {
    return this.xmlRoute;
  }

  public void setXmlRoute(String xmlRoute) {
    this.xmlRoute = xmlRoute;
  }

  public String getProcessID() {
    return this.processID;
  }

  public void setProcessID(String processID) {
    this.processID = processID;
  }

	public String getFoliosComplPago() {
		return foliosComplPago;
	}

	public void setFoliosComplPago(String foliosComplPago) {
		this.foliosComplPago = foliosComplPago;
	}
}