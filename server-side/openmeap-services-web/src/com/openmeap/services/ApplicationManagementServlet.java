/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011 OpenMEAP, Inc.                                        #
 #    Credits to Jonathan Schang & Robert Thacher                              #
 #                                                                             #
 #    Released under the GPLv3                                                 #
 #                                                                             #
 #    OpenMEAP is free software: you can redistribute it and/or modify         #
 #    it under the terms of the GNU General Public License as published by     #
 #    the Free Software Foundation, either version 3 of the License, or        #
 #    (at your option) any later version.                                      #
 #                                                                             #
 #    OpenMEAP is distributed in the hope that it will be useful,              #
 #    but WITHOUT ANY WARRANTY; without even the implied warranty of           #
 #    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the            #
 #    GNU General Public License for more details.                             #
 #                                                                             #
 #    You should have received a copy of the GNU General Public License        #
 #    along with OpenMEAP.  If not, see <http://www.gnu.org/licenses/>.        #
 #                                                                             #
 ###############################################################################
 */

package com.openmeap.services;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.codec.net.URLCodec;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.openmeap.constants.UrlParamConstants;
import com.openmeap.json.JSONObjectBuilder;
import com.openmeap.model.ModelManager;
import com.openmeap.model.dto.ApplicationArchive;
import com.openmeap.model.dto.ApplicationVersion;
import com.openmeap.model.dto.ClusterNode;
import com.openmeap.model.dto.GlobalSettings;
import com.openmeap.protocol.ApplicationManagementService;
import com.openmeap.protocol.WebServiceException;
import com.openmeap.protocol.dto.Application;
import com.openmeap.protocol.dto.ApplicationInstallation;
import com.openmeap.protocol.dto.ConnectionOpenRequest;
import com.openmeap.protocol.dto.ConnectionOpenResponse;
import com.openmeap.protocol.dto.Error;
import com.openmeap.protocol.dto.ErrorCode;
import com.openmeap.protocol.dto.Result;
import com.openmeap.protocol.dto.SLIC;
import com.openmeap.util.AuthTokenProvider;
import com.openmeap.util.Utils;

public class ApplicationManagementServlet extends HttpServlet {

	private Logger logger = LoggerFactory.getLogger(ApplicationManagementServlet.class);
	private ModelManager modelManager = null;
	private WebApplicationContext context = null;
	
	@Override
	public void init() {
		context = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		modelManager = (ModelManager)context.getBean("modelManager");
	}
	
	@Override
	public void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		Result result = new Result();
		Error err = new Error();
		
		String action = req.getParameter(UrlParamConstants.ACTION);
		if( action==null )
			action="";
		
		if( action.equals("connection-open-request") ) {
			result = connectionOpenRequest(req);
		} else if ( action.equals("archiveDownload") ) {
			result = handleArchiveDownload(req,resp);
			if( result==null ) {
				return;
			}
		} else {
			err.setCode(ErrorCode.MISSING_PARAMETER);
			err.setMessage("The \"action\" parameter is not recognized, missing, or empty.");
			result.setError(err);
		}  
		
