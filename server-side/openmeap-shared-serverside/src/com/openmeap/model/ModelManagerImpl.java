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
import java.util.Map;
import java.util.UUID;
import java.lang.reflect.Method;

import javax.persistence.PersistenceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.openmeap.AuthorizationException;
import com.openmeap.Authorizer;
import com.openmeap.model.dto.Application;
import com.openmeap.model.dto.ApplicationInstallation;
import com.openmeap.model.dto.ApplicationVersion;
import com.openmeap.model.dto.ClusterNode;
import com.openmeap.model.dto.Deployment;
import com.openmeap.model.dto.GlobalSettings;

/**
 * Handles all business logic related to the model Entity objects. 
 * @author schang
 */
public class ModelManagerImpl implements ModelManager, ApplicationContextAware {

	private ModelService modelService;
	private Logger logger = LoggerFactory.getLogger(ModelManagerImpl.class);
	private ApplicationContext context = null;
	private Authorizer authorizer = new Authorizer() {
			@Override public Boolean may(Action action, Object object) {
				return Boolean.TRUE;
			}
		};
	
	public ModelManagerImpl() {}
	public ModelManagerImpl(ModelService service) {
		setModelService(service);
	}
	
	public void setAuthorizer(Authorizer auth) {
		this.authorizer=auth;
	}
	public Authorizer getAuthorizer() {
		return authorizer;
	}
	
	public void setModelService(ModelService service) {
		modelService = service;
	}
	public ModelService getModelService() {
		return modelService;
	}
	
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		context = applicationContext;
	}
	
	public void delete(Application app) {
		if( ! getAuthorizer().may(Authorizer.Action.DELETE, app) ) {
			throw new PersistenceException(new AuthorizationException("The user logged in does not have permissions to DELETE Application objects."));
		}
		modelService.delete(app);
	}
	
	protected ModelEntity addModify(ModelEntity entity) throws PersistenceException, InvalidPropertiesException {
		
		Authorizer.Action action = Authorizer.Action.MODIFY;
		
		if( entity.getPk()==null ) {
			action = Authorizer.Action.CREATE;
		}
		
		if( ! getAuthorizer().may(action, entity) ) {
			throw new PersistenceException(new AuthorizationException("The user logged in does not have permissions to "+action.toString()+" the "+entity.getClass().getSimpleName()));
		}
			
		Map<Method,String> errors = entity.validate();
		if( errors!=null ) {
			throw new InvalidPropertiesException(entity,errors);
		}
		return modelService.saveOrUpdate(entity);
	}
	
	public Application addModify(Application application) throws PersistenceException, InvalidPropertiesException {
		return (Application)addModify((ModelEntity)application);
	}
	public ApplicationInstallation addModify(ApplicationInstallation appInst) throws PersistenceException, InvalidPropertiesException {
		return (ApplicationInstallation)addModify((ModelEntity)appInst);
	}
	public ApplicationVersion addModify(ApplicationVersion version) throws InvalidPropertiesException, PersistenceException {
		ApplicationVersion ret = (ApplicationVersion)addModify((ModelEntity)version);
		modelService.refresh(version.getApplication());
		return ret;
	}
	
	public GlobalSettings addModify(GlobalSettings settings) throws InvalidPropertiesException, PersistenceException {
		if( settings==null || settings.getId()==null || !settings.getId().equals(Long.valueOf(1)) ) {
			throw new PersistenceException("There can be only 1 instance of GlobalSettings.  "
					+ "Please first acquire with modelManager.getGlobalSettings(), make modifications, then update.");
		}
		
		if( ! getAuthorizer().may(Authorizer.Action.MODIFY, settings) ) {
			throw new PersistenceException(new AuthorizationException("The current user does not have sufficient privileges to modify the global settings."));
		}
		
		settings = modelService.saveOrUpdate(settings);
		modelService.refresh(settings);
		return settings;
	}
	
	public GlobalSettings getGlobalSettings() {
		GlobalSettings settings = modelService.findByPrimaryKey(GlobalSettings.class,(Long)1L);
		if( settings==null ) {
			settings = new GlobalSettings();
			settings.setServiceManagementAuthSalt(UUID.randomUUID().toString());
			settings = modelService.saveOrUpdate(settings);
			modelService.refresh(settings);
		}
		if( settings.getServiceManagementAuthSalt()==null || settings.getServiceManagementAuthSalt().trim().length()==0 ) {
			settings.setServiceManagementAuthSalt(UUID.randomUUID().toString());
			settings = modelService.saveOrUpdate(settings);
			modelService.refresh(settings);
		}
		return settings;
	}
	
	public ClusterNode getClusterNode() {
		if( context!=null ) {
			Map<String,String> servicesWebProperties = (Map<String,String>)context.getBean("openmeapServicesWebPropertiesMap");
			String serviceUrl = (String)servicesWebProperties.get("clusterNodeUrlPrefix");
			return this.getGlobalSettings().getClusterNodes().get(serviceUrl);
		} else {
			return null;
		}
	}
}
