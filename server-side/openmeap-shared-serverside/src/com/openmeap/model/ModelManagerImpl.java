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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipFile;

import javax.persistence.PersistenceException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.openmeap.AuthorizationException;
import com.openmeap.Authorizer;
import com.openmeap.event.EventNotificationException;
import com.openmeap.event.MessagesEvent;
import com.openmeap.event.ProcessingEvent;
import com.openmeap.file.FileOperationException;
import com.openmeap.file.FileOperationManager;
import com.openmeap.model.dto.Application;
import com.openmeap.model.dto.ApplicationArchive;
import com.openmeap.model.dto.ApplicationVersion;
import com.openmeap.model.dto.ClusterNode;
import com.openmeap.model.dto.Deployment;
import com.openmeap.model.dto.GlobalSettings;
import com.openmeap.model.event.ModelEntityEvent;
import com.openmeap.model.event.notifier.ModelServiceEventNotifier;
import com.openmeap.model.event.notifier.ModelServiceEventNotifier.CutPoint;
import com.openmeap.util.Utils;
import com.openmeap.util.ZipUtils;

/**
 * Handles all business logic related to the model Entity objects. 
 * @author schang
 */
public class ModelManagerImpl implements ModelManager, ApplicationContextAware {

	private Collection<ModelServiceEventNotifier> eventNotifiers = null;
	private ModelService modelService;
	private FileOperationManager fileManager;
	private Logger logger = LoggerFactory.getLogger(ModelManagerImpl.class);
	private ApplicationContext context = null;
	private Authorizer authorizer = new Authorizer() {
			@Override public Boolean may(Action action, Object object) {
				return Boolean.TRUE;
			}
		};
	private Map<Thread,List<ModelEntityEvent>> eventQueue = new HashMap<Thread,List<ModelEntityEvent>>();
	
	public ModelManagerImpl() {}
	public ModelManagerImpl(ModelService service) {
		setModelService(service);
	}
	
	@Override
	public ModelManager begin() {
		try {
			fileManager.begin();
		} catch (FileOperationException e) {
			throw new PersistenceException("During transaction begin",e);
		}
		modelService.begin();
		return this;
	}
	
	@Override
	public ModelManager commit() {
		return commit(null);
	}
	
	@Override 
	public ModelManager commit(List<ProcessingEvent> events) {
		
		processModelEntityEventQueue(CutPoint.IN_COMMIT_BEFORE_COMMIT, events);
		
		try {
			fileManager.commit();
		} catch (FileOperationException e) {
			throw new PersistenceException("During file operations commit",e);
		}
		modelService.commit();
		
		processModelEntityEventQueue(CutPoint.IN_COMMIT_AFTER_COMMIT, events);
		clearModelEntityEventQueue();
		
		return this;
	}
	
	@Override
	public void rollback() {
		clearModelEntityEventQueue();
		try {
			fileManager.rollback();
		} catch (FileOperationException e) {
			throw new PersistenceException("During file operations commit",e);
		}
		modelService.rollback();
	}
	
	@Override
	public <T extends ModelEntity> ModelManager refresh(T obj2Refresh, List<ProcessingEvent> events) throws PersistenceException {
		
		ModelEntityEvent event = new ModelEntityEvent(ModelServiceOperation.REFRESH,obj2Refresh);
		
		stashModelEntityEventTillCommit(event);
		callEventNotifiers(CutPoint.BEFORE_OPERATION,event,events);
		
		modelService.refresh(obj2Refresh);
		
		callEventNotifiers(CutPoint.AFTER_OPERATION,event,events);
		
		return this;
	}
	
	@Override
	public <T extends ModelEntity> ModelManager delete(T entity, List<ProcessingEvent> events) {
		
		authorize(entity,Authorizer.Action.DELETE);
		
		ModelEntityEvent event = new ModelEntityEvent(ModelServiceOperation.DELETE,entity);
		
		stashModelEntityEventTillCommit(event);
		callEventNotifiers(CutPoint.BEFORE_OPERATION,event,events);
		
		_delete(entity,events);
		
		callEventNotifiers(CutPoint.AFTER_OPERATION,event,events);
		
		return this;
	}
	
	@Override
	public <T extends ModelEntity> T addModify(T entity, List<ProcessingEvent> events) throws InvalidPropertiesException, PersistenceException {
		
		authorize(entity,determineCreateUpdateAction(entity));
		
		ModelEntityEvent event = new ModelEntityEvent(ModelServiceOperation.SAVE_OR_UPDATE,entity);
		
		stashModelEntityEventTillCommit(event);
		callEventNotifiers(CutPoint.BEFORE_OPERATION,event,events);
		
		T entityToReturn = (T) _addModify(entity,events);
		event.setPayload(entityToReturn);
		validate(entity);
		
		callEventNotifiers(CutPoint.AFTER_OPERATION,event,events);
		
		return entityToReturn;
	}
	
