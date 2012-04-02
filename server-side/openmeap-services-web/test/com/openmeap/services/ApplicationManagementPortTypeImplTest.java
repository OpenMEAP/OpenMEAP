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

import java.util.ArrayList;
import java.util.Iterator;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openmeap.model.ModelManager;
import com.openmeap.model.ModelTestUtils;
import com.openmeap.model.dto.Application;
import com.openmeap.model.dto.Deployment;
import com.openmeap.protocol.WebServiceException;
import com.openmeap.protocol.dto.ConnectionOpenRequest;
import com.openmeap.protocol.dto.ConnectionOpenResponse;
import com.openmeap.protocol.dto.SLIC;
import com.openmeap.util.AuthTokenProvider;

public class ApplicationManagementPortTypeImplTest {
	
	Logger logger = LoggerFactory.getLogger(ApplicationManagementPortTypeImplTest.class);
	
	static ModelManager modelManager = null;
	
	private ConnectionOpenResponse response = null;
	private Boolean thrown = false;
	private ApplicationManagementServiceImpl appMgmtSvc = null;
	private ConnectionOpenRequest request;
	
	@Before public void before() {
		
		//if( modelManager==null ) {
			ModelTestUtils.resetTestDb();
			ModelTestUtils.createModel(null);
			modelManager = ModelTestUtils.createModelManager();
		//}
		
		response = null;
		thrown = false;
		appMgmtSvc = new ApplicationManagementServiceImpl();
		appMgmtSvc.setModelManager(modelManager);
		
		request = new ConnectionOpenRequest();
		request.setApplication(new com.openmeap.protocol.dto.Application());
		request.getApplication().setInstallation(new com.openmeap.protocol.dto.ApplicationInstallation());
		request.getApplication().getInstallation().setUuid("Device.uuid.1");
		request.getApplication().setName("Application.name");
		request.getApplication().setVersionId("ApplicationVersion.identifier.bundled");
		request.setSlic(new SLIC());
		request.getSlic().setVersionId("CURRENT_VERSION_UNUSED");
	}
	
	@After public void after() {
		ModelTestUtils.resetTestDb();
	}
	
	/**
	 * Verify that an exception is thrown if a version that does not exist is reported by SLIC
	 * @throws Exception
	 */
	@Test public void testConnectionOpen_verifyExceptionOnNonExistingVerison() throws Exception {
		request.getApplication().setVersionId("VERSION_DOES_NOT_EXIST");
		try {
			response = appMgmtSvc.connectionOpen(request);
		} catch( WebServiceException wse ) {
			thrown = true;
		}
		Assert.assertTrue("A non-existent application version should trigger an exception",thrown);
	}
	
	/**
	 * Verify that no exceptions are thrown and no update returned
	 * when the version SLIC reports is the same as the initial version.
	 */
	@Test public void testConnectionOpen_verifyInitialVersionIdentifierRecognized() throws Exception {
		thrown = false;
		request.getApplication().setVersionId("ApplicationVersion.identifier.bundled");
		
		com.openmeap.model.dto.Application app = modelManager.getModelService().findByPrimaryKey(Application.class, 1L);
		Iterator<Deployment> i = new ArrayList<Deployment>(app.getDeployments()).iterator();
		while(i.hasNext()) {
			Deployment d = i.next();
			modelManager.delete(d);
		}
		
		try {
			response = appMgmtSvc.connectionOpen(request);
		} catch( WebServiceException wse ) {
			thrown = true;
		}
		Assert.assertTrue("No update should be returned here",response.getUpdate()==null);
		Assert.assertTrue("The originally bundled application version id should not trigger an exception",thrown==false);
	}
	
	/**
	 * Verify that SLIC reporting the currently deployed version triggers no update
	 * @throws Exception
	 */
	@Test public void testConnectionOpen_verifyCurrentVersionTriggersNoUpdateNotify() throws Exception  {
		request.getApplication().setVersionId("ApplicationVersion.identifier.2");
		response = appMgmtSvc.connectionOpen(request);
		Assert.assertTrue(response.getUpdate()==null);
		Assert.assertTrue(response.getAuthToken()!=null && response.getAuthToken().length()>0);
	}
	
	/**
	 * Verify that SLIC reporting the currently deployed version triggers an update
	 * when the hash does not match the hash associated to the currently deployed version
	 * @throws Exception
	 */
	@Test public void testConnectionOpen_verifyCurrentVersionTriggersUpdateWhenHashDiffers() throws Exception  {
		request.getApplication().setVersionId("ApplicationVersion.identifier.2");
		request.getApplication().setHashValue("Differing Hash Value");
		response = appMgmtSvc.connectionOpen(request);
		Assert.assertTrue(response.getUpdate()!=null);
		Assert.assertTrue(response.getAuthToken()!=null && response.getAuthToken().length()>0);
	}
	
