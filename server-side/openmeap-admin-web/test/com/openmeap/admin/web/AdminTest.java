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
import com.openmeap.model.dto.Deployment;
import com.openmeap.model.dto.GlobalSettings;
import com.openmeap.util.HttpHeader;
import com.openmeap.util.HttpResponse;
import com.openmeap.util.Utils;

/**
 * Performs an end-to-end test of the administrative console functionality.
 * As the admin panel grows, we'll need to split this up into separate tests.
 *  
 * Note that the order of tests in this class is significant.
 *  
 * @author Schang
 */
public class AdminTest {
	
	final static private String APP_NAME = "Integration Test Application";
	final static private String APP_DESC = "This application has been created programmatically in order to test the functions of the administrative console";
	final static private Integer APP_DEPL_LEN = 10;
	final static private String APP_ADMINS = "default-admin";
	final static private String APP_VERSION_ADMINS = "default-versioner";
	
	final static private String APP_ADDMODIFY_SUCCESS = "Application successfully created/modified!";
	
	final static private String VERSION_ORIG = "initialVersionId";
	
	final static private String VERSION_01 = "version01Id";
	final static private String VERSION_01_ZIP = "version01.zip";
	final static private String VERSION_01_HASH = "08c7f5cb8486466b872aac2059cd47f4";
	final static private String VERSION_01_NOTES = "Test notes - version01";
	
	final static private String VERSION_02 = "version02Id";
	final static private String VERSION_02_ZIP = "version02.zip";
	final static private String VERSION_02_HASH = "d2ed29ae33e7ddf9ff99fa9b6ad0724d";
	final static private String VERSION_02_NOTES = "Test notes - version02";
	
	static private AdminTestHelper helper;
	static private ModelManager modelManager;
	static private Logger logger = LoggerFactory.getLogger(AdminTest.class);
	
	@BeforeClass static public void beforeClass() {
		org.apache.log4j.BasicConfigurator.configure();
		helper = new AdminTestHelper();
		modelManager = helper.getModelManager();
	}
	
	@Test public void testLogin() throws Exception {
		
		HttpResponse response = helper.getLogin();
		Utils.consumeInputStream(response.getResponseBody());
		
		response = helper.postLogin(AdminTestHelper.ADMIN_USER, AdminTestHelper.ADMIN_PASS);
		
		Assert.assertTrue(response.getStatusCode()==302);
		HttpHeader[] headers = response.getHeaders("Location");
		Assert.assertTrue(headers.length==1);
		Assert.assertTrue(headers[0].getValue().equals(helper.getAdminUrl()));
		
		Utils.consumeInputStream(response.getResponseBody());
		
		// Tomcat will shoot you in the face if you don't follow it's redirects
		response = helper.getRequestExecuter().get(headers[0].getValue());
		Utils.consumeInputStream(response.getResponseBody());
	}
	
	@Test public void testUpdateGlobalSettings() throws Exception {
		
		// correct location of storage path prefix
		GlobalSettings settings = new GlobalSettings();
		settings.setExternalServiceUrlPrefix(AdminTestHelper.SERVICES_WEB_URL);
		settings.setMaxFileUploadSize(1234550);
		settings.setServiceManagementAuthSalt(AdminTestHelper.SERVICES_WEB_AUTH_SALT);
		settings.setTemporaryStoragePath(AdminTestHelper.ADMIN_WEB_STORAGE);
		
		// correct cluster node location and path prefix
		Map<String,ClusterNode> clusterNodeMap = new HashMap<String,ClusterNode>();
		ClusterNode node = new ClusterNode();
		node.setServiceWebUrlPrefix(AdminTestHelper.NODE_01_SERVICES_URL);
		node.setFileSystemStoragePathPrefix(AdminTestHelper.NODE_01_STORAGE);
		clusterNodeMap.put(node.getServiceWebUrlPrefix(), node);
		settings.setClusterNodes(clusterNodeMap);
		
		// validate settings stored in database
		Utils.consumeInputStream(helper.postGlobalSettings(settings).getResponseBody());
		
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
		app.setDescription(APP_DESC);
		app.setDeploymentHistoryLength(APP_DEPL_LEN);
		app.setVersionAdmins(APP_VERSION_ADMINS);
		app.setAdmins(APP_ADMINS);
		app.setInitialVersionIdentifier(VERSION_ORIG);
		
		HttpResponse response = helper.postAddModifyApp(app);
		
		Assert.assertTrue(response.getStatusCode()==200);
		String output = Utils.readInputStream(response.getResponseBody(),FormConstants.CHAR_ENC_DEFAULT);
		Assert.assertTrue(output.contains(APP_ADDMODIFY_SUCCESS));
		
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
		Utils.consumeInputStream(helper.postAddModifyApp(dbApp).getResponseBody());
		
		// validate changes
		modelManager.refresh(dbApp,null);
		Assert.assertTrue(dbApp.getDescription().equals(newDesc));
		Assert.assertTrue(dbApp.getDeploymentHistoryLength().equals(newLen));
		
		// TODO: validate changes are reflected by services-web, from the refresh hit
	}
	
	@Test public void testCreateApplicationVersion() throws Exception {
		_createApplicationVersion(VERSION_01,VERSION_01_NOTES,VERSION_01_ZIP,VERSION_01_HASH);
		_createApplicationVersion(VERSION_02,VERSION_02_NOTES,VERSION_02_ZIP,VERSION_02_HASH);
	}
	