		try {
			JSONObjectBuilder builder = new JSONObjectBuilder();
			resp.setContentType("text/javascript");
			JSONObject jsonResult = builder.toJSON(result);
			resp.getOutputStream().write(jsonResult.toString().getBytes());
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
	
	private Result handleArchiveDownload(HttpServletRequest request, HttpServletResponse response) {
		
		Result res = new Result();
		Error err = new Error();
		res.setError(err);
		
		GlobalSettings settings = modelManager.getGlobalSettings();
		Map properties = this.getServicesWebProperties();
		String nodeKey = (String)properties.get("clusterNodeUrlPrefix");
		ClusterNode clusterNode = settings.getClusterNodes().get(nodeKey);
		if( nodeKey==null || clusterNode==null ) {
			// TODO: create a configuration error code
			err.setCode(ErrorCode.UNDEFINED);
			err.setMessage("A configuration is missing.  Please consult the error logs.");
			logger.error("For each node in the cluster, the property or environment variable OPENMEAP_CLUSTER_NODE_URL_PREFIX must match the \"Service Url Prefix\" value configured in the administrative interface.  This value is currently "+nodeKey+".");
			return res;
		}
		
		String pathValidation = clusterNode.validateFileSystemStoragePathPrefix();
		if( pathValidation!=null ) {
			err.setCode(ErrorCode.UNDEFINED);
			err.setMessage("A configuration is missing.  Please consult the error logs.");
			logger.error("There is an issue with the location at \"File-system Storage Prefix\".  "+pathValidation);
			return res;
		}
		
		String hash = request.getParameter(UrlParamConstants.APPARCH_HASH);
		String hashAlg = request.getParameter(UrlParamConstants.APPARCH_HASH_ALG);
		String fileName = null;
		
		if( hash==null || hashAlg==null ) {	
			// look in the apps directory for the archive specified
			String appName = request.getParameter(UrlParamConstants.APP_NAME);
			String versionId = request.getParameter(UrlParamConstants.APP_VERSION);
			
			ApplicationVersion appVersion = modelManager.findAppVersionByNameAndId(appName, versionId);
			if( appVersion==null ) {
				String mesg = "The application version "+versionId+" was not found for application "+appName;
				err.setCode(ErrorCode.APPLICATION_VERSION_NOTFOUND);
				err.setMessage(mesg);
				logger.warn(mesg);
				return res;
			}
			
			String auth = request.getParameter(UrlParamConstants.AUTH_TOKEN);
			com.openmeap.model.dto.Application app = appVersion.getApplication();
			if( auth==null || ! AuthTokenProvider.validateAuthToken(app.getProxyAuthSalt(), auth) ) {
				err.setCode(ErrorCode.AUTHENTICATION_FAILURE);
				err.setMessage("The \"auth\" token presented is not recognized, missing, or empty.");
				return res;
			}
			
			hash = appVersion.getArchive().getHash();
			hashAlg = appVersion.getArchive().getHashAlgorithm();
			fileName = app.getName()+" - "+appVersion.getIdentifier();
		} else {
			fileName = hashAlg+"-"+hash;
		}
		
		File file = ApplicationArchive.getFile(clusterNode.getFileSystemStoragePathPrefix(),hashAlg,hash);
		if( ! file.exists() ) {
			String mesg = "The application archive with "+hashAlg+" hash "+hash+" was not found.";
			// TODO: create an enumeration for this error
			err.setCode(ErrorCode.UNDEFINED);
			err.setMessage(mesg);
			logger.warn(mesg);
			return res;
		}
		
		try {
			FileNameMap fileNameMap = URLConnection.getFileNameMap();
			String mimeType = fileNameMap.getContentTypeFor(file.toURL().toString());
			response.setContentType(mimeType);
			response.setContentLength(Long.valueOf(file.length()).intValue());
			URLCodec codec = new URLCodec();
			response.setHeader("Content-Disposition", "attachment; filename=\""+fileName+".zip\";");
			
			InputStream inputStream = null;
			OutputStream outputStream = null;
			try {
				inputStream = new BufferedInputStream(new FileInputStream(file));
				outputStream = response.getOutputStream();
				Utils.pipeInputStreamIntoOutputStream(inputStream, outputStream);
			} finally {
				if(inputStream!=null)  {inputStream.close();}
				//if(outputStream!=null) {outputStream.close();}
			}
			response.flushBuffer();
		} catch (FileNotFoundException e) {
			logger.error("Exception {}",e);
		} catch (IOException ioe) {
			logger.error("Exception {}",ioe);
		}
		
		return null;
	}

	/**
	 * Pulls parameters out of the request and passes them to the ApplicationManagementPortType bean pulled from the WebApplicationContext
	 * 
	 * @param req
	 * @return
	 */
	public Result connectionOpenRequest(HttpServletRequest req) {
		
		WebApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
		
		ConnectionOpenRequest request = createConnectionOpenRequest(req);
		
		Result result = new Result();
		
		try {
			ConnectionOpenResponse response = ((ApplicationManagementService)context.getBean("applicationManagementService")).connectionOpen(request);
			result.setConnectionOpenResponse(response);
		} catch( WebServiceException wse ) {
			Error err = new Error();
			err.setCode(wse.getType().asErrorCode());
			err.setMessage(wse.getMessage());
			result.setError(err);
		}
		
		return result;
	}
	
	private ConnectionOpenRequest createConnectionOpenRequest(HttpServletRequest req) {
		ConnectionOpenRequest request = new ConnectionOpenRequest();
		request.setApplication(new Application());
		
		Application app = request.getApplication();
		app.setVersionId(req.getParameter(UrlParamConstants.APP_VERSION));
		app.setName(req.getParameter(UrlParamConstants.APP_NAME));
		app.setHashValue(req.getParameter(UrlParamConstants.APPARCH_HASH));
		
		app.setInstallation(new ApplicationInstallation());
		app.getInstallation().setUuid(req.getParameter(UrlParamConstants.DEVICE_UUID));
		
		request.setSlic(new SLIC());
		SLIC slic = request.getSlic();
		slic.setVersionId(req.getParameter(UrlParamConstants.SLIC_VERSION));
		return request;
	}
	
	public Map getServicesWebProperties() {
		return (Map)context.getBean("openmeapServicesWebPropertiesMap");
	}
	
	// ACCESSORS
	
	public void setModelManager(ModelManager manager) {
		modelManager = manager;
	}
	public ModelManager getModelManager() {
		return modelManager;
	}
}
