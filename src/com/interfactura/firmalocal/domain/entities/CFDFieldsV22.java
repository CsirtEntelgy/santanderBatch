package com.interfactura.firmalocal.domain.entities;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * Clase que representa la tabla CFDFIELDSV22.
 * 
 * @author lortiz
 * 
 */
@Entity
@Table(name="CFDFIELDSV22")
public class CFDFieldsV22 extends BaseEntity implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@OneToOne(cascade = CascadeType.PERSIST)
	@JoinColumn(name = "FISCALENTITY_ID", unique = false, nullable = false)
	@NotNull
	private FiscalEntity fiscalEntity;
	
	@OneToOne(cascade = CascadeType.PERSIST)
	@JoinColumn(name = "REGIMENFISCAL_ID", unique = false, nullable = false)
	@NotNull
	private RegimenFiscal regimenFiscal;
	
	@Column(name="unidad_medida")
	private String unidadDeMedida;
	
	@Column(name="lugar_expedicion")
	private String lugarDeExpedicion;
	
	@Column(name="metodo_pago")
	private String metodoDePago;
	
	@Column(name="forma_pago")
	private String formaDePago;

	public FiscalEntity getFiscalEntity() {
		return fiscalEntity;
	}

	public void setFiscalEntity(FiscalEntity fiscalEntity) {
		this.fiscalEntity = fiscalEntity;
	}

	public RegimenFiscal getRegimenFiscal() {
		return regimenFiscal;
	}

	public void setRegimenFiscal(RegimenFiscal regimenFiscal) {
		this.regimenFiscal = regimenFiscal;
	}

	public String getUnidadDeMedida() {
		return unidadDeMedida;
	}

	public void setUnidadDeMedida(String unidadDeMedida) {
		this.unidadDeMedida = unidadDeMedida;
	}

	public String getLugarDeExpedicion() {
		return lugarDeExpedicion;
	}

	public void setLugarDeExpedicion(String lugarDeExpedicion) {
		this.lugarDeExpedicion = lugarDeExpedicion;
	}

	public String getMetodoDePago() {
		return metodoDePago;
	}

	public void setMetodoDePago(String metodoDePago) {
		this.metodoDePago = metodoDePago;
	}

	public String getFormaDePago() {
		return formaDePago;
	}

	public void setFormaDePago(String formaDePago) {
		this.formaDePago = formaDePago;
	}
	
}
