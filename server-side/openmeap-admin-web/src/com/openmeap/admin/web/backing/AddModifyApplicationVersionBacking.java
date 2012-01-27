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

package com.openmeap.admin.web.backing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.persistence.PersistenceException;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.openmeap.util.ParameterMapUtils.*;

import com.openmeap.protocol.dto.HashAlgorithm;
import com.openmeap.util.Utils;
import com.openmeap.util.ZipUtils;
import com.openmeap.web.html.*;
import com.openmeap.admin.web.ProcessingTargets;
import com.openmeap.admin.web.backing.event.AddSubNavAnchorEvent;
import com.openmeap.admin.web.backing.event.MessagesEvent;
import com.openmeap.constants.FormConstants;
import com.openmeap.constants.ServletNameConstants;
import com.openmeap.constants.UrlParamConstants;
import com.openmeap.model.*;
import com.openmeap.Authorizer;
import com.openmeap.model.dto.Application;
import com.openmeap.model.dto.ApplicationArchive;
import com.openmeap.model.dto.ApplicationVersion;
import com.openmeap.model.dto.GlobalSettings;
import com.openmeap.model.service.ArchiveUploadNotifier;
import com.openmeap.web.*;

// TODO: there are way to many things going on in this class
public class AddModifyApplicationVersionBacking extends AbstractTemplatedSectionBacking {

	private Logger logger = LoggerFactory.getLogger(AddModifyApplicationVersionBacking.class);
	
	private static String PROCESS_TARGET = ProcessingTargets.ADDMODIFY_APPVER;
	
	private ModelManager modelManager = null;
	private ArchiveUploadNotifier archiveUploadNotifier = null;
	
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
		
		String storagePathErrors = modelManager.getGlobalSettings().validateTemporaryStoragePath();
		if( storagePathErrors!=null ) {
			events.add( new MessagesEvent("WARNING: The archive storage path is not set and file uploads will not be processed.  The archive storage path can be set on the settings page.") );
			templateVariables.put("encodingType", "");
		} else {
			templateVariables.put("encodingType","enctype=\""+FormConstants.ENCTYPE_MULTIPART_FORMDATA+"\"");
		}
		
		// we must have an application in order to add a version
		if( ! notEmpty("applicationId", parameterMap) ) {
			return ProcessingUtils.newList(new GenericProcessingEvent<String>(ProcessingTargets.MESSAGES,"An application must be specified in order to add a version"));
		}
		Long appId = Long.valueOf( firstValue("applicationId", parameterMap) );
		app = modelManager.findApplication( appId );
		if( app==null ) {
			return ProcessingUtils.newList(new GenericProcessingEvent<String>(ProcessingTargets.MESSAGES,"The application with id "+appId+" could not be found."));
		}
		events.add( new AddSubNavAnchorEvent(new Anchor("?bean=addModifyAppPage&applicationId="+app.getId(),"View/Modify Application","View/Modify Application")) );
		events.add( new AddSubNavAnchorEvent(new Anchor("?bean=appVersionListingsPage&applicationId="+app.getId(),"Version Listings","Version Listings")) );
		
		// at this point, we're committed to form setup at least
		templateVariables.put("processTarget",PROCESS_TARGET);
		
		version = obtainExistingApplicationVersionFromParameters(app,appId,events,parameterMap);
		if( version==null ) {
			version = new ApplicationVersion();
		}
		
		Boolean willProcess = determineWillProcessPost(app,version);
		if( !willProcess ) {
			events.add( new MessagesEvent("Current user does not have permissions to make changes here") );
		}
		templateVariables.put("willProcess",willProcess);
		
		if( notEmpty("processTarget", parameterMap) 
				&& PROCESS_TARGET.compareTo(firstValue("processTarget",parameterMap ))==0 
				&& willProcess ) {
			processApplicationVersionFromParameters(app,version,events,parameterMap);
		}
		
		templateVariables.put("version", version);
		templateVariables.put("application", app);
		
