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

package com.openmeap.admin.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;

import com.openmeap.constants.FormConstants;
import com.openmeap.model.dto.Application;
import com.openmeap.util.FileHandlingHttpRequestExecuterImpl;
import com.openmeap.util.HttpRequestExecuter;
import com.openmeap.web.form.ParameterMapBuilder;
import com.openmeap.web.form.ParameterMapBuilderException;

public class AdminTestHelper {
	
	private String adminUrl = "http://localhost:7000/openmeap-admin-web/interface/";
	private HttpRequestExecuter requestExecuter;
	private ParameterMapBuilder paramsBuilder;
	
	public AdminTestHelper() {
		requestExecuter = new FileHandlingHttpRequestExecuterImpl();
		paramsBuilder = new ParameterMapBuilder();
	}
	
	public HttpRequestExecuter getRequestExecuter() {
		return requestExecuter;
	}
	
	public String getAdminUrl() {
		return adminUrl;
	}
	public void setAdminUrl(String url) {
		adminUrl = url;
	}
	
	/*
	 * Login 
	 */
	
	public HttpResponse getLogin() throws ClientProtocolException, IOException {
		return requestExecuter.get(adminUrl);
	}
	
	public HttpResponse postLogin(String userName, String password) throws ClientProtocolException, IOException {
		Map<String,Object> postData = new HashMap<String,Object>();
		postData.put("j_username", userName);
		postData.put("j_password", password);
		return requestExecuter.postData(adminUrl+"j_security_check", postData);
	}
	
	/*
	 * Application Add/Modify
	 */
	
	public HttpResponse getAddModifyAppPage(Application application) throws ClientProtocolException, IOException {
		Map<String,Object> getData = new HashMap<String,Object>();
		getData.put(FormConstants.PAGE_BEAN, FormConstants.PAGE_BEAN_APP_ADDMODIFY);
		if(application!=null && application.getPk()!=null) {
			getData.put(FormConstants.APP_ID, application.getPk().toString());
		}
		return requestExecuter.get(adminUrl,getData);
	}
	
	public HttpResponse postAddModifyApp(Application application) throws ClientProtocolException, IOException, ParameterMapBuilderException {
		
		Map<String,Object> getData = new HashMap<String,Object>();
		getData.put(FormConstants.PAGE_BEAN, FormConstants.PAGE_BEAN_APP_ADDMODIFY);
		if( application.getPk()!=null ) {
			getData.put(FormConstants.APP_ID, application.getPk().toString());
		}
		
		Map<String,Object> postData = new HashMap<String,Object>();
		postData.put(FormConstants.PAGE_BEAN, FormConstants.PAGE_BEAN_APP_ADDMODIFY);
		postData.put(FormConstants.PROCESS_TARGET, ProcessingTargets.ADDMODIFY_APP);
		postData.put("submit","Submit!");
		
		paramsBuilder.toParameters(postData,application);
		
		return requestExecuter.postData(adminUrl,getData,postData);
	}
	
	public HttpResponse postAddModifyApp_delete(Application application) throws ParameterMapBuilderException, ClientProtocolException, IOException {
		
		Map<String,Object> getData = new HashMap<String,Object>();
		getData.put(FormConstants.PAGE_BEAN, FormConstants.PAGE_BEAN_APP_ADDMODIFY);
		if( application.getPk()!=null ) {
			getData.put(FormConstants.APP_ID, application.getPk().toString());
		}
		
		Map<String,Object> postData = new HashMap<String,Object>();
		postData.put(FormConstants.PAGE_BEAN, FormConstants.PAGE_BEAN_APP_ADDMODIFY);
		postData.put(FormConstants.PROCESS_TARGET, ProcessingTargets.ADDMODIFY_APP);
		postData.put("deleteConfirm",FormConstants.APP_DELETE_CONFIRM_TEXT);
		postData.put(FormConstants.DELETE,"Delete!");
		postData.put("Delete!","Delete!");
		
		paramsBuilder.toParameters(postData, application);
		
		return requestExecuter.postData(adminUrl,getData,postData);
	}
}
