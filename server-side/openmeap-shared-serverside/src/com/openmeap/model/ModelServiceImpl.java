/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2012 OpenMEAP, Inc.                                   #
 #    Credits to Jonathan Schang & Robert Thacher                              #
 #                                                                             #
 #    Released under the LGPLv3                                                #
 #                                                                             #
 #    OpenMEAP is free software: you can redistribute it and/or modify         #
 #    it under the terms of the GNU Lesser General Public License as published #
 #    by the Free Software Foundation, either version 3 of the License, or     #
 #    (at your option) any later version.                                      #
 #                                                                             #
 #    OpenMEAP is distributed in the hope that it will be useful,              #
 #    but WITHOUT ANY WARRANTY; without even the implied warranty of           #
 #    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the            #
 #    GNU Lesser General Public License for more details.                      #
 #                                                                             #
 #    You should have received a copy of the GNU Lesser General Public License #
 #    along with OpenMEAP.  If not, see <http://www.gnu.org/licenses/>.        #
 #                                                                             #
 ###############################################################################
 */

package com.openmeap.model;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import com.openmeap.cluster.ClusterNotificationException;
import com.openmeap.event.EventNotificationException;
import com.openmeap.event.ProcessingEvent;
import com.openmeap.model.dto.Application;
import com.openmeap.model.dto.ApplicationArchive;
import com.openmeap.model.dto.ApplicationVersion;
import com.openmeap.model.dto.Deployment;
import com.openmeap.model.event.ModelEntityEvent;
import com.openmeap.model.event.ModelEntityModifyEvent;
import com.openmeap.model.event.handler.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.*;
import javax.persistence.*;

/**
 * Intended to basically pass-through to the entity manager.
 * 
 * Implemented with the idea that a different persistence 
 * mechanism might one day be used.
 * 
 * @author schang
 */
@Transactional
public class ModelServiceImpl implements ModelService 
{
	private Logger logger = LoggerFactory.getLogger(ModelServiceImpl.class);
	
	@PersistenceContext(name="openmeap-jpa")
	private EntityManager entityManager = null;
	
	public void clearPersistenceContext() {
		entityManager.clear();
	}
	
	@Override
	public <T extends ModelEntity> T saveOrUpdate(T obj) throws PersistenceException {
		T obj2Persist = obj;
		try {
			entityManager.getTransaction().begin();
			// if we haven't loaded this object yet,
			// then attempt to do so
			if( ! entityManager.contains(obj2Persist) ) {
				obj2Persist = entityManager.merge(obj);
			}
			entityManager.persist(obj2Persist);
			entityManager.getTransaction().commit();
			this._refresh(obj2Persist);
		} catch( PersistenceException pe ) {
			if( entityManager.isOpen() && entityManager.getTransaction().isActive() ) {
				entityManager.getTransaction().rollback();
			}
			throw new PersistenceException(pe);
		}
		return obj2Persist;
	}
	
	@Override
	public <T extends ModelEntity> void delete(T obj2Delete) throws PersistenceException {		
		_delete(obj2Delete,null);		
	}
	
