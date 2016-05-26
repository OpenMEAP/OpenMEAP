/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2016 OpenMEAP, Inc.                                   #
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

package com.openmeap.admin.web.backing;

import static com.openmeap.util.ParameterMapUtils.firstValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openmeap.Authorizer;
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
				
				Deployment depl = createDeployment(firstValue("userPrincipalName",parameterMap),version,deploymentType);
				
				try {
					modelManager.begin();
					depl = modelManager.addModify(depl,events);
					modelManager.commit(events);
					events.add( new MessagesEvent("Deployment successfully completed.") );
				} catch (Exception pe) {
					modelManager.rollback();
					Throwable root = ExceptionUtils.getRootCause(pe);
					events.add( new MessagesEvent(
						String.format("An exception was thrown creating the deployment: %s %s",root.getMessage(),ExceptionUtils.getStackTrace(root))));
				} 
			}
		}
		
		// making sure to order the deployments by date
		if( app!=null && app.getDeployments()!=null ) {
			List<Deployment> deployments = modelManager.getModelService().findDeploymentsByApplication(app);
			Collections.sort(deployments,new Deployment.DateComparator());
			templateVariables.put("deployments", deployments);
			
			GlobalSettings settings = modelManager.getGlobalSettings();
			
			Map<String,String> urls = new HashMap<String,String>();
			for(Deployment depl : deployments) {
				urls.put(depl.getApplicationArchive().getHash(), depl.getApplicationArchive().getDownloadUrl(settings));
			}
			templateVariables.put("deployments", deployments);
			templateVariables.put("archiveUrls", urls);
		}
		
		return events;
	}
	
	private Deployment createDeployment(String creator, ApplicationVersion version, String deploymentType) {
		GlobalSettings settings = modelManager.getGlobalSettings();
		Deployment depl = new Deployment();
		depl.setType( Deployment.Type.valueOf(deploymentType) );
		depl.setApplicationArchive(version.getArchive());
		depl.setCreateDate(new java.util.Date());
		depl.setCreator(creator);
		depl.setVersionIdentifier(version.getIdentifier());
		version.getApplication().addDeployment(depl);
		return depl;
	}
	
	/*
	 * ACCESSORS	
	 */
	
	public void setModelManager(ModelManager modelManager) {
		this.modelManager = modelManager;
	}
}
