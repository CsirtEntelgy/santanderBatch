package com.interfactura.firmalocal.domain.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

@Entity
@SequenceGenerator(sequenceName = "CFD_ISSUEDIN_SEQ", name = "CFD_ISSUEDIN_SEQ_GEN")
public class CFDIssuedIn implements Serializable 
	//extends BaseEntity implements Serializable 
{
	private static final long serialVersionUID = 1L;

	private String sourceFileName;
	private String startLine;
	private String endLine;
	private String complement;
	private String errorMessage;
	
	@Id
	@GeneratedValue(generator = "CFD_ISSUEDIN_SEQ_GEN", strategy = GenerationType.SEQUENCE)
	private long id;
	private String author;
	private Date creationDate;
	private Date issueDate;
	private String modifiedBy;

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

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	//--- OCT 28 Dic. Secuencias Santander
	
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
}

