package com.interfactura.firmalocal.domain;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.Set;

import com.interfactura.firmalocal.domain.entities.AddendumCustoms;
import com.interfactura.firmalocal.domain.entities.FolioRange;
import com.interfactura.firmalocal.domain.entities.SealCertificate;

public class CfdBean {
	
	protected ByteArrayOutputStream baosXml;
	protected String serieFiscalId; 
	protected long fiscalEntityId;
	protected SealCertificate sealCertificate;
	//protected FolioRange folioRange;
	protected Date creationDate;
	protected String customerNumber;
	protected String accountNumber;
	protected String period;
	protected String cardNumber;
	protected String subtotalMN;
	protected String subtotalReport;
	protected String totalMN;
	protected String totalReport;
	protected String ivaTotalMN;
	protected String ivaTotalReport;
	protected String lengthString ;
	protected String providerNumber;
	protected String depositAccount;
	protected String email;
	protected String currency;
	protected String purchaseOrder;
	protected String typeCurrency;
	protected String exchangeRate;
	protected String startLine;
	protected String endLine;
	protected String typeCFD;
	protected String receptorRFC;
	protected Set<AddendumCustoms> lstCustoms;
	protected String formatType;
	protected String broadcastRFC;
	protected String receivingInstitution;
	protected String beneficiary;
	protected String contract;
	protected String customerCode;
	protected String costCenter;
	protected String keySantander;
	protected String innerSheet;
	protected String dateCFD;
	protected String serieCFD;
	protected String folioCFD;
	protected String certificateNumber;
	protected String folioReference;
	protected String unidadMedida;
	protected String regimenFiscal;
	protected String metodoPago;
	protected String lugarExpedicion;
	protected String moneda;
	protected String tipoCambio;
	protected String formaPago;
	
	public CfdBean(ByteArrayOutputStream baosXml, String serieFiscalId,long fiscalEntityId, SealCertificate sealCertificate, Date date) {
		this.baosXml = baosXml;
		this.serieFiscalId = serieFiscalId;
		this.fiscalEntityId = fiscalEntityId;
		this.sealCertificate = sealCertificate;
		this.creationDate = date;
	}
	
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	
	public Date getCreationDate() {
		return creationDate;
	}
/*	
	public void setFolioRange(FolioRange folioRange) {
		this.folioRange = folioRange;
	}
	
	public FolioRange getFolioRange() {
		return folioRange;
	}
	*/ 
	public ByteArrayOutputStream getBaosXml() {
		return baosXml;
	}

	public void setBaosXml(ByteArrayOutputStream baosXml) {
		this.baosXml = baosXml;
	}

	public String getSerieFiscalId() {
		return serieFiscalId;
	}

	public void setSerieFiscalId(String serieFiscalId) {
		this.serieFiscalId = serieFiscalId;
	}

	public long getFiscalEntityId() {
		return fiscalEntityId;
	}

	public void setFiscalEntityId(long fiscalEntityId) {
		this.fiscalEntityId = fiscalEntityId;
	}
	
	public void setSealCertificate(SealCertificate sealCertificate) {
		this.sealCertificate = sealCertificate;
	}
	
	public SealCertificate getSealCertificate() {
		return sealCertificate;
	}

	public String getCustomerNumber() {
		return customerNumber;
	}

	public void setCustomerNumber(String customerNumber) {
		this.customerNumber = customerNumber;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getPeriod() {
		return period;
	}

	public void setPeriod(String period) {
		this.period = period;
	}

	public String getCardNumber() {
		return cardNumber;
	}

	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}

	public String getSubtotalMN() {
		return subtotalMN;
	}

	public void setSubtotalMN(String subtotalMN) {
		this.subtotalMN = subtotalMN;
	}

	public String getSubtotalReport() {
		return subtotalReport;
	}

	public void setSubtotalReport(String subtotalReport) {
		this.subtotalReport = subtotalReport;
	}

	public String getTotalMN() {
		return totalMN;
	}

	public void setTotalMN(String totalMN) {
		this.totalMN = totalMN;
	}

	public String getTotalReport() {
		return totalReport;
	}

	public void setTotalReport(String totalReport) {
		this.totalReport = totalReport;
	}

	public String getIvaTotalMN() {
		return ivaTotalMN;
	}

	public void setIvaTotalMN(String ivaTotalMN) {
		this.ivaTotalMN = ivaTotalMN;
	}

	public String getIvaTotalReport() {
		return ivaTotalReport;
	}

	public void setIvaTotalReport(String ivaTotalReport) {
		this.ivaTotalReport = ivaTotalReport;
	}

	public String getLengthString() {
		return lengthString;
	}

	public void setLengthString(String lengthString) {
		this.lengthString = lengthString;
	}

	public String getProviderNumber() {
		return providerNumber;
	}

	public void setProviderNumber(String providerNumber) {
		this.providerNumber = providerNumber;
	}

