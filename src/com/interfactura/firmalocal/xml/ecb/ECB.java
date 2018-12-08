package com.interfactura.firmalocal.xml.ecb;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

public class ECB {

	private ByteArrayOutputStream xmlSinECB = new ByteArrayOutputStream();
	private List<MovimientoECB> lstMovimientosECB = new ArrayList<MovimientoECB>();
	private EstadoDeCuentaBancario estadoDeCuentaBancario = new EstadoDeCuentaBancario();
	private boolean fECBFiscal = false;	
	
	//Addenda
	private String addendaReference;
	
	private ByteArrayOutputStream originalString = new ByteArrayOutputStream();
	private String seal = "";
	 
	private String tagEMISON_RFC = "";
	private String tagNUM_CTE = "";
	private String tagNUM_CTA = "";
	private String tagEMISION_PERIODO = "";
	private String tagNUM_TARJETA = "";
	private String tagFECHA_CFD = "";
	private String tagSERIE_FISCAL_CFD = "";
	private String tagFOLIO_FISCAL_CFD = "";
	private String tagYEAR_APROBACION = "";
	private String tagNUM_CERTIFICADO = "";
	private String tagNUM_APROBACION = "";
	private String tagUNIDAD_MEDIDA = "";
	private String tagLUGAR_EXPEDICION = "";
	private String tagMETODO_PAGO = "";
	private String tagREGIMEN_FISCAL = "";
	private String tagFORMA_PAGO = "";
	private String tagTIPO_CAMBIO = "";
	private String tagTIPO_MONEDA = "";
	private String tagLONGITUD = "";
	
	private long tagFis_ID;
	private String tagRECEPCION_RFC = "";
	private String tagIVATOTAL_MN = "";
	private String tagSUBTOTAL_MN = "";
	private String tagTOTAL_MN = "";
	private String tagTIPO_FORMATO = "";
	private String tagCFD_TYPE = "";
	private String startLine = "";
	private String endLine = "";
	
	private String tagTOTAL_IMP_RET = "";
	private String tagTOTAL_IMP_TRA = "";
	
	private String tagNOMBRE_APP_REPECB = "";
	
	private List<Operacion> lstOperacionesECB = new ArrayList<Operacion>();
	private List<Cobranza> lstCobranzaECB = new ArrayList<Cobranza>();
	private boolean addendaNew = false;
	
	private Document domResultado;
		
