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

package com.openmeap.thinclient;

import java.util.Random;

import com.openmeap.protocol.WebServiceException;
import com.openmeap.protocol.dto.UpdateHeader;
import com.openmeap.protocol.json.JsUpdateHeader;
import com.openmeap.util.StringUtils;

final public class OmWebViewHelper {
	
	private static final Random randoms = new Random();
	
	private OmWebViewHelper() {
	}
	
	final static public void setUpdateHeader(OmWebView webView, UpdateHeader update, WebServiceException err, Long bytesFree) {
		String js = "void(0);";
		if( err!=null ) {
			js = StringUtils.replaceAll(OmWebView.UPDATE_ERROR, "%errorCode", err.getType().toString());
			js = StringUtils.replaceAll(js, "%errorMessage", 
					StringUtils.replaceAll( err.getMessage()!=null?err.getMessage():"", "\"", "" ));
		} else if(update!=null) {
			js = StringUtils.replaceAll(OmWebView.UPDATE_NOT_NULL, "%s", new JsUpdateHeader(update,bytesFree).toString());
		} else {
			js = OmWebView.UPDATE_NULL;
		}
		webView.runJavascript(js);
	}
	
	final static public void executeJavascriptFunction(OmWebView webView, String callBack, String[] arguments) {
		
		int random = new Double(randoms.nextDouble()*10000.0).intValue();
		String func = "openMEAP_anon"+random;
		String str = "var "+func+"="+callBack+"; "+func+"(";
		int cnt = arguments.length;
		for( int i=0; i<cnt; i++ ) {
			str += i!=0 ? ",":"";
			str += arguments[i];
		}
		str += "); "+func+"=undefined;";
		webView.runJavascript(str);
	}
}
