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

package com.openmeap.file;

import java.io.File;

import org.apache.commons.transaction.file.FileResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openmeap.model.ModelService;
import com.openmeap.model.dto.GlobalSettings;

public class FileOperationManagerFactory {
	
	private ModelService modelService;
	private Logger logger = LoggerFactory.getLogger(FileOperationManagerFactory.class);
	
	public FileOperationManager newFileOperationManager() {
		
		GlobalSettings settings = (GlobalSettings)modelService.findByPrimaryKey(GlobalSettings.class, 1L);
		if( settings.getTemporaryStoragePath()==null 
				|| !new File(settings.getTemporaryStoragePath()).exists()) {
			logger.error("The storage path has not been set in GlobalSettings.  Use the settings page to fix this.");
		}
		
		FileOperationManagerImpl mgr = new FileOperationManagerImpl();
		FileResourceManager resMgr = new FileResourceManager(
				settings.getTemporaryStoragePath()
				,settings.getTemporaryStoragePath()+"/tmp",
				true,
				new SLF4JLoggerFacade(LoggerFactory.getLogger(FileResourceManager.class)));
		mgr.setFileResourceManager(resMgr);
		
		return mgr;
	}
	
	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}
}
