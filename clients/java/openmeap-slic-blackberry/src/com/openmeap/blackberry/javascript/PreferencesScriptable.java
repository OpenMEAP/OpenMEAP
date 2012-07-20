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

import com.openmeap.thinclient.Preferences;

import net.rim.device.api.script.Scriptable;
import net.rim.device.api.script.ScriptableFunction;

public class PreferencesScriptable extends Scriptable {
	
	private Preferences prefs;
	private Hashtable functions = new Hashtable();
	
	public PreferencesScriptable(Preferences prefs) {
		this.prefs = prefs;
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

	public void setupFunctions() {
		functions.put("put", new ScriptableFunction() {
			public Object invoke(Object thiz, Object[] args) throws Exception {
				return prefs.put((String)args[0], (String)args[1]);
			}
		});
		functions.put("get", new ScriptableFunction() {
			public Object invoke(Object thiz, Object[] args) throws Exception {
				return prefs.get((String)args[0]);
			}
		});
		functions.put("remove", new ScriptableFunction() {
			public Object invoke(Object thiz, Object[] args) throws Exception {
				return prefs.remove((String)args[0]);
			}
		});
		functions.put("clear", new ScriptableFunction() {
			public Object invoke(Object thiz, Object[] args) throws Exception {
				return prefs.clear();
			}
		});
	}

}
