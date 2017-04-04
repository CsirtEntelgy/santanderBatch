package com.interfactura.firmalocal.persistence;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.PageDao;
import com.interfactura.firmalocal.dao.PermissionDao;
import com.interfactura.firmalocal.domain.entities.Page;
import com.interfactura.firmalocal.domain.entities.Permission;

@Component
public class PermissionManager {
	
	public PermissionManager() {
	}
	
	@Autowired(required=true)
	PermissionDao permissionDao;
	
	@Autowired(required=true)
	PageDao pageDao;
	
	public Page getPage(long id) {
		Page page = pageDao.findById(id);
		return page;
	}
	
	public  List<Page> listAllPages() {
		return permissionDao.listAllPages();
	}

	public List<Permission> listar() {
		return permissionDao.listar();
	}
	
	public List<Permission> listar(int inicio, int cantidad, Filters<Filter> filters) {
		return permissionDao.listar(inicio, cantidad, filters);
	}
	
	public Permission get(long id) {
		Permission permission = permissionDao.findById(id);
		return permission;
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void update(Permission permission) {
		permissionDao.update(permission);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void delete(long id) {
		Permission permission = permissionDao.findById(id);
		permissionDao.remove(permission);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public Permission create(Permission permission){
		return permissionDao.persist(permission);
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public Permission addPermissionPage(long permissionId, long pageId) {
		Permission permission = this.get(permissionId);
		Page page = this.getPage(pageId);
		permission = this.addPages(permission, page);
		return permission;
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public Permission addPages(Permission permission, Page page){
		if(permission.getPages() == null){
			Set<Page> p = new HashSet<Page>();
			permission.setPages(p);
		}
		if(!permission.getPages().contains(page))
		{
			permission.getPages().add(page);
		}
		return permission;
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public Permission removePermissionPage(long permissionId) {
		Permission permission = this.get(permissionId);
		permission.setPages(null);
		this.update(permission);
		return permission;
	}
	
	public Permission findByName(String name) {
		return permissionDao.findByName(name);
	}
}
