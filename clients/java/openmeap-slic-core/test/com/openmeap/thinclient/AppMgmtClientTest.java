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

package com.openmeap.thinclient;

import java.io.InputStream;
import java.util.Hashtable;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.openmeap.http.HttpRequestExecuterFactory;
import com.openmeap.protocol.ApplicationManagementService;
import com.openmeap.protocol.WebServiceException;
import com.openmeap.protocol.dto.Application;
import com.openmeap.protocol.dto.ApplicationInstallation;
import com.openmeap.protocol.dto.ConnectionOpenRequest;
import com.openmeap.protocol.dto.ConnectionOpenResponse;
import com.openmeap.protocol.dto.HashAlgorithm;
import com.openmeap.protocol.dto.SLIC;
import com.openmeap.util.MockHttpRequestExecuter;
import com.openmeap.util.Utils;

public class AppMgmtClientTest extends TestCase {

	public void testRESTOpenConnection() throws Exception {
		
		AppMgmtClientFactory.setDefaultType(RESTAppMgmtClient.class);
		String[] templates = {
			"xml/connectionResponse-rest-update.json",
			"xml/connectionResponse-rest-noupdate.json",
			"xml/connectionResponse.404.text"
		};

		HttpRequestExecuterFactory.setDefaultType(MockHttpRequestExecuter.class);
		ApplicationManagementService client = AppMgmtClientFactory.newDefault("/nowhere/");
		
		ConnectionOpenRequest request = new ConnectionOpenRequest();
		request.setApplication(new Application());
		request.getApplication().setInstallation(new ApplicationInstallation());
		request.setSlic(new SLIC());
		
		// setup the response xml that we'll spoof as though it's from the server
		Hashtable parms = new Hashtable();
		parms.put("AUTH_TOKEN", "auth_token");
		parms.put("UPDATE_TYPE", "required");
		parms.put("UPDATE_URL", "file://none");
		parms.put("STORAGE_NEEDS", String.valueOf(15));
		parms.put("INSTALL_NEEDS", String.valueOf(15));
		parms.put("HASH", "asdf");
		parms.put("HASH_ALG", "MD5");
		parms.put("VERSION_ID", "versionId");
		InputStream inputStream = AppMgmtClientTest.class.getResourceAsStream(templates[0]);
		MockHttpRequestExecuter.setResponseText( Utils.replaceFields(parms, Utils.readInputStream(inputStream,"UTF-8") ) );
		
		// setup our request
		SLIC slic = request.getSlic();
		slic.setVersionId("slicVersion");
		Application app = request.getApplication();
		app.setName("appName");
		app.setVersionId("appVersionId");
		ApplicationInstallation appInst = request.getApplication().getInstallation();
		appInst.setUuid("appInstUuid");

		//////////////
		// Verify that a well-formed request will result in a correctly formed response object
		ConnectionOpenResponse response = client.connectionOpen(request);
		Assert.assertTrue(response.getAuthToken().equals("auth_token"));
		Assert.assertTrue(response.getUpdate().getUpdateUrl().equals("file://none"));
		Assert.assertTrue(response.getUpdate().getInstallNeeds().equals(Long.valueOf(16)));
		Assert.assertTrue(response.getUpdate().getStorageNeeds().equals(Long.valueOf(15)));
		Assert.assertTrue(response.getUpdate().getVersionIdentifier().equals("versionId"));
		Assert.assertTrue(response.getUpdate().getHash().getValue().equals("asdf"));
		Assert.assertTrue(response.getUpdate().getHash().getAlgorithm().equals(HashAlgorithm.MD5));
		
		//////////////
		// Verify that the xml, sans the update header, will generate a response with no update
		MockHttpRequestExecuter.setResponseText( Utils.replaceFields(parms, 
				Utils.readInputStream(AppMgmtClientTest.class.getResourceAsStream(templates[1]),"UTF-8") ) );
		response = client.connectionOpen(request);
		Assert.assertTrue(response.getAuthToken().equals("auth_token"));
		Assert.assertTrue(response.getUpdate()==null);
		
		//////////////
		// Verify that a non-200 will result in a WebServiceException
		Boolean thrown = Boolean.FALSE;
		Exception e = null;
		MockHttpRequestExecuter.setResponseCode(304); // 304 is not 200
		try {
			response = client.connectionOpen(request);
		} catch( Exception wse ) {
			e = wse;
			thrown = Boolean.TRUE;
		}
		Assert.assertTrue(thrown.booleanValue() && e instanceof WebServiceException);
		
		//////////////
		// Verify that invalid response content will throw an exception
		thrown = Boolean.FALSE; e=null;
		MockHttpRequestExecuter.setResponseText( Utils.replaceFields(parms, 
				Utils.readInputStream(AppMgmtClientTest.class.getResourceAsStream(templates[2]),"UTF-8") ) );
		MockHttpRequestExecuter.setResponseCode(200);
		try {
			response = client.connectionOpen(request);
		} catch( Exception wse ) {
			e = wse;
			thrown = Boolean.TRUE;
		}
		Assert.assertTrue(thrown.booleanValue() && e instanceof WebServiceException);
	}
}
