package com.interfactura.firmalocal.persistence;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.interfactura.firmalocal.controllers.Filter;
import com.interfactura.firmalocal.controllers.Filters;
import com.interfactura.firmalocal.dao.PermissionDao;
import com.interfactura.firmalocal.dao.RoleDao;
import com.interfactura.firmalocal.domain.entities.Permission;
import com.interfactura.firmalocal.domain.entities.Role;

@Component
public class RolesManager {
	
	
	private static final Logger logger = Logger.getLogger(RolesManager.class);
	
	@Autowired(required=true)
	RoleDao roleDao;
	
	@Autowired(required=true)
	PermissionDao permissionDao;
	
	public List<Role> listar() {
		return roleDao.listar();
	}
	
	public List<Role> listar(int inicio, int cantidad, Filters<Filter> filters) {
		logger.info("listar Roles");
		return roleDao.listar(inicio, cantidad, filters);
	}

	public Role get(long id) {
		Role role = roleDao.findById(id);
		return role;
	}
	
	public Permission getPermission(long id) {
		Permission permission = permissionDao.findById(id);
		return permission;
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void update(Role role) {
		roleDao.update(role);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void delete(long id) {
		Role role = roleDao.findById(id);
		roleDao.remove(role);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public Role create(Role role){
		return roleDao.persist(role);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public Role addRolePermission(long roleId, long permissionId) {
		Role role = this.get(roleId);
		Permission permission = this.getPermission(permissionId);
		role = this.addPermission(role, permission);
		return role;
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public Role removeRolePermission(long roleId) {
		Role role = this.get(roleId);
		role.setPermissions(null);
		this.update(role);
		return role;
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public Role addPermission(Role role, Permission permission){
		Set<Permission> permissions = role.getPermissions();
		if(permissions == null){
			Set<Permission> p = new HashSet<Permission>();
			role.setPermissions(p);
		}
		if(!permissions.contains(permission)){
			role.getPermissions().add(permission);
		}
		return role;
	}
	
	public Role findByName(String name) {
		return roleDao.findByName(name);
	}
}