	public ECB(){
		
	}
	public ByteArrayOutputStream getXmlSinECB() {
		return xmlSinECB;
	}
	public void setXmlSinECB(ByteArrayOutputStream xmlSinECB) {
		this.xmlSinECB = xmlSinECB;
	}
	public List<MovimientoECB> getLstMovimientosECB() {
		return lstMovimientosECB;
	}
	public void setLstMovimientosECB(List<MovimientoECB> lstMovimientosECB) {
		this.lstMovimientosECB = lstMovimientosECB;
	}
	public EstadoDeCuentaBancario getEstadoDeCuentaBancario() {
		return estadoDeCuentaBancario;
	}
	public void setEstadoDeCuentaBancario(EstadoDeCuentaBancario estadoDeCuentaBancario) {
		this.estadoDeCuentaBancario = estadoDeCuentaBancario;
	}
	public String getTagEMISON_RFC() {
		return tagEMISON_RFC;
	}
	public void setTagEMISON_RFC(String tagEMISON_RFC) {
		this.tagEMISON_RFC = tagEMISON_RFC;
	}
	public String getTagNUM_CTE() {
		return tagNUM_CTE;
	}
	public void setTagNUM_CTE(String tagNUM_CTE) {
		this.tagNUM_CTE = tagNUM_CTE;
	}
	public String getTagNUM_CTA() {
		return tagNUM_CTA;
	}
	public void setTagNUM_CTA(String tagNUM_CTA) {
		this.tagNUM_CTA = tagNUM_CTA;
	}
	public String getTagEMISION_PERIODO() {
		return tagEMISION_PERIODO;
	}
	public void setTagEMISION_PERIODO(String tagEMISION_PERIODO) {
		this.tagEMISION_PERIODO = tagEMISION_PERIODO;
	}
	public String getTagNUM_TARJETA() {
		return tagNUM_TARJETA;
	}
	public void setTagNUM_TARJETA(String tagNUM_TARJETA) {
		this.tagNUM_TARJETA = tagNUM_TARJETA;
	}
	public String getTagFECHA_CFD() {
		return tagFECHA_CFD;
	}
	public void setTagFECHA_CFD(String tagFECHA_CFD) {
		this.tagFECHA_CFD = tagFECHA_CFD;
	}
	public String getTagSERIE_FISCAL_CFD() {
		return tagSERIE_FISCAL_CFD;
	}
	public void setTagSERIE_FISCAL_CFD(String tagSERIE_FISCAL_CFD) {
		this.tagSERIE_FISCAL_CFD = tagSERIE_FISCAL_CFD;
	}
	public String getTagFOLIO_FISCAL_CFD() {
		return tagFOLIO_FISCAL_CFD;
	}
	public void setTagFOLIO_FISCAL_CFD(String tagFOLIO_FISCAL_CFD) {
		this.tagFOLIO_FISCAL_CFD = tagFOLIO_FISCAL_CFD;
	}
	public String getTagYEAR_APROBACION() {
		return tagYEAR_APROBACION;
	}
	public void setTagYEAR_APROBACION(String tagYEAR_APROBACION) {
		this.tagYEAR_APROBACION = tagYEAR_APROBACION;
	}
	public String getTagNUM_CERTIFICADO() {
		return tagNUM_CERTIFICADO;
	}
	public void setTagNUM_CERTIFICADO(String tagNUM_CERTIFICADO) {
		this.tagNUM_CERTIFICADO = tagNUM_CERTIFICADO;
	}
	public String getTagNUM_APROBACION() {
		return tagNUM_APROBACION;
	}
	public void setTagNUM_APROBACION(String tagNUM_APROBACION) {
		this.tagNUM_APROBACION = tagNUM_APROBACION;
	}
	public String getTagUNIDAD_MEDIDA() {
		return tagUNIDAD_MEDIDA;
	}
	public void setTagUNIDAD_MEDIDA(String tagUNIDAD_MEDIDA) {
		this.tagUNIDAD_MEDIDA = tagUNIDAD_MEDIDA;
	}
	public String getTagLUGAR_EXPEDICION() {
		return tagLUGAR_EXPEDICION;
	}
	public void setTagLUGAR_EXPEDICION(String tagLUGAR_EXPEDICION) {
		this.tagLUGAR_EXPEDICION = tagLUGAR_EXPEDICION;
	}
	public String getTagMETODO_PAGO() {
		return tagMETODO_PAGO;
	}
	public void setTagMETODO_PAGO(String tagMETODO_PAGO) {
		this.tagMETODO_PAGO = tagMETODO_PAGO;
	}
	public String getTagREGIMEN_FISCAL() {
		return tagREGIMEN_FISCAL;
	}
	public void setTagREGIMEN_FISCAL(String tagREGIMEN_FISCAL) {
		this.tagREGIMEN_FISCAL = tagREGIMEN_FISCAL;
	}
	public String getTagFORMA_PAGO() {
		return tagFORMA_PAGO;
	}
	public void setTagFORMA_PAGO(String tagFORMA_PAGO) {
		this.tagFORMA_PAGO = tagFORMA_PAGO;
	}
	public String getTagTIPO_CAMBIO() {
		return tagTIPO_CAMBIO;
	}
	public void setTagTIPO_CAMBIO(String tagTIPO_CAMBIO) {
		this.tagTIPO_CAMBIO = tagTIPO_CAMBIO;
	}
	public String getTagTIPO_MONEDA() {
		return tagTIPO_MONEDA;
	}
	public void setTagTIPO_MONEDA(String tagTIPO_MONEDA) {
		this.tagTIPO_MONEDA = tagTIPO_MONEDA;
	}
	public String getTagLONGITUD() {
		return tagLONGITUD;
	}
	public void setTagLONGITUD(String tagLONGITUD) {
		this.tagLONGITUD = tagLONGITUD;
	}
	public long getTagFis_ID() {
		return tagFis_ID;
	}
	public void setTagFis_ID(long tagFis_ID) {
		this.tagFis_ID = tagFis_ID;
	}
	public String getTagRECEPCION_RFC() {
		return tagRECEPCION_RFC;
	}
	public void setTagRECEPCION_RFC(String tagRECEPCION_RFC) {
		this.tagRECEPCION_RFC = tagRECEPCION_RFC;
	}
	public String getTagIVATOTAL_MN() {
		return tagIVATOTAL_MN;
	}
	public void setTagIVATOTAL_MN(String tagIVATOTAL_MN) {
		this.tagIVATOTAL_MN = tagIVATOTAL_MN;
	}
	public String getTagSUBTOTAL_MN() {
		return tagSUBTOTAL_MN;
	}
	public void setTagSUBTOTAL_MN(String tagSUBTOTAL_MN) {
		this.tagSUBTOTAL_MN = tagSUBTOTAL_MN;
	}
	public String getTagTOTAL_MN() {
		return tagTOTAL_MN;
	}
	public void setTagTOTAL_MN(String tagTOTAL_MN) {
		this.tagTOTAL_MN = tagTOTAL_MN;
	}
	public String getTagTIPO_FORMATO() {
		return tagTIPO_FORMATO;
	}
	public void setTagTIPO_FORMATO(String tagTIPO_FORMATO) {
		this.tagTIPO_FORMATO = tagTIPO_FORMATO;
	}
	public String getTagCFD_TYPE() {
		return tagCFD_TYPE;
	}
	public void setTagCFD_TYPE(String tagCFD_TYPE) {
		this.tagCFD_TYPE = tagCFD_TYPE;
	}
	public boolean isfECBFiscal() {
		return fECBFiscal;
	}
	public void setfECBFiscal(boolean fECBFiscal) {
		this.fECBFiscal = fECBFiscal;
	}
	public String getSeal() {
		return seal;
	}
	public void setSeal(String seal) {
		this.seal = seal;
	}
	public ByteArrayOutputStream getOriginalString() {
		return originalString;
	}
	public void setOriginalString(ByteArrayOutputStream originalString) {
		this.originalString = originalString;
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
	public String getTagTOTAL_IMP_RET() {
		return tagTOTAL_IMP_RET;
	}
	public void setTagTOTAL_IMP_RET(String tagTOTAL_IMP_RET) {
		this.tagTOTAL_IMP_RET = tagTOTAL_IMP_RET;
	}
	public String getTagTOTAL_IMP_TRA() {
		return tagTOTAL_IMP_TRA;
	}
	public void setTagTOTAL_IMP_TRA(String tagTOTAL_IMP_TRA) {
		this.tagTOTAL_IMP_TRA = tagTOTAL_IMP_TRA;
	}
	public String getTagNOMBRE_APP_REPECB() {
		return tagNOMBRE_APP_REPECB;
	}
	public void setTagNOMBRE_APP_REPECB(String tagNOMBRE_APP_REPECB) {
		this.tagNOMBRE_APP_REPECB = tagNOMBRE_APP_REPECB;
	}
	public Document getDomResultado() {
		return domResultado;
	}
	public void setDomResultado(Document domResultado) {
		this.domResultado = domResultado;
	}
	public String getAddendaReference() {
		return addendaReference;
	}
	public void setAddendaReference(String addendaReference) {
		this.addendaReference = addendaReference;
	}
	public List<Operacion> getLstOperacionesECB() {
		return lstOperacionesECB;
	}
	public void setLstOperacionesECB(List<Operacion> lstOperacionesECB) {
		this.lstOperacionesECB = lstOperacionesECB;
	}
	public List<Cobranza> getLstCobranzaECB() {
		return lstCobranzaECB;
	}
	public void setLstCobranzaECB(List<Cobranza> lstCobranzaECB) {
		this.lstCobranzaECB = lstCobranzaECB;
	}
	public boolean isAddendaNew() {
		return addendaNew;
	}
	public void setAddendaNew(boolean addendaNew) {
		this.addendaNew = addendaNew;
	}
	
	
	//
	
	
}
