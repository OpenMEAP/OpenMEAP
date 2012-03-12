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

import com.openmeap.Event;
import com.openmeap.cluster.ClusterNotificationException;
import com.openmeap.model.ModelEntity;
import com.openmeap.model.ModelServiceOperation;
import com.openmeap.model.dto.Application;
import com.openmeap.model.dto.ApplicationArchive;
import com.openmeap.model.dto.ApplicationVersion;
import com.openmeap.model.dto.Deployment;
import com.openmeap.model.event.ModelEntityEventAction;

public class DeploymentDeleteNotifier extends AbstractEventNotifier<Deployment> {
	
	@Override
	protected String getEventActionName() {
		return ModelEntityEventAction.DEPLOYMENT_DELETE.getActionName();
	}

	@Override
	public Boolean notifiesFor(ModelServiceOperation operation,
			ModelEntity payload) {
		if(operation==ModelServiceOperation.DELETE && Deployment.class.isAssignableFrom(payload.getClass()) ) {
			return true;
		}
		return false;
	}

	@Override
	public <E extends Event<Deployment>> void notify(final E event) throws ClusterNotificationException {
		
		Deployment deployment2Delete = (Deployment)event.getPayload();
		Application app = deployment2Delete.getApplication();
		
		// if there are any other deployments with this hash,
		// then we cannot yet delete it.
		for( Deployment deployment : app.getDeployments() ) {
			if( ! deployment.getId().equals(deployment2Delete.getId()) ) {
				if( deployment.getHash().equals(deployment2Delete.getHash()) 
						&& deployment.getHashAlgorithm().equals(deployment2Delete.getHashAlgorithm()) ) {
					return;
				}
			}
		}
		
		// if there are any application versions with this archive, 
		// then we cannot delete it.
		for( String versionId : app.getVersions().keySet() ) {
			ApplicationVersion version = app.getVersions().get(versionId);
			ApplicationArchive archive = version.getArchive();
			if( archive.getHash().equals(deployment2Delete.getHash()) 
					&& archive.getHashAlgorithm().equals(deployment2Delete.getHashAlgorithm()) ) {
				return;
			}
		}
		
		super.notify(event);
	}
}