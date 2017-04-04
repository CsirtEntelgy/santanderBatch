package com.interfactura.firmalocal.domain.entities;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

@Entity
@Table(name="LOGOSFE")
public class Logo extends BaseEntity implements Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1892747001657315237L;
	
	@OneToOne(cascade = CascadeType.PERSIST)
	@JoinColumn(name = "FISCALENTITY_ID", unique = false, nullable = false)
	@NotNull
	private FiscalEntity fiscalEntity;
	@Lob
	private byte[] image;
	private Date startDate;
	private Date finalDate;
	private String logoName;
	@Transient
	private String logoNameImg;
	
	public byte[] getImage() {
		return image;
	}
	public void setImage(byte[] image) {
		this.image = image;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getFinalDate() {
		return finalDate;
	}
	public void setFinalDate(Date finalDate) {
		this.finalDate = finalDate;
	}
	public FiscalEntity getFiscalEntity() {
		return fiscalEntity;
	}
	public void setFiscalEntity(FiscalEntity fiscalEntity) {
		this.fiscalEntity = fiscalEntity;
	}
	public String getLogoName() {
		return logoName;
	}
	public void setLogoName(String logoName) {
		this.logoName = logoName;
	}
	
	public String getStartDateString() {
		if( startDate != null )
			return new SimpleDateFormat("dd/MM/yyyy").format(startDate);
		else
			return new String("");
	}
	
	public String getFinalDateString() {
		if( finalDate != null )
			return new SimpleDateFormat("dd/MM/yyyy").format(finalDate);
		else
			return new String("");
	}
	
	public void setStartDateString(String date){
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		try {
			this.startDate = format.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	public void setFinalDateString(String date){
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		try {
			this.finalDate = format.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	public String getLogoNameImg() {
		return logoNameImg;
	}
	public void setLogoNameImg(String logoNameImg) {
		this.logoNameImg = logoNameImg;
	}
}
