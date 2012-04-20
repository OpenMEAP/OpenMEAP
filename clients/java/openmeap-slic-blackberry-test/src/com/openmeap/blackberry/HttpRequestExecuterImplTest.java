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

package com.openmeap.blackberry;

import java.io.ByteArrayInputStream;
import java.util.Hashtable;

import com.openmeap.blackberry.HttpRequestExecuterImpl;
import com.openmeap.constants.FormConstants;
import com.openmeap.constants.UrlParamConstants;
import com.openmeap.http.HttpRequestExecuter;
import com.openmeap.http.HttpResponse;
import com.openmeap.util.AuthTokenProvider;
import com.openmeap.util.GenericRuntimeException;
import com.openmeap.util.Utils;

public class HttpRequestExecuterImplTest implements Runnable {
	
	private static String TEST_AUTH_SALT = "auth-salt";
			
	OpenMEAPAppTestScreen screen;
	String url = "http://"+OpenMEAPAppTest.HTTP_HOST+"/openmeap-services-web/service-management/";
	
	public HttpRequestExecuterImplTest(OpenMEAPAppTestScreen screen) {
		this.screen=screen;
	}
	
	public void run() {
		try {
			screen.append("<h3>HttpRequestExecuterImplTest</h3><br/>");
			
			screen.append("URL: "+url+"<br/><br/>");
			
			// test get
			screen.append("GET request<br/>");
			String expectedHash = "bf9b937919b4438954f7d390c564f5b5";
			HttpRequestExecuter requester = new HttpRequestExecuterImpl();
			HttpResponse response = requester.get(url);
			String str = Utils.readInputStream(response.getResponseBody(), FormConstants.CHAR_ENC_DEFAULT);
			String md5 = Utils.hashInputStream("md5",new ByteArrayInputStream(str.getBytes()));
			screen.assertTrue(expectedHash,md5.equals(expectedHash));
			
			screen.append("POST request<br/>");
			expectedHash = "cd2c31a043afe728e96c56ffb98a1e3d";
			Hashtable parms = new Hashtable();
			parms.put(UrlParamConstants.ACTION, "refresh");
			parms.put(UrlParamConstants.AUTH_TOKEN, AuthTokenProvider.newAuthToken(TEST_AUTH_SALT));
			parms.put(UrlParamConstants.REFRESH_TYPE, "Application");
			parms.put(UrlParamConstants.REFRESH_OBJ_PKID, "999999");
			response = requester.postData(url,parms);
			str = Utils.readInputStream(response.getResponseBody(), FormConstants.CHAR_ENC_DEFAULT);
			md5 = Utils.hashInputStream("md5",new ByteArrayInputStream(str.getBytes()));
			screen.assertTrue(expectedHash,md5.equals(expectedHash));
			
		} catch(Exception e) {
			throw new GenericRuntimeException(e);
		}
	}
}
