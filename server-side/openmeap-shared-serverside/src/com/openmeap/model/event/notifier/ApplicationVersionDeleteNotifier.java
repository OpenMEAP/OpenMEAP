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

package com.openmeap.model.event.notifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.openmeap.model.dto.ApplicationVersion;

public class ApplicationVersionDeleteNotifier extends
		AbstractModelServiceEventNotifier<ApplicationVersion> {

	private Map<Thread,ApplicationArchive> archives = new HashMap<Thread,ApplicationArchive>();
	private ModelManager modelManager;
	private FileOperationManager fileManager;
	
	public void setFileManager(FileOperationManager fileManager) {
		this.fileManager = fileManager;
	}

	@Override
	public Boolean notifiesFor(ModelServiceOperation operation,
			ModelEntity payload) {
		return payload instanceof ApplicationVersion && operation==ModelServiceOperation.DELETE;
	}

	@Override
	public <E extends Event<ApplicationVersion>> void onBeforeOperation(
			E event, List<ProcessingEvent> events)
			throws EventNotificationException {
		
		ApplicationVersion version = (ApplicationVersion)event.getPayload();
		if( version.getArchive()!=null ) {
			archives.put(Thread.currentThread(), version.getArchive());
		}
	}

	@Override
	public <E extends Event<ApplicationVersion>> void onAfterOperation(E event, List<ProcessingEvent> events) throws EventNotificationException {
		
		ApplicationArchive archive = archives.get(Thread.currentThread());
		if( archive!=null ) {
			ArchiveFileHelper.maintainFileSystemCleanliness(modelManager, fileManager, archive, events);
		}
		events.add( new MessagesEvent("Application version successfully deleted!") );
	}

	public void setModelManager(ModelManager modelManager) {
		this.modelManager = modelManager;
	}

}
