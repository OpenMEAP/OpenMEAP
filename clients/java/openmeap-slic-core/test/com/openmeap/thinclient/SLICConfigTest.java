package com.openmeap.thinclient;

import junit.framework.TestCase;
import junit.framework.Assert;

import com.openmeap.thinclient.Preferences;
import com.openmeap.thinclient.SLICConfig;

import java.util.*;

public class SLICConfigTest extends TestCase {
	public void testGetInstance() {
		
		Preferences prefs = new PreferencesTestImpl();
		prefs.put("com.openmeap.slic.deviceUuid", "deviceUuid");
		
		Properties props = new Properties();
		props.put("com.openmeap.slic.appMgmtServiceUrl","serviceUrl");
		props.put("com.openmeap.slic.appName", "appName");
		props.put("com.openmeap.slic.appVersion", "appVersion");
		prefs.put("com.openmeap.slic.deviceType", "deviceType");
		
		SLICConfig inst = new SLICConfig(prefs,props);
		
		Assert.assertTrue(inst.getDeviceUuid().compareTo("deviceUuid")==0);
		Assert.assertTrue(inst.getAppMgmtServiceUrl().compareTo("serviceUrl")==0);
		Assert.assertTrue(inst.getApplicationName().compareTo("appName")==0);
		Assert.assertTrue(inst.getApplicationVersion().compareTo("appVersion")==0);
		Assert.assertTrue(inst.getDeviceType().compareTo("deviceType")==0);
		
		/////////
		// Verify that the device uuid is generated and stays the same from call to call
		prefs.put("com.openmeap.slic.deviceUuid", null);
		Assert.assertTrue(inst.getDeviceUuid().compareTo("deviceUuid")!=0 
			&& inst.getDeviceUuid().compareTo( inst.getDeviceUuid() )==0
			&& inst.getDeviceUuid().length()==36 );
	}
}
