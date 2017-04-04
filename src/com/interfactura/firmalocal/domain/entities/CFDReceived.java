package com.interfactura.firmalocal.domain.entities;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name="CFDRECEIVED",  uniqueConstraints = @UniqueConstraint(columnNames = {"folio","serie","taxIDEmisor"}))
//@SequenceGenerator(sequenceName = "CFDRECEIVED_SEQ", name = "CFDRECEIVED_SEQ_GEN")
public class CFDReceived extends BaseEntity implements Serializable 
{
	
	private static final long serialVersionUID = 1L;

	//@Id
	//@GeneratedValue(generator = "CFDRECEIVED_SEQ_GEN", strategy = GenerationType.SEQUENCE)
	//private long id;
	private Long folio;
	private String taxIDEmisor;
	private String taxIDReceptor;
	private String physicalName;
	private Double total;
	@OneToOne(cascade=CascadeType.ALL)
	private Route filePath;
	private Date dateOfRemission;
	private Date dateOfReception;
	private Date cancelationDate;
	private String serie;
	private String status;
	private Integer approbationYear;
	private String certificateNumber;
	private Long approbationNumber;
	
	public void setCancelationDate(Date cancelationDate) {
		this.cancelationDate = cancelationDate;
	}
	
	public Date getCancelationDate() {
		return cancelationDate;
	}

	public void setApprobationNumber(Long approbationNumber) {
		this.approbationNumber = approbationNumber;
	}
	
	public Long getApprobationNumber() {
		return approbationNumber;
	}
	
	public void setCertificateNumber(String certificateNumber) {
		this.certificateNumber = certificateNumber;
	}

	public String getCertificateNumber() {
		return certificateNumber;
	}
	
	public void setApprobationYear(Integer approbationYear) {
		this.approbationYear = approbationYear;
	}
	
	public Integer getApprobationYear() {
		return approbationYear;
	}
	
	public Long getFolio() {
		return folio;
	}
	public void setFolio(Long folio) {
		this.folio = folio;
	}
	
	public String getSerie() {
		return serie;
	}
	
	public void setSerie(String serie) {
		this.serie = serie;
	}
	
	public void setFilePath(Route filePath) {
		this.filePath = filePath;
	}
	
	public String getTaxIDEmisor() {
		return taxIDEmisor;
	}
	
	public void setTaxIDEmisor(String taxIDEmisor) {
		this.taxIDEmisor = taxIDEmisor;
	}
	
	public String getPhysicalName() {
		return physicalName;
	}
	
	public void setPhysicalName(String physicalName) {
		this.physicalName = physicalName;
	}
	
	public Double getTotal() {
		return total;
	}
	
	public void setTotal(Double total) {
		this.total = total;
	}
	
	public Date getDateOfRemission() {
		return dateOfRemission;
	}
	
	public void setDateOfRemission(Date dateOfRemission) {
		this.dateOfRemission = dateOfRemission;
	}
	
	public Date getDateOfReception() {
		return dateOfReception;
	}
	
	public void setDateOfReception(Date dateOfReception) {
		this.dateOfReception = dateOfReception;
	}
	
	public Route getFilePath() {
		return filePath;
	}
	
    public void setStatus(String status) {
    	if( ( status == null ) || ( status.length() == 0) ){
    		this.status = "A";
    	}else{
    		this.status = status;
    	}	
	}
    
    public String getStatus() {
		return status;
	}
    
    public String getTaxIDReceptor() {
		return taxIDReceptor;
	}
    
    public void setTaxIDReceptor(String taxIDReceptor) {
		this.taxIDReceptor = taxIDReceptor;
	}
    
   public String getDateOfReceptionString() {
    	String pattern = "yyyy-MM-dd HH:mm:ss";
    	SimpleDateFormat sformat = new SimpleDateFormat(pattern);
		return sformat.format(dateOfReception);
	}
    
    public String getDateOfRemissionString() {
    	String pattern = "yyyy-MM-dd HH:mm:ss";
    	SimpleDateFormat sformat = new SimpleDateFormat(pattern);
		return sformat.format(dateOfRemission);
	}
	
}
