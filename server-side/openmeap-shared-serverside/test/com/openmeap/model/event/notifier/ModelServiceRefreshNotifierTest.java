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

package com.openmeap.model.event.notifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import mockit.NonStrictExpectations;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.openmeap.digest.DigestInputStreamFactory;
import com.openmeap.digest.Md5DigestInputStream;
import com.openmeap.digest.Sha1DigestInputStream;
import com.openmeap.model.ModelManager;
import com.openmeap.model.ModelManagerImpl;
import com.openmeap.model.ModelService;
import com.openmeap.model.ModelServiceImpl;
import com.openmeap.model.dto.Application;
import com.openmeap.model.dto.ClusterNode;
import com.openmeap.model.dto.GlobalSettings;
import com.openmeap.model.event.ModelEntityModifyEvent;
import com.openmeap.util.AuthTokenProvider;
import com.openmeap.util.MockHttpRequestExecuter;

public class ModelServiceRefreshNotifierTest {
	
	@BeforeClass static public void beforeClass() {
		DigestInputStreamFactory.setDigestInputStreamForName("MD5", Md5DigestInputStream.class);
		DigestInputStreamFactory.setDigestInputStreamForName("SHA1", Sha1DigestInputStream.class);
	}
	
	@mockit.MockClass(realClass=ModelManagerImpl.class)
	class MockModelManager extends ModelManagerImpl {
		final ModelService modelService = new ModelServiceImpl();
		public ModelService getModelService() { return modelService; }
	}
	
	@Test public void testHandlePostSaveOrUpdate() throws Exception {
		
		try {new NonStrictExpectations() {{}};} catch(Exception e){};
		
		MockHttpRequestExecuter.setResponseCode(200);
		MockHttpRequestExecuter.setResponseText("");
		MockHttpRequestExecuter httpExecuter = new MockHttpRequestExecuter();
		
		final ModelManager modelManager = new MockModelManager();
		final GlobalSettings globalSettings = new GlobalSettings();
		globalSettings.setServiceManagementAuthSalt(UUID.randomUUID().toString());

		ClusterNode clusterNode = new ClusterNode();
		clusterNode.setServiceWebUrlPrefix("http://www.openmeap.com/openmeap-services-web");
		globalSettings.addClusterNode(clusterNode);
		new NonStrictExpectations(globalSettings,modelManager) {{
			modelManager.getGlobalSettings(); result = globalSettings;
		}};
		
		Application app = new Application();
		app.setName("Happy Name");
		app.setId(1L);
		
		ModelServiceRefreshNotifier notifier = new ModelServiceRefreshNotifier();
		notifier.setModelManager(modelManager);
		notifier.setHttpRequestExecuter(httpExecuter);
		
		notifier.notify(new ModelEntityModifyEvent(app),null);
		
		String lastPostUrl = MockHttpRequestExecuter.getLastPostUrl();
		Map<String,Object> lastPostData = MockHttpRequestExecuter.getLastPostData();
		String uri = lastPostUrl;
		String type = (String)lastPostData.get("type");
		String auth = (String)lastPostData.get("auth");
		String id = (String)lastPostData.get("id").toString();
		Assert.assertTrue(uri.equals("http://www.openmeap.com/openmeap-services-web/service-management/"));
		Assert.assertTrue(id.equals("1"));
		Assert.assertTrue(type.equals("Application"));
		Assert.assertTrue(AuthTokenProvider.validateAuthToken(globalSettings.getServiceManagementAuthSalt(),auth));
	}
}
