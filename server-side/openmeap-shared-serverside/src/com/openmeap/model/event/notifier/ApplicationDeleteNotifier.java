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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.PersistenceException;

import com.openmeap.event.Event;
import com.openmeap.event.EventNotificationException;
import com.openmeap.event.ProcessingEvent;
import com.openmeap.model.InvalidPropertiesException;
import com.openmeap.model.ModelEntity;
import com.openmeap.model.ModelManager;
import com.openmeap.model.ModelServiceOperation;
import com.openmeap.model.dto.Application;
import com.openmeap.model.dto.ApplicationVersion;
import com.openmeap.model.dto.Deployment;

public class ApplicationDeleteNotifier extends AbstractModelServiceEventNotifier<Application> {
	
	private ModelManager modelManager;
	
	public void setModelManager(ModelManager modelManager) {
		this.modelManager = modelManager;
	}

	@Override
	public Boolean notifiesFor(ModelServiceOperation operation,
			ModelEntity payload) {
		return (ModelServiceOperation.DELETE==operation && payload instanceof Application);
	}

	@Override
	public <E extends Event<Application>> void onBeforeOperation(E event,
			List<ProcessingEvent> events) throws EventNotificationException {
		
		Application app = (Application)event.getPayload();
		modelManager.refresh(app, events);
		
		deleteApplicationVersions(app,events);
		deleteDeployments(app,events);
		
	}
	
	private void deleteApplicationVersions(Application app, List<ProcessingEvent> events) {
		
		// flip all the versions to inactive, so they don't prevent archive deletion
		/*for( ApplicationVersion appVer : app.getVersions().values() ) {
			appVer.setActiveFlag(false);
			try {
				modelManager.addModify(appVer,events);
			} catch (InvalidPropertiesException e) {
				throw new PersistenceException(e);
			}
		}*/
		
		// delete each version
		// archives should be deleted as they are no longer used
		List<ApplicationVersion> appVers = new ArrayList<ApplicationVersion>(app.getVersions().values());
		for( ApplicationVersion appVer : appVers ) {
			modelManager.delete(appVer,events);
		}
	}
	
	private void deleteDeployments(Application app, List<ProcessingEvent> events) {
		
		// delete each deployment
		// archives should be deleted as they are no longer used
		List<Deployment> depls = new ArrayList<Deployment>(app.getDeployments());
		for(Deployment depl : depls) {
			modelManager.delete(depl,events);
		}
	}
}
