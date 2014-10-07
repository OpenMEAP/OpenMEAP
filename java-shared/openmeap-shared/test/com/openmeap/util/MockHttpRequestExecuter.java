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

package com.openmeap.util;

import java.io.ByteArrayInputStream;
import java.util.Hashtable;

import com.openmeap.http.HttpRequestException;
import com.openmeap.http.HttpRequestExecuter;
import com.openmeap.http.HttpResponse;
import com.openmeap.http.HttpResponseImpl;

public class MockHttpRequestExecuter implements HttpRequestExecuter {
	static private int responseCode = 200;
	static private String responseText = "";
	
	static private String lastPostXmlData = null;
	
	static private Hashtable lastGetData = null;
	static private Hashtable lastPostData = null;
	static private String lastPostUrl = null;
	
	static public void setResponseCode(int responseCode) {
		MockHttpRequestExecuter.responseCode = responseCode;
	}
	static public void setResponseText(String text) {
		MockHttpRequestExecuter.responseText = text;
	}
	static public Hashtable getLastPostData() {
		return lastPostData;
	}
	static public Hashtable getLastGetData() {
		return lastGetData;
	}
	static public String getLastPostUrl() {
		return lastPostUrl;
	}
	static public String getLastPostXmlData() {
		return lastPostXmlData;
	}
	public HttpResponse postContent(String url, String content, String contentType) throws HttpRequestException {
		lastPostUrl = url;
		lastPostXmlData = content;
		return putTogetherResponse();
	}
	public HttpResponse postData(String url,Hashtable getData, Hashtable postData) throws HttpRequestException {
		lastGetData = getData;
		lastPostData = postData;
		lastPostUrl = url;
		return putTogetherResponse();
	}
	public HttpResponse postData(String url, Hashtable postData) throws HttpRequestException {
		lastPostData = postData;
		lastPostUrl = url;
		return putTogetherResponse();
	}
	public HttpResponse get(String url) throws HttpRequestException {
		return putTogetherResponse();
	}
	
	private HttpResponse putTogetherResponse() throws HttpRequestException {
		try {
			HttpResponseImpl response = new HttpResponseImpl();
			response.setContentLength(responseText.length());
			response.setStatusCode(responseCode);
			response.setResponseBody(new ByteArrayInputStream(responseText.getBytes()));
			return response;
		} catch(Exception e) {
			throw new HttpRequestException(e);
		}
	}
	public HttpResponse get(String url, Hashtable params) throws HttpRequestException {
		lastPostData = lastGetData = params;
		lastPostUrl = url;
		return putTogetherResponse();
	}
	public void shutdown() {}
}
