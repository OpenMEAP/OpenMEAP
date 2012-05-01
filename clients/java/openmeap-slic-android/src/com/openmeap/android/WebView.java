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

package com.openmeap.android;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import com.openmeap.protocol.WebServiceException;
import com.openmeap.protocol.dto.UpdateHeader;
import com.openmeap.protocol.json.JsUpdateHeader;
import com.openmeap.thinclient.OmMainActivity;
import com.openmeap.thinclient.OmWebView;

public class WebView extends android.webkit.WebView implements OmWebView {

	private OmMainActivity activity; 
	
	Map<String,Object> jsInterfaces = new HashMap<String,Object>();
	
	public WebView(OmMainActivity activity, Context context) {
		super(context);
		this.activity = activity;
	}
	
	public void addJavascriptInterface( Object obj, String interfaceName ) {
		jsInterfaces.put(interfaceName, obj);
		super.addJavascriptInterface(obj, interfaceName);
	}
	
	public Object getJavascriptInterface(String name) {
		return jsInterfaces.get(name);
	}
	
	public void runJavascript(String stream) {
		String url = "javascript:"+stream;
		loadUrl(url);
	}
	
	public void setUpdateHeader(UpdateHeader update, WebServiceException err, Long bytesFree) {
		String js = "void(0);";
		if( err!=null ) {
			js = String.format(UPDATE_ERROR, WebServiceException.toJSON(err));
		} else if(update!=null) {
			js = String.format(UPDATE_NOT_NULL, new JsUpdateHeader(update,bytesFree).toString());
		} else {
			js = UPDATE_NULL;
		}
		runJavascript(js);
	}

	public void performOnResume() {
		activity.getUpdateHandler().clearInterruptFlag();
		// TODO: call the js callback
	}
	
	public void performOnPause() {
		activity.getUpdateHandler().interruptRunningUpdate();
		// TODO: call the js callback
	}
	
	public void executeJavascriptFunction(String callBack, String... arguments) {
		Integer random = new Double(Math.random()*10000.0).intValue();
    	String func = "openMEAP_anon"+random;
		String str = "var "+func+"="+callBack+"; "+func+"(";
		int cnt = arguments.length;
		for( int i=0; i<cnt; i++ ) {
			str += i!=0 ? ",":"";
			str += arguments[i];
		}
		str += "); "+func+"=undefined;";
		runJavascript(str);
	}
}
