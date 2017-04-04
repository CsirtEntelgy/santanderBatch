package com.interfactura.firmalocal.domain.entities;

import java.io.Serializable;



import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;

import com.interfactura.firmalocal.domain.entities.BaseEntity;

/**
 * Clase que representa la tabla CFDFIELDSV22.
 * 
 * @author lortiz
 * 
 */
@Entity
@Table(name="CFD_MX_AUX_METOD_PAG_DRO")
public class InterfaceMetodoPago implements Serializable{
 
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue
	@Column(name="ID_PK")
	private long id;
	
	@OneToOne(cascade = CascadeType.PERSIST)
	@JoinColumn(name = "ID_ENT_FISC_FK", unique = false, nullable = false)
	@NotNull
	private FiscalEntity fiscalEntity;
	
	@Column(name="FCH_CREA")
	private Date fechaCreacion;
	
	@Column(name="FCH_MOD")
	private Date fechaModificacion;
	
	@Column(name="TXT_CREA_POR")
	private String creadoPor;
	
	@Column(name="TXT_MOD_POR")
	private String modificadoPor;
	
	@Column(name="COD_CLAV_MET_PAG")
	private String claveMetodoPago;
	
	@Column(name="TXT_NOM_INTFC")
	private String nombreInterface;
	
	@Column(name="DSC_DESC_MET_PAG")
	private String descripcion;
	
	@Column(name="FLG_ACT")
	private String activo;
	

	public FiscalEntity getFiscalEntity() {
		return fiscalEntity;
	}

	public void setFiscalEntity(FiscalEntity fiscalEntity) {
		this.fiscalEntity = fiscalEntity;
	}

	public Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(Date date) {
		this.fechaCreacion = date;
	}

	public Date getFechaModificacion() {
		return fechaModificacion;
	}

	public void setFechaModificacion(Date fechaModificacion) {
		this.fechaModificacion = fechaModificacion;
	}

	public String getCreadoPor() {
		return creadoPor;
	}

	public void setCreadoPor(String creadoPor) {
		this.creadoPor = creadoPor;
	}

	public String getModificadoPor() {
		return modificadoPor;
	}

	public void setModificadoPor(String modificadoPor) {
		this.modificadoPor = modificadoPor;
	}

	public String getClaveMetodoPago() {
		return claveMetodoPago;
	}

	public void setClaveMetodoPago(String claveMetodoPago) {
		this.claveMetodoPago = claveMetodoPago;
	}

	public String getNombreInterface() {
		return nombreInterface;
	}

	public void setNombreInterface(String nombreInterface) {
		this.nombreInterface = nombreInterface;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getActivo() {
		return activo;
	}

	public void setActivo(String activo) {
		this.activo = activo;
		
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	
	//Metodos para regresar fechas legibles para el usuario
	
	public String getFechaCreacionString() {
		if( fechaCreacion != null )
			return new SimpleDateFormat("dd/MM/yyyy").format(fechaCreacion);
		else
			return new String("");
	}
	
	public void setFechaCreacionString(String date){
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		try {
			this.fechaCreacion = format.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	public String getFechaModificacionString() {
		if( fechaModificacion != null )
			return new SimpleDateFormat("dd/MM/yyyy").format(fechaModificacion);
		else
			return new String("");
	}
	
	public void setFechaModificiacionString(String date){
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		try {
			this.fechaModificacion = format.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	public String getActivoString() {
		
		if(this.activo.equals("0")){
			return "INACTIVO";
		}else if(this.activo.equals("1")){
			return"ACTIVO";
		}else {
			return "";
		}
		
		
	}
	
	public void setActivoString(String date){

		if(this.activo.equals("0")){
			this.activo ="INACTIVO";
		}else if(this.activo.equals("1")){
			this.activo ="ACTIVO";
		}else {
			this.activo ="";
		}
		
	}
	
	
	
	
	
}
