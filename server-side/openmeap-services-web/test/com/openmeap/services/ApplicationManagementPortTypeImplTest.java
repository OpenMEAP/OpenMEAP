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

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openmeap.model.*;
import com.openmeap.model.dto.Deployment;
import com.openmeap.protocol.WebServiceException;
import com.openmeap.protocol.dto.*;
import com.openmeap.util.AuthTokenProvider;

public class ApplicationManagementPortTypeImplTest {
	
	Logger logger = LoggerFactory.getLogger(ApplicationManagementPortTypeImplTest.class);
	
	static ModelManager modelManager = null;
	
	@BeforeClass static public void beforeClass() {
		if( modelManager==null ) {
			ModelTestUtils.resetTestDb();
			ModelTestUtils.createModel(null);
			modelManager = ModelTestUtils.createModelManager();
		}
	}
	
	@AfterClass static public void afterClass() {
		ModelTestUtils.resetTestDb();
	}
	
	@Test public void testConnectionOpen() throws Exception {
		
		ConnectionOpenResponse response = null;
		Boolean thrown = false;
		
		ApplicationManagementServiceImpl ams = new ApplicationManagementServiceImpl();
		ams.setModelManager(modelManager);
		
		ConnectionOpenRequest req = new ConnectionOpenRequest();
		req.setApplication(new com.openmeap.protocol.dto.Application());
		req.getApplication().setInstallation(new com.openmeap.protocol.dto.ApplicationInstallation());
		req.getApplication().getInstallation().setUuid("Device.uuid.1");
		req.getApplication().setName("Application.name");
		req.setSlic(new SLIC());
		req.getSlic().setVersionId("CURRENT_VERSION_UNUSED");
		
		////////////////
		// Verify that an exception will get thrown if the version claimed does not exist
		req.getApplication().setVersionId("VERSION_DOES_NOT_EXIST");
		try {
			response = ams.connectionOpen(req);
		} catch( WebServiceException wse ) {
			thrown = true;
		}
		Assert.assertTrue("A non-existent application version should trigger an exception",thrown);
		
		////////////////
		// Verify that, when the current version is specified,
		// an authentication token is generated and no UpdateHeader is returned
		req.getApplication().setVersionId("ApplicationVersion.identifier.2");
		response = ams.connectionOpen(req);
		Assert.assertTrue(response.getUpdate()==null);
		Assert.assertTrue(response.getAuthToken()!=null && response.getAuthToken().length()>0);
		
		////////////////
		// Verify that, when version that exists, but is not the current version, is specified,
		// an authentication token is generated AS WELL AS an UpdateHeader
		req.getApplication().setVersionId("ApplicationVersion.identifier.1");
		com.openmeap.model.dto.ApplicationVersion appVer = modelManager.findAppVersionByNameAndId(req.getApplication().getName(), "ApplicationVersion.identifier.2");
		com.openmeap.model.dto.Application app = appVer.getApplication();
		response = ams.connectionOpen(req);
		Assert.assertTrue(response.getAuthToken()!=null && response.getAuthToken().length()>0);
		Assert.assertTrue(response.getUpdate()!=null);
		Assert.assertTrue(response.getUpdate().getInstallNeeds().intValue()==appVer.getArchive().getBytesLength()+appVer.getArchive().getBytesLengthUncompressed());
		Assert.assertTrue(response.getUpdate().getStorageNeeds().intValue()==appVer.getArchive().getBytesLengthUncompressed());
		Assert.assertTrue(response.getUpdate().getHash().getValue().compareTo(appVer.getArchive().getHash())==0);
		Assert.assertTrue(AuthTokenProvider.validateAuthToken(app.getProxyAuthSalt(), response.getAuthToken()));
		
		
		////////////////
		// Verify that, if a current version has not been established for an application,
		// an exception is thrown
		app = modelManager.findApplication(1L);
		Iterator<Deployment> i = new ArrayList<Deployment>(app.getDeployments()).iterator();
		while(i.hasNext()) {
			Deployment d = i.next();
			modelManager.getModelService().delete(d);
		}
		Assert.assertTrue(app.getDeployments().size()==0);
		thrown = false;
		try {
			response = ams.connectionOpen(req);
		} catch( WebServiceException wse ) {
			thrown = true;
		}
		Assert.assertTrue("An application that has not set a current version should trigger an exception",thrown);
		app=modelManager.addModify(app);
	}
}
