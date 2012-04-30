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

package com.openmeap.admin.web.backing;

import static com.openmeap.util.ParameterMapUtils.firstValue;
import static com.openmeap.util.ParameterMapUtils.notEmpty;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

import javax.persistence.PersistenceException;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openmeap.Authorizer;
import com.openmeap.admin.web.events.AddSubNavAnchorEvent;
import com.openmeap.constants.FormConstants;
import com.openmeap.event.MessagesEvent;
import com.openmeap.event.ProcessingEvent;
import com.openmeap.event.ProcessingTargets;
import com.openmeap.model.InvalidPropertiesException;
import com.openmeap.model.ModelManager;
import com.openmeap.model.ModelServiceOperation;
import com.openmeap.model.dto.Application;
import com.openmeap.model.dto.ApplicationArchive;
import com.openmeap.model.dto.ApplicationVersion;
import com.openmeap.model.dto.Deployment;
import com.openmeap.model.dto.GlobalSettings;
import com.openmeap.model.event.ModelEntityEvent;
import com.openmeap.model.event.notifier.ArchiveFileUploadNotifier;
import com.openmeap.protocol.dto.HashAlgorithm;
import com.openmeap.util.ParameterMapUtils;
import com.openmeap.util.ServletUtils;
import com.openmeap.util.Utils;
import com.openmeap.util.ZipUtils;
import com.openmeap.web.AbstractTemplatedSectionBacking;
import com.openmeap.web.GenericProcessingEvent;
import com.openmeap.web.ProcessingContext;
import com.openmeap.web.ProcessingUtils;
import com.openmeap.web.html.Anchor;
import com.openmeap.web.html.Option;

// TODO: there are way to many things going on in this class
public class AddModifyApplicationVersionBacking extends AbstractTemplatedSectionBacking {

	private Logger logger = LoggerFactory.getLogger(AddModifyApplicationVersionBacking.class);
	
	private static String PROCESS_TARGET = ProcessingTargets.ADDMODIFY_APPVER;
	
	private ModelManager modelManager = null;
	private ArchiveFileUploadNotifier archiveUploadNotifier = null;
	
	public AddModifyApplicationVersionBacking() {
		setProcessingTargetIds(Arrays.asList(new String[]{PROCESS_TARGET}));
	}
	
	/**
	 * With the first of the bean name matching "addModifyApp", there are
	 * three ways to access this:
	 *    - request has applicationId and processTarget - modifying an application
	 *    - request has applicationId only              - pulling up an application to modify
	 *    - request has processTarget only              - submitting a brand new application  
	 *
	 * See the WEB-INF/ftl/form-application-addmodify.ftl for input/output parameters.
	 *    
	 * @param context Not referenced at all, may be null
	 * @param templateVariables Variables output to for the view
	 * @param parameterMap Parameters passed in to drive processing
	 * @return on errors, returns an array of error processingevents
	 * @see TemplatedSectionBacking::process()
	 */
	public Collection<ProcessingEvent> process(ProcessingContext context, Map<Object,Object> templateVariables, Map<Object, Object> parameterMap) {
		
		List<ProcessingEvent> events = new ArrayList<ProcessingEvent>();
		Application app = null;
		ApplicationVersion version = null;
		
		// make sure we're configured to accept uploads, warn otherwise
		validateStorageConfiguration(templateVariables,events);
		
		// we must have an application in order to add a version
		if( ! notEmpty(FormConstants.APP_ID, parameterMap) ) {
			return ProcessingUtils.newList(new GenericProcessingEvent<String>(ProcessingTargets.MESSAGES,"An application must be specified in order to add a version"));
		}
		Long appId = Long.valueOf( firstValue(FormConstants.APP_ID, parameterMap) );
		app = modelManager.getModelService().findByPrimaryKey(Application.class, appId );
		if( app==null ) {
			return ProcessingUtils.newList(new GenericProcessingEvent<String>(ProcessingTargets.MESSAGES,"The application with id "+appId+" could not be found."));
		}
		
		events.add( new AddSubNavAnchorEvent(new Anchor("?bean=addModifyAppPage&applicationId="+app.getId(),"View/Modify Application","View/Modify Application")) );
		events.add( new AddSubNavAnchorEvent(new Anchor("?bean=appVersionListingsPage&applicationId="+app.getId(),"Version Listings","Version Listings")) );
		events.add( new AddSubNavAnchorEvent(new Anchor("?bean=deploymentListingsPage&applicationId="+app.getId(),"Deployment History","Deployment History")) );
		
		// at this point, we're committed to form setup at least
		templateVariables.put(FormConstants.PROCESS_TARGET,PROCESS_TARGET);
		
		version = obtainExistingApplicationVersionFromParameters(app,appId,events,parameterMap);
		if( version==null ) {
			version = new ApplicationVersion();
		}
		
		// determine if the user is allowed to modify application versions
		Boolean willProcess = canUserModifyOrCreate(app,version);
		if( !willProcess ) {
			events.add( new MessagesEvent("Current user does not have permissions to make changes here.") );
		}
		if( !version.getActiveFlag() ) {
			events.add( new MessagesEvent("This version is not currently active.") );
			willProcess=false;
		}
		templateVariables.put("willProcess",willProcess);
		
		
		if( notEmpty(FormConstants.PROCESS_TARGET, parameterMap) 
				&& PROCESS_TARGET.compareTo(firstValue(FormConstants.PROCESS_TARGET,parameterMap ))==0 
				&& willProcess ) {
			
			// TODO: check to see if the user can delete versions
			if( ParameterMapUtils.notEmpty(FormConstants.DELETE,parameterMap) && ParameterMapUtils.notEmpty("deleteConfirm",parameterMap) ) {
				
				if( ParameterMapUtils.firstValue("deleteConfirm", parameterMap).equals(FormConstants.APPVER_DELETE_CONFIRM_TEXT) ) {
					
					try {
						modelManager.begin();
						modelManager.delete(version, events);
						modelManager.commit(events);
					} catch(Exception e) {
						modelManager.rollback();
						String msg = String.format("Unable to delete the version - %s",ExceptionUtils.getRootCauseMessage(e));
						logger.error(msg,e);
						events.add( new MessagesEvent(msg) );
					}
					
				} else {
					
					events.add( new MessagesEvent("You must confirm your desire to delete by typing in the delete confirmation message.") );
				}
			} else {
			
				processApplicationVersionFromParameters(app,version,events,parameterMap);
			}
			
		}
		
		if( version!=null ) {
			templateVariables.put("version", version);
		}
		
		templateVariables.put("application", app);
		
		createHashTypes(templateVariables,version!=null?version.getArchive():null);
		
		return events;
	}
	
