package com.openmeap.thinclient;

import java.lang.reflect.Constructor;

import com.openmeap.protocol.ApplicationManagementService;
import com.openmeap.util.HttpRequestExecuter;
import com.openmeap.util.HttpRequestExecuterFactory;

public class AppMgmtClientFactory {
	
	static private Class<? extends ApplicationManagementService> defaultClient = RESTAppMgmtClient.class;
	
	static public void setDefaultType(Class<? extends ApplicationManagementService> defaultNew) {
		defaultClient = defaultNew;		
	}
	static public Class<? extends ApplicationManagementService> getDefaultType() {
		return defaultClient;
	}
	
	static public ApplicationManagementService newDefault(String serviceUrl) {
		try {
			Constructor constructor = defaultClient.getConstructor(String.class, HttpRequestExecuter.class);
			return (ApplicationManagementService)constructor.newInstance(serviceUrl, HttpRequestExecuterFactory.newDefault());
		} catch( Exception e ) {
			throw new RuntimeException(e);
		}
	}
}
