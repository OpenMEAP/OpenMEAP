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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openmeap.event.Event;
import com.openmeap.event.EventNotificationException;
import com.openmeap.event.ProcessingEvent;
import com.openmeap.model.ArchiveFileHelper;
import com.openmeap.model.ModelEntity;
import com.openmeap.model.ModelManager;
import com.openmeap.model.ModelServiceOperation;
import com.openmeap.model.dto.Deployment;
import com.openmeap.model.event.AbstractModelServiceEventNotifier;

public class DeploymentAddModifyNotifier extends AbstractModelServiceEventNotifier<Deployment> {

	private static Logger logger = LoggerFactory.getLogger(DeploymentAddModifyNotifier.class);
	private ModelManager modelManager;
	
	public void setModelManager(ModelManager modelManager) {
		this.modelManager = modelManager;
	}

	@Override
	public <E extends Event<Deployment>> void onAfterOperation(E event, List<ProcessingEvent> events) throws EventNotificationException {
		ArchiveFileHelper.maintainFileSystemCleanliness(
				modelManager,
				((Deployment)event.getPayload()).getApplicationArchive(), 
				events);
	}

	@Override
	public Boolean notifiesFor(ModelServiceOperation operation,
			ModelEntity payload) {
		return operation.equals(ModelServiceOperation.DELETE) && Deployment.class.isAssignableFrom(payload.getClass());
	}

}
