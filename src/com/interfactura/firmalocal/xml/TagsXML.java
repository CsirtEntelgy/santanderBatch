package com.interfactura.firmalocal.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.interfactura.firmalocal.domain.entities.AddendumCustoms;
import com.interfactura.firmalocal.domain.entities.CFDIssued;
import com.interfactura.firmalocal.domain.entities.FiscalEntity;
import com.interfactura.firmalocal.persistence.CFDIssuedIncidenceManager;
import com.interfactura.firmalocal.persistence.CFDIssuedManager;

@Component
public class TagsXML {

	public TagsXML(){
		
	}
	
	public final String addenda="\r\n<as:AddendaSantanderV1 " +
			"xmlns:as=\"http://www.santander.com.mx/schemas/xsd/AddendaSantanderV1\" " +
			"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">";
	public String _E="Emisor";
	public String _R="Receptor";
	public String _Cons="Conceptos";
	public String _Con="Concepto";
	public String _I="Impuestos";
	public String _Tra="Traslado";
	public String _Tras="Traslados";
	public String _Cmp="Complemento";
	public String _ECB="ecb:";
	public String _ECBD="EstadoDeCuentaBancario";
	public String _mov="Movimientos";
	public String _movECB="MovimientoECB";
	public String _movECBF="MovimientoECBFiscal ";
	
	public String NUM_CTE;
	public String NUM_CTA;
	public String PERIODO;
	public String NUM_TARJETA;
	public String SUBTOTAL_MN;
	public String SUBTOTAL_REPORTE;
	public String TOTAL_MN;
	public String TOTAL_REPORTE;
	public String IVA_TOTAL_MN;
	public String IVA_TOTAL_REPORTE;
	public String LONGITUD;
	public String NUM_PROVEEDOR;
	public String CTA_DEPOSITO;
	public String EMAIL;
	public String MONEDA;
	public String ORDEN_COMPRA;
	public String TIPO_MONEDA;
	public String TIPO_CAMBIO;
	public String UNIDAD_MEDIDA;
	public String REGIMEN_FISCAL;
	public String METODO_PAGO;
	public String LUGAR_EXPEDICION;
	public String FORMA_PAGO;
	
	public String EMISION_RFC="";
	public String RECEPCION_RFC="";
	public String INSTITUCION_RECEPTORA="";
	public String NOMBRE_BENIFICIARIO="";
	public String EMISION_CONTRATO="";
	public String EMISION_CODIGO_CLIENTE="";
	public String EMISION_CENTRO_COSTOS="";
	public String EMISION_PERIODO="";
	public String EMISION_CLAVE_SANTANDER="";
	public String EMISION_FOLIO_INTERNO="";
	public String TIPO_FORMATO="";
	public String FECHA_CFD="";
	public String SERIE_FISCAL_CFD="";
	public String FOLIO_FISCAL_CFD="";
	public String NUM_APROBACION="";
	public String YEAR_APROBACION="";
	public String NUM_CERTIFICADO="";
	public String CFD_TYPE="";
	public String FOLIO_REFERENCE="";
	public String DESCRIPTION_TASA="";
	public String FACTORAJE_HORA="";
	public String FACTORAJE_TIPO="";
	public String FACTORAJE_SVN="";
	public String FACTORAJE_SPB="";
	public String FACTORAJE_SPF="";
	public String FACTORAJE_SID="";
	public String FACTORAJE_LCI="";
	public String FACTORAJE_LIVA="";
	public String FACTORAJE_COMISION="";
	public String FACTORAJE_LETRAS="";
	
	public String _Calle;
	public String _NoExterior;
	public String _NoInterior;
	public String _Colonia;
	public String _Municipio;
	public String _Estado;
	public String _Pais;
	public String _CodigoPostal;
	public String _Localidad;
	public String _Referencia;
	
