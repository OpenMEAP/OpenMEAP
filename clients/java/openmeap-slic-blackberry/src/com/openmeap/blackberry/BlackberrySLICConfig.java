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

package com.openmeap.blackberry;

import java.util.Hashtable;

import com.openmeap.thinclient.Preferences;
import com.openmeap.thinclient.SLICConfig;

public class BlackberrySLICConfig extends SLICConfig {

	public BlackberrySLICConfig(Preferences preferences, Hashtable properties) {
		super(preferences, properties);
	}

	public String getAssetsBaseUrl() {
		// storage location will be null until the first successful update
    	if( shouldUseAssetsOrSdCard().booleanValue() ) {
   			return AssetsInputConnection.ASSETS_PREFIX+getPackagedAppRoot();
    	} else {
    		return getStorageLocation();
    	}
	}
	
	public Preferences getPreferences() {
		return preferences;
	}
}
