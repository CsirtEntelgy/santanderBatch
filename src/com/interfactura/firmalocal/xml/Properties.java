package com.interfactura.firmalocal.xml;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Properties 
{
	@Value("${xml.name.factura}")
	private String basePathFactura;
	@Value("${xml.name.cadenaoriginal33}")
	private String pathFileSello;	
	@Value("${xml.name.factura}")
	private String nameFileXML;
	private String labelSELLO = "SELLOTEMPORAL";
	private String labelTipoCambioTemporal = "---TIPOCAMBIOTEMPORAL---";
	private String labelLugarExpedicion = "---LUGAREXPEDICIONTEMPORAL---";
	private String labelMetodoPago = "---METODOPAGOTEMPORAL---";
	private String labelFormaPago = "---NUMCTAPAGO---";
	
	private String uri =  "";

	private String LblNO_CERTIFICADO = "NCERTIFICADOTEMPORAL";
	private String LblCERTIFICADO = "CERTIFICADOTEMPORAL";
	private String LblFOLIOCFD="FOLIOTMP";
	private String LblNUMEROAPROBACIONCFD="NUMEROATMP";
	private String LblAPROBACIONCFD="APROBACIONTMP";
	private String nameSpace = "http://www.sat.gob.mx/cfd/3";
	
	
	private String nameSpaceAddenda = "http://www.santander.com.mx/schemas/xsd/AddendaSantanderV1";
	
	private String nameSpaceDonatarias = "http://www.sat.gob.mx/donat";
	
	/*
	@Value("${xml.xsdCFDI32}")
	private String pathFileValidationCFDI32;
	@Value("${xml.xsdTFD2}")
    private String pathTimbres2;
	@Value("${xml.xsdDonataria}")
    private String pathDonataria;
    @Value("${xml.name.cadenaoriginal22}")
    private String pathFileSello22;
    @Value("${xml.name.cadenaoriginal20}")
    private String pathFileSello20;
    @Value("${xml.name.cadenaoriginalCFDI}")
    private String pathFileSelloCFDI;
    @Value("${xml.name.cadenaoriginalCFDI22}")
    private String pathFileSelloCFDI32;
    @Value("${xml.xsdCFD22}")
    private String pathFileValidation22;
    @Value("${xml.xsdCFDI}")
    private String pathFileValidationCFDI;
    @Value("${xml.xsdTFD}")
    private String pathTimbres;
    */
	
	private int longitud = 166;
	@Value("${pdf.image}")
	private String pathLogoPDF;
	@Value("${xml.xsdCFD33}")
	private String pathFileValidation;
	@Value("${xml.xsdECB}")
	private String pathFileValidationECB;
	@Value("${xml.xsdADD}")
	private String pathFileValidaationADD;
	@Value("${xml.generados}")
	private String pathDirGenr;
	@Value("${xml.notasDeCreditoGeneradas}")
	private String pathDirGenrNotasDeCredito;
	@Value("${xml.procesar.tipo}")
	private String pathProTipo;
	@Value("${xml.procesar}")
	private String pathDirPro;
	@Value("${xml.procesarECB}")
	private String pathDirProECB;
	@Value("${xml.procesarCFD}")
	private String pathDirProCFD;
	@Value("${xml.incidencias}")
	private String pathDirIncd;
	@Value("${xml.procesados.salida}")
	private String pathSalida;
	@Value("${xml.procesados.incidencia}")
	private String pathIncidencia;
	@Value("${xml.procesados}")
	private String pathDirProcesados;
	@Value("${xml.backup}")
	private String pathDirBackup;
	@Value("${xml.certificados}")
	private String certificado;
	@Value("${xml.password}")
	private String pwd;
	@Value("${xml.key}")
	private String keyCertificado;
	@Value("${xml.masivo}")
	private String pathFlag;
	@Value("${xml.currency}")
	private String currency;
	@Value("${sizeECB}")
	private long sizeECB;
	@Value("${process.id}")
	private long processNumber;
	@Value("${process.configuration.path}")
	private String configurationPath;
	@Value("${process.configuration}")
	private String configuration;
	@Value("${xml.tipocambio}")
	private String tipoCambio;
	@Value("${certificado.santander}")
	private String certificadoSantander;
	@Value("${certificado.interfactura}")
	private String certificadoInterfactura;
	@Value("${certificado.pass}")
	private String certificadoPass;
	@Value("${certificado.intentosconexion}")
	private String intentosconexion;
	@Value("${interfaces.cfdfields}")
	private String interfaces;
	
	/*
	//Rutas para Reportes de facturas
	@Value("${reportes.entrada}")
	private String pathReportesEntrada;
	@Value("${reportes.proceso}")
	private String pathReportesProceso;
	@Value("${reportes.salida}")
	private String pathReportesSalida;
	
	//Rutas para Facturacion masiva
	@Value("${facturacion.entrada}")
	private String pathFacturacionEntrada;
	@Value("${facturacion.proceso}")
	private String pathFacturacionProceso;
	@Value("${facturacion.salida}")
	private String pathFacturacionSalida;
	@Value("${facturacion.ondemand}")
	private String pathFacturacionOndemand;
	
	//Rutas para Reportes de facturas (Divisas)
	@Value("${reportesDivisas.entrada}")
	private String pathReportesDivisasEntrada;
	@Value("${reportesDivisas.proceso}")
	private String pathReportesDivisasProceso;
	@Value("${reportesDivisas.salida}")
	private String pathReportesDivisasSalida;
	
	//Rutas para Facturacion masiva (Divisas)
	@Value("${facturacionDivisas.entrada}")
	private String pathFacturacionDivisasEntrada;
	@Value("${facturacionDivisas.proceso}")
	private String pathFacturacionDivisasProceso;
	@Value("${facturacionDivisas.salida}")
	private String pathFacturacionDivisasSalida;
	@Value("${facturacionDivisas.ondemand}")
	private String pathFacturacionDivisasOndemand;
	
	*/
	//Datos de conexion a ondemand
	/*@Value("${infoOndemand.server}")
	private String ondemandServer;
	@Value("${infoOndemand.user}")
	private String ondemandUser;
	@Value("${infoOndemand.pass}")
	private String ondemandPass;
	@Value("${infoOndemand.folderEmision}")
	private String ondemandFolderEmision;
	@Value("${infoOndemand.folderRecepcion}")
	private String ondemandFolderRecepcion;
	@Value("${infoOndemand.folderEstadoCuenta}")
	private String ondemandFolderEstadoCuenta;
	*/
	//Esquema divisas
	/*@Value("${xml.xsdDivisas}")
    private String pathDivisas;
	*/
	private String nameSpaceDivisas = "http://www.sat.gob.mx/divisas";
		
	@Value("${certificado.pass}")
    private String passCertificado;

	
	//Variables para acceso de WS Cancelacion
	@Value("${url.webservice.cancelacion}")
    private String urlWebServiceCancelacion;
	
	//Variable para acceso a WS de Cifras Control
	@Value("${url.webservice.cifrasControl}")
    private String urlWebServiceCifrasControl;
	
	//Variable de ubicacion a archivo XLS catalogos AMDA
	@Value("${url.archivo.catalogs}")
	private String urlArchivoCatalogs;
	
	public String getInterfaces() {
		return interfaces;
	}

	public void setInterfaces(String interfaces) {
		this.interfaces = interfaces;
	}

	public String getCertificado() {
		return certificado;
	}

	public void setCertificado(String certificado) {
		this.certificado = certificado;
	}

	public String getPathDirProcesados() {
		return pathDirProcesados;
	}

	public void setPathDirProcesados(String pathDirProcesados) {
		this.pathDirProcesados = pathDirProcesados;
	}

	public String getPathSalida() {
		return pathSalida;
	}

	public void setPathSalida(String pathSalida) {
		this.pathSalida = pathSalida;
	}

	public String getPathIncidencia() {
		return pathIncidencia;
	}

	public void setPathIncidencia(String pathIncidencia) {
		this.pathIncidencia = pathIncidencia;
	}

	public int getLongitud() {
		return longitud;
	}

	public void setLongitud(int longitud) {
		this.longitud = longitud;
	}

	public String getPathDirIncd() {
		return pathDirIncd;
	}

	public void setPathDirIncd(String pathDirIncd) {
		this.pathDirIncd = pathDirIncd;
	}

	public String getLblCERTIFICADO() {
		return LblCERTIFICADO;
	}

	public void setLblCERTIFICADO(String lblCERTIFICADO) {
		LblCERTIFICADO = lblCERTIFICADO;
	}

	public String getPathDirPro() {
		return pathDirPro;
	}

	public void setPathDirPro(String pathDirPro) {
		this.pathDirPro = pathDirPro;
	}

	public String getLblNO_CERTIFICADO() {
		return LblNO_CERTIFICADO;
	}

	public void setLblNO_CERTIFICADO(String lblNO_CERTIFICADO) {
		LblNO_CERTIFICADO = lblNO_CERTIFICADO;
	}

	public String getPathDirGenr() {
		return pathDirGenr;
	}

	public void setPathDirGenr(String pathDirGenr) {
		this.pathDirGenr = pathDirGenr;
	}

	public String getNameFileXML() {
		return nameFileXML;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getNameSpace() {
		return nameSpace;
	}

	public void setNameSpace(String nameSpace) {
		this.nameSpace = nameSpace;
	}

	public String getBasePathFactura() {
		return basePathFactura;
	}

	public void setBasePathFactura(String basePathFactura) {
		this.basePathFactura = basePathFactura;
	}

	public String getNameFileXML(String fecha) {
		return nameFileXML+fecha+".xml";
	}

	public void setNameFileXML(String nameFileXML) {
		this.nameFileXML = nameFileXML;
	}

	public String getPathFileSello() {
		return pathFileSello;
	}

	public void setPathFileSello(String pathFileSello) {
		this.pathFileSello = pathFileSello;
	}

	public String getLabelSELLO() {
		return labelSELLO;
	}

	public void setLabelSELLO(String labelSELLO) {
		this.labelSELLO = labelSELLO;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public String getKeyCertificado() {
		return keyCertificado;
	}

	public void setKeyCertificado(String keyCertificado) {
		this.keyCertificado = keyCertificado;
	}

	public String getPathFileValidation() {
		return pathFileValidation;
	}

	public void setPathFileValidation(String pathFileValidation) {
		this.pathFileValidation = pathFileValidation;
	}

	public String getPathFileValidationECB() {
		return pathFileValidationECB;
	}

	public void setPathFileValidationECB(String pathFileValidationECB) {
		this.pathFileValidationECB = pathFileValidationECB;
	}

	public String getPathFileValidaationADD() {
		return pathFileValidaationADD;
	}

	public void setPathFileValidaationADD(String pathFileValidaationADD) {
		this.pathFileValidaationADD = pathFileValidaationADD;
	}

	public String getPathLogoPDF() {
		return pathLogoPDF;
	}

	public void setPathLogoPDF(String pathLogoPDF) {
		this.pathLogoPDF = pathLogoPDF;
	}

	public String getPathDirProECB() {
		return pathDirProECB;
	}

	public void setPathDirProECB(String pathDirProECB) {
		this.pathDirProECB = pathDirProECB;
	}

	public String getPathDirProCFD() {
		return pathDirProCFD;
	}

	public void setPathDirProCFD(String pathDirProCFD) {
		this.pathDirProCFD = pathDirProCFD;
	}

	public String getPathProTipo() {
		return pathProTipo;
	}

	public void setPathProTipo(String pathProTipo) {
		this.pathProTipo = pathProTipo;
	}

	public String getLblFOLIOCFD() {
		return LblFOLIOCFD;
	}

	public void setLblFOLIOCFD(String lblFOLIOCFD) {
		LblFOLIOCFD = lblFOLIOCFD;
	}

	public String getLblNUMEROAPROBACIONCFD() {
		return LblNUMEROAPROBACIONCFD;
	}

	public void setLblNUMEROAPROBACIONCFD(String lblNUMEROAPROBACIONCFD) {
		LblNUMEROAPROBACIONCFD = lblNUMEROAPROBACIONCFD;
	}

	public String getLblAPROBACIONCFD() {
		return LblAPROBACIONCFD;
	}

	public void setLblAPROBACIONCFD(String lblAPROBACIONCFD) {
		LblAPROBACIONCFD = lblAPROBACIONCFD;
	}

	public String getPathFlag() {
		return pathFlag;
	}

	public void setPathFlag(String pathFlag) {
		this.pathFlag = pathFlag;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public long getSizeECB() {
		return sizeECB;
	}

	public void setSizeECB(long sizeECB) {
		this.sizeECB = sizeECB;
	}

	public long getProcessNumber() {
		return processNumber;
	}

	public void setProcessNumber(long processNumber) {
		this.processNumber = processNumber;
	}

	public String getConfigurationPath() {
		return configurationPath;
	}

	public void setConfigurationPath(String configurationPath) {
		this.configurationPath = configurationPath;
	}

	public String getPathDirBackup() {
		return pathDirBackup;
	}

	public void setPathDirBackup(String pathDirBackup) {
		this.pathDirBackup = pathDirBackup;
	}

	public String getConfiguration() {
		return configuration;
	}

	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}
	
	public String getLabelTipoCambioTemporal() {
		return labelTipoCambioTemporal;
	}

	public void setLabelTipoCambioTemporal(String labelTipoCambioTemporal) {
		this.labelTipoCambioTemporal = labelTipoCambioTemporal;
	}

	public String getLabelLugarExpedicion() {
		return labelLugarExpedicion;
	}

	public void setLabelLugarExpedicion(String labelLugarExpedicion) {
		this.labelLugarExpedicion = labelLugarExpedicion;
	}

	public String getLabelMetodoPago() {
		return labelMetodoPago;
	}

	public void setLabelMetodoPago(String labelMetodoPago) {
		this.labelMetodoPago = labelMetodoPago;
	}

	public void setTipoCambio(String tipoCambio) {
		this.tipoCambio = tipoCambio;
	}

	public String getTipoCambio() {
		return tipoCambio;
	}

	public String getlabelFormaPago() {
		return labelFormaPago;
	}

	public void setlabelFormaPago(String labelFormaPago) {
		this.labelFormaPago = labelFormaPago;
	}

	public String getCertificadoSantander() {
		return certificadoSantander;
	}

	public void setCertificadoSantander(String certificadoSantander) {
		this.certificadoSantander = certificadoSantander;
	}
	
	public String getCertificadoInterfactura() {
		return certificadoInterfactura;
	}

	public void setCertificadoInterfactura(String certificadoInterfactura) {
		this.certificadoInterfactura = certificadoInterfactura;
	}

	public String getCertificadoPass() {
		return certificadoPass;
	}

	public void setCertificadoPass(String certificadoPass) {
		this.certificadoPass = certificadoPass;
	}

	public String getIntentosconexion() {
		return intentosconexion;
	}

	public void setIntentosconexion(String intentosconexion) {
		this.intentosconexion = intentosconexion;
	}
	
	public String getPathDirGenrNotasDeCredito() {
		return pathDirGenrNotasDeCredito;
	}

	public void setPathDirGenrNotasDeCredito(
			String pathDirGenrNotasDeCredito) {
		this.pathDirGenrNotasDeCredito = pathDirGenrNotasDeCredito;
	}

	public String getNameSpaceAddenda() {
		return nameSpaceAddenda;
	}

	public void setNameSpaceAddenda(String nameSpaceAddenda) {
		this.nameSpaceAddenda = nameSpaceAddenda;
	}

	/*
	public String getPathFileValidationCFDI32() {
		return pathFileValidationCFDI32;
	}

	public void setPathFileValidationCFDI32(String pathFileValidationCFDI32) {
		this.pathFileValidationCFDI32 = pathFileValidationCFDI32;
	}

	public String getPathTimbres2() {
		return pathTimbres2;
	}

	public void setPathTimbres2(String pathTimbres2) {
		this.pathTimbres2 = pathTimbres2;
	}

	public String getPathDonataria() {
		return pathDonataria;
	}

	public void setPathDonataria(String pathDonataria) {
		this.pathDonataria = pathDonataria;
	}

	public String getPathFileSello22() {
		return pathFileSello22;
	}

	public void setPathFileSello22(String pathFileSello22) {
		this.pathFileSello22 = pathFileSello22;
	}

	public String getPathFileSello20() {
		return pathFileSello20;
	}

	public void setPathFileSello20(String pathFileSello20) {
		this.pathFileSello20 = pathFileSello20;
	}

	public String getPathFileSelloCFDI() {
		return pathFileSelloCFDI;
	}

	public void setPathFileSelloCFDI(String pathFileSelloCFDI) {
		this.pathFileSelloCFDI = pathFileSelloCFDI;
	}

	public String getPathFileSelloCFDI32() {
		return pathFileSelloCFDI32;
	}

	public void setPathFileSelloCFDI32(String pathFileSelloCFDI32) {
		this.pathFileSelloCFDI32 = pathFileSelloCFDI32;
	}

	public String getPathFileValidation22() {
		return pathFileValidation22;
	}

	public void setPathFileValidation22(String pathFileValidation22) {
		this.pathFileValidation22 = pathFileValidation22;
	}

	public String getPathFileValidationCFDI() {
		return pathFileValidationCFDI;
	}

	public void setPathFileValidationCFDI(String pathFileValidationCFDI) {
		this.pathFileValidationCFDI = pathFileValidationCFDI;
	}

	public String getPathTimbres() {
		return pathTimbres;
	}

	public void setPathTimbres(String pathTimbres) {
		this.pathTimbres = pathTimbres;
	}
*/
	
	public String getNameSpaceDonatarias() {
		return nameSpaceDonatarias;
	}

	public void setNameSpaceDonatarias(String nameSpaceDonatarias) {
		this.nameSpaceDonatarias = nameSpaceDonatarias;
	}

	/*
	public String getPathReportesEntrada() {
		return pathReportesEntrada;
	}

	public void setPathReportesEntrada(String pathReportesEntrada) {
		this.pathReportesEntrada = pathReportesEntrada;
	}

	public String getPathReportesProceso() {
		return pathReportesProceso;
	}

	public void setPathReportesProceso(String pathReportesProceso) {
		this.pathReportesProceso = pathReportesProceso;
	}

	public String getPathReportesSalida() {
		return pathReportesSalida;
	}

	public void setPathReportesSalida(String pathReportesSalida) {
		this.pathReportesSalida = pathReportesSalida;
	}

	public String getPathFacturacionEntrada() {
		return pathFacturacionEntrada;
	}

	public void setPathFacturacionEntrada(String pathFacturacionEntrada) {
		this.pathFacturacionEntrada = pathFacturacionEntrada;
	}

	public String getPathFacturacionProceso() {
		return pathFacturacionProceso;
	}

	public void setPathFacturacionProceso(String pathFacturacionProceso) {
		this.pathFacturacionProceso = pathFacturacionProceso;
	}

	public String getPathFacturacionSalida() {
		return pathFacturacionSalida;
	}

	public void setPathFacturacionSalida(String pathFacturacionSalida) {
		this.pathFacturacionSalida = pathFacturacionSalida;
	}

	public String getPathFacturacionOndemand() {
		return pathFacturacionOndemand;
	}

	public void setPathFacturacionOndemand(String pathFacturacionOndemand) {
		this.pathFacturacionOndemand = pathFacturacionOndemand;
	}
	*/
/*
	public String getOndemandServer() {
		return ondemandServer;
	}

	public void setOndemandServer(String ondemandServer) {
		this.ondemandServer = ondemandServer;
	}

	public String getOndemandUser() {
		return ondemandUser;
	}

	public void setOndemandUser(String ondemandUser) {
		this.ondemandUser = ondemandUser;
	}

	public String getOndemandPass() {
		return ondemandPass;
	}

	public void setOndemandPass(String ondemandPass) {
		this.ondemandPass = ondemandPass;
	}

	public String getOndemandFolderEmision() {
		return ondemandFolderEmision;
	}

	public void setOndemandFolderEmision(String ondemandFolderEmision) {
		this.ondemandFolderEmision = ondemandFolderEmision;
	}

	public String getOndemandFolderRecepcion() {
		return ondemandFolderRecepcion;
	}

	public void setOndemandFolderRecepcion(String ondemandFolderRecepcion) {
		this.ondemandFolderRecepcion = ondemandFolderRecepcion;
	}

	public String getOndemandFolderEstadoCuenta() {
		return ondemandFolderEstadoCuenta;
	}

	public void setOndemandFolderEstadoCuenta(String ondemandFolderEstadoCuenta) {
		this.ondemandFolderEstadoCuenta = ondemandFolderEstadoCuenta;
	}
*/
	/*
	public String getPathDivisas() {
		return pathDivisas;
	}

	public void setPathDivisas(String pathDivisas) {
		this.pathDivisas = pathDivisas;
	}
*/
	public String getNameSpaceDivisas() {
		return nameSpaceDivisas;
	}

	public void setNameSpaceDivisas(String nameSpaceDivisas) {
		this.nameSpaceDivisas = nameSpaceDivisas;
	}
	/*
	public String getPathReportesDivisasEntrada() {
		return pathReportesDivisasEntrada;
	}

	public void setPathReportesDivisasEntrada(String pathReportesDivisasEntrada) {
		this.pathReportesDivisasEntrada = pathReportesDivisasEntrada;
	}

	public String getPathReportesDivisasProceso() {
		return pathReportesDivisasProceso;
	}

	public void setPathReportesDivisasProceso(String pathReportesDivisasProceso) {
		this.pathReportesDivisasProceso = pathReportesDivisasProceso;
	}

	public String getPathReportesDivisasSalida() {
		return pathReportesDivisasSalida;
	}

	public void setPathReportesDivisasSalida(String pathReportesDivisasSalida) {
		this.pathReportesDivisasSalida = pathReportesDivisasSalida;
	}

	public String getPathFacturacionDivisasEntrada() {
		return pathFacturacionDivisasEntrada;
	}

	public void setPathFacturacionDivisasEntrada(
			String pathFacturacionDivisasEntrada) {
		this.pathFacturacionDivisasEntrada = pathFacturacionDivisasEntrada;
	}

	public String getPathFacturacionDivisasProceso() {
		return pathFacturacionDivisasProceso;
	}

	public void setPathFacturacionDivisasProceso(
			String pathFacturacionDivisasProceso) {
		this.pathFacturacionDivisasProceso = pathFacturacionDivisasProceso;
	}

	public String getPathFacturacionDivisasSalida() {
		return pathFacturacionDivisasSalida;
	}

	public void setPathFacturacionDivisasSalida(String pathFacturacionDivisasSalida) {
		this.pathFacturacionDivisasSalida = pathFacturacionDivisasSalida;
	}

	public String getPathFacturacionDivisasOndemand() {
		return pathFacturacionDivisasOndemand;
	}

	public void setPathFacturacionDivisasOndemand(
			String pathFacturacionDivisasOndemand) {
		this.pathFacturacionDivisasOndemand = pathFacturacionDivisasOndemand;
	}
*/
	
	public String getPassCertificado() {
		return passCertificado;
	}

	public void setPassCertificado(String passCertificado) {
		this.passCertificado = passCertificado;
	}
	
	public String getUrlWebServiceCancelacion() {
		return urlWebServiceCancelacion;
	}
	/*
	public void setUrlWebServiceCancelacion(String urlWebServiceCancelacion) {
		this.urlWebServiceCancelacion = urlWebServiceCancelacion;
	}
*/
	public String getUrlWebServiceCifrasControl() {
		return urlWebServiceCifrasControl;
	}

	public void setUrlWebServiceCifrasControl(String urlWebServiceCifrasControl) {
		this.urlWebServiceCifrasControl = urlWebServiceCifrasControl;
	}

	public String getUrlArchivoCatalogs() {
		return urlArchivoCatalogs;
	}

	public void setUrlArchivoCatalogs(String urlArchivoCatalogs) {
		this.urlArchivoCatalogs = urlArchivoCatalogs;
	}
	
}
