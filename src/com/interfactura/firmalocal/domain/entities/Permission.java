package com.interfactura.firmalocal.domain.entities;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.validation.constraints.Size;


@Entity
public class Permission extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Size(min = 1)
	private String name;
	@OneToMany(targetEntity = com.interfactura.firmalocal.domain.entities.Page.class, cascade=CascadeType.PERSIST, fetch = FetchType.EAGER)
	@JoinTable(name = "PERMISSION_PAGE", joinColumns = @JoinColumn(name = "PERMISSION_ID"), inverseJoinColumns = @JoinColumn(name = "PAGES_ID"))
	private Set<Page> pages;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<Page> getPages() {
		return pages;
	}

	public void setPages(Set<Page> pages) {
		this.pages = pages;
	}
}
