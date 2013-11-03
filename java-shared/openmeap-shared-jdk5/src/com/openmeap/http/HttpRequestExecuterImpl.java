/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2014 OpenMEAP, Inc.                                   #
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

package com.openmeap.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;

import com.openmeap.constants.FormConstants;
import com.openmeap.util.SSLUtils;
import com.openmeap.util.Utils;

/**
 * A class to encapsulate the actual transport of http
 *  
 * @author schang
 */
public class HttpRequestExecuterImpl implements HttpRequestExecuter {
	
	private HttpClient httpClient = null;
	
	public HttpRequestExecuterImpl() {
		
		if( System.getProperty(HttpRequestExecuter.SSL_PEER_NOVERIFY_PROPERTY)!=null 
				&& Boolean.parseBoolean(System.getProperty(HttpRequestExecuter.SSL_PEER_NOVERIFY_PROPERTY))==Boolean.TRUE ) {
			httpClient = SSLUtils.getRelaxedSSLVerificationHttpClient();
		} else {
			httpClient = new DefaultHttpClient();
		}
		
		((DefaultHttpClient)httpClient).setCredentialsProvider(CredentialsProviderFactory.newDefaultCredentialsProvider());
		
		httpClient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
		httpClient.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, FormConstants.CHAR_ENC_DEFAULT);
		httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
		httpClient.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, true);
		
		// setup any proxy information
		String proxyHost = System.getProperty("http.proxyHost");
		if( proxyHost!=null ) {
			
			Integer proxyPort = System.getProperty("http.proxyPort")!=null
					? Integer.valueOf(System.getProperty("http.proxyPort"))
						: 8080;
				
			String user = System.getProperty("http.proxyUser");
			String password = System.getProperty("http.proxyPassword");
			
			setProxy((DefaultHttpClient)httpClient,proxyHost,proxyPort,user,password);
		}
	}
	
	@Override 
	public void finalize() {
		this.shutdown();
	}
	
	public void shutdown() {
		httpClient.getConnectionManager().shutdown();
	}
	
	public HttpResponse get(HttpGet httpGet) throws ClientProtocolException, IOException {
		return execute(httpGet);
	}
	
	public HttpResponse get(String url, Hashtable getParams) throws HttpRequestException {
		try {
			HttpGet thisGet = new HttpGet(createUrl(url,getParams));
			return get(thisGet);
		} catch(Exception e) {
			throw new HttpRequestException(e);
		}
	}
	
	public HttpResponse get(String url) throws HttpRequestException {
		try {
			return get(new HttpGet(url));
		} catch(Exception e) {
			throw new HttpRequestException(e);
		}
	}

	public HttpResponse postData(String url, Hashtable postParams) throws HttpRequestException {
		return postData(url,null,postParams);
	}
	
	public HttpResponse postData(String url, Hashtable getParams, Hashtable postParams) throws HttpRequestException {
		try {
			List<NameValuePair> nameValuePairs = createNameValuePairs(postParams);
			
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nameValuePairs,FormConstants.CHAR_ENC_DEFAULT);
	    	//StringEntity entity = new StringEntity(Utils.createParamsString(postParams),FormConstants.CHAR_ENC_DEFAULT);
	    	
			entity.setContentType(FormConstants.CONT_TYPE_DEFAULT);
			
			HttpPost httpPost = new HttpPost(createUrl(url,getParams));
			httpPost.setHeader(FormConstants.CONTENT_TYPE,FormConstants.CONT_TYPE_DEFAULT);
			httpPost.setHeader(FormConstants.USERAGENT,FormConstants.USERAGENT_DEFAULT);
	        httpPost.setEntity(entity);
	    	
	        return execute(httpPost);
		} catch(Exception e) {
			throw new HttpRequestException(e);
		}
	}
	
	public HttpResponse postContent(String url, String content, String contentType) throws HttpRequestException {
		try {
	    	StringEntity stringEntity = new StringEntity(content,FormConstants.CHAR_ENC_DEFAULT);
	    	stringEntity.setContentType(contentType);
	    	
	    	HttpPost httppost = new HttpPost(url);
	    	httppost.setHeader(FormConstants.CONTENT_TYPE,contentType);
	    	httppost.setEntity(stringEntity);
	    	
	    	return execute(httppost);
		} catch(Exception e) {
			throw new HttpRequestException(e);
		}
	}
	
	/*
	 * Protected methods
	 */
	
	@SuppressWarnings("rawtypes")
	protected String createUrl(String url, Map<String,Object> params) throws UnsupportedEncodingException {
		String finalUrl = url;
		if(params!=null) {
			finalUrl = finalUrl + (finalUrl.contains("?")?"&":"?") + Utils.createParamsString(new Hashtable(params));
		}
		return finalUrl;
	}
	
	protected List<NameValuePair> createNameValuePairs(Map<String,Object> params) {
		
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(params.size());
		for( Map.Entry<String,Object> entry : params.entrySet() ) {
			
			if( entry.getValue() instanceof List ) {
				
				List<String> values = (List<String>)entry.getValue();
				for(String value : values) {
					
					nameValuePairs.add(new BasicNameValuePair(entry.getKey()+"[]", value.toString()));
				}
			} else {
				
				nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
			}
		}
		return nameValuePairs;
	}
	
	protected void setProxy(DefaultHttpClient httpclient, String proxyHost, Integer proxyPort, String proxyUser, String proxyPassword) {  
		
		if( proxyUser!=null ) {
			httpclient.getCredentialsProvider().setCredentials(  
					new AuthScope(proxyHost, proxyPort),  
					new UsernamePasswordCredentials(proxyUser, proxyPassword));
		}

		HttpHost proxy = new HttpHost(proxyHost, proxyPort);  
		httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);  
	} 
	
	protected HttpClient getHttpClient() {
		return httpClient;
	}
	
	protected HttpResponse execute(HttpUriRequest request) throws IllegalStateException, IOException {
		org.apache.http.HttpResponse clientResponse = httpClient.execute(request);
		
    	HttpResponseImpl returnResponse = new HttpResponseImpl();
    	
		returnResponse.setResponseBody(clientResponse.getEntity().getContent());
		returnResponse.setContentLength(clientResponse.getEntity().getContentLength());
		returnResponse.setStatusCode(clientResponse.getStatusLine().getStatusCode());
		
		List<HttpHeader> headers = new ArrayList<HttpHeader>();
		for( Header header : clientResponse.getAllHeaders() ) {
			headers.add(new HttpHeader(header.getName(),header.getValue()));
		}
		returnResponse.setHeaders(headers.toArray(new HttpHeader[headers.size()]));
		
    	return returnResponse;
	}
}