	private void validateStorageConfiguration(Map<Object,Object> templateVariables, List<ProcessingEvent> events) {
		String storagePathErrors = modelManager.getGlobalSettings().validateTemporaryStoragePath();
		if( storagePathErrors!=null ) {
			events.add( new MessagesEvent("WARNING: The archive storage path is not set and file uploads will not be processed.  The archive storage path can be set on the settings page.") );
			templateVariables.put(FormConstants.ENCODING_TYPE, "");
		} else {
			templateVariables.put(FormConstants.ENCODING_TYPE,"enctype=\""+FormConstants.ENCTYPE_MULTIPART_FORMDATA+"\"");
		}
	}
	
	private Boolean canUserModifyOrCreate(Application app, ApplicationVersion version) {
		
		// we don't want to pass it back, but the
		// Authorizer needs the Application object
		// to determine whether the user may create
		// a version or not.
		version = version!=null?version:new ApplicationVersion();
		version.setApplication(app);
		
		Boolean mayCreateVersion = modelManager.getAuthorizer().may(Authorizer.Action.CREATE, version);
		Boolean mayModifyVersion = modelManager.getAuthorizer().may(Authorizer.Action.MODIFY, version);
		return (mayCreateVersion || (mayModifyVersion && version!=null));
	}
	
	/**
	 * Creates the list of selectable hashes
	 * @param vars
	 * @param archive
	 */
	@SuppressWarnings("unchecked")
	private void createHashTypes(Map<Object,Object> vars, ApplicationArchive archive) {
		List<Option> opts = new ArrayList<Option>();
		String archiveHashAlg = archive!=null?archive.getHashAlgorithm():null;
		HashAlgorithm alg = null;
		for( HashAlgorithm thisAlg : HashAlgorithm.values() ) {
			Option newOpt = new Option();
			newOpt.setIsSelected(archiveHashAlg!=null && thisAlg.value().equals(archiveHashAlg));
			newOpt.setInnerText(thisAlg.value());
			newOpt.setValue(thisAlg.value());
			opts.add(newOpt);
		}
		vars.put("hashTypes", opts);
	}
	
	/**
	 * @param app
	 * @param appId
	 * @param events
	 * @param parameterMap
	 * @return The application version indicated by the parameterMap, or null
	 */
	private ApplicationVersion obtainExistingApplicationVersionFromParameters(Application app, Long appId, List<ProcessingEvent> events, Map<Object,Object> parameterMap) {
		// if we're not processing and there is a versionId or an identifier in the request
		// then we're pre-populating the form with information from the version
		ApplicationVersion version = null;
		String versionId = firstValue("versionId", parameterMap);
		String identifier = firstValue("identifier", parameterMap);
		if( StringUtils.isNotBlank(versionId) || StringUtils.isNotBlank(identifier) ) {

			if( StringUtils.isNotBlank(versionId) ) {
				version = modelManager.getModelService().findByPrimaryKey(ApplicationVersion.class,Long.valueOf(versionId));
			}
			if( version==null && StringUtils.isNotBlank(identifier) ) {
				version = modelManager.getModelService().findAppVersionByNameAndId(app.getName(), identifier);
			}
			
			if( version==null ) {
				events.add( new GenericProcessingEvent(ProcessingTargets.MESSAGES,"An Application Version matching input could not be found.  Creating a new version.") );
			} else if( version.getApplication()!=null && version.getApplication().getId().compareTo(appId)!=0 ){
				version = null;
				events.add( new GenericProcessingEvent(ProcessingTargets.MESSAGES,"The Application Version with id "+versionId+" is not a version of the Application with id "+appId) );
			}
		}
		return version;
	}
	
