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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import com.openmeap.constants.FormConstants;

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
			httpClient = SSLHelper.getRelaxedSSLVerificationHttpClient();
		} else {
			httpClient = new DefaultHttpClient();
		}
		
		((DefaultHttpClient)httpClient).setCredentialsProvider(HttpRequestExecuterFactory.newDefaultCredentialsProvider());
		
		httpClient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
		httpClient.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, FormConstants.CHAR_ENC_DEFAULT);
		httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
		
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
	
	public HttpResponse postXml(String url, String xmlData) throws ClientProtocolException, IOException {
		
    	StringEntity stringEntity = new StringEntity(xmlData,FormConstants.CHAR_ENC_DEFAULT);
    	stringEntity.setContentType(FormConstants.CONT_TYPE_XML);
    	
    	HttpPost httppost = new HttpPost(url);
    	httppost.setHeader(FormConstants.CONTENT_TYPE,FormConstants.CONT_TYPE_XML);
    	httppost.setEntity(stringEntity);
    	
    	// TODO: figure out how to get "application/soap+xml;charset=UTF-8" working...keeps giving me a "415: Unsupported Media Type"
    	return httpClient.execute(httppost);
	}
	
	public HttpResponse get(HttpGet httpGet) throws ClientProtocolException, IOException {
		return httpClient.execute(httpGet);
	}
	
	public HttpResponse get(String url, Map<String,Object> getParams) throws ClientProtocolException, IOException {
		HttpGet thisGet = new HttpGet(createUrl(url,getParams));
		return get(thisGet);
	}
	
	public HttpResponse get(String url) throws ClientProtocolException, IOException {
		return get(new HttpGet(url));
	}

	public HttpResponse postData(String url, Map<String, Object> postParams) throws ClientProtocolException, IOException {
		return postData(url,null,postParams);
	}
	
	public HttpResponse postData(String url, Map<String,Object> getParams, Map<String, Object> postParams) throws ClientProtocolException, IOException {
		
		List<NameValuePair> nameValuePairs = createNameValuePairs(postParams);
		
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nameValuePairs,FormConstants.CHAR_ENC_DEFAULT);
    	//StringEntity entity = new StringEntity(Utils.createParamsString(postParams),FormConstants.CHAR_ENC_DEFAULT);
    	
		entity.setContentType(FormConstants.CONT_TYPE_DEFAULT);
		
		HttpPost httpPost = new HttpPost(createUrl(url,getParams));
		httpPost.setHeader(FormConstants.CONTENT_TYPE,FormConstants.CONT_TYPE_DEFAULT);
        httpPost.setEntity(entity);
    	
    	return httpClient.execute(httpPost);
	}
	
	/*
	 * Protected methods
	 */
	
	protected String createUrl(String url, Map<String,Object> params) throws UnsupportedEncodingException {
		String finalUrl = url;
		if(params!=null) {
			finalUrl = finalUrl + (finalUrl.contains("?")?"&":"?") + Utils.createParamsString(params);
		}
		return finalUrl;
	}
	
	protected List<NameValuePair> createNameValuePairs(Map<String,Object> params) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(params.size());
		for( Map.Entry<String,Object> entry : params.entrySet() ) {
			nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
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
}
