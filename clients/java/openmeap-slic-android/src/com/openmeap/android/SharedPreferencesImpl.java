/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011 OpenMEAP, Inc.                                        #
 #    Credits to Jonathan Schang & Robert Thacher                              #
 #                                                                             #
 #    Released under the GPLv3                                                 #
 #                                                                             #
 #    OpenMEAP is free software: you can redistribute it and/or modify         #
 #    it under the terms of the GNU General Public License as published by     #
 #    the Free Software Foundation, either version 3 of the License, or        #
 #    (at your option) any later version.                                      #
 #                                                                             #
 #    OpenMEAP is distributed in the hope that it will be useful,              #
 #    but WITHOUT ANY WARRANTY; without even the implied warranty of           #
 #    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the            #
 #    GNU General Public License for more details.                             #
 #                                                                             #
 #    You should have received a copy of the GNU General Public License        #
 #    along with OpenMEAP.  If not, see <http://www.gnu.org/licenses/>.        #
 #                                                                             #
 ###############################################################################
 */

package com.openmeap.android;

import android.content.*;
import com.openmeap.thinclient.*;

public class SharedPreferencesImpl implements Preferences {
	SharedPreferences preferences = null;
	public SharedPreferencesImpl(SharedPreferences prefs) {
		preferences=prefs;
	}
	public Boolean put(String key, String value) {
		SharedPreferences.Editor editor = preferences.edit();
		return editor.putString(key, value).commit();
	}
	public String get(String key) {
		return preferences.getString(key,null);
	}
	public Boolean remove(String key) {
		SharedPreferences.Editor editor = preferences.edit();
		return editor.remove(key).commit();
	}
	public Boolean clear() {
		SharedPreferences.Editor editor = preferences.edit();
		return editor.clear().commit();
	}
}