	/**
	 * Verify that an update header is returned if the version reported by SLIC
	 * does not match the version of the most recent deployment and
	 * verify that the authentication token is generated using the proxy salt
	 * associated to the application.
	 * @throws Exception
	 */
	@Test public void testConnectionOpen_verifyUpdateHeaderOnUpdateRequired() throws Exception {
		////////////////
		// Verify that, when version that exists, but is not the current version, is specified,
		// an authentication token is generated AS WELL AS an UpdateHeader
		request.getApplication().setVersionId("ApplicationVersion.identifier.1");
		com.openmeap.model.dto.ApplicationVersion appVer = modelManager.getModelService().findAppVersionByNameAndId(request.getApplication().getName(), "ApplicationVersion.identifier.2");
		com.openmeap.model.dto.Application app = appVer.getApplication();
		response = appMgmtSvc.connectionOpen(request);
		Assert.assertTrue(response.getAuthToken()!=null && response.getAuthToken().length()>0);
		Assert.assertTrue(response.getUpdate()!=null);
		Assert.assertTrue(response.getUpdate().getInstallNeeds().intValue()==appVer.getArchive().getBytesLength()+appVer.getArchive().getBytesLengthUncompressed());
		Assert.assertTrue(response.getUpdate().getStorageNeeds().intValue()==appVer.getArchive().getBytesLengthUncompressed());
		Assert.assertTrue(response.getUpdate().getHash().getValue().compareTo(appVer.getArchive().getHash())==0);
		Assert.assertTrue(AuthTokenProvider.validateAuthToken(app.getProxyAuthSalt(), response.getAuthToken()));
	}
	
	/**
	 * Verify an exception occurs,
	 * when the SLIC current version is not communicated.
	 * @throws Exception
	 */
	@Test public void testConnectionOpen_verifyExceptionWhenCurrentVersionNotCommunicated() throws Exception {
		
		request.getApplication().setVersionId(null);
		thrown = false;
		try {
			response = appMgmtSvc.connectionOpen(request);
		} catch( WebServiceException wse ) {
			thrown = true;
		}
		Assert.assertTrue("An application that has not set a current version should trigger an exception",thrown);
	}
	
	/**
	 * Verify that no update is set,
	 * if there are no deployments
	 * and the initial version is reported
	 */ 
	@Test public void testConnectionOpen_verifyNoUpdateIfOnlyInitialVersion() throws Exception {
		com.openmeap.model.dto.Application app = modelManager.getModelService().findByPrimaryKey(Application.class,1L);
		Iterator<Deployment> i = new ArrayList<Deployment>(app.getDeployments()).iterator();
		while(i.hasNext()) {
			Deployment d = i.next();
			modelManager.delete(d);
		}
		app = modelManager.getModelService().findByPrimaryKey(Application.class,1L);
		Assert.assertTrue(app.getDeployments().size()==0);
		Assert.assertTrue(app.getVersions().size()==2);
		thrown = false;
		try {
			response = appMgmtSvc.connectionOpen(request);
		} catch( WebServiceException wse ) {
			thrown = true;
		}
		Assert.assertTrue("If no deployments have been made, it should be ok, providing the initial version is reported by SLIC.",!thrown);
		app=modelManager.addModify(app);
	}
	
	/**
	 * Verify an exception occurs,
	 * if no deployments have been made
	 * and a version is reported
	 */ 
	@Test public void testConnectionOpen_verifyExceptionOnUndeployedVersion() throws Exception {
		com.openmeap.model.dto.Application app = modelManager.getModelService().findByPrimaryKey(Application.class,1L);
		Iterator<Deployment> i = new ArrayList<Deployment>(app.getDeployments()).iterator();
		while(i.hasNext()) {
			Deployment d = i.next();
			modelManager.delete(d);
		}
		app = modelManager.getModelService().findByPrimaryKey(Application.class,1L);
		Assert.assertTrue(app.getDeployments().size()==0);
		Assert.assertTrue(app.getVersions().size()==2);
		request.getApplication().setVersionId("ApplicationVersion.identifier.1");
		thrown = false;
		try {
			response = appMgmtSvc.connectionOpen(request);
		} catch( WebServiceException wse ) {
			thrown = true;
		}
		Assert.assertTrue("SLIC reporting an undeployed version should throw an exception",thrown);
		app=modelManager.addModify(app);
	}
}
