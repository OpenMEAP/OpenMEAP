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

package com.openmeap.admin.web.backing;

import static com.openmeap.util.ParameterMapUtils.firstValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openmeap.admin.web.events.AddSubNavAnchorEvent;
import com.openmeap.constants.FormConstants;
import com.openmeap.event.MessagesEvent;
import com.openmeap.event.ProcessingEvent;
import com.openmeap.event.ProcessingTargets;
import com.openmeap.model.InvalidPropertiesException;
import com.openmeap.model.ModelManager;
import com.openmeap.model.ModelServiceImpl;
import com.openmeap.model.ModelServiceOperation;
import com.openmeap.model.dto.Application;
import com.openmeap.model.dto.ApplicationArchive;
import com.openmeap.model.dto.ApplicationVersion;
import com.openmeap.model.dto.Deployment;
import com.openmeap.model.dto.GlobalSettings;
import com.openmeap.model.event.ModelEntityEvent;
import com.openmeap.model.event.notifier.ArchiveFileUploadNotifier;
import com.openmeap.web.AbstractTemplatedSectionBacking;
import com.openmeap.web.ProcessingContext;
import com.openmeap.web.html.Anchor;

public class DeploymentListingsBacking extends AbstractTemplatedSectionBacking {
	
	private static String PROCESS_TARGET = ProcessingTargets.DEPLOYMENTS;
	
	private Logger logger = LoggerFactory.getLogger(DeploymentListingsBacking.class);
	
	private ModelManager modelManager = null;
	private ArchiveFileUploadNotifier archiveFileUploadNotifier = null;

	public Collection<ProcessingEvent> process(ProcessingContext context, Map<Object,Object> templateVariables, Map<Object, Object> parameterMap) {
		
		List<ProcessingEvent> events = new ArrayList<ProcessingEvent>();
		
		templateVariables.put(FormConstants.PROCESS_TARGET,PROCESS_TARGET);
		String appId          = firstValue(FormConstants.APP_ID,parameterMap);
		String appVerId       = firstValue("versionId",parameterMap);
		String deploymentType = firstValue("deploymentType",parameterMap);
		String processTarget  = firstValue(FormConstants.PROCESS_TARGET,parameterMap);

		Application app = null;
		try {
			app = modelManager.getModelService().findByPrimaryKey(Application.class,Long.valueOf(appId));
		} catch(NumberFormatException nfe) {
			events.add( new MessagesEvent("A valid applicationId must be supplied to either view or create deployments.") );
		}
		
		events.add( new AddSubNavAnchorEvent(new Anchor("?bean=addModifyAppPage&applicationId="+app.getId(),"View/Modify Application","View/Modify Application")) );
		events.add( new AddSubNavAnchorEvent(new Anchor("?bean=appVersionListingsPage&applicationId="+app.getId(),"Version Listings","Version Listings")) );
		
		// TODO: I'm pretty sure I should create new deployments elsewhere and forward to here from there.
		if( deploymentType!=null && PROCESS_TARGET.compareTo(processTarget)==0 && app!=null ) {
			
			ApplicationVersion version = null;
			try {
				version = modelManager.getModelService().findByPrimaryKey(ApplicationVersion.class,Long.valueOf(appVerId));
			} catch(NumberFormatException nfe) {
				events.add( new MessagesEvent("A valid versionId must be supplied to create a deployment.") );
			}			
			
			if( version!=null ) {
				Deployment depl = createDeployment(version,deploymentType);
				pushArchiveToClusterForDeployment(depl,events);
				
				try {
					depl = modelManager.addModify(depl,events);
					events.add( new MessagesEvent("Deployment successfully create!") );
				} catch (PersistenceException pe) {
					Throwable root = ExceptionUtils.getRootCause(pe);
					events.add( new MessagesEvent("An exception was thrown creating the deployment: "+root.getMessage()));
				} catch (InvalidPropertiesException pe) {
					Throwable root = ExceptionUtils.getRootCause(pe);
					events.add( new MessagesEvent("An exception was thrown creating the deployment: "+root.getMessage()));
				}
			}
		}
		
		// making sure to order the deployments by date
		if( app!=null && app.getDeployments()!=null ) {
			templateVariables.put("deployments", modelManager.getModelService().getOrderedDeployments(app, "getDeployments", new Deployment.DateComparator()));
		}
		
		return events;
	}
	
	private Deployment createDeployment(ApplicationVersion version, String deploymentType) {
		GlobalSettings settings = modelManager.getGlobalSettings();
		Deployment depl = new Deployment();
		depl.setType( Deployment.Type.valueOf(deploymentType) );
		depl.setApplicationVersion(version);
		depl.setHash(version.getArchive().getHash());
		depl.setHashAlgorithm(version.getArchive().getHashAlgorithm());
		depl.setDownloadUrl(version.getArchive().getDirectDownloadUrl(settings));
		depl.setCreateDate(new java.util.Date());
		version.getApplication().addDeployment(depl);
		return depl;
	}
	
	private void pushArchiveToClusterForDeployment(Deployment depl, List<ProcessingEvent> events) {
		try {
			ApplicationArchive archive = new ApplicationArchive();
			archive.setHash(depl.getHash());
			archive.setHashAlgorithm(depl.getHashAlgorithm());
			archiveFileUploadNotifier.notify(new ModelEntityEvent(ModelServiceOperation.SAVE_OR_UPDATE,archive), events);
		} catch (Exception e) {
			logger.error("An exception occurred pushing the new archive to cluster nodes: {}",e);
			events.add(new MessagesEvent(String.format("An exception occurred pushing the new archive to cluster nodes: %s",e.getMessage())));
		}
	}
	
	/*
	 * ACCESSORS	
	 */
	
	public void setModelManager(ModelManager modelManager) {
		this.modelManager = modelManager;
	}
	public ModelManager getModelManager() {
		return modelManager;
	}
	
	public ArchiveFileUploadNotifier getArchiveFileUploadNotifier() {
		return archiveFileUploadNotifier;
	}
	public void setArchiveFileUploadNotifier(
			ArchiveFileUploadNotifier archiveFileUploadNotifier) {
		this.archiveFileUploadNotifier = archiveFileUploadNotifier;
	}
}
