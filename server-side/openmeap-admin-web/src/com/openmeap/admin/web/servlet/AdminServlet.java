/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2015 OpenMEAP, Inc.                                   #
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

package com.openmeap.admin.web.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.openmeap.AuthorizerImpl;
import com.openmeap.cluster.ClusterNodeHealthCheckThread;
import com.openmeap.constants.FormConstants;
import com.openmeap.model.ModelManager;
import com.openmeap.model.ModelServiceImpl;
import com.openmeap.model.dto.GlobalSettings;
import com.openmeap.util.ParameterMapUtils;
import com.openmeap.util.ServletUtils;
import com.openmeap.web.DocumentProcessor;

import freemarker.ext.beans.BeanModel;
import freemarker.template.DefaultObjectWrapper;

public class AdminServlet extends HttpServlet {
	
	private static final long serialVersionUID = -7679539480528574013L;

	private Logger logger = LoggerFactory.getLogger(AdminServlet.class);
	
	WebApplicationContext context = null;
	ModelManager modelManager = null;
	
	public void init() {
		context = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		ClusterNodeHealthCheckThread healthChecker = (ClusterNodeHealthCheckThread)context.getBean("clusterNodeHealthCheck");
		new Thread(healthChecker).start();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) {
		
		logger.trace("Entering service()");
		
		try {
			DocumentProcessor documentProcessor = null;
			
			logger.debug("Request uri: {}",request.getRequestURI());
			logger.debug("Request url: {}",request.getRequestURL());
			logger.debug("Query string: {}",request.getQueryString());
			if(logger.isDebugEnabled()) {
				logger.debug("Parameter map: {}",ParameterMapUtils.toString(request.getParameterMap()));
			}
			
			if( request.getParameter("logout")!=null ) {
				logger.trace("Executing logout");
				request.getSession().invalidate();
				response.sendRedirect(request.getContextPath()+"/interface/");
			}
			
			if( request.getParameter("refreshContext")!=null && context instanceof AbstractApplicationContext ) {
				logger.trace("Refreshing context");
				((AbstractApplicationContext)context).refresh();
			}
			
			// support for clearing the persistence context
			if( request.getParameter("clearPersistenceContext")!=null && context instanceof AbstractApplicationContext ) {
				logger.trace("Clearing the persistence context");
				ModelServiceImpl ms = (ModelServiceImpl)((AbstractApplicationContext)context).getBean("modelService");
				ms.clearPersistenceContext();
			}
			
			// default to the mainOptionPage, unless otherwise specified
			String pageBean = null;
			if( request.getParameter(FormConstants.PAGE_BEAN)!=null )
				pageBean = request.getParameter(FormConstants.PAGE_BEAN);
			else pageBean = FormConstants.PAGE_BEAN_MAIN;
			logger.debug("Using page bean: {}", pageBean);
			documentProcessor = (DocumentProcessor)context.getBean(pageBean);
			
			ModelManager mgr = getModelManager();
			Map<Object,Object> map = new HashMap<Object,Object>();
			
			// TODO: I'm not really happy with this hacky work-around for the login form not being in actual request scope
			if( documentProcessor.getProcessesFormData() ) {
				GlobalSettings settings = mgr.getGlobalSettings();
				map = ServletUtils.cloneParameterMap(settings.getMaxFileUploadSize(),settings.getTemporaryStoragePath(),request);
				map.put("userPrincipalName",new String[]{request.getUserPrincipal().getName()});
				AuthorizerImpl authorizer = (AuthorizerImpl)context.getBean("authorizer");
				authorizer.setRequest(request);
			}

			response.setContentType(FormConstants.CONT_TYPE_HTML);
			
			Map<Object,Object> defaultTemplateVars = new HashMap<Object,Object>();
			defaultTemplateVars.put("request", new BeanModel(request,new DefaultObjectWrapper()));
			documentProcessor.setTemplateVariables(defaultTemplateVars);
			documentProcessor.handleProcessAndRender(map,response.getWriter());
			
			response.getWriter().flush();
			response.getWriter().close();
		} catch(IOException te) {
			throw new RuntimeException(te);
		} 
		
		logger.trace("Leaving service()");
	}
	
	public void setModelManager(ModelManager manager) {
		modelManager = manager;
	}
	public ModelManager getModelManager() {
		if( modelManager==null )
			modelManager = (ModelManager)context.getBean("modelManager");
		return modelManager;
	}
}