		createHashTypes(templateVariables,version!=null?version.getArchive():null);
		
		return events;
	}
	
	private Boolean determineWillProcessPost(Application app, ApplicationVersion version) {
		
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
				version = modelManager.findApplicationVersion(Long.valueOf(versionId));
			}
			if( version==null && StringUtils.isNotBlank(identifier) ) {
				version = modelManager.findAppVersionByNameAndId(app.getName(), identifier);
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
		// a version is not being modified
		if( version.getPk()==null ) {
			version.setArchive(new ApplicationArchive());
			version.getArchive().setVersion(version);
			version.setApplication(app);
			//app.getVersions().put(version.getIdentifier(), version);
		}
		fillInApplicationVersionFromParameters(app,version,events,parameterMap);
		if( version!=null && version.getArchive()==null ) {
			events.add( new MessagesEvent("Application archive could not be created.  Not creating empty version.") );
		} else {
			try {
				//app.addVersion(version);
				version = modelManager.addModify(version);
				modelManager.getModelService().refresh(app);
				events.add( new MessagesEvent("Application version successfully created/modified!") );
			} catch( InvalidPropertiesException ipe ) {
				events.add( new MessagesEvent(ipe.getMessage()) );
			} catch( PersistenceException pe ) {
				events.add( new MessagesEvent(pe.getMessage()) );								
			}
		}
	}
	
	private void fillInApplicationVersionFromParameters(Application app, ApplicationVersion version, List<ProcessingEvent> events, Map<Object,Object> parameterMap) {

		version.setIdentifier(firstValue("identifier",parameterMap));
		if( version.getArchive()==null ) {
			version.setArchive(new ApplicationArchive());
			version.getArchive().setVersion(version);
		}
		
		version.setApplication(app);
		/*if( app.getVersions()==null )
			app.setVersions(new HashMap<String,ApplicationVersion>());
		app.getVersions().put(version.getIdentifier(), version);*/
		
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
					ApplicationArchive archive = createApplicationArchiveFromFileItem(version.getArchive(),item,events);
					version.setArchive(archive);
					archiveUncreated = false;
				}
			}
		}
		
		// else there was no zip archive uploaded
		if( archiveUncreated ) {
			version.getArchive().setUrl(firstValue("url",parameterMap));
			
			// TODO: this should be selectable
			version.getArchive().setHashAlgorithm(firstValue("hashType",parameterMap));
			version.getArchive().setHash(firstValue("hash",parameterMap));
			if( notEmpty("bytesLength",parameterMap) )
				version.getArchive().setBytesLength(Integer.valueOf(firstValue("bytesLength",parameterMap)));
			if( notEmpty("bytesLengthUncompressed",parameterMap) )
				version.getArchive().setBytesLengthUncompressed(Integer.valueOf(firstValue("bytesLengthUncompressed",parameterMap)));
		}
		
		if( version.getArchive()==null ) {
			events.add( new MessagesEvent("Zip archive failed to process!") );
		}
	}
	
	private ApplicationArchive createApplicationArchiveFromFileItem(ApplicationArchive archive, FileItem item, List<ProcessingEvent> events) {
		
		GlobalSettings settings = modelManager.getGlobalSettings();
		Long size = item.getSize();
		
		String pathError = settings.validateTemporaryStoragePath();
		if( pathError!=null ) {
			logger.error("There is an issue with the global settings temporary storage path: "+settings.validateTemporaryStoragePath()+"\n {}",pathError);
			events.add( new MessagesEvent("There is an issue with the global settings temporary storage path: "+settings.validateTemporaryStoragePath()+" - "+pathError) );
			return null;
		}
		
		// create a temp file
		File tempFile = null;
		try {
			tempFile = File.createTempFile("uploadArchive","",new File(settings.getTemporaryStoragePath()));
			item.write(tempFile);
		} catch(Exception ioe) {
			logger.error("An error transpired creating an uploadArchive temp file: {}",ioe);
			events.add( new MessagesEvent(ioe.getMessage()) );
			return null;
		} 
		item.delete();
		
		// determine the md5 hash of the uploaded file
		// and rename the temp file by the hash
		FileInputStream is = null;
		try {
			is = new FileInputStream(tempFile);
			try {
				String hashValue = Utils.hashInputStream("MD5", is);
				
				try {
					File oldExplodedPath = archive.getExplodedPath(settings.getTemporaryStoragePath());
					if( oldExplodedPath!=null && oldExplodedPath.exists() ) {
						FileUtils.deleteDirectory(oldExplodedPath);
					}
				} catch( IOException ioe ) {
					logger.error("There was an exception deleting the old web-view directory: {}",ioe);
					events.add(new MessagesEvent(String.format("Upload process will continue.  There was an exception deleting the old web-view directory: %s",ioe.getMessage())));
				}
				
				// TODO: using MD5 or SHA1 should be a configured global setting...maybe
				archive.setHashAlgorithm("MD5");
				archive.setHash(hashValue);
			} finally {
				is.close();
			}
			
			File newFile = archive.getFile(settings.getTemporaryStoragePath());
			if( tempFile.renameTo(newFile) ) {
				
				String mesg = String.format("Uploaded temporary file %s successfully renamed to %s",tempFile.getName(),newFile.getName());
				logger.debug(mesg);
				events.add(new MessagesEvent(mesg));
				
				if( ! unzipFile(archive,newFile,events) ) {
					return null;
				}
				
				tempFile = newFile;
				
			} else {
				String mesg = String.format("Failed to renamed file %s to %s",tempFile.getName(),newFile.getName());
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
			ZipFile zipFile = new ZipFile(tempFile);
			try {
				Integer uncompressedSize = ZipUtils.getUncompressedSize(zipFile).intValue();
				archive.setBytesLengthUncompressed(new Long(uncompressedSize).intValue());
			} finally {
				zipFile.close();
			}
		} catch( IOException ioe ) {
			logger.error("An exception occurred while calculating the uncompressed size of the archive: {}",ioe);
			events.add(new MessagesEvent(String.format("An exception occurred while calculating the uncompressed size of the archive: %s"+ioe.getMessage())));
			return null;
		}
		
		// TODO: make all parameters of this url into a template and have that default template specified in the Application
		archive.setUrl(ApplicationArchive.URL_TEMPLATE);
		
		try {
			archiveUploadNotifier.notify(new ArchiveUploadEvent(archive));
		} catch (Exception e) {
			logger.error("An exception occurred pushing the new archive to cluster nodes: {}",e);
			events.add(new MessagesEvent(String.format("An exception occurred while calculating the uncompressed size of the archive: %s",e.getMessage())));
		}
		
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
			GlobalSettings settings = modelManager.getGlobalSettings();
			File dest = archive.getExplodedPath(settings.getTemporaryStoragePath());
			if( dest.exists() ) {
				FileUtils.deleteDirectory(dest);
			}
			ZipUtils.unzipFile(new ZipFile(zipFile), dest);
			return Boolean.TRUE;
		} catch( Exception e ) {
			logger.error("An exception occurred unzipping the archive to the viewing location: {}",e);
			events.add(new MessagesEvent(String.format("An exception occurred unzipping the archive to the viewing location: %s",e.getMessage())));
		}
		return Boolean.FALSE;
	}
	
	// ACCESSORS
	
	public void setModelManager(ModelManager modelManager) {
		this.modelManager = modelManager;
	}
	public ModelManager getModelManager() {
		return modelManager;
	}

	public void setArchiveUploadNotifier(ArchiveUploadNotifier archiveUploadNotifier) {
		this.archiveUploadNotifier = archiveUploadNotifier;
	}
	public ArchiveUploadNotifier getArchiveUploadNotifier() {
		return archiveUploadNotifier;
	}
}
