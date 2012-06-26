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

package com.openmeap.thinclient;

import com.openmeap.protocol.WebServiceException;
import com.openmeap.protocol.dto.UpdateHeader;

public interface OmWebView {
	
	static public String INSURE_OPENMEAP = "if(typeof OpenMEAP=='undefined') { OpenMEAP={data:{},config:{},persist:{cookie:{}},updates:{}}; };";
	static public String UPDATE_ERROR    = "if(OpenMEAP.updates.onCheckError!='undefined'){OpenMEAP.updates.onCheckError({code:\"%errorCode\",message:\"%errorMessage\"});}";
	static public String UPDATE_NOT_NULL = "if(OpenMEAP.updates.onUpdate!='undefined'){OpenMEAP.updates.onUpdate(%s);}";
	static public String UPDATE_NULL     = "if(OpenMEAP.updates.onNoUpdate!='undefined'){OpenMEAP.updates.onNoUpdate();}";
	
	public void clearCache(boolean arg0);
	
	public void runJavascript(String stream);
	
	public void setUpdateHeader(UpdateHeader update, WebServiceException err, Long bytesFree);

	public void performOnResume();
	
	public void performOnPause();
	
	/**
	 * A convenience method for executing a javascript callback function
	 * passed in from the customer code.
	 * 
	 * Note: the javascript bridge in android doesn't decode a javascript
	 * function to something usable, so it must be converted to a string
	 * client-side.
	 */
	public void executeJavascriptFunction(String callBack, String[] arguments);

	public void loadDataWithBaseURL(String baseUrl, String pageContent, String mimeType, String sourceEncoding, String historyUrl);

	public void clearView();
	
}
