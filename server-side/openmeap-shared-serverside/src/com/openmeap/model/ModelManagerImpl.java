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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipFile;

import javax.persistence.PersistenceException;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.openmeap.AuthorizationException;
import com.openmeap.Authorizer;
import com.openmeap.event.Event;
import com.openmeap.event.EventNotificationException;
import com.openmeap.event.MessagesEvent;
import com.openmeap.event.ProcessingEvent;
import com.openmeap.model.dto.Application;
import com.openmeap.model.dto.ApplicationArchive;
import com.openmeap.model.dto.ApplicationVersion;
import com.openmeap.model.dto.ClusterNode;
import com.openmeap.model.dto.Deployment;
import com.openmeap.model.dto.GlobalSettings;
import com.openmeap.model.event.ModelEntityEvent;
import com.openmeap.util.Utils;
import com.openmeap.util.ZipUtils;

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
	public <T extends ModelEntity> void refresh(T obj2Refresh, List<ProcessingEvent> events) throws PersistenceException {
		callEventNotifiers(ModelServiceOperation.REFRESH,obj2Refresh,events);
		modelService.refresh(obj2Refresh);
	}
	
	@Override
	public <T extends ModelEntity> void delete(T entity, List<ProcessingEvent> events) {
		
		if( ApplicationVersion.class.isAssignableFrom(entity.getClass()) ) {
			deleteApplicationVersion((ApplicationVersion)entity,events);
			return; 
		} else if( Application.class.isAssignableFrom(entity.getClass()) ) {
			deleteApplication((Application)entity,events);
			return;
		} else {
			_delete(entity,events);
			return;
		}
	}
	
	public void deleteApplicationVersion(ApplicationVersion version, List<ProcessingEvent> events) {
		
		ApplicationArchive archive2Delete = null;
		if( version.getArchive()!=null ) {
			archive2Delete = new ApplicationArchive();
			archive2Delete.setHash(version.getArchive().getHash());
			archive2Delete.setHashAlgorithm(version.getArchive().getHashAlgorithm());
		}
		
		Boolean versionInUse = false;
		if( version.getApplication().getDeployments()!=null ) {
			for( Deployment depl : version.getApplication().getDeployments() ) {
				if( depl.getApplicationVersion().getPk().equals(version.getPk()) ) {
					versionInUse = true;
				}
			}
		}
		if( versionInUse ) {
		
			version.setActiveFlag(false);
			try {
				version = addModify(version,events);
				refresh(version.getApplication(),events);
				events.add( new MessagesEvent("Application version successfully set to inactive.  It will be deleted when the last deployment made with it is removed.") );
			} catch( InvalidPropertiesException ipe ) {
				events.add( new MessagesEvent(ipe.getMessage()) );
			} catch( PersistenceException pe ) {
				events.add( new MessagesEvent(pe.getMessage()) );								
			}
		} else {
			version.getApplication().removeVersion(version);
			modelService.delete(version);
			if( archive2Delete!=null ) {
				maintainFileSystemCleanliness(archive2Delete, events);
			}
			events.add( new MessagesEvent("Application version successfully deleted!") );
		}
		version=null;
	}
	
	public void deleteApplication(Application app, List<ProcessingEvent> events) throws PersistenceException {
		
		// flip all the versions to inactive, so they don't prevent archive deletion
		for( ApplicationVersion appVer : app.getVersions().values() ) {
			appVer.setActiveFlag(false);
			try {
				addModify(appVer,events);
			} catch (InvalidPropertiesException e) {
				throw new PersistenceException(e);
			}
		}
		
		// call the event notifiers on each deployment that will be deleted
		Iterator iterator = app.getDeployments().iterator();
		List<Deployment> depls = new ArrayList<Deployment>();
		while(iterator.hasNext()) {
			Deployment depl = (Deployment)iterator.next();
			depls.add(depl);
		}
		iterator = depls.iterator();
		while(iterator.hasNext()) {
			Deployment depl = (Deployment)iterator.next();
			app.removeDeployment(depl);
			delete(depl,events);
		}
		
		// iterate over each version, deleting each
		List<ApplicationVersion> appVers = new ArrayList<ApplicationVersion>();
		for( ApplicationVersion appVer : app.getVersions().values() ) {
			appVers.add(appVer);
		}
		for( ApplicationVersion appVer : appVers ) {
			delete(appVer,events);
			//app.removeVersion(appVer);
		}
		
		modelService.delete(app);
	}
	
	@Override
	public <T extends ModelEntity> T addModify(T entity, List<ProcessingEvent> events) throws InvalidPropertiesException, PersistenceException {
		if( ApplicationVersion.class.isAssignableFrom(entity.getClass()) ) {
			return (T) addModifyApplicationVersion((ApplicationVersion)entity,events);
		} else if( GlobalSettings.class.isAssignableFrom(entity.getClass()) ) {
			return (T) addModifyGlobalSettings((GlobalSettings)entity,events);
		} else if( Deployment.class.isAssignableFrom(entity.getClass()) ) {
			return (T) addModifyDeployment((Deployment)entity,events);
		} else return (T) _addModify(entity,events);
	}
	
	public Deployment addModifyDeployment(Deployment deployment, List<ProcessingEvent> events) throws InvalidPropertiesException, PersistenceException {
		authorizeAndValidate(deployment,determineCreateUpdateAction(deployment));
		Application app = deployment.getApplication();
		maintainDeploymentHistoryLength(app,events);
		return (Deployment)_addModify((ModelEntity)deployment,events);
	}
	
	public ApplicationVersion addModifyApplicationVersion(ApplicationVersion version, List<ProcessingEvent> events) throws InvalidPropertiesException, PersistenceException {
		if( version.getArchive()!=null && version.getArchive().getNewFileUploaded() ) {
			if( processApplicationArchiveFileUpload(version.getArchive(),events)==null ) {
				throw new PersistenceException("Zip archive failed to process!");
			}
		}
		ApplicationVersion ret = (ApplicationVersion)_addModify((ModelEntity)version, events);
		modelService.refresh(version.getApplication());
		return ret;
	}
	
	public GlobalSettings addModifyGlobalSettings(GlobalSettings settings, List<ProcessingEvent> events) throws InvalidPropertiesException, PersistenceException {
		
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
	
	/*
	 * PRIVATE METHODS
	 */
	
	public <T extends ModelEntity> void _delete(T entity, List<ProcessingEvent> events) {
		
		if( ! getAuthorizer().may(Authorizer.Action.DELETE, entity) ) {
			throw new PersistenceException(new AuthorizationException("The user logged in does not have permissions to DELETE "+entity.getClass().getSimpleName()+" objects."));
		}
		callEventNotifiers(ModelServiceOperation.DELETE,entity,events);
		
		modelService.delete(entity);
	}
	
	public <T extends ModelEntity> T _addModify(T entity, List<ProcessingEvent> events) throws InvalidPropertiesException, PersistenceException {
		
		authorizeAndValidate(entity,determineCreateUpdateAction(entity));
		T o = modelService.saveOrUpdate(entity);
		callEventNotifiers(ModelServiceOperation.SAVE_OR_UPDATE,o,events);
		return o;
	}
	
	private void callEventNotifiers(ModelServiceOperation op, ModelEntity obj2ActOn, List<ProcessingEvent> events) {
		// if there are any web-servers out there to notify of the update, then do so
		if( eventNotifiers!=null ) {
			for( ModelServiceEventNotifier handler : eventNotifiers ) {
				try {
					if( handler.notifiesFor(op,obj2ActOn) ) {
						handler.notify( new ModelEntityEvent(op,obj2ActOn), events );
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
	}
	
	private void maintainFileSystemCleanliness(ApplicationArchive archive, List<ProcessingEvent> events) {
		
		GlobalSettings settings = getGlobalSettings();
		
		// check to see if any other archives have this hash and md5
		List<ApplicationArchive> archives = modelService
				.findApplicationArchivesByHashAndAlgorithm(archive.getHash(), archive.getHashAlgorithm());
		
		// either more than one archive has this file
		Boolean archiveIsInUseElsewhere = 
				archives!=null 
				&& (
						archives.size()>1
						|| (
								archives.size()==1 
								&& !archives.get(0).getPk().equals(archive.getPk())
						)
				);
		
		if( !archiveIsInUseElsewhere ) {
			
			// delete the web-view
			try {
				File oldExplodedPath = archive.getExplodedPath(settings.getTemporaryStoragePath());
				if( oldExplodedPath!=null && oldExplodedPath.exists() ) {
					FileUtils.deleteDirectory(oldExplodedPath);
				}
			} catch( IOException ioe ) {
				logger.error("There was an exception deleting the old web-view directory: {}",ioe);
				events.add(new MessagesEvent(String.format("Upload process will continue.  There was an exception deleting the old web-view directory: %s",ioe.getMessage())));
			}
			
			// delete the zip file
			File originalFile = archive.getFile(settings.getTemporaryStoragePath());
			if( originalFile.exists() && !originalFile.delete() ) {
				String mesg = String.format("Failed to delete old file %s, was different so proceeding anyhow.",originalFile.getName());
				logger.error(mesg);
				events.add(new MessagesEvent(mesg));
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private ApplicationArchive processApplicationArchiveFileUpload(ApplicationArchive archive, List<ProcessingEvent> events) {
		
		GlobalSettings settings = getGlobalSettings();
		File tempFile = new File(archive.getHash());
		Long size = tempFile.length();
		
		String pathError = settings.validateTemporaryStoragePath();
		if( pathError!=null ) {
			logger.error("There is an issue with the global settings temporary storage path: "+settings.validateTemporaryStoragePath()+"\n {}",pathError);
			events.add( new MessagesEvent("There is an issue with the global settings temporary storage path: "+settings.validateTemporaryStoragePath()+" - "+pathError) );
			return null;
		}
		
		// determine the md5 hash of the uploaded file
		// and rename the temp file by the hash
		FileInputStream is = null;
		File destinationFile = null;
		try {	
			String hashValue = null;
			try {
				is = new FileInputStream(tempFile);
				hashValue = Utils.hashInputStream("MD5", is);
			} finally {
				is.close();
			}
			
			// if the archive is pre-existing and not the same,
			// then determine if the web-view and zip should be deleted
			// that is, they are not used by any other versions
			if( archive.getId()!=null && !archive.getHash().equals(hashValue) ) {

				maintainFileSystemCleanliness(archive,events);
			}
			
			archive.setHashAlgorithm("MD5");
			archive.setHash(hashValue);
			
			destinationFile = archive.getFile(settings.getTemporaryStoragePath());
			
			// if the upload destination exists, then try to delete and overwrite
			// even though they are theoretically the same.
			if( destinationFile.exists() && !destinationFile.delete() ) {
				String mesg = String.format("Failed to delete old file (theoretically the same anyways, so proceeding) %s",destinationFile.getName());
				logger.error(mesg);
				events.add(new MessagesEvent(mesg));
				if( ! tempFile.delete() ) {
					mesg = String.format("Failed to delete temporary file %s",tempFile.getName());
					logger.error(mesg);
					events.add(new MessagesEvent(mesg));	
				}
			}
			
			// if it didn't exist or it was successfully deleted,
			// then rename the upload to our destination and unzip it
			// into the web-view directory
			else if( tempFile.renameTo(destinationFile) ) {
				
				String mesg = String.format("Uploaded temporary file %s successfully renamed to %s",tempFile.getName(),destinationFile.getName());
				logger.debug(mesg);
				events.add(new MessagesEvent(mesg));
				unzipFile(archive,destinationFile,events);
			} else {
				String mesg = String.format("Failed to renamed file %s to %s",tempFile.getName(),destinationFile.getName());
				logger.error(mesg);
				events.add(new MessagesEvent(mesg));
				return null;
			}
		} catch(IOException ioe) {
			events.add(new MessagesEvent(ioe.getMessage()));
			return null;
		} catch(NoSuchAlgorithmException nsae) {
			events.add(new MessagesEvent(nsae.getMessage()));
			return null;
		} 

		// determine the compressed and uncompressed size of the zip archive
		try {
			archive.setBytesLength(size.intValue());
			ZipFile zipFile = null;
			try {
				zipFile = new ZipFile(destinationFile);
				Integer uncompressedSize = ZipUtils.getUncompressedSize(zipFile).intValue();
				archive.setBytesLengthUncompressed(new Long(uncompressedSize).intValue());
			} finally {
				if(zipFile!=null) {
					zipFile.close();
				}
			}
		} catch( IOException ioe ) {
			logger.error("An exception occurred while calculating the uncompressed size of the archive: {}",ioe);
			events.add(new MessagesEvent(String.format("An exception occurred while calculating the uncompressed size of the archive: %s",ioe.getMessage())));
			return null;
		}
		
		archive.setUrl(ApplicationArchive.URL_TEMPLATE);
		archive.setNewFileUploaded(true);
		
		return archive;
	}
	
	/**
	 * 
	 * @param archive
	 * @param zipFile
	 * @param events
	 * @return TRUE if the file successfully is exploded, FALSE otherwise.
	 */
	private Boolean unzipFile(ApplicationArchive archive, File zipFile, List<ProcessingEvent> events) {
		try {
			GlobalSettings settings = getGlobalSettings();
			File dest = archive.getExplodedPath(settings.getTemporaryStoragePath());
			if( dest.exists() ) {
				FileUtils.deleteDirectory(dest);
			}
			ZipFile file = null;
			try {
				file = new ZipFile(zipFile);
				ZipUtils.unzipFile(file, dest);
			} finally {
				file.close();
			}
			return Boolean.TRUE;
		} catch( Exception e ) {
			logger.error("An exception occurred unzipping the archive to the viewing location: {}",e);
			events.add(new MessagesEvent(String.format("An exception occurred unzipping the archive to the viewing location: %s",e.getMessage())));
		}
		return Boolean.FALSE;
	}
	
	/**
	 * Trim the deployment history table.  Deleting old archives as we go.
	 * @param app
	 * @throws PersistenceException 
	 * @throws InvalidPropertiesException 
	 */
	private Boolean maintainDeploymentHistoryLength(Application app,List<ProcessingEvent> events) throws InvalidPropertiesException, PersistenceException {
		
		Integer lengthToMaintain = app.getDeploymentHistoryLength();
		List<Deployment> deployments = app.getDeployments();
		if( deployments!=null && deployments.size() > lengthToMaintain ) {
			
			Integer currentSize = deployments.size();
			
			List<Deployment> newDeployments = new ArrayList<Deployment>(deployments.subList(currentSize-lengthToMaintain,currentSize));
			List<Deployment> oldDeployments = new ArrayList<Deployment>(deployments.subList(0,currentSize-lengthToMaintain));
			
			for( Deployment deployment : oldDeployments ) {
				delete(deployment,events);
			}
			
			for( Deployment deployment : newDeployments ) {
				app.getDeployments().add(deployment);
			}
			
			addModify(app,events);
			
			return true;
		}
		return false;
	}
	
	private Authorizer.Action determineCreateUpdateAction(ModelEntity entity) {
		Authorizer.Action action = Authorizer.Action.MODIFY;
		if( entity.getPk()==null ) {
			action = Authorizer.Action.CREATE;
		}
		return action;
	}
	
	private void authorizeAndValidate(ModelEntity entity, Authorizer.Action action) throws InvalidPropertiesException {
		
		if( ! getAuthorizer().may(action, entity) ) {
			throw new PersistenceException(new AuthorizationException("The user logged in does not have permissions to "
					+ action.toString() + " the " + entity.getClass().getSimpleName()));
		}
		
		Map<Method,String> errors = entity.validate();
		if( errors!=null ) {
			throw new InvalidPropertiesException(entity,errors);
		}
	}
}
