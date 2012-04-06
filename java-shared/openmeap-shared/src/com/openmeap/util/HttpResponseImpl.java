package com.openmeap.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
		List retHeaders = new ArrayList();
		for(int i=0; i<headers.length; i++) {
			if( headers[i].getKey().equalsIgnoreCase(name) ) {
				retHeaders.add(headers[i]);
			}
		}
		HttpHeader[] headers = new HttpHeader[retHeaders.size()];
		for( int i=0; i<retHeaders.size(); i++ ) {
			headers[i]=(HttpHeader) retHeaders.get(i);
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
