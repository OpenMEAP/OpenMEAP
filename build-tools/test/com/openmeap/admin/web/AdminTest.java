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

import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openmeap.constants.FormConstants;
import com.openmeap.json.JSONObjectBuilder;
import com.openmeap.model.ModelManager;
import com.openmeap.model.dto.Application;
import com.openmeap.util.Utils;
import com.openmeap.web.form.ParameterMapBuilder;

/**
 * Performs an end-to-end test of the administrative console functionality.
 * As the admin panel grows, we'll need to split this up into separate tests.
 *  
 * Note that the order of tests in this class is significant.
 *  
 * @author Schang
 */
public class AdminTest {

	final static private String APP_NAME = "Happy Appy";
	
	static private AdminTestHelper helper;
	static private Logger logger = LoggerFactory.getLogger(AdminTest.class);
	
	@BeforeClass static public void beforeClass() {
		org.apache.log4j.BasicConfigurator.configure();
		helper = new AdminTestHelper();
	}
	
	@AfterClass static public void afterClass() {
	}
	
	@Test public void testLogin() throws Exception {
		
		HttpResponse response = helper.getLogin();
		EntityUtils.consume(response.getEntity());
		
		response = helper.postLogin("tomcat", "tomcat");
		
		Assert.assertTrue(response.getStatusLine().getStatusCode()==302);
		Header[] headers = response.getHeaders("Location");
		Assert.assertTrue(headers.length==1);
		Assert.assertTrue(headers[0].getValue().equals(helper.getAdminUrl()));
		
		EntityUtils.consume(response.getEntity());
		
		// Tomcat will shoot you in the face if you don't follow it's redirects
		response = helper.getRequestExecuter().get(headers[0].getValue());
		EntityUtils.consume(response.getEntity());
	}
	
	@Test public void testUpdateGlobalSettings() throws Exception {
		// correct location of storage path prefix
		// correct cluster node location and path prefix
		// validate settings stored in database
	}
	
	@Test public void testCreateApplication() throws Exception {
		
		Application app = new Application();
		app.setName(APP_NAME);
		app.setDescription("This is my happy appy");
		app.setDeploymentHistoryLength(10);
		app.setVersionAdmins("juno");
		app.setAdmins("jacob");
		app.setInitialVersionIdentifier("ver-1.1.x");
		
		HttpResponse response = helper.postAddModifyApp(app);
		
		Assert.assertTrue(response.getStatusLine().getStatusCode()==200);
		String output = Utils.readInputStream(response.getEntity().getContent(),FormConstants.CHAR_ENC_DEFAULT);
		Assert.assertTrue(output.contains("Application successfully created/modified!"));
		
		ModelManager modelManager = helper.getModelManager();
		
		// Now check the database, to make sure everything got in there
		
		Application dbApp = modelManager.getModelService().findApplicationByName(APP_NAME);
		Assert.assertTrue(dbApp!=null);
		Assert.assertEquals(app.getName(),dbApp.getName());
		Assert.assertEquals(app.getDescription(),dbApp.getDescription());
		Assert.assertEquals(app.getDeploymentHistoryLength(),dbApp.getDeploymentHistoryLength());
		Assert.assertEquals(app.getAdmins(),dbApp.getAdmins());
		Assert.assertEquals(app.getVersionAdmins(),dbApp.getVersionAdmins());
		Assert.assertEquals(app.getInitialVersionIdentifier(),dbApp.getInitialVersionIdentifier());
		Assert.assertTrue(dbApp.getProxyAuthSalt()!=null && dbApp.getProxyAuthSalt().length()==36);
	}
	
	@Test public void testModifyApplication() throws Exception {
		// validate changes
		// validate changes are reflected by service-web
	}
	
	@Test public void testCreateApplicationVersion() throws Exception {
		// archive is uploaded
		// version created
	}
	
	@Test public void testDeleteApplication() throws Exception {
		ModelManager modelManager = helper.getModelManager();
		Application dbApp = modelManager.getModelService().findApplicationByName(APP_NAME);
		HttpResponse response = helper.postAddModifyApp_delete(dbApp);
		dbApp = modelManager.getModelService().findApplicationByName(APP_NAME);
		Assert.assertTrue(dbApp==null);
	}
}