	public String getDepositAccount() {
		return depositAccount;
	}

	public void setDepositAccount(String depositAccount) {
		this.depositAccount = depositAccount;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getPurchaseOrder() {
		return purchaseOrder;
	}

	public void setPurchaseOrder(String purchaseOrder) {
		this.purchaseOrder = purchaseOrder;
	}

	public String getTypeCurrency() {
		return typeCurrency;
	}

	public void setTypeCurrency(String typeCurrency) {
		this.typeCurrency = typeCurrency;
	}

	public String getExchangeRate() {
		return exchangeRate;
	}

	public void setExchangeRate(String exchangeRate) {
		this.exchangeRate = exchangeRate;
	}

	public String getStartLine() {
		return startLine;
	}

	public void setStartLine(String startLine) {
		this.startLine = startLine;
	}

	public String getEndLine() {
		return endLine;
	}

	public void setEndLine(String endLine) {
		this.endLine = endLine;
	}

	public String getTypeCFD() {
		return typeCFD;
	}

	public void setTypeCFD(String typeCFD) {
		this.typeCFD = typeCFD;
	}

	public Set<AddendumCustoms> getLstCustoms() {
		return lstCustoms;
	}

	public void setLstCustoms(Set<AddendumCustoms> lstCustoms) {
		this.lstCustoms = lstCustoms;
	}

	public String getReceptorRFC() {
		return receptorRFC;
	}

	public void setReceptorRFC(String receptorRFC) {
		this.receptorRFC = receptorRFC;
	}

	public String getFormatType() {
		return formatType;
	}

	public void setFormatType(String formatType) {
		this.formatType = formatType;
	}

	public String getBroadcastRFC() {
		return broadcastRFC;
	}

	public void setBroadcastRFC(String broadcastRFC) {
		this.broadcastRFC = broadcastRFC;
	}

	public String getReceivingInstitution() {
		return receivingInstitution;
	}

	public void setReceivingInstitution(String receivingInstitution) {
		this.receivingInstitution = receivingInstitution;
	}

	public String getBeneficiary() {
		return beneficiary;
	}

	public void setBeneficiary(String beneficiary) {
		this.beneficiary = beneficiary;
	}

	public String getContract() {
		return contract;
	}

	public void setContract(String contract) {
		this.contract = contract;
	}

	public String getCustomerCode() {
		return customerCode;
	}

	public void setCustomerCode(String customerCode) {
		this.customerCode = customerCode;
	}

	public String getCostCenter() {
		return costCenter;
	}

	public void setCostCenter(String costCenter) {
		this.costCenter = costCenter;
	}

	public String getKeySantander() {
		return keySantander;
	}

	public void setKeySantander(String keySantander) {
		this.keySantander = keySantander;
	}

	public String getInnerSheet() {
		return innerSheet;
	}

	public void setInnerSheet(String innerSheet) {
		this.innerSheet = innerSheet;
	}

	public String getDateCFD() {
		return dateCFD;
	}

	public void setDateCFD(String dateCFD) {
		this.dateCFD = dateCFD;
	}

	public String getSerieCFD() {
		return serieCFD;
	}

	public void setSerieCFD(String serieCFD) {
		this.serieCFD = serieCFD;
	}

	public String getFolioCFD() {
		return folioCFD;
	}

	public void setFolioCFD(String folioCFD) {
		this.folioCFD = folioCFD;
	}

	public String getCertificateNumber() {
		return certificateNumber;
	}

	public void setCertificateNumber(String certificateNumber) {
		this.certificateNumber = certificateNumber;
	}

	public String getFolioReference() {
		return folioReference;
	}

	public void setFolioReference(String folioReference) {
		this.folioReference = folioReference;
	}

	public String getUnidadMedida() {
		return unidadMedida;
	}

	public void setUnidadMedida(String unidadMedida) {
		this.unidadMedida = unidadMedida;
	}

	public String getRegimenFiscal() {
		return regimenFiscal;
	}

	public void setRegimenFiscal(String regimenFiscal) {
		this.regimenFiscal = regimenFiscal;
	}

	public String getMetodoPago() {
		return metodoPago;
	}

	public void setMetodoPago(String metodoPago) {
		this.metodoPago = metodoPago;
	}

	public String getLugarExpedicion() {
		return lugarExpedicion;
	}

	public void setLugarExpedicion(String lugarExpedicion) {
		this.lugarExpedicion = lugarExpedicion;
	}

	public String getMoneda() {
		return moneda;
	}

	public void setMoneda(String moneda) {
		this.moneda = moneda;
	}

	public String getTipoCambio() {
		return tipoCambio;
	}

	public void setTipoCambio(String tipoCambio) {
		this.tipoCambio = tipoCambio;
	}

	public String getFormaPago() {
		return formaPago;
	}

	public void setFormaPago(String formaPago) {
		this.formaPago = formaPago;
	}
}
