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
import com.openmeap.model.dto.ApplicationVersion;
import com.openmeap.model.dto.ClusterNode;
import com.openmeap.model.dto.GlobalSettings;
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
	static private ModelManager modelManager;
	static private Logger logger = LoggerFactory.getLogger(AdminTest.class);
	
	@BeforeClass static public void beforeClass() {
		org.apache.log4j.BasicConfigurator.configure();
		helper = new AdminTestHelper();
		modelManager = helper.getModelManager();
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
		GlobalSettings settings = new GlobalSettings();
		settings.setExternalServiceUrlPrefix("http://localhost:7000/openmeap-services-web");
		settings.setMaxFileUploadSize(1234550);
		settings.setServiceManagementAuthSalt("auth-salt");
		settings.setTemporaryStoragePath("/tmp");
		
		// correct cluster node location and path prefix
		Map<String,ClusterNode> clusterNodeMap = new HashMap<String,ClusterNode>();
		ClusterNode node = new ClusterNode();
		node.setServiceWebUrlPrefix("http://localhost:7000/openmeap-services-web");
		node.setFileSystemStoragePathPrefix("/tmp/archs");
		clusterNodeMap.put(node.getServiceWebUrlPrefix(), node);
		settings.setClusterNodes(clusterNodeMap);
		
		// validate settings stored in database
		EntityUtils.consume(helper.postGlobalSettings(settings).getEntity());
		
		GlobalSettings insertedSettings = modelManager.getGlobalSettings();
		
		JSONObjectBuilder job = new JSONObjectBuilder();
		String insertedSettingsJSON = job.toJSON(insertedSettings).toString(3);
		String originalSettingsJSON = job.toJSON(settings).toString(3);
		logger.info("original: {}",originalSettingsJSON);
		logger.info("inserted: {}",insertedSettingsJSON);
		Assert.assertEquals(insertedSettingsJSON,originalSettingsJSON);
	}
	
	@Test public void testAddApplication() throws Exception {
		
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
		
		// Now check the database, to make sure everything got in there
		
		Application dbApp = modelManager.getModelService().findApplicationByName(APP_NAME);
		Assert.assertTrue(dbApp!=null);
		helper.assertSame(app,dbApp);
		Assert.assertTrue(dbApp.getProxyAuthSalt()!=null && dbApp.getProxyAuthSalt().length()==36);
	}
	
	@Test public void testModifyApplication() throws Exception {
		
		Application dbApp = modelManager.getModelService().findApplicationByName(APP_NAME);
		Assert.assertTrue(dbApp!=null);
		
		// make some changes
		String newDesc = "Creating a new description";
		Integer newLen = 2;
		dbApp.setDescription(newDesc);
		dbApp.setDeploymentHistoryLength(newLen);
		EntityUtils.consume(helper.postAddModifyApp(dbApp).getEntity());
		
		// validate changes
		modelManager.getModelService().refresh(dbApp);
		Assert.assertTrue(dbApp.getDescription().equals(newDesc));
		Assert.assertTrue(dbApp.getDeploymentHistoryLength().equals(newLen));
		
		// TODO: validate changes are reflected by service-web
	}
	
	@Test public void testCreateApplicationVersion() throws Exception {
		
		Application app = modelManager.getModelService().findApplicationByName(APP_NAME);
		ApplicationVersion version = new ApplicationVersion();
		app.addVersion(version);
		version.setIdentifier("ver-1.1.x");
		version.setNotes("Test notes");
		File uploadArchive = new File(this.getClass().getResource("version01.zip").getFile());

		EntityUtils.consume(helper.postAddModifyAppVer(version, uploadArchive).getEntity());
		
		// archive is uploaded
		modelManager.getModelService().refresh(app);
		version = app.getVersions().get("ver-1.1.x");
		Assert.assertTrue(version!=null);
		
		File uploadedArchive = version.getArchive().getFile(modelManager.getGlobalSettings().getTemporaryStoragePath());
		File webViewDir = version.getArchive().getExplodedPath(modelManager.getGlobalSettings().getTemporaryStoragePath());
		Assert.assertTrue(uploadedArchive.exists());
		Assert.assertTrue(webViewDir.exists());
		
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
