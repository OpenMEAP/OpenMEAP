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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.persistence.PersistenceException;

import org.apache.commons.lang.exception.ExceptionUtils;

import com.openmeap.admin.web.ProcessingTargets;
import com.openmeap.admin.web.backing.event.MessagesEvent;
import com.openmeap.model.InvalidPropertiesException;
import com.openmeap.model.ModelManager;
import com.openmeap.model.ModelServiceImpl;
import com.openmeap.model.dto.Application;
import com.openmeap.model.dto.ApplicationVersion;
import com.openmeap.model.dto.Deployment;
import com.openmeap.model.dto.GlobalSettings;
import com.openmeap.web.AbstractTemplatedSectionBacking;
import com.openmeap.web.ProcessingContext;
import com.openmeap.web.ProcessingEvent;
import static com.openmeap.util.ParameterMapUtils.*;

public class DeploymentListingsBacking extends AbstractTemplatedSectionBacking {
	private static String PROCESS_TARGET = ProcessingTargets.DEPLOYMENTS;
	
	private ModelManager modelManager = null;
	
	public void setModelManager(ModelManager modelManager) {
		this.modelManager = modelManager;
	}
	public ModelManager getModelManager() {
		return modelManager;
	}
	
	public Collection<ProcessingEvent> process(ProcessingContext context, Map<Object,Object> templateVariables, Map<Object, Object> parameterMap) {
		
		List<ProcessingEvent> events = new ArrayList<ProcessingEvent>();
		
		templateVariables.put("processTarget",PROCESS_TARGET);
		String appId          = firstValue("applicationId",parameterMap);
		String appVerId       = firstValue("versionId",parameterMap);
		String deploymentType = firstValue("deploymentType",parameterMap);
		String processTarget  = firstValue("processTarget",parameterMap);

		Application app = null;
		try {
			app = modelManager.findApplication(Long.valueOf(appId));
		} catch(NumberFormatException nfe) {
			events.add( new MessagesEvent("A valid applicationId must be supplied to either view or create deployments.") );
		}
		
		if( deploymentType!=null && PROCESS_TARGET.compareTo(processTarget)==0 && app!=null ) {
			
			ApplicationVersion version = null;
			try {
				version = modelManager.findApplicationVersion(Long.valueOf(appVerId));
			} catch(NumberFormatException nfe) {
				events.add( new MessagesEvent("A valid versionId must be supplied to create a deployment.") );
			}			
			
			if( version!=null ) {
				GlobalSettings settings = modelManager.getGlobalSettings();
				Deployment depl = new Deployment();
				depl.setType( Deployment.Type.valueOf(deploymentType) );
				depl.setApplicationVersion(version);
				depl.setHash(version.getArchive().getHash());
				depl.setHashAlgorithm(version.getArchive().getHashAlgorithm());
				depl.setDownloadUrl(version.getArchive().getDirectDownloadUrl(settings));
				depl.setCreateDate(new java.util.Date());
				app.addDeployment(depl);
				try {
					app = modelManager.addModify(app);
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
		
		if( app!=null && app.getDeployments()!=null ) {
			((ModelServiceImpl)modelManager.getModelService()).getEntityManager().getTransaction().begin();
			((ModelServiceImpl)modelManager.getModelService()).getEntityManager().merge(app);
			List<Deployment> depls = app.getDeployments();
			Collections.sort( depls, new Comparator<Deployment>() {
				public int compare(Deployment arg0, Deployment arg1) {
					return arg0.getCreateDate().compareTo(arg1.getCreateDate()) > 0 ? -1 : 1;
				}
			});
			((ModelServiceImpl)modelManager.getModelService()).getEntityManager().getTransaction().commit();
			templateVariables.put("deployments", depls);
		}
		
		return events;
	}
}
