/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2015 OpenMEAP, Inc.                                   #
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

import com.openmeap.http.HttpRequestExecuterFactory;
import com.openmeap.protocol.ApplicationManagementService;
import com.openmeap.util.GenericRuntimeException;

public class AppMgmtClientFactory {
	
	static private Class defaultClient = RESTAppMgmtClient.class;
	
	static public void setDefaultType(Class defaultNew) {
		if( !ApplicationManagementService.class.isAssignableFrom(defaultNew) ) {
			throw new RuntimeException("setDefaultType only accepts ApplicationManagementService subclasses/implementors");
		}
		defaultClient = defaultNew;		
	}
	static public Class getDefaultType() {
		return defaultClient;
	}
	
	static public ApplicationManagementService newDefault(String serviceUrl) {
		try {
			ApplicationManagementService service = (ApplicationManagementService)defaultClient.newInstance();
			service.setServiceUrl(serviceUrl);
			service.setHttpRequestExecuter(HttpRequestExecuterFactory.newDefault());
			return service;
		} catch( Exception e ) {
			throw new GenericRuntimeException(e);
		}
	}
}
