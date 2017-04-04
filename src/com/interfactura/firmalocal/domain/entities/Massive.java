package com.interfactura.firmalocal.domain.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.springmodules.validation.bean.conf.loader.annotation.handler.NotBlank;

@Entity
public class Massive implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue
	private long id;
	private String ipTerminal;
	private String operation;
	private String author;
	private Date creationDate;
	private Date issueDate;
	private String modifiedBy;
	
	@Column
	@NotBlank
	private String filename;
	
	@Column
	@NotBlank
	private String ciffilename;
	
	@Column
	@NotBlank
	private String xmlfilename;
	
	@Column
	@NotBlank
	private String incfilename;
	
	@Column
	@NotBlank
	private int status;
	
	@Column
	@NotBlank
	private Date uploadfiledate;
	
	@Column
	@NotBlank
	private Date downloadfiledate;
	
	@Column
	@NotBlank
	private Date startprocessdate;
	
	@Column
	@NotBlank
	private Date endprocessdate;

	@Column
	@NotBlank
	private int cfdtype;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getIpTerminal() {
		return ipTerminal;
	}

	public void setIpTerminal(String ipTerminal) {
		this.ipTerminal = ipTerminal;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
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

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getCiffilename() {
		return ciffilename;
	}

	public void setCiffilename(String ciffilename) {
		this.ciffilename = ciffilename;
	}

	public String getXmlfilename() {
		return xmlfilename;
	}

	public void setXmlfilename(String xmlfilename) {
		this.xmlfilename = xmlfilename;
	}

	public String getIncfilename() {
		return incfilename;
	}

	public void setIncfilename(String incfilename) {
		this.incfilename = incfilename;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Date getUploadfiledate() {
		return uploadfiledate;
	}

	public void setUploadfiledate(Date uploadfiledate) {
		this.uploadfiledate = uploadfiledate;
	}

	public Date getDownloadfiledate() {
		return downloadfiledate;
	}

	public void setDownloadfiledate(Date downloadfiledate) {
		this.downloadfiledate = downloadfiledate;
	}

	public Date getStartprocessdate() {
		return startprocessdate;
	}

	public void setStartprocessdate(Date startprocessdate) {
		this.startprocessdate = startprocessdate;
	}

	public Date getEndprocessdate() {
		return endprocessdate;
	}

	public void setEndprocessdate(Date endprocessdate) {
		this.endprocessdate = endprocessdate;
	}

	public int getCfdtype() {
		return cfdtype;
	}

	public void setCfdtype(int cfdtype) {
		this.cfdtype = cfdtype;
	}

	
}
