/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2014 OpenMEAP, Inc.                                   #
 #    Credits to Jonathan Schang & Rob Thacher                                 #
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

package com.openmeap.model.event.handler;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openmeap.event.Event;
import com.openmeap.event.EventHandler;
import com.openmeap.event.EventHandlingException;
import com.openmeap.model.ModelManager;
import com.openmeap.model.dto.ApplicationArchive;

/**
 * Handles the actual deletion of an application archive.
 * @author schang
 */
public class ArchiveFileDeleteHandler implements EventHandler<Map> {

	private Logger logger = LoggerFactory.getLogger(ArchiveFileDeleteHandler.class);
	
	private ModelManager modelManager;
	private String fileSystemStoragePathPrefix;
	
	@Override
	public <E extends Event<Map>> void handle(E event)
			throws EventHandlingException {

		if( logger.isTraceEnabled() ) {
			logger.trace("entering handle()");
		}
		
		ApplicationArchive archive = (ApplicationArchive)event.getPayload().get("archive");
		File file = archive.getFile(getFileSystemStoragePathPrefix());
		
		if( file.exists() ) {
			if( !file.delete() ) {
				logger.error("Failed to delete archive "+archive.getFile(getFileSystemStoragePathPrefix()));
			}
		} else {
			logger.warn("Failed to find archive "+archive.getFile(getFileSystemStoragePathPrefix())+".  It may have yet to be deployed.");
		}
		
		File directory = archive.getExplodedPath(getFileSystemStoragePathPrefix());
		if( directory.exists() ) {
			try {
				FileUtils.deleteDirectory(directory);
			} catch(IOException ioe) {
				String msg = "Unable to delete directory "+directory;
				logger.error(msg);
				throw new EventHandlingException(msg,ioe);
			}
		}		
		
		if( logger.isTraceEnabled() ) {
			logger.trace("exiting handle()");
		}
	}
	
	public void setModelManager(ModelManager modelManager) {
		this.modelManager = modelManager;
	}
	public ModelManager getModelManager() {
		return modelManager;
	}

	public String getFileSystemStoragePathPrefix() {
		return fileSystemStoragePathPrefix;
	}
	public void setFileSystemStoragePathPrefix(String fileSystemStoragePathPrefix) {
		this.fileSystemStoragePathPrefix = fileSystemStoragePathPrefix;
	}

}
