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

import java.io.File;
import java.util.*;

import org.apache.commons.lang.exception.ExceptionUtils;

import com.openmeap.admin.web.ProcessingTargets;
import com.openmeap.Authorizer;
import com.openmeap.model.*;
import com.openmeap.model.dto.Application;
import com.openmeap.model.dto.ApplicationVersion;
import com.openmeap.model.dto.Deployment;
import com.openmeap.model.dto.GlobalSettings;
import com.openmeap.util.ParameterMapUtils;
import com.openmeap.web.*;
import com.openmeap.admin.web.backing.event.MessagesEvent;
import com.openmeap.admin.web.backing.event.AddSubNavAnchorEvent;
import com.openmeap.web.html.*;
import javax.persistence.PersistenceException;

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
		
		if( ParameterMapUtils.notEmpty("applicationId", parameterMap) ) {

			Application app = modelManager.getModelService().findByPrimaryKey(Application.class, Long.valueOf( ParameterMapUtils.firstValue("applicationId", parameterMap) ) );
			if( app!=null ) {
				
				Boolean mayUpdate = modelManager.getAuthorizer().may(Authorizer.Action.MODIFY, app);
				templateVariables.put("mayUpdate", mayUpdate);
				
				Deployment lastDeployment = modelManager.getModelService().getLastDeployment(app);
				Long currentVersionId = null;
				if( lastDeployment!=null && lastDeployment.getApplicationVersion()!=null ) 
					currentVersionId = lastDeployment.getApplicationVersion().getId();
				currentVersionId = currentVersionId!=null ? currentVersionId:(-1);
				
				templateVariables.put("application", app);
				templateVariables.put("processTarget", ProcessingTargets.DEPLOYMENTS);
				templateVariables.put("currentVersionId", currentVersionId);
				
				if( app.getVersions()!=null && app.getVersions().size()>0 ) {
					createVersionsDisplayLists(app,templateVariables);
				} else {
					events.add( new MessagesEvent( "Application with id "+ParameterMapUtils.firstValue("applicationId", parameterMap)+" has no versions associated to it") );
				}
				if( modelManager.getAuthorizer().may(Authorizer.Action.CREATE, new Application()) ) {
					events.add( new AddSubNavAnchorEvent( new Anchor("?bean=addModifyAppVersionPage&applicationId="+app.getId(),"Add an Application Version","Create Application Version")) );
				}
				
				Anchor deploymentHistoryAnchor = new Anchor("?bean=deploymentListingsPage&applicationId="+app.getId(),"Deployment History","Deployment History");
				templateVariables.put("deploymentsAnchor",deploymentHistoryAnchor);
				events.add( new AddSubNavAnchorEvent(deploymentHistoryAnchor));
				
			} else {
				events.add( new MessagesEvent( "Application with id "+ParameterMapUtils.firstValue("applicationId", parameterMap)+" not found") );
			}
		} else {
			events.add( new MessagesEvent( "An application must be selected") );
		}
		
		return events;
	}
	
	public void createVersionsDisplayLists(Application app, Map<Object,Object> templateVariables) {

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
