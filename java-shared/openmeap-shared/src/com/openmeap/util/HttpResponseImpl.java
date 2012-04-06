package com.openmeap.util;

import java.io.InputStream;

public class HttpResponseImpl implements HttpResponse {

	private InputStream responseBody;
	private int statusCode;
	private long contentLength;
	
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
}
