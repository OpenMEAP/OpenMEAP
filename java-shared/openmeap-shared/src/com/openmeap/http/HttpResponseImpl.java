/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2016 OpenMEAP, Inc.                                   #
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

package com.openmeap.http;

import java.io.InputStream;
import java.util.Vector;

public class HttpResponseImpl implements HttpResponse {

	private InputStream responseBody;
	private int statusCode;
	private long contentLength;
	private HttpHeader[] headers;
	
	public long getContentLength() {
		return contentLength;
	}
	public void setContentLength(long contentLength) {
		this.contentLength = contentLength;
	}

	public InputStream getResponseBody() {
		return responseBody;
	}
	public void setResponseBody(InputStream responseBody) {
		this.responseBody = responseBody;
	}
	
	public int getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
	
	public HttpHeader[] getHeaders(String name) {
		Vector retHeaders = new Vector();
		for(int i=0; i<headers.length; i++) {
			if( headers[i].getKey().equalsIgnoreCase(name) ) {
				retHeaders.addElement(headers[i]);
			}
		}
		HttpHeader[] headers = new HttpHeader[retHeaders.size()];
		for( int i=0; i<retHeaders.size(); i++ ) {
			headers[i]=(HttpHeader) retHeaders.elementAt(i);
		}
		return headers;
	}
	public HttpHeader[] getHeaders() {
		return headers;
	}
	public void setHeaders(HttpHeader[] headers) {
		this.headers = headers;
	}
	
}
