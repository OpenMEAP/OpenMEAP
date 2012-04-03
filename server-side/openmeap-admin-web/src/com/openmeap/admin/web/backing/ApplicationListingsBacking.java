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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.openmeap.event.ProcessingEvent;
import com.openmeap.event.ProcessingTargets;
import com.openmeap.model.ModelManager;
import com.openmeap.model.dto.Application;
import com.openmeap.model.dto.Deployment;
import com.openmeap.web.AbstractTemplatedSectionBacking;
import com.openmeap.web.ProcessingContext;
import com.openmeap.web.html.Anchor;

public class ApplicationListingsBacking extends AbstractTemplatedSectionBacking {

	private static String PROCESS_TARGET = ProcessingTargets.LISTING_APP;

	private ModelManager modelManager = null;
	
	public ApplicationListingsBacking() {
		setProcessingTargetIds(Arrays.asList(new String[]{PROCESS_TARGET}));
	}
	
	public void setModelManager(ModelManager modelManager) {
		this.modelManager = modelManager;
	}
	public ModelManager getModelManager() {
		return modelManager;
	}
	
	public Collection<ProcessingEvent> process(ProcessingContext context,
			Map<Object, Object> templateVariables,
			Map<Object, Object> parameterMap) {
		
		List<Application> applications = modelManager.getModelService().findAll(Application.class);
		
		if( applications!=null && applications.size()>0 ) {
			
			Map<String,Anchor> deplUrls = new HashMap<String,Anchor>();
			
			for( Application app : applications ) {
				Deployment d = modelManager.getModelService().getLastDeployment(app);
				if( d!=null ) {
					deplUrls.put( app.getName(),
							new Anchor("?bean=addModifyAppVersionPage"
								+"&applicationId="+app.getId()
								+"&versionId="+d.getApplicationVersion().getId(),
							d.getApplicationVersion().getIdentifier(),
							d.getApplicationVersion().getIdentifier()));
				}
			}
			
			templateVariables.put("applications", applications);
			templateVariables.put("deplUrls", deplUrls);
		}
		
		return null;
	}
}
