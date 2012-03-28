package com.openmeap.admin.web;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.openmeap.constants.FormConstants;
import com.openmeap.model.dto.Application;
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

	static private AdminTestHelper helper = new AdminTestHelper();
	
	@BeforeClass static public void beforeClass() {
	}
	
	@AfterClass static public void afterClass() {
	}
	
	@Test public void testLogin() throws Exception {
		
		HttpResponse response = helper.getLogin();
		Utils.consumeInputStream(response.getEntity().getContent());
		
		response = helper.postLogin("tomcat", "tomcat");
		Assert.assertTrue(response.getStatusLine().getStatusCode()==302);
		Assert.assertTrue(response.getHeaders("Location").length==1);
		Header[] headers = response.getHeaders("Location");
		Assert.assertTrue(headers[0].getValue().equals(helper.getAdminUrl()));
		Utils.consumeInputStream(response.getEntity().getContent());
	}
	
	@Test public void testAppAddModifyPage() throws Exception {
		
		Application app = new Application();
		app.setName("Happy Appy");
		app.setDescription("This is my happy appy");
		app.setDeploymentHistoryLength(10);
		app.setVersionAdmins("juno");
		app.setAdmins("jacob");
		app.setInitialVersionIdentifier("ver-1.1.x");
		
		HttpResponse response = helper.postAddModifyApp(app);
		
		Assert.assertTrue(response.getStatusLine().getStatusCode()==200);
		String output = Utils.readInputStream(response.getEntity().getContent(),FormConstants.CHAR_ENC_DEFAULT);
		Assert.assertTrue(output.matches("successfully created"));
		// TODO: validate that the application is created in the database.
	}
}
