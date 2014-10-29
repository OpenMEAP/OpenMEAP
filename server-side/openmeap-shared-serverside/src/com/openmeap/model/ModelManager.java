/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2015 OpenMEAP, Inc.                                   #
 #    Credits to Jonathan Schang & Rob Thacher                                 #
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
import java.util.List;

import javax.persistence.PersistenceException;

import com.openmeap.Authorizer;
import com.openmeap.event.Event;
import com.openmeap.event.ProcessingEvent;
import com.openmeap.model.dto.Application;
import com.openmeap.model.dto.ApplicationInstallation;
import com.openmeap.model.dto.ApplicationVersion;
import com.openmeap.model.dto.ClusterNode;
import com.openmeap.model.dto.Deployment;
import com.openmeap.model.dto.GlobalSettings;
import com.openmeap.model.event.notifier.ModelServiceEventNotifier;

/**
 * Interface to handle the business rules of managing ModelEntity objects.
 * 
 * All business rules specifically related to CRUD operations on any ModelEntity object
 * should be performed by the implementing class of this interface.
 * 
 * @author schang
 */
public interface ModelManager {
	
	/**
	 * @param notifiers event notifiers to trigger when certain operations are beginning or completed
	 */
	void setEventNotifiers(Collection<ModelServiceEventNotifier> notifiers);
	Collection<ModelServiceEventNotifier> getEventNotifiers();
	
	/**
	 * @param auth The authorization mechanism to use
	 */
	void setAuthorizer(Authorizer auth);
	Authorizer getAuthorizer();
	
	/**
	 * @param service Used by the methods within to access persistence
	 */
	void setModelService(ModelService service);
	ModelService getModelService();
	
	/**
	 * Refresh the entity passed in from the database.
	 * 
	 * @param <T> 
	 * @param entity The model entity.
	 * @param events The list of events being generated.  These are actually processed by the caller, generally the web-tier.
	 * @return this, for chaining together commands in a more compact fashion.
	 * @throws PersistenceException
	 */
	public <T extends ModelEntity> ModelManager refresh(T entity, List<ProcessingEvent> events) throws PersistenceException;
	
	/**
	 * Delete a Model Entity
	 * 
	 * @param entity The entity to delete.
	 * @param events The list of events being generated.  These are actually processed by the caller, generally the web-tier.
	 * @return The entity returned by the entity manager after persist.
	 * @throws PersistenceException
	 */
	<T extends ModelEntity> ModelManager delete(T entity, List<ProcessingEvent> events) throws PersistenceException;
	
	/**
	 * Save or update a Model Entity
	 * 
	 * @param entity The entity to add/modify.
	 * @param events The list of events being generated.  These are actually processed by the caller, generally the web-tier.
	 * @return The entity returned by the entity manager after persist.
	 * @throws InvalidPropertiesException when the object's validate() method fails
	 * @throws PersistenceException
	 */
	<T extends ModelEntity> T addModify(T entity, List<ProcessingEvent> events) throws InvalidPropertiesException, PersistenceException;
	
	/**
	 * Get the settings for the installation.
	 * @return The global settings and cluster nodes of the setup
	 */
	GlobalSettings getGlobalSettings();
	
	/**
	 * @return The cluster node of this services war instance, else null if the admin war
	 */
	ClusterNode getClusterNode();
	
	ModelManager begin();
	ModelManager commit();
	ModelManager commit(List<ProcessingEvent> events);
	void rollback();
}