	private void processApplicationVersionFromParameters(Application app, ApplicationVersion version, List<ProcessingEvent> events, Map<Object,Object> parameterMap) {
		
		// a version is not being modified,
		// then create a new archive for it.
		if( version.getPk()==null ) {
			version.setArchive(new ApplicationArchive());
			version.getArchive().setApplication(app);
			version.setApplication(app);
		}
		
		fillInApplicationVersionFromParameters(app,version,events,parameterMap);
		
		if( version!=null && version.getArchive()==null ) {
			events.add( new MessagesEvent("Application archive could not be created.  Not creating empty version.") );
		} else {
			try {
				version.setLastModifier(firstValue("userPrincipalName",parameterMap));
				modelManager.begin();
				version.setArchive(modelManager.addModify(version.getArchive(), events));
				version = modelManager.addModify(version,events);
				app.addVersion(version);
				app = modelManager.addModify(app,events);
				modelManager.commit(events);
				modelManager.refresh(app,events);
				events.add( new MessagesEvent("Application version successfully created/modified!") );
			} catch( InvalidPropertiesException ipe ) {
				modelManager.rollback();
				logger.error("Unable to add/modify version "+version.getIdentifier(),ipe);
				events.add( new MessagesEvent("Unable to add/modify version - "+ipe.getMessage()) );
			} catch( PersistenceException pe ) {
				modelManager.rollback();
				logger.error("Unable to add/modify version "+version.getIdentifier(),pe);
				events.add( new MessagesEvent("Unable to add/modify version - "+pe.getMessage()) );								
			}
		}
	}
	
	private void fillInApplicationVersionFromParameters(Application app, ApplicationVersion version, List<ProcessingEvent> events, Map<Object,Object> parameterMap) {

		version.setIdentifier(firstValue("identifier",parameterMap));
		if( version.getArchive()==null ) {
			version.setArchive(new ApplicationArchive());
			version.getArchive().setApplication(app);
		}
		
		version.setApplication(app);
		
		version.setNotes(firstValue("notes",parameterMap));
		
		Boolean archiveUncreated = true;
		
		// if there was an uploadArchive, then attempt to auto-assemble the rest of parameters
		if( parameterMap.get("uploadArchive")!=null ) {
			
			if( ! (parameterMap.get("uploadArchive") instanceof FileItem) ) {
				
				events.add( new MessagesEvent("Uploaded file not processed!  Is the archive storage path set in settings?") );
			} else {
				
				FileItem item = (FileItem)parameterMap.get("uploadArchive");
				Long size = item.getSize();
				
				if( size>0 ) {
					
					try {
						
						File tempFile = ServletUtils.tempFileFromFileItem(modelManager.getGlobalSettings().getTemporaryStoragePath(), item);
						ApplicationArchive archive = new ApplicationArchive();
						archive.setNewFileUploaded(true);
						archive.setHash(tempFile.getAbsolutePath());
						archive.setApplication(app);
						version.setArchive(archive);
						archiveUncreated = false;
					} catch(Exception ioe) {
						
						logger.error("An error transpired creating an uploadArchive temp file: {}",ioe);
						events.add( new MessagesEvent(ioe.getMessage()) );
						return;
					} finally {
						item.delete();
					}
				} else {
					
					events.add( new MessagesEvent("Uploaded file not processed!  Is the archive storage path set in settings?") );
				}
			}
		}
		
		// else there was no zip archive uploaded
		if( archiveUncreated ) {
			ApplicationArchive archive = version.getArchive();
			
			archive.setUrl(firstValue("url",parameterMap));
			
			// TODO: this should be selectable
			archive.setHashAlgorithm(firstValue("hashType",parameterMap));
			archive.setHash(firstValue("hash",parameterMap));
			if( notEmpty("bytesLength",parameterMap) ) {
				archive.setBytesLength(Integer.valueOf(firstValue("bytesLength",parameterMap)));
			}
			if( notEmpty("bytesLengthUncompressed",parameterMap) ) {
				archive.setBytesLengthUncompressed(Integer.valueOf(firstValue("bytesLengthUncompressed",parameterMap)));
			}
		}
	}
	
	// ACCESSORS
	
	public void setModelManager(ModelManager modelManager) {
		this.modelManager = modelManager;
	}
	public ModelManager getModelManager() {
		return modelManager;
	}
}