	@Override
	public <T extends ModelEntity> void refresh(T obj2Refresh) throws PersistenceException {
		this._refresh(obj2Refresh);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends ModelEntity> T findByPrimaryKey(Class<T> clazz, Object pk) {
		try {
			T obj = (T)entityManager.find(clazz, pk);
			return obj;
		} catch( NoResultException nre ) {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends ModelEntity> List<T> findAll(Class<T> clazz) {
		Query q = entityManager.createQuery("select distinct a from "+clazz.getCanonicalName()+" a");
		try {
			return q.getResultList();
		} catch( NoResultException nre ) {
			return null;
		}
	}
	
	@Override
	public Application findApplicationByName(String name) {
		Query q = entityManager.createQuery("select distinct a "
				+"from Application a "
				+"where a.name=:name");
		q.setParameter("name", name);
		try {
			return (Application)q.getSingleResult();
		} catch( NoResultException nre ) {
			return null;
		}
	}
	
	@Override
	public ApplicationVersion findAppVersionByNameAndId(String appName, String identifier) {
		Query q = entityManager.createQuery("select distinct av "
				+"from ApplicationVersion av inner join fetch av.application a "
				+"where av.identifier=:identifier "
				+"and a.name=:name");
		q.setParameter("name", appName);
		q.setParameter("identifier", identifier);
		try {
			ApplicationVersion ver = (ApplicationVersion)q.getSingleResult();
			return ver;
		} catch( NoResultException nre ) {
			return null;
		}
	}
	
	@Override
	public List<Deployment> findDeploymentsByNameAndId(String appName, String identifier) {
		Query q = entityManager.createQuery("select d "
				+"from Deployment d inner join fetch d.applicationVersion av inner join fetch d.application a "
				+"where av.identifier=:identifier "
				+"and a.name=:name ");
		q.setParameter("name", appName);
		q.setParameter("identifier", identifier);
		try {
			@SuppressWarnings(value={"unchecked"})
			List<Deployment> deployments = (List<Deployment>)q.getResultList();
			return deployments;
		} catch( NoResultException nre ) {
			return null;
		}
	}
	
	@Override
	public Deployment getLastDeployment(Application app) {
		Query q = entityManager.createQuery("select distinct d "
			+"from Deployment d join d.application "
			+"where d.application.id=:id "
			+"order by d.createDate desc");
		q.setParameter("id", app.getId());
		q.setMaxResults(1);
		try {
			Object o = q.getSingleResult();
			return (Deployment)o;
		} catch( NoResultException nre ) {
			return null;
		}
	}
	
	@Override
	public List<ApplicationArchive> findApplicationArchivesByHashAndAlgorithm(String hash, String hashAlgorithm) {
		Query q = entityManager.createQuery("select distinct ar "
				+"from ApplicationArchive ar join fetch ar.version "
				+"where ar.hash=:hash "
				+"and ar.hashAlgorithm=:hashAlgorithm");
		q.setParameter("hash", hash);
		q.setParameter("hashAlgorithm", hashAlgorithm);
		q.setMaxResults(1);
		try {
			List<ApplicationArchive> o = (List<ApplicationArchive>)q.getResultList();
			return (List<ApplicationArchive>)o;
		} catch( NoResultException nre ) {
			return null;
		}
	}
	
	public <E extends ModelEntity, T extends ModelEntity> List<T> getOrderedDeployments(E entity, String listMethod, Comparator<T> comparator) {
		EntityManager entityManager = getEntityManager(); 
		entityManager.getTransaction().begin();
		entityManager.merge(entity);
		List<T> depls;
		try {
			depls = (List<T>) entity.getClass().getMethod(listMethod).invoke(entity);
		} catch (Exception e) {
			throw new PersistenceException(e);
		}
		Collections.sort( depls, comparator );
		entityManager.getTransaction().commit();
		return depls;
	}
	
	// ACCESSORS
	
	public void setEntityManager(EntityManager manager) {
		entityManager = manager;
	}
	public EntityManager getEntityManager() {
		return entityManager;
	}
	
	// PRIVATE METHODS
	
	private <T extends ModelEntity> void _delete(T obj2Delete, List<ProcessingEvent> events) throws PersistenceException {		
		// give the event notifiers an opportunity to act, prior to deletion
		try {
			entityManager.getTransaction().begin();
			this._refresh(obj2Delete);
			obj2Delete.remove();
			entityManager.remove(obj2Delete);
			entityManager.getTransaction().commit();
		} catch( PersistenceException pe ) {
			if( entityManager.isOpen() && entityManager.getTransaction().isActive() ) {
				entityManager.getTransaction().rollback();
			}
			throw new PersistenceException(pe);
		}			
	}
	
	private void _refresh(ModelEntity obj2Refresh) {
		if( !entityManager.contains(obj2Refresh) ) {
			entityManager.merge(obj2Refresh);
		}
		entityManager.refresh(obj2Refresh);
	}
}
