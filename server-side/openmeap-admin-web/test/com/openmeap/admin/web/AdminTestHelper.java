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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.openmeap.constants.FormConstants;
import com.openmeap.event.ProcessingTargets;
import com.openmeap.http.FileHandlingHttpRequestExecuterImpl;
import com.openmeap.model.ModelManager;
import com.openmeap.model.dto.Application;
import com.openmeap.model.dto.ApplicationVersion;
import com.openmeap.model.dto.ClusterNode;
import com.openmeap.model.dto.Deployment;
import com.openmeap.model.dto.GlobalSettings;
import com.openmeap.http.HttpRequestException;
import com.openmeap.http.HttpRequestExecuter;
import com.openmeap.http.HttpResponse;
import com.openmeap.web.form.ParameterMapBuilder;
import com.openmeap.web.form.ParameterMapBuilderException;

public class AdminTestHelper {
	
	final static public String ADMIN_HOST = "localhost:8080";
	final static public String NODE_HOST = "localhost:8080";
	
	final static public String ADMIN_USER = "tomcat";
	final static public String ADMIN_PASS = "tomcat";
	final static public String ADMIN_WEB_STORAGE = "/tmp";
	
	final static public String SERVICES_WEB_URL = "http://"+NODE_HOST+"/openmeap-services-web";
	final static public String SERVICES_WEB_AUTH_SALT = "auth-salt";
	
	final static public String NODE_01_SERVICES_URL = SERVICES_WEB_URL;
	final static public String NODE_01_STORAGE = "/tmp/archs";
	
	private String adminUrl = "http://"+ADMIN_HOST+"/openmeap-admin-web/interface/";
	
	private HttpRequestExecuter requestExecuter;
	private ParameterMapBuilder paramsBuilder;
	private ClassPathXmlApplicationContext persistenceBeans;
	
