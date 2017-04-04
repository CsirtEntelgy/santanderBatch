package com.interfactura.firmalocal.domain.entities;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Facturas {

	@Id
	@GeneratedValue
	private long ID;
	private String SOURCEFILENAME;
	private String FOLIO;
	private String FOLIOINTERNO;
	private String FOLIOSAT;
	private String TIPODEFORMATO;
	private int STATUS;
	private Date FECHADEEMISION;
	private Date FECHADECANCENLACION;
	private String RFCEMISOR;
	private String ENTIDAD;
	private String SERIE;
	private String TIPODECOMPROBANTE;
	private String MONEDA;
	private String TIPODECAMBIO;
	private String RFCCLIENTE;
	private String IDEXTRANJERO;
	private String NOMBREDELCLIENTE;
	private String METODODEPAGO;
	private String REGIMENFISCAL;
	private String LUGARDEEXPEDICION;
	private String FORMADEPAGO;
	private String NUMCTAPAGO;
	private String CALLE;
	private String NUMINTERIOR;
	private String NUMEXTERIOR;
	private String COLONIA;
	private String LOCALIDAD;
	private String REFERENCIA;
	private String MUNICIPIO;
	private String ESTADO;
	private String PAIS;
	private String CODIGOPOSTAL;
	private String CODIGODECLIENTE;
	private String NUMCONTRATO;
	private String PERIODO;
	private String DESCRIPCIONCONCEPTO;
	private String TASAIVA;
	private double SUBTOTAL;
	private double IVA;
	private double TOTAL;
	private int TIPODEADDENDA;
	private String EMAIL;
	private String CODIGOISO;
	private String ORDENCOMPRA;
	private String POSICIONCOMPRA;
	private String CUENTACONTABLE;
	private String CENTRODECOSTOS;
	private String NUMCONTRATOARRENDAMIENTO;
	private String FECHAVENCIMIENTO;
	private String NOMBREDEBENEFICIARIO;
	private String INSTITUCIONRECEPTORA;
	private String NUMCTA;
	private String NUMDEPROVEEDOR;
	private String XMLROUTE;
	private String NOMBREDEARCHIVO;
	private int EMISOR_ID;
	public long getID() {
		return ID;
	}
	public void setID(long iD) {
		ID = iD;
	}
	public String getSOURCEFILENAME() {
		return SOURCEFILENAME;
	}
	public void setSOURCEFILENAME(String sOURCEFILENAME) {
		SOURCEFILENAME = sOURCEFILENAME;
	}
	public String getFOLIO() {
		return FOLIO;
	}
	public void setFOLIO(String fOLIO) {
		FOLIO = fOLIO;
	}
	public String getFOLIOINTERNO() {
		return FOLIOINTERNO;
	}
	public void setFOLIOINTERNO(String fOLIOINTERNO) {
		FOLIOINTERNO = fOLIOINTERNO;
	}
	public String getFOLIOSAT() {
		return FOLIOSAT;
	}
	public void setFOLIOSAT(String fOLIOSAT) {
		FOLIOSAT = fOLIOSAT;
	}
	public String getTIPODEFORMATO() {
		return TIPODEFORMATO;
	}
	public void setTIPODEFORMATO(String tIPODEFORMATO) {
		TIPODEFORMATO = tIPODEFORMATO;
	}
	public int getSTATUS() {
		return STATUS;
	}
	public void setSTATUS(int sTATUS) {
		STATUS = sTATUS;
	}
	public Date getFECHADEEMISION() {
		return FECHADEEMISION;
	}
	public void setFECHADEEMISION(Date fECHADEEMISION) {
		FECHADEEMISION = fECHADEEMISION;
	}
	public Date getFECHADECANCENLACION() {
		return FECHADECANCENLACION;
	}
	public void setFECHADECANCENLACION(Date fECHADECANCENLACION) {
		FECHADECANCENLACION = fECHADECANCENLACION;
	}
	public String getRFCEMISOR() {
		return RFCEMISOR;
	}
	public void setRFCEMISOR(String rFCEMISOR) {
		RFCEMISOR = rFCEMISOR;
	}
	public String getENTIDAD() {
		return ENTIDAD;
	}
	public void setENTIDAD(String eNTIDAD) {
		ENTIDAD = eNTIDAD;
	}
	public String getSERIE() {
		return SERIE;
	}
	public void setSERIE(String sERIE) {
		SERIE = sERIE;
	}
	public String getTIPODECOMPROBANTE() {
		return TIPODECOMPROBANTE;
	}
	public void setTIPODECOMPROBANTE(String tIPODECOMPROBANTE) {
		TIPODECOMPROBANTE = tIPODECOMPROBANTE;
	}
	public String getMONEDA() {
		return MONEDA;
	}
	public void setMONEDA(String mONEDA) {
		MONEDA = mONEDA;
	}
	public String getTIPODECAMBIO() {
		return TIPODECAMBIO;
	}
	public void setTIPODECAMBIO(String tIPODECAMBIO) {
		TIPODECAMBIO = tIPODECAMBIO;
	}
	public String getRFCCLIENTE() {
		return RFCCLIENTE;
	}
	public void setRFCCLIENTE(String rFCCLIENTE) {
		RFCCLIENTE = rFCCLIENTE;
	}
	public String getIDEXTRANJERO() {
		return IDEXTRANJERO;
	}
	public void setIDEXTRANJERO(String iDEXTRANJERO) {
		IDEXTRANJERO = iDEXTRANJERO;
	}
	public String getNOMBREDELCLIENTE() {
		return NOMBREDELCLIENTE;
	}
	public void setNOMBREDELCLIENTE(String nOMBREDELCLIENTE) {
		NOMBREDELCLIENTE = nOMBREDELCLIENTE;
	}
	public String getMETODODEPAGO() {
		return METODODEPAGO;
	}
	public void setMETODODEPAGO(String mETODODEPAGO) {
		METODODEPAGO = mETODODEPAGO;
	}
	public String getREGIMENFISCAL() {
		return REGIMENFISCAL;
	}
	public void setREGIMENFISCAL(String rEGIMENFISCAL) {
		REGIMENFISCAL = rEGIMENFISCAL;
	}
	public String getLUGARDEEXPEDICION() {
		return LUGARDEEXPEDICION;
	}
	public void setLUGARDEEXPEDICION(String lUGARDEEXPEDICION) {
		LUGARDEEXPEDICION = lUGARDEEXPEDICION;
	}
	public String getFORMADEPAGO() {
		return FORMADEPAGO;
	}
	public void setFORMADEPAGO(String fORMADEPAGO) {
		FORMADEPAGO = fORMADEPAGO;
	}
	public String getNUMCTAPAGO() {
		return NUMCTAPAGO;
	}
	public void setNUMCTAPAGO(String nUMCTAPAGO) {
		NUMCTAPAGO = nUMCTAPAGO;
	}
	public String getCALLE() {
		return CALLE;
	}
	public void setCALLE(String cALLE) {
		CALLE = cALLE;
	}
	public String getNUMINTERIOR() {
		return NUMINTERIOR;
	}
	public void setNUMINTERIOR(String nUMINTERIOR) {
		NUMINTERIOR = nUMINTERIOR;
	}
	public String getNUMEXTERIOR() {
		return NUMEXTERIOR;
	}
	public void setNUMEXTERIOR(String nUMEXTERIOR) {
		NUMEXTERIOR = nUMEXTERIOR;
	}
	public String getCOLONIA() {
		return COLONIA;
	}
	public void setCOLONIA(String cOLONIA) {
		COLONIA = cOLONIA;
	}
	public String getLOCALIDAD() {
		return LOCALIDAD;
	}
	public void setLOCALIDAD(String lOCALIDAD) {
		LOCALIDAD = lOCALIDAD;
	}
	public String getREFERENCIA() {
		return REFERENCIA;
	}
	public void setREFERENCIA(String rEFERENCIA) {
		REFERENCIA = rEFERENCIA;
	}
	public String getMUNICIPIO() {
		return MUNICIPIO;
	}
	public void setMUNICIPIO(String mUNICIPIO) {
		MUNICIPIO = mUNICIPIO;
	}
	public String getESTADO() {
		return ESTADO;
	}
	public void setESTADO(String eSTADO) {
		ESTADO = eSTADO;
	}
	public String getPAIS() {
		return PAIS;
	}
	public void setPAIS(String pAIS) {
		PAIS = pAIS;
	}
	public String getCODIGOPOSTAL() {
		return CODIGOPOSTAL;
	}
	public void setCODIGOPOSTAL(String cODIGOPOSTAL) {
		CODIGOPOSTAL = cODIGOPOSTAL;
	}
	public String getCODIGODECLIENTE() {
		return CODIGODECLIENTE;
	}
	public void setCODIGODECLIENTE(String cODIGODECLIENTE) {
		CODIGODECLIENTE = cODIGODECLIENTE;
	}
	public String getNUMCONTRATO() {
		return NUMCONTRATO;
	}
	public void setNUMCONTRATO(String nUMCONTRATO) {
		NUMCONTRATO = nUMCONTRATO;
	}
	public String getPERIODO() {
		return PERIODO;
	}
	public void setPERIODO(String pERIODO) {
		PERIODO = pERIODO;
	}
	public String getDESCRIPCIONCONCEPTO() {
		return DESCRIPCIONCONCEPTO;
	}
	public void setDESCRIPCIONCONCEPTO(String dESCRIPCIONCONCEPTO) {
		DESCRIPCIONCONCEPTO = dESCRIPCIONCONCEPTO;
	}
	public String getTASAIVA() {
		return TASAIVA;
	}
	public void setTASAIVA(String tASAIVA) {
		TASAIVA = tASAIVA;
	}
	public double getSUBTOTAL() {
		return SUBTOTAL;
	}
	public void setSUBTOTAL(double sUBTOTAL) {
		SUBTOTAL = sUBTOTAL;
	}
	public double getIVA() {
		return IVA;
	}
	public void setIVA(double iVA) {
		IVA = iVA;
	}
	public double getTOTAL() {
		return TOTAL;
	}
	public void setTOTAL(double tOTAL) {
		TOTAL = tOTAL;
	}
	public int getTIPODEADDENDA() {
		return TIPODEADDENDA;
	}
	public void setTIPODEADDENDA(int tIPODEADDENDA) {
		TIPODEADDENDA = tIPODEADDENDA;
	}
	public String getEMAIL() {
		return EMAIL;
	}
	public void setEMAIL(String eMAIL) {
		EMAIL = eMAIL;
	}
	public String getCODIGOISO() {
		return CODIGOISO;
	}
	public void setCODIGOISO(String cODIGOISO) {
		CODIGOISO = cODIGOISO;
	}
	public String getORDENCOMPRA() {
		return ORDENCOMPRA;
	}
	public void setORDENCOMPRA(String oRDENCOMPRA) {
		ORDENCOMPRA = oRDENCOMPRA;
	}
	public String getPOSICIONCOMPRA() {
		return POSICIONCOMPRA;
	}
	public void setPOSICIONCOMPRA(String pOSICIONCOMPRA) {
		POSICIONCOMPRA = pOSICIONCOMPRA;
	}
	public String getCUENTACONTABLE() {
		return CUENTACONTABLE;
	}
	public void setCUENTACONTABLE(String cUENTACONTABLE) {
		CUENTACONTABLE = cUENTACONTABLE;
	}
	public String getCENTRODECOSTOS() {
		return CENTRODECOSTOS;
	}
	public void setCENTRODECOSTOS(String cENTRODECOSTOS) {
		CENTRODECOSTOS = cENTRODECOSTOS;
	}
	public String getNUMCONTRATOARRENDAMIENTO() {
		return NUMCONTRATOARRENDAMIENTO;
	}
	public void setNUMCONTRATOARRENDAMIENTO(String nUMCONTRATOARRENDAMIENTO) {
		NUMCONTRATOARRENDAMIENTO = nUMCONTRATOARRENDAMIENTO;
	}
	public String getFECHAVENCIMIENTO() {
		return FECHAVENCIMIENTO;
	}
	public void setFECHAVENCIMIENTO(String fECHAVENCIMIENTO) {
		FECHAVENCIMIENTO = fECHAVENCIMIENTO;
	}
	public String getNOMBREDEBENEFICIARIO() {
		return NOMBREDEBENEFICIARIO;
	}
	public void setNOMBREDEBENEFICIARIO(String nOMBREDEBENEFICIARIO) {
		NOMBREDEBENEFICIARIO = nOMBREDEBENEFICIARIO;
	}
	public String getINSTITUCIONRECEPTORA() {
		return INSTITUCIONRECEPTORA;
	}
	public void setINSTITUCIONRECEPTORA(String iNSTITUCIONRECEPTORA) {
		INSTITUCIONRECEPTORA = iNSTITUCIONRECEPTORA;
	}
	public String getNUMCTA() {
		return NUMCTA;
	}
	public void setNUMCTA(String nUMCTA) {
		NUMCTA = nUMCTA;
	}
	public String getNUMDEPROVEEDOR() {
		return NUMDEPROVEEDOR;
	}
	public void setNUMDEPROVEEDOR(String nUMDEPROVEEDOR) {
		NUMDEPROVEEDOR = nUMDEPROVEEDOR;
	}
	public String getXMLROUTE() {
		return XMLROUTE;
	}
	public void setXMLROUTE(String xMLROUTE) {
		XMLROUTE = xMLROUTE;
	}
	public String getNOMBREDEARCHIVO() {
		return NOMBREDEARCHIVO;
	}
	public void setNOMBREDEARCHIVO(String nOMBREDEARCHIVO) {
		NOMBREDEARCHIVO = nOMBREDEARCHIVO;
	}
	public int getEMISOR_ID() {
		return EMISOR_ID;
	}
	public void setEMISOR_ID(int eMISOR_ID) {
		EMISOR_ID = eMISOR_ID;
	}

	public String getStrFECHADEEMISION(){
		if( FECHADEEMISION != null )
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(FECHADEEMISION);
		else
			return new String("");
	}
	
	public String getStrFECHADECANCENLACION(){
		if( FECHADECANCENLACION != null )
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(FECHADECANCENLACION);
		else
			return new String("");
	}
	
}
