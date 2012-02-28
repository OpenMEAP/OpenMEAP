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
import java.net.URLEncoder;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

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
	
	@Override public void finalize() {
		this.shutdown();
	}
	
	public void shutdown() {
		httpClient.getConnectionManager().shutdown();
	}
	
	public HttpResponse postXml(String url, String xmlData) throws ClientProtocolException, IOException {
		
    	StringEntity se = new StringEntity(xmlData,HTTP.UTF_8);
    	se.setContentType("text/xml");
    	
    	HttpPost httppost = new HttpPost(url);
    	httppost.setHeader("Content-type","text/xml");
    	httppost.setEntity(se);
    	
    	// TODO: figure out how to get "application/soap+xml;charset=UTF-8" working...keeps giving me a "415: Unsupported Media Type"
    	return httpClient.execute(httppost);
	}
	
	public HttpResponse get(HttpGet httpGet) throws ClientProtocolException, IOException {
		return httpClient.execute(httpGet);
	}
	
	public HttpResponse get(String url, Map<String,Object> params) throws ClientProtocolException, IOException {
		HttpGet thisGet = new HttpGet(url);
		HttpParams httpParams = new BasicHttpParams();
		for( Map.Entry<String,Object> ent : params.entrySet() ) {
			httpParams.setParameter(ent.getKey(),ent.getValue());
		}
		thisGet.setParams(httpParams);
		return this.get(thisGet);
	}
	
	public HttpResponse get(String url) throws ClientProtocolException, IOException {
		return get(new HttpGet(url));
	}

	public HttpResponse postData(String url, Map<String, Object> params) throws ClientProtocolException, IOException {
		
		HttpPost post = new HttpPost( url );
		
		StringBuilder postData = new StringBuilder();
		Boolean firstPass = true;
		for( Map.Entry<String,Object> ent : params.entrySet() ) {
			if( !firstPass ) {
				postData.append("&");
			} else firstPass=false;
			postData.append(URLEncoder.encode(ent.getKey(), "UTF-8"));
			postData.append("=");
			postData.append(URLEncoder.encode(ent.getValue().toString(), "UTF-8"));
		}
		
    	StringEntity se = new StringEntity(postData.toString(),HTTP.UTF_8);
    	se.setContentType("application/x-www-form-urlencoded");
    	
    	post.setHeader("Content-type","application/x-www-form-urlencoded");
    	post.setEntity(se); 
    	
    	return httpClient.execute(post);
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
