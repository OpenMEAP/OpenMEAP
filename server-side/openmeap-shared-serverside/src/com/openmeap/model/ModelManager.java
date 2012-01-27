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

import java.util.*;

import javax.persistence.PersistenceException;

import com.openmeap.Authorizer;
import com.openmeap.model.dto.Application;
import com.openmeap.model.dto.ApplicationInstallation;
import com.openmeap.model.dto.ApplicationVersion;
import com.openmeap.model.dto.Deployment;
import com.openmeap.model.dto.GlobalSettings;

/**
 * Interface to handle the business rules of managing ModelEntity objects.
 * 
 * All business rules specifically related to CRUD operations on any ModelEntity object
 * should be performed by the implementing class of this interface.
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
	
	List<Application> findAllApplications();
	
	Application findApplication(Long id);
	
	Application findApplicationByName(String name);
	
	ApplicationVersion findApplicationVersion(Long id);
	
	ApplicationInstallation findAppInstByUuid(String uuid);
	
	/**
	 * Find an ApplicationVersion by Application name and ApplicationVersion identifier
	 * 
	 * @param appName The parent Application of the ApplicationVersion desired
	 * @param versionId The version identifier of the ApplicationVersion desired
	 * @return either the ApplicationVersion associated to version id and application name, else null
	 */
	ApplicationVersion findAppVersionByNameAndId(String appName, String versionId);
	
	Deployment getLastDeployment(Application app);
}
