package com.interfactura.firmalocal.domain.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "CFD_MX_MAE_AREAS_DRO")
public class AreaResponsable implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue
	@Column(name = "ID_AREA_PK")
	private long id;
	
	@Column(name = "TXT_NOM")
	private String nombre;
	
	@Column(name = "TXT_NOM_COR")
	private String nombreCorto;
	
	@Column(name = "TXT_DESCR")
	private String descripcion;
	
	@Column(name = "NUM_TIPO")
	private int tipo;
	
	@Column(name = "USR_AUTOR")
	private String autor;
	
	@Column(name = "FCH_CREAC")
	private Date fechaCreacion;
	
	@Column(name = "USR_MODIF_POR")
	private String autorCambio;
	
	@Column(name = "FCH_CAMB")
	private Date fechaCambio;
	
	@Column(name = "NUM_STAT")
	private int status;
	
	/*@OneToMany(cascade=CascadeType.PERSIST)
	@JoinColumn(name="ID_USR_ID_FK")
	private User usuario;*/

	@Column(name = "ID_USR_ID_FK")
	private long idUsuario;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getNombreCorto() {
		return nombreCorto;
	}

	public void setNombreCorto(String nombreCorto) {
		this.nombreCorto = nombreCorto;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public int getTipo() {
		return tipo;
	}

	public void setTipo(int tipo) {
		this.tipo = tipo;
	}

	public String getAutor() {
		return autor;
	}

	public void setAutor(String autor) {
		this.autor = autor;
	}

	public Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public String getAutorCambio() {
		return autorCambio;
	}

	public void setAutorCambio(String autorCambio) {
		this.autorCambio = autorCambio;
	}

	public Date getFechaCambio() {
		return fechaCambio;
	}

	public void setFechaCambio(Date fechaCambio) {
		this.fechaCambio = fechaCambio;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public long getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(long idUsuario) {
		this.idUsuario = idUsuario;
	}

}
