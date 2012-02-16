package com.openmeap.model.service;

import org.junit.*;

import java.util.*;
import java.net.*;

import com.openmeap.cluster.ClusterServiceNotifierConfig;
import com.openmeap.model.*;
import com.openmeap.model.dto.Application;
import com.openmeap.util.*;

public class ModelServiceRefreshNotifierTest {
	
	private static ModelManager modelManager = null;
	
	@BeforeClass static public void beforeClass() {
		if( modelManager == null ) {
			ModelTestUtils.resetTestDb();
			ModelTestUtils.createModel(null);
			modelManager = ModelTestUtils.createModelManager();
		}
	}
	
	@AfterClass static public void afterClass() {
		ModelTestUtils.resetTestDb();
	}
	
	@Test public void testHandlePostSaveOrUpdate() throws Exception {
		
		ModelServiceRefreshNotifier wsrn = new ModelServiceRefreshNotifier();
		wsrn.setConfig(new ClusterServiceNotifierConfig());
		wsrn.getConfig().setAuthSalt(UUID.randomUUID().toString());
		
		MockHttpRequestExecuter.setResponseCode(200);
		MockHttpRequestExecuter.setResponseText("");
		MockHttpRequestExecuter httpExecuter = new MockHttpRequestExecuter();
		
		wsrn.setHttpRequestExecuter(httpExecuter);
		
		List<URL> lu = new ArrayList<URL>();
		lu.add( new URL("http://www.openmeap.com/openmeap-services-web") );
		wsrn.getConfig().setServerUrls(lu);
		
		Application app = modelManager.getModelService().findByPrimaryKey(Application.class,1L);
		app.setName(wsrn.getConfig().getAuthSalt());
		wsrn.notify(new ModelEntityModifyEvent(app));
		
		String lastPostUrl = MockHttpRequestExecuter.getLastPostUrl();
		Map<String,Object> lastPostData = MockHttpRequestExecuter.getLastPostData();
		String uri = lastPostUrl;
		String type = (String)lastPostData.get("type");
		String auth = (String)lastPostData.get("auth");
		String id = (String)lastPostData.get("id").toString();
		Assert.assertTrue(uri.equals("http://www.openmeap.com/openmeap-services-web/service-management/"));
		Assert.assertTrue(id.equals("1"));
		Assert.assertTrue(type.equals("Application"));
		Assert.assertTrue(AuthTokenProvider.validateAuthToken(wsrn.getConfig().getAuthSalt(),auth));
	}
}
