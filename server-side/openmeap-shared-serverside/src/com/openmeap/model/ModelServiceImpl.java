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

import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.openmeap.event.ProcessingEvent;
import com.openmeap.model.dto.Application;
import com.openmeap.model.dto.ApplicationArchive;
import com.openmeap.model.dto.ApplicationVersion;
import com.openmeap.model.dto.Deployment;

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
	
	private EntityManager entityManager = null;
	
	/**
	 * Number of times to retry refreshing a model entity before failing the operation.
	 * Using SQLite, sometimes the database file is locked by another process.
	 */
	private int numberOfRefreshRetries=3;
	
	/**
	 * Amount of time to wait between each refresh retry.
	 */
	private int refreshRetryInterval=250;
	
	public void clearPersistenceContext() {
		entityManager.clear();
	}
	
	@Override
	public <T extends ModelEntity> T saveOrUpdate(T entity) throws PersistenceException {
		T entityToReturn = entity;
		try {
			// if we haven't loaded this object yet,
			// then attempt to do so
			if( ! entityManager.contains(entity) ) {
				entityToReturn = entityManager.merge(entity);
			}
			entityManager.persist(entityToReturn);
		} catch( PersistenceException pe ) {
			throw new PersistenceException(pe);
		}
		return entityToReturn;
	}
	
	@Override
	public <T extends ModelEntity> void delete(T obj2Delete) throws PersistenceException {		
		_delete(obj2Delete,null);		
	}

	@Override
	public <T extends ModelEntity> void refresh(T entity) throws PersistenceException {
		int numRetries = numberOfRefreshRetries;
		boolean notSuccessful = false;
		do {
			try{
				if(entity!=null) {
					this._refresh(entity);
				}
			} catch(Exception e) {
				Throwable t = ExceptionUtils.getRootCause(e);
				if(!(t instanceof SQLException)) {
					throw new PersistenceException(e);
				}
				logger.warn("Unable to refresh model entity, "+numRetries+" left.  "+t.getMessage());
				numRetries--;
				notSuccessful=true;
				try {
					Thread.sleep(refreshRetryInterval);
				} catch (InterruptedException e1) {
					throw new PersistenceException("Thread sleep interrupted during the refresh retry interval: "+e1.getMessage(),e1);
				}
			}
		} while(notSuccessful && numRetries!=0);
		if(numRetries==0) {
			throw new PersistenceException("Unable to refresh model entity.  "+numberOfRefreshRetries+" retries failed");
		}
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
				+"from ApplicationVersion av "
				+"inner join fetch av.application a "
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
	public List<Deployment> findDeploymentsByApplication(Application app) {
		Query q = entityManager.createQuery("select d "
				+"from Deployment d "
				+"inner join fetch d.applicationArchive aa "
				+"inner join d.application a "
				+"where a.name=:name");
		q.setParameter("name", app.getName());
		try {
			@SuppressWarnings(value={"unchecked"})
			List<Deployment> deployments = (List<Deployment>)q.getResultList();
			return deployments;
		} catch( NoResultException nre ) {
			return null;
		}
	}
	
	@Override
	public List<Deployment> findDeploymentsByApplicationArchive(ApplicationArchive archive) {
		Query q = entityManager.createQuery("select distinct d "
				+"from Deployment d "
				+"inner join fetch d.applicationArchive aa "
				+"where aa.id=:id" );
		q.setParameter("id", archive.getId());
		try {
			@SuppressWarnings(value={"unchecked"})
			List<Deployment> deployments = (List<Deployment>)q.getResultList();
			return deployments;
		} catch( NoResultException nre ) {
			return null;
		}
	}
	
	@Override
	public List<ApplicationVersion> findVersionsByApplicationArchive(ApplicationArchive archive) {
		Query q = entityManager.createQuery("select distinct av "
				+"from ApplicationVersion av "
				+"inner join fetch av.archive aa "
				+"where aa.id=:id " );
		q.setParameter("id", archive.getId());
		try {
			@SuppressWarnings(value={"unchecked"})
			List<ApplicationVersion> versions = (List<ApplicationVersion>)q.getResultList();
			return versions;
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
	public ApplicationArchive getApplicationArchiveByDeployment(Deployment depl) {
		Query q = entityManager.createQuery("select distinct aa "
				+"from ApplicationArchive aa, Deployment d  "
				+"where d.applicationArchive=aa and d.id=:id ");
		q.setParameter("id", depl.getId());
		q.setMaxResults(1);
		try {
			Object o = q.getSingleResult();
			return ((ApplicationArchive)o);
		} catch( NoResultException nre ) {
			return null;
		}
	}
	
	@Override
	public ApplicationArchive findApplicationArchiveByHashAndAlgorithm(Application app, String hash, String hashAlgorithm) {
		Query q = entityManager.createQuery("select distinct ar "
				+"from ApplicationArchive ar "
				+"join fetch ar.application app "
				+"where ar.hash=:hash "
				+"and ar.hashAlgorithm=:hashAlgorithm "
				+"and app.id=:appId");
		q.setParameter("hash", hash);
		q.setParameter("hashAlgorithm", hashAlgorithm);
		q.setParameter("appId",app.getId());
		q.setMaxResults(1);
		try {
			ApplicationArchive o = (ApplicationArchive)q.getSingleResult();
			return (ApplicationArchive)o;
		} catch( NoResultException nre ) {
			return null;
		}
	}
	
	@Override
	public int countDeploymentsByHashAndHashAlg(String hash, String hashAlg) {
		Query q = entityManager.createQuery("select count(d) "
				+"from Deployment d "
				+"left join d.applicationArchive aa "
				+"where aa.hash=:hash "
				+"and aa.hashAlgorithm=:hashAlgorithm");
		q.setParameter("hash", hash);
		q.setParameter("hashAlgorithm", hashAlg);
		try {
			Number ret = (Number)q.getSingleResult();
			return ret.intValue();
		} catch( NoResultException nre ) {
			return 0;
		}
	}
	
	@Override
	public int countVersionsByHashAndHashAlg(String hash, String hashAlg) {
		Query q = entityManager.createQuery("select count(av) "
				+"from ApplicationVersion av "
				+"left join av.archive ar "
				+"where ar.hash=:hash "
				+"and ar.hashAlgorithm=:hashAlgorithm");
		q.setParameter("hash", hash);
		q.setParameter("hashAlgorithm", hashAlg);
		try {
			Number ret = (Number)q.getSingleResult();
			return ret.intValue();
		} catch( NoResultException nre ) {
			return 0;
		}
	}
	
	@Override
	public int countApplicationArchivesByHashAndHashAlg(String hash, String hashAlg) {
		Query q = entityManager.createQuery("select count(ar) "
				+"from ApplicationArchive ar "
				+"where ar.hash=:hash "
				+"and ar.hashAlgorithm=:hashAlgorithm ");
		q.setParameter("hash", hash);
		q.setParameter("hashAlgorithm", hashAlg);
		try {
			Number ret = (Number)q.getSingleResult();
			return ret.intValue();
		} catch( NoResultException nre ) {
			return 0;
		}
	}
	
	@Override
	public <E extends ModelEntity, T extends ModelEntity> List<T> getOrdered(E entity, String listMethod, Comparator<T> comparator) {
		EntityManager entityManager = getEntityManager(); 
		entityManager.getTransaction().begin();
		entityManager.merge(entity);
		List<T> ents;
		try {
			ents = (List<T>) entity.getClass().getMethod(listMethod).invoke(entity);
		} catch (Exception e) {
			throw new PersistenceException(e);
		}
		Collections.sort( ents, comparator );
		entityManager.getTransaction().commit();
		return ents;
	}
	
	// ACCESSORS
	
	public void setEntityManager(EntityManager manager) {
		entityManager = manager;
	}
	public EntityManager getEntityManager() {
		return entityManager;
	}
	
	/**
	 * Amount of time to wait between each refresh retry.
	 */
	public void setRefreshRetryInterval(int refreshRetryInterval) {
		this.refreshRetryInterval = refreshRetryInterval;
	}

	/**
	 * Number of times to retry refreshing a model entity before failing the operation.
	 * Using SQLite, sometimes the database file is locked by another process.
	 */
	public void setNumberOfRefreshRetries(int numberOfRefreshRetries) {
		this.numberOfRefreshRetries = numberOfRefreshRetries;
	}
	
	// PRIVATE METHODS
	
	private <T extends ModelEntity> void _delete(T entity, List<ProcessingEvent> events) throws PersistenceException {		
		// give the event notifiers an opportunity to act, prior to deletion
		this._refresh(entity);
		entity.remove();
		entityManager.remove(entity);
	}
	
	private void _refresh(ModelEntity obj2Refresh) {
		if( !entityManager.contains(obj2Refresh) ) {
			entityManager.merge(obj2Refresh);
		}
		entityManager.refresh(obj2Refresh);
	}

	@Override
	public ModelService begin() {
		entityManager.getTransaction().begin();
		return this;
	}

	@Override
	public ModelService commit() {
		entityManager.getTransaction().commit();
		return this;
	}

	@Override
	public ModelService rollback() {
		if( entityManager.isOpen() && entityManager.getTransaction().isActive() ) {
			entityManager.getTransaction().rollback();
		}
		return this;
	}
}
