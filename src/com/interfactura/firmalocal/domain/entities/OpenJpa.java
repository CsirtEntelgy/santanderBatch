package com.interfactura.firmalocal.domain.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "openjpa_sequence_table")
public class OpenJpa implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	private long id;
	private long sequence_value;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getSequence_value() {
		return sequence_value;
	}
	public void setSequence_value(long sequence_value) {
		this.sequence_value = sequence_value;
	}
}
