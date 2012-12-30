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

package com.openmeap.admin.web.backing;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.openmeap.Authorizer;
import com.openmeap.admin.web.events.AddSubNavAnchorEvent;
import com.openmeap.constants.FormConstants;
import com.openmeap.event.MessagesEvent;
import com.openmeap.event.ProcessingEvent;
import com.openmeap.event.ProcessingTargets;
import com.openmeap.model.ModelManager;
import com.openmeap.model.dto.Application;
import com.openmeap.model.dto.ApplicationVersion;
import com.openmeap.model.dto.Deployment;
import com.openmeap.model.dto.GlobalSettings;
import com.openmeap.util.ParameterMapUtils;
import com.openmeap.web.AbstractTemplatedSectionBacking;
import com.openmeap.web.ProcessingContext;
import com.openmeap.web.html.Anchor;

public class ApplicationVersionListingsBacking extends AbstractTemplatedSectionBacking {

	private static String PROCESS_TARGET = ProcessingTargets.LISTING_APPVER; 
	
	public ApplicationVersionListingsBacking() {
		setProcessingTargetIds(Arrays.asList(new String[]{PROCESS_TARGET}));
	}
	
	private ModelManager modelManager = null;
	
	public void setModelManager(ModelManager modelManager) {
		this.modelManager = modelManager;
	}
	public ModelManager getModelManager() {
		return modelManager;
	}
	
	public Collection<ProcessingEvent> process(ProcessingContext context,
			Map<Object, Object> templateVariables,
			Map<Object, Object> parameterMap) {
		
		List<ProcessingEvent> events = new ArrayList<ProcessingEvent>();
		
		if( ParameterMapUtils.notEmpty(FormConstants.APP_ID, parameterMap) ) {

			Application app = modelManager.getModelService().findByPrimaryKey(Application.class, Long.valueOf( ParameterMapUtils.firstValue(FormConstants.APP_ID, parameterMap) ) );
			if( app!=null ) {
				
				setupMayCreateDeployments(templateVariables,app,events);
				
				Deployment lastDeployment = modelManager.getModelService().getLastDeployment(app);
				String currentVersionId = null;
				if( lastDeployment!=null && lastDeployment.getVersionIdentifier()!=null ) {
					currentVersionId = lastDeployment.getVersionIdentifier();
				}
				currentVersionId = currentVersionId != null ? currentVersionId : "";
				
				templateVariables.put("application", app);
				templateVariables.put(FormConstants.PROCESS_TARGET, ProcessingTargets.DEPLOYMENTS);
				templateVariables.put("currentVersionId", currentVersionId);
				
				if( app.getVersions()!=null && app.getVersions().size()>0 ) {
					createVersionsDisplayLists(app,templateVariables);
				} else {
					events.add( new MessagesEvent( "Application with id "+ParameterMapUtils.firstValue(FormConstants.APP_ID, parameterMap)+" has no versions associated to it") );
				}
				
				
				if( modelManager.getAuthorizer().may(Authorizer.Action.CREATE, new ApplicationVersion()) ) {
					events.add( new AddSubNavAnchorEvent( new Anchor("?bean=addModifyAppVersionPage&applicationId="+app.getId(),"Create new version","Create new version")) );
				}
				events.add( new AddSubNavAnchorEvent(new Anchor("?bean=addModifyAppPage&applicationId="+app.getId(),"View/Modify Application","View/Modify Application")) );
				
				Anchor deploymentHistoryAnchor = new Anchor("?bean=deploymentListingsPage&applicationId="+app.getId(),"Deployment History","Deployment History");
				templateVariables.put("deploymentsAnchor",deploymentHistoryAnchor);
				events.add( new AddSubNavAnchorEvent(deploymentHistoryAnchor));
				
			} else {
				events.add( new MessagesEvent( "Application with id "+ParameterMapUtils.firstValue(FormConstants.APP_ID, parameterMap)+" not found") );
			}
		} else {
			events.add( new MessagesEvent( "An application must be selected") );
		}
		
		return events;
	}
	
	private Boolean setupMayCreateDeployments(Map<Object,Object> templateVariables, Application app, List<ProcessingEvent> events) {
		Deployment authTestDepl = new Deployment();
		authTestDepl.setApplication(app);
		Boolean mayCreateDeployments = modelManager.getAuthorizer().may(Authorizer.Action.CREATE, authTestDepl);
		if( !mayCreateDeployments ) {
			events.add( new MessagesEvent("NOTE: Current user does not have permissions to create deployments") );
		}
		authTestDepl = null;
		templateVariables.put("mayCreateDeployments",mayCreateDeployments);
		return mayCreateDeployments;
	}
	
	private void createVersionsDisplayLists(Application app, Map<Object,Object> templateVariables) {

		templateVariables.put("versions", app.getVersions());
		GlobalSettings settings = modelManager.getGlobalSettings();
		Map<String,String> downloadUrls = new HashMap<String,String>();
		Map<String,String> viewUrls = new HashMap<String,String>();
		for( ApplicationVersion version : app.getVersions().values() ) {
			if( version.getArchive()==null ) {
				viewUrls.put(version.getIdentifier(), "");
			} else {
				downloadUrls.put(version.getIdentifier(), version.getArchive().getDownloadUrl(settings));
				File exploded = version.getArchive().getExplodedPath(settings.getTemporaryStoragePath());
				if( exploded!=null && exploded.exists() ) {
					viewUrls.put(version.getIdentifier(), version.getArchive().getViewUrl(settings));
				}
			}
		}
		templateVariables.put("downloadUrls",downloadUrls);
		templateVariables.put("viewUrls", viewUrls);
	}
}
