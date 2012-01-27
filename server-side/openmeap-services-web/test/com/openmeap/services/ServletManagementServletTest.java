/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011 OpenMEAP, Inc.                                        #
 #    Credits to Jonathan Schang & Robert Thacher                              #
 #                                                                             #
 #    Released under the GPLv3                                                 #
 #                                                                             #
 #    OpenMEAP is free software: you can redistribute it and/or modify         #
 #    it under the terms of the GNU General Public License as published by     #
 #    the Free Software Foundation, either version 3 of the License, or        #
 #    (at your option) any later version.                                      #
 #                                                                             #
 #    OpenMEAP is distributed in the hope that it will be useful,              #
 #    but WITHOUT ANY WARRANTY; without even the implied warranty of           #
 #    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the            #
 #    GNU General Public License for more details.                             #
 #                                                                             #
 #    You should have received a copy of the GNU General Public License        #
 #    along with OpenMEAP.  If not, see <http://www.gnu.org/licenses/>.        #
 #                                                                             #
 ###############################################################################
 */

package com.openmeap.services;

import com.openmeap.constants.UrlParamConstants;
import com.openmeap.model.*;

import org.springframework.mock.web.*;
import org.junit.*;

import java.util.*;
import java.io.*;
import org.w3c.dom.*;

import com.openmeap.services.ServiceManagementServlet;
import com.openmeap.model.dto.Application;
import com.openmeap.model.dto.GlobalSettings;
import com.openmeap.model.service.ModelServiceRefreshHandler;
import com.openmeap.util.AuthTokenProvider;
import com.openmeap.util.Utils;

public class ServletManagementServletTest {
	
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
	
	class Request extends MockHttpServletRequest {
		private Map<String,String[]> parameters = new HashMap<String,String[]>();
		public Map<String,String[]> getParameterMap() {
			return parameters;
		}
	};	
	
	@Test public void testRefreshApplication() throws Exception {
		MockHttpServletRequest request = new Request();	
		MockHttpServletResponse response = new MockHttpServletResponse();
		String randomUuid = UUID.randomUUID().toString();
		
		GlobalSettings settings = modelManager.getGlobalSettings();
		
		/////////////////
		// validate that finding the application, modifying it, and then finding it again 
		// will return an object with the same modifications.
		Application app = modelManager.findApplication(1L);
		app.setName(randomUuid);
		Assert.assertTrue(modelManager.findApplication(1L).getName().equals(randomUuid));
		
		modelManager.getModelService().refresh(app);
		app = modelManager.findApplication(1L);
		Assert.assertTrue(!modelManager.findApplication(1L).getName().equals(randomUuid));
		
		ServiceManagementServlet servlet = new ServiceManagementServlet();
		servlet.setModelManager(modelManager);
		servlet.setModelServiceRefreshHandler(new ModelServiceRefreshHandler());
		servlet.getModelServiceRefreshHandler().setModelManager(modelManager);
		
		////////////////////
		// validate the happy path of providing all the required information
		String authSalt = servlet.getAuthSalt();
		String authToken = AuthTokenProvider.newAuthToken(authSalt);
		request.setParameter(UrlParamConstants.REFRESH_TYPE, "Application");
		request.setParameter(UrlParamConstants.REFRESH_OBJ_PKID, "1");
		request.setParameter(UrlParamConstants.AUTH_TOKEN, authToken);
		request.setParameter(UrlParamConstants.ACTION, ModelEntityModifyEvent.NAME);
		servlet.service(request,response);
		String contentString = response.getContentAsString();
		Document result = Utils.getDocument(new ByteArrayInputStream(contentString.getBytes()));
		Assert.assertTrue( result.getFirstChild().getAttributes().getNamedItem("status").getNodeValue().equals("success"));
		Assert.assertTrue( ! modelManager.findApplication(1L).getName().equals(randomUuid) );
		
		////////////////////
		// validate that failing to provide auth token fails to refresh cache
		app = modelManager.findApplication(1L);
		app.setName(randomUuid);
		response = new MockHttpServletResponse();
		request.removeParameter(UrlParamConstants.AUTH_TOKEN);
		request.setParameter(UrlParamConstants.ACTION,ModelEntityModifyEvent.NAME);
		request.setParameter(UrlParamConstants.REFRESH_TYPE, "Application");
		request.setParameter(UrlParamConstants.REFRESH_OBJ_PKID, "1");
		servlet.service(request,response);
		contentString = response.getContentAsString();
		result = Utils.getDocument(new ByteArrayInputStream(response.getContentAsString().getBytes()));
		Assert.assertTrue( result.getFirstChild().getAttributes().getNamedItem("status").getNodeValue().equals("failure"));
		Assert.assertTrue( modelManager.findApplication(1L).getName().equals(randomUuid) );

	}
}
