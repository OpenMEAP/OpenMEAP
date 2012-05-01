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

package com.openmeap.blackberry.javascript;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.script.Scriptable;
import net.rim.device.api.script.ScriptableFunction;

import com.openmeap.thinclient.OmMainActivity;
import com.openmeap.thinclient.OmWebView;
import com.openmeap.thinclient.Preferences;
import com.openmeap.thinclient.javascript.JsApiCore;
import com.openmeap.thinclient.javascript.JsApiCoreImpl;
import com.openmeap.thinclient.update.UpdateHandler;
import com.openmeap.util.Utils;

public class BlackberryJsCoreApi extends Scriptable {

	private JsApiCore jsApi = null;
	private Hashtable functions = new Hashtable();
	
	public BlackberryJsCoreApi(OmMainActivity activity, OmWebView webView, UpdateHandler updateHandler) {
		jsApi = new JsApiCoreImpl(activity, webView, updateHandler);
		setupFunctions();
	}

	public void enumerateFields(Vector v) {
		Enumeration enumKeys = functions.keys();
		while(enumKeys.hasMoreElements()) {
			v.addElement( (String)enumKeys.nextElement() );
		}
	}

	public Object getField(String name) throws Exception {
		return functions.get(name);
	}
	
	private void setupFunctions() {
		functions.put("getOrientation", new ScriptableFunction() {
			public Object invoke(Object thiz, Object[] args) throws Exception {
				return jsApi.getOrientation();
			}
		});
		functions.put("doToast", new ScriptableFunction() {
			public Object invoke(Object thiz, Object[] args) throws Exception {
				if(args.length==2) {
					jsApi.doToast((String)args[0],new Boolean(Utils.parseBoolean((String)args[1])));
				} else {
					jsApi.doToast((String)args[0],Boolean.TRUE);
				}
				return null;
			}
		});
		functions.put("getDeviceType", new ScriptableFunction() {
			public Object invoke(Object thiz, Object[] args) throws Exception {
				return jsApi.getDeviceType();
			}
		});
		functions.put("setTitle", new ScriptableFunction() {
			public Object invoke(Object thiz, Object[] args) throws Exception {
				jsApi.setTitle((String)args[0]);
				return null;
			}
		});
		functions.put("clearCache", new ScriptableFunction() {
			public Object invoke(Object thiz, Object[] args) throws Exception {
				jsApi.clearCache();
				return null;
			}
		});
		functions.put("getPreferences", new ScriptableFunction() {
			public Object invoke(Object thiz, Object[] args) throws Exception {
				Preferences prefs = jsApi.getPreferences((String)args[0]);
				return new PreferencesScriptable(prefs);
			}
		});
		functions.put("isTimeForUpdateCheck", new ScriptableFunction() {
			public Object invoke(Object thiz, Object[] args) throws Exception {
				return jsApi.isTimeForUpdateCheck();
			}
		});
		functions.put("checkForUpdates", new ScriptableFunction() {
			public Object invoke(Object thiz, Object[] args) throws Exception {
				jsApi.checkForUpdates((String)args[0]);
				return null;
			}
		});
		functions.put("performUpdate", new ScriptableFunction() {
			public Object invoke(Object thiz, Object[] args) throws Exception {
				jsApi.performUpdate((String)args[0], (String)args[1]);
				return null;
			}
		});
	}
	
}