	@Override
	public GlobalSettings getGlobalSettings() {
		GlobalSettings settings = modelService.findByPrimaryKey(GlobalSettings.class,(Long)1L);
		boolean update = false;
		if( settings==null ) {
			settings = new GlobalSettings();
			settings.setServiceManagementAuthSalt(UUID.randomUUID().toString());
			update = true;
		}
		if( settings.getServiceManagementAuthSalt()==null || settings.getServiceManagementAuthSalt().trim().length()==0 ) {
			settings.setServiceManagementAuthSalt(UUID.randomUUID().toString());
			update = true;
		}
		if(update) {
			try {
				modelService.begin();
				settings = modelService.saveOrUpdate(settings);
				modelService.commit();
			} catch(Exception e) {
				modelService.rollback();
				throw new PersistenceException(e);
			}
		}
		return settings;
	}
	
	@Override
	public ClusterNode getClusterNode() {
		if( context!=null ) {
			try {
				Map<String,String> servicesWebProperties = (Map<String,String>)context.getBean("openmeapServicesWebPropertiesMap");
				String serviceUrl = null;
				synchronized(servicesWebProperties) {
					serviceUrl = (String)servicesWebProperties.get("clusterNodeUrlPrefix");
				}
				return this.getGlobalSettings().getClusterNode(serviceUrl);
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
	
	public void setFileManager(FileOperationManager fileManager) {
		this.fileManager = fileManager;
	}
	public FileOperationManager getFileManager() {
		return fileManager;
	}
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		context = applicationContext;
	}
	
	/*
	 * PRIVATE METHODS
	 */
	
	private <T extends ModelEntity> void _delete(T entity, List<ProcessingEvent> events) {
		
		if( ! getAuthorizer().may(Authorizer.Action.DELETE, entity) ) {
			throw new PersistenceException(new AuthorizationException("The user logged in does not have permissions to DELETE "+entity.getClass().getSimpleName()+" objects."));
		}
		modelService.delete(entity);
	}
	
	private <T extends ModelEntity> T _addModify(T entity, List<ProcessingEvent> events) throws InvalidPropertiesException, PersistenceException {
		
		authorizeAndValidate(entity,determineCreateUpdateAction(entity));
		T o = modelService.saveOrUpdate(entity);
		return o;
	}
	
	private void stashModelEntityEventTillCommit(ModelEntityEvent event) {
		List<ModelEntityEvent> modelEvents = eventQueue.get(Thread.currentThread());
		if(modelEvents==null) {
			modelEvents = new ArrayList<ModelEntityEvent>();
			eventQueue.put(Thread.currentThread(),modelEvents);
		}
		if(!modelEvents.contains(event)) {
			modelEvents.add(event);
		}
	}
	
	private void processModelEntityEventQueue(CutPoint cutPoint,List<ProcessingEvent> events) {
		List<ModelEntityEvent> modelEvents;
		if( (modelEvents = eventQueue.get(Thread.currentThread()))!=null ) {
			int size = modelEvents.size();
			for(int i=0;i<size;i++) {
				ModelEntityEvent event = modelEvents.get(i);
				callEventNotifiers(cutPoint,event,events);
			}
		}
	}
	
	private void clearModelEntityEventQueue() {
		List<ModelEntityEvent> modelEvents;
		if( (modelEvents = eventQueue.get(Thread.currentThread()))!=null ) {
			eventQueue.remove(Thread.currentThread());
		}
	}
	
	private void callEventNotifiers(CutPoint cutPoint, ModelEntityEvent event, List<ProcessingEvent> events) {
		
		if(eventNotifiers==null) {
			return;
		}
		
		for( ModelServiceEventNotifier handler : eventNotifiers ) {
			try {
				if( handler.notifiesFor(event.getOperation(),(ModelEntity)event.getPayload()) ) {
					switch(cutPoint) {
					case AFTER_OPERATION:
						handler.onAfterOperation(event, events);
						break;
					case BEFORE_OPERATION:
						handler.onBeforeOperation(event, events);
						break;
					case IN_COMMIT_AFTER_COMMIT:
						handler.onInCommitAfterCommit(event, events);
						break;
					case IN_COMMIT_BEFORE_COMMIT:
						handler.onInCommitBeforeCommit(event, events);
						break;
					}
				}
			} catch( EventNotificationException e ) {
				String msg = String.format("EventNotificationException occurred: %s",e.getMessage());
				logger.error(msg);
				if(events!=null) {
					events.add(new MessagesEvent(msg));
				}
			}
		}
	}
	
	private Authorizer.Action determineCreateUpdateAction(ModelEntity entity) {
		Authorizer.Action action = Authorizer.Action.MODIFY;
		if( entity.getPk()==null ) {
			action = Authorizer.Action.CREATE;
		}
		return action;
	}
	
	private void authorizeAndValidate(ModelEntity entity, Authorizer.Action action) throws InvalidPropertiesException {
		authorize(entity,action);
		validate(entity);
	}
	
	private void authorize(ModelEntity entity, Authorizer.Action action) {
		if( ! getAuthorizer().may(action, entity) ) {
			throw new PersistenceException(new AuthorizationException("The user logged in does not have permissions to "
					+ action.toString() + " the " + entity.getClass().getSimpleName()));
		}
	}
	
	private void validate(ModelEntity entity) throws InvalidPropertiesException {
		Map<Method,String> errors = entity.validate();
		if( errors!=null ) {
			throw new InvalidPropertiesException(entity,errors);
		}
	}
}