	public boolean isEmisor;
	public boolean isReceptor;
	public boolean isConceptos;
	public boolean isConcepto;
	public boolean isPredial;
	public boolean isParte;
	public boolean isComplementoConcepto;
	public boolean isAduanera;
	public boolean isRetenciones;
	public boolean isTralados;
	public boolean isImpuestos;
	public boolean isComplemento;
	public boolean isMovimiento;
	public boolean isComprobante;
	public boolean isAdenda;
	public boolean isDescriptionTASA;
	public boolean isEntidadFiscal;
	public boolean isFolioRange;
	public boolean isFormat;
	public boolean isAddenda;
	public String claveTipoRelacion;
	public List<String> uuidsTipoRelacion = new ArrayList<String>();
	
	public String _CONTROLCFD="CONTROLCFD";
	public String _CFD="CFD";
	public String _EMISOR="EMISOR";
	public String _RECEPTOR="RECEPTOR";
	public String _DOMICILIO="DOMICILIO";
	public String _CONCEPTO="CONCEPTO";
	public String _CCONCEPTO="CCONCEPTO";
	public String _INFOADUANERA="INFOADUANERA";
	public String _PREDIAL="PREDIAL";
	public String _PARTE="PARTE";
	public String _IMPUESTOS="IMPUESTOS";
	public String _RETENCION="RETENCION";
	public String _TRASLADO="TRASLADO";
	public String _FACTORAJE="FACTORAJE";
	public String _CFDIREL="CFDIREL";
	@Autowired
	public CFDIssuedManager cFDIssuedManager;
	@Autowired
	public CFDIssuedIncidenceManager cFDIssuedIncidenceManager;

	public FiscalEntity fis;
	public CFDIssued cfd;
	public Set<AddendumCustoms> lstCustoms;
	
	public String TOTAL_IMP_RET = "";
	public String TOTAL_IMP_TRA = "";
	
	public String NOMBRE_APP_REPECB = "";
	
	//Variable de mapeo de catalogos AMDA
	public Map<String, ArrayList<CatalogosDom>> mapCatalogos ;
	public String tipoComprobante = "";
	public String recepCP = "";
	public String recepPais = "";
	public String regimenFiscalCode = "";
	public String retencionImpuestoVal = "";
	public String retencionImporteVal = "";
	public String trasladoImpuestoVal = "";
	public String trasladoTasaVal = "";
	public String trasladoImporteVal = "";
	public Integer decimalesMoneda = 0;
	public String lineaAnterior = "";
	public String[] lineaAnteriorTokens;
	public long contCFDAnterior = 0L;
	public boolean atributoTotalImpuestosReten = false;
	public boolean atributoTotalImpuestosTras = false;
	public String sumTotalImpuestosReten = "";
	public String sumTotalImpuestosTras = "";
	public String sumRetTotalIsr = "";
	public String sumRetTotalIva = "";
	public String sumRetTotalIeps = "";
	public String sumTraTotalIsr = "";
	public String sumTraTotalIva = "";
	public String sumTraTotalIeps = "";
	
	public Double sumRetTotalIsrDou = 0.00;
	public Double sumRetTotalIvaDou = 0.00;
	public Double sumRetTotalIepsDou = 0.00;
	public Double sumTraTotalIsrDou = 0.00;
	public Double sumTraTotalIvaDou = 0.00;
	public Double sumTraTotalIepsDou = 0.00;
	public Double descuentoFactValDou = 0.00;
	public Integer numeroConceptosFac = 0;
	
	public Map<String, String[]> mapConcep = new HashMap<String, String[]>();
	public Map<String, Long> mapConcepL = new HashMap<String, Long>();
	public Integer numControl = 0;
	public String[] lineaAnteriorConceptoTokens;
	public long contCFDAnteriorConcepto = 0L;
	public String[] lineaAnteriorImpuestoTokens;
	public long contCFDAnteriorImpuesto = 0L;
	public String[] lineaAnteriorRetencionTokens;
	public long contCFDAnteriorRetencion = 0L;
	public boolean exentoT = false;
	public boolean noExentoT = false;
	public Double subtotalDoubleTag = 0.00;
	public Double totalRetAndTraDoubl = 0.00;
	public Double sumTotalImpuestosTrasDou = 0.00;
	public boolean isECBEnCeros = false;
	
}
