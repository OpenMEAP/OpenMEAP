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

import static com.openmeap.util.ParameterMapUtils.firstValue;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.openmeap.Event;
import com.openmeap.EventHandler;
import com.openmeap.EventHandlingException;
import com.openmeap.constants.UrlParamConstants;
import com.openmeap.json.JSONObjectBuilder;
import com.openmeap.model.InvalidPropertiesException;
import com.openmeap.model.ModelManager;
import com.openmeap.model.ModelServiceImpl;
import com.openmeap.model.dto.ApplicationArchive;
import com.openmeap.model.dto.ClusterNode;
import com.openmeap.model.dto.GlobalSettings;
import com.openmeap.model.event.MapPayloadEvent;
import com.openmeap.model.event.ModelEntityEventAction;
import com.openmeap.model.event.handler.ArchiveFileDeleteHandler;
import com.openmeap.model.event.handler.ArchiveFileUploadHandler;
import com.openmeap.model.event.handler.ModelServiceRefreshHandler;
import com.openmeap.services.dto.Result;
import com.openmeap.util.AuthTokenProvider;
import com.openmeap.util.ParameterMapUtils;
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

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Logger logger = LoggerFactory.getLogger(ServiceManagementServlet.class);
	
	private ModelManager modelManager = null;
	private ModelServiceRefreshHandler modelServiceRefreshHandler = null;
	private ArchiveFileUploadHandler archiveUploadHandler = null;
	private ArchiveFileDeleteHandler archiveDeleteHandler = null;
	
	private WebApplicationContext context = null;
	
	public void init() {
		
		context = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		
		modelManager = (ModelManager)context.getBean("modelManager");
		
		modelServiceRefreshHandler = (ModelServiceRefreshHandler)context.getBean("modelServiceRefreshHandler");
		
		archiveUploadHandler = (ArchiveFileUploadHandler)context.getBean("archiveUploadHandler");
		archiveUploadHandler.setFileSystemStoragePathPrefix(modelManager.getClusterNode().getFileSystemStoragePathPrefix());
		
		archiveDeleteHandler = (ArchiveFileDeleteHandler)context.getBean("archiveDeleteHandler");
		archiveDeleteHandler.setFileSystemStoragePathPrefix(modelManager.getClusterNode().getFileSystemStoragePathPrefix());
	}
	
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		Result result = null;
		
		String action = request.getParameter(UrlParamConstants.ACTION);
		if( action==null ) {
			action="";
		}
		
		logger.debug("Request url: "+request.getRequestURL());
		logger.debug("Parameter map: "+ParameterMapUtils.toString(request.getParameterMap()));
		
		PrintWriter os = new PrintWriter(response.getOutputStream());
		GlobalSettings settings = modelManager.getGlobalSettings();
		ClusterNode node = modelManager.getClusterNode();
		if( node==null ) {
			throw new RuntimeException("openmeap-services-web needs to be configured as a cluster node in the settings of the admin interface.");
		}
		Map<Method,String> validationErrors = node.validate();
		if( validationErrors != null ) {
			throw new RuntimeException(new InvalidPropertiesException(node,validationErrors));
		}
		
		if( ! authenticates(request) ) {
			
			logger.error("Request failed to authenticate ",request);
			result = new Result(Result.Status.FAILURE,"Authentication failed");
			
		} else if( request.getParameter("clearPersistenceContext")!=null && context instanceof AbstractApplicationContext ) {
			
			clearPersistenceContext();
			
		} else if( action.equals(ModelEntityEventAction.ARCHIVE_UPLOAD.getActionName()) ) {

			Map<Object,Object> paramMap = ServletUtils.cloneParameterMap(settings.getMaxFileUploadSize(),node.getFileSystemStoragePathPrefix(),request);
			result = handleArchiveEvent(archiveUploadHandler, new MapPayloadEvent(paramMap), paramMap);
			
		} else if( action.equals(ModelEntityEventAction.ARCHIVE_DELETE.getActionName()) ) {
			
			Map<Object,Object> paramMap = ServletUtils.cloneParameterMap(settings.getMaxFileUploadSize(),node.getFileSystemStoragePathPrefix(), request);
			result = handleArchiveEvent(archiveDeleteHandler, new MapPayloadEvent(paramMap), paramMap);
			
		} else if( action.equals(ModelEntityEventAction.MODEL_REFRESH.getActionName()) ) {
			
			result = refresh(request,response);
			
		}
		
		try {
			if( result.getStatus()!=Result.Status.SUCCESS ) {
				response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			}
			os.print( new JSONObjectBuilder().toJSON(result).toString(3) );
		} catch( JSONException jse ) {
			throw new IOException(jse);
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
	private Result handleArchiveEvent(EventHandler eventHandler, Event event, Map<Object,Object> paramMap) throws IOException {
		
		String hash = firstValue(UrlParamConstants.APPARCH_HASH,paramMap);
		String hashType = firstValue(UrlParamConstants.APPARCH_HASH_ALG,paramMap);
		if( logger.isInfoEnabled() ) {
			logger.info("Received request archive upload notification "+hashType+":"+hash);
		}
		
		Result result = null;
		if( hash!=null && hashType!=null ) {
			ApplicationArchive arch = new ApplicationArchive();
			arch.setHash(hash);
			arch.setHashAlgorithm(hashType);
			try {
				paramMap.put("archive",arch);
				eventHandler.handle(event);
				result = new Result(Result.Status.SUCCESS);
			} catch(EventHandlingException che) {
				String msg = "Exception occurred handing the ArchiveUploadEvent";
				logger.error(msg,che);
				result = new Result(Result.Status.FAILURE,msg);
			}
		} else {
			String msg = "Either the hash("+hash+") or the hashType("+hashType+") was null.  Both are needed to process an archive event";
			logger.error(msg);
			result = new Result(Result.Status.FAILURE,msg);
		}
		return result;
	}
	
	/**
	 * Handles the notification that this node should refresh some object from the database.
	 * 
	 * @param os
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	private Result refresh(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		response.setContentType("text/javascript");
		
		String refreshType = (String)request.getParameter("type");
		String objectId = (String)request.getParameter("id");
		Result result = null;
		
		if( refreshType!=null && objectId!=null ) {
		
			if( logger.isInfoEnabled() ) {
				logger.info("Received request to refresh "+refreshType+" with id "+objectId,request);
			}
			
			try {
				modelServiceRefreshHandler.handleRefresh(refreshType,objectId);
				if( logger.isInfoEnabled() ) { 
					logger.info("Refresh for "+refreshType+" with id "+objectId+" was successful");
				}
				result = new Result(Result.Status.SUCCESS);
			} catch( Exception e ) {
				String msg = "Exception occurred refreshing "+refreshType+" with object id "+objectId;
				logger.error(msg,e);
				result = new Result(Result.Status.FAILURE,msg);
			}	
			
		} else {
			String msg = "Must specify refresh target class, object primary key, and authentication token.";
			logger.error(msg,request);
			result = new Result(Result.Status.FAILURE,msg);
		}
		return result;
	}
	
	/**
	 * Validates that the auth in a request passes validation
	 * @param arg0
	 * @return
	 */
	private Boolean authenticates(HttpServletRequest arg0) {
		String authSalt = getAuthSalt();
		String auth = (String)arg0.getParameter(UrlParamConstants.AUTH_TOKEN);
		Boolean isGood = AuthTokenProvider.validateAuthToken(authSalt, auth);
		return (auth!=null && isGood);
	}
	
	// ACCESSORS
	
	public String getAuthSalt() {
		return modelManager.getGlobalSettings().getServiceManagementAuthSalt();
	}
	
	public void setModelManager(ModelManager manager) {
		modelManager = manager;
	}
	public ModelManager getModelManager() {
		return modelManager;
	}
	
	public void setModelServiceRefreshHandler(ModelServiceRefreshHandler refreshHandler) {
		modelServiceRefreshHandler = refreshHandler;
	}
	public ModelServiceRefreshHandler getModelServiceRefreshHandler() {
		return modelServiceRefreshHandler;
	}

}


