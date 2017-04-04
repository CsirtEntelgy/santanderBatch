package com.interfactura.firmalocal.domain.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.Size;


@Entity
@SequenceGenerator(sequenceName = "CFD_ROUTE_SEQ", name = "CFD_ROUTE_SEQ_GEN")
public class Route 
	implements Serializable 
{

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator = "CFD_ROUTE_SEQ_GEN", strategy = GenerationType.SEQUENCE)
	private long id;
	private String author;
	private Date creationDate;
	private Date issueDate;
	private String modifiedBy;
	
	@Size(min = 1)
	private String route;

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}

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
