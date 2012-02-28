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

package com.openmeap.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

import com.openmeap.util.HttpRequestExecuterImpl;


public class MockHttpRequestExecuter implements HttpRequestExecuter {
	static private int responseCode = 200;
	static private String responseText = "";
	static private HttpGet lastHttpGet = null;
	
	static private String lastPostXmlData = null;
	
	static private Map<String,Object> lastPostData = null;
	static private String lastPostUrl = null;
	
	static public void setResponseCode(int responseCode) {
		MockHttpRequestExecuter.responseCode = responseCode;
	}
	static public void setResponseText(String text) {
		MockHttpRequestExecuter.responseText = text;
	}
	static public Map<String,Object> getLastPostData() {
		return lastPostData;
	}
	static public String getLastPostUrl() {
		return lastPostUrl;
	}
	static public HttpGet getLastHttpGet() {
		return lastHttpGet;
	}
	public HttpResponse postXml(String url,String xmlData) {
		lastPostUrl = url;
		lastPostXmlData = xmlData;
		return putTogetherResponse();
	}
	public HttpResponse postData(String url,Map<String,Object> postData) {
		lastPostData = postData;
		lastPostUrl = url;
		return putTogetherResponse();
	}
	public HttpResponse get(HttpGet httpGet) {
		lastHttpGet = httpGet;
		return putTogetherResponse();
	}
	public HttpResponse get(String url) {
		return putTogetherResponse();
	}
	
	private HttpResponse putTogetherResponse() {
		ProtocolVersion ver = new ProtocolVersion("HTTP",1,1);
		StatusLine status = new BasicStatusLine(ver,responseCode,"OK");
		HttpResponse resp = new BasicHttpResponse(status);
		BasicHttpEntity ent = new BasicHttpEntity();
		ent.setContent(new BufferedInputStream(new ByteArrayInputStream(responseText.getBytes())));
		resp.setEntity(ent);
		return resp;
	}
	public HttpResponse get(String url, Map<String, Object> params)
			throws ClientProtocolException, IOException {
		lastPostData = params;
		lastPostUrl = url;
		return putTogetherResponse();
	}
	public void shutdown() {}
}