	@Test public void testDeleteApplicationVersion() throws Exception {
		
		Assert.assertTrue("if no other version shares the archive, then the archive file should be deleted",_deleteApplicationVersion(VERSION_01));
		
		// recreate version01, except with the archive used by version02 
		_createApplicationVersion(VERSION_01,VERSION_01_NOTES,VERSION_02_ZIP,VERSION_02_HASH);
		Assert.assertTrue("if another version shares the archive, then leave it there",!_deleteApplicationVersion(VERSION_01));
		
		// restore original version01
		_createApplicationVersion(VERSION_01,VERSION_01_NOTES,VERSION_01_ZIP,VERSION_01_HASH);
	}
	
	@Test public void testCreateDeployments() throws Exception {
		
		_createDeployment(VERSION_01,Deployment.Type.IMMEDIATE);
		_createDeployment(VERSION_02,Deployment.Type.REQUIRED);
		
		_createDeployment(VERSION_02,Deployment.Type.REQUIRED);
		Assert.assertTrue("as this deployment is created, the archive for version01 should be removed from the deployed location",
				!_isVersionArchiveInDeployedLocation(VERSION_01_HASH));
		
		_createDeployment(VERSION_01,Deployment.Type.IMMEDIATE);
		Assert.assertTrue("as this deployment is created, the archive for version01 should be in the deployed location",
				_isVersionArchiveInDeployedLocation(VERSION_01_HASH));
		
		Application app = modelManager.getModelService().findApplicationByName(APP_NAME);
		Assert.assertTrue(app.getDeployments().size()==2);
		Assert.assertTrue(app.getDeployments().get(0).getApplicationVersion().getIdentifier().equals(VERSION_02));
		Assert.assertTrue(app.getDeployments().get(1).getApplicationVersion().getIdentifier().equals(VERSION_01));
	}
	
	@Test public void testDeleteApplication() throws Exception {
		
		ModelManager modelManager = helper.getModelManager();
		Application dbApp = modelManager.getModelService().findApplicationByName(APP_NAME);
		
		HttpResponse response = helper.postAddModifyApp_delete(dbApp);
		
		modelManager.getModelService().clearPersistenceContext();
		dbApp = modelManager.getModelService().findApplicationByName(APP_NAME);
		Assert.assertTrue(dbApp==null);
		
		Assert.assertTrue(!_isVersionArchiveInAdminLocation(VERSION_01_HASH));
		Assert.assertTrue(!_isVersionArchiveInAdminLocation(VERSION_02_HASH));
		Assert.assertTrue(!_isVersionArchiveInDeployedLocation(VERSION_01_HASH));
		Assert.assertTrue(!_isVersionArchiveInDeployedLocation(VERSION_02_HASH));
	}
	
	/*
	 * PRIVATE HELPER METHODS
	 */
	
	private void _createApplicationVersion(String identifier, String notes, String archiveName, String hash) throws Exception {
		
		Application app = modelManager.getModelService().findApplicationByName(APP_NAME);
		ApplicationVersion version = new ApplicationVersion();
		version.setIdentifier(identifier);
		version.setNotes(notes);
		app.addVersion(version);
		File uploadArchive = new File(this.getClass().getResource(archiveName).getFile());

		Utils.consumeInputStream(helper.postAddModifyAppVer(version, uploadArchive).getResponseBody());
		
		// archive is uploaded
		modelManager.getModelService().clearPersistenceContext();
		app = modelManager.getModelService().findApplicationByName(APP_NAME);
		version = app.getVersions().get(identifier);
		Assert.assertTrue(version!=null);
		
		// validate that the archive was uploaded and exploded
		Assert.assertTrue(_isVersionArchiveInAdminLocation(hash));
	}
	
	/**
	 * 
	 * @param identifier
	 * @return true if archive deleted
	 * @throws Exception
	 */
	private Boolean _deleteApplicationVersion(String identifier) throws Exception {
		
		ApplicationVersion version = modelManager.getModelService().findAppVersionByNameAndId(APP_NAME, identifier);
		String hash = version.getArchive().getHash();
		
		Utils.consumeInputStream(helper.postAddModifyAppVer_delete(version).getResponseBody());
		
		modelManager.getModelService().clearPersistenceContext();
		version = modelManager.getModelService().findAppVersionByNameAndId(APP_NAME, identifier);
		
		Assert.assertTrue(version==null);
		return !_isVersionArchiveInAdminLocation(hash);
	}
	
	private void _createDeployment(String identifier, Deployment.Type type) throws Exception {
		ApplicationVersion version = modelManager.getModelService().findAppVersionByNameAndId(APP_NAME, identifier);
		Utils.consumeInputStream(helper.postCreateDeployment(version, type).getResponseBody());
		Assert.assertTrue(_isVersionArchiveInDeployedLocation(version.getArchive().getHash()));
	}
	
	private Boolean _isVersionArchiveInAdminLocation(String hash) {
		File verAr = new File(AdminTestHelper.ADMIN_WEB_STORAGE+"/"+hash);
		File verAr2 = new File(AdminTestHelper.ADMIN_WEB_STORAGE+"/"+hash+".zip");
		return verAr2.exists() && verAr2.isFile() && verAr.exists() && verAr.isDirectory();
	}
	
	private Boolean _isVersionArchiveInDeployedLocation(String hash) {
		File verAr = new File(AdminTestHelper.NODE_01_STORAGE+"/"+hash+".zip");
		return verAr.exists() && verAr.isFile();
	}
}
