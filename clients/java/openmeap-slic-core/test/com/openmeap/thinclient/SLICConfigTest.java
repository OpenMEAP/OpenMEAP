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

import java.util.Properties;

import junit.framework.Assert;
import junit.framework.TestCase;

public class SLICConfigTest extends TestCase {
	public void testGetInstance() {
		
		Preferences prefs = new PreferencesTestImpl();
		prefs.put("com.openmeap.slic.deviceUuid", "deviceUuid");
		
		Properties props = new Properties();
		props.put("com.openmeap.slic.appMgmtServiceUrl","serviceUrl");
		props.put("com.openmeap.slic.appName", "appName");
		props.put("com.openmeap.slic.appVersion", "appVersion");
		prefs.put("com.openmeap.slic.deviceType", "deviceType");
		
		SLICConfig slicConfig = new SLICConfig(prefs,props){
					public String getAssetsBaseUrl() {
						return null;
					}
				};
		
		Assert.assertTrue(slicConfig.getDeviceUuid().compareTo("deviceUuid")==0);
		Assert.assertTrue(slicConfig.getAppMgmtServiceUrl().compareTo("serviceUrl")==0);
		Assert.assertTrue(slicConfig.getApplicationName().compareTo("appName")==0);
		Assert.assertTrue(slicConfig.getApplicationVersion().compareTo("appVersion")==0);
		Assert.assertTrue(slicConfig.getDeviceType().compareTo("deviceType")==0);
		
		/////////
		// Verify that the device uuid is generated and stays the same from call to call
		prefs.put("com.openmeap.slic.deviceUuid", null);
		String uuid = slicConfig.getDeviceUuid();
		Assert.assertTrue(uuid.compareTo("deviceUuid")!=0 
			&& uuid.compareTo(slicConfig.getDeviceUuid())==0
			&& slicConfig.getDeviceUuid().length()==40 );
	}
}