	public AdminTestHelper() {
		requestExecuter = (HttpRequestExecuter) new FileHandlingHttpRequestExecuterImpl();
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
	
	public ModelManager getModelManager() {
		if( persistenceBeans==null ) {
			persistenceBeans=new ClassPathXmlApplicationContext(
					new String[]{"/META-INF/persistenceContext.xml",
					"/META-INF/test/persistenceContext.xml"}
				);
		}
		return (ModelManager)persistenceBeans.getBean("modelManager");
	}
	
	/*
	 * Login 
	 */
	
	public HttpResponse getLogin() throws HttpRequestException {
		return requestExecuter.get(adminUrl);
	}
	
	public HttpResponse postLogin(String userName, String password) throws HttpRequestException {
		Hashtable<String,Object> postData = new Hashtable<String,Object>();
		postData.put("j_username", userName);
		postData.put("j_password", password);
		return requestExecuter.postData(adminUrl+"j_security_check", postData);
	}
	
	/*
	 * Application Add/Modify
	 */
	
	public HttpResponse getAddModifyAppPage(Application application) throws HttpRequestException {
		Hashtable<String,Object> getData = new Hashtable<String,Object>();
		getData.put(FormConstants.PAGE_BEAN, FormConstants.PAGE_BEAN_APP_ADDMODIFY);
		if(application!=null && application.getPk()!=null) {
			getData.put(FormConstants.APP_ID, application.getPk().toString());
		}
		return requestExecuter.get(adminUrl,getData);
	}
	
	public HttpResponse postAddModifyApp(Application application) throws HttpRequestException, ParameterMapBuilderException {
		
		Hashtable<String,Object> getData = new Hashtable<String,Object>();
		getData.put(FormConstants.PAGE_BEAN, FormConstants.PAGE_BEAN_APP_ADDMODIFY);
		if( application.getPk()!=null ) {
			getData.put(FormConstants.APP_ID, application.getPk().toString());
		}
		
		Hashtable<String,Object> postData = new Hashtable<String,Object>();
		postData.put(FormConstants.PROCESS_TARGET, ProcessingTargets.ADDMODIFY_APP);
		postData.put("submit","Submit!");
		
		paramsBuilder.toParameters(postData,application);
		
		return requestExecuter.postData(adminUrl,getData,postData);
	}
	
	public HttpResponse postAddModifyApp_delete(Application application) throws HttpRequestException, ParameterMapBuilderException {
		
		Hashtable<String,Object> getData = new Hashtable<String,Object>();
		getData.put(FormConstants.PAGE_BEAN, FormConstants.PAGE_BEAN_APP_ADDMODIFY);
		if( application.getPk()!=null ) {
			getData.put(FormConstants.APP_ID, application.getPk().toString());
		}
		
		Hashtable<String,Object> postData = new Hashtable<String,Object>();
		postData.put(FormConstants.PROCESS_TARGET, ProcessingTargets.ADDMODIFY_APP);
		postData.put("deleteConfirm",FormConstants.APP_DELETE_CONFIRM_TEXT);
		postData.put(FormConstants.DELETE,"Delete!");
		postData.put("Delete!","Delete!");
		
		paramsBuilder.toParameters(postData, application);
		
		return requestExecuter.postData(adminUrl,getData,postData);
	}
	
	/*
	 * APPLICATiON VERSIONs
	 */
	
	public HttpResponse getAddModifyAppVer(Application application) throws HttpRequestException {
		
		Hashtable<String,Object> getData = new Hashtable<String,Object>();
		getData.put(FormConstants.PAGE_BEAN, FormConstants.PAGE_BEAN_APPVER_ADDMODIFY);
		getData.put(FormConstants.APP_ID, application.getPk().toString());
		return requestExecuter.get(adminUrl,getData);
	}
	
	public HttpResponse postAddModifyAppVer(ApplicationVersion appVer, File uploadArchive) throws HttpRequestException, ParameterMapBuilderException {
		
		Hashtable<String,Object> getData = new Hashtable<String,Object>();
		getData.put(FormConstants.PAGE_BEAN, FormConstants.PAGE_BEAN_APPVER_ADDMODIFY);
		getData.put(FormConstants.APP_ID, appVer.getApplication().getPk().toString());
		if( appVer.getPk()!=null ) {
			getData.put(FormConstants.APPVER_ID, appVer.getPk().toString());
		}
		
		Hashtable<String,Object> postData = new Hashtable<String,Object>();
		postData.put(FormConstants.PROCESS_TARGET, ProcessingTargets.ADDMODIFY_APPVER);
		postData.put(FormConstants.APP_ID, appVer.getApplication().getPk().toString());
		postData.put(FormConstants.UPLOAD_ARCHIVE, uploadArchive);
		paramsBuilder.toParameters(postData, appVer);
		
		return requestExecuter.postData(adminUrl,getData,postData);
	}
	
	public HttpResponse postAddModifyAppVer_delete(ApplicationVersion appVer) throws HttpRequestException, ParameterMapBuilderException {
		
		Hashtable<String,Object> getData = new Hashtable<String,Object>();
		getData.put(FormConstants.PAGE_BEAN, FormConstants.PAGE_BEAN_APPVER_ADDMODIFY);
		getData.put(FormConstants.APP_ID, appVer.getApplication().getPk().toString());
		if( appVer.getPk()!=null ) {
			getData.put(FormConstants.APPVER_ID, appVer.getPk().toString());
		}
		
		Hashtable<String,Object> postData = new Hashtable<String,Object>();
		postData.put(FormConstants.PROCESS_TARGET, ProcessingTargets.ADDMODIFY_APPVER);
		postData.put(FormConstants.APP_ID, appVer.getApplication().getPk().toString());
		if( appVer.getPk()!=null ) {
			postData.put(FormConstants.APPVER_ID, appVer.getPk().toString());
		}
		paramsBuilder.toParameters(postData, appVer);
		postData.put("deleteConfirm",FormConstants.APPVER_DELETE_CONFIRM_TEXT);
		postData.put(FormConstants.DELETE,"Delete!");
		postData.put("Delete!","Delete!");
		
		return requestExecuter.postData(adminUrl,getData,postData);
	}
	
	/*
	 * DEPLOYMENTS
	 */
	
	public HttpResponse postCreateDeployment(ApplicationVersion appVer, Deployment.Type deplType) throws HttpRequestException {
		
		Hashtable<String,Object> getData = new Hashtable<String,Object>();
		getData.put(FormConstants.PAGE_BEAN, FormConstants.PAGE_BEAN_DEPLOYMENTS);
		getData.put(FormConstants.APP_ID, appVer.getApplication().getPk().toString());
		
		Hashtable<String,Object> postData = new Hashtable<String,Object>();
		postData.put(FormConstants.PROCESS_TARGET, ProcessingTargets.DEPLOYMENTS);
		postData.put(FormConstants.APPVER_ID, appVer.getPk().toString());
		postData.put(FormConstants.DEPLOYMENT_TYPE, deplType.toString());
		
		return requestExecuter.postData(adminUrl,getData,postData);
	}
	
	/*
	 * GLOBAL SETTINGS
	 */
	
	public HttpResponse getGlobalSettings() throws HttpRequestException {
		Hashtable<String,Object> getData = new Hashtable<String,Object>();
		getData.put(FormConstants.PAGE_BEAN, FormConstants.PAGE_BEAN_GLOBAL_SETTINGS);
		return requestExecuter.get(adminUrl,getData);
	}
	
	public HttpResponse postGlobalSettings(GlobalSettings settings) throws HttpRequestException, ParameterMapBuilderException {
		
		Hashtable<String,Object> getData = new Hashtable<String,Object>();
		getData.put(FormConstants.PAGE_BEAN, FormConstants.PAGE_BEAN_GLOBAL_SETTINGS);
		
		Hashtable<String,Object> postData = new Hashtable<String,Object>();
		postData.put(FormConstants.PROCESS_TARGET, ProcessingTargets.GLOBAL_SETTINGS);
		paramsBuilder.toParameters(postData, settings);
		if(settings.getClusterNodes()!=null && !settings.getClusterNodes().isEmpty()) {
			List<String> nodeUrls = new ArrayList<String>();
			List<String> nodePaths = new ArrayList<String>();
			for( ClusterNode clusterNode : settings.getClusterNodes() ) {
				nodeUrls.add(clusterNode.getServiceWebUrlPrefix());
				nodePaths.add(clusterNode.getFileSystemStoragePathPrefix());
			}
			postData.put("clusterNodeUrl", nodeUrls);
			postData.put("clusterNodePath", nodePaths);
		}
		
		return requestExecuter.postData(adminUrl,getData,postData);
	}
	
	/*
	 * ASSERTION 
	 */
	
	public void assertSame(Application app, Application dbApp) {
		Assert.assertEquals(app.getName(),dbApp.getName());
		Assert.assertEquals(app.getDescription(),dbApp.getDescription());
		Assert.assertEquals(app.getDeploymentHistoryLength(),dbApp.getDeploymentHistoryLength());
		Assert.assertEquals(app.getAdmins(),dbApp.getAdmins());
		Assert.assertEquals(app.getVersionAdmins(),dbApp.getVersionAdmins());
		Assert.assertEquals(app.getInitialVersionIdentifier(),dbApp.getInitialVersionIdentifier());
	}
}


