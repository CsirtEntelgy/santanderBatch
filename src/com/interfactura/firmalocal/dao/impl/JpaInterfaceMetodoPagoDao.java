package com.interfactura.firmalocal.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.springframework.stereotype.Component;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.InterfaceMetodoPagoDao;
import com.interfactura.firmalocal.domain.entities.FiscalEntity;
import com.interfactura.firmalocal.domain.entities.InterfaceMetodoPago;
import com.interfactura.firmalocal.domain.entities.RegimenFiscal;
import com.interfactura.firmalocal.persistence.UtilManager;

@Component
public class JpaInterfaceMetodoPagoDao extends
		JpaDao<Long, InterfaceMetodoPago> implements InterfaceMetodoPagoDao {

	@Override
	public List<InterfaceMetodoPago> listAll() {
		String strQuery = "Select x From InterfaceMetodoPago x";
		Query query = entityManager.createQuery(strQuery);
		@SuppressWarnings("unchecked")
		List<InterfaceMetodoPago> interfaceMetPago = (List<InterfaceMetodoPago>) query
				.getResultList();
		return interfaceMetPago;
	}

	@Override
	public List<InterfaceMetodoPago> list(int begin, int quantity,
			Filters<Filter> filters, String feids) {
		String efName = "";
		String clvMetPag = "";
		String nomInt = "";
		String fchMod = "";
		String modPor = "";
		String fchCrea ="";
		String creaPor = "";
		String where = "";
		String stringQuery = "SELECT x FROM InterfaceMetodoPago x ";
		stringQuery += UtilManager.inListar(feids);
		if (filters != null && filters.size() > 0) {
			for (Filter filter : filters) {
				if (filter.getColumn().equals("efName")) {
					efName = filter.getPattern();
				}
				if (filter.getColumn().equals("clvMetPag")) {
					clvMetPag = filter.getPattern();
				}
				if (filter.getColumn().equals("nomInt")) {
					nomInt = filter.getPattern();
				}
				if (filter.getColumn().equals("fchMod")) {
					fchMod = filter.getPattern();
				}
				if (filter.getColumn().equals("modPor")) {
					modPor = filter.getPattern();
				}
				if (filter.getColumn().equals("fchCrea")) {
					modPor = filter.getPattern();
				}
				if (filter.getColumn().equals("creaPor")) {
					modPor = filter.getPattern();
				}
			}
			if (!"".equals(efName)) {
				where += " AND x.fiscalEntity.fiscalName like :efName";
			}
			if (!"".equals(clvMetPag)) {
				where += " AND x.claveMetodoPago like :clvMetPag";
			}
			if (!"".equals(nomInt)) {
				where += " AND x.nombreInterface like :nomInt";
			}
			if (!"".equals(fchMod)) {
				where += " AND x.fechaModificacion like :fchMod";
			}
			if (!"".equals(modPor)) {
				where += " AND x.modificadoPor like :modPor";
			}
			if (!"".equals(modPor)) {
				where += " AND x.fechaCreacion like :modPor";
			}
			if (!"".equals(modPor)) {
				where += " AND x.creadoPor like :creaPor";
			}
		}
		if (!"".equals(where)) {
			stringQuery += where;
		}
		stringQuery += " ORDER BY x.fiscalEntity.fiscalName";
		Query query = entityManager.createQuery(stringQuery);
		if (!"".equals(where)) {
			if (!"".equals(efName)) {
				query.setParameter("efName", "%" + efName + "%");
			}
			if (!"".equals(clvMetPag)) {
				query.setParameter("clvMetPag", "%" + clvMetPag + "%");
			}
			if (!"".equals(nomInt)) {
				query.setParameter("nomInt", "%" + nomInt + "%");
			}
			if (!"".equals(fchMod)) {
				query.setParameter("fchMod", "%" + fchMod + "%");
			}
			if (!"".equals(modPor)) {
				query.setParameter("modPor", "%" + modPor + "%");
			}
			if (!"".equals(fchCrea)) {
				query.setParameter("fchCrea", "%" + fchCrea + "%");
			}
			if (!"".equals(creaPor)) {
				query.setParameter("creaPor", "%" + creaPor + "%");
			}
		}
		if (quantity != 0) {
			query.setFirstResult(begin);
			query.setMaxResults(quantity);
		}
		@SuppressWarnings("unchecked")
		List<InterfaceMetodoPago> recordList = query.getResultList();
		return recordList;
	}
	
	@Override
	public List<InterfaceMetodoPago> listByColumn(String nomInterface) {
		String efName = "";
		String clvMetPag = "";
		String nomInt = "";
		String fchMod = "";
		String modPor = "";
		String fchCrea ="";
		String creaPor = "";
		String where = "";
		String[] fileNameSplit = nomInterface.split(",");
		List<InterfaceMetodoPago> recordList = new ArrayList<InterfaceMetodoPago>();
		
			for(String nombre : fileNameSplit){
				
				String stringQuery = "SELECT x FROM InterfaceMetodoPago x WHERE x.nombreInterface = :nomInterface";
				
				System.out.println("Este es el query que se pide: " + stringQuery);
				Query query = entityManager.createQuery(stringQuery);
				query.setParameter("nomInterface",  nombre );
				
				System.out.println("Voy a pedir la lista");
				
				List<InterfaceMetodoPago> listaquery = query.getResultList();
				recordList.addAll(listaquery);
				
			}
			
		System.out.println("Imprimire la lista que se trajo");
		for(InterfaceMetodoPago iMP : recordList){
			System.out.println("Nombre de la interface: " + iMP.getNombreInterface());
		}
		
		return recordList;
		
	}

	@Override
	public List<InterfaceMetodoPago> listExcel(int begin, int quantity,
			Filters<Filter> filters, String feids) {
		String efName = "";
		String clvMetPag = "";
		String nomInt = "";
		String fchMod = "";
		String modPor = "";
		String fchCrea = "";
		String creaPor = "";
		String where = "";

		String stringQuery = "SELECT UPPER(NVL(z.fiscalName, ' ')), "
				+ "UPPER(NVL(x.COD_CLAV_MET_PAG, ' ')), UPPER(NVL(x.TXT_NOM_INTFC, ' ')), "
				+ "UPPER(NVL(x.FCH_CREA, ' ')) FROM InterfaceMetodoPago x "
				+ "INNER JOIN FiscalEntity z ON x.fiscalentity_id = z.id";

		stringQuery += JpaInterfaceMetodoPagoDao.inListar(feids);
		if (filters != null && filters.size() > 0) {
			for (Filter filter : filters) {
				if (filter.getColumn().equals("efName")) {
					efName = filter.getPattern();
				}
				if (filter.getColumn().equals("clvMetPag")) {
					clvMetPag = filter.getPattern();
				}
				if (filter.getColumn().equals("nomInt")) {
					nomInt = filter.getPattern();
				}
				if (filter.getColumn().equals("fchMod")) {
					fchMod = filter.getPattern();
				}
				if (filter.getColumn().equals("modPor")) {
					modPor = filter.getPattern();
				}
			}
			if (!"".equals(efName)) {
				where += " AND UPPER(z.fiscalName) like '%"
						+ efName.toUpperCase() + "%'";
			}
			if (!"".equals(clvMetPag)) {
				where += " AND UPPER(x.claveMetodoPago) like '%" + clvMetPag.toUpperCase()
						+ "%'";
			}
			if (!"".equals(nomInt)) {
				where += " AND UPPER(x.nombreInterface) like '%"
						+ nomInt.toUpperCase() + "%'";
			}
			if (!"".equals(fchMod)) {
				where += " AND UPPER(x.fechaModificacion) like '%"
						+ fchMod.toUpperCase() + "%'";
			}
			if (!"".equals(modPor)) {
				where += " AND UPPER(x.modificadoPor) like '%"
						+ modPor.toUpperCase() + "%'";
			}
			if (!"".equals(creaPor)) {
				where += " AND UPPER(x.creadoPor) like '%"
						+ creaPor.toUpperCase() + "%'";
			}
			if (!"".equals(fchCrea)) {
				where += " AND UPPER(x.fchCrea) like '%"
						+ fchCrea.toUpperCase() + "%'";
			}
		}
		if (!"".equals(where)) {
			stringQuery += where;
		}
		stringQuery += " ORDER BY fiscalName_order";

		Query query = entityManager.createNativeQuery(stringQuery);
		/*
		 * if (!"".equals(where)) { if (!"".equals(efName)) {
		 * query.setParameter("efName", "%" + efName + "%"); } if
		 * (!"".equals(clvMetPag)) { query.setParameter("clvMetPag", "%" + clvMetPag +
		 * "%"); } if (!"".equals(nomInt)) { query.setParameter("nomInt", "%" +
		 * nomInt + "%"); } if (!"".equals(desc)) {
		 * query.setParameter("desc", "%" + desc + "%"); } if
		 * (!"".equals(fchMod)) { query.setParameter("fchMod", "%" + fchMod +
		 * "%"); } if (!"".equals(modPor)) { query.setParameter("modPor", "%" +
		 * modPor + "%"); } }
		 */
		if (quantity != 0) {
			query.setFirstResult(begin);
			query.setMaxResults(quantity);
		}

		// List<CFDFieldsV22> recordList = query.getResultList();

		List<InterfaceMetodoPago> recordList = null;

		@SuppressWarnings("unchecked")
		List<Object[]> resultado = query.getResultList();

		if (resultado.size() != 0)
			recordList = transformaLista(resultado);

		return recordList;
	}

	public List<InterfaceMetodoPago> transformaLista(List<Object[]> resultado) {

		List<InterfaceMetodoPago> listaInterfacesMetodoPago = new ArrayList<InterfaceMetodoPago>();
		InterfaceMetodoPago interfaceMetPag;
		//RegimenFiscal r;
		FiscalEntity f;

		System.out.println("enters transformalista");
		for (Object[] result : resultado) {
			interfaceMetPag = new InterfaceMetodoPago();
			//r = new RegimenFiscal();
			f = new FiscalEntity();

			// c.setId((Long)result[0]);

			f.setFiscalName((String) result[0]);

			//r.setName((String) result[1]);

			interfaceMetPag.setFiscalEntity(f);
			//cfdv22.setRegimenFiscal(r);

			

			listaInterfacesMetodoPago.add(interfaceMetPag);
		}
		System.out.println("exit transformalista");
		return listaInterfacesMetodoPago;
	}

	public static String inListar(String ids) {
		if (ids == null || ids.equals("")) {
			String consulta = " WHERE x.fiscalentity_id = 0 ";
			return consulta;
		}
		String[] idsList = ids.split(",");
		String consulta = " WHERE ( ";
		for (int i = 0; i < idsList.length; i++) {
			consulta += " x.fiscalentity_id = " + idsList[i];
			if (i < idsList.length - 1) {
				consulta += " OR";
			}
		}
		consulta += ")";
		return consulta;
	}

	@Override
	public InterfaceMetodoPago findByFiscalId(long id) {
		String strQuery = "Select x From InterfaceMetodoPago x Where x.fiscalEntity.id = :id";
		Query query = entityManager.createQuery(strQuery);
		query.setParameter("id", id);
		try {
			return (InterfaceMetodoPago) query.getSingleResult();
		} catch (NonUniqueResultException e) {
			return (InterfaceMetodoPago) query.getResultList().get(0);
		} catch (NoResultException e) {
			return null;
		}
	}

}
