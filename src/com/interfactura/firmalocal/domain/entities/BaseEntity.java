package com.interfactura.firmalocal.domain.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class BaseEntity implements Serializable//, Comparable<Object> 
{

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private long id;
	private String author;
	private Date creationDate;
	private Date issueDate;
	private String modifiedBy;

	// --- Comparable

	// Basic implementation. It is only reliable to validate if objects are
	// equal
	// or not. For more precision override in subclasses.
	/*
	public int compareTo(Object o) 
		throws FirmalocalRuntimeException 
	{
		if (o == null) {
			return 1;
		} else if (this.getClass().isInstance(o)) {
			Method[] methods = this.getClass().getMethods();
			int result = 0;
			try {
				for (int i = 0; i < methods.length; i++) {
					Object localValue = methods[i].invoke(this, null);
					Object externalValue = methods[i].invoke(o, null);
					if ((localValue == null) && (externalValue == null)) {
						continue;
					}
					if ((localValue == null) || (externalValue == null)) {
						return -1;
					}
					if (!(localValue.equals(externalValue))) {
						return -1;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new FirmalocalRuntimeException(e);
			}
			return 0;
		} else {
			throw new ClassCastException("Los objectos del tipo "
					+ this.getClass().getName() + " y "
					+ o.getClass().getName() + " no son comparables");
		}
	}

	*/
	
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
