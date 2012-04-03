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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.PersistenceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.openmeap.AuthorizationException;
import com.openmeap.Authorizer;
import com.openmeap.event.Event;
import com.openmeap.event.EventNotificationException;
import com.openmeap.event.ProcessingEvent;
import com.openmeap.model.dto.Application;
import com.openmeap.model.dto.ApplicationVersion;
import com.openmeap.model.dto.ClusterNode;
import com.openmeap.model.dto.GlobalSettings;
import com.openmeap.model.event.ModelEntityEvent;

/**
 * Handles all business logic related to the model Entity objects. 
 * @author schang
 */
public class ModelManagerImpl implements ModelManager, ApplicationContextAware {

	private Collection<ModelServiceEventNotifier> eventNotifiers = null;
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
	
	@Override
	public <T extends ModelEntity> void refresh(T obj2Refresh) throws PersistenceException {
		modelService.refresh(obj2Refresh);
		callEventNotifiers(ModelServiceOperation.REFRESH,obj2Refresh,null);
	}
	
	public <T extends ModelEntity> void delete(T o, List<ProcessingEvent> events) {
		if( ! getAuthorizer().may(Authorizer.Action.DELETE, o) ) {
			throw new PersistenceException(new AuthorizationException("The user logged in does not have permissions to DELETE "+o.getClass().getSimpleName()+" objects."));
		}
		callEventNotifiers(ModelServiceOperation.DELETE,o,events);
		modelService.delete(o);
	}
	
	public void delete(Application app, List<ProcessingEvent> events) {
		
		if( ! getAuthorizer().may(Authorizer.Action.DELETE, app) ) {
			throw new PersistenceException(new AuthorizationException("The user logged in does not have permissions to DELETE Application objects."));
		}
		callEventNotifiers(ModelServiceOperation.DELETE,app,events);
		modelService.delete(app);
	}
	
	public <T extends ModelEntity> T addModify(T entity, List<ProcessingEvent> events) throws InvalidPropertiesException, PersistenceException {
		
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
		T o = modelService.saveOrUpdate(entity);
		callEventNotifiers(ModelServiceOperation.SAVE_OR_UPDATE,o,events);
		return o;
	}
	
	public ApplicationVersion addModify(ApplicationVersion version, List<ProcessingEvent> events) throws InvalidPropertiesException, PersistenceException {
		ApplicationVersion ret = (ApplicationVersion)addModify((ModelEntity)version, events);
		modelService.refresh(version.getApplication());
		return ret;
	}
	
	public GlobalSettings addModify(GlobalSettings settings, List<ProcessingEvent> events) throws InvalidPropertiesException, PersistenceException {
		if( settings==null || settings.getId()==null || !settings.getId().equals(Long.valueOf(1)) ) {
			throw new PersistenceException("There can be only 1 instance of GlobalSettings.  "
					+ "Please first acquire with modelManager.getGlobalSettings(), make modifications, then update.");
		}
		
		if( ! getAuthorizer().may(Authorizer.Action.MODIFY, settings) ) {
			throw new PersistenceException(new AuthorizationException("The current user does not have sufficient privileges to modify the global settings."));
		}
		
		settings = modelService.saveOrUpdate(settings);
		callEventNotifiers(ModelServiceOperation.SAVE_OR_UPDATE,settings,events);
		
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
			try {
				Map<String,String> servicesWebProperties = (Map<String,String>)context.getBean("openmeapServicesWebPropertiesMap");
				String serviceUrl = (String)servicesWebProperties.get("clusterNodeUrlPrefix");
				return this.getGlobalSettings().getClusterNodes().get(serviceUrl);
			} catch(Exception e) {
				logger.warn("{}",e);
			}
		}
		return null;
	}
	
	/*
	 * GETTERS/SETTERS
	 */
	
	public void setEventNotifiers(Collection<ModelServiceEventNotifier> handlers) {
		eventNotifiers = handlers;
	}
	public Collection<ModelServiceEventNotifier> getEventNotifiers() {
		return eventNotifiers;
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
	
	private void callEventNotifiers(ModelServiceOperation op, ModelEntity obj2ActOn, List<ProcessingEvent> events) {
		// if there are any web-servers out there to notify of the update,
		// then do so
		if( eventNotifiers!=null ) {
			for( ModelServiceEventNotifier handler : eventNotifiers ) {
				try {
					if( handler.notifiesFor(op,obj2ActOn) ) {
						handler.notify( new ModelEntityEvent(op,obj2ActOn), events );
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
