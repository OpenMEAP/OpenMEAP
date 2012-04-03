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
import java.util.List;
import java.util.Map;

import javax.persistence.PersistenceException;

import com.openmeap.Authorizer;
import com.openmeap.admin.web.events.AddSubNavAnchorEvent;
import com.openmeap.constants.FormConstants;
import com.openmeap.event.MessagesEvent;
import com.openmeap.event.ProcessingEvent;
import com.openmeap.event.ProcessingTargets;
import com.openmeap.model.InvalidPropertiesException;
import com.openmeap.model.ModelManager;
import com.openmeap.model.dto.Application;
import com.openmeap.model.dto.ApplicationVersion;
import com.openmeap.util.ParameterMapUtils;
import com.openmeap.web.AbstractTemplatedSectionBacking;
import com.openmeap.web.ProcessingContext;
import com.openmeap.web.html.Anchor;

/**
 * Mediates information between the ModelManager and the add/modify form.
 * 
 * Any business logic specific to the creation/modification of an Application
 * should be implemented in the ModelManagerImpl.
 *  
 * @author schang
 */
public class AddModifyApplicationBacking extends AbstractTemplatedSectionBacking {

	private static String PROCESS_TARGET = ProcessingTargets.ADDMODIFY_APP;
	
	private ModelManager modelManager = null;
	
	public void setModelManager(ModelManager modelManager) {
		this.modelManager = modelManager;
	}
	public ModelManager getModelManager() {
		return modelManager;
	}
	
	/**
	 * With the first of the bean name matching "addModifyApp", there are
	 * three ways to access this:
	 *    - request has applicationId and processTarget - modifying an application
	 *    - request has applicationId only              - pulling up an application to modify
	 *    - request has processTarget only              - submitting a brand new application  
	 *
	 * See the WEB-INF/ftl/form-application-addmodify.ftl for input/output parameters.
	 *    
	 * @param context Not referenced at all, may be null
	 * @param templateVariables Variables output to for the view
	 * @param parameterMap Parameters passed in to drive processing
	 * @return on errors, returns an array of error processingevents
	 * @see TemplatedSectionBacking::process()
	 */
	public Collection<ProcessingEvent> process(ProcessingContext context, Map<Object,Object> templateVariables, Map<Object, Object> parameterMap) {
		
		List<ProcessingEvent> events = new ArrayList<ProcessingEvent>();
		
		templateVariables.put(FormConstants.PROCESS_TARGET,PROCESS_TARGET);
		
		Application app = new Application();
		if( ParameterMapUtils.notEmpty(FormConstants.APP_ID,parameterMap) ) {
			app = modelManager.getModelService().findByPrimaryKey(Application.class, Long.valueOf( ParameterMapUtils.firstValue(FormConstants.APP_ID, parameterMap) ) );
		}
		
		Boolean mayCreate = modelManager.getAuthorizer().may(Authorizer.Action.CREATE, new Application());
		Boolean mayModify = modelManager.getAuthorizer().may(Authorizer.Action.MODIFY, app);
		Boolean willProcess = mayCreate || mayModify;
		if( !willProcess ) {
			events.add( new MessagesEvent("Current user does not have permissions to make changes here") );
		}
		templateVariables.put("willProcess",willProcess);
		
		// if the request is targeting this section or the primary page of this section
		// the user is submitting the form for either an add or modify
		if( ParameterMapUtils.notEmpty(FormConstants.PROCESS_TARGET,parameterMap) 
				&& PROCESS_TARGET.equals(((String[])parameterMap.get(FormConstants.PROCESS_TARGET))[0]) 
				&& willProcess ) {
			
			app = createApplicationFromParameters(app,parameterMap, events);
			
			if( events.size()==0 ) {
				try {
					app = modelManager.addModify(app,events);
					events.add( new MessagesEvent("Application successfully created/modified!") );
				} catch( InvalidPropertiesException ipe ) {
					events.add( new MessagesEvent(ipe.getMessage()) );
				} catch( PersistenceException pe ) {
					events.add( new MessagesEvent(pe.getMessage()) );							
				}
			}
			
			if( app==null && ParameterMapUtils.notEmpty(FormConstants.APP_ID,parameterMap) )
				app = modelManager.getModelService().findByPrimaryKey(Application.class, 
						Long.valueOf(ParameterMapUtils.firstValue(FormConstants.APP_ID, parameterMap)));
			
			if( ParameterMapUtils.notEmpty(FormConstants.DELETE,parameterMap) && ParameterMapUtils.notEmpty("deleteConfirm",parameterMap) ) {
				
					if( ParameterMapUtils.firstValue("deleteConfirm", parameterMap).equals(FormConstants.APP_DELETE_CONFIRM_TEXT) ) {
						
						modelManager.delete(app,events);
						events.add( new MessagesEvent("Application successfully deleted!") );
						app = null;
						// we remove the applicationId parameter, so that the form can populate empty
						parameterMap.remove(FormConstants.APP_ID);
					} else {
						
						events.add( new MessagesEvent("You must confirm your desire to delete by typing in the delete confirmation message.") );
				}
			} 
		}
		
		// the user is visiting the page to view or modify an application
		else if( ParameterMapUtils.notEmpty(FormConstants.APP_ID,parameterMap) ) {
			app = modelManager.getModelService().findByPrimaryKey(Application.class, Long.valueOf( ParameterMapUtils.firstValue(FormConstants.APP_ID, parameterMap) ) );
		}
		
		if( app == null && ParameterMapUtils.notEmpty(FormConstants.APP_ID,parameterMap) ) {
			events.add( new MessagesEvent("Application with id "+ParameterMapUtils.firstValue(FormConstants.APP_ID, parameterMap)+" not found") );
		} else if( app!=null && app.getId()!=null ) { 
			if( app.getVersions()!=null && app.getVersions().size()>0 )
				events.add( 
						new AddSubNavAnchorEvent(
								new Anchor("?bean=appVersionListingsPage&applicationId="+app.getId(),
										"View Application Versions",
										"View Application Versions")) );
			
			// in order to create the 
			ApplicationVersion testVer = new ApplicationVersion();
			testVer.setApplication(app);
			Boolean mayCreateVersions = modelManager.getAuthorizer().may(Authorizer.Action.CREATE, testVer);
			if( mayCreateVersions ) {
				events.add( 
						new AddSubNavAnchorEvent( 
								new Anchor("?bean=addModifyAppVersionPage&applicationId="+app.getId(),
										"Add an Application Version",
										"Create Application Version")) );
			}
		}
		
		fillInVariablesFromApplication(templateVariables,app);
	
		return events;
	}
	
