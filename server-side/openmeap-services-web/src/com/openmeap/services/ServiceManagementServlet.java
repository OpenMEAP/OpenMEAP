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

package com.openmeap.services;

import java.io.*;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.openmeap.Event;
import com.openmeap.EventHandler;
import com.openmeap.EventHandlingException;
import com.openmeap.constants.UrlParamConstants;
import com.openmeap.model.*;
import com.openmeap.model.dto.ApplicationArchive;
import com.openmeap.model.dto.GlobalSettings;
import com.openmeap.model.event.ArchiveDeleteNotifiedEvent;
import com.openmeap.model.event.ArchiveUploadNotifiedEvent;
import com.openmeap.model.event.ModelEntityEventAction;
import com.openmeap.model.event.handler.*;
import com.openmeap.util.AuthTokenProvider;
import static com.openmeap.util.ParameterMapUtils.*;
import com.openmeap.util.ServletUtils;

/**
 * Used to notify that model items have been modified in the administrative interface.
 * 
 * Because the admin server is separated from the web-services,
 * we needed to create a means of notifying the web-services that
 * items in the persistence context were stale.
 * 
 * @author schang
 */
public class ServiceManagementServlet extends HttpServlet {

	private Logger logger = LoggerFactory.getLogger(ServiceManagementServlet.class);
	
	private ModelManager modelManager = null;
	private String authSalt = null;
	private ModelServiceRefreshHandler modelServiceRefreshHandler = null;
	private ArchiveUploadHandler archiveUploadHandler = null;
	private ArchiveDeleteHandler archiveDeleteHandler = null;
	
	private WebApplicationContext context = null;
	
	public void init() {
		context = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		modelManager = (ModelManager)context.getBean("modelManager");
		authSalt = modelManager.getGlobalSettings().getServiceManagementAuthSalt();
		modelServiceRefreshHandler = (ModelServiceRefreshHandler)context.getBean("modelServiceRefreshHandler");
		archiveUploadHandler = (ArchiveUploadHandler)context.getBean("archiveUploadHandler");
		archiveDeleteHandler = (ArchiveDeleteHandler)context.getBean("archiveDeleteHandler");
	}
	
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		String action = request.getParameter("action");
		
		if( action==null )
			action="";
		
		PrintWriter os = new PrintWriter(response.getOutputStream());
		GlobalSettings settings = modelManager.getGlobalSettings();
		
		if( ! authenticates(request) ) {
			
			logger.error("Request failed to authenticate ",request);
			os.print("<?xml version=\"1.0\"?>\n<result status=\"failure\" reason=\"Authentication failed\"/>");
			
		} else if( request.getParameter("clearPersistenceContext")!=null && context instanceof AbstractApplicationContext ) {
			
			clearPersistenceContext();
			
		} else if( action.equals(ModelEntityEventAction.ARCHIVE_UPLOAD.getActionName()) ) {
			
			Map<Object,Object> paramMap = ServletUtils.cloneParameterMap(settings, request);
			handleArchiveEvent(archiveUploadHandler, new ArchiveUploadNotifiedEvent(paramMap), os, paramMap);
			
		} else if( action.equals(ModelEntityEventAction.ARCHIVE_DELETE.getActionName()) ) {
			
			Map<Object,Object> paramMap = ServletUtils.cloneParameterMap(settings, request);
			handleArchiveEvent(archiveDeleteHandler, new ArchiveDeleteNotifiedEvent(paramMap), os, paramMap);
			
		} else if( action.equals(ModelEntityEventAction.MODEL_REFRESH.getActionName()) ) {
			
			refresh(os,request,response);
			
		}
		
		os.flush();
		os.close();
	}
	
	private void clearPersistenceContext() {
		if( logger.isInfoEnabled() ) {
			logger.info("Received request to clear the persistence context");
		}
		ModelServiceImpl ms = (ModelServiceImpl)((AbstractApplicationContext)context).getBean("modelService");
		ms.clearPersistenceContext();
	}
	
	@SuppressWarnings(value={ "rawtypes", "unchecked" })
	private void handleArchiveEvent(EventHandler eventHandler, Event event, PrintWriter os, Map<Object,Object> paramMap) throws IOException {
		
		String hash = firstValue(UrlParamConstants.APPARCH_HASH,paramMap);
		String hashType = firstValue(UrlParamConstants.APPARCH_HASH_ALG,paramMap);
		if( logger.isInfoEnabled() ) {
			logger.info("Received request archive upload notification "+hashType+":"+hash);
		}
		
		if( hash!=null && hashType!=null ) {
			ApplicationArchive arch = new ApplicationArchive();
			arch.setHash(hash);
			arch.setHashAlgorithm(hashType);
			
			try {
				
				// TODO: still not happy with the ArchiveUploadNotifiedEvent argument...maybe a Message type class??
				paramMap.put("archive",arch);
				eventHandler.handle(event);
			} catch(EventHandlingException che) {
				logger.error("Exception thrown handling ArchiveUploadEvent",che);
				os.print("<?xml version=\"1.0\"?>\n<result status=\"failure\" reason=\"Exception occurred handing the ArchiveUploadEvent\"/>\n");
			}
		}
	}
	
	/**
	 * Handles the notification that this node should refresh some object from the database.
	 * 
	 * @param os
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	private void refresh(PrintWriter os, HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		response.setContentType("text/xml");
		
		String refreshType = (String)request.getParameter("type");
		String objectId = (String)request.getParameter("id");
		
		if( refreshType!=null && objectId!=null ) {
		
			if( logger.isInfoEnabled() ) {
				logger.info("Received request to refresh "+refreshType+" with id "+objectId,request);
			}
			
			try {
				modelServiceRefreshHandler.handleRefresh(refreshType,objectId);
				if( logger.isInfoEnabled() ) { 
					logger.info("Refresh for "+refreshType+" with id "+objectId+" was successful");
				}
				os.print("<?xml version=\"1.0\"?>\n<result status=\"success\"/>\n");	
			} catch( Exception e ) {
				logger.error("Exception occurred refreshing "+refreshType+" with object id "+objectId,e);
				os.print("<?xml version=\"1.0\"?>\n<result status=\"failure\" reason=\"Exception occurred refreshing "+refreshType+" with object id "+objectId+"\"/>\n");
			}	
			
		} else {
			logger.error("A request failed to specify all required parameters",request);
			os.print("<?xml version=\"1.0\"?>\n<result status=\"failure\" reason=\"Must specify refresh target class, object primary key, and authentication token.\"/>\n"); 
		}
	}
	
	private Boolean authenticates(HttpServletRequest arg0) {
		String authSalt = getAuthSalt();
		String auth = (String)arg0.getParameter(UrlParamConstants.AUTH_TOKEN);
		Boolean isGood = AuthTokenProvider.validateAuthToken(authSalt, auth);
		return (auth!=null && isGood);
	}
	
	// ACCESSORS
	
	public void setModelManager(ModelManager manager) {
		modelManager = manager;
	}
	public ModelManager getModelManager() {
		return modelManager;
	}
	
	public String getAuthSalt() {
		return modelManager.getGlobalSettings().getServiceManagementAuthSalt();
	}
	
	public void setModelServiceRefreshHandler(ModelServiceRefreshHandler refreshHandler) {
		modelServiceRefreshHandler = refreshHandler;
	}
	public ModelServiceRefreshHandler getModelServiceRefreshHandler() {
		return modelServiceRefreshHandler;
	}
}


