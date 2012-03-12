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

import java.util.*;

import com.openmeap.EventNotificationException;
import com.openmeap.cluster.ClusterNotificationException;
import com.openmeap.model.dto.Application;
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
	private Collection<ModelServiceEventNotifier> eventNotifiers = null;
	
	public void clearPersistenceContext() {
		entityManager.clear();
	}
	
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
		callEventNotifiers(ModelServiceOperation.SAVE_OR_UPDATE,obj2Persist);
		return obj2Persist;
	}

	public <T extends ModelEntity> void refresh(T obj2Refresh) throws PersistenceException {
		this._refresh(obj2Refresh);
		callEventNotifiers(ModelServiceOperation.REFRESH,obj2Refresh);
	}
	
	public <T extends ModelEntity> void delete(T obj2Delete) throws PersistenceException {		
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
		// give the event notifiers an opportunity to act, prior to deletion
		callEventNotifiers(ModelServiceOperation.DELETE,obj2Delete);
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
	
	public ApplicationVersion findAppVersionByNameAndId(String appName, String versionId) {
		Query q = entityManager.createQuery("select distinct av "
				+"from ApplicationVersion av inner join fetch av.application a "
				+"where av.identifier=:identifier "
				+"and a.name=:name");
		q.setParameter("name", appName);
		q.setParameter("identifier", versionId);
		try {
			ApplicationVersion ver = (ApplicationVersion)q.getSingleResult();
			return ver;
		} catch( NoResultException nre ) {
			return null;
		}
	}
	
	public List<Deployment> findDeploymentsByNameAndId(String appName, String versionId) {
		Query q = entityManager.createQuery("select d "
				+"from Deployment d inner join fetch d.applicationVersion av inner join fetch d.application a "
				+"where av.identifier=:identifier "
				+"and a.name=:name ");
		q.setParameter("name", appName);
		q.setParameter("identifier", versionId);
		try {
			@SuppressWarnings(value={"unchecked"})
			List<Deployment> deployments = (List<Deployment>)q.getResultList();
			return deployments;
		} catch( NoResultException nre ) {
			return null;
		}
	}
	
	// ACCESSORS
	
	public void setEventNotifiers(Collection<ModelServiceEventNotifier> handlers) {
		eventNotifiers = handlers;
	}
	public Collection<ModelServiceEventNotifier> getEventNotifiers() {
		return eventNotifiers;
	}
	
	public void setEntityManager(EntityManager manager) {
		entityManager = manager;
	}
	public EntityManager getEntityManager() {
		return entityManager;
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
	
	private void _refresh(ModelEntity obj2Refresh) {
		if( !entityManager.contains(obj2Refresh) ) {
			entityManager.merge(obj2Refresh);
		}
		entityManager.refresh(obj2Refresh);
	}
	
	private void callEventNotifiers(ModelServiceOperation op, ModelEntity obj2ActOn) {
		// if there are any web-servers out there to notify of the update,
		// then do so
		if( eventNotifiers!=null ) {
			for( ModelServiceEventNotifier handler : eventNotifiers ) {
				try {
					if( handler.notifiesFor(op,obj2ActOn) ) {
						handler.notify( new ModelEntityEvent(op,obj2ActOn) );
					}
				} catch( EventNotificationException e ) {
					/* TODO: in order to handle this elegantly, i need to convert this whole interface
					 * to accept request objects and return response objects,
					 * as I do not want to simply throw these exceptions, but rather
					 * alert the user.
					 * 
					 * Perhaps I should have a more global event system?
					 */
					logger.error(String.format("EventNotificationException occurred: %s",e.getMessage()));
				}
			}
		}
	}
}
