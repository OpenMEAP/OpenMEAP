/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011 OpenMEAP, Inc.                                        #
 #    Credits to Jonathan Schang & Robert Thacher                              #
 #                                                                             #
 #    Released under the GPLv3                                                 #
 #                                                                             #
 #    OpenMEAP is free software: you can redistribute it and/or modify         #
 #    it under the terms of the GNU General Public License as published by     #
 #    the Free Software Foundation, either version 3 of the License, or        #
 #    (at your option) any later version.                                      #
 #                                                                             #
 #    OpenMEAP is distributed in the hope that it will be useful,              #
 #    but WITHOUT ANY WARRANTY; without even the implied warranty of           #
 #    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the            #
 #    GNU General Public License for more details.                             #
 #                                                                             #
 #    You should have received a copy of the GNU General Public License        #
 #    along with OpenMEAP.  If not, see <http://www.gnu.org/licenses/>.        #
 #                                                                             #
 ###############################################################################
 */

package com.openmeap.model;

import java.util.Collection;
import java.util.List;

import javax.persistence.PersistenceException;

import com.openmeap.model.dto.Application;
import com.openmeap.model.dto.ApplicationVersion;
import com.openmeap.model.dto.Deployment;

/**
 * A service interface that provides thin-wrapper access to persistence for the ModelEntity objects. 
 * 
 * @author schang
 */
public interface ModelService {	
	
	public void setEventNotifiers(Collection<ModelServiceEventNotifier> notifiers);
	public Collection<ModelServiceEventNotifier> getEventNotifiers();
	
	/**
	 * Save or update any ModelEntity
	 * @param <T>
	 * @param obj
	 * @throws ModelException 
	 */
	public <T extends ModelEntity> T saveOrUpdate(T obj) throws PersistenceException;
	
	public <T extends ModelEntity> void refresh(T obj2Refresh) throws PersistenceException;
	
	/**
	 * Find any ModelEntity by the primary key of the class passed in
	 * @param <T>
	 * @param clazz The class of the object to find
	 * @param pk The primary key of the object to find
	 * @return The entity with the primary key value passed in, or null
	 */
	public <T extends ModelEntity> T findByPrimaryKey(Class<T> clazz, Object pk);
	
	public <T extends ModelEntity> void delete(T obj2Delete) throws PersistenceException;
	
	/**
	 * Pulls back every instance of a particular ModelEntity.
	 * Implemented to pull back DeviceTypes, but could be used for any of them 
	 * @param <T>
	 * @param clazz
	 * @return every instance of a particular ModelEntity, or null
	 */
	public <T extends ModelEntity> List<T> findAll(Class<T> clazz);

	public Application findApplicationByName(String name);
	
	public ApplicationVersion findAppVersionByNameAndId(String appName, String versionId);
	
	public List<Deployment> findDeploymentsByNameAndId(String appName, String versionId);
	
	public Deployment getLastDeployment(Application app);
}
