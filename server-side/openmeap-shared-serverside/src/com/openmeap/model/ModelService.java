/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2013 OpenMEAP, Inc.                                   #
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

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import javax.persistence.PersistenceException;

import com.openmeap.model.dto.Application;
import com.openmeap.model.dto.ApplicationArchive;
import com.openmeap.model.dto.ApplicationVersion;
import com.openmeap.model.dto.Deployment;

/**
 * A service interface that provides thin-wrapper access to persistence for the ModelEntity objects. 
 * 
 * @author schang
 */
public interface ModelService {	
		
	/**
	 * Save or update any ModelEntity
	 * @param <T>
	 * @param obj
	 * @throws ModelException 
	 */
	public <T extends ModelEntity> T saveOrUpdate(T obj) throws PersistenceException;
	
	public <T extends ModelEntity> void refresh(T obj2Refresh) throws PersistenceException;
	
	public <T extends ModelEntity> void delete(T obj2Delete) throws PersistenceException;
	
	/*
	 * FIND METHODS
	 */
	
	/**
	 * Find any ModelEntity by the primary key of the class passed in
	 * @param <T>
	 * @param clazz The class of the object to find
	 * @param pk The primary key of the object to find
	 * @return The entity with the primary key value passed in, or null
	 */
	public <T extends ModelEntity> T findByPrimaryKey(Class<T> clazz, Object pk);
	
	/**
	 * Pulls back every instance of a particular ModelEntity.
	 * Implemented to pull back DeviceTypes, but could be used for any of them 
	 * @param <T>
	 * @param clazz
	 * @return every instance of a particular ModelEntity, or null
	 */
	public <T extends ModelEntity> List<T> findAll(Class<T> clazz);

	/*
	 * APPLICATION SPECIFIC
	 */
	
	public Application findApplicationByName(String name);
	public ApplicationVersion findAppVersionByNameAndId(String appName, String identifier);
	public List<Deployment> findDeploymentsByApplication(Application app);
	
	public List<Deployment> findDeploymentsByApplicationArchive(ApplicationArchive archive);
	public List<ApplicationVersion> findVersionsByApplicationArchive(ApplicationArchive archive);
	public ApplicationArchive findApplicationArchiveByHashAndAlgorithm(Application app, String hash, String hashAlgorithm);
	public Deployment getLastDeployment(Application app);
	public ApplicationArchive getApplicationArchiveByDeployment(Deployment depl);
	
	/*
	 * ACROSS ALL APPLICATIONS
	 */
	
	public int countDeploymentsByHashAndHashAlg(String hash, String hashAlg);
	public int countVersionsByHashAndHashAlg(String hash, String hashAlg);
	public int countApplicationArchivesByHashAndHashAlg(String hash, String hashAlg);
	
	public void clearPersistenceContext();
	
	public ModelService begin();
	public ModelService commit();
	public ModelService rollback();
	
	<E extends ModelEntity, T extends ModelEntity> List<T> getOrdered(E entity, String listMethod, Comparator<T> comparator);
}
