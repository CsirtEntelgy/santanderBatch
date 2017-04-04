package com.interfactura.firmalocal.dao.impl;

import java.lang.reflect.ParameterizedType;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.interfactura.firmalocal.dao.Dao;

public abstract class JpaDao<K, E> implements Dao<K, E> {

	protected Class<E> entityClass;

	@PersistenceContext
	protected EntityManager entityManager;

	@SuppressWarnings("unchecked")
	public JpaDao() {
		ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
		this.entityClass = (Class<E>) genericSuperclass.getActualTypeArguments()[1];
	}

	public E persist(E entity) {
		entityManager.persist(entity);
		return entity; 
	}

	public void remove(E entity) {
		entityManager.remove(entity);
	}

	public E findById(K id) {
		return entityManager.find(entityClass, id);
	}
	
	public E update(E entity){
		return entityManager.merge(entity);
	}

}