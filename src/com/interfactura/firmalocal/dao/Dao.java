package com.interfactura.firmalocal.dao;

/**
 * 
 * @author User
 *
 * @param <K> tipo usado para el key del objeto
 * @param <E> tipo de la entidad
 */

public interface Dao<K, E> {
	E update(E entity);
    E persist(E entity);
    E findById(K id);
    void remove(E entity);
}