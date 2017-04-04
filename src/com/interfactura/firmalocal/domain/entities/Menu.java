package com.interfactura.firmalocal.domain.entities;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.validation.constraints.Size;


@Entity
public class Menu extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Size(min = 1)
	private String name;
	@OneToOne(cascade=CascadeType.PERSIST)
	private Page page;
	@Column(name="orderMenu")
	private int order;
	private int fatherId;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Page getPage() {
		return page;
	}

	public void setPage(Page page) {
		this.page = page;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public int getFatherId() {
		return fatherId;
	}

	public void setFatherId(int fatherId) {
		this.fatherId = fatherId;
	}
}
