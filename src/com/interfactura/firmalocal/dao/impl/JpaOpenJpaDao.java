package com.interfactura.firmalocal.dao.impl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.interfactura.firmalocal.dao.OpenJpaDao;
import com.interfactura.firmalocal.domain.entities.FiscalEntity;
import com.interfactura.firmalocal.domain.entities.OpenJpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * Clase para el manejo de la tabla openjpa_sequence_table.
 */
@Component
public class JpaOpenJpaDao extends JpaDao<Long, OpenJpa> implements OpenJpaDao{
    
	@PersistenceContext
	protected EntityManager entityManager;
	
    
    public long getFolioByFiscalEntity(long fiscalEntityId){
    	try {
        long folio = 0;
        String sql = "";
        if(entityManager != null) {
        	sql = "select sequence_value from openjpa_sequence_table where id = " + fiscalEntityId;
        	Query qC = entityManager.createNativeQuery(sql);
        	@SuppressWarnings("unchecked")
			BigDecimal resultado = (BigDecimal)qC.getSingleResult();
        	if(resultado != null) {
            	folio = resultado.longValue() + 1;
            	return folio;
        	} else {
        		folio = 1;
        		return folio;
        	}
        	
        } 
      
    	} catch (Exception ex) {
    		ex.printStackTrace();
    		ex.getMessage();
    	}
        return 0;
    }

}
