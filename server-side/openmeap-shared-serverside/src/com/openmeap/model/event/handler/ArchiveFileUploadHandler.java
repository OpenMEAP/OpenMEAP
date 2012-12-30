/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2013 OpenMEAP, Inc.                                   #
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

package com.openmeap.model.event.handler;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openmeap.cluster.ClusterHandlingException;
import com.openmeap.event.Event;
import com.openmeap.event.EventHandler;
import com.openmeap.model.InvalidPropertiesException;
import com.openmeap.model.ModelManager;
import com.openmeap.model.dto.ApplicationArchive;
import com.openmeap.model.dto.ClusterNode;
import com.openmeap.model.dto.GlobalSettings;

public class ArchiveFileUploadHandler implements EventHandler<Map> {

	private Logger logger = LoggerFactory.getLogger(ArchiveFileUploadHandler.class);
	
	private ModelManager modelManager;
	private String fileSystemStoragePathPrefix;
	
	@Override
	public <E extends Event<Map>> void handle(E event) throws ClusterHandlingException {
		
		Map parms = event.getPayload();
		ApplicationArchive arch = (ApplicationArchive)parms.get("archive");
		
		String hashId = String.format("{%s}%s",arch.getHashAlgorithm(),arch.getHash());
		
		logger.debug("ArchiveUploadEvent for file {}",hashId);
		
		File file = arch.getFile(getFileSystemStoragePathPrefix());
		
		if( file.exists() ) {
			logger.warn("ApplicationArchive with {} hash already exists, ignoring ArchiveUploadEvent.",hashId);
			return;
		}
		
		if( parms.get("file")==null || !(parms.get("file") instanceof FileItem) ) {
			logger.error("Expected a FileItem under the \"file\" parameter.  Got "+parms.get("file")+" instead.");
			throw new ClusterHandlingException("Expected a FileItem under the \"file\" parameter.  Got "+parms.get("file")+" instead.");
		}
		
		FileItem item = (FileItem)parms.get("file");
		try {
			item.write(file);
		} catch(Exception ioe) {
			logger.error("An error occurred writing {}: {}",item.getName(),ioe);
			throw new ClusterHandlingException(ioe);
		} 
		item.delete();
	}

	// ACCESSORS
	
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
