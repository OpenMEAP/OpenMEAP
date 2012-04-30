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

package com.openmeap.model.event.notifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

import javax.persistence.PersistenceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openmeap.cluster.ClusterNotificationException;
import com.openmeap.constants.UrlParamConstants;
import com.openmeap.event.Event;
import com.openmeap.event.EventNotificationException;
import com.openmeap.event.MessagesEvent;
import com.openmeap.event.ProcessingEvent;
import com.openmeap.file.FileOperationManager;
import com.openmeap.model.ArchiveFileHelper;
import com.openmeap.model.ModelEntity;
import com.openmeap.model.ModelManager;
import com.openmeap.model.ModelServiceOperation;
import com.openmeap.model.dto.ApplicationArchive;
import com.openmeap.model.dto.GlobalSettings;
import com.openmeap.model.event.ModelEntityEventAction;
import com.openmeap.util.Utils;
import com.openmeap.util.ZipUtils;

public class ArchiveFileUploadNotifier extends AbstractArchiveFileEventNotifier {	
	
	private Logger logger = LoggerFactory.getLogger(ArchiveFileUploadNotifier.class);
	private FileOperationManager fileManager;
	
	public void setFileManager(FileOperationManager fileManager) {
		this.fileManager = fileManager;
	}
	
	@Override
	protected String getEventActionName() {
		return ModelEntityEventAction.ARCHIVE_UPLOAD.getActionName();
	}
		
	@Override
	protected void addRequestParameters(ModelEntity modelEntity, Map<String,Object> parms) {
		ApplicationArchive archive = (ApplicationArchive)modelEntity;
		parms.put(UrlParamConstants.APPARCH_FILE, archive.getFile(getModelManager().getGlobalSettings().getTemporaryStoragePath()));
		super.addRequestParameters(modelEntity, parms);
	}
	
	@Override
	public Boolean notifiesFor(ModelServiceOperation operation, ModelEntity payload) {
		return operation==ModelServiceOperation.SAVE_OR_UPDATE && ApplicationArchive.class.isAssignableFrom(payload.getClass());
	}
	
	@Override
	public <E extends Event<ApplicationArchive>> void onBeforeOperation(
			E event, List<ProcessingEvent> events)
			throws EventNotificationException {
		
		ApplicationArchive archive = (ApplicationArchive)event.getPayload();
		boolean newUpload = archive.getNewFileUploaded();
		ApplicationArchive ret = archive;
		if( newUpload && (ret=processApplicationArchiveFileUpload(archive,events))==null ) {
			throw new PersistenceException("Zip archive failed to process!");
		}
	}
	
	@Override
	public <E extends Event<ApplicationArchive>> void onInCommitBeforeCommit(final E event, List<ProcessingEvent> events) throws ClusterNotificationException {
		notify(event,events);
	}
	
	@Override
	public <E extends Event<ApplicationArchive>> void notify(final E event, List<ProcessingEvent> events) throws ClusterNotificationException {
		ApplicationArchive archive = (ApplicationArchive)event.getPayload();
		File archiveFile = archive.getFile(getModelManager().getGlobalSettings().getTemporaryStoragePath());
		if( !archiveFile.exists() ) {
			String msg = String.format("The archive file %s cannot be found.  This could be because you opted to fill in the version details yourself.",archiveFile.getAbsoluteFile());
			logger.warn(msg);
			events.add(new MessagesEvent(msg));
			return;
		}
		super.notify(event, events);
	}
	
	@SuppressWarnings("unchecked")
	private ApplicationArchive processApplicationArchiveFileUpload(ApplicationArchive archive, List<ProcessingEvent> events) {
		
		GlobalSettings settings = getModelManager().getGlobalSettings();
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
				ApplicationArchive archiveExists = getModelManager().getModelService().findApplicationArchiveByHashAndAlgorithm(hashValue, "MD5");
				if(archiveExists!=null) {
					if( !tempFile.delete() ) {
						String mesg = String.format("Failed to delete temporary file %s",tempFile.getName());
						logger.error(mesg);
						events.add(new MessagesEvent(mesg));	
					}
					return archiveExists;
				}
			} finally {
				is.close();
			}
			
			// if the archive is pre-existing and not the same,
			// then determine if the web-view and zip should be deleted
			// that is, they are not used by any other versions
			if( archive.getId()!=null && !archive.getHash().equals(hashValue) ) {

				ArchiveFileHelper.maintainFileSystemCleanliness(getModelManager(),archive,events);
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
				ArchiveFileHelper.unzipFile(getModelManager(),fileManager,archive,destinationFile,events);
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
}