	/**
	 * Create a new application object from the parameters passed in
	 * 
	 * @param parameterMap
	 * @return
	 */
	private Application createApplicationFromParameters(Application app, Map<Object,Object> parameterMap, List<ProcessingEvent> events) {
		if( app == null ) {
			app = new Application();
		}
		app.setName(ParameterMapUtils.firstValue("name",parameterMap));
		app.setAdmins(ParameterMapUtils.firstValue(FormConstants.APP_ADMINS,parameterMap));
		app.setVersionAdmins(ParameterMapUtils.firstValue(FormConstants.APP_VERSIONADMINS,parameterMap));
		app.setDescription(ParameterMapUtils.firstValue(FormConstants.APP_DESCRIPTION,parameterMap));
		app.setInitialVersionIdentifier(ParameterMapUtils.firstValue("initialVersionIdentifier",parameterMap));
		
		String deploymentHistoryLength = ParameterMapUtils.firstValue(FormConstants.APP_DEPL_HIST_LEN,parameterMap);
		if( deploymentHistoryLength!=null && deploymentHistoryLength.trim().matches("[\\d]+") ) {
			app.setDeploymentHistoryLength(Integer.valueOf(deploymentHistoryLength.trim()));
		}
		
		// update the salt used for generating authentication tokens
		String salt = ParameterMapUtils.firstValue("proxyAuthSalt",parameterMap);
		String saltConf = ParameterMapUtils.firstValue("proxyAuthSaltConfirm",parameterMap);
		if( salt!=null && saltConf!=null ) {
			if( salt.length()>0 && saltConf.equals(salt) ) {
				app.setProxyAuthSalt(salt);
			} else if( !salt.equals(saltConf) ) {
				events.add( new MessagesEvent("Proxy authentication salt value not set.  Entries did not match.") );
			}
		}
		
		return app;
	}
	
	/**
	 * Populate the template variables with information from an application object instance
	 * 
	 * @param section
	 * @param app
	 */
	private void fillInVariablesFromApplication(Map<Object,Object> vars, Application app) {
		vars.put("application", app);
	}
}
