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

import java.util.Hashtable;

public interface HttpRequestExecuter {
	
	public final static String SSL_PEER_NOVERIFY_PROPERTY = "com.openmeap.sslPeerNoVerify";
	
	public HttpResponse postContent(String url, String content, String contentType)  throws HttpRequestException;
	public HttpResponse postData(String url, Hashtable params) throws HttpRequestException;
	public HttpResponse postData(String url, Hashtable urlParams, Hashtable postParams) throws HttpRequestException;
	public HttpResponse get(String url) throws HttpRequestException;
	public HttpResponse get(String url, Hashtable params) throws HttpRequestException;
	public void shutdown();
}
