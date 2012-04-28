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
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipFile;

import javax.persistence.PersistenceException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openmeap.event.MessagesEvent;
import com.openmeap.event.ProcessingEvent;
import com.openmeap.file.FileOperationManager;
import com.openmeap.model.dto.ApplicationArchive;
import com.openmeap.model.dto.ApplicationVersion;
import com.openmeap.model.dto.Deployment;
import com.openmeap.model.dto.GlobalSettings;

public class ArchiveFileHelper {
	
	private static Logger logger = LoggerFactory.getLogger(ArchiveFileHelper.class);
	
	/**
	 * Trim the deployment history table.  Deleting old archives as we go.
	 * @param app
	 * @throws PersistenceException 
	 * @throws InvalidPropertiesException 
	 */
	public static void maintainFileSystemCleanliness(ModelManager modelManager, ApplicationArchive archive, List<ProcessingEvent> events) {
		
		ModelService modelService = modelManager.getModelService();
		GlobalSettings settings = modelManager.getGlobalSettings();
		
		// check to see if any deployments or versions are currently using this archive
		List<Deployment> deployments = (List<Deployment>)modelService.findDeploymentsByApplicationArchive(archive);
		List<ApplicationVersion> versions = (List<ApplicationVersion>)modelService.findVersionsByApplicationArchive(archive);
		
		// either more than one archive has this file
		Boolean archiveIsInUseElsewhere = deployments.size()>0 || versions.size()>0;
		
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
			
			modelManager.delete(archive,events);
		}
	}
	
	/**
	 * 
	 * @param archive
	 * @param zipFile
	 * @param events
	 * @return TRUE if the file successfully is exploded, FALSE otherwise.
	 */
	public static Boolean unzipFile(ModelManager modelManager, FileOperationManager fileManager, ApplicationArchive archive, File zipFile, List<ProcessingEvent> events) {
		try {
			GlobalSettings settings = modelManager.getGlobalSettings();
			File dest = archive.getExplodedPath(settings.getTemporaryStoragePath());
			if( dest.exists() ) {
				fileManager.delete(dest.getAbsolutePath());
			}
			ZipFile file = null;
			try {
				file = new ZipFile(zipFile);
				fileManager.unzipFile(file, dest.getAbsolutePath());
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
}
