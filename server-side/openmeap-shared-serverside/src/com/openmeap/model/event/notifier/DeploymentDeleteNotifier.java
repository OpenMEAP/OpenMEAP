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

import java.util.HashMap;
import java.util.Map;

import com.openmeap.Event;
import com.openmeap.EventHandlingException;
import com.openmeap.cluster.ClusterNotificationException;
import com.openmeap.model.ModelEntity;
import com.openmeap.model.ModelServiceEventNotifier;
import com.openmeap.model.ModelServiceOperation;
import com.openmeap.model.dto.Application;
import com.openmeap.model.dto.ApplicationArchive;
import com.openmeap.model.dto.ApplicationVersion;
import com.openmeap.model.dto.Deployment;
import com.openmeap.model.dto.GlobalSettings;
import com.openmeap.model.event.MapPayloadEvent;
import com.openmeap.model.event.ModelEntityEvent;
import com.openmeap.model.event.ModelEntityEventAction;
import com.openmeap.model.event.handler.ArchiveFileDeleteHandler;

/**
 * Fired off when a deployment is deleted.  Determines whether or not to delete
 * the application archive from the file-system. 
 * @author schang
 */
public class DeploymentDeleteNotifier implements ModelServiceEventNotifier<Deployment> {
	
	ArchiveFileDeleteNotifier archiveDeleteNotifier = null;
	ArchiveFileDeleteHandler archiveDeleteHandler = null;
	
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
		Application app = deployment2Delete.getApplicationVersion().getApplication();
		
		// if there are any other deployments with this hash,
		//   then we cannot yet delete it's archive.
		for( Deployment deployment : app.getDeployments() ) {
			if( deployment.getId()==null || !deployment.getId().equals(deployment2Delete.getId()) ) {
				if( deployment.getHash().equals(deployment2Delete.getHash()) 
						&& deployment.getHashAlgorithm().equals(deployment2Delete.getHashAlgorithm()) ) {
					return;
				}
			}
		}
		
		// if there are any application versions with this archive, 
		//   then we cannot delete it's archive.
		for( String versionId : app.getVersions().keySet() ) {
			ApplicationVersion version = app.getVersions().get(versionId);
			ApplicationArchive archive = version.getArchive();
			if( version.getActiveFlag().equals(Boolean.TRUE)
					&& archive.getHash().equals(deployment2Delete.getHash()) 
					&& archive.getHashAlgorithm().equals(deployment2Delete.getHashAlgorithm()) ) {
				return;
			} 
		}
		
		// our checks have passed, so we can remove the archive from the local and cluster file-systems
		
		// use the archive delete notifier to cleanup to cluster nodes
		ApplicationArchive archive = new ApplicationArchive();
		archive.setHash(deployment2Delete.getHash());
		archive.setHashAlgorithm(deployment2Delete.getHashAlgorithm());
		archiveDeleteNotifier.notify(new ModelEntityEvent(ModelServiceOperation.DELETE,archive));
		
		// use the archive delete handler to cleanup localhost
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("archive", archive);
		try {
			GlobalSettings settings = archiveDeleteNotifier.getModelManager().getGlobalSettings();
			archiveDeleteHandler.setFileSystemStoragePathPrefix(settings.getTemporaryStoragePath());
			archiveDeleteHandler.handle(new MapPayloadEvent(map));
		} catch (EventHandlingException e) {
			throw new ClusterNotificationException(e);
		}
	}

	public ArchiveFileDeleteNotifier getArchiveDeleteNotifier() {
		return archiveDeleteNotifier;
	}
	public void setArchiveDeleteNotifier(ArchiveFileDeleteNotifier archiveDeleteNotifier) {
		this.archiveDeleteNotifier = archiveDeleteNotifier;
	}

	public ArchiveFileDeleteHandler getArchiveDeleteHandler() {
		return archiveDeleteHandler;
	}
	public void setArchiveDeleteHandler(ArchiveFileDeleteHandler archiveDeleteHandler) {
		this.archiveDeleteHandler = archiveDeleteHandler;
	}
}