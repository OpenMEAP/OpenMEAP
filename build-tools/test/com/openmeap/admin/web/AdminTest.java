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
import org.apache.log4j.Logger;
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
		org.apache.log4j.BasicConfigurator.configure();
	}
	
	@AfterClass static public void afterClass() {
	}
	
	/*@Test public void test() throws Exception {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("test", "value");
		map.put("test2", "value2");
		map.put("test3", "value3");
		HttpResponse response = helper.getRequestExecuter().postData("http://localhost/get-post.php", map, map);
		String output = Utils.readInputStream(response.getEntity().getContent(),FormConstants.CHAR_ENC_DEFAULT);
		Assert.assertTrue(output.matches("successfully created"));
	}*/

	@Test public void testLogin() throws Exception {
		
		HttpResponse response = helper.getLogin();
		EntityUtils.consume(response.getEntity());
		
		response = helper.postLogin("tomcat", "tomcat");
		
		Assert.assertTrue(response.getStatusLine().getStatusCode()==302);
		Header[] headers = response.getHeaders("Location");
		Assert.assertTrue(headers.length==1);
		Assert.assertTrue(headers[0].getValue().equals(helper.getAdminUrl()));
		
		EntityUtils.consume(response.getEntity());
	
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
