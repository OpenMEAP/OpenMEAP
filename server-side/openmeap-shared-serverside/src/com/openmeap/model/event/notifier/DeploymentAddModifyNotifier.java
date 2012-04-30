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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.PersistenceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openmeap.cluster.ClusterNotificationException;
import com.openmeap.constants.UrlParamConstants;
import com.openmeap.event.Event;
import com.openmeap.event.EventNotificationException;
import com.openmeap.event.MessagesEvent;
import com.openmeap.event.ProcessingEvent;
import com.openmeap.file.FileOperationManager;
import com.openmeap.model.ArchiveFileHelper;
import com.openmeap.model.InvalidPropertiesException;
import com.openmeap.model.ModelEntity;
import com.openmeap.model.ModelManager;
import com.openmeap.model.ModelServiceOperation;
import com.openmeap.model.dto.Application;
import com.openmeap.model.dto.ApplicationArchive;
import com.openmeap.model.dto.Deployment;
import com.openmeap.model.event.ModelEntityEventAction;

public class DeploymentAddModifyNotifier extends AbstractArchiveFileEventNotifier<Deployment> {

	private static Logger logger = LoggerFactory.getLogger(DeploymentAddModifyNotifier.class);
	
	private FileOperationManager fileManager;
	
	public void setFileManager(FileOperationManager fileManager) {
		this.fileManager = fileManager;
	}

	@Override
	protected String getEventActionName() {
		return ModelEntityEventAction.ARCHIVE_UPLOAD.getActionName();
	}
		
	@Override
	protected void addRequestParameters(ModelEntity modelEntity, Map<String,Object> parms) {
		ApplicationArchive archive = (ApplicationArchive)(((Deployment) modelEntity).getApplicationArchive());
		parms.put(UrlParamConstants.APPARCH_FILE, archive.getFile(getModelManager().getGlobalSettings().getTemporaryStoragePath()));
		super.addRequestParameters(archive, parms);
	}
	
	@Override
	public Boolean notifiesFor(ModelServiceOperation operation, ModelEntity payload) {
		return operation==ModelServiceOperation.SAVE_OR_UPDATE && Deployment.class.isAssignableFrom(payload.getClass());
	}
	
	@Override
	public <E extends Event<Deployment>> void onAfterOperation(E event, List<ProcessingEvent> events) throws EventNotificationException {
		
		ArchiveFileHelper.maintainFileSystemCleanliness(
				getModelManager(),
				fileManager,
				((Deployment)event.getPayload()).getApplicationArchive(), 
				events);
		
		maintainDeploymentHistoryLength(((Deployment)event.getPayload()).getApplication(),events);
	}
	
	@Override
	public <E extends Event<Deployment>> void onInCommitBeforeCommit(final E event, List<ProcessingEvent> events) throws ClusterNotificationException {
		notify(event,events);
	}
	
	@Override
	public <E extends Event<Deployment>> void notify(final E event, List<ProcessingEvent> events) throws ClusterNotificationException {
		ApplicationArchive archive = (ApplicationArchive)((Deployment)event.getPayload()).getApplicationArchive();
		File archiveFile = archive.getFile(getModelManager().getGlobalSettings().getTemporaryStoragePath());
		if( !archiveFile.exists() ) {
			String msg = String.format("The archive file %s cannot be found.  This could be because you opted to fill in the version details yourself.",archiveFile.getAbsoluteFile());
			logger.warn(msg);
			events.add(new MessagesEvent(msg));
			return;
		}
		super.notify(event, events);
	}
	
	/**
	 * Trim the deployment history table. Deleting old archives as we go.
	 * @param app
	 * @throws EventNotificationException 
	 * @throws PersistenceException
	 * @throws InvalidPropertiesException
	 */
	private Boolean maintainDeploymentHistoryLength(Application app,List<ProcessingEvent> events) throws EventNotificationException {

		getModelManager().getModelService().refresh(app);

		Integer lengthToMaintain = app.getDeploymentHistoryLength();
		List<Deployment> deployments = app.getDeployments();
		if( deployments!=null && deployments.size() > lengthToMaintain ) {

			Integer currentSize = deployments.size();

			List<Deployment> newDeployments = new ArrayList<Deployment>(deployments.subList(currentSize-lengthToMaintain,currentSize));
			List<Deployment> oldDeployments = new ArrayList<Deployment>(deployments.subList(0,currentSize-lengthToMaintain));

			for( Deployment deployment : oldDeployments ) {
				getModelManager().delete(deployment,events);
			}

			for( Deployment deployment : newDeployments ) {
				app.getDeployments().add(deployment);
			}

			try {
				getModelManager().addModify(app,events);
			} catch (InvalidPropertiesException e) {
				throw new EventNotificationException(e);
			} catch (PersistenceException e) {
				throw new EventNotificationException(e);
			}

			return true;
		}
		return false;
	}
}
