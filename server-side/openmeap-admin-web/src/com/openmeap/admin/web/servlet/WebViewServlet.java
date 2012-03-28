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

package com.openmeap.admin.web.servlet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.net.URLDecoder;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.openmeap.constants.FormConstants;
import com.openmeap.model.ModelManager;
import com.openmeap.model.dto.ApplicationVersion;
import com.openmeap.model.dto.GlobalSettings;
import com.openmeap.util.AuthTokenProvider;
import com.openmeap.util.Utils;

public class WebViewServlet extends HttpServlet {

	private Logger logger = LoggerFactory.getLogger(WebViewServlet.class);
	
	WebApplicationContext context = null;
	ModelManager modelManager = null;
	
	private final static Integer APP_NAME_INDEX = 1;
	private final static Integer APP_VER_INDEX = 2;
	private final static Integer AUTH_TOKEN_INDEX = 3;
	
	public void init() {
		context = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		logger.trace("in service");
		
		ModelManager mgr = getModelManager();
		GlobalSettings settings = mgr.getGlobalSettings();
		String validTempPath = settings.validateTemporaryStoragePath();
		if( validTempPath!=null ) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,validTempPath);
		}
		
		String pathInfo = request.getPathInfo();
		String[] pathParts = pathInfo.split("[/]");
		if( pathParts.length<4 ) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
		String remove = pathParts[1]+"/"+pathParts[2]+"/"+pathParts[3];
		String fileRelative = pathInfo.replace(remove,""); 

		String applicationNameString = URLDecoder.decode(pathParts[APP_NAME_INDEX],FormConstants.CHAR_ENC_DEFAULT);
		String applicationVersionString = URLDecoder.decode(pathParts[APP_VER_INDEX],FormConstants.CHAR_ENC_DEFAULT);
		
		ApplicationVersion ver = mgr.getModelService().findAppVersionByNameAndId(applicationNameString, applicationVersionString);
	
		String authSalt = ver.getApplication().getProxyAuthSalt();
		String authToken = URLDecoder.decode(pathParts[AUTH_TOKEN_INDEX],FormConstants.CHAR_ENC_DEFAULT);
		
		if( ! AuthTokenProvider.validateAuthToken(authSalt, authToken) ) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
		}
		
		File fileFull = new File( ver.getArchive().getExplodedPath(settings.getTemporaryStoragePath()).getAbsolutePath() + "/" + fileRelative );
		try {
			FileNameMap fileNameMap = URLConnection.getFileNameMap();
			String mimeType = fileNameMap.getContentTypeFor(fileFull.toURL().toString());
			response.setContentType(mimeType);
			response.setContentLength(Long.valueOf(fileFull.length()).intValue());
			
			InputStream inputStream = null;
			OutputStream outputStream = null;
			try {
				//response.setStatus(HttpServletResponse.SC_FOUND);
				inputStream = new FileInputStream(fileFull);
				outputStream = response.getOutputStream();
				Utils.pipeInputStreamIntoOutputStream(inputStream, outputStream);
			} finally {
				if(inputStream!=null)  {inputStream.close();}
				response.getOutputStream().flush();
				response.getOutputStream().close();
			}
		} catch (FileNotFoundException e) {
			logger.error("Exception {}",e);
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		} catch (IOException ioe) {
			logger.error("Exception {}",ioe);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	
	// ACCESSORS
	
	public void setModelManager(ModelManager manager) {
		modelManager = manager;
	}
	public ModelManager getModelManager() {
		if( modelManager==null )
			modelManager = (ModelManager)context.getBean("modelManager");
		return modelManager;
	}
}
