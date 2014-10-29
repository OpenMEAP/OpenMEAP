/*
###############################################################################
#                                                                             #
#    Copyright (C) 2011-2015 OpenMEAP, Inc.                                   #
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

import javax.persistence.PersistenceException;

import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openmeap.cluster.ClusterNotificationException;
import com.openmeap.event.Event;
import com.openmeap.event.EventHandlingException;
import com.openmeap.event.EventNotificationException;
import com.openmeap.event.ProcessingEvent;
import com.openmeap.model.InvalidPropertiesException;
import com.openmeap.model.ModelEntity;
import com.openmeap.model.ModelServiceOperation;
import com.openmeap.model.dto.Application;
import com.openmeap.model.dto.ApplicationArchive;
import com.openmeap.model.dto.ApplicationVersion;
import com.openmeap.model.dto.Deployment;
import com.openmeap.model.dto.GlobalSettings;
import com.openmeap.model.event.MapPayloadEvent;
import com.openmeap.model.event.ModelEntityEvent;
import com.openmeap.model.event.handler.ArchiveFileDeleteHandler;

/**
 * Fired off when a deployment is deleted.  Determines whether or not to delete
 * the application archive from the file-system. 
 * @author schang
 */
public class DeploymentDeleteNotifier extends AbstractModelServiceEventNotifier<Deployment> {
	
	private Logger logger = LoggerFactory.getLogger(DeploymentDeleteNotifier.class);
	private ArchiveFileDeleteNotifier archiveDeleteNotifier = null;
	private ArchiveFileDeleteHandler archiveDeleteHandler = null;
	
	@Override
	public Boolean notifiesFor(ModelServiceOperation operation,
			ModelEntity payload) {
		if(operation==ModelServiceOperation.DELETE && Deployment.class.isAssignableFrom(payload.getClass()) ) {
			return true;
		}
		return false;
	}
	
	@Override
	public <E extends Event<Deployment>> void onAfterOperation(E event, List<ProcessingEvent> events) throws EventNotificationException {
	//public <E extends Event<Deployment>> void onInCommitBeforeCommit(E event, List<ProcessingEvent> events) throws EventNotificationException {
	
		Deployment deployment2Delete = (Deployment)event.getPayload();
		ApplicationArchive archive = deployment2Delete.getApplicationArchive();
		
		// if there are any other deployments with this hash,
		//   then we cannot yet delete it's archive yet at all.
		List<Deployment> deployments = archiveDeleteHandler.getModelManager().getModelService().findDeploymentsByApplicationArchive(archive);
		if(deployments.size()!=0) {
			return;
		} else {
			int deplCount = archiveDeleteHandler.getModelManager().getModelService().countDeploymentsByHashAndHashAlg(archive.getHash(), archive.getHashAlgorithm());
			if(deplCount==0) {
				// use the archive delete notifier to cleanup to cluster nodes
				archiveDeleteNotifier.notify(new ModelEntityEvent(ModelServiceOperation.DELETE,archive), events);
			}
		}
		
		// if there are any application versions with this archive, 
		//   then we cannot delete it's archive.
		List<ApplicationVersion> versions = archiveDeleteHandler.getModelManager().getModelService().findVersionsByApplicationArchive(archive);
		if(versions.size()!=0) {
			return;
		}
		
		// OK TO DELETE THIS APPLICATION'S COPY OF THE ARCHIVE, 
		// but possibly not the archive file...as it may be in use by another app
		
		// use the archive delete handler to cleanup localhost
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("archive", archive);
		try {
			int archivesWithHashAndAlg = archiveDeleteHandler.getModelManager().getModelService().countApplicationArchivesByHashAndHashAlg(archive.getHash(), archive.getHashAlgorithm());
			if(archivesWithHashAndAlg==1) {
				
				// use the delete handler to cleanup the admin servers copy
				GlobalSettings settings = archiveDeleteHandler.getModelManager().getGlobalSettings();
				archiveDeleteHandler.setFileSystemStoragePathPrefix(settings.getTemporaryStoragePath());
				archiveDeleteHandler.handle(new MapPayloadEvent(map));
			}			
			archiveDeleteHandler.getModelManager().delete(archive,events);
		} catch (EventHandlingException e) {
			throw new ClusterNotificationException("An event handling exception occured",e);
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
