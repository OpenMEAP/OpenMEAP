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

import java.util.List;

import javax.persistence.PersistenceException;

import com.openmeap.Authorizer;
import com.openmeap.model.dto.Application;
import com.openmeap.model.dto.ApplicationInstallation;
import com.openmeap.model.dto.ApplicationVersion;
import com.openmeap.model.dto.ClusterNode;
import com.openmeap.model.dto.Deployment;
import com.openmeap.model.dto.GlobalSettings;

/**
 * Interface to handle the business rules of managing ModelEntity objects.
 * 
 * All business rules specifically related to CRUD operations on any ModelEntity object
 * should be performed by the implementing class of this interface.
 * 
 * Yes, I realize that all the business logic is currently in the backings...
 * that needs to change.
 * 
 * @author schang
 */
public interface ModelManager {
	
	void setAuthorizer(Authorizer auth);
	Authorizer getAuthorizer();
	
	/**
	 * @param service Used by the methods within to access persistence
	 */
	void setModelService(ModelService service);
	ModelService getModelService();
	
	void delete(Application app);
	
	/**
	 * Save or update an Application object.
	 * 
	 * If the proxyAuthSalt is not set, then this method will assign the Application a new random UUID.
	 * 
	 * @throws InvalidPropertiesException when name, description, or device types are empty
	 * @throws PersistenceException 
	 */
	Application addModify(Application application) throws InvalidPropertiesException, PersistenceException;
	
	ApplicationInstallation addModify(ApplicationInstallation appInst) throws InvalidPropertiesException, PersistenceException;
	
	ApplicationVersion addModify(ApplicationVersion applicationVersion) throws InvalidPropertiesException, PersistenceException;
	
	GlobalSettings addModify(GlobalSettings settings) throws InvalidPropertiesException, PersistenceException;
	
	GlobalSettings getGlobalSettings();
	
	/**
	 * @return The cluster node of this services war instance, else null if the admin war
	 */
	ClusterNode getClusterNode();
}
