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

import java.io.File;
import java.io.IOException;

import javax.servlet.http.*;

import java.security.Principal;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.openmeap.AuthorizerImpl;
import com.openmeap.constants.FormConstants;
import com.openmeap.model.ModelManager;
import com.openmeap.model.ModelServiceImpl;
import com.openmeap.model.dto.GlobalSettings;
import com.openmeap.util.ServletUtils;
import com.openmeap.web.*;

import freemarker.ext.beans.BeanModel;
import freemarker.template.DefaultObjectWrapper;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class AdminServlet extends HttpServlet {
	
	private static final long serialVersionUID = -7679539480528574013L;

	private Logger logger = LoggerFactory.getLogger(AdminServlet.class);
	
	WebApplicationContext context = null;
	ModelManager modelManager = null;
	
	public void init() {
		context = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) {
		try {
			DocumentProcessor td = null;
			
			if( request.getParameter("logout")!=null ) {
				request.getSession().invalidate();
				response.sendRedirect(request.getContextPath()+"/interface/");
			}
			
			if( request.getParameter("refreshContext")!=null && context instanceof AbstractApplicationContext ) {
				((AbstractApplicationContext)context).refresh();
			}
			
			// support for clearing the persistence context
			if( request.getParameter("clearPersistenceContext")!=null && context instanceof AbstractApplicationContext ) {
				ModelServiceImpl ms = (ModelServiceImpl)((AbstractApplicationContext)context).getBean("modelService");
				ms.clearPersistenceContext();
			}
			
			// default to the mainOptionPage, unless otherwise specified
			if( request.getParameter(FormConstants.PAGE_BEAN)!=null )
				td = (DocumentProcessor)context.getBean(request.getParameter(FormConstants.PAGE_BEAN));
			else td = (DocumentProcessor)context.getBean(FormConstants.PAGE_BEAN_MAIN);
			
			ModelManager mgr = getModelManager();
			Map<Object,Object> map = new HashMap<Object,Object>();
			
			// TODO: I'm not really happy with this hacky work-around for the login form not being in actual request scope
			if( td.getProcessesFormData() ) {
				GlobalSettings settings = mgr.getGlobalSettings();
				map = ServletUtils.cloneParameterMap(settings.getMaxFileUploadSize(),settings.getTemporaryStoragePath(),request);
				
				AuthorizerImpl auth = (AuthorizerImpl)context.getBean("authorizer");
				auth.setRequest(request);
			}

			response.setContentType("text/html");
			
			Map<Object,Object> defaultTemplateVars = new HashMap<Object,Object>();
			defaultTemplateVars.put("request", new BeanModel(request,new DefaultObjectWrapper()));
			td.setTemplateVariables(defaultTemplateVars);
			td.handleProcessAndRender(map,response.getWriter());
			
			response.getWriter().flush();
			response.getWriter().close();
		} catch(IOException te) {
			throw new RuntimeException(te);
		} 
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
