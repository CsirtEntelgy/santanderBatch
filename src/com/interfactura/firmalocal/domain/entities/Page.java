package com.interfactura.firmalocal.domain.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.validation.constraints.Size;

@Entity
public class Page extends BaseEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Size(min = 1)
	private String route;

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}
}
