/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2013 OpenMEAP, Inc.                                   #
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

package com.openmeap.thinclient;

import java.io.IOException;
import java.util.Hashtable;

import org.xml.sax.InputSource;

import com.openmeap.constants.UrlParamConstants;
import com.openmeap.http.HttpRequestException;
import com.openmeap.http.HttpRequestExecuter;
import com.openmeap.http.HttpResponse;
import com.openmeap.json.JSONObjectBuilder;
import com.openmeap.protocol.ApplicationManagementService;
import com.openmeap.protocol.WebServiceException;
import com.openmeap.protocol.dto.ConnectionOpenRequest;
import com.openmeap.protocol.dto.ConnectionOpenResponse;
import com.openmeap.protocol.dto.Result;
import com.openmeap.protocol.dto.UpdateNotification;
import com.openmeap.thirdparty.org.json.me.JSONException;
import com.openmeap.thirdparty.org.json.me.JSONObject;
import com.openmeap.util.StringUtils;
import com.openmeap.util.Utils;

public class RESTAppMgmtClient implements ApplicationManagementService {
	
	private String serviceUrl = null;

	private HttpRequestExecuter requester = null;
	
	public RESTAppMgmtClient() {
	}
	
	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}
	
	public void setHttpRequestExecuter(HttpRequestExecuter executer) {
		this.requester = executer;
	}
	
	public ConnectionOpenResponse connectionOpen(ConnectionOpenRequest request) throws WebServiceException {
		
		ConnectionOpenResponse response = null;
		
		Hashtable postData = new Hashtable();
		postData.put(UrlParamConstants.ACTION, "connection-open-request");
		postData.put(UrlParamConstants.DEVICE_UUID, request.getApplication().getInstallation().getUuid());
		postData.put(UrlParamConstants.APP_NAME, request.getApplication().getName());
		postData.put(UrlParamConstants.APP_VERSION, request.getApplication().getVersionId());
		postData.put(UrlParamConstants.APPARCH_HASH, StringUtils.orEmpty(request.getApplication().getHashValue()));
		postData.put(UrlParamConstants.SLIC_VERSION, request.getSlic().getVersionId());
		
		HttpResponse httpResponse = null;
		InputSource responseInputSource = null;
		String responseText = null;
		try {
			httpResponse = requester.postData(serviceUrl, postData);
			if( httpResponse.getStatusCode()!=200 ) {
				throw new WebServiceException(WebServiceException.TypeEnum.CLIENT,"Posting to the service resulted in a "+httpResponse.getStatusCode()+" status code");
			}
			responseText = Utils.readInputStream(httpResponse.getResponseBody(), "UTF-8");
		} catch (Exception e) {
			throw new WebServiceException(WebServiceException.TypeEnum.CLIENT,
					StringUtils.isEmpty(e.getMessage())
					? e.getMessage()
					: "There's a problem connecting. Check your network or try again later",e);
		}
		
		// now we parse the response into a ConnectionOpenResponse object
		if( responseText!=null ) {
			Result result = new Result();
			JSONObjectBuilder builder = new JSONObjectBuilder();
			try {
				result = (Result)builder.fromJSON(new JSONObject(responseText), result);
				if( result.getError()!=null ) {
					throw new WebServiceException(WebServiceException.TypeEnum.fromValue(result.getError().getCode().value()),result.getError().getMessage());
				}
			} catch( JSONException e ) {
				throw new WebServiceException(WebServiceException.TypeEnum.CLIENT,"Unable to parse service response content.");
			}
			response = result.getConnectionOpenResponse();
		}
		
		return response;
	}

	public void notifyUpdateResult(UpdateNotification notification) {
	}
}
