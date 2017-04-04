package com.interfactura.firmalocal.domain.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.springmodules.validation.bean.conf.loader.annotation.handler.Length;
import org.springmodules.validation.bean.conf.loader.annotation.handler.NotBlank;

@Entity
public class MassiveReport implements Serializable {
	
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
	@Length(min=1,max=4000)
	private String strQuery;
	
	@Column
	@NotBlank
	private int status;
	
	@Column
	@NotBlank
	private Date requestdate;
	
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

	public String getStrQuery() {
		return strQuery;
	}

	public void setStrQuery(String strQuery) {
		this.strQuery = strQuery;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Date getRequestdate() {
		return requestdate;
	}

	public void setRequestdate(Date requestdate) {
		this.requestdate = requestdate;
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
