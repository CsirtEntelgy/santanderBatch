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
import com.interfactura.firmalocal.dao.FiscalEntityDao;
import com.interfactura.firmalocal.dao.RoleDao;
import com.interfactura.firmalocal.dao.UserDao;
import com.interfactura.firmalocal.domain.entities.FiscalEntity;
import com.interfactura.firmalocal.domain.entities.Role;
import com.interfactura.firmalocal.domain.entities.User;

@Component
public class UserManager 
{
	
	public UserManager() {
	}
	
	@Autowired(required=true)
	UserDao userDao;
	
	@Autowired(required=true)
	FiscalEntityDao fiscalEntityDao;
	
//	@Autowired(required=true)
//	RoleDao roleDao;

	public List<Role> listAllRoles() {
		return userDao.listAllRoles();
	}
	
	public List<FiscalEntity> listAllFiscalEntity() {
		return userDao.listAllFiscalEntity();
	}
	
	public List<User> listar() {	
		return userDao.listar();
	}
	
	public List<User> listar(int inicio, int cantidad, Filters<Filter> filters) {
		return userDao.listar(inicio, cantidad, filters);
	}
	
	public User findByName(String userName) {
		return userDao.findByName(userName);
	}
	
	public User get(long id) {
		User user = userDao.findById(id);
		return user;
	}
	
//	public Role getRole(long id) {
//		Role role = roleDao.findById(id);
//		return role;
//	}

	public FiscalEntity getFiscalEntity(long id) {
		FiscalEntity fiscalEntity = fiscalEntityDao.findById(id);
		return fiscalEntity;
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void update(User user) {
		userDao.update(user);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void delete(long id) {
		User user = userDao.findById(id);
		userDao.remove(user);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public User create(User user){
		return userDao.persist(user);
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void desactiveUser(long id) {
		User user = this.get(id);
		user.setStatus("Desactivado");
		this.update(user);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public void activeUser(long id) {
		User user = this.get(id);
		user.setStatus("Activado");
		this.update(user);
	}
	
//	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
//	public User addUserRole(long userId, long roleId) {
//		User user = this.get(userId);
//		Role role = this.getRole(roleId);
//		this.addRoles(user, role);
//		return user;
//	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public User addUserFiscalEntity(long userId, long fiscalEntityId) {
		User user = this.get(userId);
		FiscalEntity fiscalEntity = this.getFiscalEntity(fiscalEntityId);
		user = this.addFiscalEntities(user, fiscalEntity);
		return user;
	}
	
//	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
//	public User addRoles(User user, Role role)
//	{
//		if(user.getRoles() == null){
//			Set<Role> r = new HashSet<Role>();
//			user.setRoles(r);
//		}
//		if(!user.getRoles().contains(role))
//		{
//			user.getRoles().add(role);
//		}
//		return user;
//	}

	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public User addFiscalEntities(User user, FiscalEntity fiscalEntity){
		if (user.getFiscalEntities() == null) {
			Set<FiscalEntity> f = new HashSet<FiscalEntity>();
			user.setFiscalEntities(f);
		}
		if(!user.getFiscalEntities().contains(fiscalEntity))
		{
			user.getFiscalEntities().add(fiscalEntity);
		}
		return user;
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
	public User removeUserFiscalEntities(long userId) 
	{
		User user = this.get(userId);
		user.setFiscalEntities(null);
		this.update(user);
		return user;
	}
	
//	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor=Exception.class)
//	public User removeUserRoles(long userId) 
//	{
//		User user = this.get(userId);
//		user.setRoles(null);
//		this.update(user);
//		return user;
//	}
}
